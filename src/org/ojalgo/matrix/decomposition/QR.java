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

import org.ojalgo.access.Access2D;
import org.ojalgo.array.DenseArray;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * QR: [A] = [Q][R] Decomposes [this] into [Q] and [R] where:
 * <ul>
 * <li>[Q] is an orthogonal matrix (orthonormal columns). It has the same number of rows as [this].</li>
 * <li>[R] is a right (upper) triangular matrix. It has the same number of columns as [this].</li>
 * <li>[this] = [Q][R]</li>
 * </ul>
 * Note: Either Q or R will be square. The interface does not specify which.
 * <p>
 * You create instances of (some subclass of) this class by calling one of the static factory methods:
 * {@linkplain #BIG}, {@linkplain #COMPLEX}, {@linkplain #PRIMITIVE} or {@linkplain #make(Access2D)}
 * </p>
 * <p>
 * The QR decompostion always exists, even if the matrix does not have full column rank, so the compute method
 * will never fail. The primary use of the QR decomposition is in the least squares solution of overdetermined
 * systems of simultaneous linear equations. This will fail if the matrix does not have full column rank. The
 * rank must be equal to the number of columns.
 * </p>
 *
 * @author apete
 */
public interface QR<N extends Number> extends MatrixDecomposition<N>, MatrixDecomposition.Solver<N>, MatrixDecomposition.EconomySize<N>,
        MatrixDecomposition.Determinant<N>, MatrixDecomposition.RankRevealing<N> {

    interface Factory<N extends Number> extends MatrixDecomposition.Factory<QR<N>> {

    }

    public static final Factory<ComplexNumber> COMPLEX = typical -> new QRDecomposition.Complex();

    public static final Factory<Double> PRIMITIVE = typical -> {
        if (typical.isFat() || ((256L < typical.countColumns()) && (typical.count() <= DenseArray.MAX_ARRAY_SIZE))) {
            return new QRDecomposition.Primitive();
        } else {
            return new RawQR();
        }
    };

    public static final Factory<Quaternion> QUATERNION = typical -> new QRDecomposition.Quat();

    public static final Factory<RationalNumber> RATIONAL = typical -> new QRDecomposition.Rational();

    @SuppressWarnings("unchecked")
    public static <N extends Number> QR<N> make(final Access2D<N> typical) {

        final N tmpNumber = typical.get(0, 0);

        if (tmpNumber instanceof RationalNumber) {
            return (QR<N>) RATIONAL.make(typical);
        } else if (tmpNumber instanceof Quaternion) {
            return (QR<N>) QUATERNION.make(typical);
        } else if (tmpNumber instanceof ComplexNumber) {
            return (QR<N>) COMPLEX.make(typical);
        } else if (tmpNumber instanceof Double) {
            return (QR<N>) PRIMITIVE.make(typical);
        } else {
            throw new IllegalArgumentException();
        }
    }

    static <N extends Number> boolean equals(final MatrixStore<N> matrix, final QR<N> decomposition, final NumberContext context) {

        final MatrixStore<N> tmpQ = decomposition.getQ();
        final MatrixStore<N> tmpR = decomposition.getR();

        final MatrixStore<N> tmpStore = tmpQ.multiply(tmpR);

        return Access2D.equals(tmpStore, matrix, context);
    }

    static <N extends Number> MatrixStore<N> reconstruct(final QR<N> decomposition) {
        return decomposition.getQ().multiply(decomposition.getR());
    }

    MatrixStore<N> getQ();

    MatrixStore<N> getR();

    /**
     * @deprecated v44 Use {@link #isFullRank()} instead
     */
    @Deprecated
    default boolean isFullColumnRank() {
        return this.isFullRank();
    }

    default boolean isOrdered() {
        return false;
    }

    default MatrixStore<N> reconstruct() {
        return QR.reconstruct(this);
    }

}
