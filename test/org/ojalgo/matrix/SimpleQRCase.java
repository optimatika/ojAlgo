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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * Gilbert Strang, Linear Algebra and its Applications III, Problem 3.4.16
 *
 * @author apete
 */
public class SimpleQRCase extends BasicMatrixTest {

    private static final NumberContext DEFINITION = NumberContext.getGeneral(9).withScale(18);

    public static RationalMatrix getOriginal() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 1.0, 1.0 }, { 2.0, 3.0 }, { 2.0, 1.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    private static RationalMatrix getFactorQ() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 1.0 / 3.0, 0.0 }, { 2.0 / 3.0, 1.0 / PrimitiveFunction.SQRT.invoke(2.0) },
                { 2.0 / 3.0, -1.0 / PrimitiveFunction.SQRT.invoke(2.0) } });
        return tmpMtrx.enforce(DEFINITION);
    }

    private static RationalMatrix getFactorR() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 3.0, 3.0 }, { 0.0, PrimitiveFunction.SQRT.invoke(2.0) } });
        return tmpMtrx.enforce(DEFINITION);
    }

    @Override
    @BeforeEach
    public void setUp() {

        evaluation = evaluation.withScale(9).withPrecision(15);

        rationalAA = SimpleQRCase.getFactorQ();
        rationalAX = SimpleQRCase.getFactorR();
        rationalAB = SimpleQRCase.getOriginal();

        rationlI = BasicMatrixTest.getIdentity(rationalAA.countRows(), rationalAA.countColumns(), evaluation);
        rationalSafe = BasicMatrixTest.getSafe(rationalAA.countRows(), rationalAA.countColumns(), evaluation);

        super.setUp();
    }

    @Test
    public void testData() {

        expMtrx = SimpleQRCase.getOriginal();
        final RationalMatrix tmpFactorQ = SimpleQRCase.getFactorQ();
        final RationalMatrix tmpFactorR = SimpleQRCase.getFactorR();
        actMtrx = tmpFactorQ.multiply(tmpFactorR);

        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);
    }

    @Test
    public void testProblem() {

        // QR

        final QR<RationalNumber> tmpQR = QR.RATIONAL.make();
        tmpQR.decompose(GenericDenseStore.RATIONAL.copy(SimpleQRCase.getOriginal()));

        final MatrixStore<RationalNumber> tmpQ = tmpQR.getQ();
        final MatrixStore<RationalNumber> tmpR = tmpQR.getR();

        expMtrx = SimpleQRCase.getOriginal();
        actMtrx = RationalMatrix.FACTORY.copy(tmpQ.multiply(tmpR));

        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        // Q

        expMtrx = SimpleQRCase.getFactorQ();
        actMtrx = RationalMatrix.FACTORY.copy(tmpQ);

        // TODO JUnitUtils.assertEquals(myExpected, myActual);

        // R

        expMtrx = SimpleQRCase.getFactorR();
        actMtrx = RationalMatrix.FACTORY.copy(tmpR);

        // TODO JUnitUtils.assertEquals(myExpected, myActual);
    }

}
