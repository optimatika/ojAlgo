/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import org.ojalgo.TestUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.BasicMatrix.Builder;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public abstract class BasicMatrixTest extends MatrixTests {

    public static NumberContext DEFINITION = NumberContext.getGeneral(9);
    public static NumberContext EVALUATION = NumberContext.getGeneral(9);

    public static BigMatrix getIdentity(final long rows, final long columns, final NumberContext context) {
        final BigMatrix tmpMtrx = BigMatrix.FACTORY.makeEye(rows, columns);
        return tmpMtrx.enforce(context);
    }

    public static BigMatrix getSafe(final long rows, final long columns, final NumberContext context) {
        final BigMatrix tmpMtrx = BigMatrix.FACTORY.makeFilled(rows, columns, new Uniform(PrimitiveMath.E, PrimitiveMath.PI));
        return tmpMtrx.enforce(context);
    }

    boolean myActBool;
    int myActInt;
    BasicMatrix myActMtrx;
    Number myActNmbr;
    Scalar<?> myActSclr;
    BigMatrix myBigAA;
    BigMatrix myBigAB;
    BigMatrix myBigAX;
    BigMatrix myBigI;
    BigMatrix myBigSafe;
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
    Number myNmbr;
    PrimitiveMatrix myPrimitiveAA;
    PrimitiveMatrix myPrimitiveAB;
    PrimitiveMatrix myPrimitiveAX;
    PrimitiveMatrix myPrimitiveI;
    PrimitiveMatrix myPrimitiveSafe;

    public BasicMatrixTest() {
        super();
    }

    public BasicMatrixTest(final String arg0) {
        super(arg0);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#add(org.ojalgo.matrix.BasicMatrix)
     */
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
    public void testAddIntIntNumber() {

        final int tmpRow = Uniform.randomInteger((int) myBigAA.countRows());
        final int tmpCol = Uniform.randomInteger((int) myBigAA.countColumns());

        final Builder<BigMatrix> tmpBigBuilder = myBigAA.copyToBuilder();
        tmpBigBuilder.add(tmpRow, tmpCol, myNmbr);
        myExpMtrx = tmpBigBuilder.build();

        final Builder<ComplexMatrix> tmpComplexBuilder = myComplexAA.copyToBuilder();
        tmpComplexBuilder.add(tmpRow, tmpCol, myNmbr);
        myActMtrx = tmpComplexBuilder.build();

        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        final Builder<PrimitiveMatrix> tmpPrimitiveBuilder = myPrimitiveAA.copyToBuilder();
        tmpPrimitiveBuilder.add(tmpRow, tmpCol, myNmbr);
        myActMtrx = tmpPrimitiveBuilder.build();

        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#add(java.lang.Number)
     */
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
    public void testConjugate() {

        myExpMtrx = myBigAA.conjugate();

        myActMtrx = myComplexAA.conjugate();
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.conjugate();
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    abstract public void testData();

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
    public void testDivideNumber() {

        myExpMtrx = myBigAA.divide(myNmbr);

        myActMtrx = myComplexAA.divide(myNmbr);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.divide(myNmbr);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    public void testDotAccess1D() {

        final int[] tmpCol = new int[] { (int) Uniform.randomInteger(myBigAA.countColumns()) };

        myExpNmbr = myBigAA.selectColumns(tmpCol).dot(myBigSafe.selectColumns(tmpCol));

        myActNmbr = myComplexAA.selectColumns(tmpCol).dot(myComplexSafe.selectColumns(tmpCol));
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.selectColumns(tmpCol).dot(myPrimitiveSafe.selectColumns(tmpCol));
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#doubleValue(long,long)
     */
    public void testDoubleValueIntInt() {

        final int tmpRow = (int) Uniform.randomInteger(myBigAA.countRows());
        final int tmpCol = (int) Uniform.randomInteger(myBigAA.countColumns());

        myExpNmbr = new Double(myBigAA.doubleValue(tmpRow, tmpCol));

        myActNmbr = new Double(myComplexAA.doubleValue(tmpRow, tmpCol));
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = new Double(myPrimitiveAA.doubleValue(tmpRow, tmpCol));
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#countColumns()
     */
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
    public void testGetDeterminant() {

        if (myBigAA.isSquare()) {

            myExpNmbr = myBigAA.getDeterminant().getNumber();

            myActNmbr = myComplexAA.getDeterminant().getNumber();
            TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

            myActNmbr = myPrimitiveAA.getDeterminant().getNumber();
            TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getEigenvalues()
     */
    public void testGetEigenvalues() {

        if (myBigAA.isSquare()) {

            final List<ComplexNumber> tmpExpStore = myPrimitiveAA.getEigenvalues();
            List<ComplexNumber> tmpActStore;

            if (MatrixUtils.isHermitian(myBigAA.toPrimitiveStore())) {

                tmpActStore = myBigAA.getEigenvalues();
                for (int i = 0; i < tmpExpStore.size(); i++) {
                    TestUtils.assertEquals("Scalar<?> != Scalar<?>", tmpExpStore.get(i), tmpActStore.get(i), EVALUATION);
                }

                tmpActStore = myComplexAA.getEigenvalues();
                for (int i = 0; i < tmpExpStore.size(); i++) {
                    TestUtils.assertEquals("Scalar<?> != Scalar<?>", tmpExpStore.get(i), tmpActStore.get(i), EVALUATION);
                }
            }

        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getFrobeniusNorm()
     */
    public void testGetFrobeniusNorm() {

        myExpNmbr = myBigAA.getFrobeniusNorm().getNumber();

        myActNmbr = myComplexAA.getFrobeniusNorm().getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.getFrobeniusNorm().getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getInfinityNorm()
     */
    public void testGetInfinityNorm() {

        myExpNmbr = myBigAA.getInfinityNorm().getNumber();

        myActNmbr = myComplexAA.getInfinityNorm().getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.getInfinityNorm().getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getKyFanNorm(int)
     */
    public void testGetKyFanNormInt() {

        final int tmpDegree = Uniform.randomInteger(1, (int) Math.min(myBigAA.countRows(), myBigAA.countColumns()));

        myExpNmbr = myBigAA.getKyFanNorm(tmpDegree).getNumber();

        myActNmbr = myComplexAA.getKyFanNorm(tmpDegree).getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.getKyFanNorm(tmpDegree).getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getOneNorm()
     */
    public void testGetOneNorm() {

        myExpNmbr = myBigAA.getOneNorm().getNumber();

        myActNmbr = myComplexAA.getOneNorm().getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.getOneNorm().getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getOperatorNorm()
     */
    public void testGetOperatorNorm() {

        myExpNmbr = myBigAA.getOperatorNorm().getNumber();

        myActNmbr = myComplexAA.getOperatorNorm().getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.getOperatorNorm().getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getRank()
     */
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
    public void testGetTrace() {

        myExpNmbr = myBigAA.getTrace().getNumber();

        myActNmbr = myComplexAA.getTrace().getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.getTrace().getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getTraceNorm()
     */
    public void testGetTraceNorm() {

        myExpNmbr = myBigAA.getTraceNorm().getNumber();

        myActNmbr = myComplexAA.getTraceNorm().getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.getTraceNorm().getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getVectorNorm(int)
     */
    public void testGetVectorNorm0() {
        this.testGetVectorNormInt(0);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getVectorNorm(int)
     */
    public void testGetVectorNorm1() {
        this.testGetVectorNormInt(1);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getVectorNorm(int)
     */
    public void testGetVectorNorm2() {
        this.testGetVectorNormInt(2);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getVectorNorm(int)
     */
    public void testGetVectorNormI() {
        this.testGetVectorNormInt(Integer.MAX_VALUE);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#invert()
     */
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
    public void testIsVector() {

        myExpBool = myBigAA.isVector();

        myActBool = myComplexAA.isVector();
        TestUtils.assertEquals(myExpBool, myActBool);

        myActBool = myPrimitiveAA.isVector();
        TestUtils.assertEquals(myExpBool, myActBool);

    }

    public void testMergeColumnsBasicMatrix() {

        myExpMtrx = myBigAA.mergeColumns(myBigSafe);

        myActMtrx = myComplexAA.mergeColumns(myComplexSafe);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.mergeColumns(myPrimitiveSafe);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

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
    public void testMultiplyBasicMatrix() {

        myExpMtrx = myBigAA.multiply(myBigAX);

        myActMtrx = myComplexAA.multiply(myComplexAX);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.multiply(myPrimitiveAX);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

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
    public void testNegate() {

        myExpMtrx = myBigAA.negate();

        myActMtrx = myComplexAA.negate();
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.negate();
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    abstract public void testProblem();

    /**
     * @see BasicMatrix.Builder#set(long, long, Number)
     */
    public void testSetIntIntNumber() {

        final int tmpRow = Uniform.randomInteger((int) myBigAA.countRows());
        final int tmpCol = Uniform.randomInteger((int) myBigAA.countColumns());

        final Builder<BigMatrix> tmpBigBuilder = myBigAA.copyToBuilder();
        tmpBigBuilder.set(tmpRow, tmpCol, myNmbr);
        myExpMtrx = tmpBigBuilder.build();

        final Builder<ComplexMatrix> tmpComplexBuilder = myComplexAA.copyToBuilder();
        tmpComplexBuilder.set(tmpRow, tmpCol, myNmbr);
        myActMtrx = tmpComplexBuilder.build();

        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        final Builder<PrimitiveMatrix> tmpPrimitiveBuilder = myPrimitiveAA.copyToBuilder();
        tmpPrimitiveBuilder.set(tmpRow, tmpCol, myNmbr);
        myActMtrx = tmpPrimitiveBuilder.build();

        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);
    }

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
    public void testSubtractNumber() {

        myExpMtrx = myBigAA.subtract(myNmbr);

        myActMtrx = myComplexAA.subtract(myNmbr);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.subtract(myNmbr);
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toBigDecimal(int,int)
     */
    public void testToBigDecimalIntInt() {

        final int tmpRow = (int) Uniform.randomInteger(myBigAA.countRows());
        final int tmpCol = (int) Uniform.randomInteger(myBigAA.countColumns());

        myExpNmbr = myBigAA.toBigDecimal(tmpRow, tmpCol);

        myActNmbr = myComplexAA.toBigDecimal(tmpRow, tmpCol);
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.toBigDecimal(tmpRow, tmpCol);
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toBigStore()
     */
    public void testToBigStore() {

        final PhysicalStore<BigDecimal> tmpExpStore = myBigAA.toBigStore();
        PhysicalStore<BigDecimal> tmpActStore;

        tmpActStore = myComplexAA.toBigStore();
        TestUtils.assertEquals(tmpExpStore, tmpActStore, EVALUATION);

        tmpActStore = myPrimitiveAA.toBigStore();
        TestUtils.assertEquals(tmpExpStore, tmpActStore, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toComplexNumber(int,int)
     */
    public void testToComplexNumberIntInt() {

        final int tmpRow = (int) Uniform.randomInteger(myBigAA.countRows());
        final int tmpCol = (int) Uniform.randomInteger(myBigAA.countColumns());

        myExpNmbr = myBigAA.toComplexNumber(tmpRow, tmpCol);

        myActNmbr = myComplexAA.toComplexNumber(tmpRow, tmpCol);
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.toComplexNumber(tmpRow, tmpCol);
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toComplexStore()
     */
    public void testToComplexStore() {

        final PhysicalStore<ComplexNumber> tmpExpStore = myBigAA.toComplexStore();
        PhysicalStore<ComplexNumber> tmpActStore;

        tmpActStore = myComplexAA.toComplexStore();
        TestUtils.assertEquals(tmpExpStore, tmpActStore, EVALUATION);

        tmpActStore = myPrimitiveAA.toComplexStore();
        TestUtils.assertEquals(tmpExpStore, tmpActStore, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toListOfColumns()
     */
    public void testToListOfColumns() {

        final List<BasicMatrix> tmpExpStore = myBigAA.toListOfColumns();
        List<BasicMatrix> tmpActStore;

        tmpActStore = myComplexAA.toListOfColumns();
        for (int i = 0; i < tmpExpStore.size(); i++) {
            TestUtils.assertEquals(tmpExpStore.get(i), tmpActStore.get(i), EVALUATION);
        }

        tmpActStore = myPrimitiveAA.toListOfColumns();
        for (int i = 0; i < tmpExpStore.size(); i++) {
            TestUtils.assertEquals(tmpExpStore.get(i), tmpActStore.get(i), EVALUATION);
        }

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toListOfElements()
     */
    public void testToListOfElements() {

        final List<? extends Number> tmpExpStore = myBigAA.toListOfElements();
        List<? extends Number> tmpActStore;

        final int tmpFirstIndex = 0;
        final int tmpLastIndex = (int) (myBigAA.count() - 1);

        tmpActStore = myComplexAA.toListOfElements();
        TestUtils.assertEquals(tmpExpStore.get(tmpFirstIndex), tmpActStore.get(tmpFirstIndex), EVALUATION);
        TestUtils.assertEquals(tmpExpStore.get(tmpLastIndex), tmpActStore.get(tmpLastIndex), EVALUATION);
        if (myBigAA.isVector()) {
            for (int i = 0; i < tmpExpStore.size(); i++) {
                TestUtils.assertEquals(tmpExpStore.get(i), tmpActStore.get(i), EVALUATION);
            }
        }

        tmpActStore = myPrimitiveAA.toListOfElements();
        TestUtils.assertEquals(tmpExpStore.get(tmpFirstIndex), tmpActStore.get(tmpFirstIndex), EVALUATION);
        TestUtils.assertEquals(tmpExpStore.get(tmpLastIndex), tmpActStore.get(tmpLastIndex), EVALUATION);
        if (myBigAA.isVector()) {
            for (int i = 0; i < tmpExpStore.size(); i++) {
                TestUtils.assertEquals(tmpExpStore.get(i), tmpActStore.get(i), EVALUATION);
            }
        }

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toListOfRows()
     */
    public void testToListOfRows() {

        final List<BasicMatrix> tmpExpStore = myBigAA.toListOfRows();
        List<BasicMatrix> tmpActStore;

        tmpActStore = myComplexAA.toListOfRows();
        for (int i = 0; i < tmpExpStore.size(); i++) {
            TestUtils.assertEquals(tmpExpStore.get(i), tmpActStore.get(i), EVALUATION);
        }

        tmpActStore = myPrimitiveAA.toListOfRows();
        for (int i = 0; i < tmpExpStore.size(); i++) {
            TestUtils.assertEquals(tmpExpStore.get(i), tmpActStore.get(i), EVALUATION);
        }

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toPrimitiveStore()
     */
    public void testToPrimitiveStore() {

        final PhysicalStore<Double> tmpExpStore = myBigAA.toPrimitiveStore();
        PhysicalStore<Double> tmpActStore;

        tmpActStore = myComplexAA.toPrimitiveStore();
        TestUtils.assertEquals(tmpExpStore, tmpActStore, EVALUATION);

        tmpActStore = myPrimitiveAA.toPrimitiveStore();
        TestUtils.assertEquals(tmpExpStore, tmpActStore, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#toScalar(long, long)
     */
    public void testToScalarIntInt() {

        final int tmpRow = Uniform.randomInteger((int) myBigAA.countRows());
        final int tmpCol = Uniform.randomInteger((int) myBigAA.countColumns());

        myExpNmbr = myBigAA.toScalar(tmpRow, tmpCol).getNumber();

        myActNmbr = myComplexAA.toScalar(tmpRow, tmpCol).getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.toScalar(tmpRow, tmpCol).getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#transpose()
     */
    public void testTranspose() {

        myExpMtrx = myBigAA.transpose();

        myActMtrx = myComplexAA.transpose();
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

        myActMtrx = myPrimitiveAA.transpose();
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);

    }

    private void testGetVectorNormInt(final int tmpDegree) {

        myExpNmbr = myBigAA.getVectorNorm(tmpDegree).getNumber();

        myActNmbr = myComplexAA.getVectorNorm(tmpDegree).getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

        myActNmbr = myPrimitiveAA.getVectorNorm(tmpDegree).getNumber();
        TestUtils.assertEquals(myExpNmbr, myActNmbr, EVALUATION);

    }

    protected final BasicMatrix getBigAA() {
        return myBigAA;
    }

    protected final BasicMatrix getBigAB() {
        return myBigAB;
    }

    protected final BasicMatrix getBigAX() {
        return myBigAX;
    }

    protected final BasicMatrix getBigI() {
        return myBigI;
    }

    protected final BasicMatrix getBigSafe() {
        return myBigSafe;
    }

    protected final void setBigAA(final BigMatrix someBigAA) {
        myBigAA = someBigAA;
    }

    protected final void setBigAB(final BigMatrix someBigAB) {
        myBigAB = someBigAB;
    }

    protected final void setBigAX(final BigMatrix someBigAX) {
        myBigAX = someBigAX;
    }

    protected final void setBigI(final BigMatrix someBigI) {
        myBigI = someBigI;
    }

    protected final void setBigSafe(final BigMatrix someBigSafe) {
        myBigSafe = someBigSafe;
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {

        super.setUp();

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

    @Override
    protected final void tearDown() throws Exception {

        super.tearDown();

        DEFINITION = NumberContext.getGeneral(9);
        EVALUATION = NumberContext.getGeneral(9);
    }

}
