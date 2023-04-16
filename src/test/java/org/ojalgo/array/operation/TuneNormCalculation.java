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
package org.ojalgo.array.operation;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * MacBook Pro (16-inch, 2019): 2022-01-15
 *
 * <pre>
Benchmark                       (dim)   Mode  Cnt            Score            Error    Units
TuneNormCalculation.naive           1  thrpt    3  18708411390.604 ± 9906833392.816  ops/min
TuneNormCalculation.doubleLoop      1  thrpt    3  10818037114.798 ± 3303127188.748  ops/min
TuneNormCalculation.usingHypot      1  thrpt    3  13035352977.438 ± 3126782975.300  ops/min
TuneNormCalculation.naive           2  thrpt    3  16299264814.873 ± 4821991013.927  ops/min
TuneNormCalculation.doubleLoop      2  thrpt    3   7681982893.182 ± 2110796168.687  ops/min
TuneNormCalculation.usingHypot      2  thrpt    3   6870674669.808 ±  266824075.431  ops/min
TuneNormCalculation.naive           5  thrpt    3  10617237742.320 ± 1907742353.636  ops/min
TuneNormCalculation.doubleLoop      5  thrpt    3   5378778611.967 ±  605717771.217  ops/min
TuneNormCalculation.usingHypot      5  thrpt    3   2497331628.473 ±  150782972.701  ops/min
TuneNormCalculation.naive          10  thrpt    3   6624554579.603 ± 7146448322.912  ops/min
TuneNormCalculation.doubleLoop     10  thrpt    3   3061786830.483 ±  413407943.379  ops/min
TuneNormCalculation.usingHypot     10  thrpt    3    955846544.250 ±   20276843.447  ops/min
TuneNormCalculation.naive          20  thrpt    3   5660136651.780 ± 2417222267.783  ops/min
TuneNormCalculation.doubleLoop     20  thrpt    3   1457255324.311 ±  172947053.531  ops/min
TuneNormCalculation.usingHypot     20  thrpt    3    358290813.975 ±   22152483.268  ops/min
TuneNormCalculation.naive          50  thrpt    3   1956215393.407 ±  471002067.709  ops/min
TuneNormCalculation.doubleLoop     50  thrpt    3    620924025.117 ±  107283810.336  ops/min
TuneNormCalculation.usingHypot     50  thrpt    3    125101844.494 ±   10096184.765  ops/min
TuneNormCalculation.naive         100  thrpt    3    940850035.704 ±  692026521.507  ops/min
TuneNormCalculation.doubleLoop    100  thrpt    3    308744839.246 ±   39849924.913  ops/min
TuneNormCalculation.usingHypot    100  thrpt    3     59986590.915 ±    1249237.745  ops/min
TuneNormCalculation.naive         200  thrpt    3    398767418.368 ±   94166784.824  ops/min
TuneNormCalculation.doubleLoop    200  thrpt    3    154663339.211 ±   29805687.402  ops/min
TuneNormCalculation.usingHypot    200  thrpt    3     29394072.537 ±    1296217.022  ops/min
TuneNormCalculation.naive         500  thrpt    3    141944610.712 ±    5087365.803  ops/min
TuneNormCalculation.doubleLoop    500  thrpt    3     55708172.187 ±   11269452.293  ops/min
TuneNormCalculation.usingHypot    500  thrpt    3     11515185.823 ±    1674013.826  ops/min
TuneNormCalculation.naive        1000  thrpt    3     68216688.740 ±     610232.555  ops/min
TuneNormCalculation.doubleLoop   1000  thrpt    3     29380564.059 ±    1030529.827  ops/min
TuneNormCalculation.usingHypot   1000  thrpt    3      5782976.519 ±     396260.347  ops/min
TuneNormCalculation.naive        2000  thrpt    3     33575083.783 ±    2632859.047  ops/min
TuneNormCalculation.doubleLoop   2000  thrpt    3     14558321.580 ±     463161.486  ops/min
TuneNormCalculation.usingHypot   2000  thrpt    3      2884279.531 ±     199102.579  ops/min
TuneNormCalculation.naive        5000  thrpt    3     12840789.486 ±    6922125.186  ops/min
TuneNormCalculation.doubleLoop   5000  thrpt    3      5759508.894 ±    5014717.918  ops/min
TuneNormCalculation.usingHypot   5000  thrpt    3      1152696.289 ±      50131.111  ops/min
TuneNormCalculation.naive       10000  thrpt    3      6481765.593 ±    1328276.858  ops/min
TuneNormCalculation.doubleLoop  10000  thrpt    3      2585402.789 ±    2322696.945  ops/min
TuneNormCalculation.usingHypot  10000  thrpt    3       577175.912 ±      37372.262  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class TuneNormCalculation {

    @Param({ "1", "2", "5", "10", "20", "50", "100", "200", "500", "1000", "2000", "5000", "10000" })
    public int dim;

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(TuneNormCalculation.class);
    }

    public double[] array;

    @Setup
    public void setup() {

        ArrayR064 l = ArrayR064.make(dim);
        l.fillAll(Uniform.standard());

        array = l.data;
    }

    @Benchmark
    public double naive() {
        double retVal = 0D;
        for (int i = 0; i < array.length; i++) {
            double tmpVal = array[i];
            retVal += tmpVal * tmpVal;
        }
        return retVal;
    }

    @Benchmark
    public double usingHypot() {
        double retVal = 0D;
        for (int i = 0; i < array.length; i++) {
            retVal = MissingMath.hypot(retVal, array[i]);
        }
        return retVal;
    }

    @Benchmark
    public double doubleLoop() {
        double infNorm = 0D;
        for (int i = 0; i < array.length; i++) {
            infNorm = Math.max(infNorm, Math.abs(array[i]));
        }
        double retVal = 0D;
        for (int i = 0; i < array.length; i++) {
            double tmpVal = array[i] / infNorm;
            retVal += tmpVal * tmpVal;
        }
        return Math.sqrt(retVal) * infNorm;
    }

}
