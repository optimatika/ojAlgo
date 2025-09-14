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

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.PlainArray;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.matrix.Provider2D;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

/**
 * Singular Value: [A] = [U][S][V]<sup>T</sup> Decomposes [this] into [U], [S] and [V] where:
 * <ul>
 * <li>[U] is an orthogonal matrix. The columns are the left, orthonormal, singular vectors of [this]. Its
 * columns are the eigenvectors of [A][A]<sup>T</sup>, and therefore has the same number of rows as [this].
 * </li>
 * <li>[S] is a diagonal matrix. The elements on the diagonal are the singular values of [this]. It is either
 * square or has the same dimensions as [this]. The singular values of [this] are the square roots of the
 * nonzero eigenvalues of [A][A]<sup>T</sup> and [A]<sup>T</sup>[A] (they are the same)</li>
 * <li>[V] is an orthogonal matrix. The columns are the right, orthonormal, singular vectors of [this]. Its
 * columns are the eigenvectors of [A][A]<sup>T</sup>, and therefore has the same number of rows as [this] has
 * columns.</li>
 * <li>[this] = [U][S][V]<sup>T</sup></li>
 * </ul>
 * A singular values decomposition always exists.
 *
 * @author apete
 */
public interface SingularValue<N extends Comparable<N>> extends MatrixDecomposition.Solver<N>, MatrixDecomposition.EconomySize<N>,
        MatrixDecomposition.RankRevealing<N>, MatrixDecomposition.Values<N>, Provider2D.Condition {

    interface Factory<N extends Comparable<N>> extends MatrixDecomposition.Factory<SingularValue<N>> {

        default SingularValue<N> make(final boolean fullSize) {
            return this.make(TYPICAL, fullSize);
        }

        @Override
        default SingularValue<N> make(final Structure2D typical) {
            return this.make(typical, false);
        }

        SingularValue<N> make(Structure2D typical, boolean fullSize);

    }

    Factory<ComplexNumber> C128 = (typical, fullSize) -> new DenseSingularValue.C128(fullSize);

    Factory<Quaternion> H256 = (typical, fullSize) -> new DenseSingularValue.H256(fullSize);

    Factory<RationalNumber> Q128 = (typical, fullSize) -> new DenseSingularValue.Q128(fullSize);

    Factory<Double> R064 = (typical, fullSize) -> {
        if (fullSize || 1024L < typical.countColumns() && typical.count() <= PlainArray.MAX_SIZE) {
            return new DenseSingularValue.R064(fullSize);
        } else {
            return new RawSingularValue();
        }
    };

    Factory<Quadruple> R128 = (typical, fullSize) -> new DenseSingularValue.R128(fullSize);

    static <N extends Comparable<N>> boolean equals(final MatrixStore<N> matrix, final SingularValue<N> decomposition, final NumberContext context) {

        int nbRows = matrix.getRowDim();
        int nbCols = matrix.getColDim();

        MatrixStore<N> tmpU = decomposition.getU();
        MatrixStore<N> tmpD = decomposition.getS();
        MatrixStore<N> tmpV = decomposition.getV();

        MatrixStore<N> tmpThis;
        MatrixStore<N> tmpThat;

        boolean retVal = nbRows == tmpU.countRows() && tmpV.countRows() == nbCols;

        // Check that [A][V] == [U][S]
        if (retVal) {

            tmpThis = matrix.multiply(tmpV);
            tmpThat = tmpU.multiply(tmpD);

            retVal &= tmpThis.equals(tmpThat, context);
        }

        // If Q1 is square, then check if it is orthogonal/unitary.
        if (retVal && tmpU.countRows() == tmpU.countColumns()) {

            tmpThis = tmpU.physical().makeEye(nbRows, nbRows);
            tmpThat = tmpU.conjugate().multiply(tmpU);

            retVal &= tmpThis.equals(tmpThat, context);
        }

        // If Q2 is square, then check if it is orthogonal/unitary.
        if (retVal && tmpV.countRows() == tmpV.countColumns()) {

            tmpThis = tmpV.physical().makeEye(nbCols, nbCols);
            tmpThat = tmpV.multiply(tmpV.conjugate());

            retVal &= tmpThis.equals(tmpThat, context);
        }

        // Check the pseudoinverse.
        if (retVal) {
            MatrixStore<N> inverse = decomposition.getInverse();
            MatrixStore<N> multiplied = matrix.multiply(inverse.multiply(matrix));
            retVal &= matrix.equals(multiplied, context);
        }

        // Check that the singular values are sorted in descending order
        if (retVal) {
            Array1D<Double> tmpSV = decomposition.getSingularValues();
            for (int i = 1; retVal && i < tmpSV.size(); i++) {
                retVal &= tmpSV.doubleValue(i - 1) >= tmpSV.doubleValue(i);
            }
            if (retVal && decomposition.isOrdered()) {
                for (int ij = 1; retVal && ij < tmpD.countRows(); ij++) {
                    retVal &= tmpD.doubleValue(ij - 1, ij - 1) >= tmpD.doubleValue(ij, ij);
                }
            }
        }

        return retVal;
    }

    static <N extends Comparable<N>> MatrixStore<N> invert(final SingularValue<N> decomposition, final PhysicalStore<N> preallocated) {

        PhysicalStore.Factory<N, ?> factory = preallocated.physical();
        Scalar.Factory<N> scalar = factory.scalar();

        int rank = decomposition.getRank();

        if (rank == 0) {

            preallocated.fillAll(scalar.zero().get());

        } else {

            PhysicalStore<N> work = decomposition.getV().limits(-1, rank).collect(factory);

            BinaryFunction<N> multiply = factory.function().multiply();

            Array1D<Double> singularValues = decomposition.getSingularValues();

            for (int j = 0; j < rank; j++) {
                N factor = scalar.cast(1.0 / singularValues.doubleValue(j));
                work.modifyColumn(0, j, multiply.by(factor));
            }

            preallocated.fillByMultiplying(work, decomposition.getU().limits(-1, rank).conjugate());
        }

        return preallocated;
    }

    static <N extends Comparable<N>> MatrixStore<N> reconstruct(final SingularValue<N> decomposition) {
        MatrixStore<N> mtrxQ1 = decomposition.getU();
        MatrixStore<N> mtrxD = decomposition.getS();
        MatrixStore<N> mtrxQ2 = decomposition.getV();
        return mtrxQ1.multiply(mtrxD).multiply(mtrxQ2.conjugate());
    }

    static <N extends Comparable<N>> MatrixStore<N> solve(final SingularValue<N> decomposition, final MatrixStore<N> rhs, final PhysicalStore<N> preallocated) {

        PhysicalStore.Factory<N, ?> factory = preallocated.physical();
        Scalar.Factory<N> scalar = factory.scalar();

        int rank = decomposition.getRank();

        if (rank == 0) {

            preallocated.fillAll(scalar.zero().get());

        } else {

            PhysicalStore<N> work = decomposition.getU().limits(-1, rank).conjugate().multiply(rhs).copy();

            BinaryFunction<N> multiply = factory.function().multiply();

            Array1D<Double> singularValues = decomposition.getSingularValues();

            for (int i = 0; i < rank; i++) {
                N factor = scalar.cast(1.0 / singularValues.doubleValue(i));
                work.modifyRow(i, 0, multiply.by(factor));
            }

            preallocated.fillByMultiplying(decomposition.getV().limits(-1, rank), work);
        }

        return preallocated;
    }

    @Override
    default void ftran(final PhysicalStore<N> arg) {
        this.getSolution(arg.copy(), arg);
    }

    /**
     * The condition number.
     *
     * @return The largest singular value divided by the smallest singular value.
     */
    @Override
    double getCondition();

    /**
     * @return [[A]<sup>T</sup>[A]]<sup>-1</sup> Where [A] is the original matrix.
     */
    MatrixStore<N> getCovariance();

    /**
     * @return The diagonal matrix of singular values.
     * @deprecated Use {@link #getS()} instead
     */
    @Deprecated
    MatrixStore<N> getD();

    /**
     * Sometimes also called the Schatten 2-norm or Hilbert-Schmidt norm.
     *
     * @return The square root of the sum of squares of the singular values.
     */
    double getFrobeniusNorm();

    /**
     * <p>
     * Ky Fan k-norm.
     * </p>
     * <p>
     * The first Ky Fan k-norm is the operator norm (the largest singular value), and the last is called the
     * trace norm (the sum of all singular values).
     * </p>
     *
     * @param k The number of singular values to add up.
     * @return The sum of the k largest singular values.
     */
    double getKyFanNorm(int k);

    /**
     * @return 2-norm
     */
    double getOperatorNorm();

    /**
     * @return The diagonal matrix of singular values.
     */
    MatrixStore<N> getS();

    /**
     * @return The singular values ordered in descending order.
     */
    Array1D<Double> getSingularValues();

    /**
     * @param values An array that will receive the singular values
     */
    default void getSingularValues(final double[] values) {

        ProgrammingError.throwIfNull(values);

        Array1D<Double> singulars = this.getSingularValues();

        int length = values.length;
        for (int i = 0; i < length; i++) {
            values[i] = singulars.doubleValue(i);
        }
    }

    double getTraceNorm();

    /**
     * If [A] is m-by-n and its rank is r, then:
     * <ul>
     * <li>The first r columns of [U] span the column space, range or image of [A].</li>
     * <li>The last m-r columns of [U] span the left nullspace or cokernel of [A].</li>
     * </ul>
     * Calculating the QR decomposition of [A] is a faster alternative.
     */
    MatrixStore<N> getU();

    /**
     * If [A] is m-by-n and its rank is r, then:
     * <ul>
     * <li>The first r columns of [V] span the row space or coimage of [A].</li>
     * <li>The last n-r columns of [V] span the nullspace or kernel of [A].</li>
     * </ul>
     * Calculating the QR decomposition of [A]<sup>T</sup> is a faster alternative.
     */
    MatrixStore<N> getV();

    @Override
    default MatrixStore<N> reconstruct() {
        return SingularValue.reconstruct(this);
    }

    default MatrixStore<N> reconstruct(final int k) {

        int limit = Math.min(k, this.getRank());

        MatrixStore<N> mtrxQ1 = this.getU().limits(-1, limit);
        MatrixStore<N> mtrxD = this.getS().limits(limit, limit);
        MatrixStore<N> mtrxQ2 = this.getV().limits(-1, limit);

        return mtrxQ1.multiply(mtrxD).multiply(mtrxQ2.conjugate());
    }

}
