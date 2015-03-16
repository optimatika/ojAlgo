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
package org.ojalgo.optimisation.system;

import org.ojalgo.FunctionalityTest;
import org.ojalgo.TestUtils;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.ArrayUtils;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.BigMatrix;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.ZeroStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.convex.MostBasicCase;
import org.ojalgo.optimisation.convex.Qsd20030327P1Case;
import org.ojalgo.optimisation.convex.Qsd20030409P1Case;
import org.ojalgo.optimisation.convex.QsdOldFundOfFundsCase;
import org.ojalgo.optimisation.convex.QsdOldNormalCase;
import org.ojalgo.optimisation.convex.QsdOldOptimalCase;
import org.ojalgo.type.context.NumberContext;

public class KKTSolverTest extends FunctionalityTest {

    public KKTSolverTest() {
        super();
    }

    public KKTSolverTest(final String someName) {
        super(someName);
    }

    /**
     * Data taken from {@linkplain MostBasicCase}
     */
    public void testMostBasicCase() {

        final BasicMatrix[] tmpMatrices = new BigMatrix[5];

        // Equations/Equalities
        tmpMatrices[0] = BigMatrix.FACTORY.rows(new double[][] { { 1.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0 }, { 0.0, 0.0, 1.0 } });

        // Levels/Values
        tmpMatrices[1] = BigMatrix.FACTORY.rows(new double[][] { { 2.0 }, { 3.0 }, { 4.0 } });

        // Quadratic
        tmpMatrices[2] = BigMatrix.FACTORY.rows(new double[][] { { 1.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0 }, { 0.0, 0.0, 1.0 } });

        // Linear
        tmpMatrices[3] = BigMatrix.FACTORY.rows(new double[][] { { 2.0 }, { 3.0 }, { 4.0 } });

        // LagrangeSolver
        tmpMatrices[4] = BigMatrix.FACTORY.rows(new double[][] { { 2.0 }, { 3.0 }, { 4.0 } });

        this.doTest(tmpMatrices, true, new NumberContext(7, 14));

    }

    /**
     * Data taken from {@linkplain Qsd20030327P1Case}
     */
    public void testQsd20030327P1Case() {

        final BasicMatrix[] tmpMatrices = new BigMatrix[5];

        // Equations/Equalities
        tmpMatrices[0] = BigMatrix.FACTORY.rows(new double[][] { { 1, 1, 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 1, 0, 0 }, { 0, 0, 0, 0, 0, 1, 0 },
                { 0, 0, 0, 0, 0, 0, 1 } });

        // Levels/Values
        tmpMatrices[1] = BigMatrix.FACTORY.rows(new double[][] { { 1 }, { 0.0000 }, { 0.0000 }, { 0.0000 } });

        // Quadratic
        tmpMatrices[2] = BigMatrix.FACTORY.rows(new double[][] {
                { 1.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000,
                        0.000000000000000000000000, 0.000000000000000000000000 },
                { 0.000000000000000000000000, 1.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000,
                        0.000000000000000000000000, 0.000000000000000000000000 },
                { 0.000000000000000000000000, 0.000000000000000000000000, 1.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000,
                        0.000000000000000000000000, 0.000000000000000000000000 },
                { 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 1.000000000000000000000000, 0.000000000000000000000000,
                        0.000000000000000000000000, 0.000000000000000000000000 },
                { 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 1.000000000000000000000000,
                        0.000000000000000000000000, 0.000000000000000000000000 },
                { 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000,
                        1.000000000000000000000000, 0.000000000000000000000000 },
                { 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000,
                        0.000000000000000000000000, 1.000000000000000000000000 } });

        // Linear
        tmpMatrices[3] = BigMatrix.FACTORY.rows(
                new double[][] { { -0.1100000000000000000000000000 }, { -0.1100000000000000000000000000 }, { -0.5000000000000000000000000000 },
                        { -0.2800000000000000000000000000 }, { 0.0000000000000000000000000000 }, { 0.0000000000000000000000000000 },
                        { 0.0000000000000000000000000000 } }).negate();

        // LagrangeSolver
        tmpMatrices[4] = tmpMatrices[3];

        this.doTest(tmpMatrices, false, new NumberContext(7, 8)); // A not full row rank
    }

    /**
     * Data taken from {@linkplain Qsd20030409P1Case}
     */
    public void testQsd20030409P1Case() {

        final BasicMatrix[] tmpMatrices = new BigMatrix[5];

        // Equations/Equalities
        tmpMatrices[0] = BigMatrix.FACTORY.rows(new double[][] { { 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 0, 0, 0, 0, 0 }, { 0, 0, 1, 0, 0, 0, 0 },
                { 0, 0, 0, 1, 0, 0, 0 }, { 0, 0, 0, 0, 1, 0, 0 }, { 0, 0, 0, 0, 0, 1, 0 }, { 0, 0, 0, 0, 0, 0, 1 } });

        // Levels/Values
        tmpMatrices[1] = BigMatrix.FACTORY.rows(new double[][] { { 1 }, { 0.0000 }, { 0.0000 }, { 0.0000 }, { 0.8709 }, { 0.0000 }, { 0.1291 } });

        // Quadratic
        tmpMatrices[2] = BigMatrix.FACTORY.rows(new double[][] {
                { 1.001190250000000000000000, 0.001421400000000000000000, 0.002546100000000000000000, 0.004443600000000000000000, 0.000000000000000000000000,
                        0.001380000000000000000000, 0.002400337500000000000000 },
                { 0.001421400000000000000000, 1.001697440000000000000000, 0.003040560000000000000000, 0.005306560000000000000000, 0.000000000000000000000000,
                        0.001648000000000000000000, 0.002866490000000000000000 },
                { 0.002546100000000000000000, 0.003040560000000000000000, 1.005446440000000000000000, 0.009505440000000000000000, 0.000000000000000000000000,
                        0.002952000000000000000000, 0.005134635000000000000000 },
                { 0.004443600000000000000000, 0.005306560000000000000000, 0.009505440000000000000000, 1.016589440000000000000000, 0.000000000000000000000000,
                        0.005152000000000000000000, 0.008961260000000000000000 },
                { 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 1.000000000000000000000000,
                        0.000000000000000000000000, 0.000000000000000000000000 },
                { 0.001380000000000000000000, 0.001648000000000000000000, 0.002952000000000000000000, 0.005152000000000000000000, 0.000000000000000000000000,
                        1.001600000000000000000000, 0.002783000000000000000000 },
                { 0.002400337500000000000000, 0.002866490000000000000000, 0.005134635000000000000000, 0.008961260000000000000000, 0.000000000000000000000000,
                        0.002783000000000000000000, 1.004840680625000000000000 } });

        // Linear
        tmpMatrices[3] = BigMatrix.FACTORY.rows(
                new double[][] { { -0.00059512500000000000000000000000000000000000 }, { -0.00071070000000000000000000000000000000000000 },
                        { -0.00127305000000000000000000000000000000000000 }, { -0.00222180000000000000000000000000000000000000 },
                        { -1.00000000000000000000000000000000000000000000 }, { -0.00069000000000000000000000000000000000000000 },
                        { -0.00120016875000000000000000000000000000000000 } }).negate();

        // LagrangeSolver
        tmpMatrices[4] = BigMatrix.FACTORY.rows(new double[][] { { 0.0000 }, { 0.0000 }, { 0.0000 }, { 0.0000 }, { 0.8709 }, { 0.0000 }, { 0.1291 } });

        this.doTest(tmpMatrices, true, new NumberContext(3, 3));
    }

    /**
     * Data taken from {@linkplain QsdOldFundOfFundsCase}
     */
    public void testQsdOldFundOfFundsCase() {

        final BasicMatrix[] tmpMatrices = new BigMatrix[5];

        // Equations/Equalities
        tmpMatrices[0] = BigMatrix.FACTORY
                .rows(new double[][] { { 1.0, 1.0, 1.0, 1.0, 1.0 }, { 0.0345, 0.0412, 0.069575, 0.0738, 0.1288 }, { 1.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 1.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 1.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 1.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 1.0 } });

        // Levels/Values
        tmpMatrices[1] = BigMatrix.FACTORY.rows(new double[][] { { 1.0 }, { 0.069575 }, { 0.0 }, { 0.0 }, { 1.0 }, { 0.0 }, { 0.0 } });

        // Quadratic
        tmpMatrices[2] = BigMatrix.FACTORY.rows(new double[][] { { 2.0, -0.0, 0.0, -0.0, -0.0 }, { -0.0, 2.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 2.0, 0.0, 0.0 },
                { -0.0, 0.0, 0.0, 2.0, 0.0 }, { -0.0, 0.0, 0.0, 0.0, 2.0 } });

        // Linear
        tmpMatrices[3] = BigMatrix.FACTORY.rows(new double[][] { { -0.5 }, { -0.5 }, { -0.0 }, { -0.5 }, { -0.5 } }).negate();

        // LagrangeSolver
        tmpMatrices[4] = BigMatrix.FACTORY.rows(new double[][] { { 0.0 }, { 0.0 }, { 1.0 }, { 0.0 }, { 0.0 } });

        final BasicMatrix tmpLHS = tmpMatrices[0].multiply(tmpMatrices[4]);
        final BasicMatrix tmpRHS = tmpMatrices[1];

        this.doTest(tmpMatrices, false, new NumberContext(7, 14)); // A not full row rank
    }

    /**
     * Data taken from {@linkplain QsdOldNormalCase}
     */
    public void testQsdOldNormalCase() {

        final BasicMatrix[] tmpMatrices = new BigMatrix[5];

        // Equations/Equalities
        tmpMatrices[0] = BigMatrix.FACTORY.rows(new double[][] { { 1.0, 1.0, 1.0, 1.0, 1.0 }, { 0.0345, 0.0412, 0.0738, 0.1288, 0.069575 },
                { 0.0, 0.0, 0.0, 0.0, 1.0 } });

        // Levels/Values
        tmpMatrices[1] = BigMatrix.FACTORY.rows(new double[][] { { 1.0 }, { 0.069575 }, { 0.0 } });

        // Quadratic
        tmpMatrices[2] = BigMatrix.FACTORY.rows(new double[][] { { 2.0, -0.0, -0.0, -0.0, 0.0 }, { -0.0, 2.0, 0.0, 0.0, 0.0 }, { -0.0, 0.0, 2.0, 0.0, 0.0 },
                { -0.0, 0.0, 0.0, 2.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 2.0 } });

        // Linear
        tmpMatrices[3] = BigMatrix.FACTORY.rows(new double[][] { { -0.5 }, { -0.5 }, { -0.5 }, { -0.5 }, { -0.0 } }).negate();

        // LagrangeSolver
        tmpMatrices[4] = BigMatrix.FACTORY.rows(new double[][] { { 0.25 }, { 0.25 }, { 0.25 }, { 0.25 }, { 0.0 } });

        this.doTest(tmpMatrices, false, new NumberContext(7, 7)); // A not full row rank
    }

    /**
     * Data taken from {@linkplain QsdOldOptimalCase}
     */
    public void testQsdOldOptimalCase() {

        final BasicMatrix[] tmpMatrices = new BigMatrix[5];

        // Equations/Equalities
        tmpMatrices[0] = BigMatrix.FACTORY.rows(new double[][] { { 1.0, 1.0, 1.0, 1.0, 1.0 }, { 0.0345, 0.0412, 0.0738, 0.1288, 0.069575 },
                { 0.0, 0.0, 0.0, 0.0, 1.0 } });

        // Levels/Values
        tmpMatrices[1] = BigMatrix.FACTORY.rows(new double[][] { { 1.0 }, { 0.043807039117990006 }, { 0.0 } });

        // Quadratic
        tmpMatrices[2] = BigMatrix.FACTORY.rows(new double[][] { { 2.005994, -0.077922, -0.041957999999999995, -0.17982, 0.0 },
                { -0.077922, 2.95904, 0.50949, 2.17782, 0.0 }, { -0.041957999999999995, 0.50949, 35.454511999999994, 29.804166, 0.0 },
                { -0.17982, 2.17782, 29.804166, 139.150712, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 16.747238000000003 } });

        // Linear
        tmpMatrices[3] = BigMatrix.FACTORY.rows(new double[][] { { -0.5 }, { -0.5 }, { -0.5 }, { -0.5 }, { -0.0 } }).negate();

        // LagrangeSolver
        tmpMatrices[4] = BigMatrix.FACTORY.rows(new double[][] { { 0.4506664080256741780 }, { 0.4388067927187100669 }, { 0.0737389738732712572 },
                { 0.0367878253823444979 }, { 0.0000000000000000000 } });

        this.doTest(tmpMatrices, false, new NumberContext(7, 4)); // A not full row rank
    }

    public void testTMA4180() {

        final Access2D<?> tmpQ = ArrayUtils.wrapAccess2D(new double[][] { { 4, 2 }, { 2, 2 } });
        final Access2D<?> tmpC = ArrayUtils.wrapAccess2D(new double[][] { { 10 }, { 10 } });

        Access2D<?> tmpA = null;
        Access2D<?> tmpB = null;

        Access2D<?> tmpX = ArrayUtils.wrapAccess2D(new double[][] { { 0 }, { 5 } });

        this.doTest(new Access2D<?>[] { tmpA, tmpB, tmpQ, tmpC, tmpX }, true, new NumberContext(7, 6));

        tmpA = ArrayUtils.wrapAccess2D(new double[][] { { 3, 1 } });
        tmpB = ArrayUtils.wrapAccess2D(new double[][] { { 6 } });

        tmpX = ArrayUtils.wrapAccess2D(new double[][] { { 2.0 / 5.0 }, { 24.0 / 5.0 } });

        this.doTest(new Access2D<?>[] { tmpA, tmpB, tmpQ, tmpC, tmpX }, true, new NumberContext(7, 6));
    }

    public void testStratMixCase() {

        final PrimitiveDenseStore tmpA = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0, 1.0, 1.0, 0.0, 0.0, 0.0 }, { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0, 0.0, -1.0, 0.0 }, { 1.0, 0.0, 0.0, -1.0, 0.0, 0.0 }, { 0.0, 0.0, 1.0, 0.0, 0.0, -1.0 },
                { 0.0, -1.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, -1.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, -1.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, -1.0 } });
        final PrimitiveDenseStore tmpB = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 },
                { 0.0 }, { 0.0 }, { 0.0 } });

        final PrimitiveDenseStore tmpQ = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0, 0.5, 0.5, 0.0, 0.0, 0.0 }, { 0.5, 1.0, 0.5, 0.0, 0.0, 0.0 },
                { 0.5, 0.5, 1.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 } });
        final PrimitiveDenseStore tmpC = PrimitiveDenseStore.FACTORY.rows(new double[][] { { -0.33330000000000004 }, { 0.16669999999999996 },
                { 0.16659999999999997 }, { 0.0 }, { 0.0 }, { 0.0 } });

        final PrimitiveDenseStore tmpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 } });

        final PrimitiveDenseStore tmpL = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 3.469446951953614E-17 }, { -0.2222000000000002 },
                { -0.11110000000000014 }, { 0.05556666666666671 }, { -0.11110000000000002 }, { 0.05553333333333336 }, { -0.11113333333333346 },
                { -0.11106666666666672 }, { -0.05556666666666672 }, { -0.05553333333333335 } });

        final PhysicalStore<Double> tmpActL = this.doTest(new Access2D<?>[] { tmpA, tmpB, tmpQ, tmpC, tmpX }, false, new NumberContext(12, 14)).copy();

        final QR<Double> tmpQR = QR.makePrimitive();
        tmpQR.compute(tmpA.transpose()); //TODO Shouldn't have to do this. Can solve directly with the already calculated  myQR.compute(tmpA).

        if (OptimisationSystemTests.DEBUG) {
            BasicLogger.debug("Q", tmpQR.getQ());
            BasicLogger.debug("R", tmpQR.getR());
        }

        TestUtils.assertEquals(tmpL, tmpActL);
    }

    /**
     * @param matrices A, B, Q, C, X
     * @param validate TODO
     * @param context TODO
     */
    private MatrixStore<Double> doTest(final Access2D<?>[] matrices, final boolean validate, final NumberContext context) {

        final Optimisation.Options tmpOptions = new Optimisation.Options();
        tmpOptions.validate = validate;

        final MatrixStore<Double> tmpA = matrices[0] != null ? PrimitiveDenseStore.FACTORY.copy(matrices[0]) : null;
        final MatrixStore<Double> tmpB = matrices[1] != null ? PrimitiveDenseStore.FACTORY.copy(matrices[1]) : null;
        final MatrixStore<Double> tmpQ = PrimitiveDenseStore.FACTORY.copy(matrices[2]);
        final MatrixStore<Double> tmpC = PrimitiveDenseStore.FACTORY.copy(matrices[3]);
        final MatrixStore<Double> tmpX = PrimitiveDenseStore.FACTORY.copy(matrices[4]);

        final KKTSolver.Input tmpFullInput = new KKTSolver.Input(tmpQ, tmpC, tmpA, tmpB);
        final KKTSolver tmpFullSolver = new KKTSolver(tmpFullInput);

        final KKTSolver.Output tmpFullOutput = tmpFullSolver.solve(tmpFullInput, tmpOptions);
        final MatrixStore<Double> tmpFullX = tmpFullOutput.getX();
        final MatrixStore<Double> tmpFullL = tmpFullOutput.getL();

        TestUtils.assertTrue(tmpFullOutput.isSolvable());

        TestUtils.assertEquals(tmpX, tmpFullX, context);

        final KKTSolver.Input tmpStepInput = new KKTSolver.Input(tmpQ, tmpC.add(tmpQ.multiply(tmpFullX).negate()), tmpA,
                tmpB != null ? ZeroStore.makePrimitive((int) tmpB.countRows(), (int) tmpB.countColumns()) : null);

        final KKTSolver tmpStepSolver = new KKTSolver(tmpFullInput);
        final KKTSolver.Output tmpStepOutput = tmpStepSolver.solve(tmpStepInput, tmpOptions);
        final MatrixStore<Double> tmpStepX = tmpStepOutput.getX();
        final MatrixStore<Double> tmpStepL = tmpStepOutput.getL();

        TestUtils.assertTrue(tmpStepOutput.isSolvable());

        TestUtils.assertEquals(tmpX, tmpFullX.add(tmpStepX), context);

        if ((tmpA != null) && (tmpA.countRows() <= tmpA.countColumns())) {
            TestUtils.assertEquals(tmpFullL, tmpStepL, context);
        }

        return tmpFullL;
    }

}
