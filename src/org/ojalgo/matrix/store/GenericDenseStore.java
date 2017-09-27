/*
 * Copyright 1997-2017 Optimatika
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
package org.ojalgo.matrix.store;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.BaseStream;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.ColumnView;
import org.ojalgo.access.ElementView2D;
import org.ojalgo.access.Mutate1D;
import org.ojalgo.access.RowView;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.DenseArray;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;

/**
 * @deprecated Experimental code. Doesn't work and may never do so.
 * @author apete
 */
@Deprecated
final class GenericDenseStore<N extends Number & Scalar<N>> implements PhysicalStore<N>, DecompositionStore<N> {

    private final DenseArray<N> myDenseArray = null;
    private final long myStructure = 0L;
    private final Array2D<N> myUtility = null;

    public GenericDenseStore() {
        super();
    }

    public void add(final long index, final double addend) {
        myUtility.add(index, addend);
    }

    public void add(final long row, final long col, final double addend) {
        myUtility.add(row, col, addend);
    }

    public void add(final long row, final long col, final Number addend) {
        myUtility.add(row, col, addend);
    }

    public void add(final long index, final Number addend) {
        myUtility.add(index, addend);
    }

    public void applyCholesky(final int iterationPoint, final BasicArray<N> multipliers) {
        // TODO Auto-generated method stub

    }

    public void applyLDL(final int iterationPoint, final BasicArray<N> multipliers) {
        // TODO Auto-generated method stub

    }

    public void applyLU(final int iterationPoint, final BasicArray<N> multipliers) {
        // TODO Auto-generated method stub

    }

    public List<N> asList() {
        // TODO Auto-generated method stub
        return null;
    }

    public void axpy(final double a, final Mutate1D y) {
        myUtility.axpy(a, y);
    }

    public Iterable<ColumnView<N>> columns() {
        return myUtility.columns();
    }

    public Array1D<ComplexNumber> computeInPlaceSchur(final PhysicalStore<N> transformationCollector, final boolean eigenvalue) {
        // TODO Auto-generated method stub
        return null;
    }

    public PhysicalStore<N> copy() {
        // TODO Auto-generated method stub
        return null;
    }

    public long count() {
        return myUtility.count();
    }

    public long countColumns() {
        return myUtility.countColumns();
    }

    public long countRows() {
        return myUtility.countRows();
    }

    public void divideAndCopyColumn(final int row, final int column, final BasicArray<N> destination) {
        // TODO Auto-generated method stub

    }

    public double dot(final Access1D<?> vector) {
        return myUtility.dot(vector);
    }

    public double doubleValue(final long index) {
        return myUtility.doubleValue(index);
    }

    public double doubleValue(final long row, final long col) {
        return myUtility.doubleValue(row, col);
    }

    public ElementView2D<N, ?> elements() {
        return myUtility.elements();
    }

    public void exchangeColumns(final long colA, final long colB) {
        myUtility.exchangeColumns(colA, colB);
    }

    public void exchangeHermitian(final int indexA, final int indexB) {
        // TODO Auto-generated method stub

    }

    public void exchangeRows(final long rowA, final long rowB) {
        myUtility.exchangeRows(rowA, rowB);
    }

    public void fillAll(final N value) {
        myUtility.fillAll(value);
    }

    public void fillAll(final NullaryFunction<N> supplier) {
        myUtility.fillAll(supplier);
    }

    public void fillByMultiplying(final Access1D<N> left, final Access1D<N> right) {
        // TODO Auto-generated method stub

    }

    public void fillColumn(final long col, final Access1D<N> values) {
        myUtility.fillColumn(col, values);
    }

    public void fillColumn(final long row, final long col, final Access1D<N> values) {
        myUtility.fillColumn(row, col, values);
    }

    public void fillColumn(final long row, final long col, final N value) {
        myUtility.fillColumn(row, col, value);
    }

    public void fillColumn(final long row, final long col, final NullaryFunction<N> supplier) {
        myUtility.fillColumn(row, col, supplier);
    }

    public void fillColumn(final long col, final N value) {
        myUtility.fillColumn(col, value);
    }

    public void fillColumn(final long col, final NullaryFunction<N> supplier) {
        myUtility.fillColumn(col, supplier);
    }

    public void fillDiagonal(final long row, final long col, final Access1D<N> values) {
        myUtility.fillDiagonal(row, col, values);
    }

    public void fillDiagonal(final long row, final long col, final N value) {
        myUtility.fillDiagonal(row, col, value);
    }

    public void fillDiagonal(final long row, final long col, final NullaryFunction<N> supplier) {
        myUtility.fillDiagonal(row, col, supplier);
    }

    public void fillMatching(final Access1D<?> values) {
        myUtility.fillMatching(values);
    }

    public void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
        myUtility.fillOne(index, values, valueIndex);
    }

    public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        myUtility.fillOne(row, col, values, valueIndex);
    }

    public void fillOne(final long row, final long col, final N value) {
        myUtility.fillOne(row, col, value);
    }

    public void fillOne(final long row, final long col, final NullaryFunction<N> supplier) {
        myUtility.fillOne(row, col, supplier);
    }

    public void fillOne(final long index, final N value) {
        myUtility.fillOne(index, value);
    }

    public void fillOne(final long index, final NullaryFunction<N> supplier) {
        myUtility.fillOne(index, supplier);
    }

    public void fillRange(final long first, final long limit, final N value) {
        myUtility.fillRange(first, limit, value);
    }

    public void fillRange(final long first, final long limit, final NullaryFunction<N> supplier) {
        myUtility.fillRange(first, limit, supplier);
    }

    public void fillRow(final long row, final Access1D<N> values) {
        myUtility.fillRow(row, values);
    }

    public void fillRow(final long row, final long col, final Access1D<N> values) {
        myUtility.fillRow(row, col, values);
    }

    public void fillRow(final long row, final long col, final N value) {
        myUtility.fillRow(row, col, value);
    }

    public void fillRow(final long row, final long col, final NullaryFunction<N> supplier) {
        myUtility.fillRow(row, col, supplier);
    }

    public void fillRow(final long row, final N value) {
        myUtility.fillRow(row, value);
    }

    public void fillRow(final long row, final NullaryFunction<N> supplier) {
        myUtility.fillRow(row, supplier);
    }

    public void forEach(final Consumer<? super N> action) {
        myUtility.forEach(action);
    }

    public boolean generateApplyAndCopyHouseholderColumn(final int row, final int column, final Householder<N> destination) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean generateApplyAndCopyHouseholderRow(final int row, final int column, final Householder<N> destination) {
        // TODO Auto-generated method stub
        return false;
    }

    public N get(final long index) {
        return myUtility.get(index);
    }

    public N get(final long row, final long col) {
        return myUtility.get(row, col);
    }

    @Override
    public int hashCode() {
        return myUtility.hashCode();
    }

    public long indexOfLargest() {
        return myUtility.indexOfLargest();
    }

    public long indexOfLargestInColumn(final long col) {
        return myUtility.indexOfLargestInColumn(col);
    }

    public long indexOfLargestInColumn(final long row, final long col) {
        return myUtility.indexOfLargestInColumn(row, col);
    }

    public long indexOfLargestInRange(final long first, final long limit) {
        return myUtility.indexOfLargestInRange(first, limit);
    }

    public long indexOfLargestInRow(final long row) {
        return myUtility.indexOfLargestInRow(row);
    }

    public long indexOfLargestInRow(final long row, final long col) {
        return myUtility.indexOfLargestInRow(row, col);
    }

    public long indexOfLargestOnDiagonal(final long first) {
        return myUtility.indexOfLargestOnDiagonal(first);
    }

    public boolean isAbsolute(final long index) {
        return myUtility.isAbsolute(index);
    }

    public boolean isAbsolute(final long row, final long col) {
        return myUtility.isAbsolute(row, col);
    }

    public boolean isAllSmall(final double comparedTo) {
        return myUtility.isAllSmall(comparedTo);
    }

    public boolean isColumnSmall(final long col, final double comparedTo) {
        return myUtility.isColumnSmall(col, comparedTo);
    }

    public boolean isColumnSmall(final long row, final long col, final double comparedTo) {
        return myUtility.isColumnSmall(row, col, comparedTo);
    }

    public boolean isEmpty() {
        return myUtility.isEmpty();
    }

    public boolean isFat() {
        return myUtility.isFat();
    }

    public boolean isRowSmall(final long row, final double comparedTo) {
        return myUtility.isRowSmall(row, comparedTo);
    }

    public boolean isRowSmall(final long row, final long col, final double comparedTo) {
        return myUtility.isRowSmall(row, col, comparedTo);
    }

    public boolean isScalar() {
        return myUtility.isScalar();
    }

    public boolean isSmall(final long index, final double comparedTo) {
        return myUtility.isSmall(index, comparedTo);
    }

    public boolean isSmall(final long row, final long col, final double comparedTo) {
        return myUtility.isSmall(row, col, comparedTo);
    }

    public boolean isSquare() {
        return myUtility.isSquare();
    }

    public boolean isTall() {
        return myUtility.isTall();
    }

    public boolean isVector() {
        return myUtility.isVector();
    }

    public Iterator<N> iterator() {
        return myUtility.iterator();
    }

    public void modifyAll(final UnaryFunction<N> modifier) {
        myUtility.modifyAll(modifier);
    }

    public void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
        myUtility.modifyColumn(row, col, modifier);
    }

    public void modifyColumn(final long col, final UnaryFunction<N> modifier) {
        myUtility.modifyColumn(col, modifier);
    }

    public void modifyDiagonal(final long row, final long col, final UnaryFunction<N> modifier) {
        myUtility.modifyDiagonal(row, col, modifier);
    }

    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
        myUtility.modifyMatching(left, function);
    }

    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
        myUtility.modifyMatching(function, right);
    }

    public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
        myUtility.modifyOne(row, col, modifier);
    }

    public void modifyOne(final long index, final UnaryFunction<N> modifier) {
        myUtility.modifyOne(index, modifier);
    }

    public void modifyRange(final long first, final long limit, final UnaryFunction<N> modifier) {
        myUtility.modifyRange(first, limit, modifier);
    }

    public void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
        myUtility.modifyRow(row, col, modifier);
    }

    public void modifyRow(final long row, final UnaryFunction<N> modifier) {
        myUtility.modifyRow(row, modifier);
    }

    public N multiplyBoth(final Access1D<N> leftAndRight) {
        // TODO Auto-generated method stub
        return null;
    }

    public void negateColumn(final int column) {
        // TODO Auto-generated method stub

    }

    public org.ojalgo.matrix.store.PhysicalStore.Factory<N, ?> physical() {
        // TODO Auto-generated method stub
        return null;
    }

    public ElementsConsumer<N> regionByColumns(final int... columns) {
        // TODO Auto-generated method stub
        return null;
    }

    public ElementsConsumer<N> regionByLimits(final int rowLimit, final int columnLimit) {
        // TODO Auto-generated method stub
        return null;
    }

    public ElementsConsumer<N> regionByOffsets(final int rowOffset, final int columnOffset) {
        // TODO Auto-generated method stub
        return null;
    }

    public ElementsConsumer<N> regionByRows(final int... rows) {
        // TODO Auto-generated method stub
        return null;
    }

    public ElementsConsumer<N> regionByTransposing() {
        // TODO Auto-generated method stub
        return null;
    }

    public void rotateRight(final int aLow, final int aHigh, final double aCos, final double aSin) {
        // TODO Auto-generated method stub

    }

    public Iterable<RowView<N>> rows() {
        return myUtility.rows();
    }

    public void set(final long index, final double value) {
        myUtility.set(index, value);
    }

    public void set(final long row, final long col, final double value) {
        myUtility.set(row, col, value);
    }

    public void set(final long row, final long col, final Number value) {
        myUtility.set(row, col, value);
    }

    public void set(final long index, final Number value) {
        myUtility.set(index, value);
    }

    public void setToIdentity(final int aCol) {
        // TODO Auto-generated method stub

    }

    public Array1D<N> sliceColumn(final long row, final long col) {
        return myUtility.sliceColumn(row, col);
    }

    public Array1D<N> sliceDiagonal(final long row, final long col) {
        return myUtility.sliceDiagonal(row, col);
    }

    public Array1D<N> sliceRange(final long first, final long limit) {
        return myUtility.sliceRange(first, limit);
    }

    public Array1D<N> sliceRow(final long row, final long col) {
        return myUtility.sliceRow(row, col);
    }

    public Spliterator<N> spliterator() {
        return myUtility.spliterator();
    }

    public BaseStream<N, ? extends BaseStream<N, ?>> stream(final boolean parallel) {
        return myUtility.stream(parallel);
    }

    public void substituteBackwards(final Access2D<N> body, final boolean unitDiagonal, final boolean conjugated, final boolean hermitian) {
        // TODO Auto-generated method stub

    }

    public void substituteForwards(final Access2D<N> body, final boolean unitDiagonal, final boolean conjugated, final boolean identity) {
        // TODO Auto-generated method stub

    }

    public void supplyTo(final ElementsConsumer<N> receiver) {
        // TODO Auto-generated method stub

    }

    public double[] toRawCopy1D() {
        return myUtility.toRawCopy1D();
    }

    public double[][] toRawCopy2D() {
        return myUtility.toRawCopy2D();
    }

    @Override
    public String toString() {
        return myUtility.toString();
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

    public void transformSymmetric(final Householder<N> transformation) {
        // TODO Auto-generated method stub

    }

    public void tred2(final BasicArray<N> mainDiagonal, final BasicArray<N> offDiagonal, final boolean yesvecs) {
        // TODO Auto-generated method stub

    }

    public void visitAll(final VoidFunction<N> visitor) {
        myUtility.visitAll(visitor);
    }

    public void visitColumn(final long row, final long col, final VoidFunction<N> visitor) {
        myUtility.visitColumn(row, col, visitor);
    }

    public void visitColumn(final long col, final VoidFunction<N> visitor) {
        myUtility.visitColumn(col, visitor);
    }

    public void visitDiagonal(final long row, final long col, final VoidFunction<N> visitor) {
        myUtility.visitDiagonal(row, col, visitor);
    }

    public void visitOne(final long row, final long col, final VoidFunction<N> visitor) {
        myUtility.visitOne(row, col, visitor);
    }

    public void visitOne(final long index, final VoidFunction<N> visitor) {
        myUtility.visitOne(index, visitor);
    }

    public void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
        myUtility.visitRange(first, limit, visitor);
    }

    public void visitRow(final long row, final long col, final VoidFunction<N> visitor) {
        myUtility.visitRow(row, col, visitor);
    }

    public void visitRow(final long row, final VoidFunction<N> visitor) {
        myUtility.visitRow(row, visitor);
    }

}
