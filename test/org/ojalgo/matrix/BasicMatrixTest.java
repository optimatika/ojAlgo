/*
 * Copyright 1997-2019 Optimatika
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
import org.ojalgo.function.constant.ComplexMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.constant.QuaternionMath;
import org.ojalgo.function.constant.RationalMath;
import org.ojalgo.matrix.BasicMatrix.PhysicalReceiver;
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
    Comparable<?> actNumber;
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
    Comparable<?> expNumber;
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
     * @see BasicMatrix.PhysicalReceiver#add(long, long, Number)
     */
    @Test
    public void testAddIntIntNumber() {

        final int tmpRow = Uniform.randomInteger((int) rationalAA.countRows());
        final int tmpCol = Uniform.randomInteger((int) rationalAA.countColumns());

        final BasicMatrix.PhysicalReceiver<RationalNumber, RationalMatrix> tmpBigBuilder = rationalAA.copy();
        tmpBigBuilder.add(tmpRow, tmpCol, bigNumber);
        expMtrx = tmpBigBuilder.build();

        final BasicMatrix.PhysicalReceiver<ComplexNumber, ComplexMatrix> tmpComplexBuilder = complexAA.copy();
        tmpComplexBuilder.add(tmpRow, tmpCol, bigNumber);
        actMtrx = tmpComplexBuilder.build();

        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        final BasicMatrix.PhysicalReceiver<Double, PrimitiveMatrix> tmpPrimitiveBuilder = primitiveAA.copy();
        tmpPrimitiveBuilder.add(tmpRow, tmpCol, bigNumber);
        actMtrx = tmpPrimitiveBuilder.build();

        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#add(java.lang.Number)
     */
    @Test
    public void testAddNumber() {

        expMtrx = rationalAA.add(RationalNumber.valueOf(bigNumber));

        actMtrx = complexAA.add(ComplexNumber.valueOf(bigNumber));
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = primitiveAA.add(bigNumber.doubleValue());
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

        PhysicalReceiver<RationalNumber, RationalMatrix> copyRational = rationalAA.copy();
        copyRational.modifyMatching(RationalMath.DIVIDE, rationalSafe);
        expMtrx = copyRational.get();

        PhysicalReceiver<Double, PrimitiveMatrix> copyPrimitive = primitiveAA.copy();
        copyPrimitive.modifyMatching(PrimitiveMath.DIVIDE, primitiveSafe);
        actMtrx = copyPrimitive.get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        PhysicalReceiver<ComplexNumber, ComplexMatrix> copyComplex = complexAA.copy();
        copyComplex.modifyMatching(ComplexMath.DIVIDE, complexSafe);
        actMtrx = copyComplex.get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        PhysicalReceiver<Quaternion, QuaternionMatrix> copyQuaternion = quaternionAA.copy();
        copyQuaternion.modifyMatching(QuaternionMath.DIVIDE, quaternionSafe);
        actMtrx = copyQuaternion.get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#divide(java.lang.Number)
     */
    @Test
    public void testDivideNumber() {

        expMtrx = rationalAA.divide(RationalNumber.valueOf(bigNumber));

        actMtrx = complexAA.divide(ComplexNumber.valueOf(bigNumber));
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = primitiveAA.divide(bigNumber.doubleValue());
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

            final int tmpExpCondMag = (int) Math.round(PrimitiveMath.LOG10.invoke(rationalAA.getCondition().doubleValue()));

            int tmpActCondMag = (int) Math.round(PrimitiveMath.LOG10.invoke(primitiveAA.getCondition().doubleValue()));
            TestUtils.assertEquals(tmpExpCondMag, tmpActCondMag);

            tmpActCondMag = (int) Math.round(PrimitiveMath.LOG10.invoke(complexAA.getCondition().doubleValue()));
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

    @Test
    public void testGetInfinityNorm() {

        expValue = BasicMatrix.calculateInfinityNorm(rationalAA);

        actValue = BasicMatrix.calculateInfinityNorm(complexAA);
        TestUtils.assertEquals(expValue, actValue, evaluation);

        actValue = BasicMatrix.calculateInfinityNorm(primitiveAA);
        TestUtils.assertEquals(expValue, actValue, evaluation);
    }

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

    @Test
    public void testGetSingularValues() {

        SingularValue<RationalNumber> rationalSVD = SingularValue.RATIONAL.make(rationalAA);
        rationalSVD.compute(rationalAA);
        TestUtils.assertEquals(GenericStore.RATIONAL.copy(rationalAA), rationalSVD, evaluation);
        Array1D<Double> expected = rationalSVD.getSingularValues();

        SingularValue<ComplexNumber> complexSVD = SingularValue.COMPLEX.make(complexAA);
        complexSVD.compute(complexAA);
        TestUtils.assertEquals(GenericStore.COMPLEX.copy(complexAA), complexSVD, evaluation);
        TestUtils.assertEquals(expected, complexSVD.getSingularValues(), evaluation);

        SingularValue<Quaternion> quaternionSVD = SingularValue.QUATERNION.make(quaternionAA);
        quaternionSVD.compute(quaternionAA);
        TestUtils.assertEquals(GenericStore.QUATERNION.copy(quaternionAA), quaternionSVD, evaluation);
        TestUtils.assertEquals(expected, quaternionSVD.getSingularValues(), evaluation);

        for (SingularValue<Double> primitiveSVD : MatrixDecompositionTests.getPrimitiveSingularValue()) {
            primitiveSVD.compute(primitiveAA);
            TestUtils.assertEquals(Primitive64Store.FACTORY.copy(primitiveAA), primitiveSVD, evaluation);
            TestUtils.assertEquals(expected, primitiveSVD.getSingularValues(), evaluation);
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

        PhysicalReceiver<RationalNumber, RationalMatrix> copyRational = rationalAA.copy();
        copyRational.modifyMatching(RationalMath.MULTIPLY, rationalSafe);
        expMtrx = copyRational.get();

        PhysicalReceiver<Double, PrimitiveMatrix> copyPrimitive = primitiveAA.copy();
        copyPrimitive.modifyMatching(PrimitiveMath.MULTIPLY, primitiveSafe);
        actMtrx = copyPrimitive.get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        PhysicalReceiver<ComplexNumber, ComplexMatrix> copyComplex = complexAA.copy();
        copyComplex.modifyMatching(ComplexMath.MULTIPLY, complexSafe);
        actMtrx = copyComplex.get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        PhysicalReceiver<Quaternion, QuaternionMatrix> copyQuaternion = quaternionAA.copy();
        copyQuaternion.modifyMatching(QuaternionMath.MULTIPLY, quaternionSafe);
        actMtrx = copyQuaternion.get();
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#multiply(java.lang.Number)
     */
    @Test
    public void testMultiplyNumber() {

        expMtrx = rationalAA.multiply(RationalNumber.valueOf(bigNumber));

        actMtrx = complexAA.multiply(ComplexNumber.valueOf(bigNumber));
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = primitiveAA.multiply(bigNumber.doubleValue());
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
     * @see BasicMatrix.PhysicalReceiver#set(long, long, Number)
     */
    @Test
    public void testSetIntIntNumber() {

        final int tmpRow = Uniform.randomInteger((int) rationalAA.countRows());
        final int tmpCol = Uniform.randomInteger((int) rationalAA.countColumns());

        final BasicMatrix.PhysicalReceiver<RationalNumber, RationalMatrix> tmpBigBuilder = rationalAA.copy();
        tmpBigBuilder.set(tmpRow, tmpCol, bigNumber);
        expMtrx = tmpBigBuilder.build();

        final BasicMatrix.PhysicalReceiver<ComplexNumber, ComplexMatrix> tmpComplexBuilder = complexAA.copy();
        tmpComplexBuilder.set(tmpRow, tmpCol, bigNumber);
        actMtrx = tmpComplexBuilder.build();

        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        final BasicMatrix.PhysicalReceiver<Double, PrimitiveMatrix> tmpPrimitiveBuilder = primitiveAA.copy();
        tmpPrimitiveBuilder.set(tmpRow, tmpCol, bigNumber);
        actMtrx = tmpPrimitiveBuilder.build();

        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);
    }

    @Test
    public void testSize() {

        expInt = (int) rationalAA.count();

        actInt = (int) complexAA.count();
        TestUtils.assertEquals(expInt, actInt);

        actInt = (int) primitiveAA.count();
        TestUtils.assertEquals(expInt, actInt);

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

        expMtrx = rationalAA.subtract(RationalNumber.valueOf(bigNumber));

        actMtrx = complexAA.subtract(ComplexNumber.valueOf(bigNumber));
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        actMtrx = primitiveAA.subtract(bigNumber.doubleValue());
        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

    }

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

    @Test
    public void testToComplexStore() {

        final PhysicalStore<ComplexNumber> tmpExpStore = GenericStore.COMPLEX.copy(rationalAA);
        PhysicalStore<ComplexNumber> tmpActStore;

        tmpActStore = GenericStore.COMPLEX.copy(complexAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, evaluation);

        tmpActStore = GenericStore.COMPLEX.copy(primitiveAA);
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

    @Test
    public void testToPrimitiveStore() {

        final PhysicalStore<Double> tmpExpStore = Primitive64Store.FACTORY.copy(rationalAA);
        PhysicalStore<Double> tmpActStore;

        tmpActStore = Primitive64Store.FACTORY.copy(complexAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, evaluation);

        tmpActStore = Primitive64Store.FACTORY.copy(primitiveAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, evaluation);

    }

    @Test
    public void testToRationalStore() {

        final PhysicalStore<RationalNumber> tmpExpStore = GenericStore.RATIONAL.copy(rationalAA);
        PhysicalStore<RationalNumber> tmpActStore;

        tmpActStore = GenericStore.RATIONAL.copy(complexAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, evaluation);

        tmpActStore = GenericStore.RATIONAL.copy(primitiveAA);
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
