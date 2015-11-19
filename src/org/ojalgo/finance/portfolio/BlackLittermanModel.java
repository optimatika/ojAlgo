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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Access2D.Builder;
import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.scalar.BigScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

public final class BlackLittermanModel extends EquilibriumModel {

    /**
     * View/Forecast/Opinion
     *
     * @author apete
     */
    private static final class View extends FinancePortfolio {

        private BigDecimal myMeanReturn = BigMath.ZERO;
        private final BlackLittermanModel myModel;
        private BigDecimal myReturnVariance = null;
        private BigDecimal myScale = null;
        private final List<BigDecimal> myWeights;

        public View(final BlackLittermanModel aModel, final List<BigDecimal> someWeights) {

            super();

            myModel = aModel;
            myWeights = someWeights;
        }

        @SuppressWarnings("unused")
        private View() {

            super();

            myModel = null;
            myWeights = null;

            ProgrammingError.throwForIllegalInvocation();
        }

        @Override
        public double getMeanReturn() {
            if (myMeanReturn != null) {
                return myMeanReturn.doubleValue();
            } else {
                return PrimitiveMath.ZERO;
            }
        }

        @Override
        public double getReturnVariance() {

            if (myReturnVariance != null) {

                return myReturnVariance.doubleValue();

            } else {

                final BasicMatrix tmpWeights = MATRIX_FACTORY.columns(myWeights);

                BigDecimal retVal = myModel.calculateVariance(tmpWeights);

                if (myScale != null) {

                    retVal = retVal.multiply(myScale);

                } else {

                    retVal = retVal.multiply(myModel.getConfidence().toBigDecimal());
                }

                return retVal.doubleValue();
            }
        }

        @Override
        public List<BigDecimal> getWeights() {
            return myWeights;
        }

        @Override
        protected void reset() {
        }

        protected final void setMeanReturn(final BigDecimal aMeanReturn) {
            myMeanReturn = aMeanReturn;
        }

        protected final void setReturnVariance(final BigDecimal aReturnVariance) {
            myReturnVariance = aReturnVariance;
        }

        protected final void setScale(final BigDecimal aScale) {
            myScale = aScale;
        }

    }

    private BigDecimal myConfidence = BigMath.ONE;
    private final BasicMatrix myOriginalWeights;
    private final List<FinancePortfolio> myViews;

    public BlackLittermanModel(final Context context, final FinancePortfolio originalWeights) {

        super(context);

        myOriginalWeights = MATRIX_FACTORY.columns(originalWeights.getWeights());
        myViews = new ArrayList<FinancePortfolio>();
    }

    /**
     * @param marketEquilibrium The covariance matrix, and market risk aversion
     * @param originalWeights The market portfolio
     */
    public BlackLittermanModel(final MarketEquilibrium marketEquilibrium, final BasicMatrix originalWeights) {

        super(marketEquilibrium);

        myOriginalWeights = originalWeights;
        myViews = new ArrayList<FinancePortfolio>();
    }

    private BlackLittermanModel(final MarketEquilibrium aMarketEquilibrium) {

        super(aMarketEquilibrium);

        myOriginalWeights = null;
        myViews = null;

        ProgrammingError.throwForIllegalInvocation();
    }

    public final void addView(final FinancePortfolio aView) {
        myViews.add(aView);
    }

    public final void addViewWithBalancedConfidence(final List<BigDecimal> someWeights, final Number aReturn) {

        final View tmpView = new View(this, someWeights);

        tmpView.setMeanReturn(TypeUtils.toBigDecimal(aReturn));
        tmpView.setReturnVariance(null);
        tmpView.setScale(null);

        myViews.add(tmpView);
    }

    public final void addViewWithScaledConfidence(final List<BigDecimal> someWeights, final Number aReturn, final Number aScale) {

        final View tmpView = new View(this, someWeights);

        tmpView.setMeanReturn(TypeUtils.toBigDecimal(aReturn));
        tmpView.setReturnVariance(null);
        tmpView.setScale(TypeUtils.toBigDecimal(aScale));

        myViews.add(tmpView);
    }

    /**
     * @deprecated v30
     */
    @Deprecated
    public final void addViewWithStandardDeviation(final List<BigDecimal> someWeights, final BigDecimal aReturn, final BigDecimal aStdDev) {

        final View tmpView = new View(this, someWeights);

        tmpView.setMeanReturn(aReturn);
        tmpView.setReturnVariance(aStdDev.multiply(aStdDev));
        tmpView.setScale(null);

        myViews.add(tmpView);
    }

    /**
     * "weight on views" or "tau" A parameter that describes the general confidence in the views. Typically
     * set to sometghing between 0.0 and 1.0. 0.0 = "No confidence!" Why bother... 1.0 = As confident as the
     * market. This is highly unlikely.
     */
    public final Scalar<?> getConfidence() {
        return BigScalar.of(myConfidence);
    }

    /**
     * @see #getConfidence()
     */
    public final void setConfidence(final Number aWeight) {
        myConfidence = TypeUtils.toBigDecimal(aWeight);
    }

    @Override
    protected BasicMatrix calculateAssetReturns() {
        return this.calculateAssetReturns(this.calculateAssetWeights());
    }

    @Override
    protected BasicMatrix calculateAssetWeights() {

        final BasicMatrix tmpViewPortfolios = this.getViewPortfolios();
        final BasicMatrix tmpViewReturns = this.getViewReturns();
        final BasicMatrix tmpViewVariances = this.getViewVariances();

        final BasicMatrix tmpCovariances = this.getCovariances();

        final BasicMatrix tmpRightParenthesis = tmpViewReturns.subtract(tmpViewPortfolios.multiply(tmpCovariances).multiply(myOriginalWeights));

        final BasicMatrix tmpViewsTransposed = tmpViewPortfolios.transpose();

        final BasicMatrix tmpLeftParenthesis = tmpViewVariances.add(tmpViewPortfolios.multiply(tmpCovariances).multiply(tmpViewsTransposed));

        return myOriginalWeights.add(tmpViewsTransposed.multiply(tmpLeftParenthesis.solve(tmpRightParenthesis)));
    }

    protected final BasicMatrix getOriginalReturns() {
        return this.calculateAssetReturns(myOriginalWeights);
    }

    /**
     * @see org.ojalgo.finance.portfolio.BlackLittermanModel#getOriginalWeights()
     * @see org.ojalgo.finance.portfolio.BlackLittermanModel#getAssetWeights()
     */
    protected final BasicMatrix getOriginalWeights() {
        return myOriginalWeights;
    }

    protected final BasicMatrix getViewPortfolios() {

        final int tmpRowDim = myViews.size();
        final int tmpColDim = (int) myOriginalWeights.count();

        final Access2D.Builder<PrimitiveMatrix> retVal = MATRIX_FACTORY.getBuilder(tmpRowDim, tmpColDim);

        FinancePortfolio tmpView;
        List<BigDecimal> tmpWeights;

        for (int i = 0; i < tmpRowDim; i++) {

            tmpView = myViews.get(i);
            tmpWeights = tmpView.getWeights();

            for (int j = 0; j < tmpColDim; j++) {
                retVal.set(i, j, tmpWeights.get(j));
            }
        }

        return retVal.build();
    }

    /**
     * Scaled by risk aversion factor.
     */
    protected final BasicMatrix getViewReturns() {

        final int tmpRowDim = myViews.size();
        final int tmpColDim = 1;

        final Access2D.Builder<PrimitiveMatrix> retVal = MATRIX_FACTORY.getBuilder(tmpRowDim, tmpColDim);

        double tmpRet;
        final double tmpRAF = this.getRiskAversion().doubleValue();

        for (int i = 0; i < tmpRowDim; i++) {

            tmpRet = myViews.get(i).getMeanReturn();

            retVal.set(i, 0, PrimitiveFunction.DIVIDE.invoke(tmpRet, tmpRAF));
        }

        return retVal.build();
    }

    protected final List<FinancePortfolio> getViews() {
        return myViews;
    }

    /**
     * Scaled by tau / weight on views
     */
    protected final BasicMatrix getViewVariances() {

        final int tmpDim = myViews.size();

        final Builder<PrimitiveMatrix> retVal = MATRIX_FACTORY.getBuilder(tmpDim, tmpDim);

        if (myConfidence.compareTo(BigMath.ONE) == 0) {

            for (int ij = 0; ij < tmpDim; ij++) {
                retVal.set(ij, ij, myViews.get(ij).getReturnVariance());
            }

        } else {

            final double tmpScale = myConfidence.doubleValue();

            double tmpVar;
            for (int ij = 0; ij < tmpDim; ij++) {

                tmpVar = myViews.get(ij).getReturnVariance();

                retVal.set(ij, ij, PrimitiveFunction.DIVIDE.invoke(tmpVar, tmpScale));
            }
        }

        return retVal.build();
    }

    BigDecimal calculateVariance(final BasicMatrix aWeightsMtrx) {

        BasicMatrix tmpVal = this.getCovariances();

        tmpVal = tmpVal.multiply(aWeightsMtrx);

        return tmpVal.multiplyLeft(aWeightsMtrx.transpose()).toBigDecimal(0, 0);
    }
}
