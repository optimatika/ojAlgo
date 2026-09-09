/*
 * Copyright 1997-2026 Optimatika
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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.ojalgo.netio.BasicLogger;

/**
 * Forwards {@link Thread#interrupt()} of the calling thread as a repeated invocation of a stop action.
 * <p>
 * This is useful when the calling thread is blocked inside native code that cannot observe Java's interrupt
 * flag on its own. A shared scheduler polls the flag; once set, it calls the provided stop action repeatedly
 * until {@link #close()} is called.
 * <p>
 * Use it in a try-with-resources block around the blocking call:
 *
 * <pre>
 * try (InterruptForwarder fwd = InterruptForwarder.watch(solver::stop)) {
 *     solver.solve();
 * }
 * </pre>
 */
public final class InterruptForwarder implements AutoCloseable {

    private static final long POLL_MILLIS = 100L;

    private static final ScheduledExecutorService SCHEDULER = DaemonPoolExecutor.newSingleThreadScheduledExecutor("ojAlgo-interrupt-fwd");

    /**
     * Starts watching the calling thread. Call this immediately before the blocking call, and
     * {@link #close()} as soon as it returns.
     *
     * @param stopAction called repeatedly from the scheduler thread once the interrupt is detected; must be
     *        safe to call while the blocking operation is running and to call more than once.
     */
    public static InterruptForwarder watch(final Runnable stopAction) {

        Thread watched = Thread.currentThread();

        ScheduledFuture<?> task = SCHEDULER.scheduleWithFixedDelay(() -> {
            try {
                if (watched.isInterrupted()) {
                    stopAction.run();
                }
            } catch (RuntimeException cause) {
                BasicLogger.error("Failed to forward interrupt!", cause);
            }
        }, POLL_MILLIS, POLL_MILLIS, TimeUnit.MILLISECONDS);

        return new InterruptForwarder(task);
    }

    private final ScheduledFuture<?> myTask;

    private InterruptForwarder(final ScheduledFuture<?> task) {
        super();
        myTask = task;
    }

    @Override
    public void close() {
        myTask.cancel(false);
    }

}
