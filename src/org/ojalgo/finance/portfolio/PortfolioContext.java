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

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.finance.FinanceUtils;
import org.ojalgo.matrix.PrimitiveMatrix;

public class PortfolioContext implements FinancePortfolio.Context {

    private final PrimitiveMatrix myAssetReturns;
    private PrimitiveMatrix myAssetVolatilities = null;
    private PrimitiveMatrix myCorrelations = null;
    private PrimitiveMatrix myCovariances = null;

    public PortfolioContext(final Access1D<?> assetReturns, final Access1D<?> assetVolatilities, final Access2D<?> correlations) {

        super();

        myAssetReturns = FinancePortfolio.MATRIX_FACTORY.columns(assetReturns);

        myAssetVolatilities = FinancePortfolio.MATRIX_FACTORY.columns(assetVolatilities);
        myCorrelations = FinancePortfolio.MATRIX_FACTORY.copy(correlations);
    }

    public PortfolioContext(final Access1D<?> assetReturns, final Access2D<?> covariances) {

        super();

        myAssetReturns = FinancePortfolio.MATRIX_FACTORY.columns(assetReturns);

        myCovariances = FinancePortfolio.MATRIX_FACTORY.copy(covariances);
    }

    @SuppressWarnings("unused")
    private PortfolioContext() {

        super();

        myAssetReturns = null;
    }

    @SuppressWarnings("unchecked")
    public double calculatePortfolioReturn(final FinancePortfolio weightsPortfolio) {
        return FinancePortfolio.MATRIX_FACTORY.rows(weightsPortfolio.getWeights()).multiply(this.getAssetReturns()).doubleValue(0);
    }

    @SuppressWarnings("unchecked")
    public double calculatePortfolioVariance(final FinancePortfolio weightsPortfolio) {
        final PrimitiveMatrix tmpWeights = FinancePortfolio.MATRIX_FACTORY.columns(weightsPortfolio.getWeights());
        return this.getCovariances().multiply(tmpWeights).multiplyLeft(tmpWeights.transpose()).doubleValue(0);
    }

    public PrimitiveMatrix getAssetReturns() {
        return myAssetReturns;
    }

    public PrimitiveMatrix getAssetVolatilities() {
        if (myAssetVolatilities == null) {
            myAssetVolatilities = FinanceUtils.toAssetVolatilities(myCovariances);
        }
        return myAssetVolatilities;
    }

    public PrimitiveMatrix getCorrelations() {
        if (myCorrelations == null) {
            myCorrelations = FinanceUtils.toCorrelations(myCovariances, false);
        }
        return myCorrelations;
    }

    public PrimitiveMatrix getCovariances() {
        if (myCovariances == null) {
            myCovariances = FinanceUtils.toCovariances(myAssetVolatilities, myCorrelations);
        }
        return myCovariances;
    }

    public int size() {
        return (int) myAssetReturns.count();
    }

}
