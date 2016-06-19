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

import org.ojalgo.TestUtils;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.random.Uniform;
import org.ojalgo.type.context.NumberContext;

/**
 * There was a problem with the solve() method in the Jama Cholesky decomposition - it simply wasn't correct.
 * (This case tests that BigMatrix doesn't have the same problem.) Problem reported to jama(a)nist.gov
 *
 * @author apete
 */
public class P20050125Case extends BasicMatrixTest {

    private static int DIM = 3;

    public static BigMatrix getProblematic() {
        final BigMatrix tmpMtrx = BigMatrix.FACTORY.makeFilled(DIM, DIM * DIM, new Uniform());
        return tmpMtrx.multiply(tmpMtrx.transpose());
    }

    public P20050125Case() {
        super();
    }

    public P20050125Case(final String arg0) {
        super(arg0);
    }

    @Override
    public void testData() {

        final Cholesky<BigDecimal> tmpDelegate = Cholesky.BIG.make();
        tmpDelegate.decompose(myBigAA.toBigStore());

        TestUtils.assertEquals(myBigAA.toBigStore(), tmpDelegate, EVALUATION);
    }

    @Override
    public void testProblem() {

        final Cholesky<BigDecimal> tmpDelegate = Cholesky.BIG.make();
        tmpDelegate.decompose(myBigAA.toBigStore());

        final MatrixStore<BigDecimal> tmpInv = tmpDelegate.solve(myBigI.toBigStore());

        final MatrixStore<BigDecimal> tmpExpMtrx = myBigI.toBigStore();
        final MatrixStore<BigDecimal> tmpActMtrx = myBigAA.toBigStore().multiply(tmpInv);

        TestUtils.assertEquals(tmpExpMtrx, tmpActMtrx, EVALUATION);
    }

    @Override
    public void testSolveBasicMatrix() {
        super.testSolveBasicMatrix();
    }

    @Override
    protected void setUp() throws Exception {

        DEFINITION = new NumberContext(7, 9);
        EVALUATION = new NumberContext(7, 6);

        myBigAA = P20050125Case.getProblematic();
        myBigAX = BasicMatrixTest.getIdentity(myBigAA.countColumns(), myBigAA.countColumns(), DEFINITION);
        myBigAB = myBigAA;

        myBigI = BasicMatrixTest.getIdentity(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);
        myBigSafe = BasicMatrixTest.getSafe(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);

        super.setUp();
    }

}
