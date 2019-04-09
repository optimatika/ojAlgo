/*
 * Copyright 1997-2019 Optimatika
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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.decomposition.MatrixDecomposition.EconomySize;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Normal;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class DesignCase {

    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testCholeskySolveInverse() {

        final PhysicalStore<ComplexNumber> tmpRandomComplexStore = TestUtils.makeRandomComplexStore(4, 9);
        final PhysicalStore<Double> tmpVctr = PrimitiveDenseStore.FACTORY.copy(tmpRandomComplexStore);
        final MatrixStore<Double> tmpMtrx = tmpVctr.multiply(tmpVctr.transpose());

        this.doTestSolveInverse(Cholesky.PRIMITIVE.make(), tmpMtrx);
    }

    @Test
    public void testFullSize() {

        final NumberContext precision = new NumberContext(12, 8);

        MatrixStore<Double> tall = PrimitiveDenseStore.FACTORY.makeFilled(7, 5, new Uniform());
        MatrixStore<Double> fat = PrimitiveDenseStore.FACTORY.makeFilled(5, 7, new Uniform());

        MatrixDecomposition.EconomySize<Double>[] all = MatrixDecompositionTests.getFullSizePrimitive();
        for (EconomySize<Double> decomp : all) {

            final String className = decomp.getClass().getName();

            if (decomp instanceof Bidiagonal<?>) {
                Bidiagonal<Double> bidiagonal = (Bidiagonal<Double>) decomp;

                bidiagonal.decompose(tall);

                TestUtils.assertEquals(className, 7, bidiagonal.getQ1().countRows());
                TestUtils.assertEquals(className, 7, bidiagonal.getQ1().countColumns());

                TestUtils.assertEquals(className, 5, bidiagonal.getQ2().countRows());
                TestUtils.assertEquals(className, 5, bidiagonal.getQ2().countColumns());

                TestUtils.assertEquals(tall, bidiagonal, precision);

                bidiagonal.decompose(fat);

                TestUtils.assertEquals(className, 5, bidiagonal.getQ1().countRows());
                TestUtils.assertEquals(className, 5, bidiagonal.getQ1().countColumns());

                TestUtils.assertEquals(className, 7, bidiagonal.getQ2().countRows());
                TestUtils.assertEquals(className, 7, bidiagonal.getQ2().countColumns());

                TestUtils.assertEquals(fat, bidiagonal, precision);

            } else if (decomp instanceof QR<?>) {
                QR<Double> qr = (QR<Double>) decomp;

                qr.decompose(tall);

                TestUtils.assertEquals(className, 7, qr.getQ().countRows());
                TestUtils.assertEquals(className, 7, qr.getQ().countColumns());

                TestUtils.assertEquals(className, 7, qr.getR().countRows());
                TestUtils.assertEquals(className, 5, qr.getR().countColumns());

                TestUtils.assertEquals(tall, qr, precision);

                qr.decompose(fat);

                TestUtils.assertEquals(className, 5, qr.getQ().countRows());
                TestUtils.assertEquals(className, 5, qr.getQ().countColumns());

                TestUtils.assertEquals(className, 5, qr.getR().countRows());
                TestUtils.assertEquals(className, 7, qr.getR().countColumns());

                TestUtils.assertEquals(fat, qr, precision);

            } else if (decomp instanceof SingularValue<?>) {
                SingularValue<Double> svd = (SingularValue<Double>) decomp;

                svd.decompose(tall);

                TestUtils.assertEquals(className, 7, svd.getQ1().countRows());
                TestUtils.assertEquals(className, 7, svd.getQ1().countColumns());

                TestUtils.assertEquals(className, 5, svd.getQ2().countRows());
                TestUtils.assertEquals(className, 5, svd.getQ2().countColumns());

                TestUtils.assertEquals(tall, svd, precision);

                svd.decompose(fat);

                TestUtils.assertEquals(className, 5, svd.getQ1().countRows());
                TestUtils.assertEquals(className, 5, svd.getQ1().countColumns());

                TestUtils.assertEquals(className, 7, svd.getQ2().countRows());
                TestUtils.assertEquals(className, 7, svd.getQ2().countColumns());

                TestUtils.assertEquals(fat, svd, precision);
            }

        }

    }

    @Test
    public void testLuSolveInverse() {

        final PhysicalStore<ComplexNumber> tmpRandomComplexStore = TestUtils.makeRandomComplexStore(4, 9);
        final PhysicalStore<Double> tmpVctr = PrimitiveDenseStore.FACTORY.copy(tmpRandomComplexStore);
        final MatrixStore<Double> tmpMtrx = tmpVctr.multiply(tmpVctr.transpose());

        this.doTestSolveInverse(LU.PRIMITIVE.make(), tmpMtrx);
    }

    @Test
    public void testRandomUnderdetermined() {

        final PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY.makeFilled(3, 9, new Normal());
        final PhysicalStore<Double> tmpB = PrimitiveDenseStore.FACTORY.makeFilled(3, 1, new Normal());

        final QR<Double> tmpQR = QR.PRIMITIVE.make(tmpA);
        tmpQR.decompose(tmpA);

        final PhysicalStore<Double> tmpX = tmpQR.getSolution(tmpB).copy();

        // BasicLogger.debug("Straigt X: " + tmpX.toString());
        tmpB.modifyMatching(PrimitiveMath.SUBTRACT, tmpA.multiply(tmpX));
        // BasicLogger.debug("Residual B: " + tmpB.toString());

    }

    @Test
    public void testSolveIdentity() {

        final Access2D<?> tmpIdentity = MatrixStore.PRIMITIVE.makeIdentity(9).get();
        final Access2D<?> tmpRandom = PrimitiveDenseStore.FACTORY.makeFilled(9, 1, new Uniform());

        final List<MatrixDecomposition<Double>> tmpAllDecomps = MatrixDecompositionTests.getAllPrimitive();
        for (final MatrixDecomposition<Double> tmpDecomp : tmpAllDecomps) {
            if (tmpDecomp instanceof SolverTask) {
                final SolverTask<Double> tmpSolverTask = (SolverTask<Double>) tmpDecomp;
                try {
                    TestUtils.assertEquals(tmpDecomp.getClass().toString(), tmpRandom, tmpSolverTask.solve(tmpIdentity, tmpRandom));
                } catch (final RecoverableCondition xcptn) {
                    TestUtils.fail(tmpDecomp.getClass().toString() + " " + xcptn.getMessage());
                }
            }
        }
    }

    @Test
    public void testTridiagonal() {

        final Tridiagonal<Double> tmpDecomposition = Tridiagonal.PRIMITIVE.make();
        //final Tridiagonal<Double> tmpDecomposition = new TridiagonalAltDecomp();

        final PhysicalStore<Double> tmpOriginalMatrix = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 4, 2, 2, 1 }, { 2, -3, 1, 1 }, { 2, 1, 3, 1 }, { 1, 1, 1, 2 } });

        tmpDecomposition.decompose(tmpOriginalMatrix);

        TestUtils.assertTrue(Tridiagonal.equals(tmpOriginalMatrix, tmpDecomposition, new NumberContext(7, 6)));
    }

    /**
     * http://en.wikipedia.org/wiki/Kernel_%28matrix%29
     */
    @Test
    public void testWikipediaNullspace() {

        final PhysicalStore<Double> mtrxA = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 2, 3, 5 }, { -4, 2, 3 } });
        final MatrixStore<Double> mtrxAt = mtrxA.transpose();

        final NumberContext precision = new NumberContext(14, 8);

        final QR<Double> decompPriQR = QR.PRIMITIVE.make(true);
        decompPriQR.decompose(mtrxAt);
        TestUtils.assertEquals(mtrxAt, decompPriQR, precision);
        TestUtils.assertEquals(3, decompPriQR.getQ().countRows());
        TestUtils.assertEquals(3, decompPriQR.getQ().countColumns());

        final SingularValue<Double> decompPriSVD = SingularValue.PRIMITIVE.make(true);
        decompPriSVD.decompose(mtrxA);
        TestUtils.assertEquals(mtrxA, decompPriSVD, precision);
        TestUtils.assertEquals(3, decompPriSVD.getQ2().countRows());
        TestUtils.assertEquals(3, decompPriSVD.getQ2().countColumns());

        final PhysicalStore<Double> nullspacePriQR = decompPriQR.getQ().logical().offsets(0, decompPriQR.getRank()).get().copy();
        final PhysicalStore<Double> nullspacePriSVD = decompPriSVD.getQ2().logical().offsets(0, decompPriSVD.getRank()).get().copy();

        final double scalePriQR = PrimitiveMath.ABS.invoke(nullspacePriQR.doubleValue(0));
        nullspacePriQR.modifyAll(PrimitiveMath.DIVIDE.second(scalePriQR));

        final double scalePriSVD = PrimitiveMath.ABS.invoke(nullspacePriSVD.doubleValue(0));
        nullspacePriSVD.modifyAll(PrimitiveMath.DIVIDE.second(scalePriSVD));

        final PrimitiveDenseStore nullspace = PrimitiveDenseStore.FACTORY.columns(new double[] { -1, -26, 16 });

        TestUtils.assertEquals(nullspace, nullspacePriQR, precision);
        TestUtils.assertEquals(nullspace, nullspacePriSVD, precision);
    }

    /**
     * http://en.wikipedia.org/wiki/Singular_value_decomposition
     */
    @Test
    public void testWikipediaSVD() {

        final PhysicalStore<Double> tmpOriginalMatrix = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 1.0, 0.0, 0.0, 0.0, 2.0 }, { 0.0, 0.0, 3.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 4.0, 0.0, 0.0, 0.0 } });
        Array1D.PRIMITIVE64.copy(new double[] { 4.0, 3.0, PrimitiveMath.SQRT.invoke(5.0), 0.0 });

        final SingularValue<Double> tmpOldDecomp = new SingularValueDecomposition.Primitive();
        tmpOldDecomp.decompose(tmpOriginalMatrix);
        tmpOldDecomp.getD();
        tmpOldDecomp.getQ1();
        tmpOldDecomp.getQ2();

        final SingularValue<Double> tmpNewDecomp = new RawSingularValue();
        tmpNewDecomp.decompose(tmpOriginalMatrix);
        tmpNewDecomp.getD();
        tmpNewDecomp.getQ1();
        tmpNewDecomp.getQ2();

        TestUtils.assertEquals(tmpOriginalMatrix, tmpNewDecomp, new NumberContext(7, 6));
    }

    private void doTestSolveInverse(final MatrixDecomposition.Solver<Double> aDecomp, final MatrixStore<Double> aMtrx) {

        TestUtils.assertEquals("Matrix not square!", aMtrx.countRows(), aMtrx.countColumns());

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Original", aMtrx);
        }

        aDecomp.decompose(aMtrx);

        TestUtils.assertTrue("Decomposition not solveable", aDecomp.isSolvable());

        final int tmpMinDim = (int) Math.min(aMtrx.countRows(), aMtrx.countColumns());
        final PhysicalStore<Double> tmpEye = PrimitiveDenseStore.FACTORY.makeEye(tmpMinDim, tmpMinDim);

        final MatrixStore<Double> tmpDirInv = aDecomp.getInverse();
        final MatrixStore<Double> tmpSolInv = aDecomp.getSolution(tmpEye);

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Direct Inverse", tmpDirInv);
            BasicLogger.debug("Solved Inverse", tmpSolInv);
        }

        TestUtils.assertEquals("Not inverted/solved correctly!", tmpDirInv, tmpSolInv);
        TestUtils.assertEquals("Not inverted correctly!", aMtrx, aMtrx.multiply(tmpDirInv).multiply(aMtrx));
        TestUtils.assertEquals("Not solved correctly!", aMtrx, aMtrx.multiply(tmpSolInv).multiply(aMtrx));

    }
}
