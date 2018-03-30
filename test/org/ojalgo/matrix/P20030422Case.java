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
import org.ojalgo.type.context.NumberContext;

/**
 * The problem was/is to calculate a numerically correct (6 decimals) inverse. Reported to jama@nist.gov
 *
 * @author apete
 * @see org.ojalgo.matrix.P20030512Case
 */
public class P20030422Case extends BasicMatrixTest {

    public static RationalMatrix getProblematic() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY
                .rows(new double[][] { { 0.973950, 0.132128, -0.009493, 0.052934, -0.069248, 0.015658, -0.008564, 0.004549 },
                        { -0.006969, -0.829742, -0.036236, 0.161777, -0.210089, 0.047385, -0.025882, 0.013746 },
                        { 0.000143, 0.006440, -0.998445, -0.016720, 0.021093, -0.004711, 0.002560, -0.001359 },
                        { -0.000036, -0.001408, 0.000752, -0.955688, -0.169493, 0.027513, -0.013046, 0.006811 },
                        { 0.000020, 0.000783, -0.000406, 0.058420, -0.910235, -0.074152, 0.023345, -0.011574 },
                        { -0.000003, -0.000101, 0.000052, -0.006126, 0.031007, -0.993209, -0.006144, 0.007871 },
                        { 0.000000, 0.000009, -0.000004, 0.000458, -0.001702, 0.002139, -0.946651, 0.219946 },
                        { 0.000000, 0.000002, -0.000001, 0.000085, -0.000388, -0.004230, -0.222064, 0.051624 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    @BeforeEach
    @Override
    public void setUp() {

        DEFINITION = new NumberContext(7, 6);
        EVALUATION = new NumberContext(7, 3);

        myBigAA = P20030422Case.getProblematic();
        myBigAX = BasicMatrixTest.getIdentity(myBigAA.countColumns(), myBigAA.countColumns(), DEFINITION);
        myBigAB = myBigAA;

        myBigI = BasicMatrixTest.getIdentity(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);
        myBigSafe = BasicMatrixTest.getSafe(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);

        super.setUp();
    }

    @Test
    public void testProblem() {

        myExpMtrx = P20030422Case.getProblematic();
        myActMtrx = myExpMtrx.invert().invert();

        // The RationalMatrix implementation can do this do 6 decimals, but not the others
        TestUtils.assertEquals(myExpMtrx, myActMtrx, DEFINITION);
    }

}
