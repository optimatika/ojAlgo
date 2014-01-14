/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ForkJoinPool;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.type.IntCount;

public final class DaemonPoolExecutor extends ForkJoinPool {

    public static final DaemonPoolExecutor INSTANCE = new DaemonPoolExecutor(OjAlgoUtils.ENVIRONMENT.threads);

    private DaemonPoolExecutor() {
        super();
    }

    private DaemonPoolExecutor(final int parallelism) {
        super(parallelism);
    }

    private DaemonPoolExecutor(final int parallelism, final ForkJoinWorkerThreadFactory factory, final UncaughtExceptionHandler handler, final boolean asyncMode) {
        super(parallelism, factory, handler, asyncMode);
    }

    public IntCount countActiveDaemons() {
        return new IntCount(this.getActiveThreadCount());
    }

    public IntCount countExistingDaemons() {
        return new IntCount(this.getPoolSize());
    }

    public IntCount countIdleDaemons() {
        return new IntCount(this.getPoolSize() - this.getActiveThreadCount());
    }

    public boolean isDaemonAvailable() {
        return this.getPoolSize() > this.getActiveThreadCount();
    }

}
