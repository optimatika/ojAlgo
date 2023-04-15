/*
 * Copyright 1997-2023 Optimatika
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
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * Random generated SPD.
 *
 * @author apete
 */
public class LargerCholeskyCase extends BasicMatrixTest {

    private static final NumberContext DEFINITION = NumberContext.of(7, 4);

    public static MatrixR064 getOriginal() {

        PhysicalStore<ComplexNumber> randomComplex = TestUtils.makeRandomComplexStore(9, 9);

        return MatrixR064.FACTORY.copy(randomComplex.multiply(randomComplex.conjugate()));
    }

    @Override
    @BeforeEach
    public void doBeforeEach() {

        mtrxB = LargerCholeskyCase.getOriginal();

        Cholesky<Double> tmpCholesky = Cholesky.PRIMITIVE.make();
        tmpCholesky.decompose(mtrxB);

        mtrxA = MatrixR064.FACTORY.copy(tmpCholesky.getL());
        mtrxX = mtrxA.transpose();

        mtrxI = BasicMatrixTest.getIdentity(mtrxA.countRows(), mtrxA.countColumns(), DEFINITION);
        mtrxSafe = BasicMatrixTest.getSafe(mtrxA.countRows(), mtrxA.countColumns(), DEFINITION);

        super.doBeforeEach();
    }

    @Test
    public void testData() {

        MatrixR064 tmpMtrx = LargerCholeskyCase.getOriginal();
        Cholesky<Double> tmpDecomp = Cholesky.PRIMITIVE.make();
        tmpDecomp.decompose(tmpMtrx);
        TestUtils.assertEquals(true, tmpDecomp.isSolvable());
    }

    @Test
    public void testProblem() {

        MatrixR064 tmpMtrx = LargerCholeskyCase.getOriginal();
        Cholesky<Double> tmpDecomp = Cholesky.PRIMITIVE.make();
        tmpDecomp.decompose(tmpMtrx);

        TestUtils.assertEquals(Primitive64Store.FACTORY.copy(tmpMtrx), tmpDecomp, ACCURACY);
    }

}
