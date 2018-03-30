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
import org.ojalgo.array.Array2D;
import org.ojalgo.function.ComplexFunction;
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

    /**
     * @return A fat, 3x5, matrix with complex valued elements.
     */
    public static ComplexMatrix getProblematic() {

        final Normal tmpRand = new Normal(0.0, 9.9);
        ComplexNumber tmpNmbr;

        final int tmpRowDim = 3;
        final int tmpColDim = 5;

        final Array2D<ComplexNumber> tmpArray = Array2D.COMPLEX.makeZero(tmpRowDim, tmpColDim);

        for (int i = 0; i < tmpRowDim; i++) {
            for (int j = 0; j < tmpColDim; j++) {
                tmpNmbr = ComplexNumber.makePolar(tmpRand.doubleValue(), tmpRand.doubleValue()).multiply(ComplexNumber.ONE);
                tmpArray.set(i, j, tmpNmbr);
            }
        }

        return ComplexMatrix.FACTORY.copy(tmpArray).enforce(DEFINITION);
    }

    @BeforeEach
    @Override
    public void setUp() {

        DEFINITION = NumberContext.getGeneral(12);
        EVALUATION = NumberContext.getGeneral(6).newPrecision(12);

        myBigAA = RationalMatrix.FACTORY.copy(P20050827Case.getProblematic());
        myBigAX = BasicMatrixTest.getIdentity(myBigAA.countColumns(), myBigAA.countColumns(), DEFINITION);
        myBigAB = myBigAA;

        myBigI = BasicMatrixTest.getIdentity(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);
        myBigSafe = BasicMatrixTest.getSafe(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);

        super.setUp();
    }

    @Test
    public void testData() {

        // 3x5
        final ComplexMatrix tmpProblematic = P20050827Case.getProblematic();
        TestUtils.assertEquals(3, tmpProblematic.countRows());
        TestUtils.assertEquals(5, tmpProblematic.countColumns());

        // 5x5
        final ComplexMatrix tmpBig = tmpProblematic.conjugate().multiply(tmpProblematic);
        TestUtils.assertEquals(5, tmpBig.countRows());
        TestUtils.assertEquals(5, tmpBig.countColumns());

        // 3x3
        final ComplexMatrix tmpSmall = tmpProblematic.multiply(tmpProblematic.conjugate());
        TestUtils.assertEquals(3, tmpSmall.countRows());
        TestUtils.assertEquals(3, tmpSmall.countColumns());

        final Scalar<ComplexNumber> tmpBigTrace = tmpBig.getTrace();
        final Scalar<ComplexNumber> tmpSmallTrace = tmpSmall.getTrace();

        for (int ij = 0; ij < 3; ij++) {
            TestUtils.assertTrue(tmpSmall.toScalar(ij, ij).toString(), tmpSmall.get(ij, ij).isReal());
        }

        for (int ij = 0; ij < 5; ij++) {
            TestUtils.assertTrue(tmpBig.toScalar(ij, ij).toString(), tmpBig.get(ij, ij).isReal());
        }

        TestUtils.assertEquals("Scalar<?> != Scalar<?>", tmpBigTrace.get(), tmpSmallTrace.get(), EVALUATION);
    }

    @Test
    public void testProblem() {

        final ComplexMatrix tmpProblematic = P20050827Case.getProblematic();

        final ComplexMatrix tmpMtrx = tmpProblematic.multiply(tmpProblematic.conjugate());
        final ComplexNumber tmpVal = tmpMtrx.getTrace().get();
        final ComplexNumber tmpExpected = ComplexFunction.ROOT.invoke(tmpVal, 2);
        final ComplexNumber tmpActual = ComplexNumber.valueOf(tmpProblematic.norm());

        TestUtils.assertEquals(tmpExpected.norm(), tmpActual.norm(), EVALUATION);
        TestUtils.assertEquals(tmpExpected, tmpActual, EVALUATION);

    }

}
