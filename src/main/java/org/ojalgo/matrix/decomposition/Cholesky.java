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
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * Cholesky: [A] = [L][L]<sup>H</sup> (or [R]<sup>H</sup>[R])
 * </p>
 * <p>
 * [A]<sup>H</sup> = [A] = [L][L]<sup>H</sup>
 * </p>
 * <p>
 * If [A] is symmetric and positive definite then the general LU decomposition - [P][L][D][U] - becomes
 * [I][L][D][L]<sup>T</sup> (or [I][U]<sup>T</sup>[D][U]). [I] can be left out and [D] is normally split in
 * halves and merged with [L] (and/or [U]). We'll express it as [A] = [L][L]<sup>T</sup>.
 * </p>
 * <p>
 * A cholesky decomposition is still/also an LU decomposition where [P][L][D][U] =&gt; [L][L]<sup>T</sup>.
 * </p>
 *
 * @author apete
 */
public interface Cholesky<N extends Comparable<N>> extends LDU<N>, MatrixDecomposition.Hermitian<N> {

    interface Factory<N extends Comparable<N>> extends MatrixDecomposition.Factory<Cholesky<N>> {

    }

    Factory<ComplexNumber> C128 = typical -> new CholeskyDecomposition.C128();

    Factory<Double> R064 = typical -> {
        if ((32L < typical.countColumns()) && (typical.count() <= PlainArray.MAX_SIZE)) {
            return new CholeskyDecomposition.R064();
        } else {
            return new RawCholesky();
        }
    };

    Factory<Quadruple> R128 = typical -> new CholeskyDecomposition.R128();

    Factory<Quaternion> H256 = typical -> new CholeskyDecomposition.H256();

    Factory<RationalNumber> Q128 = typical -> new CholeskyDecomposition.Q128();

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

    static <N extends Comparable<N>> boolean equals(final MatrixStore<N> matrix, final Cholesky<N> decomposition, final NumberContext context) {

        boolean retVal = false;

        final MatrixStore<N> tmpL = decomposition.getL();

        return Access2D.equals(tmpL.multiply(tmpL.conjugate()), matrix, context);
    }

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

    /**
     * To use the Cholesky decomposition rather than the LU decomposition the matrix must be symmetric and
     * positive definite. It is recommended that the decomposition algorithm checks for this during
     * calculation. Possibly the matrix could be assumed to be symmetric (to improve performance) but tests
     * should be made to assure the matrix is positive definite.
     *
     * @return true if the tests did not fail.
     */
    boolean isSPD();

    default MatrixStore<N> reconstruct() {
        final MatrixStore<N> mtrxL = this.getL();
        return mtrxL.multiply(mtrxL.conjugate());
    }

}
