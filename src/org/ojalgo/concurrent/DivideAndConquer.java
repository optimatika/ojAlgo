/*
 * Copyright 1997-2013 Optimatika (www.optimatika.se)
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

import java.util.concurrent.ForkJoinTask;

import org.ojalgo.OjAlgoUtils;

/**
 * @author apete
 */
public abstract class DivideAndConquer extends Object {

    abstract class Task extends ForkJoinTask<Void> {

        Task() {
            super();
        }

        @Override
        public final Void getRawResult() {
            return null;
        }

        @Override
        protected final void setRawResult(final Void value) {
        }

    }

    static int INITIAL = OjAlgoUtils.ENVIRONMENT.threads;

    static int adjustThreshold(final int threshold, final int count) {
        return Math.max(1, (threshold * threshold) / count);
    }

    static boolean shouldDivideFurther(final int count, final int threshold, final int workers) {
        return (count > threshold) && (workers > 1);
    }

    public DivideAndConquer() {
        super();
    }

    /**
    * Asynchronous execution - start it, and forget about it.
    * 
    * @param first The first index, in a range, to include.
    * @param limit The first index NOT to include - last (excl.) index in a range.
    */
    public final void execute(final int first, final int limit, final int threshold) {

        DaemonPoolExecutor.INSTANCE.execute(new Task() {

            @Override
            protected boolean exec() {
                DivideAndConquer.this.divide(first, limit, DivideAndConquer.adjustThreshold(threshold, limit - first), INITIAL);
                return true;
            }

        });
    }

    /**
     * Synchronous execution - wait until it's finished.
     * 
     * @param first The first index, in a range, to include.
     * @param limit The first index NOT to include - last (excl.) index in a range.
     */
    public final void invoke(final int first, final int limit, final int threshold) {

        DaemonPoolExecutor.INSTANCE.invoke(new Task() {

            @Override
            protected boolean exec() {
                DivideAndConquer.this.divide(first, limit, DivideAndConquer.adjustThreshold(threshold, limit - first), INITIAL);
                return true;
            }

        });
    }

    protected abstract void conquer(final int first, final int limit);

    protected final void divide(final int first, final int limit, final int threshold, final int workers) {

        final int tmpCount = limit - first;

        if (DivideAndConquer.shouldDivideFurther(tmpCount, threshold, workers)) {

            final int tmpSplit = first + (tmpCount / 2);
            final int tmpWorkers = workers / 2;

            final Task tmpForkedTask = new Task() {

                @Override
                protected boolean exec() {
                    DivideAndConquer.this.divide(first, tmpSplit, threshold, tmpWorkers);
                    return true;
                }

            };

            tmpForkedTask.fork();

            this.divide(tmpSplit, limit, threshold, tmpWorkers);

            tmpForkedTask.join();

        } else {

            this.conquer(first, limit);
        }
    }

}
