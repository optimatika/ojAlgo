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
 * The asset weights are given. The returns are the implied equilibrium returns, [r] = RAF [C][w].
 */
public final class FixedWeightsPortfolio extends EquilibriumModel {

    /**
     * The covariances from the context, the weights from the portfolio, and the default risk aversion (1.0).
     */
    public static FixedWeightsPortfolio of(final Context context, final FinancePortfolio weightsPortfolio) {
        return new FixedWeightsPortfolio(MarketEquilibrium.of(context.getCovariances()), FinancePortfolio.MATRIX_FACTORY.column(weightsPortfolio.getWeights()));
    }

    public static FixedWeightsPortfolio of(final MarketEquilibrium marketEquilibrium, final MatrixR064 weights) {
        return new FixedWeightsPortfolio(marketEquilibrium, weights);
    }

    private final MatrixR064 myWeights;

    /**
     * @deprecated v57 Use {@link #of(FinancePortfolio.Context, FinancePortfolio)} instead.
     */
    @Deprecated
    public FixedWeightsPortfolio(final Context aContext, final FinancePortfolio weightsPortfolio) {
        this(MarketEquilibrium.of(aContext.getCovariances()), FinancePortfolio.MATRIX_FACTORY.column(weightsPortfolio.getWeights()));
    }

    /**
     * @deprecated v57 Use {@link #of(MarketEquilibrium, MatrixR064)} instead. This constructor will become
     *             package-private.
     */
    @Deprecated
    public FixedWeightsPortfolio(final MarketEquilibrium aMarketEquilibrium, final MatrixR064 assetWeightsInColumn) {

        super(aMarketEquilibrium);

        myWeights = assetWeightsInColumn;
    }

    /**
     * Sets the risk aversion to the best fit for these weights and returns.
     *
     * @return true if calibrated, false if the implied risk aversion is not positive - the weights and
     *         returns don't imply a positive risk premium (e.g. historical returns from a falling market).
     *         The risk aversion is then left unchanged.
     */
    public boolean calibrate(final FinancePortfolio.Context targetReturns) {
        return this.calibrate(myWeights, targetReturns.getAssetReturns());
    }

    /**
     * @see #calibrate(FinancePortfolio.Context)
     */
    public boolean calibrate(final List<? extends Comparable<?>> targetReturns) {
        return this.calibrate(myWeights, FinancePortfolio.MATRIX_FACTORY.column(targetReturns));
    }

    @Override
    protected MatrixR064 calculateAssetReturns() {
        return this.calculateAssetReturns(myWeights);
    }

    @Override
    protected MatrixR064 calculateAssetWeights() {
        return myWeights;
    }

}
