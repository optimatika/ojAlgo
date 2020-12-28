/*
 * Copyright 1997-2020 Optimatika
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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.array.operation.FillMatchingSingle;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro. 2018-02-27 => 256
 *
 * <pre>
# Run complete. Total time: 00:07:02

Benchmark                              (dim)  (z)   Mode  Cnt         Score        Error    Units
ThresholdFillMatchingSingle.scale         64    1  thrpt    5  27532147.699 ± 255291.937  ops/min
ThresholdFillMatchingSingle.scale         64    2  thrpt    5   1081815.725 ±   7258.474  ops/min
ThresholdFillMatchingSingle.scale        128    1  thrpt    5   4961463.150 ± 225250.200  ops/min
ThresholdFillMatchingSingle.scale        128    2  thrpt    5   1022582.571 ±  10030.762  ops/min
ThresholdFillMatchingSingle.scale        256    1  thrpt    5   1081973.237 ±   6552.974  ops/min
ThresholdFillMatchingSingle.scale        256    2  thrpt    5    746357.032 ±   9702.760  ops/min
ThresholdFillMatchingSingle.scale        512    1  thrpt    5    255185.958 ±   3674.086  ops/min
ThresholdFillMatchingSingle.scale        512    2  thrpt    5    353670.831 ±  13752.074  ops/min
ThresholdFillMatchingSingle.scale       1024    1  thrpt    5     27875.710 ±   4226.076  ops/min
ThresholdFillMatchingSingle.scale       1024    2  thrpt    5     56361.768 ±   2358.219  ops/min
ThresholdFillMatchingSingle.scale       2048    1  thrpt    5      6439.711 ±    143.318  ops/min
ThresholdFillMatchingSingle.scale       2048    2  thrpt    5     13748.618 ±     78.911  ops/min
ThresholdFillMatchingSingle.scale       4096    1  thrpt    5      1605.287 ±     49.253  ops/min
ThresholdFillMatchingSingle.scale       4096    2  thrpt    5      3237.000 ±    164.260  ops/min
ThresholdFillMatchingSingle.transpose     64    1  thrpt    5   7589557.234 ±  16111.865  ops/min
ThresholdFillMatchingSingle.transpose     64    2  thrpt    5   2143737.283 ±  69819.004  ops/min
ThresholdFillMatchingSingle.transpose    128    1  thrpt    5   1722099.022 ±   4258.504  ops/min
ThresholdFillMatchingSingle.transpose    128    2  thrpt    5   1222498.416 ±  40806.933  ops/min
ThresholdFillMatchingSingle.transpose    256    1  thrpt    5    293766.401 ±   5940.355  ops/min
ThresholdFillMatchingSingle.transpose    256    2  thrpt    5    410123.155 ±   5710.892  ops/min
ThresholdFillMatchingSingle.transpose    512    1  thrpt    5     68976.109 ±    895.890  ops/min
ThresholdFillMatchingSingle.transpose    512    2  thrpt    5    118447.471 ±   1268.603  ops/min
ThresholdFillMatchingSingle.transpose   1024    1  thrpt    5      5515.819 ±     16.226  ops/min
ThresholdFillMatchingSingle.transpose   1024    2  thrpt    5     10289.166 ±    638.056  ops/min
ThresholdFillMatchingSingle.transpose   2048    1  thrpt    5      1025.087 ±      7.510  ops/min
ThresholdFillMatchingSingle.transpose   2048    2  thrpt    5      1835.658 ±    134.385  ops/min
ThresholdFillMatchingSingle.transpose   4096    1  thrpt    5       207.640 ±      0.581  ops/min
ThresholdFillMatchingSingle.transpose   4096    2  thrpt    5       399.284 ±     17.831  ops/min
 * </pre>
 *
 * MacBook Air. 2015-06-26 => 512
 *
 * <pre>
# Run complete. Total time: 00:01:26

Benchmark                       (dim)  (z)   Mode  Cnt        Score         Error    Units
ThresholdFillMatchingLeft.tune    128    1  thrpt    3  7992286,830 ± 3317290,978  ops/min
ThresholdFillMatchingLeft.tune    128    2  thrpt    3  1036970,177 ±  120596,694  ops/min
ThresholdFillMatchingLeft.tune    256    1  thrpt    3  1330731,871 ±  249897,655  ops/min
ThresholdFillMatchingLeft.tune    256    2  thrpt    3   763934,514 ±  233037,987  ops/min
ThresholdFillMatchingLeft.tune    512    1  thrpt    3   160204,493 ±   48410,164  ops/min
ThresholdFillMatchingLeft.tune    512    2  thrpt    3   144239,895 ±   30524,578  ops/min
ThresholdFillMatchingLeft.tune   1024    1  thrpt    3    33640,979 ±    2456,084  ops/min
ThresholdFillMatchingLeft.tune   1024    2  thrpt    3    34657,438 ±    1534,592  ops/min
ThresholdFillMatchingLeft.tune   2048    1  thrpt    3     8430,484 ±    3957,855  ops/min
ThresholdFillMatchingLeft.tune   2048    2  thrpt    3     9055,332 ±    1506,639  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdFillMatchingSingle extends AbstractThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdFillMatchingSingle.class);
    }

    @Param({ "64", "128", "256", "512", "1024", "2048", "4096" })
    public int dim;

    @Param({ "1", "2" })
    public int z;

    Primitive64Store original;
    Primitive64Store target;
    double scalar;

    @Benchmark
    public Object scale() {
        target.fillMatching(PrimitiveFunction.MULTIPLY.first(scalar), original);
        return target;
    }

    @Override
    @Setup
    public void setup() {

        FillMatchingSingle.THRESHOLD = dim / z;

        final Uniform tmpSupplier = new Uniform();

        scalar = tmpSupplier.doubleValue();

        original = Primitive64Store.FACTORY.makeFilled(dim, dim, tmpSupplier);
        target = Primitive64Store.FACTORY.makeZero(dim, dim);
    }

    @Benchmark
    public Object transpose() {
        target.fillMatching(original.transpose());
        return target;
    }

    @Override
    public Object tune() {
        return null;
    };

}
