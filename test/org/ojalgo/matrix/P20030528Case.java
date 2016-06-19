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

import org.ojalgo.TestUtils;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.type.context.NumberContext;

/**
 * Using the original Jama implementation this matrix hung the SVD algorithm. The problem was "only" that the
 * matrix is fat (m smaller than n). A matrix of sixe 54-by-57 is too big for JUnit tests. Reported to
 * jama@nist.gov
 *
 * @author apete
 */
public class P20030528Case extends BasicMatrixTest {

    public static BigMatrix getProblematic() {
        final BigMatrix tmpMtrx = BigMatrix.FACTORY
                .rows(new double[][] { { 1, 0, 0, 0, 0, 0, 1 }, { 0, 1, 0, 0, 0, 1, 0 }, { 0, 0, 1, 0, 1, 0, 0 }, { 0, 0, 0, 1, 0, 0, 0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    public P20030528Case() {
        super();
    }

    public P20030528Case(final String arg0) {
        super(arg0);
    }

    @Override
    public void testData() {

        final BigMatrix tmpProb = P20030528Case.getProblematic();

        TestUtils.assertFalse(tmpProb.isEmpty());
        TestUtils.assertTrue(tmpProb.isFat());
    }

    @Override
    public void testProblem() {

        final PhysicalStore<Double> tmpA = P20030528Case.getProblematic().toPrimitiveStore();

        final SingularValue<Double> tmpSVD = SingularValue.make(tmpA);
        tmpSVD.decompose(tmpA);

        tmpSVD.equals(tmpA, EVALUATION);
    }

    @Override
    protected void setUp() throws Exception {

        DEFINITION = new NumberContext(7, 1);
        EVALUATION = new NumberContext(7, 9);

        myBigAA = P20030528Case.getProblematic();
        myBigAX = BasicMatrixTest.getIdentity(myBigAA.countColumns(), myBigAA.countColumns(), DEFINITION);
        myBigAB = myBigAA;

        myBigI = BasicMatrixTest.getIdentity(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);
        myBigSafe = BasicMatrixTest.getSafe(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);

        super.setUp();
    }

}
