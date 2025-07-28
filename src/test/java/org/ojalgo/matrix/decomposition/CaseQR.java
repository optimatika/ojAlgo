/*
 * Copyright 1997-2025 Optimatika
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
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.array.Array2D;
import org.ojalgo.matrix.MatrixQ128;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.matrix.MatrixR064.DenseReceiver;
import org.ojalgo.matrix.P20030422Case;
import org.ojalgo.matrix.SimpleCholeskyCase;
import org.ojalgo.matrix.SimpleEquationCase;
import org.ojalgo.matrix.operation.MatrixOperation;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Normal;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
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

        final DecompositionStore<ComplexNumber> tmpInPlace = GenericStore.C128.copy(tmpOriginal);

        final DecompositionStore<ComplexNumber> tmpNowQ = GenericStore.C128.makeEye(DIMENSION, DIMENSION);
        final DecompositionStore<ComplexNumber> tmpNowR = GenericStore.C128.copy(tmpOriginal);

        final DecompositionStore<ComplexNumber> tmpForwardQ = GenericStore.C128.makeEye(DIMENSION, DIMENSION);
        final DecompositionStore<ComplexNumber> tmpForwardR = GenericStore.C128.copy(tmpOriginal);

        final DecompositionStore<ComplexNumber> tmpReverseQ = GenericStore.C128.makeEye(DIMENSION, DIMENSION);

        final Householder.Generic<ComplexNumber>[] tmpHouseholders = new Householder.Generic[tmpLim];

        for (int ij = 0; ij < tmpLim; ij++) {

            final Householder.Generic<ComplexNumber> tmpVector = new Householder.Generic<>(ComplexNumber.FACTORY, DIMENSION);

            if (tmpInPlace.generateApplyAndCopyHouseholderColumn(ij, ij, tmpVector)) {
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

}
