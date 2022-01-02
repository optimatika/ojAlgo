/*
 * Copyright 1997-2022 Optimatika
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
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro 2015-06-24 => 256
 *
 * <pre>
 * # Run complete. Total time: 00:02:08
 *
 * Benchmark                   (dim)   Mode  Cnt        Score        Error    Units
 * PrimitiveOrRawQR.primitive     10  thrpt    3  7005064,903 ± 529385,425  ops/min
 * PrimitiveOrRawQR.primitive     20  thrpt    3  1506248,641 ± 386383,563  ops/min
 * PrimitiveOrRawQR.primitive     50  thrpt    3   155327,525 ±  17646,625  ops/min
 * PrimitiveOrRawQR.primitive    100  thrpt    3    23010,318 ±   2628,191  ops/min
 * PrimitiveOrRawQR.primitive    200  thrpt    3     3878,911 ±    228,663  ops/min
 * PrimitiveOrRawQR.primitive    500  thrpt    3      469,588 ±     48,837  ops/min
 * PrimitiveOrRawQR.primitive   1000  thrpt    3       99,547 ±     34,182  ops/min
 * PrimitiveOrRawQR.raw           10  thrpt    3  8179336,175 ± 106054,067  ops/min
 * PrimitiveOrRawQR.raw           20  thrpt    3  1715730,663 ±  52624,893  ops/min
 * PrimitiveOrRawQR.raw           50  thrpt    3   192468,663 ±  17281,973  ops/min
 * PrimitiveOrRawQR.raw          100  thrpt    3    31189,874 ±    876,228  ops/min
 * PrimitiveOrRawQR.raw          200  thrpt    3     4518,101 ±     38,338  ops/min
 * PrimitiveOrRawQR.raw          500  thrpt    3      323,669 ±      3,898  ops/min
 * PrimitiveOrRawQR.raw         1000  thrpt    3       35,908 ±      2,514  ops/min
 * </pre>
 *
 * MacBook Air: 2015-05-28
 *
 * <pre>
 * Result: 0,065 ±(99.9%) 0,000 ops/s [Average]
 *   Statistics: (min, avg, max) = (0,064, 0,065, 0,066), stdev = 0,000
 *   Confidence interval (99.9%): [0,065, 0,066]
 *
 *
 * # Run complete. Total time: 04:20:53
 *
 * Benchmark                   (dim)   Mode  Cnt        Score       Error  Units
 * PrimitiveOrRawQR.primitive      2  thrpt  200  2078858,455 ± 14771,019  ops/s
 * PrimitiveOrRawQR.raw            2  thrpt  200  2820820,335 ± 11903,626  ops/s
 * PrimitiveOrRawQR.primitive     20  thrpt  200    27346,494 ±   137,346  ops/s
 * PrimitiveOrRawQR.raw           20  thrpt  200    28785,751 ±    66,570  ops/s
 * PrimitiveOrRawQR.primitive    200  thrpt  200       58,159 ±     0,062  ops/s
 * PrimitiveOrRawQR.raw          200  thrpt  200       75,317 ±     0,072  ops/s
 * PrimitiveOrRawQR.primitive   2000  thrpt  200        0,056 ±     0,000  ops/s
 * PrimitiveOrRawQR.raw         2000  thrpt  200        0,065 ±     0,000  ops/s
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class PrimitiveOrRawQR extends AbstractPrimitiveOrRaw<QR<Double>> {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(PrimitiveOrRawQR.class);
    }

    @Param({ "10", "20", "50", "100", "200", "500", "1000" })
    public int dim;

    MatrixStore<Double> body;
    PhysicalStore<Double> preallocated;
    MatrixStore<Double> rhs;

    @Override
    @Benchmark
    public MatrixStore<Double> primitive() {
        primitive.compute(body);
        return primitive.getSolution(rhs, preallocated);
    }

    @Override
    @Benchmark
    public MatrixStore<Double> raw() {
        raw.compute(body);
        return raw.getSolution(rhs, preallocated);
    }

    @Override
    @Setup
    public void setup() {

        super.setup();
        final int dim1 = dim;

        // body = MatrixUtils.makeSPD(dim).builder().below(new IdentityStore<>(PrimitiveDenseStore.FACTORY, dim)).build();
        body = Primitive64Store.FACTORY.makeSPD(dim1).below(Primitive64Store.FACTORY.makeIdentity(dim));
        rhs = Primitive64Store.FACTORY.makeFilled(dim + dim, 1, new Uniform());

        preallocated = primitive.preallocate(body, rhs);
    }

    @Override
    protected QR<Double> makePrimitive() {
        return new QRDecomposition.Primitive();
    }

    @Override
    protected QR<Double> makeRaw() {
        return new RawQR();
    }
}
