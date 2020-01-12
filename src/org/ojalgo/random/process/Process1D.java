/*
 * Copyright 1997-2019 Optimatika
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

import java.util.List;

import org.ojalgo.array.Array1D;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.random.Random1D;
import org.ojalgo.random.process.RandomProcess.SimulationResults;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;

/**
 * Drive a set of processes simultaneously – correlated or uncorrelated.
 *
 * @author apete
 */
public class Process1D<P extends AbstractProcess<?>> {

    /**
     * Correlated processes
     */
    public static <P extends AbstractProcess<?>> Process1D of(final Access2D<?> correlations, final P... processes) {
        return new Process1D<>(new Random1D(correlations), processes);
    }

    /**
     * Uncorrelated processes
     */
    public static <P extends AbstractProcess<?>> Process1D of(final P... processes) {
        return new Process1D<>(new Random1D(processes.length), processes);
    }

    private final Random1D myGenerator;
    private final P[] myProcesses;

    @SuppressWarnings("unchecked")
    protected Process1D(final Access2D<?> correlations, final List<? extends P> processes) {
        this(new Random1D(correlations), (P[]) processes.toArray(new AbstractProcess[processes.size()]));
    }

    @SuppressWarnings("unchecked")
    protected Process1D(final List<? extends P> processes) {
        this(new Random1D(processes.size()), (P[]) processes.toArray(new AbstractProcess[processes.size()]));
    }

    Process1D(final Random1D generator, final P... processes) {

        super();

        myGenerator = generator;
        myProcesses = processes;
    }

    public double getValue(final int index) {
        return myProcesses[index].getValue();
    }

    public Primitive64Array getValues() {

        final int tmpLength = myProcesses.length;
        final Primitive64Array retVal = Primitive64Array.make(tmpLength);

        for (int p = 0; p < tmpLength; p++) {
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

        final Array1D<Double> retVal = myGenerator.nextGaussian();

        for (int p = 0; p < myProcesses.length; p++) {
            retVal.set(p, myProcesses[p].step(this.getValue(p), stepSize, retVal.doubleValue(p)));
        }

        return retVal;
    }

    protected P getProcess(final int index) {
        return myProcesses[index];
    }

    double getExpected(final int index, final double stepSize) {
        return myProcesses[index].getExpected(stepSize);
    }

    double getLowerConfidenceQuantile(final int index, final double stepSize, final double confidence) {
        return myProcesses[index].getLowerConfidenceQuantile(stepSize, confidence);
    }

    double getStandardDeviation(final int index, final double stepSize) {
        return myProcesses[index].getStandardDeviation(stepSize);
    }

    double getUpperConfidenceQuantile(final int index, final double stepSize, final double confidence) {
        return myProcesses[index].getUpperConfidenceQuantile(stepSize, confidence);
    }

    double getVariance(final int index, final double stepSize) {
        return myProcesses[index].getVariance(stepSize);
    }

    SimulationResults simulate(final int index, final int numberOfRealisations, final int numberOfSteps, final double stepSize) {
        return myProcesses[index].simulate(numberOfRealisations, numberOfSteps, stepSize);
    }

    double step(final int index, final double stepSize) {
        return myProcesses[index].step(stepSize);
    }

}
