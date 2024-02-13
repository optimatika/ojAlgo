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
package org.ojalgo.array;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.Stopwatch;

public class NativeCleanupTest {

    static final AtomicInteger COUNTER = new AtomicInteger();
    static final int SIZE = 134_217_728;

    public static void main(final String[] args) {

        DenseArray<Double> org = OffHeapArray.R064.make(SIZE);
        org.fillAll(new Uniform());

        Stopwatch watch = new Stopwatch();

        while (true) {

            DenseArray<Double> copy = OffHeapArray.R064.make(SIZE);

            copy.fillMatching(org);

            org = copy;

            BasicLogger.debug("Copied {} times. Last copy took {}", COUNTER.incrementAndGet(), watch.restart(CalendarDateUnit.SECOND));

            try {
                TimeUnit.SECONDS.sleep(1);
                System.gc();
            } catch (final InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }

}
