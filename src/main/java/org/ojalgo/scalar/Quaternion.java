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
package org.ojalgo.scalar;

import java.math.BigDecimal;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Mutate2D.ModifiableReceiver;
import org.ojalgo.structure.Transformation2D;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.context.NumberContext;

public final class Quaternion implements SelfDeclaringScalar<Quaternion>, Access2D<Double>, Transformation2D<Double>, Access2D.Collectable<Double, Mutate2D> {

    public enum RotationAxis {

        X(0, new double[] { 1.0, 0.0, 0.0 }), Y(1, new double[] { 0.0, 1.0, 0.0 }), Z(2, new double[] { 0.0, 0.0, 1.0 });

        private final int myIndex;
        private final double[] myVector;

        RotationAxis(final int index, final double[] axis) {
            myIndex = index;
            myVector = axis;
        }

        int index() {
            return myIndex;
        }

        double[] vector() {
            return myVector;
        }

    }

    public static final Scalar.Factory<Quaternion> FACTORY = new Scalar.Factory<>() {

        @Override
        public Quaternion cast(final Comparable<?> number) {
            return Quaternion.valueOf(number);
        }

        @Override
        public Quaternion cast(final double value) {
            return Quaternion.valueOf(value);
        }

        @Override
        public Quaternion convert(final Comparable<?> number) {
            return Quaternion.valueOf(number);
        }

        @Override
        public Quaternion convert(final double value) {
            return Quaternion.valueOf(value);
        }

        @Override
        public Quaternion one() {
            return ONE;
        }

        @Override
        public Quaternion zero() {
            return ZERO;
        }

    };

    public static final Quaternion I = new Quaternion(PrimitiveMath.ONE, PrimitiveMath.ZERO, PrimitiveMath.ZERO);
    public static final Quaternion IJK = new Quaternion(PrimitiveMath.ONE, PrimitiveMath.ONE, PrimitiveMath.ONE).versor();
    public static final Quaternion INFINITY = Quaternion.makePolar(Double.POSITIVE_INFINITY, IJK.vector().toRawCopy1D(), PrimitiveMath.ZERO);
    public static final Quaternion J = new Quaternion(PrimitiveMath.ZERO, PrimitiveMath.ONE, PrimitiveMath.ZERO);
    public static final Quaternion K = new Quaternion(PrimitiveMath.ZERO, PrimitiveMath.ZERO, PrimitiveMath.ONE);
    public static final Quaternion NaN = new Quaternion(PrimitiveMath.NaN, PrimitiveMath.NaN, PrimitiveMath.NaN, PrimitiveMath.NaN);
    public static final Quaternion NEG = new Quaternion(PrimitiveMath.NEG);
    public static final Quaternion ONE = new Quaternion(PrimitiveMath.ONE);
    public static final Quaternion TWO = new Quaternion(PrimitiveMath.TWO);
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

        }
        if (PrimitiveMath.ABS.invoke(tmpAngle - PrimitiveMath.PI) <= ARGUMENT_TOLERANCE) {

            return new Quaternion(-norm);

        }
        double tmpScalar = PrimitiveMath.ZERO;
        if (norm != PrimitiveMath.ZERO) {
            final double tmpCos = PrimitiveMath.COS.invoke(tmpAngle);
            if (tmpCos != PrimitiveMath.ZERO) {
                tmpScalar = norm * tmpCos;
            }
        }

        double tmpI = PrimitiveMath.ZERO;
        double tmpJ = PrimitiveMath.ZERO;
        double tmpK = PrimitiveMath.ZERO;
        if (norm != PrimitiveMath.ZERO) {
            final double tmpSin = PrimitiveMath.SIN.invoke(tmpAngle);
            if (tmpSin != PrimitiveMath.ZERO) {
                tmpI = unit[0] * norm * tmpSin;
                tmpJ = unit[1] * norm * tmpSin;
                tmpK = unit[2] * norm * tmpSin;
            }
        }

        return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);

    }

    public static Quaternion makeRotation(final RotationAxis axis, final double angle) {

        final double tmpScalar = PrimitiveMath.COS.invoke(angle);

        double tmpI = PrimitiveMath.ZERO;
        double tmpJ = PrimitiveMath.ZERO;
        double tmpK = PrimitiveMath.ZERO;

        switch (axis) {

        case X:

            tmpI = PrimitiveMath.SIN.invoke(angle);
            break;

        case Y:

            tmpJ = PrimitiveMath.SIN.invoke(angle);
            break;

        case Z:

            tmpK = PrimitiveMath.SIN.invoke(angle);
            break;

        default:

            throw new ProgrammingError("How could this happen?");
        }

        return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
    }

    public static Quaternion of(final double i, final double j, final double k) {
        return new Quaternion(0.0, i, j, k);
    }

    public static Quaternion of(final double scalar, final double i, final double j, final double k) {
        return new Quaternion(scalar, i, j, k);
    }

    public static Quaternion valueOf(final Comparable<?> number) {

        if (number == null) {
            return ZERO;
        }

        if (number instanceof Quaternion) {
            return (Quaternion) number;
        }

        if (number instanceof ComplexNumber) {
            ComplexNumber tmpComplex = (ComplexNumber) number;
            return new Quaternion(tmpComplex.doubleValue(), tmpComplex.i, PrimitiveMath.ZERO, PrimitiveMath.ZERO);
        }

        return new Quaternion(NumberDefinition.doubleValue(number));
    }

    public static Quaternion valueOf(final double value) {
        return new Quaternion(value);
    }

    public final double i;
    public final double j;
    public final double k;

    private final boolean myPureForSure;
    private final boolean myRealForSure;
    private final double myScalar;

    public Quaternion() {

        super();

        myScalar = PrimitiveMath.ZERO;

        myRealForSure = true;
        myPureForSure = true;

        i = PrimitiveMath.ZERO;
        j = PrimitiveMath.ZERO;
        k = PrimitiveMath.ZERO;
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

    Quaternion(final double scalar) {

        super();

        myScalar = scalar;

        myRealForSure = true;
        myPureForSure = false;

        i = PrimitiveMath.ZERO;
        j = PrimitiveMath.ZERO;
        k = PrimitiveMath.ZERO;
    }

    Quaternion(final double i, final double j, final double k) {

        super();

        myScalar = PrimitiveMath.ZERO;

        myRealForSure = false;
        myPureForSure = true;

        this.i = i;
        this.j = j;
        this.k = k;
    }

    Quaternion(final double scalar, final double i, final double j, final double k) {

        super();

        myScalar = scalar;

        myRealForSure = false;
        myPureForSure = false;

        this.i = i;
        this.j = j;
        this.k = k;
    }

    @Override
    public Quaternion add(final double arg) {

        if (this.isReal()) {
            return new Quaternion(myScalar + arg);
        }
        return new Quaternion(myScalar + arg, i, j, k);
    }

    @Override
    public Quaternion add(final Quaternion arg) {

        if (this.isReal()) {

            return arg.add(myScalar);

        }
        final double tmpScalar = myScalar + arg.scalar();
        final double tmpI = i + arg.i;
        final double tmpJ = j + arg.j;
        final double tmpK = k + arg.k;

        return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
    }

    public double angle() {
        return PrimitiveMath.ACOS.invoke(myScalar / this.norm());
    }

    /**
     * First compares the real values. Only if they are equal will compare the imaginary part.
     */
    @Override
    public int compareTo(final Quaternion other) {

        int retVal = Double.compare(myScalar, other.doubleValue());

        if (retVal != 0) {
            return retVal;
        }
        retVal = Double.compare(i, other.i);

        if (retVal != 0) {
            return retVal;
        }
        retVal = Double.compare(j, other.j);

        if (retVal != 0) {
            return retVal;
        }
        return Double.compare(k, other.k);
    }

    @Override
    public Quaternion conjugate() {

        final double tmpScalar = myScalar;
        final double tmpI = -i;
        final double tmpJ = -j;
        final double tmpK = -k;

        return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
    }

    @Override
    public long count() {
        return 16L;
    }

    @Override
    public long countColumns() {
        return 4L;
    }

    @Override
    public long countRows() {
        return 4L;
    }

    @Override
    public Quaternion divide(final double arg) {

        if (this.isReal()) {

            return new Quaternion(myScalar / arg);

        }
        if (this.isPure()) {

            final double tmpI = i / arg;
            final double tmpJ = j / arg;
            final double tmpK = k / arg;

            return new Quaternion(tmpI, tmpJ, tmpK);

        }
        final double tmpScalar = myScalar / arg;
        final double tmpI = i / arg;
        final double tmpJ = j / arg;
        final double tmpK = k / arg;

        return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
    }

    /**
     * Will calculate <code>this * reciprocal(arg)</code> which is <b>not</b> the same as
     * <code>reciprocal(arg) * this</code>.
     */
    @Override
    public Quaternion divide(final Quaternion arg) {

        final Quaternion tmpReciprocal = arg.invert();

        return this.multiply(tmpReciprocal);
    }

    @Override
    public double doubleValue() {
        return myScalar;
    }

    @Override
    public double doubleValue(final int index) {
        switch (index) {
        case 0:
            return myScalar;
        case 1:
            return i;
        case 2:
            return j;
        case 3:
            return k;
        case 4:
            return -i;
        case 5:
            return myScalar;
        case 6:
            return k;
        case 7:
            return -j;
        case 8:
            return -j;
        case 9:
            return -k;
        case 10:
            return myScalar;
        case 11:
            return i;
        case 12:
            return -k;
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

    @Override
    public double doubleValue(final int row, final int col) {
        if (row == col) {
            return myScalar;
        } else {
            return this.doubleValue(row + col * 4);
        }
    }

    @Override
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
        if (!(obj instanceof Quaternion)) {
            return false;
        }
        Quaternion other = (Quaternion) obj;
        if (Double.doubleToLongBits(myScalar) != Double.doubleToLongBits(other.myScalar) || Double.doubleToLongBits(i) != Double.doubleToLongBits(other.i)
                || Double.doubleToLongBits(j) != Double.doubleToLongBits(other.j) || Double.doubleToLongBits(k) != Double.doubleToLongBits(other.k)) {
            return false;
        }
        return true;
    }

    @Override
    public float floatValue() {
        return (float) myScalar;
    }

    @Override
    public Quaternion get() {
        return this;
    }

    @Override
    public Double get(final long index) {
        return Double.valueOf(this.doubleValue(index));
    }

    @Override
    public Double get(final long row, final long col) {
        return Double.valueOf(this.doubleValue(row, col));
    }

    /**
     * The fourth power of the norm of a quaternion is the determinant of the corresponding matrix.
     */
    public double getDeterminant() {
        final double tmpSumOfSquares = this.calculateSumOfSquaresAll();
        return tmpSumOfSquares * tmpSumOfSquares;
    }

    /**
     * @return A normalised Quaternion with the real/scalar part "removed".
     */
    public Quaternion getPureVersor() {

        final double tmpLength = this.getVectorLength();

        if (tmpLength > 0.0) {
            return new Quaternion(i / tmpLength, j / tmpLength, k / tmpLength);
        }
        return IJK;

    }

    public double getVectorLength() {
        return PrimitiveMath.SQRT.invoke(this.calculateSumOfSquaresVector());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(i);
        result = prime * result + (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(j);
        result = prime * result + (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(k);
        result = prime * result + (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(myScalar);
        return prime * result + (int) (temp ^ temp >>> 32);
    }

    @Override
    public int intValue() {
        return (int) myScalar;
    }

    @Override
    public Quaternion invert() {

        final Quaternion tmpConjugate = this.conjugate();

        final double tmpSumOfSquares = this.calculateSumOfSquaresAll();

        return tmpConjugate.divide(tmpSumOfSquares);
    }

    @Override
    public boolean isAbsolute() {
        if (myRealForSure) {
            return myScalar >= PrimitiveMath.ZERO;
        }
        return !PrimitiveScalar.CONTEXT.isDifferent(myScalar, this.norm());
    }

    public boolean isPure() {
        return myPureForSure || PrimitiveScalar.CONTEXT.isSmall(this.norm(), myScalar);
    }

    public boolean isReal() {
        final NumberContext cntxt = PrimitiveScalar.CONTEXT;
        return myRealForSure || cntxt.isSmall(myScalar, i) && cntxt.isSmall(myScalar, j) && cntxt.isSmall(myScalar, k);
    }

    @Override
    public boolean isSmall(final double comparedTo) {
        return PrimitiveScalar.CONTEXT.isSmall(comparedTo, this.norm());
    }

    @Override
    public long longValue() {
        return (long) myScalar;
    }

    @Override
    public Quaternion multiply(final double arg) {

        if (this.isReal()) {

            return new Quaternion(myScalar * arg);

        }
        if (this.isPure()) {

            final double tmpI = i * arg;
            final double tmpJ = j * arg;
            final double tmpK = k * arg;

            return new Quaternion(tmpI, tmpJ, tmpK);

        }
        final double tmpScalar = myScalar * arg;
        final double tmpI = i * arg;
        final double tmpJ = j * arg;
        final double tmpK = k * arg;

        return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
    }

    @Override
    public Quaternion multiply(final Quaternion arg) {

        if (this.isReal()) {

            return arg.multiply(myScalar);

        }
        final double tmpScalar = myScalar * arg.scalar() - i * arg.i - j * arg.j - k * arg.k;
        final double tmpI = myScalar * arg.i + i * arg.scalar() + j * arg.k - k * arg.j;
        final double tmpJ = myScalar * arg.j - i * arg.k + j * arg.scalar() + k * arg.i;
        final double tmpK = myScalar * arg.k + i * arg.j - j * arg.i + k * arg.scalar();

        return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
    }

    @Override
    public Quaternion negate() {

        final double tmpScalar = -myScalar;
        final double tmpI = -i;
        final double tmpJ = -j;
        final double tmpK = -k;

        return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
    }

    @Override
    public double norm() {
        return PrimitiveMath.SQRT.invoke(this.calculateSumOfSquaresAll());
    }

    @Override
    public Quaternion power(final int power) {

        Quaternion retVal = ONE;

        for (int p = 0; p < power; p++) {
            retVal = retVal.multiply(this);
        }

        return retVal;
    }

    public double scalar() {
        return myScalar;
    }

    @Override
    public Quaternion signum() {
        return this.versor();
    }

    @Override
    public Quaternion subtract(final double arg) {

        if (this.isReal()) {
            return new Quaternion(myScalar - arg);
        }
        return new Quaternion(myScalar - arg, i, j, k);
    }

    @Override
    public Quaternion subtract(final Quaternion arg) {

        final double tmpScalar = myScalar - arg.scalar();
        final double tmpI = i - arg.i;
        final double tmpJ = j - arg.j;
        final double tmpK = k - arg.k;

        return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
    }

    @Override
    public void supplyTo(final Mutate2D receiver) {
        receiver.set(0L, myScalar);
        receiver.set(1L, i);
        receiver.set(2L, j);
        receiver.set(3L, k);
        receiver.set(4L, -i);
        receiver.set(5L, myScalar);
        receiver.set(6L, k);
        receiver.set(7L, -j);
        receiver.set(8L, -j);
        receiver.set(9L, -k);
        receiver.set(10L, myScalar);
        receiver.set(11L, i);
        receiver.set(12L, -k);
        receiver.set(13L, j);
        receiver.set(14L, -i);
        receiver.set(15L, myScalar);
    }

    @Override
    public BigDecimal toBigDecimal() {
        return new BigDecimal(myScalar, PrimitiveScalar.CONTEXT.getMathContext());
    }

    public MatrixStore<ComplexNumber> toComplexMatrix() {

        final GenericStore<ComplexNumber> retVal = GenericStore.C128.make(2L, 2L);

        retVal.set(0L, ComplexNumber.of(myScalar, i));
        retVal.set(1L, ComplexNumber.of(-j, k));
        retVal.set(2L, ComplexNumber.of(j, k));
        retVal.set(3L, ComplexNumber.of(myScalar, -i));

        return retVal;
    }

    public MatrixStore<Double> toMultiplicationMatrix() {
        final Primitive64Store retVal = Primitive64Store.FACTORY.make(this);
        this.supplyTo(retVal);
        return retVal;
    }

    public MatrixStore<Double> toMultiplicationVector() {

        final Primitive64Store retVal = Primitive64Store.FACTORY.make(4L, 1L);

        retVal.set(0L, myScalar);
        retVal.set(1L, i);
        retVal.set(2L, j);
        retVal.set(3L, k);

        return retVal;
    }

    public MatrixStore<Double> toRotationMatrix() {

        final Primitive64Store retVal = Primitive64Store.FACTORY.make(3L, 3L);

        final double s = myScalar;

        final double ss = s * s;
        final double ii = i * i;
        final double jj = j * j;
        final double kk = k * k;

        double tmp1;
        double tmp2;

        final double invs = 1.0 / (ii + jj + kk + ss);

        final double r00 = (ii + ss - (jj + kk)) * invs;
        final double r11 = (jj + ss - (ii + kk)) * invs;
        final double r22 = (kk + ss - (ii + jj)) * invs;

        tmp1 = i * j;
        tmp2 = k * s;
        final double r10 = 2.0 * (tmp1 + tmp2) * invs;
        final double r01 = 2.0 * (tmp1 - tmp2) * invs;

        tmp1 = i * k;
        tmp2 = j * s;
        final double r20 = 2.0 * (tmp1 - tmp2) * invs;
        final double r02 = 2.0 * (tmp1 + tmp2) * invs;

        tmp1 = j * k;
        tmp2 = i * s;
        final double r21 = 2.0 * (tmp1 + tmp2) * invs;
        final double r12 = 2.0 * (tmp1 - tmp2) * invs;

        retVal.set(0L, r00);
        retVal.set(1L, r10);
        retVal.set(2L, r20);
        retVal.set(3L, r01);
        retVal.set(4L, r11);
        retVal.set(5L, r21);
        retVal.set(6L, r02);
        retVal.set(7L, r12);
        retVal.set(8L, r22);

        return retVal;
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
        retVal.append(Double.toString(PrimitiveMath.ABS.invoke(i)));
        retVal.append("i");

        if (j < PrimitiveMath.ZERO) {
            retVal.append(" - ");
        } else {
            retVal.append(" + ");
        }
        retVal.append(Double.toString(PrimitiveMath.ABS.invoke(j)));
        retVal.append("j");

        if (k < PrimitiveMath.ZERO) {
            retVal.append(" - ");
        } else {
            retVal.append(" + ");
        }
        retVal.append(Double.toString(PrimitiveMath.ABS.invoke(k)));
        retVal.append("k)");

        return retVal.toString();
    }

    @Override
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

    @Override
    public <T extends ModifiableReceiver<Double>> void transform(final T transformable) {

        final double s = myScalar;

        final double ss = s * s;
        final double ii = i * i;
        final double jj = j * j;
        final double kk = k * k;

        double tmp1;
        double tmp2;

        final double invs = 1.0 / (ii + jj + kk + ss);

        final double r00 = (ii + ss - (jj + kk)) * invs;
        final double r11 = (jj + ss - (ii + kk)) * invs;
        final double r22 = (kk + ss - (ii + jj)) * invs;

        tmp1 = i * j;
        tmp2 = k * s;
        final double r10 = 2.0 * (tmp1 + tmp2) * invs;
        final double r01 = 2.0 * (tmp1 - tmp2) * invs;

        tmp1 = i * k;
        tmp2 = j * s;
        final double r20 = 2.0 * (tmp1 - tmp2) * invs;
        final double r02 = 2.0 * (tmp1 + tmp2) * invs;

        tmp1 = j * k;
        tmp2 = i * s;
        final double r21 = 2.0 * (tmp1 + tmp2) * invs;
        final double r12 = 2.0 * (tmp1 - tmp2) * invs;

        if (transformable.count() == 3L) {

            final double x = transformable.doubleValue(0);
            final double y = transformable.doubleValue(1);
            final double z = transformable.doubleValue(2);

            transformable.set(0, r00 * x + r01 * y + r02 * z);
            transformable.set(1, r10 * x + r11 * y + r12 * z);
            transformable.set(2, r20 * x + r21 * y + r22 * z);

        } else if (transformable.countRows() == 3L) {

            for (long c = 0L, limit = transformable.countColumns(); c < limit; c++) {

                final double x = transformable.doubleValue(0, c);
                final double y = transformable.doubleValue(1, c);
                final double z = transformable.doubleValue(2, c);

                transformable.set(0, c, r00 * x + r01 * y + r02 * z);
                transformable.set(1, c, r10 * x + r11 * y + r12 * z);
                transformable.set(2, c, r20 * x + r21 * y + r22 * z);
            }

        } else if (transformable.countColumns() == 3L) {

            for (long r = 0L, limit = transformable.countRows(); r < limit; r++) {

                final double x = transformable.doubleValue(r, 0);
                final double y = transformable.doubleValue(r, 1);
                final double z = transformable.doubleValue(r, 2);

                transformable.set(r, 0, r00 * x + r01 * y + r02 * z);
                transformable.set(r, 1, r10 * x + r11 * y + r12 * z);
                transformable.set(r, 2, r20 * x + r21 * y + r22 * z);
            }

        } else {

            throw new ProgrammingError("Only works for 3D stuff!");
        }
    }

    public double[] unit() {
        final double tmpLength = this.getVectorLength();
        if (tmpLength > 0.0) {
            return new double[] { i / tmpLength, j / tmpLength, k / tmpLength };
        }
        return new double[] { IJK.i, IJK.j, IJK.k };
    }

    public PhysicalStore<Double> vector() {

        final Primitive64Store retVal = Primitive64Store.FACTORY.make(3L, 1L);

        retVal.set(0L, i);
        retVal.set(1L, j);
        retVal.set(2L, k);

        return retVal;
    }

    public Quaternion versor() {

        final double norm = this.norm();

        if (this.isReal()) {
            return new Quaternion(myScalar / norm);
        }
        if (this.isPure()) {
            return new Quaternion(i / norm, j / norm, k / norm);
        }
        return new Quaternion(myScalar / norm, i / norm, j / norm, k / norm);
    }

    private double calculateSumOfSquaresAll() {
        return myScalar * myScalar + this.calculateSumOfSquaresVector();
    }

    private double calculateSumOfSquaresVector() {
        return i * i + j * j + k * k;
    }

    MatrixStore<Double> toRotationMatrixVersor() {

        final Primitive64Store retVal = Primitive64Store.FACTORY.make(3L, 3L);

        final double s = this.doubleValue();

        final double ss = s * s;
        final double ii = i * i;
        final double jj = j * j;
        final double kk = k * k;

        double tmp1;
        double tmp2;

        final double r00 = ii + ss - (jj + kk);
        final double r11 = jj + ss - (ii + kk);
        final double r22 = kk + ss - (ii + jj);

        tmp1 = i * j;
        tmp2 = k * s;
        final double r10 = 2.0 * (tmp1 + tmp2);
        final double r01 = 2.0 * (tmp1 - tmp2);

        tmp1 = i * k;
        tmp2 = j * s;
        final double r20 = 2.0 * (tmp1 - tmp2);
        final double r02 = 2.0 * (tmp1 + tmp2);

        tmp1 = j * k;
        tmp2 = i * s;
        final double r21 = 2.0 * (tmp1 + tmp2);
        final double r12 = 2.0 * (tmp1 - tmp2);

        retVal.set(0L, r00);
        retVal.set(1L, r10);
        retVal.set(2L, r20);
        retVal.set(3L, r01);
        retVal.set(4L, r11);
        retVal.set(5L, r21);
        retVal.set(6L, r02);
        retVal.set(7L, r12);
        retVal.set(8L, r22);

        return retVal;
    }

    <T extends ModifiableReceiver<Double>> void transformVersor(final T transformable) {

        final double s = this.doubleValue();

        final double ss = s * s;
        final double ii = i * i;
        final double jj = j * j;
        final double kk = k * k;

        double tmp1;
        double tmp2;

        final double r00 = ii + ss - (jj + kk);
        final double r11 = jj + ss - (ii + kk);
        final double r22 = kk + ss - (ii + jj);

        tmp1 = i * j;
        tmp2 = k * s;
        final double r10 = 2.0 * (tmp1 + tmp2);
        final double r01 = 2.0 * (tmp1 - tmp2);

        tmp1 = i * k;
        tmp2 = j * s;
        final double r20 = 2.0 * (tmp1 - tmp2);
        final double r02 = 2.0 * (tmp1 + tmp2);

        tmp1 = j * k;
        tmp2 = i * s;
        final double r21 = 2.0 * (tmp1 + tmp2);
        final double r12 = 2.0 * (tmp1 - tmp2);

        if (transformable.count() == 3L) {

            final double x = transformable.doubleValue(0);
            final double y = transformable.doubleValue(1);
            final double z = transformable.doubleValue(2);

            transformable.set(0, r00 * x + r01 * y + r02 * z);
            transformable.set(1, r10 * x + r11 * y + r12 * z);
            transformable.set(2, r20 * x + r21 * y + r22 * z);

        } else if (transformable.countRows() == 3L) {

            for (long c = 0L, limit = transformable.countColumns(); c < limit; c++) {

                final double x = transformable.doubleValue(0, c);
                final double y = transformable.doubleValue(1, c);
                final double z = transformable.doubleValue(2, c);

                transformable.set(0, c, r00 * x + r01 * y + r02 * z);
                transformable.set(1, c, r10 * x + r11 * y + r12 * z);
                transformable.set(2, c, r20 * x + r21 * y + r22 * z);
            }

        } else if (transformable.countColumns() == 3L) {

            for (long r = 0L, limit = transformable.countRows(); r < limit; r++) {

                final double x = transformable.doubleValue(r, 0);
                final double y = transformable.doubleValue(r, 1);
                final double z = transformable.doubleValue(r, 2);

                transformable.set(r, 0, r00 * x + r01 * y + r02 * z);
                transformable.set(r, 1, r10 * x + r11 * y + r12 * z);
                transformable.set(r, 2, r20 * x + r21 * y + r22 * z);
            }

        } else {

            throw new ProgrammingError("Only works for 3D stuff!");
        }
    }

}
