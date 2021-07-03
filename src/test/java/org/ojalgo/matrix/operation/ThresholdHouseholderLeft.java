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
 * Mac Pro: 2015-06-24 => 128
 *
 * <pre>
 * # Run complete. Total time: 00:01:39
 *
 * Benchmark                           (dim)  (z)   Mode  Cnt      Score      Error    Units
 * ThresholdHouseholderLeft.decompose     64    1  thrpt    3  99922,105 ± 4963,112  ops/min
 * ThresholdHouseholderLeft.decompose     64    2  thrpt    3  37139,205 ± 1547,954  ops/min
 * ThresholdHouseholderLeft.decompose    128    1  thrpt    3  12635,520 ±  250,008  ops/min
 * ThresholdHouseholderLeft.decompose    128    2  thrpt    3  11527,097 ±  281,880  ops/min
 * ThresholdHouseholderLeft.decompose    256    1  thrpt    3   1675,460 ±   21,261  ops/min
 * ThresholdHouseholderLeft.decompose    256    2  thrpt    3   2312,005 ±  172,481  ops/min
 * ThresholdHouseholderLeft.decompose    512    1  thrpt    3    215,570 ±   27,005  ops/min
 * ThresholdHouseholderLeft.decompose    512    2  thrpt    3    384,772 ±   16,522  ops/min
 * ThresholdHouseholderLeft.decompose   1024    1  thrpt    3     25,558 ±   18,280  ops/min
 * ThresholdHouseholderLeft.decompose   1024    2  thrpt    3     50,403 ±    1,336  ops/min
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2021-07-03 => 128, maybe 64
 *
 * <pre>
Benchmark                      (dim)  (z)   Mode  Cnt        Score       Error    Units
ThresholdHouseholderLeft.tune     32    1  thrpt    3  1051183.673 ± 49191.323  ops/min
ThresholdHouseholderLeft.tune     32    2  thrpt    3   305679.211 ± 16514.372  ops/min
ThresholdHouseholderLeft.tune     64    1  thrpt    3   153534.040 ±  9529.241  ops/min
ThresholdHouseholderLeft.tune     64    2  thrpt    3   100912.411 ±  7704.396  ops/min
ThresholdHouseholderLeft.tune    128    1  thrpt    3    22405.663 ±   477.055  ops/min
ThresholdHouseholderLeft.tune    128    2  thrpt    3    23413.001 ±  1091.634  ops/min
ThresholdHouseholderLeft.tune    256    1  thrpt    3     2914.775 ±    25.036  ops/min
ThresholdHouseholderLeft.tune    256    2  thrpt    3     3849.013 ±   212.316  ops/min
ThresholdHouseholderLeft.tune    512    1  thrpt    3      372.784 ±    11.004  ops/min
ThresholdHouseholderLeft.tune    512    2  thrpt    3      540.688 ±    49.843  ops/min
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

    @Param({ "32", "64", "128", "256", "512" })
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
