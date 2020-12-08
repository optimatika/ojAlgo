/*
 * Copyright 1997-2020 Optimatika
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

import org.ojalgo.core.scalar.ComplexNumber;
import org.ojalgo.core.scalar.Quaternion;
import org.ojalgo.core.scalar.RationalNumber;
import org.ojalgo.core.structure.Access2D;
import org.ojalgo.core.type.context.NumberContext;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;

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

    Factory<ComplexNumber> COMPLEX = typical -> new HessenbergDecomposition.Complex();

    Factory<Double> PRIMITIVE = typical -> new HessenbergDecomposition.Primitive();

    Factory<Quaternion> QUATERNION = typical -> new HessenbergDecomposition.Quat();

    Factory<RationalNumber> RATIONAL = typical -> new HessenbergDecomposition.Rational();

    static <N extends Comparable<N>> boolean equals(final MatrixStore<N> matrix, final Hessenberg<N> decomposition, final NumberContext context) {

        final MatrixStore<N> tmpH = decomposition.getH();
        final MatrixStore<N> tmpQ = decomposition.getQ();

        final MatrixStore<N> tmpStore1 = matrix.multiply(tmpQ);
        final MatrixStore<N> tmpStore2 = tmpQ.multiply(tmpH);

        return Access2D.equals(tmpStore1, tmpStore2, context);
    }

    /**
     * @deprecated v48 Use {link #COMPLEX}, {@link #PRIMITIVE}. {@link #QUATERNION} or {@link #RATIONAL}
     *             innstead.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    static <N extends Comparable<N>> Hessenberg<N> make(final Access2D<N> typical) {

        final N tmpNumber = typical.get(0, 0);

        if (tmpNumber instanceof RationalNumber) {
            return (Hessenberg<N>) RATIONAL.make(typical);
        } else if (tmpNumber instanceof Quaternion) {
            return (Hessenberg<N>) QUATERNION.make(typical);
        } else if (tmpNumber instanceof ComplexNumber) {
            return (Hessenberg<N>) COMPLEX.make(typical);
        } else if (tmpNumber instanceof Double) {
            return (Hessenberg<N>) PRIMITIVE.make(typical);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @deprecated v48 Use {@link #reconstruct()} instead
     */
    @Deprecated
    static <N extends Comparable<N>> MatrixStore<N> reconstruct(final Hessenberg<N> decomposition) {
        return decomposition.reconstruct();
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
