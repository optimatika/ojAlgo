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

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.IntSupplier;

import org.ojalgo.OjAlgoUtils;

/**
 * Divide an index (int) range into smaller parts, and conquer each part in parallel.
 * <p>
 * The preferred way to use this is with a {@link Divider}, from {@link Parallelism#newDivider()} or
 * {@link ProcessingService#newDivider(IntSupplier)}, that calls a {@link Conquerer} with each part. A divider
 * is typically created once and kept as a static constant. The alternative is to extend this class, implement
 * {@link #conquer(int, int)} and call {@link #invoke(int, int, int)}.
 * <p>
 * A range of count indices is split while count is larger than the threshold and more than one worker is
 * available. The number of workers is the parallelism, but at most count. Each split divides the w workers
 * into w/2 and w - w/2, and the range in the same proportion. The first part is submitted to the executor,
 * the second is conquered by the calling thread, that then waits for the first. Unless the threshold stops
 * it, a range is divided into as many parts, of (almost) equal size, as there are workers. A
 * {@link RuntimeException} or {@link Error} thrown by the conquerer is rethrown in the calling thread.
 *
 * @author apete
 */
public abstract class DivideAndConquer {

    /**
     * Called with each part, a range of indices from first (inclusive) to limit (exclusive). Parts are
     * disjoint and may be conquered concurrently, by different threads.
     */
    @FunctionalInterface
    public interface Conquerer {

        void conquer(final int first, final int limit);

    }

    /**
     * Divides a range of indices and conquers the parts in parallel. The parallelism, the maximum number of
     * parts, is fixed when the divider is created. The threshold, the number of indices above which a range
     * is split, is set with {@link #threshold(int)}, typically before each call to
     * {@link #divide(int, int, Conquerer)}.
     * <p>
     * A divider may be shared between threads. The threshold is not synchronised: when threads set different
     * thresholds on the same divider, a call may use the threshold set by another thread. That only changes
     * how the range is split, not which indices are conquered.
     */
    public static final class Divider {

        private final ExecutorService myExecutor;
        private final IntSupplier myParallelism;
        private int myThreshold = 128;

        Divider(final ExecutorService executor, final IntSupplier parallelism) {
            super();
            myExecutor = Objects.requireNonNull(executor);
            myParallelism = Objects.requireNonNull(parallelism);
        }

        /**
         * Same as {@link #divide(int, int, Conquerer)} with first 0.
         */
        public void divide(final int limit, final Conquerer conquerer) {
            DivideAndConquer.call(myExecutor, 0, limit, myThreshold, myParallelism.getAsInt(), Objects.requireNonNull(conquerer));
        }

        /**
         * Conquers the range from first (inclusive) to limit (exclusive), in parallel parts, and returns when
         * all parts are done.
         */
        public void divide(final int first, final int limit, final Conquerer conquerer) {
            DivideAndConquer.call(myExecutor, first, limit, myThreshold, myParallelism.getAsInt(), Objects.requireNonNull(conquerer));
        }

        /**
         * @deprecated v58 Use {@link ProcessingService#newDivider(IntSupplier)} instead. This method no
         *             longer does anything!
         */
        @Deprecated
        public Divider parallelism(final IntSupplier parallelism) {
            return this;
        }

        /**
         * @param threshold A range is split only when it has more indices than this. The default is 128.
         * @return This divider
         */
        public Divider threshold(final int threshold) {
            myThreshold = threshold;
            return this;
        }

    }

    static void call(final ExecutorService executor, final int first, final int limit, final int threshold, final int workers,
            final DivideAndConquer.Conquerer conquerer) {

        int count = limit - first;
        int available = Math.min(workers, count);

        if (count > threshold && available > 1) {

            int firstWorkers = available / 2;
            int lastWorkers = available - firstWorkers;
            int split = first + (int) ((long) count * firstWorkers / available);

            Future<?> firstPart = executor.submit(() -> DivideAndConquer.call(executor, first, split, threshold, firstWorkers, conquerer));

            try {
                DivideAndConquer.call(executor, split, limit, threshold, lastWorkers, conquerer);
            } catch (RuntimeException | Error t) {
                firstPart.cancel(true);
                throw t;
            }
            try {
                firstPart.get();
            } catch (InterruptedException ie) {
                firstPart.cancel(true);
                Thread.currentThread().interrupt();
                throw new RuntimeException(ie);
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else if (cause instanceof Error) {
                    throw (Error) cause;
                } else {
                    throw new RuntimeException(cause != null ? cause : ee);
                }
            }

        } else {

            conquerer.conquer(first, limit);
        }
    }

    protected DivideAndConquer() {
        super();
    }

    /**
     * Conquers the range from first (inclusive) to limit (exclusive), in parallel parts, and returns when all
     * parts are done. The parallelism is {@link Parallelism#THREADS}.
     *
     * @param first     The first index to include
     * @param limit     The first index not to include
     * @param threshold A range is split only when it has more indices than this
     */
    public final void invoke(final int first, final int limit, final int threshold) {

        int availableWorkers = OjAlgoUtils.ENVIRONMENT.threads;

        DivideAndConquer.call(DaemonPoolExecutor.INSTANCE, first, limit, threshold, availableWorkers, this::conquer);
    }

    /**
     * @see Conquerer#conquer(int, int)
     */
    protected abstract void conquer(final int first, final int limit);

}
