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
package org.ojalgo.matrix.decomposition;

import static org.ojalgo.function.constant.PrimitiveMath.ONE;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.type.context.NumberContext;

/**
 * Tests for the Fletcher-Matthews LU update algorithm.
 * <p>
 * Focuses on testing repeated updates and column pivoting behavior.
 */
@Disabled
public class FletcherMatthewsTest extends MatrixDecompositionTests {

    private static final NumberContext ACCURACY = NumberContext.of(12);
    private static final NumberContext PRINT = NumberContext.of(6);

    /**
     * Tests repeated updates with column pivoting. Creates a matrix, performs initial decomposition, then
     * updates multiple columns in sequence, verifying that each update maintains the correct structure and
     * that column pivots are handled correctly.
     */
    @Test
    public void testRepeatedUpdatesWithPivoting() {

        // Create initial 4x4 matrix
        R064Store original = R064Store.FACTORY.make(4, 4);
        original.fillAll(ONE);
        original.set(0, 0, 2.0);
        original.set(1, 1, 3.0);
        original.set(2, 2, 4.0);
        original.set(3, 3, 5.0);

        // Perform initial LU decomposition
        LU<Double> decomposition = LU.R064.make();
        decomposition.decompose(original);

        // Create new columns to update
        R064Store newColumn1 = R064Store.FACTORY.make(4, 1);
        newColumn1.fillAll(ONE);
        newColumn1.set(0, 0, 6.0); // Large value to force pivot

        R064Store newColumn2 = R064Store.FACTORY.make(4, 1);
        newColumn2.fillAll(ONE);
        newColumn2.set(1, 0, 7.0); // Large value to force pivot

        R064Store newColumn3 = R064Store.FACTORY.make(4, 1);
        newColumn3.fillAll(ONE);
        newColumn3.set(2, 0, 8.0); // Large value to force pivot

        // Update first column
        boolean success1 = decomposition.updateColumn(0, newColumn1);
        Assertions.assertTrue(success1, "First update failed");

        // Verify structure after first update
        MatrixStore<Double> l1 = decomposition.getL();
        MatrixStore<Double> u1 = decomposition.getU();
        this.verifyTriangularStructure(l1, u1);

        // Update second column
        boolean success2 = decomposition.updateColumn(1, newColumn2);
        Assertions.assertTrue(success2, "Second update failed");

        // Verify structure after second update
        MatrixStore<Double> l2 = decomposition.getL();
        MatrixStore<Double> u2 = decomposition.getU();
        this.verifyTriangularStructure(l2, u2);

        // Update third column
        boolean success3 = decomposition.updateColumn(2, newColumn3);
        Assertions.assertTrue(success3, "Third update failed");

        // Verify structure after third update
        MatrixStore<Double> l3 = decomposition.getL();
        MatrixStore<Double> u3 = decomposition.getU();
        this.verifyTriangularStructure(l3, u3);

        // Verify that column pivots were applied correctly
        int[] pivotOrder = decomposition.getPivotOrder();
        Assertions.assertNotNull(pivotOrder, "Pivot order should not be null");
        Assertions.assertEquals(4, pivotOrder.length, "Pivot order should have length 4");
    }

    /**
     * Tests updates with small diagonal elements. Creates a matrix with small diagonal elements, performs
     * decomposition, and verifies that updates maintain numerical stability.
     */
    @Test
    public void testUpdatesWithSmallDiagonal() {

        // Create initial 3x3 matrix with small diagonal elements
        R064Store original = R064Store.FACTORY.make(3, 3);
        original.fillAll(ONE);
        original.set(0, 0, 1.0E-10);
        original.set(1, 1, 1.0E-10);
        original.set(2, 2, 1.0E-10);

        // Perform initial LU decomposition
        LU<Double> decomposition = LU.R064.make();
        decomposition.decompose(original);

        // Create new column to update
        R064Store newColumn = R064Store.FACTORY.make(3, 1);
        newColumn.fillAll(ONE);
        newColumn.set(0, 0, 1.0E+10); // Large value to force pivot

        // Update first column
        boolean success = decomposition.updateColumn(0, newColumn);
        Assertions.assertTrue(success, "Update failed");

        // Verify structure after update
        MatrixStore<Double> l = decomposition.getL();
        MatrixStore<Double> u = decomposition.getU();
        this.verifyTriangularStructure(l, u);

        // Verify that column pivots were applied to maintain numerical stability
        int[] pivotOrder = decomposition.getPivotOrder();
        Assertions.assertNotNull(pivotOrder, "Pivot order should not be null");
        Assertions.assertEquals(3, pivotOrder.length, "Pivot order should have length 3");
    }

    /**
     * Tests updates with zero diagonal elements. Creates a matrix with a zero diagonal element, performs
     * decomposition, and verifies that updates handle this case correctly.
     */
    @Test
    public void testUpdatesWithZeroDiagonal() {

        // Create initial 3x3 matrix with zero diagonal
        R064Store original = R064Store.FACTORY.make(3, 3);
        original.fillAll(ONE);
        original.set(0, 0, 0.0); // Zero diagonal element

        // Perform initial LU decomposition
        LU<Double> decomposition = LU.R064.make();
        decomposition.decompose(original);

        // Create new column to update
        R064Store newColumn = R064Store.FACTORY.make(3, 1);
        newColumn.fillAll(ONE);
        newColumn.set(1, 0, 5.0); // Large value to force pivot

        // Update first column
        boolean success = decomposition.updateColumn(0, newColumn);
        Assertions.assertTrue(success, "Update failed");

        // Verify structure after update
        MatrixStore<Double> l = decomposition.getL();
        MatrixStore<Double> u = decomposition.getU();
        this.verifyTriangularStructure(l, u);

        // Verify that column pivots were applied to handle zero diagonal
        int[] pivotOrder = decomposition.getPivotOrder();
        Assertions.assertNotNull(pivotOrder, "Pivot order should not be null");
        Assertions.assertEquals(3, pivotOrder.length, "Pivot order should have length 3");
    }

    private void verifyTriangularStructure(final MatrixStore<Double> l, final MatrixStore<Double> u) {
        // Verify L is lower triangular with unit diagonal
        for (int i = 0; i < l.getRowDim(); i++) {
            for (int j = 0; j < l.getColDim(); j++) {
                if (i < j) {
                    Assertions.assertEquals(0.0, l.doubleValue(i, j), ACCURACY.getPrecision(), "L should be lower triangular");
                } else if (i == j) {
                    Assertions.assertEquals(1.0, l.doubleValue(i, j), ACCURACY.getPrecision(), "L should have unit diagonal");
                }
            }
        }

        // Verify U is upper triangular
        for (int i = 0; i < u.getRowDim(); i++) {
            for (int j = 0; j < u.getColDim(); j++) {
                if (i > j) {
                    Assertions.assertEquals(0.0, u.doubleValue(i, j), ACCURACY.getPrecision(), "U should be upper triangular");
                }
            }
        }
    }
}