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
 * MacBook Pro (16-inch, 2019): 2021-07-04 => 64
 *
 * <pre>
Benchmark                      (dim)  (z)   Mode  Cnt        Score        Error    Units
ThresholdHouseholderLeft.tune     32    1  thrpt    3  1065669.953 ±  68849.504  ops/min
ThresholdHouseholderLeft.tune     32    2  thrpt    3   301045.609 ± 116468.853  ops/min
ThresholdHouseholderLeft.tune     32    4  thrpt    3   144178.136 ±   7511.837  ops/min
ThresholdHouseholderLeft.tune     64    1  thrpt    3   149282.407 ±  94620.604  ops/min
ThresholdHouseholderLeft.tune     64    2  thrpt    3   100683.021 ±   8034.743  ops/min
ThresholdHouseholderLeft.tune     64    4  thrpt    3    56954.030 ±  14410.421  ops/min
ThresholdHouseholderLeft.tune    128    1  thrpt    3    22527.363 ±    627.945  ops/min
ThresholdHouseholderLeft.tune    128    2  thrpt    3    23640.128 ±   2046.596  ops/min
ThresholdHouseholderLeft.tune    128    4  thrpt    3    19174.137 ±   3315.871  ops/min
ThresholdHouseholderLeft.tune    256    1  thrpt    3     2890.183 ±    170.201  ops/min
ThresholdHouseholderLeft.tune    256    2  thrpt    3     3826.749 ±    356.905  ops/min
ThresholdHouseholderLeft.tune    256    4  thrpt    3     4380.464 ±    963.782  ops/min
ThresholdHouseholderLeft.tune    512    1  thrpt    3      364.338 ±    220.880  ops/min
ThresholdHouseholderLeft.tune    512    2  thrpt    3      519.076 ±     21.333  ops/min
ThresholdHouseholderLeft.tune    512    4  thrpt    3      766.312 ±    215.441  ops/min
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
