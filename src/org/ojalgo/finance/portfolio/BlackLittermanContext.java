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

import org.ojalgo.matrix.BasicMatrix;

final class BlackLittermanContext implements FinancePortfolio.Context {

    public BlackLittermanContext() {
        super();
    }

    public double calculatePortfolioReturn(final FinancePortfolio weightsPortfolio) {
        // TODO Auto-generated method stub
        return 0;
    }

    public double calculatePortfolioVariance(final FinancePortfolio weightsPortfolio) {
        // TODO Auto-generated method stub
        return 0;
    }

    public BasicMatrix getAssetReturns() {
        // TODO Auto-generated method stub
        return null;
    }

    public BasicMatrix getAssetVolatilities() {
        // TODO Auto-generated method stub
        return null;
    }

    public BasicMatrix getCorrelations() {
        // TODO Auto-generated method stub
        return null;
    }

    public BasicMatrix getCovariances() {
        // TODO Auto-generated method stub
        return null;
    }

    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

}
