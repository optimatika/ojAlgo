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
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.operation.ApplyLU;
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
 * # Run complete. Total time: 00:00:51
 *
 * Benchmark                   (dim)  (z)   Mode  Cnt      Score      Error    Units
 * ThresholdApplyLU.decompose    128    1  thrpt    3  64739,821 ±  710,296  ops/min
 * ThresholdApplyLU.decompose    128    2  thrpt    3  19282,920 ± 1812,448  ops/min
 * ThresholdApplyLU.decompose    256    1  thrpt    3   8847,622 ±  338,990  ops/min
 * ThresholdApplyLU.decompose    256    2  thrpt    3   5166,169 ±  849,321  ops/min
 * ThresholdApplyLU.decompose    512    1  thrpt    3   1143,877 ±  109,664  ops/min
 * ThresholdApplyLU.decompose    512    2  thrpt    3   1378,481 ±  141,927  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdApplyLU extends AbstractThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        LinearAlgebraBenchmark.run(ThresholdApplyLU.class);
    }

    @Param({ "128", "256", "512", "1024" })
    public int dim;

    @Param({ "1", "2" })
    public int z;

    LU<Double> decomposition = new LUDecomposition.Primitive();

    PrimitiveDenseStore matrix;

    @Override
    @Setup
    public void setup() {

        ApplyLU.THRESHOLD = dim / z;

        matrix = MatrixUtils.makeSPD(dim);
    }

    @Override
    @Benchmark
    public Object tune() {
        return decomposition.decompose(matrix);
    };

}
