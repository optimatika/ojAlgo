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

import org.ojalgo.function.FunctionSet;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.type.math.MathType;

/**
 * R064-Linked-Sparse-Columns (like CSC but columns as linked lists).
 */
public final class R064LSC extends SparseR064 {

    public static final class Factory implements Factory2D<R064LSC> {

        @Override
        public FunctionSet<Double> function() {
            return R064Store.FACTORY.function();
        }

        @Override
        public MathType getMathType() {
            return R064Store.FACTORY.getMathType();
        }

        @Override
        public R064LSC make(final int nbRows, final int nbCols) {
            return new R064LSC(nbRows, nbCols);
        }

        @Override
        public Scalar.Factory<?> scalar() {
            return R064Store.FACTORY.scalar();
        }
    }

    public static final R064LSC.Factory FACTORY = new R064LSC.Factory();

    private final SparseR064.ElementNode[] myFirstInColumns;
    private final SparseR064.ElementNode[] myLastInColumns;
    private final int mySplit;

    R064LSC(final int nbRows, final int nbCols) {
        super(nbRows, nbCols);
        myFirstInColumns = new SparseR064.ElementNode[nbCols];
        myLastInColumns = new SparseR064.ElementNode[nbCols];
        mySplit = nbCols / 2;
    }

    @Override
    public double doubleValue(final int row, final int col) {

        ElementNode current = null;

        if (col <= mySplit) {
            current = myFirstInColumns[col];
            while (current != null && current.index < row) {
                current = current.next;
            }
        } else {
            current = myLastInColumns[col];
            while (current != null && current.index > row) {
                current = current.previous;
            }
        }

        return (current != null && current.index == row) ? current.value : ZERO;
    }

    @Override
    public void exchangeColumns(final long colA, final long colB) {

        int intA = Math.toIntExact(colA);
        int intB = Math.toIntExact(colB);

        ElementNode tmpFirst = myFirstInColumns[intA];
        ElementNode tmpLast = myLastInColumns[intA];

        myFirstInColumns[intA] = myFirstInColumns[intB];
        myLastInColumns[intA] = myLastInColumns[intB];

        myFirstInColumns[intB] = tmpFirst;
        myLastInColumns[intB] = tmpLast;
    }

    public void exchangeRows(final int rowA, final int rowB, final int limit) {

        for (int col = 0; col < limit; col++) {

            if (myFirstInColumns[col] != null) {

                ElementNode nodeA = this.getNode(rowA, col);
                ElementNode nodeB = this.getNode(rowB, col);

                double tmpVal = nodeA.value;
                nodeA.value = nodeB.value;
                nodeB.value = tmpVal;

                this.removeIfZero(rowA, col, nodeA);
                this.removeIfZero(rowB, col, nodeB);
            }
        }
    }

    @Override
    public void exchangeRows(final long rowA, final long rowB) {
        this.exchangeRows(Math.toIntExact(rowA), Math.toIntExact(rowB), this.getColDim());
    }

    @Override
    public int firstInColumn(final int col) {
        ElementNode node = myFirstInColumns[col];
        if (node != null) {
            return node.index;
        } else {
            return super.firstInColumn(col);
        }
    }

    public ElementNode getFirstInColumn(final int col) {
        return myFirstInColumns[col];
    }

    public ElementNode getLastInColumn(final int col) {
        return myLastInColumns[col];
    }

    @Override
    public int limitOfColumn(final int col) {
        ElementNode node = myLastInColumns[col];
        if (node != null) {
            return node.index + 1;
        } else {
            return super.limitOfColumn(col);
        }
    }

    @Override
    public void reset() {
        Arrays.fill(myFirstInColumns, null);
        Arrays.fill(myLastInColumns, null);
    }

    @Override
    public void supplyTo(final TransformableRegion<Double> receiver) {

        // First clear the receiver
        receiver.reset();

        // Iterate through each column
        for (int col = 0; col < myFirstInColumns.length; col++) {
            // Get first element in this column
            ElementNode current = myFirstInColumns[col];

            // Traverse the linked list for this column
            while (current != null) {
                // Set non-zero value in receiver
                receiver.set(current.index, col, current.value);
                current = current.next;
            }
        }
    }

    private void remove(final int col, final ElementNode node) {

        // Only node in column
        if (node.previous == null && node.next == null) {
            myFirstInColumns[col] = null;
            myLastInColumns[col] = null;
            return;
        }

        // First node in column
        if (node.previous == null) {
            myFirstInColumns[col] = node.next;
            node.next.previous = null;
            return;
        }

        // Last node in column
        if (node.next == null) {
            myLastInColumns[col] = node.previous;
            node.previous.next = null;
            return;
        }

        // Middle node
        node.previous.next = node.next;
        node.next.previous = node.previous;
    }

    @Override
    ElementNode getNode(final int row, final int col) {

        ElementNode prev = null;
        ElementNode next = null;

        // Search from top or bottom based on split
        if (col <= mySplit) {
            next = myFirstInColumns[col];
            while (next != null && next.index < row) {
                prev = next;
                next = next.next;
            }
            if (next != null && next.index == row) {
                return next;
            }
        } else {
            prev = myLastInColumns[col];
            while (prev != null && prev.index > row) {
                next = prev;
                prev = prev.previous;
            }
            if (prev != null && prev.index == row) {
                return prev;
            }
        }

        // Create new node
        ElementNode node = new ElementNode(row, ZERO);

        // Empty column
        if (prev == null && next == null) {
            myFirstInColumns[col] = node;
            myLastInColumns[col] = node;
            return node;
        }

        // Insert at start
        if (prev == null) {
            node.next = myFirstInColumns[col];
            myFirstInColumns[col].previous = node;
            myFirstInColumns[col] = node;
            return node;
        }

        // Insert at end
        if (next == null) {
            node.previous = myLastInColumns[col];
            myLastInColumns[col].next = node;
            myLastInColumns[col] = node;
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
    void removeIfZero(final int row, final int col, final ElementNode node) {

        if (PRECISION.isZero(node.value)) {
            this.remove(col, node);
        }
    }

}
