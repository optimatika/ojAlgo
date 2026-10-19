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

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class CaseHessenberg extends MatrixDecompositionTests {

    @Override
    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
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

    /**
     * A column with a tiny part below the subdiagonal (compared to the column) still gets a reflector, so
     * that the decomposition is accurate to full precision.
     */
    @Test
    public void testTinySubdiagonal() {

        NumberContext accuracy = NumberContext.of(12, 14);

        for (double tail : new double[] { 1E-9, 1E-12, 1E-17 }) {

            Random random = new Random(2);
            double[][] rows = new double[8][8];
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    rows[i][j] = random.nextDouble() - 0.5;
                }
            }
            for (int i = 2; i < 8; i++) {
                rows[i][0] *= tail;
            }

            R064Store primitive = R064Store.FACTORY.rows(rows);
            Hessenberg<Double> decomposition = Hessenberg.R064.make(primitive);
            decomposition.decompose(primitive);
            TestUtils.assertEquals(primitive, decomposition, accuracy);
            MatrixStore<Double> q = decomposition.getQ();
            TestUtils.assertEquals(R064Store.FACTORY.makeIdentity(8), q.transpose().multiply(q), accuracy);

            GenericStore<ComplexNumber> complex = GenericStore.C128.rows(rows);
            Hessenberg<ComplexNumber> complexDecomposition = Hessenberg.C128.make(complex);
            complexDecomposition.decompose(complex);
            TestUtils.assertEquals(complex, complexDecomposition, accuracy);
        }
    }

    private void doPrint(final DenseHessenberg<Double, R064Store> aDecomposition, final PhysicalStore<Double> aMatrix) {
        BasicLogger.debug();
        BasicLogger.debug();
        BasicLogger.debugMatrix("Original: ", aMatrix);
        BasicLogger.debugMatrix("Q get: ", aDecomposition.getQ());
        BasicLogger.debugMatrix("Q do: ",
                aDecomposition.doQ(this.makeEye((int) aMatrix.countRows(), (int) Math.min(aMatrix.countRows(), aMatrix.countColumns()))));
        BasicLogger.debugMatrix("H: ", aDecomposition.getH());
        BasicLogger.debugMatrix("Reconstructed: ", aDecomposition.reconstruct());
    }

    private void doTestCorrect(final PhysicalStore<Double> aMatrix) {

        final DenseHessenberg<Double, R064Store> tmpDecomposition = (DenseHessenberg<Double, R064Store>) Hessenberg.R064.make();
        tmpDecomposition.decompose(aMatrix);

        if (!Hessenberg.equals(aMatrix, tmpDecomposition, NumberContext.of(7, 6))) {
            this.doPrint(tmpDecomposition, aMatrix);
            TestUtils.fail("Not equals!");
        }

        final MatrixStore<Double> tmpReconstructed = tmpDecomposition.reconstruct();
        if (!Access2D.equals(aMatrix, tmpReconstructed, NumberContext.of(7, 6))) {
            this.doPrint(tmpDecomposition, aMatrix);
            TestUtils.fail("Failed to reconstruct!");
        }

        if (!Access2D.equals(tmpDecomposition.getQ(),
                tmpDecomposition.doQ(this.makeEye((int) aMatrix.countRows(), (int) Math.min(aMatrix.countRows(), aMatrix.countColumns()))),
                NumberContext.of(7, 6))) {
            this.doPrint(tmpDecomposition, aMatrix);
            TestUtils.fail("get and do Q are different!");
        }

        TestUtils.assertEquals(aMatrix, tmpDecomposition, NumberContext.of(7, 6));
    }

    private R064Store makeEye(final int aRowDim, final int aColDim) {
        return R064Store.FACTORY.makeEye(aRowDim, aColDim);
    }

}
