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
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro (Early 2009): 2022-01-08 => CORES/THREADS
 *
 * <pre>
Benchmark                        (parallelism)   Mode  Cnt  Score   Error    Units
ParallelismMultiplyNeither.tune          UNITS  thrpt    3  0.291 ± 0.057  ops/min
ParallelismMultiplyNeither.tune          CORES  thrpt    3  1.278 ± 0.880  ops/min
ParallelismMultiplyNeither.tune        THREADS  thrpt    3  1.450 ± 0.401  ops/min
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2022-01-06 => THREADS
 *
 * <pre>
Benchmark                        (parallelism)   Mode  Cnt  Score   Error    Units
ParallelismMultiplyNeither.tune          UNITS  thrpt    3  0.580 ± 0.112  ops/min
ParallelismMultiplyNeither.tune          CORES  thrpt    3  2.844 ± 3.069  ops/min
ParallelismMultiplyNeither.tune        THREADS  thrpt    3  3.318 ± 0.914  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ParallelismMultiplyNeither extends ParallelismTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ParallelismTuner.options(), ParallelismMultiplyNeither.class);
    }

    MultiplyThresholdTuner.CodeAndData benchmark;

    @Override
    @Setup
    public void setup() {

        MultiplyNeither.PARALLELISM = parallelism;

        benchmark = new MultiplyThresholdTuner.CodeAndData(DIM, false, false);
    }

    @Override
    @Benchmark
    public Object tune() {
        return benchmark.execute();
    }

}
