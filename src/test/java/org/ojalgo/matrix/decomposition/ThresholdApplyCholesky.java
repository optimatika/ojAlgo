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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.array.operation.ApplyCholesky;
import org.ojalgo.matrix.store.Primitive64Store;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro. 2015-06-24 => 128
 *
 * <pre>
 * # Run complete. Total time: 00:00:51
 *
 * Benchmark                         (dim)  (z)   Mode  Cnt       Score        Error    Units
 * ThresholdApplyCholesky.decompose    128    1  thrpt    3  473062,064 ±  39441,809  ops/min
 * ThresholdApplyCholesky.decompose    128    2  thrpt    3  209607,295 ± 127572,842  ops/min
 * ThresholdApplyCholesky.decompose    256    1  thrpt    3   96804,170 ±   3820,152  ops/min
 * ThresholdApplyCholesky.decompose    256    2  thrpt    3  105632,770 ±  43359,445  ops/min
 * ThresholdApplyCholesky.decompose    512    1  thrpt    3   32785,271 ±   1172,475  ops/min
 * ThresholdApplyCholesky.decompose    512    2  thrpt    3   48798,885 ±   6349,178  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdApplyCholesky extends AbstractThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdApplyCholesky.class);
    }

    @Param({ "64", "128", "256", "512" })
    public int dim;

    @Param({ "1", "2" })
    public int z;

    Cholesky<Double> decomposition = new CholeskyDecomposition.Primitive();

    Primitive64Store matrix;

    @Override
    @Setup
    public void setup() {

        ApplyCholesky.THRESHOLD = dim / z;
        final int dim1 = dim;

        matrix = Primitive64Store.FACTORY.makeSPD(dim1);
    }

    @Override
    @Benchmark
    public Object tune() {
        return decomposition.decompose(matrix);
    };

}
