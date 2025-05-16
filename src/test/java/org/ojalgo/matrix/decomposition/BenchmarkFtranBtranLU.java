/*
 * Copyright 1997-2025 Optimatika
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
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * <pre>
# Run complete. Total time: 00:10:07

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                           (density)  (dim)   Mode  Cnt    Score    Error  Units
BenchmarkFtranBtranLU.dense_btran        0.01   1000  thrpt    3  895.960 ± 92.446  ops/s
BenchmarkFtranBtranLU.dense_ftran        0.01   1000  thrpt    3  629.773 ± 27.280  ops/s
BenchmarkFtranBtranLU.raw_btran          0.01   1000  thrpt    3  580.072 ± 14.350  ops/s
BenchmarkFtranBtranLU.raw_ftran          0.01   1000  thrpt    3  970.401 ± 55.886  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.01   1000  thrpt    3  577.551 ± 72.713  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.01   1000  thrpt    3  598.610 ± 32.700  ops/s
 * </pre>
 */
@State(Scope.Benchmark)
public class BenchmarkFtranBtranLU extends AbstractBenchmarkSparseLU {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(BenchmarkFtranBtranLU.class);
    }

    @Param({ "0.005", "0.01", "0.02" })
    public double density;
    @Param({ "500", "1000", "2000" })
    public int dim;

    PhysicalStore<Double> vector;

    @Benchmark
    public PhysicalStore<Double> dense_btran() {
        dense.btran(vector);
        return vector;
    }

    @Benchmark
    public PhysicalStore<Double> dense_ftran() {
        dense.ftran(vector);
        return vector;
    }

    @Benchmark
    public PhysicalStore<Double> raw_btran() {
        raw.btran(vector);
        return vector;
    }

    @Benchmark
    public PhysicalStore<Double> raw_ftran() {
        raw.ftran(vector);
        return vector;
    }

    @Setup(Level.Trial)
    public void setup() {

        MatrixStore<Double> matrix = AbstractBenchmarkSparseLU.newSparseMatrix(dim, density);
        // Decompose matrices
        sparse.decompose(matrix);
        dense.decompose(matrix);
        raw.decompose(matrix);

        vector = AbstractBenchmarkSparseLU.newDenseVector(dim);
    }

    @Benchmark
    public PhysicalStore<Double> sparse_btran() {
        sparse.btran(vector);
        return vector;
    }

    @Benchmark
    public PhysicalStore<Double> sparse_ftran() {
        sparse.ftran(vector);
        return vector;
    }

}