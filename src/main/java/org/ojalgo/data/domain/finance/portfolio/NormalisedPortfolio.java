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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.type.context.NumberContext;

/**
 * Normalised weights Portfolio
 *
 * @author apete
 */
final class NormalisedPortfolio extends FinancePortfolio {

    private final FinancePortfolio myBasePortfolio;
    private transient BigDecimal myTotalWeight;
    private final NumberContext myWeightsContext;

    @SuppressWarnings("unused")
    private NormalisedPortfolio() {

        this(null, null);

        ProgrammingError.throwForIllegalInvocation();
    }

    NormalisedPortfolio(final FinancePortfolio basePortfolio, final NumberContext weightsContext) {

        super();

        myBasePortfolio = basePortfolio;
        myWeightsContext = weightsContext;
    }

    @Override
    public double getMeanReturn() {
        return myBasePortfolio.getMeanReturn() / this.getTotalWeight().doubleValue();
    }

    @Override
    public double getVolatility() {
        return myBasePortfolio.getVolatility() / this.getTotalWeight().doubleValue();
    }

    @Override
    public List<BigDecimal> getWeights() {

        List<BigDecimal> retVal = new ArrayList<>();

        BigDecimal totalWeight = this.getTotalWeight();

        BigDecimal tmpSum = BigMath.ZERO;
        BigDecimal tmpLargest = BigMath.ZERO;
        int tmpIndexOfLargest = -1;

        List<BigDecimal> weights = myBasePortfolio.getWeights();
        BigDecimal weight;
        for (int i = 0; i < weights.size(); i++) {

            weight = weights.get(i);
            weight = BigMath.DIVIDE.invoke(weight, totalWeight);
            weight = myWeightsContext.enforce(weight);

            retVal.add(weight);

            tmpSum = tmpSum.add(weight);

            if (weight.abs().compareTo(tmpLargest) > 0) {
                tmpLargest = weight.abs();
                tmpIndexOfLargest = i;
            }
        }

        if (tmpSum.compareTo(BigMath.ONE) != 0 && tmpIndexOfLargest != -1) {
            retVal.set(tmpIndexOfLargest, retVal.get(tmpIndexOfLargest).subtract(tmpSum.subtract(BigMath.ONE)));
        }

        return retVal;
    }

    private BigDecimal getTotalWeight() {

        if (myTotalWeight == null) {
            myTotalWeight = BigMath.ZERO;
            for (final BigDecimal tmpWeight : myBasePortfolio.getWeights()) {
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

        myBasePortfolio.reset();

        myTotalWeight = null;
    }

}
