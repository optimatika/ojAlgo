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
package org.ojalgo.function.special;

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

    public static BigDecimal hypot(final BigDecimal arg1, final BigDecimal arg2) {
        BigDecimal prod1 = arg1.multiply(arg1);
        BigDecimal prod2 = arg2.multiply(arg2);
        return MissingMath.root(prod1.add(prod2), 2);
    }

    public static double hypot(double arg1, double arg2) {

        if (Double.isNaN(arg1) || Double.isNaN(arg2)) {
            return Double.NaN;
        }

        final double abs1 = Math.abs(arg1);
        final double abs2 = Math.abs(arg2);

        double retVal = 0.0;

        if (abs1 > abs2) {
            retVal = abs1 * MissingMath.sqrt1px2(abs2 / abs1);
        } else if (abs2 > 0.0) {
            retVal = abs2 * MissingMath.sqrt1px2(abs1 / abs2);
        }

        return retVal;
    }

    public static double logistic(final double arg) {
        return 1.0 / (1.0 + Math.exp(-arg));
    }

    public static double logit(final double arg) {
        return Math.log(1.0 / (1.0 - arg));
    }

    public static BigDecimal pow(BigDecimal arg1, BigDecimal arg2) {
        if (arg2.signum() == 0) {
            return BigDecimal.ONE;
        } else if (arg1.signum() == 0) {
            return BigDecimal.ZERO;
        } else if (arg2.compareTo(BigDecimal.ONE) == 0) {
            return arg1;
        } else {
            return BigDecimal.valueOf(Math.pow(arg1.doubleValue(), arg2.doubleValue()));
        }
    }

    public static BigDecimal power(final BigDecimal arg, final int param) {
        switch (param) {
        case 0:
            return BigDecimal.ONE;
        case 1:
            return arg;
        case 2:
            return arg.multiply(arg, MathContext.DECIMAL128);
        case 3:
            return arg.multiply(arg).multiply(arg, MathContext.DECIMAL128);
        case 4:
            BigDecimal arg2 = arg.multiply(arg);
            return arg2.multiply(arg2, MathContext.DECIMAL128);
        default:
            return arg.pow(param, MathContext.DECIMAL128);
        }
    }

    public static double power(double arg, int param) {

        if (param < 0) {

            return 1.0 / MissingMath.power(arg, -param);

        } else {

            double retVal = 1.0;

            while (param > 0) {
                retVal = retVal * arg;
                param--;
            }

            return retVal;
        }
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
            final double primArg = bigArg.doubleValue();
            if (!Double.isInfinite(primArg) && !Double.isNaN(primArg)) {
                retVal = BigDecimal.valueOf(Math.pow(primArg, 1.0 / param)); // Intial guess
            }

            BigDecimal shouldBeZero;
            while ((shouldBeZero = MissingMath.power(retVal, param).subtract(bigArg)).signum() != 0) {
                retVal = retVal.subtract(shouldBeZero.divide(bigParam.multiply(retVal.pow(param - 1)), MathContext.DECIMAL128));
            }

            return retVal;
        }
    }

    public static double root(double arg, int param) {
        if (param != 0) {
            return Math.pow(arg, 1.0 / param);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static double scale(double arg, int param) {

        if (param == 0) {

            return 1.0;

        } else if (param < 0) {

            int factor = 1;
            while (param < 0) {
                factor *= 10;
                param++;
            }

            return Math.rint(factor / arg) * factor;

        } else {

            int factor = 1;
            while (param > 0) {
                factor *= (long) 10;
                param--;
            }

            return Math.rint(factor * arg) / factor;
        }
    }

    public static BigDecimal signum(BigDecimal arg) {
        switch (arg.signum()) {
        case 1:
            return BigDecimal.ONE;
        case -1:
            return BigDecimal.ONE.negate();
        default:
            return BigDecimal.ZERO;
        }
    }

    public static double sqrt1px2(double arg) {
        return Math.sqrt(1.0 + (arg * arg));
    }

    /**
     * Greatest Common Denominator
     */
    public static int gcd(int value1, int value2) {
    
        int retVal = 1;
    
        value1 = Math.abs(value1);
        value2 = Math.abs(value2);
    
        int tmpMax = Math.max(value1, value2);
        int tmpMin = Math.min(value1, value2);
    
        while (tmpMin != 0) {
            retVal = tmpMin;
            tmpMin = tmpMax % tmpMin;
            tmpMax = retVal;
        }
    
        return retVal;
    }

    /**
     * Greatest Common Denominator
     */
    public static long gcd(long value1, long value2) {
    
        long retVal = 1L;
    
        value1 = Math.abs(value1);
        value2 = Math.abs(value2);
    
        long tmpMax = Math.max(value1, value2);
        long tmpMin = Math.min(value1, value2);
    
        while (tmpMin != 0L) {
            retVal = tmpMin;
            tmpMin = tmpMax % tmpMin;
            tmpMax = retVal;
        }
    
        return retVal;
    }

}
