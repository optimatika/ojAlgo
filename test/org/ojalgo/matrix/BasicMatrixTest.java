/*
 * Copyright 1997-2018 Optimatika
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
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.ComplexFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.QuaternionFunction;
import org.ojalgo.function.RationalFunction;
import org.ojalgo.matrix.BasicMatrix.PhysicalBuilder;
import org.ojalgo.matrix.decomposition.Eigenvalue.Eigenpair;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.ColumnView;
import org.ojalgo.structure.RowView;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public abstract class BasicMatrixTest extends MatrixTests {

    public static RationalMatrix getIdentity(final long rows, final long columns, final NumberContext context) {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.makeEye(Math.toIntExact(rows), Math.toIntExact(columns));
        return tmpMtrx.enforce(context);
    }

    public static RationalMatrix getSafe(final long rows, final long columns, final NumberContext context) {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.makeFilled(rows, columns, new Uniform(PrimitiveMath.E, PrimitiveMath.PI));
        return tmpMtrx.enforce(context);
    }

    protected NumberContext evaluation = NumberContext.getGeneral(9);

    boolean actBoolean;
    int actInt;
    BasicMatrix<?, ?> actMtrx;
    Number actNumber;
    Scalar<?> actScalar;
    double actValue;
    BigDecimal bigNumber;
    ComplexMatrix complexAA;
    ComplexMatrix complexAB;
    ComplexMatrix complexAX;
    ComplexMatrix complexI;
    ComplexMatrix complexSafe;
    boolean expBoolean;
    int expInt;
    BasicMatrix<?, ?> expMtrx;
    Number expNumber;
    Scalar<?> expScalar;
    double expValue;
    PrimitiveMatrix primitiveAA;
    PrimitiveMatrix primitiveAB;
    PrimitiveMatrix primitiveAX;
    PrimitiveMatrix primitiveI;
    PrimitiveMatrix primitiveSafe;
    QuaternionMatrix quaternionAA;
    QuaternionMatrix quaternionAB;
    QuaternionMatrix quaternionAX;
    QuaternionMatrix quaternionI;
    QuaternionMatrix quaternionSafe;
    RationalMatrix rationalAA;
    RationalMatrix rationalAB;
    RationalMatrix rationalAX;
    RationalMatrix rationalSafe;
    RationalMatrix rationlI;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @BeforeEach
    public void setUp() {

        TestUtils.minimiseAllBranchLimits();

        primitiveAA = PrimitiveMatrix.FACTORY.copy(rationalAA);
        primitiveAX = PrimitiveMatrix.FACTORY.copy(rationalAX);
        primitiveAB = PrimitiveMatrix.FACTORY.copy(rationalAB);
        primitiveI = PrimitiveMatrix.FACTORY.copy(rationlI);
        primitiveSafe = PrimitiveMatrix.FACTORY.copy(rationalSafe);

        complexAA = ComplexMatrix.FACTORY.copy(rationalAA);
        complexAX = ComplexMatrix.FACTORY.copy(rationalAX);
        complexAB = ComplexMatrix.FACTORY.copy(rationalAB);
        complexI = ComplexMatrix.FACTORY.copy(rationlI);
        complexSafe = ComplexMatrix.FACTORY.copy(rationalSafe);

        quaternionAA = QuaternionMatrix.FACTORY.copy(rationalAA);
        quaternionAX = QuaternionMatrix.FACTORY.copy(rationalAX);
        quaternionAB = QuaternionMatrix.FACTORY.copy(rationalAB);
        quaternionI = QuaternionMatrix.FACTORY.copy(rationlI);
        quaternionSafe = QuaternionMatrix.FACTORY.copy(rationalSafe);

        bigNumber = new BigDecimal(Math.random());
    }

    @AfterEach
    public void tearDown() {
        evaluation = NumberContext.getGeneral(9);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#add(org.ojalgo.matrix.BasicMatrix)
     */
    @Test
    public void testAddBasicMatrix() {

        expMtrx = rationalAA.add(rationalSafe);

        actMtrx = complexAA.add(complexSafe);
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = primitiveAA.add(primitiveSafe);
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

    }

    /**
     * @see BasicMatrix.PhysicalBuilder#add(long, long, Number)
     */
    @Test
    public void testAddIntIntNumber() {

        final int tmpRow = Uniform.randomInteger((int) rationalAA.countRows());
        final int tmpCol = Uniform.randomInteger((int) rationalAA.countColumns());

        final BasicMatrix.PhysicalBuilder<RationalNumber, RationalMatrix> tmpBigBuilder = rationalAA.copy();
        tmpBigBuilder.add(tmpRow, tmpCol, bigNumber);
        expMtrx = tmpBigBuilder.build();

        final BasicMatrix.PhysicalBuilder<ComplexNumber, ComplexMatrix> tmpComplexBuilder = complexAA.copy();
        tmpComplexBuilder.add(tmpRow, tmpCol, bigNumber);
        actMtrx = tmpComplexBuilder.build();

        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        final BasicMatrix.PhysicalBuilder<Double, PrimitiveMatrix> tmpPrimitiveBuilder = primitiveAA.copy();
        tmpPrimitiveBuilder.add(tmpRow, tmpCol, bigNumber);
        actMtrx = tmpPrimitiveBuilder.build();

        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#add(java.lang.Number)
     */
    @Test
    public void testAddNumber() {

        expMtrx = rationalAA.add(bigNumber);

        actMtrx = complexAA.add(bigNumber);
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = primitiveAA.add(bigNumber);
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#conjugate()
     */
    @Test
    public void testConjugate() {

        expMtrx = rationalAA.conjugate();

        actMtrx = complexAA.conjugate();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = primitiveAA.conjugate();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

    }

    @Test
    public void testDivideElementsBasicMatrix() {

        PhysicalBuilder<RationalNumber, RationalMatrix> copyRational = rationalAA.copy();
        copyRational.modifyMatching(RationalFunction.DIVIDE, rationalSafe);
        expMtrx = copyRational.get();

        PhysicalBuilder<Double, PrimitiveMatrix> copyPrimitive = primitiveAA.copy();
        copyPrimitive.modifyMatching(PrimitiveFunction.DIVIDE, primitiveSafe);
        actMtrx = copyPrimitive.get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        PhysicalBuilder<ComplexNumber, ComplexMatrix> copyComplex = complexAA.copy();
        copyComplex.modifyMatching(ComplexFunction.DIVIDE, complexSafe);
        actMtrx = copyComplex.get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        PhysicalBuilder<Quaternion, QuaternionMatrix> copyQuaternion = quaternionAA.copy();
        copyQuaternion.modifyMatching(QuaternionFunction.DIVIDE, quaternionSafe);
        actMtrx = copyQuaternion.get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#divide(java.lang.Number)
     */
    @Test
    public void testDivideNumber() {

        expMtrx = rationalAA.divide(bigNumber);

        actMtrx = complexAA.divide(bigNumber);
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = primitiveAA.divide(bigNumber);
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

    }

    @Test
    public void testDotAccess1D() {

        final int[] tmpCol = new int[] { (int) Uniform.randomInteger(rationalAA.countColumns()) };

        expNumber = rationalAA.logical().column(tmpCol).get().dot(rationalSafe.logical().column(tmpCol).get());

        actNumber = complexAA.logical().column(tmpCol).get().dot(complexSafe.logical().column(tmpCol).get());
        TestUtils.assertEquals(expNumber, actNumber, evaluation);

        actNumber = primitiveAA.logical().column(tmpCol).get().dot(primitiveSafe.logical().column(tmpCol).get());
        TestUtils.assertEquals(expNumber, actNumber, evaluation);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#doubleValue(long, long)
     */
    @Test
    public void testDoubleValueIntInt() {

        final int tmpRow = (int) Uniform.randomInteger(rationalAA.countRows());
        final int tmpCol = (int) Uniform.randomInteger(rationalAA.countColumns());

        expNumber = rationalAA.doubleValue(tmpRow, tmpCol);

        actNumber = complexAA.doubleValue(tmpRow, tmpCol);
        TestUtils.assertEquals(expNumber, actNumber, evaluation);

        actNumber = primitiveAA.doubleValue(tmpRow, tmpCol);
        TestUtils.assertEquals(expNumber, actNumber, evaluation);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#countColumns()
     */
    @Test
    public void testGetColDim() {

        expInt = (int) rationalAA.countColumns();

        actInt = (int) complexAA.countColumns();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actInt = (int) primitiveAA.countColumns();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#selectColumns(int[])
     */
    @Test
    public void testGetColumnsIntArray() {

        final int[] tmpArr = new int[(int) (1 + Uniform.randomInteger(rationalAA.countColumns()))];

        for (int i = 0; i < tmpArr.length; i++) {
            tmpArr[i] = (int) Uniform.randomInteger(rationalAA.countColumns());
        }

        expMtrx = rationalAA.logical().column(tmpArr).get();

        actMtrx = complexAA.logical().column(tmpArr).get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = primitiveAA.logical().column(tmpArr).get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getCondition()
     */
    @Test
    public void testGetCondition() {

        if (rationalAA.isFullRank()) {

            // Difficult to test numerically
            // Will only check that they are the same order of magnitude

            final int tmpExpCondMag = (int) Math.round(PrimitiveFunction.LOG10.invoke(rationalAA.getCondition().doubleValue()));

            int tmpActCondMag = (int) Math.round(PrimitiveFunction.LOG10.invoke(primitiveAA.getCondition().doubleValue()));
            TestUtils.assertEquals(tmpExpCondMag, tmpActCondMag);

            tmpActCondMag = (int) Math.round(PrimitiveFunction.LOG10.invoke(complexAA.getCondition().doubleValue()));
            TestUtils.assertEquals(tmpExpCondMag, tmpActCondMag);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getDeterminant()
     */
    @Test
    public void testGetDeterminant() {

        if (rationalAA.isSquare()) {

            expNumber = rationalAA.getDeterminant().get();

            actNumber = complexAA.getDeterminant().get();
            TestUtils.assertEquals(expNumber, actNumber, evaluation);

            actNumber = primitiveAA.getDeterminant().get();
            TestUtils.assertEquals(expNumber, actNumber, evaluation);

        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getEigenvalues()
     */
    @Test
    public void testGetEigenvalues() {

        if (rationalAA.isSquare() && MatrixUtils.isHermitian(rationalAA)) {

            final List<Eigenpair> expected = primitiveAA.getEigenpairs();
            List<Eigenpair> actual;

            actual = rationalAA.getEigenpairs();
            for (int i = 0; i < expected.size(); i++) {
                TestUtils.assertEquals("Scalar<?> != Scalar<?>", expected.get(i).value, actual.get(i).value, evaluation);
            }

            actual = complexAA.getEigenpairs();
            for (int i = 0; i < expected.size(); i++) {
                TestUtils.assertEquals("Scalar<?> != Scalar<?>", expected.get(i).value, actual.get(i).value, evaluation);
            }
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getInfinityNorm()
     */
    @Test
    public void testGetInfinityNorm() {

        expValue = BasicMatrix.calculateInfinityNorm(rationalAA);

        actValue = BasicMatrix.calculateInfinityNorm(complexAA);
        TestUtils.assertEquals(expValue, actValue, evaluation);

        actValue = BasicMatrix.calculateInfinityNorm(primitiveAA);
        TestUtils.assertEquals(expValue, actValue, evaluation);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getOneNorm()
     */
    @Test
    public void testGetOneNorm() {

        expValue = BasicMatrix.calculateOneNorm(rationalAA);

        actValue = BasicMatrix.calculateOneNorm(complexAA);
        TestUtils.assertEquals(expValue, actValue, evaluation);

        actValue = BasicMatrix.calculateOneNorm(primitiveAA);
        TestUtils.assertEquals(expValue, actValue, evaluation);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getRank()
     */
    @Test
    public void testGetRank() {

        expInt = rationalAA.getRank();

        actInt = complexAA.getRank();
        TestUtils.assertEquals(expInt, actInt);

        actInt = primitiveAA.getRank();
        TestUtils.assertEquals(expInt, actInt);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#countRows()
     */
    @Test
    public void testGetRowDim() {

        expInt = (int) rationalAA.countRows();

        actInt = (int) complexAA.countRows();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actInt = (int) primitiveAA.countRows();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#selectRows(int[])
     */
    @Test
    public void testGetRowsIntArray() {

        final int[] tmpArr = new int[(int) (1 + Uniform.randomInteger(rationalAA.countRows()))];

        for (int i = 0; i < tmpArr.length; i++) {
            tmpArr[i] = (int) Uniform.randomInteger(rationalAA.countRows());
        }

        expMtrx = rationalAA.logical().row(tmpArr).get();

        actMtrx = complexAA.logical().row(tmpArr).get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = primitiveAA.logical().row(tmpArr).get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getSingularValues()
     */
    @Test
    public void testGetSingularValues() {

        final List<? extends Number> tmpExpStore = rationalAA.getSingularValues();
        if (MatrixTests.DEBUG) {
            BasicLogger.debug("Big SVs: {}", tmpExpStore);
        }
        List<? extends Number> tmpActStore;

        tmpActStore = primitiveAA.getSingularValues();
        if (MatrixTests.DEBUG) {
            BasicLogger.debug("Primitive SVs: {}", tmpActStore);
        }
        for (int i = 0; i < tmpExpStore.size(); i++) {
            TestUtils.assertEquals(tmpExpStore.get(i), tmpActStore.get(i), evaluation);
        }

        tmpActStore = complexAA.getSingularValues();
        if (MatrixTests.DEBUG) {
            BasicLogger.debug("Complex SVs: {}", tmpActStore);
        }
        for (int i = 0; i < tmpExpStore.size(); i++) {
            TestUtils.assertEquals(tmpExpStore.get(i), tmpActStore.get(i), evaluation);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getTrace()
     */
    @Test
    public void testGetTrace() {

        expNumber = rationalAA.getTrace().get();

        actNumber = complexAA.getTrace().get();
        TestUtils.assertEquals(expNumber, actNumber, evaluation);

        actNumber = primitiveAA.getTrace().get();
        TestUtils.assertEquals(expNumber, actNumber, evaluation);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#invert()
     */
    @Test
    public void testInvert() {

        if (rationalAA.isSquare() && (rationalAA.getRank() >= rationalAA.countColumns())) {

            expMtrx = rationalAA.invert();

            actMtrx = complexAA.invert();
            TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

            actMtrx = primitiveAA.invert();
            TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isEmpty()
     */
    @Test
    public void testIsEmpty() {

        expBoolean = rationalAA.isEmpty();

        actBoolean = complexAA.isEmpty();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actBoolean = primitiveAA.isEmpty();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isFat()
     */
    @Test
    public void testIsFat() {

        expBoolean = rationalAA.isFat();

        actBoolean = complexAA.isFat();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actBoolean = primitiveAA.isFat();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isFullRank()
     */
    @Test
    public void testIsFullRank() {

        expBoolean = rationalAA.isFullRank();

        actBoolean = complexAA.isFullRank();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actBoolean = primitiveAA.isFullRank();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isHermitian()
     */
    @Test
    public void testIsHermitian() {

        expBoolean = rationalAA.isHermitian();

        actBoolean = complexAA.isHermitian();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actBoolean = primitiveAA.isHermitian();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isSquare()
     */
    @Test
    public void testIsSquare() {

        expBoolean = rationalAA.isSquare();

        actBoolean = complexAA.isSquare();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actBoolean = primitiveAA.isSquare();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isSymmetric()
     */
    @Test
    public void testIsSymmetric() {

        expBoolean = rationalAA.isSymmetric();

        actBoolean = complexAA.isSymmetric();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actBoolean = primitiveAA.isSymmetric();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isTall()
     */
    @Test
    public void testIsTall() {

        expBoolean = rationalAA.isTall();

        actBoolean = complexAA.isTall();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actBoolean = primitiveAA.isTall();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isVector()
     */
    @Test
    public void testIsVector() {

        expBoolean = rationalAA.isVector();

        actBoolean = complexAA.isVector();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actBoolean = primitiveAA.isVector();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    @Test
    public void testMergeColumnsBasicMatrix() {

        expMtrx = rationalAA.logical().below(rationalSafe).get();

        actMtrx = primitiveAA.logical().below(primitiveSafe).get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = complexAA.logical().below(complexSafe).get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = quaternionAA.logical().below(quaternionSafe).get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);
    }

    @Test
    public void testMergeRowsBasicMatrix() {

        expMtrx = rationalAA.logical().right(rationalSafe).get();

        actMtrx = primitiveAA.logical().right(primitiveSafe).get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = complexAA.logical().right(complexSafe).get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = quaternionAA.logical().right(quaternionSafe).get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#multiply(org.ojalgo.matrix.BasicMatrix)
     */
    @Test
    public void testMultiplyBasicMatrix() {

        expMtrx = rationalAA.multiply(rationalAX);

        actMtrx = complexAA.multiply(complexAX);
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = primitiveAA.multiply(primitiveAX);
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

    }

    @Test
    public void testMultiplyElementsBasicMatrix() {

        PhysicalBuilder<RationalNumber, RationalMatrix> copyRational = rationalAA.copy();
        copyRational.modifyMatching(RationalFunction.MULTIPLY, rationalSafe);
        expMtrx = copyRational.get();

        PhysicalBuilder<Double, PrimitiveMatrix> copyPrimitive = primitiveAA.copy();
        copyPrimitive.modifyMatching(PrimitiveFunction.MULTIPLY, primitiveSafe);
        actMtrx = copyPrimitive.get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        PhysicalBuilder<ComplexNumber, ComplexMatrix> copyComplex = complexAA.copy();
        copyComplex.modifyMatching(ComplexFunction.MULTIPLY, complexSafe);
        actMtrx = copyComplex.get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        PhysicalBuilder<Quaternion, QuaternionMatrix> copyQuaternion = quaternionAA.copy();
        copyQuaternion.modifyMatching(QuaternionFunction.MULTIPLY, quaternionSafe);
        actMtrx = copyQuaternion.get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#multiply(java.lang.Number)
     */
    @Test
    public void testMultiplyNumber() {

        expMtrx = rationalAA.multiply(bigNumber);

        actMtrx = complexAA.multiply(bigNumber);
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = primitiveAA.multiply(bigNumber);
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#negate()
     */
    @Test
    public void testNegate() {

        expMtrx = rationalAA.negate();

        actMtrx = complexAA.negate();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = primitiveAA.negate();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#norm()
     */
    @Test
    public void testNorm() {

        expValue = rationalAA.norm();

        actValue = complexAA.norm();
        TestUtils.assertEquals(expValue, actValue, evaluation);

        actValue = primitiveAA.norm();
        TestUtils.assertEquals(expValue, actValue, evaluation);

    }

    /**
     * @see BasicMatrix.PhysicalBuilder#set(long, long, Number)
     */
    @Test
    public void testSetIntIntNumber() {

        final int tmpRow = Uniform.randomInteger((int) rationalAA.countRows());
        final int tmpCol = Uniform.randomInteger((int) rationalAA.countColumns());

        final BasicMatrix.PhysicalBuilder<RationalNumber, RationalMatrix> tmpBigBuilder = rationalAA.copy();
        tmpBigBuilder.set(tmpRow, tmpCol, bigNumber);
        expMtrx = tmpBigBuilder.build();

        final BasicMatrix.PhysicalBuilder<ComplexNumber, ComplexMatrix> tmpComplexBuilder = complexAA.copy();
        tmpComplexBuilder.set(tmpRow, tmpCol, bigNumber);
        actMtrx = tmpComplexBuilder.build();

        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        final BasicMatrix.PhysicalBuilder<Double, PrimitiveMatrix> tmpPrimitiveBuilder = primitiveAA.copy();
        tmpPrimitiveBuilder.set(tmpRow, tmpCol, bigNumber);
        actMtrx = tmpPrimitiveBuilder.build();

        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);
    }

    @Test
    public void testSize() {

        expInt = (int) rationalAA.count();

        actInt = (int) complexAA.count();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actInt = (int) primitiveAA.count();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#solve(org.ojalgo.structure.Access2D)
     */
    @Test
    public void testSolveBasicMatrix() {

        if (rationalAA.isSquare() && (rationalAA.getRank() >= rationalAA.countColumns())) {

            expMtrx = rationalAA.solve(rationalAB);

            actMtrx = complexAA.solve(complexAB);
            TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

            actMtrx = primitiveAA.solve(primitiveAB);
            TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#subtract(org.ojalgo.matrix.BasicMatrix)
     */
    @Test
    public void testSubtractBasicMatrix() {

        expMtrx = rationalAA.subtract(rationalSafe);

        actMtrx = complexAA.subtract(complexSafe);
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = primitiveAA.subtract(primitiveSafe);
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#subtract(java.lang.Number)
     */
    @Test
    public void testSubtractNumber() {

        expMtrx = rationalAA.subtract(bigNumber);

        actMtrx = complexAA.subtract(bigNumber);
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = primitiveAA.subtract(bigNumber);
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toBigDecimal(int, int)
     */
    @Test
    public void testToBigDecimalIntInt() {

        final int tmpRow = (int) Uniform.randomInteger(rationalAA.countRows());
        final int tmpCol = (int) Uniform.randomInteger(rationalAA.countColumns());

        expNumber = TypeUtils.toBigDecimal(rationalAA.get(tmpRow, tmpCol));

        actNumber = TypeUtils.toBigDecimal(complexAA.get(tmpRow, tmpCol));
        TestUtils.assertEquals(expNumber, actNumber, evaluation);

        actNumber = TypeUtils.toBigDecimal(primitiveAA.get(tmpRow, tmpCol));
        TestUtils.assertEquals(expNumber, actNumber, evaluation);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toComplexNumber(int, int)
     */
    @Test
    public void testToComplexNumberIntInt() {

        final int tmpRow = (int) Uniform.randomInteger(rationalAA.countRows());
        final int tmpCol = (int) Uniform.randomInteger(rationalAA.countColumns());

        expNumber = ComplexNumber.valueOf(rationalAA.get(tmpRow, tmpCol));

        actNumber = ComplexNumber.valueOf(complexAA.get(tmpRow, tmpCol));
        TestUtils.assertEquals(expNumber, actNumber, evaluation);

        actNumber = ComplexNumber.valueOf(primitiveAA.get(tmpRow, tmpCol));
        TestUtils.assertEquals(expNumber, actNumber, evaluation);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toComplexStore()
     */
    @Test
    public void testToComplexStore() {

        final PhysicalStore<ComplexNumber> tmpExpStore = GenericDenseStore.COMPLEX.copy(rationalAA);
        PhysicalStore<ComplexNumber> tmpActStore;

        tmpActStore = GenericDenseStore.COMPLEX.copy(complexAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, evaluation);

        tmpActStore = GenericDenseStore.COMPLEX.copy(primitiveAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, evaluation);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#columns()
     */
    @Test
    public void testToListOfColumns() {

        final Iterable<ColumnView<RationalNumber>> tmpColumns = rationalAA.columns();

        for (final ColumnView<RationalNumber> tmpColumnView : tmpColumns) {
            final long j = tmpColumnView.column();
            for (long i = 0L; i < tmpColumnView.count(); i++) {
                TestUtils.assertEquals(tmpColumnView.get(i), complexAA.get(i, j), evaluation);
                TestUtils.assertEquals(tmpColumnView.get(i), primitiveAA.get(i, j), evaluation);
            }
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#rows()
     */
    @Test
    public void testToListOfRows() {

        final Iterable<RowView<RationalNumber>> tmpRows = rationalAA.rows();

        for (final RowView<RationalNumber> tmpRowView : tmpRows) {
            final long i = tmpRowView.row();
            for (long j = 0L; j < tmpRowView.count(); j++) {
                TestUtils.assertEquals(tmpRowView.get(j), complexAA.get(i, j), evaluation);
                TestUtils.assertEquals(tmpRowView.get(j), primitiveAA.get(i, j), evaluation);
            }
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toPrimitiveStore()
     */
    @Test
    public void testToPrimitiveStore() {

        final PhysicalStore<Double> tmpExpStore = PrimitiveDenseStore.FACTORY.copy(rationalAA);
        PhysicalStore<Double> tmpActStore;

        tmpActStore = PrimitiveDenseStore.FACTORY.copy(complexAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, evaluation);

        tmpActStore = PrimitiveDenseStore.FACTORY.copy(primitiveAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, evaluation);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toBigStore()
     */
    @Test
    public void testToRationalStore() {

        final PhysicalStore<RationalNumber> tmpExpStore = GenericDenseStore.RATIONAL.copy(rationalAA);
        PhysicalStore<RationalNumber> tmpActStore;

        tmpActStore = GenericDenseStore.RATIONAL.copy(complexAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, evaluation);

        tmpActStore = GenericDenseStore.RATIONAL.copy(primitiveAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, evaluation);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toRawCopy1D()
     */
    @Test
    public void testToRawCopy1D() {

        final double[] tmpExpStore = rationalAA.toRawCopy1D();
        double[] tmpActStore;

        final int tmpFirstIndex = 0;
        final int tmpLastIndex = (int) (rationalAA.count() - 1);

        tmpActStore = complexAA.toRawCopy1D();
        TestUtils.assertEquals(tmpExpStore[tmpFirstIndex], tmpActStore[tmpFirstIndex], evaluation);
        TestUtils.assertEquals(tmpExpStore[tmpLastIndex], tmpActStore[tmpLastIndex], evaluation);
        if (rationalAA.isVector()) {
            for (int i = 0; i < tmpExpStore.length; i++) {
                TestUtils.assertEquals(tmpExpStore[i], tmpActStore[i], evaluation);
            }
        }

        tmpActStore = primitiveAA.toRawCopy1D();
        TestUtils.assertEquals(tmpExpStore[tmpFirstIndex], tmpActStore[tmpFirstIndex], evaluation);
        TestUtils.assertEquals(tmpExpStore[tmpLastIndex], tmpActStore[tmpLastIndex], evaluation);
        if (rationalAA.isVector()) {
            for (int i = 0; i < tmpExpStore.length; i++) {
                TestUtils.assertEquals(tmpExpStore[i], tmpActStore[i], evaluation);
            }
        }

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toScalar(long, long)
     */
    @Test
    public void testToScalarIntInt() {

        final int tmpRow = Uniform.randomInteger((int) rationalAA.countRows());
        final int tmpCol = Uniform.randomInteger((int) rationalAA.countColumns());

        expNumber = rationalAA.toScalar(tmpRow, tmpCol).get();

        actNumber = complexAA.toScalar(tmpRow, tmpCol).get();
        TestUtils.assertEquals(expNumber, actNumber, evaluation);

        actNumber = primitiveAA.toScalar(tmpRow, tmpCol).get();
        TestUtils.assertEquals(expNumber, actNumber, evaluation);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#transpose()
     */
    @Test
    public void testTranspose() {

        expMtrx = rationalAA.transpose();

        actMtrx = complexAA.transpose();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = primitiveAA.transpose();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

    }

}
