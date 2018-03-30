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
 * An overdetermined equation system described in Scientific Computing An Introductory Survey II By Micheal T.
 * Heath Example 3.1
 *
 * @author apete
 */
public class SimpleLeastSquaresCase extends BasicMatrixTest {

    public static RationalMatrix getBody() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY
                .rows(new double[][] { { 1.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0 }, { 0.0, 0.0, 1.0 }, { -1.0, 1.0, 0.0 }, { -1.0, 0.0, 1.0 }, { 0.0, -1.0, 1.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    public static RationalMatrix getRHS() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 1237 }, { 1941 }, { 2417 }, { 711 }, { 1177 }, { 475 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    public static RationalMatrix getSolution() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 1236 }, { 1943 }, { 2416 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    private static RationalMatrix getFactorR() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY
                .rows(new double[][] { { -1.7321, 0.5774, 0.5774 }, { 0.0, -1.6330, 0.8165 }, { 0.0, 0.0, -1.4142 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    private static RationalMatrix getTransformedRHS() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 376 }, { -1200 }, { -3417 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    @BeforeEach
    @Override
    public void setUp() {

        DEFINITION = new NumberContext(7, 4);
        EVALUATION = new NumberContext(4, 4); // TODO Something must be wrong here!

        myBigAA = SimpleLeastSquaresCase.getFactorR();
        myBigAX = SimpleLeastSquaresCase.getSolution();
        myBigAB = SimpleLeastSquaresCase.getTransformedRHS();

        myBigI = BasicMatrixTest.getIdentity(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);
        myBigSafe = BasicMatrixTest.getSafe(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);

        super.setUp();
    }

    @Test
    public void testData() {

        myExpMtrx = SimpleLeastSquaresCase.getTransformedRHS();
        myActMtrx = SimpleLeastSquaresCase.getFactorR().multiply(SimpleLeastSquaresCase.getSolution());

        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);
    }

    @Test
    public void testProblem() {

        // Solve

        myExpMtrx = SimpleLeastSquaresCase.getSolution();
        myActMtrx = SimpleLeastSquaresCase.getBody().solve(SimpleLeastSquaresCase.getRHS());

        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);
    }

}
