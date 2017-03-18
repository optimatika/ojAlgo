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
 * Mac Pro: 2015-06-23 => 8192L < primitive
 *
 * <pre>
 * # Run complete. Total time: 01:50:13
 *
 * Benchmark                           (dim)   Mode  Cnt         Score        Error    Units
 * PrimitiveOrRawEigenvalue.primitive      5  thrpt    3   1601950,602 ±  41898,899  ops/min
 * PrimitiveOrRawEigenvalue.primitive     10  thrpt    3    417818,262 ±  16765,578  ops/min
 * PrimitiveOrRawEigenvalue.primitive     20  thrpt    3    109291,846 ±  40996,968  ops/min
 * PrimitiveOrRawEigenvalue.primitive     50  thrpt    3     17944,622 ±   1045,067  ops/min
 * PrimitiveOrRawEigenvalue.primitive    100  thrpt    3      3532,074 ±    235,511  ops/min
 * PrimitiveOrRawEigenvalue.primitive    200  thrpt    3       574,723 ±     63,854  ops/min
 * PrimitiveOrRawEigenvalue.primitive    500  thrpt    3        75,237 ±     13,454  ops/min
 * PrimitiveOrRawEigenvalue.primitive   1000  thrpt    3        14,551 ±      0,344  ops/min
 * PrimitiveOrRawEigenvalue.primitive   2000  thrpt    3         2,168 ±      0,020  ops/min
 * PrimitiveOrRawEigenvalue.primitive   5000  thrpt    3         0,152 ±      0,002  ops/min
 * PrimitiveOrRawEigenvalue.raw            5  thrpt    3  20168235,047 ± 742079,618  ops/min
 * PrimitiveOrRawEigenvalue.raw           10  thrpt    3   5171421,262 ± 154827,904  ops/min
 * PrimitiveOrRawEigenvalue.raw           20  thrpt    3   1043347,383 ±  34181,534  ops/min
 * PrimitiveOrRawEigenvalue.raw           50  thrpt    3    120998,134 ±   5026,435  ops/min
 * PrimitiveOrRawEigenvalue.raw          100  thrpt    3     19887,301 ±    661,461  ops/min
 * PrimitiveOrRawEigenvalue.raw          200  thrpt    3      2946,835 ±     67,958  ops/min
 * PrimitiveOrRawEigenvalue.raw          500  thrpt    3       203,515 ±      4,626  ops/min
 * PrimitiveOrRawEigenvalue.raw         1000  thrpt    3        26,505 ±      1,474  ops/min
 * PrimitiveOrRawEigenvalue.raw         2000  thrpt    3         2,697 ±      0,327  ops/min
 * PrimitiveOrRawEigenvalue.raw         5000  thrpt    3         0,172 ±      0,012  ops/min
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

    @Param({ "20", "50", "100", "200", "500", "1000" })
    public int dim;

    MatrixStore<Double> matrix;
    DecompositionStore<Double> preallocated;
    MatrixStore<Double> rhs;

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

    @Override
    protected Eigenvalue<Double> makePrimitive() {
        return new HermitianEvD.Primitive();
    }

    @Override
    protected Eigenvalue<Double> makeRaw() {
        return new RawEigenvalue.Symmetric();
    }

}
