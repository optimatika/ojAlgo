/*
 * Copyright 1997-2021 Optimatika
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
package org.ojalgo.type.context;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.format.NumberStyle;

/**
 * <p>
 * Think of this as a {@linkplain MathContext} that specifies both precision and scale. Numeric data types
 * (non-integers) in databases are specified using precison and scale. While doing maths the precision is all
 * that matters, but before sending a number to a database, or printing/displaying it, rounding to a specified
 * scale is desireable.
 * </p>
 * <p>
 * The enforce methods first enforce the precision and then set the scale. It is possible that this will
 * create a number with trailing zeros and more digits than the precision allows. It is also possible to
 * define a context with a scale that is larger than the precision. This is NOT how precision and scale is
 * used with numeric types in databases.
 * </p>
 *
 * @author apete
 */
public final class NumberContext extends FormatContext<Comparable<?>> {

    public interface Enforceable<T> {

        T enforce(NumberContext context);

    }

    public interface FormatPattern {

        String toLocalizedPattern();

        String toPattern();

    }

    private static final MathContext DEFAULT_MATH = MathContext.DECIMAL64;
    private static final int DEFAULT_SCALE = Integer.MIN_VALUE;
    private static final NumberStyle DEFAULT_STYLE = NumberStyle.GENERAL;

    /**
     * Variation of {@link Double#compare(double, double)} that returns 0 if arg1 == arg2.
     */
    public static int compare(final double arg1, final double arg2) {
        if (arg1 == arg2) {
            return 0;
        } else {
            return Double.compare(arg1, arg2);
        }
    }

    /**
     * Variation of {@link Float#compare(float, float)} that returns 0 if arg1 == arg2.
     */
    public static int compare(final float arg1, final float arg2) {
        if (arg1 == arg2) {
            return 0;
        } else {
            return Float.compare(arg1, arg2);
        }
    }

    public static NumberContext getCurrency(final Locale locale) {

        final NumberFormat tmpFormat = NumberStyle.CURRENCY.getFormat(locale);
        final int tmpPrecision = DEFAULT_MATH.getPrecision();
        final int tmpScale = 2;
        final RoundingMode tmpRoundingMode = DEFAULT_MATH.getRoundingMode();

        return new NumberContext(tmpFormat, tmpPrecision, tmpScale, tmpRoundingMode);
    }

    /**
     * @deprecated v49
     */
    @Deprecated
    public static NumberContext getGeneral(final int scale) {
        return NumberContext.ofScale(scale);
    }

    /**
     * @deprecated v49
     */
    @Deprecated
    public static NumberContext getGeneral(final int precision, final int scale) {

        final NumberFormat tmpFormat = NumberStyle.GENERAL.getFormat();
        final int tmpPrecision = precision;
        final int tmpScale = scale;
        final RoundingMode tmpRoundingMode = DEFAULT_MATH.getRoundingMode();

        return new NumberContext(tmpFormat, tmpPrecision, tmpScale, tmpRoundingMode);
    }

    /**
     * @deprecated v49
     */
    @Deprecated
    public static NumberContext getGeneral(final int scale, final RoundingMode roundingMode) {
        return NumberContext.ofScale(scale).withMode(roundingMode);
    }

    /**
     * The scale will be set to half the precision.
     *
     * @deprecated v49
     */
    @Deprecated
    public static NumberContext getGeneral(final MathContext context) {

        final NumberFormat tmpFormat = NumberStyle.GENERAL.getFormat();
        final int tmpPrecision = context.getPrecision();
        final int tmpScale = tmpPrecision / 2;
        final RoundingMode tmpRoundingMode = context.getRoundingMode();

        return new NumberContext(tmpFormat, tmpPrecision, tmpScale, tmpRoundingMode);
    }

    public static NumberContext getInteger(final Locale locale) {

        final NumberFormat tmpFormat = NumberStyle.INTEGER.getFormat(locale);
        final int tmpPrecision = 0;
        final int tmpScale = 0;
        final RoundingMode tmpRoundingMode = DEFAULT_MATH.getRoundingMode();

        return new NumberContext(tmpFormat, tmpPrecision, tmpScale, tmpRoundingMode);
    }

    /**
     * @deprecated v49
     */
    @Deprecated
    public static NumberContext getMath(final int precisionAndScale) {
        return NumberContext.of(precisionAndScale);
    }

    /**
     * @deprecated v49
     */
    @Deprecated
    public static NumberContext getMath(final int precisionAndScale, final RoundingMode roundingMode) {
        return NumberContext.of(precisionAndScale).withMode(roundingMode);
    }

    /**
     * The scale will be undefined/unlimited.
     *
     * @deprecated v49
     */
    @Deprecated
    public static NumberContext getMath(final MathContext context) {
        return NumberContext.ofMath(context);
    }

    public static NumberContext getPercent(final int scale, final Locale locale) {

        final NumberFormat tmpFormat = NumberStyle.PERCENT.getFormat(locale);
        final int tmpPrecision = MathContext.DECIMAL32.getPrecision();
        final int tmpScale = scale;
        final RoundingMode tmpRoundingMode = MathContext.DECIMAL32.getRoundingMode();

        return new NumberContext(tmpFormat, tmpPrecision, tmpScale, tmpRoundingMode);
    }

    public static NumberContext getPercent(final Locale locale) {
        return NumberContext.getPercent(4, locale);
    }

    public static NumberContext of(final int precisionAndScale) {
        NumberFormat format = NumberStyle.GENERAL.getFormat();
        MathContext math = new MathContext(precisionAndScale, DEFAULT_MATH.getRoundingMode());
        return new NumberContext(format, math, precisionAndScale);
    }

    public static NumberContext of(final int precision, final int scale) {
        NumberFormat format = NumberStyle.GENERAL.getFormat();
        MathContext math = new MathContext(precision, DEFAULT_MATH.getRoundingMode());
        return new NumberContext(format, math, scale);
    }

    public static NumberContext ofMath(final MathContext math) {
        NumberFormat format = NumberStyle.GENERAL.getFormat();
        return new NumberContext(format, math, DEFAULT_SCALE);
    }

    public static NumberContext ofPrecision(final int precision) {
        NumberFormat format = NumberStyle.GENERAL.getFormat();
        MathContext math = new MathContext(precision, DEFAULT_MATH.getRoundingMode());
        return new NumberContext(format, math, DEFAULT_SCALE);
    }

    public static NumberContext ofScale(final int scale) {
        NumberFormat format = NumberStyle.GENERAL.getFormat();
        return new NumberContext(format, DEFAULT_MATH, scale);
    }

    public static Format toFormat(final NumberStyle style, final Locale locale) {
        return style != null ? style.getFormat(locale) : DEFAULT_STYLE.getFormat(locale);
    }

    private static boolean isZero(final double value, final double tolerance) {
        return (PrimitiveMath.ABS.invoke(value) <= tolerance);
    }

    private final double myEpsilon;
    private final MathContext myMathContext;
    private final double myRoundingFactor;
    private final int myScale;
    private final double myZeroError;

    /**
     * @deprecated v49
     */
    @Deprecated
    public NumberContext() {
        this(DEFAULT_STYLE.getFormat(), DEFAULT_MATH.getPrecision(), DEFAULT_SCALE, DEFAULT_MATH.getRoundingMode());
    }

    /**
     * @deprecated v49
     */
    @Deprecated
    public NumberContext(final int precision, final int scale) {
        this(DEFAULT_STYLE.getFormat(), precision, scale, DEFAULT_MATH.getRoundingMode());
    }

    /**
     * @deprecated v49
     */
    @Deprecated
    public NumberContext(final int precision, final int scale, final RoundingMode mode) {
        this(DEFAULT_STYLE.getFormat(), precision, scale, mode);
    }

    /**
     * @deprecated v49
     */
    @Deprecated
    public NumberContext(final int scale, final RoundingMode mode) {
        this(DEFAULT_STYLE.getFormat(), DEFAULT_MATH.getPrecision(), scale, mode);
    }

    /**
     * @deprecated v49
     */
    @Deprecated
    public NumberContext(final NumberFormat format, final int precision, final int scale, final RoundingMode mode) {
        this(format, new MathContext(precision, mode), scale);
    }

    /**
     * @deprecated v49
     */
    @Deprecated
    public NumberContext(final RoundingMode mode) {
        this(DEFAULT_STYLE.getFormat(), DEFAULT_MATH.getPrecision(), DEFAULT_SCALE, mode);
    }

    /**
     * @deprecated v49
     */
    @Deprecated
    private NumberContext(final NumberFormat format) {
        this(format, DEFAULT_MATH.getPrecision(), DEFAULT_SCALE, DEFAULT_MATH.getRoundingMode());
        ProgrammingError.throwForIllegalInvocation();
    }

    NumberContext(final NumberFormat format, final MathContext math, final int scale) {

        super(format);

        myMathContext = math;

        if (math.getPrecision() > 0) {
            myEpsilon = PrimitiveMath.MAX.invoke(PrimitiveMath.MACHINE_EPSILON, PrimitiveMath.POW.invoke(PrimitiveMath.TEN, 1 - math.getPrecision()));
        } else {
            myEpsilon = PrimitiveMath.MACHINE_EPSILON;
        }

        myScale = scale;

        if (scale > Integer.MIN_VALUE) {
            myZeroError = PrimitiveMath.MAX.invoke(PrimitiveMath.MACHINE_SMALLEST, PrimitiveMath.HALF * PrimitiveMath.POW.invoke(PrimitiveMath.TEN, -scale));
            myRoundingFactor = PrimitiveMath.POWER.invoke(PrimitiveMath.TEN, scale);
        } else {
            myZeroError = PrimitiveMath.MACHINE_SMALLEST;
            myRoundingFactor = PrimitiveMath.ONE;
        }
    }

    /**
     * Will first enforce the precision, and then the scale. Both operations will comply with the rounding
     * mode.
     */
    public BigDecimal enforce(final BigDecimal number) {

        BigDecimal tmpDecimal = number;

        if (myMathContext.getPrecision() > 0) {
            tmpDecimal = tmpDecimal.plus(this.getMathContext());
        }

        return this.scale(tmpDecimal);
    }

    @Override
    public Comparable<?> enforce(final Comparable<?> object) {
        if (object instanceof BigDecimal) {
            return this.enforce((BigDecimal) object);
        } else if (object instanceof Enforceable<?>) {
            return (Comparable<?>) ((Enforceable<?>) object).enforce(this);
        } else {
            return this.enforce(NumberDefinition.doubleValue(object));
        }
    }

    /**
     * Does not enforce the precision and does not use the specified rounding mode. The precision is given by
     * the type double and the rounding mode is always "half even" as given by
     * {@linkplain StrictMath#rint(double)}.
     */
    public double enforce(final double number) {
        return PrimitiveMath.RINT.invoke(myRoundingFactor * number) / myRoundingFactor;
    }

    /**
     * @return the epsilon
     */
    public double epsilon() {
        return myEpsilon;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof NumberContext)) {
            return false;
        }
        final NumberContext other = (NumberContext) obj;
        if (myMathContext == null) {
            if (other.myMathContext != null) {
                return false;
            }
        } else if (!myMathContext.equals(other.myMathContext)) {
            return false;
        }
        if (myScale != other.myScale) {
            return false;
        }
        return true;
    }

    public String format(final double number) {
        if (!Double.isFinite(number)) {
            return Double.toString(number);
        }
        if (this.isConfigured()) {
            return this.format().format(number);
        } else {
            return this.format(Double.valueOf(number));
        }
    }

    public String format(final long number) {
        return this.format().format(number);
    }

    @Override
    public NumberFormat getFormat() {
        return (NumberFormat) super.getFormat();
    }

    public <N extends Comparable<N>> UnaryFunction<N> getFunction(final FunctionSet<N> functions) {
        return functions.enforce(this);
    }

    public MathContext getMathContext() {
        return myMathContext;
    }

    public int getPrecision() {
        return myMathContext.getPrecision();
    }

    public RoundingMode getRoundingMode() {
        return myMathContext.getRoundingMode();
    }

    public int getScale() {
        return myScale;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + ((myMathContext == null) ? 0 : myMathContext.hashCode());
        result = (prime * result) + myScale;
        return result;
    }

    public boolean isDifferent(final double expected, final double actual) {
        if (expected == actual) {
            return false;
        } else {
            return !this.isSmall(Math.max(Math.abs(expected), Math.abs(actual)), actual - expected);
        }
    }

    public boolean isLessThan(final BigDecimal reference, final BigDecimal value) {
        return (value.compareTo(reference) == -1) && this.isDifferent(reference.doubleValue(), value.doubleValue());
    }

    public boolean isMoreThan(final BigDecimal reference, final BigDecimal value) {
        return (value.compareTo(reference) == 1) && this.isDifferent(reference.doubleValue(), value.doubleValue());
    }

    public boolean isSmall(final double comparedTo, final double value) {
        double norm = PrimitiveMath.ABS.invoke(comparedTo);
        if (NumberContext.isZero(norm, myZeroError)) {
            return NumberContext.isZero(value, myZeroError);
        } else {
            return NumberContext.isZero(value / norm, myEpsilon);
        }
    }

    public boolean isZero(final double value) {
        return NumberContext.isZero(value, myZeroError);
    }

    /**
     * Will create an "enforced" BigDecimal instance.
     */
    public BigDecimal toBigDecimal(final double number) {

        final BigDecimal decimal = myMathContext.getPrecision() > 0 ? new BigDecimal(number, myMathContext) : new BigDecimal(number);

        return this.scale(decimal);
    }

    /**
     * Works with {@linkplain DecimalFormat} and {@linkplain FormatPattern} implementations. In other cases it
     * returns null.
     */
    public String toLocalizedPattern() {

        String retVal = null;

        if (this.getFormat() instanceof DecimalFormat) {
            retVal = ((DecimalFormat) this.getFormat()).toLocalizedPattern();
        } else if (this.getFormat() instanceof FormatPattern) {
            retVal = ((FormatPattern) this.getFormat()).toLocalizedPattern();
        }

        return retVal;
    }

    /**
     * Works with {@linkplain DecimalFormat} and {@linkplain FormatPattern} implementations. In other cases it
     * returns null.
     */
    public String toPattern() {

        String retVal = null;

        if (this.getFormat() instanceof DecimalFormat) {
            retVal = ((DecimalFormat) this.getFormat()).toPattern();
        } else if (this.getFormat() instanceof FormatPattern) {
            retVal = ((FormatPattern) this.getFormat()).toPattern();
        }

        return retVal;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " " + myMathContext.getPrecision() + ":" + myScale + " " + myMathContext.getRoundingMode().toString();
    }

    public NumberContext withFormat(final NumberStyle style, final Locale locale) {
        NumberFormat format = style.getFormat(locale);
        return new NumberContext(format, myMathContext, myScale);
    }

    public NumberContext withMath(final MathContext math) {
        NumberFormat format = (NumberFormat) this.format();
        return new NumberContext(format, math, myScale);
    }

    public NumberContext withMode(final RoundingMode mode) {
        NumberFormat format = (NumberFormat) this.format();
        MathContext math = new MathContext(myMathContext.getPrecision(), mode);
        return new NumberContext(format, math, myScale);
    }

    public NumberContext withoutPrecision() {
        NumberFormat format = (NumberFormat) this.format();
        MathContext math = new MathContext(0, myMathContext.getRoundingMode());
        return new NumberContext(format, math, myScale);
    }

    public NumberContext withoutScale() {
        NumberFormat format = (NumberFormat) this.format();
        return new NumberContext(format, myMathContext, DEFAULT_SCALE);
    }

    public NumberContext withPrecision(final int precision) {
        NumberFormat format = (NumberFormat) this.format();
        MathContext math = new MathContext(precision, myMathContext.getRoundingMode());
        return new NumberContext(format, math, myScale);
    }

    public NumberContext withScale(final int scale) {
        NumberFormat format = (NumberFormat) this.format();
        return new NumberContext(format, myMathContext, scale);
    }

    private BigDecimal scale(final BigDecimal number) {

        BigDecimal retVal = number;

        if (myScale > DEFAULT_SCALE) {
            retVal = retVal.setScale(myScale, myMathContext.getRoundingMode());
            retVal = retVal.stripTrailingZeros();
        }

        return retVal;
    }

    @Override
    protected void configureFormat(final Format format, final Object object) {

        if (format instanceof DecimalFormat) {

            final DecimalFormat tmpDF = (DecimalFormat) format;

            final int tmpModScale = myScale - PrimitiveMath.LOG10.invoke(tmpDF.getMultiplier());

            tmpDF.setMaximumFractionDigits(tmpModScale);
            tmpDF.setMinimumFractionDigits(Math.min(2, tmpModScale));

            if (object instanceof BigDecimal) {
                ((DecimalFormat) this.getFormat()).setParseBigDecimal(true);
            } else {
                ((DecimalFormat) this.getFormat()).setParseBigDecimal(false);
            }
        }
    }

    @Override
    protected String handleFormatException(final Format format, final Object object) {
        return "";
    }

    @Override
    protected Comparable<?> handleParseException(final Format format, final String string) {
        return BigMath.ZERO;
    }

}
