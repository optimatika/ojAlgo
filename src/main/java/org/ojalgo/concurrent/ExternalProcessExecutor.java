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

import org.ojalgo.machine.JavaType;

/**
 * Execute each submitted task/method in a separate JVM (separate/external OS process) with arbitrary
 * {@link Serializable} arguments/return. Provides hard cancellation/timeout via process kill.
 */
public final class ExternalProcessExecutor {

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

        private static long readLong(final InputStream in) throws IOException {
            byte[] b = new byte[8];
            IPC.readFully(in, b, 0, 8);
            return ((long) (b[0] & 0xFF) << 56) | ((long) (b[1] & 0xFF) << 48) | ((long) (b[2] & 0xFF) << 40) | ((long) (b[3] & 0xFF) << 32)
                    | ((long) (b[4] & 0xFF) << 24) | ((long) (b[5] & 0xFF) << 16) | ((long) (b[6] & 0xFF) << 8) | (b[7] & 0xFF);
        }

        private static void writeInt(final OutputStream out, final int v) throws IOException {
            out.write(new byte[] { (byte) (v >>> 24), (byte) (v >>> 16), (byte) (v >>> 8), (byte) (v) });
        }

        private static void writeLong(final OutputStream out, final long v) throws IOException {
            out.write(new byte[] { (byte) (v >>> 56), (byte) (v >>> 48), (byte) (v >>> 40), (byte) (v >>> 32), (byte) (v >>> 24), (byte) (v >>> 16),
                    (byte) (v >>> 8), (byte) (v) });
        }

        static <T> T readFrame(final java.io.InputStream in, final Class<T> type) throws java.io.IOException, ClassNotFoundException {
            // Scan for MAGIC to resync even if stdout is polluted
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
            // Version
            int ver = bin.read();
            if (ver < 0) {
                throw new EOFException("EOF after MAGIC");
            }
            if (ver != VERSION) {
                throw new IOException("Unsupported IPC version: " + ver);
            }
            // Length
            int len = IPC.readInt(bin);
            if (len < 0 || len > MAX_FRAME_SIZE) {
                throw new IOException("Invalid frame length: " + len);
            }
            // Payload
            byte[] payload = new byte[len];
            IPC.readFully(bin, payload, 0, len);
            // CRC32
            int crcRead = IPC.readInt(bin);
            java.util.zip.CRC32 crc = new java.util.zip.CRC32();
            crc.update(payload);
            long crcCalc = crc.getValue();
            if ((crcRead & 0xFFFFFFFFL) != crcCalc) {
                throw new IOException("CRC32 mismatch");
            }
            // Deserialize
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload))) {
                // Directly deserialize without ObjectInputFilter
                try {
                    Object obj = ois.readObject();
                    return type.cast(obj);
                } catch (Throwable deserialiseProblem) {
                    if (deserialiseProblem instanceof IOException) throw (IOException) deserialiseProblem;
                    if (deserialiseProblem instanceof ClassNotFoundException) throw (ClassNotFoundException) deserialiseProblem;
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
            java.util.zip.CRC32 crc = new java.util.zip.CRC32();
            crc.update(payload);
            int crc32 = (int) crc.getValue();
            // Write header + payload + crc
            // Note: deliberately not wrapping with DataOutputStream to avoid accidentally writing Java serialization headers
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

    static final class ProcessTask<T> implements Future<T>, Runnable {

        private static void destroyProcessTree(final Process proc) {
            try {
                // Destroy all descendants first, then the process itself
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

        private final Object[] myArguments;
        private final AtomicBoolean myCancelled = new AtomicBoolean(false);
        private final AtomicBoolean myDone = new AtomicBoolean(false);
        private volatile Throwable myError;
        private final Object myLock = new Object();
        private final MethodDescriptor myMethod;
        private final ProcessOptions myOptions;
        private volatile Process myProcess;

        private volatile T myResult;

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
            if (myProcess != null) {
                try {
                    ProcessTask.destroyProcessTree(myProcess);
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
            // Use monotonic clock for relative timeout calculations
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
            List<String> cmd = new ArrayList<>();
            String javaBin = System.getProperty("java.home") + "/bin/java";
            cmd.add(javaBin);
            if (myOptions.xmx != null) {
                cmd.add("-Xmx" + myOptions.xmx);
            }
            if (myOptions.enableNativeAccessAllUnnamed) {
                cmd.add("--enable-native-access=ALL-UNNAMED");
            }
            if (myOptions.systemProperties != null) {
                for (Map.Entry<String, String> e : myOptions.systemProperties.entrySet()) {
                    cmd.add("-D" + e.getKey() + "=" + e.getValue());
                }
            }
            if (myOptions.jvmArgs != null && !myOptions.jvmArgs.isEmpty()) {
                cmd.addAll(myOptions.jvmArgs);
            }
            cmd.add("-cp");
            String testCp = System.getProperty("surefire.test.class.path");
            String mainCp = System.getProperty("java.class.path");
            String pathSep = File.pathSeparator;
            String effectiveCp;
            if (myOptions.classpath != null && !myOptions.classpath.isEmpty()) {
                effectiveCp = myOptions.classpath;
            } else if (testCp != null && !testCp.isEmpty()) {
                // Combine test and main classpaths to be safe
                effectiveCp = mainCp != null && !mainCp.isEmpty() ? (testCp + pathSep + mainCp) : testCp;
            } else {
                effectiveCp = mainCp;
            }
            // Try to include local build output dirs when running outside Surefire/Gradle
            try {
                String userDir = System.getProperty("user.dir");
                List<String> candidates = new ArrayList<>(8);
                // Maven
                candidates.add(userDir + File.separator + "target" + File.separator + "test-classes");
                candidates.add(userDir + File.separator + "target" + File.separator + "classes");
                // Gradle
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
            try {
                java.net.URL loc = ProcessWorker.class.getProtectionDomain().getCodeSource().getLocation();
                if (loc != null) {
                    String workerPath = new java.io.File(loc.toURI()).getAbsolutePath();
                    if (effectiveCp == null || effectiveCp.isEmpty()) {
                        effectiveCp = workerPath;
                    } else if (!effectiveCp.contains(workerPath)) {
                        effectiveCp = workerPath + pathSep + effectiveCp;
                    }
                }
            } catch (Throwable ignore) {
            }
            cmd.add(effectiveCp);
            cmd.add("org.ojalgo.concurrent.ProcessWorker");

            ProcessBuilder pb = new ProcessBuilder(cmd);
            if (myOptions.env != null) {
                pb.environment().putAll(myOptions.env);
            }

            final StringBuilder stderrBuf = new StringBuilder(1024);
            try {
                myProcess = pb.start();
                // Bind this process to the executing thread so an interrupt kills it
                Thread t = Thread.currentThread();
                if (t instanceof ProcessAwareThread) {
                    ((ProcessAwareThread) t).setProcess(myProcess);
                }

                // Watchdog for timeout
                final Thread watchdog;
                if (!myOptions.timeout.isZero() && myOptions.timeout.compareTo(Duration.ZERO) > 0) {
                    watchdog = DaemonPoolExecutor.newThreadFactory("ojAlgo-proc-watchdog").newThread(() -> {
                        try {
                            long millis = Math.max(1L, myOptions.timeout.toMillis());
                            boolean finished = myProcess.waitFor(millis, TimeUnit.MILLISECONDS);
                            if (!finished) {
                                ProcessTask.destroyProcessTree(myProcess);
                            }
                        } catch (InterruptedException ignore) {
                            Thread.currentThread().interrupt();
                        }
                    });
                    watchdog.start();
                } else {
                    watchdog = null;
                }

                // Send request
                OutputStream childIn = myProcess.getOutputStream();
                IPC.writeFrame(childIn, new ProcessRequest(myMethod, myArguments));
                childIn.flush();
                childIn.close();

                // Drain stderr asynchronously to avoid pipe blocking and capture it
                final InputStream childErr = myProcess.getErrorStream();
                Thread errDrainer = DaemonPoolExecutor.newThreadFactory("ojAlgo-proc-stderr").newThread(() -> {
                    // Capture up to 64 KiB to avoid unbounded growth
                    final int MAX_BYTES = 64 * 1024;
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.min(4096, MAX_BYTES));
                    final OutputStream capped = new OutputStream() {
                        private int written = 0;

                        @Override
                        public void close() throws IOException {
                            baos.close();
                        }

                        @Override
                        public void flush() throws IOException {
                            baos.flush();
                        }

                        @Override
                        public void write(final byte[] b, final int off, final int len) throws IOException {
                            int toWrite = Math.min(len, Math.max(0, MAX_BYTES - written));
                            if (toWrite > 0) {
                                baos.write(b, off, toWrite);
                                written += toWrite;
                            }
                        }

                        @Override
                        public void write(final int b) throws IOException {
                            if (written < MAX_BYTES) {
                                baos.write(b);
                                written++;
                            }
                        }
                    };
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
                        synchronized (stderrBuf) {
                            stderrBuf.append(new String(baos.toByteArray()));
                        }
                    }
                });
                errDrainer.start();

                // Read response (blocks until child writes)
                ProcessResponse resp = IPC.readFrame(myProcess.getInputStream(), ProcessResponse.class);

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

                // Ensure process exit
                try {
                    myProcess.waitFor(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ignore) {
                    Thread.currentThread().interrupt();
                }

            } catch (Throwable t) {
                // If cancelled, prefer cancellation outcome
                if (!myCancelled.get()) {
                    String msg;
                    synchronized (stderrBuf) {
                        msg = stderrBuf.toString();
                    }
                    myError = msg.isEmpty() ? t : new IOException(t.getMessage() + " | child-stderr: " + msg, t);
                }
            } finally {
                if (myProcess != null && myProcess.isAlive()) {
                    try {
                        ProcessTask.destroyProcessTree(myProcess);
                    } catch (Throwable ignore) {
                    }
                }
                // Clear any binding if present
                Thread t = Thread.currentThread();
                if (t instanceof ProcessAwareThread) {
                    ((ProcessAwareThread) t).setProcess(null);
                }
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

    public static ExternalProcessExecutor newInstance() {
        return new ExternalProcessExecutor(Executors.newCachedThreadPool(DaemonPoolExecutor.newProcessAwareThreadFactory("external-process-executor")));
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