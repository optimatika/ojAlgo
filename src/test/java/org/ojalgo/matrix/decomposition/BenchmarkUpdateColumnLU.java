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
# Run complete. Total time: 00:45:39

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                       (density)  (dim)   Mode  Cnt      Score      Error  Units
BenchmarkUpdateColumnLU.dense       0.005    500  thrpt    3   5424.223 ±  459.031  ops/s
BenchmarkUpdateColumnLU.dense       0.005   1000  thrpt    3   1322.074 ±  124.781  ops/s
BenchmarkUpdateColumnLU.dense       0.005   2000  thrpt    3    313.872 ±    2.482  ops/s
BenchmarkUpdateColumnLU.dense        0.01    500  thrpt    3   5386.049 ±   46.703  ops/s
BenchmarkUpdateColumnLU.dense        0.01   1000  thrpt    3   1326.845 ±   43.153  ops/s
BenchmarkUpdateColumnLU.dense        0.01   2000  thrpt    3    315.582 ±    7.197  ops/s
BenchmarkUpdateColumnLU.dense        0.02    500  thrpt    3   5378.188 ±   32.443  ops/s
BenchmarkUpdateColumnLU.dense        0.02   1000  thrpt    3   1317.328 ±  178.363  ops/s
BenchmarkUpdateColumnLU.dense        0.02   2000  thrpt    3    312.010 ±   45.731  ops/s
BenchmarkUpdateColumnLU.raw         0.005    500  thrpt    3   8339.717 ±   19.000  ops/s
BenchmarkUpdateColumnLU.raw         0.005   1000  thrpt    3   1995.571 ±   79.612  ops/s
BenchmarkUpdateColumnLU.raw         0.005   2000  thrpt    3    489.088 ±    4.421  ops/s
BenchmarkUpdateColumnLU.raw          0.01    500  thrpt    3   8346.718 ±  141.460  ops/s
BenchmarkUpdateColumnLU.raw          0.01   1000  thrpt    3   2003.039 ±  107.751  ops/s
BenchmarkUpdateColumnLU.raw          0.01   2000  thrpt    3    485.526 ±   62.020  ops/s
BenchmarkUpdateColumnLU.raw          0.02    500  thrpt    3   8334.504 ±   81.373  ops/s
BenchmarkUpdateColumnLU.raw          0.02   1000  thrpt    3   2006.586 ±   11.834  ops/s
BenchmarkUpdateColumnLU.raw          0.02   2000  thrpt    3    488.897 ±    1.049  ops/s
BenchmarkUpdateColumnLU.sparse      0.005    500  thrpt    3  92339.697 ± 2506.696  ops/s
BenchmarkUpdateColumnLU.sparse      0.005   1000  thrpt    3   5650.841 ±  842.956  ops/s
BenchmarkUpdateColumnLU.sparse      0.005   2000  thrpt    3    738.278 ±   17.471  ops/s
BenchmarkUpdateColumnLU.sparse       0.01    500  thrpt    3  22459.524 ±  429.021  ops/s
BenchmarkUpdateColumnLU.sparse       0.01   1000  thrpt    3   3333.277 ±   54.107  ops/s
BenchmarkUpdateColumnLU.sparse       0.01   2000  thrpt    3    563.453 ±    9.687  ops/s
BenchmarkUpdateColumnLU.sparse       0.02    500  thrpt    3  13808.485 ±   40.060  ops/s
BenchmarkUpdateColumnLU.sparse       0.02   1000  thrpt    3   2677.103 ±    8.712  ops/s
BenchmarkUpdateColumnLU.sparse       0.02   2000  thrpt    3    502.731 ±   45.767  ops/s
 * </pre>
 *
 * Sorted
 *
 * <pre>
Benchmark                       (density)  (dim)   Mode  Cnt      Score      Error  Units
BenchmarkUpdateColumnLU.dense       0.005    500  thrpt    3   5424.223 ±  459.031  ops/s
BenchmarkUpdateColumnLU.raw         0.005    500  thrpt    3   8339.717 ±   19.000  ops/s
BenchmarkUpdateColumnLU.sparse      0.005    500  thrpt    3  92339.697 ± 2506.696  ops/s

BenchmarkUpdateColumnLU.dense       0.005   1000  thrpt    3   1322.074 ±  124.781  ops/s
BenchmarkUpdateColumnLU.raw         0.005   1000  thrpt    3   1995.571 ±   79.612  ops/s
BenchmarkUpdateColumnLU.sparse      0.005   1000  thrpt    3   5650.841 ±  842.956  ops/s

BenchmarkUpdateColumnLU.dense       0.005   2000  thrpt    3    313.872 ±    2.482  ops/s
BenchmarkUpdateColumnLU.raw         0.005   2000  thrpt    3    489.088 ±    4.421  ops/s
BenchmarkUpdateColumnLU.sparse      0.005   2000  thrpt    3    738.278 ±   17.471  ops/s

BenchmarkUpdateColumnLU.dense        0.01    500  thrpt    3   5386.049 ±   46.703  ops/s
BenchmarkUpdateColumnLU.raw          0.01    500  thrpt    3   8346.718 ±  141.460  ops/s
BenchmarkUpdateColumnLU.sparse       0.01    500  thrpt    3  22459.524 ±  429.021  ops/s

BenchmarkUpdateColumnLU.dense        0.01   1000  thrpt    3   1326.845 ±   43.153  ops/s
BenchmarkUpdateColumnLU.raw          0.01   1000  thrpt    3   2003.039 ±  107.751  ops/s
BenchmarkUpdateColumnLU.sparse       0.01   1000  thrpt    3   3333.277 ±   54.107  ops/s

BenchmarkUpdateColumnLU.dense        0.01   2000  thrpt    3    315.582 ±    7.197  ops/s
BenchmarkUpdateColumnLU.raw          0.01   2000  thrpt    3    485.526 ±   62.020  ops/s
BenchmarkUpdateColumnLU.sparse       0.01   2000  thrpt    3    563.453 ±    9.687  ops/s

BenchmarkUpdateColumnLU.dense        0.02    500  thrpt    3   5378.188 ±   32.443  ops/s
BenchmarkUpdateColumnLU.raw          0.02    500  thrpt    3   8334.504 ±   81.373  ops/s
BenchmarkUpdateColumnLU.sparse       0.02    500  thrpt    3  13808.485 ±   40.060  ops/s

BenchmarkUpdateColumnLU.dense        0.02   1000  thrpt    3   1317.328 ±  178.363  ops/s
BenchmarkUpdateColumnLU.raw          0.02   1000  thrpt    3   2006.586 ±   11.834  ops/s
BenchmarkUpdateColumnLU.sparse       0.02   1000  thrpt    3   2677.103 ±    8.712  ops/s

BenchmarkUpdateColumnLU.dense        0.02   2000  thrpt    3    312.010 ±   45.731  ops/s
BenchmarkUpdateColumnLU.raw          0.02   2000  thrpt    3    488.897 ±    1.049  ops/s
BenchmarkUpdateColumnLU.sparse       0.02   2000  thrpt    3    502.731 ±   45.767  ops/s
 * </pre>
 *
 * Partial/temporary: combined permutation and eta transformations
 *
 * <pre>
# Run complete. Total time: 00:15:13

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                       (density)  (dim)   Mode  Cnt       Score      Error  Units
BenchmarkUpdateColumnLU.sparse      0.005    500  thrpt    3  113258.119 ± 9202.077  ops/s
BenchmarkUpdateColumnLU.sparse      0.005   1000  thrpt    3    6068.011 ±  736.475  ops/s
BenchmarkUpdateColumnLU.sparse      0.005   2000  thrpt    3     724.196 ±   13.024  ops/s
BenchmarkUpdateColumnLU.sparse       0.01    500  thrpt    3   21758.028 ± 1015.265  ops/s
BenchmarkUpdateColumnLU.sparse       0.01   1000  thrpt    3    3352.685 ±   20.619  ops/s
BenchmarkUpdateColumnLU.sparse       0.01   2000  thrpt    3     580.287 ±   46.956  ops/s
BenchmarkUpdateColumnLU.sparse       0.02    500  thrpt    3   13012.797 ±  610.773  ops/s
BenchmarkUpdateColumnLU.sparse       0.02   1000  thrpt    3    2665.106 ±   21.400  ops/s
BenchmarkUpdateColumnLU.sparse       0.02   2000  thrpt    3     505.826 ±   38.301  ops/s
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