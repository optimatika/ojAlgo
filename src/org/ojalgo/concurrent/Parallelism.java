/*
 * Copyright 1997-2020 Optimatika
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

/**
 * A set of standard levels of parallelsim derived from the number of available cores and optionally capped by
 * reserving a specified amount of memory per thread. The info about available cores/threads/memory comes from
 * {@link OjAlgoUtils#ENVIRONMENT}.
 */
public enum Parallelism implements IntSupplier {

    /**
     * Double the {@link #HIGHER} parallelism
     */
    OVERDRIVE(() -> 2 * Parallelism.higher()),
    /**
     * If the number of cores is a power of 2, then {@link #LOWER} and {@link #HIGHER)} will be equal.
     *
     * @return The number of cores adjusted upwards to be power of 2
     */
    HIGHER(() -> Parallelism.higher()),
    /**
     * If the number of cores is a power of 2, then {@link #LOWER} and {@link #HIGHER)} will be equal.
     *
     * @return The number of cores adjusted downwards to be power of 2
     */
    LOWER(() -> Parallelism.lower()),
    /**
     * Half the {@link #LOWER} parallelism
     */
    HALF(() -> Math.max(1, Parallelism.lower() / 2)),
    /**
     * A quarter of the {@link #LOWER} parallelism
     */
    QUARTER(() -> Math.max(1, Parallelism.lower() / 4)),
    /**
     * 4
     */
    FOUR(() -> 4),
    /**
     * 2
     */
    TWO(() -> 2),
    /**
     * 1
     */
    ONE(() -> 1);

    static int higher() {
        return PowerOf2.adjustUp(OjAlgoUtils.ENVIRONMENT.cores);
    }

    static int lower() {
        return PowerOf2.adjustDown(OjAlgoUtils.ENVIRONMENT.cores);
    }

    private final IntSupplier myValue;

    Parallelism(final IntSupplier value) {
        myValue = value;
    }

    public int getAsInt() {
        return myValue.getAsInt();
    }

    public IntSupplier reserveBytes(final long bytesPerThread) {
        return () -> Math.max(1, Math.min(myValue.getAsInt(), PowerOf2.adjustDown(Math.toIntExact(OjAlgoUtils.ENVIRONMENT.memory / bytesPerThread))));
    }

    public IntSupplier reserveGigaBytes(final long gigaBytesPerThread) {
        return this.reserveMegaBytes(1024L * gigaBytesPerThread);
    }

    public IntSupplier reserveKiloBytes(final long kiloBytesPerThread) {
        return this.reserveBytes(1024L * kiloBytesPerThread);
    }

    public IntSupplier reserveMegaBytes(final long megaBytesPerThread) {
        return this.reserveKiloBytes(1024L * megaBytesPerThread);
    }

    public IntSupplier reserveTeraBytes(final long teraBytesPerThread) {
        return this.reserveGigaBytes(1024L * teraBytesPerThread);
    }

}
