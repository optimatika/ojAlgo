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
import org.ojalgo.array.Array1D;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Normal;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class DesignCase extends MatrixDecompositionTests {

    public DesignCase() {
        super();
    }

    public DesignCase(final String arg0) {
        super(arg0);
    }

    public void testCholeskySolveInverse() {

        final PhysicalStore<ComplexNumber> tmpRandomComplexStore = MatrixUtils.makeRandomComplexStore(4, 9);
        final PhysicalStore<Double> tmpVctr = PrimitiveDenseStore.FACTORY.copy(tmpRandomComplexStore);
        final MatrixStore<Double> tmpMtrx = tmpVctr.multiplyRight(tmpVctr.transpose());

        this.doTestSolveInverse(CholeskyDecomposition.makePrimitive(), tmpMtrx);
    }

    public void testLuSolveInverse() {

        final PhysicalStore<ComplexNumber> tmpRandomComplexStore = MatrixUtils.makeRandomComplexStore(4, 9);
        final PhysicalStore<Double> tmpVctr = PrimitiveDenseStore.FACTORY.copy(tmpRandomComplexStore);
        final MatrixStore<Double> tmpMtrx = tmpVctr.multiplyRight(tmpVctr.transpose());

        this.doTestSolveInverse(LUDecomposition.makePrimitive(), tmpMtrx);
    }

    public void testRandomUnderdetermined() {

        final PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY.makeRandom(3, 9, new Normal());
        final PhysicalStore<Double> tmpB = PrimitiveDenseStore.FACTORY.makeRandom(3, 1, new Normal());

        final QR<Double> tmpQR = QRDecomposition.makePrimitive();
        tmpQR.compute(tmpA, false);

        final PhysicalStore<Double> tmpX = tmpQR.solve(tmpB).copy();

        BasicLogger.debug("Straigt X: " + tmpX.toString());
        tmpB.fillMatching(tmpB, PrimitiveFunction.SUBTRACT, tmpA.multiplyRight(tmpX));
        BasicLogger.debug("Residual B: " + tmpB.toString());

    }

    public void testTridiagonal() {

        final Tridiagonal<Double> tmpDecomposition = TridiagonalDecomposition.makePrimitive();
        //final Tridiagonal<Double> tmpDecomposition = new TridiagonalAltDecomp();

        final PhysicalStore<Double> tmpOriginalMatrix = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 4, 2, 2, 1 }, { 2, -3, 1, 1 }, { 2, 1, 3, 1 },
                { 1, 1, 1, 2 } });

        tmpDecomposition.compute(tmpOriginalMatrix);

        TestUtils.assertTrue(MatrixUtils.equals(tmpOriginalMatrix, tmpDecomposition, new NumberContext(7, 6)));
    }

    /**
     * http://en.wikipedia.org/wiki/Kernel_%28matrix%29
     */
    public void testWikipediaNullspace() {

        final PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 2, 3, 5 }, { -4, 2, 3 } });

        final QR<Double> tmpQR = QRDecomposition.makePrimitive();
        tmpQR.compute(tmpA.transpose(), true);

        final SingularValue<Double> tmpSVD = SingularValueDecomposition.makePrimitive();
        tmpSVD.compute(tmpA, false, true);

        final PhysicalStore<Double> tmpNullspaceQR = tmpQR.getQ().builder().columns(tmpQR.getRank(), (int) tmpA.countColumns()).build().copy();
        final PhysicalStore<Double> tmpNullspaceSVD = tmpSVD.getQ2().builder().columns(tmpSVD.getRank(), (int) tmpA.countColumns()).build().copy();

        final double tmpScaleQR = Math.abs(tmpNullspaceQR.doubleValue(0));
        tmpNullspaceQR.modifyAll(PrimitiveFunction.DIVIDE.second(tmpScaleQR));

        final double tmpScaleSVD = Math.abs(tmpNullspaceSVD.doubleValue(0));
        tmpNullspaceSVD.modifyAll(PrimitiveFunction.DIVIDE.second(tmpScaleSVD));

        final PrimitiveDenseStore tmpExpected = PrimitiveDenseStore.FACTORY.columns(new double[] { -1, -26, 16 });
        final NumberContext tmpPrecision = NumberContext.getGeneral(8);

        TestUtils.assertEquals(tmpExpected, tmpNullspaceQR, tmpPrecision);
        TestUtils.assertEquals(tmpExpected, tmpNullspaceSVD, tmpPrecision);
    }

    /**
     * http://en.wikipedia.org/wiki/Singular_value_decomposition
     */
    public void testWikipediaSVD() {

        final PhysicalStore<Double> tmpOriginalMatrix = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0, 0.0, 0.0, 0.0, 2.0 },
                { 0.0, 0.0, 3.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 4.0, 0.0, 0.0, 0.0 } });
        final Array1D<Double> tmpSingularValues = Array1D.PRIMITIVE.copy(new double[] { 4.0, 3.0, Math.sqrt(5.0), 0.0 });

        final SingularValue<Double> tmpOldDecomp = SingularValueDecomposition.makeAlternative();
        tmpOldDecomp.compute(tmpOriginalMatrix);
        tmpOldDecomp.getD();
        tmpOldDecomp.getQ1();
        tmpOldDecomp.getQ2();

        final SingularValue<Double> tmpNewDecomp = SingularValueDecomposition.makePrimitive();
        tmpNewDecomp.compute(tmpOriginalMatrix);
        tmpNewDecomp.getD();
        tmpNewDecomp.getQ1();
        tmpNewDecomp.getQ2();

        TestUtils.assertEquals(tmpOriginalMatrix, tmpNewDecomp, new NumberContext(7, 6));
    }

    private void doTestSolveInverse(final MatrixDecomposition<Double> aDecomp, final MatrixStore<Double> aMtrx) {

        TestUtils.assertEquals("Matrix not square!", aMtrx.countRows(), aMtrx.countColumns());

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Original", aMtrx);
        }

        aDecomp.compute(aMtrx);

        TestUtils.assertTrue("Decomposition not solveable", aDecomp.isSolvable());

        final int tmpMinDim = (int) Math.min(aMtrx.countRows(), aMtrx.countColumns());
        final PhysicalStore<Double> tmpEye = PrimitiveDenseStore.FACTORY.makeEye(tmpMinDim, tmpMinDim);

        final MatrixStore<Double> tmpDirInv = aDecomp.getInverse();
        final MatrixStore<Double> tmpSolInv = aDecomp.solve(tmpEye);

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Direct Inverse", tmpDirInv);
            BasicLogger.debug("Solved Inverse", tmpSolInv);
        }

        TestUtils.assertEquals("Not inverted/solved correctly!", tmpDirInv, tmpSolInv);
        TestUtils.assertEquals("Not inverted correctly!", aMtrx, tmpDirInv.multiplyLeft(aMtrx).multiplyRight(aMtrx));
        TestUtils.assertEquals("Not solved correctly!", aMtrx, tmpSolInv.multiplyLeft(aMtrx).multiplyRight(aMtrx));

    }
}
