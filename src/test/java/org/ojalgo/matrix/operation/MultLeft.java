/*
 * Copyright 1997-2024 Optimatika
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
 * MacBook Pro (16-inch, 2019): 2022-01-07
 *
 * <pre>
Benchmark         (dim)   Mode  Cnt         Score         Error    Units
MultLeft.fillMxN     10  thrpt    3  69259457.238 ± 5549502.288  ops/min
MultLeft.fillMxN     20  thrpt    3  12076532.520 ± 1693226.108  ops/min
MultLeft.fillMxN     50  thrpt    3    923541.875 ±   76201.564  ops/min
MultLeft.fillMxN    100  thrpt    3    118642.396 ±   31969.063  ops/min
MultLeft.fillMxN    200  thrpt    3     13728.607 ±    1674.356  ops/min
MultLeft.fillMxN    500  thrpt    3       901.384 ±     160.241  ops/min
MultLeft.fillMxN   1000  thrpt    3       110.021 ±      40.650  ops/min
MultLeft.fillRxN     10  thrpt    3  59883986.589 ± 2749563.930  ops/min
MultLeft.fillRxN     20  thrpt    3  10547798.485 ± 2227399.580  ops/min
MultLeft.fillRxN     50  thrpt    3    968815.505 ±  123463.870  ops/min
MultLeft.fillRxN    100  thrpt    3    136613.659 ±   29847.679  ops/min
MultLeft.fillRxN    200  thrpt    3     15711.097 ±    6107.656  ops/min
MultLeft.fillRxN    500  thrpt    3      1005.652 ±     141.445  ops/min
MultLeft.fillRxN   1000  thrpt    3       122.445 ±       8.942  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class MultLeft {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(MultLeft.class);
    }

    @Param({ "10", "20", "50", "100", "200", "500", "1000" })
    public int dim;

    public R064Store left;
    public R064Store product;
    public R064Store right;

    @Benchmark
    public R064Store fillMxN() {
        MultiplyLeft.fillMxN(product.data, left, dim, right.data);
        return product;
    }

    @Benchmark
    public R064Store fillRxN() {
        MultiplyLeft.fillRxN(product.data, 0, dim, left, dim, right.data);
        return product;
    }

    @Setup
    public void setup() {

        left = R064Store.FACTORY.makeFilled(dim, dim, Normal.standard());
        right = R064Store.FACTORY.makeFilled(dim, dim, Normal.standard());
        product = R064Store.FACTORY.make(dim, dim);

    }
}
