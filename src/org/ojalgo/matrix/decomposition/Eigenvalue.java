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
package org.ojalgo.matrix.decomposition;

import java.math.BigDecimal;
import java.util.Optional;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Structure2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.DenseArray;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * [A] = [V][D][V]<sup>-1</sup> ([A][V] = [V][D])
 * <ul>
 * <li>[A] = any square matrix.</li>
 * <li>[V] = contains the eigenvectors as columns.</li>
 * <li>[D] = a diagonal matrix with the eigenvalues on the diagonal (possibly in blocks).</li>
 * </ul>
 * <p>
 * [A] is normal if [A][A]<sup>H</sup> = [A]<sup>H</sup>[A], and [A] is normal if and only if there exists a
 * unitary matrix [Q] such that [A] = [Q][D][Q]<sup>H</sup>. Hermitian matrices are normal.
 * </p>
 * <p>
 * [V] and [D] can always be calculated in the sense that they will satisfy [A][V] = [V][D], but it is not
 * always possible to calculate [V]<sup>-1</sup>. (Check the rank and/or the condition number of [V] to
 * determine the validity of [V][D][V]<sup>-1</sup>.)
 * </p>
 * <p>
 * The eigenvalues (and their corresponding eigenvectors) of a non-symmetric matrix could be complex.
 * </p>
 *
 * @author apete
 */
public interface Eigenvalue<N extends Number>
        extends MatrixDecomposition<N>, MatrixDecomposition.Hermitian<N>, MatrixDecomposition.Determinant<N>, MatrixDecomposition.Values<N> {

    public static class Eigenpair implements Comparable<Eigenpair> {

        public final ComplexNumber value;
        public final Access1D<ComplexNumber> vector;

        public Eigenpair(final ComplexNumber aValue, final Access1D<ComplexNumber> aVector) {
            super();
            value = aValue;
            vector = aVector;
        }

        public int compareTo(final Eigenpair other) {
            return other.value.compareTo(value);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Eigenpair)) {
                return false;
            }
            final Eigenpair other = (Eigenpair) obj;
            if (value == null) {
                if (other.value != null) {
                    return false;
                }
            } else if (!value.equals(other.value)) {
                return false;
            }
            if (vector == null) {
                if (other.vector != null) {
                    return false;
                }
            } else if (!vector.equals(other.vector)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((value == null) ? 0 : value.hashCode());
            result = (prime * result) + ((vector == null) ? 0 : vector.hashCode());
            return result;
        }

    }

    interface Factory<N extends Number> extends MatrixDecomposition.Factory<Eigenvalue<N>> {

        default Eigenvalue<N> make(final boolean hermitian) {
            return this.make(TYPICAL, hermitian);
        }

        default Eigenvalue<N> make(final int dimension, final boolean hermitian) {
            return this.make(new Structure2D() {

                public long countColumns() {
                    return dimension;
                }

                public long countRows() {
                    return dimension;
                }

            }, hermitian);
        }

        default Eigenvalue<N> make(final Structure2D typical) {
            if (typical instanceof Access2D) {
                return this.make(typical, MatrixUtils.isHermitian((Access2D<?>) typical));
            } else {
                return this.make(typical, false);
            }
        }

        Eigenvalue<N> make(Structure2D typical, boolean hermitian);

    }

    public static final Factory<BigDecimal> BIG = (typical, hermitian) -> hermitian ? new HermitianEvD.Big() : null;

    public static final Factory<ComplexNumber> COMPLEX = (typical, hermitian) -> hermitian ? new HermitianEvD.Complex() : null;

    public static final Factory<Double> PRIMITIVE = new Factory<Double>() {

        public Eigenvalue<Double> make(final Structure2D typical) {
            if ((8192L < typical.countColumns()) && (typical.count() <= DenseArray.MAX_ARRAY_SIZE)) {
                return new DynamicEvD.Primitive();
            } else {
                return new RawEigenvalue.Dynamic();
            }
        }

        public Eigenvalue<Double> make(final Structure2D typical, final boolean hermitian) {
            if (hermitian) {
                if ((8192L < typical.countColumns()) && (typical.count() <= DenseArray.MAX_ARRAY_SIZE)) {
                    return new HermitianEvD.SimultaneousPrimitive();
                } else {
                    return new RawEigenvalue.Symmetric();
                }
            } else {
                if ((8192L < typical.countColumns()) && (typical.count() <= DenseArray.MAX_ARRAY_SIZE)) {
                    return new OldGeneralEvD.Primitive();
                } else {
                    return new RawEigenvalue.General();
                }
            }
        }

    };

    public static final Factory<Quaternion> QUATERNION = (typical, hermitian) -> hermitian ? new HermitianEvD.Quat() : null;

    public static final Factory<RationalNumber> RATIONAL = (typical, hermitian) -> hermitian ? new HermitianEvD.Rational() : null;

    public static <N extends Number> Eigenvalue<N> make(final Access2D<N> typical) {
        return Eigenvalue.make(typical, MatrixUtils.isHermitian(typical));
    }

    @SuppressWarnings("unchecked")
    public static <N extends Number> Eigenvalue<N> make(final Access2D<N> typical, final boolean hermitian) {

        final N tmpNumber = typical.get(0L, 0L);

        if (tmpNumber instanceof BigDecimal) {
            return (Eigenvalue<N>) BIG.make(typical, hermitian);
        } else if (tmpNumber instanceof ComplexNumber) {
            return (Eigenvalue<N>) COMPLEX.make(typical, hermitian);
        } else if (tmpNumber instanceof Double) {
            return (Eigenvalue<N>) PRIMITIVE.make(typical, hermitian);
        } else if (tmpNumber instanceof Quaternion) {
            return (Eigenvalue<N>) QUATERNION.make(typical, hermitian);
        } else if (tmpNumber instanceof RationalNumber) {
            return (Eigenvalue<N>) RATIONAL.make(typical, hermitian);
        } else {
            throw new IllegalArgumentException();
        }
    }

    static <N extends Number> boolean equals(final MatrixStore<N> matrix, final Eigenvalue<N> decomposition, final NumberContext context) {

        final MatrixStore<N> tmpD = decomposition.getD();
        final MatrixStore<N> tmpV = decomposition.getV();

        // Check that [A][V] == [V][D] ([A] == [V][D][V]<sup>T</sup> is not always true)
        final MatrixStore<N> tmpStore1 = matrix.multiply(tmpV);
        final MatrixStore<N> tmpStore2 = tmpV.multiply(tmpD);

        return Access2D.equals(tmpStore1, tmpStore2, context);
    }

    static <N extends Number> MatrixStore<N> reconstruct(final Eigenvalue<N> decomposition) {
        final MatrixStore<N> tmpV = decomposition.getV();
        return tmpV.multiply(decomposition.getD()).multiply(tmpV.conjugate());
    }

    /**
     * @deprecated With Java 9 this will be made private. Use {@link #getEigenvectors()} or
     *             {@link #getEigenvector(int)} instead.
     */
    @Deprecated
    default void copyEigenvector(final int index, final Array1D<ComplexNumber> destination) {

        final MatrixStore<N> tmpV = this.getV();
        final MatrixStore<N> tmpD = this.getD();
        final long tmpDimension = tmpD.countColumns();

        final int prevCol = index - 1;
        final int nextCol = index + 1;

        if ((index < (tmpDimension - 1L)) && (tmpD.doubleValue(nextCol, index) != 0.0)) {
            for (int i = 0; i < tmpDimension; i++) {
                destination.set(i, ComplexNumber.of(tmpV.doubleValue(i, index), tmpV.doubleValue(i, nextCol)));
            }
        } else if ((index > 0) && (tmpD.doubleValue(prevCol, index) != 0.0)) {
            for (int i = 0; i < tmpDimension; i++) {
                destination.set(i, ComplexNumber.of(tmpV.doubleValue(i, prevCol), -tmpV.doubleValue(i, index)));
            }
        } else {
            for (int i = 0; i < tmpDimension; i++) {
                destination.set(i, tmpV.doubleValue(i, index));
            }
        }
    }

    /**
     * The only requirements on [D] are that it should contain the eigenvalues and that [A][V] = [V][D]. The
     * ordering of the eigenvalues is not specified.
     * <ul>
     * <li>If [A] is real and symmetric then [D] is (purely) diagonal with real eigenvalues.</li>
     * <li>If [A] is real but not symmetric then [D] is block-diagonal with real eigenvalues in 1-by-1 blocks
     * and complex eigenvalues in 2-by-2 blocks.</li>
     * <li>If [A] is complex then [D] is (purely) diagonal with complex eigenvalues.</li>
     * </ul>
     *
     * @return The (block) diagonal eigenvalue matrix.
     */
    MatrixStore<N> getD();

    default Eigenpair getEigenpair(final int index) {

        final long tmpDimension = this.getV().countColumns();

        final GenericDenseStore<ComplexNumber> retVal = GenericDenseStore.COMPLEX.makeZero(tmpDimension, 1L);

        this.copyEigenvector(index, retVal.sliceColumn(0, index));

        return new Eigenpair(this.getEigenvalues().get(index), retVal);
    }

    /**
     * <p>
     * Even for real matrices the eigenvalues (and eigenvectors) are potentially complex numbers. Typically
     * they need to be expressed as complex numbers when [A] is not symmetric.
     * </p>
     * <p>
     * Prior to v41 this array should always be ordered in descending order - largest (modulus) first. As of
     * v41 the values should be in the same order as the matrices "V" and "D", and if that is ordered or not
     * is indicated by the {@link #isOrdered()} method.
     * </p>
     *
     * @return The eigenvalues.
     */
    Array1D<ComplexNumber> getEigenvalues();

    /**
     * @param realParts An array that will receive the real parts of the eigenvalues
     * @param imaginaryParts An optional array that, if present, will receive the imaginary parts of the
     *        eigenvalues
     */
    default void getEigenvalues(final double[] realParts, final Optional<double[]> imaginaryParts) {

        ProgrammingError.throwIfNull(realParts, imaginaryParts);

        final Array1D<ComplexNumber> values = this.getEigenvalues();

        final int length = realParts.length;

        if (imaginaryParts.isPresent()) {
            final double[] imagParts = imaginaryParts.get();
            for (int i = 0; i < length; i++) {
                final ComplexNumber value = values.get(i);
                realParts[i] = value.getReal();
                imagParts[i] = value.getImaginary();
            }
        } else {
            for (int i = 0; i < length; i++) {
                realParts[i] = values.doubleValue(i);
            }
        }
    }

    /**
     * @param index Index corresponding to an entry in {@link #getEigenvalues()} and/or a column in
     *        {@link #getEigenvectors()}.
     * @return One eigenvector
     * @deprecated v43 Use {@link #getEigenpair(int)} instead.
     */
    @Deprecated
    default MatrixStore<ComplexNumber> getEigenvector(final int index) {

        final long tmpDimension = this.getV().countColumns();

        final GenericDenseStore<ComplexNumber> retVal = GenericDenseStore.COMPLEX.makeZero(tmpDimension, 1L);

        this.copyEigenvector(index, retVal.sliceColumn(0, index));

        return retVal;
    }

    /**
     * @return A complex valued alternative to {@link #getV()}.
     */
    default MatrixStore<ComplexNumber> getEigenvectors() {

        final long tmpDimension = this.getV().countColumns();

        final GenericDenseStore<ComplexNumber> retVal = GenericDenseStore.COMPLEX.makeZero(tmpDimension, tmpDimension);

        for (int j = 0; j < tmpDimension; j++) {
            this.copyEigenvector(j, retVal.sliceColumn(0, j));
        }

        return retVal;
    }

    /**
     * A matrix' trace is the sum of the diagonal elements. It is also the sum of the eigenvalues. This method
     * should return the sum of the eigenvalues.
     *
     * @return The matrix' trace
     */
    ComplexNumber getTrace();

    /**
     * The columns of [V] represent the eigenvectors of [A] in the sense that [A][V] = [V][D].
     *
     * @return The eigenvector matrix.
     */
    MatrixStore<N> getV();

    /**
     * If [A] is hermitian then [V][D][V]<sup>-1</sup> becomes [Q][D][Q]<sup>H</sup>...
     */
    boolean isHermitian();

    /**
     * The eigenvalues in D (and the eigenvectors in V) are not necessarily ordered. This is a property of the
     * algorithm/implementation, not the data.
     *
     * @return true if they are ordered
     */
    boolean isOrdered();

    default MatrixStore<N> reconstruct() {
        return Eigenvalue.reconstruct(this);
    }

}
