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

import java.util.concurrent.Callable;
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
        final int tmpThreshold = Math.max(1, (threshold * threshold) / (limit - first));
        final int tmpWorkers = OjAlgoUtils.ENVIRONMENT.threads;
        this.divide(first, limit, tmpThreshold, tmpWorkers);
    }

    protected abstract void conquer(final int first, final int limit);

    final void divide(final int first, final int limit, final int threshold, final int workers) {

        final int tmpCount = limit - first;

        if ((tmpCount > threshold) && (workers > 1)) {

            final int tmpSplit = first + (tmpCount / 2);
            final int tmpWorkers = workers / 2;

            final Future<Void> tmpFirstPart = DaemonPoolExecutor.INSTANCE.submit(new Callable<Void>() {

                public Void call() {
                    DivideAndConquer.this.divide(first, tmpSplit, threshold, tmpWorkers);
                    return null;
                }
            });

            final Future<Void> tmpSecondPart = DaemonPoolExecutor.INSTANCE.submit(new Callable<Void>() {

                public Void call() {
                    DivideAndConquer.this.divide(tmpSplit, limit, threshold, tmpWorkers);
                    return null;
                }
            });

            try {
                tmpFirstPart.get();
                tmpSecondPart.get();
            } catch (final InterruptedException | ExecutionException exception) {
                exception.printStackTrace();
                throw new ProgrammingError(exception);
            }

        } else {

            this.conquer(first, limit);
        }
    }

}
