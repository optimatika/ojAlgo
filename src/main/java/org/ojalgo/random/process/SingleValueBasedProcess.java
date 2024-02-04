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

import static org.ojalgo.function.constant.PrimitiveMath.ONE;

import org.ojalgo.array.Array2D;
import org.ojalgo.random.Distribution;

abstract class SingleValueBasedProcess<D extends Distribution> extends AbstractProcess<D> {

    private double myCurrentValue = ONE;

    SingleValueBasedProcess() {
        super();
    }

    /**
     * @return An array of sample sets. The array has aNumberOfSteps elements, and each sample set has
     *         aNumberOfRealisations samples.
     */
    public RandomProcess.SimulationResults simulate(final int numberOfRealisations, final int numberOfSteps, final double stepSize) {

        double tmpInitialValue = myCurrentValue;

        Array2D<Double> tmpRealisationValues = Array2D.R064.make(numberOfRealisations, numberOfSteps);

        for (int r = 0; r < numberOfRealisations; r++) {
            double tmpCurrentValue = tmpInitialValue;
            for (int s = 0; s < numberOfSteps; s++) {
                tmpCurrentValue = this.doStep(stepSize, this.getNormalisedRandomIncrement());
                tmpRealisationValues.set(r, s, tmpCurrentValue);
            }
            myCurrentValue = tmpInitialValue;
        }

        return new RandomProcess.SimulationResults(tmpInitialValue, tmpRealisationValues);
    }

    @Override
    double getCurrentValue() {
        return myCurrentValue;
    }

    @Override
    void setCurrentValue(final double currentValue) {
        myCurrentValue = currentValue;
    }

}
