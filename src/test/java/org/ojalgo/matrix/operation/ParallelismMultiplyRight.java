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
 * Mac Pro (Early 2009): 2022-01-07 => CORES
 *
 * <pre>
Benchmark                      (parallelism)   Mode  Cnt  Score   Error    Units
ParallelismMultiplyRight.tune          UNITS  thrpt    3  0.275 ± 0.179  ops/min
ParallelismMultiplyRight.tune          CORES  thrpt    3  1.184 ± 0.737  ops/min
ParallelismMultiplyRight.tune        THREADS  thrpt    3  1.317 ± 0.473  ops/min
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2022-01-06 => CORES
 *
 * <pre>
Benchmark                      (parallelism)   Mode  Cnt  Score   Error    Units
ParallelismMultiplyRight.tune          UNITS  thrpt    3  0.571 ± 0.117  ops/min
ParallelismMultiplyRight.tune          CORES  thrpt    3  3.080 ± 3.383  ops/min
ParallelismMultiplyRight.tune        THREADS  thrpt    3  3.261 ± 0.779  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ParallelismMultiplyRight extends ParallelismTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ParallelismTuner.options(), ParallelismMultiplyRight.class);
    }

    MultiplyThresholdTuner.CodeAndData benchmark;

    @Override
    @Setup
    public void setup() {

        MultiplyRight.PARALLELISM = parallelism;

        benchmark = new MultiplyThresholdTuner.CodeAndData(DIM, false, true);
    }

    @Override
    @Benchmark
    public Object tune() {
        return benchmark.execute();
    }

}
