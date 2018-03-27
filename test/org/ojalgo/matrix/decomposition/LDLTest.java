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
package org.ojalgo.matrix.decomposition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.RawStore;

/**
 * @author apete
 */
public class LDLTest {

    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testWikipediaCase() {

        final RawStore tmpA = new RawStore(new double[][] { { 4, 12, -16 }, { 12, 37, -43 }, { -16, -43, 98 } });
        final RawStore tmpL = new RawStore(new double[][] { { 1, 0, 0 }, { 3, 1, 0 }, { -4, 5, 1 } });
        final RawStore tmpD = new RawStore(new double[][] { { 4, 0, 0 }, { 0, 1, 0 }, { 0, 0, 9 } });

        final RawStore tmpReconstructed = tmpL.multiply(tmpD.multiply(tmpL.transpose()));
        TestUtils.assertEquals(tmpA, tmpReconstructed);

        final RawLDL tmpRawLDL = new RawLDL();
        tmpRawLDL.decompose(tmpA);

        final LDL<Double> tmpPrimLDL = new LDLDecomposition.Primitive();
        tmpPrimLDL.decompose(tmpA);

        //        BasicLogger.debug(tmpL);
        //        BasicLogger.debug(tmpD);

        //        BasicLogger.debug("RAW L", tmpRawLDL.getL());
        //        BasicLogger.debug("RAW D", tmpRawLDL.getD());
        //
        //        BasicLogger.debug("PRIM L", tmpPrimLDL.getL());
        //        BasicLogger.debug("PRIM D", tmpPrimLDL.getD());

        TestUtils.assertEquals(tmpL, tmpRawLDL.getL());
        TestUtils.assertEquals(tmpD, tmpRawLDL.getD());

        final MatrixStore<Double> tmpRawInv = tmpRawLDL.getSolution(MatrixStore.PRIMITIVE.makeIdentity(3).get());
        final MatrixStore<Double> tmpPrimInv = tmpPrimLDL.getSolution(MatrixStore.PRIMITIVE.makeIdentity(3).get());

        tmpRawInv.multiply(tmpA);
        tmpPrimInv.multiply(tmpA);

        tmpRawLDL.decompose(tmpRawInv);
        final MatrixStore<Double> tmpInverse2 = tmpRawLDL.getSolution(MatrixStore.PRIMITIVE.makeIdentity(3).get());

        TestUtils.assertEquals(tmpA, tmpInverse2);
    }
}
