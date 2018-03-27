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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Normal;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
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

        final PhysicalStore<ComplexNumber> tmpRandomComplexStore = MatrixUtils.makeRandomComplexStore(4, 9);
        final PhysicalStore<Double> tmpVctr = PrimitiveDenseStore.FACTORY.copy(tmpRandomComplexStore);
        final MatrixStore<Double> tmpMtrx = tmpVctr.multiply(tmpVctr.transpose());

        this.doTestSolveInverse(Cholesky.PRIMITIVE.make(), tmpMtrx);
    }

    @Test
    public void testLuSolveInverse() {

        final PhysicalStore<ComplexNumber> tmpRandomComplexStore = MatrixUtils.makeRandomComplexStore(4, 9);
        final PhysicalStore<Double> tmpVctr = PrimitiveDenseStore.FACTORY.copy(tmpRandomComplexStore);
        final MatrixStore<Double> tmpMtrx = tmpVctr.multiply(tmpVctr.transpose());

        this.doTestSolveInverse(LU.PRIMITIVE.make(), tmpMtrx);
    }

    @Test
    public void testRandomUnderdetermined() {

        final PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY.makeFilled(3, 9, new Normal());
        final PhysicalStore<Double> tmpB = PrimitiveDenseStore.FACTORY.makeFilled(3, 1, new Normal());

        final QR<Double> tmpQR = QR.PRIMITIVE.make(tmpA);
        tmpQR.setFullSize(false);
        tmpQR.decompose(tmpA);

        final PhysicalStore<Double> tmpX = tmpQR.getSolution(tmpB).copy();

        // BasicLogger.debug("Straigt X: " + tmpX.toString());
        tmpB.modifyMatching(PrimitiveFunction.SUBTRACT, tmpA.multiply(tmpX));
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

        final PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 2, 3, 5 }, { -4, 2, 3 } });

        final QR<Double> tmpQR = QR.PRIMITIVE.make(tmpA);
        tmpQR.setFullSize(true);
        tmpQR.decompose(tmpA.transpose());

        final SingularValue<Double> tmpSVD = new SingularValueDecomposition.Primitive();
        tmpSVD.setFullSize(true); // Supports full size
        tmpSVD.decompose(tmpA);

        final PhysicalStore<Double> tmpNullspaceQR = tmpQR.getQ().logical().offsets(0, tmpQR.getRank()).get().copy();
        final PhysicalStore<Double> tmpNullspaceSVD = tmpSVD.getQ2().logical().offsets(0, tmpSVD.getRank()).get().copy();

        final double tmpScaleQR = PrimitiveFunction.ABS.invoke(tmpNullspaceQR.doubleValue(0));
        tmpNullspaceQR.modifyAll(PrimitiveFunction.DIVIDE.second(tmpScaleQR));

        final double tmpScaleSVD = PrimitiveFunction.ABS.invoke(tmpNullspaceSVD.doubleValue(0));
        tmpNullspaceSVD.modifyAll(PrimitiveFunction.DIVIDE.second(tmpScaleSVD));

        final PrimitiveDenseStore tmpExpected = PrimitiveDenseStore.FACTORY.columns(new double[] { -1, -26, 16 });
        final NumberContext tmpPrecision = new NumberContext(14, 8);

        TestUtils.assertEquals(tmpExpected, tmpNullspaceQR, tmpPrecision);
        TestUtils.assertEquals(tmpExpected, tmpNullspaceSVD, tmpPrecision);
    }

    /**
     * http://en.wikipedia.org/wiki/Singular_value_decomposition
     */
    @Test
    public void testWikipediaSVD() {

        final PhysicalStore<Double> tmpOriginalMatrix = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 1.0, 0.0, 0.0, 0.0, 2.0 }, { 0.0, 0.0, 3.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 4.0, 0.0, 0.0, 0.0 } });
        Array1D.PRIMITIVE64.copy(new double[] { 4.0, 3.0, PrimitiveFunction.SQRT.invoke(5.0), 0.0 });

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
