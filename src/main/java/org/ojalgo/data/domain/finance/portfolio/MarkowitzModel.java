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

import static org.ojalgo.function.constant.BigMath.HALF;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.context.NumberContext;

/**
 * The Markowitz model, in this class, is defined as:
 * <p>
 * min (RAF/2) [w]<sup>T</sup>[C][w] - [w]<sup>T</sup>[r] <br>
 * subject to the weights summing to 1 (100%)
 * <p>
 * RAF stands for Risk Aversion Factor - it balances risk and return. The expected returns, [r], must be
 * excess returns. Don't include a risk-free asset (no excess return and zero variance).
 * <p>
 * Shorting can be allowed or not, {@link #setShortingAllowed(boolean)}, and there can be limits on individual
 * assets, {@link #setLowerLimit(int, BigDecimal)} and {@link #setUpperLimit(int, BigDecimal)}, or on groups
 * of assets, {@link #addConstraint(BigDecimal, BigDecimal, int...)}.
 * <p>
 * Do one of:
 * <ol>
 * <li>{@link #setRiskAversion(Comparable)} - or use the risk aversion of the {@link MarketEquilibrium}
 * <li>{@link #setTargetReturn(BigDecimal)}
 * <li>{@link #setTargetVariance(BigDecimal)}
 * </ol>
 * and then call {@link #getWeights()} or {@link #getAssetWeights()}.
 * <p>
 * Check {@code model.optimiser().getState()} - if no usable solution was found, all weights are zero. If the
 * results are not what you expect, turn on optimisation model validation:
 * {@code model.optimiser().validate(true);}
 *
 * @author apete
 */
public final class MarkowitzModel extends OptimisedPortfolio {

    /**
     * When searching for a risk aversion factor that gives the target variance, the bracketing interval is
     * expanded by a factor 10 at most this many times (in either direction).
     */
    private static final int MAX_EXPANSIONS = 20;
    private static final NumberContext TARGET_CONTEXT = NumberContext.of(5, 4);

    /**
     * The covariances and (expected excess) returns from the context, and the default risk aversion (1.0).
     */
    public static MarkowitzModel of(final FinancePortfolio.Context portfolioContext) {
        return new MarkowitzModel(MarketEquilibrium.of(portfolioContext.getCovariances()), portfolioContext.getAssetReturns());
    }

    /**
     * @param marketEquilibrium     The covariances and risk aversion
     * @param expectedExcessReturns The expected excess returns
     */
    public static MarkowitzModel of(final MarketEquilibrium marketEquilibrium, final MatrixR064 expectedExcessReturns) {
        return new MarkowitzModel(marketEquilibrium, expectedExcessReturns);
    }

    /**
     * With the default risk aversion (1.0).
     */
    public static MarkowitzModel of(final Access2D<?> covariances, final MatrixR064 expectedExcessReturns) {
        return new MarkowitzModel(MarketEquilibrium.of(covariances), expectedExcessReturns);
    }

    private final Map<List<Integer>, LowerUpper> myConstraints = new LinkedHashMap<>();
    private BigDecimal myTargetReturn;
    private BigDecimal myTargetVariance;

    /**
     * @deprecated v57 Use {@link #of(FinancePortfolio.Context)} instead.
     */
    @Deprecated
    public MarkowitzModel(final FinancePortfolio.Context portfolioContext) {
        this(MarketEquilibrium.of(portfolioContext.getCovariances()), portfolioContext.getAssetReturns());
    }

    /**
     * @deprecated v57 Use {@link #of(MarketEquilibrium, MatrixR064)} instead. This constructor will become
     *             package-private.
     */
    @Deprecated
    public MarkowitzModel(final MarketEquilibrium marketEquilibrium, final MatrixR064 expectedExcessReturns) {
        super(marketEquilibrium, expectedExcessReturns);
    }

    /**
     * @deprecated v57 Use {@link #of(Access2D, MatrixR064)} instead.
     */
    @Deprecated
    public MarkowitzModel(final MatrixR064 covarianceMatrix, final MatrixR064 expectedExcessReturns) {
        this(MarketEquilibrium.of(covarianceMatrix), expectedExcessReturns);
    }

    /**
     * Will add a constraint on the sum of the asset weights specified by the asset indices. Either (but not
     * both) of the limits may be null. A constraint on the same set of assets replaces any previous one.
     */
    public void addConstraint(final BigDecimal lowerLimit, final BigDecimal upperLimit, final int... assetIndices) {
        myConstraints.put(LowerUpper.key(assetIndices), new LowerUpper(lowerLimit, upperLimit));
        this.reset();
    }

    public void clearAllConstraints() {
        myConstraints.clear();
        this.reset();
    }

    public void setLowerLimit(final int assetIndex, final BigDecimal lowerLimit) {
        this.getVariable(assetIndex).lower = lowerLimit;
        this.reset();
    }

    /**
     * Will set the target return to whatever you input and the target variance to {@code null}.
     * <p>
     * Setting the target return implies that you disregard the risk aversion factor and want the minimum
     * variance portfolio with a return of at least the target. That is a single (quadratic) optimisation
     * problem.
     * <ul>
     * <li>If the target is below the return of the minimum variance portfolio, you get the minimum variance
     * portfolio - its return is higher than the target.
     * <li>If the target is above the maximum attainable return, you get the maximum return portfolio.
     * </ul>
     *
     * @see #setTargetVariance(BigDecimal)
     */
    public void setTargetReturn(final BigDecimal targetReturn) {
        myTargetReturn = targetReturn;
        myTargetVariance = null;
        this.reset();
    }

    /**
     * Will set the target variance to whatever you input and the target return to {@code null}.
     * <p>
     * Setting the target variance implies that you disregard the risk aversion factor and want the maximum
     * return portfolio with variance (approximately) equal to the target.
     * <ul>
     * <li>If the target is below the variance of the minimum variance portfolio, you get the minimum variance
     * portfolio.
     * <li>If the target is above the variance of the maximum return portfolio, you get the maximum return
     * portfolio.
     * </ul>
     * There is a performance penalty for setting a target variance as the underlying optimisation model has
     * to be solved several (many) times with different parameters (different risk aversion factors).
     *
     * @see #setTargetReturn(BigDecimal)
     */
    public void setTargetVariance(final BigDecimal targetVariance) {
        myTargetVariance = targetVariance;
        myTargetReturn = null;
        this.reset();
    }

    public void setUpperLimit(final int assetIndex, final BigDecimal upperLimit) {
        this.getVariable(assetIndex).upper = upperLimit;
        this.reset();
    }

    private Optimisation.Result solveForTargetReturn(final BigDecimal targetReturn) {

        Optimisation.Result retVal = this.solveMinimumVariance(targetReturn);

        if (!retVal.getState().isFeasible()) {
            // Target not attainable - settle for the maximum return portfolio
            retVal = this.solve(myConstraints, PrimitiveMath.ZERO);
        }

        return retVal;
    }

    private Optimisation.Result solveForTargetVariance(final double targetVariance) {

        Optimisation.Result retVal = this.solveMinimumVariance(null);
        if (!retVal.getState().isFeasible() || this.calculatePortfolioVariance(retVal).doubleValue() >= targetVariance) {
            return retVal;
        }

        retVal = this.solve(myConstraints, PrimitiveMath.ZERO);
        if (retVal.getState() != Optimisation.State.UNBOUNDED && this.calculatePortfolioVariance(retVal).doubleValue() <= targetVariance) {
            return retVal;
        }

        // The target is now between the variances of the minimum variance and the maximum return portfolios.
        // The variance decreases monotonically with increasing risk aversion.

        double low = this.getRiskAversion().doubleValue(); // variance(low) >= target
        double high = low; // variance(high) <= target

        retVal = this.solve(myConstraints, low);
        double variance = this.calculatePortfolioVariance(retVal).doubleValue();

        if (variance > targetVariance) {
            for (int i = 0; variance > targetVariance && i < MAX_EXPANSIONS; i++) {
                low = high;
                high *= PrimitiveMath.TEN;
                retVal = this.solve(myConstraints, high);
                variance = this.calculatePortfolioVariance(retVal).doubleValue();
            }
        } else {
            for (int i = 0; variance < targetVariance && i < MAX_EXPANSIONS; i++) {
                high = low;
                low /= PrimitiveMath.TEN;
                retVal = this.solve(myConstraints, low);
                variance = this.calculatePortfolioVariance(retVal).doubleValue();
            }
        }

        while (!TARGET_CONTEXT.isSmall(targetVariance, variance - targetVariance) && TARGET_CONTEXT.isDifferent(PrimitiveMath.ONE, high / low)) {

            double middle = PrimitiveMath.SQRT.invoke(low * high);

            retVal = this.solve(myConstraints, middle);
            variance = this.calculatePortfolioVariance(retVal).doubleValue();

            if (variance > targetVariance) {
                low = middle;
            } else {
                high = middle;
            }
        }

        return retVal;
    }

    /**
     * min (1/2) [w]<sup>T</sup>[C][w] subject to [w]<sup>T</sup>[r] >= minimumReturn (if not null)
     */
    private Optimisation.Result solveMinimumVariance(final BigDecimal minimumReturn) {

        ExpressionsBasedModel model = this.makeModel(myConstraints);

        model.getExpression(VARIANCE).weight(HALF);
        if (minimumReturn != null) {
            model.getExpression(RETURN).lower(minimumReturn);
        }

        return model.minimise();
    }

    /**
     * Constrained optimisation.
     */
    @Override
    protected MatrixR064 calculateAssetWeights() {

        Optimisation.Result result;

        if (myTargetReturn != null) {
            result = this.solveForTargetReturn(myTargetReturn);
        } else if (myTargetVariance != null) {
            result = this.solveForTargetVariance(myTargetVariance.doubleValue());
        } else {
            result = this.solve(myConstraints, this.getRiskAversion().doubleValue());
        }

        return this.handle(result);
    }

    Scalar<?> calculatePortfolioVariance(final Access1D<?> weightsVctr) {
        return super.calculatePortfolioVariance(MATRIX_FACTORY.column(weightsVctr));
    }

}
