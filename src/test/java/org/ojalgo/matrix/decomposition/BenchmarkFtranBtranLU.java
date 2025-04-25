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
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.SparseStore;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * <pre>
# Run complete. Total time: 01:38:22

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                           (density)  (dim)   Mode  Cnt     Score      Error  Units
BenchmarkFtranBtranLU.dense_btran        0.01    500  thrpt    3  3744.022 ±  263.148  ops/s
BenchmarkFtranBtranLU.raw_btran          0.01    500  thrpt    3  3028.069 ±   43.125  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.01    500  thrpt    3  8295.596 ±  128.190  ops/s

BenchmarkFtranBtranLU.dense_btran        0.01   1000  thrpt    3   929.038 ±   95.276  ops/s
BenchmarkFtranBtranLU.raw_btran          0.01   1000  thrpt    3   638.594 ±    3.398  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.01   1000  thrpt    3  1069.474 ±   36.416  ops/s

BenchmarkFtranBtranLU.dense_btran        0.01   2000  thrpt    3   223.062 ±    3.643  ops/s
BenchmarkFtranBtranLU.raw_btran          0.01   2000  thrpt    3    89.454 ±    3.027  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.01   2000  thrpt    3    93.269 ±    5.803  ops/s

BenchmarkFtranBtranLU.dense_btran        0.02    500  thrpt    3  3738.260 ±   31.192  ops/s
BenchmarkFtranBtranLU.raw_btran          0.02    500  thrpt    3  3056.622 ±   10.100  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.02    500  thrpt    3  3676.681 ±   86.131  ops/s

BenchmarkFtranBtranLU.dense_btran        0.02   1000  thrpt    3   930.853 ±    4.754  ops/s
BenchmarkFtranBtranLU.raw_btran          0.02   1000  thrpt    3   635.943 ±   78.845  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.02   1000  thrpt    3   417.247 ±   15.641  ops/s

BenchmarkFtranBtranLU.dense_btran        0.02   2000  thrpt    3   220.190 ±    2.395  ops/s
BenchmarkFtranBtranLU.raw_btran          0.02   2000  thrpt    3    91.930 ±   16.344  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.02   2000  thrpt    3    54.371 ±    2.099  ops/s

BenchmarkFtranBtranLU.dense_btran        0.05    500  thrpt    3  3724.198 ±   68.179  ops/s
BenchmarkFtranBtranLU.raw_btran          0.05    500  thrpt    3  3045.920 ±   94.843  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.05    500  thrpt    3  1623.183 ±   80.072  ops/s

BenchmarkFtranBtranLU.dense_btran        0.05   1000  thrpt    3   930.091 ±   35.458  ops/s
BenchmarkFtranBtranLU.raw_btran          0.05   1000  thrpt    3   648.643 ±    0.508  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.05   1000  thrpt    3   274.593 ±    9.074  ops/s

BenchmarkFtranBtranLU.dense_btran        0.05   2000  thrpt    3   220.080 ±    1.884  ops/s
BenchmarkFtranBtranLU.raw_btran          0.05   2000  thrpt    3    91.760 ±    1.059  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.05   2000  thrpt    3    39.314 ±    1.164  ops/s

BenchmarkFtranBtranLU.dense_ftran        0.01    500  thrpt    3  2677.326 ±   62.077  ops/s
BenchmarkFtranBtranLU.raw_ftran          0.01    500  thrpt    3  4137.415 ±   55.398  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.01    500  thrpt    3  6328.070 ± 6469.189  ops/s

BenchmarkFtranBtranLU.dense_ftran        0.01   1000  thrpt    3   659.779 ±   10.904  ops/s
BenchmarkFtranBtranLU.raw_ftran          0.01   1000  thrpt    3  1003.777 ±    4.804  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.01   1000  thrpt    3   819.965 ±   25.226  ops/s

BenchmarkFtranBtranLU.dense_ftran        0.01   2000  thrpt    3   146.052 ±    0.775  ops/s
BenchmarkFtranBtranLU.raw_ftran          0.01   2000  thrpt    3   224.405 ±    3.687  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.01   2000  thrpt    3    80.483 ±    3.926  ops/s

BenchmarkFtranBtranLU.dense_ftran        0.02    500  thrpt    3  2678.801 ±   15.349  ops/s
BenchmarkFtranBtranLU.raw_ftran          0.02    500  thrpt    3  4147.813 ±   94.395  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.02    500  thrpt    3  3024.271 ±   54.259  ops/s

BenchmarkFtranBtranLU.dense_ftran        0.02   1000  thrpt    3   660.347 ±    4.033  ops/s
BenchmarkFtranBtranLU.raw_ftran          0.02   1000  thrpt    3  1005.359 ±    2.309  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.02   1000  thrpt    3   457.626 ±   26.674  ops/s

BenchmarkFtranBtranLU.dense_ftran        0.02   2000  thrpt    3   146.101 ±    1.972  ops/s
BenchmarkFtranBtranLU.raw_ftran          0.02   2000  thrpt    3   224.970 ±    1.616  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.02   2000  thrpt    3    50.620 ±    1.409  ops/s

BenchmarkFtranBtranLU.dense_ftran        0.05    500  thrpt    3  2678.858 ±    5.843  ops/s
BenchmarkFtranBtranLU.raw_ftran          0.05    500  thrpt    3  4142.439 ±   88.717  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.05    500  thrpt    3  1635.525 ±   21.963  ops/s

BenchmarkFtranBtranLU.dense_ftran        0.05   1000  thrpt    3   660.320 ±    8.407  ops/s
BenchmarkFtranBtranLU.raw_ftran          0.05   1000  thrpt    3  1004.383 ±    4.516  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.05   1000  thrpt    3   311.941 ±   26.059  ops/s

BenchmarkFtranBtranLU.dense_ftran        0.05   2000  thrpt    3   146.130 ±    0.501  ops/s
BenchmarkFtranBtranLU.raw_ftran          0.05   2000  thrpt    3   236.156 ±   32.853  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.05   2000  thrpt    3    35.568 ±    1.816  ops/s
 * </pre>
 */
@State(Scope.Benchmark)
public class BenchmarkFtranBtranLU extends AbstractBenchmarkSparseLU {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(BenchmarkFtranBtranLU.class);
    }

    @Param({ "0.005", "0.01", "0.02", "0.05" })
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

        SparseStore<Double> matrix = AbstractBenchmarkSparseLU.newSparseMatrix(dim, density);
        // Decompose matrices
        dense.decompose(matrix);
        raw.decompose(matrix);

        vector = AbstractBenchmarkSparseLU.newDenseVector(dim);
    }

}