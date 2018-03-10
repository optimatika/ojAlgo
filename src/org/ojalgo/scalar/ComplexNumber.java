/*
 * Copyright 1997-2018 Optimatika
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
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Mutate2D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.transformation.TransformationMatrix;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.context.NumberContext.Enforceable;

/**
 * ComplexNumber is an immutable complex number class. It only implements the most basic complex number
 * operations. {@linkplain org.ojalgo.function.ComplexFunction} implements some of the more complicated ones.
 *
 * @author apete
 * @see org.ojalgo.function.ComplexFunction
 */
public class ComplexNumber extends Number implements Scalar<ComplexNumber>, Enforceable<ComplexNumber>, Access2D<Double>,
        TransformationMatrix<Double, PhysicalStore<Double>>, Access2D.Collectable<Double, Mutate2D.Receiver<Double>> {

    public static final class Normalised extends ComplexNumber {

        Normalised(final double real, final double imaginary) {
            super(real, imaginary);
        }

        @Override
        public double norm() {
            return PrimitiveMath.ONE;
        }

        @Override
        public Normalised signum() {
            return this;
        }

        @Override
        public void transform(final PhysicalStore<Double> matrix) {

            final double s = this.doubleValue();

            final double ss = s * s;
            final double ii = i * i;

            final double r00 = (ii + ss);
            final double r11 = (ss - ii);

            if (matrix.count() == 2L) {

                final double x = matrix.doubleValue(0);
                final double y = matrix.doubleValue(1);

                matrix.set(0, r00 * x);
                matrix.set(1, r11 * y);

            } else if (matrix.countRows() == 2L) {

                for (long c = 0L, limit = matrix.countColumns(); c < limit; c++) {

                    final double x = matrix.doubleValue(0, c);
                    final double y = matrix.doubleValue(1, c);

                    matrix.set(0, c, r00 * x);
                    matrix.set(1, c, r11 * y);
                }

            } else if (matrix.countColumns() == 2L) {

                for (long r = 0L, limit = matrix.countRows(); r < limit; r++) {

                    final double x = matrix.doubleValue(r, 0);
                    final double y = matrix.doubleValue(r, 1);

                    matrix.set(r, 0, r00 * x);
                    matrix.set(r, 1, r11 * y);
                }

            } else {

                throw new ProgrammingError("Only works for 2D stuff!");
            }
        }

    }

    public static final Scalar.Factory<ComplexNumber> FACTORY = new Scalar.Factory<ComplexNumber>() {

        public ComplexNumber cast(final double value) {
            return ComplexNumber.valueOf(value);
        }

        public ComplexNumber cast(final Number number) {
            return ComplexNumber.valueOf(number);
        }

        public ComplexNumber convert(final double value) {
            return ComplexNumber.valueOf(value);
        }

        public ComplexNumber convert(final Number number) {
            return ComplexNumber.valueOf(number);
        }

        public ComplexNumber one() {
            return ONE;
        }

        public ComplexNumber zero() {
            return ZERO;
        }

    };

    public static final ComplexNumber I = new ComplexNumber(PrimitiveMath.ZERO, PrimitiveMath.ONE);
    public static final ComplexNumber INFINITY = ComplexNumber.makePolar(Double.POSITIVE_INFINITY, PrimitiveMath.ZERO);
    public static final ComplexNumber NEG = ComplexNumber.valueOf(PrimitiveMath.NEG);
    public static final ComplexNumber ONE = ComplexNumber.valueOf(PrimitiveMath.ONE);
    public static final ComplexNumber TWO = ComplexNumber.valueOf(PrimitiveMath.TWO);
    public static final ComplexNumber ZERO = ComplexNumber.valueOf(PrimitiveMath.ZERO);

    private static final double ARGUMENT_TOLERANCE = PrimitiveMath.PI * PrimitiveScalar.CONTEXT.epsilon();

    private static final String LEFT = "(";
    private static final String MINUS = " - ";
    private static final String PLUS = " + ";
    private static final String RIGHT = "i)";

    public static boolean isAbsolute(final ComplexNumber value) {
        return value.isAbsolute();
    }

    public static boolean isInfinite(final ComplexNumber value) {
        return Double.isInfinite(value.doubleValue()) || Double.isInfinite(value.i);
    }

    public static boolean isNaN(final ComplexNumber value) {
        return Double.isNaN(value.doubleValue()) || Double.isNaN(value.i);
    }

    public static boolean isReal(final ComplexNumber value) {
        return value.isReal();
    }

    public static boolean isSmall(final double comparedTo, final ComplexNumber value) {
        return value.isSmall(comparedTo);
    }

    public static ComplexNumber makePolar(final double norm, final double phase) {

        double tmpStdPhase = phase % PrimitiveMath.TWO_PI;
        if (tmpStdPhase < PrimitiveMath.ZERO) {
            tmpStdPhase += PrimitiveMath.TWO_PI;
        }

        if (tmpStdPhase <= ARGUMENT_TOLERANCE) {

            return new ComplexNumber(norm);

        } else if (PrimitiveFunction.ABS.invoke(tmpStdPhase - PrimitiveMath.PI) <= ARGUMENT_TOLERANCE) {

            return new ComplexNumber(-norm);

        } else {

            double tmpRe = PrimitiveMath.ZERO;
            if (norm != PrimitiveMath.ZERO) {
                final double tmpCos = PrimitiveFunction.COS.invoke(tmpStdPhase);
                if (tmpCos != PrimitiveMath.ZERO) {
                    tmpRe = norm * tmpCos;
                }
            }

            double tmpIm = PrimitiveMath.ZERO;
            if (norm != PrimitiveMath.ZERO) {
                final double tmpSin = PrimitiveFunction.SIN.invoke(tmpStdPhase);
                if (tmpSin != PrimitiveMath.ZERO) {
                    tmpIm = norm * tmpSin;
                }
            }

            return new ComplexNumber(tmpRe, tmpIm);
        }
    }

    public static Normalised makeRotation(final double angle) {
        return new Normalised(PrimitiveFunction.COS.invoke(angle), PrimitiveFunction.SIN.invoke(angle));
    }

    public static ComplexNumber of(final double real, final double imaginary) {
        if (PrimitiveScalar.CONTEXT.isSmall(real, imaginary)) {
            return new ComplexNumber(real);
        } else {
            return new ComplexNumber(real, imaginary);
        }
    }

    public static ComplexNumber valueOf(final double value) {
        return new ComplexNumber(value);
    }

    public static ComplexNumber valueOf(final Number number) {

        if (number != null) {

            if (number instanceof ComplexNumber) {

                return (ComplexNumber) number;

            } else {

                return new ComplexNumber(number.doubleValue());
            }

        } else {

            return ZERO;
        }
    }

    public final double i;

    private final boolean myRealForSure;
    private final double myRealValue;

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

    public ComplexNumber add(final ComplexNumber arg) {
        return new ComplexNumber(myRealValue + arg.doubleValue(), i + arg.i);
    }

    public ComplexNumber add(final double arg) {
        return new ComplexNumber(myRealValue + arg, i);
    }

    public int compareTo(final ComplexNumber reference) {

        int retVal = 0;

        if ((retVal = NumberContext.compare(this.norm(), reference.norm())) == 0) {
            if ((retVal = NumberContext.compare(this.doubleValue(), reference.doubleValue())) == 0) {
                retVal = NumberContext.compare(i, reference.i);
            }
        }

        return retVal;
    }

    public ComplexNumber conjugate() {
        return new ComplexNumber(myRealValue, -i);
    }

    public long count() {
        return 4L;
    }

    public long countColumns() {
        return 2L;
    }

    public long countRows() {
        return 2L;
    }

    public ComplexNumber divide(final ComplexNumber arg) {

        final double tmpRe = arg.doubleValue();
        final double tmpIm = arg.i;

        if (PrimitiveFunction.ABS.invoke(tmpRe) > PrimitiveFunction.ABS.invoke(tmpIm)) {

            final double r = tmpIm / tmpRe;
            final double d = tmpRe + (r * tmpIm);

            return new ComplexNumber((myRealValue + (r * i)) / d, (i - (r * myRealValue)) / d);

        } else {

            final double r = tmpRe / tmpIm;
            final double d = tmpIm + (r * tmpRe);

            return new ComplexNumber(((r * myRealValue) + i) / d, ((r * i) - myRealValue) / d);
        }
    }

    public ComplexNumber divide(final double arg) {
        return new ComplexNumber(myRealValue / arg, i / arg);
    }

    /**
     * @see java.lang.Number#doubleValue()
     */
    @Override
    public double doubleValue() {
        return myRealValue;
    }

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

    public double doubleValue(final long row, final long col) {
        if (row == col) {
            return myRealValue;
        } else if (row == 1L) {
            return i;
        } else if (col == 1L) {
            return -i;
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Will call {@linkplain NumberContext#enforce(double)} on the real and imaginary parts separately.
     */
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ComplexNumber)) {
            return false;
        }
        final ComplexNumber other = (ComplexNumber) obj;
        if (Double.doubleToLongBits(myRealValue) != Double.doubleToLongBits(other.myRealValue)) {
            return false;
        }
        if (Double.doubleToLongBits(i) != Double.doubleToLongBits(other.i)) {
            return false;
        }
        return true;
    }

    /**
     * @see java.lang.Number#floatValue()
     */
    @Override
    public float floatValue() {
        return (float) this.doubleValue();
    }

    public ComplexNumber get() {
        return this;
    }

    public Double get(final long index) {
        return this.doubleValue(index);
    }

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
        temp = Double.doubleToLongBits(myRealValue);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(i);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * @see java.lang.Number#intValue()
     */
    @Override
    public int intValue() {
        return (int) this.doubleValue();
    }

    @Override
    public ComplexNumber invert() {
        return ComplexNumber.makePolar(PrimitiveMath.ONE / this.norm(), -this.phase());
    }

    public boolean isAbsolute() {
        if (myRealForSure) {
            return myRealValue >= PrimitiveMath.ZERO;
        } else {
            return !PrimitiveScalar.CONTEXT.isDifferent(this.norm(), myRealValue);
        }
    }

    public boolean isReal() {
        return myRealForSure || PrimitiveScalar.CONTEXT.isSmall(myRealValue, i);
    }

    public boolean isSmall(final double comparedTo) {
        return PrimitiveScalar.CONTEXT.isSmall(comparedTo, this.norm());
    }

    /**
     * @see java.lang.Number#longValue()
     */
    @Override
    public long longValue() {
        return (long) this.doubleValue();
    }

    @Override
    public ComplexNumber multiply(final ComplexNumber arg) {

        final double tmpRe = arg.doubleValue();
        final double tmpIm = arg.i;

        return new ComplexNumber((myRealValue * tmpRe) - (i * tmpIm), (myRealValue * tmpIm) + (i * tmpRe));
    }

    @Override
    public ComplexNumber multiply(final double arg) {
        return new ComplexNumber(myRealValue * arg, i * arg);
    }

    @Override
    public ComplexNumber negate() {
        return new ComplexNumber(-myRealValue, -i);
    }

    public double norm() {
        return PrimitiveFunction.HYPOT.invoke(myRealValue, i);
    }

    public double phase() {
        return Math.atan2(i, myRealValue);
    }

    public ComplexNumber.Normalised signum() {
        if (ComplexNumber.isSmall(PrimitiveMath.ONE, this)) {
            return ComplexNumber.makeRotation(PrimitiveMath.ZERO);
        } else {
            return ComplexNumber.makeRotation(this.phase());
        }
    }

    public ComplexNumber subtract(final ComplexNumber arg) {
        return new ComplexNumber(myRealValue - arg.doubleValue(), i - arg.i);
    }

    @Override
    public ComplexNumber subtract(final double arg) {
        return new ComplexNumber(myRealValue - arg, i);
    }

    public void supplyTo(final Mutate2D.Receiver<Double> receiver) {
        receiver.set(0L, myRealValue);
        receiver.set(1L, i);
        receiver.set(2L, -i);
        receiver.set(3L, myRealValue);
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal(this.doubleValue(), MathContext.DECIMAL64);
    }

    public MatrixStore<Double> toMultiplicationMatrix() {
        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(this);
        this.supplyTo(retVal);
        return retVal;
    }

    public MatrixStore<Double> toMultiplicationVector() {

        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(2L, 1L);

        retVal.set(0L, myRealValue);
        retVal.set(1L, i);

        return retVal;
    }

    public MatrixStore<Double> toRotationMatrix() {

        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(2L, 2L);

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
        retVal.append(Double.toString(PrimitiveFunction.ABS.invoke(tmpIm)));

        return retVal.append(RIGHT).toString();
    }

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

    public void transform(final PhysicalStore<Double> matrix) {

        final double s = myRealValue;

        final double ss = s * s;
        final double ii = i * i;

        final double invs = 1.0 / (ii + ss);

        final double r00 = (ii + ss) * invs;
        final double r11 = (ss - ii) * invs;

        if (matrix.count() == 2L) {

            final double x = matrix.doubleValue(0);
            final double y = matrix.doubleValue(1);

            matrix.set(0, r00 * x);
            matrix.set(1, r11 * y);

        } else if (matrix.countRows() == 2L) {

            for (long c = 0L, limit = matrix.countColumns(); c < limit; c++) {

                final double x = matrix.doubleValue(0, c);
                final double y = matrix.doubleValue(1, c);

                matrix.set(0, c, r00 * x);
                matrix.set(1, c, r11 * y);
            }

        } else if (matrix.countColumns() == 2L) {

            for (long r = 0L, limit = matrix.countRows(); r < limit; r++) {

                final double x = matrix.doubleValue(r, 0);
                final double y = matrix.doubleValue(r, 1);

                matrix.set(r, 0, r00 * x);
                matrix.set(r, 1, r11 * y);
            }

        } else {

            throw new ProgrammingError("Only works for 2D stuff!");
        }
    }

}
