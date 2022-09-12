/*
 * Copyright 1997-2022 Optimatika
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.ojalgo.array.Array1D;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.Primitive64Matrix;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

abstract class EquilibriumModel extends FinancePortfolio implements FinancePortfolio.Context {

    private transient Primitive64Matrix myAssetReturns;
    private transient Primitive64Matrix myAssetVolatilities;
    private transient Primitive64Matrix myAssetWeights;
    private final MarketEquilibrium myMarketEquilibrium;
    private transient Scalar<?> myMeanReturn;
    private transient Scalar<?> myReturnVariance;

    protected EquilibriumModel(final FinancePortfolio.Context portfolioContext) {

        super();

        myMarketEquilibrium = new MarketEquilibrium(portfolioContext.getCovariances());
    }

    protected EquilibriumModel(final MarketEquilibrium marketEquilibrium) {

        super();

        myMarketEquilibrium = marketEquilibrium.copy();
    }

    public final double calculatePortfolioReturn(final FinancePortfolio weightsPortfolio) {
        final List<BigDecimal> tmpWeights = weightsPortfolio.getWeights();
        final Primitive64Matrix tmpAssetWeights = FinancePortfolio.MATRIX_FACTORY.columns(tmpWeights);
        final Primitive64Matrix tmpAssetReturns = this.getAssetReturns();
        return this.calculatePortfolioReturn(tmpAssetWeights, tmpAssetReturns).doubleValue();
    }

    public final double calculatePortfolioVariance(final FinancePortfolio weightsPortfolio) {
        final List<BigDecimal> tmpWeights = weightsPortfolio.getWeights();
        final Primitive64Matrix tmpAssetWeights = FinancePortfolio.MATRIX_FACTORY.columns(tmpWeights);
        return this.calculatePortfolioVariance(tmpAssetWeights).doubleValue();
    }

    public final Primitive64Matrix getAssetReturns() {
        if (myAssetReturns == null) {
            myAssetReturns = this.calculateAssetReturns();
        }
        return myAssetReturns;
    }

    public final Primitive64Matrix getAssetVolatilities() {
        if (myAssetVolatilities == null) {
            myAssetVolatilities = myMarketEquilibrium.toCorrelations();
        }
        return myAssetVolatilities;
    }

    public final Primitive64Matrix getAssetWeights() {
        if (myAssetWeights == null) {
            myAssetWeights = this.calculateAssetWeights();
        }
        return myAssetWeights;
    }

    public final Primitive64Matrix getCorrelations() {
        return myMarketEquilibrium.toCorrelations();
    }

    public final Primitive64Matrix getCovariances() {
        return myMarketEquilibrium.getCovariances();
    }

    public final MarketEquilibrium getMarketEquilibrium() {
        return myMarketEquilibrium.copy();
    }

    @Override
    public final double getMeanReturn() {
        if (myMeanReturn == null) {
            final Primitive64Matrix tmpAssetWeights = this.getAssetWeights();
            final Primitive64Matrix tmpAssetReturns = this.getAssetReturns();
            if ((tmpAssetWeights != null) && (tmpAssetReturns != null)) {
                myMeanReturn = this.calculatePortfolioReturn(tmpAssetWeights, tmpAssetReturns);
            }
        }
        return myMeanReturn.doubleValue();
    }

    @Override
    public final double getReturnVariance() {
        if (myReturnVariance == null) {
            myReturnVariance = this.calculatePortfolioVariance(this.getAssetWeights());
        }
        return myReturnVariance.doubleValue();
    }

    public final Scalar<?> getRiskAversion() {
        return myMarketEquilibrium.getRiskAversion();
    }

    public final String[] getSymbols() {
        return myMarketEquilibrium.getAssetKeys();
    }

    @Override
    public final List<BigDecimal> getWeights() {

        final Primitive64Matrix tmpAssetWeights = this.getAssetWeights();

        if (tmpAssetWeights != null) {

            return Array1D.R128.copy(tmpAssetWeights);

        } else {

            return null;
        }
    }

    public final void setRiskAversion(final Comparable<?> factor) {

        myMarketEquilibrium.setRiskAversion(factor);

        this.reset();
    }

    public int size() {
        return myMarketEquilibrium.size();
    }

    public final List<SimpleAsset> toSimpleAssets() {

        final Primitive64Matrix tmpReturns = this.getAssetReturns();
        final Primitive64Matrix tmpCovariances = this.getCovariances();
        final List<BigDecimal> tmpWeights = this.getWeights();

        final ArrayList<SimpleAsset> retVal = new ArrayList<>(tmpWeights.size());

        for (int i = 0; i < tmpWeights.size(); i++) {
            final double tmpMeanReturn = tmpReturns.doubleValue(i, 0);
            final double tmpVolatility = PrimitiveMath.SQRT.invoke(tmpCovariances.doubleValue(i, i));
            final BigDecimal tmpWeight = tmpWeights.get(i);
            retVal.add(new SimpleAsset(tmpMeanReturn, tmpVolatility, tmpWeight));
        }

        return retVal;
    }

    public final SimplePortfolio toSimplePortfolio() {
        return new SimplePortfolio(this.getCorrelations(), this.toSimpleAssets());
    }

    @Override
    public String toString() {
        return TypeUtils.format("RAF={} {}", this.getRiskAversion().toString(), super.toString());
    }

    protected abstract Primitive64Matrix calculateAssetReturns();

    protected final Primitive64Matrix calculateAssetReturns(final Primitive64Matrix aWeightsVctr) {
        return myMarketEquilibrium.calculateAssetReturns(aWeightsVctr);
    }

    protected abstract Primitive64Matrix calculateAssetWeights();

    protected final Primitive64Matrix calculateAssetWeights(final Primitive64Matrix aReturnsVctr) {
        return myMarketEquilibrium.calculateAssetWeights(aReturnsVctr);
    }

    protected final Scalar<?> calculatePortfolioReturn(final Primitive64Matrix aWeightsVctr, final Primitive64Matrix aReturnsVctr) {
        return MarketEquilibrium.calculatePortfolioReturn(aWeightsVctr, aReturnsVctr);
    }

    protected final Scalar<?> calculatePortfolioVariance(final Primitive64Matrix aWeightsVctr) {
        return myMarketEquilibrium.calculatePortfolioVariance(aWeightsVctr);
    }

    protected final void calibrate(final Primitive64Matrix aWeightsVctr, final Primitive64Matrix aReturnsVctr) {

        final Scalar<?> tmpRiskAvesrion = myMarketEquilibrium.calculateImpliedRiskAversion(aWeightsVctr, aReturnsVctr);

        this.setRiskAversion(tmpRiskAvesrion.get());
    }

    @Override
    protected void reset() {
        myAssetWeights = null;
        myAssetReturns = null;
        myMeanReturn = null;
        myReturnVariance = null;
    }

    final boolean isDefaultRiskAversion() {
        return myMarketEquilibrium.isDefaultRiskAversion();
    }

}
