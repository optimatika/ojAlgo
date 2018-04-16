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
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.access.ColumnView;
import org.ojalgo.access.RowView;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.BasicMatrix.Builder;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public abstract class BasicMatrixTest {

    public static NumberContext DEFINITION = NumberContext.getGeneral(9);
    public static NumberContext EVALUATION = NumberContext.getGeneral(9);

    public static RationalMatrix getIdentity(final long rows, final long columns, final NumberContext context) {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.makeEye(rows, columns);
        return tmpMtrx.enforce(context);
    }

    public static RationalMatrix getSafe(final long rows, final long columns, final NumberContext context) {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.makeFilled(rows, columns, new Uniform(PrimitiveMath.E, PrimitiveMath.PI));
        return tmpMtrx.enforce(context);
    }

    boolean myActBool;
    int myActInt;
    BasicMatrix myActMtrx;
    Number myActNmbr;
    Scalar<?> myActSclr;
    double myActVal;
    RationalMatrix myBigAA;
    RationalMatrix myBigAB;
    RationalMatrix myBigAX;
    RationalMatrix myBigI;
    RationalMatrix myBigSafe;
    ComplexMatrix myComplexAA;
    ComplexMatrix myComplexAB;
    ComplexMatrix myComplexAX;
    ComplexMatrix myComplexI;
    ComplexMatrix myComplexSafe;
    boolean myExpBool;
    int myExpInt;
    BasicMatrix myExpMtrx;
    Number myExpNmbr;
    Scalar<?> myExpSclr;
    double myExpVal;
    Number myNmbr;
    PrimitiveMatrix myPrimitiveAA;
    PrimitiveMatrix myPrimitiveAB;
    PrimitiveMatrix myPrimitiveAX;
    PrimitiveMatrix myPrimitiveI;
    PrimitiveMatrix myPrimitiveSafe;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @BeforeEach
    public void setUp() {

        TestUtils.minimiseAllBranchLimits();

        myPrimitiveAA = PrimitiveMatrix.FACTORY.copy(myBigAA);
        myPrimitiveAX = PrimitiveMatrix.FACTORY.copy(myBigAX);
        myPrimitiveAB = PrimitiveMatrix.FACTORY.copy(myBigAB);
        myPrimitiveI = PrimitiveMatrix.FACTORY.copy(myBigI);
        myPrimitiveSafe = PrimitiveMatrix.FACTORY.copy(myBigSafe);

        myComplexAA = ComplexMatrix.FACTORY.copy(myBigAA);
        myComplexAX = ComplexMatrix.FACTORY.copy(myBigAX);
        myComplexAB = ComplexMatrix.FACTORY.copy(myBigAB);
        myComplexI = ComplexMatrix.FACTORY.copy(myBigI);
        myComplexSafe = ComplexMatrix.FACTORY.copy(myBigSafe);

        myNmbr = new BigDecimal(Math.random());
    }

    @AfterEach
    public void tearDown() {
        DEFINITION = NumberContext.getGeneral(9);
        EVALUATION = NumberContext.getGeneral(9);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#add(org.ojalgo.matrix.BasicMatrix)
     */
    @Test
    public void testAddBasicMatrix() {

        myExpMtrx = myBigAA.add(myBigSafe);

        myActMtrx = myComplexAA.add(myComplexSafe);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.add(myPrimitiveSafe);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    /**
     * @see BasicMatrix.Builder#add(long, long, Number)
     */
    @Test
    public void testAddIntIntNumber() {

        final int tmpRow = Uniform.randomInteger((int) myBigAA.countRows());
        final int tmpCol = Uniform.randomInteger((int) myBigAA.countColumns());

        final Builder<RationalMatrix> tmpBigBuilder = myBigAA.copy();
        tmpBigBuilder.add(tmpRow, tmpCol, myNmbr);
        myExpMtrx = tmpBigBuilder.build();

        final Builder<ComplexMatrix> tmpComplexBuilder = myComplexAA.copy();
        tmpComplexBuilder.add(tmpRow, tmpCol, myNmbr);
        myActMtrx = tmpComplexBuilder.build();

        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        final Builder<PrimitiveMatrix> tmpPrimitiveBuilder = myPrimitiveAA.copy();
        tmpPrimitiveBuilder.add(tmpRow, tmpCol, myNmbr);
        myActMtrx = tmpPrimitiveBuilder.build();

        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#add(java.lang.Number)
     */
    @Test
    public void testAddNumber() {

        myExpMtrx = myBigAA.add(myNmbr);

        myActMtrx = myComplexAA.add(myNmbr);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.add(myNmbr);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#conjugate()
     */
    @Test
    public void testConjugate() {

        myExpMtrx = myBigAA.conjugate();

        myActMtrx = myComplexAA.conjugate();
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.conjugate();
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    @Test
    public void testDivideElementsBasicMatrix() {

        myExpMtrx = myBigAA.divideElements(myBigSafe);

        myActMtrx = myComplexAA.divideElements(myComplexSafe);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.divideElements(myPrimitiveSafe);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#divide(java.lang.Number)
     */
    @Test
    public void testDivideNumber() {

        myExpMtrx = myBigAA.divide(myNmbr);

        myActMtrx = myComplexAA.divide(myNmbr);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.divide(myNmbr);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    @Test
    public void testDotAccess1D() {

        final int[] tmpCol = new int[] { (int) Uniform.randomInteger(myBigAA.countColumns()) };

        myExpNmbr = myBigAA.selectColumns(tmpCol).dot(myBigSafe.selectColumns(tmpCol));

        myActNmbr = myComplexAA.selectColumns(tmpCol).dot(myComplexSafe.selectColumns(tmpCol));
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.selectColumns(tmpCol).dot(myPrimitiveSafe.selectColumns(tmpCol));
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#doubleValue(long, long)
     */
    @Test
    public void testDoubleValueIntInt() {

        final int tmpRow = (int) Uniform.randomInteger(myBigAA.countRows());
        final int tmpCol = (int) Uniform.randomInteger(myBigAA.countColumns());

        myExpNmbr = myBigAA.doubleValue(tmpRow, tmpCol);

        myActNmbr = myComplexAA.doubleValue(tmpRow, tmpCol);
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.doubleValue(tmpRow, tmpCol);
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#countColumns()
     */
    @Test
    public void testGetColDim() {

        myExpInt = (int) myBigAA.countColumns();

        myActInt = (int) myComplexAA.countColumns();
        TestUtils.assertEquals(myExpBool, myActBool);

        myActInt = (int) myPrimitiveAA.countColumns();
        TestUtils.assertEquals(myExpBool, myActBool);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#selectColumns(int[])
     */
    @Test
    public void testGetColumnsIntArray() {

        final int[] tmpArr = new int[(int) (1 + Uniform.randomInteger(myBigAA.countColumns()))];

        for (int i = 0; i < tmpArr.length; i++) {
            tmpArr[i] = (int) Uniform.randomInteger(myBigAA.countColumns());
        }

        myExpMtrx = myBigAA.selectColumns(tmpArr);

        myActMtrx = myComplexAA.selectColumns(tmpArr);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.selectColumns(tmpArr);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getCondition()
     */
    @Test
    public void testGetCondition() {

        if (myBigAA.isFullRank()) {

            // Difficult to test numerically
            // Will only check that they are the same order of magnitude

            final int tmpExpCondMag = (int) Math.round(PrimitiveFunction.LOG10.invoke(myBigAA.getCondition().doubleValue()));

            int tmpActCondMag = (int) Math.round(PrimitiveFunction.LOG10.invoke(myPrimitiveAA.getCondition().doubleValue()));
            TestUtils.assertEquals(tmpExpCondMag, tmpActCondMag);

            tmpActCondMag = (int) Math.round(PrimitiveFunction.LOG10.invoke(myComplexAA.getCondition().doubleValue()));
            TestUtils.assertEquals(tmpExpCondMag, tmpActCondMag);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getDeterminant()
     */
    @Test
    public void testGetDeterminant() {

        if (myBigAA.isSquare()) {

            myExpNmbr = myBigAA.getDeterminant().get();

            myActNmbr = myComplexAA.getDeterminant().get();
            TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

            myActNmbr = myPrimitiveAA.getDeterminant().get();
            TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getEigenvalues()
     */
    @Test
    public void testGetEigenvalues() {

        if (myBigAA.isSquare() && MatrixUtils.isHermitian(myBigAA)) {

            final List<ComplexNumber> expected = myPrimitiveAA.getEigenvalues();
            Collections.sort(expected);
            List<ComplexNumber> actual;

            actual = myBigAA.getEigenvalues();
            Collections.sort(actual);
            for (int i = 0; i < expected.size(); i++) {
                TestUtils.assertEquals("Scalar<?> != Scalar<?>", expected.get(i), actual.get(i), EVALUATION);
            }

            actual = myComplexAA.getEigenvalues();
            Collections.sort(actual);
            for (int i = 0; i < expected.size(); i++) {
                TestUtils.assertEquals("Scalar<?> != Scalar<?>", expected.get(i), actual.get(i), EVALUATION);
            }
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getInfinityNorm()
     */
    @Test
    public void testGetInfinityNorm() {

        myExpNmbr = myBigAA.getInfinityNorm().get();

        myActNmbr = myComplexAA.getInfinityNorm().get();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.getInfinityNorm().get();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getKyFanNorm(int)
     */
    @Test
    public void testGetKyFanNormInt() {

        final int tmpDegree = Uniform.randomInteger(1, (int) Math.min(myBigAA.countRows(), myBigAA.countColumns()));

        myExpNmbr = myBigAA.getKyFanNorm(tmpDegree).get();

        myActNmbr = myComplexAA.getKyFanNorm(tmpDegree).get();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.getKyFanNorm(tmpDegree).get();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getOneNorm()
     */
    @Test
    public void testGetOneNorm() {

        myExpNmbr = myBigAA.getOneNorm().get();

        myActNmbr = myComplexAA.getOneNorm().get();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.getOneNorm().get();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getOperatorNorm()
     */
    @Test
    public void testGetOperatorNorm() {

        myExpNmbr = myBigAA.getOperatorNorm().get();

        myActNmbr = myComplexAA.getOperatorNorm().get();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.getOperatorNorm().get();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getRank()
     */
    @Test
    public void testGetRank() {

        myExpInt = myBigAA.getRank();

        myActInt = myComplexAA.getRank();
        TestUtils.assertEquals(myExpInt, myActInt);

        myActInt = myPrimitiveAA.getRank();
        TestUtils.assertEquals(myExpInt, myActInt);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#countRows()
     */
    @Test
    public void testGetRowDim() {

        myExpInt = (int) myBigAA.countRows();

        myActInt = (int) myComplexAA.countRows();
        TestUtils.assertEquals(myExpBool, myActBool);

        myActInt = (int) myPrimitiveAA.countRows();
        TestUtils.assertEquals(myExpBool, myActBool);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#selectRows(int[])
     */
    @Test
    public void testGetRowsIntArray() {

        final int[] tmpArr = new int[(int) (1 + Uniform.randomInteger(myBigAA.countRows()))];

        for (int i = 0; i < tmpArr.length; i++) {
            tmpArr[i] = (int) Uniform.randomInteger(myBigAA.countRows());
        }

        myExpMtrx = myBigAA.selectRows(tmpArr);

        myActMtrx = myComplexAA.selectRows(tmpArr);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.selectRows(tmpArr);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getSingularValues()
     */
    @Test
    public void testGetSingularValues() {

        final List<? extends Number> tmpExpStore = myBigAA.getSingularValues();
        if (MatrixTests.DEBUG) {
            BasicLogger.debug("Big SVs: {}", tmpExpStore);
        }
        List<? extends Number> tmpActStore;

        tmpActStore = myPrimitiveAA.getSingularValues();
        if (MatrixTests.DEBUG) {
            BasicLogger.debug("Primitive SVs: {}", tmpActStore);
        }
        for (int i = 0; i < tmpExpStore.size(); i++) {
            TestUtils.assertEquals(tmpExpStore.get(i), tmpActStore.get(i), EVALUATION);
        }

        tmpActStore = myComplexAA.getSingularValues();
        if (MatrixTests.DEBUG) {
            BasicLogger.debug("Complex SVs: {}", tmpActStore);
        }
        for (int i = 0; i < tmpExpStore.size(); i++) {
            TestUtils.assertEquals(tmpExpStore.get(i), tmpActStore.get(i), EVALUATION);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getTrace()
     */
    @Test
    public void testGetTrace() {

        myExpNmbr = myBigAA.getTrace().get();

        myActNmbr = myComplexAA.getTrace().get();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.getTrace().get();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getTraceNorm()
     */
    @Test
    public void testGetTraceNorm() {

        myExpNmbr = myBigAA.getTraceNorm().get();

        myActNmbr = myComplexAA.getTraceNorm().get();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.getTraceNorm().get();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getVectorNorm(int)
     */
    @Test
    public void testGetVectorNorm0() {
        this.testGetVectorNormInt(0);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getVectorNorm(int)
     */
    @Test
    public void testGetVectorNorm1() {
        this.testGetVectorNormInt(1);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getVectorNorm(int)
     */
    @Test
    public void testGetVectorNorm2() {
        this.testGetVectorNormInt(2);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getVectorNorm(int)
     */
    @Test
    public void testGetVectorNormI() {
        this.testGetVectorNormInt(Integer.MAX_VALUE);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#invert()
     */
    @Test
    public void testInvert() {

        if (myBigAA.isSquare() && (myBigAA.getRank() >= myBigAA.countColumns())) {

            myExpMtrx = myBigAA.invert();

            myActMtrx = myComplexAA.invert();
            TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

            myActMtrx = myPrimitiveAA.invert();
            TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isEmpty()
     */
    @Test
    public void testIsEmpty() {

        myExpBool = myBigAA.isEmpty();

        myActBool = myComplexAA.isEmpty();
        TestUtils.assertEquals(myExpBool, myActBool);

        myActBool = myPrimitiveAA.isEmpty();
        TestUtils.assertEquals(myExpBool, myActBool);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isFat()
     */
    @Test
    public void testIsFat() {

        myExpBool = myBigAA.isFat();

        myActBool = myComplexAA.isFat();
        TestUtils.assertEquals(myExpBool, myActBool);

        myActBool = myPrimitiveAA.isFat();
        TestUtils.assertEquals(myExpBool, myActBool);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isFullRank()
     */
    @Test
    public void testIsFullRank() {

        myExpBool = myBigAA.isFullRank();

        myActBool = myComplexAA.isFullRank();
        TestUtils.assertEquals(myExpBool, myActBool);

        myActBool = myPrimitiveAA.isFullRank();
        TestUtils.assertEquals(myExpBool, myActBool);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isHermitian()
     */
    @Test
    public void testIsHermitian() {

        myExpBool = myBigAA.isHermitian();

        myActBool = myComplexAA.isHermitian();
        TestUtils.assertEquals(myExpBool, myActBool);

        myActBool = myPrimitiveAA.isHermitian();
        TestUtils.assertEquals(myExpBool, myActBool);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isSquare()
     */
    @Test
    public void testIsSquare() {

        myExpBool = myBigAA.isSquare();

        myActBool = myComplexAA.isSquare();
        TestUtils.assertEquals(myExpBool, myActBool);

        myActBool = myPrimitiveAA.isSquare();
        TestUtils.assertEquals(myExpBool, myActBool);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isSymmetric()
     */
    @Test
    public void testIsSymmetric() {

        myExpBool = myBigAA.isSymmetric();

        myActBool = myComplexAA.isSymmetric();
        TestUtils.assertEquals(myExpBool, myActBool);

        myActBool = myPrimitiveAA.isSymmetric();
        TestUtils.assertEquals(myExpBool, myActBool);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isTall()
     */
    @Test
    public void testIsTall() {

        myExpBool = myBigAA.isTall();

        myActBool = myComplexAA.isTall();
        TestUtils.assertEquals(myExpBool, myActBool);

        myActBool = myPrimitiveAA.isTall();
        TestUtils.assertEquals(myExpBool, myActBool);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isVector()
     */
    @Test
    public void testIsVector() {

        myExpBool = myBigAA.isVector();

        myActBool = myComplexAA.isVector();
        TestUtils.assertEquals(myExpBool, myActBool);

        myActBool = myPrimitiveAA.isVector();
        TestUtils.assertEquals(myExpBool, myActBool);

    }

    @Test
    public void testMergeColumnsBasicMatrix() {

        myExpMtrx = myBigAA.mergeColumns(myBigSafe);

        myActMtrx = myComplexAA.mergeColumns(myComplexSafe);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.mergeColumns(myPrimitiveSafe);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    @Test
    public void testMergeRowsBasicMatrix() {

        myExpMtrx = myBigAA.mergeRows(myBigSafe);

        myActMtrx = myComplexAA.mergeRows(myComplexSafe);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.mergeRows(myPrimitiveSafe);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#multiply(org.ojalgo.matrix.BasicMatrix)
     */
    @Test
    public void testMultiplyBasicMatrix() {

        myExpMtrx = myBigAA.multiply(myBigAX);

        myActMtrx = myComplexAA.multiply(myComplexAX);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.multiply(myPrimitiveAX);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    @Test
    public void testMultiplyElementsBasicMatrix() {

        myExpMtrx = myBigAA.multiplyElements(myBigSafe);

        myActMtrx = myComplexAA.multiplyElements(myComplexSafe);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.multiplyElements(myPrimitiveSafe);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#multiply(java.lang.Number)
     */
    @Test
    public void testMultiplyNumber() {

        myExpMtrx = myBigAA.multiply(myNmbr);

        myActMtrx = myComplexAA.multiply(myNmbr);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.multiply(myNmbr);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#negate()
     */
    @Test
    public void testNegate() {

        myExpMtrx = myBigAA.negate();

        myActMtrx = myComplexAA.negate();
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.negate();
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#norm()
     */
    @Test
    public void testNorm() {

        myExpVal = myBigAA.norm();

        myActVal = myComplexAA.norm();
        TestUtils.assertEquals(myExpVal, myActVal, EVALUATION);

        myActVal = myPrimitiveAA.norm();
        TestUtils.assertEquals(myExpVal, myActVal, EVALUATION);

    }

    /**
     * @see BasicMatrix.Builder#set(long, long, Number)
     */
    @Test
    public void testSetIntIntNumber() {

        final int tmpRow = Uniform.randomInteger((int) myBigAA.countRows());
        final int tmpCol = Uniform.randomInteger((int) myBigAA.countColumns());

        final Builder<RationalMatrix> tmpBigBuilder = myBigAA.copy();
        tmpBigBuilder.set(tmpRow, tmpCol, myNmbr);
        myExpMtrx = tmpBigBuilder.build();

        final Builder<ComplexMatrix> tmpComplexBuilder = myComplexAA.copy();
        tmpComplexBuilder.set(tmpRow, tmpCol, myNmbr);
        myActMtrx = tmpComplexBuilder.build();

        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        final Builder<PrimitiveMatrix> tmpPrimitiveBuilder = myPrimitiveAA.copy();
        tmpPrimitiveBuilder.set(tmpRow, tmpCol, myNmbr);
        myActMtrx = tmpPrimitiveBuilder.build();

        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);
    }

    @Test
    public void testSize() {

        myExpInt = (int) myBigAA.count();

        myActInt = (int) myComplexAA.count();
        TestUtils.assertEquals(myExpBool, myActBool);

        myActInt = (int) myPrimitiveAA.count();
        TestUtils.assertEquals(myExpBool, myActBool);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#solve(org.ojalgo.access.Access2D)
     */
    @Test
    public void testSolveBasicMatrix() {

        if (myBigAA.isSquare() && (myBigAA.getRank() >= myBigAA.countColumns())) {

            myExpMtrx = myBigAA.solve(myBigAB);

            myActMtrx = myComplexAA.solve(myComplexAB);
            TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

            myActMtrx = myPrimitiveAA.solve(myPrimitiveAB);
            TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#subtract(org.ojalgo.matrix.BasicMatrix)
     */
    @Test
    public void testSubtractBasicMatrix() {

        myExpMtrx = myBigAA.subtract(myBigSafe);

        myActMtrx = myComplexAA.subtract(myComplexSafe);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.subtract(myPrimitiveSafe);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#subtract(java.lang.Number)
     */
    @Test
    public void testSubtractNumber() {

        myExpMtrx = myBigAA.subtract(myNmbr);

        myActMtrx = myComplexAA.subtract(myNmbr);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.subtract(myNmbr);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toBigDecimal(int, int)
     */
    @Test
    public void testToBigDecimalIntInt() {

        final int tmpRow = (int) Uniform.randomInteger(myBigAA.countRows());
        final int tmpCol = (int) Uniform.randomInteger(myBigAA.countColumns());

        myExpNmbr = TypeUtils.toBigDecimal(myBigAA.get(tmpRow, tmpCol));

        myActNmbr = TypeUtils.toBigDecimal(myComplexAA.get(tmpRow, tmpCol));
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = TypeUtils.toBigDecimal(myPrimitiveAA.get(tmpRow, tmpCol));
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toComplexNumber(int, int)
     */
    @Test
    public void testToComplexNumberIntInt() {

        final int tmpRow = (int) Uniform.randomInteger(myBigAA.countRows());
        final int tmpCol = (int) Uniform.randomInteger(myBigAA.countColumns());

        myExpNmbr = ComplexNumber.valueOf(myBigAA.get(tmpRow, tmpCol));

        myActNmbr = ComplexNumber.valueOf(myComplexAA.get(tmpRow, tmpCol));
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = ComplexNumber.valueOf(myPrimitiveAA.get(tmpRow, tmpCol));
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toComplexStore()
     */
    @Test
    public void testToComplexStore() {

        final PhysicalStore<ComplexNumber> tmpExpStore = ComplexDenseStore.FACTORY.copy(myBigAA);
        PhysicalStore<ComplexNumber> tmpActStore;

        tmpActStore = ComplexDenseStore.FACTORY.copy(myComplexAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, EVALUATION);

        tmpActStore = ComplexDenseStore.FACTORY.copy(myPrimitiveAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#columns()
     */
    @Test
    public void testToListOfColumns() {

        final Iterable<ColumnView<Number>> tmpColumns = myBigAA.columns();

        for (final ColumnView<Number> tmpColumnView : tmpColumns) {
            final long j = tmpColumnView.column();
            for (long i = 0L; i < tmpColumnView.count(); i++) {
                TestUtils.assertEquals(tmpColumnView.get(i), myComplexAA.get(i, j), EVALUATION);
                TestUtils.assertEquals(tmpColumnView.get(i), myPrimitiveAA.get(i, j), EVALUATION);
            }
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#rows()
     */
    @Test
    public void testToListOfRows() {

        final Iterable<RowView<Number>> tmpRows = myBigAA.rows();

        for (final RowView<Number> tmpRowView : tmpRows) {
            final long i = tmpRowView.row();
            for (long j = 0L; j < tmpRowView.count(); j++) {
                TestUtils.assertEquals(tmpRowView.get(j), myComplexAA.get(i, j), EVALUATION);
                TestUtils.assertEquals(tmpRowView.get(j), myPrimitiveAA.get(i, j), EVALUATION);
            }
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toPrimitiveStore()
     */
    @Test
    public void testToPrimitiveStore() {

        final PhysicalStore<Double> tmpExpStore = PrimitiveDenseStore.FACTORY.copy(myBigAA);
        PhysicalStore<Double> tmpActStore;

        tmpActStore = PrimitiveDenseStore.FACTORY.copy(myComplexAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, EVALUATION);

        tmpActStore = PrimitiveDenseStore.FACTORY.copy(myPrimitiveAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toBigStore()
     */
    @Test
    public void testToRationalStore() {

        final PhysicalStore<RationalNumber> tmpExpStore = GenericDenseStore.RATIONAL.copy(myBigAA);
        PhysicalStore<RationalNumber> tmpActStore;

        tmpActStore = GenericDenseStore.RATIONAL.copy(myComplexAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, EVALUATION);

        tmpActStore = GenericDenseStore.RATIONAL.copy(myPrimitiveAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toRawCopy1D()
     */
    @Test
    public void testToRawCopy1D() {

        final double[] tmpExpStore = myBigAA.toRawCopy1D();
        double[] tmpActStore;

        final int tmpFirstIndex = 0;
        final int tmpLastIndex = (int) (myBigAA.count() - 1);

        tmpActStore = myComplexAA.toRawCopy1D();
        TestUtils.assertEquals(tmpExpStore[tmpFirstIndex], tmpActStore[tmpFirstIndex], EVALUATION);
        TestUtils.assertEquals(tmpExpStore[tmpLastIndex], tmpActStore[tmpLastIndex], EVALUATION);
        if (myBigAA.isVector()) {
            for (int i = 0; i < tmpExpStore.length; i++) {
                TestUtils.assertEquals(tmpExpStore[i], tmpActStore[i], EVALUATION);
            }
        }

        tmpActStore = myPrimitiveAA.toRawCopy1D();
        TestUtils.assertEquals(tmpExpStore[tmpFirstIndex], tmpActStore[tmpFirstIndex], EVALUATION);
        TestUtils.assertEquals(tmpExpStore[tmpLastIndex], tmpActStore[tmpLastIndex], EVALUATION);
        if (myBigAA.isVector()) {
            for (int i = 0; i < tmpExpStore.length; i++) {
                TestUtils.assertEquals(tmpExpStore[i], tmpActStore[i], EVALUATION);
            }
        }

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toScalar(long, long)
     */
    @Test
    public void testToScalarIntInt() {

        final int tmpRow = Uniform.randomInteger((int) myBigAA.countRows());
        final int tmpCol = Uniform.randomInteger((int) myBigAA.countColumns());

        myExpNmbr = myBigAA.toScalar(tmpRow, tmpCol).get();

        myActNmbr = myComplexAA.toScalar(tmpRow, tmpCol).get();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.toScalar(tmpRow, tmpCol).get();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#transpose()
     */
    @Test
    public void testTranspose() {

        myExpMtrx = myBigAA.transpose();

        myActMtrx = myComplexAA.transpose();
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.transpose();
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    private void testGetVectorNormInt(final int tmpDegree) {

        myExpNmbr = myBigAA.getVectorNorm(tmpDegree).get();

        myActNmbr = myComplexAA.getVectorNorm(tmpDegree).get();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.getVectorNorm(tmpDegree).get();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

}
