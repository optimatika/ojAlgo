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
import org.ojalgo.array.BasicArray.BasicFactory;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.random.RandomNumber;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;

/**
 * Array2D
 *
 * @author apete
 */
public final class Array2D<N extends Number> implements Access2D<N>, Access2D.Elements, Access2D.Fillable<N>, Access2D.Iterable2D<N>, Access2D.Modifiable<N>,
Access2D.Visitable<N>, Serializable {

    public static abstract class Factory<N extends Number> implements Access2D.Factory<Array2D<N>> {

        public final Array2D<N> columns(final Access1D<?>... source) {

            final int tmpColumns = source.length;
            final long tmpRows = source[0].count();

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(tmpRows, tmpColumns);

            long tmpIndex = 0L;
            for (int j = 0; j < tmpColumns; j++) {
                final Access1D<?> tmpColumn = source[j];
                for (long i = 0L; i < tmpRows; i++) {
                    tmpDelegate.set(tmpIndex++, tmpColumn.get(i));
                }
            }

            return tmpDelegate.asArray2D(tmpRows);
        }

        public final Array2D<N> columns(final double[]... source) {

            final int tmpColumns = source.length;
            final int tmpRows = source[0].length;

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(tmpRows, tmpColumns);

            long tmpIndex = 0L;
            for (int j = 0; j < tmpColumns; j++) {
                final double[] tmpColumn = source[j];
                for (int i = 0; i < tmpRows; i++) {
                    tmpDelegate.set(tmpIndex++, tmpColumn[i]);
                }
            }

            return tmpDelegate.asArray2D(tmpRows);
        }

        @SafeVarargs
        public final Array2D<N> columns(final List<? extends Number>... source) {

            final int tmpColumns = source.length;
            final int tmpRows = source[0].size();

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(tmpRows, tmpColumns);

            long tmpIndex = 0L;
            for (int j = 0; j < tmpColumns; j++) {
                final List<? extends Number> tmpColumn = source[j];
                for (int i = 0; i < tmpRows; i++) {
                    tmpDelegate.set(tmpIndex++, tmpColumn.get(i));
                }
            }

            return tmpDelegate.asArray2D(tmpRows);
        }

        public final Array2D<N> columns(final Number[]... source) {

            final int tmpColumns = source.length;
            final int tmpRows = source[0].length;

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(tmpRows, tmpColumns);

            long tmpIndex = 0L;
            for (int j = 0; j < tmpColumns; j++) {
                final Number[] tmpColumn = source[j];
                for (int i = 0; i < tmpRows; i++) {
                    tmpDelegate.set(tmpIndex++, tmpColumn[i]);
                }
            }

            return tmpDelegate.asArray2D(tmpRows);
        }

        public final Array2D<N> copy(final Access2D<?> source) {

            final long tmpColumns = source.countColumns();
            final long tmpRows = source.countRows();

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(tmpRows, tmpColumns);

            long tmpIndex = 0L;
            for (long j = 0L; j < tmpColumns; j++) {
                for (long i = 0L; i < tmpRows; i++) {
                    tmpDelegate.set(tmpIndex++, source.get(i, j));
                }
            }

            return tmpDelegate.asArray2D(tmpRows);
        }

        public final Array2D<N> makeEye(final long rows, final long columns) {

            final BasicArray<N> tmpDelegate = this.delegate().makeStructuredZero(rows, columns);

            final long tmpLimit = Math.min(rows, columns);

            final long tmpIncr = rows + 1L;
            for (long ij = 0L; ij < tmpLimit; ij++) {
                tmpDelegate.set(ij * tmpIncr, 1.0);
            }

            return tmpDelegate.asArray2D(rows);
        }

        public final Array2D<N> makeRandom(final long rows, final long columns, final RandomNumber distribution) {

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(rows, columns);

            long tmpIndex = 0L;
            for (long j = 0L; j < columns; j++) {
                for (long i = 0L; i < rows; i++) {
                    tmpDelegate.set(tmpIndex++, distribution);
                }
            }

            return tmpDelegate.asArray2D(rows);
        }

        public final Array2D<N> makeZero(final long rows, final long columns) {
            return this.delegate().makeStructuredZero(rows, columns).asArray2D(rows);
        }

        public final Array2D<N> rows(final Access1D<?>... source) {

            final int tmpRows = source.length;
            final long tmpColumns = source[0].count();

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(tmpRows, tmpColumns);

            for (int i = 0; i < tmpRows; i++) {
                final Access1D<?> tmpRow = source[i];
                for (long j = 0L; j < tmpColumns; j++) {
                    tmpDelegate.set(i + (j * tmpRows), tmpRow.get(j));
                }
            }

            return tmpDelegate.asArray2D(tmpRows);
        }

        public final Array2D<N> rows(final double[]... source) {

            final int tmpRows = source.length;
            final int tmpColumns = source[0].length;

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(tmpRows, tmpColumns);

            for (int i = 0; i < tmpRows; i++) {
                final double[] tmpRow = source[i];
                for (int j = 0; j < tmpColumns; j++) {
                    tmpDelegate.set(i + (j * tmpRows), tmpRow[j]);
                }
            }

            return tmpDelegate.asArray2D(tmpRows);
        }

        @SuppressWarnings("unchecked")
        public final Array2D<N> rows(final List<? extends Number>... source) {

            final int tmpRows = source.length;
            final int tmpColumns = source[0].size();

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(tmpRows, tmpColumns);

            for (int i = 0; i < tmpRows; i++) {
                final List<? extends Number> tmpRow = source[i];
                for (int j = 0; j < tmpColumns; j++) {
                    tmpDelegate.set(i + (j * tmpRows), tmpRow.get(j));
                }
            }

            return tmpDelegate.asArray2D(tmpRows);
        }

        public final Array2D<N> rows(final Number[]... source) {

            final int tmpRows = source.length;
            final int tmpColumns = source[0].length;

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(tmpRows, tmpColumns);

            for (int i = 0; i < tmpRows; i++) {
                final Number[] tmpRow = source[i];
                for (int j = 0; j < tmpColumns; j++) {
                    tmpDelegate.set(i + (j * tmpRows), tmpRow[j]);
                }
            }

            return tmpDelegate.asArray2D(tmpRows);
        }

        abstract BasicArray.BasicFactory<N> delegate();

    }

    public static final Factory<BigDecimal> BIG = new Factory<BigDecimal>() {

        @Override
        BasicFactory<BigDecimal> delegate() {
            return BasicArray.BIG;
        }

    };

    public static final Factory<ComplexNumber> COMPLEX = new Factory<ComplexNumber>() {

        @Override
        BasicFactory<ComplexNumber> delegate() {
            return BasicArray.COMPLEX;
        }

    };

    public static final Factory<Double> PRIMITIVE = new Factory<Double>() {

        @Override
        BasicFactory<Double> delegate() {
            return BasicArray.PRIMITIVE;
        }

    };

    public static final Factory<RationalNumber> RATIONAL = new Factory<RationalNumber>() {

        @Override
        BasicFactory<RationalNumber> delegate() {
            return BasicArray.RATIONAL;
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

    @Override
    public int hashCode() {
        return (int) (myRowsCount * myColumnsCount * myDelegate.hashCode());
    }

    public long indexOfLargestInColumn(final long row, final long column) {
        return myDelegate.indexOfLargest(row + (column * myRowsCount), myRowsCount + (column * myRowsCount), 1L) % myRowsCount;
    }

    public long indexOfLargestInRow(final long row, final long column) {
        return myDelegate.indexOfLargest(row + (column * myRowsCount), row + (myColumnsCount * myRowsCount), myRowsCount) / myRowsCount;
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

    public boolean isPositive(final long index) {
        return myDelegate.isPositive(index);
    }

    public boolean isPositive(final long row, final long column) {
        return myDelegate.isPositive(row + (column * myRowsCount));
    }

    public boolean isRowZeros(final long row, final long column) {
        return myDelegate.isZeros(row + (column * myRowsCount), row + (myColumnsCount * myRowsCount), myRowsCount);
    }

    public boolean isSmall(final long index, final double comparedTo) {
        return myDelegate.isSmall(index, comparedTo);
    }

    public boolean isSmall(final long row, final long column, final double comparedTo) {
        return myDelegate.isSmall(row + (column * myRowsCount), comparedTo);
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
