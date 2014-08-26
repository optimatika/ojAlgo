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

import java.math.BigDecimal;

import org.ojalgo.TestUtils;
import org.ojalgo.matrix.BigMatrix;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.P20030422Case;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;

/**
 * @author apete
 */
public class QRTest extends AbstractMatrixDecompositionTest {

    private static final int DIMENSION = 4;

    /**
     * An Hermitian matrix (or self-adjoint matrix) is a square matrix with complex entries that is equal to its own
     * conjugate transpose.
     */
    private static MatrixStore<ComplexNumber> makeHermitianMatrix() {
        final PhysicalStore<ComplexNumber> tmpBase = MatrixUtils.makeRandomComplexStore(DIMENSION, DIMENSION);
        return tmpBase.multiplyRight(tmpBase.conjugate());
    }

    public QRTest() {
        super();
    }

    public QRTest(final String arg0) {
        super(arg0);
    }

    public void testDiagonalCase() {

        final PhysicalStore<Double> tmpOriginalMatrix = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 4.0, 3.0, 2.0, 1.0 }, { 0.0, 3.0, 2.0, 1.0 },
                { 0.0, 0.0, 2.0, 1.0 }, { 0.0, 0.0, 0.0, 1.0 } });

        final QR<Double> tmpDecomp = QRDecomposition.makePrimitive();
        tmpDecomp.compute(tmpOriginalMatrix);

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Should be I", tmpDecomp.getQ());
            BasicLogger.debug("Should be A", tmpDecomp.getR());
        }

        TestUtils.assertEquals(tmpOriginalMatrix, tmpDecomp, TestUtils.EQUALS.newScale(6));
        TestUtils.assertEquals(PrimitiveDenseStore.FACTORY.makeEye(4, 4), tmpDecomp.getQ(), TestUtils.EQUALS.newScale(6));
        TestUtils.assertEquals(tmpOriginalMatrix, tmpDecomp.getR(), TestUtils.EQUALS.newScale(6));
    }

    public void testHermitian() {

        final int tmpLim = DIMENSION - 1;
        final MatrixStore<ComplexNumber> tmpOriginal = QRTest.makeHermitianMatrix();

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Original", tmpOriginal);
        }

        final QR<ComplexNumber> tmpDecomposition = QRDecomposition.makeComplex();
        tmpDecomposition.compute(tmpOriginal);
        final MatrixStore<ComplexNumber> tmpDecompQ = tmpDecomposition.getQ();
        final MatrixStore<ComplexNumber> tmpDecompR = tmpDecomposition.getR();

        final DecompositionStore<ComplexNumber> tmpInPlace = ComplexDenseStore.FACTORY.copy(tmpOriginal);

        final DecompositionStore<ComplexNumber> tmpNowQ = ComplexDenseStore.FACTORY.makeEye(DIMENSION, DIMENSION);
        final DecompositionStore<ComplexNumber> tmpNowR = ComplexDenseStore.FACTORY.copy(tmpOriginal);

        final DecompositionStore<ComplexNumber> tmpForwardQ = ComplexDenseStore.FACTORY.makeEye(DIMENSION, DIMENSION);
        final DecompositionStore<ComplexNumber> tmpForwardR = ComplexDenseStore.FACTORY.copy(tmpOriginal);

        final DecompositionStore<ComplexNumber> tmpReverseQ = ComplexDenseStore.FACTORY.makeEye(DIMENSION, DIMENSION);

        final Householder.Complex[] tmpHouseholders = new Householder.Complex[tmpLim];

        for (int ij = 0; ij < tmpLim; ij++) {

            final Householder.Complex tmpVector = new Householder.Complex(DIMENSION);

            if (tmpInPlace.generateApplyAndCopyHouseholderColumn(ij, ij, tmpVector)) {
                tmpInPlace.transformLeft(tmpVector, ij + 1);
                tmpNowQ.transformRight(tmpVector, 0);
                tmpNowR.transformLeft(tmpVector, ij);
            }

            tmpHouseholders[ij] = tmpVector;
        }

        for (int h = 0; h < tmpHouseholders.length; h++) {

            final Householder.Complex tmpVector = tmpHouseholders[h];

            tmpForwardQ.transformRight(tmpVector, 0);
            tmpForwardR.transformLeft(tmpVector, h);
        }

        for (int h = tmpHouseholders.length - 1; h >= 0; h--) {

            final Householder.Complex tmpVector = tmpHouseholders[h];

            tmpReverseQ.transformLeft(tmpVector, 0);
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Decomp Q", tmpDecompQ);
            BasicLogger.debug("Now Q", tmpNowQ);
            BasicLogger.debug("Forward Q", tmpForwardQ);
            BasicLogger.debug("Reverse Q", tmpReverseQ);
            BasicLogger.debug();
            BasicLogger.debug("Decomp R", tmpDecompR);
            BasicLogger.debug("Now R", tmpNowR);
            BasicLogger.debug("Forward R", tmpForwardR);
        }

        TestUtils.assertEquals(tmpOriginal, tmpDecomposition, TestUtils.EQUALS.newScale(6));

        TestUtils.assertEquals(tmpDecompQ, tmpNowQ, TestUtils.EQUALS.newScale(6));
        TestUtils.assertEquals(tmpDecompQ, tmpForwardQ, TestUtils.EQUALS.newScale(6));
        TestUtils.assertEquals(tmpDecompQ, tmpReverseQ, TestUtils.EQUALS.newScale(6));

        TestUtils.assertEquals(tmpDecompR, tmpNowR, TestUtils.EQUALS.newScale(6));
        TestUtils.assertEquals(tmpDecompR, tmpForwardR, TestUtils.EQUALS.newScale(6));
    }

    public void testP20030422Case() {

        final BigMatrix tmpOriginal = P20030422Case.getProblematic();

        final QR<BigDecimal> tmpBigDecomp = QRDecomposition.makeBig();
        final QR<ComplexNumber> tmpComplexDecomp = QRDecomposition.makeComplex();
        final QR<Double> tmpPrimitiveDecomp = QRDecomposition.makePrimitive();

        tmpBigDecomp.compute(tmpOriginal);
        tmpComplexDecomp.compute(tmpOriginal);
        tmpPrimitiveDecomp.compute(tmpOriginal);

        final MatrixStore<BigDecimal> tmpBigQ = tmpBigDecomp.getQ();
        final MatrixStore<ComplexNumber> tmpComplexQ = tmpComplexDecomp.getQ();
        final MatrixStore<Double> tmpPrimitiveQ = tmpPrimitiveDecomp.getQ();

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Big Q", tmpBigQ);
            BasicLogger.debug("Complex Q", tmpComplexQ);
            BasicLogger.debug("Primitive Q", tmpPrimitiveQ);
        }

        final MatrixStore<BigDecimal> tmpBigR = tmpBigDecomp.getR();
        final MatrixStore<ComplexNumber> tmpComplexR = tmpComplexDecomp.getR();
        final MatrixStore<Double> tmpPrimitiveR = tmpPrimitiveDecomp.getR();

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Big R", tmpBigR);
            BasicLogger.debug("Complex R", tmpComplexR);
            BasicLogger.debug("Primitive R", tmpPrimitiveR);
        }

        TestUtils.assertEquals(tmpOriginal.toBigStore(), tmpBigDecomp, TestUtils.EQUALS);
        ;
        TestUtils.assertEquals(tmpOriginal.toComplexStore(), tmpComplexDecomp, TestUtils.EQUALS);
        ;
        TestUtils.assertEquals(tmpOriginal.toPrimitiveStore(), tmpPrimitiveDecomp, TestUtils.EQUALS);
        ;

    }

}
