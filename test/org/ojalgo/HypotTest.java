/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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
package org.ojalgo;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
public class HypotTest {

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder().include(".*" + HypotTest.class.getSimpleName() + ".*").forks(1).build();
        new Runner(opt).run();
    }

    static double hypot(final double a, final double b) {
        double retVal;
        if (Math.abs(a) > Math.abs(b)) {
            retVal = b / a;
            retVal = Math.abs(a) * Math.sqrt(1 + (retVal * retVal));
        } else if (b != 0) {
            retVal = a / b;
            retVal = Math.abs(b) * Math.sqrt(1 + (retVal * retVal));
        } else {
            retVal = 0.0;
        }
        return retVal;
    }

    @Param({ "1.0", "0.1", "10.0", "-1.0", "-0.1", "-10.0" })
    double a;
    @Param({ "1.0", "0.1", "10.0", "-1.0", "-0.1", "-10.0" })
    double b;

    @Benchmark
    public Double testMath() {
        return Math.hypot(a, b);
    }

    @Benchmark
    public double testMyOwn() {
        return HypotTest.hypot(a, b);
    }

    @Benchmark
    public Double testStrictMath() {
        return StrictMath.hypot(a, b);
    }

}
