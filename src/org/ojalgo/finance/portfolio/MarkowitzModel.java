/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import static org.ojalgo.constant.BigMath.*;

import java.math.BigDecimal;
import java.util.HashMap;

import org.ojalgo.access.Access1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * The Markowitz model, in this class, is defined as:
 * </p>
 * <p>
 * min (RAF/2) [w]<sup>T</sup>[C][w] - [w]<sup>T</sup>[r] <br>
 * subject to |[w]| = 1
 * </p>
 * <p>
 * RAF stands for Risk Aversion Factor. Instead of specifying a desired risk or return level you specify a
 * level of risk aversion that is used to balance the risk and return.
 * </p>
 * <p>
 * The expected returns for each of the assets must be excess returns. Otherwise this formulation is wrong.
 * </p>
 * <p>
 * The total weights of all assets will always be 100%, but shorting can be allowed or not according to your
 * preference. ( {@linkplain #setShortingAllowed(boolean)} ) In addition you may set lower and upper limits on
 * any individual asset. ( {@linkplain #setLowerLimit(int, BigDecimal)} and
 * {@linkplain #setUpperLimit(int, BigDecimal)} )
 * </p>
 * <p>
 * Risk-free asset: That means there is no excess return and zero variance. Don't (try to) include a risk-free
 * asset here.
 * </p>
 * <p>
 * Do not worry about the minus sign in front of the return part of the objective function - it is
 * handled/negated for you. When you're asked to supply the expected excess returns you should supply
 * precisely that.
 * </p>
 * <p>
 * Basic usage instructions
 * </p>
 * After you've instantiated the MarkowitzModel you need to do one of three different things:
 * <ol>
 * <li>{@link #setRiskAversion(Number)} unless this was already set in the {@link MarketEquilibrium} or
 * {@link FinancePortfolio.Context} used to instantiate the MarkowitzModel</li>
 * <li>{@link #setTargetReturn(BigDecimal)}</li>
 * <li>{@link #setTargetVariance(BigDecimal)}</li>
 * </ol>
 * <p>
 * Optionally you may {@linkplain #setLowerLimit(int, BigDecimal)},
 * {@linkplain #setUpperLimit(int, BigDecimal)} or {@linkplain #setShortingAllowed(boolean)}.
 * </p>
 * <p>
 * To get the optimal asset weighs you simply call {@link #getWeights()} or {@link #getAssetWeights()}.
 * </p>
 * <p>
 * If the results are not what you expect the first thing you should try is to turn on optimisation model
 * validation: <code>model.optimisation().validate(true);</code>
 * </p>
 *
 * @author apete
 */
public final class MarkowitzModel extends OptimisedPortfolio {

    private static final double _0_0 = ZERO.doubleValue();
    private static final double INIT = PrimitiveFunction.SQRT.invoke(PrimitiveMath.TEN);
    private static final double MAX = PrimitiveMath.HUNDRED * PrimitiveMath.HUNDRED;
    private static final double MIN = PrimitiveMath.HUNDREDTH;
    private static final NumberContext TARGET_CONTEXT = NumberContext.getGeneral(7, 14);

    private final HashMap<int[], LowerUpper> myConstraints = new HashMap<>();
    private transient ExpressionsBasedModel myOptimisationModel;
    private BigDecimal myTargetReturn;
    private BigDecimal myTargetVariance;

    public MarkowitzModel(final BasicMatrix covarianceMatrix, final BasicMatrix expectedExcessReturns) {
        super(covarianceMatrix, expectedExcessReturns);
    }

    public MarkowitzModel(final FinancePortfolio.Context portfolioContext) {
        super(portfolioContext);
    }

    public MarkowitzModel(final MarketEquilibrium marketEquilibrium, final BasicMatrix expectedExcessReturns) {
        super(marketEquilibrium, expectedExcessReturns);
    }

    /**
     * Will add a constraint on the sum of the asset weights specified by the asset indices. Either (but not
     * both) of the limits may be null.
     */
    public LowerUpper addConstraint(final BigDecimal lowerLimit, final BigDecimal upperLimit, final int... assetIndeces) {
        return myConstraints.put(assetIndeces, new LowerUpper(lowerLimit, upperLimit));
    }

    public final void clearAllConstraints() {
        myConstraints.clear();
        this.reset();
    }

    public final void setLowerLimit(final int assetIndex, final BigDecimal lowerLimit) {
        this.getVariable(assetIndex).lower(lowerLimit);
        this.reset();
    }

    /**
     * <p>
     * Will set the target return to whatever you input and the target variance to <code>null</code>.
     * </p>
     * <p>
     * Setting the target return implies that you disregard the risk aversion factor and want the minimum risk
     * portfolio with return that is equal to or as close to the target as possible.
     * </p>
     * <p>
     * There is a performance penalty for setting a target return as the underlying optimisation model has to
     * be solved several (many) times with different pararmeters (different risk aversion factors).
     * </p>
     * <p>
     * Setting a target return (or variance) is not recommnded. It's much better to simply modify the risk
     * aversion factor.
     * </p>
     *
     * @see #setTargetVariance(BigDecimal)
     */
    public final void setTargetReturn(final BigDecimal targetReturn) {
        myTargetReturn = targetReturn;
        myTargetVariance = null;
        this.reset();
    }

    /**
     * <p>
     * Will set the target variance to whatever you input and the target return to <code>null</code>.
     * </p>
     * <p>
     * Setting the target variance implies that you disregard the risk aversion factor and want the maximum
     * return portfolio with risk that is equal to or as close to the target as possible.
     * </p>
     * <p>
     * There is a performance penalty for setting a target variance as the underlying optimisation model has
     * to be solved several (many) times with different pararmeters (different risk aversion factors).
     * </p>
     * <p>
     * Setting a target variance is not recommnded. It's much better to modify the risk aversion factor.
     * </p>
     *
     * @see #setTargetReturn(BigDecimal)
     */
    public final void setTargetVariance(final BigDecimal targetVariance) {
        myTargetVariance = targetVariance;
        myTargetReturn = null;
        this.reset();
    }

    public final void setUpperLimit(final int assetIndex, final BigDecimal upperLimit) {
        this.getVariable(assetIndex).upper(upperLimit);
        this.reset();
    }

    @Override
    public String toString() {

        if (myOptimisationModel == null) {
            this.calculateAssetWeights();
        }

        return myOptimisationModel.toString();
    }

    private ExpressionsBasedModel generateOptimisationModel(final double riskAversion) {

        if (myOptimisationModel == null) {
            myOptimisationModel = this.makeModel(myConstraints);
        }

        myOptimisationModel.getExpression(VARIANCE).weight(riskAversion / 2.0);

        return myOptimisationModel;
    }

    /**
     * Constrained optimisation.
     */
    @Override
    protected BasicMatrix calculateAssetWeights() {

        if (this.getOptimisationOptions().debug_appender != null) {
            BasicLogger.debug();
            BasicLogger.debug("###################################################");
            BasicLogger.debug("BEGIN RAF: {} MarkowitzModel optimisation", this.getRiskAversion());
            BasicLogger.debug("###################################################");
            BasicLogger.debug();
        }

        Optimisation.Result tmpResult;

        if ((myTargetReturn != null) || (myTargetVariance != null)) {

            final double tmpTargetValue;
            if (myTargetVariance != null) {
                tmpTargetValue = myTargetVariance.doubleValue();
            } else if (myTargetReturn != null) {
                tmpTargetValue = myTargetReturn.doubleValue();
            } else {
                tmpTargetValue = _0_0;
            }

            tmpResult = this.generateOptimisationModel(_0_0).minimise();

            double tmpTargetNow = _0_0;
            double tmpTargetDiff = _0_0;
            double tmpTargetLast = _0_0;

            if (tmpResult.getState().isFeasible()) {

                double tmpCurrent;
                double tmpLow;
                double tmpHigh;
                if (this.isDefaultRiskAversion()) {
                    tmpCurrent = INIT;
                    tmpLow = MAX;
                    tmpHigh = MIN;
                } else {
                    tmpCurrent = this.getRiskAversion().doubleValue();
                    tmpLow = tmpCurrent * INIT;
                    tmpHigh = tmpCurrent / INIT;
                }

                do {

                    tmpResult = this.generateOptimisationModel(tmpCurrent).minimise();

                    tmpTargetLast = tmpTargetNow;
                    if (myTargetVariance != null) {
                        tmpTargetNow = this.calculatePortfolioVariance(tmpResult).doubleValue();
                    } else if (myTargetReturn != null) {
                        tmpTargetNow = this.calculatePortfolioReturn(tmpResult, this.calculateAssetReturns()).doubleValue();
                    } else {
                        tmpTargetNow = tmpTargetValue;
                    }
                    tmpTargetDiff = tmpTargetNow - tmpTargetValue;

                    if (tmpTargetDiff < _0_0) {
                        tmpLow = tmpCurrent;
                    } else if (tmpTargetDiff > _0_0) {
                        tmpHigh = tmpCurrent;
                    }
                    tmpCurrent = PrimitiveFunction.SQRT.invoke(tmpLow * tmpHigh);

                    if (this.getOptimisationOptions().debug_appender != null) {
                        BasicLogger.debug();
                        BasicLogger.debug("RAF:   {}", tmpCurrent);
                        BasicLogger.debug("Last: {}", tmpTargetLast);
                        BasicLogger.debug("Now: {}", tmpTargetNow);
                        BasicLogger.debug("Target: {}", tmpTargetValue);
                        BasicLogger.debug("Diff:   {}", tmpTargetDiff);
                    }

                } while (!TARGET_CONTEXT.isSmall(tmpTargetValue, tmpTargetDiff) && TARGET_CONTEXT.isDifferent(tmpTargetLast, tmpTargetNow));
            }

        } else {

            tmpResult = this.generateOptimisationModel(this.getRiskAversion().doubleValue()).minimise();

        }

        return this.handle(tmpResult);
    }

    @Override
    protected void reset() {

        super.reset();

        myOptimisationModel = null;

    }

    final Scalar<?> calculatePortfolioReturn(final Access1D<?> weightsVctr, final BasicMatrix returnsVctr) {
        return super.calculatePortfolioReturn(MATRIX_FACTORY.columns(weightsVctr), returnsVctr);
    }

    final Scalar<?> calculatePortfolioVariance(final Access1D<?> weightsVctr) {
        return super.calculatePortfolioVariance(MATRIX_FACTORY.columns(weightsVctr));
    }

}
