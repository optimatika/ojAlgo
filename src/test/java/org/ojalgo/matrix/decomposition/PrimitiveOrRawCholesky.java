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
 * Mac Pro: 2015-06-24 => 32
 *
 * <pre>
 * # Run complete. Total time: 00:02:01
 *
 * Benchmark                         (dim)   Mode  Cnt         Score         Error    Units
 * PrimitiveOrRawCholesky.primitive     10  thrpt    3  20887723,803 ±  422236,323  ops/min
 * PrimitiveOrRawCholesky.primitive     20  thrpt    3   4631781,508 ±   92060,389  ops/min
 * PrimitiveOrRawCholesky.primitive     50  thrpt    3    634024,689 ±   40277,396  ops/min
 * PrimitiveOrRawCholesky.primitive    100  thrpt    3     99042,111 ±   18081,213  ops/min
 * PrimitiveOrRawCholesky.primitive    200  thrpt    3     34986,925 ±    4212,244  ops/min
 * PrimitiveOrRawCholesky.primitive    500  thrpt    3      3932,736 ±     222,647  ops/min
 * PrimitiveOrRawCholesky.primitive   1000  thrpt    3       638,863 ±     226,068  ops/min
 * PrimitiveOrRawCholesky.raw           10  thrpt    3  25869022,210 ± 1567602,259  ops/min
 * PrimitiveOrRawCholesky.raw           20  thrpt    3   5034905,993 ±  138446,657  ops/min
 * PrimitiveOrRawCholesky.raw           50  thrpt    3    569926,004 ±   11244,616  ops/min
 * PrimitiveOrRawCholesky.raw          100  thrpt    3     90931,407 ±   11988,216  ops/min
 * PrimitiveOrRawCholesky.raw          200  thrpt    3     20947,526 ±     341,215  ops/min
 * PrimitiveOrRawCholesky.raw          500  thrpt    3      1568,520 ±     171,935  ops/min
 * PrimitiveOrRawCholesky.raw         1000  thrpt    3       198,236 ±      19,299  ops/min
 * </pre>
 *
 * MacBook Air: 2015-06-24
 *
 * <pre>
 * # Run complete. Total time: 00:02:03
 *
 * Benchmark                         (dim)   Mode  Cnt         Score         Error    Units
 * PrimitiveOrRawCholesky.primitive     10  thrpt    3  24763687,617 ± 8694981,041  ops/min
 * PrimitiveOrRawCholesky.primitive     20  thrpt    3   5514557,488 ±  674815,391  ops/min
 * PrimitiveOrRawCholesky.primitive     50  thrpt    3    704929,484 ±  143388,270  ops/min
 * PrimitiveOrRawCholesky.primitive    100  thrpt    3    106341,820 ±   39011,514  ops/min
 * PrimitiveOrRawCholesky.primitive    200  thrpt    3     22579,441 ±     801,931  ops/min
 * PrimitiveOrRawCholesky.primitive    500  thrpt    3      1508,195 ±    1434,132  ops/min
 * PrimitiveOrRawCholesky.primitive   1000  thrpt    3       185,326 ±     181,128  ops/min
 * PrimitiveOrRawCholesky.raw           10  thrpt    3  31172118,622 ± 8259001,036  ops/min
 * PrimitiveOrRawCholesky.raw           20  thrpt    3   7412210,436 ± 2378759,396  ops/min
 * PrimitiveOrRawCholesky.raw           50  thrpt    3    647425,035 ± 1097147,206  ops/min
 * PrimitiveOrRawCholesky.raw          100  thrpt    3     91538,565 ±    3584,241  ops/min
 * PrimitiveOrRawCholesky.raw          200  thrpt    3     16085,473 ±    1542,892  ops/min
 * PrimitiveOrRawCholesky.raw          500  thrpt    3       936,441 ±     296,879  ops/min
 * PrimitiveOrRawCholesky.raw         1000  thrpt    3       116,876 ±      32,892  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class PrimitiveOrRawCholesky extends AbstractPrimitiveOrRaw<Cholesky<Double>> {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(PrimitiveOrRawCholesky.class);
    }

    @Param({ "10", "20", "50", "100", "200", "500", "1000" })
    public int dim;

    Primitive64Store matrix;
    Primitive64Store preallocated;

    @Override
    @Benchmark
    public MatrixStore<Double> primitive() {
        primitive.compute(matrix);
        return primitive.getInverse(preallocated);
    }

    @Override
    @Benchmark
    public MatrixStore<Double> raw() {
        raw.compute(matrix);
        return raw.getInverse(preallocated);
    }

    @Override
    @Setup
    public void setup() {

        super.setup();
        final int dim1 = dim;

        matrix = Primitive64Store.FACTORY.makeSPD(dim1);

        preallocated = Primitive64Store.FACTORY.make(dim, dim);
    }

    @Override
    protected Cholesky<Double> makePrimitive() {
        return new CholeskyDecomposition.R064();
    }

    @Override
    protected Cholesky<Double> makeRaw() {
        return new RawCholesky();
    }

}
