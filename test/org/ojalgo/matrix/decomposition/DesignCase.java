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
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.matrix.decomposition.MatrixDecomposition.EconomySize;
import org.ojalgo.matrix.decomposition.MatrixDecomposition.Solver;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
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

        PhysicalStore<ComplexNumber> randomComplexStore = TestUtils.makeRandomComplexStore(4, 9);
        PhysicalStore<Double> vctrs = PrimitiveDenseStore.FACTORY.copy(randomComplexStore);
        MatrixStore<Double> mtrx = vctrs.multiply(vctrs.transpose());

        for (Cholesky<Double> decomp : MatrixDecompositionTests.getPrimitiveCholesky()) {
            this.doTestSolveInverse(decomp, mtrx);
        }
    }

    @Test
    public void testFullSize() {

        NumberContext precision = new NumberContext(12, 8);

        MatrixStore<Double> tall = PrimitiveDenseStore.FACTORY.makeFilled(7, 5, new Uniform());
        MatrixStore<Double> fat = PrimitiveDenseStore.FACTORY.makeFilled(5, 7, new Uniform());

        @SuppressWarnings("unchecked")
        EconomySize<Double>[] all = (EconomySize<Double>[]) new EconomySize<?>[] { new BidiagonalDecomposition.Primitive(true),
                new QRDecomposition.Primitive(true), new SingularValueDecomposition.Primitive(true) };
        for (EconomySize<Double> decomp : all) {

            String className = decomp.getClass().getName();

            if (decomp instanceof Bidiagonal<?>) {
                Bidiagonal<Double> bidiagonal = (Bidiagonal<Double>) decomp;

                bidiagonal.decompose(tall);

                TestUtils.assertEquals(className, 7, bidiagonal.getLQ().countRows());
                TestUtils.assertEquals(className, 7, bidiagonal.getLQ().countColumns());

                TestUtils.assertEquals(className, 5, bidiagonal.getRQ().countRows());
                TestUtils.assertEquals(className, 5, bidiagonal.getRQ().countColumns());

                TestUtils.assertEquals(tall, bidiagonal, precision);

                bidiagonal.decompose(fat);

                TestUtils.assertEquals(className, 5, bidiagonal.getLQ().countRows());
                TestUtils.assertEquals(className, 5, bidiagonal.getLQ().countColumns());

                TestUtils.assertEquals(className, 7, bidiagonal.getRQ().countRows());
                TestUtils.assertEquals(className, 7, bidiagonal.getRQ().countColumns());

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

                TestUtils.assertEquals(className, 7, svd.getU().countRows());
                TestUtils.assertEquals(className, 7, svd.getU().countColumns());

                TestUtils.assertEquals(className, 5, svd.getV().countRows());
                TestUtils.assertEquals(className, 5, svd.getV().countColumns());

                TestUtils.assertEquals(tall, svd, precision);

                svd.decompose(fat);

                TestUtils.assertEquals(className, 5, svd.getU().countRows());
                TestUtils.assertEquals(className, 5, svd.getU().countColumns());

                TestUtils.assertEquals(className, 7, svd.getV().countRows());
                TestUtils.assertEquals(className, 7, svd.getV().countColumns());

                TestUtils.assertEquals(fat, svd, precision);
            }

        }

    }

    @Test
    public void testRandomUnderdetermined() {

        PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY.makeFilled(3, 9, new Normal());
        PhysicalStore<Double> tmpB = PrimitiveDenseStore.FACTORY.makeFilled(3, 1, new Normal());

        QR<Double> tmpQR = QR.PRIMITIVE.make(tmpA);
        tmpQR.decompose(tmpA);

        PhysicalStore<Double> tmpX = tmpQR.getSolution(tmpB).copy();

        // BasicLogger.debug("Straigt X: " + tmpX.toString());
        tmpB.modifyMatching(PrimitiveMath.SUBTRACT, tmpA.multiply(tmpX));
        // BasicLogger.debug("Residual B: " + tmpB.toString());

    }

    @Test
    public void testSolvable() {

        PhysicalStore<Double> matrix = PrimitiveDenseStore.FACTORY.makeSPD(9);

        List<Solver<Double>> all = MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver();
        for (MatrixDecomposition.Solver<Double> decomposition : all) {
            decomposition.decompose(matrix);
            String message = decomposition.getClass().toString();
            TestUtils.assertTrue(message, decomposition.isComputed());
            TestUtils.assertTrue(message, decomposition.isSolvable());
        }
    }

    @Test
    public void testSolveIdentity() {

        Access2D<?> identity = MatrixStore.PRIMITIVE.makeIdentity(9).get();
        Access2D<?> random = PrimitiveDenseStore.FACTORY.makeFilled(9, 1, new Uniform());

        List<Solver<Double>> all = MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver();
        for (Solver<Double> solver : all) {
            try {
                TestUtils.assertEquals(solver.getClass().toString(), random, solver.solve(identity, random));
            } catch (RecoverableCondition xcptn) {
                TestUtils.fail(solver.getClass().toString() + " " + xcptn.getMessage());
            }
        }
    }

    @Test
    public void testSolveInverse() {

        PhysicalStore<ComplexNumber> randomComplexStore = TestUtils.makeRandomComplexStore(4, 9);
        PhysicalStore<Double> vectors = PrimitiveDenseStore.FACTORY.copy(randomComplexStore);
        MatrixStore<Double> matrix = vectors.multiply(vectors.transpose());

        List<Solver<Double>> all = MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver();
        for (Solver<Double> solver : all) {
            this.doTestSolveInverse(solver, matrix);
        }
    }

    @Test
    public void testTridiagonal() {

        Tridiagonal<Double> tmpDecomposition = Tridiagonal.PRIMITIVE.make();
        //  Tridiagonal<Double> tmpDecomposition = new TridiagonalAltDecomp();

        PhysicalStore<Double> tmpOriginalMatrix = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 4, 2, 2, 1 }, { 2, -3, 1, 1 }, { 2, 1, 3, 1 }, { 1, 1, 1, 2 } });

        tmpDecomposition.decompose(tmpOriginalMatrix);

        TestUtils.assertTrue(Tridiagonal.equals(tmpOriginalMatrix, tmpDecomposition, new NumberContext(7, 6)));
    }

    /**
     * http://en.wikipedia.org/wiki/Kernel_%28matrix%29
     */
    @Test
    public void testWikipediaNullspace() {

        PhysicalStore<Double> mtrxA = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 2, 3, 5 }, { -4, 2, 3 } });
        MatrixStore<Double> mtrxAt = mtrxA.transpose();

        NumberContext precision = new NumberContext(14, 8);

        QR<Double> decompPriQR = QR.PRIMITIVE.make(true);
        decompPriQR.decompose(mtrxAt);
        TestUtils.assertEquals(mtrxAt, decompPriQR, precision);
        TestUtils.assertEquals(3, decompPriQR.getQ().countRows());
        TestUtils.assertEquals(3, decompPriQR.getQ().countColumns());

        SingularValue<Double> decompPriSVD = SingularValue.PRIMITIVE.make(true);
        decompPriSVD.decompose(mtrxA);
        TestUtils.assertEquals(mtrxA, decompPriSVD, precision);
        TestUtils.assertEquals(3, decompPriSVD.getV().countRows());
        TestUtils.assertEquals(3, decompPriSVD.getV().countColumns());

        PhysicalStore<Double> nullspacePriQR = decompPriQR.getQ().logical().offsets(0, decompPriQR.getRank()).get().copy();
        PhysicalStore<Double> nullspacePriSVD = decompPriSVD.getV().logical().offsets(0, decompPriSVD.getRank()).get().copy();

        double scalePriQR = PrimitiveMath.ABS.invoke(nullspacePriQR.doubleValue(0));
        nullspacePriQR.modifyAll(PrimitiveMath.DIVIDE.second(scalePriQR));

        double scalePriSVD = PrimitiveMath.ABS.invoke(nullspacePriSVD.doubleValue(0));
        nullspacePriSVD.modifyAll(PrimitiveMath.DIVIDE.second(scalePriSVD));

        PrimitiveDenseStore nullspace = PrimitiveDenseStore.FACTORY.columns(new double[] { -1, -26, 16 });

        TestUtils.assertEquals(nullspace, nullspacePriQR, precision);
        TestUtils.assertEquals(nullspace, nullspacePriSVD, precision);
    }

    /**
     * http://en.wikipedia.org/wiki/Singular_value_decomposition
     */
    @Test
    public void testWikipediaSVD() {

        PhysicalStore<Double> tmpOriginalMatrix = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 1.0, 0.0, 0.0, 0.0, 2.0 }, { 0.0, 0.0, 3.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 4.0, 0.0, 0.0, 0.0 } });
        Array1D.PRIMITIVE64.copy(new double[] { 4.0, 3.0, PrimitiveMath.SQRT.invoke(5.0), 0.0 });

        SingularValue<Double> tmpOldDecomp = new SingularValueDecomposition.Primitive();
        tmpOldDecomp.decompose(tmpOriginalMatrix);
        tmpOldDecomp.getD();
        tmpOldDecomp.getU();
        tmpOldDecomp.getV();

        SingularValue<Double> tmpNewDecomp = new RawSingularValue();
        tmpNewDecomp.decompose(tmpOriginalMatrix);
        tmpNewDecomp.getD();
        tmpNewDecomp.getU();
        tmpNewDecomp.getV();

        TestUtils.assertEquals(tmpOriginalMatrix, tmpNewDecomp, new NumberContext(7, 6));
    }

    private void doTestSolveInverse(final MatrixDecomposition.Solver<Double> solver, final MatrixStore<Double> matrix) {

        TestUtils.assertEquals("Matrix not square!", matrix.countRows(), matrix.countColumns());

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Original", matrix);
            BasicLogger.debug("Solver: {}", solver.getClass());
        }

        solver.decompose(matrix);

        TestUtils.assertTrue("Decomposition not solvable", solver.isSolvable());

        int dim = MissingMath.toMinIntExact(matrix.countRows(), matrix.countColumns());
        PhysicalStore<Double> tmpEye = PrimitiveDenseStore.FACTORY.makeEye(dim, dim);

        MatrixStore<Double> tmpDirInv = solver.getInverse();
        MatrixStore<Double> tmpSolInv = solver.getSolution(tmpEye);

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Direct Inverse", tmpDirInv);
            BasicLogger.debug("Solved Inverse", tmpSolInv);
        }

        TestUtils.assertEquals("Not inverted/solved correctly!", tmpDirInv, tmpSolInv);
        TestUtils.assertEquals("Not inverted correctly!", matrix, matrix.multiply(tmpDirInv).multiply(matrix));
        TestUtils.assertEquals("Not solved correctly!", matrix, matrix.multiply(tmpSolInv).multiply(matrix));
    }
}
