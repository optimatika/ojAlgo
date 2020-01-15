/*
 * Copyright 1997-2020 Optimatika
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
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * Problem reported on the ojalgu-user mailing list. A user claimed that Colt, Jama & Mtj calculated
 * eigenvalue decompositions that do not satisfy: [A][V] = [D][V]
 * </p>
 * <p>
 * The problem/solution was that you have to compare [A][V] = [V][D] instead.
 * </p>
 * <p>
 * The supplied matrix did however cause other problems...
 * </p>
 * <p>
 * (2009-04-13, years later) Unfortunately I forgot to document what those other problems were. I assume the
 * matrix is just generally numerically difficult.
 * </p>
 *
 * @author apete
 */
public class P20061119Case extends BasicMatrixTest {

    private static final NumberContext DEFINITION = new NumberContext(7, 2);

    public static RationalMatrix getProblematic() {

        final RationalMatrix retVal = RationalMatrix.FACTORY.rows(new double[][] { { 9.28, 0.48, -2.72, 1.28, -8.32 }, { 4.48, 0.68, -6.52, 2.48, -1.12 },
                { -8.32, -0.12, 8.68, -2.32, 2.08 }, { 7.68, 0.88, -10.32, 3.68, -1.92 }, { -13.12, -1.92, 10.88, -5.12, 9.28 } });

        return retVal.enforce(DEFINITION);
    }

    @Override
    @BeforeEach
    public void doBeforeEach() {

        // evaluation = NumberContext.getGeneral(8).withPrecision(14);

        rAA = P20061119Case.getProblematic();
        rAX = BasicMatrixTest.getIdentity(rAA.countColumns(), rAA.countColumns(), DEFINITION);
        rAB = rAA;

        rI = BasicMatrixTest.getIdentity(rAA.countRows(), rAA.countColumns(), DEFINITION);
        rSafe = BasicMatrixTest.getSafe(rAA.countRows(), rAA.countColumns(), DEFINITION);

        super.doBeforeEach();
    }

    @Test
    public void testData() {
        TestUtils.assertEquals(true, P20061119Case.getProblematic().isSquare());
    }

    @Override
    @Test
    public void testGetRank() {

        int expected = rAA.getRank();
        int actual;

        actual = cAA.getRank();
        TestUtils.assertEquals(expected, actual);

        actual = p64AA.getRank();
        TestUtils.assertEquals(expected, actual);

        // TODO Why doesn't this work?
        // actual = p32AA.getRank();
        // TestUtils.assertEquals(expected, actual);
    }

    @Override
    @Test
    public void testIsFullRank() {

        boolean expected = rAA.isFullRank();
        boolean actual;

        actual = cAA.isFullRank();
        TestUtils.assertEquals(expected, actual);

        actual = p64AA.isFullRank();
        TestUtils.assertEquals(expected, actual);

        // TODO Why doesn't this work?
        // actual = p32AA.isFullRank();
        // TestUtils.assertEquals(expected, actual);
    }

    @Test
    public void testProblem() {

        final RationalMatrix tmpMatrix = P20061119Case.getProblematic();

        final Eigenvalue<Double> tmpEigenvalue = Eigenvalue.PRIMITIVE.make();
        final PhysicalStore<Double> tmpPrimitiveStore = Primitive64Store.FACTORY.copy(tmpMatrix);
        tmpEigenvalue.decompose(tmpPrimitiveStore);

        TestUtils.assertEquals(tmpPrimitiveStore, tmpEigenvalue, ACCURACY);
    }

}
