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

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.DenseArray;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * Singular Value: [A] = [Q1][D][Q2]<sup>T</sup> Decomposes [this] into [Q1], [D] and [Q2] where:
 * <ul>
 * <li>[Q1] is an orthogonal matrix. The columns are the left, orthonormal, singular vectors of [this]. Its
 * columns are the eigenvectors of [A][A]<sup>T</sup>, and therefore has the same number of rows as [this].
 * </li>
 * <li>[D] is a diagonal matrix. The elements on the diagonal are the singular values of [this]. It is either
 * square or has the same dimensions as [this]. The singular values of [this] are the square roots of the
 * nonzero eigenvalues of [A][A]<sup>T</sup> and [A]<sup>T</sup>[A] (they are the same)</li>
 * <li>[Q2] is an orthogonal matrix. The columns are the right, orthonormal, singular vectors of [this]. Its
 * columns are the eigenvectors of [A][A]<sup>T</sup>, and therefore has the same number of rows as [this] has
 * columns.</li>
 * <li>[this] = [Q1][D][Q2]<sup>T</sup></li>
 * </ul>
 * A singular values decomposition always exists.
 *
 * @author apete
 */
public interface SingularValue<N extends Number> extends MatrixDecomposition<N>, MatrixDecomposition.Solver<N>, MatrixDecomposition.EconomySize<N>,
        MatrixDecomposition.RankRevealing<N>, MatrixDecomposition.Values<N> {

    interface Factory<N extends Number> extends MatrixDecomposition.Factory<SingularValue<N>> {

    }

    public static final Factory<BigDecimal> BIG = typical -> new SingularValueDecomposition.Big();

    public static final Factory<ComplexNumber> COMPLEX = typical -> new SingularValueDecomposition.Complex();

    public static final Factory<Double> PRIMITIVE = typical -> {
        if ((1024L < typical.countColumns()) && (typical.count() <= DenseArray.MAX_ARRAY_SIZE)) {
            return new SingularValueDecomposition.Primitive();
        } else {
            return new RawSingularValue();
        }
    };

    public static final Factory<Quaternion> QUATERNION = typical -> new SingularValueDecomposition.Quat();

    public static final Factory<RationalNumber> RATIONAL = typical -> new SingularValueDecomposition.Rational();

    @SuppressWarnings("unchecked")
    public static <N extends Number> SingularValue<N> make(final Access2D<N> typical) {

        final N tmpNumber = typical.get(0, 0);

        if (tmpNumber instanceof RationalNumber) {
            return (SingularValue<N>) RATIONAL.make(typical);
        } else if (tmpNumber instanceof BigDecimal) {
            return (SingularValue<N>) BIG.make(typical);
        } else if (tmpNumber instanceof ComplexNumber) {
            return (SingularValue<N>) COMPLEX.make(typical);
        } else if (tmpNumber instanceof Double) {
            return (SingularValue<N>) PRIMITIVE.make(typical);
        } else if (tmpNumber instanceof Quaternion) {
            return (SingularValue<N>) QUATERNION.make(typical);
        } else {
            throw new IllegalArgumentException();
        }
    }

    static <N extends Number> boolean equals(final MatrixStore<N> matrix, final SingularValue<N> decomposition, final NumberContext context) {

        final int tmpRowDim = (int) matrix.countRows();
        final int tmpColDim = (int) matrix.countColumns();

        final MatrixStore<N> tmpQ1 = decomposition.getQ1();
        final MatrixStore<N> tmpD = decomposition.getD();
        final MatrixStore<N> tmpQ2 = decomposition.getQ2();

        MatrixStore<N> tmpThis;
        MatrixStore<N> tmpThat;

        boolean retVal = (tmpRowDim == tmpQ1.countRows()) && (tmpQ2.countRows() == tmpColDim);

        // Check that [A][Q2] == [Q1][D]
        if (retVal) {

            tmpThis = matrix.multiply(tmpQ2);
            tmpThat = tmpQ1.multiply(tmpD);

            retVal &= tmpThis.equals(tmpThat, context);
        }

        // If Q1 is square, then check if it is orthogonal/unitary.
        if (retVal && (tmpQ1.countRows() == tmpQ1.countColumns())) {

            tmpThis = tmpQ1.physical().makeEye(tmpRowDim, tmpRowDim);
            tmpThat = tmpQ1.logical().conjugate().get().multiply(tmpQ1);

            retVal &= tmpThis.equals(tmpThat, context);
        }

        // If Q2 is square, then check if it is orthogonal/unitary.
        if (retVal && (tmpQ2.countRows() == tmpQ2.countColumns())) {

            tmpThis = tmpQ2.physical().makeEye(tmpColDim, tmpColDim);
            tmpThat = tmpQ2.multiply(tmpQ2.logical().conjugate().get());

            retVal &= tmpThis.equals(tmpThat, context);
        }

        // Check the pseudoinverse.
        if (retVal) {
            final MatrixStore<N> inverse = decomposition.getInverse();
            final MatrixStore<N> multiplied = matrix.multiply(inverse.multiply(matrix));
            retVal &= matrix.equals(multiplied, context);
        }

        // Check that the singular values are sorted in descending order
        if (retVal) {
            final Array1D<Double> tmpSV = decomposition.getSingularValues();
            for (int i = 1; retVal && (i < tmpSV.size()); i++) {
                retVal &= tmpSV.doubleValue(i - 1) >= tmpSV.doubleValue(i);
            }
            if (retVal && decomposition.isOrdered()) {
                for (int ij = 1; retVal && (ij < tmpD.countRows()); ij++) {
                    retVal &= tmpD.doubleValue(ij - 1, ij - 1) >= tmpD.doubleValue(ij, ij);
                }
            }
        }

        return retVal;
    }

    static <N extends Number> MatrixStore<N> reconstruct(final SingularValue<N> decomposition) {
        return decomposition.getQ1().multiply(decomposition.getD()).multiply(decomposition.getQ2().conjugate());
    }

    /**
     * The condition number.
     *
     * @return The largest singular value divided by the smallest singular value.
     */
    double getCondition();

    /**
     * @return The diagonal matrix of singular values.
     */
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
     * If [A] is m-by-n and its rank is r, then:
     * <ul>
     * <li>The first r columns of [Q1] span the column space, range or image of [A].</li>
     * <li>The last m-r columns of [Q1] span the left nullspace or cokernel of [A].</li>
     * </ul>
     * Calculating the QR decomposition of [A] is a faster alternative.
     */
    MatrixStore<N> getQ1();

    /**
     * If [A] is m-by-n and its rank is r, then:
     * <ul>
     * <li>The first r columns of [Q2] span the row space or coimage of [A].</li>
     * <li>The last n-r columns of [Q2] span the nullspace or kernel of [A].</li>
     * </ul>
     * Calculating the QR decomposition of [A]<sup>T</sup> is a faster alternative.
     */
    MatrixStore<N> getQ2();

    /**
     * @return The singular values ordered in descending order.
     */
    Array1D<Double> getSingularValues();

    /**
     * @param values An array that will receive the singular values
     */
    default void getSingularValues(final double[] values) {

        ProgrammingError.throwIfNull(values);

        final Array1D<Double> singulars = this.getSingularValues();

        final int length = values.length;
        for (int i = 0; i < length; i++) {
            values[i] = singulars.doubleValue(i);
        }
    }

    double getTraceNorm();

    default MatrixStore<N> reconstruct() {
        return SingularValue.reconstruct(this);
    }

}
