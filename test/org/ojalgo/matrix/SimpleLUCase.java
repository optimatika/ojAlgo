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
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * Gilbert Strang, Linear Algebra and its Applications III, Problem 3.6.15
 *
 * @author apete
 */
public class SimpleLUCase extends BasicMatrixTest {

    private static final NumberContext DEFINITION = new NumberContext(7, 1);

    public static RationalMatrix getOrginal() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 1.0, -1.0, 0.0 }, { 0.0, 1.0, -1.0 }, { 1.0, 0.0, -1.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    private static RationalMatrix getMtrxL() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 1.0, 0.0 }, { 0.0, 1.0 }, { 1.0, 1.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    private static RationalMatrix getMtrxU() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 1.0, -1.0, 0.0 }, { 0.0, 1.0, -1.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    @Override
    @BeforeEach
    public void doBeforeEach() {

        // ACCURACY = new NumberContext(7, 9);

        rAA = SimpleLUCase.getMtrxL();
        rAX = SimpleLUCase.getMtrxU();
        rAB = SimpleLUCase.getOrginal();

        rI = BasicMatrixTest.getIdentity(rAA.countRows(), rAA.countColumns(), DEFINITION);
        rSafe = BasicMatrixTest.getSafe(rAA.countRows(), rAA.countColumns(), DEFINITION);

        super.doBeforeEach();
    }

    @Test
    public void testData() {

        BasicMatrix<?, ?> actMtrx;
        BasicMatrix<?, ?> expMtrx;

        expMtrx = SimpleLUCase.getOrginal();
        actMtrx = SimpleLUCase.getMtrxL().multiply(SimpleLUCase.getMtrxU());

        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    @Test
    public void testProblem() {

        // PLDU

        final LU<RationalNumber> tmpLU = LU.RATIONAL.make();
        tmpLU.decompose(GenericStore.RATIONAL.copy(SimpleLUCase.getOrginal()));

        TestUtils.assertEquals(GenericStore.RATIONAL.copy(SimpleLUCase.getOrginal()), tmpLU, ACCURACY);
    }

}
