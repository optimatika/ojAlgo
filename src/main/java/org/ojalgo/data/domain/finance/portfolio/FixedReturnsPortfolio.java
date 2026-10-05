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

import java.util.List;

import org.ojalgo.matrix.MatrixR064;

/**
 * The asset returns are given. The weights are the unconstrained equilibrium weights, [w] =
 * [C]<sup>-1</sup>[r] / RAF - they do not necessarily sum to 1.
 */
public final class FixedReturnsPortfolio extends EquilibriumModel {

    /**
     * The covariances and returns from the context, and the default risk aversion (1.0).
     */
    public static FixedReturnsPortfolio of(final Context context) {
        return new FixedReturnsPortfolio(MarketEquilibrium.of(context.getCovariances()), context.getAssetReturns());
    }

    public static FixedReturnsPortfolio of(final MarketEquilibrium marketEquilibrium, final MatrixR064 returns) {
        return new FixedReturnsPortfolio(marketEquilibrium, returns);
    }

    private final MatrixR064 myReturns;

    /**
     * @deprecated v57 Use {@link #of(FinancePortfolio.Context)} instead.
     */
    @Deprecated
    public FixedReturnsPortfolio(final Context aContext) {
        this(MarketEquilibrium.of(aContext.getCovariances()), aContext.getAssetReturns());
    }

    /**
     * @deprecated v57 Use {@link #of(MarketEquilibrium, MatrixR064)} instead. This constructor will become
     *             package-private.
     */
    @Deprecated
    public FixedReturnsPortfolio(final MarketEquilibrium aMarketEquilibrium, final MatrixR064 returnsVector) {

        super(aMarketEquilibrium);

        myReturns = returnsVector;
    }

    /**
     * Sets the risk aversion to the best fit for these weights and returns.
     *
     * @return true if calibrated, false if the implied risk aversion is not positive - the weights and
     *         returns don't imply a positive risk premium (e.g. historical returns from a falling market).
     *         The risk aversion is then left unchanged.
     */
    public boolean calibrate(final FinancePortfolio targetWeights) {
        return this.calibrate(targetWeights.getWeights());
    }

    /**
     * @see #calibrate(FinancePortfolio)
     */
    public boolean calibrate(final List<? extends Comparable<?>> targetWeights) {
        return this.calibrate(FinancePortfolio.MATRIX_FACTORY.column(targetWeights), myReturns);
    }

    @Override
    protected MatrixR064 calculateAssetReturns() {
        return myReturns;
    }

    @Override
    protected MatrixR064 calculateAssetWeights() {
        return this.calculateAssetWeights(myReturns);
    }

}
