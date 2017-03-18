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
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro: 2015-06-24 => 16
 *
 * <pre>
 * # Run complete. Total time: 00:02:03
 *
 * Benchmark                   (dim)   Mode  Cnt         Score        Error    Units
 * PrimitiveOrRawLU.primitive     10  thrpt    3  13578080,870 ± 802789,675  ops/min
 * PrimitiveOrRawLU.primitive     20  thrpt    3   2920265,006 ±  23322,349  ops/min
 * PrimitiveOrRawLU.primitive     50  thrpt    3    265251,744 ±   3410,323  ops/min
 * PrimitiveOrRawLU.primitive    100  thrpt    3     55651,238 ±   1843,507  ops/min
 * PrimitiveOrRawLU.primitive    200  thrpt    3     10950,428 ±    328,406  ops/min
 * PrimitiveOrRawLU.primitive    500  thrpt    3       956,466 ±     75,056  ops/min
 * PrimitiveOrRawLU.primitive   1000  thrpt    3       157,199 ±     29,044  ops/min
 * PrimitiveOrRawLU.raw           10  thrpt    3  14313960,051 ± 157788,131  ops/min
 * PrimitiveOrRawLU.raw           20  thrpt    3   2636161,879 ±  47762,990  ops/min
 * PrimitiveOrRawLU.raw           50  thrpt    3    249533,910 ±   4837,851  ops/min
 * PrimitiveOrRawLU.raw          100  thrpt    3     55654,408 ±   4362,385  ops/min
 * PrimitiveOrRawLU.raw          200  thrpt    3     10497,027 ±    389,121  ops/min
 * PrimitiveOrRawLU.raw          500  thrpt    3       786,141 ±    145,693  ops/min
 * PrimitiveOrRawLU.raw         1000  thrpt    3       100,325 ±      7,884  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class PrimitiveOrRawLU extends AbstractPrimitiveOrRaw<LU<Double>> {

    public static void main(final String[] args) throws RunnerException {
        LinearAlgebraBenchmark.run(PrimitiveOrRawLU.class);
    }

    @Param({ "10", "20", "50", "100", "200", "500", "1000" })
    public int dim;

    PrimitiveDenseStore matrix;
    PrimitiveDenseStore preallocated;

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

    @Setup
    public void setup() {

        matrix = MatrixUtils.makeSPD(dim);

        preallocated = PrimitiveDenseStore.FACTORY.makeZero(dim, dim);

    }

    @Override
    protected LU<Double> makePrimitive() {
        return new LUDecomposition.Primitive();
    }

    @Override
    protected LU<Double> makeRaw() {
        return new RawLU();
    }

}
