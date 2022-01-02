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
 * MacBook Pro (16-inch, 2019): 2021-07-08 => 32
 *
 * <pre>
Benchmark                    (dim)  (z)   Mode  Cnt         Score         Error    Units
ThresholdMultiplyRight.tune      8    1  thrpt    3  86856851.754 ± 2245608.193  ops/min
ThresholdMultiplyRight.tune      8    2  thrpt    3   5654901.954 ±  236802.326  ops/min
ThresholdMultiplyRight.tune      8    4  thrpt    3   2868836.300 ±  306341.286  ops/min
ThresholdMultiplyRight.tune     16    1  thrpt    3  12397523.317 ± 2793006.627  ops/min
ThresholdMultiplyRight.tune     16    2  thrpt    3   4597485.749 ±  101466.856  ops/min
ThresholdMultiplyRight.tune     16    4  thrpt    3   2628861.968 ±  323306.409  ops/min
ThresholdMultiplyRight.tune     32    1  thrpt    3   2273712.338 ±  296654.555  ops/min
ThresholdMultiplyRight.tune     32    2  thrpt    3   2244136.445 ±  647437.349  ops/min
ThresholdMultiplyRight.tune     32    4  thrpt    3   1626640.843 ±  514732.840  ops/min
ThresholdMultiplyRight.tune     64    1  thrpt    3    356278.586 ±    6398.783  ops/min
ThresholdMultiplyRight.tune     64    2  thrpt    3    552667.490 ±  101596.595  ops/min
ThresholdMultiplyRight.tune     64    4  thrpt    3    689220.545 ±  275448.930  ops/min
ThresholdMultiplyRight.tune    128    1  thrpt    3     50073.358 ±    7361.293  ops/min
ThresholdMultiplyRight.tune    128    2  thrpt    3     85156.322 ±   13541.683  ops/min
ThresholdMultiplyRight.tune    128    4  thrpt    3    136230.760 ±   42145.165  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdMultiplyRight extends ThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdTuner.options(), ThresholdMultiplyRight.class);
    }

    @Param({ "16", "32", "64", "128" })
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
