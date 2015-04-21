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

public abstract class BigPrefix {

    public static final BigDecimal YOCTO = new BigDecimal("0.000000000000000000000001", MathContext.DECIMAL128);
    public static final BigDecimal ZEPTO = new BigDecimal("0.000000000000000000001", MathContext.DECIMAL128);
    public static final BigDecimal ATTO = new BigDecimal("0.000000000000000001", MathContext.DECIMAL128);
    public static final BigDecimal FEMTO = new BigDecimal("0.000000000000001", MathContext.DECIMAL128);
    public static final BigDecimal PICO = new BigDecimal("0.000000000001", MathContext.DECIMAL128);
    public static final BigDecimal NANO = new BigDecimal("0.000000001", MathContext.DECIMAL128);
    public static final BigDecimal MICRO = new BigDecimal("0.000001", MathContext.DECIMAL128);
    public static final BigDecimal MILLI = new BigDecimal("0.001", MathContext.DECIMAL128);
    public static final BigDecimal CENTI = new BigDecimal("0.01", MathContext.DECIMAL128);
    public static final BigDecimal DECI = new BigDecimal("0.1", MathContext.DECIMAL128);
    public static final BigDecimal DEKA = new BigDecimal("10", MathContext.DECIMAL128);
    public static final BigDecimal HECTO = new BigDecimal("100", MathContext.DECIMAL128);
    public static final BigDecimal KILO = new BigDecimal("1000", MathContext.DECIMAL128);
    public static final BigDecimal MEGA = new BigDecimal("1000000", MathContext.DECIMAL128);
    public static final BigDecimal GIGA = new BigDecimal("1000000000", MathContext.DECIMAL128);
    public static final BigDecimal TERA = new BigDecimal("1000000000000", MathContext.DECIMAL128);
    public static final BigDecimal PETA = new BigDecimal("1000000000000000", MathContext.DECIMAL128);
    public static final BigDecimal EXA = new BigDecimal("1000000000000000000", MathContext.DECIMAL128);
    public static final BigDecimal ZETTA = new BigDecimal("1000000000000000000000", MathContext.DECIMAL128);
    public static final BigDecimal YOTTA = new BigDecimal("1000000000000000000000000", MathContext.DECIMAL128);

    private BigPrefix() {
        super();
    }

}
