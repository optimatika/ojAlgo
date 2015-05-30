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
package org.ojalgo.type.context;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;

import org.ojalgo.ProgrammingError;
import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
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
public final class NumberContext extends FormatContext<Number> {

    public static interface Enforceable<N extends Number> {

        N enforce(NumberContext context);

    }

    public static interface FormatPattern {

        String toLocalizedPattern();

        String toPattern();

    }

    private static final MathContext DEFAULT_MATH = MathContext.DECIMAL64;
    private static final int DEFAULT_SCALE = Integer.MIN_VALUE;
    private static final NumberStyle DEFAULT_STYLE = NumberStyle.GENERAL;

    public static NumberContext getCurrency(final Locale locale) {

        final NumberFormat tmpFormat = NumberStyle.CURRENCY.getFormat(locale);
        final int tmpPrecision = DEFAULT_MATH.getPrecision();
        final int tmpScale = 2;
        final RoundingMode tmpRoundingMode = DEFAULT_MATH.getRoundingMode();

        return new NumberContext(tmpFormat, tmpPrecision, tmpScale, tmpRoundingMode);
    }

    public static NumberContext getGeneral(final int scale) {

        final NumberFormat tmpFormat = NumberStyle.GENERAL.getFormat();
        final int tmpPrecision = DEFAULT_MATH.getPrecision();
        final int tmpScale = scale;
        final RoundingMode tmpRoundingMode = DEFAULT_MATH.getRoundingMode();

        return new NumberContext(tmpFormat, tmpPrecision, tmpScale, tmpRoundingMode);
    }

    public static NumberContext getGeneral(final int aPrecision, final int scale) {

        final NumberFormat tmpFormat = NumberStyle.GENERAL.getFormat();
        final int tmpPrecision = aPrecision;
        final int tmpScale = scale;
        final RoundingMode tmpRoundingMode = DEFAULT_MATH.getRoundingMode();

        return new NumberContext(tmpFormat, tmpPrecision, tmpScale, tmpRoundingMode);
    }

    public static NumberContext getGeneral(final int scale, final RoundingMode aRoundingMode) {

        final NumberFormat tmpFormat = NumberStyle.GENERAL.getFormat();
        final int tmpPrecision = DEFAULT_MATH.getPrecision();
        final int tmpScale = scale;
        final RoundingMode tmpRoundingMode = aRoundingMode;

        return new NumberContext(tmpFormat, tmpPrecision, tmpScale, tmpRoundingMode);
    }

    /**
     * The scale will be set to half the precision.
     */
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

    public static NumberContext getMath(final int aPrecisionAndScale, final RoundingMode aRoundingMode) {

        final NumberFormat tmpFormat = NumberStyle.GENERAL.getFormat();
        final int tmpPrecision = aPrecisionAndScale;
        final int tmpScale = aPrecisionAndScale;
        final RoundingMode tmpRoundingMode = aRoundingMode;

        return new NumberContext(tmpFormat, tmpPrecision, tmpScale, tmpRoundingMode);
    }

    /**
     * The scale will be undefined/unlimited.
     */
    public static NumberContext getMath(final MathContext context) {

        final NumberFormat tmpFormat = NumberStyle.GENERAL.getFormat();
        final int tmpPrecision = context.getPrecision();
        final int tmpScale = DEFAULT_SCALE;
        final RoundingMode tmpRoundingMode = context.getRoundingMode();

        return new NumberContext(tmpFormat, tmpPrecision, tmpScale, tmpRoundingMode);
    }

    public static NumberContext getPercent(final int scale, final Locale locale) {

        final NumberFormat tmpFormat = NumberStyle.PERCENT.getFormat(Locale.getDefault());
        final int tmpPrecision = MathContext.DECIMAL32.getPrecision();
        final int tmpScale = scale;
        final RoundingMode tmpRoundingMode = MathContext.DECIMAL32.getRoundingMode();

        return new NumberContext(tmpFormat, tmpPrecision, tmpScale, tmpRoundingMode);
    }

    public static NumberContext getPercent(final Locale locale) {
        return NumberContext.getPercent(4, locale);
    }

    public static Format toFormat(final NumberStyle style, final Locale locale) {
        return style != null ? style.getFormat(locale) : DEFAULT_STYLE.getFormat(locale);
    }

    private static boolean isZero(final double value, final double tolerance) {
        return (Math.abs(value) <= tolerance);
    }

    private final double myEpsilon;
    private final MathContext myMathContext;
    private final double myRoundingFactor;
    private final int myScale;

    private final double myZeroError;

    public NumberContext() {
        this(DEFAULT_STYLE.getFormat(), DEFAULT_MATH.getPrecision(), DEFAULT_SCALE, DEFAULT_MATH.getRoundingMode());
    }

    public NumberContext(final Format format, final int precision, final int scale, final RoundingMode mode) {

        super(format);

        myMathContext = new MathContext(precision, mode);

        if (precision > 0) {
            myEpsilon = Math.max(PrimitiveMath.MACHINE_EPSILON, Math.pow(PrimitiveMath.TEN, 1 - precision));
        } else {
            myEpsilon = PrimitiveMath.MACHINE_EPSILON;
        }

        myScale = scale;

        if (scale > Integer.MIN_VALUE) {
            myZeroError = Math.max(PrimitiveMath.MACHINE_SMALLEST, PrimitiveMath.HALF * Math.pow(PrimitiveMath.TEN, -scale));
            myRoundingFactor = PrimitiveFunction.POWER.invoke(PrimitiveMath.TEN, scale);
        } else {
            myZeroError = PrimitiveMath.MACHINE_SMALLEST;
            myRoundingFactor = PrimitiveMath.ONE;
        }

    }

    public NumberContext(final int precision, final int scale) {
        this(DEFAULT_STYLE.getFormat(), precision, scale, DEFAULT_MATH.getRoundingMode());
    }

    public NumberContext(final int precision, final int scale, final RoundingMode mode) {
        this(DEFAULT_STYLE.getFormat(), precision, scale, mode);
    }

    public NumberContext(final int scale, final RoundingMode mode) {
        this(DEFAULT_STYLE.getFormat(), DEFAULT_MATH.getPrecision(), scale, mode);
    }

    public NumberContext(final RoundingMode mode) {
        this(DEFAULT_STYLE.getFormat(), DEFAULT_MATH.getPrecision(), DEFAULT_SCALE, mode);
    }

    private NumberContext(final Format format) {
        this(format, DEFAULT_MATH.getPrecision(), DEFAULT_SCALE, DEFAULT_MATH.getRoundingMode());
        ProgrammingError.throwForIllegalInvocation();
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

    /**
     * Does not enforce the precision and does not use the specified rounding mode. The precision is given by
     * the type double and the rounding mode is always "half even" as given by
     * {@linkplain StrictMath#rint(double)}.
     */
    public double enforce(final double number) {
        return Math.rint(myRoundingFactor * number) / myRoundingFactor;
    }

    @Override
    public Number enforce(final Number object) {
        if (object instanceof BigDecimal) {
            return this.enforce((BigDecimal) object);
        } else if (object instanceof Enforceable<?>) {
            return ((Enforceable<?>) object).enforce(this);
        } else {
            return this.enforce(object.doubleValue());
        }
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

    /**
     * "big" enforce(...)
     */
    public UnaryFunction<BigDecimal> getBigFunction() {
        return new UnaryFunction<BigDecimal>() {

            public BigDecimal invoke(final BigDecimal arg) {
                return NumberContext.this.enforce(arg);
            }

            public double invoke(final double arg) {
                return NumberContext.this.enforce(arg);
            }
        };
    }

    /**
     * "complex" enforce(...)
     */
    public UnaryFunction<ComplexNumber> getComplexFunction() {
        return new UnaryFunction<ComplexNumber>() {

            public ComplexNumber invoke(final ComplexNumber arg) {
                return arg.enforce(NumberContext.this);
            }

            public double invoke(final double arg) {
                return NumberContext.this.enforce(arg);
            }
        };
    }

    public MathContext getMathContext() {
        return myMathContext;
    }

    public int getPrecision() {
        return myMathContext.getPrecision();
    }

    /**
     * "primitive" enforce(...)
     */
    public UnaryFunction<Double> getPrimitiveFunction() {
        return new UnaryFunction<Double>() {

            public double invoke(final double arg) {
                return NumberContext.this.enforce(arg);
            }

            public Double invoke(final Double arg) {
                return NumberContext.this.enforce(arg.doubleValue());
            }
        };
    }

    /**
     * "quaternion" enforce(...)
     */
    public UnaryFunction<Quaternion> getQuaternionFunction() {
        return new UnaryFunction<Quaternion>() {

            public double invoke(final double arg) {
                return NumberContext.this.enforce(arg);
            }

            public Quaternion invoke(final Quaternion arg) {
                return arg.enforce(NumberContext.this);
            }
        };
    }

    /**
     * "rational" enforce(...)
     */
    public UnaryFunction<RationalNumber> getRationalFunction() {
        return new UnaryFunction<RationalNumber>() {

            public double invoke(final double arg) {
                return NumberContext.this.enforce(arg);
            }

            public RationalNumber invoke(final RationalNumber arg) {
                return arg.enforce(NumberContext.this);
            }
        };
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
        return !this.isSmall(expected, actual - expected);
    }

    public boolean isSmall(final double comparedTo, final double value) {
        final double tmpComparedTo = Math.abs(comparedTo);
        if (NumberContext.isZero(tmpComparedTo, myZeroError)) {
            return NumberContext.isZero(value, myZeroError);
        } else {
            return NumberContext.isZero(value / tmpComparedTo, myEpsilon);
        }
    }

    public boolean isZero(final double value) {
        return NumberContext.isZero(value, myZeroError);
    }

    public NumberContext newFormat(final NumberStyle style, final Locale locale) {
        return new NumberContext(style.getFormat(locale));
    }

    public NumberContext newMathContext(final MathContext context) {
        return new NumberContext(this.getFormat(), context.getPrecision(), this.getScale(), context.getRoundingMode());
    }

    public NumberContext newPrecision(final int precision) {
        return new NumberContext(this.getFormat(), precision, this.getScale(), this.getRoundingMode());
    }

    public NumberContext newRoundingMode(final RoundingMode mode) {
        return new NumberContext(this.getFormat(), this.getPrecision(), this.getScale(), mode);
    }

    public NumberContext newScale(final int scale) {
        return new NumberContext(this.getFormat(), this.getPrecision(), scale, this.getRoundingMode());
    }

    /**
     * Will create an "enforced" BigDecimal instance.
     */
    public BigDecimal toBigDecimal(final double number) {

        final BigDecimal tmpDecimal = myMathContext.getPrecision() > 0 ? new BigDecimal(number, myMathContext) : new BigDecimal(number);

        return this.scale(tmpDecimal);
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

    private BigDecimal scale(final BigDecimal number) {

        BigDecimal retVal = number;

        if (myScale > Integer.MIN_VALUE) {
            retVal = retVal.setScale(myScale, myMathContext.getRoundingMode());
        }

        if (retVal.signum() == 0) {
            return BigMath.ZERO;
        } else {
            return retVal;
        }
    }

    @Override
    protected void configureFormat(final Format format, final Object object) {

        if (format instanceof DecimalFormat) {

            final DecimalFormat tmpDF = (DecimalFormat) format;

            final int tmpModScale = myScale - (int) Math.log10(tmpDF.getMultiplier());

            tmpDF.setMaximumFractionDigits(tmpModScale);
            tmpDF.setMinimumFractionDigits(tmpModScale);

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
    protected Number handleParseException(final Format format, final String string) {
        return BigMath.ZERO;
    }

}
