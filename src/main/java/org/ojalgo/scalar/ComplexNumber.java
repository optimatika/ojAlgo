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
package org.ojalgo.scalar;

import java.math.BigDecimal;
import java.math.MathContext;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Mutate2D.ModifiableReceiver;
import org.ojalgo.structure.Transformation2D;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.context.NumberContext;

/**
 * ComplexNumber is an immutable complex number class. It only implements the most basic complex number
 * operations. {@linkplain org.ojalgo.function.ComplexFunction} implements some of the more complicated ones.
 *
 * @author apete
 * @see org.ojalgo.function.ComplexFunction
 */
public final class ComplexNumber implements SelfDeclaringScalar<ComplexNumber>, Access2D<Double>, Transformation2D<Double>, Access2D.Collectable<Mutate2D> {

    public static final Scalar.Factory<ComplexNumber> FACTORY = new Scalar.Factory<ComplexNumber>() {

        @Override
        public ComplexNumber cast(final Comparable<?> number) {
            return ComplexNumber.valueOf(number);
        }

        @Override
        public ComplexNumber cast(final double value) {
            return ComplexNumber.valueOf(value);
        }

        @Override
        public ComplexNumber convert(final Comparable<?> number) {
            return ComplexNumber.valueOf(number);
        }

        @Override
        public ComplexNumber convert(final double value) {
            return ComplexNumber.valueOf(value);
        }

        @Override
        public ComplexNumber one() {
            return ONE;
        }

        @Override
        public ComplexNumber zero() {
            return ZERO;
        }

    };

    /**
     * Complex number {@code i}, satisfies i<sup>2</sup> = -1;
     */
    public static final ComplexNumber I = new ComplexNumber(PrimitiveMath.ZERO, PrimitiveMath.ONE);
    /**
     * Complex number Z = (+∞ + 0.0i)
     */
    public static final ComplexNumber INFINITY = ComplexNumber.makePolar(Double.POSITIVE_INFINITY, PrimitiveMath.ZERO);
    /**
     * Complex number Z = (NaN + NaNi)
     */
    public static final ComplexNumber NaN = ComplexNumber.of(PrimitiveMath.NaN, PrimitiveMath.NaN);
    /**
     * Complex number Z = (-1.0 + 0.0i)
     */
    public static final ComplexNumber NEG = ComplexNumber.valueOf(PrimitiveMath.NEG);
    /**
     * Complex number Z = (1.0 + 0.0i)
     */
    public static final ComplexNumber ONE = ComplexNumber.valueOf(PrimitiveMath.ONE);
    /**
     * Complex number Z = (2.0 + 0.0i)
     */
    public static final ComplexNumber TWO = ComplexNumber.valueOf(PrimitiveMath.TWO);
    /**
     * Complex number Z = (0.0 + 0.0i)
     */
    public static final ComplexNumber ZERO = ComplexNumber.valueOf(PrimitiveMath.ZERO);

    private static final double ARGUMENT_TOLERANCE = PrimitiveMath.PI * PrimitiveScalar.CONTEXT.epsilon();

    private static final String LEFT = "(";
    private static final String MINUS = " - ";
    private static final String PLUS = " + ";
    private static final String RIGHT = "i)";

    public static boolean isAbsolute(final ComplexNumber value) {
        return value.isAbsolute();
    }

    /**
     * Test if {@code value} is infinite. A complex number is infinite if its real part and/or its imaginary
     * part is infinite.
     *
     * @param value the complex number to test
     * @return true if the specified value is infinite (real and/or imaginary part) otherwise false
     */
    public static boolean isInfinite(final ComplexNumber value) {
        return Double.isInfinite(value.doubleValue()) || Double.isInfinite(value.i);
    }

    /**
     * Test if {@code value} is NaN. A complex number is NaN if its real and/or its imaginary part is NaN.
     *
     * @param value the complex number to test
     * @return true if the specified value is NaN (real and/or imaginary part) otherwise false
     */
    public static boolean isNaN(final ComplexNumber value) {
        return Double.isNaN(value.doubleValue()) || Double.isNaN(value.i);
    }

    /**
     * Test if {@code value} is real. A complex number Z is real if and only if {@literal Im(Z) = 0.0}.
     *
     * @param value the complex number to test
     * @return true if the imaginary part of the specified value is null otherwise false
     */
    public static boolean isReal(final ComplexNumber value) {
        return value.isReal();
    }

    public static boolean isSmall(final double comparedTo, final ComplexNumber value) {
        return value.isSmall(comparedTo);
    }

    /**
     * Static factory method returning a complex number from polar coordinates
     *
     * @param norm the complex number's norm
     * @param phase the complex number's phase
     * @return a complex number
     */
    public static ComplexNumber makePolar(final double norm, final double phase) {

        double tmpStdPhase = phase % PrimitiveMath.TWO_PI;
        if (tmpStdPhase < PrimitiveMath.ZERO) {
            tmpStdPhase += PrimitiveMath.TWO_PI;
        }

        if (tmpStdPhase <= ARGUMENT_TOLERANCE) {

            return new ComplexNumber(norm);

        }
        if (PrimitiveMath.ABS.invoke(tmpStdPhase - PrimitiveMath.PI) <= ARGUMENT_TOLERANCE) {

            return new ComplexNumber(-norm);

        }
        double tmpRe = PrimitiveMath.ZERO;
        if (norm != PrimitiveMath.ZERO) {
            final double tmpCos = PrimitiveMath.COS.invoke(tmpStdPhase);
            if (tmpCos != PrimitiveMath.ZERO) {
                tmpRe = norm * tmpCos;
            }
        }

        double tmpIm = PrimitiveMath.ZERO;
        if (norm != PrimitiveMath.ZERO) {
            final double tmpSin = PrimitiveMath.SIN.invoke(tmpStdPhase);
            if (tmpSin != PrimitiveMath.ZERO) {
                tmpIm = norm * tmpSin;
            }
        }

        return new ComplexNumber(tmpRe, tmpIm);
    }

    public static ComplexNumber makeRotation(final double angle) {
        return new ComplexNumber(PrimitiveMath.COS.invoke(angle), PrimitiveMath.SIN.invoke(angle));
    }

    /**
     * Static factory method returning a complex number from cartesian coordinates.
     *
     * @param real the complex number's real part
     * @param imaginary the complex number's imaginary part
     * @return a complex number
     */
    public static ComplexNumber of(final double real, final double imaginary) {
        if (PrimitiveScalar.CONTEXT.isSmall(real, imaginary)) {
            return new ComplexNumber(real);
        }
        return new ComplexNumber(real, imaginary);
    }

    /**
     * Static factory method returning a complex number from arbitrary number
     *
     * @param number a numeric value
     * @return {@link ComplexNumber#ZERO} if {@code number} is null otherwise the double value of
     *         {@code number}
     */
    public static ComplexNumber valueOf(final Comparable<?> number) {

        if (number == null) {
            return ZERO;
        }

        if (number instanceof ComplexNumber) {

            return (ComplexNumber) number;

        }
        return new ComplexNumber(NumberDefinition.doubleValue(number));
    }

    /**
     * Static factory method returning a complex number from a real value
     *
     * @param value the complex number's real part
     * @return a complex number Z = ({@code value} + 0.0i)
     */
    public static ComplexNumber valueOf(final double value) {
        return new ComplexNumber(value);
    }

    public final double i;

    private final boolean myRealForSure;
    private final double myRealValue;

    /**
     * Complex number constructor, returns {@link ComplexNumber#ZERO}
     */
    public ComplexNumber() {
        this(PrimitiveMath.ZERO);
    }

    private ComplexNumber(final double real) {

        super();

        myRealValue = real;

        myRealForSure = true;

        i = PrimitiveMath.ZERO;
    }

    ComplexNumber(final double real, final double imaginary) {

        super();

        myRealValue = real;

        myRealForSure = false;

        i = imaginary;
    }

    /**
     * Performs the binary operation '+' with a complex number.
     *
     * @param arg the complex number to add
     * @return a complex number {@literal Z = ((Re(this) + Re(arg)) + (Im(this) + Im(arg))i)}
     */
    @Override
    public ComplexNumber add(final ComplexNumber arg) {
        return new ComplexNumber(myRealValue + arg.doubleValue(), i + arg.i);
    }

    /**
     * Performs the binary operation '+' with a real number
     *
     * @param arg the real number to add
     * @return a complex number {@literal Z = ((Re(this) + arg) + Im(this)i)}
     */
    @Override
    public ComplexNumber add(final double arg) {
        return new ComplexNumber(myRealValue + arg, i);
    }

    @Override
    public ComplexNumber add(final float scalarAddend) {
        return this.add((double) scalarAddend);
    }

    /**
     * First compares the real values. Only if they are equal will compare the imaginary part.
     */
    @Override
    public int compareTo(final ComplexNumber other) {

        int retVal = Double.compare(myRealValue, other.doubleValue());

        if (retVal != 0) {
            return retVal;
        }

        return Double.compare(i, other.i);
    }

    /**
     * Returns the conjugate of this complex number. A complex number conjugate is its reflexion about the
     * real axis.
     *
     * @return a complex number Z = (Re(this) - Im(this)i)
     */
    @Override
    public ComplexNumber conjugate() {
        return new ComplexNumber(myRealValue, -i);
    }

    @Override
    public long count() {
        return 4L;
    }

    @Override
    public long countColumns() {
        return 2L;
    }

    @Override
    public long countRows() {
        return 2L;
    }

    /**
     * Performs the binary operation '/' with a complex number.
     *
     * @param arg the complex number to divide by
     * @return a complex number {@literal Z = this / arg}
     */
    @Override
    public ComplexNumber divide(final ComplexNumber arg) {

        final double tmpRe = arg.doubleValue();
        final double tmpIm = arg.i;

        if (PrimitiveMath.ABS.invoke(tmpRe) > PrimitiveMath.ABS.invoke(tmpIm)) {

            final double r = tmpIm / tmpRe;
            final double d = tmpRe + r * tmpIm;

            return new ComplexNumber((myRealValue + r * i) / d, (i - r * myRealValue) / d);

        }
        final double r = tmpRe / tmpIm;
        final double d = tmpIm + r * tmpRe;

        return new ComplexNumber((r * myRealValue + i) / d, (r * i - myRealValue) / d);
    }

    /**
     * Performs the binary operation '/' with a real number.
     *
     * @param arg the real number to divide by
     * @return a complex number {@literal Z = ((Re(this) / arg) + (Im(this) / arg)i)}
     */
    @Override
    public ComplexNumber divide(final double arg) {
        return new ComplexNumber(myRealValue / arg, i / arg);
    }

    @Override
    public ComplexNumber divide(final float scalarDivisor) {
        return this.divide((double) scalarDivisor);
    }

    @Override
    public double doubleValue() {
        return myRealValue;
    }

    @Override
    public double doubleValue(final long index) {
        switch ((int) index) {
        case 0:
            return myRealValue;
        case 1:
            return i;
        case 2:
            return -i;
        case 3:
            return myRealValue;
        default:
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    @Override
    public double doubleValue(final long row, final long col) {
        if (row == col) {
            return myRealValue;
        }
        if (row == 1L) {
            return i;
        }
        if (col == 1L) {
            return -i;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    /**
     * Will call {@linkplain NumberContext#enforce(double)} on the real and imaginary parts separately.
     */
    @Override
    public ComplexNumber enforce(final NumberContext context) {

        final double tmpRe = context.enforce(myRealValue);
        final double tmpIm = context.enforce(i);

        return new ComplexNumber(tmpRe, tmpIm);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ComplexNumber)) {
            return false;
        }
        ComplexNumber other = (ComplexNumber) obj;
        if (Double.doubleToLongBits(myRealValue) != Double.doubleToLongBits(other.myRealValue)
                || Double.doubleToLongBits(i) != Double.doubleToLongBits(other.i)) {
            return false;
        }
        return true;
    }

    @Override
    public float floatValue() {
        return (float) this.doubleValue();
    }

    @Override
    public ComplexNumber get() {
        return this;
    }

    @Override
    public Double get(final long index) {
        return this.doubleValue(index);
    }

    @Override
    public Double get(final long row, final long col) {
        return this.doubleValue(row, col);
    }

    public double getArgument() {
        return this.phase();
    }

    public double getImaginary() {
        return i;
    }

    public double getModulus() {
        return this.norm();
    }

    public double getReal() {
        return this.doubleValue();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(i);
        result = prime * result + (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(myRealValue);
        result = prime * result + (int) (temp ^ temp >>> 32);
        return result;
    }

    @Override
    public int intValue() {
        return (int) this.doubleValue();
    }

    /**
     * Performs the unary operation '1/x'
     *
     * @return the complex number Z inverse of this, satisfies {@literal Z * this = 1}
     */
    @Override
    public ComplexNumber invert() {
        return ComplexNumber.makePolar(PrimitiveMath.ONE / this.norm(), -this.phase());
    }

    @Override
    public boolean isAbsolute() {
        if (myRealForSure) {
            return myRealValue >= PrimitiveMath.ZERO;
        }
        return !PrimitiveScalar.CONTEXT.isDifferent(this.norm(), myRealValue);
    }

    public boolean isReal() {
        return myRealForSure || PrimitiveScalar.CONTEXT.isSmall(myRealValue, i);
    }

    @Override
    public boolean isSmall(final double comparedTo) {
        return PrimitiveScalar.CONTEXT.isSmall(comparedTo, this.norm());
    }

    @Override
    public long longValue() {
        return (long) this.doubleValue();
    }

    /**
     * Performs the binary operation '*' with a complex number.
     *
     * @param arg the complex number to multiply by
     * @return a complex number {@literal Z = this * arg}
     */
    @Override
    public ComplexNumber multiply(final ComplexNumber arg) {

        final double tmpRe = arg.doubleValue();
        final double tmpIm = arg.i;

        return new ComplexNumber(myRealValue * tmpRe - i * tmpIm, myRealValue * tmpIm + i * tmpRe);
    }

    /**
     * Performs the binary operation '*' with a real number.
     *
     * @param arg the real number to multiply by
     * @return a complex number Z = ((Re(this) * arg) + Im(this) * arg))
     */
    @Override
    public ComplexNumber multiply(final double arg) {
        return new ComplexNumber(myRealValue * arg, i * arg);
    }

    @Override
    public ComplexNumber multiply(final float scalarMultiplicand) {
        return this.multiply((double) scalarMultiplicand);
    }

    /**
     * Performs the unary operation '-'.
     *
     * @return a complex number Z = -this
     */
    @Override
    public ComplexNumber negate() {
        return new ComplexNumber(-myRealValue, -i);
    }

    /**
     * Returns the norm of this complex number. The norm of a complex number is defined by |Z| =
     * (ZZ<sup>*</sup>)<sup>1/2</sup>.
     *
     * @return the norm of this complex number.
     */
    @Override
    public double norm() {
        return PrimitiveMath.HYPOT.invoke(myRealValue, i);
    }

    /**
     * Returns the phase of this complex number. The phase of a complex number Z is the angle between the
     * positive real axis and the straight line defined by origin and Z in complex plane.
     *
     * @return the phase of this complex number
     */
    public double phase() {
        return Math.atan2(i, myRealValue);
    }

    @Override
    public ComplexNumber power(final int power) {

        double norm = Math.pow(this.norm(), power);
        double phase = this.phase() * power;

        return ComplexNumber.makePolar(norm, phase);
    }

    @Override
    public ComplexNumber signum() {
        if (ComplexNumber.isSmall(PrimitiveMath.ONE, this)) {
            return ComplexNumber.makeRotation(PrimitiveMath.ZERO);
        }
        return ComplexNumber.makeRotation(this.phase());
    }

    /**
     * Performs the binary operation '-' with a complex number.
     *
     * @param arg the complex number to subtract
     * @return a complex number Z = this - {@code arg}
     */
    @Override
    public ComplexNumber subtract(final ComplexNumber arg) {
        return new ComplexNumber(myRealValue - arg.doubleValue(), i - arg.i);
    }

    /**
     * Performs the binary operation '-' with a real number.
     *
     * @param arg the real number to subtract
     * @return a complex number Z = ((Re(this) - arg) + Im(this)i)
     */
    @Override
    public ComplexNumber subtract(final double arg) {
        return new ComplexNumber(myRealValue - arg, i);
    }

    @Override
    public ComplexNumber subtract(final float scalarSubtrahend) {
        return this.subtract((double) scalarSubtrahend);
    }

    @Override
    public void supplyTo(final Mutate2D receiver) {
        receiver.set(0L, myRealValue);
        receiver.set(1L, i);
        receiver.set(2L, -i);
        receiver.set(3L, myRealValue);
    }

    @Override
    public BigDecimal toBigDecimal() {
        return new BigDecimal(this.doubleValue(), MathContext.DECIMAL64);
    }

    public MatrixStore<Double> toMultiplicationMatrix() {
        final Primitive64Store retVal = Primitive64Store.FACTORY.make(this);
        this.supplyTo(retVal);
        return retVal;
    }

    public MatrixStore<Double> toMultiplicationVector() {

        final Primitive64Store retVal = Primitive64Store.FACTORY.make(2L, 1L);

        retVal.set(0L, myRealValue);
        retVal.set(1L, i);

        return retVal;
    }

    public MatrixStore<Double> toRotationMatrix() {

        final Primitive64Store retVal = Primitive64Store.FACTORY.make(2L, 2L);

        final double s = myRealValue;

        final double ss = s * s;
        final double ii = i * i;

        final double invs = 1.0 / (ii + ss);

        final double r00 = (ii + ss) * invs;
        final double r11 = (ss - ii) * invs;

        retVal.set(0L, r00);
        retVal.set(3L, r11);

        return retVal;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder retVal = new StringBuilder(LEFT);

        final double tmpRe = myRealValue;
        final double tmpIm = i;

        retVal.append(Double.toString(tmpRe));

        if (tmpIm < PrimitiveMath.ZERO) {
            retVal.append(MINUS);
        } else {
            retVal.append(PLUS);
        }
        retVal.append(Double.toString(PrimitiveMath.ABS.invoke(tmpIm)));

        return retVal.append(RIGHT).toString();
    }

    @Override
    public String toString(final NumberContext context) {

        final StringBuilder retVal = new StringBuilder(LEFT);

        final BigDecimal tmpRe = context.enforce(new BigDecimal(myRealValue, PrimitiveScalar.CONTEXT.getMathContext()));
        final BigDecimal tmpIm = context.enforce(new BigDecimal(i, PrimitiveScalar.CONTEXT.getMathContext()));

        retVal.append(tmpRe.toString());

        if (tmpIm.signum() < 0) {
            retVal.append(MINUS);
        } else {
            retVal.append(PLUS);
        }
        retVal.append(tmpIm.abs().toString());

        return retVal.append(RIGHT).toString();
    }

    @Override
    public <T extends ModifiableReceiver<Double>> void transform(final T transformable) {

        final double s = myRealValue;

        final double ss = s * s;
        final double ii = i * i;

        final double invs = 1.0 / (ii + ss);

        final double r00 = (ii + ss) * invs;
        final double r11 = (ss - ii) * invs;

        if (transformable.count() == 2L) {

            final double x = transformable.doubleValue(0);
            final double y = transformable.doubleValue(1);

            transformable.set(0, r00 * x);
            transformable.set(1, r11 * y);

        } else if (transformable.countRows() == 2L) {

            for (long c = 0L, limit = transformable.countColumns(); c < limit; c++) {

                final double x = transformable.doubleValue(0, c);
                final double y = transformable.doubleValue(1, c);

                transformable.set(0, c, r00 * x);
                transformable.set(1, c, r11 * y);
            }

        } else if (transformable.countColumns() == 2L) {

            for (long r = 0L, limit = transformable.countRows(); r < limit; r++) {

                final double x = transformable.doubleValue(r, 0);
                final double y = transformable.doubleValue(r, 1);

                transformable.set(r, 0, r00 * x);
                transformable.set(r, 1, r11 * y);
            }

        } else {

            throw new ProgrammingError("Only works for 2D stuff!");
        }
    }

    <T extends ModifiableReceiver<Double>> void transformWhenUnit(final T transformable) {

        final double s = this.doubleValue();

        final double ss = s * s;
        final double ii = i * i;

        final double r00 = ii + ss;
        final double r11 = ss - ii;

        if (transformable.count() == 2L) {

            final double x = transformable.doubleValue(0);
            final double y = transformable.doubleValue(1);

            transformable.set(0, r00 * x);
            transformable.set(1, r11 * y);

        } else if (transformable.countRows() == 2L) {

            for (long c = 0L, limit = transformable.countColumns(); c < limit; c++) {

                final double x = transformable.doubleValue(0, c);
                final double y = transformable.doubleValue(1, c);

                transformable.set(0, c, r00 * x);
                transformable.set(1, c, r11 * y);
            }

        } else if (transformable.countColumns() == 2L) {

            for (long r = 0L, limit = transformable.countRows(); r < limit; r++) {

                final double x = transformable.doubleValue(r, 0);
                final double y = transformable.doubleValue(r, 1);

                transformable.set(r, 0, r00 * x);
                transformable.set(r, 1, r11 * y);
            }

        } else {

            throw new ProgrammingError("Only works for 2D stuff!");
        }
    }

}
