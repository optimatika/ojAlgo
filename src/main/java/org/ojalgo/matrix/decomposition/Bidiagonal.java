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

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

/**
 * A general matrix [A] can be factorized by similarity transformations into the form [A]=[LQ][D][RQ]
 * <sup>-1</sup> where:
 * <ul>
 * <li>[A] (m-by-n) is any, real or complex, matrix</li>
 * <li>[D] (r-by-r) or (m-by-n) is, upper or lower, bidiagonal</li>
 * <li>[LQ] (m-by-r) or (m-by-m) is orthogonal</li>
 * <li>[RQ] (n-by-r) or (n-by-n) is orthogonal</li>
 * <li>r = min(m,n)</li>
 * </ul>
 *
 * @author apete
 */
public interface Bidiagonal<N extends Comparable<N>> extends MatrixDecomposition<N>, MatrixDecomposition.EconomySize<N> {

    interface Factory<N extends Comparable<N>> extends MatrixDecomposition.Factory<Bidiagonal<N>> {

        default Bidiagonal<N> make(final boolean fullSize) {
            return this.make(TYPICAL, fullSize);
        }

        default Bidiagonal<N> make(final Structure2D typical) {
            return this.make(typical, false);
        }

        Bidiagonal<N> make(Structure2D typical, boolean fullSize);

    }

    Factory<ComplexNumber> C128 = (typical, fullSize) -> new BidiagonalDecomposition.C128(fullSize);

    Factory<Quadruple> R128 = (typical, fullSize) -> new BidiagonalDecomposition.R128(fullSize);

    Factory<Double> R064 = (typical, fullSize) -> new BidiagonalDecomposition.R064(fullSize);

    Factory<Quaternion> H256 = (typical, fullSize) -> new BidiagonalDecomposition.H256(fullSize);

    Factory<RationalNumber> Q128 = (typical, fullSize) -> new BidiagonalDecomposition.Q128(fullSize);

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
    Factory<Quaternion> QUATERNION = H256;

    /**
     * @deprecated
     */
    @Deprecated
    Factory<RationalNumber> RATIONAL = Q128;

    static <N extends Comparable<N>> boolean equals(final MatrixStore<N> matrix, final Bidiagonal<N> decomposition, final NumberContext context) {

        final int tmpRowDim = (int) matrix.countRows();
        final int tmpColDim = (int) matrix.countColumns();

        final MatrixStore<N> tmpQ1 = decomposition.getLQ();
        decomposition.getD();
        final MatrixStore<N> tmpQ2 = decomposition.getRQ();

        final MatrixStore<N> tmpConjugatedQ1 = tmpQ1.conjugate();
        final MatrixStore<N> tmpConjugatedQ2 = tmpQ2.conjugate();

        MatrixStore<N> tmpThis;
        MatrixStore<N> tmpThat;

        boolean retVal = (tmpRowDim == tmpQ1.countRows()) && (tmpQ2.countRows() == tmpColDim);

        // Check that it's possible to reconstruct the original matrix.
        if (retVal) {

            tmpThis = matrix;
            tmpThat = decomposition.reconstruct();

            retVal &= tmpThis.equals(tmpThat, context);
        }

        // If Q1 is square, then check if it is orthogonal/unitary.
        if (retVal && (tmpQ1.countRows() == tmpQ1.countColumns())) {

            tmpThis = tmpQ1;
            tmpThat = tmpQ1.multiply(tmpConjugatedQ1).multiply(tmpQ1);

            retVal &= tmpThis.equals(tmpThat, context);
        }

        // If Q2 is square, then check if it is orthogonal/unitary.
        if (retVal && (tmpQ2.countRows() == tmpQ2.countColumns())) {

            tmpThis = tmpQ2;
            tmpThat = tmpQ2.multiply(tmpConjugatedQ2).multiply(tmpQ2);

            retVal &= tmpThis.equals(tmpThat, context);
        }

        return retVal;
    }

    MatrixStore<N> getD();

    MatrixStore<N> getLQ();

    MatrixStore<N> getRQ();

    boolean isUpper();

    default MatrixStore<N> reconstruct() {
        MatrixStore<N> mtrxQ1 = this.getLQ();
        MatrixStore<N> mtrxD = this.getD();
        MatrixStore<N> mtrxQ2 = this.getRQ();
        return mtrxQ1.multiply(mtrxD).multiply(mtrxQ2.conjugate());
    }

}
