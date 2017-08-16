/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.LinearAlgebraBenchmark;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.operation.MultiplyLeft;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro. 2017-08-16 => 16
 *
 * <pre>
# Run complete. Total time: 00:01:28

Benchmark                   (dim)  (z)   Mode  Cnt        Score         Error    Units
ThresholdMultiplyLeft.tune      8    1  thrpt    3  7486576.379 ±   50550.285  ops/min
ThresholdMultiplyLeft.tune      8    2  thrpt    3  1256364.920 ±   15629.756  ops/min
ThresholdMultiplyLeft.tune     16    1  thrpt    3  6034091.209 ±  449494.011  ops/min
ThresholdMultiplyLeft.tune     16    2  thrpt    3  1219166.636 ±   79791.638  ops/min
ThresholdMultiplyLeft.tune     32    1  thrpt    3   931258.161 ± 2242906.125  ops/min
ThresholdMultiplyLeft.tune     32    2  thrpt    3   942363.439 ±  294455.026  ops/min
ThresholdMultiplyLeft.tune     64    1  thrpt    3   158591.994 ±    1437.316  ops/min
ThresholdMultiplyLeft.tune     64    2  thrpt    3   412682.819 ±  143663.913  ops/min
ThresholdMultiplyLeft.tune    128    1  thrpt    3    22318.964 ±     357.957  ops/min
ThresholdMultiplyLeft.tune    128    2  thrpt    3    77806.891 ±    1061.067  ops/min
 * </pre>
 *
 * MacBook Air. 2015-06-27 => 32
 *
 * <pre>
# Run complete. Total time: 00:01:25

Benchmark                   (dim)  (z)   Mode  Cnt         Score         Error    Units
ThresholdMultiplyLeft.tune      8    1  thrpt    3  17735108,881 ±  879865,876  ops/min
ThresholdMultiplyLeft.tune      8    2  thrpt    3   1103305,318 ±  784771,896  ops/min
ThresholdMultiplyLeft.tune     16    1  thrpt    3   9043222,483 ± 4440280,902  ops/min
ThresholdMultiplyLeft.tune     16    2  thrpt    3    989021,285 ±  893301,002  ops/min
ThresholdMultiplyLeft.tune     32    1  thrpt    3   1431336,096 ±  491276,178  ops/min
ThresholdMultiplyLeft.tune     32    2  thrpt    3    770786,217 ±  654645,207  ops/min
ThresholdMultiplyLeft.tune     64    1  thrpt    3    192464,841 ±   46965,287  ops/min
ThresholdMultiplyLeft.tune     64    2  thrpt    3    304053,329 ±  166260,345  ops/min
ThresholdMultiplyLeft.tune    128    1  thrpt    3     24843,283 ±   10248,266  ops/min
ThresholdMultiplyLeft.tune    128    2  thrpt    3     56610,220 ±    4850,227  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdMultiplyLeft extends AbstractThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        LinearAlgebraBenchmark.run(ThresholdMultiplyLeft.class);
    }

    @Param({ "8", "16", "32", "64", "128" })
    public int dim;

    @Param({ "1", "2" })
    public int z;

    MatrixStore<Double> left;
    MatrixStore<Double> right;
    PhysicalStore<Double> target;

    @Override
    @Setup
    public void setup() {

        MultiplyLeft.THRESHOLD = dim / z;

        final Uniform tmpSupplier = new Uniform();

        left = PrimitiveDenseStore.FACTORY.makeFilled(dim, dim, tmpSupplier).transpose();
        right = PrimitiveDenseStore.FACTORY.makeFilled(dim, dim, tmpSupplier);
        target = PrimitiveDenseStore.FACTORY.makeZero(dim, dim);
    }

    @Override
    @Benchmark
    public Object tune() {
        target.fillByMultiplying(left, right);
        return target;
    };

}
