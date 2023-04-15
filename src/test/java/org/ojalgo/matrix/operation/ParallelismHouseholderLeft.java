/*
 * Copyright 1997-2023 Optimatika
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
package org.ojalgo.matrix.operation;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.matrix.decomposition.PrimitiveOrRawQR;
import org.ojalgo.matrix.decomposition.QR;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro (Early 2009): 2022-01-13 => THREADS
 *
 * <pre>
Benchmark                        (parallelism)   Mode  Cnt  Score   Error    Units
ParallelismHouseholderLeft.tune          UNITS  thrpt    3  0.192 ± 0.153  ops/min
ParallelismHouseholderLeft.tune          CORES  thrpt    3  0.736 ± 0.178  ops/min
ParallelismHouseholderLeft.tune        THREADS  thrpt    3  0.852 ± 0.025  ops/min
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2022-01-13 => CORES
 *
 * <pre>
Benchmark                        (parallelism)   Mode  Cnt  Score   Error    Units
ParallelismHouseholderLeft.tune          UNITS  thrpt    3  0.448 ± 0.067  ops/min
ParallelismHouseholderLeft.tune          CORES  thrpt    3  1.126 ± 0.137  ops/min
ParallelismHouseholderLeft.tune        THREADS  thrpt    3  1.080 ± 0.062  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ParallelismHouseholderLeft extends ParallelismTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ParallelismTuner.options(), ParallelismHouseholderLeft.class);
    }

    PrimitiveOrRawQR.CodeAndData benchmark;
    QR<Double> decomposition;

    @Override
    @Setup
    public void setup() {

        HouseholderLeft.PARALLELISM = parallelism;

        benchmark = new PrimitiveOrRawQR.CodeAndData(DIM);
        decomposition = PrimitiveOrRawQR.CodeAndData.newPrimitive();
    }

    @Override
    @Benchmark
    public Object tune() {
        return benchmark.execute(decomposition);
    }

}
