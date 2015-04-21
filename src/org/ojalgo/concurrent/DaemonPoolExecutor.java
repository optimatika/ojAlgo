/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import java.util.concurrent.*;

import org.ojalgo.OjAlgoUtils;

public final class DaemonPoolExecutor extends ThreadPoolExecutor {

    static final DaemonPoolExecutor INSTANCE = new DaemonPoolExecutor(OjAlgoUtils.ENVIRONMENT.cores, Integer.MAX_VALUE, 2L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), DaemonFactory.INSTANCE);

    /**
     * @see java.util.concurrent.AbstractExecutorService#submit(java.util.concurrent.Callable)
     */
    public static <T> Future<T> invoke(final Callable<T> task) {
        return INSTANCE.submit(task);
    }

    /**
     * @see java.util.concurrent.AbstractExecutorService#submit(java.lang.Runnable)
     */
    public static Future<?> invoke(final Runnable task) {
        return INSTANCE.submit(task);
    }

    /**
     * @see java.util.concurrent.AbstractExecutorService#submit(java.lang.Runnable, java.lang.Object)
     */
    public static <T> Future<T> invoke(final Runnable task, final T result) {
        return INSTANCE.submit(task, result);
    }

    public static boolean isDaemonAvailable() {
        return INSTANCE.getActiveCount() < OjAlgoUtils.ENVIRONMENT.threads;
    }

    static final DaemonPoolExecutor makeSingle() {
        return new DaemonPoolExecutor(1, 1, Long.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), DaemonFactory.INSTANCE);
    }

    DaemonPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    DaemonPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue, final RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    DaemonPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    DaemonPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory, final RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

}
