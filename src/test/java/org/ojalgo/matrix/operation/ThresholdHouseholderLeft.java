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
import org.ojalgo.matrix.decomposition.PrimitiveOrRawQR;
import org.ojalgo.matrix.decomposition.QR;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro (Early 2009): 2022-01-17 => 128 (64)
 *
 * <pre>
Benchmark                               (dim)  (z)   Mode  Cnt       Score        Error    Units
ThresholdHouseholderLeft.tune              32    1  thrpt    3  353553.344 ± 141913.307  ops/min
ThresholdHouseholderLeft.tune              32    2  thrpt    3  119746.585 ±  55186.997  ops/min
ThresholdHouseholderLeft.tune              32    4  thrpt    3   51450.041 ±   2847.745  ops/min
ThresholdHouseholderLeft.tune              64    1  thrpt    3   70433.582 ±   5717.678  ops/min
ThresholdHouseholderLeft.tune              64    2  thrpt    3   38121.514 ±   6985.214  ops/min
ThresholdHouseholderLeft.tune              64    4  thrpt    3   19995.023 ±   1707.662  ops/min
ThresholdHouseholderLeft.tune             128    1  thrpt    3   12598.913 ±   1066.256  ops/min
ThresholdHouseholderLeft.tune             128    2  thrpt    3   10199.605 ±    823.861  ops/min
ThresholdHouseholderLeft.tune             128    4  thrpt    3    7176.843 ±   1023.984  ops/min
ThresholdHouseholderLeft.tune             256    1  thrpt    3    2263.744 ±    358.395  ops/min
ThresholdHouseholderLeft.tune             256    2  thrpt    3    2508.761 ±    311.591  ops/min
ThresholdHouseholderLeft.tune             256    4  thrpt    3    2230.713 ±    317.781  ops/min
ThresholdHouseholderLeft.tune             512    1  thrpt    3     212.039 ±      6.149  ops/min
ThresholdHouseholderLeft.tune             512    2  thrpt    3     314.946 ±     13.375  ops/min
ThresholdHouseholderLeft.tune             512    4  thrpt    3     409.597 ±     15.239  ops/min
ThresholdHouseholderLeft.tune            1024    1  thrpt    3      22.186 ±      0.942  ops/min
ThresholdHouseholderLeft.tune            1024    2  thrpt    3      34.647 ±      4.798  ops/min
ThresholdHouseholderLeft.tune            1024    4  thrpt    3      55.926 ±      7.290  ops/min
ThresholdHouseholderLeft.tune            2048    1  thrpt    3       2.719 ±      0.239  ops/min
ThresholdHouseholderLeft.tune            2048    2  thrpt    3       4.224 ±      1.295  ops/min
ThresholdHouseholderLeft.tune            2048    4  thrpt    3       6.832 ±      1.005  ops/min
ThresholdHouseholderLeft.tunePrimitive     32    1  thrpt    3  486822.636 ±  12911.405  ops/min
ThresholdHouseholderLeft.tunePrimitive     32    2  thrpt    3  127491.120 ±  12319.121  ops/min
ThresholdHouseholderLeft.tunePrimitive     32    4  thrpt    3   48232.766 ±  11935.789  ops/min
ThresholdHouseholderLeft.tunePrimitive     64    1  thrpt    3   83020.062 ±   2576.902  ops/min
ThresholdHouseholderLeft.tunePrimitive     64    2  thrpt    3   43599.557 ±   1937.552  ops/min
ThresholdHouseholderLeft.tunePrimitive     64    4  thrpt    3   22591.907 ±   1550.370  ops/min
ThresholdHouseholderLeft.tunePrimitive    128    1  thrpt    3   12161.260 ±    520.059  ops/min
ThresholdHouseholderLeft.tunePrimitive    128    2  thrpt    3   11145.088 ±    537.810  ops/min
ThresholdHouseholderLeft.tunePrimitive    128    4  thrpt    3    8186.468 ±   3166.387  ops/min
ThresholdHouseholderLeft.tunePrimitive    256    1  thrpt    3    1615.460 ±     88.902  ops/min
ThresholdHouseholderLeft.tunePrimitive    256    2  thrpt    3    1925.502 ±     73.646  ops/min
ThresholdHouseholderLeft.tunePrimitive    256    4  thrpt    3    2205.700 ±    165.989  ops/min
ThresholdHouseholderLeft.tunePrimitive    512    1  thrpt    3     211.717 ±     11.231  ops/min
ThresholdHouseholderLeft.tunePrimitive    512    2  thrpt    3     312.709 ±     12.086  ops/min
ThresholdHouseholderLeft.tunePrimitive    512    4  thrpt    3     412.284 ±     11.886  ops/min
ThresholdHouseholderLeft.tunePrimitive   1024    1  thrpt    3      23.376 ±      6.975  ops/min
ThresholdHouseholderLeft.tunePrimitive   1024    2  thrpt    3      35.223 ±      9.804  ops/min
ThresholdHouseholderLeft.tunePrimitive   1024    4  thrpt    3      59.801 ±      9.395  ops/min
ThresholdHouseholderLeft.tunePrimitive   2048    1  thrpt    3       2.781 ±      0.152  ops/min
ThresholdHouseholderLeft.tunePrimitive   2048    2  thrpt    3       4.298 ±      0.012  ops/min
ThresholdHouseholderLeft.tunePrimitive   2048    4  thrpt    3       6.847 ±      1.308  ops/min
ThresholdHouseholderLeft.tuneRaw           32    1  thrpt    3  324680.271 ±   4626.744  ops/min
ThresholdHouseholderLeft.tuneRaw           32    2  thrpt    3  106183.631 ±   8125.122  ops/min
ThresholdHouseholderLeft.tuneRaw           32    4  thrpt    3   47089.852 ±  13618.248  ops/min
ThresholdHouseholderLeft.tuneRaw           64    1  thrpt    3   70417.323 ±   4806.951  ops/min
ThresholdHouseholderLeft.tuneRaw           64    2  thrpt    3   39712.508 ±  10507.199  ops/min
ThresholdHouseholderLeft.tuneRaw           64    4  thrpt    3   19935.458 ±    540.976  ops/min
ThresholdHouseholderLeft.tuneRaw          128    1  thrpt    3   13334.190 ±    542.080  ops/min
ThresholdHouseholderLeft.tuneRaw          128    2  thrpt    3   10945.330 ±    547.596  ops/min
ThresholdHouseholderLeft.tuneRaw          128    4  thrpt    3    7026.944 ±    616.661  ops/min
ThresholdHouseholderLeft.tuneRaw          256    1  thrpt    3    2207.517 ±    308.655  ops/min
ThresholdHouseholderLeft.tuneRaw          256    2  thrpt    3    2461.649 ±    542.202  ops/min
ThresholdHouseholderLeft.tuneRaw          256    4  thrpt    3    2234.749 ±    280.125  ops/min
ThresholdHouseholderLeft.tuneRaw          512    1  thrpt    3     285.837 ±     36.702  ops/min
ThresholdHouseholderLeft.tuneRaw          512    2  thrpt    3     390.173 ±     31.482  ops/min
ThresholdHouseholderLeft.tuneRaw          512    4  thrpt    3     471.644 ±     27.007  ops/min
ThresholdHouseholderLeft.tuneRaw         1024    1  thrpt    3      28.784 ±      4.565  ops/min
ThresholdHouseholderLeft.tuneRaw         1024    2  thrpt    3      42.546 ±      7.849  ops/min
ThresholdHouseholderLeft.tuneRaw         1024    4  thrpt    3      66.377 ±     10.566  ops/min
ThresholdHouseholderLeft.tuneRaw         2048    1  thrpt    3       3.166 ±      0.159  ops/min
ThresholdHouseholderLeft.tuneRaw         2048    2  thrpt    3       4.728 ±      0.425  ops/min
ThresholdHouseholderLeft.tuneRaw         2048    4  thrpt    3       7.356 ±      0.601  ops/min
 * </pre>
 *
 * Mac Pro (Early 2009): 2022-01-13 => 64 (128)
 *
 * <pre>
Benchmark                      (dim)  (z)   Mode  Cnt       Score       Error    Units
ThresholdHouseholderLeft.tune     32    1  thrpt    3  504704.866 ± 32977.091  ops/min
ThresholdHouseholderLeft.tune     32    2  thrpt    3  138829.022 ± 10947.430  ops/min
ThresholdHouseholderLeft.tune     32    4  thrpt    3   55622.151 ±  2847.763  ops/min
ThresholdHouseholderLeft.tune     64    1  thrpt    3   86400.158 ±  9551.850  ops/min
ThresholdHouseholderLeft.tune     64    2  thrpt    3   47139.867 ±  2806.197  ops/min
ThresholdHouseholderLeft.tune     64    4  thrpt    3   21252.819 ±   826.063  ops/min
ThresholdHouseholderLeft.tune    128    1  thrpt    3   12829.606 ±   420.210  ops/min
ThresholdHouseholderLeft.tune    128    2  thrpt    3   12016.348 ±   969.765  ops/min
ThresholdHouseholderLeft.tune    128    4  thrpt    3    8395.980 ±  1691.322  ops/min
ThresholdHouseholderLeft.tune    256    1  thrpt    3    1672.029 ±    46.116  ops/min
ThresholdHouseholderLeft.tune    256    2  thrpt    3    2181.757 ±   114.159  ops/min
ThresholdHouseholderLeft.tune    256    4  thrpt    3    2194.723 ±  1463.957  ops/min
ThresholdHouseholderLeft.tune    512    1  thrpt    3     221.181 ±     2.988  ops/min
ThresholdHouseholderLeft.tune    512    2  thrpt    3     329.581 ±    20.506  ops/min
ThresholdHouseholderLeft.tune    512    4  thrpt    3     430.332 ±    55.390  ops/min
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2022-01-16 => 128
 *
 * <pre>
Benchmark                               (dim)  (z)   Mode  Cnt        Score        Error    Units
ThresholdHouseholderLeft.tune              32    1  thrpt    3   743827.775 ± 206997.962  ops/min
ThresholdHouseholderLeft.tune              32    2  thrpt    3   267997.686 ±  39839.443  ops/min
ThresholdHouseholderLeft.tune              32    4  thrpt    3   117358.956 ±  49409.487  ops/min
ThresholdHouseholderLeft.tune              64    1  thrpt    3   165746.270 ±  21479.859  ops/min
ThresholdHouseholderLeft.tune              64    2  thrpt    3    79892.108 ±  19676.734  ops/min
ThresholdHouseholderLeft.tune              64    4  thrpt    3    43392.288 ±  40683.387  ops/min
ThresholdHouseholderLeft.tune             128    1  thrpt    3    32238.618 ±   3089.051  ops/min
ThresholdHouseholderLeft.tune             128    2  thrpt    3    23068.980 ±   8393.886  ops/min
ThresholdHouseholderLeft.tune             128    4  thrpt    3    14800.804 ±   1693.224  ops/min
ThresholdHouseholderLeft.tune             256    1  thrpt    3     4987.486 ±    233.018  ops/min
ThresholdHouseholderLeft.tune             256    2  thrpt    3     4937.098 ±   2020.118  ops/min
ThresholdHouseholderLeft.tune             256    4  thrpt    3     4312.567 ±   1376.294  ops/min
ThresholdHouseholderLeft.tune             512    1  thrpt    3      514.295 ±    195.919  ops/min
ThresholdHouseholderLeft.tune             512    2  thrpt    3      686.387 ±    212.668  ops/min
ThresholdHouseholderLeft.tune             512    4  thrpt    3      898.419 ±     91.981  ops/min
ThresholdHouseholderLeft.tune            1024    1  thrpt    3       61.683 ±     54.179  ops/min
ThresholdHouseholderLeft.tune            1024    2  thrpt    3       96.678 ±     12.247  ops/min
ThresholdHouseholderLeft.tune            1024    4  thrpt    3      135.101 ±     12.652  ops/min
ThresholdHouseholderLeft.tune            2048    1  thrpt    3        6.898 ±      1.792  ops/min
ThresholdHouseholderLeft.tune            2048    2  thrpt    3       10.129 ±      1.407  ops/min
ThresholdHouseholderLeft.tune            2048    4  thrpt    3       14.574 ±      0.861  ops/min
ThresholdHouseholderLeft.tunePrimitive     32    1  thrpt    3  1432855.142 ± 122210.498  ops/min
ThresholdHouseholderLeft.tunePrimitive     32    2  thrpt    3   292542.108 ±  97224.456  ops/min
ThresholdHouseholderLeft.tunePrimitive     32    4  thrpt    3   117233.602 ±  53304.523  ops/min
ThresholdHouseholderLeft.tunePrimitive     64    1  thrpt    3   219729.292 ±  73119.604  ops/min
ThresholdHouseholderLeft.tunePrimitive     64    2  thrpt    3   104238.584 ±  34231.039  ops/min
ThresholdHouseholderLeft.tunePrimitive     64    4  thrpt    3    51244.995 ±   5815.955  ops/min
ThresholdHouseholderLeft.tunePrimitive    128    1  thrpt    3    29386.896 ±  14961.492  ops/min
ThresholdHouseholderLeft.tunePrimitive    128    2  thrpt    3    24584.616 ±  24000.894  ops/min
ThresholdHouseholderLeft.tunePrimitive    128    4  thrpt    3    19475.326 ±   6795.046  ops/min
ThresholdHouseholderLeft.tunePrimitive    256    1  thrpt    3     3962.804 ±    537.355  ops/min
ThresholdHouseholderLeft.tunePrimitive    256    2  thrpt    3     4957.450 ±   2566.185  ops/min
ThresholdHouseholderLeft.tunePrimitive    256    4  thrpt    3     4865.345 ±    942.564  ops/min
ThresholdHouseholderLeft.tunePrimitive    512    1  thrpt    3      526.393 ±     63.387  ops/min
ThresholdHouseholderLeft.tunePrimitive    512    2  thrpt    3      720.366 ±     19.834  ops/min
ThresholdHouseholderLeft.tunePrimitive    512    4  thrpt    3      925.873 ±    264.203  ops/min
ThresholdHouseholderLeft.tunePrimitive   1024    1  thrpt    3       64.065 ±      1.905  ops/min
ThresholdHouseholderLeft.tunePrimitive   1024    2  thrpt    3       94.866 ±     20.456  ops/min
ThresholdHouseholderLeft.tunePrimitive   1024    4  thrpt    3      137.201 ±     54.550  ops/min
ThresholdHouseholderLeft.tunePrimitive   2048    1  thrpt    3        6.861 ±      0.991  ops/min
ThresholdHouseholderLeft.tunePrimitive   2048    2  thrpt    3       10.283 ±      0.615  ops/min
ThresholdHouseholderLeft.tunePrimitive   2048    4  thrpt    3        5.876 ±      7.140  ops/min
ThresholdHouseholderLeft.tuneRaw           32    1  thrpt    3   575478.221 ± 606227.365  ops/min
ThresholdHouseholderLeft.tuneRaw           32    2  thrpt    3   155218.358 ±  68434.195  ops/min
ThresholdHouseholderLeft.tuneRaw           32    4  thrpt    3    43076.430 ±  45147.289  ops/min
ThresholdHouseholderLeft.tuneRaw           64    1  thrpt    3    83117.668 ±  91720.432  ops/min
ThresholdHouseholderLeft.tuneRaw           64    2  thrpt    3    54224.364 ±   3002.375  ops/min
ThresholdHouseholderLeft.tuneRaw           64    4  thrpt    3    16981.205 ±   9855.643  ops/min
ThresholdHouseholderLeft.tuneRaw          128    1  thrpt    3    16661.072 ±  20526.360  ops/min
ThresholdHouseholderLeft.tuneRaw          128    2  thrpt    3    15311.936 ±    598.576  ops/min
ThresholdHouseholderLeft.tuneRaw          128    4  thrpt    3     6143.786 ±    565.475  ops/min
ThresholdHouseholderLeft.tuneRaw          256    1  thrpt    3     3780.825 ±   6079.651  ops/min
ThresholdHouseholderLeft.tuneRaw          256    2  thrpt    3     3632.208 ±   9328.943  ops/min
ThresholdHouseholderLeft.tuneRaw          256    4  thrpt    3     2430.576 ±   2043.138  ops/min
ThresholdHouseholderLeft.tuneRaw          512    1  thrpt    3      530.161 ±    716.427  ops/min
ThresholdHouseholderLeft.tuneRaw          512    2  thrpt    3      756.044 ±     12.686  ops/min
ThresholdHouseholderLeft.tuneRaw          512    4  thrpt    3      582.835 ±   1257.191  ops/min
ThresholdHouseholderLeft.tuneRaw         1024    1  thrpt    3      107.226 ±     23.073  ops/min
ThresholdHouseholderLeft.tuneRaw         1024    2  thrpt    3      140.714 ±      8.272  ops/min
ThresholdHouseholderLeft.tuneRaw         1024    4  thrpt    3      207.568 ±     55.258  ops/min
ThresholdHouseholderLeft.tuneRaw         2048    1  thrpt    3        9.892 ±      1.900  ops/min
ThresholdHouseholderLeft.tuneRaw         2048    2  thrpt    3       13.725 ±      1.733  ops/min
ThresholdHouseholderLeft.tuneRaw         2048    4  thrpt    3       17.166 ±      2.694  ops/min
 * </pre>
 *
 * MacBook Pro (16-inch, 2019): 2022-01-13 => 64
 *
 * <pre>
Benchmark                      (dim)  (z)   Mode  Cnt        Score        Error    Units
ThresholdHouseholderLeft.tune     32    1  thrpt    3  1428582.331 ± 373831.670  ops/min
ThresholdHouseholderLeft.tune     32    2  thrpt    3   339301.030 ±  58073.646  ops/min
ThresholdHouseholderLeft.tune     32    4  thrpt    3   142777.495 ±  44775.975  ops/min
ThresholdHouseholderLeft.tune     64    1  thrpt    3   210555.060 ±  30434.438  ops/min
ThresholdHouseholderLeft.tune     64    2  thrpt    3   119029.857 ±  22882.677  ops/min
ThresholdHouseholderLeft.tune     64    4  thrpt    3    66061.076 ±   2155.043  ops/min
ThresholdHouseholderLeft.tune    128    1  thrpt    3    30443.908 ±   3309.796  ops/min
ThresholdHouseholderLeft.tune    128    2  thrpt    3    31494.788 ±   3025.836  ops/min
ThresholdHouseholderLeft.tune    128    4  thrpt    3    24290.269 ±   5805.974  ops/min
ThresholdHouseholderLeft.tune    256    1  thrpt    3     4065.628 ±    953.317  ops/min
ThresholdHouseholderLeft.tune    256    2  thrpt    3     5694.790 ±   1339.554  ops/min
ThresholdHouseholderLeft.tune    256    4  thrpt    3     6260.057 ±   1545.870  ops/min
ThresholdHouseholderLeft.tune    512    1  thrpt    3      529.087 ±     36.135  ops/min
ThresholdHouseholderLeft.tune    512    2  thrpt    3      789.680 ±     32.697  ops/min
ThresholdHouseholderLeft.tune    512    4  thrpt    3     1206.603 ±     46.300  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdHouseholderLeft extends ThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdTuner.options(), ThresholdHouseholderLeft.class);
    }

    @Param({ "32", "64", "128", "256", "512", "1024" })
    public int dim;

    PrimitiveOrRawQR.CodeAndData benchmark;
    QR<Double> decomposition;
    QR<Double> primitive;
    QR<Double> raw;

    @Override
    @Setup
    public void setup() {

        HouseholderLeft.THRESHOLD = dim / z;

        benchmark = new PrimitiveOrRawQR.CodeAndData(dim);
        decomposition = PrimitiveOrRawQR.CodeAndData.newInstance(dim);
        primitive = PrimitiveOrRawQR.CodeAndData.newPrimitive();
        raw = PrimitiveOrRawQR.CodeAndData.newRaw();
    }

    @Override
    @Benchmark
    public Object tune() {
        return benchmark.execute(decomposition);
    }

    @Benchmark
    public Object tunePrimitive() {
        return benchmark.execute(primitive);
    }

    @Benchmark
    public Object tuneRaw() {
        return benchmark.execute(raw);
    }

}
