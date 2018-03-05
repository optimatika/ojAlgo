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
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * Schur: [A] = [Q][U][Q]<sup>-1</sup> [A] = [Q][U][Q]<sup>-1</sup> where:
 * <ul>
 * <li>[A] is a square complex entry matrix.</li>
 * <li>[Q] is a unitary matrix (so that [Q]<sup>-1</sup> equals [Q]<sup>H</sup>).</li>
 * <li>[U] is an upper triangular matrix, which is called a Schur form of [A]. Since [U] is similar to [A], it
 * has the same multiset of eigenvalues, and since it is triangular, those eigenvalues are the diagonal
 * entries of [U].</li>
 * </ul>
 *
 * @author apete
 * @deprecated v43 Use {@linkplain Eigenvalue} instead
 */
@Deprecated
public interface Schur<N extends Number> extends MatrixDecomposition<N> {

    @SuppressWarnings("unchecked")
    public static <N extends Number> Schur<N> make(final Access2D<N> typical) {

        final N tmpNumber = typical.get(0, 0);

        if (tmpNumber instanceof Double) {
            return (Schur<N>) new SchurDecomposition.Primitive();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static Schur<Double> makePrimitive() {
        return new SchurDecomposition.Primitive();
    }

    static <N extends Number> boolean equals(final MatrixStore<N> matrix, final Schur<N> decomposition, final NumberContext context) {

        final MatrixStore<N> tmpU = decomposition.getU();
        final MatrixStore<N> tmpQ = decomposition.getQ();

        // Check that [A][Q] == [Q][U] ([A] == [Q][U][Q]<sup>T</sup> is not always true)
        final MatrixStore<N> tmpStore1 = matrix.multiply(tmpQ);
        final MatrixStore<N> tmpStore2 = tmpQ.multiply(tmpU);

        return Access2D.equals(tmpStore1, tmpStore2, context);
    }

    static <N extends Number> MatrixStore<N> reconstruct(final Schur<N> decomposition) {
        final MatrixStore<N> tmpQ = decomposition.getQ();
        return tmpQ.multiply(decomposition.getU()).multiply(tmpQ.logical().transpose().get());
    }

    Array1D<ComplexNumber> getDiagonal();

    MatrixStore<N> getQ();

    MatrixStore<N> getU();

    boolean isOrdered();

    default MatrixStore<N> reconstruct() {
        return Schur.reconstruct(this);
    }

}
