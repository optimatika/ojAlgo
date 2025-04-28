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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.LinkedR064.ElementNode;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;

public class SparseR064Test extends MatrixStoreTests {

    private static void assertNodesNotPresent(final LinkedR064 store, final int row, final int col) {
        ElementNode node = store.getNode(row, col);
        TestUtils.assertEquals(ZERO, node.value);
    }

    private static void verifyBackwardLinks(final R064LSC store, final int col) {
        ElementNode current = store.getLastInColumn(col);
        while (current != null && current.previous != null) {
            TestUtils.assertTrue(current.index > current.previous.index);
            TestUtils.assertTrue(current == current.previous.next);
            current = current.previous;
        }
    }

    private static void verifyBackwardLinks(final R064LSR store, final int row) {
        ElementNode current = store.getLastInRow(row);
        while (current != null && current.previous != null) {
            TestUtils.assertTrue(current.index > current.previous.index);
            TestUtils.assertTrue(current == current.previous.next);
            current = current.previous;
        }
    }

    private static void verifyForwardLinks(final R064LSC store, final int col) {
        ElementNode current = store.getFirstInColumn(col);
        while (current != null && current.next != null) {
            TestUtils.assertTrue(current.index < current.next.index);
            TestUtils.assertTrue(current == current.next.previous);
            current = current.next;
        }
    }

    private static void verifyForwardLinks(final R064LSR store, final int row) {
        ElementNode current = store.getFirstInRow(row);
        while (current != null && current.next != null) {
            TestUtils.assertTrue(current.index < current.next.index);
            TestUtils.assertTrue(current == current.next.previous);
            current = current.next;
        }
    }

    @Test
    public void testAddOperation() {
        R064LSR rowStore = new R064LSR(5, 5);
        R064LSC colStore = new R064LSC(5, 5);

        // Add to zero
        rowStore.add(2, 3, 1.5);
        colStore.add(2, 3, 1.5);
        TestUtils.assertEquals(1.5, rowStore.doubleValue(2, 3));
        TestUtils.assertEquals(1.5, colStore.doubleValue(2, 3));

        // Add to existing value
        rowStore.add(2, 3, 1.0);
        colStore.add(2, 3, 1.0);
        TestUtils.assertEquals(2.5, rowStore.doubleValue(2, 3));
        TestUtils.assertEquals(2.5, colStore.doubleValue(2, 3));

        // Add to make zero
        rowStore.add(2, 3, -2.5);
        colStore.add(2, 3, -2.5);
        TestUtils.assertEquals(0.0, rowStore.doubleValue(2, 3));
        TestUtils.assertEquals(0.0, colStore.doubleValue(2, 3));
        SparseR064Test.assertNodesNotPresent(rowStore, 2, 3);
        SparseR064Test.assertNodesNotPresent(colStore, 2, 3);
    }

    @Test
    public void testBasicOperations() {

        R064LSR rowStore = new R064LSR(5, 5);
        R064LSC colStore = new R064LSC(5, 5);

        // Test set and get
        rowStore.set(1, 2, 3.0);
        colStore.set(1, 2, 3.0);
        TestUtils.assertEquals(3.0, rowStore.doubleValue(1, 2));
        TestUtils.assertEquals(3.0, colStore.doubleValue(1, 2));

        // Test zero values are not stored
        rowStore.set(1, 2, 0.0);
        colStore.set(1, 2, 0.0);
        TestUtils.assertEquals(0.0, rowStore.doubleValue(1, 2));
        TestUtils.assertEquals(0.0, colStore.doubleValue(1, 2));
        SparseR064Test.assertNodesNotPresent(rowStore, 1, 2);
        SparseR064Test.assertNodesNotPresent(colStore, 1, 2);
    }

    @Test
    public void testLinkedListIntegrity() {
        R064LSR rowStore = new R064LSR(5, 5);
        R064LSC colStore = new R064LSC(5, 5);

        // Create a sequence of nodes
        rowStore.set(2, 1, 1.0);
        rowStore.set(2, 2, 2.0);
        rowStore.set(2, 3, 3.0);

        colStore.set(1, 2, 1.0);
        colStore.set(2, 2, 2.0);
        colStore.set(3, 2, 3.0);

        // Verify forward links
        SparseR064Test.verifyForwardLinks(rowStore, 2);
        SparseR064Test.verifyForwardLinks(colStore, 2);

        // Verify backward links
        SparseR064Test.verifyBackwardLinks(rowStore, 2);
        SparseR064Test.verifyBackwardLinks(colStore, 2);

        // Remove middle node and verify links
        rowStore.set(2, 2, 0.0);
        colStore.set(2, 2, 0.0);

        SparseR064Test.verifyForwardLinks(rowStore, 2);
        SparseR064Test.verifyForwardLinks(colStore, 2);
        SparseR064Test.verifyBackwardLinks(rowStore, 2);
        SparseR064Test.verifyBackwardLinks(colStore, 2);
    }

    @Test
    public void testModifyOperation() {
        R064LSR rowStore = new R064LSR(5, 5);
        R064LSC colStore = new R064LSC(5, 5);

        PrimitiveFunction.Unary doubler = value -> value * 2.0;
        PrimitiveFunction.Unary makeZero = value -> 0.0;

        // Modify zero value
        rowStore.set(3, 4, 2.0);
        colStore.set(3, 4, 2.0);
        rowStore.modifyOne(3, 4, doubler);
        colStore.modifyOne(3, 4, doubler);
        TestUtils.assertEquals(4.0, rowStore.doubleValue(3, 4));
        TestUtils.assertEquals(4.0, colStore.doubleValue(3, 4));

        // Modify to zero
        rowStore.modifyOne(3, 4, makeZero);
        colStore.modifyOne(3, 4, makeZero);
        TestUtils.assertEquals(0.0, rowStore.doubleValue(3, 4));
        TestUtils.assertEquals(0.0, colStore.doubleValue(3, 4));
        SparseR064Test.assertNodesNotPresent(rowStore, 3, 4);
        SparseR064Test.assertNodesNotPresent(colStore, 3, 4);
    }

    @Test
    public void testRandomSet() {

        int dim = 9;

        R064Store dense = R064Store.FACTORY.make(dim, dim);
        R064LSR rows = new R064LSR(dim, dim);
        R064LSC cols = new R064LSC(dim, dim);

        for (int index = 0; index < dim; index++) {

            int row = Uniform.randomInteger(dim);
            int col = Uniform.randomInteger(dim);
            double value = Math.random();

            dense.set(row, index, value);
            rows.set(row, index, value);
            cols.set(row, index, value);

            dense.set(index, col, value);
            rows.set(index, col, value);
            cols.set(index, col, value);
        }

        if (DEBUG) {
            BasicLogger.debugMatrix("dense", dense);
            BasicLogger.debugMatrix("rows", rows);
            BasicLogger.debugMatrix("cols", cols);
        }

        TestUtils.assertEquals(dense, rows);
        TestUtils.assertEquals(dense, cols);
    }

    @Test
    public void testSplitBasedSearch() {
        // Test row store with split
        R064LSR rowStore = new R064LSR(10, 5);
        rowStore.set(2, 3, 1.0); // Upper half
        rowStore.set(7, 3, 2.0); // Lower half
        TestUtils.assertEquals(1.0, rowStore.doubleValue(2, 3));
        TestUtils.assertEquals(2.0, rowStore.doubleValue(7, 3));

        // Test column store with split
        R064LSC colStore = new R064LSC(5, 10);
        colStore.set(3, 2, 1.0); // Left half
        colStore.set(3, 7, 2.0); // Right half
        TestUtils.assertEquals(1.0, colStore.doubleValue(3, 2));
        TestUtils.assertEquals(2.0, colStore.doubleValue(3, 7));
    }

}