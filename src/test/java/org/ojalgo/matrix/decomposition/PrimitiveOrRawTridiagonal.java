/*
 * Copyright 1997-2021 Optimatika
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
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro: 2017-03-15 (tred2j)
 *
 * <pre>
# Run complete. Total time: 00:09:05

Benchmark                            (dim)   Mode  Cnt        Score       Error  Units
PrimitiveOrRawTridiagonal.primitive      5  thrpt   15   853271.228 ±  4072.026  ops/s
PrimitiveOrRawTridiagonal.primitive     10  thrpt   15   199540.746 ±  2488.442  ops/s
PrimitiveOrRawTridiagonal.primitive     20  thrpt   15    36362.780 ±   350.417  ops/s
PrimitiveOrRawTridiagonal.primitive     50  thrpt   15     3428.091 ±    15.493  ops/s
PrimitiveOrRawTridiagonal.primitive    100  thrpt   15      522.300 ±     2.906  ops/s
PrimitiveOrRawTridiagonal.primitive    200  thrpt   15       69.933 ±     0.351  ops/s
PrimitiveOrRawTridiagonal.primitive    500  thrpt   15        5.537 ±     0.042  ops/s
PrimitiveOrRawTridiagonal.raw            5  thrpt   15  1269212.994 ± 13237.750  ops/s
PrimitiveOrRawTridiagonal.raw           10  thrpt   15   262246.453 ±  5806.558  ops/s
PrimitiveOrRawTridiagonal.raw           20  thrpt   15    48977.285 ±   154.403  ops/s
PrimitiveOrRawTridiagonal.raw           50  thrpt   15     4012.536 ±    31.228  ops/s
PrimitiveOrRawTridiagonal.raw          100  thrpt   15      574.762 ±     3.224  ops/s
PrimitiveOrRawTridiagonal.raw          200  thrpt   15       76.904 ±     0.279  ops/s
PrimitiveOrRawTridiagonal.raw          500  thrpt   15        5.128 ±     0.099  ops/s
 * </pre>
 *
 * Mac Pro: 2017-03-15 (tred2nr)
 *
 * <pre>
# Run complete. Total time: 00:12:56

Benchmark                            (dim)   Mode  Cnt      Score     Error  Units
PrimitiveOrRawTridiagonal.primitive     20  thrpt   15  35261.569 ± 236.887  ops/s
PrimitiveOrRawTridiagonal.primitive     50  thrpt   15   3402.721 ±  12.891  ops/s
PrimitiveOrRawTridiagonal.primitive    100  thrpt   15    522.202 ±   1.380  ops/s
PrimitiveOrRawTridiagonal.primitive    200  thrpt   15     69.721 ±   0.264  ops/s
PrimitiveOrRawTridiagonal.primitive    500  thrpt   15      5.527 ±   0.039  ops/s
PrimitiveOrRawTridiagonal.primitive   1000  thrpt   15      1.084 ±   0.008  ops/s
PrimitiveOrRawTridiagonal.raw           20  thrpt   15  42367.793 ± 295.795  ops/s
PrimitiveOrRawTridiagonal.raw           50  thrpt   15   3028.783 ±  25.172  ops/s
PrimitiveOrRawTridiagonal.raw          100  thrpt   15    423.221 ±   1.395  ops/s
PrimitiveOrRawTridiagonal.raw          200  thrpt   15     55.150 ±   0.148  ops/s
PrimitiveOrRawTridiagonal.raw          500  thrpt   15      2.749 ±   0.059  ops/s
PrimitiveOrRawTridiagonal.raw         1000  thrpt   15      0.114 ±   0.001  ops/s
 * </pre>
 *
 * MacBook Air: 2015-06-15 (tred2j)
 *
 * <pre>
 * # Run complete. Total time: 01:41:02
 *
 * Benchmark                            (dim)   Mode  Cnt         Score         Error    Units
 * PrimitiveOrRawTridiagonal.primitive      5  thrpt    5  41513388,484 ±  161585,356  ops/min
 * PrimitiveOrRawTridiagonal.primitive     10  thrpt    5   9575679,814 ±   38523,945  ops/min
 * PrimitiveOrRawTridiagonal.primitive     20  thrpt    5   2046315,904 ±    4805,435  ops/min
 * PrimitiveOrRawTridiagonal.primitive     50  thrpt    5    191141,919 ±     716,270  ops/min
 * PrimitiveOrRawTridiagonal.primitive    100  thrpt    5     14765,199 ±     141,631  ops/min
 * PrimitiveOrRawTridiagonal.primitive    200  thrpt    5      1587,269 ±      16,126  ops/min
 * PrimitiveOrRawTridiagonal.primitive    500  thrpt    5       216,861 ±       2,546  ops/min
 * PrimitiveOrRawTridiagonal.primitive   1000  thrpt    5        58,657 ±       1,366  ops/min
 * PrimitiveOrRawTridiagonal.primitive   2000  thrpt    5        10,502 ±       0,112  ops/min
 * PrimitiveOrRawTridiagonal.primitive   5000  thrpt    5         0,661 ±       0,019  ops/min
 * PrimitiveOrRawTridiagonal.raw            5  thrpt    5  74010865,197 ± 2226096,098  ops/min
 * PrimitiveOrRawTridiagonal.raw           10  thrpt    5  15984293,810 ±  241398,540  ops/min
 * PrimitiveOrRawTridiagonal.raw           20  thrpt    5   2973952,467 ±   82688,852  ops/min
 * PrimitiveOrRawTridiagonal.raw           50  thrpt    5    261199,056 ±    6085,220  ops/min
 * PrimitiveOrRawTridiagonal.raw          100  thrpt    5     34213,459 ±     128,789  ops/min
 * PrimitiveOrRawTridiagonal.raw          200  thrpt    5      4660,978 ±      82,333  ops/min
 * PrimitiveOrRawTridiagonal.raw          500  thrpt    5       331,257 ±       2,215  ops/min
 * PrimitiveOrRawTridiagonal.raw         1000  thrpt    5        43,110 ±       0,954  ops/min
 * PrimitiveOrRawTridiagonal.raw         2000  thrpt    5         4,710 ±       0,031  ops/min
 * PrimitiveOrRawTridiagonal.raw         5000  thrpt    5         0,292 ±       0,002  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class PrimitiveOrRawTridiagonal extends AbstractPrimitiveOrRaw<Tridiagonal<Double>> {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(PrimitiveOrRawTridiagonal.class);
    }

    @Param({ "100", "200", "500", "1000" })
    public int dim;

    MatrixStore<Double> matrix;

    @Override
    @Benchmark
    public MatrixStore<Double> primitive() {
        primitive.decompose(matrix);
        return primitive.getQ();
    }

    @Override
    @Benchmark
    public MatrixStore<Double> raw() {
        raw.decompose(matrix);
        return raw.getQ();
    }

    @Override
    @Setup
    public void setup() {

        super.setup();
        final int dim1 = dim;

        matrix = Primitive64Store.FACTORY.makeSPD(dim1);

        //        primitive.decompose(matrix);
        //        final MatrixStore<Double> pQ = primitive.getQ();
        //
        //        raw.decompose(matrix);
        //        final MatrixStore<Double> rQ = raw.getQ();
        //
        //        BasicLogger.debug("Primitive", primitive.getD());
        //        BasicLogger.debug("Raw", raw.getD());

    }

    @Override
    protected Tridiagonal<Double> makePrimitive() {
        return new DeferredTridiagonal.Primitive();
    }

    @Override
    protected Tridiagonal<Double> makeRaw() {
        return new SimultaneousTridiagonal();
    }

}
