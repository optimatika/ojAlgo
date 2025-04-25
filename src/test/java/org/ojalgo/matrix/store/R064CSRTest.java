package org.ojalgo.matrix.store;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.structure.ElementView2D;

public class R064CSRTest extends MatrixStoreTests {

    private static void validateStructure(final R064CSR matrix) {

        // Check array lengths
        TestUtils.assertEquals(matrix.values.length, matrix.indices.length);
        TestUtils.assertEquals(matrix.getRowDim() + 1, matrix.pointers.length);

        // Check pointer values
        TestUtils.assertEquals(0, matrix.pointers[0]);
        TestUtils.assertEquals(matrix.values.length, matrix.pointers[matrix.getRowDim()]);

        // Check pointers are non-decreasing
        for (int i = 0; i < matrix.getRowDim(); i++) {
            TestUtils.assertTrue(matrix.pointers[i] <= matrix.pointers[i + 1]);
        }

        // Check column indices are strictly increasing within each row
        for (int i = 0; i < matrix.getRowDim(); i++) {
            int start = matrix.pointers[i];
            int end = matrix.pointers[i + 1];
            for (int j = start + 1; j < end; j++) {
                TestUtils.assertTrue(matrix.indices[j - 1] < matrix.indices[j]);
            }
        }

        // Check column indices are within bounds
        for (int i = 0; i < matrix.indices.length; i++) {
            TestUtils.assertTrue(matrix.indices[i] >= 0 && matrix.indices[i] < matrix.getColDim());
        }
    }

    @Test
    public void testMatrixMatrixMultiplication() {

        // Test matrix A:
        // [1 0 2]
        // [0 3 0]
        // [4 0 5]
        double[] valuesA = { 1.0, 2.0, 3.0, 4.0, 5.0 };
        int[] columnIndicesA = { 0, 2, 1, 0, 2 };
        int[] rowPointersA = { 0, 2, 3, 5 };
        R064CSR matrixA = new R064CSR(3, 3, valuesA, columnIndicesA, rowPointersA);

        R064CSRTest.validateStructure(matrixA);

        // Test matrix B:
        // [1 0]
        // [0 2]
        // [3 0]
        double[] valuesB = { 1.0, 2.0, 3.0 };
        int[] columnIndicesB = { 0, 1, 0 };
        int[] rowPointersB = { 0, 1, 2, 3 };
        R064CSR matrixB = new R064CSR(3, 2, valuesB, columnIndicesB, rowPointersB);

        R064CSRTest.validateStructure(matrixB);

        // Expected result:
        // [7 0]
        // [0 6]
        // [19 0]
        double[] valuesC = { 7.0, 6.0, 19.0 };
        int[] columnIndicesC = { 0, 1, 0 };
        int[] rowPointersC = { 0, 1, 2, 3 };
        R064CSR matrixC = new R064CSR(3, 2, valuesC, columnIndicesC, rowPointersC);

        R064CSRTest.validateStructure(matrixC);

        TransformableRegion<Double> result = R064Store.FACTORY.make(3, 2);
        matrixA.multiply(matrixB, result);

        TestUtils.assertEquals(matrixC, result);
    }

    @Test
    public void testMatrixVectorMultiplication() {
        // Test matrix: [1 0 2]
        // [0 3 0]
        // [4 0 5]
        double[] values = { 1.0, 2.0, 3.0, 4.0, 5.0 };
        int[] columnIndices = { 0, 2, 1, 0, 2 };
        int[] rowPointers = { 0, 2, 3, 5 };
        R064CSR matrix = new R064CSR(3, 3, values, columnIndices, rowPointers);

        // Test with dense vector [1, 2, 3]
        double[][] vector = { { 1.0 }, { 2.0 }, { 3.0 } };
        double[] expected = { 7.0, 6.0, 19.0 }; // [1*1 + 0*2 + 2*3, 0*1 + 3*2 + 0*3, 4*1 + 0*2 + 5*3]

        TransformableRegion<Double> result = R064Store.FACTORY.make(3, 1);
        matrix.multiply(R064Store.FACTORY.rows(vector), result);

        for (int i = 0; i < 3; i++) {
            TestUtils.assertEquals(expected[i], result.doubleValue(i, 0));
        }

        // Test with sparse vector [0, 1, 0]
        double[][] sparseVector = { { 0.0 }, { 1.0 }, { 0.0 } };
        double[] expectedSparse = { 0.0, 3.0, 0.0 };

        result.reset();
        matrix.multiply(R064Store.FACTORY.rows(sparseVector), result);

        for (int i = 0; i < 3; i++) {
            TestUtils.assertEquals(expectedSparse[i], result.doubleValue(i, 0));
        }
    }

    @Test
    public void testNonzerosBidirectional() {

        double[] values = { 1.0, 2.0, 3.0 };
        int[] columnIndices = { 0, 1, 2 };
        int[] rowPointers = { 0, 1, 2, 3 };
        R064CSR matrix = new R064CSR(3, 3, values, columnIndices, rowPointers);

        R064CSRTest.validateStructure(matrix);

        long ind = 0L;
        for (ElementView2D<Double, ?> view : matrix.nonzeros()) {
            TestUtils.assertEquals(ind, view.row());
            TestUtils.assertEquals(ind, view.column());
            TestUtils.assertEquals(ind + 1.0, view.doubleValue());
            ind++;
        }

        ElementView2D<Double, ?> iterator = matrix.nonzeros();

        // Forward iteration
        TestUtils.assertTrue(iterator.hasNext());
        iterator.next();
        TestUtils.assertEquals(1.0, iterator.doubleValue());

        TestUtils.assertTrue(iterator.hasNext());
        iterator.next();
        TestUtils.assertEquals(2.0, iterator.doubleValue());

        TestUtils.assertTrue(iterator.hasNext());
        iterator.next();
        TestUtils.assertEquals(3.0, iterator.doubleValue());

        TestUtils.assertFalse(iterator.hasNext());

        // Backward iteration - we're already at the last element
        TestUtils.assertEquals(3.0, iterator.doubleValue()); // Check current element first

        TestUtils.assertTrue(iterator.hasPrevious());
        iterator.previous();
        TestUtils.assertEquals(2.0, iterator.doubleValue());

        TestUtils.assertTrue(iterator.hasPrevious());
        iterator.previous();
        TestUtils.assertEquals(1.0, iterator.doubleValue());

        TestUtils.assertFalse(iterator.hasPrevious());
    }

    @Test
    public void testNonzerosEmptyMatrix() {

        R064CSR matrix = new R064CSR(3, 3, new double[0], new int[0], new int[] { 0, 0, 0, 0 });

        R064CSRTest.validateStructure(matrix);

        ElementView2D<Double, ?> iterator = matrix.nonzeros();
        TestUtils.assertFalse(iterator.hasNext());
        TestUtils.assertFalse(iterator.hasPrevious());
    }

    @Test
    public void testNonzerosExceptions() {

        double[] values = { 1.0 };
        int[] columnIndices = { 0 };
        int[] rowPointers = { 0, 1, 1, 1 };
        R064CSR matrix = new R064CSR(3, 3, values, columnIndices, rowPointers);

        R064CSRTest.validateStructure(matrix);

        // Test next() at end
        ElementView2D<Double, ?> iterator1 = matrix.nonzeros();
        iterator1.next();
        TestUtils.assertThrows(NoSuchElementException.class, () -> iterator1.next());

        // Test previous() at start
        ElementView2D<Double, ?> iterator2 = matrix.nonzeros();
        TestUtils.assertThrows(NoSuchElementException.class, () -> iterator2.previous());
    }

    @Test
    public void testNonzerosIndex() {

        double[] values = { 1.0, 2.0, 3.0 };
        int[] columnIndices = { 0, 1, 2 };
        int[] rowPointers = { 0, 1, 2, 3 };
        R064CSR matrix = new R064CSR(3, 3, values, columnIndices, rowPointers);

        R064CSRTest.validateStructure(matrix);

        ElementView2D<Double, ?> iterator = matrix.nonzeros();

        iterator.next();
        TestUtils.assertEquals(0, iterator.index()); // First element at (0,0)
        iterator.next();
        TestUtils.assertEquals(4, iterator.index()); // Second element at (1,1)
        iterator.next();
        TestUtils.assertEquals(8, iterator.index()); // Third element at (2,2)
    }

    @Test
    public void testNonzerosMultipleElements() {

        double[] values = { 1.0, 2.0, 3.0 };
        int[] columnIndices = { 0, 1, 2 };
        int[] rowPointers = { 0, 1, 2, 3 };
        R064CSR matrix = new R064CSR(3, 3, values, columnIndices, rowPointers);

        R064CSRTest.validateStructure(matrix);

        List<Double> collectedValues = new ArrayList<>();
        List<Long> collectedRows = new ArrayList<>();
        List<Long> collectedCols = new ArrayList<>();

        for (ElementView2D<Double, ?> element : matrix.nonzeros()) {
            collectedValues.add(element.doubleValue());
            collectedRows.add(element.row());
            collectedCols.add(element.column());
        }

        TestUtils.assertEquals(3, collectedValues.size());
        TestUtils.assertArrayEquals(new double[] { 1.0, 2.0, 3.0 }, collectedValues.stream().mapToDouble(Double::doubleValue).toArray());
        TestUtils.assertArrayEquals(new long[] { 0, 1, 2 }, collectedRows.stream().mapToLong(Long::longValue).toArray());
        TestUtils.assertArrayEquals(new long[] { 0, 1, 2 }, collectedCols.stream().mapToLong(Long::longValue).toArray());
    }

    @Test
    public void testNonzerosSingleElement() {

        double[] values = { 1.0 };
        int[] columnIndices = { 1 };
        int[] rowPointers = { 0, 0, 1, 1 };
        R064CSR matrix = new R064CSR(3, 3, values, columnIndices, rowPointers);

        R064CSRTest.validateStructure(matrix);

        ElementView2D<Double, ?> iterator = matrix.nonzeros();
        TestUtils.assertTrue(iterator.hasNext());
        TestUtils.assertFalse(iterator.hasPrevious());

        iterator.next();
        TestUtils.assertEquals(1, iterator.row());
        TestUtils.assertEquals(1, iterator.column());
        TestUtils.assertEquals(1.0, iterator.doubleValue());

        TestUtils.assertFalse(iterator.hasNext());
    }

    @Test
    public void testNonzerosSparseMatrix() {

        // Test a sparse matrix with non-zero elements in different rows
        // Column indices must be strictly increasing within each row
        double[] values = { 1.0, 2.0, 3.0 };
        int[] columnIndices = { 0, 1, 2 }; // Column indices are strictly increasing
        int[] rowPointers = { 0, 2, 2, 3 }; // First row has 2 elements, second row has 0, third row has 1
        R064CSR matrix = new R064CSR(3, 3, values, columnIndices, rowPointers);

        R064CSRTest.validateStructure(matrix);

        List<Double> collectedValues = new ArrayList<>();
        List<Long> collectedRows = new ArrayList<>();
        List<Long> collectedCols = new ArrayList<>();

        for (ElementView2D<Double, ?> element : matrix.nonzeros()) {
            collectedValues.add(element.doubleValue());
            collectedRows.add(element.row());
            collectedCols.add(element.column());
        }

        TestUtils.assertEquals(3, collectedValues.size());
        TestUtils.assertArrayEquals(new double[] { 1.0, 2.0, 3.0 }, collectedValues.stream().mapToDouble(Double::doubleValue).toArray());
        TestUtils.assertArrayEquals(new long[] { 0, 0, 2 }, collectedRows.stream().mapToLong(Long::longValue).toArray());
        TestUtils.assertArrayEquals(new long[] { 0, 1, 2 }, collectedCols.stream().mapToLong(Long::longValue).toArray());
    }

}