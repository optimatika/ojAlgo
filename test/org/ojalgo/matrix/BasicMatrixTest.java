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

    protected static final NumberContext ACCURACY = new NumberContext(7, 7);

    public static RationalMatrix getIdentity(final long rows, final long columns, final NumberContext context) {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.makeEye(Math.toIntExact(rows), Math.toIntExact(columns));
        return tmpMtrx.enforce(context);
    }

    public static RationalMatrix getSafe(final long rows, final long columns, final NumberContext context) {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.makeFilled(rows, columns, new Uniform(PrimitiveMath.E, PrimitiveMath.PI));
        return tmpMtrx.enforce(context);
    }

    boolean actBoolean;
    int actInt;
    BasicMatrix<?, ?> actMtrx;
    Comparable<?> actNumber;
    Scalar<?> actScalar;
    double actValue;

    ComplexMatrix cAA;
    ComplexMatrix cAB;
    ComplexMatrix cAX;
    ComplexMatrix cI;
    ComplexMatrix cSafe;

    boolean expBoolean;
    int expInt;
    BasicMatrix<?, ?> expMtrx;
    Comparable<?> expNumber;
    Scalar<?> expScalar;
    double expValue;
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

    @AfterEach
    public void doAfterEach() {
        // ACCURACY = NumberContext.getGeneral(9);
    }

    /**
     * @see BasicMatrix.PhysicalReceiver#add(long, long, Comparable)
     */
    @Test
    public void testAddElement() {

        long row = Uniform.randomInteger(rAA.countRows());
        long col = Uniform.randomInteger(rAA.countColumns());

        BasicMatrix.PhysicalReceiver<RationalNumber, RationalMatrix> rBuilder = rAA.copy();
        rBuilder.add(row, col, scalar);
        expMtrx = rBuilder.build();

        BasicMatrix.PhysicalReceiver<ComplexNumber, ComplexMatrix> cBuilder = cAA.copy();
        cBuilder.add(row, col, scalar);
        actMtrx = cBuilder.build();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        BasicMatrix.PhysicalReceiver<Double, Primitive64Matrix> p64Builder = p64AA.copy();
        p64Builder.add(row, col, scalar);
        actMtrx = p64Builder.build();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        BasicMatrix.PhysicalReceiver<Double, Primitive32Matrix> p32Builder = p32AA.copy();
        p32Builder.add(row, col, scalar);
        actMtrx = p32Builder.build();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#add(org.ojalgo.matrix.BasicMatrix)
     */
    @Test
    public void testAddMatrix() {

        expMtrx = rAA.add(rSafe);

        actMtrx = cAA.add(cSafe);
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p64AA.add(p64Safe);
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p32AA.add(p32Safe);
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#add(Comparable)
     */
    @Test
    public void testAddScalar() {

        expMtrx = rAA.add(RationalNumber.valueOf(scalar));

        actMtrx = cAA.add(ComplexNumber.valueOf(scalar));
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p64AA.add(scalar.doubleValue());
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p32AA.add(scalar.floatValue());
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#conjugate()
     */
    @Test
    public void testConjugate() {

        expMtrx = rAA.conjugate();

        actMtrx = cAA.conjugate();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p64AA.conjugate();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p32AA.conjugate();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    @Test
    public void testDivideElementsBasicMatrix() {

        PhysicalReceiver<RationalNumber, RationalMatrix> copyRational = rAA.copy();
        copyRational.modifyMatching(RationalMath.DIVIDE, rSafe);
        expMtrx = copyRational.get();

        PhysicalReceiver<Double, Primitive64Matrix> copyPrimitive = p64AA.copy();
        copyPrimitive.modifyMatching(PrimitiveMath.DIVIDE, p64Safe);
        actMtrx = copyPrimitive.get();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        PhysicalReceiver<ComplexNumber, ComplexMatrix> copyComplex = cAA.copy();
        copyComplex.modifyMatching(ComplexMath.DIVIDE, cSafe);
        actMtrx = copyComplex.get();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        PhysicalReceiver<Quaternion, QuaternionMatrix> copyQuaternion = qAA.copy();
        copyQuaternion.modifyMatching(QuaternionMath.DIVIDE, qSafe);
        actMtrx = copyQuaternion.get();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#divide(Comparable)
     */
    @Test
    public void testDivideScalar() {

        expMtrx = rAA.divide(RationalNumber.valueOf(scalar));

        actMtrx = cAA.divide(ComplexNumber.valueOf(scalar));
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p64AA.divide(scalar.doubleValue());
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p32AA.divide(scalar.floatValue());
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    @Test
    public void testDotAccess1D() {

        final int[] tmpCol = new int[] { (int) Uniform.randomInteger(rAA.countColumns()) };

        expNumber = rAA.logical().column(tmpCol).get().dot(rSafe.logical().column(tmpCol).get());

        actNumber = cAA.logical().column(tmpCol).get().dot(cSafe.logical().column(tmpCol).get());
        TestUtils.assertEquals(expNumber, actNumber, ACCURACY);

        actNumber = p64AA.logical().column(tmpCol).get().dot(p64Safe.logical().column(tmpCol).get());
        TestUtils.assertEquals(expNumber, actNumber, ACCURACY);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#doubleValue(long, long)
     */
    @Test
    public void testDoubleValueIntInt() {

        final int tmpRow = (int) Uniform.randomInteger(rAA.countRows());
        final int tmpCol = (int) Uniform.randomInteger(rAA.countColumns());

        expNumber = rAA.doubleValue(tmpRow, tmpCol);

        actNumber = cAA.doubleValue(tmpRow, tmpCol);
        TestUtils.assertEquals(expNumber, actNumber, ACCURACY);

        actNumber = p64AA.doubleValue(tmpRow, tmpCol);
        TestUtils.assertEquals(expNumber, actNumber, ACCURACY);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#countColumns()
     */
    @Test
    public void testGetColDim() {

        expInt = (int) rAA.countColumns();

        actInt = (int) cAA.countColumns();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actInt = (int) p64AA.countColumns();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    @Test
    public void testGetColumnsIntArray() {

        final int[] tmpArr = new int[(int) (1 + Uniform.randomInteger(rAA.countColumns()))];

        for (int i = 0; i < tmpArr.length; i++) {
            tmpArr[i] = (int) Uniform.randomInteger(rAA.countColumns());
        }

        expMtrx = rAA.logical().column(tmpArr).get();

        actMtrx = cAA.logical().column(tmpArr).get();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p64AA.logical().column(tmpArr).get();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getCondition()
     */
    @Test
    public void testGetCondition() {

        if (rAA.isFullRank()) {

            // Difficult to test numerically
            // Will only check that they are the same order of magnitude

            final int tmpExpCondMag = (int) Math.round(PrimitiveMath.LOG10.invoke(rAA.getCondition().doubleValue()));

            int tmpActCondMag = (int) Math.round(PrimitiveMath.LOG10.invoke(p64AA.getCondition().doubleValue()));
            TestUtils.assertEquals(tmpExpCondMag, tmpActCondMag);

            tmpActCondMag = (int) Math.round(PrimitiveMath.LOG10.invoke(cAA.getCondition().doubleValue()));
            TestUtils.assertEquals(tmpExpCondMag, tmpActCondMag);
        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getDeterminant()
     */
    @Test
    public void testGetDeterminant() {

        if (rAA.isSquare()) {

            expNumber = rAA.getDeterminant().get();

            actNumber = cAA.getDeterminant().get();
            TestUtils.assertEquals(expNumber, actNumber, ACCURACY);

            actNumber = p64AA.getDeterminant().get();
            TestUtils.assertEquals(expNumber, actNumber, ACCURACY);

        }
    }

    @Test
    public void testGetEigenvalues() {

        if (rAA.isSquare() && MatrixUtils.isHermitian(rAA)) {

            final List<Eigenpair> expected = p64AA.getEigenpairs();
            List<Eigenpair> actual;

            actual = rAA.getEigenpairs();
            for (int i = 0; i < expected.size(); i++) {
                TestUtils.assertEquals("Scalar<?> != Scalar<?>", expected.get(i).value, actual.get(i).value, ACCURACY);
            }

            actual = cAA.getEigenpairs();
            for (int i = 0; i < expected.size(); i++) {
                TestUtils.assertEquals("Scalar<?> != Scalar<?>", expected.get(i).value, actual.get(i).value, ACCURACY);
            }
        }
    }

    @Test
    public void testGetInfinityNorm() {

        expValue = BasicMatrix.calculateInfinityNorm(rAA);

        actValue = BasicMatrix.calculateInfinityNorm(cAA);
        TestUtils.assertEquals(expValue, actValue, ACCURACY);

        actValue = BasicMatrix.calculateInfinityNorm(p64AA);
        TestUtils.assertEquals(expValue, actValue, ACCURACY);
    }

    @Test
    public void testGetOneNorm() {

        expValue = BasicMatrix.calculateOneNorm(rAA);

        actValue = BasicMatrix.calculateOneNorm(cAA);
        TestUtils.assertEquals(expValue, actValue, ACCURACY);

        actValue = BasicMatrix.calculateOneNorm(p64AA);
        TestUtils.assertEquals(expValue, actValue, ACCURACY);

        actValue = BasicMatrix.calculateOneNorm(p32AA);
        TestUtils.assertEquals(expValue, actValue, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getRank()
     */
    @Test
    public void testGetRank() {

        expInt = rAA.getRank();

        actInt = cAA.getRank();
        TestUtils.assertEquals(expInt, actInt);

        actInt = p64AA.getRank();
        TestUtils.assertEquals(expInt, actInt);

        actInt = p32AA.getRank();
        TestUtils.assertEquals(expInt, actInt);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#countRows()
     */
    @Test
    public void testGetRowDim() {

        expInt = (int) rAA.countRows();

        actInt = (int) cAA.countRows();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actInt = (int) p64AA.countRows();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actInt = (int) p32AA.countRows();
        TestUtils.assertEquals(expBoolean, actBoolean);
    }

    @Test
    public void testGetRowsIntArray() {

        final int[] tmpArr = new int[(int) (1 + Uniform.randomInteger(rAA.countRows()))];

        for (int i = 0; i < tmpArr.length; i++) {
            tmpArr[i] = (int) Uniform.randomInteger(rAA.countRows());
        }

        expMtrx = rAA.logical().row(tmpArr).get();

        actMtrx = cAA.logical().row(tmpArr).get();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p64AA.logical().row(tmpArr).get();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

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

        expNumber = rAA.getTrace().get();

        actNumber = cAA.getTrace().get();
        TestUtils.assertEquals(expNumber, actNumber, ACCURACY);

        actNumber = p64AA.getTrace().get();
        TestUtils.assertEquals(expNumber, actNumber, ACCURACY);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#invert()
     */
    @Test
    public void testInvert() {

        if (rAA.isSquare() && (rAA.getRank() >= rAA.countColumns())) {

            expMtrx = rAA.invert();

            actMtrx = cAA.invert();
            TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

            actMtrx = p64AA.invert();
            TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isEmpty()
     */
    @Test
    public void testIsEmpty() {

        expBoolean = rAA.isEmpty();

        actBoolean = cAA.isEmpty();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actBoolean = p64AA.isEmpty();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isFat()
     */
    @Test
    public void testIsFat() {

        expBoolean = rAA.isFat();

        actBoolean = cAA.isFat();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actBoolean = p64AA.isFat();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isFullRank()
     */
    @Test
    public void testIsFullRank() {

        expBoolean = rAA.isFullRank();

        actBoolean = cAA.isFullRank();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actBoolean = p64AA.isFullRank();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isHermitian()
     */
    @Test
    public void testIsHermitian() {

        expBoolean = rAA.isHermitian();

        actBoolean = cAA.isHermitian();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actBoolean = p64AA.isHermitian();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isSquare()
     */
    @Test
    public void testIsSquare() {

        expBoolean = rAA.isSquare();

        actBoolean = cAA.isSquare();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actBoolean = p64AA.isSquare();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isSymmetric()
     */
    @Test
    public void testIsSymmetric() {

        expBoolean = rAA.isSymmetric();

        actBoolean = cAA.isSymmetric();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actBoolean = p64AA.isSymmetric();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isTall()
     */
    @Test
    public void testIsTall() {

        expBoolean = rAA.isTall();

        actBoolean = cAA.isTall();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actBoolean = p64AA.isTall();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#isVector()
     */
    @Test
    public void testIsVector() {

        expBoolean = rAA.isVector();

        actBoolean = cAA.isVector();
        TestUtils.assertEquals(expBoolean, actBoolean);

        actBoolean = p64AA.isVector();
        TestUtils.assertEquals(expBoolean, actBoolean);

    }

    @Test
    public void testMergeColumnsBasicMatrix() {

        expMtrx = rAA.logical().below(rSafe).get();

        actMtrx = p64AA.logical().below(p64Safe).get();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = cAA.logical().below(cSafe).get();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = qAA.logical().below(qSafe).get();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    @Test
    public void testMergeRowsBasicMatrix() {

        expMtrx = rAA.logical().right(rSafe).get();

        actMtrx = p64AA.logical().right(p64Safe).get();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = cAA.logical().right(cSafe).get();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = qAA.logical().right(qSafe).get();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#multiply(org.ojalgo.matrix.BasicMatrix)
     */
    @Test
    public void testMultiplyMatrix() {

        expMtrx = rAA.multiply(rAX);

        actMtrx = cAA.multiply(cAX);
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p64AA.multiply(p64AX);
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p32AA.multiply(p32AX);
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    @Test
    public void testMultiplyElementsBasicMatrix() {

        PhysicalReceiver<RationalNumber, RationalMatrix> copyRational = rAA.copy();
        copyRational.modifyMatching(RationalMath.MULTIPLY, rSafe);
        expMtrx = copyRational.get();

        PhysicalReceiver<Double, Primitive64Matrix> copyPrimitive = p64AA.copy();
        copyPrimitive.modifyMatching(PrimitiveMath.MULTIPLY, p64Safe);
        actMtrx = copyPrimitive.get();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        PhysicalReceiver<ComplexNumber, ComplexMatrix> copyComplex = cAA.copy();
        copyComplex.modifyMatching(ComplexMath.MULTIPLY, cSafe);
        actMtrx = copyComplex.get();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        PhysicalReceiver<Quaternion, QuaternionMatrix> copyQuaternion = qAA.copy();
        copyQuaternion.modifyMatching(QuaternionMath.MULTIPLY, qSafe);
        actMtrx = copyQuaternion.get();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#multiply(java.lang.Number)
     */
    @Test
    public void testMultiplyScalar() {

        expMtrx = rAA.multiply(RationalNumber.valueOf(scalar));

        actMtrx = cAA.multiply(ComplexNumber.valueOf(scalar));
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p64AA.multiply(scalar.doubleValue());
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p32AA.multiply(scalar.floatValue());
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#negate()
     */
    @Test
    public void testNegate() {

        expMtrx = rAA.negate();

        actMtrx = cAA.negate();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p64AA.negate();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#norm()
     */
    @Test
    public void testNorm() {

        expValue = rAA.norm();

        actValue = cAA.norm();
        TestUtils.assertEquals(expValue, actValue, ACCURACY);

        actValue = p64AA.norm();
        TestUtils.assertEquals(expValue, actValue, ACCURACY);

    }

    /**
     * @see BasicMatrix.PhysicalReceiver#set(long, long, Number)
     */
    @Test
    public void testSetIntIntNumber() {

        final int tmpRow = Uniform.randomInteger((int) rAA.countRows());
        final int tmpCol = Uniform.randomInteger((int) rAA.countColumns());

        final BasicMatrix.PhysicalReceiver<RationalNumber, RationalMatrix> tmpBigBuilder = rAA.copy();
        tmpBigBuilder.set(tmpRow, tmpCol, scalar);
        expMtrx = tmpBigBuilder.build();

        final BasicMatrix.PhysicalReceiver<ComplexNumber, ComplexMatrix> tmpComplexBuilder = cAA.copy();
        tmpComplexBuilder.set(tmpRow, tmpCol, scalar);
        actMtrx = tmpComplexBuilder.build();

        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        final BasicMatrix.PhysicalReceiver<Double, Primitive64Matrix> tmpPrimitiveBuilder = p64AA.copy();
        tmpPrimitiveBuilder.set(tmpRow, tmpCol, scalar);
        actMtrx = tmpPrimitiveBuilder.build();

        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    @Test
    public void testSize() {

        expInt = (int) rAA.count();

        actInt = (int) cAA.count();
        TestUtils.assertEquals(expInt, actInt);

        actInt = (int) p64AA.count();
        TestUtils.assertEquals(expInt, actInt);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#solve(org.ojalgo.structure.Access2D)
     */
    @Test
    public void testSolveBasicMatrix() {

        if (rAA.isSquare() && (rAA.getRank() >= rAA.countColumns())) {

            expMtrx = rAA.solve(rAB);

            actMtrx = cAA.solve(cAB);
            TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

            actMtrx = p64AA.solve(p64AB);
            TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        }
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#subtract(BasicMatrix)
     */
    @Test
    public void testSubtractMatrix() {

        expMtrx = rAA.subtract(rSafe);

        actMtrx = cAA.subtract(cSafe);
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p64AA.subtract(p64Safe);
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p32AA.subtract(p32Safe);
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#subtract(Comparable)
     */
    @Test
    public void testSubtractScalar() {

        expMtrx = rAA.subtract(RationalNumber.valueOf(scalar));

        actMtrx = cAA.subtract(ComplexNumber.valueOf(scalar));
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p64AA.subtract(scalar.doubleValue());
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p32AA.subtract(scalar.floatValue());
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    @Test
    public void testToBigDecimalIntInt() {

        final int tmpRow = (int) Uniform.randomInteger(rAA.countRows());
        final int tmpCol = (int) Uniform.randomInteger(rAA.countColumns());

        expNumber = TypeUtils.toBigDecimal(rAA.get(tmpRow, tmpCol));

        actNumber = TypeUtils.toBigDecimal(cAA.get(tmpRow, tmpCol));
        TestUtils.assertEquals(expNumber, actNumber, ACCURACY);

        actNumber = TypeUtils.toBigDecimal(p64AA.get(tmpRow, tmpCol));
        TestUtils.assertEquals(expNumber, actNumber, ACCURACY);

    }

    @Test
    public void testToComplexNumberIntInt() {

        final int tmpRow = (int) Uniform.randomInteger(rAA.countRows());
        final int tmpCol = (int) Uniform.randomInteger(rAA.countColumns());

        expNumber = ComplexNumber.valueOf(rAA.get(tmpRow, tmpCol));

        actNumber = ComplexNumber.valueOf(cAA.get(tmpRow, tmpCol));
        TestUtils.assertEquals(expNumber, actNumber, ACCURACY);

        actNumber = ComplexNumber.valueOf(p64AA.get(tmpRow, tmpCol));
        TestUtils.assertEquals(expNumber, actNumber, ACCURACY);

    }

    @Test
    public void testToComplexStore() {

        final PhysicalStore<ComplexNumber> tmpExpStore = GenericStore.COMPLEX.copy(rAA);
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

        final Iterable<ColumnView<RationalNumber>> tmpColumns = rAA.columns();

        for (final ColumnView<RationalNumber> tmpColumnView : tmpColumns) {
            final long j = tmpColumnView.column();
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

        final Iterable<RowView<RationalNumber>> tmpRows = rAA.rows();

        for (final RowView<RationalNumber> tmpRowView : tmpRows) {
            final long i = tmpRowView.row();
            for (long j = 0L; j < tmpRowView.count(); j++) {
                TestUtils.assertEquals(tmpRowView.get(j), cAA.get(i, j), ACCURACY);
                TestUtils.assertEquals(tmpRowView.get(j), p64AA.get(i, j), ACCURACY);
            }
        }
    }

    @Test
    public void testToPrimitiveStore() {

        final PhysicalStore<Double> tmpExpStore = Primitive64Store.FACTORY.copy(rAA);
        PhysicalStore<Double> tmpActStore;

        tmpActStore = Primitive64Store.FACTORY.copy(cAA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, ACCURACY);

        tmpActStore = Primitive64Store.FACTORY.copy(p64AA);
        TestUtils.assertEquals(tmpExpStore, tmpActStore, ACCURACY);

    }

    @Test
    public void testToRationalStore() {

        final PhysicalStore<RationalNumber> tmpExpStore = GenericStore.RATIONAL.copy(rAA);
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

        final double[] tmpExpStore = rAA.toRawCopy1D();
        double[] tmpActStore;

        final int tmpFirstIndex = 0;
        final int tmpLastIndex = (int) (rAA.count() - 1);

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

        final int tmpRow = Uniform.randomInteger((int) rAA.countRows());
        final int tmpCol = Uniform.randomInteger((int) rAA.countColumns());

        expNumber = rAA.toScalar(tmpRow, tmpCol).get();

        actNumber = cAA.toScalar(tmpRow, tmpCol).get();
        TestUtils.assertEquals(expNumber, actNumber, ACCURACY);

        actNumber = p64AA.toScalar(tmpRow, tmpCol).get();
        TestUtils.assertEquals(expNumber, actNumber, ACCURACY);

    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#transpose()
     */
    @Test
    public void testTranspose() {

        expMtrx = rAA.transpose();

        actMtrx = cAA.transpose();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p64AA.transpose();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        actMtrx = p32AA.transpose();
        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

}
