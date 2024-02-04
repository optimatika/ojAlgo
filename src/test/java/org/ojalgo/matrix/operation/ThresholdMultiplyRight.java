/*
 * Copyright 1997-2024 Optimatika
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
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro. 2015-06-24 => ?
 *
 * <pre>
 * </pre>
 *
 * MacBook Air: 2015-06-27 => 32
 *
 * <pre>
Benchmark                    (dim)  (z)   Mode  Cnt         Score          Error    Units
ThresholdMultiplyRight.tune      8    1  thrpt    3  73994274,805 ± 56632052,355  ops/min
ThresholdMultiplyRight.tune      8    2  thrpt    3   1063917,938 ±   859526,602  ops/min
ThresholdMultiplyRight.tune     16    1  thrpt    3   6462490,133 ±  3023992,427  ops/min
ThresholdMultiplyRight.tune     16    2  thrpt    3   1027540,843 ±  1884022,995  ops/min
ThresholdMultiplyRight.tune     32    1  thrpt    3    968094,105 ±  1658828,115  ops/min
ThresholdMultiplyRight.tune     32    2  thrpt    3    795953,054 ±   281585,089  ops/min
ThresholdMultiplyRight.tune     64    1  thrpt    3    194669,580 ±    28140,716  ops/min
ThresholdMultiplyRight.tune     64    2  thrpt    3    275349,177 ±    94609,126  ops/min
ThresholdMultiplyRight.tune    128    1  thrpt    3     25848,018 ±     6415,269  ops/min
ThresholdMultiplyRight.tune    128    2  thrpt    3     45033,847 ±    11734,425  ops/min
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2022-01-06 => 32
 *
 * <pre>
Benchmark                    (dim)  (z)   Mode  Cnt         Score         Error    Units
ThresholdMultiplyRight.tune     16    1  thrpt    3  12197428.702 ± 2858267.720  ops/min
ThresholdMultiplyRight.tune     16    2  thrpt    3   4852895.726 ± 1756527.010  ops/min
ThresholdMultiplyRight.tune     16    4  thrpt    3   2506256.720 ± 1078102.143  ops/min
ThresholdMultiplyRight.tune     32    1  thrpt    3   2184587.576 ±  565552.024  ops/min
ThresholdMultiplyRight.tune     32    2  thrpt    3   2144548.932 ±   58913.513  ops/min
ThresholdMultiplyRight.tune     32    4  thrpt    3   1599414.696 ±  521034.208  ops/min
ThresholdMultiplyRight.tune     64    1  thrpt    3    341108.288 ±  228935.395  ops/min
ThresholdMultiplyRight.tune     64    2  thrpt    3    488833.547 ±  290624.790  ops/min
ThresholdMultiplyRight.tune     64    4  thrpt    3    642903.409 ±  110833.340  ops/min
ThresholdMultiplyRight.tune    128    1  thrpt    3     36280.618 ±   17930.865  ops/min
ThresholdMultiplyRight.tune    128    2  thrpt    3     78613.894 ±   20212.967  ops/min
ThresholdMultiplyRight.tune    128    4  thrpt    3    110284.853 ±   70144.087  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdMultiplyRight extends MultiplyThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdTuner.options(), ThresholdMultiplyRight.class);
    }

    @Param({ "16", "32", "64", "128" })
    public int dim;

    @Override
    @Setup
    public void setup() {

        MultiplyRight.THRESHOLD = dim / z;

        benchmark = new MultiplyThresholdTuner.CodeAndData(dim, false, true);
    }

}
