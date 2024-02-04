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
package org.ojalgo.data.domain.finance.portfolio.simulator;

import java.math.BigDecimal;
import java.util.List;

import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.data.domain.finance.portfolio.SimplePortfolio;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.random.process.GeometricBrownianMotion;
import org.ojalgo.random.process.Process1D;
import org.ojalgo.random.process.RandomProcess;
import org.ojalgo.structure.Access2D;

public class PortfolioSimulator {

    private Process1D<GeometricBrownianMotion> myProcess;

    public PortfolioSimulator(final Access2D<?> correlations, final List<GeometricBrownianMotion> assetProcesses) {

        super();

        if (assetProcesses == null || assetProcesses.size() < 1) {
            throw new IllegalArgumentException();
        }

        if (correlations != null) {
            myProcess = Process1D.of(correlations, assetProcesses);
        } else {
            myProcess = Process1D.of(assetProcesses);
        }
    }

    @SuppressWarnings("unused")
    private PortfolioSimulator() {
        super();
    }

    public RandomProcess.SimulationResults simulate(final int aNumberOfRealisations, final int aNumberOfSteps, final double aStepSize) {
        return this.simulate(aNumberOfRealisations, aNumberOfSteps, aStepSize, null);
    }

    public RandomProcess.SimulationResults simulate(final int aNumberOfRealisations, final int aNumberOfSteps, final double aStepSize,
            final int rebalancingInterval) {
        return this.simulate(aNumberOfRealisations, aNumberOfSteps, aStepSize, Integer.valueOf(rebalancingInterval));
    }

    RandomProcess.SimulationResults simulate(final int aNumberOfRealisations, final int aNumberOfSteps, final double aStepSize,
            final Integer rebalancingInterval) {

        int tmpProcDim = myProcess.size();

        ArrayR064 tmpInitialValues = myProcess.getValues();
        Comparable<?>[] tmpValues = new Comparable<?>[tmpProcDim];
        for (int p = 0; p < tmpProcDim; p++) {
            tmpValues[p] = tmpInitialValues.get(p);
        }
        List<BigDecimal> tmpWeights = new SimplePortfolio(tmpValues).normalise().getWeights();

        Array2D<Double> tmpRealisationValues = Array2D.R064.make(aNumberOfRealisations, aNumberOfSteps);

        for (int r = 0; r < aNumberOfRealisations; r++) {

            for (int s = 0; s < aNumberOfSteps; s++) {

                if (rebalancingInterval != null && s != 0 && s % rebalancingInterval == 0) {

                    double tmpPortfolioValue = tmpRealisationValues.doubleValue(r, s - 1);

                    for (int p = 0; p < tmpProcDim; p++) {
                        myProcess.setValue(p, tmpPortfolioValue * tmpWeights.get(p).doubleValue());
                    }
                }

                Array1D<Double> tmpRealisation = myProcess.step(aStepSize);

                AggregatorFunction<Double> tmpAggregator = Aggregator.SUM.getFunction(PrimitiveAggregator.getSet());
                tmpRealisation.visitAll(tmpAggregator);
                tmpRealisationValues.set(r, s, tmpAggregator.doubleValue());
            }

            myProcess.setValues(tmpInitialValues);
        }

        AggregatorFunction<Double> tmpAggregator = Aggregator.SUM.getFunction(PrimitiveAggregator.getSet());
        for (int i = 0; i < tmpInitialValues.count(); i++) {
            tmpAggregator.invoke(tmpInitialValues.doubleValue(i));
        }

        return new RandomProcess.SimulationResults(tmpAggregator.doubleValue(), tmpRealisationValues);
    }
}
