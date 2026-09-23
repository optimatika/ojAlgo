package org.ojalgo.matrix.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.DensityTrackingArray;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ElementView2D;
import org.ojalgo.structure.Structure2D;

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

    /**
     * Each route of {@link R064CSR#of(Access2D.Collectable)} gives the same matrix, with its dimensions also
     * when the last row and column are empty, and an {@link R064CSR} is returned as is.
     */
    @Test
    public void testOf() {

        R064Store dense = R064Store.FACTORY.make(4, 3);
        dense.set(0, 0, 1.0);
        dense.set(2, 0, -2.0);
        dense.set(0, 1, 4.0);
        dense.set(1, 1, 3.0);

        R064CSR fromDense = R064CSR.of(dense);
        TestUtils.assertEquals(4, fromDense.getRowDim());
        TestUtils.assertEquals(3, fromDense.getColDim());
        TestUtils.assertEquals(4, fromDense.countNonzeros());
        TestUtils.assertEquals(dense, fromDense);

        SparseStore<Double> sparse = SparseStore.R064.make(4, 3);
        dense.supplyTo(sparse);
        TestUtils.assertEquals(dense, R064CSR.of(sparse));

        Access2D.Collectable<Double, TransformableRegion<Double>> collectable = new Access2D.Collectable<>() {

            @Override
            public int getColDim() {
                return 3;
            }

            @Override
            public int getRowDim() {
                return 4;
            }

            @Override
            public void supplyTo(final TransformableRegion<Double> receiver) {
                dense.supplyTo(receiver);
            }
        };
        TestUtils.assertEquals(dense, R064CSR.of(collectable));

        TestUtils.assertTrue(R064CSR.of(fromDense) == fromDense);
    }

    /**
     * Each route of {@link R064CSR#of(Access2D.Collectable, Structure2D.IntRowColPredicate)} keeps only the
     * elements the filter accepts, and the dimensions of the matrix.
     */
    @Test
    public void testOfFiltered() {

        R064Store dense = R064Store.FACTORY.make(4, 3);
        dense.set(0, 0, 1.0);
        dense.set(2, 0, -2.0);
        dense.set(0, 1, 4.0);
        dense.set(1, 1, 3.0);
        dense.set(3, 1, 5.0);

        R064Store upper = dense.copy();
        upper.set(2, 0, 0.0);
        upper.set(3, 1, 0.0);

        Structure2D.IntRowColPredicate filter = (row, col) -> row <= col;

        R064CSR fromDense = R064CSR.of(dense, filter);
        TestUtils.assertEquals(4, fromDense.getRowDim());
        TestUtils.assertEquals(3, fromDense.getColDim());
        TestUtils.assertEquals(3, fromDense.countNonzeros());
        TestUtils.assertEquals(upper, fromDense);

        SparseStore<Double> sparse = SparseStore.R064.make(4, 3);
        dense.supplyTo(sparse);
        TestUtils.assertEquals(upper, R064CSR.of(sparse, filter));
        TestUtils.assertEquals(upper, R064CSR.of(R064CSR.of(dense), filter));
        TestUtils.assertEquals(upper, R064CSR.of(R064CSC.of(dense), filter));

        Access2D.Collectable<Double, TransformableRegion<Double>> collectable = new Access2D.Collectable<>() {

            @Override
            public int getColDim() {
                return 3;
            }

            @Override
            public int getRowDim() {
                return 4;
            }

            @Override
            public void supplyTo(final TransformableRegion<Double> receiver) {
                dense.supplyTo(receiver);
            }
        };
        TestUtils.assertEquals(upper, R064CSR.of(collectable, filter));
    }

    /**
     * A dense row vector (the left vector's density is at least 0.1) times the matrix: accumulated without
     * tracking, and the index of the result built (with a scan) when needed. The sparse, tracking, branch is
     * tested by {@link #testPremultiplyTracksTouchedColumns()}.
     */
    @Test
    public void testPremultiplyDense() {
        // Test matrix: [1 0 2]
        // [0 3 0]
        // [4 0 5]
        double[] values = { 1.0, 2.0, 3.0, 4.0, 5.0 };
        int[] columnIndices = { 0, 2, 1, 0, 2 };
        int[] rowPointers = { 0, 2, 3, 5 };
        R064CSR matrix = new R064CSR(3, 3, values, columnIndices, rowPointers);

        DensityTrackingArray left = new DensityTrackingArray(3);
        left.set(0, 2.0);
        left.set(2, -1.0);

        DensityTrackingArray target = new DensityTrackingArray(3);
        target.set(1, 99.0);

        matrix.premultiply(left, target);

        // [2 0 -1] * A = [2*1 - 4, 0, 2*2 - 5] = [-2, 0, -1]
        TestUtils.assertEquals(-2.0, target.doubleValue(0));
        TestUtils.assertEquals(0.0, target.doubleValue(1));
        TestUtils.assertEquals(-1.0, target.doubleValue(2));
        TestUtils.assertEquals(2, target.countNonzeros());

        left.reset();
        left.set(1, 1.0);

        matrix.premultiply(left, target);

        TestUtils.assertEquals(0.0, target.doubleValue(0));
        TestUtils.assertEquals(3.0, target.doubleValue(1));
        TestUtils.assertEquals(0.0, target.doubleValue(2));
        TestUtils.assertEquals(1, target.countNonzeros());
    }

    /**
     * With a sparse left vector the touched columns are tracked. A column that cancels to exactly zero may be
     * listed, but no column is listed twice.
     */
    @Test
    public void testPremultiplyTracksTouchedColumns() {

        // Row 3: 1.0 at column 5, 2.0 at column 7
        // Row 11: -1.0 at column 5, 4.0 at column 9
        double[] values = { 1.0, 2.0, -1.0, 4.0 };
        int[] columnIndices = { 5, 7, 5, 9 };
        int[] rowPointers = new int[31];
        for (int i = 4; i <= 11; i++) {
            rowPointers[i] = 2;
        }
        for (int i = 12; i <= 30; i++) {
            rowPointers[i] = 4;
        }
        R064CSR matrix = new R064CSR(30, 30, values, columnIndices, rowPointers);

        DensityTrackingArray left = new DensityTrackingArray(30);
        left.set(3, 1.0);
        left.set(11, 1.0);
        TestUtils.assertTrue(left.isSparse());

        DensityTrackingArray target = new DensityTrackingArray(30);

        matrix.premultiply(left, target);

        TestUtils.assertEquals(0.0, target.doubleValue(5));
        TestUtils.assertEquals(2.0, target.doubleValue(7));
        TestUtils.assertEquals(4.0, target.doubleValue(9));

        target.add(5, 3.0);

        int[] listed = Arrays.copyOf(target.indices(), target.countNonzeros());
        Arrays.sort(listed);
        TestUtils.assertEquals(new int[] { 5, 7, 9 }, listed);
        TestUtils.assertEquals(3.0, target.doubleValue(5));
    }

}