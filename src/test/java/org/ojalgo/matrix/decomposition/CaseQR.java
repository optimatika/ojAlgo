/*
 * Copyright 1997-2026 Optimatika
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
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.array.Array2D;
import org.ojalgo.matrix.MatrixQ128;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.matrix.MatrixR064.DenseReceiver;
import org.ojalgo.matrix.P20030422Case;
import org.ojalgo.matrix.SimpleCholeskyCase;
import org.ojalgo.matrix.SimpleEquationCase;
import org.ojalgo.matrix.operation.GenerateApplyAndCopyHouseholderColumn;
import org.ojalgo.matrix.operation.MatrixOperation;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.InvertibleFactor;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Normal;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class CaseQR extends MatrixDecompositionTests {

    private static final NumberContext ACCURACY = NumberContext.of(7, 6);
    private static final int DIMENSION = 4;

    /**
     * An Hermitian matrix (or self-adjoint matrix) is a square matrix with complex entries that is equal to
     * its own conjugate transpose.
     */
    private static MatrixStore<ComplexNumber> makeHermitianMatrix() {
        final PhysicalStore<ComplexNumber> tmpBase = TestUtils.makeRandomComplexStore(DIMENSION, DIMENSION);
        return tmpBase.multiply(tmpBase.conjugate());
    }

    @Override
    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testCompilation() {

        // Just want to verify that this compiles and runs without throwing exceptions

        QR<Double> tmpDecomp = QR.R064.make();

        Array2D<Double> a2d = Array2D.R064.makeFilled(3, 3, Normal.standard());

        tmpDecomp.decompose(a2d);

        DenseReceiver dr = MatrixR064.FACTORY.newDenseBuilder(3, 3);
        dr.fillAll(Normal.standard());
        tmpDecomp.decompose(dr);

        MatrixR064 ps = dr.get();
        tmpDecomp.decompose(ps);

        MatrixR064 lb = dr.get().below(2);
        tmpDecomp.decompose(lb);

        MatrixR064 bm = lb;
        tmpDecomp.decompose(bm);
    }

    @Test
    public void testDiagonalCase() {

        PhysicalStore<Double> tmpOriginalMatrix = RawStore
                .wrap(new double[][] { { 4.0, 3.0, 2.0, 1.0 }, { 0.0, 3.0, 2.0, 1.0 }, { 0.0, 0.0, 2.0, 1.0 }, { 0.0, 0.0, 0.0, 1.0 } });

        final QR<Double> tmpDecomp = QR.R064.make();
        tmpDecomp.decompose(tmpOriginalMatrix);

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debugMatrix("This is A", tmpOriginalMatrix);
            BasicLogger.debugMatrix("Should be I", tmpDecomp.getQ());
            BasicLogger.debugMatrix("Should be A", tmpDecomp.getR());
        }

        TestUtils.assertEquals(tmpOriginalMatrix, tmpDecomp, ACCURACY);
        // TODO Fix so that Q == I when the original A is already triangular, even for RawQR
        TestUtils.assertEquals(R064Store.FACTORY.makeEye(4, 4), tmpDecomp.getQ(), ACCURACY);
        TestUtils.assertEquals(tmpOriginalMatrix, tmpDecomp.getR(), ACCURACY);
    }

    @Test
    public void testHermitian() {

        final int tmpLim = DIMENSION - 1;
        final MatrixStore<ComplexNumber> tmpOriginal = CaseQR.makeHermitianMatrix();

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debugMatrix("Original", tmpOriginal);
        }

        final QR<ComplexNumber> tmpDecomposition = QR.C128.make();
        tmpDecomposition.decompose(tmpOriginal);
        final MatrixStore<ComplexNumber> tmpDecompQ = tmpDecomposition.getQ();
        final MatrixStore<ComplexNumber> tmpDecompR = tmpDecomposition.getR();

        final GenericStore<ComplexNumber> tmpInPlace = GenericStore.C128.copy(tmpOriginal);

        final PhysicalStore<ComplexNumber> tmpNowQ = GenericStore.C128.makeEye(DIMENSION, DIMENSION);
        final PhysicalStore<ComplexNumber> tmpNowR = GenericStore.C128.copy(tmpOriginal);

        final PhysicalStore<ComplexNumber> tmpForwardQ = GenericStore.C128.makeEye(DIMENSION, DIMENSION);
        final PhysicalStore<ComplexNumber> tmpForwardR = GenericStore.C128.copy(tmpOriginal);

        final PhysicalStore<ComplexNumber> tmpReverseQ = GenericStore.C128.makeEye(DIMENSION, DIMENSION);

        final Householder.Generic<ComplexNumber>[] tmpHouseholders = new Householder.Generic[tmpLim];

        for (int ij = 0; ij < tmpLim; ij++) {

            final Householder.Generic<ComplexNumber> tmpVector = new Householder.Generic<>(ComplexNumber.FACTORY, DIMENSION);

            if (GenerateApplyAndCopyHouseholderColumn.invokeGeneric(tmpInPlace, ij, ij, tmpVector)) {
                tmpInPlace.transformLeft(tmpVector, ij + 1);
                tmpNowQ.transformRight(tmpVector, 0);
                tmpNowR.transformLeft(tmpVector, ij);
            }

            tmpHouseholders[ij] = tmpVector;
        }

        for (int h = 0; h < tmpHouseholders.length; h++) {

            final Householder.Generic<ComplexNumber> tmpVector = tmpHouseholders[h];

            tmpForwardQ.transformRight(tmpVector, 0);
            tmpForwardR.transformLeft(tmpVector, h);
        }

        for (int h = tmpHouseholders.length - 1; h >= 0; h--) {

            final Householder.Generic<ComplexNumber> tmpVector = tmpHouseholders[h];

            tmpReverseQ.transformLeft(tmpVector, 0);
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debugMatrix("Decomp Q", tmpDecompQ);
            BasicLogger.debugMatrix("Now Q", tmpNowQ);
            BasicLogger.debugMatrix("Forward Q", tmpForwardQ);
            BasicLogger.debugMatrix("Reverse Q", tmpReverseQ);
            BasicLogger.debug();
            BasicLogger.debugMatrix("Decomp R", tmpDecompR);
            BasicLogger.debugMatrix("Now R", tmpNowR);
            BasicLogger.debugMatrix("Forward R", tmpForwardR);
        }

        TestUtils.assertEquals(tmpOriginal, tmpDecomposition, ACCURACY);

        TestUtils.assertEquals(tmpDecompQ, tmpNowQ, ACCURACY);
        TestUtils.assertEquals(tmpDecompQ, tmpForwardQ, ACCURACY);
        TestUtils.assertEquals(tmpDecompQ, tmpReverseQ, ACCURACY);

        TestUtils.assertEquals(tmpDecompR, tmpNowR, ACCURACY);
        TestUtils.assertEquals(tmpDecompR, tmpForwardR, ACCURACY);
    }

    /**
     * i*[[1,2],[3,4]] is non-singular, and the diagonal of R need not be real.
     */
    @Test
    public void testImaginaryMatrix() {

        GenericStore<ComplexNumber> matrix = GenericStore.C128.make(2, 2);
        matrix.set(0, 0, ComplexNumber.of(0.0, 1.0));
        matrix.set(0, 1, ComplexNumber.of(0.0, 2.0));
        matrix.set(1, 0, ComplexNumber.of(0.0, 3.0));
        matrix.set(1, 1, ComplexNumber.of(0.0, 4.0));

        QR<ComplexNumber> decomposition = QR.C128.make(matrix);
        decomposition.decompose(matrix);

        TestUtils.assertEquals(2, decomposition.getRank());
        TestUtils.assertTrue(decomposition.isFullRank());
        TestUtils.assertTrue(decomposition.isSolvable());
    }

    @Test
    public void testLeastSquaresInvert() {

        MatrixOperation.setThresholdsMinValue(100000);

        final int tmpDim = 3;
        final MatrixStore<Double> tmpA = R064Store.FACTORY.makeSPD(tmpDim).below(R064Store.FACTORY.makeIdentity(tmpDim));

        final QR<Double> tmpDenseQR = new DenseQR.R064();
        final QR<Double> tmpRawQR = new RawQR();

        final PhysicalStore<Double> tmpDenseAlloc = tmpDenseQR.preallocate(tmpA);
        final PhysicalStore<Double> tmpRawAlloc = tmpRawQR.preallocate(tmpA);

        MatrixStore<Double> tmpDenseInv;
        try {
            tmpDenseInv = tmpDenseQR.invert(tmpA, tmpDenseAlloc);
            final MatrixStore<Double> tmpRawInv = tmpRawQR.invert(tmpA, tmpRawAlloc);

            TestUtils.assertEquals(tmpDenseInv, tmpRawInv);

            final MatrixStore<Double> tmpIdentity = R064Store.FACTORY.makeIdentity(tmpDim);
            TestUtils.assertEquals(tmpIdentity, tmpDenseInv.multiply(tmpA));
            TestUtils.assertEquals(tmpIdentity, tmpRawInv.multiply(tmpA));

        } catch (final RecoverableCondition anException) {
            anException.printStackTrace();
            TestUtils.fail(anException.toString());
        }

    }

    @Test
    public void testP20030422Case() {

        final MatrixQ128 tmpOriginal = P20030422Case.getProblematic();

        final QR<RationalNumber> tmpBigDecomp = QR.Q128.make();
        final QR<ComplexNumber> tmpComplexDecomp = QR.C128.make();
        final QR<Double> tmpPrimitiveDecomp = QR.R064.make();

        tmpBigDecomp.decompose(GenericStore.Q128.copy(tmpOriginal));
        tmpComplexDecomp.decompose(GenericStore.C128.copy(tmpOriginal));
        tmpPrimitiveDecomp.decompose(R064Store.FACTORY.copy(tmpOriginal));

        final MatrixStore<RationalNumber> tmpBigQ = tmpBigDecomp.getQ();
        final MatrixStore<ComplexNumber> tmpComplexQ = tmpComplexDecomp.getQ();
        final MatrixStore<Double> tmpPrimitiveQ = tmpPrimitiveDecomp.getQ();

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debugMatrix("Big Q", tmpBigQ);
            BasicLogger.debugMatrix("Complex Q", tmpComplexQ);
            BasicLogger.debugMatrix("Primitive Q", tmpPrimitiveQ);
        }

        final MatrixStore<RationalNumber> tmpBigR = tmpBigDecomp.getR();
        final MatrixStore<ComplexNumber> tmpComplexR = tmpComplexDecomp.getR();
        final MatrixStore<Double> tmpPrimitiveR = tmpPrimitiveDecomp.getR();

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debugMatrix("Big R", tmpBigR);
            BasicLogger.debugMatrix("Complex R", tmpComplexR);
            BasicLogger.debugMatrix("Primitive R", tmpPrimitiveR);
        }

        TestUtils.assertEquals(GenericStore.Q128.copy(tmpOriginal), tmpBigDecomp, NumberContext.of(7, 14));
        TestUtils.assertEquals(GenericStore.C128.copy(tmpOriginal), tmpComplexDecomp, NumberContext.of(7, 14));
        TestUtils.assertEquals(R064Store.FACTORY.copy(tmpOriginal), tmpPrimitiveDecomp, NumberContext.of(7, 14));
    }

    /**
     * R(k,k) is -norm when the column's k:th element is 0, as when it is positive.
     */
    @Test
    public void testSignOfZeroDiagonal() {

        double[][] rows = { { 0.0, 1.0 }, { 1.0, 0.0 } };

        for (QR<Double> decomp : MatrixDecompositionTests.getPrimitiveQR()) {
            decomp.decompose(R064Store.FACTORY.rows(rows));
            TestUtils.assertEquals(decomp.getClass().getSimpleName(), -1.0, decomp.getR().doubleValue(0, 0), 1E-15);
        }

        QR<ComplexNumber> complex = QR.C128.make();
        complex.decompose(GenericStore.C128.rows(rows));
        TestUtils.assertEquals("C128", -1.0, complex.getR().doubleValue(0, 0), 1E-15);

        QR<Quadruple> quadruple = QR.R128.make();
        quadruple.decompose(GenericStore.R128.rows(rows));
        TestUtils.assertEquals("R128", -1.0, quadruple.getR().doubleValue(0, 0), 1E-15);
    }

    @Test
    public void testSolveBothWays() {

        MatrixR064 body = SimpleEquationCase.getBody();
        MatrixR064 rhs = SimpleEquationCase.getRHS();
        MatrixR064 solution = SimpleEquationCase.getSolution();

        R064Store expected = R064Store.FACTORY.make(solution.getRowDim(), solution.getColDim());
        R064Store actual = R064Store.FACTORY.make(solution.getRowDim(), solution.getColDim());

        for (QR<Double> decomp : MatrixDecompositionTests.getPrimitiveQR()) {

            if (decomp instanceof RawQR) {
                continue;
            }

            decomp.decompose(body);

            if (DEBUG) {
                BasicLogger.debugMatrix("Q", decomp.getQ());
                BasicLogger.debugMatrix("R", decomp.getR());
            }
            rhs.supplyTo(actual);
            decomp.ftran(actual);

            TestUtils.assertEquals(solution, actual);

            decomp.decompose(body.transpose());

            if (DEBUG) {
                BasicLogger.debugMatrix("Q", decomp.getQ());
                BasicLogger.debugMatrix("R", decomp.getR());
            }
            rhs.supplyTo(expected);
            decomp.ftran(expected);

            decomp.decompose(body);

            if (DEBUG) {
                BasicLogger.debugMatrix("Q", decomp.getQ());
                BasicLogger.debugMatrix("R", decomp.getR());
            }
            rhs.supplyTo(actual);
            decomp.btran(actual);

            TestUtils.assertEquals(expected, actual);
        }

    }

    @Test
    public void testSolveBothWaysSymmetric() {

        MatrixR064 body = SimpleCholeskyCase.getOriginal();
        MatrixR064 rhs = SimpleEquationCase.getRHS();

        R064Store expected = R064Store.FACTORY.make(rhs.getRowDim(), 1);
        R064Store actual = R064Store.FACTORY.make(rhs.getRowDim(), 1);

        for (QR<Double> decomp : MatrixDecompositionTests.getPrimitiveQR()) {

            if (decomp instanceof RawQR) {
                continue;
            }

            decomp.decompose(body);

            MatrixStore<Double> mtrxQ = decomp.getQ();
            MatrixStore<Double> mtrxR = decomp.getR();

            if (DEBUG) {
                BasicLogger.debugMatrix("Q", mtrxQ);
                BasicLogger.debugMatrix("R", mtrxR);
            }
            rhs.supplyTo(expected);
            decomp.ftran(expected);
            rhs.supplyTo(actual);
            decomp.btran(actual);

            TestUtils.assertEquals(expected, actual);
        }
    }

    /**
     * A column with a very small part below the diagonal, compared to the rest of the column. Solving with
     * the decomposition, and with its factors, must give the solution of A*x = (1, 1), x = (1, 1 - t), to
     * full precision: there is a reflector unless that part is exactly 0. Applying a reflector that the
     * factorisation did not apply (or not applying one that it did) flips the sign of the first element.
     */
    @Test
    public void testTinySubdiagonal() {

        NumberContext accuracy = NumberContext.of(14);

        for (double tail : new double[] { 1E-6, 1E-9, 1E-10, 1E-12, 1E-17 }) {

            double[][] rows = { { 1.0, 0.0 }, { tail, 1.0 } };
            R064Store expected = R064Store.FACTORY.column(1.0, 1.0 - tail);

            for (QR<Double> decomp : MatrixDecompositionTests.getPrimitiveQR()) {

                String message = decomp.getClass().getSimpleName() + " tail=" + tail;

                decomp.decompose(R064Store.FACTORY.rows(rows));
                TestUtils.assertEquals(message, expected, decomp.getSolution(R064Store.FACTORY.column(1.0, 1.0)), accuracy);

                double[] x = { 1.0, 1.0 };
                for (InvertibleFactor<Double> factor : decomp.getFactors()) {
                    factor.ftran(x);
                }
                TestUtils.assertEquals(message + " factors", expected, Access1D.wrap(x), accuracy);
            }

            QR<ComplexNumber> complex = QR.C128.make();
            complex.decompose(GenericStore.C128.rows(rows));
            TestUtils.assertEquals("C128 tail=" + tail, expected, complex.getSolution(GenericStore.C128.column(1.0, 1.0)), accuracy);

            QR<Quadruple> quadruple = QR.R128.make();
            quadruple.decompose(GenericStore.R128.rows(rows));
            TestUtils.assertEquals("R128 tail=" + tail, expected, quadruple.getSolution(GenericStore.R128.column(1.0, 1.0)), accuracy);
        }
    }

    /**
     * As {@link #testTinySubdiagonal()}, but large enough for the native implementation, when there is one.
     * LAPACK generates a reflector for any part below the diagonal that is not exactly 0, also a tiny one.
     * Solving with the factors must use the reflectors the factorisation generated.
     */
    @Test
    public void testTinySubdiagonalLarge() {

        int n = 100;
        NumberContext accuracy = NumberContext.of(12);

        for (double tail : new double[] { 1E-9, 1E-17, 1E-20 }) {

            Random random = new Random(1);
            R064Store matrix = R064Store.FACTORY.make(n, n);
            for (int j = 0; j < n; j++) {
                for (int i = 0; i <= j; i++) {
                    matrix.set(i, j, random.nextDouble() + (i == j ? n : 0));
                }
            }
            matrix.set(1, 0, tail);

            R064Store rhs = R064Store.FACTORY.make(n, 1);
            for (int i = 0; i < n; i++) {
                rhs.set(i, 0, 1.0 + i);
            }

            for (QR<Double> decomp : MatrixDecompositionTests.getPrimitiveQR()) {

                String message = decomp.getClass().getSimpleName() + " tail=" + tail;

                decomp.decompose(matrix);
                TestUtils.assertEquals(message, rhs, matrix.multiply(decomp.getSolution(rhs)), accuracy);

                double[] x = rhs.toRawCopy1D();
                for (InvertibleFactor<Double> factor : decomp.getFactors()) {
                    factor.ftran(x);
                }
                TestUtils.assertEquals(message + " factors", rhs, matrix.multiply(R064Store.FACTORY.column(x)), accuracy);
            }
        }
    }

    /**
     * A column that is 0 on and below the diagonal, from the start or after the previous reflections, gets no
     * reflector, and the columns after it are still factorised. RawQR used to stop at such a column.
     */
    @Test
    public void testZeroColumn() {

        double[][][] matrices = { { { 0.0, 1.0 }, { 0.0, 2.0 }, { 0.0, 3.0 } }, { { 1.0, 5.0, 1.0 }, { 0.0, 0.0, 2.0 }, { 0.0, 0.0, 3.0 }, { 0.0, 0.0, 4.0 } },
                { { 2.0, 0.0, 1.0, 3.0 }, { 1.0, 0.0, 4.0, 1.0 }, { 3.0, 0.0, 1.0, 5.0 }, { 1.0, 0.0, 2.0, 2.0 }, { 2.0, 0.0, 1.0, 1.0 } } };

        for (double[][] rows : matrices) {

            R064Store matrix = R064Store.FACTORY.rows(rows);
            int m = matrix.getRowDim();
            int minDim = Math.min(m, matrix.getColDim());

            for (QR<Double> decomp : MatrixDecompositionTests.getPrimitiveQR()) {

                String message = decomp.getClass().getSimpleName() + " " + m + "x" + matrix.getColDim();

                decomp.decompose(matrix);

                TestUtils.assertEquals(matrix, decomp, ACCURACY);

                MatrixStore<Double> q = decomp.getQ();
                TestUtils.assertEquals(message + " Q^T*Q", R064Store.FACTORY.makeIdentity(minDim), q.transpose().multiply(q), ACCURACY);

                double[] x = { 1.0, -2.0, 3.0, -4.0, 5.0 };
                double[] y = Arrays.copyOf(x, m);
                InvertibleFactor<Double> factorQ = decomp.getFactors().get(0);
                factorQ.ftran(y);
                factorQ.btran(y);
                TestUtils.assertEquals(message + " Q*Q^T*x", Access1D.wrap(Arrays.copyOf(x, m)), Access1D.wrap(y), ACCURACY);
            }
        }
    }

}
