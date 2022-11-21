/*
 * Copyright 1997-2022 Optimatika
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * Gilbert Strang, Linear Algebra and its Applications III, Problem 3.4.16
 *
 * @author apete
 */
public class SimpleQRCase extends BasicMatrixTest {

    private static final NumberContext DEFINITION = NumberContext.ofScale(9).withScale(18);

    public static Primitive64Matrix getOriginal() {
        Primitive64Matrix tmpMtrx = Primitive64Matrix.FACTORY.rows(new double[][] { { 1.0, 1.0 }, { 2.0, 3.0 }, { 2.0, 1.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    private static Primitive64Matrix getFactorQ() {
        Primitive64Matrix tmpMtrx = Primitive64Matrix.FACTORY.rows(new double[][] { { 1.0 / 3.0, 0.0 }, { 2.0 / 3.0, 1.0 / PrimitiveMath.SQRT.invoke(2.0) },
                { 2.0 / 3.0, -1.0 / PrimitiveMath.SQRT.invoke(2.0) } });
        return tmpMtrx.enforce(DEFINITION);
    }

    private static Primitive64Matrix getFactorR() {
        Primitive64Matrix tmpMtrx = Primitive64Matrix.FACTORY.rows(new double[][] { { 3.0, 3.0 }, { 0.0, PrimitiveMath.SQRT.invoke(2.0) } });
        return tmpMtrx.enforce(DEFINITION);
    }

    @Override
    @BeforeEach
    public void doBeforeEach() {

        mtrxA = SimpleQRCase.getFactorQ();
        mtrxX = SimpleQRCase.getFactorR();
        mtrxB = SimpleQRCase.getOriginal();

        mtrxI = BasicMatrixTest.getIdentity(mtrxA.countRows(), mtrxA.countColumns(), ACCURACY);
        mtrxSafe = BasicMatrixTest.getSafe(mtrxA.countRows(), mtrxA.countColumns(), ACCURACY);

        super.doBeforeEach();
    }

    @Test
    public void testData() {

        BasicMatrix<?, ?> actMtrx;
        BasicMatrix<?, ?> expMtrx;

        expMtrx = SimpleQRCase.getOriginal();
        Primitive64Matrix tmpFactorQ = SimpleQRCase.getFactorQ();
        Primitive64Matrix tmpFactorR = SimpleQRCase.getFactorR();
        actMtrx = tmpFactorQ.multiply(tmpFactorR);

        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    @Test
    public void testProblem() {

        BasicMatrix<?, ?> actMtrx;
        BasicMatrix<?, ?> expMtrx;

        // QR

        QR<RationalNumber> tmpQR = QR.RATIONAL.make();
        tmpQR.decompose(GenericStore.RATIONAL.copy(SimpleQRCase.getOriginal()));

        MatrixStore<RationalNumber> tmpQ = tmpQR.getQ();
        MatrixStore<RationalNumber> tmpR = tmpQR.getR();

        expMtrx = SimpleQRCase.getOriginal();
        actMtrx = Primitive64Matrix.FACTORY.copy(tmpQ.multiply(tmpR));

        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        // Q

        expMtrx = SimpleQRCase.getFactorQ();
        actMtrx = Primitive64Matrix.FACTORY.copy(tmpQ);

        // TODO JUnitUtils.assertEquals(myExpected, myActual);

        // R

        expMtrx = SimpleQRCase.getFactorR();
        actMtrx = Primitive64Matrix.FACTORY.copy(tmpR);

        // TODO JUnitUtils.assertEquals(myExpected, myActual);
    }

}
