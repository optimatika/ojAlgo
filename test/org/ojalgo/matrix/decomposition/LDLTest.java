/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.TestUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.store.IdentityStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.netio.BasicLogger;

/**
 * @author apete
 */
public class LDLTest extends MatrixDecompositionTests {

    public LDLTest() {
        super();
    }

    public LDLTest(final String arg0) {
        super(arg0);
    }

    public void testStratMixCase() {

        final double[][] tmpRawA = new double[][] { { 1.0, 0.5, 0.5, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.5, 1.0, 0.5, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0 },
                { 0.5, 0.5, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, -1.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, -1.0 },
                { 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 1.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 1.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 1.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 } };
        final RawStore tmpA = RawStore.FACTORY.rows(tmpRawA);

        final SingularValue<Double> tmpSVD = SingularValue.makePrimitive();
        tmpSVD.compute(tmpA);

        int tmpRank = tmpSVD.getRank();
        final boolean tmpSolvable = tmpSVD.isSolvable();

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug(tmpSVD.getSingularValues());
        }

        final QR<Double> tmpPrimQR = new RawQR();
        tmpPrimQR.compute(tmpA);
        tmpRank = tmpPrimQR.getRank();
        TestUtils.assertEquals(tmpSolvable, tmpPrimQR.isSolvable());

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug(tmpPrimQR.getR());
        }

        final RawLU tmpRawLU = new RawLU();
        tmpRawLU.compute(tmpA);
        TestUtils.assertEquals(tmpSolvable, tmpRawLU.isSolvable());

        final LU<Double> tmpPrimLU = new LUDecomposition.Primitive();
        tmpPrimLU.compute(tmpA);
        TestUtils.assertEquals(tmpSolvable, tmpPrimLU.isSolvable());

    }

    public void testWikipediaCase() {

        final Access2D<?> tmpA = new RawStore(new double[][] { { 4, 12, -16 }, { 12, 37, -43 }, { -16, -43, 98 } });
        final RawStore tmpL = new RawStore(new double[][] { { 1, 0, 0 }, { 3, 1, 0 }, { -4, 5, 1 } });
        final RawStore tmpD = new RawStore(new double[][] { { 4, 0, 0 }, { 0, 1, 0 }, { 0, 0, 9 } });

        final RawStore tmpA2 = tmpD.multiply(tmpL.transpose()).multiplyLeft(tmpL);

        TestUtils.assertEquals(tmpA, tmpA2);

        final RawLDL tmpLDL = new RawLDL();
        tmpLDL.compute(tmpA);

        final LDL<Double> tmpPrim = new LDLDecomposition.Primitive();
        tmpPrim.compute(tmpA);

        BasicLogger.debug(tmpL);
        BasicLogger.debug(tmpD);

        BasicLogger.debug(tmpLDL.getL());
        BasicLogger.debug(tmpLDL.getD());

        BasicLogger.debug(tmpPrim.getL());
        BasicLogger.debug(tmpPrim.getD());

        TestUtils.assertEquals(tmpL, tmpLDL.getL());
        TestUtils.assertEquals(tmpD, tmpLDL.getD());

        final MatrixStore<Double> tmpInverse = tmpLDL.solve(IdentityStore.PRIMITIVE.make(3));

        final MatrixStore<Double> tmpIdentity = tmpInverse.multiply((Access1D<Double>) tmpA);

        tmpLDL.compute(tmpInverse);
        final MatrixStore<Double> tmpInverse2 = tmpLDL.solve(IdentityStore.PRIMITIVE.make(3));

        TestUtils.assertEquals(tmpA, tmpInverse2);
    }
}
