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
# Run complete. Total time: 00:46:07

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                    (density)  (dim)   Mode  Cnt     Score     Error  Units
BenchmarkDecomposeLU.dense       0.005    500  thrpt    3    69.896 ±   0.863  ops/s
BenchmarkDecomposeLU.dense       0.005   1000  thrpt    3    15.099 ±   0.493  ops/s
BenchmarkDecomposeLU.dense       0.005   2000  thrpt    3     2.320 ±   0.316  ops/s
BenchmarkDecomposeLU.dense        0.01    500  thrpt    3    68.774 ±   1.310  ops/s
BenchmarkDecomposeLU.dense        0.01   1000  thrpt    3    16.232 ±   0.910  ops/s
BenchmarkDecomposeLU.dense        0.01   2000  thrpt    3     2.278 ±   0.089  ops/s
BenchmarkDecomposeLU.dense        0.02    500  thrpt    3    68.062 ±   6.053  ops/s
BenchmarkDecomposeLU.dense        0.02   1000  thrpt    3    15.929 ±   1.324  ops/s
BenchmarkDecomposeLU.dense        0.02   2000  thrpt    3     2.130 ±   0.064  ops/s
BenchmarkDecomposeLU.raw         0.005    500  thrpt    3  2578.130 ± 151.817  ops/s
BenchmarkDecomposeLU.raw         0.005   1000  thrpt    3    47.873 ±   6.020  ops/s
BenchmarkDecomposeLU.raw         0.005   2000  thrpt    3     2.846 ±   0.079  ops/s
BenchmarkDecomposeLU.raw          0.01    500  thrpt    3   347.626 ±   1.410  ops/s
BenchmarkDecomposeLU.raw          0.01   1000  thrpt    3    24.648 ±   0.167  ops/s
BenchmarkDecomposeLU.raw          0.01   2000  thrpt    3     2.024 ±   0.752  ops/s
BenchmarkDecomposeLU.raw          0.02    500  thrpt    3   180.264 ±   0.879  ops/s
BenchmarkDecomposeLU.raw          0.02   1000  thrpt    3    17.081 ±   0.058  ops/s
BenchmarkDecomposeLU.raw          0.02   2000  thrpt    3     1.657 ±   0.032  ops/s
BenchmarkDecomposeLU.sparse      0.005    500  thrpt    3   383.042 ±   2.270  ops/s
BenchmarkDecomposeLU.sparse      0.005   1000  thrpt    3    12.545 ±   0.098  ops/s
BenchmarkDecomposeLU.sparse      0.005   2000  thrpt    3     0.753 ±   0.007  ops/s
BenchmarkDecomposeLU.sparse       0.01    500  thrpt    3    79.269 ±   1.839  ops/s
BenchmarkDecomposeLU.sparse       0.01   1000  thrpt    3     5.747 ±   0.008  ops/s
BenchmarkDecomposeLU.sparse       0.01   2000  thrpt    3     0.515 ±   0.010  ops/s
BenchmarkDecomposeLU.sparse       0.02    500  thrpt    3    40.313 ±   0.123  ops/s
BenchmarkDecomposeLU.sparse       0.02   1000  thrpt    3     4.194 ±   0.022  ops/s
BenchmarkDecomposeLU.sparse       0.02   2000  thrpt    3     0.416 ±   0.027  ops/s
 * </pre>
 *
 * Partial/temporary:
 *
 * <pre>
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
