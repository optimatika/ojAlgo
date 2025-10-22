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

import java.util.concurrent.atomic.AtomicReference;

/**
 * A daemon thread that can be bound to an external {@link Process}. When this thread is interrupted, it will
 * attempt to forcibly terminate the bound process before delegating to {@link Thread#interrupt()}.
 * <P>
 * Typical usage:
 *
 * <pre>
 * Thread t = DaemonPoolExecutor.newProcessThreadFactory("ojAlgo-proc").newThread(() -> {
 *     Process p = new ProcessBuilder("sleep", "10").start();
 *     if (Thread.currentThread() instanceof ProcessAwareThread pat) pat.setProcess(p);
 *     p.waitFor();
 * });
 * t.start();
 * // later...
 * t.interrupt(); // will destroy the process
 * </pre>
 */
final class ProcessAwareThread extends Thread {

    private final AtomicReference<Process> myReference = new AtomicReference<>();

    ProcessAwareThread(final ThreadGroup group, final Runnable target, final String name) {
        super(group, target, name);
        this.setDaemon(true);
    }

    @Override
    public void interrupt() {
        Process process = myReference.get();
        if (process != null) {
            try {
                process.destroyForcibly();
            } catch (Throwable ignore) {
                // ignored
            }
        }
        super.interrupt();
    }

    void setProcess(final Process process) {
        myReference.set(process);
    }

}
