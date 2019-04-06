/*
 * Copyright 1997-2019 Optimatika
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
package org.ojalgo.function.constant.tmp;

import java.math.BigDecimal;
import java.math.MathContext;

public abstract class MissingMath {

    public static double acosh(final double arg) {
        return Math.log(arg + Math.sqrt((arg * arg) - 1.0));
    }

    public static double asinh(final double arg) {
        return Math.log(arg + Math.sqrt((arg * arg) + 1.0));
    }

    public static double atanh(final double arg) {
        return Math.log((1.0 + arg) / (1.0 - arg)) / 2.0;
    }

    public static BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {
        return numerator.divide(denominator, MathContext.DECIMAL128);
    }

    public static double logistic(final double arg) {
        return 1.0 / (1.0 + Math.exp(-arg));
    }

    public static double logit(final double arg) {
        return Math.log(1.0 / (1.0 - arg));
    }

    public static BigDecimal root(final BigDecimal arg, final int param) {

        if (param <= 0) {

            throw new IllegalArgumentException();

        } else if (param == 1) {

            return arg;

        } else {

            final BigDecimal bigArg = arg.round(MathContext.DECIMAL128);
            final BigDecimal bigParam = BigDecimal.valueOf(param);

            BigDecimal retVal = BigDecimal.ZERO;
            final double primArg = arg.doubleValue();
            if (!Double.isInfinite(primArg) && !Double.isNaN(primArg)) {
                retVal = BigDecimal.valueOf(Math.pow(primArg, 1.0 / param)); // Intial guess
            }

            BigDecimal shouldBeZero;
            while ((shouldBeZero = retVal.pow(param, MathContext.DECIMAL128).subtract(bigArg)).signum() != 0) {
                retVal = retVal.subtract(shouldBeZero.divide(bigParam.multiply(retVal.pow(param - 1)), MathContext.DECIMAL128));
            }

            return retVal;
        }
    }

}
