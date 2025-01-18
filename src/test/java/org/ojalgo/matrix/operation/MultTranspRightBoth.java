/*
 * Copyright 1997-2025 Optimatika
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
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.random.Normal;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Book Air 2016-06-18
 *
 * <pre>
Benchmark                             (complexity)   Mode  Cnt          Score          Error    Units
MultTranspRightBoth.multiplyBoth                 2  thrpt   15  859308617,494 ± 21265332,061  ops/min
MultTranspRightBoth.multiplyRight                2  thrpt   15  814120117,909 ± 14632299,918  ops/min
MultTranspRightBoth.multiplyDynamic              2  thrpt   15  789207226,918 ± 11621371,499  ops/min
MultTranspRightBoth.multiplyStatic               2  thrpt   15  504910322,261 ±  8235883,331  ops/min

MultTranspRightBoth.multiplyBoth                 3  thrpt   15  340427132,670 ±  6646513,672  ops/min
MultTranspRightBoth.multiplyRight                3  thrpt   15  338825570,766 ±  6587920,901  ops/min
MultTranspRightBoth.multiplyDynamic              3  thrpt   15  293713412,060 ±  4259437,565  ops/min
MultTranspRightBoth.multiplyStatic               3  thrpt   15  237515527,698 ±  4322985,976  ops/min

MultTranspRightBoth.multiplyBoth                 4  thrpt   15  198732502,615 ±  3375943,325  ops/min
MultTranspRightBoth.multiplyRight                4  thrpt   15  191099811,287 ±  6832061,364  ops/min
MultTranspRightBoth.multiplyDynamic              4  thrpt   15  190540659,383 ±  4663657,465  ops/min
MultTranspRightBoth.multiplyStatic               4  thrpt   15  134114256,259 ±  2869575,716  ops/min

MultTranspRightBoth.multiplyBoth                 5  thrpt   15  120080116,124 ±  2126115,730  ops/min
MultTranspRightBoth.multiplyRight                5  thrpt   15  127857366,783 ±  2559097,053  ops/min
MultTranspRightBoth.multiplyDynamic              5  thrpt   15  122262080,263 ±  2402192,416  ops/min
MultTranspRightBoth.multiplyStatic               5  thrpt   15   87029405,511 ±  2558033,737  ops/min

MultTranspRightBoth.multiplyBoth                10  thrpt   15   22462829,149 ±   355613,578  ops/min
MultTranspRightBoth.multiplyRight               10  thrpt   15   20923021,041 ±   320628,572  ops/min
MultTranspRightBoth.multiplyDynamic             10  thrpt   15   20993988,501 ±   463315,030  ops/min
MultTranspRightBoth.multiplyStatic              10  thrpt   15   18630712,688 ±   489887,345  ops/min

MultTranspRightBoth.multiplyBoth                20  thrpt   15     612678,662 ±    20014,652  ops/min
MultTranspRightBoth.multiplyRight               20  thrpt   15    3428840,829 ±    46539,854  ops/min
MultTranspRightBoth.multiplyDynamic             20  thrpt   15    3411110,218 ±    54602,306  ops/min
MultTranspRightBoth.multiplyStatic              20  thrpt   15    3468706,998 ±    38134,638  ops/min

MultTranspRightBoth.multiplyBoth                50  thrpt   15      58127,220 ±     2913,874  ops/min
MultTranspRightBoth.multiplyRight               50  thrpt   15     383111,904 ±    42737,856  ops/min
MultTranspRightBoth.multiplyDynamic             50  thrpt   15     378615,766 ±    37735,254  ops/min
MultTranspRightBoth.multiplyStatic              50  thrpt   15     323235,126 ±     5036,849  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class MultTranspRightBoth {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(MultTranspRightBoth.class);
    }

    @Param({ "2", "3", "4", "5", "10", "20", "50" })
    public int complexity;

    public R064Store left;
    public R064Store product;
    public R064Store right;

    MultiplyBoth.Primitive MB;
    MultiplyRight.Primitive64 MR;

    @Benchmark
    public R064Store multiplyBoth() {
        MB.invoke(product, left, complexity, right.transpose());
        return product;
    }

    @Benchmark
    public R064Store multiplyDynamic() {
        product.fillByMultiplying(left, right.transpose());
        return product;
    }

    @Benchmark
    public R064Store multiplyRight() {
        MR.invoke(product.data, left.data, complexity, right.transpose());
        return product;
    }

    @Benchmark
    public R064Store multiplyStatic() {
        MultiplyRight.addMxC(product.data, 0, complexity, left.data, complexity, right.transpose());
        return product;
    }

    @Setup
    public void setup() {

        left = R064Store.FACTORY.makeFilled(complexity, complexity, new Normal());
        right = R064Store.FACTORY.makeFilled(complexity, complexity, new Normal());
        product = R064Store.FACTORY.make(complexity, complexity);

        MR = MultiplyRight.newPrimitive64(complexity, complexity);
        MB = MultiplyBoth.newPrimitive64(complexity, complexity);
    }

}
