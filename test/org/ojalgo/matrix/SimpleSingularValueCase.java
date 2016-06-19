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
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.type.context.NumberContext;

/**
 * Gilbert Strang, Linear Algebra and its Applications III, Example 1 in Appendix A
 *
 * @author apete
 */
public class SimpleSingularValueCase extends BasicMatrixTest {

    public static BigMatrix getMatrixD() {
        final BigMatrix tmpMtrx = BigMatrix.FACTORY.rows(new double[][] { { 2.0, 0.0 }, { 0.0, 3.0 }, { 0.0, 0.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    public static BigMatrix getMatrixQ1() {
        final BigMatrix tmpMtrx = BigMatrix.FACTORY.rows(new double[][] { { 1.0, 0.0, 0.0 }, { 0.0, -1.0, 0.0 }, { 0.0, 0.0, 1.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    public static BigMatrix getMatrixQ2() {
        final BigMatrix tmpMtrx = BigMatrix.FACTORY.rows(new double[][] { { 1.0, 0.0 }, { 0.0, 1.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    public static BigMatrix getOriginal() {
        final BigMatrix tmpMtrx = BigMatrix.FACTORY.rows(new double[][] { { 2.0, 0.0 }, { 0.0, -3.0 }, { 0.0, 0.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    public SimpleSingularValueCase() {
        super();
    }

    public SimpleSingularValueCase(final String arg0) {
        super(arg0);
    }

    @Override
    public void testData() {

        final PhysicalStore<Double> tmpExp = SimpleSingularValueCase.getOriginal().toPrimitiveStore()
                .multiply(SimpleSingularValueCase.getMatrixQ2().toPrimitiveStore()).copy();

        final PhysicalStore<Double> tmpAct = SimpleSingularValueCase.getMatrixQ1().toPrimitiveStore()
                .multiply(SimpleSingularValueCase.getMatrixD().toPrimitiveStore()).copy();

        TestUtils.assertEquals(tmpExp, tmpAct, EVALUATION);
    }

    @Override
    public void testInvert() {
        super.testInvert();
    }

    @Override
    public void testProblem() {

        final MatrixStore<Double> tmpA = SimpleSingularValueCase.getOriginal().toPrimitiveStore();

        final SingularValue<Double> tmpSVD = SingularValue.make(tmpA);
        tmpSVD.decompose(tmpA);

        tmpSVD.equals(tmpA, EVALUATION);
    }

    @Override
    public void testSolveBasicMatrix() {
        super.testSolveBasicMatrix();
    }

    @Override
    protected void setUp() throws Exception {

        DEFINITION = new NumberContext(7, 1);
        EVALUATION = new NumberContext(7, 9);

        myBigAA = SimpleSingularValueCase.getMatrixQ1();
        myBigAX = SimpleSingularValueCase.getMatrixD();
        myBigAB = SimpleSingularValueCase.getOriginal();

        myBigI = BasicMatrixTest.getIdentity(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);
        myBigSafe = BasicMatrixTest.getSafe(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);

        super.setUp();
    }

}
