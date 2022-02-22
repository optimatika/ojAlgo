/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.array.operation;

import java.util.Random;

import org.ojalgo.BenchmarkUtils;
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
public class TuneImplDetailDOT {

    private static final Random RANDOM = new Random();

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(TuneImplDetailDOT.class);
    }

    public double a0, a1, a2, a3;
    public double[] aa;

    public double b0, b1, b2, b3;
    public double[] bb;

    @Benchmark
    public double arraysCombined() {

        double retVal = 0.0;

        retVal += aa[0] * bb[0];
        retVal += aa[1] * bb[1];
        retVal += aa[2] * bb[2];
        retVal += aa[3] * bb[3];

        return retVal;
    }

    @Benchmark
    public double arraysLoop() {

        double retVal = 0.0;

        for (int i = 0; i < 4; i++) {
            retVal += aa[i] * bb[i];
        }

        return retVal;
    }

    @Benchmark
    public double arraysSeparated() {

        double retVal0 = aa[0] * bb[0];
        double retVal1 = aa[1] * bb[1];
        double retVal2 = aa[2] * bb[2];
        double retVal3 = aa[3] * bb[3];

        return retVal0 + retVal1 + retVal2 + retVal3;
    }

    @Benchmark
    public double elementsCombined() {

        double retVal = 0.0;

        retVal += a0 * b0;
        retVal += a1 * b1;
        retVal += a2 * b2;
        retVal += a3 * b3;

        return retVal;
    }

    @Benchmark
    public double elementsSeparated() {

        double retVal0 = a0 * b0;
        double retVal1 = a1 * b1;
        double retVal2 = a2 * b2;
        double retVal3 = a3 * b3;

        return retVal0 + retVal1 + retVal2 + retVal3;
    }

    @Setup
    public void setup() {

        a0 = RANDOM.nextDouble();
        a1 = RANDOM.nextDouble();
        a2 = RANDOM.nextDouble();
        a3 = RANDOM.nextDouble();

        b0 = RANDOM.nextDouble();
        b1 = RANDOM.nextDouble();
        b2 = RANDOM.nextDouble();
        b3 = RANDOM.nextDouble();

        aa = new double[] { a0, a1, a2, a3 };
        bb = new double[] { b0, b1, b2, b3 };
    }

}
