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

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.PrimitiveFunction;

public final class CharacteristicLine {

    public double beta, alpha, epsilon;

    private final FinancePortfolio myMarketPortfolio;

    public CharacteristicLine(final FinancePortfolio theMarketPortfolio) {

        super();

        myMarketPortfolio = theMarketPortfolio;
    }

    @SuppressWarnings("unused")
    private CharacteristicLine() {

        this(null);

        ProgrammingError.throwForIllegalInvocation();
    }

    public double calculateBeta(final FinancePortfolio anyAsset) {
        return anyAsset.getMeanReturn() / myMarketPortfolio.getMeanReturn();
    }

    public double calculateCorrelation(final FinancePortfolio anyAsset) {

        final double tmpCovar = this.calculateCovariance(anyAsset);

        final double tmpVal = myMarketPortfolio.getReturnVariance() * anyAsset.getReturnVariance();

        return tmpCovar / PrimitiveFunction.SQRT.invoke(tmpVal);
    }

    public double calculateCovariance(final FinancePortfolio anyAsset) {

        final double tmpBeta = this.calculateBeta(anyAsset);

        return myMarketPortfolio.getReturnVariance() * tmpBeta;
    }

}
