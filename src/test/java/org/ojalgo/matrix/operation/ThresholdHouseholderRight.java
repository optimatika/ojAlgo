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
package org.ojalgo.matrix.operation;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.matrix.decomposition.Bidiagonal;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro. 2015-06-24 => 512
 *
 * <pre>
 * # Run complete. Total time: 00:32:32
 *
 * Benchmark                            (dim)  (z)   Mode  Cnt    Score    Error    Units
 * ThresholdHouseholderRight.decompose    512    1  thrpt    3  160,486 ± 18,885  ops/min
 * ThresholdHouseholderRight.decompose    512    2  thrpt    3  157,054 ± 17,449  ops/min
 * ThresholdHouseholderRight.decompose   1024    1  thrpt    3   29,190 ±  1,876  ops/min
 * ThresholdHouseholderRight.decompose   1024    2  thrpt    3   30,504 ± 17,942  ops/min
 * ThresholdHouseholderRight.decompose   2048    1  thrpt    3    3,960 ±  0,354  ops/min
 * ThresholdHouseholderRight.decompose   2048    2  thrpt    3    4,465 ±  0,299  ops/min
 * ThresholdHouseholderRight.decompose   4096    1  thrpt    3    0,539 ±  0,036  ops/min
 * ThresholdHouseholderRight.decompose   4096    2  thrpt    3    0,626 ±  0,013  ops/min
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2021-07-04 => 512
 *
 * <pre>
Benchmark                       (dim)  (z)   Mode  Cnt     Score      Error    Units
ThresholdHouseholderRight.tune    128    1  thrpt    3  9510.973 ±  468.728  ops/min
ThresholdHouseholderRight.tune    128    2  thrpt    3  7262.200 ± 3798.119  ops/min
ThresholdHouseholderRight.tune    128    4  thrpt    3  4891.214 ± 1662.073  ops/min
ThresholdHouseholderRight.tune    256    1  thrpt    3  1895.646 ±  468.174  ops/min
ThresholdHouseholderRight.tune    256    2  thrpt    3  1514.779 ±  478.991  ops/min
ThresholdHouseholderRight.tune    256    4  thrpt    3  1126.434 ±  283.917  ops/min
ThresholdHouseholderRight.tune    512    1  thrpt    3   354.724 ±  116.550  ops/min
ThresholdHouseholderRight.tune    512    2  thrpt    3   339.003 ±    3.934  ops/min
ThresholdHouseholderRight.tune    512    4  thrpt    3   239.940 ±  146.999  ops/min
ThresholdHouseholderRight.tune   1024    1  thrpt    3    54.753 ±   14.236  ops/min
ThresholdHouseholderRight.tune   1024    2  thrpt    3    66.600 ±    3.543  ops/min
ThresholdHouseholderRight.tune   1024    4  thrpt    3    53.725 ±    3.405  ops/min
ThresholdHouseholderRight.tune   2048    1  thrpt    3     6.124 ±    0.447  ops/min
ThresholdHouseholderRight.tune   2048    2  thrpt    3     7.451 ±    1.540  ops/min
ThresholdHouseholderRight.tune   2048    4  thrpt    3     7.081 ±    0.699  ops/min
 * </pre>
 *
 * old/alternate code
 *
 * <pre>
Benchmark                       (dim)  (z)   Mode  Cnt     Score      Error    Units
ThresholdHouseholderRight.tune    128    1  thrpt    3  7502.753 ±  565.919  ops/min
ThresholdHouseholderRight.tune    128    2  thrpt    3  7337.924 ± 2539.952  ops/min
ThresholdHouseholderRight.tune    128    4  thrpt    3  5124.327 ±  995.457  ops/min
ThresholdHouseholderRight.tune    256    1  thrpt    3  1029.643 ±   45.015  ops/min
ThresholdHouseholderRight.tune    256    2  thrpt    3  1297.135 ±  365.526  ops/min
ThresholdHouseholderRight.tune    256    4  thrpt    3  1251.378 ±   47.748  ops/min
ThresholdHouseholderRight.tune    512    1  thrpt    3   133.748 ±   15.840  ops/min
ThresholdHouseholderRight.tune    512    2  thrpt    3   200.476 ±   59.299  ops/min
ThresholdHouseholderRight.tune    512    4  thrpt    3   229.117 ±  107.045  ops/min
ThresholdHouseholderRight.tune   1024    1  thrpt    3    17.097 ±    3.473  ops/min
ThresholdHouseholderRight.tune   1024    2  thrpt    3    27.048 ±    8.516  ops/min
ThresholdHouseholderRight.tune   1024    4  thrpt    3    32.631 ±   12.436  ops/min
ThresholdHouseholderRight.tune   2048    1  thrpt    3     1.582 ±    1.027  ops/min
ThresholdHouseholderRight.tune   2048    2  thrpt    3     2.350 ±    0.394  ops/min
ThresholdHouseholderRight.tune   2048    4  thrpt    3     3.020 ±    1.307  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdHouseholderRight extends ThresholdTuner {

    static final class CodeAndData {

        Bidiagonal<Double> decomposition;
        MatrixStore<Double> matrix;

        CodeAndData(final int dim) {

            super();

            matrix = Primitive64Store.FACTORY.makeSPD(dim).below(Primitive64Store.FACTORY.makeIdentity(dim)).copy();

            decomposition = Bidiagonal.PRIMITIVE.make(matrix);
        }

        Boolean tune() {
            if (decomposition.decompose(matrix)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
    }

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdTuner.options(), ThresholdHouseholderRight.class);
    }

    @Param({ "128", "256", "512", "1024", "2048" })
    public int dim;

    CodeAndData benchmark;

    @Override
    @Setup
    public void setup() {

        HouseholderRight.THRESHOLD = dim / z;

        benchmark = new CodeAndData(dim);
    }

    @Override
    @Benchmark
    public Object tune() {
        return benchmark.tune();
    }

}
