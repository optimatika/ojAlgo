/*
 * Copyright 1997-2022 Optimatika
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
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro:
 *
 * <pre>
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2022-01-06 => CORES
 *
 * <pre>
Benchmark                     (parallelism)   Mode  Cnt  Score   Error    Units
ParallelismMultiplyLeft.tune          UNITS  thrpt    3  0.581 ± 1.557  ops/min
ParallelismMultiplyLeft.tune          CORES  thrpt    3  0.975 ± 0.029  ops/min
ParallelismMultiplyLeft.tune        THREADS  thrpt    3  0.955 ± 0.055  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ParallelismMultiplyLeft extends ParallelismTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ParallelismTuner.options(), ParallelismMultiplyLeft.class);
    }

    MultiplyThresholdTuner.CodeAndData benchmark;

    @Override
    @Setup
    public void setup() {

        MultiplyLeft.PARALLELISM = parallelism;

        benchmark = new MultiplyThresholdTuner.CodeAndData(DIM, true, false);
    }

    @Override
    @Benchmark
    public Object tune() {
        return benchmark.execute();
    }

}