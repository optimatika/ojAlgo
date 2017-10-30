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

import java.util.Random;

import org.ojalgo.LinearAlgebraBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * <pre>
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class TestDot {

    public static void main(final String[] args) throws RunnerException {
        LinearAlgebraBenchmark.run(TestDot.class);
    }

    public double a0, a1, a2, a3;
    public double b0, b1, b2, b3;

    public double[] aa;
    public double[] bb;

    @Benchmark
    public double vectorsLoop() {

        double retVal = 0.0;

        for (int i = 0; i < 4; i++) {
            retVal += aa[i] * bb[i];
        }

        return retVal;
    };

    @Benchmark
    public double vectorsUnrolled() {

        double retVal = 0.0;

        retVal += aa[0] * bb[0];
        retVal += aa[1] * bb[1];
        retVal += aa[2] * bb[2];
        retVal += aa[3] * bb[3];

        return retVal;
    };

    @Benchmark
    public double elementsUnrolled() {

        double retVal = 0.0;

        retVal += a0 * b0;
        retVal += a1 * b1;
        retVal += a2 * b2;
        retVal += a3 * b3;

        return retVal;
    };

    @Benchmark
    public double vectorsSeparated() {

        final double retVal0 = aa[0] * bb[0];
        final double retVal1 = aa[1] * bb[1];
        final double retVal2 = aa[2] * bb[2];
        final double retVal3 = aa[3] * bb[3];

        return retVal0 + retVal1 + retVal2 + retVal3;
    };

    @Benchmark
    public double elementsSeparated() {

        final double retVal0 = a0 * b0;
        final double retVal1 = a1 * b1;
        final double retVal2 = a2 * b2;
        final double retVal3 = a3 * b3;

        return retVal0 + retVal1 + retVal2 + retVal3;
    };

    @Setup
    public void setup() {

        final Random rnd = new Random();

        a0 = rnd.nextDouble();
        a1 = rnd.nextDouble();
        a2 = rnd.nextDouble();
        a3 = rnd.nextDouble();

        b0 = rnd.nextDouble();
        b1 = rnd.nextDouble();
        b2 = rnd.nextDouble();
        b3 = rnd.nextDouble();

        aa = new double[] { a0, a1, a2, a3 };
        bb = new double[] { b0, b1, b2, b3 };

    }
}
