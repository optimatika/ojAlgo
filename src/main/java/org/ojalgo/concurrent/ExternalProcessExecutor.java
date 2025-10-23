/*
 * Copyright 1997-2025 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.concurrent;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.CRC32;

import org.ojalgo.machine.JavaType;

/**
 * Execute submitted tasks/methods in external JVM processes with arbitrary {@link Serializable}
 * arguments/return. Provides hard cancellation/timeout via process kill. Each executor thread owns a
 * persistent child process kept alive across tasks until the owner thread is interrupted or the process is
 * killed due to failure/timeout. This enables reusing JVM warm state for a sequence of tasks.
 */
public final class ExternalProcessExecutor {

    private static final class ProcessTask<T> implements Future<T>, Runnable {

        private static final ThreadLocal<WorkerChannel> WORKER = new ThreadLocal<>();

        private static WorkerChannel ensureWorker(final ProcessOptions opts) throws Exception {
            WorkerChannel wc = WORKER.get();
            if (wc == null || !wc.isAlive() || !ProcessTask.sameOptions(wc.options(), opts)) {
                if (wc != null) {
                    try {
                        wc.kill();
                    } catch (Throwable ignore) {
                    }
                }
                wc = WorkerChannel.start(opts);
                WORKER.set(wc);
            }
            return wc;
        }

        private static boolean sameOptions(final ProcessOptions opt1, final ProcessOptions opt2) {
            if (opt1 == opt2) {
                return true;
            }
            if (opt1 == null || opt2 == null) {
                return false;
            }
            return Objects.equals(opt1.classpath, opt2.classpath) && opt1.enableNativeAccessAllUnnamed == opt2.enableNativeAccessAllUnnamed
                    && Objects.equals(opt1.env, opt2.env) && Objects.equals(opt1.jvmArgs, opt2.jvmArgs)
                    && Objects.equals(opt1.systemProperties, opt2.systemProperties) && Objects.equals(opt1.xmx, opt2.xmx);
        }

        private final Object[] myArguments;

        private final AtomicBoolean myCancelled = new AtomicBoolean(false);
        private final AtomicBoolean myDone = new AtomicBoolean(false);
        private volatile Throwable myError;
        private final Object myLock = new Object();
        private final MethodDescriptor myMethod;
        private final ProcessOptions myOptions;
        private volatile T myResult;
        private volatile WorkerChannel myWorker;

        ProcessTask(final MethodDescriptor spec, final Object[] args, final ProcessOptions opts) {
            myMethod = spec;
            myArguments = args;
            myOptions = opts;
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            if (myDone.get()) {
                return false;
            }
            myCancelled.set(true);
            WorkerChannel wc = myWorker;
            if (wc != null) {
                try {
                    wc.kill();
                } catch (Throwable ignore) {
                }
            }
            synchronized (myLock) {
                myDone.set(true);
                myLock.notifyAll();
            }
            return true;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            synchronized (myLock) {
                while (!myDone.get()) {
                    myLock.wait();
                }
            }
            if (myCancelled.get()) {
                throw new CancellationException();
            }
            if (myError != null) {
                throw new ExecutionException(myError);
            }
            return myResult;
        }

        @Override
        public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            long timeoutNanos = Math.max(1L, unit.toNanos(timeout));
            long deadline = System.nanoTime() + timeoutNanos;
            synchronized (myLock) {
                while (!myDone.get()) {
                    long remaining = deadline - System.nanoTime();
                    if (remaining <= 0L) {
                        break;
                    }
                    long millis = remaining / 1_000_000L;
                    int nanos = (int) (remaining - millis * 1_000_000L);
                    myLock.wait(millis, nanos);
                }
            }
            if (!myDone.get()) {
                throw new TimeoutException();
            }
            if (myCancelled.get()) {
                throw new CancellationException();
            }
            if (myError != null) {
                throw new ExecutionException(myError);
            }
            return myResult;
        }

        @Override
        public boolean isCancelled() {
            return myCancelled.get();
        }

        @Override
        public boolean isDone() {
            return myDone.get();
        }

        @Override
        public void run() {
            try {
                if (myCancelled.get()) {
                    return;
                }
                ProcessOptions procOpts = myOptions != null ? myOptions : ProcessOptions.DEFAULT;
                WorkerChannel wc = ProcessTask.ensureWorker(procOpts);
                myWorker = wc;

                ProcessRequest req = new ProcessRequest(myMethod, myArguments);
                ProcessResponse resp = wc.transact(req, procOpts.timeout);

                if (resp.error != null) {
                    myError = resp.error;
                } else {
                    Object value = resp.result;
                    if (value == null) {
                        myResult = null;
                    } else {
                        myResult = (T) JavaType.box(myMethod.getMethod().getReturnType()).cast(value);
                    }
                }

            } catch (TimeoutException te) {
                WorkerChannel wc = myWorker;
                if (wc != null) {
                    try {
                        wc.kill();
                    } catch (Throwable ignore) {
                    }
                }
                myError = te;
            } catch (Throwable t) {
                WorkerChannel wc = myWorker;
                String msg = null;
                if (wc != null) {
                    try {
                        msg = wc.getCapturedStderr();
                    } catch (Throwable ignore) {
                    }
                    try {
                        wc.kill();
                    } catch (Throwable ignore) {
                    }
                }
                if (msg != null && !msg.isEmpty()) {
                    myError = new IOException(t.getMessage() + " | child-stderr: " + msg, t);
                } else {
                    myError = t;
                }
            } finally {
                synchronized (myLock) {
                    myDone.set(true);
                    myLock.notifyAll();
                }
            }
        }

        ProcessTask<T> start(final ExecutorService executor) {
            executor.submit(this);
            return this;
        }
    }

    /**
     * Ring buffer OutputStream that retains only the last N bytes written. toString() decodes using the
     * platform default charset (consistent with previous ByteArrayOutputStream#toString()).
     */
    private static final class RingBufferOutput extends OutputStream {
        private final byte[] buf;
        private int pos = 0;
        private int size = 0;

        RingBufferOutput(final int capacity) {
            buf = new byte[Math.max(1, capacity)];
        }

        @Override
        public void close() throws IOException {
            // no-op
        }

        @Override
        public void flush() throws IOException {
            // no-op
        }

        @Override
        public synchronized String toString() {
            if (size == 0) {
                return "";
            }
            byte[] out = new byte[size];
            int start = (pos - size + buf.length) % buf.length;
            int first = Math.min(size, buf.length - start);
            System.arraycopy(buf, start, out, 0, first);
            if (first < size) {
                System.arraycopy(buf, 0, out, first, size - first);
            }
            return new String(out);
        }

        @Override
        public synchronized void write(final byte[] b, final int off, final int len) throws IOException {
            int remaining = len;
            int idx = off;
            while (remaining > 0) {
                int chunk = Math.min(remaining, buf.length - pos);
                System.arraycopy(b, idx, buf, pos, chunk);
                pos = (pos + chunk) % buf.length;
                idx += chunk;
                remaining -= chunk;
                size = Math.min(buf.length, size + chunk);
            }
        }

        @Override
        public synchronized void write(final int b) throws IOException {
            buf[pos] = (byte) b;
            pos = (pos + 1) % buf.length;
            if (size < buf.length) {
                size++;
            }
        }
    }

    /**
     * A per-thread, persistent channel to a child JVM. Not thread-safe; each instance must only be used from
     * its owning thread. Kills and restarts the underlying process on demand.
     */
    private static final class WorkerChannel {

        private static final int STDERR_MAX_BYTES = 64 * 1024;

        private static void destroyProcessTree(final Process proc) {
            try {
                ProcessHandle handle = proc.toHandle();
                handle.descendants().forEach(ph -> {
                    try {
                        ph.destroyForcibly();
                    } catch (Throwable ignore) {
                    }
                });
                handle.destroyForcibly();
            } catch (Throwable ignore) {
                try {
                    proc.destroyForcibly();
                } catch (Throwable ignore2) {
                }
            }
        }

        static WorkerChannel start(final ProcessOptions options) throws Exception {
            List<String> cmd = new ArrayList<>();
            String javaBin = System.getProperty("java.home") + "/bin/java";
            cmd.add(javaBin);
            if (options.xmx != null) {
                cmd.add("-Xmx" + options.xmx);
            }
            if (options.enableNativeAccessAllUnnamed) {
                cmd.add("--enable-native-access=ALL-UNNAMED");
            }
            try {
                String parentLibPath = System.getProperty("java.library.path");
                if (parentLibPath != null && (options.systemProperties == null || !options.systemProperties.containsKey("java.library.path"))) {
                    cmd.add("-Djava.library.path=" + parentLibPath);
                }
            } catch (Throwable ignore) {
                // ignore
            }
            if (options.systemProperties != null) {
                for (Map.Entry<String, String> e : options.systemProperties.entrySet()) {
                    cmd.add("-D" + e.getKey() + "=" + e.getValue());
                }
            }
            if (options.jvmArgs != null && !options.jvmArgs.isEmpty()) {
                cmd.addAll(options.jvmArgs);
            }
            cmd.add("-cp");
            String testCp = System.getProperty("surefire.test.class.path");
            String mainCp = System.getProperty("java.class.path");
            String pathSep = File.pathSeparator;
            String effectiveCp;
            if (options.classpath != null && !options.classpath.isEmpty()) {
                effectiveCp = options.classpath;
            } else if (testCp != null && !testCp.isEmpty()) {
                effectiveCp = testCp;
            } else if (mainCp != null && !mainCp.isEmpty()) {
                effectiveCp = mainCp;
            } else {
                effectiveCp = null;
            }
            if ((testCp == null || testCp.isEmpty())) {
                try {
                    String userDir = System.getProperty("user.dir");
                    List<String> candidates = new ArrayList<>(8);
                    candidates.add(userDir + File.separator + "target" + File.separator + "test-classes");
                    candidates.add(userDir + File.separator + "target" + File.separator + "classes");
                    candidates.add(userDir + File.separator + "build" + File.separator + "classes" + File.separator + "java" + File.separator + "test");
                    candidates.add(userDir + File.separator + "build" + File.separator + "classes" + File.separator + "java" + File.separator + "main");
                    StringBuilder sb = new StringBuilder(effectiveCp == null ? 64 : effectiveCp.length() + 128);
                    boolean any = false;
                    for (String c : candidates) {
                        File f = new File(c);
                        if (f.isDirectory() && f.exists()) {
                            if (!any) {
                                any = true;
                            }
                            sb.append(f.getAbsolutePath()).append(pathSep);
                        }
                    }
                    if (effectiveCp != null && !effectiveCp.isEmpty()) {
                        sb.append(effectiveCp);
                    }
                    String alt = sb.toString();
                    if (any) {
                        effectiveCp = alt;
                    }
                } catch (Throwable ignore) {
                }
            }
            try {
                java.net.URL loc = ProcessWorker.class.getProtectionDomain().getCodeSource().getLocation();
                if (loc != null) {
                    String workerPath = new java.io.File(loc.toURI()).getAbsolutePath();
                    if (effectiveCp == null || effectiveCp.isEmpty()) {
                        effectiveCp = workerPath;
                    } else if (!effectiveCp.contains(workerPath)) {
                        effectiveCp = workerPath + File.pathSeparator + effectiveCp;
                    }
                }
            } catch (Throwable ignore) {
            }
            cmd.add(effectiveCp);
            cmd.add("org.ojalgo.concurrent.ProcessWorker");

            ProcessBuilder pb = new ProcessBuilder(cmd);
            if (options.env != null) {
                pb.environment().putAll(options.env);
            }

            final Process proc = pb.start();
            Thread owner = Thread.currentThread();
            if (owner instanceof ProcessAwareThread) {
                ((ProcessAwareThread) owner).setProcess(proc);
            }

            final OutputStream toChild = proc.getOutputStream();
            final InputStream fromChild = proc.getInputStream();
            final InputStream childErr = proc.getErrorStream();

            final RingBufferOutput errBuf = new RingBufferOutput(STDERR_MAX_BYTES);
            final OutputStream capped = errBuf;
            Thread errDrainer = DaemonPoolExecutor.newThreadFactory("ojAlgo-proc-stderr").newThread(() -> {
                try {
                    childErr.transferTo(capped);
                } catch (IOException ignore) {
                } finally {
                    try {
                        capped.close();
                    } catch (IOException ignore) {
                    }
                    try {
                        childErr.close();
                    } catch (IOException ignore) {
                    }
                }
            });
            errDrainer.start();

            return new WorkerChannel(effectiveCp, options, owner, proc, toChild, fromChild, childErr, errDrainer, errBuf);
        }

        private InputStream myChildErr;
        private final String myEffectiveClasspath;
        private final RingBufferOutput myErrBuffer;
        private Thread myErrDrainer;
        private InputStream myFromChild;
        private final ProcessOptions myOptions;
        private final Thread myOwnerThread;

        private Process myProcess;

        private OutputStream myToChild;

        private WorkerChannel(final String effectiveCp, final ProcessOptions options, final Thread owner, final Process proc, final OutputStream toChild,
                final InputStream fromChild, final InputStream err, final Thread errDrainer, final RingBufferOutput errBuffer) {
            myEffectiveClasspath = effectiveCp;
            myOptions = options;
            myOwnerThread = owner;
            myProcess = proc;
            myToChild = toChild;
            myFromChild = fromChild;
            myChildErr = err;
            myErrDrainer = errDrainer;
            myErrBuffer = errBuffer;
        }

        String getCapturedStderr() {
            synchronized (myErrBuffer) {
                return myErrBuffer.toString();
            }
        }

        boolean isAlive() {
            return myProcess != null && myProcess.isAlive();
        }

        void kill() {
            if (myProcess != null) {
                try {
                    WorkerChannel.destroyProcessTree(myProcess);
                } catch (Throwable ignore) {
                }
            }
            try {
                if (myToChild != null) {
                    myToChild.close();
                }
            } catch (IOException ignore) {
            }
            try {
                if (myFromChild != null) {
                    myFromChild.close();
                }
            } catch (IOException ignore) {
            }
            try {
                if (myChildErr != null) {
                    myChildErr.close();
                }
            } catch (IOException ignore) {
            }
            if (myErrDrainer != null) {
                try {
                    myErrDrainer.join(100L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
            if (myOwnerThread instanceof ProcessAwareThread) {
                ((ProcessAwareThread) myOwnerThread).setProcess(null);
            }
            myProcess = null;
            myToChild = null;
            myFromChild = null;
            myChildErr = null;
            myErrDrainer = null;
        }

        ProcessOptions options() {
            return myOptions;
        }

        ProcessResponse transact(final ProcessRequest req, final Duration timeout) throws Exception {
            if (!this.isAlive()) {
                throw new IOException("Worker process not alive");
            }
            // Send request
            IPC.writeFrame(myToChild, req);
            myToChild.flush();

            final AtomicBoolean done = new AtomicBoolean(false);
            final AtomicBoolean timedOut = new AtomicBoolean(false);
            Thread watchdog = null;
            if (timeout != null && !timeout.isZero() && timeout.compareTo(Duration.ZERO) > 0) {
                long millis = Math.max(1L, timeout.toMillis());
                watchdog = DaemonPoolExecutor.newThreadFactory("ojAlgo-proc-watchdog").newThread(() -> {
                    try {
                        Thread.sleep(millis);
                        if (!done.get()) {
                            timedOut.set(true);
                            WorkerChannel.destroyProcessTree(myProcess);
                        }
                    } catch (InterruptedException ignore) {
                        Thread.currentThread().interrupt();
                    }
                });
                watchdog.start();
            }

            ProcessResponse resp = null;
            Throwable readError = null;
            try {
                resp = IPC.readFrame(myFromChild, ProcessResponse.class);
            } catch (Throwable t) {
                readError = t;
            } finally {
                done.set(true);
            }

            if (watchdog != null) {
                try {
                    watchdog.join(50L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }

            if (timedOut.get()) {
                throw new TimeoutException("External process request timed out");
            }
            if (readError != null) {
                if (readError instanceof EOFException) {
                    throw new IOException("Child process terminated unexpectedly");
                }
                if (readError instanceof ClassNotFoundException) {
                    throw (ClassNotFoundException) readError;
                }
                if (readError instanceof IOException) {
                    throw (IOException) readError;
                }
                throw new IOException(readError);
            }
            return resp;
        }
    }

    /**
     * Inter-Process Communication
     * <p>
     * Frame format: [MAGIC:8][VER:1][LEN:4][PAYLOAD:LEN][CRC32:4]
     */
    static abstract class IPC {

        private static final long MAGIC = 0x6F4A414C474F4950L; // ASCII-ish: 'oJALGOIP'
        private static final int MAX_FRAME_SIZE = 64 * 1024 * 1024; // 64 MiB safety cap
        private static final int VERSION = 1;

        private static void readFully(final InputStream in, final byte[] buf, final int off, final int len) throws IOException {
            int read = 0;
            while (read < len) {
                int r = in.read(buf, off + read, len - read);
                if (r < 0) {
                    throw new EOFException("Unexpected EOF");
                }
                read += r;
            }
        }

        private static int readInt(final InputStream in) throws IOException {
            byte[] b = new byte[4];
            IPC.readFully(in, b, 0, 4);
            return ((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | (b[3] & 0xFF);
        }

        private static void writeInt(final OutputStream out, final int v) throws IOException {
            out.write(new byte[] { (byte) (v >>> 24), (byte) (v >>> 16), (byte) (v >>> 8), (byte) (v) });
        }

        private static void writeLong(final OutputStream out, final long v) throws IOException {
            out.write(new byte[] { (byte) (v >>> 56), (byte) (v >>> 48), (byte) (v >>> 40), (byte) (v >>> 32), (byte) (v >>> 24), (byte) (v >>> 16),
                    (byte) (v >>> 8), (byte) (v) });
        }

        static <T> T readFrame(final java.io.InputStream in, final Class<T> type) throws java.io.IOException, ClassNotFoundException {
            final InputStream bin = in;
            long window = 0L;
            int seen = 0;
            while (true) {
                int b = bin.read();
                if (b < 0) {
                    throw new EOFException("EOF before frame MAGIC");
                }
                window = (window << 8) | (b & 0xFFL);
                if (seen < 8) {
                    seen++;
                }
                if (seen >= 8 && window == MAGIC) {
                    break;
                }
            }
            int ver = bin.read();
            if (ver < 0) {
                throw new EOFException("EOF after MAGIC");
            }
            if (ver != VERSION) {
                throw new IOException("Unsupported IPC version: " + ver);
            }
            int len = IPC.readInt(bin);
            if (len < 0 || len > MAX_FRAME_SIZE) {
                throw new IOException("Invalid frame length: " + len);
            }
            byte[] payload = new byte[len];
            IPC.readFully(bin, payload, 0, len);
            int crcRead = IPC.readInt(bin);
            java.util.zip.CRC32 crc = new java.util.zip.CRC32();
            crc.update(payload);
            long crcCalc = crc.getValue();
            if ((crcRead & 0xFFFFFFFFL) != crcCalc) {
                throw new IOException("CRC32 mismatch");
            }
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload))) {
                try {
                    Object obj = ois.readObject();
                    return type.cast(obj);
                } catch (Throwable deserialiseProblem) {
                    if (deserialiseProblem instanceof IOException) {
                        throw (IOException) deserialiseProblem;
                    }
                    if (deserialiseProblem instanceof ClassNotFoundException) {
                        throw (ClassNotFoundException) deserialiseProblem;
                    }
                    throw new IOException(deserialiseProblem);
                }
            }
        }

        static void writeFrame(final OutputStream out, final Object obj) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
            try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(obj);
            }
            byte[] payload = bos.toByteArray();
            CRC32 crc = new CRC32();
            crc.update(payload);
            int crc32 = (int) crc.getValue();
            IPC.writeLong(out, MAGIC);
            out.write((byte) (VERSION & 0xFF));
            IPC.writeInt(out, payload.length);
            out.write(payload);
            IPC.writeInt(out, crc32);
            out.flush();
        }

    }

    static final class ProcessRequest implements Serializable {

        private static final long serialVersionUID = 1L;

        final Object[] arguments;
        final MethodDescriptor descriptor;

        ProcessRequest(final MethodDescriptor method, final Object... args) {
            descriptor = method;
            arguments = args;
        }

        Object invoke() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException,
                SecurityException {
            return descriptor.getMethod().invoke(null, arguments);
        }

    }

    static final class ProcessResponse implements Serializable {

        private static final long serialVersionUID = 1L;

        private static Throwable unwrap(final Throwable problem) {

            if (problem instanceof InvocationTargetException) {
                InvocationTargetException ite = (InvocationTargetException) problem;
                Throwable target = ite.getTargetException();
                if (target != null) {
                    return target;
                }
            }

            return problem;
        }

        static ProcessResponse fail(final Throwable problem) {
            return new ProcessResponse(null, ProcessResponse.unwrap(problem));
        }

        static ProcessResponse ok(final Object result) {
            return new ProcessResponse(result, null);
        }

        final Throwable error;

        final Object result;

        ProcessResponse(final Object result, final Throwable error) {
            this.result = result;
            this.error = error;
        }

    }

    public static ExternalProcessExecutor newInstance() {
        return new ExternalProcessExecutor(Executors.newCachedThreadPool(DaemonPoolExecutor.newProcessAwareThreadFactory("external-process-executor")));
    }

    /**
     * Create an executor backed by a fixed-size pool. The pool size effectively caps the number of persistent
     * worker processes running concurrently.
     */
    public static ExternalProcessExecutor newInstance(final int nThreads) {
        return new ExternalProcessExecutor(
                Executors.newFixedThreadPool(nThreads, DaemonPoolExecutor.newProcessAwareThreadFactory("external-process-executor")));
    }

    private final ExecutorService myExecutorService;

    ExternalProcessExecutor(final ExecutorService executor) {

        super();

        myExecutorService = executor;
    }

    public <T, C extends Callable<T> & Serializable> Future<T> call(final C callable) {

        Objects.requireNonNull(callable);

        return this.execute(MethodDescriptor.of(ProcessWorker.class, "call", Callable.class), null, callable);
    }

    public <T, C extends Callable<T> & Serializable> Future<T> call(final C callable, final ProcessOptions options) {

        Objects.requireNonNull(callable);

        return this.execute(MethodDescriptor.of(ProcessWorker.class, "call", Callable.class), options, callable);
    }

    public <T> Future<T> execute(final Class<?> owner, final String name, final Class<?>[] parameters, final Object... arguments) {

        Objects.requireNonNull(owner);
        Objects.requireNonNull(name);
        Objects.requireNonNull(parameters);

        return this.execute(MethodDescriptor.of(owner, name, parameters), null, arguments);
    }

    public <T> Future<T> execute(final Class<?> owner, final String name, final Class<?>[] parameters, final ProcessOptions options,
            final Object... arguments) {

        Objects.requireNonNull(owner);
        Objects.requireNonNull(name);
        Objects.requireNonNull(parameters);

        return this.execute(MethodDescriptor.of(owner, name, parameters), options, arguments);
    }

    public <T> Future<T> execute(final Method method, final Object... arguments) {

        Objects.requireNonNull(method);

        return this.execute(MethodDescriptor.of(method), null, arguments);
    }

    public <T> Future<T> execute(final Method method, final ProcessOptions options, final Object... arguments) {

        Objects.requireNonNull(method);

        return this.execute(MethodDescriptor.of(method), options, arguments);
    }

    public <T> Future<T> execute(final MethodDescriptor method, final Object... arguments) {

        Objects.requireNonNull(method);

        return new ProcessTask<T>(method, arguments, ProcessOptions.DEFAULT).start(myExecutorService);
    }

    public <T> Future<T> execute(final MethodDescriptor method, final ProcessOptions options, final Object... arguments) {

        Objects.requireNonNull(method);

        return new ProcessTask<T>(method, arguments, options != null ? options : ProcessOptions.DEFAULT).start(myExecutorService);
    }

    public <R extends Runnable & Serializable> Future<Void> run(final R runnable) {

        Objects.requireNonNull(runnable);

        return this.execute(MethodDescriptor.of(ProcessWorker.class, "run", Runnable.class), null, runnable);
    }

    public <R extends Runnable & Serializable> Future<Void> run(final R runnable, final ProcessOptions options) {

        Objects.requireNonNull(runnable);

        return this.execute(MethodDescriptor.of(ProcessWorker.class, "run", Runnable.class), options, runnable);
    }

}
