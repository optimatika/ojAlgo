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
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.context.NumberContext;

/**
 * Hessenberg: [A] = [Q][H][Q]<sup>T</sup> A general square matrix [A] can be decomposed by orthogonal
 * similarity transformations into the form [A]=[Q][H][Q]<sup>T</sup> where
 * <ul>
 * <li>[H] is upper (or lower) hessenberg matrix</li>
 * <li>[Q] is orthogonal/unitary</li>
 * </ul>
 *
 * @author apete
 */
public interface Hessenberg<N extends Comparable<N>> extends MatrixDecomposition<N> {

    interface Factory<N extends Comparable<N>> extends MatrixDecomposition.Factory<Hessenberg<N>> {

    }

    Factory<ComplexNumber> C128 = typical -> new HessenbergDecomposition.C128();

    Factory<Quadruple> R128 = typical -> new HessenbergDecomposition.R128();

    Factory<Double> R064 = typical -> new HessenbergDecomposition.R064();

    Factory<Quaternion> H256 = typical -> new HessenbergDecomposition.H256();

    Factory<RationalNumber> Q128 = typical -> new HessenbergDecomposition.Q128();

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

    static <N extends Comparable<N>> boolean equals(final MatrixStore<N> matrix, final Hessenberg<N> decomposition, final NumberContext context) {

        final MatrixStore<N> tmpH = decomposition.getH();
        final MatrixStore<N> tmpQ = decomposition.getQ();

        final MatrixStore<N> tmpStore1 = matrix.multiply(tmpQ);
        final MatrixStore<N> tmpStore2 = tmpQ.multiply(tmpH);

        return Access2D.equals(tmpStore1, tmpStore2, context);
    }

    boolean compute(Access2D.Collectable<N, ? super PhysicalStore<N>> matrix, boolean upper);

    MatrixStore<N> getH();

    MatrixStore<N> getQ();

    boolean isUpper();

    default MatrixStore<N> reconstruct() {
        MatrixStore<N> mtrxQ = this.getQ();
        MatrixStore<N> mtrxH = this.getH();
        return mtrxQ.multiply(mtrxH).multiply(mtrxQ.transpose());
    }
}
