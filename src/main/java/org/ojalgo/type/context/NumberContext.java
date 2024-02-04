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
package org.ojalgo.type.context;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;

import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.format.NumberStyle;

/**
 * <p>
 * Think of this as a {@linkplain MathContext} that specifies both precision and scale. Numeric data types
 * (non-integers) in databases are specified using precision and scale. While doing maths the precision is all
 * that matters, but before sending a number to a database, or printing/displaying it, rounding to a specified
 * scale is desirable.
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
        }
        return Double.compare(arg1, arg2);
    }

    /**
     * Variation of {@link Float#compare(float, float)} that returns 0 if arg1 == arg2.
     */
    public static int compare(final float arg1, final float arg2) {
        if (arg1 == arg2) {
            return 0;
        }
        return Float.compare(arg1, arg2);
    }

    public static NumberContext getCurrency(final Locale locale) {

        NumberFormat tmpFormat = NumberStyle.CURRENCY.getFormat(locale);
        int tmpPrecision = DEFAULT_MATH.getPrecision();
        int tmpScale = 2;
        RoundingMode tmpRoundingMode = DEFAULT_MATH.getRoundingMode();

        return new NumberContext(tmpFormat, tmpPrecision, tmpScale, tmpRoundingMode);
    }

    public static NumberContext getInteger(final Locale locale) {

        NumberFormat tmpFormat = NumberStyle.INTEGER.getFormat(locale);
        int tmpPrecision = 0;
        int tmpScale = 0;
        RoundingMode tmpRoundingMode = DEFAULT_MATH.getRoundingMode();

        return new NumberContext(tmpFormat, tmpPrecision, tmpScale, tmpRoundingMode);
    }

    public static NumberContext getPercent(final int scale, final Locale locale) {

        NumberFormat tmpFormat = NumberStyle.PERCENT.getFormat(locale);
        int tmpPrecision = MathContext.DECIMAL32.getPrecision();
        int tmpScale = scale;
        RoundingMode tmpRoundingMode = MathContext.DECIMAL32.getRoundingMode();

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
        return value == 0D || Math.abs(value) <= tolerance;
    }

    private final double myEpsilon;
    private final MathContext myMathContext;
    private final double myRoundingFactor;
    private final int myScale;
    private final double myZeroError;

    private NumberContext(final NumberFormat format, final int precision, final int scale, final RoundingMode mode) {
        this(format, new MathContext(precision, mode), scale);
    }

    NumberContext(final NumberFormat format, final MathContext math, final int scale) {

        super(format);

        myMathContext = math;

        if (math.getPrecision() > 0) {
            myEpsilon = Math.max(PrimitiveMath.MACHINE_EPSILON, Math.pow(PrimitiveMath.TEN, 1 - math.getPrecision()));
        } else {
            myEpsilon = PrimitiveMath.MACHINE_EPSILON;
        }

        myScale = scale;

        if (scale > Integer.MIN_VALUE) {
            myZeroError = Math.max(PrimitiveMath.MACHINE_SMALLEST, PrimitiveMath.HALF * Math.pow(PrimitiveMath.TEN, -scale));
            myRoundingFactor = MissingMath.power(PrimitiveMath.TEN, scale);
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
        }
        if (object instanceof Enforceable<?>) {
            return (Comparable<?>) ((Enforceable<?>) object).enforce(this);
        }
        return Double.valueOf(this.enforce(NumberDefinition.doubleValue(object)));
    }

    /**
     * If precision is specified then this method instantiates a {@link BigDecimal}, enforces that, and then
     * extracts a double again.
     * <P>
     * If only a scale is specified then this enforces without creating any objects. In this case the
     * precision is given by the type double and the rounding mode is always "half even" as given by
     * {@linkplain Math#rint(double)} (regardless of what rounding mode is specified).
     */
    public double enforce(final double number) {
        if (myMathContext.getPrecision() > 0) {
            return this.enforce(BigDecimal.valueOf(number)).doubleValue();
        }
        if (myScale > Integer.MIN_VALUE) {
            return PrimitiveMath.RINT.invoke(myRoundingFactor * number) / myRoundingFactor;
        }
        return number;
    }

    /**
     * epsilon is defined as the difference between 1 and the next larger decimal number with the given number
     * of digits (precision).
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
        if (!super.equals(obj) || !(obj instanceof NumberContext)) {
            return false;
        }
        NumberContext other = (NumberContext) obj;
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
        }
        return this.format(Double.valueOf(number));
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
        int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myMathContext == null ? 0 : myMathContext.hashCode());
        return prime * result + myScale;
    }

    public boolean isDifferent(final double expected, final double actual) {
        if (expected == actual) {
            return false;
        }
        return !this.isSmall(Math.max(Math.abs(expected), Math.abs(actual)), actual - expected);
    }

    public boolean isInteger(final double value) {
        return this.isSmall(PrimitiveMath.ONE, Math.rint(value) - value);
    }

    public boolean isLessThan(final BigDecimal reference, final BigDecimal value) {
        return value.compareTo(reference) < 0 && this.isDifferent(reference.doubleValue(), value.doubleValue());
    }

    /**
     * The absolute smallest number allowed by this context, but not zero. A single ±1 at the very last
     * decimal place.
     * <p>
     * Say you rounded a number to scale 4 and ended up with a number that is ±1E-4.
     */
    public boolean isMinimal(final BigDecimal value) {
        return value.scale() == myScale && Math.abs(value.unscaledValue().intValue()) == 1;
    }

    public boolean isMoreThan(final BigDecimal reference, final BigDecimal value) {
        return value.compareTo(reference) > 0 && this.isDifferent(reference.doubleValue(), value.doubleValue());
    }

    public boolean isSmall(final BigDecimal comparedTo, final BigDecimal value) {
        if (this.isZero(comparedTo)) {
            return this.isZero(value);
        }
        BigDecimal reference = this.enforce(comparedTo);
        return this.enforce(reference.add(value)).compareTo(reference) == 0;
    }

    public boolean isSmall(final double comparedTo, final double value) {
        if (NumberContext.isZero(comparedTo, myZeroError)) {
            return NumberContext.isZero(value, myZeroError);
        }
        double relative = value / comparedTo;
        return NumberContext.isZero(relative, myEpsilon);
    }

    public boolean isZero(final BigDecimal value) {

        if (value.signum() == 0) {
            return true;
        }

        return this.enforce(value).signum() == 0;
    }

    public boolean isZero(final double value) {
        return NumberContext.isZero(value, myZeroError);
    }

    /**
     * Will create an "enforced" BigDecimal instance.
     */
    public BigDecimal toBigDecimal(final double number) {

        BigDecimal decimal = myMathContext.getPrecision() > 0 ? new BigDecimal(number, myMathContext) : new BigDecimal(number);

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

    public NumberContext withDecrementedPrecision() {
        return this.withDecrementedPrecision(1);
    }

    public NumberContext withDecrementedPrecision(final int subtrahend) {
        return this.withPrecision(myMathContext.getPrecision() - subtrahend);
    }

    public NumberContext withDecrementedScale() {
        return this.withDecrementedScale(1);
    }

    public NumberContext withDecrementedScale(final int subtrahend) {
        return this.withScale(myScale - subtrahend);
    }

    public NumberContext withDoubledPrecision() {
        return this.withPrecision(myMathContext.getPrecision() * 2);
    }

    public NumberContext withDoubledScale() {
        return this.withScale(myScale * 2);
    }

    public NumberContext withFormat(final NumberStyle style, final Locale locale) {
        NumberFormat format = style.getFormat(locale);
        return new NumberContext(format, myMathContext, myScale);
    }

    public NumberContext withHalvedPrecision() {
        return this.withPrecision(myMathContext.getPrecision() / 2);
    }

    public NumberContext withHalvedScale() {
        return this.withScale(myScale / 2);
    }

    public NumberContext withIncrementedPrecision() {
        return this.withIncrementedPrecision(1);
    }

    public NumberContext withIncrementedPrecision(final int addend) {
        return this.withPrecision(myMathContext.getPrecision() + addend);
    }

    public NumberContext withIncrementedScale() {
        return this.withIncrementedScale(1);
    }

    public NumberContext withIncrementedScale(final int addend) {
        return this.withScale(myScale + addend);
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

            DecimalFormat tmpDF = (DecimalFormat) format;

            int tmpModScale = myScale - PrimitiveMath.LOG10.invoke(tmpDF.getMultiplier());

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
