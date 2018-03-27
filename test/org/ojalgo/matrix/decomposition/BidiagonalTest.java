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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class BidiagonalTest {

    @Test
    public void testCaseFromMatrixComputations() {

        final PhysicalStore<Double> tmpMatrix = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 }, { 10, 11, 12 } });

        this.doTestCorrect(tmpMatrix);
    }

    @Test
    public void testComplexSquareCase() {

        final PhysicalStore<ComplexNumber> tmpOriginal = MatrixUtils.makeRandomComplexStore(4, 4);

        final Bidiagonal<ComplexNumber> tmpDecomposition = Bidiagonal.COMPLEX.make();
        tmpDecomposition.decompose(tmpOriginal);

        final MatrixStore<ComplexNumber> tmpReconstructed = tmpDecomposition.reconstruct();

        final MatrixStore<ComplexNumber> tmpQ1 = tmpDecomposition.getQ1();
        final MatrixStore<ComplexNumber> tmpD = tmpDecomposition.getD();
        final MatrixStore<ComplexNumber> tmpQ2 = tmpDecomposition.getQ2();

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug(tmpDecomposition.toString());
            BasicLogger.debug("Original", tmpOriginal);
            BasicLogger.debug("Q1", tmpQ1);
            BasicLogger.debug("D", tmpD);
            BasicLogger.debug("Q2", tmpQ2);
            BasicLogger.debug("Reconstructed", tmpReconstructed);
            BasicLogger.debug("Q1 orthogonal (left)", tmpQ1.conjugate().multiply(tmpQ1));
            BasicLogger.debug("Q1 orthogonal (right)", tmpQ1.multiply(tmpQ1.conjugate()));
            BasicLogger.debug("Q2 orthogonal (left)", tmpQ2.conjugate().multiply(tmpQ2));
            BasicLogger.debug("Q2 orthogonal (right)", tmpQ2.multiply(tmpQ2.conjugate()));
        }

        TestUtils.assertEquals(tmpOriginal, tmpDecomposition, new NumberContext(7, 4));
        TestUtils.assertEquals(tmpOriginal, tmpReconstructed, new NumberContext(7, 6));
    }

    @Test
    public void testFatEye() {

        final PhysicalStore<Double> tmpMatrix = PrimitiveDenseStore.FACTORY.makeEye(4, 6);

        this.doTestCorrect(tmpMatrix);
    }

    @Test
    public void testFatRandom() {

        final PhysicalStore<Double> tmpMatrix = PrimitiveDenseStore.FACTORY.copy(MatrixUtils.makeRandomComplexStore(4, 6));

        this.doTestCorrect(tmpMatrix);
    }

    @Test
    public void testSquareBidiagonal() {

        final PhysicalStore<Double> tmpMatrix = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 1, 4, 0, 0 }, { 0, 4, 1, 0 }, { 0, 0, 3, 4 }, { 0, 0, 0, 3 } });

        this.doTestCorrect(tmpMatrix);
    }

    @Test
    public void testSquareEye() {

        final PhysicalStore<Double> tmpMatrix = PrimitiveDenseStore.FACTORY.makeEye(5, 5);

        this.doTestCorrect(tmpMatrix);
    }

    @Test
    public void testSquareRandom() {

        final PhysicalStore<Double> tmpMatrix = PrimitiveDenseStore.FACTORY.copy(MatrixUtils.makeRandomComplexStore(5, 5));

        this.doTestCorrect(tmpMatrix);
    }

    @Test
    public void testTallEye() {

        final PhysicalStore<Double> tmpMatrix = PrimitiveDenseStore.FACTORY.makeEye(6, 4);

        this.doTestCorrect(tmpMatrix);
    }

    @Test
    public void testTallRandom() {

        final PhysicalStore<Double> tmpMatrix = PrimitiveDenseStore.FACTORY.copy(MatrixUtils.makeRandomComplexStore(6, 4));

        this.doTestCorrect(tmpMatrix);
    }

    private void doPrint(final BidiagonalDecomposition<Double> aDecomposition, final PhysicalStore<Double> aMatrix) {
        BasicLogger.debug();
        BasicLogger.debug();
        BasicLogger.debug("Original: ", aMatrix);
        BasicLogger.debug("Q1 get: ", aDecomposition.getQ1());
        BasicLogger.debug("D: ", aDecomposition.getD());
        BasicLogger.debug("Q2 get: ", aDecomposition.getQ2());
        BasicLogger.debug("Reconstructed: ", Bidiagonal.reconstruct(aDecomposition));
    }

    private void doTestCorrect(final PhysicalStore<Double> aMatrix) {

        final BidiagonalDecomposition<Double> tmpDecomposition = (BidiagonalDecomposition<Double>) Bidiagonal.PRIMITIVE.make();
        tmpDecomposition.decompose(aMatrix);

        if (!Bidiagonal.equals(aMatrix, tmpDecomposition, new NumberContext(7, 6))) {
            this.doPrint(tmpDecomposition, aMatrix);
            TestUtils.fail("Not equals, easy!");
        }

        if (!Bidiagonal.equals(aMatrix, tmpDecomposition, new NumberContext(7, 6))) {
            this.doPrint(tmpDecomposition, aMatrix);
            TestUtils.fail("Not equals, hard!");
        }

        final MatrixStore<Double> tmpReconstructed = Bidiagonal.reconstruct(tmpDecomposition);
        if (!Access2D.equals(aMatrix, tmpReconstructed, new NumberContext(7, 6))) {
            this.doPrint(tmpDecomposition, aMatrix);
            TestUtils.fail("Failed to reconstruct!");
        }

        TestUtils.assertEquals(aMatrix, tmpDecomposition, new NumberContext(7, 6));
    }

    private PrimitiveDenseStore makeEye(final int aRowDim, final int aColDim) {
        return PrimitiveDenseStore.FACTORY.makeEye(aRowDim, aColDim);
    }

}
