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
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.operation.HouseholderRight;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro. 2015-06-24 => 512
 *
 * <pre>
 * # Run complete. Total time: 00:32:32
 *
 * Benchmark                            (dim)  (z)   Mode  Cnt    Score    Error    Units
 * ThresholdHouseholderRight.decompose    512    1  thrpt    3  160,486 ± 18,885  ops/min
 * ThresholdHouseholderRight.decompose    512    2  thrpt    3  157,054 ± 17,449  ops/min
 * ThresholdHouseholderRight.decompose   1024    1  thrpt    3   29,190 ±  1,876  ops/min
 * ThresholdHouseholderRight.decompose   1024    2  thrpt    3   30,504 ± 17,942  ops/min
 * ThresholdHouseholderRight.decompose   2048    1  thrpt    3    3,960 ±  0,354  ops/min
 * ThresholdHouseholderRight.decompose   2048    2  thrpt    3    4,465 ±  0,299  ops/min
 * ThresholdHouseholderRight.decompose   4096    1  thrpt    3    0,539 ±  0,036  ops/min
 * ThresholdHouseholderRight.decompose   4096    2  thrpt    3    0,626 ±  0,013  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdHouseholderRight extends AbstractThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        LinearAlgebraBenchmark.run(ThresholdHouseholderRight.class);
    }

    @Param({ "256", "512", "1024", "2048" })
    public int dim;

    @Param({ "1", "2" })
    public int z;

    Bidiagonal<Double> decomposition = new BidiagonalDecomposition.Primitive();

    MatrixStore<Double> matrix;

    @Override
    @Setup
    public void setup() {

        HouseholderRight.THRESHOLD = dim / z;

        matrix = MatrixUtils.makeSPD(dim).logical().below(MatrixStore.PRIMITIVE.makeIdentity(dim).get()).get().copy();
    }

    @Override
    @Benchmark
    public Object tune() {
        final boolean retVal = decomposition.decompose(matrix);
        return retVal;
    };

}
