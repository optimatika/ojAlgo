/*
 * Copyright 1997-2024 Optimatika
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

import org.ojalgo.ProgrammingError;
import org.ojalgo.matrix.MatrixR064;

public final class FixedWeightsPortfolio extends EquilibriumModel {

    private final MatrixR064 myWeights;

    public FixedWeightsPortfolio(final Context aContext, final FinancePortfolio weightsPortfolio) {

        super(aContext);

        myWeights = FinancePortfolio.MATRIX_FACTORY.columns(weightsPortfolio.getWeights());
    }

    public FixedWeightsPortfolio(final MarketEquilibrium aMarketEquilibrium, final MatrixR064 assetWeightsInColumn) {

        super(aMarketEquilibrium);

        myWeights = assetWeightsInColumn;
    }

    @SuppressWarnings("unused")
    private FixedWeightsPortfolio(final MarketEquilibrium aMarketEquilibrium) {

        super(aMarketEquilibrium);

        myWeights = null;

        ProgrammingError.throwForIllegalInvocation();
    }

    public void calibrate(final FinancePortfolio.Context targetReturns) {
        this.calibrate(myWeights, targetReturns.getAssetReturns());
    }

    public void calibrate(final List<? extends Comparable<?>> targetReturns) {
        this.calibrate(myWeights, FinancePortfolio.MATRIX_FACTORY.columns(targetReturns));
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
