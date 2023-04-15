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
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * MacBook Pro: 2017-11-28 => 32
 *
 * <pre>
Benchmark                      (dim)  (z)   Mode  Cnt          Score        Error    Units
ThresholdMultiplyNeither.tune      8    1  thrpt    5  100165562.893 ± 487581.344  ops/min
ThresholdMultiplyNeither.tune      8    2  thrpt    5    1224455.193 ±  17242.789  ops/min
ThresholdMultiplyNeither.tune     16    1  thrpt    5    6103235.893 ± 121424.112  ops/min
ThresholdMultiplyNeither.tune     16    2  thrpt    5    1198383.898 ±  40991.241  ops/min
ThresholdMultiplyNeither.tune     32    1  thrpt    5     913934.467 ±   2224.720  ops/min
ThresholdMultiplyNeither.tune     32    2  thrpt    5     906993.004 ±  23080.158  ops/min
ThresholdMultiplyNeither.tune     64    1  thrpt    5     118052.610 ±    153.411  ops/min
ThresholdMultiplyNeither.tune     64    2  thrpt    5     356521.307 ±   4806.156  ops/min
ThresholdMultiplyNeither.tune    128    1  thrpt    5      21722.580 ±  26793.071  ops/min
ThresholdMultiplyNeither.tune    128    2  thrpt    5      66277.842 ±    836.778  ops/min
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2022-01-06 => 32 (maybe 16)
 *
 * <pre>
Benchmark                      (dim)  (z)   Mode  Cnt         Score         Error    Units
ThresholdMultiplyNeither.tune     16    1  thrpt    3  21084009.287 ± 3258299.677  ops/min
ThresholdMultiplyNeither.tune     16    2  thrpt    3   5204243.161 ±  296473.952  ops/min
ThresholdMultiplyNeither.tune     16    4  thrpt    3   2401115.498 ± 1183573.514  ops/min
ThresholdMultiplyNeither.tune     32    1  thrpt    3   2818699.905 ± 1630469.319  ops/min
ThresholdMultiplyNeither.tune     32    2  thrpt    3   2999934.179 ±  738472.613  ops/min
ThresholdMultiplyNeither.tune     32    4  thrpt    3   1951145.428 ±  924230.954  ops/min
ThresholdMultiplyNeither.tune     64    1  thrpt    3    385491.666 ±   65830.668  ops/min
ThresholdMultiplyNeither.tune     64    2  thrpt    3    676951.262 ±  269298.379  ops/min
ThresholdMultiplyNeither.tune     64    4  thrpt    3    838715.568 ±  506708.696  ops/min
ThresholdMultiplyNeither.tune    128    1  thrpt    3     47859.384 ±   13448.113  ops/min
ThresholdMultiplyNeither.tune    128    2  thrpt    3    107449.078 ±     983.852  ops/min
ThresholdMultiplyNeither.tune    128    4  thrpt    3    149453.928 ±   80750.534  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdMultiplyNeither extends MultiplyThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdTuner.options(), ThresholdMultiplyNeither.class);
    }

    @Param({ "8", "16", "32", "64", "128", "256" })
    public int dim;

    @Override
    @Setup
    public void setup() {

        MultiplyNeither.THRESHOLD = dim / z;

        benchmark = new MultiplyThresholdTuner.CodeAndData(dim, false, false);
    }

}
