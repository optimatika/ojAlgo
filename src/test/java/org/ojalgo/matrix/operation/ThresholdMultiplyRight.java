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
 * Mac Pro. 2015-06-24 => ?
 *
 * <pre>
 * </pre>
 *
 * MacBook Air. 2015-06-27 => 32
 *
 * <pre>
# Run complete. Total time: 00:01:25

Benchmark                    (dim)  (z)   Mode  Cnt         Score          Error    Units
ThresholdMultiplyRight.tune      8    1  thrpt    3  73994274,805 ± 56632052,355  ops/min
ThresholdMultiplyRight.tune      8    2  thrpt    3   1063917,938 ±   859526,602  ops/min
ThresholdMultiplyRight.tune     16    1  thrpt    3   6462490,133 ±  3023992,427  ops/min
ThresholdMultiplyRight.tune     16    2  thrpt    3   1027540,843 ±  1884022,995  ops/min
ThresholdMultiplyRight.tune     32    1  thrpt    3    968094,105 ±  1658828,115  ops/min
ThresholdMultiplyRight.tune     32    2  thrpt    3    795953,054 ±   281585,089  ops/min
ThresholdMultiplyRight.tune     64    1  thrpt    3    194669,580 ±    28140,716  ops/min
ThresholdMultiplyRight.tune     64    2  thrpt    3    275349,177 ±    94609,126  ops/min
ThresholdMultiplyRight.tune    128    1  thrpt    3     25848,018 ±     6415,269  ops/min
ThresholdMultiplyRight.tune    128    2  thrpt    3     45033,847 ±    11734,425  ops/min
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2021-07-08 => 16
 *
 * <pre>
Benchmark                    (dim)  (z)   Mode  Cnt         Score          Error    Units
ThresholdMultiplyRight.tune      8    1  thrpt    3  82784837.438 ± 65640478.006  ops/min
ThresholdMultiplyRight.tune      8    2  thrpt    3   5409713.904 ±   221597.990  ops/min
ThresholdMultiplyRight.tune      8    4  thrpt    3   2735482.295 ±  2107889.049  ops/min
ThresholdMultiplyRight.tune     16    1  thrpt    3  10386710.053 ±  2147856.458  ops/min
ThresholdMultiplyRight.tune     16    2  thrpt    3   4276427.264 ±  1148125.057  ops/min
ThresholdMultiplyRight.tune     16    4  thrpt    3   2250778.269 ±   445136.305  ops/min
ThresholdMultiplyRight.tune     32    1  thrpt    3   1741201.140 ±  1174160.477  ops/min
ThresholdMultiplyRight.tune     32    2  thrpt    3   1672839.257 ±   435739.598  ops/min
ThresholdMultiplyRight.tune     32    4  thrpt    3   1542568.784 ±   131129.199  ops/min
ThresholdMultiplyRight.tune     64    1  thrpt    3    254583.739 ±   283035.017  ops/min
ThresholdMultiplyRight.tune     64    2  thrpt    3    388499.264 ±    52184.247  ops/min
ThresholdMultiplyRight.tune     64    4  thrpt    3    477775.949 ±   304680.019  ops/min
ThresholdMultiplyRight.tune    128    1  thrpt    3     35783.104 ±    26376.655  ops/min
ThresholdMultiplyRight.tune    128    2  thrpt    3     63085.873 ±     3261.578  ops/min
ThresholdMultiplyRight.tune    128    4  thrpt    3     93742.973 ±     2895.409  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdMultiplyRight extends ThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdTuner.options(), ThresholdMultiplyRight.class);
    }

    @Param({ "8", "16", "32", "64", "128" })
    public int dim;

    MatrixStore<Double> left;
    MatrixStore<Double> right;
    PhysicalStore<Double> target;

    @Override
    @Setup
    public void setup() {

        MultiplyRight.THRESHOLD = dim / z;

        Uniform tmpSupplier = new Uniform();

        left = Primitive64Store.FACTORY.makeFilled(dim, dim, tmpSupplier);
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
