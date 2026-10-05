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
package org.ojalgo.data.domain.finance.portfolio;

import static org.ojalgo.function.constant.PrimitiveMath.ONE;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.math.BigDecimal;
import java.util.List;

import org.ojalgo.data.domain.finance.FinanceUtils;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.random.process.GeometricBrownianMotion;
import org.ojalgo.type.StandardType;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * A FinancePortfolio is primarily a set of portfolio asset weights.
 * <p>
 * The mean return, return variance and volatility refer to the return over one time unit - whatever unit
 * (year, month...) the inputs are expressed in.
 * <p>
 * The loss probability and Value at Risk are calculated using {@link #forecast()}.
 *
 * @author apete
 */
public abstract class FinancePortfolio implements Comparable<FinancePortfolio> {

    /**
     * The asset returns, volatilities, correlations and covariances all refer to the returns over one time
     * unit. Growth rate statistics, e.g. from {@link FinanceUtils#makeCovarianceMatrix}, can be converted
     * using {@link FinanceUtils#toExpectedReturnsFromGrowthRates} and
     * {@link FinanceUtils#toCovariancesFromGrowthRates}.
     */
    public interface Context {

        double calculatePortfolioReturn(final FinancePortfolio weightsPortfolio);

        double calculatePortfolioVariance(final FinancePortfolio weightsPortfolio);

        MatrixR064 getAssetReturns();

        MatrixR064 getAssetVolatilities();

        MatrixR064 getCorrelations();

        MatrixR064 getCovariances();

        int size();

    }

    protected static final MatrixR064.Factory MATRIX_FACTORY = MatrixR064.FACTORY;

    protected FinancePortfolio() {
        super();
    }

    /**
     * Compares the Sharpe ratios.
     */
    @Override
    public final int compareTo(final FinancePortfolio reference) {
        return NumberContext.compare(this.getSharpeRatio(), reference.getSharpeRatio());
    }

    /**
     * A geometric Brownian motion, with initial value 1.0, matched to the mean return and return variance: At
     * time 1.0 its expected value is 1.0 + mean return, and its variance is the return variance.
     */
    public final GeometricBrownianMotion forecast() {

        final double tmpInitialValue = ONE;
        final double tmpExpectedValue = ONE + this.getMeanReturn();
        final double tmpValueVariance = this.getReturnVariance();
        final double tmpHorizon = ONE;

        return GeometricBrownianMotion.make(tmpInitialValue, tmpExpectedValue, tmpValueVariance, tmpHorizon);
    }

    /**
     * The cosine similarity of the weights: 1.0 means the same relative weights, 0.0 that there's no overlap.
     */
    public final double getConformance(final FinancePortfolio reference) {

        final MatrixR064 tmpMyWeights = MATRIX_FACTORY.column(this.getWeights());
        final MatrixR064 tmpRefWeights = MATRIX_FACTORY.column(reference.getWeights());

        final double tmpNumerator = tmpMyWeights.dot(tmpRefWeights);
        final double tmpDenom1 = PrimitiveMath.SQRT.invoke(tmpMyWeights.dot(tmpMyWeights));
        final double tmpDenom2 = PrimitiveMath.SQRT.invoke(tmpRefWeights.dot(tmpRefWeights));

        return tmpNumerator / (tmpDenom1 * tmpDenom2);
    }

    public final double getLossProbability() {
        return this.getLossProbability(ONE);
    }

    /**
     * The probability that the value, after the given time period, is less than the initial value - using the
     * {@link #forecast()} model.
     */
    public final double getLossProbability(final Number timePeriod) {

        final GeometricBrownianMotion tmpProc = this.forecast();

        final double tmpDoubleValue = timePeriod.doubleValue();
        final double tmpValue = tmpProc.getValue();

        return tmpProc.getDistribution(tmpDoubleValue).getDistribution(tmpValue);
    }

    /**
     * The mean/expected return of this portfolio. May be either the absolute or the excess return - the
     * context in which an instance is used should make it clear which.
     */
    public abstract double getMeanReturn();

    /**
     * The return variance. Subclasses must override either {@linkplain #getReturnVariance()} or
     * {@linkplain #getVolatility()}.
     */
    public double getReturnVariance() {
        final double tmpVolatility = this.getVolatility();
        return tmpVolatility * tmpVolatility;
    }

    /**
     * (mean return - risk-free return) / volatility. Without a risk-free return, the mean return is assumed
     * to already be an excess return.
     */
    public final double getSharpeRatio() {
        return this.getSharpeRatio(null);
    }

    public final double getSharpeRatio(final Number riskFreeReturn) {
        if (riskFreeReturn != null) {
            return (this.getMeanReturn() - riskFreeReturn.doubleValue()) / this.getVolatility();
        }
        return this.getMeanReturn() / this.getVolatility();
    }

    /**
     * Value at Risk (VaR) is the maximum loss not exceeded with a given probability defined as the confidence
     * level, over a given period of time. It is expressed as a fraction of the initial value, and calculated
     * using the same (geometric Brownian motion) model as {@link #forecast()} and
     * {@link #getLossProbability(Number)}.
     */
    public final double getValueAtRisk(final Number confidenceLevel, final Number timePeriod) {

        GeometricBrownianMotion process = this.forecast();

        double quantile = process.getDistribution(timePeriod.doubleValue()).getQuantile(ONE - confidenceLevel.doubleValue());

        return PrimitiveMath.MAX.invoke(process.getValue() - quantile, ZERO);
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
        return PrimitiveMath.SQRT.invoke(this.getReturnVariance());
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
        return new NormalisedPortfolio(this, StandardType.PERCENT);
    }

    /**
     * Normalised weights Portfolio
     */
    public final FinancePortfolio normalise(final NumberContext weightsContext) {
        return new NormalisedPortfolio(this, weightsContext);
    }

    @Override
    public String toString() {
        return TypeUtils.format("{}: Return={}, Variance={}, Volatility={}, Weights={}", this.getClass().getSimpleName(), this.getMeanReturn(),
                this.getReturnVariance(), this.getVolatility(), this.getWeights());
    }

    protected abstract void reset();

}
