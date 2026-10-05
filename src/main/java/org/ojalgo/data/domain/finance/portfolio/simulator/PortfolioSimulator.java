/*
 * Copyright 1997-2026 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.ONE;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.List;

import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.random.process.GeometricBrownianMotion;
import org.ojalgo.random.process.Process1D;
import org.ojalgo.random.process.RandomProcess;
import org.ojalgo.structure.Access2D;

/**
 * Simulates the value of a portfolio of assets modelled as (correlated) geometric Brownian motions, with or
 * without periodic rebalancing.
 */
public class PortfolioSimulator {

    /**
     * @param correlations   The correlations of the processes' growth rates (the Wiener process increments),
     *                       not of the returns. May be null, meaning uncorrelated processes.
     * @param assetProcesses One process per asset, with the initial value set to the amount invested in that
     *                       asset.
     */
    public static PortfolioSimulator of(final Access2D<?> correlations, final List<GeometricBrownianMotion> assetProcesses) {
        return new PortfolioSimulator(PortfolioSimulator.toProcess(correlations, assetProcesses));
    }

    private static Process1D<GeometricBrownianMotion> toProcess(final Access2D<?> correlations, final List<GeometricBrownianMotion> assetProcesses) {

        if (assetProcesses == null || assetProcesses.size() < 1) {
            throw new IllegalArgumentException();
        }

        if (correlations != null) {
            return Process1D.of(correlations, assetProcesses);
        } else {
            return Process1D.of(assetProcesses);
        }
    }

    private final Process1D<GeometricBrownianMotion> myProcess;

    /**
     * @deprecated v57 Use {@link #of(Access2D, List)} instead.
     */
    @Deprecated
    public PortfolioSimulator(final Access2D<?> correlations, final List<GeometricBrownianMotion> assetProcesses) {
        this(PortfolioSimulator.toProcess(correlations, assetProcesses));
    }

    PortfolioSimulator(final Process1D<GeometricBrownianMotion> process) {

        super();

        myProcess = process;
    }

    public RandomProcess.SimulationResults simulate(final int aNumberOfRealisations, final int aNumberOfSteps, final double aStepSize) {
        return this.simulate(aNumberOfRealisations, aNumberOfSteps, aStepSize, null);
    }

    /**
     * @param rebalancingInterval Every this many steps the portfolio is rebalanced to the initial (relative)
     *                            weights.
     */
    public RandomProcess.SimulationResults simulate(final int aNumberOfRealisations, final int aNumberOfSteps, final double aStepSize,
            final int rebalancingInterval) {
        return this.simulate(aNumberOfRealisations, aNumberOfSteps, aStepSize, Integer.valueOf(rebalancingInterval));
    }

    RandomProcess.SimulationResults simulate(final int aNumberOfRealisations, final int aNumberOfSteps, final double aStepSize,
            final Integer rebalancingInterval) {

        int tmpProcDim = myProcess.size();

        ArrayR064 tmpInitialValues = myProcess.getValues();

        double tmpInitialValue = ZERO;
        for (int p = 0; p < tmpProcDim; p++) {
            tmpInitialValue += tmpInitialValues.doubleValue(p);
        }

        double tmpTotal = tmpInitialValue != ZERO ? tmpInitialValue : ONE;
        double[] tmpWeights = new double[tmpProcDim];
        for (int p = 0; p < tmpProcDim; p++) {
            tmpWeights[p] = tmpInitialValues.doubleValue(p) / tmpTotal;
        }

        Array2D<Double> tmpRealisationValues = Array2D.R064.make(aNumberOfRealisations, aNumberOfSteps);

        for (int r = 0; r < aNumberOfRealisations; r++) {

            for (int s = 0; s < aNumberOfSteps; s++) {

                if (rebalancingInterval != null && s != 0 && s % rebalancingInterval == 0) {

                    double tmpPortfolioValue = tmpRealisationValues.doubleValue(r, s - 1);

                    for (int p = 0; p < tmpProcDim; p++) {
                        myProcess.setValue(p, tmpPortfolioValue * tmpWeights[p]);
                    }
                }

                Array1D<Double> tmpRealisation = myProcess.step(aStepSize);

                double tmpPortfolioValue = ZERO;
                for (int p = 0; p < tmpProcDim; p++) {
                    tmpPortfolioValue += tmpRealisation.doubleValue(p);
                }
                tmpRealisationValues.set(r, s, tmpPortfolioValue);
            }

            myProcess.setValues(tmpInitialValues);
        }

        return new RandomProcess.SimulationResults(tmpInitialValue, tmpRealisationValues);
    }
}
