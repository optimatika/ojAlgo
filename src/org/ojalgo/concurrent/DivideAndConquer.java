/*
 * Copyright 1997-2018 Optimatika
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
import java.util.concurrent.Future;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.ProgrammingError;

/**
 * @author apete
 */
public abstract class DivideAndConquer extends Object {

    public DivideAndConquer() {
        super();
    }

    /**
     * Synchronous execution - wait until it's finished.
     *
     * @param first The first index, in a range, to include.
     * @param limit The first index NOT to include - last (excl.) index in a range.
     * @param threshold
     */
    public final void invoke(final int first, final int limit, final int threshold) {

        final int availableWorkers = OjAlgoUtils.ENVIRONMENT.threads - (DaemonPoolExecutor.INSTANCE.getActiveCount() / 2);

        this.divide(first, limit, threshold, availableWorkers);
    }

    protected abstract void conquer(final int first, final int limit);

    final void divide(final int first, final int limit, final int threshold, final int workers) {

        final int count = limit - first;

        if ((count > threshold) && (workers > 1)) {

            final int split = first + (count / 2);
            final int nextWorkers = workers / 2;

            final Future<?> firstPart = DaemonPoolExecutor.INSTANCE.submit(() -> this.divide(first, split, threshold, nextWorkers));
            final Future<?> secondPart = DaemonPoolExecutor.INSTANCE.submit(() -> this.divide(split, limit, threshold, nextWorkers));

            try {
                firstPart.get();
                secondPart.get();
            } catch (final InterruptedException | ExecutionException exception) {
                exception.printStackTrace();
                throw new ProgrammingError(exception);
            }

        } else {

            this.conquer(first, limit);
        }
    }

}
