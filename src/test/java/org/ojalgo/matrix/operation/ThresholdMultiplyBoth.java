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
package org.ojalgo.matrix.operation;

import org.ojalgo.BenchmarkUtils;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * MacBook Air. 2015-06-27 => 16
 *
 * <pre>
Benchmark                   (dim)  (z)   Mode  Cnt         Score         Error    Units
ThresholdMultiplyBoth.tune      8    1  thrpt    3  17128292,840 ± 2991881,643  ops/min
ThresholdMultiplyBoth.tune      8    2  thrpt    3   1131723,502 ±  564612,285  ops/min
ThresholdMultiplyBoth.tune     16    1  thrpt    3   2257845,836 ± 1237077,126  ops/min
ThresholdMultiplyBoth.tune     16    2  thrpt    3    848425,485 ±  361964,884  ops/min
ThresholdMultiplyBoth.tune     32    1  thrpt    3    300695,106 ±   22419,156  ops/min
ThresholdMultiplyBoth.tune     32    2  thrpt    3    366420,327 ±  132391,475  ops/min
ThresholdMultiplyBoth.tune     64    1  thrpt    3     38720,797 ±   12963,650  ops/min
ThresholdMultiplyBoth.tune     64    2  thrpt    3     66750,322 ±   11931,145  ops/min
ThresholdMultiplyBoth.tune    128    1  thrpt    3      4805,198 ±     406,351  ops/min
ThresholdMultiplyBoth.tune    128    2  thrpt    3      8763,536 ±     572,748  ops/min
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2022-01-09 => 8
 *
 * <pre>
Benchmark                   (dim)  (z)   Mode  Cnt          Score          Error    Units
ThresholdMultiplyBoth.tune      4    1  thrpt    3  221010762.892 ± 95554449.586  ops/min
ThresholdMultiplyBoth.tune      4    2  thrpt    3    6410976.153 ±  1836355.817  ops/min
ThresholdMultiplyBoth.tune      4    4  thrpt    3    3048698.158 ±  1195662.961  ops/min
ThresholdMultiplyBoth.tune      8    1  thrpt    3   14170176.188 ±  2533321.921  ops/min
ThresholdMultiplyBoth.tune      8    2  thrpt    3    5182995.091 ±   761623.394  ops/min
ThresholdMultiplyBoth.tune      8    4  thrpt    3    2370199.173 ±   287700.125  ops/min
ThresholdMultiplyBoth.tune     16    1  thrpt    3    1853268.068 ±   206242.761  ops/min
ThresholdMultiplyBoth.tune     16    2  thrpt    3    2130571.049 ±  1237683.162  ops/min
ThresholdMultiplyBoth.tune     16    4  thrpt    3    1702310.870 ±   394224.420  ops/min
ThresholdMultiplyBoth.tune     32    1  thrpt    3     235521.617 ±   191388.948  ops/min
ThresholdMultiplyBoth.tune     32    2  thrpt    3     386306.914 ±     8371.932  ops/min
ThresholdMultiplyBoth.tune     32    4  thrpt    3     545974.483 ±   238381.834  ops/min
ThresholdMultiplyBoth.tune     64    1  thrpt    3      29433.007 ±    24440.485  ops/min
ThresholdMultiplyBoth.tune     64    2  thrpt    3      52450.287 ±     2508.972  ops/min
ThresholdMultiplyBoth.tune     64    4  thrpt    3      79512.315 ±     9900.565  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdMultiplyBoth extends MultiplyThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdTuner.options(), ThresholdMultiplyBoth.class);
    }

    @Param({ "4", "8", "16", "32", "64" })
    public int dim;

    @Override
    @Setup
    public void setup() {

        MultiplyBoth.THRESHOLD = dim / z;

        benchmark = new MultiplyThresholdTuner.CodeAndData(dim, true, true);
    }

}
