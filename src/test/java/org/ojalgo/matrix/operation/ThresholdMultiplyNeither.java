/*
 * Copyright 1997-2021 Optimatika
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
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * <h1>MacBook Pro</h1>
 *
 * <pre>
 * </pre>
 *
 * <h1>Mac Pro</h1> 2017-11-28 => 32
 *
 * <pre>
Result "org.ojalgo.matrix.decomposition.ThresholdMultiplyNeither.tune":
  66277.842 ±(99.9%) 836.778 ops/min [Average]
  (min, avg, max) = (65911.056, 66277.842, 66462.264), stdev = 217.309
  CI (99.9%): [65441.064, 67114.621] (assumes normal distribution)


# Run complete. Total time: 00:02:27

Benchmark                      (dim)  (z)   Mode  Cnt          Score        Error    Units
ThresholdMultiplyNeither.tune      8    1  thrpt    5  100165562.893 ± 487581.344  ops/min
ThresholdMultiplyNeither.tune      8    2  thrpt    5    1224455.193 ±  17242.789  ops/min
ThresholdMultiplyNeither.tune     16    1  thrpt    5    6103235.893 ± 121424.112  ops/min
ThresholdMultiplyNeither.tune     16    2  thrpt    5    1198383.898 ±  40991.241  ops/min
ThresholdMultiplyNeither.tune     32    1  thrpt    5     913934.467 ±   2224.720  ops/min
ThresholdMultiplyNeither.tune     32    2  thrpt    5     906993.004 ±  23080.158  ops/min
ThresholdMultiplyNeither.tune     64    1  thrpt    5     118052.610 ±    153.411  ops/min
ThresholdMultiplyNeither.tune     64    2  thrpt    5     356521.307 ±   4806.156  ops/min
ThresholdMultiplyNeither.tune    128    1  thrpt    5      21722.580 ±  26793.071  ops/min
ThresholdMultiplyNeither.tune    128    2  thrpt    5      66277.842 ±    836.778  ops/min
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2021-07-08 => 16
 *
 * <pre>
Benchmark                      (dim)  (z)   Mode  Cnt          Score           Error    Units
ThresholdMultiplyNeither.tune      8    1  thrpt    3  320517801.752 ± 109454851.232  ops/min
ThresholdMultiplyNeither.tune      8    2  thrpt    3    5262504.922 ±   8655895.967  ops/min
ThresholdMultiplyNeither.tune      8    4  thrpt    3    2639780.469 ±    237580.224  ops/min
ThresholdMultiplyNeither.tune     16    1  thrpt    3   16174533.544 ±  11892145.698  ops/min
ThresholdMultiplyNeither.tune     16    2  thrpt    3    4401672.135 ±   2772814.674  ops/min
ThresholdMultiplyNeither.tune     16    4  thrpt    3    2302383.934 ±    938891.592  ops/min
ThresholdMultiplyNeither.tune     32    1  thrpt    3    2287568.482 ±   2097808.868  ops/min
ThresholdMultiplyNeither.tune     32    2  thrpt    3    2454889.623 ±   1452680.453  ops/min
ThresholdMultiplyNeither.tune     32    4  thrpt    3    1840110.930 ±    111883.096  ops/min
ThresholdMultiplyNeither.tune     64    1  thrpt    3     313790.575 ±    172920.018  ops/min
ThresholdMultiplyNeither.tune     64    2  thrpt    3     535867.427 ±    276422.663  ops/min
ThresholdMultiplyNeither.tune     64    4  thrpt    3     656074.495 ±     18432.218  ops/min
ThresholdMultiplyNeither.tune    128    1  thrpt    3      37724.564 ±     28104.934  ops/min
ThresholdMultiplyNeither.tune    128    2  thrpt    3      82951.381 ±       506.592  ops/min
ThresholdMultiplyNeither.tune    128    4  thrpt    3     129057.015 ±     60779.708  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdMultiplyNeither extends ThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdTuner.options(), ThresholdMultiplyNeither.class);
    }

    @Param({ "8", "16", "32", "64", "128" })
    public int dim;

    MatrixStore<Double> left;
    MatrixStore<Double> right;
    PhysicalStore<Double> target;

    @Override
    @Setup
    public void setup() {

        MultiplyNeither.THRESHOLD = dim / z;

        final Uniform tmpSupplier = new Uniform();

        left = Primitive64Store.FACTORY.makeFilled(dim, dim, tmpSupplier);
        right = Primitive64Store.FACTORY.makeFilled(dim, dim, tmpSupplier);
        target = Primitive64Store.FACTORY.make(dim, dim);
    }

    @Override
    @Benchmark
    public Object tune() {
        target.fillByMultiplying(left, right);
        return target;
    }

}
