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
import org.ojalgo.array.Array2D;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.ComplexMath;
import org.ojalgo.random.Normal;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * Found a problem with calculating the Frobenius norm for complex matrices.
 *
 * @author apete
 */
public class P20050827Case extends BasicMatrixTest {

    private static final NumberContext DEFINITION = NumberContext.ofScale(12);

    /**
     * @return A fat, 3x5, matrix with complex valued elements.
     */
    public static MatrixC128 getProblematic() {

        Normal tmpRand = new Normal(0.0, 9.9);
        ComplexNumber tmpNmbr;

        int tmpRowDim = 3;
        int tmpColDim = 5;

        Array2D<ComplexNumber> tmpArray = Array2D.C128.make(tmpRowDim, tmpColDim);

        for (int i = 0; i < tmpRowDim; i++) {
            for (int j = 0; j < tmpColDim; j++) {
                tmpNmbr = ComplexNumber.makePolar(tmpRand.doubleValue(), tmpRand.doubleValue()).multiply(ComplexNumber.ONE);
                tmpArray.set(i, j, tmpNmbr);
            }
        }

        return MatrixC128.FACTORY.copy(tmpArray).enforce(DEFINITION);
    }

    @Override
    @BeforeEach
    public void doBeforeEach() {

        mtrxA = MatrixR064.FACTORY.copy(P20050827Case.getProblematic());
        mtrxX = BasicMatrixTest.getIdentity(mtrxA.countColumns(), mtrxA.countColumns(), DEFINITION);
        mtrxB = mtrxA;

        mtrxI = BasicMatrixTest.getIdentity(mtrxA.countRows(), mtrxA.countColumns(), DEFINITION);
        mtrxSafe = BasicMatrixTest.getSafe(mtrxA.countRows(), mtrxA.countColumns(), DEFINITION);

        super.doBeforeEach();
    }

    @Test
    public void testData() {

        // 3x5
        MatrixC128 tmpProblematic = P20050827Case.getProblematic();
        TestUtils.assertEquals(3, tmpProblematic.countRows());
        TestUtils.assertEquals(5, tmpProblematic.countColumns());

        // 5x5
        MatrixC128 tmpBig = tmpProblematic.conjugate().multiply(tmpProblematic);
        TestUtils.assertEquals(5, tmpBig.countRows());
        TestUtils.assertEquals(5, tmpBig.countColumns());

        // 3x3
        MatrixC128 tmpSmall = tmpProblematic.multiply(tmpProblematic.conjugate());
        TestUtils.assertEquals(3, tmpSmall.countRows());
        TestUtils.assertEquals(3, tmpSmall.countColumns());

        Scalar<ComplexNumber> tmpBigTrace = tmpBig.getTrace();
        Scalar<ComplexNumber> tmpSmallTrace = tmpSmall.getTrace();

        for (int ij = 0; ij < 3; ij++) {
            TestUtils.assertTrue(tmpSmall.toScalar(ij, ij).toString(), tmpSmall.get(ij, ij).isReal());
        }

        for (int ij = 0; ij < 5; ij++) {
            TestUtils.assertTrue(tmpBig.toScalar(ij, ij).toString(), tmpBig.get(ij, ij).isReal());
        }

        TestUtils.assertEquals("Scalar<?> != Scalar<?>", tmpBigTrace.get(), tmpSmallTrace.get(), ACCURACY);
    }

    @Test
    public void testProblem() {

        MatrixC128 tmpProblematic = P20050827Case.getProblematic();

        MatrixC128 tmpMtrx = tmpProblematic.multiply(tmpProblematic.conjugate());
        ComplexNumber tmpVal = tmpMtrx.getTrace().get();
        ComplexNumber tmpExpected = ComplexMath.ROOT.invoke(tmpVal, 2);
        ComplexNumber tmpActual = ComplexNumber.valueOf(tmpProblematic.aggregateAll(Aggregator.NORM2));

        TestUtils.assertEquals(tmpExpected.norm(), tmpActual.norm(), ACCURACY);
        TestUtils.assertEquals(tmpExpected, tmpActual, ACCURACY);

    }

}
