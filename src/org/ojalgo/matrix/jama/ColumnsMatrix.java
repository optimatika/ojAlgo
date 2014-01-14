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
package org.ojalgo.matrix.jama;

import java.util.Iterator;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Iterator1D;
import org.ojalgo.access.RowsIterator;

final class ColumnsMatrix implements Access2D<Double>, Access2D.Fillable<Double>, Access2D.Iterable2D<Double> {

    private final long myColumnLength;
    private final double[][] myColumns;

    public ColumnsMatrix(final int rowsCount, final int columnsCount) {

        super();

        myColumns = new double[columnsCount][rowsCount];
        myColumnLength = rowsCount;
    }

    @SuppressWarnings("unused")
    private ColumnsMatrix() {
        this(0, 0);
    }

    public double[] column(final int column) {
        return myColumns[column];
    }

    public Iterable<Access1D<Double>> columns() {
        // TODO Auto-generated method stub
        return null;
    }

    public Iterator<double[]> columns2() {
        return new Iterator<double[]>() {

            private int myNextCol = 0;

            public boolean hasNext() {
                return myNextCol < myColumns.length;
            }

            public double[] next() {
                return myColumns[myNextCol++];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        };

    }

    public long count() {
        return myColumns.length * myColumnLength;
    }

    public long countColumns() {
        return myColumns.length;
    }

    public long countRows() {
        return myColumnLength;
    }

    @Override
    public double doubleValue(final long index) {
        return myColumns[(int) (index / myColumnLength)][(int) (index % myColumnLength)];
    }

    @Override
    public double doubleValue(final long row, final long column) {
        return myColumns[(int) column][(int) row];
    }

    public void fillAll(final Double value) {
        // TODO Auto-generated method stub

    }

    public void fillColumn(final long row, final long column, final Double value) {
        // TODO Auto-generated method stub

    }

    public void fillDiagonal(final long row, final long column, final Double value) {
        // TODO Auto-generated method stub

    }

    public void fillRange(final long first, final long limit, final Double value) {
        // TODO Auto-generated method stub

    }

    public void fillRow(final long row, final long column, final Double value) {
        // TODO Auto-generated method stub

    }

    public Double get(final long index) {
        return myColumns[(int) (index / myColumnLength)][(int) (index % myColumnLength)];
    }

    @Override
    public Double get(final long row, final long column) {
        return myColumns[(int) column][(int) row];
    }

    public Iterator<Double> iterator() {
        return new Iterator1D<Double>(this);
    }

    public Iterable<Access1D<Double>> rows() {
        return RowsIterator.make(this);
    }

    public void set(final long index, final double value) {
        // TODO Auto-generated method stub

    }

    public void set(final long row, final long column, final double value) {
        // TODO Auto-generated method stub

    }

    public void set(final long row, final long column, final Number value) {
        // TODO Auto-generated method stub

    }

    public void set(final long index, final Number value) {
        // TODO Auto-generated method stub

    }

}
