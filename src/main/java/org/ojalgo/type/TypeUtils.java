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
package org.ojalgo.type;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.netio.ASCII;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
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

        final int patternLength = messagePattern.length();
        final int argsCount = args.length;

        int first = 0;
        int limit = patternLength;

        final StringBuilder retVal = new StringBuilder(patternLength + argsCount * 20);

        for (int a = 0; a < argsCount; a++) {

            limit = messagePattern.indexOf(TypeUtils.START, first);

            if (limit == -1) {
                retVal.append(ASCII.SP);
                if (args[a] instanceof Access1D) {
                    retVal.append(Access1D.toString((Access1D<?>) args[a]));
                } else if (args[a] instanceof double[]) {
                    retVal.append(Arrays.toString((double[]) args[a]));
                } else {
                    retVal.append(args[a]);
                }
            } else {
                retVal.append(messagePattern.substring(first, limit));
                if (args[a] instanceof double[]) {
                    retVal.append(Arrays.toString((double[]) args[a]));
                } else if (args[a] instanceof float[]) {
                    retVal.append(Arrays.toString((float[]) args[a]));
                } else if (args[a] instanceof long[]) {
                    retVal.append(Arrays.toString((long[]) args[a]));
                } else if (args[a] instanceof int[]) {
                    retVal.append(Arrays.toString((int[]) args[a]));
                } else if (args[a] instanceof short[]) {
                    retVal.append(Arrays.toString((short[]) args[a]));
                } else if (args[a] instanceof byte[]) {
                    retVal.append(Arrays.toString((byte[]) args[a]));
                } else if (args[a] instanceof boolean[]) {
                    retVal.append(Arrays.toString((boolean[]) args[a]));
                } else if (args[a] instanceof char[]) {
                    retVal.append(Arrays.toString((char[]) args[a]));
                } else {
                    retVal.append(args[a]);
                }
                first = limit + 2;
            }
        }

        retVal.append(messagePattern.substring(first, patternLength));

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
     * If the input {@linkplain java.lang.Comparale} is a {@linkplain java.math.BigDecimal} it is passed
     * through unaltered. Otherwise an equivalent BigDecimal is created. ALWAYS returns a valid
     * {@link BigDecimal} instance - which, among other things, means that null and NaN are replaced by 0.0
     * (zero).
     *
     * @param number Any Number
     * @return A corresponding BigDecimal
     */
    public static BigDecimal toBigDecimal(final Comparable<?> number) {

        if (number == null) {
            return BigMath.ZERO;
        }

        if (number instanceof BigDecimal) {

            return (BigDecimal) number;

        } else if (number instanceof Scalar<?>) {

            return ((Scalar<?>) number).toBigDecimal();

        } else {

            try {

                return new BigDecimal(number.toString());

            } catch (final NumberFormatException cause) {

                double value = PrimitiveMath.NaN;
                if (number instanceof NumberDefinition) {
                    value = ((NumberDefinition) number).doubleValue();
                } else if (number instanceof Number) {
                    value = ((Number) number).doubleValue();
                }

                if (Double.isNaN(value)) {
                    return BigMath.ZERO;
                } else if (value == Double.POSITIVE_INFINITY) {
                    return BigMath.VERY_POSITIVE;
                } else if (value == Double.NEGATIVE_INFINITY) {
                    return BigMath.VERY_NEGATIVE;
                } else {
                    return BigDecimal.valueOf(value);
                }
            }
        }
    }

    public static BigDecimal toBigDecimal(final Comparable<?> number, final NumberContext context) {
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

        retVal = retVal && aCal1.get(Calendar.MONTH) == aCal2.get(Calendar.MONTH);

        return retVal && aCal1.get(Calendar.DAY_OF_MONTH) == aCal2.get(Calendar.DAY_OF_MONTH);
    }

    static boolean isSameTime(final Calendar aCal1, final Calendar aCal2) {

        boolean retVal = aCal1.get(Calendar.HOUR_OF_DAY) == aCal2.get(Calendar.HOUR_OF_DAY);

        retVal = retVal && aCal1.get(Calendar.MINUTE) == aCal2.get(Calendar.MINUTE);

        return retVal && aCal1.get(Calendar.SECOND) == aCal2.get(Calendar.SECOND);
    }

    protected TypeUtils() {
        super();
    }

}
