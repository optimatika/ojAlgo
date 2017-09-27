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
import org.ojalgo.matrix.store.operation.FillMatchingRight;
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
 * MacBook Air. 2015-06-27 => 512
 *
 * <pre>
# Run complete. Total time: 00:01:26

Benchmark                        (dim)  (z)   Mode  Cnt        Score         Error    Units
ThresholdFillMatchingRight.tune    128    1  thrpt    3  8135207,951 ± 2741386,864  ops/min
ThresholdFillMatchingRight.tune    128    2  thrpt    3  1014091,828 ±  224318,819  ops/min
ThresholdFillMatchingRight.tune    256    1  thrpt    3  1352907,045 ±  499973,719  ops/min
ThresholdFillMatchingRight.tune    256    2  thrpt    3   726391,231 ±  369047,778  ops/min
ThresholdFillMatchingRight.tune    512    1  thrpt    3   157381,736 ±   59341,751  ops/min
ThresholdFillMatchingRight.tune    512    2  thrpt    3   138109,426 ±   90177,318  ops/min
ThresholdFillMatchingRight.tune   1024    1  thrpt    3    31918,693 ±   22068,158  ops/min
ThresholdFillMatchingRight.tune   1024    2  thrpt    3    35042,770 ±     526,916  ops/min
ThresholdFillMatchingRight.tune   2048    1  thrpt    3     8190,369 ±    3164,707  ops/min
ThresholdFillMatchingRight.tune   2048    2  thrpt    3     8453,745 ±    1194,325  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdFillMatchingRight extends AbstractThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        LinearAlgebraBenchmark.run(ThresholdFillMatchingRight.class);
    }

    @Param({ "128", "256", "512", "1024", "2048" })
    public int dim;

    @Param({ "1", "2" })
    public int z;

    Double left;
    PhysicalStore<Double> right;
    PhysicalStore<Double> target;

    @Override
    @Setup
    public void setup() {

        FillMatchingRight.THRESHOLD = dim / z;

        final Uniform tmpSupplier = new Uniform();

        left = tmpSupplier.doubleValue();
        right = PrimitiveDenseStore.FACTORY.makeFilled(dim, dim, tmpSupplier);
        target = PrimitiveDenseStore.FACTORY.makeZero(dim, dim);
    }

    @Override
    @Benchmark
    public Object tune() {
        // target.fillMatching(left, PrimitiveFunction.MULTIPLY, right);
        return target;
    }

}
