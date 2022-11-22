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
 * Mac Pro (Early 2009): 2022-01-19 (THRESHOLD=128)
 *
 * <pre>
Benchmark                   (dim)   Mode  Cnt         Score         Error    Units
PrimitiveOrRawQR.primitive      5  thrpt    3  26636294.883 ± 5760740.613  ops/min
PrimitiveOrRawQR.primitive     10  thrpt    3   6949046.412 ± 1167689.550  ops/min
PrimitiveOrRawQR.primitive     20  thrpt    3   1552741.769 ±  155000.245  ops/min
PrimitiveOrRawQR.primitive     50  thrpt    3    161902.638 ±   16821.535  ops/min
PrimitiveOrRawQR.primitive    100  thrpt    3     25754.908 ±     492.051  ops/min
PrimitiveOrRawQR.primitive    200  thrpt    3      3994.875 ±    2550.248  ops/min
PrimitiveOrRawQR.primitive    500  thrpt    3       435.366 ±      10.623  ops/min
PrimitiveOrRawQR.primitive   1000  thrpt    3        79.496 ±       2.469  ops/min
PrimitiveOrRawQR.primitive   2000  thrpt    3        11.180 ±       0.672  ops/min
PrimitiveOrRawQR.primitive   5000  thrpt    3         0.835 ±       0.031  ops/min
PrimitiveOrRawQR.raw            5  thrpt    3  14850704.026 ±  721068.421  ops/min
PrimitiveOrRawQR.raw           10  thrpt    3   3779094.662 ±  352186.659  ops/min
PrimitiveOrRawQR.raw           20  thrpt    3    858292.024 ±   87312.352  ops/min
PrimitiveOrRawQR.raw           50  thrpt    3    118714.314 ±   15174.010  ops/min
PrimitiveOrRawQR.raw          100  thrpt    3     26581.289 ±    2393.740  ops/min
PrimitiveOrRawQR.raw          200  thrpt    3      4324.070 ±     611.890  ops/min
PrimitiveOrRawQR.raw          500  thrpt    3       490.001 ±      18.495  ops/min
PrimitiveOrRawQR.raw         1000  thrpt    3        87.984 ±       6.549  ops/min
PrimitiveOrRawQR.raw         2000  thrpt    3        11.877 ±       1.328  ops/min
PrimitiveOrRawQR.raw         5000  thrpt    3         0.889 ±       0.149  ops/min
 * </pre>
 * 
 * Mac Pro (Early 2009): 2022-01-19 (Modified code with multi-threading removed)
 * 
 * <pre>
 * new
Benchmark                   (dim)   Mode  Cnt         Score          Error    Units
PrimitiveOrRawQR.primitive      5  thrpt    3  82083258.994 ±  4463646.037  ops/min
PrimitiveOrRawQR.primitive     10  thrpt    3  21246661.298 ±  1960486.264  ops/min
PrimitiveOrRawQR.primitive     20  thrpt    3   4673525.744 ±   462739.631  ops/min
PrimitiveOrRawQR.primitive     50  thrpt    3    426339.598 ±    67903.478  ops/min
PrimitiveOrRawQR.primitive    100  thrpt    3     53988.736 ±     5648.318  ops/min
PrimitiveOrRawQR.primitive    200  thrpt    3      9740.146 ±     2586.553  ops/min
PrimitiveOrRawQR.primitive    500  thrpt    3      1168.068 ±       58.006  ops/min
PrimitiveOrRawQR.primitive   1000  thrpt    3       181.626 ±       57.049  ops/min
PrimitiveOrRawQR.primitive   2000  thrpt    3        19.205 ±        2.080  ops/min
PrimitiveOrRawQR.raw            5  thrpt    3  34622967.750 ± 10131278.531  ops/min
PrimitiveOrRawQR.raw           10  thrpt    3   9068261.483 ±  2711079.692  ops/min
PrimitiveOrRawQR.raw           20  thrpt    3   2008622.896 ±   516713.797  ops/min
PrimitiveOrRawQR.raw           50  thrpt    3    195235.046 ±    39308.275  ops/min
PrimitiveOrRawQR.raw          100  thrpt    3     35639.909 ±    12453.238  ops/min
PrimitiveOrRawQR.raw          200  thrpt    3      4689.203 ±     5259.302  ops/min
PrimitiveOrRawQR.raw          500  thrpt    3       489.794 ±      512.251  ops/min
PrimitiveOrRawQR.raw         1000  thrpt    3        83.007 ±       13.119  ops/min
PrimitiveOrRawQR.raw         2000  thrpt    3         6.959 ±        2.280  ops/min
 * </pre>
 *
 * <pre>
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class PrimitiveOrRawQR extends AbstractPrimitiveOrRaw<QR<Double>> {

    public static final class CodeAndData {

        public static QR<Double> newInstance(final int dim) {
            return QR.PRIMITIVE.make(2 * dim, dim);
        }

        public static QR<Double> newPrimitive() {
            return new QRDecomposition.R064();
        }

        public static QR<Double> newRaw() {
            return new RawQR();
        }

        final Primitive64Store body;
        transient PhysicalStore<Double> preallocated = null;
        final Primitive64Store rhs;

        public CodeAndData(final int dim) {

            super();

            body = Primitive64Store.FACTORY.makeFilled(2 * dim, dim, Uniform.standard());
            rhs = Primitive64Store.FACTORY.makeFilled(2 * dim, 1, Uniform.standard());
        }

        public MatrixStore<Double> execute(final QR<Double> decomposition) {
            if (preallocated == null) {
                preallocated = decomposition.preallocate(body, rhs);
            }
            decomposition.decompose(body);
            return decomposition.getSolution(rhs, preallocated);
        }

    }

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(PrimitiveOrRawQR.class);
    }

    @Param({ "5", "10", "20", "50", "100", "200", "500", "1000", "2000" })
    public int dim;

    CodeAndData codeAndData;

    @Override
    @Benchmark
    public MatrixStore<Double> primitive() {
        return codeAndData.execute(primitive);
    }

    @Override
    @Benchmark
    public MatrixStore<Double> raw() {
        return codeAndData.execute(raw);
    }

    @Override
    @Setup
    public void setup() {

        super.setup();

        codeAndData = new CodeAndData(dim);
    }

    @Override
    protected QR<Double> makePrimitive() {
        return new QRDecomposition.R064();
    }

    @Override
    protected QR<Double> makeRaw() {
        return new RawQR();
    }
}
