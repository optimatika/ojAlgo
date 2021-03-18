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
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro: 2017-03-20
 *
 * <pre>
# Run complete. Total time: 00:14:36

Benchmark                              (dim)   Mode  Cnt      Score      Error  Units
PrimitiveOrRawEigenvalue.primitive        20  thrpt    3   9155.920 ± 5070.871  ops/s
PrimitiveOrRawEigenvalue.raw              20  thrpt    3  20428.726 ± 1962.925  ops/s
PrimitiveOrRawEigenvalue.simultaneous     20  thrpt    3  16106.847 ± 1480.838  ops/s
PrimitiveOrRawEigenvalue.primitive        50  thrpt    3    671.606 ± 4389.406  ops/s
PrimitiveOrRawEigenvalue.raw              50  thrpt    3   2312.351 ±  210.690  ops/s
PrimitiveOrRawEigenvalue.simultaneous     50  thrpt    3   1535.463 ±   44.235  ops/s
PrimitiveOrRawEigenvalue.primitive       100  thrpt    3     93.056 ±  161.906  ops/s
PrimitiveOrRawEigenvalue.raw             100  thrpt    3    333.901 ±   87.067  ops/s
PrimitiveOrRawEigenvalue.simultaneous    100  thrpt    3    213.885 ±    9.403  ops/s
PrimitiveOrRawEigenvalue.primitive       200  thrpt    3     12.075 ±   44.446  ops/s
PrimitiveOrRawEigenvalue.raw             200  thrpt    3     52.143 ±   10.428  ops/s
PrimitiveOrRawEigenvalue.simultaneous    200  thrpt    3     29.206 ±   16.148  ops/s
PrimitiveOrRawEigenvalue.primitive       500  thrpt    3      2.028 ±    0.244  ops/s
PrimitiveOrRawEigenvalue.raw             500  thrpt    3      3.646 ±    0.090  ops/s
PrimitiveOrRawEigenvalue.simultaneous    500  thrpt    3      2.058 ±    0.949  ops/s
PrimitiveOrRawEigenvalue.primitive      1000  thrpt    3      0.313 ±    0.127  ops/s
PrimitiveOrRawEigenvalue.raw            1000  thrpt    3      0.478 ±    0.214  ops/s
PrimitiveOrRawEigenvalue.simultaneous   1000  thrpt    3      0.274 ±    0.011  ops/s
PrimitiveOrRawEigenvalue.primitive      2000  thrpt    3      0.040 ±    0.002  ops/s
PrimitiveOrRawEigenvalue.raw            2000  thrpt    3      0.048 ±    0.001  ops/s
PrimitiveOrRawEigenvalue.simultaneous   2000  thrpt    3      0.030 ±    0.030  ops/s
 * </pre>
 *
 * MacBook Air: 2017-03-15
 *
 * <pre>
# Run complete. Total time: 00:10:17

Benchmark                           (dim)   Mode  Cnt      Score      Error  Units
PrimitiveOrRawEigenvalue.primitive     20  thrpt   15  15422.767 ±  448.000  ops/s
PrimitiveOrRawEigenvalue.primitive     50  thrpt   15   1548.721 ±   58.641  ops/s
PrimitiveOrRawEigenvalue.primitive    100  thrpt   15    156.563 ±    3.416  ops/s
PrimitiveOrRawEigenvalue.primitive    200  thrpt   15     24.574 ±    0.582  ops/s
PrimitiveOrRawEigenvalue.primitive    500  thrpt   15      2.325 ±    0.144  ops/s
PrimitiveOrRawEigenvalue.primitive   1000  thrpt   15      0.297 ±    0.005  ops/s
PrimitiveOrRawEigenvalue.raw           20  thrpt   15  21054.544 ± 1251.169  ops/s
PrimitiveOrRawEigenvalue.raw           50  thrpt   15   2571.904 ±   87.115  ops/s
PrimitiveOrRawEigenvalue.raw          100  thrpt   15    399.230 ±   10.124  ops/s
PrimitiveOrRawEigenvalue.raw          200  thrpt   15     58.788 ±    2.082  ops/s
PrimitiveOrRawEigenvalue.raw          500  thrpt   15      3.326 ±    0.064  ops/s
PrimitiveOrRawEigenvalue.raw         1000  thrpt   15      0.436 ±    0.015  ops/s
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class PrimitiveOrRawEigenvalue extends AbstractPrimitiveOrRaw<Eigenvalue<Double>> {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(PrimitiveOrRawEigenvalue.class);
    }

    @Param({ "20", "50", "100", "200", "500", "1000", "2000" })
    public int dim;

    MatrixStore<Double> matrix;
    DecompositionStore<Double> preallocated;
    MatrixStore<Double> rhs;

    protected Eigenvalue<Double> simultaneous;

    public PrimitiveOrRawEigenvalue() {

        super();

        simultaneous = this.makeSimultaneousPrimitive();
    }

    @Override
    @Benchmark
    public MatrixStore<Double> primitive() {
        primitive.decompose(matrix);
        return primitive.getV();
    }

    @Override
    @Benchmark
    public MatrixStore<Double> raw() {
        raw.decompose(matrix);
        return raw.getV();
    }

    @Override
    @Setup
    public void setup() {

        super.setup();

        matrix = MatrixUtils.makeSPD(dim);
    }

    @Benchmark
    public MatrixStore<Double> simultaneous() {
        simultaneous.decompose(matrix);
        return simultaneous.getV();
    }

    @Override
    protected Eigenvalue<Double> makePrimitive() {
        return new HermitianEvD.Primitive();
    }

    @Override
    protected Eigenvalue<Double> makeRaw() {
        return new RawEigenvalue.Symmetric();
    }

    protected Eigenvalue<Double> makeSimultaneousPrimitive() {
        return new HermitianEvD.Primitive();
    }

}
