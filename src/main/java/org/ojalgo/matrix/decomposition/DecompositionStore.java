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

import org.ojalgo.array.Array1D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.matrix.decomposition.function.ExchangeColumns;
import org.ojalgo.matrix.decomposition.function.NegateColumn;
import org.ojalgo.matrix.decomposition.function.RotateRight;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.ComplexNumber;

/**
 * <p>
 * Only classes that will act as a delegate to a {@linkplain MatrixDecomposition} implementation from this
 * package should implement this interface. The interface specifications are entirely dictated by the classes
 * in this package.
 * </p>
 * <p>
 * Do not use it for anything else!
 * </p>
 *
 * @author apete
 */
public interface DecompositionStore<N extends Comparable<N>> extends PhysicalStore<N>, RotateRight, ExchangeColumns, NegateColumn {

    /**
     * Cholesky transformations
     */
    void applyCholesky(final int iterationPoint, final BasicArray<N> multipliers);

    /**
     * LDL transformations
     */
    void applyLDL(final int iterationPoint, final BasicArray<N> multipliers);

    /**
     * LU transformations
     */
    void applyLU(final int iterationPoint, final BasicArray<N> multipliers);

    Array1D<ComplexNumber> computeInPlaceSchur(PhysicalStore<N> transformationCollector, boolean eigenvalue);

    void divideAndCopyColumn(int row, int column, BasicArray<N> destination);

    default void exchangeColumns(final int colA, final int colB) {
        this.exchangeColumns((long) colA, (long) colB);
    }

    void exchangeHermitian(int indexA, int indexB);

    boolean generateApplyAndCopyHouseholderColumn(final int row, final int column, final Householder<N> destination);

    boolean generateApplyAndCopyHouseholderRow(final int row, final int column, final Householder<N> destination);

    void setToIdentity(int aCol);

    default Array1D<N> sliceColumn(final long col) {
        return this.sliceColumn(0L, col);
    }

    Array1D<N> sliceColumn(long row, long col);

    Array1D<N> sliceDiagonal(long row, long col);

    Array1D<N> sliceRange(long first, long limit);

    default Array1D<N> sliceRow(final long row) {
        return this.sliceRow(row, 0L);
    }

    Array1D<N> sliceRow(long row, long col);

    void transformSymmetric(Householder<N> transformation);

    void tred2(BasicArray<N> mainDiagonal, BasicArray<N> offDiagonal, boolean yesvecs);

}
