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
 * Mac Pro: 2015-06-23 => 2048
 *
 * <pre>
 * # Run complete. Total time: 03:49:11
 *
 * Benchmark                              (dim)   Mode  Cnt        Score        Error    Units
 * PrimitiveOrRawSingularValue.primitive      5  thrpt    3  1245689,416 ±   6939,980  ops/min
 * PrimitiveOrRawSingularValue.primitive     10  thrpt    3   335548,634 ±   8511,615  ops/min
 * PrimitiveOrRawSingularValue.primitive     20  thrpt    3    72986,841 ±    783,793  ops/min
 * PrimitiveOrRawSingularValue.primitive     50  thrpt    3    10778,273 ±    120,010  ops/min
 * PrimitiveOrRawSingularValue.primitive    100  thrpt    3     2189,222 ±     61,703  ops/min
 * PrimitiveOrRawSingularValue.primitive    200  thrpt    3      432,115 ±     20,932  ops/min
 * PrimitiveOrRawSingularValue.primitive    500  thrpt    3       44,580 ±      4,428  ops/min
 * PrimitiveOrRawSingularValue.primitive   1000  thrpt    3        7,285 ±      0,835  ops/min
 * PrimitiveOrRawSingularValue.primitive   2000  thrpt    3        1,044 ±      0,104  ops/min
 * PrimitiveOrRawSingularValue.primitive   5000  thrpt    3        0,081 ±      0,007  ops/min
 * PrimitiveOrRawSingularValue.raw            5  thrpt    3  8726260,609 ± 143736,901  ops/min
 * PrimitiveOrRawSingularValue.raw           10  thrpt    3  2410703,814 ±  85389,067  ops/min
 * PrimitiveOrRawSingularValue.raw           20  thrpt    3   492392,413 ±   7726,306  ops/min
 * PrimitiveOrRawSingularValue.raw           50  thrpt    3    55911,516 ±   1168,551  ops/min
 * PrimitiveOrRawSingularValue.raw          100  thrpt    3     8790,293 ±    205,273  ops/min
 * PrimitiveOrRawSingularValue.raw          200  thrpt    3     1247,406 ±     64,764  ops/min
 * PrimitiveOrRawSingularValue.raw          500  thrpt    3       95,638 ±      7,603  ops/min
 * PrimitiveOrRawSingularValue.raw         1000  thrpt    3        9,490 ±      1,559  ops/min
 * PrimitiveOrRawSingularValue.raw         2000  thrpt    3        1,111 ±      0,130  ops/min
 * PrimitiveOrRawSingularValue.raw         5000  thrpt    3        0,071 ±      0,003  ops/min
 * </pre>
 *
 * MacBook Air: 2015-06-23
 *
 * <pre>
 * # Run complete. Total time: 02:28:37
 *
 * Benchmark                              (dim)   Mode  Cnt  Score   Error    Units
 * PrimitiveOrRawSingularValue.primitive    512  thrpt    3  50,619 ± 27,497  ops/min
 * PrimitiveOrRawSingularValue.primitive   1024  thrpt    3   7,994 ±  0,833  ops/min
 * PrimitiveOrRawSingularValue.primitive   2048  thrpt    3   1,113 ±  0,021  ops/min
 * PrimitiveOrRawSingularValue.primitive   4096  thrpt    3   0,155 ±  0,034  ops/min
 * PrimitiveOrRawSingularValue.raw          512  thrpt    3  79,906 ± 30,870  ops/min
 * PrimitiveOrRawSingularValue.raw         1024  thrpt    3   9,114 ±  0,844  ops/min
 * PrimitiveOrRawSingularValue.raw         2048  thrpt    3   1,184 ±  0,097  ops/min
 * PrimitiveOrRawSingularValue.raw         4096  thrpt    3   0,150 ±  0,024  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class PrimitiveOrRawSingularValue extends AbstractPrimitiveOrRaw<SingularValue<Double>> {

    public static void main(final String[] args) throws RunnerException {
        LinearAlgebraBenchmark.run(PrimitiveOrRawSingularValue.class);
    }

    @Param({ "500", "1000", "2000", "5000" })
    public int dim;

    protected SingularValue<Double> alternative = new SVDold30.Primitive();

    MatrixStore<Double> matrix;

    public MatrixStore<Double> alternative() {

        alternative.compute(matrix);

        alternative.getQ1();
        alternative.getQ2();

        return alternative.getD();
    }

    @Override
    @Benchmark
    public MatrixStore<Double> primitive() {

        primitive.compute(matrix);

        primitive.getQ1();
        primitive.getQ2();

        return primitive.getD();
    }

    @Override
    @Benchmark
    public MatrixStore<Double> raw() {

        raw.compute(matrix);

        raw.getQ1();
        raw.getQ2();

        return raw.getD();
    }

    @Setup
    public void setup() {

        matrix = MatrixUtils.makeSPD(dim);
    }

    @Override
    protected SingularValue<Double> makePrimitive() {
        return new SVDnew32.Primitive();
    }

    @Override
    protected SingularValue<Double> makeRaw() {
        return new RawSingularValue();
    }

}
