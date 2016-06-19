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
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.store.PhysicalStore;
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

    public static BigMatrix getProblematic() {

        final BigMatrix retVal = BigMatrix.FACTORY.rows(new double[][] { { 9.28, 0.48, -2.72, 1.28, -8.32 }, { 4.48, 0.68, -6.52, 2.48, -1.12 },
                { -8.32, -0.12, 8.68, -2.32, 2.08 }, { 7.68, 0.88, -10.32, 3.68, -1.92 }, { -13.12, -1.92, 10.88, -5.12, 9.28 } });

        return retVal.enforce(DEFINITION);
    }

    public P20061119Case() {
        super();
    }

    public P20061119Case(final String arg0) {
        super(arg0);
    }

    @Override
    public void testData() {
        TestUtils.assertEquals(true, P20061119Case.getProblematic().isSquare());
    }

    @Override
    public void testProblem() {

        final BasicMatrix tmpMatrix = P20061119Case.getProblematic();

        final Eigenvalue<Double> tmpEigenvalue = Eigenvalue.PRIMITIVE.make();
        final PhysicalStore<Double> tmpPrimitiveStore = tmpMatrix.toPrimitiveStore();
        tmpEigenvalue.decompose(tmpPrimitiveStore);

        TestUtils.assertEquals(tmpPrimitiveStore, tmpEigenvalue, EVALUATION);
    }

    @Override
    protected void setUp() throws Exception {

        DEFINITION = new NumberContext(7, 2);
        EVALUATION = NumberContext.getGeneral(8).newPrecision(14);

        myBigAA = P20061119Case.getProblematic();
        myBigAX = BasicMatrixTest.getIdentity(myBigAA.countColumns(), myBigAA.countColumns(), DEFINITION);
        myBigAB = myBigAA;

        myBigI = BasicMatrixTest.getIdentity(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);
        myBigSafe = BasicMatrixTest.getSafe(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);

        super.setUp();
    }

}
