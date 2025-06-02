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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.decomposition.DecompositionUpdateTest.UpdateCase;
import org.ojalgo.matrix.decomposition.DecompositionUpdateTest.UpdateSequence;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

/**
 * Tests for the Fletcher-Matthews LU update algorithm.
 * <p>
 * Focuses on testing repeated updates and column pivoting behavior.
 */
public class FletcherMatthewsTest extends MatrixDecompositionTests {

    /**
     * Tests updateColumn on a small matrix with a known pattern.
     */
    @Test
    public void test3x3SmallMatrixUpdate() {

        UpdateCase updateCase = DecompositionUpdateTest.make3x3SmallMatrixUpdate();

        // Perform initial LU decomposition
        MatrixStore<Double> matrix = updateCase.originalMatrix;
        LU<Double> decomposition = LU.R064.decompose(matrix);

        int columnIndex = updateCase.columnIndex;
        MatrixStore<Double> newColumn = updateCase.newColumn;

        TestUtils.assertFalse(decomposition.updateColumn(columnIndex, newColumn));

    }

    /**
     * Tests repeated updates with column pivoting. Creates a matrix, performs initial decomposition, then
     * updates multiple columns in sequence, verifying that each update maintains the correct structure and
     * that column pivots are handled correctly.
     */
    @Test
    public void testRepeatedUpdatesWithPivoting() {

        UpdateSequence sequence = DecompositionUpdateTest.makeRepeatedUpdatesWithPivoting();

        // Perform initial LU decomposition
        PhysicalStore<Double> matrix = sequence.matrix;
        LU<Double> decomposition = LU.R064.decompose(matrix);

        MatrixStore<Double> rhs = sequence.rhs();

        DecompositionUpdateTest.doTestTran(matrix, decomposition, rhs);

        for (KeyedPrimitive<MatrixStore<Double>> update : sequence.updates) {

            int columnIndex = update.intValue();
            MatrixStore<Double> newColumn = update.left();

            matrix.fillColumn(columnIndex, newColumn);

            decomposition.updateColumn(columnIndex, newColumn);

            DecompositionUpdateTest.doTestTran(matrix, decomposition, rhs);
        }
    }

    /**
     * Tests updates with small diagonal elements. Creates a matrix with small diagonal elements, performs
     * decomposition, and verifies that updates maintain numerical stability.
     */
    @Test
    public void testUpdatesWithSmallDiagonal() {

        UpdateCase updateCase = DecompositionUpdateTest.makeUpdatesWithSmallDiagonal();

        // Perform initial LU decomposition
        LU<Double> decomposition = LU.R064.make();
        decomposition.decompose(updateCase.originalMatrix);
        // Update first column
        boolean success = decomposition.updateColumn(updateCase.columnIndex, updateCase.newColumn);
        Assertions.assertTrue(success, "Update failed");

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

        UpdateCase updateCase = DecompositionUpdateTest.makeUpdatesWithZeroDiagonal();

        MatrixStore<Double> originalMatrix = updateCase.originalMatrix;

        // Perform initial LU decomposition
        LU<Double> decomposition = LU.R064.decompose(originalMatrix);

        // Update first column
        int columnIndex = updateCase.columnIndex;
        MatrixStore<Double> newColumn = updateCase.newColumn;

        boolean success = decomposition.updateColumn(columnIndex, newColumn);

        Assertions.assertFalse(success, "Update should report failure, but didn't");
    }

}