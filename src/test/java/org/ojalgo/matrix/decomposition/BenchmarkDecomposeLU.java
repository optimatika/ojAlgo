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
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * <pre>
# Run complete. Total time: 00:05:08

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                    (density)  (dim)   Mode  Cnt   Score   Error  Units
BenchmarkDecomposeLU.dense        0.01   1000  thrpt    3  14.914 ± 0.704  ops/s
BenchmarkDecomposeLU.raw          0.01   1000  thrpt    3  23.900 ± 0.679  ops/s
BenchmarkDecomposeLU.sparse       0.01   1000  thrpt    3   0.787 ± 0.422  ops/s
 * </pre>
 */
@State(Scope.Benchmark)
public class BenchmarkDecomposeLU extends AbstractBenchmarkSparseLU {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(BenchmarkDecomposeLU.class);
    }

    @Param({ "0.005", "0.01", "0.02", "0.05" })
    public double density;
    @Param({ "500", "1000", "2000" })
    public int dim;

    MatrixStore<Double> matrix;

    @Benchmark
    public LU<Double> dense() {
        dense.decompose(matrix);
        return dense;
    }

    @Benchmark
    public LU<Double> raw() {
        raw.decompose(matrix);
        return raw;
    }

    @Setup(Level.Trial)
    public void setup() {
        matrix = AbstractBenchmarkSparseLU.newSparseMatrix(dim, density);
    }

    @Benchmark
    public LU<Double> sparse() {
        sparse.decompose(matrix);
        return sparse;
    }

}
