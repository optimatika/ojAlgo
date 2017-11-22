/*
 * Copyright 1997-2015 Optimatika
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

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.ojalgo.matrix.MatrixUtils;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public abstract class LinearAlgebraBenchmark {

    public static void run(final Class<?> clazz) throws RunnerException {
        new Runner(LinearAlgebraBenchmark.options().include(clazz.getSimpleName()).build()).run();
    }

    protected static ChainedOptionsBuilder options() {
        return new OptionsBuilder().forks(1).measurementIterations(5).warmupIterations(9).mode(Mode.Throughput).timeUnit(TimeUnit.MINUTES)
                .timeout(new TimeValue(1L, TimeUnit.HOURS)).jvmArgs("-Xmx6g");
    }

    protected BenchmarkContestant<?> contestant;

    protected LinearAlgebraBenchmark() {
        super();
    }

    public abstract Object execute();

    /**
     * Verify that the tested library/functionality conforms with the benchmark specifications. Annotate the
     * implementation with <code>@TearDown(Level.Iteration)</code>
     */
    public abstract void verify() throws BenchmarkRequirementsException;

    protected final Object makeRandom(final int numberOfRows, final int numberOfColumns, final BenchmarkContestant<?> contestant) {
        final BenchmarkContestant<?>.MatrixBuilder tmpSupplier = contestant.getMatrixBuilder(numberOfRows, numberOfColumns);
        for (int j = 0; j < numberOfColumns; j++) {
            for (int i = 0; i < numberOfRows; i++) {
                tmpSupplier.set(i, j, Math.random());
            }
        }
        return tmpSupplier.get();
    }

    protected double[][] makeSPD(final int size) {
        return MatrixUtils.makeSPD(size).toRawCopy2D();
    }

    protected final Object makeSPD(final int size, final BenchmarkContestant<?> contestant) {

        final double[] tmpRandom = new double[size];

        final BenchmarkContestant<?>.MatrixBuilder tmpSupplier = contestant.getMatrixBuilder(size, size);

        for (int i = 0; i < size; i++) {

            tmpRandom[i] = Math.random();

            for (int j = 0; j < i; j++) {
                tmpSupplier.set(i, j, tmpRandom[i] + tmpRandom[j]);
                tmpSupplier.set(j, i, tmpRandom[j] + tmpRandom[i]);
            }
            tmpSupplier.set(i, i, tmpRandom[i] + 1.0);
        }

        return tmpSupplier.get();
    }

    protected void verifyStateless(final Class<?> clazz) throws BenchmarkRequirementsException {

        for (final Field tmpField : clazz.getDeclaredFields()) {
            if (!tmpField.getName().equals("this$0")) {
                throw new BenchmarkRequirementsException(tmpField.toString());
            }
        }

        final Class<?> tmpSuperclazz = clazz.getSuperclass();
        if (tmpSuperclazz != null) {
            this.verifyStateless(tmpSuperclazz);
        }

        for (final Class<?> tmpInterface : clazz.getInterfaces()) {
            this.verifyStateless(tmpInterface);
        }
    }

}
