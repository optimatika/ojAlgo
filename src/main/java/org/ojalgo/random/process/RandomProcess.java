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

import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.random.Distribution;
import org.ojalgo.random.SampleSet;
import org.ojalgo.series.primitive.PrimitiveSeries;

/**
 * A random/stochastic process (a series of random variables indexed by time or space) representing the
 * evolution of some random value. The key thing you can do with a random process is to ask for its
 * distribution at a given (future) time.
 *
 * @author apete
 */
public interface RandomProcess<D extends Distribution> {

    public static final class SimulationResults {

        private final double myInitialValue;
        private final Array2D<Double> myResults;

        /**
         * @param initialValue Initial value
         * @param results (Random values) scenarios/realisations/series in rows, and sample sets in columns.
         */
        public SimulationResults(final double initialValue, final Array2D<Double> results) {

            super();

            myInitialValue = initialValue;
            myResults = results;
        }

        public int countSampleSets() {
            return myResults.getColDim();
        }

        public int countScenarios() {
            return myResults.getRowDim();
        }

        public double getInitialValue() {
            return myInitialValue;
        }

        public SampleSet getSampleSet(final int sampleSetIndex) {
            return SampleSet.wrap(myResults.sliceColumn(sampleSetIndex));
        }

        /**
         * A series representing one scenario. Each series has length "number of simulation steps" + 1 as the
         * series includes the initial value.
         */
        public PrimitiveSeries getScenario(final int scenarioIndex) {

            Array1D<Double> slicedRow = myResults.sliceRow(scenarioIndex);

            return new PrimitiveSeries() {

                @Override
                public int size() {
                    return slicedRow.size() + 1;
                }

                @Override
                public double value(final int index) {
                    if (index == 0) {
                        return myInitialValue;
                    } else {
                        return slicedRow.doubleValue(index - 1);
                    }
                }

            };
        }

    }

    /**
     * @param evaluationPoint How far into the future?
     * @return The distribution for the process value at that future time.
     */
    D getDistribution(double evaluationPoint);

    /**
     * Returns an collection of sample sets. The array has numberOfSteps elements, and each sample set has
     * numberOfRealisations samples.
     */
    RandomProcess.SimulationResults simulate(int numberOfRealisations, int numberOfSteps, double stepSize);

}
