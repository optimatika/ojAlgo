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
package org.ojalgo.array;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.ColumnsIterator;
import org.ojalgo.access.Iterator1D;
import org.ojalgo.access.RowsIterator;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.random.RandomNumber;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;

/**
 * Array2D
 * 
 * @author apete
 */
public final class Array2D<N extends Number> implements Access2D<N>, Access2D.Elements, Access2D.Fillable<N>, Access2D.Iterable2D<N>, Access2D.Modifiable<N>,
        Access2D.Visitable<N>, Serializable {

    public static final Access2D.Factory<Array2D<BigDecimal>> BIG = new Access2D.Factory<Array2D<BigDecimal>>() {

        public Array2D<BigDecimal> columns(final Access1D<?>... source) {
            return BigDenseStore.FACTORY.columns(source).asArray2D();
        }

        public Array2D<BigDecimal> columns(final double[]... source) {
            return BigDenseStore.FACTORY.columns(source).asArray2D();
        }

        public Array2D<BigDecimal> columns(final List<? extends Number>... source) {
            return BigDenseStore.FACTORY.columns(source).asArray2D();
        }

        public Array2D<BigDecimal> columns(final Number[]... source) {
            return BigDenseStore.FACTORY.columns(source).asArray2D();
        }

        public Array2D<BigDecimal> copy(final Access2D<?> source) {
            return BigDenseStore.FACTORY.copy(source).asArray2D();
        }

        public Array2D<BigDecimal> makeEye(final long rows, final long columns) {
            return BigDenseStore.FACTORY.makeEye(rows, columns).asArray2D();
        }

        public Array2D<BigDecimal> makeRandom(final long rows, final long columns, final RandomNumber distribution) {
            return BigDenseStore.FACTORY.makeRandom(rows, columns, distribution).asArray2D();
        }

        public Array2D<BigDecimal> makeZero(final long rows, final long columns) {
            return BigDenseStore.FACTORY.makeZero(rows, columns).asArray2D();
        }

        public Array2D<BigDecimal> rows(final Access1D<?>... source) {
            return BigDenseStore.FACTORY.rows(source).asArray2D();
        }

        public Array2D<BigDecimal> rows(final double[]... source) {
            return BigDenseStore.FACTORY.rows(source).asArray2D();
        }

        public Array2D<BigDecimal> rows(final List<? extends Number>... source) {
            return BigDenseStore.FACTORY.rows(source).asArray2D();
        }

        public Array2D<BigDecimal> rows(final Number[]... source) {
            return BigDenseStore.FACTORY.rows(source).asArray2D();
        }

    };

    public static final Factory<Array2D<ComplexNumber>> COMPLEX = new Factory<Array2D<ComplexNumber>>() {

        public Array2D<ComplexNumber> columns(final Access1D<?>... source) {
            return ComplexDenseStore.FACTORY.columns(source).asArray2D();
        }

        public Array2D<ComplexNumber> columns(final double[]... source) {
            return ComplexDenseStore.FACTORY.columns(source).asArray2D();
        }

        public Array2D<ComplexNumber> columns(final List<? extends Number>... source) {
            return ComplexDenseStore.FACTORY.columns(source).asArray2D();
        }

        public Array2D<ComplexNumber> columns(final Number[]... source) {
            return ComplexDenseStore.FACTORY.columns(source).asArray2D();
        }

        public Array2D<ComplexNumber> copy(final Access2D<?> source) {
            return ComplexDenseStore.FACTORY.copy(source).asArray2D();
        }

        public Array2D<ComplexNumber> makeEye(final long rows, final long columns) {
            return ComplexDenseStore.FACTORY.makeEye(rows, columns).asArray2D();
        }

        public Array2D<ComplexNumber> makeRandom(final long rows, final long columns, final RandomNumber distribution) {
            return ComplexDenseStore.FACTORY.makeRandom(rows, columns, distribution).asArray2D();
        }

        public Array2D<ComplexNumber> makeZero(final long rows, final long columns) {
            return ComplexDenseStore.FACTORY.makeZero(rows, columns).asArray2D();
        }

        public Array2D<ComplexNumber> rows(final Access1D<?>... source) {
            return ComplexDenseStore.FACTORY.rows(source).asArray2D();
        }

        public Array2D<ComplexNumber> rows(final double[]... source) {
            return ComplexDenseStore.FACTORY.rows(source).asArray2D();
        }

        public Array2D<ComplexNumber> rows(final List<? extends Number>... source) {
            return ComplexDenseStore.FACTORY.rows(source).asArray2D();
        }

        public Array2D<ComplexNumber> rows(final Number[]... source) {
            return ComplexDenseStore.FACTORY.rows(source).asArray2D();
        }

    };

    public static final Access2D.Factory<Array2D<Double>> PRIMITIVE = new Access2D.Factory<Array2D<Double>>() {

        public Array2D<Double> columns(final Access1D<?>... source) {
            return PrimitiveDenseStore.FACTORY.columns(source).asArray2D();
        }

        public Array2D<Double> columns(final double[]... source) {
            return PrimitiveDenseStore.FACTORY.columns(source).asArray2D();
        }

        @SuppressWarnings("unchecked")
        public Array2D<Double> columns(final List<? extends Number>... source) {
            return PrimitiveDenseStore.FACTORY.columns(source).asArray2D();
        }

        public Array2D<Double> columns(final Number[]... source) {
            return PrimitiveDenseStore.FACTORY.columns(source).asArray2D();
        }

        public Array2D<Double> copy(final Access2D<?> source) {
            return PrimitiveDenseStore.FACTORY.copy(source).asArray2D();
        }

        public Array2D<Double> makeEye(final long rows, final long columns) {
            return PrimitiveDenseStore.FACTORY.makeEye(rows, columns).asArray2D();
        }

        public Array2D<Double> makeRandom(final long rows, final long columns, final RandomNumber distribution) {
            return PrimitiveDenseStore.FACTORY.makeRandom(rows, columns, distribution).asArray2D();
        }

        public Array2D<Double> makeZero(final long rows, final long columns) {
            return PrimitiveDenseStore.FACTORY.makeZero(rows, columns).asArray2D();
        }

        public Array2D<Double> rows(final Access1D<?>... source) {
            return PrimitiveDenseStore.FACTORY.rows(source).asArray2D();
        }

        public Array2D<Double> rows(final double[]... source) {
            return PrimitiveDenseStore.FACTORY.rows(source).asArray2D();
        }

        public Array2D<Double> rows(final List<? extends Number>... source) {
            return PrimitiveDenseStore.FACTORY.rows(source).asArray2D();
        }

        public Array2D<Double> rows(final Number[]... source) {
            return PrimitiveDenseStore.FACTORY.rows(source).asArray2D();
        }

    };

    private final long myColumnsCount;

    private final BasicArray<N> myDelegate;
    private final long myRowsCount;

    @SuppressWarnings("unused")
    private Array2D() {
        this(null, 0L);
    }

    Array2D(final BasicArray<N> delegate, final long structure) {

        super();

        myDelegate = delegate;

        myRowsCount = structure;
        myColumnsCount = structure == 0L ? 0L : delegate.count() / structure;
    }

    /**
     * Flattens this two dimensional array to a one dimensional array. The (internal/actual) array is not copied, it is
     * just accessed through a different adaptor.
     */
    public Array1D<N> asArray1D() {
        return myDelegate.asArray1D();
    }

    public Iterable<Access1D<N>> columns() {
        return ColumnsIterator.make(this);
    }

    public long count() {
        return myDelegate.count();
    }

    public long countColumns() {
        return myColumnsCount;
    }

    public long countRows() {
        return myRowsCount;
    }

    public double doubleValue(final long index) {
        return myDelegate.doubleValue(index);
    }

    public double doubleValue(final long row, final long column) {
        return myDelegate.doubleValue(row + (column * myRowsCount));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Array2D) {
            final Array2D<N> tmpObj = (Array2D<N>) obj;
            return (myRowsCount == tmpObj.countRows()) && (myColumnsCount == tmpObj.countColumns()) && myDelegate.equals(tmpObj.getDelegate());
        } else {
            return super.equals(obj);
        }
    }

    public void exchangeColumns(final long aColA, final long aColB) {
        myDelegate.exchange(aColA * myRowsCount, aColB * myRowsCount, 1L, myRowsCount);
    }

    public void exchangeRows(final long aRowA, final long aRowB) {
        myDelegate.exchange(aRowA, aRowB, myRowsCount, myColumnsCount);
    }

    public void fillAll(final N value) {
        myDelegate.fill(0L, this.count(), 1L, value);
    }

    public void fillColumn(final long row, final long column, final N value) {
        final long tmpFirst = (row + (column * myRowsCount));
        final long tmpLimit = (myRowsCount + (column * myRowsCount));
        myDelegate.fill(tmpFirst, tmpLimit, 1L, value);
    }

    public void fillDiagonal(final long row, final long column, final N value) {

        final long tmpCount = Math.min(myRowsCount - row, myColumnsCount - column);

        final long tmpFirst = row + (column * myRowsCount);
        final long tmpLimit = row + tmpCount + ((column + tmpCount) * myRowsCount);
        final long tmpStep = 1L + myRowsCount;

        myDelegate.fill(tmpFirst, tmpLimit, tmpStep, value);
    }

    public void fillMatching(final Array2D<N> aLeftArg, final BinaryFunction<N> function, final Array2D<N> aRightArg) {
        myDelegate.fill(0L, this.count(), aLeftArg.getDelegate(), function, aRightArg.getDelegate());
    }

    public void fillRange(final long first, final long limit, final N value) {
        myDelegate.fill((int) first, (int) limit, 1, value);
    }

    public void fillRow(final long row, final long column, final N value) {
        final int tmpFirst = (int) (row + (column * myRowsCount));
        final int tmpLimit = (int) (row + (myColumnsCount * myRowsCount));
        myDelegate.fill(tmpFirst, tmpLimit, myRowsCount, value);
    }

    public N get(final long index) {
        return myDelegate.get(index);
    }

    public N get(final long row, final long column) {
        return myDelegate.get(row + (column * myRowsCount));
    }

    public long getIndexOfLargestInColumn(final long row, final long column) {
        return myDelegate.getIndexOfLargest(row + (column * myRowsCount), myRowsCount + (column * myRowsCount), 1L) % myRowsCount;
    }

    public long getIndexOfLargestInRow(final long row, final long column) {
        return myDelegate.getIndexOfLargest(row + (column * myRowsCount), row + (myColumnsCount * myRowsCount), myRowsCount) / myRowsCount;
    }

    @Override
    public int hashCode() {
        return (int) (myRowsCount * myColumnsCount * myDelegate.hashCode());
    }

    public boolean isAbsolute(final long index) {
        return myDelegate.isAbsolute(index);
    }

    /**
     * @see Scalar#isAbsolute()
     */
    public boolean isAbsolute(final long row, final long column) {
        return myDelegate.isAbsolute(row + (column * myRowsCount));
    }

    public boolean isAllZeros() {
        return myDelegate.isZeros(0L, this.count(), 1L);
    }

    public boolean isColumnZeros(final long row, final long column) {
        return myDelegate.isZeros(row + (column * myRowsCount), myRowsCount + (column * myRowsCount), 1L);
    }

    public boolean isDiagonalZeros(final long row, final long column) {

        final long tmpCount = Math.min(myRowsCount - row, myColumnsCount - column);

        return myDelegate.isZeros(row + (column * myRowsCount), row + tmpCount + ((column + tmpCount) * myRowsCount), 1L + myRowsCount);
    }

    public boolean isInfinite(final long index) {
        return myDelegate.isInfinite(index);
    }

    public boolean isInfinite(final long row, final long column) {
        return myDelegate.isInfinite(row + (column * myRowsCount));
    }

    public boolean isNaN(final long index) {
        return myDelegate.isNaN(index);
    }

    public boolean isNaN(final long row, final long column) {
        return myDelegate.isNaN(row + (column * myRowsCount));
    }

    public boolean isPositive(final long index) {
        return myDelegate.isPositive(index);
    }

    public boolean isPositive(final long row, final long column) {
        return myDelegate.isPositive(row + (column * myRowsCount));
    }

    public boolean isReal(final long index) {
        return myDelegate.isReal(index);
    }

    public boolean isReal(final long row, final long column) {
        return myDelegate.isReal(row + (column * myRowsCount));
    }

    public boolean isRowZeros(final long row, final long column) {
        return myDelegate.isZeros(row + (column * myRowsCount), row + (myColumnsCount * myRowsCount), myRowsCount);
    }

    public boolean isZero(final long index) {
        return myDelegate.isZero(index);
    }

    public boolean isZero(final long row, final long column) {
        return myDelegate.isZero(row + (column * myRowsCount));
    }

    public Iterator<N> iterator() {
        return new Iterator1D<N>(this);
    }

    public void modifyAll(final UnaryFunction<N> function) {
        myDelegate.modify(0L, this.count(), 1L, function);
    }

    public void modifyColumn(final long row, final long column, final UnaryFunction<N> function) {
        myDelegate.modify(row + (column * myRowsCount), myRowsCount + (column * myRowsCount), 1L, function);
    }

    public void modifyDiagonal(final long row, final long column, final UnaryFunction<N> function) {

        final long tmpCount = Math.min(myRowsCount - row, myColumnsCount - column);

        final long tmpFirst = row + (column * myRowsCount);
        final long tmpLimit = row + tmpCount + ((column + tmpCount) * myRowsCount);
        final long tmpStep = 1L + myRowsCount;

        myDelegate.modify(tmpFirst, tmpLimit, tmpStep, function);
    }

    public void modifyMatching(final Array2D<N> aLeftArg, final BinaryFunction<N> function) {
        myDelegate.modify(0L, this.count(), 1L, aLeftArg.getDelegate(), function);
    }

    public void modifyMatching(final BinaryFunction<N> function, final Array2D<N> aRightArg) {
        myDelegate.modify(0L, this.count(), 1L, function, aRightArg.getDelegate());
    }

    public void modifyRange(final long first, final long limit, final UnaryFunction<N> function) {
        myDelegate.modify(first, limit, 1L, function);
    }

    public void modifyRow(final long row, final long column, final UnaryFunction<N> function) {
        myDelegate.modify(row + (column * myRowsCount), row + (myColumnsCount * myRowsCount), myRowsCount, function);
    }

    public Iterable<Access1D<N>> rows() {
        return RowsIterator.make(this);
    }

    public void set(final long index, final double value) {
        myDelegate.set(index, value);
    }

    public void set(final long row, final long column, final double value) {
        myDelegate.set(row + (column * myRowsCount), value);
    }

    public void set(final long row, final long column, final Number value) {
        myDelegate.set(row + (column * myRowsCount), value);
    }

    public void set(final long index, final Number value) {
        myDelegate.set(index, value);
    }

    public Array1D<N> sliceColumn(final long row, final long column) {
        return new Array1D<N>(myDelegate, row + (column * myRowsCount), myRowsCount + (column * myRowsCount), 1L);
    }

    public Array1D<N> sliceDiagonal(final long row, final long column) {
        final long tmpCount = Math.min(myRowsCount - row, myColumnsCount - column);
        return new Array1D<N>(myDelegate, row + (column * myRowsCount), row + tmpCount + ((column + tmpCount) * myRowsCount), 1L + myRowsCount);
    }

    public Array1D<N> sliceRow(final long row, final long column) {
        return new Array1D<N>(myDelegate, row + (column * myRowsCount), row + (myColumnsCount * myRowsCount), myRowsCount);
    }

    /**
     * @return An array of arrays of doubles
     */
    public double[][] toRawCopy() {
        return ArrayUtils.toRawCopyOf(this);
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return myDelegate.toScalar(row + (column * myRowsCount));
    }

    @Override
    public String toString() {
        return myDelegate.toString();
    }

    public void visitAll(final VoidFunction<N> visitor) {
        myDelegate.visit(0L, this.count(), 1L, visitor);
    }

    public void visitColumn(final long row, final long column, final VoidFunction<N> visitor) {
        final long tmpFirst = row + (column * myRowsCount);
        final long tmpLimit = myRowsCount + (column * myRowsCount);
        myDelegate.visit(tmpFirst, tmpLimit, 1L, visitor);
    }

    public void visitDiagonal(final long row, final long column, final VoidFunction<N> visitor) {

        final long tmpCount = Math.min(myRowsCount - row, myColumnsCount - column);

        final long tmpFirst = row + (column * myRowsCount);
        final long tmpLimit = row + tmpCount + ((column + tmpCount) * myRowsCount);
        myDelegate.visit(tmpFirst, tmpLimit, 1L + myRowsCount, visitor);
    }

    public void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
        myDelegate.visit(first, limit, 1L, visitor);
    }

    public void visitRow(final long row, final long column, final VoidFunction<N> visitor) {
        final long tmpFirst = row + (column * myRowsCount);
        final long tmpLimit = row + (myColumnsCount * myRowsCount);
        myDelegate.visit(tmpFirst, tmpLimit, myRowsCount, visitor);
    }

    BasicArray<N> getDelegate() {
        return myDelegate;
    }

}
