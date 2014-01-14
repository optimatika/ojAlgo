/*
 * Copyright 1997-2013 Optimatika (www.optimatika.se)
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
package org.ojalgo.optimisation;

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.type.context.NumberContext;

public abstract class OptimisationUtils {

    static final NumberContext DISPLAY = NumberContext.getGeneral(6);

    static int getAdjustmentFactorExponent(final AggregatorFunction<?> aLargestAggr, final AggregatorFunction<?> aSmallestAggr) {

        final double tmpLargestValue = aLargestAggr.doubleValue();
        final double tmpSmallestValue = aSmallestAggr.doubleValue();

        final double tmpLargestExp = tmpLargestValue >= IS_ZERO ? Math.log10(tmpLargestValue) : ZERO;
        final double tmpSmallestExp = tmpSmallestValue >= IS_ZERO ? Math.log10(tmpSmallestValue) : -TWELFTH;

        return (int) Math.rint(Math.max((tmpLargestExp + tmpSmallestExp) / (-TWO), (-SIX) - tmpSmallestExp));
    }

    private OptimisationUtils() {
        super();
    }

}
