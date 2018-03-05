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

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Mutate2D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.transformation.TransformationMatrix;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.context.NumberContext.Enforceable;

public class Quaternion extends Number implements Scalar<Quaternion>, Enforceable<Quaternion>, Access2D<Double>,
        TransformationMatrix<Double, PhysicalStore<Double>>, Access2D.Collectable<Double, Mutate2D.Receiver<Double>> {

    public static enum RotationAxis {

        X(0, new double[] { 1.0, 0.0, 0.0 }), Y(1, new double[] { 0.0, 1.0, 0.0 }), Z(2, new double[] { 0.0, 0.0, 1.0 });

        private final int myIndex;
        private final double[] myVector;

        private RotationAxis(final int index, final double[] axis) {
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

    public static final class Versor extends Quaternion {

        Versor(final double scalar) {
            super(scalar);
        }

        Versor(final double i, final double j, final double k) {
            super(i, j, k);
        }

        Versor(final double scalar, final double i, final double j, final double k) {
            super(scalar, i, j, k);
        }

        @Override
        public double norm() {
            return PrimitiveMath.ONE;
        }

        @Override
        public MatrixStore<Double> toRotationMatrix() {

            final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(3L, 3L);

            final double s = this.doubleValue();

            final double ss = s * s;
            final double ii = i * i;
            final double jj = j * j;
            final double kk = k * k;

            double tmp1;
            double tmp2;

            final double r00 = ((ii + ss) - (jj + kk));
            final double r11 = ((jj + ss) - (ii + kk));
            final double r22 = ((kk + ss) - (ii + jj));

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

        @Override
        public void transform(final PhysicalStore<Double> matrix) {

            final double s = this.doubleValue();

            final double ss = s * s;
            final double ii = i * i;
            final double jj = j * j;
            final double kk = k * k;

            double tmp1;
            double tmp2;

            final double r00 = ((ii + ss) - (jj + kk));
            final double r11 = ((jj + ss) - (ii + kk));
            final double r22 = ((kk + ss) - (ii + jj));

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

            if (matrix.count() == 3L) {

                final double x = matrix.doubleValue(0);
                final double y = matrix.doubleValue(1);
                final double z = matrix.doubleValue(2);

                matrix.set(0, (r00 * x) + (r01 * y) + (r02 * z));
                matrix.set(1, (r10 * x) + (r11 * y) + (r12 * z));
                matrix.set(2, (r20 * x) + (r21 * y) + (r22 * z));

            } else if (matrix.countRows() == 3L) {

                for (long c = 0L, limit = matrix.countColumns(); c < limit; c++) {

                    final double x = matrix.doubleValue(0, c);
                    final double y = matrix.doubleValue(1, c);
                    final double z = matrix.doubleValue(2, c);

                    matrix.set(0, c, (r00 * x) + (r01 * y) + (r02 * z));
                    matrix.set(1, c, (r10 * x) + (r11 * y) + (r12 * z));
                    matrix.set(2, c, (r20 * x) + (r21 * y) + (r22 * z));
                }

            } else if (matrix.countColumns() == 3L) {

                for (long r = 0L, limit = matrix.countRows(); r < limit; r++) {

                    final double x = matrix.doubleValue(r, 0);
                    final double y = matrix.doubleValue(r, 1);
                    final double z = matrix.doubleValue(r, 2);

                    matrix.set(r, 0, (r00 * x) + (r01 * y) + (r02 * z));
                    matrix.set(r, 1, (r10 * x) + (r11 * y) + (r12 * z));
                    matrix.set(r, 2, (r20 * x) + (r21 * y) + (r22 * z));
                }

            } else {

                throw new ProgrammingError("Only works for 3D stuff!");
            }
        }

        @Override
        public Versor versor() {
            return this;
        }

    }

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

    public static final Quaternion I = new Versor(PrimitiveMath.ONE, PrimitiveMath.ZERO, PrimitiveMath.ZERO);
    public static final Quaternion IJK = new Quaternion(PrimitiveMath.ONE, PrimitiveMath.ONE, PrimitiveMath.ONE).versor();
    public static final Quaternion INFINITY = Quaternion.makePolar(Double.POSITIVE_INFINITY, IJK.vector().toRawCopy1D(), PrimitiveMath.ZERO);
    public static final Quaternion J = new Versor(PrimitiveMath.ZERO, PrimitiveMath.ONE, PrimitiveMath.ZERO);
    public static final Quaternion K = new Versor(PrimitiveMath.ZERO, PrimitiveMath.ZERO, PrimitiveMath.ONE);
    public static final Quaternion NEG = new Versor(PrimitiveMath.NEG);
    public static final Quaternion ONE = new Versor(PrimitiveMath.ONE);
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

        } else if (PrimitiveFunction.ABS.invoke(tmpAngle - PrimitiveMath.PI) <= ARGUMENT_TOLERANCE) {

            return new Quaternion(-norm);

        } else {

            double tmpScalar = PrimitiveMath.ZERO;
            if (norm != PrimitiveMath.ZERO) {
                final double tmpCos = PrimitiveFunction.COS.invoke(tmpAngle);
                if (tmpCos != PrimitiveMath.ZERO) {
                    tmpScalar = norm * tmpCos;
                }
            }

            double tmpI = PrimitiveMath.ZERO;
            double tmpJ = PrimitiveMath.ZERO;
            double tmpK = PrimitiveMath.ZERO;
            if (norm != PrimitiveMath.ZERO) {
                final double tmpSin = PrimitiveFunction.SIN.invoke(tmpAngle);
                if (tmpSin != PrimitiveMath.ZERO) {
                    tmpI = unit[0] * norm * tmpSin;
                    tmpJ = unit[1] * norm * tmpSin;
                    tmpK = unit[2] * norm * tmpSin;
                }
            }

            return new Quaternion(tmpScalar, tmpI, tmpJ, tmpK);
        }

    }

    public static Versor makeRotation(final RotationAxis axis, final double angle) {

        final double tmpScalar = PrimitiveFunction.COS.invoke(angle);

        double tmpI = PrimitiveMath.ZERO;
        double tmpJ = PrimitiveMath.ZERO;
        double tmpK = PrimitiveMath.ZERO;

        switch (axis) {

        case X:

            tmpI = PrimitiveFunction.SIN.invoke(angle);
            break;

        case Y:

            tmpJ = PrimitiveFunction.SIN.invoke(angle);
            break;

        case Z:

            tmpK = PrimitiveFunction.SIN.invoke(angle);
            break;

        default:

            throw new ProgrammingError("How could this happen?");
        }

        return new Versor(tmpScalar, tmpI, tmpJ, tmpK);
    }

    public static Quaternion of(final double i, final double j, final double k) {
        return new Quaternion(0.0, i, j, k);
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
        return PrimitiveFunction.ACOS.invoke(myScalar / this.norm());
    }

    public int compareTo(final Quaternion reference) {

        int retVal = 0;

        if ((retVal = NumberContext.compare(this.norm(), reference.norm())) == 0) {
            if ((retVal = NumberContext.compare(myScalar, reference.scalar())) == 0) {
                if ((retVal = NumberContext.compare(i, reference.i)) == 0) {
                    if ((retVal = NumberContext.compare(j, reference.j)) == 0) {
                        retVal = NumberContext.compare(k, reference.k);
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

    public double doubleValue(final long row, final long col) {
        if (row == col) {
            return myScalar;
        } else {
            return this.doubleValue(row + (col * 4L));
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

    public Quaternion get() {
        return this;
    }

    public Double get(final long index) {
        return this.doubleValue(index);
    }

    public Double get(final long row, final long col) {
        return this.doubleValue(row, col);
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
        } else {
            return IJK;
        }

    }

    public double getVectorLength() {
        return PrimitiveFunction.SQRT.invoke(this.calculateSumOfSquaresVector());
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
        final NumberContext cntxt = PrimitiveScalar.CONTEXT;
        return myRealForSure || (cntxt.isSmall(myScalar, i) && cntxt.isSmall(myScalar, j) && cntxt.isSmall(myScalar, k));
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
        return PrimitiveFunction.SQRT.invoke(this.calculateSumOfSquaresAll());
    }

    public double scalar() {
        return myScalar;
    }

    public Versor signum() {
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

    public void supplyTo(final Mutate2D.Receiver<Double> receiver) {
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

    public BigDecimal toBigDecimal() {
        return new BigDecimal(myScalar, PrimitiveScalar.CONTEXT.getMathContext());
    }

    public MatrixStore<ComplexNumber> toComplexMatrix() {

        final GenericDenseStore<ComplexNumber> retVal = GenericDenseStore.COMPLEX.makeZero(2L, 2L);

        retVal.set(0L, ComplexNumber.of(myScalar, i));
        retVal.set(1L, ComplexNumber.of(-j, k));
        retVal.set(2L, ComplexNumber.of(j, k));
        retVal.set(3L, ComplexNumber.of(myScalar, -i));

        return retVal;
    }

    public MatrixStore<Double> toMultiplicationMatrix() {
        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(this);
        this.supplyTo(retVal);
        return retVal;
    }

    public MatrixStore<Double> toMultiplicationVector() {

        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(4L, 1L);

        retVal.set(0L, myScalar);
        retVal.set(1L, i);
        retVal.set(2L, j);
        retVal.set(3L, k);

        return retVal;
    }

    public MatrixStore<Double> toRotationMatrix() {

        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(3L, 3L);

        final double s = myScalar;

        final double ss = s * s;
        final double ii = i * i;
        final double jj = j * j;
        final double kk = k * k;

        double tmp1;
        double tmp2;

        final double invs = 1.0 / (ii + jj + kk + ss);

        final double r00 = ((ii + ss) - (jj + kk)) * invs;
        final double r11 = ((jj + ss) - (ii + kk)) * invs;
        final double r22 = ((kk + ss) - (ii + jj)) * invs;

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
        retVal.append(Double.toString(PrimitiveFunction.ABS.invoke(i)));
        retVal.append("i");

        if (j < PrimitiveMath.ZERO) {
            retVal.append(" - ");
        } else {
            retVal.append(" + ");
        }
        retVal.append(Double.toString(PrimitiveFunction.ABS.invoke(j)));
        retVal.append("j");

        if (k < PrimitiveMath.ZERO) {
            retVal.append(" - ");
        } else {
            retVal.append(" + ");
        }
        retVal.append(Double.toString(PrimitiveFunction.ABS.invoke(k)));
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

    public void transform(final PhysicalStore<Double> matrix) {

        final double s = myScalar;

        final double ss = s * s;
        final double ii = i * i;
        final double jj = j * j;
        final double kk = k * k;

        double tmp1;
        double tmp2;

        final double invs = 1.0 / (ii + jj + kk + ss);

        final double r00 = ((ii + ss) - (jj + kk)) * invs;
        final double r11 = ((jj + ss) - (ii + kk)) * invs;
        final double r22 = ((kk + ss) - (ii + jj)) * invs;

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

        if (matrix.count() == 3L) {

            final double x = matrix.doubleValue(0);
            final double y = matrix.doubleValue(1);
            final double z = matrix.doubleValue(2);

            matrix.set(0, (r00 * x) + (r01 * y) + (r02 * z));
            matrix.set(1, (r10 * x) + (r11 * y) + (r12 * z));
            matrix.set(2, (r20 * x) + (r21 * y) + (r22 * z));

        } else if (matrix.countRows() == 3L) {

            for (long c = 0L, limit = matrix.countColumns(); c < limit; c++) {

                final double x = matrix.doubleValue(0, c);
                final double y = matrix.doubleValue(1, c);
                final double z = matrix.doubleValue(2, c);

                matrix.set(0, c, (r00 * x) + (r01 * y) + (r02 * z));
                matrix.set(1, c, (r10 * x) + (r11 * y) + (r12 * z));
                matrix.set(2, c, (r20 * x) + (r21 * y) + (r22 * z));
            }

        } else if (matrix.countColumns() == 3L) {

            for (long r = 0L, limit = matrix.countRows(); r < limit; r++) {

                final double x = matrix.doubleValue(r, 0);
                final double y = matrix.doubleValue(r, 1);
                final double z = matrix.doubleValue(r, 2);

                matrix.set(r, 0, (r00 * x) + (r01 * y) + (r02 * z));
                matrix.set(r, 1, (r10 * x) + (r11 * y) + (r12 * z));
                matrix.set(r, 2, (r20 * x) + (r21 * y) + (r22 * z));
            }

        } else {

            throw new ProgrammingError("Only works for 3D stuff!");
        }
    }

    public double[] unit() {
        final double tmpLength = this.getVectorLength();
        if (tmpLength > 0.0) {
            return new double[] { i / tmpLength, j / tmpLength, k / tmpLength };
        } else {
            return new double[] { IJK.i, IJK.j, IJK.k };
        }
    }

    public PhysicalStore<Double> vector() {

        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(3L, 1L);

        retVal.set(0L, i);
        retVal.set(1L, j);
        retVal.set(2L, k);

        return retVal;
    }

    public Versor versor() {

        final double norm = this.norm();

        if (this.isReal()) {
            return new Versor(myScalar / norm);
        } else if (this.isPure()) {
            return new Versor(i / norm, j / norm, k / norm);
        } else {
            return new Versor(myScalar / norm, i / norm, j / norm, k / norm);
        }
    }

    private double calculateSumOfSquaresAll() {
        return (myScalar * myScalar) + this.calculateSumOfSquaresVector();
    }

    private double calculateSumOfSquaresVector() {
        return (i * i) + (j * j) + (k * k);
    }

}
