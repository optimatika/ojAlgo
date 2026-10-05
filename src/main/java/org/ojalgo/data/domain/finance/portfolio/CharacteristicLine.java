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

import static org.ojalgo.function.constant.PrimitiveMath.SQRT;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.math.BigDecimal;
import java.util.List;

import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.random.SampleSet;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.TypeUtils;

/**
 * The (security) characteristic line of one asset against the market: r<sub>a</sub> = alpha + beta
 * r<sub>M</sub> + epsilon, where r<sub>a</sub> and r<sub>M</sub> are the (excess) returns of the asset and
 * the market, and epsilon is the asset specific (residual) part of the return - uncorrelated with the market.
 * <p>
 * There are 2 sets of factory methods:
 * <ol>
 * <li>Assuming CAPM - the asset is correctly priced (alpha is 0), and beta is implied by the expected
 * returns: {@link #assumingCAPM(FinancePortfolio, FinancePortfolio)}
 * <li>Not assuming CAPM - alpha and beta are derived from the covariances of a portfolio model,
 * {@link #of(FinancePortfolio.Context, FinancePortfolio, int)}, or estimated from samples,
 * {@link #estimate(Access1D, Access1D)}.
 * </ol>
 */
public final class CharacteristicLine {

    /**
     * Assuming CAPM the asset is correctly priced, alpha is 0, and beta = E[r<sub>a</sub>] /
     * E[r<sub>M</sub>]. Only the expected returns and volatilities of the asset and the market are used.
     *
     * @throws IllegalArgumentException if the asset's variance is less than the part explained by the market
     *                                  (beta<sup>2</sup> Var[r<sub>M</sub>]) - the inputs are not consistent
     *                                  with CAPM
     */
    public static CharacteristicLine assumingCAPM(final FinancePortfolio market, final FinancePortfolio asset) {

        double beta = asset.getMeanReturn() / market.getMeanReturn();
        double marketVariance = market.getReturnVariance();

        double residualVariance = CharacteristicLine.toResidualVariance(asset.getReturnVariance(), beta * beta * marketVariance);

        return new CharacteristicLine(ZERO, beta, marketVariance, residualVariance);
    }

    /**
     * Least squares fit to paired samples of (excess) returns - the asset's and the market's returns over the
     * same periods.
     * <p>
     * beta = Cov[r<sub>a</sub>, r<sub>M</sub>] / Var[r<sub>M</sub>] and alpha = mean(r<sub>a</sub>) - beta
     * mean(r<sub>M</sub>)
     */
    public static CharacteristicLine estimate(final Access1D<?> marketReturns, final Access1D<?> assetReturns) {

        if (marketReturns.count() != assetReturns.count() || marketReturns.count() < 2L) {
            throw new IllegalArgumentException("Need (at least 2) paired samples!");
        }

        SampleSet market = SampleSet.wrap(marketReturns);
        SampleSet asset = SampleSet.wrap(assetReturns);

        double marketVariance = market.getVariance();
        double covariance = asset.getCovariance(market);

        double beta = covariance / marketVariance;
        double alpha = asset.getMean() - beta * market.getMean();

        double residualVariance = CharacteristicLine.toResidualVariance(asset.getVariance(), beta * covariance);

        return new CharacteristicLine(alpha, beta, marketVariance, residualVariance);
    }

    /**
     * The line implied by a portfolio model - its asset returns and covariances - and the market portfolio
     * weights (normalised to sum to 1).
     * <p>
     * beta = Cov[r<sub>a</sub>, r<sub>M</sub>] / Var[r<sub>M</sub>] and alpha = E[r<sub>a</sub>] - beta
     * E[r<sub>M</sub>]
     * <p>
     * Weighted by the market portfolio, the betas average to 1 and the alphas to 0. If the model's asset
     * returns are the equilibrium returns of the market portfolio, all alphas are 0.
     *
     * @param context The asset returns and covariances
     * @param market  The market portfolio weights
     * @param asset   The index of the asset
     */
    public static CharacteristicLine of(final FinancePortfolio.Context context, final FinancePortfolio market, final int asset) {

        List<BigDecimal> weights = market.getWeights();

        if (weights.size() != context.size()) {
            throw new IllegalArgumentException("The market portfolio and the context must have the same number of assets!");
        }

        BigDecimal total = weights.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.signum() == 0) {
            throw new IllegalArgumentException("The market portfolio weights sum to 0!");
        }
        MatrixR064 marketWeights = FinancePortfolio.MATRIX_FACTORY.column(weights).divide(total.doubleValue());

        MatrixR064 assetReturns = context.getAssetReturns();
        MatrixR064 covariances = context.getCovariances();

        MatrixR064 covariancesWithMarket = covariances.multiply(marketWeights);

        double marketVariance = marketWeights.dot(covariancesWithMarket);
        double marketReturn = marketWeights.dot(assetReturns);

        double beta = covariancesWithMarket.doubleValue(asset) / marketVariance;
        double alpha = assetReturns.doubleValue(asset) - beta * marketReturn;

        double residualVariance = CharacteristicLine.toResidualVariance(covariances.doubleValue(asset, asset), beta * beta * marketVariance);

        return new CharacteristicLine(alpha, beta, marketVariance, residualVariance);
    }

    /**
     * The part of the asset's variance not explained by the market. Small negative values (rounding errors)
     * are set to 0.
     */
    private static double toResidualVariance(final double assetVariance, final double explainedVariance) {

        double retVal = assetVariance - explainedVariance;

        if (retVal < ZERO) {
            if (!PrimitiveScalar.isSmall(assetVariance, retVal)) {
                throw new IllegalArgumentException(
                        "The asset's variance (" + assetVariance + ") is less than the part explained by the market (" + explainedVariance + ")!");
            }
            retVal = ZERO;
        }

        return retVal;
    }

    private final double myAlpha;
    private final double myBeta;
    private final double myMarketVariance;
    private final double myResidualVariance;

    CharacteristicLine(final double alpha, final double beta, final double marketVariance, final double residualVariance) {

        super();

        myAlpha = alpha;
        myBeta = beta;
        myMarketVariance = marketVariance;
        myResidualVariance = residualVariance;
    }

    public double getAlpha() {
        return myAlpha;
    }

    public double getBeta() {
        return myBeta;
    }

    /**
     * The correlation between the asset's and the market's returns.
     */
    public double getCorrelation() {
        double explainedVariance = myBeta * myBeta * myMarketVariance;
        return Math.signum(myBeta) * SQRT.invoke(explainedVariance / (explainedVariance + myResidualVariance));
    }

    /**
     * The covariance between the asset's and the market's returns.
     */
    public double getCovariance() {
        return myBeta * myMarketVariance;
    }

    /**
     * The standard deviation of epsilon - the asset specific risk.
     */
    public double getResidualVolatility() {
        return SQRT.invoke(myResidualVariance);
    }

    @Override
    public String toString() {
        return TypeUtils.format("alpha={}, beta={}, residual volatility={}", myAlpha, myBeta, this.getResidualVolatility());
    }

}
