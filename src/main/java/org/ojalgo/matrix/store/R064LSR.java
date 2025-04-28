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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ojalgo.function.FunctionSet;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.type.math.MathType;

/**
 * R064-Linked-Sparse-Rows (like CSR but rows as linked lists).
 */
public final class R064LSR extends LinkedR064 {

    public static final class Factory implements Factory2D<R064LSR> {

        @Override
        public FunctionSet<Double> function() {
            return R064Store.FACTORY.function();
        }

        @Override
        public MathType getMathType() {
            return R064Store.FACTORY.getMathType();
        }

        @Override
        public R064LSR make(final int nbRows, final int nbCols) {
            return new R064LSR(nbRows, nbCols);
        }

        @Override
        public Scalar.Factory<?> scalar() {
            return R064Store.FACTORY.scalar();
        }
    }

    public static final R064LSR.Factory FACTORY = new R064LSR.Factory();

    private final LinkedR064.ElementNode[] myFirstInRows;
    private final LinkedR064.ElementNode[] myLastInRows;
    private final int mySplit;

    R064LSR(final int nbRows, final int nbCols) {
        super(nbRows, nbCols);
        myFirstInRows = new LinkedR064.ElementNode[nbRows];
        myLastInRows = new LinkedR064.ElementNode[nbRows];
        mySplit = nbRows / 2;
    }

    @Override
    public double density() {
        int nonZeroCount = 0;
        for (int row = 0; row < myFirstInRows.length; row++) {
            ElementNode current = myFirstInRows[row];
            while (current != null) {
                nonZeroCount++;
                current = current.next;
            }
        }
        return ((double) nonZeroCount) / this.count();
    }

    /**
     * Performs the row/column cyclic shifts required by the Forrest-Tomlin update algorithm as implemented in
     * ojAlgo's own sparse LU decomposition. Although public, this method is not intended for any other use
     * case.
     *
     * @param from
     * @param row
     * @param to
     * @param column
     */
    public void doCyclicFT(final int from, final Mutate1D row, final int to, final Access1D<?> column) {

        if (from >= to) {
            throw new IllegalArgumentException();
        }

        ElementNode next = null;
        ElementNode current = this.getFirstInRow(from);
        while (current != null) {
            row.set(current.index - 1, current.value);
            next = current.next;
            this.remove(from, current);
            current = next;
        }

        for (int i = from; i < to; i++) {
            myFirstInRows[i] = myFirstInRows[i + 1];
            myLastInRows[i] = myLastInRows[i + 1];
        }
        myFirstInRows[to] = null;
        myLastInRows[to] = null;

        for (int i = 0; i < myFirstInRows.length; i++) {

            current = myFirstInRows[i];
            while (current != null && current.index <= to) {

                if (current.index == from) {
                    next = current.next;
                    this.remove(i, current);
                    current = next;
                } else if (from < current.index) {
                    --current.index;
                    current = current.next;
                } else {
                    current = current.next;
                }
            }
        }

        double tmpVal;
        for (int i = 0; i < from; i++) {
            tmpVal = column.doubleValue(i);
            if (tmpVal != ZERO) {
                this.set(i, to, tmpVal);
            }
        }
        for (int i = from; i < to; i++) {
            tmpVal = column.doubleValue(i + 1);
            if (tmpVal != ZERO) {
                this.set(i, to, tmpVal);
            }
        }
    }

    @Override
    public double doubleValue(final int row, final int col) {

        ElementNode current = null;

        if (row <= mySplit) {
            current = myFirstInRows[row];
            while (current != null && current.index < col) {
                current = current.next;
            }
        } else {
            current = myLastInRows[row];
            while (current != null && current.index > col) {
                current = current.previous;
            }
        }

        return (current != null && current.index == col) ? current.value : ZERO;
    }

    @Override
    public void exchangeColumns(final long colA, final long colB) {

        int intA = Math.toIntExact(colA);
        int intB = Math.toIntExact(colB);

        for (int row = 0; row < myFirstInRows.length; row++) {

            if (myFirstInRows[row] != null) {

                ElementNode nodeA = this.getNode(row, intA);
                ElementNode nodeB = this.getNode(row, intB);

                double tmpVal = nodeA.value;
                nodeA.value = nodeB.value;
                nodeB.value = tmpVal;

                this.removeIfZero(row, intA, nodeA);
                this.removeIfZero(row, intB, nodeB);
            }
        }
    }

    @Override
    public void exchangeRows(final long rowA, final long rowB) {

        int intA = Math.toIntExact(rowA);
        int intB = Math.toIntExact(rowB);

        ElementNode tmpFirst = myFirstInRows[intA];
        ElementNode tmpLast = myLastInRows[intA];

        myFirstInRows[intA] = myFirstInRows[intB];
        myLastInRows[intA] = myLastInRows[intB];

        myFirstInRows[intB] = tmpFirst;
        myLastInRows[intB] = tmpLast;
    }

    @Override
    public int firstInRow(final int row) {
        ElementNode node = myFirstInRows[row];
        if (node != null) {
            return node.index;
        } else {
            return super.firstInRow(row);
        }
    }

    public ElementNode getFirstInRow(final int row) {
        return myFirstInRows[row];
    }

    public ElementNode getLastInRow(final int row) {
        return myLastInRows[row];
    }

    @Override
    public ElementNode getNode(final int row, final int col) {

        ElementNode prev = null;
        ElementNode next = null;

        // Search from left or right based on split
        if (row <= mySplit) {
            next = myFirstInRows[row];
            while (next != null && next.index < col) {
                prev = next;
                next = next.next;
            }
            if (next != null && next.index == col) {
                return next;
            }
        } else {
            prev = myLastInRows[row];
            while (prev != null && prev.index > col) {
                next = prev;
                prev = prev.previous;
            }
            if (prev != null && prev.index == col) {
                return prev;
            }
        }

        // Create new node
        ElementNode node = LinkedR064.newNode(col, ZERO);

        // Empty row
        if (prev == null && next == null) {
            myFirstInRows[row] = node;
            myLastInRows[row] = node;
            return node;
        }

        // Insert at start
        if (prev == null) {
            node.next = myFirstInRows[row];
            myFirstInRows[row].previous = node;
            myFirstInRows[row] = node;
            return node;
        }

        // Insert at end
        if (next == null) {
            node.previous = myLastInRows[row];
            myLastInRows[row].next = node;
            myLastInRows[row] = node;
            return node;
        }

        // Insert in middle
        node.next = next;
        node.previous = prev;
        prev.next = node;
        next.previous = node;
        return node;
    }

    @Override
    public ElementNode getNodeIfExists(final int row, final int col) {

        ElementNode prev = null;
        ElementNode next = null;

        // Search from left or right based on split
        if (row <= mySplit) {
            next = myFirstInRows[row];
            while (next != null && next.index < col) {
                prev = next;
                next = next.next;
            }
            if (next != null && next.index == col) {
                return next;
            }
        } else {
            prev = myLastInRows[row];
            while (prev != null && prev.index > col) {
                next = prev;
                prev = prev.previous;
            }
            if (prev != null && prev.index == col) {
                return prev;
            }
        }

        return null;
    }

    public ElementNode insertNodeAfter(final ElementNode existingNode, final int col, final double value) {
        ElementNode newNode = LinkedR064.newNode(col, value);
        newNode.previous = existingNode;
        newNode.next = existingNode.next;
        if (existingNode.next != null) {
            existingNode.next.previous = newNode;
        } else {
            myLastInRows[existingNode.index] = newNode;
        }
        existingNode.next = newNode;
        return newNode;
    }

    public ElementNode insertNodeBefore(final ElementNode existingNode, final int col, final double value) {
        ElementNode newNode = LinkedR064.newNode(col, value);
        newNode.next = existingNode;
        newNode.previous = existingNode.previous;
        if (existingNode.previous != null) {
            existingNode.previous.next = newNode;
        } else {
            myFirstInRows[existingNode.index] = newNode;
        }
        existingNode.previous = newNode;
        return newNode;
    }

    @Override
    public int limitOfRow(final int row) {
        ElementNode node = myLastInRows[row];
        if (node != null) {
            return node.index + 1;
        } else {
            return super.limitOfRow(row);
        }
    }

    public void modifyNodeValue(final ElementNode node, final double newValue) {
        node.value = newValue;
    }

    public void remove(final int row, final ElementNode node) {

        // Only node in row
        if (node.previous == null && node.next == null) {
            myFirstInRows[row] = null;
            myLastInRows[row] = null;
            LinkedR064.recycle(node);
            return;
        }

        // First node in row
        if (node.previous == null) {
            myFirstInRows[row] = node.next;
            node.next.previous = null;
            LinkedR064.recycle(node);
            return;
        }

        // Last node in row
        if (node.next == null) {
            myLastInRows[row] = node.previous;
            node.previous.next = null;
            LinkedR064.recycle(node);
            return;
        }

        // Middle node
        node.previous.next = node.next;
        node.next.previous = node.previous;

        LinkedR064.recycle(node);
    }

    public void removeAndShift(final int remove, final int insert) {

        if (remove != insert) {
            for (int i = 0; i < myFirstInRows.length; i++) {
                this.removeAndShift(i, myFirstInRows[i], remove, insert);
            }
        }
    }

    public void removeShiftAndInsert(final int remove, final int insert, final Access1D<?> column) {

        this.removeAndShift(remove, insert);

        for (ElementView1D<?, ?> element : column.nonzeros()) {
            this.set(element.index(), insert, element.doubleValue());
        }
    }

    @Override
    public void reset() {
        Arrays.fill(myFirstInRows, null);
        Arrays.fill(myLastInRows, null);
    }

    @Override
    public void setFirst(final int row, final int col, final double value) {

        if (!PRECISION.isZero(value)) {

            ElementNode next = myFirstInRows[row];
            ElementNode previous = LinkedR064.newNode(col, value);

            if (next != null) {

                next.previous = previous;
                previous.next = next;

            } else {

                myLastInRows[row] = previous;
            }

            myFirstInRows[row] = previous;
        }
    }

    /**
     * If/when you know you're creating a new last non-zero in the specified row this is much faster than the
     * usual {@link #set(int, int, double)}.
     */
    @Override
    public void setLast(final int row, final int col, final double value) {

        if (!PRECISION.isZero(value)) {

            ElementNode previous = myLastInRows[row];
            ElementNode next = LinkedR064.newNode(col, value);

            if (previous != null) {

                previous.next = next;
                next.previous = previous;

            } else {

                myFirstInRows[row] = next;
            }

            myLastInRows[row] = next;
        }
    }

    @Override
    public void supplyTo(final TransformableRegion<Double> receiver) {

        // First clear the receiver
        receiver.reset();

        // Iterate through each column
        for (int row = 0; row < myFirstInRows.length; row++) {
            // Get first element in this column
            ElementNode current = myFirstInRows[row];

            // Traverse the linked list for this column
            while (current != null) {
                // Set non-zero value in receiver
                receiver.set(row, current.index, current.value);
                current = current.next;
            }
        }
    }

    @Override
    public R064CSC toCSC() {

        int nbRows = this.getRowDim();
        int nbCols = this.getColDim();

        // Count non-zeros per column
        int[] colCounts = new int[nbCols];
        for (int row = 0; row < nbRows; row++) {
            ElementNode current = myFirstInRows[row];
            while (current != null) {
                colCounts[current.index]++;
                current = current.next;
            }
        }

        // Calculate column pointers
        int[] colPointers = new int[nbCols + 1];
        int nnz = 0;
        for (int col = 0; col < nbCols; col++) {
            colPointers[col] = nnz;
            nnz += colCounts[col];
        }
        colPointers[nbCols] = nnz;

        // Create arrays for row indices and values
        int[] rowIndices = new int[nnz];
        double[] values = new double[nnz];

        // Fill arrays
        int[] colPositions = new int[nbCols]; // Track current position in each column
        for (int row = 0; row < nbRows; row++) {
            ElementNode current = myFirstInRows[row];
            while (current != null) {
                int col = current.index;
                int pos = colPointers[col] + colPositions[col];
                rowIndices[pos] = row;
                values[pos] = current.value;
                colPositions[col]++;
                current = current.next;
            }
        }

        return new R064CSC(nbRows, nbCols, values, rowIndices, colPointers);
    }

    @Override
    public R064CSR toCSR() {

        int nbRows = this.getRowDim();
        int nbCols = this.getColDim();

        // Count non-zeros and calculate row pointers in one pass
        int[] rowPointers = new int[nbRows + 1];
        int nnz = 0;
        for (int row = 0; row < nbRows; row++) {
            rowPointers[row] = nnz;
            ElementNode current = myFirstInRows[row];
            while (current != null) {
                nnz++;
                current = current.next;
            }
        }
        rowPointers[nbRows] = nnz;

        // Create arrays for column indices and values
        int[] colIndices = new int[nnz];
        double[] values = new double[nnz];

        // Fill arrays
        int pos = 0;
        for (int row = 0; row < nbRows; row++) {
            ElementNode current = myFirstInRows[row];
            while (current != null) {
                colIndices[pos] = current.index;
                values[pos] = current.value;
                pos++;
                current = current.next;
            }
        }

        return new R064CSR(nbRows, nbCols, values, colIndices, rowPointers);
    }

    @Override
    public List<Triplet> toTriplets() {
        List<Triplet> retVal = new ArrayList<>();
        for (int row = 0; row < myFirstInRows.length; row++) {
            ElementNode current = myFirstInRows[row];
            while (current != null) {
                retVal.add(new Triplet(row, current.index, current.value));
                current = current.next;
            }
        }
        return retVal;
    }

    private void removeAndShift(final int row, final ElementNode firstInRow, final int remove, final int insert) {

        ElementNode current = firstInRow;
        while (current != null && current.index <= insert) {

            if (current.index == remove) {
                ElementNode next = current.next;
                this.remove(row, current);
                current = next;
            } else if (remove < current.index) {
                --current.index;
                current = current.next;
            } else {
                current = current.next;
            }
        }
    }

    @Override
    void removeIfZero(final int row, final int col, final ElementNode node) {

        if (PRECISION.isZero(node.value)) {
            this.remove(row, node);
        }
    }

}
