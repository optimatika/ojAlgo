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
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.type.math.MathType;

/**
 * R064-Linked-Sparse-Rows (like CSR but rows as linked lists).
 */
public final class R064LSR extends SparseR064 {

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

    private final SparseR064.ElementNode[] myFirstInRows;
    private final SparseR064.ElementNode[] myLastInRows;
    private final int mySplit;

    R064LSR(final int nbRows, final int nbCols) {
        super(nbRows, nbCols);
        myFirstInRows = new SparseR064.ElementNode[nbRows];
        myLastInRows = new SparseR064.ElementNode[nbRows];
        mySplit = nbRows / 2;
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
    public int limitOfRow(final int row) {
        ElementNode node = myLastInRows[row];
        if (node != null) {
            return node.index + 1;
        } else {
            return super.limitOfRow(row);
        }
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

    private void remove(final int row, final ElementNode node) {

        // Only node in row
        if (node.previous == null && node.next == null) {
            myFirstInRows[row] = null;
            myLastInRows[row] = null;
            return;
        }

        // First node in row
        if (node.previous == null) {
            myFirstInRows[row] = node.next;
            node.next.previous = null;
            return;
        }

        // Last node in row
        if (node.next == null) {
            myLastInRows[row] = node.previous;
            node.previous.next = null;
            return;
        }

        // Middle node
        node.previous.next = node.next;
        node.next.previous = node.previous;
    }

    private void removeAndShift(final int row, final ElementNode firstInRow, final int remove, final int insert) {

        ElementNode current = firstInRow;
        while (current != null && current.index <= insert) {

            if (current.index == remove) {
                this.remove(row, current);
            } else if (remove < current.index) {
                --current.index;
            }

            current = current.next;
        }
    }

    @Override
    ElementNode getNode(final int row, final int col) {
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
        ElementNode node = new ElementNode(col, ZERO);

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
    void removeIfZero(final int row, final int col, final ElementNode node) {

        if (PRECISION.isZero(node.value)) {
            this.remove(row, node);
        }
    }

}
