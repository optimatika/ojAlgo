package org.ojalgo.matrix.store;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.DensityTrackingArray;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ElementView2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

public class R064CSCTest extends MatrixStoreTests {

    private static final NumberContext ACCURACY = NumberContext.of(12);

    private static void validateStructure(final R064CSC matrix) {

        // Check array lengths
        TestUtils.assertEquals(matrix.values.length, matrix.indices.length);
        TestUtils.assertEquals(matrix.getColDim() + 1, matrix.pointers.length);

        // Check pointer values
        TestUtils.assertEquals(0, matrix.pointers[0]);
        TestUtils.assertEquals(matrix.values.length, matrix.pointers[matrix.getColDim()]);

        // Check pointers are non-decreasing
        for (int i = 0; i < matrix.getColDim(); i++) {
            TestUtils.assertTrue(matrix.pointers[i] <= matrix.pointers[i + 1]);
        }

        // Check row indices are strictly increasing within each column
        for (int i = 0; i < matrix.getColDim(); i++) {
            int start = matrix.pointers[i];
            int end = matrix.pointers[i + 1];
            for (int j = start + 1; j < end; j++) {
                TestUtils.assertTrue(matrix.indices[j - 1] < matrix.indices[j]);
            }
        }

        // Check row indices are within bounds
        for (int i = 0; i < matrix.indices.length; i++) {
            TestUtils.assertTrue(matrix.indices[i] >= 0 && matrix.indices[i] < matrix.getRowDim());
        }
    }

    @Test
    public void testCSCTransposeMatchesDense() {

        // Matrix A (3x4):
        // [ 1 0 2 0 ]
        // [ 0 3 0 0 ]
        // [ 4 0 5 6 ]
        double[][] dense = { { 1.0, 0.0, 2.0, 0.0 }, { 0.0, 3.0, 0.0, 0.0 }, { 4.0, 0.0, 5.0, 6.0 } };

        // CSC representation of A
        double[] values = { 1, 4, 3, 2, 5, 6 };
        int[] rowIdx = { 0, 2, 1, 0, 2, 2 };
        int[] colPtr = { 0, 2, 3, 5, 6 };

        R064CSC csc = new R064CSC(3, 4, values, rowIdx, colPtr);

        // Dense transpose A^T
        int rowsT = 4;
        int colsT = 3;

        // Sparse transpose implementation
        R064CSR csrT = csc.transpose();

        for (int i = 0; i < rowsT; i++) {
            for (int j = 0; j < colsT; j++) {
                double expected = dense[j][i];
                double actual = csrT.doubleValue(i, j);
                TestUtils.assertEquals("Mismatch at (" + i + "," + j + ")", expected, actual, ACCURACY);
            }
        }
    }

    @Test
    public void testMatrixMatrixMultiplication() {

        // Test matrix A:
        // [1 0 2]
        // [0 3 0]
        // [4 0 5]
        double[] valuesA = { 1.0, 4.0, 3.0, 2.0, 5.0 };
        int[] rowIndicesA = { 0, 2, 1, 0, 2 };
        int[] columnPointersA = { 0, 2, 3, 5 };
        R064CSC matrixA = new R064CSC(3, 3, valuesA, rowIndicesA, columnPointersA);

        R064CSCTest.validateStructure(matrixA);

        // Test matrix B:
        // [1 0]
        // [0 2]
        // [3 0]
        double[] valuesB = { 1.0, 3.0, 2.0 };
        int[] rowIndicesB = { 0, 2, 1 };
        int[] columnPointersB = { 0, 2, 3 };
        R064CSC matrixB = new R064CSC(3, 2, valuesB, rowIndicesB, columnPointersB);

        R064CSCTest.validateStructure(matrixB);

        // Expected result:
        // [7 0]
        // [0 6]
        // [19 0]
        double[] valuesC = { 7.0, 19.0, 6.0 };
        int[] rowIndicesC = { 0, 2, 1 };
        int[] columnPointersC = { 0, 2, 3 };
        R064CSC matrixC = new R064CSC(3, 2, valuesC, rowIndicesC, columnPointersC);

        R064CSCTest.validateStructure(matrixC);

        TransformableRegion<Double> result = R064Store.FACTORY.make(3, 2);
        matrixA.multiply(matrixB, result);

        TestUtils.assertEquals(matrixC, result);
    }

    @Test
    public void testMatrixVectorMultiplication() {
        // Test matrix: [1 0 2]
        // [0 3 0]
        // [4 0 5]
        double[] values = { 1.0, 4.0, 3.0, 2.0, 5.0 };
        int[] rowIndices = { 0, 2, 1, 0, 2 };
        int[] columnPointers = { 0, 2, 3, 5 };
        R064CSC matrix = new R064CSC(3, 3, values, rowIndices, columnPointers);

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
        int[] rowIndices = { 0, 1, 2 };
        int[] columnPointers = { 0, 1, 2, 3 };
        R064CSC matrix = new R064CSC(3, 3, values, rowIndices, columnPointers);

        R064CSCTest.validateStructure(matrix);

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

        R064CSC matrix = new R064CSC(3, 3, new double[0], new int[0], new int[] { 0, 0, 0, 0 });

        R064CSCTest.validateStructure(matrix);

        ElementView2D<Double, ?> iterator = matrix.nonzeros();
        TestUtils.assertFalse(iterator.hasNext());
        TestUtils.assertFalse(iterator.hasPrevious());
    }

    @Test
    public void testNonzerosExceptions() {

        double[] values = { 1.0 };
        int[] rowIndices = { 0 };
        int[] columnPointers = { 0, 1, 1, 1 };
        R064CSC matrix = new R064CSC(3, 3, values, rowIndices, columnPointers);

        R064CSCTest.validateStructure(matrix);

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
        int[] rowIndices = { 0, 1, 2 };
        int[] columnPointers = { 0, 1, 2, 3 };
        R064CSC matrix = new R064CSC(3, 3, values, rowIndices, columnPointers);

        R064CSCTest.validateStructure(matrix);

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
        int[] rowIndices = { 0, 1, 2 };
        int[] columnPointers = { 0, 1, 2, 3 };
        R064CSC matrix = new R064CSC(3, 3, values, rowIndices, columnPointers);

        R064CSCTest.validateStructure(matrix);

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
        int[] rowIndices = { 1 };
        int[] columnPointers = { 0, 0, 1, 1 };
        R064CSC matrix = new R064CSC(3, 3, values, rowIndices, columnPointers);

        R064CSCTest.validateStructure(matrix);

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

        // Test a sparse matrix with non-zero elements in different columns
        // Row indices must be strictly increasing within each column
        double[] values = { 1.0, 2.0, 3.0 };
        int[] rowIndices = { 0, 1, 2 }; // Row indices are strictly increasing
        int[] columnPointers = { 0, 2, 2, 3 }; // First column has 2 elements, second column has 0, third
                                               // column has 1
        R064CSC matrix = new R064CSC(3, 3, values, rowIndices, columnPointers);

        R064CSCTest.validateStructure(matrix);

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
        TestUtils.assertArrayEquals(new long[] { 0, 0, 2 }, collectedCols.stream().mapToLong(Long::longValue).toArray());
    }

    /**
     * Each route of {@link R064CSC#of(Access2D.Collectable)} gives the same matrix, with its dimensions also
     * when the last row and column are empty, and an {@link R064CSC} is returned as is.
     */
    @Test
    public void testOf() {

        R064Store dense = R064Store.FACTORY.make(4, 3);
        dense.set(0, 0, 1.0);
        dense.set(2, 0, -2.0);
        dense.set(0, 1, 4.0);
        dense.set(1, 1, 3.0);

        R064CSC fromDense = R064CSC.of(dense);
        TestUtils.assertEquals(4, fromDense.getRowDim());
        TestUtils.assertEquals(3, fromDense.getColDim());
        TestUtils.assertEquals(4, fromDense.countNonzeros());
        TestUtils.assertEquals(dense, fromDense);

        SparseStore<Double> sparse = SparseStore.R064.make(4, 3);
        dense.supplyTo(sparse);
        TestUtils.assertEquals(dense, R064CSC.of(sparse));

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
        TestUtils.assertEquals(dense, R064CSC.of(collectable));

        TestUtils.assertTrue(R064CSC.of(fromDense) == fromDense);
    }

    /**
     * Each route of {@link R064CSC#of(Access2D.Collectable, Structure2D.IntRowColPredicate)} keeps only the
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

        R064CSC fromDense = R064CSC.of(dense, filter);
        TestUtils.assertEquals(4, fromDense.getRowDim());
        TestUtils.assertEquals(3, fromDense.getColDim());
        TestUtils.assertEquals(3, fromDense.countNonzeros());
        TestUtils.assertEquals(upper, fromDense);

        SparseStore<Double> sparse = SparseStore.R064.make(4, 3);
        dense.supplyTo(sparse);
        TestUtils.assertEquals(upper, R064CSC.of(sparse, filter));
        TestUtils.assertEquals(upper, R064CSC.of(R064CSR.of(dense), filter));
        TestUtils.assertEquals(upper, R064CSC.of(R064CSC.of(dense), filter));

        RowsSupplier<Double> rows = R064Store.FACTORY.makeRowsSupplier(3);
        rows.addRows(4);
        dense.supplyTo(rows);
        R064CSC fromRows = R064CSC.of(rows, filter);
        TestUtils.assertEquals(3, fromRows.countNonzeros());
        TestUtils.assertEquals(upper, fromRows);

        ColumnsSupplier<Double> columns = R064Store.FACTORY.makeColumnsSupplier(4);
        columns.addColumns(3);
        dense.supplyTo(columns);
        R064CSC fromColumns = R064CSC.of(columns, filter);
        TestUtils.assertEquals(3, fromColumns.countNonzeros());
        TestUtils.assertEquals(upper, fromColumns);

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
        TestUtils.assertEquals(upper, R064CSC.of(collectable, filter));
    }

    /**
     * Loading a column into a {@link DensityTrackingArray} resets it first, and lists exactly the column's
     * nonzeros.
     */
    @Test
    public void testSupplyToDensityTrackingArray() {

        R064Store dense = R064Store.FACTORY.make(4, 2);
        dense.set(0, 1, 3.0);
        dense.set(2, 1, -1.0);
        dense.set(3, 0, 5.0);
        R064CSC csc = R064CSC.of(dense);

        DensityTrackingArray target = new DensityTrackingArray(4);
        target.set(1, 9.0);

        csc.supplyTo(1, target);

        TestUtils.assertEquals(2, target.countNonzeros());
        TestUtils.assertEquals(new double[] { 3.0, 0.0, -1.0, 0.0 }, target.values);
    }

}
