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

public final class FixedReturnsPortfolio extends EquilibriumModel {

    private final MatrixR064 myReturns;

    public FixedReturnsPortfolio(final Context aContext) {

        super(aContext);

        myReturns = aContext.getAssetReturns();
    }

    public FixedReturnsPortfolio(final MarketEquilibrium aMarketEquilibrium, final MatrixR064 returnsVector) {

        super(aMarketEquilibrium);

        myReturns = returnsVector;
    }

    @SuppressWarnings("unused")
    private FixedReturnsPortfolio(final MarketEquilibrium aMarketEquilibrium) {

        super(aMarketEquilibrium);

        myReturns = null;

        ProgrammingError.throwForIllegalInvocation();
    }

    public void calibrate(final FinancePortfolio targetWeights) {
        this.calibrate(targetWeights.getWeights());
    }

    public void calibrate(final List<? extends Comparable<?>> targetWeights) {
        this.calibrate(FinancePortfolio.MATRIX_FACTORY.columns(targetWeights), myReturns);
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
