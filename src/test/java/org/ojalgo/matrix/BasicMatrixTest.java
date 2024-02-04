/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.matrix;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.decomposition.Eigenvalue.Eigenpair;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access2D.ColumnView;
import org.ojalgo.structure.Access2D.RowView;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
@SuppressWarnings("rawtypes")
public abstract class BasicMatrixTest extends MatrixTests {

    protected static final NumberContext ACCURACY = NumberContext.of(6, 4);

    public static MatrixR064 getIdentity(final long rows, final long columns, final NumberContext context) {
        MatrixR064 tmpMtrx = MatrixR064.FACTORY.makeEye(Math.toIntExact(rows), Math.toIntExact(columns));
        return tmpMtrx.enforce(context);
    }

    public static MatrixR064 getSafe(final long rows, final long columns, final NumberContext context) {
        MatrixR064 tmpMtrx = MatrixR064.FACTORY.makeFilled(rows, columns, new Uniform(PrimitiveMath.E, PrimitiveMath.PI));
        return tmpMtrx.enforce(context);
    }

    static MatrixFactory<?, ?, ?, ?>[] factories() {
        return new MatrixFactory<?, ?, ?, ?>[] { MatrixR064.FACTORY, MatrixR032.FACTORY, MatrixR128.FACTORY, MatrixQ128.FACTORY, MatrixC128.FACTORY,
                MatrixH256.FACTORY };
    }

    MatrixR064 mtrxA;
    MatrixR064 mtrxB;
    MatrixR064 mtrxI;
    MatrixR064 mtrxSafe;
    MatrixR064 mtrxX;

    double scalar;

    @AfterEach
    public void doAfterEach() {
        // ACCURACY = NumberContext.getGeneral(9);
    }

    @BeforeEach
    public void doBeforeEach() {

        TestUtils.minimiseAllBranchLimits();

        scalar = Math.random();
    }

    /**
     * @see org.ojalgo.matrix.Mutator2D#add(long, long, Comparable)
     */
    @Test
    public void testAddElement() {

        long row = Uniform.randomInteger(mtrxA.countRows());
        long col = Uniform.randomInteger(mtrxA.countColumns());

        Mutator2D mutator = mtrxA.copy();
        mutator.add(row, col, scalar);
        BasicMatrix expected = mutator.get();

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            mutator = tmpA.copy();
            mutator.add(row, col, scalar);
            actual = mutator.get();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#add(org.ojalgo.matrix.BasicMatrix)
     */
    @Test
    public void testAddMatrix() {

        BasicMatrix expected = mtrxA.add(mtrxSafe);

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);
            BasicMatrix tmpSafe = factory.copy(mtrxSafe);

            actual = tmpA.add(tmpSafe);

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#add(double)
     */
    @Test
    public void testAddScalar() {

        BasicMatrix expected = mtrxA.add(scalar);

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.add(scalar);

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#conjugate()
     */
    @Test
    public void testConjugate() {

        BasicMatrix expected = mtrxA.conjugate();

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.conjugate();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#count()
     */
    @Test
    public void testCount() {

        long expected = mtrxA.count();

        long actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {
            BasicMatrix tmpA = factory.copy(mtrxA);
            actual = tmpA.count();
            TestUtils.assertEquals(factory.getClass().toString(), expected, actual);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#countColumns()
     */
    @Test
    public void testCountColumns() {

        long expected = mtrxA.countColumns();

        long actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {
            BasicMatrix tmpA = factory.copy(mtrxA);
            actual = tmpA.countColumns();
            TestUtils.assertEquals(factory.getClass().toString(), expected, actual);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#countRows()
     */
    @Test
    public void testCountRows() {

        long expected = mtrxA.countRows();

        long actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {
            BasicMatrix tmpA = factory.copy(mtrxA);
            actual = tmpA.countRows();
            TestUtils.assertEquals(factory.getClass().toString(), expected, actual);
        }
    }

    @Test
    public void testDivideElementsBasicMatrix() {

        Mutator2D mutator = mtrxA.copy();
        mutator.modifyMatching(PrimitiveMath.DIVIDE, mtrxSafe);
        BasicMatrix expected = mutator.get();

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);
            BasicMatrix tmpSafe = factory.copy(mtrxSafe);

            mutator = tmpA.copy();
            mutator.modifyMatching(factory.function().divide(), tmpSafe);
            actual = mutator.get();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#divide(Comparable)
     */
    @Test
    public void testDivideScalar() {

        BasicMatrix expected = mtrxA.divide(scalar);

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.divide(scalar);

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    @Test
    public void testDotAccess1D() {

        int col = Uniform.randomInteger(mtrxA.getColDim());

        double expected = mtrxA.column(col).dot(mtrxSafe.column(col));

        double actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);
            BasicMatrix tmpSafe = factory.copy(mtrxSafe);

            actual = tmpA.column(col).dot(tmpSafe.column(col));
            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#doubleValue(long, long)
     */
    @Test
    public void testDoubleValueIntInt() {

        long row = Uniform.randomInteger(mtrxA.countRows());
        long col = Uniform.randomInteger(mtrxA.countColumns());

        double expected = mtrxA.doubleValue(row, col);

        double actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.doubleValue(row, col);

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    @Test
    public void testGetColumnsIntArray() {

        int[] cols = new int[1 + Uniform.randomInteger(mtrxA.getColDim())];
        for (int i = 0; i < cols.length; i++) {
            cols[i] = Uniform.randomInteger(mtrxA.getColDim());
        }

        BasicMatrix expected = mtrxA.columns(cols);

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.columns(cols);

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getCondition()
     */
    @Test
    public void testGetCondition() {

        if (mtrxA.getRank() == mtrxA.getMinDim()) {

            // Difficult to test numerically
            // Will only check that they are the same order of magnitude

            long expected = Math.round(Math.log10(mtrxA.getCondition()));

            long actual;
            for (MatrixFactory factory : BasicMatrixTest.factories()) {

                BasicMatrix tmpA = factory.copy(mtrxA);

                actual = Math.round(Math.log10(tmpA.getCondition()));

                TestUtils.assertEquals(factory.getClass().toString(), expected, actual);
            }
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getDeterminant()
     */
    @Test
    public void testGetDeterminant() {

        if (mtrxA.isSquare()) {

            Comparable<?> expected = mtrxA.getDeterminant();

            Comparable<?> actual;
            for (MatrixFactory factory : BasicMatrixTest.factories()) {

                BasicMatrix tmpA = factory.copy(mtrxA);

                actual = tmpA.getDeterminant();

                TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
            }
        }
    }

    @Test
    public void testGetEigenvalues() {

        if (mtrxA.isSquare() && mtrxA.isHermitian()) {

            List<Eigenpair> expected = mtrxA.getEigenpairs();

            List<Eigenpair> actual;
            for (MatrixFactory factory : BasicMatrixTest.factories()) {

                BasicMatrix tmpA = factory.copy(mtrxA);

                actual = tmpA.getEigenpairs();

                for (int i = 0; i < expected.size(); i++) {
                    TestUtils.assertEquals(factory.getClass().toString(), expected.get(i).value, actual.get(i).value, ACCURACY);
                }
            }
        }
    }

    @Test
    public void testGetInfinityNorm() {

        double expected = BasicMatrix.calculateInfinityNorm(mtrxA);

        double actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = BasicMatrix.calculateInfinityNorm(tmpA);

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    @Test
    public void testGetOneNorm() {

        double expected = BasicMatrix.calculateOneNorm(mtrxA);

        double actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = BasicMatrix.calculateOneNorm(tmpA);

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getRank()
     */
    @Test
    public void testGetRank() {

        int expected = mtrxA.getRank();

        int actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.getRank();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual);
        }
    }

    @Test
    public void testGetRowsIntArray() {

        int[] rows = new int[1 + Uniform.randomInteger(mtrxA.getRowDim())];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = Uniform.randomInteger(mtrxA.getRowDim());
        }

        BasicMatrix expected = mtrxA.rows(rows);

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.rows(rows);

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getTrace()
     */
    @Test
    public void testGetTrace() {

        Comparable<?> expected = mtrxA.getTrace();

        Comparable<?> actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.getTrace();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#invert()
     */
    @Test
    public void testInvert() {

        if (mtrxA.isSquare() && mtrxA.getRank() >= mtrxA.getColDim()) {

            BasicMatrix expected = mtrxA.invert();

            BasicMatrix actual;
            for (MatrixFactory factory : BasicMatrixTest.factories()) {

                BasicMatrix tmpA = factory.copy(mtrxA);

                actual = tmpA.invert();

                TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
            }
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isEmpty()
     */
    @Test
    public void testIsEmpty() {

        boolean expected = mtrxA.isEmpty();

        boolean actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.isEmpty();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isFat()
     */
    @Test
    public void testIsFat() {

        boolean expected = mtrxA.isFat();

        boolean actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.isFat();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isHermitian()
     */
    @Test
    public void testIsHermitian() {

        boolean expected = mtrxA.isHermitian();

        boolean actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.isHermitian();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isSquare()
     */
    @Test
    public void testIsSquare() {

        boolean expected = mtrxA.isSquare();

        boolean actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.isSquare();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isSymmetric()
     */
    @Test
    public void testIsSymmetric() {

        boolean expected = mtrxA.isSymmetric();

        boolean actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.isSymmetric();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isTall()
     */
    @Test
    public void testIsTall() {

        boolean expected = mtrxA.isTall();

        boolean actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.isTall();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isVector()
     */
    @Test
    public void testIsVector() {

        boolean expected = mtrxA.isVector();

        boolean actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.isVector();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual);
        }
    }

    @Test
    public void testLogicalBuilder() {

        BasicMatrix expected = mtrxA.below(mtrxSafe).repeat(1, 2).onAll(PrimitiveMath.SIN).diagonal();

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);
            BasicMatrix tmpSafe = factory.copy(mtrxSafe);

            UnaryFunction sin = factory.function().sin();

            actual = tmpA.below(tmpSafe).repeat(1, 2).diagonal().onAll(sin);
            TestUtils.assertEquals(expected, actual, ACCURACY);

            actual = tmpA.onAll(sin).below(tmpSafe.onAll(sin)).repeat(1, 2).diagonal();
            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    @Test
    public void testMergeColumnsBasicMatrix() {

        BasicMatrix expected = mtrxA.below(mtrxSafe);

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);
            BasicMatrix tmpSafe = factory.copy(mtrxSafe);

            actual = tmpA.below(tmpSafe);

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    @Test
    public void testMergeRowsBasicMatrix() {

        BasicMatrix expected = mtrxA.right(mtrxSafe);

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);
            BasicMatrix tmpSafe = factory.copy(mtrxSafe);

            actual = tmpA.right(tmpSafe);

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    @Test
    public void testMultiplyElementsBasicMatrix() {

        Mutator2D mutator = mtrxA.copy();
        mutator.modifyMatching(PrimitiveMath.MULTIPLY, mtrxSafe);
        BasicMatrix expected = mutator.get();

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);
            BasicMatrix tmpSafe = factory.copy(mtrxSafe);

            mutator = tmpA.copy();
            mutator.modifyMatching(factory.function().multiply(), tmpSafe);
            actual = mutator.get();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#multiply(org.ojalgo.matrix.BasicMatrix)
     */
    @Test
    public void testMultiplyMatrix() {

        BasicMatrix expected = mtrxA.multiply(mtrxX);

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);
            BasicMatrix tmpX = factory.copy(mtrxX);

            actual = tmpA.multiply(tmpX);

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#multiply(java.lang.Number)
     */
    @Test
    public void testMultiplyScalar() {

        BasicMatrix expected = mtrxA.multiply(scalar);

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.multiply(scalar);

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#negate()
     */
    @Test
    public void testNegate() {

        BasicMatrix expected = mtrxA.negate();

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.negate();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#norm()
     */
    @Test
    public void testNorm() {

        double expected = mtrxA.norm();

        double actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.norm();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    @Test
    public void testReduceRowsAndColumns() {

        BasicMatrix actual;
        BasicMatrix intermediate;
        BasicMatrix expected;

        for (Aggregator aggregator : Aggregator.values()) {

            if (aggregator == Aggregator.PRODUCT2) {
                continue; // Likely to overflow
            }

            String name = aggregator.name();

            // rows -> cols

            intermediate = mtrxA.reduceRows(aggregator);
            expected = intermediate.reduceColumns(aggregator);

            TestUtils.assertEquals(1L, expected.count());

            for (MatrixFactory factory : BasicMatrixTest.factories()) {

                BasicMatrix tmpA = factory.copy(mtrxA);

                intermediate = tmpA.reduceRows(aggregator);
                actual = intermediate.reduceColumns(aggregator);

                TestUtils.assertEquals(name, expected, actual, ACCURACY);
            }

            // cols -> rows

            intermediate = mtrxA.reduceColumns(aggregator);
            expected = intermediate.reduceRows(aggregator);

            TestUtils.assertEquals(1L, expected.count());

            for (MatrixFactory factory : BasicMatrixTest.factories()) {

                BasicMatrix tmpA = factory.copy(mtrxA);

                intermediate = tmpA.reduceColumns(aggregator);
                actual = intermediate.reduceRows(aggregator);

                TestUtils.assertEquals(name, expected, actual, ACCURACY);
            }
        }
    }

    /**
     * @see BasicMatrix.PhysicalReceiver#set(long, long, Number)
     */
    @Test
    public void testSetElement() {

        long row = Uniform.randomInteger(mtrxA.countRows());
        long col = Uniform.randomInteger(mtrxA.countColumns());

        Mutator2D mutator = mtrxA.copy();
        mutator.set(row, col, scalar);
        BasicMatrix expected = mutator.get();

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            mutator = tmpA.copy();
            mutator.set(row, col, scalar);
            actual = mutator.get();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#solve(org.ojalgo.structure.Access2D)
     */
    @Test
    public void testSolveMatrix() {

        if (mtrxA.isSquare() && mtrxA.getRank() >= mtrxA.countColumns()) {

            BasicMatrix expected = mtrxA.solve(mtrxB);

            BasicMatrix actual;
            for (MatrixFactory factory : BasicMatrixTest.factories()) {

                BasicMatrix tmpA = factory.copy(mtrxA);
                BasicMatrix tmpB = factory.copy(mtrxB);

                actual = tmpA.solve(tmpB);

                TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
            }
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#subtract(BasicMatrix)
     */
    @Test
    public void testSubtractMatrix() {

        BasicMatrix expected = mtrxA.subtract(mtrxSafe);

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);
            BasicMatrix tmpSafe = factory.copy(mtrxSafe);

            actual = tmpA.subtract(tmpSafe);

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#subtract(Comparable)
     */
    @Test
    public void testSubtractScalar() {

        BasicMatrix expected = mtrxA.subtract(scalar);

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.subtract(scalar);

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#columns()
     */
    @Test
    public void testToListOfColumns() {

        BasicMatrix expected = mtrxA;

        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            Iterable<ColumnView> columns = tmpA.columns();

            for (ColumnView actual : columns) {

                long j = actual.column();

                for (int i = 0; i < actual.size(); i++) {
                    TestUtils.assertEquals(factory.getClass().toString(), expected.doubleValue(i, j), actual.doubleValue(i), ACCURACY);
                }
            }
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#rows()
     */
    @Test
    public void testToListOfRows() {

        BasicMatrix expected = mtrxA;

        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            Iterable<RowView> rows = tmpA.rows();

            for (RowView actual : rows) {

                long i = actual.row();

                for (int j = 0; j < actual.size(); j++) {
                    TestUtils.assertEquals(factory.getClass().toString(), expected.doubleValue(i, j), actual.doubleValue(j), ACCURACY);
                }
            }
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toRawCopy1D()
     */
    @Test
    public void testToRawCopy1D() {

        double[] expected = mtrxA.toRawCopy1D();

        double[] actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.toRawCopy1D();

            for (int i = 0; i < expected.length; i++) {
                TestUtils.assertEquals(factory.getClass().toString(), expected[i], actual[i], ACCURACY);
            }
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toScalar(long, long)
     */
    @Test
    public void testToScalarIntInt() {

        long row = Uniform.randomInteger(mtrxA.countRows());
        long col = Uniform.randomInteger(mtrxA.countColumns());

        Scalar<?> expected = mtrxA.toScalar(row, col);

        Scalar<?> actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.toScalar(row, col);

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#transpose()
     */
    @Test
    public void testTranspose() {

        BasicMatrix expected = mtrxA.transpose();

        BasicMatrix actual;
        for (MatrixFactory factory : BasicMatrixTest.factories()) {

            BasicMatrix tmpA = factory.copy(mtrxA);

            actual = tmpA.transpose();

            TestUtils.assertEquals(factory.getClass().toString(), expected, actual, ACCURACY);
        }
    }

}
