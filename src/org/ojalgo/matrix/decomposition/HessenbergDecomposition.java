/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * @deprecated v38 This class will be made package private. Use the inteface instead.
 * @author apete
 */
@Deprecated
public abstract class HessenbergDecomposition<N extends Number> extends InPlaceDecomposition<N> implements Hessenberg<N> {

    static final class Big extends HessenbergDecomposition<BigDecimal> {

        Big() {
            super(BigDenseStore.FACTORY);
        }

    }

    static final class Complex extends HessenbergDecomposition<ComplexNumber> {

        Complex() {
            super(ComplexDenseStore.FACTORY);
        }

    }

    static final class Primitive extends HessenbergDecomposition<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY);
        }

    }

    /**
     * @deprecated v38 Use {@link Hessenberg#make(Access2D)} instead
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static final <N extends Number> Hessenberg<N> make(final Access2D<N> aTypical) {
        return Hessenberg.make(aTypical);
    }

    /**
     * @deprecated v38 Use {@link Hessenberg#makeBig()} instead
     */
    @Deprecated
    public static Hessenberg<BigDecimal> makeBig() {
        return Hessenberg.makeBig();
    }

    /**
     * @deprecated v38 Use {@link Hessenberg#makeComplex()} instead
     */
    @Deprecated
    public static Hessenberg<ComplexNumber> makeComplex() {
        return Hessenberg.makeComplex();
    }

    /**
     * @deprecated v38 Use {@link Hessenberg#makePrimitive()} instead
     */
    @Deprecated
    public static Hessenberg<Double> makePrimitive() {
        return Hessenberg.makePrimitive();
    }

    private transient DecompositionStore<N> myQ = null;

    private boolean myUpper = true;

    protected HessenbergDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        super(aFactory);
    }

    public final boolean compute(final Access2D<?> matrix) {
        return this.compute(matrix, true);
    }

    public final boolean compute(final Access2D<?> matrix, final boolean upper) {

        this.reset();

        myUpper = upper;

        final DecompositionStore<N> tmpStore = this.setInPlace(matrix);

        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim();

        if (upper) {

            final Householder<N> tmpHouseholderCol = this.makeHouseholder(tmpRowDim);

            final int tmpLimit = Math.min(tmpRowDim, tmpColDim) - 2;

            for (int ij = 0; ij < tmpLimit; ij++) {
                if (tmpStore.generateApplyAndCopyHouseholderColumn(ij + 1, ij, tmpHouseholderCol)) {
                    tmpStore.transformLeft(tmpHouseholderCol, ij + 1);
                    tmpStore.transformRight(tmpHouseholderCol, 0);
                }
            }

        } else {

            final Householder<N> tmpHouseholderRow = this.makeHouseholder(tmpColDim);

            final int tmpLimit = Math.min(tmpRowDim, tmpColDim) - 2;

            for (int ij = 0; ij < tmpLimit; ij++) {
                if (tmpStore.generateApplyAndCopyHouseholderRow(ij, ij + 1, tmpHouseholderRow)) {
                    tmpStore.transformRight(tmpHouseholderRow, ij + 1);
                    tmpStore.transformLeft(tmpHouseholderRow, 0);
                }
            }
        }

        return this.computed(true);
    }

    public final boolean equals(final MatrixStore<N> aStore, final NumberContext context) {
        return MatrixUtils.equals(aStore, this, context);
    }

    public final MatrixStore<N> getH() {
        return this.getInPlace().builder().hessenberg(myUpper).build();
    }

    public final MatrixStore<N> getQ() {
        if (myQ == null) {
            myQ = this.makeQ(this.makeEye(this.getRowDim(), this.getColDim()), myUpper, true);
        }
        return myQ;
    }

    public final boolean isFullSize() {
        return true;
    }

    public final boolean isSolvable() {
        return false;
    }

    public boolean isUpper() {
        return myUpper;
    }

    @Override
    public void reset() {

        super.reset();

        myQ = null;
        myUpper = true;
    }

    public MatrixStore<N> solve(final Access2D<N> rhs, final DecompositionStore<N> preallocated) {
        throw new UnsupportedOperationException();
    }

    private final DecompositionStore<N> makeQ(final DecompositionStore<N> aStoreToTransform, final boolean tmpUpper, final boolean eye) {

        final int tmpRowAndColDim = (int) aStoreToTransform.countRows();

        final DecompositionStore.HouseholderReference<N> tmpHouseholderReference = new DecompositionStore.HouseholderReference<N>(this.getInPlace(), tmpUpper);

        for (int ij = tmpRowAndColDim - 3; ij >= 0; ij--) {

            tmpHouseholderReference.row = tmpUpper ? ij + 1 : ij;
            tmpHouseholderReference.col = tmpUpper ? ij : ij + 1;

            if (!tmpHouseholderReference.isZero()) {
                aStoreToTransform.transformLeft(tmpHouseholderReference, eye ? ij : 0);
            }
        }

        return aStoreToTransform;
    }

    final DecompositionStore<N> doQ(final DecompositionStore<N> aStoreToTransform) {
        return this.makeQ(aStoreToTransform, myUpper, false);
    }

}
