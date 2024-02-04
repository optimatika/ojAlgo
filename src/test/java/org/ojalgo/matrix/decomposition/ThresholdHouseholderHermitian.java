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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.array.operation.HermitianRank2Update;
import org.ojalgo.array.operation.MultiplyHermitianAndVector;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro: 2017-03-15 => 256
 *
 * <pre>
# Run complete. Total time: 00:06:05

Benchmark                           (dim)  (z)   Mode  Cnt    Score   Error  Units
ThresholdHouseholderHermitian.tune    128    1  thrpt   15  470.734 ± 1.640  ops/s
ThresholdHouseholderHermitian.tune    128    2  thrpt   15  153.692 ± 3.111  ops/s
ThresholdHouseholderHermitian.tune    256    1  thrpt   15   61.165 ± 0.501  ops/s
ThresholdHouseholderHermitian.tune    256    2  thrpt   15   44.372 ± 0.181  ops/s
ThresholdHouseholderHermitian.tune    512    1  thrpt   15    7.366 ± 0.032  ops/s
ThresholdHouseholderHermitian.tune    512    2  thrpt   15    8.186 ± 0.064  ops/s
ThresholdHouseholderHermitian.tune   1024    1  thrpt   15    0.488 ± 0.001  ops/s
ThresholdHouseholderHermitian.tune   1024    2  thrpt   15    0.727 ± 0.006  ops/s
 * </pre>
 *
 * MacBook Air: 2017-03-15
 *
 * <pre>
# Run complete. Total time: 00:07:51

Benchmark                           (dim)  (z)   Mode  Cnt     Score    Error  Units
ThresholdHouseholderHermitian.tune     64    1  thrpt   15  3132.855 ± 53.737  ops/s
ThresholdHouseholderHermitian.tune     64    2  thrpt   15   287.174 ± 12.451  ops/s
ThresholdHouseholderHermitian.tune    128    1  thrpt   15   481.848 ± 16.648  ops/s
ThresholdHouseholderHermitian.tune    128    2  thrpt   15   125.416 ±  8.888  ops/s
ThresholdHouseholderHermitian.tune    256    1  thrpt   15    63.603 ±  0.926  ops/s
ThresholdHouseholderHermitian.tune    256    2  thrpt   15    39.505 ±  1.353  ops/s
ThresholdHouseholderHermitian.tune    512    1  thrpt   15     7.385 ±  0.216  ops/s
ThresholdHouseholderHermitian.tune    512    2  thrpt   15     7.877 ±  0.317  ops/s
ThresholdHouseholderHermitian.tune   1024    1  thrpt   15     0.409 ±  0.004  ops/s
ThresholdHouseholderHermitian.tune   1024    2  thrpt   15     0.581 ±  0.029  ops/s
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdHouseholderHermitian extends AbstractThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdHouseholderHermitian.class);
    }

    @Param({ "128", "256", "512", "1024" })
    public int dim;

    @Param({ "1", "2" })
    public int z;

    Tridiagonal<Double> decomposition = new DeferredTridiagonal.R064();

    MatrixStore<Double> matrix;

    @Override
    @Setup
    public void setup() {

        MultiplyHermitianAndVector.THRESHOLD = dim / z;
        HermitianRank2Update.THRESHOLD = dim / z;
        final int dim1 = dim;

        matrix = Primitive64Store.FACTORY.makeSPD(dim1);
    }

    @Override
    @Benchmark
    public Object tune() {
        return decomposition.decompose(matrix);
    };

}
