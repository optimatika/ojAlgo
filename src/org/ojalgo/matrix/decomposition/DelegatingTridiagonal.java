/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.HouseholderReference;
import org.ojalgo.scalar.ComplexNumber;

abstract class DelegatingTridiagonal<N extends Number> extends TridiagonalDecomposition<N> {

    protected DelegatingTridiagonal(final PhysicalStore.Factory<N, ? extends DecompositionStore<N>> factory) {
        super(factory);
    }

    static final class Big extends DelegatingTridiagonal<BigDecimal> {

        Big() {
            super(BigDenseStore.FACTORY);
        }

    }

    static final class Complex extends DelegatingTridiagonal<ComplexNumber> {

        Complex() {
            super(ComplexDenseStore.FACTORY);
        }

    }

    static final class Primitive extends DelegatingTridiagonal<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY);
        }

    }

    private transient DecompositionStore<N> myQ = null;

    public final boolean decompose(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix) {

        this.reset();

        final DecompositionStore<N> store = this.setInPlace(matrix);

        final int size = this.getRowDim();

        final Householder<N> tmpHouseholderCol = this.makeHouseholder(size);

        final int limit = size - 2;
        for (int ij = 0; ij < limit; ij++) {
            if (store.generateApplyAndCopyHouseholderColumn(ij + 1, ij, tmpHouseholderCol)) {
                store.transformLeft(tmpHouseholderCol, ij + 1);
                store.transformRight(tmpHouseholderCol, ij);
            }
        }

        return this.computed(true);
    }

    public final MatrixStore<N> getD() {
        return this.getInPlace().logical().tridiagonal().get();
    }

    public final MatrixStore<N> getQ() {

        if (myQ == null) {

            final int size = this.getMinDim();

            myQ = this.makeEye(size, size);

            final HouseholderReference<N> tmpHouseholder = HouseholderReference.make(this.getInPlace(), true);

            for (int ij = size - 3; ij >= 0; ij--) {
                tmpHouseholder.point(ij + 1, ij);
                if (!tmpHouseholder.isZero()) {
                    myQ.transformLeft(tmpHouseholder, ij);
                }
            }
        }

        return myQ;
    }

    @Override
    public void reset() {

        super.reset();

        myQ = null;
    }

}
