/*
 * Copyright 1997-2020 Optimatika
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
 * Mac Pro: 2017-04-17 => 2048
 *
 * <pre>
# Run complete. Total time: 04:45:42

Benchmark                              (dim)   Mode  Cnt     Score     Error    Units
PrimitiveOrRawSingularValue.primitive    100  thrpt    3  5941.421 ± 649.420  ops/min
PrimitiveOrRawSingularValue.primitive    200  thrpt    3   727.060 ±  98.187  ops/min
PrimitiveOrRawSingularValue.primitive    500  thrpt    3    55.095 ±   3.849  ops/min
PrimitiveOrRawSingularValue.primitive   1000  thrpt    3     7.536 ±   2.056  ops/min
PrimitiveOrRawSingularValue.primitive   2000  thrpt    3     0.981 ±   0.070  ops/min
PrimitiveOrRawSingularValue.primitive   5000  thrpt    3     0.066 ±   0.003  ops/min
PrimitiveOrRawSingularValue.raw          100  thrpt    3  8469.504 ±  94.651  ops/min
PrimitiveOrRawSingularValue.raw          200  thrpt    3  1199.734 ±  18.795  ops/min
PrimitiveOrRawSingularValue.raw          500  thrpt    3    82.169 ±   1.427  ops/min
PrimitiveOrRawSingularValue.raw         1000  thrpt    3     9.282 ±   0.147  ops/min
PrimitiveOrRawSingularValue.raw         2000  thrpt    3     0.851 ±   0.130  ops/min
PrimitiveOrRawSingularValue.raw         5000  thrpt    3     0.055 ±   0.001  ops/min
 * </pre>
 *
 * MacBook Air: 2017-04-13
 *
 * <pre>
# Run complete. Total time: 00:19:21

Benchmark                              (dim)   Mode  Cnt    Score    Error  Units
PrimitiveOrRawSingularValue.primitive    100  thrpt    3   89.159 ± 66.324  ops/s
PrimitiveOrRawSingularValue.primitive    200  thrpt    3   11.214 ± 11.993  ops/s
PrimitiveOrRawSingularValue.primitive    500  thrpt    3    0.697 ±  0.816  ops/s
PrimitiveOrRawSingularValue.primitive   1000  thrpt    3    0.141 ±  0.053  ops/s
PrimitiveOrRawSingularValue.primitive   2000  thrpt    3    0.018 ±  0.004  ops/s
PrimitiveOrRawSingularValue.raw          100  thrpt    3  148.800 ± 43.192  ops/s
PrimitiveOrRawSingularValue.raw          200  thrpt    3   20.564 ± 13.709  ops/s
PrimitiveOrRawSingularValue.raw          500  thrpt    3    1.145 ±  0.320  ops/s
PrimitiveOrRawSingularValue.raw         1000  thrpt    3    0.154 ±  0.014  ops/s
PrimitiveOrRawSingularValue.raw         2000  thrpt    3    0.016 ±  0.003  ops/s
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class PrimitiveOrRawSingularValue extends AbstractPrimitiveOrRaw<SingularValue<Double>> {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(PrimitiveOrRawSingularValue.class);
    }

    @Param({ "100", "200", "500", "1000", "2000", "5000" })
    public int dim;

    MatrixStore<Double> matrix;

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

    @Override
    @Setup
    public void setup() {

        super.setup();

        matrix = MatrixUtils.makeSPD(dim);
    }

    @Override
    protected SingularValue<Double> makePrimitive() {
        return new SingularValueDecomposition.Primitive();
    }

    @Override
    protected SingularValue<Double> makeRaw() {
        return new RawSingularValue();
    }

}
