/*
 * Copyright 1997-2025 Optimatika
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.PlainArray;
import org.ojalgo.matrix.Provider2D;
import org.ojalgo.matrix.decomposition.function.ExchangeColumns;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;
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
public interface Eigenvalue<N extends Comparable<N>>
        extends MatrixDecomposition.Hermitian<N>, MatrixDecomposition.Determinant<N>, MatrixDecomposition.Values<N>, Provider2D.Eigenpairs {

    public static class Eigenpair implements Comparable<Eigenpair> {

        public final ComplexNumber value;
        public final Access1D<ComplexNumber> vector;

        public Eigenpair(final ComplexNumber aValue, final Access1D<ComplexNumber> aVector) {
            super();
            value = aValue;
            vector = aVector;
        }

        @Override
        public int compareTo(final Eigenpair other) {
            return DESCENDING_NORM.compare(value, other.value);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || !(obj instanceof Eigenpair)) {
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
            result = prime * result + (value == null ? 0 : value.hashCode());
            return prime * result + (vector == null ? 0 : vector.hashCode());
        }

    }

    interface Factory<N extends Comparable<N>> extends MatrixDecomposition.Factory<Eigenvalue<N>> {

        default Eigenvalue<N> make(final boolean hermitian) {
            return this.make(TYPICAL, hermitian);
        }

        default Eigenvalue<N> make(final int dimension, final boolean hermitian) {
            return this.make(new Structure2D() {

                @Override
                public int getColDim() {
                    return dimension;
                }

                @Override
                public int getRowDim() {
                    return dimension;
                }

            }, hermitian);
        }

        @Override
        default Eigenvalue<N> make(final Structure2D typical) {
            if (typical instanceof MatrixStore) {
                return this.make(typical, ((MatrixStore<?>) typical).isHermitian());
            }
            return this.make(typical, false);
        }

        Eigenvalue<N> make(Structure2D typical, boolean hermitian);

        /**
         * Generalised: [A][V] = [B][V][D]
         *
         * @see #makeGeneralised(Structure2D, Eigenvalue.Generalisation)
         * @deprecated v56 Use {@link #makeGeneralised(Structure2D, Eigenvalue.Generalisation)} instead, then
         *             it's more clear what you're actually doing.
         */
        @Deprecated
        default Eigenvalue.Generalised<N> makeGeneralised(final Structure2D typical) {
            return this.makeGeneralised(typical, Eigenvalue.Generalisation.A_B);
        }

        /**
         * "Generalised" does NOT refer to general (as in not symmetric/hermitian) matrices, but to
         * generalised eigenvalue problems.
         * <ul>
         * <li>http://www.cmth.ph.ic.ac.uk/people/a.mackinnon/Lectures/compphys/node72.html
         * <li>https://www.netlib.org/lapack/lug/node54.html
         * </ul>
         */
        Eigenvalue.Generalised<N> makeGeneralised(Structure2D typical, Eigenvalue.Generalisation type);

    }

    /**
     * <a href="https://www.netlib.org/lapack/lug/node54.html">Generalized Symmetric Definite
     * Eigenproblems</a>
     *
     * @author apete
     */
    enum Generalisation {

        /**
         * [A][V]=[B][V][D]
         */
        A_B,

        /**
         * [A][B][V]=[V][D]
         */
        AB,

        /**
         * [B][A][V]=[V][D]
         */
        BA;

    }

    interface Generalised<N extends Comparable<N>> extends Eigenvalue<N> {

        /**
         * Corresponding to {@link #computeValuesOnly(org.ojalgo.structure.Access2D.Collectable)} but for the
         * generalised case.
         *
         * @see #computeValuesOnly(org.ojalgo.structure.Access2D.Collectable)
         */
        default boolean computeValuesOnly(final Access2D.Collectable<N, ? super TransformableRegion<N>> matrixA,
                final Access2D.Collectable<N, ? super TransformableRegion<N>> matrixB) {
            return this.prepare(matrixB) && this.computeValuesOnly(matrixA);
        }

        /**
         * Corresponding to {@link #decompose(org.ojalgo.structure.Access2D.Collectable)} but for the
         * generalised case.
         *
         * @see #decompose(org.ojalgo.structure.Access2D.Collectable)
         */
        default boolean decompose(final Access2D.Collectable<N, ? super TransformableRegion<N>> matrixA,
                final Access2D.Collectable<N, ? super TransformableRegion<N>> matrixB) {
            return this.prepare(matrixB) && this.decompose(matrixA);
        }

        boolean prepare(Access2D.Collectable<N, ? super TransformableRegion<N>> matrixB);

    }

    Factory<ComplexNumber> C128 = new Factory<>() {

        @Override
        public Eigenvalue<ComplexNumber> make(final Structure2D typical, final boolean hermitian) {
            return hermitian ? new HermitianEvD.C128() : null;
        }

        @Override
        public Eigenvalue.Generalised<ComplexNumber> makeGeneralised(final Structure2D typical, final Eigenvalue.Generalisation type) {

            PhysicalStore.Factory<ComplexNumber, GenericStore<ComplexNumber>> factory = GenericStore.C128;
            Cholesky<ComplexNumber> cholesky = Cholesky.C128.make(typical);
            Eigenvalue<ComplexNumber> eigenvalue = this.make(typical, true);

            return new GeneralisedEvD<>(factory, cholesky, eigenvalue, type);
        }

    };

    /**
     * Sorts on the norm in descending order. If the 2 eigenvalues have equal norm then the usual
     * {@link ComplexNumber} sort order is used (reversed).
     */
    Comparator<ComplexNumber> DESCENDING_NORM = (arg1, arg2) -> {
        int retVal = Double.compare(arg2.norm(), arg1.norm());
        if (retVal == 0) {
            return arg2.compareTo(arg1);
        }
        return retVal;
    };

    Factory<Quaternion> H256 = new Factory<>() {

        @Override
        public Eigenvalue<Quaternion> make(final Structure2D typical, final boolean hermitian) {
            return hermitian ? new HermitianEvD.H256() : null;
        }

        @Override
        public Eigenvalue.Generalised<Quaternion> makeGeneralised(final Structure2D typical, final Eigenvalue.Generalisation type) {

            PhysicalStore.Factory<Quaternion, GenericStore<Quaternion>> factory = GenericStore.H256;
            Cholesky<Quaternion> cholesky = Cholesky.H256.make(typical);
            Eigenvalue<Quaternion> eigenvalue = this.make(typical, true);

            return new GeneralisedEvD<>(factory, cholesky, eigenvalue, type);
        }

    };

    Factory<RationalNumber> Q128 = new Factory<>() {

        @Override
        public Eigenvalue<RationalNumber> make(final Structure2D typical, final boolean hermitian) {
            return hermitian ? new HermitianEvD.Q128() : null;
        }

        @Override
        public Eigenvalue.Generalised<RationalNumber> makeGeneralised(final Structure2D typical, final Eigenvalue.Generalisation type) {

            PhysicalStore.Factory<RationalNumber, GenericStore<RationalNumber>> factory = GenericStore.Q128;
            Cholesky<RationalNumber> cholesky = Cholesky.Q128.make(typical);
            Eigenvalue<RationalNumber> eigenvalue = this.make(typical, true);

            return new GeneralisedEvD<>(factory, cholesky, eigenvalue, type);
        }

    };

    Factory<Double> R064 = new Factory<>() {

        @Override
        public Eigenvalue<Double> make(final Structure2D typical) {
            if (8192L < typical.countColumns() && typical.count() <= PlainArray.MAX_SIZE) {
                return new DynamicEvD.R064();
            } else {
                return new RawEigenvalue.Dynamic();
            }
        }

        @Override
        public Eigenvalue<Double> make(final Structure2D typical, final boolean hermitian) {
            if (hermitian) {
                if (8192L < typical.countColumns() && typical.count() <= PlainArray.MAX_SIZE) {
                    return new HermitianEvD.R064();
                } else {
                    return new RawEigenvalue.Symmetric();
                }
            } else {
                if (8192L < typical.countColumns() && typical.count() <= PlainArray.MAX_SIZE) {
                    return new GeneralEvD.R064();
                } else {
                    return new RawEigenvalue.General();
                }
            }
        }

        @Override
        public Eigenvalue.Generalised<Double> makeGeneralised(final Structure2D typical, final Eigenvalue.Generalisation type) {

            PhysicalStore.Factory<Double, R064Store> factory = R064Store.FACTORY;
            Cholesky<Double> cholesky = Cholesky.R064.make(typical);
            Eigenvalue<Double> eigenvalue = this.make(typical, true);

            return new GeneralisedEvD<>(factory, cholesky, eigenvalue, type);
        }

    };

    Factory<Quadruple> R128 = new Factory<>() {

        @Override
        public Eigenvalue<Quadruple> make(final Structure2D typical, final boolean hermitian) {
            return hermitian ? new HermitianEvD.R128() : null;
        }

        @Override
        public Eigenvalue.Generalised<Quadruple> makeGeneralised(final Structure2D typical, final Eigenvalue.Generalisation type) {

            PhysicalStore.Factory<Quadruple, GenericStore<Quadruple>> factory = GenericStore.R128;
            Cholesky<Quadruple> cholesky = Cholesky.R128.make(typical);
            Eigenvalue<Quadruple> eigenvalue = this.make(typical, true);

            return new GeneralisedEvD<>(factory, cholesky, eigenvalue, type);
        }

    };

    static <N extends Comparable<N>> boolean equals(final MatrixStore<N> matrix, final Eigenvalue<N> decomposition, final NumberContext context) {

        final MatrixStore<N> tmpD = decomposition.getD();
        final MatrixStore<N> tmpV = decomposition.getV();

        // Check that [A][V] == [V][D] ([A] == [V][D][V]<sup>T</sup> is not always true)
        final MatrixStore<N> tmpStore1 = matrix.multiply(tmpV);
        final MatrixStore<N> tmpStore2 = tmpV.multiply(tmpD);

        return Access2D.equals(tmpStore1, tmpStore2, context);
    }

    /**
     * Sort eigenvalues and corresponding vectors.
     *
     * @param values         The eigenvalues to sort
     * @param vectorExchange A function that can exchange the eigenvectors to follow the new order.
     */
    static void sort(final double[] values, final ExchangeColumns vectorExchange) {

        int size = values.length;

        for (int i = 0, limit = size - 1; i < limit; i++) {

            int k = i;
            double p = Math.abs(values[i]);

            for (int j = i + 1; j < size; j++) {
                double m = Math.abs(values[j]);
                if (m > p) {
                    k = j;
                    p = m;
                }
            }

            if (k != i) {
                p = values[k];
                values[k] = values[i];
                values[i] = p;
                vectorExchange.exchangeColumns(i, k);
            }
        }
    }

    private void copyEigenvector(final int index, final Array1D<ComplexNumber> destination) {

        final MatrixStore<N> tmpV = this.getV();
        final MatrixStore<N> tmpD = this.getD();
        final long tmpDimension = tmpD.countColumns();

        final int prevCol = index - 1;
        final int nextCol = index + 1;

        if (index < tmpDimension - 1L && tmpD.doubleValue(nextCol, index) != 0.0) {
            for (int i = 0; i < tmpDimension; i++) {
                destination.set(i, ComplexNumber.of(tmpV.doubleValue(i, index), tmpV.doubleValue(i, nextCol)));
            }
        } else if (index > 0 && tmpD.doubleValue(prevCol, index) != 0.0) {
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

        final long dim = this.getV().countColumns();

        final Array1D<ComplexNumber> vector = Array1D.C128.make(dim);
        this.copyEigenvector(index, vector);

        final Array1D<ComplexNumber> values = this.getEigenvalues();
        final ComplexNumber value = values.get(index);

        return new Eigenpair(value, vector);
    }

    /**
     * This list is always ordered in descending eigenvalue order – that's regardless of if
     * {@link #isOrdered()} returns true or false.
     *
     * @see org.ojalgo.matrix.Provider2D.Eigenpairs#getEigenpairs()
     */
    @Override
    default List<Eigenpair> getEigenpairs() {

        List<Eigenpair> retVal = new ArrayList<>();

        for (int i = 0, limit = this.getEigenvalues().size(); i < limit; i++) {
            retVal.add(this.getEigenpair(i));
        }

        Collections.sort(retVal);

        return retVal;
    }

    /**
     * <p>
     * Even for real matrices the eigenvalues (and eigenvectors) are potentially complex numbers. Typically
     * they need to be expressed as complex numbers when [A] is not symmetric.
     * </p>
     * <p>
     * The values should be in the same order as the matrices "V" and "D", and if they is ordered or not is
     * indicated by the {@link #isOrdered()} method.
     * </p>
     *
     * @return The eigenvalues.
     */
    Array1D<ComplexNumber> getEigenvalues();

    // /**
    // * @return The matrix exponential
    // */
    // default MatrixStore<N> getExponential() {
    //
    // final MatrixStore<N> mtrxV = this.getV();
    //
    // final PhysicalStore<N> tmpD = this.getD().copy();
    // tmpD.modifyDiagonal(mtrxV.physical().function().exp());
    // final MatrixStore<N> mtrxD = tmpD.diagonal();
    //
    // return mtrxV.multiply(mtrxD).multiply(mtrxV.conjugate());
    // }
    //
    // /**
    // * @return The matrix power
    // */
    // default MatrixStore<N> getPower(final int exponent) {
    //
    // final MatrixStore<N> mtrxV = this.getV();
    // final MatrixStore<N> mtrxD = this.getD();
    //
    // MatrixStore<N> retVal = mtrxV;
    // for (int e = 0; e < exponent; e++) {
    // retVal = retVal.multiply(mtrxD);
    // }
    // retVal = retVal.multiply(mtrxV.conjugate());
    //
    // return retVal;
    // }

    /**
     * @param realParts      An array that will receive the real parts of the eigenvalues
     * @param imaginaryParts An optional array that, if present, will receive the imaginary parts of the
     *                       eigenvalues
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
     * @return A complex valued alternative to {@link #getV()}.
     */
    default MatrixStore<ComplexNumber> getEigenvectors() {

        final long tmpDimension = this.getV().countColumns();

        final GenericStore<ComplexNumber> retVal = GenericStore.C128.make(tmpDimension, tmpDimension);

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
    @Override
    boolean isOrdered();

    @Override
    default MatrixStore<N> reconstruct() {
        MatrixStore<N> mtrxV = this.getV();
        MatrixStore<N> mtrxD = this.getD();
        return mtrxV.multiply(mtrxD).multiply(mtrxV.conjugate());
    }

}
