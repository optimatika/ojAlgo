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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

/**
 * One asset (portfolio member) - its weight, mean return and volatility.
 *
 * @author apete
 */
public final class SimpleAsset extends FinancePortfolio {

    /**
     * With weight 1.0
     */
    public static SimpleAsset of(final Comparable<?> meanReturn, final Comparable<?> volatility) {
        return SimpleAsset.of(meanReturn, volatility, BigMath.ONE);
    }

    /**
     * A null mean return or volatility is treated as 0.0
     */
    public static SimpleAsset of(final Comparable<?> meanReturn, final Comparable<?> volatility, final Comparable<?> weight) {
        return new SimpleAsset(SimpleAsset.toDouble(meanReturn), SimpleAsset.toDouble(volatility), TypeUtils.toBigDecimal(weight));
    }

    /**
     * The mean return and volatility of the portfolio, and the given weight
     */
    public static SimpleAsset of(final FinancePortfolio portfolio, final Comparable<?> weight) {
        return new SimpleAsset(portfolio.getMeanReturn(), portfolio.getVolatility(), TypeUtils.toBigDecimal(weight));
    }

    /**
     * Only a weight - mean return and volatility are 0.0
     */
    public static SimpleAsset ofWeight(final Comparable<?> weight) {
        return new SimpleAsset(PrimitiveMath.ZERO, PrimitiveMath.ZERO, TypeUtils.toBigDecimal(weight));
    }

    private static double toDouble(final Comparable<?> number) {
        return number != null ? Scalar.doubleValue(number) : PrimitiveMath.ZERO;
    }

    private final double myMeanReturn;
    private final double myVolatility;
    private final BigDecimal myWeight;

    /**
     * @deprecated v57 Use {@link #ofWeight(Comparable)} instead.
     */
    @Deprecated
    public SimpleAsset(final Comparable<?> weight) {
        this(PrimitiveMath.ZERO, PrimitiveMath.ZERO, TypeUtils.toBigDecimal(weight));
    }

    /**
     * @deprecated v57 Use {@link #of(Comparable, Comparable)} instead.
     */
    @Deprecated
    public SimpleAsset(final Comparable<?> meanReturn, final Comparable<?> volatility) {
        this(SimpleAsset.toDouble(meanReturn), SimpleAsset.toDouble(volatility), BigMath.ONE);
    }

    /**
     * @deprecated v57 Use {@link #of(Comparable, Comparable, Comparable)} instead.
     */
    @Deprecated
    public SimpleAsset(final Comparable<?> meanReturn, final Comparable<?> volatility, final Comparable<?> weight) {
        this(SimpleAsset.toDouble(meanReturn), SimpleAsset.toDouble(volatility), TypeUtils.toBigDecimal(weight));
    }

    /**
     * @deprecated v57 Use {@link #of(FinancePortfolio, Comparable)}, with weight 1.0, instead.
     */
    @Deprecated
    public SimpleAsset(final FinancePortfolio portfolio) {
        this(portfolio.getMeanReturn(), portfolio.getVolatility(), BigMath.ONE);
    }

    /**
     * @deprecated v57 Use {@link #of(FinancePortfolio, Comparable)} instead.
     */
    @Deprecated
    public SimpleAsset(final FinancePortfolio portfolio, final Comparable<?> weight) {
        this(portfolio.getMeanReturn(), portfolio.getVolatility(), TypeUtils.toBigDecimal(weight));
    }

    SimpleAsset(final double meanReturn, final double volatility, final BigDecimal weight) {

        super();

        myMeanReturn = meanReturn;
        myVolatility = volatility;
        myWeight = weight;
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

    }

}
