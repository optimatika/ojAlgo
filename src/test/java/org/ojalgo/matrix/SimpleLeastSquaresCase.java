/*
 * Copyright 1997-2025 Optimatika
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
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.type.context.NumberContext;

/**
 * An overdetermined equation system described in Scientific Computing An Introductory Survey II By Micheal T.
 * Heath Example 3.1
 *
 * @author apete
 */
public class SimpleLeastSquaresCase extends BasicMatrixTest {

    private static final NumberContext DEFINITION = NumberContext.of(7, 4);

    public static MatrixR064 getBody() {
        MatrixR064 tmpMtrx = MatrixR064.FACTORY.copy(RawStore
                .wrap(new double[][] { { 1.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0 }, { 0.0, 0.0, 1.0 }, { -1.0, 1.0, 0.0 }, { -1.0, 0.0, 1.0 }, { 0.0, -1.0, 1.0 } }));
        return tmpMtrx.enforce(DEFINITION);
    }

    public static MatrixR064 getRHS() {
        MatrixR064 tmpMtrx = MatrixR064.FACTORY.copy(RawStore.wrap(new double[][] { { 1237 }, { 1941 }, { 2417 }, { 711 }, { 1177 }, { 475 } }));
        return tmpMtrx.enforce(DEFINITION);
    }

    public static MatrixR064 getSolution() {
        MatrixR064 tmpMtrx = MatrixR064.FACTORY.copy(RawStore.wrap(new double[][] { { 1236 }, { 1943 }, { 2416 } }));
        return tmpMtrx.enforce(DEFINITION);
    }

    private static MatrixR064 getFactorR() {
        MatrixR064 tmpMtrx = MatrixR064.FACTORY
                .copy(RawStore.wrap(new double[][] { { -1.7321, 0.5774, 0.5774 }, { 0.0, -1.6330, 0.8165 }, { 0.0, 0.0, -1.4142 } }));
        return tmpMtrx.enforce(DEFINITION);
    }

    private static MatrixR064 getTransformedRHS() {
        MatrixR064 tmpMtrx = MatrixR064.FACTORY.copy(RawStore.wrap(new double[][] { { 376 }, { -1200 }, { -3417 } }));
        return tmpMtrx.enforce(DEFINITION);
    }

    @Override
    @BeforeEach
    public void doBeforeEach() {

        mtrxA = SimpleLeastSquaresCase.getFactorR();
        mtrxX = SimpleLeastSquaresCase.getSolution();
        mtrxB = SimpleLeastSquaresCase.getTransformedRHS();

        mtrxI = BasicMatrixTest.getIdentity(mtrxA.countRows(), mtrxA.countColumns(), DEFINITION);
        mtrxSafe = BasicMatrixTest.getSafe(mtrxA.countRows(), mtrxA.countColumns(), DEFINITION);

        super.doBeforeEach();
    }

    @Test
    public void testData() {

        BasicMatrix<?, ?> actMtrx;
        BasicMatrix<?, ?> expMtrx;

        NumberContext accuracy = NumberContext.of(4, 4); // TODO Something must be wrong here!

        expMtrx = SimpleLeastSquaresCase.getTransformedRHS();
        actMtrx = SimpleLeastSquaresCase.getFactorR().multiply(SimpleLeastSquaresCase.getSolution());

        TestUtils.assertEquals(expMtrx, actMtrx, accuracy);
    }

    @Test
    public void testProblem() {

        BasicMatrix<?, ?> actMtrx;
        BasicMatrix<?, ?> expMtrx;

        // Solve

        expMtrx = SimpleLeastSquaresCase.getSolution();
        actMtrx = SimpleLeastSquaresCase.getBody().solve(SimpleLeastSquaresCase.getRHS());

        TestUtils.assertEquals(expMtrx, actMtrx, ACCURACY);
    }

}
