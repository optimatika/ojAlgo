/*
 * Copyright 1997-2021 Optimatika
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

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.netio.BasicLogger;

/**
 * @author apete
 */
public class CaseLDL extends MatrixDecompositionTests {

    @Override
    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testQuadOptKTH() {

        RawStore mtrxA = RawStore.wrap(new double[][] { { 2, 4, 6, 8 }, { 4, 9, 17, 22 }, { 6, 17, 44, 61 }, { 8, 22, 61, 118 } });
        RawStore mtrxL = RawStore.wrap(new double[][] { { 1, 0, 0, 0 }, { 2, 1, 0, 0 }, { 3, 5, 1, 0 }, { 4, 6, 7, 1 } });
        RawStore mtrxD = RawStore.wrap(new double[][] { { 2, 0, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 1, 0 }, { 0, 0, 0, 1 } });

        this.doTest(mtrxA, mtrxL, mtrxD);
    }

    @Test
    @Disabled
    public void testQuadOptKTHPivot() {

        RawStore mtrxA = RawStore.wrap(new double[][] { { 2, 4, 6, 8 }, { 4, 9, 17, 22 }, { 6, 17, 44, 61 }, { 8, 22, 61, 118 } });
        RawStore mtrxL = RawStore.wrap(new double[][] { { 1, 0, 0, 0 }, { 2, 1, 0, 0 }, { 3, 5, 1, 0 }, { 4, 6, 7, 1 } });
        RawStore mtrxD = RawStore.wrap(new double[][] { { 2, 0, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 1, 0 }, { 0, 0, 0, 1 } });

        MatrixStore<Double> permA = mtrxA.logical().row(3, 2, 1, 0).column(3, 2, 1, 0).get();

        this.doTest(permA, mtrxL, mtrxD);
    }

    @Test
    public void testWikipediaCase() {

        RawStore mtrxA = RawStore.wrap(new double[][] { { 4, 12, -16 }, { 12, 37, -43 }, { -16, -43, 98 } });
        RawStore mtrxL = RawStore.wrap(new double[][] { { 1, 0, 0 }, { 3, 1, 0 }, { -4, 5, 1 } });
        RawStore mtrxD = RawStore.wrap(new double[][] { { 4, 0, 0 }, { 0, 1, 0 }, { 0, 0, 9 } });

        this.doTest(mtrxA, mtrxL, mtrxD);
    }

    @Test
    public void testWikipediaCasePrePivoted() {

        RawStore mtrxA = RawStore.wrap(new double[][] { { 98, -43, -16 }, { -43, 37, 12 }, { -16, 12, 4 } });
        RawStore mtrxL = RawStore
                .wrap(new double[][] { { 1.0, 0.0, 0.0 }, { -0.4387755102040816, 1.0, 0.0 }, { -0.16326530612244897, 0.2746201463140124, 1.0 } });
        RawStore mtrxD = RawStore.wrap(new double[][] { { 98.0, 0.0, 0.0 }, { 0.0, 18.13265306122449, 0.0 }, { 0.0, 0.0, 0.020258863252673898 } });

        this.doTest(mtrxA, mtrxL, mtrxD);
    }

    private void doTest(final MatrixStore<Double> mtrxA, final RawStore mtrxL, final RawStore mtrxD) {

        MatrixStore<Double> mtrxIdentity = Primitive64Store.FACTORY.makeIdentity((int) mtrxA.countRows()).get();

        RawStore reconstructed = mtrxL.multiply(mtrxD.multiply(mtrxL.transpose()));
        TestUtils.assertEquals(mtrxA, reconstructed);

        // TODO Change to new RawLDL() when it's done
        LDL<Double> rawLDL = new LDLDecomposition.Primitive();
        rawLDL.decompose(mtrxA);

        LDL<Double> primLDL = new LDLDecomposition.Primitive();
        primLDL.decompose(mtrxA.logical().triangular(false, false).get());

        if (MatrixDecompositionTests.DEBUG) {

            BasicLogger.debug("Expected L", mtrxL);
            BasicLogger.debug("Expected D", mtrxD);

            BasicLogger.debug("RAW P: {}", Arrays.toString(rawLDL.getPivotOrder()));
            BasicLogger.debug("RAW L", rawLDL.getL());
            BasicLogger.debug("RAW D", rawLDL.getD());

            BasicLogger.debug("PRIM P: {}", Arrays.toString(primLDL.getPivotOrder()));
            BasicLogger.debug("PRIM L", primLDL.getL());
            BasicLogger.debug("PRIM D", primLDL.getD());
        }

        if (!rawLDL.isPivoted()) {
            TestUtils.assertEquals(mtrxL, rawLDL.getL());
            TestUtils.assertEquals(mtrxD, rawLDL.getD());
        }

        if (!primLDL.isPivoted()) {
            TestUtils.assertEquals(mtrxL, primLDL.getL());
            TestUtils.assertEquals(mtrxD, primLDL.getD());
        }

        TestUtils.assertEquals(mtrxA, rawLDL.reconstruct());
        TestUtils.assertEquals(mtrxA, primLDL.reconstruct());

        MatrixStore<Double> rawInv = rawLDL.getInverse();
        MatrixStore<Double> primInv = primLDL.getInverse();

        TestUtils.assertEquals(primInv, rawInv);
        TestUtils.assertEquals(mtrxIdentity, primInv.multiply(mtrxA));
        TestUtils.assertEquals(mtrxIdentity, rawInv.multiply(mtrxA));

        MatrixStore<Double> rawSol = rawLDL.getSolution(mtrxIdentity);
        MatrixStore<Double> primSol = primLDL.getSolution(mtrxIdentity);

        TestUtils.assertEquals(primSol, rawSol);
        TestUtils.assertEquals(mtrxIdentity, primSol.multiply(mtrxA));
        TestUtils.assertEquals(mtrxIdentity, rawSol.multiply(mtrxA));
    }
}
