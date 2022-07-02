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
package org.ojalgo.random.process;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.ojalgo.array.Array2D;
import org.ojalgo.random.Distribution;
import org.ojalgo.type.keyvalue.EntryPair;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

/**
 */
abstract class MultipleValuesBasedProcess<D extends Distribution> extends AbstractProcess<D> {

    private final TreeSet<EntryPair.KeyedPrimitive<Double>> myObservations = new TreeSet<>();

    MultipleValuesBasedProcess() {
        super();
    }

    public boolean addObservation(final Double x, final double y) {
        return myObservations.add(EntryPair.of(x, y));
    }

    /**
     * @return An array of sample sets. The array has aNumberOfSteps elements, and each sample set has
     *         aNumberOfRealisations samples.
     */
    public RandomProcess.SimulationResults simulate(final int numberOfRealisations, final int numberOfSteps, final double stepSize) {

        List<KeyedPrimitive<Double>> initialState = new ArrayList<>(myObservations);
        double initialValue = this.getCurrentValue();

        Array2D<Double> tmpRealisationValues = Array2D.PRIMITIVE64.make(numberOfRealisations, numberOfSteps);

        for (int r = 0; r < numberOfRealisations; r++) {
            double tmpCurrentValue = initialValue;
            for (int s = 0; s < numberOfSteps; s++) {
                tmpCurrentValue = this.doStep(stepSize, this.getNormalisedRandomIncrement());
                tmpRealisationValues.set(r, s, tmpCurrentValue);
            }
            this.setObservations(initialState);
        }

        return new RandomProcess.SimulationResults(initialValue, tmpRealisationValues);
    }

    @Override
    double getCurrentValue() {
        return myObservations.last().doubleValue();
    }

    TreeSet<KeyedPrimitive<Double>> getObservations() {
        return myObservations;
    }

    @Override
    void setCurrentValue(final double newValue) {
        if (myObservations.size() <= 0) {
            myObservations.add(EntryPair.of(Double.valueOf(ZERO), newValue));
        } else {
            myObservations.add(EntryPair.of(myObservations.pollLast().getKey(), newValue));
        }
    }

    final void setObservations(final Collection<? extends KeyedPrimitive<Double>> c) {
        myObservations.clear();
        myObservations.addAll(c);
    }

}
