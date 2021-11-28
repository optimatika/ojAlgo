package org.ojalgo.matrix.store;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.NullaryFunction;

public class ComposingStoreTest extends MatrixStoreTests {

    private final static int BLOCK_COUNT = 128;
    private final static int SPARSE_SIZE = 128;
    private MatrixStore<Double> theMatrix;

    @Test
    public void firstInColumn() {
        int block = (int) Math.floor(Math.random() * BLOCK_COUNT);
        TestUtils.assertEquals(block * 128 + SPARSE_SIZE, theMatrix.firstInColumn(block * 128 + SPARSE_SIZE + 64));
    }

    @Test
    public void firstInRow() {
        int block = (int) Math.floor(Math.random() * BLOCK_COUNT);
        TestUtils.assertEquals(block * 128 + SPARSE_SIZE, theMatrix.firstInRow(block * 128 + SPARSE_SIZE + 64));
    }

    @Test
    public void limitOfColumn() {
        int block = (int) Math.floor(Math.random() * BLOCK_COUNT);
        TestUtils.assertEquals((block + 1) * 128 + 128, theMatrix.limitOfColumn(block * 128 + 192));
    }

    @Test
    public void limitOfRow() {
        int block = (int) Math.floor(Math.random() * BLOCK_COUNT);
        TestUtils.assertEquals((block + 1) * 128 + 128, theMatrix.limitOfRow(block * 128 + 192));
    }

    @BeforeEach
    public void setUp() {
        theMatrix = this.sparseMatrix(SPARSE_SIZE, SPARSE_SIZE);
        for (int i = 0; i < BLOCK_COUNT; i++) {
            theMatrix = this.blockMatrix(theMatrix, this.filledMatrix(128, 128));
        }
    }

    private MatrixStore<Double> blockMatrix(final MatrixStore<Double> upperLeft, final MatrixStore<Double> lowerRight) {
        return upperLeft.right((int) lowerRight.countColumns()).below(lowerRight.left((int) upperLeft.countColumns()));
    }

    private MatrixStore<Double> filledMatrix(final int rowCount, final int colCount) {
        return this.filledMatrix(rowCount, colCount, 1d);
    }

    private MatrixStore<Double> filledMatrix(final int rowCount, final int colCount, final double value) {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        return storeFactory.makeFilled(rowCount, colCount, new NullaryFunction<Double>() {

            public double doubleValue() {
                return ONE;
            }

            public Double invoke() {
                return ONE;
            }

        });
    }

    private MatrixStore<Double> sparseMatrix(final int rowCount, final int colCount) {
        return this.sparseMatrix(rowCount, colCount, (int) Math.floor(Math.random() * rowCount * colCount * .075d));
    }

    private MatrixStore<Double> sparseMatrix(final int rowCount, final int colCount, final int nonzeroCount) {
        SparseStore<Double> matrix = SparseStore.makePrimitive(rowCount, colCount);
        for (int i = 0; i < nonzeroCount; i++) {
            int row = (int) Math.floor(Math.random() * rowCount);
            int col = (int) Math.floor(Math.random() * colCount);
            matrix.set(row, col, Math.random() * 5 + 1);
        }
        return matrix;
    }
}
