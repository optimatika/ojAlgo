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
package org.ojalgo.type;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.netio.ASCII;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

public abstract class TypeUtils {

    public static final long HOURS_PER_CENTURY = 876582L; // 100 * 365.2425 * 24 = 876582
    public static final long MILLIS_PER_HOUR = 60L * 60L * 1000L;

    private static final String HEX = "#";
    private static final char START = '{';

    /**
     * Compatible with slf4j. {} in the message pattern will be replaced by the arguments.
     */
    public static String format(final String messagePattern, final Object... args) {

        if (messagePattern == null) {
            return null;
        }

        final int tmpPatternSize = messagePattern.length();
        final int tmpArgsCount = args.length;

        int tmpFirst = 0;
        int tmpLimit = tmpPatternSize;

        final StringBuilder retVal = new StringBuilder(tmpPatternSize + (tmpArgsCount * 20));

        for (int a = 0; a < tmpArgsCount; a++) {

            tmpLimit = messagePattern.indexOf(TypeUtils.START, tmpFirst);

            if (tmpLimit == -1) {
                retVal.append(ASCII.SP);
                retVal.append(args[a]);
            } else {
                retVal.append(messagePattern.substring(tmpFirst, tmpLimit));
                retVal.append(args[a]);
                tmpFirst = tmpLimit + 2;
            }
        }

        retVal.append(messagePattern.substring(tmpFirst, tmpPatternSize));

        return retVal.toString();
    }

    public static final GregorianCalendar getHundredYearsAgo() {

        final GregorianCalendar retVal = new GregorianCalendar();

        retVal.add(Calendar.YEAR, -100);

        return retVal;
    }

    public static final GregorianCalendar getThousandYearsAgo() {

        final GregorianCalendar retVal = new GregorianCalendar();

        retVal.add(Calendar.YEAR, -1000);

        return retVal;
    }

    public static final GregorianCalendar getThousandYearsFromNow() {

        final GregorianCalendar retVal = new GregorianCalendar();

        retVal.add(Calendar.YEAR, 1000);

        return retVal;
    }

    /**
     * If the input {@linkplain java.lang.Number} is a {@linkplain java.math.BigDecimal} it is passed through
     * unaltered. Otherwise an equivalent BigDecimal is created.
     *
     * @param number Any Number
     * @return A corresponding BigDecimal
     */
    public static BigDecimal toBigDecimal(final Number number) {

        BigDecimal retVal = BigMath.ZERO;

        if (number != null) {

            if (number instanceof BigDecimal) {

                retVal = (BigDecimal) number;

            } else if (number instanceof Scalar<?>) {

                retVal = ((Scalar<?>) number).toBigDecimal();

            } else {

                try {

                    retVal = new BigDecimal(number.toString());

                } catch (final NumberFormatException exception) {

                    final double tmpVal = number.doubleValue();

                    if (Double.isNaN(tmpVal)) {
                        retVal = BigMath.ZERO;
                    } else if (Double.isInfinite(tmpVal) && (tmpVal > PrimitiveMath.ZERO)) {
                        retVal = BigMath.VERY_POSITIVE;
                    } else if (Double.isInfinite(tmpVal) && (tmpVal < PrimitiveMath.ZERO)) {
                        retVal = BigMath.VERY_NEGATIVE;
                    } else {
                        retVal = BigDecimal.valueOf(tmpVal);
                    }
                }
            }
        }

        return retVal;
    }

    public static BigDecimal toBigDecimal(final Number number, final NumberContext context) {
        return context.enforce(TypeUtils.toBigDecimal(number));
    }

    /**
     * The way colours are specified in html pages.
     */
    public static String toHexString(final int colour) {
        return HEX + Integer.toHexString(colour).substring(2);
    }

    static boolean isSameDate(final Calendar aCal1, final Calendar aCal2) {

        boolean retVal = aCal1.get(Calendar.YEAR) == aCal2.get(Calendar.YEAR);

        retVal = retVal && (aCal1.get(Calendar.MONTH) == aCal2.get(Calendar.MONTH));

        retVal = retVal && (aCal1.get(Calendar.DAY_OF_MONTH) == aCal2.get(Calendar.DAY_OF_MONTH));

        return retVal;
    }

    static boolean isSameTime(final Calendar aCal1, final Calendar aCal2) {

        boolean retVal = aCal1.get(Calendar.HOUR_OF_DAY) == aCal2.get(Calendar.HOUR_OF_DAY);

        retVal = retVal && (aCal1.get(Calendar.MINUTE) == aCal2.get(Calendar.MINUTE));

        retVal = retVal && (aCal1.get(Calendar.SECOND) == aCal2.get(Calendar.SECOND));

        return retVal;
    }

    protected TypeUtils() {
        super();
    }

}
