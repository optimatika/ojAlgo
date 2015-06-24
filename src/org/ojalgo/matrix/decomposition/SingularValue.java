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
package org.ojalgo.matrix.decomposition;

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;

/**
 * Singular Value: [A] = [Q1][D][Q2]<sup>T</sup> Decomposes [this] into [Q1], [D] and [Q2] where:
 * <ul>
 * <li>[Q1] is an orthogonal matrix. The columns are the left, orthonormal, singular vectors of [this]. Its
 * columns are the eigenvectors of [A][A]<sup>T</sup>, and therefore has the same number of rows as [this].</li>
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
        MatrixDecomposition.Values<N> {

    @SuppressWarnings("unchecked")
    public static <N extends Number> SingularValue<N> make(final Access2D<N> typical) {

        final N tmpNumber = typical.get(0, 0);

        if (tmpNumber instanceof BigDecimal) {
            return (SingularValue<N>) new SVDnew32.Big();
        } else if (tmpNumber instanceof ComplexNumber) {
            return (SingularValue<N>) new SVDnew32.Complex();
        } else if (tmpNumber instanceof Double) {
            if ((2048L < typical.countColumns()) && (typical.count() <= BasicArray.MAX_ARRAY_SIZE)) {
                return (SingularValue<N>) new SVDnew32.Primitive();
            } else {
                return (SingularValue<N>) new RawSingularValue();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static SingularValue<BigDecimal> makeBig() {
        return new SVDnew32.Big();
    }

    public static SingularValue<ComplexNumber> makeComplex() {
        return new SVDnew32.Complex();
    }

    public static SingularValue<Double> makePrimitive() {
        return new SVDnew32.Primitive();
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
     * Effective numerical matrix rank.
     *
     * @return The number of nonnegligible singular values.
     */
    int getRank();

    /**
     * @return The singular values ordered in descending order.
     */
    Array1D<Double> getSingularValues();

    double getTraceNorm();

    default MatrixStore<N> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }

}
