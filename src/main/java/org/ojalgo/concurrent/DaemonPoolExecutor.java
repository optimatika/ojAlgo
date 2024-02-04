/*
 * Copyright 1997-2024 Optimatika
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
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.OjAlgoUtils;

public final class DaemonPoolExecutor extends ThreadPoolExecutor {

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final ThreadGroup GROUP = new ThreadGroup("ojAlgo-daemon-group");

    static final DaemonPoolExecutor INSTANCE = new DaemonPoolExecutor(OjAlgoUtils.ENVIRONMENT.units, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), DaemonPoolExecutor.newThreadFactory("ojAlgo-daemon-"));

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

    /**
     * Like {@link Executors#newCachedThreadPool()} but with identifiable (daemon) threads
     */
    public static ExecutorService newCachedThreadPool(final String name) {
        return Executors.newCachedThreadPool(DaemonPoolExecutor.newThreadFactory(name));
    }

    /**
     * Like {@link Executors#newFixedThreadPool(int)} but with identifiable (daemon) threads
     */
    public static ExecutorService newFixedThreadPool(final String name, final int nThreads) {
        return Executors.newFixedThreadPool(nThreads, DaemonPoolExecutor.newThreadFactory(name));
    }

    /**
     * Like {@link Executors#newScheduledThreadPool(int)} but with identifiable (daemon) threads
     */
    public static ExecutorService newScheduledThreadPool(final String name, final int corePoolSize) {
        return Executors.newScheduledThreadPool(corePoolSize, DaemonPoolExecutor.newThreadFactory(name));
    }

    /**
     * Like {@link Executors#newSingleThreadExecutor()} but with identifiable (daemon) threads
     */
    public static ExecutorService newSingleThreadExecutor(final String name) {
        return Executors.newSingleThreadExecutor(DaemonPoolExecutor.newThreadFactory(name));
    }

    /**
     * Like {@link Executors#newSingleThreadScheduledExecutor()} but with identifiable (daemon) threads
     */
    public static ExecutorService newSingleThreadScheduledExecutor(final String name) {
        return Executors.newSingleThreadScheduledExecutor(DaemonPoolExecutor.newThreadFactory(name));
    }

    public static ThreadFactory newThreadFactory(final String name) {
        return DaemonPoolExecutor.newThreadFactory(GROUP, name);
    }

    public static ThreadFactory newThreadFactory(final ThreadGroup group, final String name) {

        String prefix = name.endsWith("-") ? name : name + "-";

        return target -> {
            Thread thread = new Thread(group, target, prefix + DaemonPoolExecutor.COUNTER.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    DaemonPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue) {
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
