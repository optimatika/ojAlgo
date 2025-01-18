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
package org.ojalgo.matrix;

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

    public MatrixR064 left;
    public MatrixR064 right;

    @Benchmark
    public double hypot() {
        return Math.hypot(rows, columns);
    }

    @Benchmark
    public MatrixR064 multiply() {
        return left.multiply(right);
    }

    @Setup
    public void setup() {
        BasicLogger.DEBUG.println();
        BasicLogger.DEBUG.println("setup");
        BasicLogger.DEBUG.println("rows: " + rows);
        BasicLogger.DEBUG.println("complexity: " + complexity);
        BasicLogger.DEBUG.println("columns: " + columns);
        left = MatrixR064.FACTORY.makeFilled(rows, complexity, new Normal());
        right = MatrixR064.FACTORY.makeFilled(complexity, columns, new Normal());
    }
}
