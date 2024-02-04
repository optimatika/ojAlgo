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
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Normal;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * <pre>
# Run complete. Total time: 00:14:21

Benchmark                    (complexity)   Mode  Cnt         Score        Error  Units
MultiplyLeftRight.invokeCIJ             2  thrpt    5  21056125.854 ±  20370.049  ops/s
MultiplyLeftRight.invokeCIJ             4  thrpt    5   5407028.640 ±  90499.069  ops/s
MultiplyLeftRight.invokeCIJ             8  thrpt    5    747775.755 ±   5403.970  ops/s
MultiplyLeftRight.invokeCIJ            16  thrpt    5     98068.834 ±    298.179  ops/s
MultiplyLeftRight.invokeCIJ            32  thrpt    5     11935.340 ±   1470.660  ops/s
MultiplyLeftRight.invokeCIJ            64  thrpt    5      1402.634 ±      1.552  ops/s
MultiplyLeftRight.invokeCIJ           128  thrpt    5       111.040 ±      0.392  ops/s
MultiplyLeftRight.invokeCIJ           256  thrpt    5         9.118 ±      0.016  ops/s
MultiplyLeftRight.invokeCIJ           512  thrpt    5         0.370 ±      0.001  ops/s
MultiplyLeftRight.invokeCJI             2  thrpt    5  18295936.755 ±  36830.902  ops/s
MultiplyLeftRight.invokeCJI             4  thrpt    5   4786651.447 ±  12806.289  ops/s
MultiplyLeftRight.invokeCJI             8  thrpt    5    915611.946 ±   9682.285  ops/s
MultiplyLeftRight.invokeCJI            16  thrpt    5    148501.308 ±    840.925  ops/s
MultiplyLeftRight.invokeCJI            32  thrpt    5     20811.069 ±     75.881  ops/s
MultiplyLeftRight.invokeCJI            64  thrpt    5      2805.549 ±     23.012  ops/s
MultiplyLeftRight.invokeCJI           128  thrpt    5       338.683 ±      0.905  ops/s
MultiplyLeftRight.invokeCJI           256  thrpt    5        40.806 ±      0.039  ops/s
MultiplyLeftRight.invokeCJI           512  thrpt    5         5.012 ±      0.018  ops/s
MultiplyLeftRight.invokeICJ             2  thrpt    5  20288767.056 ±  76250.514  ops/s
MultiplyLeftRight.invokeICJ             4  thrpt    5   4684541.690 ± 358504.476  ops/s
MultiplyLeftRight.invokeICJ             8  thrpt    5    701274.579 ±   1088.738  ops/s
MultiplyLeftRight.invokeICJ            16  thrpt    5     92131.835 ±    501.072  ops/s
MultiplyLeftRight.invokeICJ            32  thrpt    5     11485.825 ±     66.947  ops/s
MultiplyLeftRight.invokeICJ            64  thrpt    5      1348.113 ±      6.727  ops/s
MultiplyLeftRight.invokeICJ           128  thrpt    5        99.544 ±      0.450  ops/s
MultiplyLeftRight.invokeICJ           256  thrpt    5         9.038 ±      0.058  ops/s
MultiplyLeftRight.invokeICJ           512  thrpt    5         0.352 ±      0.005  ops/s
MultiplyLeftRight.invokeIJC             2  thrpt    5  20782354.673 ± 134759.283  ops/s
MultiplyLeftRight.invokeIJC             4  thrpt    5   4853658.807 ±  26693.144  ops/s
MultiplyLeftRight.invokeIJC             8  thrpt    5    831410.861 ±  82148.726  ops/s
MultiplyLeftRight.invokeIJC            16  thrpt    5    118904.169 ±    191.124  ops/s
MultiplyLeftRight.invokeIJC            32  thrpt    5     15741.425 ±     58.990  ops/s
MultiplyLeftRight.invokeIJC            64  thrpt    5      1941.779 ±      3.213  ops/s
MultiplyLeftRight.invokeIJC           128  thrpt    5       210.169 ±      1.066  ops/s
MultiplyLeftRight.invokeIJC           256  thrpt    5        16.835 ±      0.055  ops/s
MultiplyLeftRight.invokeIJC           512  thrpt    5         1.967 ±      0.007  ops/s
MultiplyLeftRight.invokeJCI             2  thrpt    5  18581413.704 ±  32673.736  ops/s
MultiplyLeftRight.invokeJCI             4  thrpt    5   5155519.711 ±  12201.333  ops/s
MultiplyLeftRight.invokeJCI             8  thrpt    5    971354.052 ±  11093.597  ops/s
MultiplyLeftRight.invokeJCI            16  thrpt    5    154076.958 ±   1450.814  ops/s
MultiplyLeftRight.invokeJCI            32  thrpt    5     20863.085 ±     65.136  ops/s
MultiplyLeftRight.invokeJCI            64  thrpt    5      2890.490 ±      7.372  ops/s
MultiplyLeftRight.invokeJCI           128  thrpt    5       347.419 ±      0.595  ops/s
MultiplyLeftRight.invokeJCI           256  thrpt    5        43.653 ±      0.120  ops/s
MultiplyLeftRight.invokeJCI           512  thrpt    5         5.523 ±      0.038  ops/s
MultiplyLeftRight.invokeJIC             2  thrpt    5  21403540.415 ±  79578.573  ops/s
MultiplyLeftRight.invokeJIC             4  thrpt    5   4771148.458 ±   7415.554  ops/s
MultiplyLeftRight.invokeJIC             8  thrpt    5    864885.050 ± 114305.418  ops/s
MultiplyLeftRight.invokeJIC            16  thrpt    5    130894.511 ±    252.545  ops/s
MultiplyLeftRight.invokeJIC            32  thrpt    5     16641.781 ±     51.022  ops/s
MultiplyLeftRight.invokeJIC            64  thrpt    5      2109.033 ±      7.236  ops/s
MultiplyLeftRight.invokeJIC           128  thrpt    5       227.927 ±      0.226  ops/s
MultiplyLeftRight.invokeJIC           256  thrpt    5        17.652 ±      0.037  ops/s
MultiplyLeftRight.invokeJIC           512  thrpt    5         2.093 ±      0.017  ops/s
 * </pre>
 *
 * <pre>
# Run complete. Total time: 00:18:57

Benchmark                             (complexity)   Mode  Cnt         Score        Error  Units
MultiplyLeftRight.invokeNeitherMaybe             2  thrpt    7  20375281,540 ±  42461,680  ops/s
MultiplyLeftRight.invokeNeitherMaybe             4  thrpt    7   5027033,478 ±   7302,907  ops/s
MultiplyLeftRight.invokeNeitherMaybe             8  thrpt    7    960901,769 ±   8737,255  ops/s
MultiplyLeftRight.invokeNeitherMaybe            16  thrpt    7    155271,403 ±    230,297  ops/s
MultiplyLeftRight.invokeNeitherMaybe            32  thrpt    7     21926,140 ±     69,487  ops/s
MultiplyLeftRight.invokeNeitherMaybe            64  thrpt    7      2866,150 ±      4,942  ops/s
MultiplyLeftRight.invokeNeitherMaybe           128  thrpt    7       370,036 ±      1,453  ops/s
MultiplyLeftRight.invokeNeitherMaybe           256  thrpt    7        45,928 ±      0,093  ops/s
MultiplyLeftRight.invokeNeitherMaybe           512  thrpt    7         5,657 ±      0,029  ops/s
MultiplyLeftRight.invokeNeitherMaybe          1024  thrpt    7         0,636 ±      0,005  ops/s
MultiplyLeftRight.invokeNeitherMaybe          2048  thrpt    7         0,073 ±      0,001  ops/s
MultiplyLeftRight.invokeNeitherNow               2  thrpt    7  15511005,733 ± 346365,718  ops/s
MultiplyLeftRight.invokeNeitherNow               4  thrpt    7   4887105,906 ±  10721,266  ops/s
MultiplyLeftRight.invokeNeitherNow               8  thrpt    7    991360,097 ±  22797,356  ops/s
MultiplyLeftRight.invokeNeitherNow              16  thrpt    7    187765,133 ±   3854,363  ops/s
MultiplyLeftRight.invokeNeitherNow              32  thrpt    7     27290,580 ±    183,830  ops/s
MultiplyLeftRight.invokeNeitherNow              64  thrpt    7      3407,571 ±     87,240  ops/s
MultiplyLeftRight.invokeNeitherNow             128  thrpt    7       406,031 ±      0,696  ops/s
MultiplyLeftRight.invokeNeitherNow             256  thrpt    7        50,260 ±      0,066  ops/s
MultiplyLeftRight.invokeNeitherNow             512  thrpt    7         6,033 ±      0,035  ops/s
MultiplyLeftRight.invokeNeitherNow            1024  thrpt    7         0,667 ±      0,004  ops/s
MultiplyLeftRight.invokeNeitherNow            2048  thrpt    7         0,078 ±      0,001  ops/s
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class MultiplyLeftRight {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(MultiplyLeftRight.class);
    }

    static void invokeCIJ(final double[] product, final double[] left, final int complexity, final double[] right) {

        int numbRows = left.length / complexity;
        int numbCols = right.length / complexity;

        for (int c = 0; c < complexity; c++) {
            for (int i = 0; i < numbRows; i++) {
                for (int j = 0; j < numbCols; j++) {
                    product[i + j * numbRows] += left[i + c * numbRows] * right[c + j * complexity];
                }
            }
        }
    }

    static void invokeCJI(final double[] product, final double[] left, final int complexity, final double[] right) {

        int numbRows = left.length / complexity;
        int numbCols = right.length / complexity;

        for (int c = 0; c < complexity; c++) {
            for (int j = 0; j < numbCols; j++) {
                for (int i = 0; i < numbRows; i++) {
                    product[i + j * numbRows] += left[i + c * numbRows] * right[c + j * complexity];
                }
            }
        }
    }

    static void invokeICJ(final double[] product, final double[] left, final int complexity, final double[] right) {

        int numbRows = left.length / complexity;
        int numbCols = right.length / complexity;

        for (int i = 0; i < numbRows; i++) {
            for (int c = 0; c < complexity; c++) {
                for (int j = 0; j < numbCols; j++) {
                    product[i + j * numbRows] += left[i + c * numbRows] * right[c + j * complexity];
                }
            }
        }
    }

    static void invokeIJC(final double[] product, final double[] left, final int complexity, final double[] right) {

        int numbRows = left.length / complexity;
        int numbCols = right.length / complexity;

        for (int i = 0; i < numbRows; i++) {
            for (int j = 0; j < numbCols; j++) {
                for (int c = 0; c < complexity; c++) {
                    product[i + j * numbRows] += left[i + c * numbRows] * right[c + j * complexity];
                }
            }
        }
    }

    static void invokeJCI(final double[] product, final double[] left, final int complexity, final double[] right) {

        int numbRows = left.length / complexity;
        int numbCols = right.length / complexity;

        for (int j = 0; j < numbCols; j++) {
            for (int c = 0; c < complexity; c++) {
                for (int i = 0; i < numbRows; i++) {
                    product[i + j * numbRows] += left[i + c * numbRows] * right[c + j * complexity];
                }
            }
        }
    }

    static void invokeJIC(final double[] product, final double[] left, final int complexity, final double[] right) {

        int numbRows = left.length / complexity;
        int numbCols = right.length / complexity;

        for (int j = 0; j < numbCols; j++) {
            for (int i = 0; i < numbRows; i++) {
                for (int c = 0; c < complexity; c++) {
                    product[i + j * numbRows] += left[i + c * numbRows] * right[c + j * complexity];
                }
            }
        }
    }

    @Param({ "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048" })
    public int complexity;

    public Primitive64Store left;
    public Primitive64Store product;
    public Primitive64Store right;

    @Benchmark
    public Primitive64Store invokeCIJ() {
        MultiplyLeftRight.invokeCIJ(product.data, left.data, complexity, right.data);
        return product;
    }

    @Benchmark
    public Primitive64Store invokeCJI() {
        MultiplyLeftRight.invokeCJI(product.data, left.data, complexity, right.data);
        return product;
    }

    @Benchmark
    public Primitive64Store invokeICJ() {
        MultiplyLeftRight.invokeICJ(product.data, left.data, complexity, right.data);
        return product;
    }

    @Benchmark
    public Primitive64Store invokeIJC() {
        MultiplyLeftRight.invokeIJC(product.data, left.data, complexity, right.data);
        return product;
    }

    @Benchmark
    public Primitive64Store invokeJCI() {
        MultiplyLeftRight.invokeJCI(product.data, left.data, complexity, right.data);
        return product;
    }

    @Benchmark
    public Primitive64Store invokeJIC() {
        MultiplyLeftRight.invokeJIC(product.data, left.data, complexity, right.data);
        return product;
    }

    @Setup
    public void setup() {
        left = Primitive64Store.FACTORY.makeFilled(complexity, complexity, Normal.standard());
        right = Primitive64Store.FACTORY.makeFilled(complexity, complexity, Normal.standard());
        product = Primitive64Store.FACTORY.make(complexity, complexity);

    }
}
