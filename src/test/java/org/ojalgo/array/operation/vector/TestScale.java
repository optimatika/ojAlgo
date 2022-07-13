/*
 * Copyright 1997-2021 Optimatika
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
package org.ojalgo.array.operation.vector;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

@State(Scope.Benchmark)
public class TestScale {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(BenchmarkUtils.options().jvmArgsAppend("--enable-preview", "--add-modules", "jdk.incubator.vector"), TestScale.class);
    }

    static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    public TestScale() {

    }

    double[] a;
    double[] b;
    double[] c;

    double alpha;

    @Setup
    public void setup() {

        ArrayR064 tmpA = ArrayR064.make(100_000);
        tmpA.fillAll(Uniform.standard());
        a = tmpA.data;

        ArrayR064 tmpB = ArrayR064.make(100_000);
        tmpB.fillAll(Uniform.standard());
        b = tmpB.data;

        c = new double[100_000];

        alpha = Uniform.standard().doubleValue();
    }

    @Benchmark
    public double[] tunePlain() {
        for (int i = 0; i < c.length; i++) {
            c[i] = a[i] + alpha * b[i];
        }
        return c;
    }

    @Benchmark
    public double[] tuneVector() {

        int limit = c.length;
        int bound = SPECIES.loopBound(limit);
        int length = SPECIES.length();

        for (int i = 0; i < bound; i += length) {
            DoubleVector aa = DoubleVector.fromArray(SPECIES, a, i);
            DoubleVector bb = DoubleVector.fromArray(SPECIES, b, i);
            DoubleVector cc = aa.lanewise(VectorOperators.FMA, alpha, bb);
            cc.intoArray(c, i);
        }

        for (int i = bound; i < limit; i++) {
            c[i] = a[i] + alpha * b[i];
        }

        return c;
    }
}
