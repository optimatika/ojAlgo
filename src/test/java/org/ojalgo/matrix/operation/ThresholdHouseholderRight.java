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
package org.ojalgo.matrix.operation;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.matrix.decomposition.Bidiagonal;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro (Early 2009): 2022-01-13 => 256
 *
 * <pre>
Benchmark                       (dim)  (z)   Mode  Cnt       Score      Error    Units
ThresholdHouseholderRight.tune     64    1  thrpt    3  110612.297 ± 9065.663  ops/min
ThresholdHouseholderRight.tune     64    2  thrpt    3   44077.718 ± 4643.325  ops/min
ThresholdHouseholderRight.tune     64    4  thrpt    3   22107.514 ±  592.447  ops/min
ThresholdHouseholderRight.tune    128    1  thrpt    3    9643.418 ± 3135.729  ops/min
ThresholdHouseholderRight.tune    128    2  thrpt    3    8661.197 ± 1040.084  ops/min
ThresholdHouseholderRight.tune    128    4  thrpt    3    5651.640 ±  250.043  ops/min
ThresholdHouseholderRight.tune    256    1  thrpt    3    1581.234 ±   47.155  ops/min
ThresholdHouseholderRight.tune    256    2  thrpt    3    1458.823 ±   99.917  ops/min
ThresholdHouseholderRight.tune    256    4  thrpt    3    1386.028 ±   85.117  ops/min
ThresholdHouseholderRight.tune    512    1  thrpt    3     258.619 ±   53.413  ops/min
ThresholdHouseholderRight.tune    512    2  thrpt    3     262.324 ±   50.952  ops/min
ThresholdHouseholderRight.tune    512    4  thrpt    3     280.717 ±    7.375  ops/min
ThresholdHouseholderRight.tune   1024    1  thrpt    3      36.970 ±    5.826  ops/min
ThresholdHouseholderRight.tune   1024    2  thrpt    3      40.612 ±    7.569  ops/min
ThresholdHouseholderRight.tune   1024    4  thrpt    3      44.500 ±    2.200  ops/min
ThresholdHouseholderRight.tune   2048    1  thrpt    3       4.651 ±    0.215  ops/min
ThresholdHouseholderRight.tune   2048    2  thrpt    3       5.297 ±    0.479  ops/min
ThresholdHouseholderRight.tune   2048    4  thrpt    3       5.837 ±    1.816  ops/min
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2022-01-13 => 128 (256)
 *
 * <pre>
Benchmark                       (dim)  (z)   Mode  Cnt       Score       Error    Units
ThresholdHouseholderRight.tune     64    1  thrpt    3  282050.993 ± 15109.702  ops/min
ThresholdHouseholderRight.tune     64    2  thrpt    3  107553.881 ± 24204.942  ops/min
ThresholdHouseholderRight.tune     64    4  thrpt    3   62519.219 ±  2201.527  ops/min
ThresholdHouseholderRight.tune    128    1  thrpt    3   26150.846 ±   612.641  ops/min
ThresholdHouseholderRight.tune    128    2  thrpt    3   21585.088 ±  3202.590  ops/min
ThresholdHouseholderRight.tune    128    4  thrpt    3   14967.346 ± 14426.271  ops/min
ThresholdHouseholderRight.tune    256    1  thrpt    3    4352.582 ±   168.386  ops/min
ThresholdHouseholderRight.tune    256    2  thrpt    3    4325.733 ±   170.975  ops/min
ThresholdHouseholderRight.tune    256    4  thrpt    3    4046.382 ±  1189.641  ops/min
ThresholdHouseholderRight.tune    512    1  thrpt    3     713.736 ±    82.786  ops/min
ThresholdHouseholderRight.tune    512    2  thrpt    3     759.927 ±    43.905  ops/min
ThresholdHouseholderRight.tune    512    4  thrpt    3     759.048 ±   149.099  ops/min
ThresholdHouseholderRight.tune   1024    1  thrpt    3     105.325 ±    25.630  ops/min
ThresholdHouseholderRight.tune   1024    2  thrpt    3     118.275 ±    18.851  ops/min
ThresholdHouseholderRight.tune   1024    4  thrpt    3     125.592 ±    24.284  ops/min
ThresholdHouseholderRight.tune   2048    1  thrpt    3      10.245 ±     4.537  ops/min
ThresholdHouseholderRight.tune   2048    2  thrpt    3      12.916 ±     2.133  ops/min
ThresholdHouseholderRight.tune   2048    4  thrpt    3      13.672 ±     0.216  ops/min
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

            matrix = R064Store.FACTORY.makeFilled(dim, dim, Uniform.standard());

            decomposition = Bidiagonal.R064.make(matrix);
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

    @Param({ "64", "128", "256", "512", "1024", "2048" })
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
