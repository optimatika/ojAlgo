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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class HessenbergTest {

    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
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

    private void doPrint(final HessenbergDecomposition<Double> aDecomposition, final PhysicalStore<Double> aMatrix) {
        BasicLogger.debug();
        BasicLogger.debug();
        BasicLogger.debug("Original: ", aMatrix);
        BasicLogger.debug("Q get: ", aDecomposition.getQ());
        BasicLogger.debug("Q do: ", aDecomposition.doQ(this.makeEye((int) aMatrix.countRows(), (int) Math.min(aMatrix.countRows(), aMatrix.countColumns()))));
        BasicLogger.debug("H: ", aDecomposition.getH());
        BasicLogger.debug("Reconstructed: ", Hessenberg.reconstruct(aDecomposition));
    }

    private void doTestCorrect(final PhysicalStore<Double> aMatrix) {

        final HessenbergDecomposition<Double> tmpDecomposition = (HessenbergDecomposition<Double>) Hessenberg.PRIMITIVE.make();
        tmpDecomposition.decompose(aMatrix);

        if (!Hessenberg.equals(aMatrix, tmpDecomposition, new NumberContext(7, 6))) {
            this.doPrint(tmpDecomposition, aMatrix);
            TestUtils.fail("Not equals!");
        }

        final MatrixStore<Double> tmpReconstructed = Hessenberg.reconstruct(tmpDecomposition);
        if (!Access2D.equals(aMatrix, tmpReconstructed, new NumberContext(7, 6))) {
            this.doPrint(tmpDecomposition, aMatrix);
            TestUtils.fail("Failed to reconstruct!");
        }

        if (!Access2D.equals(tmpDecomposition.getQ(),
                tmpDecomposition.doQ(this.makeEye((int) aMatrix.countRows(), (int) Math.min(aMatrix.countRows(), aMatrix.countColumns()))),
                new NumberContext(7, 6))) {
            this.doPrint(tmpDecomposition, aMatrix);
            TestUtils.fail("get and do Q are different!");
        }

        TestUtils.assertEquals(aMatrix, tmpDecomposition, new NumberContext(7, 6));
    }

    private PrimitiveDenseStore makeEye(final int aRowDim, final int aColDim) {
        return PrimitiveDenseStore.FACTORY.makeEye(aRowDim, aColDim);
    }

}
