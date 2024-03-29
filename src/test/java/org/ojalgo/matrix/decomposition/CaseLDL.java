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
package org.ojalgo.matrix.decomposition;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;

/**
 * @author apete
 */
public class CaseLDL extends MatrixDecompositionTests {

    private static void doTest(final MatrixStore<Double> mtrxA, final RawStore mtrxL, final RawStore mtrxD) {

        MatrixStore<Double> mtrxIdentity = R064Store.FACTORY.makeIdentity((int) mtrxA.countRows());

        RawStore reconstructed = mtrxL.multiply(mtrxD.multiply(mtrxL.transpose()));
        TestUtils.assertEquals(mtrxA, reconstructed);

        // TODO Change to new RawLDL() when it's done
        LDL<Double> rawLDL = new LDLDecomposition.R064();
        rawLDL.decompose(mtrxA);

        LDL<Double> primLDL = new LDLDecomposition.R064();
        primLDL.decompose(mtrxA.triangular(false, false));

        if (MatrixDecompositionTests.DEBUG) {

            BasicLogger.debugMatrix("Expected L", mtrxL);
            BasicLogger.debugMatrix("Expected D", mtrxD);

            BasicLogger.debug("RAW P: {}", Arrays.toString(rawLDL.getPivotOrder()));
            BasicLogger.debugMatrix("RAW L", rawLDL.getL());
            BasicLogger.debugMatrix("RAW D", rawLDL.getD());

            BasicLogger.debug("PRIM P: {}", Arrays.toString(primLDL.getPivotOrder()));
            BasicLogger.debugMatrix("PRIM L", primLDL.getL());
            BasicLogger.debugMatrix("PRIM D", primLDL.getD());
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

    private static void doTestBoundOfModifiedLDL(final MatrixStore<Double> matrix) {

        for (int p = 0; p <= 32; p++) {

            double threshold = Math.pow(10.0, -p);

            LDL<Double> decompLDL = LDL.R064.modified(threshold).make(matrix);
            decompLDL.decompose(matrix);

            MatrixStore<Double> mtrxD = decompLDL.getD();

            for (int ij = 0; ij < mtrxD.getMinDim(); ij++) {
                TestUtils.assertTrue("exp = -" + p, mtrxD.doubleValue(ij, ij) >= threshold);
            }
        }
    }

    private static boolean hasNegativeEigenvalue(MatrixStore<Double> mtrxA) {
        Eigenvalue<Double> EVD = EigenvalueDecomposition.R064.make(mtrxA);
        EVD.decompose(mtrxA);
        if (!EVD.isHermitian()) {
            //            This test was to sensitive
            //            throw new AssertionError(" Matrix should be Hermitian");
        }
        boolean hasNegativeEigenvalue = false;
        for (ComplexNumber eigenvalue : EVD.getEigenvalues()) {
            if (!eigenvalue.isReal()) {
                throw new AssertionError(" Eigenvalue should be real");
            }
            if (eigenvalue.getReal() < 0) {
                hasNegativeEigenvalue = true;
            }
        }
        return hasNegativeEigenvalue;
    }

    private static void throwIfNotSymmetric(MatrixStore<Double> mtrxPD) {
        final Double largest = mtrxPD.aggregateAll(Aggregator.LARGEST);
        final Double epsilon = mtrxPD.subtract(mtrxPD.transpose()).aggregateAll(Aggregator.LARGEST);
        final double relativeError = epsilon / largest;
        if (Math.abs(relativeError) > 1e-15) {
            throw new AssertionError(" Matrix should be symmetric");
        }
    }

    /**
     * The reconstructed matrix was reported to have a negative eigenvalue after (modified) decomposition.
     * (That was probably due to a bug in the "reconstruct" code.)
     */
    static RawStore newProblematic1() {
        double[][] data = {
                { -2.912604368291111E-7, 3.640755460363888E-5, 0.06651608000000023, 0.029126043682911, 0.029126043682911, 0.029126043682911, 0.029126043682911,
                        0.029126043682911, 0.029126043682911 },
                { 3.640755460363888E-5, -0.027332819869656178, -275.74349875789756, -79.59399942876519, -105.54615795739191, -125.17822113209371,
                        -141.93065056561915, -159.56941680369314, -686.9419118437424 },
                { 0.06651608000000023, -275.74349875789756, 0.0, 581704.6490661446, 657389.7856455694, 634139.8892843382, 807559.6633962581, 803722.3361887309,
                        1772853.4312689211 },
                { 0.029126043682911, -79.59399942876519, 581704.6490661446, 11537.629188387507, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.029126043682911, -105.54615795739191, 657389.7856455694, 0.0, 13436.20714481499, 0.0, 0.0, 0.0, 0.0 },
                { 0.029126043682911, -125.17822113209371, 634139.8892843382, 0.0, 0.0, 12552.830903558457, 0.0, 0.0, 0.0 },
                { 0.029126043682911, -141.93065056561915, 807559.6633962581, 0.0, 0.0, 0.0, 19851.98342817498, 0.0, 0.0 },
                { 0.029126043682911, -159.56941680369314, 803722.3361887309, 0.0, 0.0, 0.0, 0.0, 19156.887688073493, 0.0 },
                { 0.029126043682911, -686.9419118437424, 1772853.4312689211, 0.0, 0.0, 0.0, 0.0, 0.0, 2.9126081693566335E15 } };
        return RawStore.wrap(data);
    }

    /**
     * Matrix mentioned in Modified Cholesky Decomposition and Applications by McSweeney, Thomas.
     * (modified-cholesky-decomposition-and-applications.pdf)
     * <p>
     * Just a matrix, from Schnabel and Eskow, with some particular interesting/difficult property.
     */
    static RawStore newSpecialSchnabelEskow() {
        double[][] data = { { 1890.3, -1705.6, -315.8, 3000.3 }, { -1705.6, 1538.3, 284.9, -2706.6 }, { -315.8, 284.9, 52.5, -501.2 },
                { 3000.3, -2706.6, -501.2, 4760.8 } };
        return RawStore.wrap(data);
    }

    @Override
    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testBoundOnGeneratedAllPossitive() {

        MatrixStore<Double> matrix = CaseEigenvalue.newRandom(10000.0, 100.0, 1.0, 0.01, 0.0001);

        CaseLDL.doTestBoundOfModifiedLDL(matrix);
    }

    @Test
    public void testBoundOnGeneratedSomeNegative() {

        MatrixStore<Double> matrix = CaseEigenvalue.newRandom(10000.0, 100.0, 1.0, 0.01, -0.0001);

        CaseLDL.doTestBoundOfModifiedLDL(matrix);
    }

    @Test
    public void testBoundsOnProblematic1() {

        MatrixStore<Double> matrix = CaseLDL.newProblematic1();

        CaseLDL.doTestBoundOfModifiedLDL(matrix);
    }

    @Test
    public void testBoundsOnSpecialSchnabelEskow() {

        MatrixStore<Double> matrix = CaseLDL.newSpecialSchnabelEskow();

        CaseLDL.doTestBoundOfModifiedLDL(matrix);
    }

    @Test
    public void testLDLModified() {

        RawStore mtrxA = RawStore.wrap(new double[][] {
                { 0.0, 2.162205857243941E-10, 7.359481277033227E-6, 7.122465559727183E-6, 1.961873982690657E-6, 1.9476822397761537E-6, 1.979780163298276E-6,
                        1.9511152517151967E-6, 3.194255663618267E-6, 3.1369693153378357E-6, 2.8938378661582233E-6, 3.1236139036970613E-6 },
                { 2.162205857243941E-10, -0.02184782416705, -63.91060441246661, -75.9085631976346, -13.239939715995128, -22.43300034556904, -30.206773299572053,
                        -9.061622203484413, -29.151348354923186, -30.900879254141632, -28.42051620313139, -24.89555674647439 },
                { 7.359481277033227E-6, -63.91060441246661, 0.0, 0.0, -27709.23693584429, -39929.38560952523, -48521.21202926942, -10904.112681735494, 0.0, 0.0,
                        0.0, 0.0 },
                { 7.122465559727183E-6, -75.9085631976346, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -30775.845231982683, -42995.99404879068, -51587.82020311749,
                        -32712.958453289648 },
                { 1.961873982690657E-6, -13.239939715995128, -27709.23693584429, 0.0, 2712.1277428407525, 2802.7405278481065, 11037.099694663495,
                        -8964.395842502854, 0.0, 0.0, 0.0, 0.0 },
                { 1.9476822397761537E-6, -22.43300034556904, -39929.38560952523, 0.0, 2802.7405278481065, 14447.571951410924, -771.7113056143766,
                        -8408.012858779355, 0.0, 0.0, 0.0, 0.0 },
                { 1.979780163298276E-6, -30.206773299572053, -48521.21202926942, 0.0, 11037.099694663495, -771.7113056143766, 167961.81174714575,
                        -5229.026794587395, 0.0, 0.0, 0.0, 0.0 },
                { 1.9511152517151967E-6, -9.061622203484413, -10904.112681735494, 0.0, -8964.395842502854, -8408.012858779355, -5229.026794587395,
                        6469.01670389912, 0.0, 0.0, 0.0, 0.0 },
                { 3.194255663618267E-6, -29.151348354923186, 0.0, -30775.845231982683, 0.0, 0.0, 0.0, 0.0, 19449.89493033873, 2378.321022155628,
                        -2649.661519060273, -11855.031196213946 },
                { 3.1369693153378357E-6, -30.900879254141632, 0.0, -42995.99404879068, 0.0, 0.0, 0.0, 0.0, 2378.321022155628, 11357.838998679135,
                        -2937.0094135800778, -18263.73949521518 },
                { 2.8938378661582233E-6, -28.42051620313139, 0.0, -51587.82020311749, 0.0, 0.0, 0.0, 0.0, -2649.661519060273, -2937.0094135800778,
                        3540.367921075315, -21983.989032245478 },
                { 3.1236139036970613E-6, -24.89555674647439, 0.0, -32712.958453289648, 0.0, 0.0, 0.0, 0.0, -11855.031196213946, -18263.73949521518,
                        -21983.989032245478, 3534.4332646769 } });
        throwIfNotSymmetric(mtrxA);
        if (!hasNegativeEigenvalue(mtrxA)) {
            throw new AssertionError(" At least one Eigenvalue should be negative for this test to be useful");
        }
        final LDL<Double> ldl = LDL.modified(LDL.R064, 1.0).make(mtrxA);
        if (!ldl.decompose(mtrxA)) {
            throw new AssertionError(" Decomposition failed");
        }
        MatrixStore<Double> mtrxPD = ldl.reconstruct();
        throwIfNotSymmetric(mtrxPD);
        if (hasNegativeEigenvalue(mtrxPD)) {
            throw new AssertionError(" Eigenvalues should now be all positive");
        }
    }

    @Test
    public void testNoNegativeEigenvalues() {

        RawStore matrix = CaseLDL.newProblematic1();

        Eigenvalue<Double> decompEvD = Eigenvalue.R064.make(matrix, true);
        decompEvD.computeValuesOnly(matrix);
        Array1D<ComplexNumber> eigenvalues = decompEvD.getEigenvalues();
        if (DEBUG) {
            BasicLogger.debug("Original Eigenvalues:{} ", eigenvalues);
            BasicLogger.debug();
        }

        for (int p = 0; p <= 32; p++) {

            double threshold = Math.pow(10.0, -p);

            LDL<Double> decompLDL = LDL.R064.modified(threshold).make(matrix);
            decompLDL.decompose(matrix);
            MatrixStore<Double> reconstructed = decompLDL.reconstruct();

            decompEvD.computeValuesOnly(reconstructed);
            eigenvalues = decompEvD.getEigenvalues();

            if (DEBUG) {
                BasicLogger.debug("Reconstructed Eigenvalues:{} ", eigenvalues);
            }
            for (ComplexNumber eigenvalue : eigenvalues) {
                TestUtils.assertTrue("exp = -" + p, eigenvalue.doubleValue() >= 0.0);
            }
        }
    }

    @Test
    public void testQuadOptKTH() {

        RawStore mtrxA = RawStore.wrap(new double[][] { { 2, 4, 6, 8 }, { 4, 9, 17, 22 }, { 6, 17, 44, 61 }, { 8, 22, 61, 118 } });
        RawStore mtrxL = RawStore.wrap(new double[][] { { 1, 0, 0, 0 }, { 2, 1, 0, 0 }, { 3, 5, 1, 0 }, { 4, 6, 7, 1 } });
        RawStore mtrxD = RawStore.wrap(new double[][] { { 2, 0, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 1, 0 }, { 0, 0, 0, 1 } });

        CaseLDL.doTest(mtrxA, mtrxL, mtrxD);
    }

    @Test
    public void testReconstructWhenPivoted() {

        MatrixStore<Double> matrix = CaseLDL.newSpecialSchnabelEskow();

        LDL<Double> decomp = LDL.R064.make(matrix);
        decomp.decompose(matrix);

        if (DEBUG) {
            BasicLogger.debugMatrix("Original", matrix);
            BasicLogger.debugMatrix("L", decomp.getL());
            BasicLogger.debugMatrix("D", decomp.getD());
            BasicLogger.debugMatrix("Reconstructed", decomp.reconstruct());
        }

        TestUtils.assertEquals(matrix, decomp.reconstruct());
    }

    @Test
    public void testWikipediaCase() {

        RawStore mtrxA = RawStore.wrap(new double[][] { { 4, 12, -16 }, { 12, 37, -43 }, { -16, -43, 98 } });
        RawStore mtrxL = RawStore.wrap(new double[][] { { 1, 0, 0 }, { 3, 1, 0 }, { -4, 5, 1 } });
        RawStore mtrxD = RawStore.wrap(new double[][] { { 4, 0, 0 }, { 0, 1, 0 }, { 0, 0, 9 } });

        CaseLDL.doTest(mtrxA, mtrxL, mtrxD);
    }

    @Test
    public void testWikipediaCasePrePivoted() {

        RawStore mtrxA = RawStore.wrap(new double[][] { { 98, -43, -16 }, { -43, 37, 12 }, { -16, 12, 4 } });
        RawStore mtrxL = RawStore
                .wrap(new double[][] { { 1.0, 0.0, 0.0 }, { -0.4387755102040816, 1.0, 0.0 }, { -0.16326530612244897, 0.2746201463140124, 1.0 } });
        RawStore mtrxD = RawStore.wrap(new double[][] { { 98.0, 0.0, 0.0 }, { 0.0, 18.13265306122449, 0.0 }, { 0.0, 0.0, 0.020258863252673898 } });

        CaseLDL.doTest(mtrxA, mtrxL, mtrxD);
    }

}
