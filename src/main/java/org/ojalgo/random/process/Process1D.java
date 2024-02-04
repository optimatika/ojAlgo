/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.random.process;

import java.util.Collection;
import java.util.List;

import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.random.Distribution;
import org.ojalgo.random.Random1D;
import org.ojalgo.random.process.Process1D.ComponentProcess;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;

/**
 * Drive a set of processes simultaneously – correlated or uncorrelated.
 *
 * @author apete
 */
public final class Process1D<P extends ComponentProcess<?>> {

    interface ComponentProcess<D extends Distribution> extends RandomProcess<D> {

        double getValue();

        void setValue(double newValue);

        double step(double stepSize, double standardGaussianInnovation);

    }

    /**
     * Correlated processes
     */
    public static <P extends ComponentProcess<?>> Process1D<P> of(final Access2D<?> correlations, final List<? extends P> processes) {
        return new Process1D<>(new Random1D(correlations), Process1D.toArray(processes));
    }

    /**
     * Correlated processes
     */
    public static <P extends ComponentProcess<?>> Process1D<P> of(final Access2D<?> correlations, final P... processes) {
        return new Process1D<>(new Random1D(correlations), processes);
    }

    /**
     * Uncorrelated processes
     */
    public static <P extends ComponentProcess<?>> Process1D<P> of(final List<? extends P> processes) {
        return new Process1D<>(new Random1D(processes.size()), Process1D.toArray(processes));
    }

    /**
     * Uncorrelated processes
     */
    public static <P extends ComponentProcess<?>> Process1D<P> of(final P... processes) {
        return new Process1D<>(new Random1D(processes.length), processes);
    }

    static <P extends ComponentProcess<?>> P[] toArray(final Collection<? extends P> processes) {
        return (P[]) processes.toArray(new ComponentProcess[processes.size()]);
    }

    private final Random1D myGenerator;
    private final P[] myProcesses;

    Process1D(final Random1D generator, final P... processes) {

        super();

        myGenerator = generator;
        myProcesses = processes;
    }

    public double getValue(final int index) {
        return myProcesses[index].getValue();
    }

    public ArrayR064 getValues() {

        int length = myProcesses.length;
        ArrayR064 retVal = ArrayR064.make(length);

        for (int p = 0; p < length; p++) {
            retVal.set(p, myProcesses[p].getValue());
        }

        return retVal;
    }

    public void setValue(final int index, final double newValue) {
        myProcesses[index].setValue(newValue);
    }

    public void setValues(final Access1D<?> newValues) {
        for (int p = 0; p < myProcesses.length; p++) {
            myProcesses[p].setValue(newValues.doubleValue(p));
        }
    }

    public int size() {
        return myProcesses.length;
    }

    public Array1D<Double> step(final double stepSize) {

        Array1D<Double> retVal = myGenerator.nextGaussian();

        for (int p = 0; p < myProcesses.length; p++) {
            double standardGaussianInnovation = retVal.doubleValue(p);
            double nextValue = myProcesses[p].step(stepSize, standardGaussianInnovation);
            retVal.set(p, nextValue);
        }

        return retVal;
    }

}
