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
package org.ojalgo.matrix.store;

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.ElementView2D;
import org.ojalgo.structure.Structure2D;

/**
 * A compressed sparse column (CSC) matrix store implementation for double precision values. This format is
 * efficient for column-wise operations and matrix-vector multiplication.
 * <p>
 * The CSC format uses three arrays to store the matrix:
 * <ul>
 * <li>values[] - stores the non-zero values</li>
 * <li>rowIndices[] - stores the row index for each non-zero value</li>
 * <li>columnPointers[] - stores the starting position in values/rowIndices arrays for each column</li>
 * </ul>
 * <p>
 * This format is particularly efficient for:
 * <ul>
 * <li>Column-wise access and operations</li>
 * <li>Matrix-vector multiplication</li>
 * <li>Iterating over non-zero elements column by column</li>
 * </ul>
 *
 * @author apete
 */
public final class R064CSC extends CompressedR064 {

    public static final class NonZeroView implements ElementView2D<Double, NonZeroView> {

        private int myColumn = 0;
        private int myCursor = 0;
        private final int myLast;
        private final R064CSC myMatrix;

        NonZeroView(final R064CSC matrix) {
            this(matrix, -1);
        }

        NonZeroView(final R064CSC matrix, final int cursor) {

            super();

            myMatrix = matrix;
            myLast = matrix.values.length - 1;

            myCursor = cursor;
            while (myCursor >= myMatrix.pointers[myColumn + 1]) {
                myColumn++;
            }
        }

        @Override
        public long column() {
            return myColumn;
        }

        @Override
        public double doubleValue() {
            return myMatrix.values[myCursor];
        }

        @Override
        public long estimateSize() {
            return myMatrix.values.length;
        }

        @Override
        public Double get() {
            return Double.valueOf(this.doubleValue());
        }

        @Override
        public boolean hasNext() {
            return myCursor < myLast;
        }

        @Override
        public boolean hasPrevious() {
            return myCursor > 0;
        }

        @Override
        public long index() {
            return Structure2D.index(myMatrix.countRows(), myMatrix.indices[myCursor], myColumn);
        }

        @Override
        public NonZeroView iterator() {
            return this;
        }

        @Override
        public NonZeroView next() {
            myCursor++;
            if (myCursor > myLast) {
                throw new NoSuchElementException();
            }
            while (myCursor >= myMatrix.pointers[myColumn + 1]) {
                myColumn++;
            }
            return this;
        }

        @Override
        public NonZeroView previous() {
            myCursor--;
            if (myCursor < 0) {
                throw new NoSuchElementException();
            }
            while (myCursor < myMatrix.pointers[myColumn]) {
                myColumn--;
            }
            return this;
        }

        @Override
        public long row() {
            return myMatrix.indices[myCursor];
        }

        @Override
        public NonZeroView trySplit() {

            int remaining = myLast - myCursor;

            if (remaining <= 1) {
                return null;
            } else {
                return new NonZeroView(myMatrix, myCursor + remaining / 2);
            }
        }
    }

    /**
     * Assumes mtrxL is unit lower triangular, with the unit diagonal not stored.
     */
    public static void ftranUnitLowerTriangular(final R064CSC mtrxL, final int n, final PhysicalStore<Double> arg) {

        for (int j = 0; j < n; j++) {
            double argJ = -arg.doubleValue(j);
            for (int k = mtrxL.pointers[j], limit = mtrxL.pointers[j + 1]; k < limit; k++) {
                arg.add(mtrxL.indices[k], mtrxL.values[k] * argJ);
            }
        }
    }

    /**
     * Creates a new CSC matrix store.
     *
     * @param rows           The number of rows in the matrix
     * @param cols           The number of columns in the matrix
     * @param elementValues  The non-zero values
     * @param rowIndices     The row index for each non-zero value
     * @param columnPointers The starting position in elementValues/rowIndices for each column
     */
    R064CSC(final int nbRows, final int nbCols, final double[] elementValues, final int[] rowIndices, final int[] columnPointers) {
        super(nbRows, nbCols, elementValues, rowIndices, columnPointers);
    }

    /**
     * Gets the value at the specified row and column. Returns 0.0 if the element is not stored (i.e., is
     * zero).
     *
     * @param row The row index
     * @param col The column index
     * @return The value at the specified position, or 0.0 if not stored
     */
    @Override
    public double doubleValue(final int row, final int col) {

        for (int i = pointers[col], limit = pointers[col + 1]; i < limit; i++) {
            if (indices[i] == row) {
                return values[i];
            }
        }
        return ZERO;
    }

    @Override
    public int firstInColumn(final int col) {
        // Return the row index of the first non-zero element in column col
        int start = pointers[col];
        int end = pointers[col + 1];
        if (start < end) {
            return indices[start];
        } else {
            return super.firstInColumn(col);
        }
    }

    @Override
    public int firstInRow(final int row) {
        // Return the column index of the first non-zero element in row row
        // We need to scan through each column to find the first occurrence
        for (int col = 0; col < this.getColDim(); col++) {
            int start = pointers[col];
            int end = pointers[col + 1];
            for (int i = start; i < end; i++) {
                if (indices[i] == row) {
                    return col;
                }
            }
        }
        return super.firstInRow(row);
    }

    @Override
    public int limitOfColumn(final int col) {
        // Return the row index of the last non-zero element in column col
        int start = pointers[col];
        int end = pointers[col + 1];
        if (start < end) {
            return indices[end - 1];
        } else {
            return super.limitOfColumn(col);
        }
    }

    @Override
    public int limitOfRow(final int row) {
        // Return the column index of the last non-zero element in row row
        // We need to scan through each column from the end to find the last occurrence
        for (int col = this.getColDim() - 1; col >= 0; col--) {
            int start = pointers[col];
            int end = pointers[col + 1];
            for (int i = end - 1; i >= start; i--) {
                if (indices[i] == row) {
                    return col;
                }
            }
        }
        return super.limitOfRow(row);
    }

    /**
     * Performs matrix-vector multiplication using the CSC format. This implementation is optimized for the
     * CSC format by iterating over non-zero elements column by column and accumulating the results.
     *
     * @param right  The vector to multiply with
     * @param target The target vector to store the result
     */
    @Override
    public void multiply(final Access1D<Double> right, final TransformableRegion<Double> target) {

        target.reset();

        int complexity = this.getColDim();
        int nbCols = right.size() / complexity;

        for (int c = 0; c < complexity; c++) {

            for (int k = pointers[c], limit = pointers[c + 1]; k < limit; k++) {
                int i = indices[k];

                double value = values[k];

                for (int j = 0; j < nbCols; j++) {
                    target.add(i, j, value * right.doubleValue(Structure2D.index(complexity, c, j)));
                }
            }
        }
    }

    @Override
    public NonZeroView nonzeros() {
        return new NonZeroView(this);
    }

    @Override
    public R064CSC toCSC() {
        return this;
    }

    @Override
    public R064CSR toCSR() {

        int nbRows = this.getRowDim();
        int nbCols = this.getColDim();
        int nnz = values.length;

        int[] rowCounts = new int[nbRows];
        for (int j = 0; j < nbCols; j++) {
            for (int k = pointers[j], limit = pointers[j + 1]; k < limit; k++) {
                rowCounts[indices[k]]++;
            }
        }

        int[] rowPointers = new int[nbRows + 1];
        for (int i = 0; i < nbRows; i++) {
            rowPointers[i + 1] = rowPointers[i] + rowCounts[i];
        }

        double[] valuesCSR = new double[nnz];
        int[] colIndicesCSR = new int[nnz];
        int[] next = Arrays.copyOf(rowPointers, nbRows);
        for (int j = 0; j < nbCols; j++) {
            for (int k = pointers[j], limit = pointers[j + 1]; k < limit; k++) {
                int row = indices[k];
                int dest = next[row]++;
                valuesCSR[dest] = values[k];
                colIndicesCSR[dest] = j;
            }
        }

        return new R064CSR(nbRows, nbCols, valuesCSR, colIndicesCSR, rowPointers);
    }

}
