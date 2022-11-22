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
import org.ojalgo.matrix.store.Primitive64Store;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro: 2019-04-25 => 512
 *
 * <pre>
Benchmark                   (dim)   Mode  Cnt       Score      Error    Units
PrimitiveOrRawLU.primitive     50  thrpt    3  301157.789 ± 4491.290  ops/min
PrimitiveOrRawLU.primitive    100  thrpt    3   53757.171 ± 1817.663  ops/min
PrimitiveOrRawLU.primitive    200  thrpt    3    9749.099 ±  129.020  ops/min
PrimitiveOrRawLU.primitive    500  thrpt    3     953.864 ±   43.429  ops/min
PrimitiveOrRawLU.primitive   1000  thrpt    3     166.926 ±    9.213  ops/min
PrimitiveOrRawLU.primitive   2000  thrpt    3      19.061 ±    2.633  ops/min
PrimitiveOrRawLU.raw           50  thrpt    3  358907.961 ± 9528.108  ops/min
PrimitiveOrRawLU.raw          100  thrpt    3   66471.777 ±  503.598  ops/min
PrimitiveOrRawLU.raw          200  thrpt    3   12666.588 ±  826.128  ops/min
PrimitiveOrRawLU.raw          500  thrpt    3    1016.314 ±   58.658  ops/min
PrimitiveOrRawLU.raw         1000  thrpt    3     137.489 ±   42.776  ops/min
PrimitiveOrRawLU.raw         2000  thrpt    3      11.569 ±    3.558  ops/min
 * </pre>
 *
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
        BenchmarkUtils.run(PrimitiveOrRawLU.class);
    }

    @Param({ "100", "200", "500", "1000", "2000" })
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

        matrix = Primitive64Store.FACTORY.makeSPD(dim);

        preallocated = Primitive64Store.FACTORY.make(dim, dim);
    }

    @Override
    protected LU<Double> makePrimitive() {
        return new LUDecomposition.R064();
    }

    @Override
    protected LU<Double> makeRaw() {
        return new RawLU();
    }

}
