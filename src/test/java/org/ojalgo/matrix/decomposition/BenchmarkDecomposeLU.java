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
import org.ojalgo.array.SparseArray;
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
# Run complete. Total time: 00:52:27

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                    (density)  (dim)   Mode  Cnt     Score     Error  Units
BenchmarkDecomposeLU.dense       0.005    500  thrpt    3    69.362 ±   0.371  ops/s
BenchmarkDecomposeLU.dense       0.005   1000  thrpt    3    15.146 ±   0.200  ops/s
BenchmarkDecomposeLU.dense       0.005   2000  thrpt    3     2.332 ±   0.541  ops/s

BenchmarkDecomposeLU.dense        0.01    500  thrpt    3    69.424 ±   0.735  ops/s
BenchmarkDecomposeLU.dense        0.01   1000  thrpt    3    16.155 ±   0.749  ops/s
BenchmarkDecomposeLU.dense        0.01   2000  thrpt    3     2.144 ±   1.826  ops/s

BenchmarkDecomposeLU.dense        0.02    500  thrpt    3    68.555 ±   0.744  ops/s
BenchmarkDecomposeLU.dense        0.02   1000  thrpt    3    15.649 ±   0.229  ops/s
BenchmarkDecomposeLU.dense        0.02   2000  thrpt    3     1.979 ±   0.138  ops/s

BenchmarkDecomposeLU.raw         0.005    500  thrpt    3  2090.220 ± 690.082  ops/s
BenchmarkDecomposeLU.raw         0.005   1000  thrpt    3    50.535 ±   0.358  ops/s
BenchmarkDecomposeLU.raw         0.005   2000  thrpt    3     2.901 ±   0.119  ops/s

BenchmarkDecomposeLU.raw          0.01    500  thrpt    3   354.896 ±   2.527  ops/s
BenchmarkDecomposeLU.raw          0.01   1000  thrpt    3    24.214 ±   0.922  ops/s
BenchmarkDecomposeLU.raw          0.01   2000  thrpt    3     1.890 ±   0.068  ops/s

BenchmarkDecomposeLU.raw          0.02    500  thrpt    3   181.709 ±   3.500  ops/s
BenchmarkDecomposeLU.raw          0.02   1000  thrpt    3    17.091 ±   0.720  ops/s
BenchmarkDecomposeLU.raw          0.02   2000  thrpt    3     1.504 ±   0.030  ops/s

BenchmarkDecomposeLU.sparse      0.005    500  thrpt    3   289.577 ±  14.224  ops/s
BenchmarkDecomposeLU.sparse      0.005   1000  thrpt    3     1.822 ±   0.112  ops/s
BenchmarkDecomposeLU.sparse      0.005   2000  thrpt    3     0.053 ±   0.436  ops/s

BenchmarkDecomposeLU.sparse       0.01    500  thrpt    3    17.398 ±   0.383  ops/s
BenchmarkDecomposeLU.sparse       0.01   1000  thrpt    3     0.805 ±   0.036  ops/s
BenchmarkDecomposeLU.sparse       0.01   2000  thrpt    3     0.052 ±   0.245  ops/s

BenchmarkDecomposeLU.sparse       0.02    500  thrpt    3     7.628 ±   0.558  ops/s
BenchmarkDecomposeLU.sparse       0.02   1000  thrpt    3     0.535 ±   0.210  ops/s
BenchmarkDecomposeLU.sparse       0.02   2000  thrpt    3     0.047 ±   0.228  ops/s
 * </pre>
 *
 * With newer {@link SparseArray} based L
 *
 * <pre>
# Run complete. Total time: 00:16:36

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                    (density)  (dim)   Mode  Cnt   Score    Error  Units
BenchmarkDecomposeLU.sparse      0.005    500  thrpt    3  36.826 ± 83.025  ops/s
BenchmarkDecomposeLU.sparse      0.005   1000  thrpt    3   1.503 ±  0.665  ops/s
BenchmarkDecomposeLU.sparse      0.005   2000  thrpt    3   0.166 ±  0.015  ops/s

BenchmarkDecomposeLU.sparse       0.01    500  thrpt    3  10.117 ±  0.147  ops/s
BenchmarkDecomposeLU.sparse       0.01   1000  thrpt    3   1.205 ±  0.086  ops/s
BenchmarkDecomposeLU.sparse       0.01   2000  thrpt    3   0.146 ±  0.024  ops/s

BenchmarkDecomposeLU.sparse       0.02    500  thrpt    3   8.410 ±  0.158  ops/s
BenchmarkDecomposeLU.sparse       0.02   1000  thrpt    3   1.093 ±  0.031  ops/s
BenchmarkDecomposeLU.sparse       0.02   2000  thrpt    3   0.140 ±  0.013  ops/s
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
