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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.array.PlainArray;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;
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
 * {@linkplain #Q128}, {@linkplain #C128}, {@linkplain #PRIMITIVE} or {@linkplain #make(Access2D)}
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
public interface QR<N extends Comparable<N>> extends MatrixDecomposition<N>, MatrixDecomposition.Solver<N>, MatrixDecomposition.EconomySize<N>,
        MatrixDecomposition.Determinant<N>, MatrixDecomposition.RankRevealing<N> {

    interface Factory<N extends Comparable<N>> extends MatrixDecomposition.Factory<QR<N>> {

        default QR<N> make(final boolean fullSize) {
            return this.make(TYPICAL, fullSize);
        }

        default QR<N> make(final Structure2D typical) {
            return this.make(typical, false);
        }

        QR<N> make(Structure2D typical, boolean fullSize);

    }

    Factory<ComplexNumber> C128 = (typical, fullSize) -> new QRDecomposition.C128(fullSize);

    Factory<Double> R064 = (typical, fullSize) -> {
        if (fullSize || typical.isFat() || 64L >= typical.countColumns() && typical.count() <= PlainArray.MAX_SIZE) {
            return new QRDecomposition.R064(fullSize);
        }
        return new RawQR();
    };

    Factory<Quadruple> R128 = (typical, fullSize) -> new QRDecomposition.R128(fullSize);

    Factory<Quaternion> H256 = (typical, fullSize) -> new QRDecomposition.H256(fullSize);

    Factory<RationalNumber> Q128 = (typical, fullSize) -> new QRDecomposition.Q128(fullSize);

    /**
     * @deprecated
     */
    @Deprecated
    Factory<ComplexNumber> COMPLEX = C128;

    /**
     * @deprecated
     */
    @Deprecated
    Factory<Double> PRIMITIVE = R064;

    /**
     * @deprecated
     */
    @Deprecated
    Factory<Quadruple> QUADRUPLE = R128;

    /**
     * @deprecated
     */
    @Deprecated
    Factory<Quaternion> QUATERNION = H256;

    /**
     * @deprecated
     */
    @Deprecated
    Factory<RationalNumber> RATIONAL = Q128;

    static <N extends Comparable<N>> boolean equals(final MatrixStore<N> matrix, final QR<N> decomposition, final NumberContext context) {

        final MatrixStore<N> tmpQ = decomposition.getQ();
        final MatrixStore<N> tmpR = decomposition.getR();

        final MatrixStore<N> tmpStore = tmpQ.multiply(tmpR);

        return Access2D.equals(tmpStore, matrix, context);
    }

    MatrixStore<N> getQ();

    MatrixStore<N> getR();

    default boolean isOrdered() {
        return false;
    }

    default MatrixStore<N> reconstruct() {
        MatrixStore<N> mtrxQ = this.getQ();
        MatrixStore<N> mtrxR = this.getR();
        return mtrxQ.multiply(mtrxR);
    }

}
