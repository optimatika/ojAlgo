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
import org.ojalgo.type.context.NumberContext;

/**
 * Tridiagonal: [A] = [Q][D][Q]<sup>H</sup> Any square symmetric (hermitian) matrix [A] can be factorized by
 * similarity transformations into the form, [A]=[Q][D][Q]<sup>-1</sup> where [Q] is an orthogonal (unitary)
 * matrix and [D] is a real symmetric tridiagonal matrix. Note that [D] can/should be made real even when [A]
 * has complex elements. Since [Q] is orthogonal (unitary) [Q]<sup>-1</sup> = [Q]<sup>H</sup> and when it is
 * real [Q]<sup>H</sup> = [Q]<sup>T</sup>.
 *
 * @author apete
 */
public interface Tridiagonal<N extends Comparable<N>> extends MatrixDecomposition<N> {

    interface Factory<N extends Comparable<N>> extends MatrixDecomposition.Factory<Tridiagonal<N>> {

    }

    Factory<ComplexNumber> C128 = typical -> new DeferredTridiagonal.C128();

    Factory<Double> R064 = typical -> new DeferredTridiagonal.R064();

    Factory<Quadruple> R128 = typical -> new DeferredTridiagonal.R128();

    Factory<Quaternion> H256 = typical -> new DeferredTridiagonal.H256();

    Factory<RationalNumber> Q128 = typical -> new DeferredTridiagonal.Q128();

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

    static <N extends Comparable<N>> boolean equals(final MatrixStore<N> matrix, final Tridiagonal<N> decomposition, final NumberContext context) {

        boolean retVal = true;

        // Check that [A] == [Q][D][Q]<sup>T</sup>
        retVal &= Access2D.equals(matrix, decomposition.reconstruct(), context);

        // Check that Q is orthogonal/unitary...

        final MatrixStore<N> mtrxQ = decomposition.getQ();
        MatrixStore<N> identity = mtrxQ.physical().makeEye(mtrxQ.countRows(), mtrxQ.countColumns());

        MatrixStore<N> qqh = mtrxQ.multiply(mtrxQ.conjugate());
        retVal &= qqh.equals(identity, context);

        MatrixStore<N> qhq = mtrxQ.conjugate().multiply(mtrxQ);
        retVal &= qhq.equals(identity, context);

        return retVal;
    }

    MatrixStore<N> getD();

    MatrixStore<N> getQ();

    default MatrixStore<N> reconstruct() {
        MatrixStore<N> mtrxQ = this.getQ();
        MatrixStore<N> mtrxD = this.getD();
        return mtrxQ.multiply(mtrxD).multiply(mtrxQ.conjugate());
    }

}
