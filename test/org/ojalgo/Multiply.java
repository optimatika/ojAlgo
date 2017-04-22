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
package org.ojalgo;

import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Normal;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
public class Multiply {

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder().include(".*" + Multiply.class.getSimpleName() + ".*").forks(1).build();
        new Runner(opt).run();
    }

    @Param({ "3", "4" })
    public int rows;
    @Param({ "3", "4" })
    public int complexity;
    @Param({ "3", "4" })
    public int columns;

    public BasicMatrix left;
    public BasicMatrix right;

    @Benchmark
    public double hypot() {
        return Math.hypot(rows, columns);
    };

    @Benchmark
    public BasicMatrix multiply() {
        return left.multiply(right);
    }

    @Setup
    public void setup() {
        BasicLogger.DEBUG.println();
        BasicLogger.DEBUG.println("setup");
        BasicLogger.DEBUG.println("rows: " + rows);
        BasicLogger.DEBUG.println("complexity: " + complexity);
        BasicLogger.DEBUG.println("columns: " + columns);
        left = PrimitiveMatrix.FACTORY.makeFilled(rows, complexity, new Normal());
        right = PrimitiveMatrix.FACTORY.makeFilled(complexity, columns, new Normal());
    }
}
