/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
package org.ojalgo.finance.portfolio;

import static org.ojalgo.constant.PrimitiveMath.*;

import java.math.BigDecimal;
import java.util.List;

import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.BasicMatrix.Factory;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.random.RandomUtils;
import org.ojalgo.random.process.GeometricBrownianMotion;
import org.ojalgo.type.StandardType;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * A FinancePortfolio is primarily a set of portfolio asset weights.
 *
 * @author apete
 */
public abstract class FinancePortfolio implements Comparable<FinancePortfolio> {

    public static interface Context {

        double calculatePortfolioReturn(final FinancePortfolio weightsPortfolio);

        double calculatePortfolioVariance(final FinancePortfolio weightsPortfolio);

        BasicMatrix getAssetReturns();

        BasicMatrix getAssetVolatilities();

        BasicMatrix getCorrelations();

        BasicMatrix getCovariances();

        int size();

    }

    protected static final Factory<PrimitiveMatrix> MATRIX_FACTORY = PrimitiveMatrix.FACTORY;

    protected static final NumberContext WEIGHT_CONTEXT = StandardType.PERCENT;

    protected FinancePortfolio() {
        super();
    }

    public final int compareTo(final FinancePortfolio reference) {
        return Double.compare(this.getSharpeRatio(), reference.getSharpeRatio());
    }

    public final GeometricBrownianMotion forecast() {

        final double tmpInitialValue = ONE;
        final double tmpExpectedValue = ONE + this.getMeanReturn();
        final double tmpValueVariance = this.getReturnVariance();
        final double tmpHorizon = ONE;

        return GeometricBrownianMotion.make(tmpInitialValue, tmpExpectedValue, tmpValueVariance, tmpHorizon);
    }

    public final double getConformance(final FinancePortfolio reference) {

        final BasicMatrix tmpMyWeights = MATRIX_FACTORY.columns(this.getWeights());
        final BasicMatrix tmpRefWeights = MATRIX_FACTORY.columns(reference.getWeights());

        final double tmpNumerator = tmpMyWeights.multiplyVectors(tmpRefWeights).doubleValue();
        final double tmpDenom1 = Math.sqrt(tmpMyWeights.multiplyVectors(tmpMyWeights).doubleValue());
        final double tmpDenom2 = Math.sqrt(tmpRefWeights.multiplyVectors(tmpRefWeights).doubleValue());

        return tmpNumerator / (tmpDenom1 * tmpDenom2);
    }

    public final double getLossProbability() {
        return this.getLossProbability(ONE);
    }

    public final double getLossProbability(final Number timePeriod) {

        final GeometricBrownianMotion tmpProc = this.forecast();

        final double tmpDoubleValue = timePeriod.doubleValue();
        final double tmpValue = tmpProc.getValue();

        return tmpProc.getDistribution(tmpDoubleValue).getDistribution(tmpValue);
    }

    /**
     * The mean/expected return of this instrument. May return either the absolute or excess return of the
     * instrument. The context in which an instance is used should make it clear which. return.
     */
    public abstract double getMeanReturn();

    /**
     * The instrument's return variance. Subclasses must override either {@linkplain #getReturnVariance()} or
     * {@linkplain #getVolatility()}.
     */
    public double getReturnVariance() {
        final double tmpVolatility = this.getVolatility();
        return tmpVolatility * tmpVolatility;
    }

    public final double getSharpeRatio() {
        return this.getSharpeRatio(null);
    }

    public final double getSharpeRatio(final Number riskFreeReturn) {
        if (riskFreeReturn != null) {
            return (this.getMeanReturn() - riskFreeReturn.doubleValue()) / this.getVolatility();
        } else {
            return this.getMeanReturn() / this.getVolatility();
        }
    }

    /**
     * Value at Risk (VaR) is the maximum loss not exceeded with a given probability defined as the confidence
     * level, over a given period of time.
     */
    public final double getValueAtRisk(final Number confidenceLevel, final Number timePeriod) {

        final double aReturn = this.getMeanReturn();
        final double aStdDev = this.getVolatility();

        final double tmpConfidenceScale = SQRT_TWO * RandomUtils.erfi(ONE - (TWO * (ONE - confidenceLevel.doubleValue())));
        final double tmpTimePeriod = timePeriod.doubleValue();

        return Math.max((Math.sqrt(tmpTimePeriod) * aStdDev * tmpConfidenceScale) - (tmpTimePeriod * aReturn), ZERO);
    }

    public final double getValueAtRisk95() {
        return this.getValueAtRisk(0.95, ONE);
    }

    /**
     * Volatility refers to the standard deviation of the change in value of an asset with a specific time
     * horizon. It is often used to quantify the risk of the asset over that time period. Subclasses must
     * override either {@linkplain #getReturnVariance()} or {@linkplain #getVolatility()}.
     */
    public double getVolatility() {
        return PrimitiveFunction.SQRT.invoke(this.getReturnVariance());
    }

    /**
     * This method returns a list of the weights of the Portfolio's contained assets. An asset weight is NOT
     * restricted to being a share/percentage - it can be anything. Most subclasses do however assume that the
     * list of asset weights are shares/percentages that sum up to 100%. Calling {@linkplain #normalise()}
     * will transform any set of weights to that form.
     */
    public abstract List<BigDecimal> getWeights();

    /**
     * Normalised weights Portfolio
     */
    public final FinancePortfolio normalise() {
        return new NormalisedPortfolio(this);
    }

    @Override
    public String toString() {
        return TypeUtils.format("{}: Return={}, Variance={}, Volatility={}, Weights={}", this.getClass().getSimpleName(), this.getMeanReturn(),
                this.getReturnVariance(), this.getVolatility(), this.getWeights());
    }

    protected abstract void reset();

}
