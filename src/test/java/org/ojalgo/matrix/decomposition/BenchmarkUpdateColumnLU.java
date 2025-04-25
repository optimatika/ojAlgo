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
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.SparseStore;
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
# Run complete. Total time: 00:09:48

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                       (density)  (dim)   Mode  Cnt     Score     Error  Units
BenchmarkUpdateColumnLU.dense        0.01    500  thrpt    3  5312.442 ± 916.871  ops/s
BenchmarkUpdateColumnLU.raw          0.01    500  thrpt    3  8358.566 ± 657.719  ops/s
BenchmarkUpdateColumnLU.sparse       0.01    500  thrpt    3     0.014 ±   0.125  ops/s
 * </pre>
 */
@State(Scope.Benchmark)
public class BenchmarkUpdateColumnLU extends AbstractBenchmarkSparseLU {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(BenchmarkUpdateColumnLU.class);
    }

    @Param({ "0.005", "0.01", "0.02", "0.05" })
    public double density;
    @Param({ "500", "1000", "2000" })
    public int dim;

    int index;
    PhysicalStore<Double> vector;
    PhysicalStore<Double> work;

    @Benchmark
    public LU<Double> dense() {
        dense.updateColumn(index, vector, work);
        return dense;
    }

    @Benchmark
    public LU<Double> raw() {
        raw.updateColumn(index, vector, work);
        return raw;
    }

    @Setup(Level.Trial)
    public void setup() {

        SparseStore<Double> matrix = AbstractBenchmarkSparseLU.newSparseMatrix(dim, density);
        // Decompose matrices
        sparse.decompose(matrix);
        dense.decompose(matrix);
        raw.decompose(matrix);

        vector = AbstractBenchmarkSparseLU.newDenseVector(dim);
        work = R064Store.FACTORY.make(dim, 1);
        index = Uniform.randomInteger(dim);
    }

    @Benchmark
    public LU<Double> sparse() {
        sparse.updateColumn(index, vector, work);
        return sparse;
    }

}