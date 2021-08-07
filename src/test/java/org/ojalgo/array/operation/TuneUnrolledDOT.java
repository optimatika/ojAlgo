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
import org.ojalgo.TestUtils;
import org.ojalgo.array.Primitive32Array;
import org.ojalgo.random.Uniform;
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
public class TuneUnrolledDOT {

    private static final int _1_000 = 1_000;
    private static final int _100 = 100;

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(TuneUnrolledDOT.class);
    }

    public float[] left;
    public float[] product;
    public float[] right;

    @Benchmark
    public void plain() {

        for (int j = 0, nbCols = product.length; j < nbCols; j++) {
            product[j] = DOT.plain(left, 0, right, j * _1_000, 0, _1_000);
        }
    }

    @Setup
    public void setup() {

        Primitive32Array l = Primitive32Array.make(1 * _1_000);
        l.fillAll(Uniform.standard());
        left = l.data;

        Primitive32Array r = Primitive32Array.make(_1_000 * _100);
        r.fillAll(Uniform.standard());
        right = r.data;

        Primitive32Array p = Primitive32Array.make(1 * _100);
        p.fillAll(Uniform.standard());
        product = p.data;

        float rp = DOT.plain(left, 0, right, 2 * _1_000, 0, _1_000);
        float ru2 = DOT.unrolled02(left, 0, right, 2 * _1_000, 0, _1_000);
        float ru4 = DOT.unrolled04(left, 0, right, 2 * _1_000, 0, _1_000);
        float ru8 = DOT.unrolled08(left, 0, right, 2 * _1_000, 0, _1_000);
        float ru16 = DOT.unrolled16(left, 0, right, 2 * _1_000, 0, _1_000);

        float delta = 0.001F;
        TestUtils.assertEquals(rp, ru2, delta);
        TestUtils.assertEquals(rp, ru4, delta);
        TestUtils.assertEquals(rp, ru8, delta);
        TestUtils.assertEquals(rp, ru16, delta);
    }

    @Benchmark
    public void unrolled02() {

        for (int j = 0, nbCols = product.length; j < nbCols; j++) {
            product[j] = DOT.unrolled02(left, 0, right, j * _1_000, 0, _1_000);
        }
    }

    @Benchmark
    public void unrolled04() {

        for (int j = 0, nbCols = product.length; j < nbCols; j++) {
            product[j] = DOT.unrolled04(left, 0, right, j * _1_000, 0, _1_000);
        }
    }

    @Benchmark
    public void unrolled08() {

        for (int j = 0, nbCols = product.length; j < nbCols; j++) {
            product[j] = DOT.unrolled08(left, 0, right, j * _1_000, 0, _1_000);
        }
    }

    @Benchmark
    public void unrolled16() {

        for (int j = 0, nbCols = product.length; j < nbCols; j++) {
            product[j] = DOT.unrolled16(left, 0, right, j * _1_000, 0, _1_000);
        }
    }

}
