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
package org.ojalgo.optimisation;

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.type.context.NumberContext;

public abstract class OptimisationUtils {

    private static final double _32_0 = EIGHT + EIGHT + EIGHT + EIGHT;

    static final NumberContext DISPLAY = NumberContext.getGeneral(6);

    public static int getAdjustmentExponent(final double largest, final double smallest) {

        final double tmpLargestExp = largest > ZERO ? PrimitiveFunction.LOG10.invoke(largest) : ZERO;
        final double tmpSmallestExp = smallest > ZERO ? PrimitiveFunction.LOG10.invoke(smallest) : -EIGHT;

        if ((tmpLargestExp - tmpSmallestExp) > _32_0) {

            return 0;

        } else {

            final double tmpNegatedAverage = (tmpLargestExp + tmpSmallestExp) / (-TWO);

            return (int) PrimitiveFunction.RINT.invoke(tmpNegatedAverage);
        }
    }

    private OptimisationUtils() {
        super();
    }

}
