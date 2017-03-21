/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import org.ojalgo.LinearAlgebraBenchmark;
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
# Run complete. Total time: 00:37:47

Benchmark                           (dim)   Mode  Cnt      Score      Error  Units
PrimitiveOrRawEigenvalue.primitive     20  thrpt   15  13107.915 ±  114.357  ops/s
PrimitiveOrRawEigenvalue.primitive     50  thrpt   15   1277.783 ±   12.526  ops/s
PrimitiveOrRawEigenvalue.primitive    100  thrpt   15    204.110 ±    3.790  ops/s
PrimitiveOrRawEigenvalue.primitive    200  thrpt   15     28.279 ±    0.291  ops/s
PrimitiveOrRawEigenvalue.primitive    500  thrpt   15      2.127 ±    0.034  ops/s
PrimitiveOrRawEigenvalue.primitive   1000  thrpt   15      0.320 ±    0.003  ops/s
PrimitiveOrRawEigenvalue.primitive   2000  thrpt   15      0.041 ±    0.001  ops/s
PrimitiveOrRawEigenvalue.raw           20  thrpt   15  19428.335 ± 1105.222  ops/s
PrimitiveOrRawEigenvalue.raw           50  thrpt   15   2281.822 ±   40.410  ops/s
PrimitiveOrRawEigenvalue.raw          100  thrpt   15    345.549 ±   12.416  ops/s
PrimitiveOrRawEigenvalue.raw          200  thrpt   15     53.138 ±    0.462  ops/s
PrimitiveOrRawEigenvalue.raw          500  thrpt   15      3.683 ±    0.009  ops/s
PrimitiveOrRawEigenvalue.raw         1000  thrpt   15      0.483 ±    0.003  ops/s
PrimitiveOrRawEigenvalue.raw         2000  thrpt   15      0.047 ±    0.001  ops/s
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
        LinearAlgebraBenchmark.run(PrimitiveOrRawEigenvalue.class);
    }

    @Param({ "20", "50", "100", "200" })
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

    @Setup
    public void setup() {
        matrix = MatrixUtils.makeSPD(dim);
    }

    @Benchmark
    public MatrixStore<Double> simultaneous() {
        simultaneous.decompose(matrix);
        return simultaneous.getV();
    }

    @Override
    protected Eigenvalue<Double> makePrimitive() {
        return new HermitianEvD.DeferredPrimitive();
    }

    @Override
    protected Eigenvalue<Double> makeRaw() {
        return new RawEigenvalue.Symmetric();
    }

    protected Eigenvalue<Double> makeSimultaneousPrimitive() {
        return new HermitianEvD.SimultaneousPrimitive();
    }

}
