/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * You create instances of (some subclass of) this class by calling the static factory method
 * {@linkplain Schur#makePrimitive()}.
 *
 * @deprecated v38 This class will be made package private. Use the inteface instead.
 * @author apete
 */
@Deprecated
public abstract class SchurDecomposition<N extends Number> extends InPlaceDecomposition<N> implements Schur<N> {

    public static final class SchurResult<N extends Number> extends Object {

        private final Array1D<ComplexNumber> myDiagonal;
        private final MatrixStore<N> myQ;

        public SchurResult(final Array1D<ComplexNumber> aDiagonal, final MatrixStore<N> aQ) {

            super();

            myDiagonal = aDiagonal;
            myQ = aQ;
        }

        @SuppressWarnings("unused")
        private SchurResult() {
            this(null, null);
        }

        public final Array1D<ComplexNumber> getDiagonal() {
            return myDiagonal;
        }

        public final MatrixStore<N> getQ() {
            return myQ;
        }

    }

    static final class Primitive extends SchurDecomposition<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY);
        }

    }

    /**
     * @deprecated v38 Use {@link Schur#make(Access2D<N>)} instead
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static final <N extends Number> Schur<N> make(final Access2D<N> aTypical) {
        return Schur.make(aTypical);
    }

    /**
     * @deprecated v38 Use {@link Schur#makePrimitive()} instead
     */
    @Deprecated
    public static final Schur<Double> makePrimitive() {
        return Schur.makePrimitive();
    }

    private Array1D<ComplexNumber> myDiagonal;
    private MatrixStore<N> myQ;

    protected SchurDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        super(aFactory);
    }

    public boolean compute(final Access2D<?> matrix) {

        this.reset();

        this.setInPlace(matrix);

        final int tmpDiagDim = this.getMinDim();

        final DecompositionStore<N> tmpQ = this.makeEye(tmpDiagDim, tmpDiagDim);

        final Array1D<ComplexNumber> tmpDiagonal = this.getInPlace().computeInPlaceSchur(tmpQ, false);

        this.setQ(tmpQ);
        this.setDiagonal(tmpDiagonal);

        return this.computed(true);
    }

    public boolean equals(final MatrixStore<N> other, final NumberContext context) {
        return MatrixUtils.equals(other, this, context);
    }

    public Array1D<ComplexNumber> getDiagonal() {
        return myDiagonal;
    }

    public MatrixStore<N> getQ() {
        return myQ;
    }

    public MatrixStore<N> getU() {
        return this.getInPlace().builder().hessenberg(true).build();
    }

    public boolean isFullSize() {
        return true;
    }

    public boolean isOrdered() {
        return false;
    }

    public boolean isSolvable() {
        return false;
    }

    public MatrixStore<N> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }

    @Override
    public void reset() {

        super.reset();

        myDiagonal = null;
        myQ = null;
    }

    final void setDiagonal(final Array1D<ComplexNumber> newDiagonal) {
        myDiagonal = newDiagonal;
    }

    final void setQ(final MatrixStore<N> newQ) {
        myQ = newQ;
    }

}
