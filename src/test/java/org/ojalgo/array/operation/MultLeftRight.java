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
package org.ojalgo.array.operation;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Normal;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

@State(Scope.Benchmark)
public class MultLeftRight {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(MultLeftRight.class);
    }

    //@Param({ "3", "4", "9", "10" })
    @Param({ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" })
    public int complexity;

    public Primitive64Store left;
    public Primitive64Store right;
    public Primitive64Store product;

    MultiplyLeft.Primitive64 ML;
    MultiplyRight.Primitive64 MR;
    MultiplyNeither.Primitive64 MN;
    MultiplyBoth.Primitive MB;

    @Benchmark
    public Primitive64Store multiplyLeftFixed() {
        ML.invoke(product.data, left, complexity, right.data);
        return product;
    };

    @Benchmark
    public Primitive64Store multiplyLeftStandard() {
        MultiplyLeft.invoke(product.data, 0, complexity, left, complexity, right.data);
        return product;
    }

    public Primitive64Store multiplyRightFixed() {
        MR.invoke(product.data, left.data, complexity, right);
        return product;
    }

    public Primitive64Store multiplyRightStandard() {
        MultiplyRight.invoke(product.data, 0, complexity, left.data, complexity, right);
        return product;
    }

    @Setup
    public void setup() {
        left = Primitive64Store.FACTORY.makeFilled(complexity, complexity, new Normal());
        right = Primitive64Store.FACTORY.makeFilled(complexity, complexity, new Normal());
        product = Primitive64Store.FACTORY.makeZero(complexity, complexity);
        ML = MultiplyLeft.newPrimitive64(complexity, complexity);
        MR = MultiplyRight.newPrimitive64(complexity, complexity);
        MN = MultiplyNeither.newPrimitive64(complexity, complexity);
        MB = MultiplyBoth.newPrimitive64(complexity, complexity);
    }
}
