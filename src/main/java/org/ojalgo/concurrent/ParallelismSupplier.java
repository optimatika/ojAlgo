/*
 * Copyright 1997-2022 Optimatika
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

import java.util.function.IntSupplier;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.function.special.PowerOf2;

@FunctionalInterface
public interface ParallelismSupplier extends IntSupplier {

    default ParallelismSupplier adjustDown() {
        return () -> PowerOf2.adjustDown(this.getAsInt());
    }

    default ParallelismSupplier adjustUp() {
        return () -> PowerOf2.adjustUp(this.getAsInt());
    }

    default ParallelismSupplier decrement() {
        return () -> Math.max(1, this.getAsInt() - 1);
    }

    /**
     * Round up as in: 9 / 2 = 5
     */
    default ParallelismSupplier divideBy(final int divisor) {
        if (divisor > 1) {
            return () -> Math.max(1, (this.getAsInt() + divisor - 1) / divisor);
        }
        return this::getAsInt;
    }

    default ParallelismSupplier halve() {
        return this.divideBy(2);
    }

    default ParallelismSupplier increment() {
        return () -> Math.max(1, this.getAsInt() + 1);
    }

    default ParallelismSupplier max(final int other) {
        return this.max(() -> other);
    }

    default ParallelismSupplier max(final IntSupplier other) {
        return () -> Math.max(this.getAsInt(), other.getAsInt());
    }

    default ParallelismSupplier min(final int other) {
        return this.min(() -> other);
    }

    default ParallelismSupplier min(final IntSupplier other) {
        return () -> Math.min(this.getAsInt(), other.getAsInt());
    }

    /**
     * Make sure there's this much memory per thread by, if necessary, limiting the parallelism. The total
     * amount of memory available is defined by {@link OjAlgoUtils#ENVIRONMENT}
     */
    default ParallelismSupplier reserveBytes(final long bytesPerThread) {
        return () -> Math.max(1, Math.min(this.getAsInt(), PowerOf2.adjustDown(OjAlgoUtils.ENVIRONMENT.memory / bytesPerThread)));
    }

    /**
     * @see #reserveBytes(long)
     */
    default ParallelismSupplier reserveGigaBytes(final long gigaBytesPerThread) {
        return this.reserveMegaBytes(1024L * gigaBytesPerThread);
    }

    /**
     * @see #reserveBytes(long)
     */
    default ParallelismSupplier reserveKiloBytes(final long kiloBytesPerThread) {
        return this.reserveBytes(1024L * kiloBytesPerThread);
    }

    /**
     * @see #reserveBytes(long)
     */
    default ParallelismSupplier reserveMegaBytes(final long megaBytesPerThread) {
        return this.reserveKiloBytes(1024L * megaBytesPerThread);
    }

    /**
     * @see #reserveBytes(long)
     */
    default ParallelismSupplier reserveTeraBytes(final long teraBytesPerThread) {
        return this.reserveGigaBytes(1024L * teraBytesPerThread);
    }

}
