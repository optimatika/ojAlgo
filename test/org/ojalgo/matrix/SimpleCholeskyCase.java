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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * This problem is taken from example 2.21 of the Scientific Computing, An Introductory Survey.
 *
 * @author apete
 */
public class SimpleCholeskyCase extends BasicMatrixTest {

    private static final NumberContext DEFINITION = new NumberContext(7, 4);

    /**
     * This matrix is taken from example 2.21 of the Scientific Computing, An Introductory Survey
     *
     * @return The data00 value
     */
    public static RationalMatrix getOriginal() {
        return RationalMatrix.FACTORY.rows(new double[][] { { 3.0, -1.0, -1.0 }, { -1.0, 3.0, -1.0 }, { -1.0, -1.0, 3.0 } }).enforce(DEFINITION);
    }

    private static RationalMatrix getFactorL() {
        return RationalMatrix.FACTORY.rows(new double[][] { { 1.7321, 0.0, 0.0 }, { -0.5774, 1.6330, 0.0 }, { -0.5774, -0.8165, 1.4142 } }).enforce(DEFINITION);
    }

    private static RationalMatrix getFactorR() {
        return RationalMatrix.FACTORY.rows(new double[][] { { 1.7321, -0.5774, -0.5774 }, { 0.0, 1.6330, -0.8165 }, { 0.0, 0.0, 1.4142 } }).enforce(DEFINITION);
    }

    @Override
    @BeforeEach
    public void setUp() {

        evaluation = new NumberContext(4, 3);

        rationalAA = SimpleCholeskyCase.getFactorL();
        rationalAX = SimpleCholeskyCase.getFactorR();
        rationalAB = SimpleCholeskyCase.getOriginal();

        rationlI = BasicMatrixTest.getIdentity(rationalAA.countRows(), rationalAA.countColumns(), DEFINITION);
        rationalSafe = BasicMatrixTest.getSafe(rationalAA.countRows(), rationalAA.countColumns(), DEFINITION);

        super.setUp();
    }

    @Test
    public void testData() {

        final RationalMatrix tmpA = SimpleCholeskyCase.getOriginal();
        final RationalMatrix tmpL = SimpleCholeskyCase.getFactorL();
        final RationalMatrix tmpR = SimpleCholeskyCase.getFactorR();

        expMtrx = tmpL;
        actMtrx = tmpR.transpose();

        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        expMtrx = tmpA;
        actMtrx = tmpL.multiply(tmpR);

        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);
    }

    //    @Test
    //    @Disabled("Was commented uot before JUnit 5 transition")
    //    @Test public void testSolve() {
    //
    //        BasicMatrix tmpMtrx = SimpleCholeskyCase.getOriginal();
    //        Cholesky<BigDecimal> tmpDecomp = ArbitraryCholesky.makeBig();
    //        tmpDecomp.compute(tmpMtrx.toBigStore());
    //
    //        BasicMatrix tmpL = SimpleCholeskyCase.getFactorL();
    //        BasicMatrix tmpR = SimpleCholeskyCase.getFactorR();
    //
    //        PhysicalStore<BigDecimal> tmpXY = dbI.toBigStore().copy();
    //        tmpXY.substituteForwards(tmpL.toBigStore(), false);
    //        tmpXY.substituteBackwards(tmpR.toBigStore(), false);
    //
    //        MatrixStore<BigDecimal> tmpExpMtrx = dbI.toBigStore();
    //        MatrixStore<BigDecimal> tmpActMtrx = tmpMtrx.toBigStore().multiplyRight(tmpXY);
    //
    //        JUnitUtils.assertEquals(tmpExpMtrx, tmpActMtrx, EVAL_CNTXT);
    //    }

    @Test
    public void testProblem() {

        final RationalMatrix tmpMtrx = SimpleCholeskyCase.getOriginal();
        final Cholesky<RationalNumber> tmpDecomp = Cholesky.RATIONAL.make();
        tmpDecomp.decompose(GenericDenseStore.RATIONAL.copy(tmpMtrx));

        TestUtils.assertEquals(GenericDenseStore.RATIONAL.copy(tmpMtrx), tmpDecomp, evaluation);
    }

}
