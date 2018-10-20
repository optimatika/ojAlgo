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
import org.ojalgo.function.RationalFunction;
import org.ojalgo.matrix.BasicMatrix.PhysicalReceiver;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * Gilbert Strang, Linear Algebra and its Applications III, Chapter 5
 *
 * @author apete
 */
public class SimpleEigenvalueCase extends BasicMatrixTest {

    private static final NumberContext DEFINITION = new NumberContext(7, 14);

    public static RationalMatrix getOriginal() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 4.0, -5.0 }, { 2.0, -3.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    private static RationalMatrix getMatrixD() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 2.0, 0.0 }, { 0.0, -1.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    private static RationalMatrix getMatrixV() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 5.0, 1.0 }, { 2.0, 1.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    @Override
    @BeforeEach
    public void setUp() {

        evaluation = new NumberContext(7, 3);

        rationalAA = SimpleEigenvalueCase.getOriginal();
        rationalAX = SimpleEigenvalueCase.getMatrixV();
        rationalAB = SimpleEigenvalueCase.getMatrixV().multiply(SimpleEigenvalueCase.getMatrixD());

        rationlI = BasicMatrixTest.getIdentity(rationalAA.countRows(), rationalAA.countColumns(), DEFINITION);
        rationalSafe = BasicMatrixTest.getSafe(rationalAA.countRows(), rationalAA.countColumns(), DEFINITION);

        super.setUp();
    }

    @Test
    public void testData() {

        expMtrx = SimpleEigenvalueCase.getOriginal().multiply(SimpleEigenvalueCase.getMatrixV());

        actMtrx = SimpleEigenvalueCase.getMatrixV().multiply(SimpleEigenvalueCase.getMatrixD());

        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);
    }

    @Test
    public void testProblem() {

        final Eigenvalue<Double> tmpEigen = Eigenvalue.PRIMITIVE.make();
        tmpEigen.decompose(PrimitiveDenseStore.FACTORY.copy(SimpleEigenvalueCase.getOriginal()));

        final MatrixStore<Double> tmpV = tmpEigen.getV();
        final MatrixStore<Double> tmpD = tmpEigen.getD();

        expMtrx = SimpleEigenvalueCase.getMatrixD();
        actMtrx = PrimitiveMatrix.FACTORY.copy(tmpD);

        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);

        final RationalMatrix tmpExpV = SimpleEigenvalueCase.getMatrixV();
        final RationalMatrix tmpActV = RationalMatrix.FACTORY.copy(tmpV);

        PhysicalReceiver<RationalNumber, RationalMatrix> tmpCopy = tmpExpV.copy();
        tmpCopy.modifyMatching(RationalFunction.DIVIDE, tmpActV);
        final RationalMatrix tmpMtrx = tmpCopy.get();
        double tmpExp;
        double tmpAct;
        for (int j = 0; j < tmpMtrx.countColumns(); j++) {
            tmpExp = tmpMtrx.doubleValue(0, j);
            for (int i = 0; i < tmpMtrx.countRows(); i++) {
                tmpAct = tmpMtrx.doubleValue(i, j);
                TestUtils.assertEquals(tmpExp, tmpAct, evaluation);
            }
        }

        TestUtils.assertEquals(expMtrx, actMtrx, evaluation);
    }

}
