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
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro (Early 2009): 2022-01-13 => 64 (128)
 *
 * <pre>
Benchmark                      (dim)  (z)   Mode  Cnt       Score       Error    Units
ThresholdHouseholderLeft.tune     32    1  thrpt    3  504704.866 ± 32977.091  ops/min
ThresholdHouseholderLeft.tune     32    2  thrpt    3  138829.022 ± 10947.430  ops/min
ThresholdHouseholderLeft.tune     32    4  thrpt    3   55622.151 ±  2847.763  ops/min
ThresholdHouseholderLeft.tune     64    1  thrpt    3   86400.158 ±  9551.850  ops/min
ThresholdHouseholderLeft.tune     64    2  thrpt    3   47139.867 ±  2806.197  ops/min
ThresholdHouseholderLeft.tune     64    4  thrpt    3   21252.819 ±   826.063  ops/min
ThresholdHouseholderLeft.tune    128    1  thrpt    3   12829.606 ±   420.210  ops/min
ThresholdHouseholderLeft.tune    128    2  thrpt    3   12016.348 ±   969.765  ops/min
ThresholdHouseholderLeft.tune    128    4  thrpt    3    8395.980 ±  1691.322  ops/min
ThresholdHouseholderLeft.tune    256    1  thrpt    3    1672.029 ±    46.116  ops/min
ThresholdHouseholderLeft.tune    256    2  thrpt    3    2181.757 ±   114.159  ops/min
ThresholdHouseholderLeft.tune    256    4  thrpt    3    2194.723 ±  1463.957  ops/min
ThresholdHouseholderLeft.tune    512    1  thrpt    3     221.181 ±     2.988  ops/min
ThresholdHouseholderLeft.tune    512    2  thrpt    3     329.581 ±    20.506  ops/min
ThresholdHouseholderLeft.tune    512    4  thrpt    3     430.332 ±    55.390  ops/min
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2022-01-13 => 64
 *
 * <pre>
Benchmark                      (dim)  (z)   Mode  Cnt        Score        Error    Units
ThresholdHouseholderLeft.tune     32    1  thrpt    3  1428582.331 ± 373831.670  ops/min
ThresholdHouseholderLeft.tune     32    2  thrpt    3   339301.030 ±  58073.646  ops/min
ThresholdHouseholderLeft.tune     32    4  thrpt    3   142777.495 ±  44775.975  ops/min
ThresholdHouseholderLeft.tune     64    1  thrpt    3   210555.060 ±  30434.438  ops/min
ThresholdHouseholderLeft.tune     64    2  thrpt    3   119029.857 ±  22882.677  ops/min
ThresholdHouseholderLeft.tune     64    4  thrpt    3    66061.076 ±   2155.043  ops/min
ThresholdHouseholderLeft.tune    128    1  thrpt    3    30443.908 ±   3309.796  ops/min
ThresholdHouseholderLeft.tune    128    2  thrpt    3    31494.788 ±   3025.836  ops/min
ThresholdHouseholderLeft.tune    128    4  thrpt    3    24290.269 ±   5805.974  ops/min
ThresholdHouseholderLeft.tune    256    1  thrpt    3     4065.628 ±    953.317  ops/min
ThresholdHouseholderLeft.tune    256    2  thrpt    3     5694.790 ±   1339.554  ops/min
ThresholdHouseholderLeft.tune    256    4  thrpt    3     6260.057 ±   1545.870  ops/min
ThresholdHouseholderLeft.tune    512    1  thrpt    3      529.087 ±     36.135  ops/min
ThresholdHouseholderLeft.tune    512    2  thrpt    3      789.680 ±     32.697  ops/min
ThresholdHouseholderLeft.tune    512    4  thrpt    3     1206.603 ±     46.300  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdHouseholderLeft extends ThresholdTuner {

    static final class CodeAndData {

        Primitive64Store body;
        QR<Double> decomposition;
        Primitive64Store preallocated;
        Primitive64Store rhs;

        CodeAndData(final int dim) {

            super();

            decomposition = QR.PRIMITIVE.make(true);

            body = Primitive64Store.FACTORY.makeFilled(2 * dim, dim, Uniform.standard());
            rhs = Primitive64Store.FACTORY.makeFilled(2 * dim, 1, Uniform.standard());
            preallocated = Primitive64Store.FACTORY.make(2 * dim, 1);
        }

        MatrixStore<Double> tune() {
            decomposition.decompose(body);
            return decomposition.getSolution(rhs, preallocated);
        }
    }

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdTuner.options(), ThresholdHouseholderLeft.class);
    }

    @Param({ "32", "64", "128", "256", "512", "1024" })
    public int dim;

    CodeAndData benchmark;

    @Override
    @Setup
    public void setup() {

        HouseholderLeft.THRESHOLD = dim / z;

        benchmark = new CodeAndData(dim);
    }

    @Override
    @Benchmark
    public Object tune() {
        return benchmark.tune();
    }

}
