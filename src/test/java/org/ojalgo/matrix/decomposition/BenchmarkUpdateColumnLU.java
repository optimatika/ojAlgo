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
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * <pre>
# Run complete. Total time: 00:45:38

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                       (density)  (dim)   Mode  Cnt       Score      Error  Units
BenchmarkUpdateColumnLU.dense       0.005    500  thrpt    3    5393.930 ±   27.148  ops/s
BenchmarkUpdateColumnLU.dense       0.005   1000  thrpt    3    1326.159 ±    7.707  ops/s
BenchmarkUpdateColumnLU.dense       0.005   2000  thrpt    3     315.141 ±    2.796  ops/s
BenchmarkUpdateColumnLU.dense        0.01    500  thrpt    3    5380.639 ±  321.954  ops/s
BenchmarkUpdateColumnLU.dense        0.01   1000  thrpt    3    1328.569 ±    4.724  ops/s
BenchmarkUpdateColumnLU.dense        0.01   2000  thrpt    3     313.675 ±    2.535  ops/s
BenchmarkUpdateColumnLU.dense        0.02    500  thrpt    3    5384.512 ±   32.438  ops/s
BenchmarkUpdateColumnLU.dense        0.02   1000  thrpt    3    1326.366 ±    1.525  ops/s
BenchmarkUpdateColumnLU.dense        0.02   2000  thrpt    3     314.393 ±   23.420  ops/s
BenchmarkUpdateColumnLU.raw         0.005    500  thrpt    3    8304.669 ±  190.305  ops/s
BenchmarkUpdateColumnLU.raw         0.005   1000  thrpt    3    1997.867 ±  197.280  ops/s
BenchmarkUpdateColumnLU.raw         0.005   2000  thrpt    3     484.455 ±   64.550  ops/s
BenchmarkUpdateColumnLU.raw          0.01    500  thrpt    3    8320.706 ±  180.531  ops/s
BenchmarkUpdateColumnLU.raw          0.01   1000  thrpt    3    2000.113 ±   15.744  ops/s
BenchmarkUpdateColumnLU.raw          0.01   2000  thrpt    3     487.266 ±    3.254  ops/s
BenchmarkUpdateColumnLU.raw          0.02    500  thrpt    3    8332.677 ±   72.716  ops/s
BenchmarkUpdateColumnLU.raw          0.02   1000  thrpt    3    2005.974 ±    1.847  ops/s
BenchmarkUpdateColumnLU.raw          0.02   2000  thrpt    3     487.717 ±    3.455  ops/s
BenchmarkUpdateColumnLU.sparse      0.005    500  thrpt    3  100448.475 ± 8515.196  ops/s
BenchmarkUpdateColumnLU.sparse      0.005   1000  thrpt    3    5649.611 ±  120.694  ops/s
BenchmarkUpdateColumnLU.sparse      0.005   2000  thrpt    3     769.437 ±    8.479  ops/s
BenchmarkUpdateColumnLU.sparse       0.01    500  thrpt    3   19901.066 ±  144.348  ops/s
BenchmarkUpdateColumnLU.sparse       0.01   1000  thrpt    3    3555.723 ±   28.142  ops/s
BenchmarkUpdateColumnLU.sparse       0.01   2000  thrpt    3     583.291 ±   14.344  ops/s
BenchmarkUpdateColumnLU.sparse       0.02    500  thrpt    3   11981.005 ±   65.528  ops/s
BenchmarkUpdateColumnLU.sparse       0.02   1000  thrpt    3    2641.026 ±    7.800  ops/s
BenchmarkUpdateColumnLU.sparse       0.02   2000  thrpt    3     522.534 ±    2.073  ops/s
 * </pre>
 *
 * Before:
 *
 * <pre>
Benchmark                       (density)  (dim)   Mode  Cnt       Score      Error  Units
BenchmarkUpdateColumnLU.sparse      0.005    500  thrpt    3  116935.027 ± 4419.610  ops/s
BenchmarkUpdateColumnLU.sparse      0.005   1000  thrpt    3    5757.816 ±   18.891  ops/s
BenchmarkUpdateColumnLU.sparse      0.005   2000  thrpt    3     804.840 ±   52.349  ops/s
BenchmarkUpdateColumnLU.sparse       0.01    500  thrpt    3   19415.809 ± 4152.648  ops/s
BenchmarkUpdateColumnLU.sparse       0.01   1000  thrpt    3    3256.046 ±  147.629  ops/s
BenchmarkUpdateColumnLU.sparse       0.01   2000  thrpt    3     612.089 ±  224.765  ops/s
BenchmarkUpdateColumnLU.sparse       0.02    500  thrpt    3   11972.875 ±  399.515  ops/s
BenchmarkUpdateColumnLU.sparse       0.02   1000  thrpt    3    2627.650 ±  220.212  ops/s
BenchmarkUpdateColumnLU.sparse       0.02   2000  thrpt    3     540.547 ±   32.974  ops/s
 * </pre>
 *
 * After:
 *
 * <pre>
Benchmark                       (density)  (dim)   Mode  Cnt      Score      Error  Units
BenchmarkUpdateColumnLU.sparse      0.005    500  thrpt    3  99834.585 ± 1236.167  ops/s
BenchmarkUpdateColumnLU.sparse      0.005   1000  thrpt    3   5493.401 ±  215.758  ops/s
BenchmarkUpdateColumnLU.sparse      0.005   2000  thrpt    3    858.892 ±    7.239  ops/s
BenchmarkUpdateColumnLU.sparse       0.01    500  thrpt    3  22373.056 ±  239.326  ops/s
BenchmarkUpdateColumnLU.sparse       0.01   1000  thrpt    3   3361.485 ±  161.363  ops/s
BenchmarkUpdateColumnLU.sparse       0.01   2000  thrpt    3    667.622 ±   15.321  ops/s
BenchmarkUpdateColumnLU.sparse       0.02    500  thrpt    3  12654.919 ±   17.503  ops/s
BenchmarkUpdateColumnLU.sparse       0.02   1000  thrpt    3   2689.259 ±   12.415  ops/s
BenchmarkUpdateColumnLU.sparse       0.02   2000  thrpt    3    594.799 ±    6.564  ops/s
 * </pre>
 */
@State(Scope.Benchmark)
public class BenchmarkUpdateColumnLU extends AbstractBenchmarkSparseLU {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(BenchmarkUpdateColumnLU.class);
    }

    @Param({ "0.005", "0.01", "0.02" })
    public double density;
    @Param({ "500", "1000", "2000" })
    public int dim;

    int index;
    PhysicalStore<Double> vector;

    @Benchmark
    public LU<Double> dense() {
        dense.updateColumn(index, vector);
        return dense;
    }

    @Benchmark
    public LU<Double> raw() {
        raw.updateColumn(index, vector);
        return raw;
    }

    @Setup(Level.Trial)
    public void setup() {

        MatrixStore<Double> matrix = AbstractBenchmarkSparseLU.newSparseMatrix(dim, density);
        // Decompose matrices
        sparse.decompose(matrix);
        dense.decompose(matrix);
        raw.decompose(matrix);

        vector = AbstractBenchmarkSparseLU.newDenseVector(dim);
        index = Uniform.randomInteger(dim);
    }

    @Benchmark
    public LU<Double> sparse() {
        sparse.updateColumn(index, vector);
        return sparse;
    }

}