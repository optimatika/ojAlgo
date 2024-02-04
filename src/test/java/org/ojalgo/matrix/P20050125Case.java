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
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * There was a problem with the solve() method in the Jama Cholesky decomposition - it simply wasn't correct.
 * (This case tests that MatrixQ128 doesn't have the same problem.) Problem reported to jama(a)nist.gov
 *
 * @author apete
 */
public class P20050125Case extends BasicMatrixTest {

    private static final NumberContext DEFINITION = NumberContext.of(7, 9);

    public static MatrixR064 getProblematic() {
        int DIM = 3;
        MatrixR064 tmpMtrx = MatrixR064.FACTORY.makeFilled(DIM, DIM * DIM, new Uniform());
        return tmpMtrx.multiply(tmpMtrx.transpose());
    }

    @Override
    @BeforeEach
    public void doBeforeEach() {

        mtrxA = P20050125Case.getProblematic();
        mtrxX = BasicMatrixTest.getIdentity(mtrxA.countColumns(), mtrxA.countColumns(), DEFINITION);
        mtrxB = mtrxA;

        mtrxI = BasicMatrixTest.getIdentity(mtrxA.countRows(), mtrxA.countColumns(), DEFINITION);
        mtrxSafe = BasicMatrixTest.getSafe(mtrxA.countRows(), mtrxA.countColumns(), DEFINITION);

        super.doBeforeEach();
    }

    @Test
    public void testData() {

        Cholesky<RationalNumber> tmpDelegate = Cholesky.RATIONAL.make();
        tmpDelegate.decompose(GenericStore.Q128.copy(mtrxA));

        TestUtils.assertEquals(GenericStore.Q128.copy(mtrxA), tmpDelegate, ACCURACY);
    }

    @Test
    public void testProblem() {

        Cholesky<RationalNumber> tmpDelegate = Cholesky.RATIONAL.make();
        tmpDelegate.decompose(GenericStore.Q128.copy(mtrxA));

        MatrixStore<RationalNumber> tmpInv = tmpDelegate.getSolution(GenericStore.Q128.copy(mtrxI));

        MatrixStore<RationalNumber> tmpExpMtrx = GenericStore.Q128.copy(mtrxI);
        MatrixStore<RationalNumber> tmpActMtrx = GenericStore.Q128.copy(mtrxA).multiply(tmpInv);

        TestUtils.assertEquals(tmpExpMtrx, tmpActMtrx, ACCURACY);
    }

    @Override
    @Test
    public void testSolveMatrix() {
        super.testSolveMatrix();
    }

}
