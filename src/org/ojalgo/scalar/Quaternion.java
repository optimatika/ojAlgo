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
package org.ojalgo.scalar;

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.context.NumberContext.Enforceable;

public final class Quaternion extends Number implements Scalar<Quaternion>, Enforceable<Quaternion>, Access2D<Double> {

    public static final Scalar.Factory<Quaternion> FACTORY = new Scalar.Factory<Quaternion>() {

        public Quaternion cast(final double value) {
            return Quaternion.valueOf(value);
        }

        public Quaternion cast(final Number number) {
            return Quaternion.valueOf(number);
        }

        public Quaternion convert(final double value) {
            return Quaternion.valueOf(value);
        }

        public Quaternion convert(final Number number) {
            return Quaternion.valueOf(number);
        }

        public Quaternion one() {
            return ONE;
        }

        public Quaternion zero() {
            return ZERO;
        }

    };

    public static final Quaternion I = new Quaternion(PrimitiveMath.ONE, PrimitiveMath.ZERO, PrimitiveMath.ZERO);
    public static final Quaternion IJK = new Quaternion(PrimitiveMath.ONE, PrimitiveMath.ONE, PrimitiveMath.ONE).versor();
    public static final Quaternion INFINITY = Quaternion.makePolar(Double.POSITIVE_INFINITY, IJK.vector(), PrimitiveMath.ZERO);
    public static final Quaternion J = new Quaternion(PrimitiveMath.ZERO, PrimitiveMath.ONE, PrimitiveMath.ZERO);
    public static final Quaternion K = new Quaternion(PrimitiveMath.ZERO, PrimitiveMath.ZERO, PrimitiveMath.ONE);
    public static final Quaternion NEG = new Quaternion(PrimitiveMath.NEG);
    public static final Quaternion ONE = new Quaternion(PrimitiveMath.ONE);
    public static final Quaternion ZERO = new Quaternion();

    private static final double ARGUMENT_TOLERANCE = PrimitiveMath.PI * PrimitiveScalar.CONTEXT.epsilon();

    public static boolean isAbsolute(final Quaternion value) {
        return value.isAbsolute();
    }

    public static boolean isInfinite(final Quaternion value) {
        return Double.isInfinite(value.doubleValue()) || Double.isInfinite(value.i) || Double.isInfinite(value.j) || Double.isInfinite(value.k)
                || Double.isInfinite(value.norm());
    }

    public static boolean isNaN(final Quaternion value) {
        return Double.isNaN(value.doubleValue()) || Double.isNaN(value.i) || Double.isNaN(value.j) || Double.isNaN(value.k);
    }

    public static boolean isReal(final Quaternion value) {
        return value.isReal();
    }

    public static boolean isSmall(final double comparedTo, final Quaternion value) {
        return value.isSmall(comparedTo);
    }

    public static Quaternion makePolar(final double norm, final double[] unit, final double angle) {

        double tmpAngle = angle % PrimitiveMath.TWO_PI;
        if (tmpAngle < PrimitiveMath.ZERO) {
            tmpAngle += PrimitiveMath.TWO_PI;
        }

        //  BasicLogger.debug("Koordinater: {} {} {}", norm, Arrays.toString(unitVector), phase);

        if (tmpAngle <= ARGUMENT_TOLERANCE) {

            return new Quaternion(norm);

        } else if (Math.abs(tmpAngle - PrimitiveMath.PI) <= ARGUMENT_TOLERANCE) {

            return new Quaternion(-norm);

        } else {

            double tmpScalar = PrimitiveMath.ZERO;
            if (norm != PrimitiveMath.ZERO) {
                final double tmpCos = Math.cos(tmpAngle);
                if (tmpCos != PrimitiveMath.ZERO) {
                    tmpScalar = norm * tmpCos;
                }
            }

            double tmpI = PrimitiveMath.ZERO;
            double tmpJ = PrimitiveMath.ZERO;
            double tmpK = PrimitiveMath.ZERO;
            if (norm != PrimitiveMath.ZERO) {
                final double tmpSin = Math.sin(tmpAngle);
                if (tmpSin != PrimitiveMath.ZERO) {
                    tmpI = unit[0] * norm * tmpSin;
                    tmpJ = unit[1] * norm * tmpSin;
                    tmpK = unit[2] * norm * tmpSin;
                }
            }

            return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
        }

    }

    public static Quaternion of(final double scalar, final double i, final double j, final double k) {
        return new Quaternion(scalar, i, j, k);
    }

    public static Quaternion valueOf(final double value) {
        return new Quaternion(value);
    }

    public static Quaternion valueOf(final Number number) {

        if (number != null) {

            if (number instanceof Quaternion) {

                return (Quaternion) number;

            } else if (number instanceof ComplexNumber) {

                final ComplexNumber tmpComplex = (ComplexNumber) number;
                return new Quaternion(tmpComplex.doubleValue(), tmpComplex.i, PrimitiveMath.ZERO, PrimitiveMath.ZERO);

            } else {

                return new Quaternion(number.doubleValue());
            }

        } else {

            return ZERO;
        }
    }

    public final double i;
    public final double j;
    public final double k;

    private final boolean myPureForSure;
    private final boolean myRealForSure;
    private final double myScalar;

    private Quaternion(final double scalar) {

        super();

        myScalar = scalar;

        myRealForSure = true;
        myPureForSure = false;

        i = PrimitiveMath.ZERO;
        j = PrimitiveMath.ZERO;
        k = PrimitiveMath.ZERO;
    }

    private Quaternion(final double i, final double j, final double k) {

        super();

        myScalar = PrimitiveMath.ZERO;

        myRealForSure = false;
        myPureForSure = true;

        this.i = i;
        this.j = j;
        this.k = k;
    }

    private Quaternion(final double scalar, final double i, final double j, final double k) {

        super();

        myScalar = scalar;

        myRealForSure = false;
        myPureForSure = false;

        this.i = i;
        this.j = j;
        this.k = k;
    }

    private Quaternion(final double scalar, final double[] vector) {

        super();

        myScalar = scalar;

        myRealForSure = false;
        myPureForSure = false;

        i = vector[0];
        j = vector[1];
        k = vector[2];
    }

    private Quaternion(final double[] vector) {

        super();

        myScalar = PrimitiveMath.ZERO;

        myRealForSure = false;
        myPureForSure = true;

        i = vector[0];
        j = vector[1];
        k = vector[2];
    }

    Quaternion() {

        super();

        myScalar = PrimitiveMath.ZERO;

        myRealForSure = true;
        myPureForSure = true;

        i = PrimitiveMath.ZERO;
        j = PrimitiveMath.ZERO;
        k = PrimitiveMath.ZERO;
    }

    public Quaternion add(final double arg) {

        if (this.isReal()) {
            return new Quaternion(myScalar + arg);
        } else {
            return new Quaternion(myScalar + arg, i, j, k);
        }
    }

    public Quaternion add(final Quaternion arg) {

        if (this.isReal()) {

            return arg.add(myScalar);

        } else {

            final double tmpScalar = myScalar + arg.scalar();
            final double tmpI = i + arg.i;
            final double tmpJ = j + arg.j;
            final double tmpK = k + arg.k;

            return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
        }
    }

    public double angle() {
        return Math.acos(myScalar / this.norm());
    }

    public MatrixStore<ComplexNumber> asComplex2D() {

        final ComplexDenseStore retVal = ComplexDenseStore.FACTORY.makeZero(2L, 2L);

        retVal.set(0L, ComplexNumber.of(myScalar, i));
        retVal.set(1L, ComplexNumber.of(-j, k));
        retVal.set(2L, ComplexNumber.of(j, k));
        retVal.set(3L, ComplexNumber.of(myScalar, -i));

        return retVal;
    }

    public int compareTo(final Quaternion reference) {

        int retVal = 0;

        if ((retVal = Double.compare(this.norm(), reference.norm())) == 0) {
            if ((retVal = Double.compare(myScalar, reference.scalar())) == 0) {
                if ((retVal = Double.compare(i, reference.i)) == 0) {
                    if ((retVal = Double.compare(j, reference.j)) == 0) {
                        retVal = Double.compare(k, reference.k);
                    }
                }
            }
        }

        return retVal;
    }

    public Quaternion conjugate() {

        final double tmpScalar = myScalar;
        final double tmpI = -i;
        final double tmpJ = -j;
        final double tmpK = -k;

        return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
    }

    public long count() {
        return 16L;
    }

    public long countColumns() {
        return 4L;
    }

    public long countRows() {
        return 4L;
    }

    public Quaternion divide(final double arg) {

        if (this.isReal()) {

            return new Quaternion(myScalar / arg);

        } else if (this.isPure()) {

            final double tmpI = i / arg;
            final double tmpJ = j / arg;
            final double tmpK = k / arg;

            return new Quaternion(tmpI, tmpJ, tmpK);

        } else {

            final double tmpScalar = myScalar / arg;
            final double tmpI = i / arg;
            final double tmpJ = j / arg;
            final double tmpK = k / arg;

            return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
        }
    }

    /**
     * Will calculate <code>this * reciprocal(arg)</code> which is <b>not</b> the same as
     * <code>reciprocal(arg) * this</code>.
     *
     * @see org.ojalgo.scalar.Scalar#divide(java.lang.Number)
     */
    public Quaternion divide(final Quaternion arg) {

        final Quaternion tmpReciprocal = arg.invert();

        return this.multiply(tmpReciprocal);
    }

    @Override
    public double doubleValue() {
        return myScalar;
    }

    public double doubleValue(final long index) {
        switch ((int) index) {
        case 0:
            return myScalar;
        case 1:
            return -i;
        case 2:
            return -j;
        case 3:
            return -k;
        case 4:
            return i;
        case 5:
            return myScalar;
        case 6:
            return k;
        case 7:
            return -j;
        case 8:
            return j;
        case 9:
            return -k;
        case 10:
            return myScalar;
        case 11:
            return i;
        case 12:
            return k;
        case 13:
            return j;
        case 14:
            return -i;
        case 15:
            return myScalar;
        default:
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public double doubleValue(final long row, final long column) {
        if (row == column) {
            return myScalar;
        } else {
            return this.doubleValue(row + (column * 4L));
        }
    }

    public Quaternion enforce(final NumberContext context) {

        final double tmpScalar = context.enforce(myScalar);
        final double tmpI = context.enforce(i);
        final double tmpJ = context.enforce(j);
        final double tmpK = context.enforce(k);

        return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Quaternion)) {
            return false;
        }
        final Quaternion other = (Quaternion) obj;
        if (Double.doubleToLongBits(myScalar) != Double.doubleToLongBits(other.myScalar)) {
            return false;
        }
        if (Double.doubleToLongBits(i) != Double.doubleToLongBits(other.i)) {
            return false;
        }
        if (Double.doubleToLongBits(j) != Double.doubleToLongBits(other.j)) {
            return false;
        }
        if (Double.doubleToLongBits(k) != Double.doubleToLongBits(other.k)) {
            return false;
        }
        return true;
    }

    @Override
    public float floatValue() {
        return (float) myScalar;
    }

    public Double get(final long index) {
        return this.doubleValue(index);
    }

    public Double get(final long row, final long column) {
        return this.doubleValue(row, column);
    }

    /**
     * The fourth power of the norm of a quaternion is the determinant of the corresponding matrix.
     */
    public double getDeterminant() {
        final double tmpSumOfSquares = this.calculateSumOfSquaresAll();
        return tmpSumOfSquares * tmpSumOfSquares;
    }

    public Quaternion getNumber() {
        return this;
    }

    /**
     * @return A normalised Quaternion with the real/scalar part "removed".
     */
    public Quaternion getPureVersor() {

        final double tmpLength = this.getVectorLength();

        if (tmpLength > 0.0) {
            return new Quaternion(i / tmpLength, j / tmpLength, k / tmpLength);
        } else {
            return IJK;
        }

    }

    public double getVectorLength() {
        return Math.sqrt(this.calculateSumOfSquaresVector());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(myScalar);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(i);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(j);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(k);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public int intValue() {
        return (int) myScalar;
    }

    public Quaternion invert() {

        final Quaternion tmpConjugate = this.conjugate();

        final double tmpSumOfSquares = this.calculateSumOfSquaresAll();

        return tmpConjugate.divide(tmpSumOfSquares);
    }

    public boolean isAbsolute() {
        if (myRealForSure) {
            return myScalar >= PrimitiveMath.ZERO;
        } else {
            return !PrimitiveScalar.CONTEXT.isDifferent(myScalar, this.norm());
        }
    }

    public boolean isPure() {
        return myPureForSure || PrimitiveScalar.CONTEXT.isSmall(this.norm(), myScalar);
    }

    public boolean isReal() {
        final NumberContext tmpCntxt = PrimitiveScalar.CONTEXT;
        return myRealForSure || (tmpCntxt.isSmall(myScalar, i) && tmpCntxt.isSmall(myScalar, j) && tmpCntxt.isSmall(myScalar, k));
    }

    public boolean isSmall(final double comparedTo) {
        return PrimitiveScalar.CONTEXT.isSmall(comparedTo, this.norm());
    }

    @Override
    public long longValue() {
        return (long) myScalar;
    }

    public Quaternion multiply(final double arg) {

        if (this.isReal()) {

            return new Quaternion(myScalar * arg);

        } else if (this.isPure()) {

            final double tmpI = i * arg;
            final double tmpJ = j * arg;
            final double tmpK = k * arg;

            return new Quaternion(tmpI, tmpJ, tmpK);

        } else {

            final double tmpScalar = myScalar * arg;
            final double tmpI = i * arg;
            final double tmpJ = j * arg;
            final double tmpK = k * arg;

            return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
        }
    }

    public Quaternion multiply(final Quaternion arg) {

        if (this.isReal()) {

            return arg.multiply(myScalar);

        } else {

            final double tmpScalar = (myScalar * arg.scalar()) - (i * arg.i) - (j * arg.j) - (k * arg.k);
            final double tmpI = ((myScalar * arg.i) + (i * arg.scalar()) + (j * arg.k)) - (k * arg.j);
            final double tmpJ = ((myScalar * arg.j) - (i * arg.k)) + (j * arg.scalar()) + (k * arg.i);
            final double tmpK = (((myScalar * arg.k) + (i * arg.j)) - (j * arg.i)) + (k * arg.scalar());

            return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
        }
    }

    public Quaternion negate() {

        final double tmpScalar = -myScalar;
        final double tmpI = -i;
        final double tmpJ = -j;
        final double tmpK = -k;

        return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
    }

    public double norm() {
        return Math.sqrt(this.calculateSumOfSquaresAll());
    }

    public double scalar() {
        return myScalar;
    }

    public Quaternion signum() {
        return this.versor();
    }

    public Quaternion subtract(final double arg) {

        if (this.isReal()) {
            return new Quaternion(myScalar - arg);
        } else {
            return new Quaternion(myScalar - arg, i, j, k);
        }
    }

    public Quaternion subtract(final Quaternion arg) {

        final double tmpScalar = myScalar - arg.scalar();
        final double tmpI = i - arg.i;
        final double tmpJ = j - arg.j;
        final double tmpK = k - arg.k;

        return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal(myScalar, PrimitiveScalar.CONTEXT.getMathContext());
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder retVal = new StringBuilder("(");

        retVal.append(Double.toString(myScalar));

        if (i < PrimitiveMath.ZERO) {
            retVal.append(" - ");
        } else {
            retVal.append(" + ");
        }
        retVal.append(Double.toString(Math.abs(i)));
        retVal.append("i");

        if (j < PrimitiveMath.ZERO) {
            retVal.append(" - ");
        } else {
            retVal.append(" + ");
        }
        retVal.append(Double.toString(Math.abs(j)));
        retVal.append("j");

        if (k < PrimitiveMath.ZERO) {
            retVal.append(" - ");
        } else {
            retVal.append(" + ");
        }
        retVal.append(Double.toString(Math.abs(k)));
        retVal.append("k)");

        return retVal.toString();
    }

    public String toString(final NumberContext context) {

        final StringBuilder retVal = new StringBuilder("(");

        final BigDecimal tmpScalar = context.enforce(new BigDecimal(myScalar, PrimitiveScalar.CONTEXT.getMathContext()));
        final BigDecimal tmpI = context.enforce(new BigDecimal(i, PrimitiveScalar.CONTEXT.getMathContext()));
        final BigDecimal tmpJ = context.enforce(new BigDecimal(j, PrimitiveScalar.CONTEXT.getMathContext()));
        final BigDecimal tmpK = context.enforce(new BigDecimal(k, PrimitiveScalar.CONTEXT.getMathContext()));

        retVal.append(tmpScalar.toString());

        if (tmpI.signum() < 0) {
            retVal.append(" - ");
        } else {
            retVal.append(" + ");
        }
        retVal.append(tmpI.abs().toString());
        retVal.append("i");

        if (tmpJ.signum() < 0) {
            retVal.append(" - ");
        } else {
            retVal.append(" + ");
        }
        retVal.append(tmpJ.abs().toString());
        retVal.append("j");

        if (tmpK.signum() < 0) {
            retVal.append(" - ");
        } else {
            retVal.append(" + ");
        }
        retVal.append(tmpK.abs().toString());
        retVal.append("k)");

        return retVal.toString();
    }

    public double[] unit() {
        final double tmpLength = this.getVectorLength();
        if (tmpLength > 0.0) {
            return new double[] { i / tmpLength, j / tmpLength, k / tmpLength };
        } else {
            return IJK.vector();
        }
    }

    public double[] vector() {
        return new double[] { i, j, k };
    }

    public Quaternion versor() {
        return this.divide(this.norm());
    }

    private double calculateSumOfSquaresAll() {
        return (myScalar * myScalar) + this.calculateSumOfSquaresVector();
    }

    private double calculateSumOfSquaresVector() {
        return (i * i) + (j * j) + (k * k);
    }

}
