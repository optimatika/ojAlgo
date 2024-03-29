/*
 * Copyright 1997-2024 Optimatika
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
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.type.context.NumberContext;

/**
 * Gilbert Strang, Linear Algebra and its Applications III, Chapter 5
 *
 * @author apete
 */
public class SimpleEigenvalueCase extends BasicMatrixTest {

    private static final NumberContext DEFINITION = NumberContext.of(7, 14);

    public static MatrixR064 getOriginal() {
        MatrixR064 tmpMtrx = MatrixR064.FACTORY.rows(new double[][] { { 4.0, -5.0 }, { 2.0, -3.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    private static MatrixR064 getMatrixD() {
        MatrixR064 tmpMtrx = MatrixR064.FACTORY.rows(new double[][] { { 2.0, 0.0 }, { 0.0, -1.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    private static MatrixR064 getMatrixV() {
        MatrixR064 tmpMtrx = MatrixR064.FACTORY.rows(new double[][] { { 5.0, 1.0 }, { 2.0, 1.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    @Override
    @BeforeEach
    public void doBeforeEach() {

        mtrxA = SimpleEigenvalueCase.getOriginal();
        mtrxX = SimpleEigenvalueCase.getMatrixV();
        mtrxB = SimpleEigenvalueCase.getMatrixV().multiply(SimpleEigenvalueCase.getMatrixD());

        mtrxI = BasicMatrixTest.getIdentity(mtrxA.countRows(), mtrxA.countColumns(), DEFINITION);
        mtrxSafe = BasicMatrixTest.getSafe(mtrxA.countRows(), mtrxA.countColumns(), DEFINITION);

        super.doBeforeEach();
    }

    @Test
    public void testData() {

        BasicMatrix<?, ?> actMtrx;
        BasicMatrix<?, ?> expMtrx;

        expMtrx = SimpleEigenvalueCase.getOriginal().multiply(SimpleEigenvalueCase.getMatrixV());

        actMtrx = SimpleEigenvalueCase.getMatrixV().multiply(SimpleEigenvalueCase.getMatrixD());

        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

    @Test
    public void testProblem() {

        BasicMatrix<?, ?> actMtrx;
        BasicMatrix<?, ?> expMtrx;

        Eigenvalue<Double> tmpEigen = Eigenvalue.R064.make();
        tmpEigen.decompose(R064Store.FACTORY.copy(SimpleEigenvalueCase.getOriginal()));

        MatrixStore<Double> tmpV = tmpEigen.getV();
        MatrixStore<Double> tmpD = tmpEigen.getD();

        expMtrx = SimpleEigenvalueCase.getMatrixD();
        actMtrx = MatrixR064.FACTORY.copy(tmpD);

        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);

        MatrixR064 tmpExpV = SimpleEigenvalueCase.getMatrixV();
        MatrixR064 tmpActV = MatrixR064.FACTORY.copy(tmpV);

        MatrixR064.DenseReceiver tmpCopy = tmpExpV.copy();
        tmpCopy.modifyMatching(PrimitiveMath.DIVIDE, tmpActV);
        MatrixR064 tmpMtrx = tmpCopy.get();
        double tmpExp;
        double tmpAct;
        for (int j = 0; j < tmpMtrx.countColumns(); j++) {
            tmpExp = tmpMtrx.doubleValue(0, j);
            for (int i = 0; i < tmpMtrx.countRows(); i++) {
                tmpAct = tmpMtrx.doubleValue(i, j);
                TestUtils.assertEquals(tmpExp, tmpAct, ACCURACY);
            }
        }

        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

}
