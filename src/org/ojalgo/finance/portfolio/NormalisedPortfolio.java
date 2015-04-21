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
import org.ojalgo.constant.BigMath;
import org.ojalgo.function.BigFunction;

/**
 * Normalised weights Portfolio
 *
 * @author apete
 */
final class NormalisedPortfolio extends FinancePortfolio {

    private final FinancePortfolio myPortfolio;
    private transient BigDecimal myTotalWeight;

    public NormalisedPortfolio(final FinancePortfolio aPortfolio) {

        super();

        myPortfolio = aPortfolio;
    }

    @SuppressWarnings("unused")
    private NormalisedPortfolio() {

        this(null);

        ProgrammingError.throwForIllegalInvocation();
    }

    @Override
    public double getMeanReturn() {
        return myPortfolio.getMeanReturn() / this.getTotalWeight().doubleValue();
    }

    @Override
    public double getVolatility() {
        return myPortfolio.getVolatility() / this.getTotalWeight().doubleValue();
    }

    @Override
    public List<BigDecimal> getWeights() {

        final List<BigDecimal> retVal = new ArrayList<BigDecimal>();

        final BigDecimal tmpTotalWeight = this.getTotalWeight();

        BigDecimal tmpSum = BigMath.ZERO;
        BigDecimal tmpLargest = BigMath.ZERO;
        int tmpIndex = -1;

        final List<BigDecimal> tmpWeights = myPortfolio.getWeights();
        BigDecimal tmpWeight;
        for (int i = 0; i < tmpWeights.size(); i++) {

            tmpWeight = tmpWeights.get(i);
            tmpWeight = BigFunction.DIVIDE.invoke(tmpWeight, tmpTotalWeight);
            tmpWeight = WEIGHT_CONTEXT.enforce(tmpWeight);

            retVal.add(tmpWeight);

            tmpSum = tmpSum.add(tmpWeight);

            if (tmpWeight.abs().compareTo(tmpLargest) == 1) {
                tmpLargest = tmpWeight.abs();
                tmpIndex = i;
            }
        }

        if ((tmpSum.compareTo(BigMath.ONE) != 0) && (tmpIndex != -1)) {
            retVal.set(tmpIndex, retVal.get(tmpIndex).subtract(tmpSum.subtract(BigMath.ONE)));
        }

        return retVal;
    }

    private final BigDecimal getTotalWeight() {

        if (myTotalWeight == null) {
            myTotalWeight = BigMath.ZERO;
            for (final BigDecimal tmpWeight : myPortfolio.getWeights()) {
                myTotalWeight = myTotalWeight.add(tmpWeight);
            }
            if (myTotalWeight.signum() == 0) {
                myTotalWeight = BigMath.ONE;
            }
        }

        return myTotalWeight;
    }

    @Override
    protected void reset() {

        myPortfolio.reset();

        myTotalWeight = null;
    }

}
