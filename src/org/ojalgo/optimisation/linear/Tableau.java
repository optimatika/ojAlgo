/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
package org.ojalgo.optimisation.linear;

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Mutate1D;
import org.ojalgo.array.SparseArray;
import org.ojalgo.function.PrimitiveFunction;

final class Tableau implements Access2D<Double> {

    static final class Row implements Comparable<Row>, Access1D<Double>, Mutate1D {

        /**
         * The nonzero elements of this equation/row
         */
        private final SparseArray<Double> myElements;
        private double myRHS;

        public Row(final int row, final long numberOfColumns, final double rhs) {
            super();
            myElements = SparseArray.makePrimitive(numberOfColumns);
            myRHS = rhs;
        }

        public Row(final int row, final long numberOfColumns, final double rhs, final int numberOfNonzeros) {
            super();
            myElements = SparseArray.makePrimitive(numberOfColumns, numberOfNonzeros);
            myRHS = rhs;
        }

        public void add(final long index, final double addend) {
            myElements.add(index, addend);
        }

        public void add(final long index, final Number addend) {
            this.add(index, addend.doubleValue());
        }

        public final void addToRHS(final double addition) {
            myRHS += addition;
        }

        public int compareTo(final Row o) {
            // TODO Auto-generated method stub
            return 0;
        }

        public long count() {
            return myElements.count();
        }

        public double dot(final Access1D<?> vector) {
            return myElements.dot(vector);
        }

        public double doubleValue(final long index) {
            return myElements.doubleValue(index);
        }

        public Double get(final long index) {
            return myElements.get(index);
        }

        /**
         * @return The equation RHS
         */
        public double getRHS() {
            return myRHS;
        }

        public void set(final long index, final double value) {
            myElements.set(index, value);
        }

        public void set(final long index, final Number value) {
            this.set(index, value.doubleValue());
        }

        void pivot(final int col, final double pivotValue) {
            myElements.modifyAll(PrimitiveFunction.DIVIDE.second(pivotValue));
        }

        void pivot(final Row target, final int index, final double pivotValue) {

            final double tmpTargetValue = target.doubleValue(index);

            if (tmpTargetValue != ZERO) {

                final double tmpFactor = -tmpTargetValue / pivotValue;

                myElements.daxpy(tmpFactor, target);
                target.addToRHS(tmpFactor * myRHS);
            }
        }

    }

    private final long myNumberOfConstraints;
    private final long myNumberOfVariables;

    private final Row[] myRows;

    Tableau(final int numberOfConstraints, final int numberOfVariables) {

        super();

        myNumberOfConstraints = numberOfConstraints;
        myNumberOfVariables = numberOfVariables;

        myRows = new Row[numberOfConstraints + 2];
    }

    public long countColumns() {
        return myNumberOfVariables + 1L;
    }

    public long countRows() {
        return myNumberOfConstraints + 2L;
    }

    public double doubleValue(final long row, final long col) {
        if (myNumberOfVariables != col) {
            return myRows[(int) row].doubleValue(col);
        } else {
            return myRows[(int) row].getRHS();
        }
    }

    public Double get(final long row, final long column) {
        return this.doubleValue(row, column);
    }

    void pivot(final int row, final int col) {

        final Row tmpPivotRow = myRows[row];
        final double tmpPivotValue = tmpPivotRow.doubleValue(col);

        for (int i = 0; i < row; i++) {
            tmpPivotRow.pivot(myRows[i], col, tmpPivotValue);
        }
        for (int i = row + 1; i < myRows.length; i++) {
            tmpPivotRow.pivot(myRows[i], col, tmpPivotValue);
        }

        tmpPivotRow.pivot(col, tmpPivotValue);
    }

}
