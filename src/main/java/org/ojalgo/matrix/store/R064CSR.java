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
 * A compressed sparse row (CSR) matrix store implementation for double precision values. This format is
 * efficient for row-wise operations and matrix-vector multiplication.
 * <p>
 * The CSR format uses three arrays to store the matrix:
 * <ul>
 * <li>values[] - stores the non-zero values</li>
 * <li>columnIndices[] - stores the column index for each non-zero value</li>
 * <li>rowPointers[] - stores the starting position in values/columnIndices arrays for each row</li>
 * </ul>
 * <p>
 * This format is particularly efficient for:
 * <ul>
 * <li>Row-wise access and operations</li>
 * <li>Matrix-vector multiplication</li>
 * <li>Iterating over non-zero elements row by row</li>
 * </ul>
 *
 * @author apete
 */
public final class R064CSR extends CompressedSparseR064 {

    public static final class NonZeroView implements ElementView2D<Double, NonZeroView> {

        private int myCursor = 0;
        private final int myLast;
        private final R064CSR myMatrix;
        private int myRow = 0;

        NonZeroView(final R064CSR matrix) {
            this(matrix, -1);
        }

        NonZeroView(final R064CSR matrix, final int cursor) {

            super();

            myMatrix = matrix;
            myLast = matrix.values.length - 1;

            myCursor = cursor;
            while (myCursor >= myMatrix.pointers[myRow + 1]) {
                myRow++;
            }
        }

        @Override
        public long column() {
            return myMatrix.indices[myCursor];
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
            return Structure2D.index(myMatrix.countRows(), myRow, myMatrix.indices[myCursor]);
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
            while (myCursor >= myMatrix.pointers[myRow + 1]) {
                myRow++;
            }
            return this;
        }

        @Override
        public NonZeroView previous() {
            myCursor--;
            if (myCursor < 0) {
                throw new NoSuchElementException();
            }
            while (myCursor < myMatrix.pointers[myRow]) {
                myRow--;
            }
            return this;
        }

        @Override
        public long row() {
            return myRow;
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
     * Creates a new CSR matrix store.
     *
     * @param nbRows        The number of rows in the matrix
     * @param nbCols        The number of columns in the matrix
     * @param elementValues Array containing the non-zero values
     * @param columnIndices Array containing the column index for each non-zero value
     * @param rowPointers   Array containing the starting position in values/columnIndices for each row
     */
    R064CSR(final int nbRows, final int nbCols, final double[] elementValues, final int[] columnIndices, final int[] rowPointers) {
        super(nbRows, nbCols, elementValues, columnIndices, rowPointers);
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

        for (int i = pointers[row], limit = pointers[row + 1]; i < limit; i++) {
            if (indices[i] == col) {
                return values[i];
            }
        }
        return ZERO;
    }

    @Override
    public int firstInColumn(final int col) {
        // Return the row index of the first non-zero element in column col
        // We need to scan through each row to find the first occurrence
        for (int row = 0; row < this.getRowDim(); row++) {
            int start = pointers[row];
            int end = pointers[row + 1];
            for (int i = start; i < end; i++) {
                if (indices[i] == col) {
                    return row;
                }
            }
        }
        return super.firstInColumn(col);
    }

    @Override
    public int firstInRow(final int row) {
        // Return the column index of the first non-zero element in row row
        int start = pointers[row];
        int end = pointers[row + 1];
        if (start < end) {
            return indices[start];
        } else {
            return super.firstInRow(row);
        }
    }

    @Override
    public int limitOfColumn(final int col) {
        // Return the row index of the last non-zero element in column col
        // We need to scan through each row from the end to find the last occurrence
        for (int row = this.getRowDim() - 1; row >= 0; row--) {
            int start = pointers[row];
            int end = pointers[row + 1];
            for (int i = end - 1; i >= start; i--) {
                if (indices[i] == col) {
                    return row;
                }
            }
        }
        return super.limitOfColumn(col);
    }

    @Override
    public int limitOfRow(final int row) {
        // Return the column index of the last non-zero element in row row
        int start = pointers[row];
        int end = pointers[row + 1];
        if (start < end) {
            return indices[end - 1];
        } else {
            return super.limitOfRow(row);
        }
    }

    /**
     * Performs matrix-vector multiplication using the CSR format. This implementation is optimized for the
     * CSR format by iterating over non-zero elements row by row and accumulating the results.
     *
     * @param right  The vector to multiply with
     * @param target The target vector to store the result
     */
    @Override
    public void multiply(final Access1D<Double> right, final TransformableRegion<Double> target) {

        target.reset();

        int nbRows = this.getRowDim();
        int complexity = this.getColDim();
        int nbCols = right.size() / complexity;

        for (int i = 0; i < nbRows; i++) {

            for (int k = pointers[i], limit = pointers[i + 1]; k < limit; k++) {
                int c = indices[k];

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

        int nbRows = this.getRowDim();
        int nbCols = this.getColDim();
        int nnz = values.length;

        int[] colCounts = new int[nbCols];
        for (int k = 0; k < nnz; k++) {
            colCounts[indices[k]]++;
        }

        int[] colPointers = new int[nbCols + 1];
        for (int j = 0; j < nbCols; j++) {
            colPointers[j + 1] = colPointers[j] + colCounts[j];
        }

        double[] valuesCSC = new double[nnz];
        int[] rowIndicesCSC = new int[nnz];
        int[] next = Arrays.copyOf(colPointers, nbCols);
        for (int i = 0; i < nbRows; i++) {
            for (int k = pointers[i], limit = pointers[i + 1]; k < limit; k++) {
                int col = indices[k];
                int dest = next[col]++;
                valuesCSC[dest] = values[k];
                rowIndicesCSC[dest] = i;
            }
        }

        return new R064CSC(nbRows, nbCols, valuesCSC, rowIndicesCSC, colPointers);
    }

    @Override
    public R064CSR toCSR() {
        return this;
    }

}
