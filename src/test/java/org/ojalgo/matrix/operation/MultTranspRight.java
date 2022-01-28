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
package org.ojalgo.matrix.operation;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Normal;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro 2016-06-16
 *
 * <pre>
# Run complete. Total time: 00:18:56

Benchmark                                 (complexity)   Mode  Cnt          Score         Error    Units
MultTranspRight.multiplyRightDynamicInt              2  thrpt   15  736236268,688 ±  379374,174  ops/min
MultTranspRight.multiplyRightDynamicPre              2  thrpt   15  726592647,030 ± 2779151,584  ops/min
MultTranspRight.multiplyRightFixedInt                2  thrpt   15  750249398,058 ±  545457,722  ops/min
MultTranspRight.multiplyRightFixedPre                2  thrpt   15  756574148,341 ± 3347064,721  ops/min
MultTranspRight.multiplyRightStandardInt             2  thrpt   15  514015756,600 ± 3446226,385  ops/min
MultTranspRight.multiplyRightStandardPre             2  thrpt   15  511881631,316 ± 4173388,650  ops/min

MultTranspRight.multiplyRightDynamicInt              3  thrpt   15  302274315,411 ±  424729,824  ops/min
MultTranspRight.multiplyRightDynamicPre              3  thrpt   15  314489479,247 ±  301864,142  ops/min
MultTranspRight.multiplyRightFixedInt                3  thrpt   15  336922959,484 ± 2736321,762  ops/min
MultTranspRight.multiplyRightFixedPre                3  thrpt   15  354381269,090 ± 3821255,643  ops/min
MultTranspRight.multiplyRightStandardInt             3  thrpt   15  236978301,393 ±  452889,945  ops/min
MultTranspRight.multiplyRightStandardPre             3  thrpt   15  228184751,274 ±  185102,355  ops/min

MultTranspRight.multiplyRightDynamicInt              4  thrpt   15  181252141,073 ±  470566,829  ops/min
MultTranspRight.multiplyRightDynamicPre              4  thrpt   15  184844819,099 ±  769102,965  ops/min
MultTranspRight.multiplyRightFixedInt                4  thrpt   15  183871725,041 ±  250672,896  ops/min
MultTranspRight.multiplyRightFixedPre                4  thrpt   15  187840719,230 ±  101230,571  ops/min
MultTranspRight.multiplyRightStandardInt             4  thrpt   15  132806363,148 ±  751551,579  ops/min
MultTranspRight.multiplyRightStandardPre             4  thrpt   15  122714777,952 ± 4376221,990  ops/min

MultTranspRight.multiplyRightDynamicInt              5  thrpt   15  118013240,521 ± 3242699,452  ops/min
MultTranspRight.multiplyRightDynamicPre              5  thrpt   15  122511808,103 ±  327975,986  ops/min
MultTranspRight.multiplyRightFixedInt                5  thrpt   15  120833041,630 ±  217311,086  ops/min
MultTranspRight.multiplyRightFixedPre                5  thrpt   15  123195358,544 ±  971039,837  ops/min
MultTranspRight.multiplyRightStandardInt             5  thrpt   15   84387191,351 ±  422472,951  ops/min
MultTranspRight.multiplyRightStandardPre             5  thrpt   15   83147842,353 ±  521312,876  ops/min

MultTranspRight.multiplyRightDynamicInt             10  thrpt   15   19017599,199 ±   12626,284  ops/min
MultTranspRight.multiplyRightDynamicPre             10  thrpt   15   19120228,242 ±   72025,993  ops/min
MultTranspRight.multiplyRightFixedInt               10  thrpt   15   19062670,245 ±   16656,107  ops/min
MultTranspRight.multiplyRightFixedPre               10  thrpt   15   18613951,721 ±  500136,024  ops/min
MultTranspRight.multiplyRightStandardInt            10  thrpt   15   18482285,176 ±   33982,862  ops/min
MultTranspRight.multiplyRightStandardPre            10  thrpt   15   18019793,791 ±   56753,084  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class MultTranspRight {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(MultTranspRight.class);
    }

    @Param({ "2", "3", "4", "5", "10" })
    public int complexity;

    public Primitive64Store left;
    public Primitive64Store product;
    public MatrixStore<Double> rightD;
    public MatrixStore<Double> rightT;

    MultiplyRight.Primitive64 MR;

    @Benchmark
    public Primitive64Store multiplyRightDynamicInt() {
        product.fillByMultiplying(left, rightD.transpose());
        return product;
    }

    @Benchmark
    public Primitive64Store multiplyRightDynamicPre() {
        product.fillByMultiplying(left, rightT);
        return product;
    }

    @Benchmark
    public Primitive64Store multiplyRightFixedInt() {
        MR.invoke(product.data, left.data, complexity, rightD.transpose());
        return product;
    }

    @Benchmark
    public Primitive64Store multiplyRightFixedPre() {
        MR.invoke(product.data, left.data, complexity, rightT);
        return product;
    }

    @Benchmark
    public Primitive64Store multiplyRightStandardInt() {
        MultiplyRight.addMxC(product.data, 0, complexity, left.data, complexity, rightD.transpose());
        return product;
    }

    @Benchmark
    public Primitive64Store multiplyRightStandardPre() {
        MultiplyRight.addMxC(product.data, 0, complexity, left.data, complexity, rightT);
        return product;
    }

    @Setup
    public void setup() {

        left = Primitive64Store.FACTORY.makeFilled(complexity, complexity, new Normal());
        rightD = Primitive64Store.FACTORY.makeFilled(complexity, complexity, new Normal());
        rightT = rightD.transpose();
        product = Primitive64Store.FACTORY.make(complexity, complexity);

        MR = MultiplyRight.newPrimitive64(complexity, complexity);
    }

}
