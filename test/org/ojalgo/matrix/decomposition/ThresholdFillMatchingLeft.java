/*
 * Copyright 1997-2015 Optimatika
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
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.operation.FillMatchingLeft;
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
 * MacBook Air. 2015-06-26 => 512
 *
 * <pre>
# Run complete. Total time: 00:01:26

Benchmark                       (dim)  (z)   Mode  Cnt        Score         Error    Units
ThresholdFillMatchingLeft.tune    128    1  thrpt    3  7992286,830 ± 3317290,978  ops/min
ThresholdFillMatchingLeft.tune    128    2  thrpt    3  1036970,177 ±  120596,694  ops/min
ThresholdFillMatchingLeft.tune    256    1  thrpt    3  1330731,871 ±  249897,655  ops/min
ThresholdFillMatchingLeft.tune    256    2  thrpt    3   763934,514 ±  233037,987  ops/min
ThresholdFillMatchingLeft.tune    512    1  thrpt    3   160204,493 ±   48410,164  ops/min
ThresholdFillMatchingLeft.tune    512    2  thrpt    3   144239,895 ±   30524,578  ops/min
ThresholdFillMatchingLeft.tune   1024    1  thrpt    3    33640,979 ±    2456,084  ops/min
ThresholdFillMatchingLeft.tune   1024    2  thrpt    3    34657,438 ±    1534,592  ops/min
ThresholdFillMatchingLeft.tune   2048    1  thrpt    3     8430,484 ±    3957,855  ops/min
ThresholdFillMatchingLeft.tune   2048    2  thrpt    3     9055,332 ±    1506,639  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdFillMatchingLeft extends AbstractThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        LinearAlgebraBenchmark.run(ThresholdFillMatchingLeft.class);
    }

    @Param({ "128", "256", "512", "1024", "2048" })
    public int dim;

    @Param({ "1", "2" })
    public int z;

    PhysicalStore<Double> left;
    Double right;
    PhysicalStore<Double> target;

    @Override
    @Setup
    public void setup() {

        FillMatchingLeft.THRESHOLD = dim / z;

        final Uniform tmpSupplier = new Uniform();

        left = PrimitiveDenseStore.FACTORY.makeFilled(dim, dim, tmpSupplier);
        right = tmpSupplier.doubleValue();
        target = PrimitiveDenseStore.FACTORY.makeZero(dim, dim);
    }

    @Override
    @Benchmark
    public Object tune() {
        // target.fillMatching(left, PrimitiveFunction.MULTIPLY, right);
        return target;
    };

}
