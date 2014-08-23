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

import java.util.Iterator;
import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

final class TransposedDecompositionStore<N extends Number> implements DecompositionStore<N> {

    private final DecompositionStore<N> myDelegate;

    TransposedDecompositionStore(final DecompositionStore<N> delegate) {

        super();

        myDelegate = delegate;
    }

    public MatrixStore<N> add(final MatrixStore<N> addend) {
        // TODO Auto-generated method stub
        return null;
    }

    public N aggregateAll(final Aggregator aggregator) {
        return myDelegate.aggregateAll(aggregator);
    }

    public void applyCholesky(final int iterationPoint, final BasicArray<N> multipliers) {
        // TODO Auto-generated method stub

    }

    public void applyLU(final int iterationPoint, final BasicArray<N> multipliers) {
        // TODO Auto-generated method stub

    }

    public Array2D<N> asArray2D() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<N> asList() {
        // TODO Auto-generated method stub
        return null;
    }

    public MatrixStore.Builder<N> builder() {
        return new MatrixStore.Builder<N>(myDelegate.transpose());
    }

    public void caxpy(final N aSclrA, final int aColX, final int aColY, final int aFirstRow) {
        myDelegate.raxpy(aSclrA, aColX, aColY, aFirstRow);
    }

    public Array1D<ComplexNumber> computeInPlaceSchur(final PhysicalStore<N> aTransformationCollector, final boolean eigenvalue) {
        // TODO Auto-generated method stub
        return null;
    }

    public MatrixStore<N> conjugate() {
        // TODO Auto-generated method stub
        return null;
    }

    public PhysicalStore<N> copy() {
        // TODO Auto-generated method stub
        return null;
    }

    public long count() {
        return myDelegate.count();
    }

    public long countColumns() {
        return myDelegate.countRows();
    }

    public long countRows() {
        return myDelegate.countColumns();
    }

    public void divideAndCopyColumn(final int aRow, final int aCol, final BasicArray<N> aDestination) {
        // TODO Auto-generated method stub

    }

    public double doubleValue(final long index) {
        // TODO Auto-generated method stub
        return 0;
    }

    public double doubleValue(final long row, final long column) {
        return myDelegate.doubleValue(column, row);
    }

    public boolean equals(final MatrixStore<N> other, final NumberContext context) {
        // TODO Auto-generated method stub
        return false;
    }

    public void exchangeColumns(final int aColA, final int aColB) {
        myDelegate.exchangeRows(aColA, aColB);
    }

    public void exchangeRows(final int aRowA, final int aRowB) {
        // TODO Auto-generated method stub
    }

    public PhysicalStore.Factory<N, ?> factory() {
        return myDelegate.factory();
    }

    public void fillAll(final N value) {
        myDelegate.fillAll(value);
    }

    public void fillByMultiplying(final Access1D<N> leftMtrx, final Access1D<N> rightMtrx) {
        // TODO Auto-generated method stub

    }

    public void fillColumn(final long row, final long column, final N value) {
        // TODO Auto-generated method stub

    }

    public void fillConjugated(final Access2D<? extends Number> source) {
        // TODO Auto-generated method stub

    }

    public void fillDiagonal(final long row, final long column, final N value) {
        // TODO Auto-generated method stub

    }

    public void fillMatching(final Access1D<? extends Number> source) {
        // TODO Auto-generated method stub

    }

    public void fillMatching(final Access1D<N> leftArg, final BinaryFunction<N> func, final Access1D<N> rightArg) {
        // TODO Auto-generated method stub

    }

    public void fillMatching(final Access1D<N> leftArg, final BinaryFunction<N> func, final N rightArg) {
        // TODO Auto-generated method stub
    }

    public void fillMatching(final N leftArg, final BinaryFunction<N> func, final Access1D<N> rightArg) {
        // TODO Auto-generated method stub

    }

    public void fillRange(final long first, final long limit, final N value) {
        // TODO Auto-generated method stub

    }

    public void fillRow(final long row, final long column, final N value) {
        myDelegate.fillColumn(column, row, value);
    }

    public void fillTransposed(final Access2D<? extends Number> source) {
        // TODO Auto-generated method stub

    }

    public boolean generateApplyAndCopyHouseholderColumn(final int aRow, final int aCol, final Householder<N> aDestination) {
        return myDelegate.generateApplyAndCopyHouseholderRow(aCol, aRow, aDestination);
    }

    public boolean generateApplyAndCopyHouseholderRow(final int aRow, final int aCol, final Householder<N> aDestination) {
        return myDelegate.generateApplyAndCopyHouseholderColumn(aCol, aRow, aDestination);
    }

    public N get(final long index) {
        // TODO Auto-generated method stub
        return null;
    }

    public N get(final long row, final long column) {
        return myDelegate.get(column, row);
    }

    public int getIndexOfLargestInColumn(final int aRow, final int aCol) {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isAbsolute(final long index) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isAbsolute(final long row, final long column) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isInfinite(final long index) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isInfinite(final long row, final long column) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isLowerLeftShaded() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isNaN(final long index) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isNaN(final long row, final long column) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isPositive(final long index) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isPositive(final long row, final long column) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isReal(final long index) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isReal(final long row, final long column) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isUpperRightShaded() {
        return myDelegate.isLowerLeftShaded();
    }

    public boolean isZero(final long index) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isZero(final long row, final long column) {
        // TODO Auto-generated method stub
        return false;
    }

    public Iterator<N> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    public void maxpy(final N aSclrA, final MatrixStore<N> aMtrxX) {
        // TODO Auto-generated method stub

    }

    public void modifyAll(final UnaryFunction<N> function) {
        // TODO Auto-generated method stub

    }

    public void modifyColumn(final long row, final long column, final UnaryFunction<N> function) {
        // TODO Auto-generated method stub

    }

    public void modifyDiagonal(final long row, final long column, final UnaryFunction<N> function) {
        // TODO Auto-generated method stub

    }

    public void modifyOne(final long row, final long column, final UnaryFunction<N> func) {
        // TODO Auto-generated method stub

    }

    public void modifyRange(final long first, final long limit, final UnaryFunction<N> function) {
        // TODO Auto-generated method stub

    }

    public void modifyRow(final long row, final long column, final UnaryFunction<N> function) {
        // TODO Auto-generated method stub

    }

    public MatrixStore<N> multiplyLeft(final Access1D<N> leftMtrx) {
        // TODO Auto-generated method stub
        return null;
    }

    public MatrixStore<N> multiplyRight(final Access1D<N> rightMtrx) {
        // TODO Auto-generated method stub
        return null;
    }

    public MatrixStore<N> negate() {
        // TODO Auto-generated method stub
        return null;
    }

    public void negateColumn(final int aCol) {
        // TODO Auto-generated method stub

    }

    public void raxpy(final N aSclrA, final int aRowX, final int aRowY, final int aFirstCol) {
        myDelegate.caxpy(aSclrA, aRowX, aRowY, aFirstCol);
    }

    public void rotateRight(final int aLow, final int aHigh, final double aCos, final double aSin) {
        // TODO Auto-generated method stub

    }

    public MatrixStore<N> scale(final N scalar) {
        return myDelegate.scale(scalar);
    }

    public void set(final long index, final double value) {
        // TODO Auto-generated method stub

    }

    public void set(final long row, final long column, final double value) {
        myDelegate.set(column, row, value);
    }

    public void set(final long row, final long column, final Number value) {
        myDelegate.set(column, row, value);
    }

    public void set(final long index, final Number value) {
        // TODO Auto-generated method stub
    }

    public void setToIdentity(final int aCol) {
        // TODO Auto-generated method stub
    }

    public void substituteBackwards(final Access2D<N> aBody, final boolean conjugated) {
        // TODO Auto-generated method stub
    }

    public void substituteForwards(final Access2D<N> aBody, final boolean onesOnDiagonal, final boolean zerosAboveDiagonal) {
        // TODO Auto-generated method stub
    }

    public MatrixStore<N> subtract(final MatrixStore<N> subtrahend) {
        // TODO Auto-generated method stub
        return null;
    }

    public Scalar<N> toScalar(final long row, final long column) {
        // TODO Auto-generated method stub
        return null;
    }

    public void transformLeft(final Householder<N> transformation, final int firstColumn) {
        // TODO Auto-generated method stub
    }

    public void transformLeft(final Rotation<N> transformation) {
        // TODO Auto-generated method stub
    }

    public void transformRight(final Householder<N> transformation, final int firstRow) {
        // TODO Auto-generated method stub
    }

    public void transformRight(final Rotation<N> transformation) {
        // TODO Auto-generated method stub
    }

    public void transformSymmetric(final Householder<N> aTransf) {
        // TODO Auto-generated method stub
    }

    public MatrixStore<N> transpose() {
        return myDelegate;
    }

    public void tred2(final BasicArray<N> mainDiagonal, final BasicArray<N> offDiagonal, final boolean yesvecs) {
        // TODO Auto-generated method stub
    }

    public void visitAll(final VoidFunction<N> visitor) {
        myDelegate.visitAll(visitor);
    }

    public void visitColumn(final long row, final long column, final VoidFunction<N> visitor) {
        myDelegate.visitRow(column, row, visitor);
    }

    public void visitDiagonal(final long row, final long column, final VoidFunction<N> visitor) {
        myDelegate.visitDiagonal(column, row, visitor);
    }

    public void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
        // TODO Auto-generated method stub

    }

    public void visitRow(final long row, final long column, final VoidFunction<N> visitor) {
        myDelegate.visitColumn(column, row, visitor);
    }
}
