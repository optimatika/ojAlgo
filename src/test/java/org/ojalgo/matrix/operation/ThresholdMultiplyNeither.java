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
 * MacBook Pro (16-inch, 2019): 2021-07-19 => 32
 *
 * <pre>
Benchmark                      (dim)  (z)   Mode  Cnt          Score         Error    Units
ThresholdMultiplyNeither.tune      8    1  thrpt    3  329143532.371 ± 8081114.486  ops/min
ThresholdMultiplyNeither.tune      8    2  thrpt    3    5274249.808 ±  332192.154  ops/min
ThresholdMultiplyNeither.tune      8    4  thrpt    3    2941137.564 ± 1088718.511  ops/min
ThresholdMultiplyNeither.tune     16    1  thrpt    3   23029954.309 ± 6123392.045  ops/min
ThresholdMultiplyNeither.tune     16    2  thrpt    3    4907197.704 ± 2725316.417  ops/min
ThresholdMultiplyNeither.tune     16    4  thrpt    3    2829635.574 ±  338626.686  ops/min
ThresholdMultiplyNeither.tune     32    1  thrpt    3    3291888.141 ±  689253.631  ops/min
ThresholdMultiplyNeither.tune     32    2  thrpt    3    2620563.560 ±  366185.744  ops/min
ThresholdMultiplyNeither.tune     32    4  thrpt    3    2030877.577 ±  800971.434  ops/min
ThresholdMultiplyNeither.tune     64    1  thrpt    3     480794.827 ±   25598.245  ops/min
ThresholdMultiplyNeither.tune     64    2  thrpt    3     671739.271 ±   37969.385  ops/min
ThresholdMultiplyNeither.tune     64    4  thrpt    3     833359.571 ±  130093.930  ops/min
ThresholdMultiplyNeither.tune    128    1  thrpt    3      62390.313 ±    9636.965  ops/min
ThresholdMultiplyNeither.tune    128    2  thrpt    3     108447.628 ±    6690.205  ops/min
ThresholdMultiplyNeither.tune    128    4  thrpt    3     165042.252 ±   50054.999  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdMultiplyNeither extends ThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdTuner.options(), ThresholdMultiplyNeither.class);
    }

    @Param({ "16", "32", "64", "128" })
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
