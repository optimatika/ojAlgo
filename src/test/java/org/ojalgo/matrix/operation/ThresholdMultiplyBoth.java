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
 * Mac Pro. 2015-06-24 => ?
 *
 * <pre>
 * </pre>
 *
 * MacBook Air. 2015-06-27 => 16
 *
 * <pre>
# Run complete. Total time: 00:01:25

Benchmark                   (dim)  (z)   Mode  Cnt         Score         Error    Units
ThresholdMultiplyBoth.tune      8    1  thrpt    3  17128292,840 ± 2991881,643  ops/min
ThresholdMultiplyBoth.tune      8    2  thrpt    3   1131723,502 ±  564612,285  ops/min
ThresholdMultiplyBoth.tune     16    1  thrpt    3   2257845,836 ± 1237077,126  ops/min
ThresholdMultiplyBoth.tune     16    2  thrpt    3    848425,485 ±  361964,884  ops/min
ThresholdMultiplyBoth.tune     32    1  thrpt    3    300695,106 ±   22419,156  ops/min
ThresholdMultiplyBoth.tune     32    2  thrpt    3    366420,327 ±  132391,475  ops/min
ThresholdMultiplyBoth.tune     64    1  thrpt    3     38720,797 ±   12963,650  ops/min
ThresholdMultiplyBoth.tune     64    2  thrpt    3     66750,322 ±   11931,145  ops/min
ThresholdMultiplyBoth.tune    128    1  thrpt    3      4805,198 ±     406,351  ops/min
ThresholdMultiplyBoth.tune    128    2  thrpt    3      8763,536 ±     572,748  ops/min
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2021-07-08 => 8
 *
 * <pre>
Benchmark                   (dim)  (z)   Mode  Cnt          Score          Error    Units
ThresholdMultiplyBoth.tune      4    1  thrpt    3  216618439.222 ± 11873393.331  ops/min
ThresholdMultiplyBoth.tune      4    2  thrpt    3    5983596.437 ±  3720592.577  ops/min
ThresholdMultiplyBoth.tune      4    4  thrpt    3    2753827.167 ±  2319085.959  ops/min
ThresholdMultiplyBoth.tune      8    1  thrpt    3   13645736.087 ±  6426679.023  ops/min
ThresholdMultiplyBoth.tune      8    2  thrpt    3    4559637.236 ±   678795.706  ops/min
ThresholdMultiplyBoth.tune      8    4  thrpt    3    2391485.649 ±   235221.563  ops/min
ThresholdMultiplyBoth.tune     16    1  thrpt    3    1851686.554 ±   101602.848  ops/min
ThresholdMultiplyBoth.tune     16    2  thrpt    3    1966505.496 ±   479966.666  ops/min
ThresholdMultiplyBoth.tune     16    4  thrpt    3    1741386.822 ±   116972.129  ops/min
ThresholdMultiplyBoth.tune     32    1  thrpt    3     227853.857 ±   100274.833  ops/min
ThresholdMultiplyBoth.tune     32    2  thrpt    3     380500.664 ±    34137.501  ops/min
ThresholdMultiplyBoth.tune     32    4  thrpt    3     517229.892 ±   142965.411  ops/min
ThresholdMultiplyBoth.tune     64    1  thrpt    3      30466.670 ±    21871.552  ops/min
ThresholdMultiplyBoth.tune     64    2  thrpt    3      52805.910 ±     3355.346  ops/min
ThresholdMultiplyBoth.tune     64    4  thrpt    3      83292.909 ±     6241.039  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdMultiplyBoth extends ThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdTuner.options(), ThresholdMultiplyBoth.class);
    }

    @Param({ "4", "8", "16", "32", "64" })
    public int dim;

    MatrixStore<Double> left;
    MatrixStore<Double> right;
    PhysicalStore<Double> target;

    @Override
    @Setup
    public void setup() {

        MultiplyBoth.THRESHOLD = dim / z;

        final Uniform tmpSupplier = new Uniform();

        left = Primitive64Store.FACTORY.makeFilled(dim, dim, tmpSupplier).transpose();
        right = Primitive64Store.FACTORY.makeFilled(dim, dim, tmpSupplier).transpose();
        target = Primitive64Store.FACTORY.make(dim, dim);
    }

    @Override
    @Benchmark
    public Object tune() {
        target.fillByMultiplying(left, right);
        return target;
    }

}
