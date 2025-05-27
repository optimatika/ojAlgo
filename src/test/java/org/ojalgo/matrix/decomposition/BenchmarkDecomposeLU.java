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
# Run complete. Total time: 00:46:03

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                    (density)  (dim)   Mode  Cnt     Score    Error  Units
BenchmarkDecomposeLU.dense       0.005    500  thrpt    3    68.639 ± 27.708  ops/s
BenchmarkDecomposeLU.dense       0.005   1000  thrpt    3    15.058 ±  1.300  ops/s
BenchmarkDecomposeLU.dense       0.005   2000  thrpt    3     2.375 ±  0.156  ops/s
BenchmarkDecomposeLU.dense        0.01    500  thrpt    3    68.311 ±  4.548  ops/s
BenchmarkDecomposeLU.dense        0.01   1000  thrpt    3    14.936 ±  1.819  ops/s
BenchmarkDecomposeLU.dense        0.01   2000  thrpt    3     2.288 ±  0.278  ops/s
BenchmarkDecomposeLU.dense        0.02    500  thrpt    3    67.503 ±  2.879  ops/s
BenchmarkDecomposeLU.dense        0.02   1000  thrpt    3    16.023 ±  1.422  ops/s
BenchmarkDecomposeLU.dense        0.02   2000  thrpt    3     2.073 ±  1.379  ops/s
BenchmarkDecomposeLU.raw         0.005    500  thrpt    3  2164.642 ± 57.630  ops/s
BenchmarkDecomposeLU.raw         0.005   1000  thrpt    3    48.337 ± 11.419  ops/s
BenchmarkDecomposeLU.raw         0.005   2000  thrpt    3     2.876 ±  0.581  ops/s
BenchmarkDecomposeLU.raw          0.01    500  thrpt    3   386.084 ±  1.266  ops/s
BenchmarkDecomposeLU.raw          0.01   1000  thrpt    3    23.168 ±  2.117  ops/s
BenchmarkDecomposeLU.raw          0.01   2000  thrpt    3     2.013 ±  0.092  ops/s
BenchmarkDecomposeLU.raw          0.02    500  thrpt    3   186.432 ±  4.092  ops/s
BenchmarkDecomposeLU.raw          0.02   1000  thrpt    3    16.433 ±  3.751  ops/s
BenchmarkDecomposeLU.raw          0.02   2000  thrpt    3     1.695 ±  0.208  ops/s
BenchmarkDecomposeLU.sparse      0.005    500  thrpt    3   360.089 ±  3.198  ops/s
BenchmarkDecomposeLU.sparse      0.005   1000  thrpt    3    12.464 ±  5.343  ops/s
BenchmarkDecomposeLU.sparse      0.005   2000  thrpt    3     0.704 ±  0.121  ops/s
BenchmarkDecomposeLU.sparse       0.01    500  thrpt    3    96.019 ±  1.470  ops/s
BenchmarkDecomposeLU.sparse       0.01   1000  thrpt    3     5.875 ±  0.018  ops/s
BenchmarkDecomposeLU.sparse       0.01   2000  thrpt    3     0.478 ±  0.067  ops/s
BenchmarkDecomposeLU.sparse       0.02    500  thrpt    3    43.784 ±  3.590  ops/s
BenchmarkDecomposeLU.sparse       0.02   1000  thrpt    3     4.334 ±  0.292  ops/s
BenchmarkDecomposeLU.sparse       0.02   2000  thrpt    3     0.417 ±  0.007  ops/s
 * </pre>
 *
 * Partial/temporary: int indices in SparseArray
 *
 * <pre>
# Run complete. Total time: 00:15:45

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                    (density)  (dim)   Mode  Cnt    Score   Error  Units
BenchmarkDecomposeLU.sparse      0.005    500  thrpt    3  373.304 ± 2.486  ops/s
BenchmarkDecomposeLU.sparse      0.005   1000  thrpt    3   11.757 ± 0.149  ops/s
BenchmarkDecomposeLU.sparse      0.005   2000  thrpt    3    0.727 ± 0.018  ops/s
BenchmarkDecomposeLU.sparse       0.01    500  thrpt    3   81.824 ± 0.307  ops/s
BenchmarkDecomposeLU.sparse       0.01   1000  thrpt    3    5.619 ± 0.029  ops/s
BenchmarkDecomposeLU.sparse       0.01   2000  thrpt    3    0.520 ± 0.016  ops/s
BenchmarkDecomposeLU.sparse       0.02    500  thrpt    3   51.786 ± 1.594  ops/s
BenchmarkDecomposeLU.sparse       0.02   1000  thrpt    3    4.093 ± 0.208  ops/s
BenchmarkDecomposeLU.sparse       0.02   2000  thrpt    3    0.443 ± 0.011  ops/s
 * </pre>
 */
@State(Scope.Benchmark)
public class BenchmarkDecomposeLU extends AbstractBenchmarkSparseLU {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(BenchmarkDecomposeLU.class);
    }

    @Param({ "0.005", "0.01", "0.02" })
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
