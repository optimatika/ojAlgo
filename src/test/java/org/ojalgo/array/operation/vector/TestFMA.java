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
public class TestFMA {

    static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(BenchmarkUtils.options().jvmArgsAppend("--enable-preview", "--add-modules", "jdk.incubator.vector"), TestFMA.class);
    }

    double[] addend;
    double[] base;
    double[] data;
    double[] multiplier;

    public TestFMA() {

    }

    @Setup
    public void setup() {

        int size = 100;

        ArrayR064 tmpData = ArrayR064.make(size);
        tmpData.fillAll(Uniform.standard());
        data = tmpData.data;

        ArrayR064 tmpBase = ArrayR064.make(size);
        tmpBase.fillAll(Uniform.standard());
        base = tmpBase.data;

        ArrayR064 tmpMultiplier = ArrayR064.make(size);
        tmpMultiplier.fillAll(Uniform.standard());
        multiplier = tmpMultiplier.data;

        ArrayR064 tmpAddend = ArrayR064.make(size);
        tmpAddend.fillAll(Uniform.standard());
        addend = tmpAddend.data;

    }

    @Benchmark
    public double[] tuneFMA() {
        for (int i = 0; i < data.length; i++) {
            data[i] = Math.fma(base[i], multiplier[i], addend[i]);
        }
        return data;
    }

    @Benchmark
    public double[] tunePlain() {
        for (int i = 0; i < data.length; i++) {
            data[i] = base[i] * multiplier[i] + addend[i];
        }
        return data;
    }

    @Benchmark
    public double[] tuneVector() {

        int limit = data.length;
        int bound = SPECIES.loopBound(limit);
        int length = SPECIES.length();

        for (int i = 0; i < bound; i += length) {
            DoubleVector bv = DoubleVector.fromArray(SPECIES, base, i);
            DoubleVector mv = DoubleVector.fromArray(SPECIES, multiplier, i);
            DoubleVector av = DoubleVector.fromArray(SPECIES, addend, i);
            DoubleVector dv = bv.lanewise(VectorOperators.FMA, mv, av);
            dv.intoArray(data, i);
        }

        for (int i = bound; i < limit; i++) {
            data[i] = base[i] * multiplier[i] + addend[i];
        }

        return data;
    }

}
