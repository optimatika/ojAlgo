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
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;
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

        /**
         * @see LDL#modified(Factory, Comparable)
         */
        default Factory<N> modified(final N threshold) {
            return new ModifiedFactory<>(this, threshold);
        }

    }

    final class ModifiedFactory<N extends Comparable<N>> implements Factory<N> {

        private final Factory<N> myDelegate;
        private final N myThreshold;

        ModifiedFactory(final Factory<N> delegate, final N threshold) {
            super();
            myDelegate = delegate;
            myThreshold = threshold;
        }

        public LDL<N> make(final Structure2D typical) {
            LDL<N> retVal = myDelegate.make(typical);
            if (myThreshold != null && retVal instanceof LDLDecomposition) {
                ((LDLDecomposition<N>) retVal).setThreshold(myThreshold);
            }
            return retVal;
        }

    }

    Factory<ComplexNumber> C128 = typical -> new LDLDecomposition.C128();

    /**
     * @deprecated
     */
    @Deprecated
    Factory<ComplexNumber> COMPLEX = C128;

    Factory<Quaternion> H256 = typical -> new LDLDecomposition.H256();

    /**
     * @deprecated
     */
    @Deprecated
    Factory<Quaternion> QUATERNION = H256;

    Factory<Double> R064 = typical -> new LDLDecomposition.R064();

    Factory<Quadruple> R128 = typical -> new LDLDecomposition.R128();

    /**
     * @deprecated
     */
    @Deprecated
    Factory<Double> PRIMITIVE = R064;

    Factory<RationalNumber> Q128 = typical -> new LDLDecomposition.Q128();

    /**
     * @deprecated
     */
    @Deprecated
    Factory<Quadruple> QUADRUPLE = R128;

    /**
     * @deprecated
     */
    @Deprecated
    Factory<RationalNumber> RATIONAL = Q128;

    static <N extends Comparable<N>> boolean equals(final MatrixStore<N> matrix, final LDL<N> decomposition, final NumberContext context) {
        return Access2D.equals(matrix, decomposition.reconstruct(), context);
    }

    /**
     * Will return a modified LDL decomposition algoritm. It's the Gill, Murray and Wright (GMW) algorithm.
     * <p>
     * The input threshold is the bound on the diagonal values.
     * <p>
     * The second parameter of the GMW algorithm, that is supposed to cap the magnitude of the elements in the
     * triangular (Cholesky) matrices, is set to something very large. More correctly, it is assumed to be
     * very large and therefore resulting in a negligible contribution to the algorithm.
     */
    static <N extends Comparable<N>> Factory<N> modified(final Factory<N> delegate, final N threshold) {
        return new ModifiedFactory<>(delegate, threshold);
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

        int[] reverseOrder = this.getReversePivotOrder();

        return mtrxL.multiply(mtrxD).multiply(mtrxR).rows(reverseOrder).columns(reverseOrder);
    }
}
