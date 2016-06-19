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
import org.ojalgo.type.context.NumberContext;

/**
 * This problem is taken from example 2.21 of the Scientific Computing, An Introductory Survey.
 *
 * @author apete
 */
public class LargerCholeskyCase extends BasicMatrixTest {

    public static BigMatrix getOriginal() {

        BasicMatrix tmpMtrx = PrimitiveMatrix.FACTORY.copy(MatrixUtils.makeRandomComplexStore(9, 9));
        tmpMtrx = tmpMtrx.multiply(tmpMtrx.transpose());

        return BigMatrix.FACTORY.copy(tmpMtrx);
    }

    public LargerCholeskyCase() {
        super();
    }

    public LargerCholeskyCase(final String arg0) {
        super(arg0);
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrixTest#testData()
     */
    @Override
    public void testData() {

        final MatrixStore<Double> tmpMtrx = LargerCholeskyCase.getOriginal().toPrimitiveStore();
        final Cholesky<Double> tmpDecomp = Cholesky.PRIMITIVE.make();
        tmpDecomp.decompose(tmpMtrx);
        TestUtils.assertEquals(true, tmpDecomp.isSolvable());
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrixTest#testProblem()
     */
    @Override
    public void testProblem() {

        final BasicMatrix tmpMtrx = LargerCholeskyCase.getOriginal();
        final Cholesky<Double> tmpDecomp = Cholesky.PRIMITIVE.make();
        tmpDecomp.decompose(tmpMtrx.toPrimitiveStore());

        TestUtils.assertEquals(tmpMtrx.toPrimitiveStore(), tmpDecomp, EVALUATION);
    }

    @Override
    protected void setUp() throws Exception {

        DEFINITION = new NumberContext(7, 4);
        EVALUATION = new NumberContext(7, 3);

        myBigAB = LargerCholeskyCase.getOriginal();

        final Cholesky<BigDecimal> tmpCholesky = Cholesky.BIG.make();
        tmpCholesky.decompose(myBigAB.toBigStore());

        myBigAA = BigMatrix.FACTORY.copy(tmpCholesky.getL());
        myBigAX = myBigAA.transpose();

        myBigI = BasicMatrixTest.getIdentity(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);
        myBigSafe = BasicMatrixTest.getSafe(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);

        super.setUp();
    }

}
