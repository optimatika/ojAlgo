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

import java.util.function.IntSupplier;

import org.ojalgo.OjAlgoUtils;

/**
 * A set of standard levels of parallelsim derived from the number of available cores and optionally capped by
 * reserving a specified amount of memory per thread. The info about available cores/threads/memory comes from
 * {@link OjAlgoUtils#ENVIRONMENT}.
 */
public enum Parallelism implements ParallelismSupplier {

    /**
     * The total number of threads (incl. hyperthreads)
     */
    THREADS(() -> OjAlgoUtils.ENVIRONMENT.threads),
    /**
     * The number of CPU cores
     */
    CORES(() -> OjAlgoUtils.ENVIRONMENT.cores),
    /**
     * The number of CPU:s or, more precisely, top level (L3) cache units. It is generally assumed that there
     * is one L3 cache unit per CPU.
     */
    UNITS(() -> OjAlgoUtils.ENVIRONMENT.units),
    /**
     * 8
     */
    EIGHT(() -> 8),
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

    private final IntSupplier myValue;

    Parallelism(final IntSupplier value) {
        myValue = value;
    }

    public int getAsInt() {
        return myValue.getAsInt();
    }

}
