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

import static org.ojalgo.constant.PrimitiveMath.*;

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.MatrixStore.Builder;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

abstract class LDLDecomposition<N extends Number> extends InPlaceDecomposition<N> implements LDL<N> {

    static final class Primitive extends LDLDecomposition<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY);
        }

    }

    static final class Big extends LDLDecomposition<BigDecimal> {

        Big() {
            super(BigDenseStore.FACTORY);
        }

    }

    static final class Complex extends LDLDecomposition<ComplexNumber> {

        Complex() {
            super(ComplexDenseStore.FACTORY);
        }

    }

    protected LDLDecomposition(final PhysicalStore.Factory<N, ? extends DecompositionStore<N>> factory) {
        super(factory);
    }

    public boolean compute(final Access2D<?> matrix) {

        this.reset();

        final DecompositionStore<N> tmpInPlace = this.setInPlace(matrix);

        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim();
        final int tmpMinDim = this.getMinDim();

        final BasicArray<N> tmpMultipliers = this.makeArray(tmpRowDim);

        // Main loop - along the diagonal
        for (int ij = 0; ij < tmpMinDim; ij++) {

            // Do the calculations...
            if (tmpInPlace.doubleValue(ij, ij) != PrimitiveMath.ZERO) {

                // Calculate multipliers and copy to local column
                // Current column, below the diagonal
                tmpInPlace.divideAndCopyColumn(ij, ij, tmpMultipliers);

                // Apply transformations to everything below and to the right of the pivot element
                tmpInPlace.applyLDL(ij, tmpMultipliers);

            } else {

                tmpInPlace.set(ij, ij, ZERO);
            }

        }

        return this.computed(true);
    }

    public boolean equals(final MatrixStore<N> other, final NumberContext context) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isFullSize() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSolvable() {
        // TODO Auto-generated method stub
        return false;
    }

    public MatrixStore<N> reconstruct() {
        // TODO Auto-generated method stub
        return null;
    }

    public N getDeterminant() {
        // TODO Auto-generated method stub
        return null;
    }

    public MatrixStore<N> getL() {
        final DecompositionStore<N> tmpInPlace = this.getInPlace();
        final Builder<N> tmpBuilder = tmpInPlace.builder();
        final Builder<N> tmpTriangular = tmpBuilder.triangular(false, true);
        return tmpTriangular.build();
    }

    public MatrixStore<N> getD() {
        final DecompositionStore<N> tmpInPlace = this.getInPlace();
        final Builder<N> tmpBuilder = tmpInPlace.builder();
        final Builder<N> tmpTriangular = tmpBuilder.diagonal(false);
        return tmpTriangular.build();
    }

    public int getRank() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isSquareAndNotSingular() {
        // TODO Auto-generated method stub
        return false;
    }

}
