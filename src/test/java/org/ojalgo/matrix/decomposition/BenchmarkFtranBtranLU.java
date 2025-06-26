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
# Run complete. Total time: 01:32:41

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                           (density)  (dim)   Mode  Cnt       Score       Error  Units
BenchmarkFtranBtranLU.dense_btran       0.005    500  thrpt    3    2568.789 ±  2311.938  ops/s
BenchmarkFtranBtranLU.dense_btran       0.005   1000  thrpt    3     606.999 ±   174.897  ops/s
BenchmarkFtranBtranLU.dense_btran       0.005   2000  thrpt    3     148.056 ±   144.471  ops/s
BenchmarkFtranBtranLU.dense_btran        0.01    500  thrpt    3    1661.762 ± 11823.593  ops/s
BenchmarkFtranBtranLU.dense_btran        0.01   1000  thrpt    3     443.385 ±   222.969  ops/s
BenchmarkFtranBtranLU.dense_btran        0.01   2000  thrpt    3      90.944 ±   419.193  ops/s
BenchmarkFtranBtranLU.dense_btran        0.02    500  thrpt    3    1195.815 ±  1742.526  ops/s
BenchmarkFtranBtranLU.dense_btran        0.02   1000  thrpt    3     914.800 ±    34.398  ops/s
BenchmarkFtranBtranLU.dense_btran        0.02   2000  thrpt    3     217.973 ±    20.491  ops/s
BenchmarkFtranBtranLU.dense_ftran       0.005    500  thrpt    3    2677.746 ±   147.458  ops/s
BenchmarkFtranBtranLU.dense_ftran       0.005   1000  thrpt    3     650.652 ±    27.119  ops/s
BenchmarkFtranBtranLU.dense_ftran       0.005   2000  thrpt    3     140.283 ±     5.102  ops/s
BenchmarkFtranBtranLU.dense_ftran        0.01    500  thrpt    3    2600.587 ±   309.674  ops/s
BenchmarkFtranBtranLU.dense_ftran        0.01   1000  thrpt    3     650.781 ±    28.138  ops/s
BenchmarkFtranBtranLU.dense_ftran        0.01   2000  thrpt    3     139.926 ±     3.021  ops/s
BenchmarkFtranBtranLU.dense_ftran        0.02    500  thrpt    3    2621.146 ±    44.248  ops/s
BenchmarkFtranBtranLU.dense_ftran        0.02   1000  thrpt    3     646.680 ±    58.705  ops/s
BenchmarkFtranBtranLU.dense_ftran        0.02   2000  thrpt    3     143.606 ±     5.891  ops/s
BenchmarkFtranBtranLU.raw_btran         0.005    500  thrpt    3    2975.814 ±   336.824  ops/s
BenchmarkFtranBtranLU.raw_btran         0.005   1000  thrpt    3     624.872 ±   150.026  ops/s
BenchmarkFtranBtranLU.raw_btran         0.005   2000  thrpt    3      92.503 ±     0.367  ops/s
BenchmarkFtranBtranLU.raw_btran          0.01    500  thrpt    3    2996.515 ±    82.692  ops/s
BenchmarkFtranBtranLU.raw_btran          0.01   1000  thrpt    3     630.717 ±    30.865  ops/s
BenchmarkFtranBtranLU.raw_btran          0.01   2000  thrpt    3      89.400 ±     5.981  ops/s
BenchmarkFtranBtranLU.raw_btran          0.02    500  thrpt    3    3010.040 ±    25.596  ops/s
BenchmarkFtranBtranLU.raw_btran          0.02   1000  thrpt    3     624.447 ±    73.160  ops/s
BenchmarkFtranBtranLU.raw_btran          0.02   2000  thrpt    3      91.805 ±     2.468  ops/s
BenchmarkFtranBtranLU.raw_ftran         0.005    500  thrpt    3    4058.548 ±    43.686  ops/s
BenchmarkFtranBtranLU.raw_ftran         0.005   1000  thrpt    3     982.478 ±    86.184  ops/s
BenchmarkFtranBtranLU.raw_ftran         0.005   2000  thrpt    3     223.124 ±     7.354  ops/s
BenchmarkFtranBtranLU.raw_ftran          0.01    500  thrpt    3    4050.380 ±   172.096  ops/s
BenchmarkFtranBtranLU.raw_ftran          0.01   1000  thrpt    3     981.404 ±    92.155  ops/s
BenchmarkFtranBtranLU.raw_ftran          0.01   2000  thrpt    3     214.306 ±     8.775  ops/s
BenchmarkFtranBtranLU.raw_ftran          0.02    500  thrpt    3    4038.564 ±   196.836  ops/s
BenchmarkFtranBtranLU.raw_ftran          0.02   1000  thrpt    3     950.155 ±  1078.737  ops/s
BenchmarkFtranBtranLU.raw_ftran          0.02   2000  thrpt    3     216.818 ±     4.501  ops/s
BenchmarkFtranBtranLU.sparse_btran      0.005    500  thrpt    3  100701.098 ± 20197.302  ops/s
BenchmarkFtranBtranLU.sparse_btran      0.005   1000  thrpt    3    5228.287 ±   125.001  ops/s
BenchmarkFtranBtranLU.sparse_btran      0.005   2000  thrpt    3     542.732 ±    12.702  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.01    500  thrpt    3   18538.655 ±   934.790  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.01   1000  thrpt    3    2817.888 ±   414.540  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.01   2000  thrpt    3     445.213 ±    14.320  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.02    500  thrpt    3   11016.257 ±   271.128  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.02   1000  thrpt    3    2280.518 ±   152.706  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.02   2000  thrpt    3     396.123 ±    85.830  ops/s
BenchmarkFtranBtranLU.sparse_ftran      0.005    500  thrpt    3   75430.225 ±  3986.424  ops/s
BenchmarkFtranBtranLU.sparse_ftran      0.005   1000  thrpt    3    1270.065 ±    24.756  ops/s
BenchmarkFtranBtranLU.sparse_ftran      0.005   2000  thrpt    3     170.037 ±     6.444  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.01    500  thrpt    3   10249.114 ±   262.955  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.01   1000  thrpt    3    1633.095 ±   471.200  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.01   2000  thrpt    3     132.737 ±     8.272  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.02    500  thrpt    3    6454.844 ±  1342.481  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.02   1000  thrpt    3    1357.646 ±   225.287  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.02   2000  thrpt    3     275.424 ±    11.573  ops/s
 * </pre>
 *
 * Before:
 *
 * <pre>
Benchmark                           (density)  (dim)   Mode  Cnt      Score      Error  Units
BenchmarkFtranBtranLU.sparse_btran      0.005    500  thrpt    3  92956.732 ± 2274.472  ops/s
BenchmarkFtranBtranLU.sparse_btran      0.005   1000  thrpt    3   5251.542 ±   26.793  ops/s
BenchmarkFtranBtranLU.sparse_btran      0.005   2000  thrpt    3    538.363 ±   36.304  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.01    500  thrpt    3  18378.347 ± 2977.525  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.01   1000  thrpt    3   3055.801 ±  611.079  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.01   2000  thrpt    3    451.144 ±  254.528  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.02    500  thrpt    3  11535.502 ±  800.743  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.02   1000  thrpt    3   2389.042 ±  540.758  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.02   2000  thrpt    3    419.353 ±   34.126  ops/s

BenchmarkFtranBtranLU.sparse_ftran      0.005    500  thrpt    3  67758.370 ± 4176.193  ops/s
BenchmarkFtranBtranLU.sparse_ftran      0.005   1000  thrpt    3   1278.397 ±   10.445  ops/s
BenchmarkFtranBtranLU.sparse_ftran      0.005   2000  thrpt    3    167.613 ±    1.098  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.01    500  thrpt    3  11277.701 ± 2113.958  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.01   1000  thrpt    3   1699.599 ±   68.113  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.01   2000  thrpt    3    133.456 ±   14.746  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.02    500  thrpt    3   6515.055 ±   84.103  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.02   1000  thrpt    3   1327.999 ±  122.512  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.02   2000  thrpt    3    283.473 ±   22.712  ops/s
 * </pre>
 *
 * After:
 *
 * <pre>
Benchmark                           (density)  (dim)   Mode  Cnt      Score      Error  Units
BenchmarkFtranBtranLU.sparse_btran      0.005    500  thrpt    3  82116.379 ± 3814.879  ops/s
BenchmarkFtranBtranLU.sparse_btran      0.005   1000  thrpt    3   3187.112 ±  489.443  ops/s
BenchmarkFtranBtranLU.sparse_btran      0.005   2000  thrpt    3    453.085 ±   13.389  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.01    500  thrpt    3  12987.138 ±  822.794  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.01   1000  thrpt    3   1852.584 ±   10.416  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.01   2000  thrpt    3    348.718 ±   27.386  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.02    500  thrpt    3   7040.623 ±  260.246  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.02   1000  thrpt    3   1469.908 ±  356.900  ops/s
BenchmarkFtranBtranLU.sparse_btran       0.02   2000  thrpt    3    312.392 ±    3.024  ops/s

BenchmarkFtranBtranLU.sparse_ftran      0.005    500  thrpt    3  80749.122 ± 5614.117  ops/s
BenchmarkFtranBtranLU.sparse_ftran      0.005   1000  thrpt    3   3573.344 ±  136.410  ops/s
BenchmarkFtranBtranLU.sparse_ftran      0.005   2000  thrpt    3    420.229 ±    5.610  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.01    500  thrpt    3  11201.267 ±  472.675  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.01   1000  thrpt    3   1782.340 ±   40.507  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.01   2000  thrpt    3    329.854 ±   10.079  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.02    500  thrpt    3   6850.473 ±  125.651  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.02   1000  thrpt    3   1386.778 ±   24.207  ops/s
BenchmarkFtranBtranLU.sparse_ftran       0.02   2000  thrpt    3    296.519 ±    4.096  ops/s
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