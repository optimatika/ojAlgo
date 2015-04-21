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
package org.ojalgo.constant;

import java.math.BigDecimal;
import java.math.MathContext;

import org.ojalgo.function.BigFunction;

public abstract class BigMath {

    public static final BigDecimal ZERO = new BigDecimal("0", MathContext.DECIMAL128);
    public static final BigDecimal ONE = new BigDecimal("1", MathContext.DECIMAL128);
    public static final BigDecimal TWO = new BigDecimal("2", MathContext.DECIMAL128);
    public static final BigDecimal THREE = new BigDecimal("3", MathContext.DECIMAL128);
    public static final BigDecimal FOUR = new BigDecimal("4", MathContext.DECIMAL128);
    public static final BigDecimal FIVE = new BigDecimal("5", MathContext.DECIMAL128);
    public static final BigDecimal SIX = new BigDecimal("6", MathContext.DECIMAL128);
    public static final BigDecimal SEVEN = new BigDecimal("7", MathContext.DECIMAL128);
    public static final BigDecimal EIGHT = new BigDecimal("8", MathContext.DECIMAL128);
    public static final BigDecimal NINE = new BigDecimal("9", MathContext.DECIMAL128);
    public static final BigDecimal TEN = new BigDecimal("10", MathContext.DECIMAL128);
    public static final BigDecimal ELEVEN = new BigDecimal("11", MathContext.DECIMAL128);
    public static final BigDecimal TWELVE = new BigDecimal("12", MathContext.DECIMAL128);
    public static final BigDecimal HUNDRED = new BigDecimal("100", MathContext.DECIMAL128);
    public static final BigDecimal THOUSAND = new BigDecimal("1000", MathContext.DECIMAL128);

    public static final BigDecimal NEG = ONE.negate();

    public static final BigDecimal HALF = ONE.divide(TWO, MathContext.DECIMAL128);
    public static final BigDecimal THIRD = ONE.divide(THREE, MathContext.DECIMAL128);
    public static final BigDecimal QUARTER = ONE.divide(FOUR, MathContext.DECIMAL128);
    public static final BigDecimal FITH = ONE.divide(FIVE, MathContext.DECIMAL128);
    public static final BigDecimal SIXTH = ONE.divide(SIX, MathContext.DECIMAL128);
    public static final BigDecimal SEVENTH = ONE.divide(SEVEN, MathContext.DECIMAL128);
    public static final BigDecimal EIGHTH = ONE.divide(EIGHT, MathContext.DECIMAL128);
    public static final BigDecimal NINTH = ONE.divide(NINE, MathContext.DECIMAL128);
    public static final BigDecimal TENTH = ONE.divide(TEN, MathContext.DECIMAL128);
    public static final BigDecimal ELEVENTH = ONE.divide(ELEVEN, MathContext.DECIMAL128);
    public static final BigDecimal TWELFTH = ONE.divide(TWELVE, MathContext.DECIMAL128);
    public static final BigDecimal HUNDREDTH = ONE.divide(HUNDRED, MathContext.DECIMAL128);
    public static final BigDecimal THOUSANDTH = ONE.divide(THOUSAND, MathContext.DECIMAL128);

    public static final BigDecimal E = new BigDecimal("2.71828182845904523536028747135266249775724709369996", MathContext.DECIMAL128);
    public static final BigDecimal PI = new BigDecimal("3.14159265358979323846264338327950288419716939937511", MathContext.DECIMAL128);

    public static final BigDecimal HALF_PI = HALF.multiply(PI, MathContext.DECIMAL128);
    public static final BigDecimal TWO_PI = TWO.multiply(PI, MathContext.DECIMAL128);

    public static final BigDecimal SQRT_TWO = BigFunction.SQRT.invoke(TWO);
    public static final BigDecimal SQRT_PI = BigFunction.SQRT.invoke(PI);
    public static final BigDecimal SQRT_TWO_PI = BigFunction.SQRT.invoke(TWO.multiply(PI));

    public static final BigDecimal VERY_NEGATIVE = new BigDecimal(Long.MIN_VALUE, MathContext.DECIMAL128);
    public static final BigDecimal VERY_POSITIVE = new BigDecimal(Long.MAX_VALUE, MathContext.DECIMAL128);

    private BigMath() {
        super();
    }

}
