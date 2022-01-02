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
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * There was a problem with the solve() method in the Jama Cholesky decomposition - it simply wasn't correct.
 * (This case tests that RationalMatrix doesn't have the same problem.) Problem reported to jama(a)nist.gov
 *
 * @author apete
 */
public class P20050125Case extends BasicMatrixTest {

    private static final NumberContext DEFINITION = NumberContext.of(7, 9);

    public static RationalMatrix getProblematic() {
        int DIM = 3;
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.makeFilled(DIM, DIM * DIM, new Uniform());
        return tmpMtrx.multiply(tmpMtrx.transpose());
    }

    @Override
    @BeforeEach
    public void doBeforeEach() {

        // ACCURACY = new NumberContext(7, 6);

        rAA = P20050125Case.getProblematic();
        rAX = BasicMatrixTest.getIdentity(rAA.countColumns(), rAA.countColumns(), DEFINITION);
        rAB = rAA;

        rI = BasicMatrixTest.getIdentity(rAA.countRows(), rAA.countColumns(), DEFINITION);
        rSafe = BasicMatrixTest.getSafe(rAA.countRows(), rAA.countColumns(), DEFINITION);

        super.doBeforeEach();
    }

    @Test
    public void testData() {

        final Cholesky<RationalNumber> tmpDelegate = Cholesky.RATIONAL.make();
        tmpDelegate.decompose(GenericStore.RATIONAL.copy(rAA));

        TestUtils.assertEquals(GenericStore.RATIONAL.copy(rAA), tmpDelegate, ACCURACY);
    }

    @Test
    public void testProblem() {

        final Cholesky<RationalNumber> tmpDelegate = Cholesky.RATIONAL.make();
        tmpDelegate.decompose(GenericStore.RATIONAL.copy(rAA));

        final MatrixStore<RationalNumber> tmpInv = tmpDelegate.getSolution(GenericStore.RATIONAL.copy(rI));

        final MatrixStore<RationalNumber> tmpExpMtrx = GenericStore.RATIONAL.copy(rI);
        final MatrixStore<RationalNumber> tmpActMtrx = GenericStore.RATIONAL.copy(rAA).multiply(tmpInv);

        TestUtils.assertEquals(tmpExpMtrx, tmpActMtrx, ACCURACY);
    }

    @Override
    @Test
    public void testSolveMatrix() {
        super.testSolveMatrix();
    }

}
