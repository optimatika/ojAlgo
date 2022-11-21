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
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class SimpleEquationCase extends BasicMatrixTest {

    private static final NumberContext DEFINITION = NumberContext.of(7, 1);

    public static Primitive64Matrix getBody() {
        Primitive64Matrix tmpMtrx = Primitive64Matrix.FACTORY.rows(new double[][] { { 2.0, 1.0, 1.0 }, { 4.0, -6.0, 0.0 }, { -2.0, 7.0, 2.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    public static Primitive64Matrix getRHS() {
        Primitive64Matrix tmpMtrx = Primitive64Matrix.FACTORY.rows(new double[][] { { 5.0 }, { -2.0 }, { 9.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    public static Primitive64Matrix getSolution() {
        Primitive64Matrix tmpMtrx = Primitive64Matrix.FACTORY.rows(new double[][] { { 1.0 }, { 1.0 }, { 2.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    @Override
    @BeforeEach
    public void doBeforeEach() {

        mtrxA = SimpleEquationCase.getBody();
        mtrxX = SimpleEquationCase.getSolution();
        mtrxB = SimpleEquationCase.getRHS();

        mtrxI = BasicMatrixTest.getIdentity(mtrxA.countRows(), mtrxA.countColumns(), DEFINITION);
        mtrxSafe = BasicMatrixTest.getSafe(mtrxA.countRows(), mtrxA.countColumns(), DEFINITION);

        super.doBeforeEach();
    }

}
