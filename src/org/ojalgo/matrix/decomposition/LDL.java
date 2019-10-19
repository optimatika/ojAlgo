/*
 * Copyright 1997-2019 Optimatika
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
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * LDL: [A] = [L][D][L]<sup>H</sup> (or [R]<sup>H</sup>[D][R])
 * </p>
 * <p>
 * [A]<sup>H</sup> = [A] = [L][D][L]<sup>H</sup>
 * </p>
 * <p>
 * If [A] is symmetric (but not necessarily positive definite) then it can be decomposed into [L][D][L]
 * <sup>T</sup> (or [R]<sup>H</sup>[D][R]).
 * </p>
 * <ul>
 * <li>[L] is a unit lower (left) triangular matrix. It has the same dimensions as [this], and ones on the
 * diagonal.</li>
 * <li>[D] is a diagonal matrix. It has the same dimensions as [this].</li>
 * <li>[this] = [L][D][L]<sup>H</sup></li>
 * </ul>
 *
 * @author apete
 */
public interface LDL<N extends Comparable<N>> extends LDU<N>, MatrixDecomposition.Hermitian<N>, MatrixDecomposition.Pivoting<N> {

    interface Factory<N extends Comparable<N>> extends MatrixDecomposition.Factory<LDL<N>> {

    }

    Factory<ComplexNumber> COMPLEX = typical -> new LDLDecomposition.Complex();

    Factory<Double> PRIMITIVE = typical -> new LDLDecomposition.Primitive();

    Factory<Quaternion> QUATERNION = typical -> new LDLDecomposition.Quat();

    Factory<RationalNumber> RATIONAL = typical -> new LDLDecomposition.Rational();

    static <N extends Comparable<N>> boolean equals(final MatrixStore<N> matrix, final LDL<N> decomposition, final NumberContext context) {
        return Access2D.equals(matrix, decomposition.reconstruct(), context);
    }

    /**
     * @deprecated v48 Use {link #COMPLEX}, {@link #PRIMITIVE}. {@link #QUATERNION} or {@link #RATIONAL}
     *             innstead.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    static <N extends Comparable<N>> LDL<N> make(final Access2D<N> typical) {

        final N tmpNumber = typical.get(0, 0);

        if (tmpNumber instanceof RationalNumber) {
            return (LDL<N>) RATIONAL.make(typical);
        } else if (tmpNumber instanceof Quaternion) {
            return (LDL<N>) QUATERNION.make(typical);
        } else if (tmpNumber instanceof ComplexNumber) {
            return (LDL<N>) COMPLEX.make(typical);
        } else if (tmpNumber instanceof Double) {
            return (LDL<N>) PRIMITIVE.make(typical);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @deprecated v48 Use {@link #reconstruct()} instead
     */
    @Deprecated
    static <N extends Comparable<N>> MatrixStore<N> reconstruct(final LDL<N> decomposition) {
        return decomposition.reconstruct();
    }

    MatrixStore<N> getD();

    /**
     * Must implement either {@link #getL()} or {@link #getR()}.
     */
    default MatrixStore<N> getL() {
        return this.getR().conjugate();
    }

    /**
     * Must implement either {@link #getL()} or {@link #getR()}.
     */
    default MatrixStore<N> getR() {
        return this.getL().conjugate();
    }

    default MatrixStore<N> reconstruct() {

        MatrixStore<N> mtrxL = this.getL();
        MatrixStore<N> mtrxD = this.getD();
        MatrixStore<N> mtrxR = this.getR();

        int[] pivotOrder = this.getPivotOrder();

        return mtrxL.multiply(mtrxD).multiply(mtrxR).logical().row(pivotOrder).column(pivotOrder).get();
    }
}
