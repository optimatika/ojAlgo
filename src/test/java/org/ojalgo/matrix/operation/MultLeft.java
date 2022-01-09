/*
 * Copyright (c) 2005, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.ojalgo.matrix.operation;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.matrix.store.Primitive64Store;
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

    public Primitive64Store left;
    public Primitive64Store product;
    public Primitive64Store right;

    @Benchmark
    public Primitive64Store fillRxN() {
        MultiplyLeft.fillRxN(product.data, 0, dim, left, dim, right.data);
        return product;
    }

    @Benchmark
    public Primitive64Store fillMxN() {
        MultiplyLeft.fillMxN(product.data, left, dim, right.data);
        return product;
    }

    @Setup
    public void setup() {

        left = Primitive64Store.FACTORY.makeFilled(dim, dim, Normal.standard());
        right = Primitive64Store.FACTORY.makeFilled(dim, dim, Normal.standard());
        product = Primitive64Store.FACTORY.make(dim, dim);

    }
}
