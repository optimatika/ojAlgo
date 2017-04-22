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
package org.ojalgo.matrix.store.operation;

import org.ojalgo.LinearAlgebraBenchmark;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
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
        LinearAlgebraBenchmark.run(MultLeftRight.class);
    }

    //@Param({ "3", "4", "9", "10" })
    @Param({ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" })
    public int complexity;

    public PrimitiveDenseStore left;
    public PrimitiveDenseStore right;
    public PrimitiveDenseStore product;

    PrimitiveDenseStore.PrimitiveMultiplyLeft ML;
    PrimitiveDenseStore.PrimitiveMultiplyRight MR;
    PrimitiveDenseStore.PrimitiveMultiplyNeither MN;
    PrimitiveDenseStore.PrimitiveMultiplyBoth MB;

    @Benchmark
    public PrimitiveDenseStore multiplyLeftFixed() {
        ML.invoke(product.data, left, complexity, right.data);
        return product;
    };

    @Benchmark
    public PrimitiveDenseStore multiplyLeftStandard() {
        MultiplyLeft.invoke(product.data, 0, complexity, left, complexity, right.data);
        return product;
    }

    public PrimitiveDenseStore multiplyRightFixed() {
        MR.invoke(product.data, left.data, complexity, right);
        return product;
    }

    public PrimitiveDenseStore multiplyRightStandard() {
        MultiplyRight.invoke(product.data, 0, complexity, left.data, complexity, right);
        return product;
    }

    @Setup
    public void setup() {
        left = PrimitiveDenseStore.FACTORY.makeFilled(complexity, complexity, new Normal());
        right = PrimitiveDenseStore.FACTORY.makeFilled(complexity, complexity, new Normal());
        product = PrimitiveDenseStore.FACTORY.makeZero(complexity, complexity);
        ML = MultiplyLeft.getPrimitive(complexity, complexity);
        MR = MultiplyRight.getPrimitive(complexity, complexity);
        MN = MultiplyNeither.getPrimitive(complexity, complexity);
        MB = MultiplyBoth.getPrimitive(complexity, complexity);
    }
}
