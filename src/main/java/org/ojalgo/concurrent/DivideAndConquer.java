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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.IntSupplier;

import org.ojalgo.OjAlgoUtils;

/**
 * @author apete
 */
public abstract class DivideAndConquer {

    @FunctionalInterface
    public interface Conquerer {

        void conquer(final int first, final int limit);

    }

    public static final class Divider {

        private final ExecutorService myExecutor;

        private IntSupplier myParallelism = Parallelism.THREADS;

        private int myThreshold = 128;

        Divider(final ExecutorService executor) {
            super();
            myExecutor = executor;
        }

        public void divide(final int limit, final Conquerer conquerer) {
            this.divide(0, limit, conquerer);
        }

        public void divide(final int first, final int limit, final Conquerer conquerer) {
            DivideAndConquer.call(myExecutor, first, limit, myThreshold, myParallelism.getAsInt(), conquerer);
        }

        public Divider parallelism(final IntSupplier parallelism) {
            if (parallelism != null) {
                myParallelism = parallelism;
            }
            return this;
        }

        public Divider threshold(final int threshold) {
            myThreshold = threshold;
            return this;
        }

    }

    static void call(final ExecutorService executor, final int first, final int limit, final int threshold, final int workers,
            final DivideAndConquer.Conquerer conquerer) {

        int count = limit - first;

        if (count > threshold && workers > 1) {

            int split = first + count / 2;
            int nextWorkers = workers / 2;

            Future<?> firstPart = executor.submit(() -> DivideAndConquer.call(executor, first, split, threshold, nextWorkers, conquerer));
            Future<?> secondPart = executor.submit(() -> DivideAndConquer.call(executor, split, limit, threshold, nextWorkers, conquerer));

            try {
                firstPart.get();
                secondPart.get();
            } catch (final InterruptedException | ExecutionException cause) {
                throw new RuntimeException(cause);
            }

        } else {

            conquerer.conquer(first, limit);
        }
    }

    public DivideAndConquer() {
        super();
    }

    /**
     * Synchronous execution - wait until it's finished.
     *
     * @param first The first index, in a range, to include.
     * @param limit The first index NOT to include - last (excl.) index in a range.
     */
    public final void invoke(final int first, final int limit, final int threshold) {

        // int availableWorkers = OjAlgoUtils.ENVIRONMENT.threads - DaemonPoolExecutor.INSTANCE.getActiveCount() / 2;
        int availableWorkers = OjAlgoUtils.ENVIRONMENT.threads;

        DivideAndConquer.call(DaemonPoolExecutor.INSTANCE, first, limit, threshold, availableWorkers, this::conquer);
    }

    protected abstract void conquer(final int first, final int limit);

}
