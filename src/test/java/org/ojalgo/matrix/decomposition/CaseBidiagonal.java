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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class CaseBidiagonal extends MatrixDecompositionTests {

    @Test
    public void testCaseFromMatrixComputations() {

        PhysicalStore<Double> tmpMatrix = RawStore.wrap(new double[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 }, { 10, 11, 12 } });

        this.doTestCorrect(tmpMatrix);
    }

    @Test
    public void testComplexSquareCase() {

        final PhysicalStore<ComplexNumber> tmpOriginal = TestUtils.makeRandomComplexStore(4, 4);

        final Bidiagonal<ComplexNumber> tmpDecomposition = Bidiagonal.C128.make();
        tmpDecomposition.decompose(tmpOriginal);

        final MatrixStore<ComplexNumber> tmpReconstructed = tmpDecomposition.reconstruct();

        final MatrixStore<ComplexNumber> tmpQ1 = tmpDecomposition.getLQ();
        final MatrixStore<ComplexNumber> tmpD = tmpDecomposition.getD();
        final MatrixStore<ComplexNumber> tmpQ2 = tmpDecomposition.getRQ();

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug(tmpDecomposition.toString());
            BasicLogger.debugMatrix("Original", tmpOriginal);
            BasicLogger.debugMatrix("Q1", tmpQ1);
            BasicLogger.debugMatrix("D", tmpD);
            BasicLogger.debugMatrix("Q2", tmpQ2);
            BasicLogger.debugMatrix("Reconstructed", tmpReconstructed);
            BasicLogger.debugMatrix("Q1 orthogonal (left)", tmpQ1.conjugate().multiply(tmpQ1));
            BasicLogger.debugMatrix("Q1 orthogonal (right)", tmpQ1.multiply(tmpQ1.conjugate()));
            BasicLogger.debugMatrix("Q2 orthogonal (left)", tmpQ2.conjugate().multiply(tmpQ2));
            BasicLogger.debugMatrix("Q2 orthogonal (right)", tmpQ2.multiply(tmpQ2.conjugate()));
        }

        TestUtils.assertEquals(tmpOriginal, tmpDecomposition, NumberContext.of(7, 4));
        TestUtils.assertEquals(tmpOriginal, tmpReconstructed, NumberContext.of(7, 6));
    }

    @Test
    public void testFatEye() {

        final PhysicalStore<Double> tmpMatrix = R064Store.FACTORY.makeEye(4, 6);

        this.doTestCorrect(tmpMatrix);
    }

    @Test
    public void testFatRandom() {

        final PhysicalStore<Double> tmpMatrix = R064Store.FACTORY.copy(TestUtils.makeRandomComplexStore(4, 6));

        this.doTestCorrect(tmpMatrix);
    }

    @Test
    public void testSquareBidiagonal() {

        PhysicalStore<Double> tmpMatrix = RawStore.wrap(new double[][] { { 1, 4, 0, 0 }, { 0, 4, 1, 0 }, { 0, 0, 3, 4 }, { 0, 0, 0, 3 } });

        this.doTestCorrect(tmpMatrix);
    }

    @Test
    public void testSquareEye() {

        final PhysicalStore<Double> tmpMatrix = R064Store.FACTORY.makeEye(5, 5);

        this.doTestCorrect(tmpMatrix);
    }

    @Test
    public void testSquareRandom() {

        final PhysicalStore<Double> tmpMatrix = R064Store.FACTORY.copy(TestUtils.makeRandomComplexStore(5, 5));

        this.doTestCorrect(tmpMatrix);
    }

    @Test
    public void testTallEye() {

        final PhysicalStore<Double> tmpMatrix = R064Store.FACTORY.makeEye(6, 4);

        this.doTestCorrect(tmpMatrix);
    }

    @Test
    public void testTallRandom() {

        final PhysicalStore<Double> tmpMatrix = R064Store.FACTORY.copy(TestUtils.makeRandomComplexStore(6, 4));

        this.doTestCorrect(tmpMatrix);
    }

    private void doPrint(final DenseBidiagonal<Double> aDecomposition, final PhysicalStore<Double> aMatrix) {
        BasicLogger.debug();
        BasicLogger.debug();
        BasicLogger.debugMatrix("Original: ", aMatrix);
        BasicLogger.debugMatrix("Q1 get: ", aDecomposition.getLQ());
        BasicLogger.debugMatrix("D: ", aDecomposition.getD());
        BasicLogger.debugMatrix("Q2 get: ", aDecomposition.getRQ());
        BasicLogger.debugMatrix("Reconstructed: ", aDecomposition.reconstruct());
    }

    private void doTestCorrect(final PhysicalStore<Double> aMatrix) {

        final DenseBidiagonal<Double> tmpDecomposition = (DenseBidiagonal<Double>) Bidiagonal.R064.make();
        tmpDecomposition.decompose(aMatrix);

        if (!Bidiagonal.equals(aMatrix, tmpDecomposition, NumberContext.of(7, 6))) {
            this.doPrint(tmpDecomposition, aMatrix);
            TestUtils.fail("Not equals, easy!");
        }

        if (!Bidiagonal.equals(aMatrix, tmpDecomposition, NumberContext.of(7, 6))) {
            this.doPrint(tmpDecomposition, aMatrix);
            TestUtils.fail("Not equals, hard!");
        }

        final MatrixStore<Double> tmpReconstructed = tmpDecomposition.reconstruct();
        if (!Access2D.equals(aMatrix, tmpReconstructed, NumberContext.of(7, 6))) {
            this.doPrint(tmpDecomposition, aMatrix);
            TestUtils.fail("Failed to reconstruct!");
        }

        TestUtils.assertEquals(aMatrix, tmpDecomposition, NumberContext.of(7, 6));
    }

    private R064Store makeEye(final int aRowDim, final int aColDim) {
        return R064Store.FACTORY.makeEye(aRowDim, aColDim);
    }

}
