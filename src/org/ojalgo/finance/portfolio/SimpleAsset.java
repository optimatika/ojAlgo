/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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
import java.util.Collections;
import java.util.List;

import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.type.TypeUtils;

/**
 * SimpleAsset is used to describe 1 asset (portfolio member).
 *
 * @author apete
 */
public final class SimpleAsset extends FinancePortfolio {

    private final double myMeanReturn;
    private final double myVolatility;
    private final BigDecimal myWeight;

    public SimpleAsset(final FinancePortfolio portfolio) {
        this(portfolio.getMeanReturn(), portfolio.getVolatility(), BigMath.ONE);
    }

    public SimpleAsset(final FinancePortfolio portfolio, final Number weight) {
        this(portfolio.getMeanReturn(), portfolio.getVolatility(), weight);
    }

    public SimpleAsset(final Number weight) {
        this(PrimitiveMath.ZERO, PrimitiveMath.ZERO, weight);
    }

    public SimpleAsset(final Number meanReturn, final Number volatility) {
        this(meanReturn, volatility, BigMath.ONE);
    }

    public SimpleAsset(final Number meanReturn, final Number volatility, final Number weight) {

        super();

        myMeanReturn = meanReturn != null ? meanReturn.doubleValue() : PrimitiveMath.ZERO;
        myVolatility = volatility != null ? volatility.doubleValue() : PrimitiveMath.ZERO;
        myWeight = TypeUtils.toBigDecimal(weight);
    }

    @SuppressWarnings("unused")
    private SimpleAsset() {
        this(BigMath.ZERO, BigMath.ZERO, BigMath.ONE);
    }

    @Override
    public double getMeanReturn() {
        return myMeanReturn;
    }

    @Override
    public double getVolatility() {
        return myVolatility;
    }

    /**
     * Assuming there is precisely 1 weight - this class is used to describe 1 asset (portfolio member).
     */
    public BigDecimal getWeight() {
        return myWeight;
    }

    @Override
    public List<BigDecimal> getWeights() {
        return Collections.singletonList(myWeight);
    }

    @Override
    protected void reset() {
        ;
    }

}
