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
package org.ojalgo.array.operation;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayR032;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * <pre>
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class TuneUnrolledDOT {

    private static final int _1_000 = 1_000;
    private static final int _100 = 100;

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(TuneUnrolledDOT.class);
    }

    public float[] left;
    public float[] product;
    public float[] right;

    @Benchmark
    public void plain() {

        for (int j = 0, nbCols = product.length; j < nbCols; j++) {
            product[j] = DOT.plain(left, 0, right, j * _1_000, 0, _1_000);
        }
    }

    @Setup
    public void setup() {

        ArrayR032 l = ArrayR032.make(1 * _1_000);
        l.fillAll(Uniform.standard());
        left = l.data;

        ArrayR032 r = ArrayR032.make(_1_000 * _100);
        r.fillAll(Uniform.standard());
        right = r.data;

        ArrayR032 p = ArrayR032.make(1 * _100);
        p.fillAll(Uniform.standard());
        product = p.data;

        float rp = DOT.plain(left, 0, right, 2 * _1_000, 0, _1_000);
        float ru2 = DOT.unrolled02(left, 0, right, 2 * _1_000, 0, _1_000);
        float ru4 = DOT.unrolled04(left, 0, right, 2 * _1_000, 0, _1_000);
        float ru8 = DOT.unrolled08(left, 0, right, 2 * _1_000, 0, _1_000);
        float ru16 = DOT.unrolled16(left, 0, right, 2 * _1_000, 0, _1_000);

        float delta = 0.001F;
        TestUtils.assertEquals(rp, ru2, delta);
        TestUtils.assertEquals(rp, ru4, delta);
        TestUtils.assertEquals(rp, ru8, delta);
        TestUtils.assertEquals(rp, ru16, delta);
    }

    @Benchmark
    public void unrolled02() {

        for (int j = 0, nbCols = product.length; j < nbCols; j++) {
            product[j] = DOT.unrolled02(left, 0, right, j * _1_000, 0, _1_000);
        }
    }

    @Benchmark
    public void unrolled04() {

        for (int j = 0, nbCols = product.length; j < nbCols; j++) {
            product[j] = DOT.unrolled04(left, 0, right, j * _1_000, 0, _1_000);
        }
    }

    @Benchmark
    public void unrolled08() {

        for (int j = 0, nbCols = product.length; j < nbCols; j++) {
            product[j] = DOT.unrolled08(left, 0, right, j * _1_000, 0, _1_000);
        }
    }

    @Benchmark
    public void unrolled16() {

        for (int j = 0, nbCols = product.length; j < nbCols; j++) {
            product[j] = DOT.unrolled16(left, 0, right, j * _1_000, 0, _1_000);
        }
    }

}
