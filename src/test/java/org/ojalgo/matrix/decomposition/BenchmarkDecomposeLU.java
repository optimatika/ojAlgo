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
# Run complete. Total time: 01:08:04

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                    (density)  (dim)   Mode  Cnt     Score     Error  Units
BenchmarkDecomposeLU.dense       0.005    500  thrpt    3    76.254 ±   2.037  ops/s
BenchmarkDecomposeLU.dense       0.005   1000  thrpt    3    11.233 ±   0.846  ops/s
BenchmarkDecomposeLU.dense       0.005   2000  thrpt    3     1.725 ±   2.257  ops/s
BenchmarkDecomposeLU.dense        0.01    500  thrpt    3    75.498 ±   9.873  ops/s
BenchmarkDecomposeLU.dense        0.01   1000  thrpt    3    11.088 ±   0.329  ops/s
BenchmarkDecomposeLU.dense        0.01   2000  thrpt    3     1.735 ±   2.115  ops/s
BenchmarkDecomposeLU.dense        0.02    500  thrpt    3    75.658 ±   2.713  ops/s
BenchmarkDecomposeLU.dense        0.02   1000  thrpt    3     8.435 ±  75.571  ops/s
BenchmarkDecomposeLU.dense        0.02   2000  thrpt    3     1.847 ±   0.342  ops/s
BenchmarkDecomposeLU.dense        0.05    500  thrpt    3    72.810 ±   6.511  ops/s
BenchmarkDecomposeLU.dense        0.05   1000  thrpt    3     3.092 ±   2.774  ops/s
BenchmarkDecomposeLU.dense        0.05   2000  thrpt    3     1.613 ±   0.388  ops/s
BenchmarkDecomposeLU.raw         0.005    500  thrpt    3  2510.076 ± 510.231  ops/s
BenchmarkDecomposeLU.raw         0.005   1000  thrpt    3    46.021 ±  11.187  ops/s
BenchmarkDecomposeLU.raw         0.005   2000  thrpt    3     2.560 ±   2.018  ops/s
BenchmarkDecomposeLU.raw          0.01    500  thrpt    3   348.602 ±  10.865  ops/s
BenchmarkDecomposeLU.raw          0.01   1000  thrpt    3    22.280 ±   0.728  ops/s
BenchmarkDecomposeLU.raw          0.01   2000  thrpt    3     1.099 ±   0.462  ops/s
BenchmarkDecomposeLU.raw          0.02    500  thrpt    3    90.443 ±  50.320  ops/s
BenchmarkDecomposeLU.raw          0.02   1000  thrpt    3     8.646 ±   3.158  ops/s
BenchmarkDecomposeLU.raw          0.02   2000  thrpt    3     0.891 ±   0.112  ops/s
BenchmarkDecomposeLU.raw          0.05    500  thrpt    3    47.467 ± 165.415  ops/s
BenchmarkDecomposeLU.raw          0.05   1000  thrpt    3     8.455 ±  71.487  ops/s
BenchmarkDecomposeLU.raw          0.05   2000  thrpt    3     1.155 ±   0.026  ops/s
BenchmarkDecomposeLU.sparse      0.005    500  thrpt    3   192.276 ±  60.509  ops/s
BenchmarkDecomposeLU.sparse      0.005   1000  thrpt    3     2.734 ±   0.181  ops/s
BenchmarkDecomposeLU.sparse      0.005   2000  thrpt    3     0.143 ±   0.185  ops/s
BenchmarkDecomposeLU.sparse       0.01    500  thrpt    3    17.893 ±   0.430  ops/s
BenchmarkDecomposeLU.sparse       0.01   1000  thrpt    3     0.997 ±   0.085  ops/s
BenchmarkDecomposeLU.sparse       0.01   2000  thrpt    3     0.068 ±   0.120  ops/s
BenchmarkDecomposeLU.sparse       0.02    500  thrpt    3     7.505 ±   0.109  ops/s
BenchmarkDecomposeLU.sparse       0.02   1000  thrpt    3     0.673 ±   0.038  ops/s
BenchmarkDecomposeLU.sparse       0.02   2000  thrpt    3     0.042 ±   0.092  ops/s
BenchmarkDecomposeLU.sparse       0.05    500  thrpt    3     3.748 ±   0.084  ops/s
BenchmarkDecomposeLU.sparse       0.05   1000  thrpt    3     0.349 ±   0.115  ops/s
BenchmarkDecomposeLU.sparse       0.05   2000  thrpt    3     0.035 ±   0.068  ops/s
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

}
