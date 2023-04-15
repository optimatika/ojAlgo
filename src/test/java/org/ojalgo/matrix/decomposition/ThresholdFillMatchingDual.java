/*
 * Copyright 1997-2023 Optimatika
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

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.array.operation.FillMatchingDual;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro. 2015-06-24 => 256
 *
 * <pre>
# Run complete. Total time: 00:03:31

Benchmark                       (dim)  (z)   Mode  Cnt         Score        Error    Units
ThresholdFillMatchingDual.tune     64    1  thrpt    5  14616456.553 ±  81455.181  ops/min
ThresholdFillMatchingDual.tune     64    2  thrpt    5   1107633.595 ±  13117.100  ops/min
ThresholdFillMatchingDual.tune    128    1  thrpt    5   2956027.652 ± 258260.203  ops/min
ThresholdFillMatchingDual.tune    128    2  thrpt    5    974803.689 ±  10291.024  ops/min
ThresholdFillMatchingDual.tune    256    1  thrpt    5    657323.885 ±  30483.095  ops/min
ThresholdFillMatchingDual.tune    256    2  thrpt    5    688971.995 ±   7524.894  ops/min
ThresholdFillMatchingDual.tune    512    1  thrpt    5    157099.731 ±  15573.810  ops/min
ThresholdFillMatchingDual.tune    512    2  thrpt    5    302458.026 ±   4549.699  ops/min
ThresholdFillMatchingDual.tune   1024    1  thrpt    5     17915.722 ±    818.355  ops/min
ThresholdFillMatchingDual.tune   1024    2  thrpt    5     37552.676 ±    414.989  ops/min
ThresholdFillMatchingDual.tune   2048    1  thrpt    5      4416.115 ±    109.324  ops/min
ThresholdFillMatchingDual.tune   2048    2  thrpt    5      9901.855 ±    513.071  ops/min
ThresholdFillMatchingDual.tune   4096    1  thrpt    5      1112.311 ±     18.534  ops/min
ThresholdFillMatchingDual.tune   4096    2  thrpt    5      2327.984 ±    107.298  ops/min
 * </pre>
 *
 * MacBook Pro. 2015-06-26 => 512
 *
 * <pre>
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdFillMatchingDual extends AbstractThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdFillMatchingDual.class);
    }

    @Param({ "64", "128", "256", "512", "1024", "2048", "4096" })
    public int dim;

    @Param({ "1", "2" })
    public int z;

    Primitive64Store left;
    Primitive64Store right;
    Primitive64Store target;

    @Override
    @Setup
    public void setup() {

        FillMatchingDual.THRESHOLD = dim / z;

        final Uniform tmpSupplier = new Uniform();

        left = Primitive64Store.FACTORY.makeFilled(dim, dim, tmpSupplier);
        right = Primitive64Store.FACTORY.makeFilled(dim, dim, tmpSupplier);
        target = Primitive64Store.FACTORY.make(dim, dim);
    }

    @Override
    @Benchmark
    public Object tune() {
        target.fillMatching(left, PrimitiveMath.MULTIPLY, right);
        return target;
    }

}
