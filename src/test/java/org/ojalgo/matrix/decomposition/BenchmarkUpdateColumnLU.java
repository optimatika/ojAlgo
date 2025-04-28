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
# Run complete. Total time: 01:08:06

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                       (density)  (dim)   Mode  Cnt      Score       Error  Units
BenchmarkUpdateColumnLU.dense       0.005    500  thrpt    3   5412.828 ±   765.589  ops/s
BenchmarkUpdateColumnLU.dense       0.005   1000  thrpt    3   1324.511 ±    22.317  ops/s
BenchmarkUpdateColumnLU.dense       0.005   2000  thrpt    3    313.748 ±     6.152  ops/s

BenchmarkUpdateColumnLU.dense        0.01    500  thrpt    3   5389.836 ±   196.816  ops/s
BenchmarkUpdateColumnLU.dense        0.01   1000  thrpt    3   1318.667 ±    18.680  ops/s
BenchmarkUpdateColumnLU.dense        0.01   2000  thrpt    3    312.377 ±    42.743  ops/s

BenchmarkUpdateColumnLU.dense        0.02    500  thrpt    3   4968.338 ± 13479.498  ops/s
BenchmarkUpdateColumnLU.dense        0.02   1000  thrpt    3   1313.446 ±    91.635  ops/s
BenchmarkUpdateColumnLU.dense        0.02   2000  thrpt    3    300.652 ±    41.347  ops/s

BenchmarkUpdateColumnLU.dense        0.05    500  thrpt    3   5330.555 ±   132.548  ops/s
BenchmarkUpdateColumnLU.dense        0.05   1000  thrpt    3   1312.381 ±    59.284  ops/s
BenchmarkUpdateColumnLU.dense        0.05   2000  thrpt    3    302.660 ±    14.948  ops/s

BenchmarkUpdateColumnLU.raw         0.005    500  thrpt    3   8261.610 ±  1057.208  ops/s
BenchmarkUpdateColumnLU.raw         0.005   1000  thrpt    3   1992.446 ±   162.812  ops/s
BenchmarkUpdateColumnLU.raw         0.005   2000  thrpt    3    463.148 ±    46.336  ops/s

BenchmarkUpdateColumnLU.raw          0.01    500  thrpt    3   8223.410 ±  1074.755  ops/s
BenchmarkUpdateColumnLU.raw          0.01   1000  thrpt    3   1995.176 ±   245.614  ops/s
BenchmarkUpdateColumnLU.raw          0.01   2000  thrpt    3    467.345 ±    70.736  ops/s

BenchmarkUpdateColumnLU.raw          0.02    500  thrpt    3   8292.320 ±   760.681  ops/s
BenchmarkUpdateColumnLU.raw          0.02   1000  thrpt    3   1991.568 ±   279.072  ops/s
BenchmarkUpdateColumnLU.raw          0.02   2000  thrpt    3    459.651 ±    90.368  ops/s

BenchmarkUpdateColumnLU.raw          0.05    500  thrpt    3   8297.932 ±   852.909  ops/s
BenchmarkUpdateColumnLU.raw          0.05   1000  thrpt    3   1384.125 ±  1511.263  ops/s
BenchmarkUpdateColumnLU.raw          0.05   2000  thrpt    3    460.173 ±    28.323  ops/s

BenchmarkUpdateColumnLU.sparse      0.005    500  thrpt    3  66715.448 ±  3952.038  ops/s
BenchmarkUpdateColumnLU.sparse      0.005   1000  thrpt    3   1555.197 ±    76.578  ops/s
BenchmarkUpdateColumnLU.sparse      0.005   2000  thrpt    3    165.852 ±    13.381  ops/s

BenchmarkUpdateColumnLU.sparse       0.01    500  thrpt    3   2876.148 ±    45.290  ops/s
BenchmarkUpdateColumnLU.sparse       0.01   1000  thrpt    3    753.533 ±    21.218  ops/s
BenchmarkUpdateColumnLU.sparse       0.01   2000  thrpt    3    122.058 ±     1.158  ops/s

BenchmarkUpdateColumnLU.sparse       0.02    500  thrpt    3   2916.609 ±    63.195  ops/s
BenchmarkUpdateColumnLU.sparse       0.02   1000  thrpt    3    563.883 ±    14.597  ops/s
BenchmarkUpdateColumnLU.sparse       0.02   2000  thrpt    3    106.236 ±     0.213  ops/s

BenchmarkUpdateColumnLU.sparse       0.05    500  thrpt    3   2140.658 ±   133.028  ops/s
BenchmarkUpdateColumnLU.sparse       0.05   1000  thrpt    3    445.800 ±    16.613  ops/s
BenchmarkUpdateColumnLU.sparse       0.05   2000  thrpt    3      5.941 ±     0.125  ops/s
 * </pre>
 *
 * With SparseArray instead of linked lists
 *
 * <pre>
# Run complete. Total time: 00:15:13

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                       (density)  (dim)   Mode  Cnt      Score      Error  Units
BenchmarkUpdateColumnLU.sparse      0.005    500  thrpt    3  93974.618 ± 7449.470  ops/s
BenchmarkUpdateColumnLU.sparse      0.005   1000  thrpt    3   5991.439 ±   38.681  ops/s
BenchmarkUpdateColumnLU.sparse      0.005   2000  thrpt    3    725.095 ±   44.641  ops/s
BenchmarkUpdateColumnLU.sparse       0.01    500  thrpt    3  23451.490 ± 2062.216  ops/s
BenchmarkUpdateColumnLU.sparse       0.01   1000  thrpt    3   3334.096 ±  194.537  ops/s
BenchmarkUpdateColumnLU.sparse       0.01   2000  thrpt    3    541.520 ±  707.255  ops/s
BenchmarkUpdateColumnLU.sparse       0.02    500  thrpt    3  13859.182 ±  110.930  ops/s
BenchmarkUpdateColumnLU.sparse       0.02   1000  thrpt    3   2747.076 ±    9.256  ops/s
BenchmarkUpdateColumnLU.sparse       0.02   2000  thrpt    3    507.356 ±   23.977  ops/s
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