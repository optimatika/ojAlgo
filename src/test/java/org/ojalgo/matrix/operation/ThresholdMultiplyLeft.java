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
 * Mac Pro. 2017-08-16 => 16
 *
 * <pre>
# Run complete. Total time: 00:01:28

Benchmark                   (dim)  (z)   Mode  Cnt        Score         Error    Units
ThresholdMultiplyLeft.tune      8    1  thrpt    3  7486576.379 ±   50550.285  ops/min
ThresholdMultiplyLeft.tune      8    2  thrpt    3  1256364.920 ±   15629.756  ops/min
ThresholdMultiplyLeft.tune     16    1  thrpt    3  6034091.209 ±  449494.011  ops/min
ThresholdMultiplyLeft.tune     16    2  thrpt    3  1219166.636 ±   79791.638  ops/min
ThresholdMultiplyLeft.tune     32    1  thrpt    3   931258.161 ± 2242906.125  ops/min
ThresholdMultiplyLeft.tune     32    2  thrpt    3   942363.439 ±  294455.026  ops/min
ThresholdMultiplyLeft.tune     64    1  thrpt    3   158591.994 ±    1437.316  ops/min
ThresholdMultiplyLeft.tune     64    2  thrpt    3   412682.819 ±  143663.913  ops/min
ThresholdMultiplyLeft.tune    128    1  thrpt    3    22318.964 ±     357.957  ops/min
ThresholdMultiplyLeft.tune    128    2  thrpt    3    77806.891 ±    1061.067  ops/min
 * </pre>
 *
 * MacBook Air. 2015-06-27 => 32
 *
 * <pre>
# Run complete. Total time: 00:01:25

Benchmark                   (dim)  (z)   Mode  Cnt         Score         Error    Units
ThresholdMultiplyLeft.tune      8    1  thrpt    3  17735108,881 ±  879865,876  ops/min
ThresholdMultiplyLeft.tune      8    2  thrpt    3   1103305,318 ±  784771,896  ops/min
ThresholdMultiplyLeft.tune     16    1  thrpt    3   9043222,483 ± 4440280,902  ops/min
ThresholdMultiplyLeft.tune     16    2  thrpt    3    989021,285 ±  893301,002  ops/min
ThresholdMultiplyLeft.tune     32    1  thrpt    3   1431336,096 ±  491276,178  ops/min
ThresholdMultiplyLeft.tune     32    2  thrpt    3    770786,217 ±  654645,207  ops/min
ThresholdMultiplyLeft.tune     64    1  thrpt    3    192464,841 ±   46965,287  ops/min
ThresholdMultiplyLeft.tune     64    2  thrpt    3    304053,329 ±  166260,345  ops/min
ThresholdMultiplyLeft.tune    128    1  thrpt    3     24843,283 ±   10248,266  ops/min
ThresholdMultiplyLeft.tune    128    2  thrpt    3     56610,220 ±    4850,227  ops/min
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2021-07-08 => 32
 *
 * <pre>
Benchmark                   (dim)  (z)   Mode  Cnt         Score         Error    Units
ThresholdMultiplyLeft.tune     16    1  thrpt    3  12321091.209 ± 2039034.843  ops/min
ThresholdMultiplyLeft.tune     16    2  thrpt    3   4370127.400 ±  453701.725  ops/min
ThresholdMultiplyLeft.tune     16    4  thrpt    3   2299080.203 ±  669580.632  ops/min
ThresholdMultiplyLeft.tune     32    1  thrpt    3   2327673.885 ±  107507.179  ops/min
ThresholdMultiplyLeft.tune     32    2  thrpt    3   2016947.208 ±  108527.210  ops/min
ThresholdMultiplyLeft.tune     32    4  thrpt    3   1375674.120 ±  503001.297  ops/min
ThresholdMultiplyLeft.tune     64    1  thrpt    3    343804.844 ±   45763.720  ops/min
ThresholdMultiplyLeft.tune     64    2  thrpt    3    462681.416 ±   14775.273  ops/min
ThresholdMultiplyLeft.tune     64    4  thrpt    3    477745.799 ±  216938.256  ops/min
ThresholdMultiplyLeft.tune    128    1  thrpt    3     47393.831 ±   28131.974  ops/min
ThresholdMultiplyLeft.tune    128    2  thrpt    3     70191.920 ±   95208.621  ops/min
ThresholdMultiplyLeft.tune    128    4  thrpt    3     98211.691 ±   54457.402  ops/min
ThresholdMultiplyLeft.tune    256    1  thrpt    3      5857.360 ±    9761.232  ops/min
ThresholdMultiplyLeft.tune    256    2  thrpt    3     10952.948 ±     205.508  ops/min
ThresholdMultiplyLeft.tune    256    4  thrpt    3     17060.206 ±    2651.255  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdMultiplyLeft extends MultiplyThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdTuner.options(), ThresholdMultiplyLeft.class);
    }

    @Param({ "16", "32", "64", "128", "256" })
    public int dim;

    @Override
    @Setup
    public void setup() {

        MultiplyLeft.THRESHOLD = dim / z;

        benchmark = new MultiplyThresholdTuner.CodeAndData(dim, true, false);
    }

}
