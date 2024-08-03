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

import org.ojalgo.data.domain.finance.FinanceUtils;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;

public class PortfolioContext implements FinancePortfolio.Context {

    private final MatrixR064 myAssetReturns;
    private MatrixR064 myAssetVolatilities = null;
    private MatrixR064 myCorrelations = null;
    private MatrixR064 myCovariances = null;

    public PortfolioContext(final Access1D<?> assetReturns, final Access1D<?> assetVolatilities, final Access2D<?> correlations) {

        super();

        myAssetReturns = FinancePortfolio.MATRIX_FACTORY.column(assetReturns);

        myAssetVolatilities = FinancePortfolio.MATRIX_FACTORY.column(assetVolatilities);
        myCorrelations = FinancePortfolio.MATRIX_FACTORY.copy(correlations);
    }

    public PortfolioContext(final Access1D<?> assetReturns, final Access2D<?> covariances) {

        super();

        myAssetReturns = FinancePortfolio.MATRIX_FACTORY.column(assetReturns);

        myCovariances = FinancePortfolio.MATRIX_FACTORY.copy(covariances);
    }

    @SuppressWarnings("unused")
    private PortfolioContext() {

        super();

        myAssetReturns = null;
    }

    @Override
    public double calculatePortfolioReturn(final FinancePortfolio weightsPortfolio) {
        return FinancePortfolio.MATRIX_FACTORY.row(weightsPortfolio.getWeights()).multiply(this.getAssetReturns()).doubleValue(0);
    }

    @Override
    public double calculatePortfolioVariance(final FinancePortfolio weightsPortfolio) {
        final MatrixR064 tmpWeights = FinancePortfolio.MATRIX_FACTORY.column(weightsPortfolio.getWeights());
        return tmpWeights.transpose().multiply(this.getCovariances().multiply(tmpWeights)).doubleValue(0);
    }

    @Override
    public MatrixR064 getAssetReturns() {
        return myAssetReturns;
    }

    @Override
    public MatrixR064 getAssetVolatilities() {
        if (myAssetVolatilities == null) {
            myAssetVolatilities = FinanceUtils.toVolatilities(myCovariances);
        }
        return myAssetVolatilities;
    }

    @Override
    public MatrixR064 getCorrelations() {
        if (myCorrelations == null) {
            myCorrelations = FinanceUtils.toCorrelations(myCovariances, false);
        }
        return myCorrelations;
    }

    @Override
    public MatrixR064 getCovariances() {
        if (myCovariances == null) {
            myCovariances = FinanceUtils.toCovariances(myAssetVolatilities, myCorrelations);
        }
        return myCovariances;
    }

    @Override
    public int size() {
        return (int) myAssetReturns.count();
    }

}
