/*
 * Copyright 1997-2025 Optimatika
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
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.type.context.NumberContext;

/**
 * LU: [A] = [L][U]
 * <p>
 * Decomposes [this] into [L] and [U] (with pivot order information in an int[]) where:
 * </p>
 * <ul>
 * <li>[L] is a unit lower (left) triangular matrix. It has the same number of rows as [this], and ones on the
 * diagonal.</li>
 * <li>[U] is an upper (right) triangular matrix. It has the same number of columns as [this].</li>
 * <li>[this] = [L][U] (with reordered rows according to the pivot order)</li>
 * </ul>
 * <p>
 * Note: The number of columns in [L] and the number of rows in [U] is not specified by this interface.
 * </p>
 * <p>
 * The LU decomposition always exists - the compute method should always succeed - even for non-square and/or
 * singular matrices. The primary use of the LU decomposition is in the solution of systems of simultaneous
 * linear equations. That will, however, only work for square non-singular matrices.
 * </p>
 *
 * @author apete
 */
public interface LU<N extends Comparable<N>> extends LDU<N>, MatrixDecomposition.Updatable<N>, MatrixDecomposition.Pivoting<N> {

    interface Factory<N extends Comparable<N>> extends MatrixDecomposition.Factory<LU<N>> {

    }

    Factory<ComplexNumber> C128 = typical -> new DenseLU.C128();

    Factory<Quaternion> H256 = typical -> new DenseLU.H256();

    Factory<RationalNumber> Q128 = typical -> new DenseLU.Q128();

    Factory<Double> R064 = typical -> {

        if (512L < typical.countColumns() && typical.count() <= PlainArray.MAX_SIZE) {
            return new DenseLU.R064();
        } else {
            return new RawLU();
        }
    };

    Factory<Quadruple> R128 = typical -> new DenseLU.R128();

    static <N extends Comparable<N>> boolean equals(final MatrixStore<N> matrix, final LU<N> decomposition, final NumberContext context) {

        MatrixStore<N> tmpL = decomposition.getL();
        MatrixStore<N> tmpU = decomposition.getU();
        int[] tmpPivotOrder = decomposition.getPivotOrder();

        return Access2D.equals(matrix.rows(tmpPivotOrder), tmpL.multiply(tmpU), context);
    }

    MatrixStore<N> getL();

    @Override
    default MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs) {
        return this.getSolution(rhs, this.preallocate(this, rhs));
    }

    /**
     * http://en.wikipedia.org/wiki/Row_echelon_form <br>
     * <br>
     * This is the same as [D][U]. Together with the pivotOrder and [L] this constitutes an alternative, more
     * compact, way to express the decomposition.
     *
     * @see #getPivotOrder()
     * @see #getL()
     */
    MatrixStore<N> getU();

    @Override
    default PhysicalStore<N> preallocate(final int nbEquations) {
        return this.preallocate(nbEquations, 1);
    }

    @Override
    default MatrixStore<N> reconstruct() {
        MatrixStore<N> mtrxL = this.getL();
        MatrixStore<N> mtrxU = this.getU();
        int[] reversePivotOrder = this.getReversePivotOrder();
        return mtrxL.multiply(mtrxU).rows(reversePivotOrder);
    }

    @Override
    default boolean updateColumn(final int columnIndex, final Access1D.Collectable<N, ? super TransformableRegion<N>> newColumn,
            final PhysicalStore<N> preallocated) {
        throw new UnsupportedOperationException();
    }

}
