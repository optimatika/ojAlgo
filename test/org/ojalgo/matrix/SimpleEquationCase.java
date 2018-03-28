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
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class SimpleEquationCase extends BasicMatrixTest {

    public static RationalMatrix getBody() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 2.0, 1.0, 1.0 }, { 4.0, -6.0, 0.0 }, { -2.0, 7.0, 2.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    public static RationalMatrix getRHS() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 5.0 }, { -2.0 }, { 9.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    public static RationalMatrix getSolution() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 1.0 }, { 1.0 }, { 2.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    @BeforeEach
    @Override
    public void setUp() {

        DEFINITION = new NumberContext(7, 1);
        EVALUATION = new NumberContext(7, 9);

        myBigAA = SimpleEquationCase.getBody();
        myBigAX = SimpleEquationCase.getSolution();
        myBigAB = SimpleEquationCase.getRHS();

        myBigI = BasicMatrixTest.getIdentity(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);
        myBigSafe = BasicMatrixTest.getSafe(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);

        super.setUp();
    }

}
