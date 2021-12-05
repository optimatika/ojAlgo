/*
 * Copyright 1997-2021 Optimatika
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

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.ComplexMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.constant.QuaternionMath;
import org.ojalgo.function.constant.RationalMath;
import org.ojalgo.matrix.decomposition.Eigenvalue.Eigenpair;
import org.ojalgo.matrix.decomposition.MatrixDecompositionTests;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.ColumnView;
import org.ojalgo.structure.RowView;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public abstract class BasicMatrixTest extends MatrixTests {

    protected static final NumberContext ACCURACY = NumberContext.of(7, 5);

    public static RationalMatrix getIdentity(final long rows, final long columns, final NumberContext context) {
        RationalMatrix tmpMtrx = RationalMatrix.FACTORY.makeEye(Math.toIntExact(rows), Math.toIntExact(columns));
        return tmpMtrx.enforce(context);
    }

    public static RationalMatrix getSafe(final long rows, final long columns, final NumberContext context) {
        RationalMatrix tmpMtrx = RationalMatrix.FACTORY.makeFilled(rows, columns, new Uniform(PrimitiveMath.E, PrimitiveMath.PI));
        return tmpMtrx.enforce(context);
    }

    ComplexMatrix cAA;
    ComplexMatrix cAB;
    ComplexMatrix cAX;
    ComplexMatrix cI;
    ComplexMatrix cSafe;

    Primitive32Matrix p32AA;
    Primitive32Matrix p32AB;
    Primitive32Matrix p32AX;
    Primitive32Matrix p32I;
    Primitive32Matrix p32Safe;

    Primitive64Matrix p64AA;
    Primitive64Matrix p64AB;
    Primitive64Matrix p64AX;
    Primitive64Matrix p64I;
    Primitive64Matrix p64Safe;

    QuaternionMatrix qAA;
    QuaternionMatrix qAB;
    QuaternionMatrix qAX;
    QuaternionMatrix qI;
    QuaternionMatrix qSafe;

    RationalMatrix rAA;
    RationalMatrix rAB;
    RationalMatrix rAX;
    RationalMatrix rI;
    RationalMatrix rSafe;

    BigDecimal scalar;

    @AfterEach
    public void doAfterEach() {
        // ACCURACY = NumberContext.getGeneral(9);
    }

    @BeforeEach
    public void doBeforeEach() {

        TestUtils.minimiseAllBranchLimits();

        p32AA = Primitive32Matrix.FACTORY.copy(rAA);
        p32AX = Primitive32Matrix.FACTORY.copy(rAX);
        p32AB = Primitive32Matrix.FACTORY.copy(rAB);
        p32I = Primitive32Matrix.FACTORY.copy(rI);
        p32Safe = Primitive32Matrix.FACTORY.copy(rSafe);

        p64AA = Primitive64Matrix.FACTORY.copy(rAA);
        p64AX = Primitive64Matrix.FACTORY.copy(rAX);
        p64AB = Primitive64Matrix.FACTORY.copy(rAB);
        p64I = Primitive64Matrix.FACTORY.copy(rI);
        p64Safe = Primitive64Matrix.FACTORY.copy(rSafe);

        cAA = ComplexMatrix.FACTORY.copy(rAA);
        cAX = ComplexMatrix.FACTORY.copy(rAX);
        cAB = ComplexMatrix.FACTORY.copy(rAB);
        cI = ComplexMatrix.FACTORY.copy(rI);
        cSafe = ComplexMatrix.FACTORY.copy(rSafe);

        qAA = QuaternionMatrix.FACTORY.copy(rAA);
        qAX = QuaternionMatrix.FACTORY.copy(rAX);
        qAB = QuaternionMatrix.FACTORY.copy(rAB);
        qI = QuaternionMatrix.FACTORY.copy(rI);
        qSafe = QuaternionMatrix.FACTORY.copy(rSafe);

        scalar = new BigDecimal(Math.random());
    }

    /**
     * @see BasicMatrix.PhysicalReceiver#add(long, long, Comparable)
     */
    @Test
    public void testAddElement() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> expected;

        long row = Uniform.randomInteger(rAA.countRows());
        long col = Uniform.randomInteger(rAA.countColumns());

        RationalMatrix.DenseReceiver rBuilder = rAA.copy();
        rBuilder.add(row, col, scalar);
        expected = rBuilder.get();

        ComplexMatrix.DenseReceiver cBuilder = cAA.copy();
        cBuilder.add(row, col, scalar);
        actual = cBuilder.get();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        Primitive64Matrix.DenseReceiver p64Builder = p64AA.copy();
        p64Builder.add(row, col, scalar);
        actual = p64Builder.get();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        Primitive32Matrix.DenseReceiver p32Builder = p32AA.copy();
        p32Builder.add(row, col, scalar);
        actual = p32Builder.get();
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#add(org.ojalgo.matrix.BasicMatrix)
     */
    @Test
    public void testAddMatrix() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> expected;

        expected = rAA.add(rSafe);

        actual = cAA.add(cSafe);
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.add(p64Safe);
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p32AA.add(p32Safe);
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#add(Comparable)
     */
    @Test
    public void testAddScalar() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> expected;

        expected = rAA.add(RationalNumber.valueOf(scalar));

        actual = cAA.add(ComplexNumber.valueOf(scalar));
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.add(scalar.doubleValue());
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p32AA.add(scalar.floatValue());
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#conjugate()
     */
    @Test
    public void testConjugate() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> expected;

        expected = rAA.conjugate();

        actual = cAA.conjugate();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.conjugate();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p32AA.conjugate();
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    @Test
    public void testCount() {

        long expected = rAA.count();
        long actual;

        actual = cAA.count();
        TestUtils.assertEquals(expected, actual);

        actual = p64AA.count();
        TestUtils.assertEquals(expected, actual);

        actual = p32AA.count();
        TestUtils.assertEquals(expected, actual);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#countColumns()
     */
    @Test
    public void testCountColumns() {

        long expected = rAA.countColumns();
        long actual;

        actual = cAA.countColumns();
        TestUtils.assertEquals(expected, actual);

        actual = p64AA.countColumns();
        TestUtils.assertEquals(expected, actual);

        actual = p32AA.countColumns();
        TestUtils.assertEquals(expected, actual);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#countRows()
     */
    @Test
    public void testCountRows() {

        long expected = rAA.countRows();
        long actual;

        actual = cAA.countRows();
        TestUtils.assertEquals(expected, actual);

        actual = p64AA.countRows();
        TestUtils.assertEquals(expected, actual);

        actual = p32AA.countRows();
        TestUtils.assertEquals(expected, actual);
    }

    @Test
    public void testDivideElementsBasicMatrix() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> expected;

        RationalMatrix.DenseReceiver copyRational = rAA.copy();
        copyRational.modifyMatching(RationalMath.DIVIDE, rSafe);
        expected = copyRational.get();

        Primitive64Matrix.DenseReceiver copyPrimitive = p64AA.copy();
        copyPrimitive.modifyMatching(PrimitiveMath.DIVIDE, p64Safe);
        actual = copyPrimitive.get();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        ComplexMatrix.DenseReceiver copyComplex = cAA.copy();
        copyComplex.modifyMatching(ComplexMath.DIVIDE, cSafe);
        actual = copyComplex.get();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        QuaternionMatrix.DenseReceiver copyQuaternion = qAA.copy();
        copyQuaternion.modifyMatching(QuaternionMath.DIVIDE, qSafe);
        actual = copyQuaternion.get();
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#divide(Comparable)
     */
    @Test
    public void testDivideScalar() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> expected;

        expected = rAA.divide(RationalNumber.valueOf(scalar));

        actual = cAA.divide(ComplexNumber.valueOf(scalar));
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.divide(scalar.doubleValue());
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p32AA.divide(scalar.floatValue());
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    @Test
    public void testDotAccess1D() {

        double actual;
        double expected = 0.0;

        int col = Uniform.randomInteger(rAA.getColDim());

        for (int i = 0; i < rAA.getRowDim(); i++) {
            expected += rAA.doubleValue(i, col) * rSafe.doubleValue(i, col);
        }

        actual = rAA.logical().column(col).get().dot(rSafe.logical().column(col).get());
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = cAA.logical().column(col).get().dot(cSafe.logical().column(col).get());
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.logical().column(col).get().dot(p64Safe.logical().column(col).get());
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#doubleValue(long, long)
     */
    @Test
    public void testDoubleValueIntInt() {

        Comparable<?> actual;
        Comparable<?> expected;

        int tmpRow = (int) Uniform.randomInteger(rAA.countRows());
        int tmpCol = (int) Uniform.randomInteger(rAA.countColumns());

        expected = rAA.doubleValue(tmpRow, tmpCol);

        actual = cAA.doubleValue(tmpRow, tmpCol);
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.doubleValue(tmpRow, tmpCol);
        TestUtils.assertEquals(expected, actual, ACCURACY);

    }

    @Test
    public void testGetColumnsIntArray() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> expected;

        int[] tmpArr = new int[(int) (1 + Uniform.randomInteger(rAA.countColumns()))];

        for (int i = 0; i < tmpArr.length; i++) {
            tmpArr[i] = (int) Uniform.randomInteger(rAA.countColumns());
        }

        expected = rAA.logical().column(tmpArr).get();

        actual = cAA.logical().column(tmpArr).get();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.logical().column(tmpArr).get();
        TestUtils.assertEquals(expected, actual, ACCURACY);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getCondition()
     */
    @Test
    public void testGetCondition() {

        if (rAA.isFullRank()) {

            // Difficult to test numerically
            // Will only check that they are the same order of magnitude

            long expected = Math.round(PrimitiveMath.LOG10.invoke(rAA.getCondition()));

            long actual = Math.round(PrimitiveMath.LOG10.invoke(p64AA.getCondition()));
            TestUtils.assertEquals(expected, actual);

            actual = Math.round(PrimitiveMath.LOG10.invoke(cAA.getCondition()));
            TestUtils.assertEquals(expected, actual);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getDeterminant()
     */
    @Test
    public void testGetDeterminant() {

        if (rAA.isSquare()) {

            Comparable<?> actual;
            Comparable<?> expected;

            expected = rAA.getDeterminant();

            actual = cAA.getDeterminant();
            TestUtils.assertEquals(expected, actual, ACCURACY);

            actual = p64AA.getDeterminant();
            TestUtils.assertEquals(expected, actual, ACCURACY);
        }
    }

    @Test
    public void testGetEigenvalues() {

        if (rAA.isSquare() && rAA.isHermitian()) {

            List<Eigenpair> expected = rAA.getEigenpairs();
            List<Eigenpair> actual;

            actual = cAA.getEigenpairs();
            for (int i = 0; i < expected.size(); i++) {
                TestUtils.assertEquals("Scalar<?> != Scalar<?>", expected.get(i).value, actual.get(i).value, ACCURACY);
            }

            actual = p64AA.getEigenpairs();
            for (int i = 0; i < expected.size(); i++) {
                TestUtils.assertEquals("Scalar<?> != Scalar<?>", expected.get(i).value, actual.get(i).value, ACCURACY);
            }

            actual = p32AA.getEigenpairs();
            for (int i = 0; i < expected.size(); i++) {
                TestUtils.assertEquals("Scalar<?> != Scalar<?>", expected.get(i).value, actual.get(i).value, ACCURACY);
            }
        }
    }

    @Test
    public void testGetInfinityNorm() {

        double actual;
        double expected;

        expected = BasicMatrix.calculateInfinityNorm(rAA);

        actual = BasicMatrix.calculateInfinityNorm(cAA);
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = BasicMatrix.calculateInfinityNorm(p64AA);
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    @Test
    public void testGetOneNorm() {

        double actual;
        double expected;

        expected = BasicMatrix.calculateOneNorm(rAA);

        actual = BasicMatrix.calculateOneNorm(cAA);
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = BasicMatrix.calculateOneNorm(p64AA);
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = BasicMatrix.calculateOneNorm(p32AA);
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getRank()
     */
    @Test
    public void testGetRank() {

        int expected = rAA.getRank();
        int actual;

        actual = cAA.getRank();
        TestUtils.assertEquals(expected, actual);

        actual = p64AA.getRank();
        TestUtils.assertEquals(expected, actual);

        actual = p32AA.getRank();
        TestUtils.assertEquals(expected, actual);
    }

    @Test
    public void testGetRowsIntArray() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> expected;

        int[] tmpArr = new int[(int) (1 + Uniform.randomInteger(rAA.countRows()))];

        for (int i = 0; i < tmpArr.length; i++) {
            tmpArr[i] = (int) Uniform.randomInteger(rAA.countRows());
        }

        expected = rAA.logical().row(tmpArr).get();

        actual = cAA.logical().row(tmpArr).get();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.logical().row(tmpArr).get();
        TestUtils.assertEquals(expected, actual, ACCURACY);

    }

    @Test
    public void testGetSingularValues() {

        SingularValue<RationalNumber> rationalSVD = SingularValue.RATIONAL.make(rAA);
        rationalSVD.compute(rAA);
        TestUtils.assertEquals(GenericStore.RATIONAL.copy(rAA), rationalSVD, ACCURACY);
        Array1D<Double> expected = rationalSVD.getSingularValues();

        SingularValue<ComplexNumber> complexSVD = SingularValue.COMPLEX.make(cAA);
        complexSVD.compute(cAA);
        TestUtils.assertEquals(GenericStore.COMPLEX.copy(cAA), complexSVD, ACCURACY);
        TestUtils.assertEquals(expected, complexSVD.getSingularValues(), ACCURACY);

        SingularValue<Quaternion> quaternionSVD = SingularValue.QUATERNION.make(qAA);
        quaternionSVD.compute(qAA);
        TestUtils.assertEquals(GenericStore.QUATERNION.copy(qAA), quaternionSVD, ACCURACY);
        TestUtils.assertEquals(expected, quaternionSVD.getSingularValues(), ACCURACY);

        for (SingularValue<Double> primitiveSVD : MatrixDecompositionTests.getPrimitiveSingularValue()) {
            primitiveSVD.compute(p64AA);
            TestUtils.assertEquals(Primitive64Store.FACTORY.copy(p64AA), primitiveSVD, ACCURACY);
            TestUtils.assertEquals(expected, primitiveSVD.getSingularValues(), ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getTrace()
     */
    @Test
    public void testGetTrace() {

        Comparable<?> actual;
        Comparable<?> expected;

        expected = rAA.getTrace();

        actual = cAA.getTrace();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.getTrace();
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    @Test
    public void testLogicalBuilder() {

        BasicMatrix<?, ?> actual;
        RationalMatrix expected;

        expected = rAA.logical().below(rSafe).repeat(1, 2).onAll(RationalMath.SIN).diagonal().get();

        actual = rAA.logical().below(rSafe).repeat(1, 2).diagonal().onAll(RationalMath.SIN).get();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.logical().below(p64Safe).repeat(1, 2).diagonal().onAll(PrimitiveMath.SIN).get();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.logical().onAll(PrimitiveMath.SIN).below(p64Safe.logical().onAll(PrimitiveMath.SIN).get()).repeat(1, 2).diagonal().get();
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#invert()
     */
    @Test
    public void testInvert() {

        if (rAA.isSquare() && rAA.getRank() >= rAA.countColumns()) {

            BasicMatrix<?, ?> expected = rAA.invert();
            BasicMatrix<?, ?> actual;

            actual = cAA.invert();
            TestUtils.assertEquals(expected, actual, ACCURACY);

            actual = p64AA.invert();
            TestUtils.assertEquals(expected, actual, ACCURACY);

            actual = p32AA.invert();
            TestUtils.assertEquals(expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isEmpty()
     */
    @Test
    public void testIsEmpty() {

        boolean expected = rAA.isEmpty();
        boolean actual;

        actual = cAA.isEmpty();
        TestUtils.assertEquals(expected, actual);

        actual = p64AA.isEmpty();
        TestUtils.assertEquals(expected, actual);

        actual = p32AA.isEmpty();
        TestUtils.assertEquals(expected, actual);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isFat()
     */
    @Test
    public void testIsFat() {

        boolean expected = rAA.isFat();
        boolean actual;

        actual = cAA.isFat();
        TestUtils.assertEquals(expected, actual);

        actual = p64AA.isFat();
        TestUtils.assertEquals(expected, actual);

        actual = p32AA.isFat();
        TestUtils.assertEquals(expected, actual);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isFullRank()
     */
    @Test
    public void testIsFullRank() {

        boolean expected = rAA.isFullRank();
        boolean actual;

        actual = cAA.isFullRank();
        TestUtils.assertEquals(expected, actual);

        actual = p64AA.isFullRank();
        TestUtils.assertEquals(expected, actual);

        actual = p32AA.isFullRank();
        TestUtils.assertEquals(expected, actual);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isHermitian()
     */
    @Test
    public void testIsHermitian() {

        boolean expected = rAA.isHermitian();
        boolean actual;

        actual = cAA.isHermitian();
        TestUtils.assertEquals(expected, actual);

        actual = p64AA.isHermitian();
        TestUtils.assertEquals(expected, actual);

        actual = p32AA.isHermitian();
        TestUtils.assertEquals(expected, actual);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isSquare()
     */
    @Test
    public void testIsSquare() {

        boolean expected = rAA.isSquare();
        boolean actual;

        actual = cAA.isSquare();
        TestUtils.assertEquals(expected, actual);

        actual = p64AA.isSquare();
        TestUtils.assertEquals(expected, actual);

        actual = p32AA.isSquare();
        TestUtils.assertEquals(expected, actual);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isSymmetric()
     */
    @Test
    public void testIsSymmetric() {

        boolean expected = rAA.isSymmetric();
        boolean actual;

        actual = cAA.isSymmetric();
        TestUtils.assertEquals(expected, actual);

        actual = p64AA.isSymmetric();
        TestUtils.assertEquals(expected, actual);

        actual = p32AA.isSymmetric();
        TestUtils.assertEquals(expected, actual);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isTall()
     */
    @Test
    public void testIsTall() {

        boolean expected = rAA.isTall();
        boolean actual;

        actual = cAA.isTall();
        TestUtils.assertEquals(expected, actual);

        actual = p64AA.isTall();
        TestUtils.assertEquals(expected, actual);

        actual = p32AA.isTall();
        TestUtils.assertEquals(expected, actual);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isVector()
     */
    @Test
    public void testIsVector() {

        boolean expected = rAA.isVector();
        boolean actual;

        actual = cAA.isVector();
        TestUtils.assertEquals(expected, actual);

        actual = p64AA.isVector();
        TestUtils.assertEquals(expected, actual);

        actual = p32AA.isVector();
        TestUtils.assertEquals(expected, actual);
    }

    @Test
    public void testMergeColumnsBasicMatrix() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> expected;

        expected = rAA.logical().below(rSafe).get();

        actual = p64AA.logical().below(p64Safe).get();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = cAA.logical().below(cSafe).get();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = qAA.logical().below(qSafe).get();
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    @Test
    public void testMergeRowsBasicMatrix() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> expected;

        expected = rAA.logical().right(rSafe).get();

        actual = p64AA.logical().right(p64Safe).get();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = cAA.logical().right(cSafe).get();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = qAA.logical().right(qSafe).get();
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    @Test
    public void testMultiplyElementsBasicMatrix() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> expected;

        RationalMatrix.DenseReceiver copyRational = rAA.copy();
        copyRational.modifyMatching(RationalMath.MULTIPLY, rSafe);
        expected = copyRational.get();

        Primitive64Matrix.DenseReceiver copyPrimitive = p64AA.copy();
        copyPrimitive.modifyMatching(PrimitiveMath.MULTIPLY, p64Safe);
        actual = copyPrimitive.get();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        ComplexMatrix.DenseReceiver copyComplex = cAA.copy();
        copyComplex.modifyMatching(ComplexMath.MULTIPLY, cSafe);
        actual = copyComplex.get();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        QuaternionMatrix.DenseReceiver copyQuaternion = qAA.copy();
        copyQuaternion.modifyMatching(QuaternionMath.MULTIPLY, qSafe);
        actual = copyQuaternion.get();
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#multiply(org.ojalgo.matrix.BasicMatrix)
     */
    @Test
    public void testMultiplyMatrix() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> expected;

        expected = rAA.multiply(rAX);

        actual = cAA.multiply(cAX);
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.multiply(p64AX);
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p32AA.multiply(p32AX);
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#multiply(java.lang.Number)
     */
    @Test
    public void testMultiplyScalar() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> expected;

        expected = rAA.multiply(RationalNumber.valueOf(scalar));

        actual = cAA.multiply(ComplexNumber.valueOf(scalar));
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.multiply(scalar.doubleValue());
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p32AA.multiply(scalar.floatValue());
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#negate()
     */
    @Test
    public void testNegate() {

        BasicMatrix<?, ?> expected = rAA.negate();
        BasicMatrix<?, ?> actual;

        actual = cAA.negate();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.negate();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p32AA.negate();
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#norm()
     */
    @Test
    public void testNorm() {

        double expected = rAA.norm();
        double actual;

        actual = cAA.norm();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.norm();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p32AA.norm();
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    @Test
    public void testReduceRowsAndColumns() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> intermediate;
        BasicMatrix<?, ?> expected;

        for (Aggregator aggregator : Aggregator.values()) {

            if (aggregator == Aggregator.PRODUCT2) {
                continue; // Likely to overflow
            }

            String name = aggregator.name();

            // rows -> cols

            intermediate = rAA.reduceRows(aggregator);
            expected = intermediate.reduceColumns(aggregator);

            TestUtils.assertEquals(1, expected.count());

            intermediate = p32AA.reduceRows(aggregator);
            actual = intermediate.reduceColumns(aggregator);
            TestUtils.assertEquals(name, expected, actual, ACCURACY);

            intermediate = p64AA.reduceRows(aggregator);
            actual = intermediate.reduceColumns(aggregator);
            TestUtils.assertEquals(name, expected, actual, ACCURACY);

            intermediate = cAA.reduceRows(aggregator);
            actual = intermediate.reduceColumns(aggregator);
            TestUtils.assertEquals(name, expected, actual, ACCURACY);

            intermediate = qAA.reduceRows(aggregator);
            actual = intermediate.reduceColumns(aggregator);
            TestUtils.assertEquals(name, expected, actual, ACCURACY);

            // cols -> rows

            intermediate = rAA.reduceColumns(aggregator);
            expected = intermediate.reduceRows(aggregator);

            TestUtils.assertEquals(1, expected.count());

            intermediate = p32AA.reduceColumns(aggregator);
            actual = intermediate.reduceRows(aggregator);
            TestUtils.assertEquals(name, expected, actual, ACCURACY);

            intermediate = p64AA.reduceColumns(aggregator);
            actual = intermediate.reduceRows(aggregator);
            TestUtils.assertEquals(name, expected, actual, ACCURACY);

            intermediate = cAA.reduceColumns(aggregator);
            actual = intermediate.reduceRows(aggregator);
            TestUtils.assertEquals(name, expected, actual, ACCURACY);

            intermediate = qAA.reduceColumns(aggregator);
            actual = intermediate.reduceRows(aggregator);
            TestUtils.assertEquals(name, expected, actual, ACCURACY);
        }
    }

    /**
     * @see BasicMatrix.PhysicalReceiver#set(long, long, Number)
     */
    @Test
    public void testSetElement() {

        long tmpRow = Uniform.randomInteger(rAA.countRows());
        long tmpCol = Uniform.randomInteger(rAA.countColumns());

        RationalMatrix.DenseReceiver rBuilder = rAA.copy();
        rBuilder.set(tmpRow, tmpCol, scalar);
        BasicMatrix<?, ?> expected = rBuilder.build();
        BasicMatrix<?, ?> actual;

        ComplexMatrix.DenseReceiver cBuilder = cAA.copy();
        cBuilder.set(tmpRow, tmpCol, scalar);
        actual = cBuilder.build();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        Primitive64Matrix.DenseReceiver p64Builder = p64AA.copy();
        p64Builder.set(tmpRow, tmpCol, scalar);
        actual = p64Builder.build();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        Primitive32Matrix.DenseReceiver p32Builder = p32AA.copy();
        p32Builder.set(tmpRow, tmpCol, scalar);
        actual = p32Builder.build();
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#solve(org.ojalgo.structure.Access2D)
     */
    @Test
    public void testSolveMatrix() {

        if (rAA.isSquare() && rAA.getRank() >= rAA.countColumns()) {

            BasicMatrix<?, ?> expected = rAA.solve(rAB);
            BasicMatrix<?, ?> actual;

            actual = cAA.solve(cAB);
            TestUtils.assertEquals(expected, actual, ACCURACY);

            actual = p64AA.solve(p64AB);
            TestUtils.assertEquals(expected, actual, ACCURACY);

            actual = p32AA.solve(p64AB);
            TestUtils.assertEquals(expected, actual, ACCURACY);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#subtract(BasicMatrix)
     */
    @Test
    public void testSubtractMatrix() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> expected;

        expected = rAA.subtract(rSafe);

        actual = cAA.subtract(cSafe);
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.subtract(p64Safe);
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p32AA.subtract(p32Safe);
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#subtract(Comparable)
     */
    @Test
    public void testSubtractScalar() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> expected;

        expected = rAA.subtract(RationalNumber.valueOf(scalar));

        actual = cAA.subtract(ComplexNumber.valueOf(scalar));
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.subtract(scalar.doubleValue());
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p32AA.subtract(scalar.floatValue());
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    @Test
    public void testToComplexNumberIntInt() {

        Comparable<?> actual;
        Comparable<?> expected;

        int tmpRow = (int) Uniform.randomInteger(rAA.countRows());
        int tmpCol = (int) Uniform.randomInteger(rAA.countColumns());

        expected = ComplexNumber.valueOf(rAA.get(tmpRow, tmpCol));

        actual = ComplexNumber.valueOf(cAA.get(tmpRow, tmpCol));
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = ComplexNumber.valueOf(p64AA.get(tmpRow, tmpCol));
        TestUtils.assertEquals(expected, actual, ACCURACY);

    }

    @Test
    public void testToComplexStore() {

        PhysicalStore<ComplexNumber> tmpExpStore = GenericStore.COMPLEX.copy(rAA);
        PhysicalStore<ComplexNumber> tmpActStore;

        tmpActStore = GenericStore.COMPLEX.copy(cAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, ACCURACY);

        tmpActStore = GenericStore.COMPLEX.copy(p64AA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, ACCURACY);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#columns()
     */
    @Test
    public void testToListOfColumns() {

        Iterable<ColumnView<RationalNumber>> tmpColumns = rAA.columns();

        for (ColumnView<RationalNumber> tmpColumnView : tmpColumns) {
            long j = tmpColumnView.column();
            for (long i = 0L; i < tmpColumnView.count(); i++) {
                TestUtils.assertEquals(tmpColumnView.get(i), cAA.get(i, j), ACCURACY);
                TestUtils.assertEquals(tmpColumnView.get(i), p64AA.get(i, j), ACCURACY);
            }
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#rows()
     */
    @Test
    public void testToListOfRows() {

        Iterable<RowView<RationalNumber>> tmpRows = rAA.rows();

        for (RowView<RationalNumber> tmpRowView : tmpRows) {
            long i = tmpRowView.row();
            for (long j = 0L; j < tmpRowView.count(); j++) {
                TestUtils.assertEquals(tmpRowView.get(j), cAA.get(i, j), ACCURACY);
                TestUtils.assertEquals(tmpRowView.get(j), p64AA.get(i, j), ACCURACY);
            }
        }
    }

    @Test
    public void testToPrimitiveStore() {

        PhysicalStore<Double> tmpExpStore = Primitive64Store.FACTORY.copy(rAA);
        PhysicalStore<Double> tmpActStore;

        tmpActStore = Primitive64Store.FACTORY.copy(cAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, ACCURACY);

        tmpActStore = Primitive64Store.FACTORY.copy(p64AA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, ACCURACY);

    }

    @Test
    public void testToRationalStore() {

        PhysicalStore<RationalNumber> tmpExpStore = GenericStore.RATIONAL.copy(rAA);
        PhysicalStore<RationalNumber> tmpActStore;

        tmpActStore = GenericStore.RATIONAL.copy(cAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, ACCURACY);

        tmpActStore = GenericStore.RATIONAL.copy(p64AA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, ACCURACY);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toRawCopy1D()
     */
    @Test
    public void testToRawCopy1D() {

        double[] tmpExpStore = rAA.toRawCopy1D();
        double[] tmpActStore;

        int tmpFirstIndex = 0;
        int tmpLastIndex = (int) (rAA.count() - 1);

        tmpActStore = cAA.toRawCopy1D();
        TestUtils.assertEquals(tmpExpStore[tmpFirstIndex], tmpActStore[tmpFirstIndex], ACCURACY);
        TestUtils.assertEquals(tmpExpStore[tmpLastIndex], tmpActStore[tmpLastIndex], ACCURACY);
        if (rAA.isVector()) {
            for (int i = 0; i < tmpExpStore.length; i++) {
                TestUtils.assertEquals(tmpExpStore[i], tmpActStore[i], ACCURACY);
            }
        }

        tmpActStore = p64AA.toRawCopy1D();
        TestUtils.assertEquals(tmpExpStore[tmpFirstIndex], tmpActStore[tmpFirstIndex], ACCURACY);
        TestUtils.assertEquals(tmpExpStore[tmpLastIndex], tmpActStore[tmpLastIndex], ACCURACY);
        if (rAA.isVector()) {
            for (int i = 0; i < tmpExpStore.length; i++) {
                TestUtils.assertEquals(tmpExpStore[i], tmpActStore[i], ACCURACY);
            }
        }

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toScalar(long, long)
     */
    @Test
    public void testToScalarIntInt() {

        long tmpRow = Uniform.randomInteger(rAA.countRows());
        long tmpCol = Uniform.randomInteger(rAA.countColumns());

        Scalar<?> expected = rAA.toScalar(tmpRow, tmpCol);
        Scalar<?> actual;

        actual = cAA.toScalar(tmpRow, tmpCol);
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.toScalar(tmpRow, tmpCol);
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p32AA.toScalar(tmpRow, tmpCol);
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#transpose()
     */
    @Test
    public void testTranspose() {

        BasicMatrix<?, ?> actual;
        BasicMatrix<?, ?> expected;

        expected = rAA.transpose();

        actual = cAA.transpose();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p64AA.transpose();
        TestUtils.assertEquals(expected, actual, ACCURACY);

        actual = p32AA.transpose();
        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

}
