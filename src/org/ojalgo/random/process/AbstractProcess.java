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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.ojalgo.array.Array2D;
import org.ojalgo.random.ContinuousDistribution;
import org.ojalgo.random.Distribution;
import org.ojalgo.type.keyvalue.ComparableToDouble;

/**
 * @deprecated Don't reference this class directly. Use the superinterface {@link RandomProcess} or one of the
 *             subclasses instead. This class will be refactored and/or made private.
 */
@Deprecated
public abstract class AbstractProcess<D extends Distribution> implements RandomProcess<D> {

    private final TreeSet<ComparableToDouble<Double>> myObservations = new TreeSet<>();

    protected AbstractProcess() {
        super();
    }

    public final boolean addObservation(final Double x, final double y) {
        return myObservations.add(new ComparableToDouble<>(x, y));
    }

    /**
     * Equivalent to calling {@link RandomProcess#getDistribution(double)} with argumant <code>1.0</code>, and
     * then {@link Distribution#getExpected()}.
     */
    public final double getExpected() {
        return this.getExpected(ONE);
    }

    /**
     * The same thing can be achieved by first calling {@link RandomProcess#getDistribution(double)} with
     * argumant <code>1.0</code>, and then {@link ContinuousDistribution#getQuantile(double)} (but with
     * different input argument).
     */
    public final double getLowerConfidenceQuantile(final double confidence) {
        return this.getLowerConfidenceQuantile(ONE, confidence);
    }

    /**
     * Equivalent to calling {@link RandomProcess#getDistribution(double)} with argumant <code>1.0</code>, and
     * then {@link Distribution#getStandardDeviation()}.
     */
    public final double getStandardDeviation() {
        return this.getStandardDeviation(ONE);
    }

    /**
     * The same thing can be achieved by first calling {@link RandomProcess#getDistribution(double)} with
     * argumant <code>1.0</code>, and then {@link ContinuousDistribution#getQuantile(double)} (but with
     * different input argument).
     */
    public final double getUpperConfidenceQuantile(final double confidence) {
        return this.getUpperConfidenceQuantile(ONE, confidence);
    }

    public final double getValue() {
        return myObservations.last().value;
    }

    /**
     * Equivalent to calling {@link RandomProcess#getDistribution(double)} with argumant <code>1.0</code>, and
     * then {@link Distribution#getVariance()}.
     */
    public final double getVariance() {
        return this.getVariance(ONE);
    }

    public final void setValue(final double newValue) {
        if (myObservations.size() <= 0) {
            myObservations.add(new ComparableToDouble<>(ZERO, newValue));
        } else {
            myObservations.add(new ComparableToDouble<>(myObservations.pollLast().key, newValue));
        }
    }

    /**
     * @return An array of sample sets. The array has aNumberOfSteps elements, and each sample set has
     *         aNumberOfRealisations samples.
     */
    public final RandomProcess.SimulationResults simulate(final int numberOfRealisations, final int numberOfSteps, final double stepSize) {

        final List<ComparableToDouble<Double>> tmpInitialState = new ArrayList<>(myObservations);
        final double tmpInitialValue = this.getValue();

        final Array2D<Double> tmpRealisationValues = Array2D.PRIMITIVE64.make(numberOfRealisations, numberOfSteps);

        for (int r = 0; r < numberOfRealisations; r++) {
            double tmpCurrentValue = tmpInitialValue;
            for (int s = 0; s < numberOfSteps; s++) {
                tmpCurrentValue = this.step(tmpCurrentValue, stepSize, this.getNormalisedRandomIncrement());
                tmpRealisationValues.set(r, s, tmpCurrentValue);
            }
            this.setObservations(tmpInitialState);
        }

        return new RandomProcess.SimulationResults(tmpInitialValue, tmpRealisationValues);
    }

    protected abstract double getNormalisedRandomIncrement();

    protected final void setObservations(final Collection<? extends ComparableToDouble<Double>> c) {
        myObservations.clear();
        myObservations.addAll(c);
    }

    protected abstract double step(double currentValue, final double stepSize, final double normalisedRandomIncrement);

    abstract double getExpected(double stepSize);

    abstract double getLowerConfidenceQuantile(double stepSize, final double confidence);

    final TreeSet<ComparableToDouble<Double>> getObservations() {
        return myObservations;
    }

    abstract double getStandardDeviation(double stepSize);

    abstract double getUpperConfidenceQuantile(double stepSize, final double confidence);

    abstract double getVariance(double stepSize);

    final double step(final double stepSize) {
        return this.step(this.getValue(), stepSize, this.getNormalisedRandomIncrement());
    }

}
