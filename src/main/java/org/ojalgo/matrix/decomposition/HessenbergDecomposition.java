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

import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.HouseholderReference;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access2D;

abstract class HessenbergDecomposition<N extends Comparable<N>> extends InPlaceDecomposition<N> implements Hessenberg<N> {

    static final class C128 extends HessenbergDecomposition<ComplexNumber> {

        C128() {
            super(GenericStore.C128);
        }

    }

    static final class H256 extends HessenbergDecomposition<Quaternion> {

        H256() {
            super(GenericStore.H256);
        }

    }

    static final class Q128 extends HessenbergDecomposition<RationalNumber> {

        Q128() {
            super(GenericStore.Q128);
        }

    }

    static final class R064 extends HessenbergDecomposition<Double> {

        R064() {
            super(Primitive64Store.FACTORY);
        }

    }

    static final class R128 extends HessenbergDecomposition<Quadruple> {

        R128() {
            super(GenericStore.R128);
        }

    }

    private transient DecompositionStore<N> myQ = null;

    private boolean myUpper = true;

    protected HessenbergDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        super(aFactory);
    }

    public final boolean compute(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix, final boolean upper) {

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

    public final boolean decompose(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix) {
        return this.compute(matrix, true);
    }

    public final MatrixStore<N> getH() {
        return this.getInPlace().hessenberg(myUpper);
    }

    public final MatrixStore<N> getQ() {
        if (myQ == null) {
            myQ = this.makeQ(this.makeEye(this.getRowDim(), this.getColDim()), myUpper, true);
        }
        return myQ;
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

    private final DecompositionStore<N> makeQ(final DecompositionStore<N> storeToTransform, final boolean upper, final boolean eye) {

        final int tmpRowAndColDim = (int) storeToTransform.countRows();

        final HouseholderReference<N> tmpReference = HouseholderReference.make(this.getInPlace(), upper);

        for (int ij = tmpRowAndColDim - 3; ij >= 0; ij--) {

            tmpReference.point(upper ? ij + 1 : ij, upper ? ij : ij + 1);

            if (!tmpReference.isZero()) {
                storeToTransform.transformLeft(tmpReference, eye ? ij : 0);
            }
        }

        return storeToTransform;
    }

    final DecompositionStore<N> doQ(final DecompositionStore<N> aStoreToTransform) {
        return this.makeQ(aStoreToTransform, myUpper, false);
    }

}
