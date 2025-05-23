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
import org.ojalgo.TestUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class SVDbyEvD extends MatrixDecompositionTests {

    private static final NumberContext CONTEXT = NumberContext.of(7, 6);

    @Override
    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    /**
     * Data from example 3.15 in Scientific Computing by Michael T. Heath
     */
    @Test
    public void testHeath() {

        final PhysicalStore<Double> tmpMtrx = RawStore.wrap(new double[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 }, { 10, 11, 12 } });

        final Array1D<Double> tmpSingularValues = Array1D.R064.copy(new double[] { 25.4624074360364, 1.29066167576123, 0.0 });

        this.doTest(tmpMtrx, tmpSingularValues);
    }

    /**
     * http://www.miislita.com/information-retrieval-tutorial/singular-value-decomposition-fast-track-tutorial
     * .pdf
     */
    @Test
    public void testSmall2x2() {

        final PhysicalStore<Double> tmpMtrx = RawStore.wrap(new double[][] { { 4.0, 0.0 }, { 3.0, -5.0 } });

        final Array1D<Double> tmpSingularValues = Array1D.R064.copy(new double[] { 6.324555320336759, 3.1622776601683795 });

        this.doTest(tmpMtrx, tmpSingularValues);
    }

    /**
     * http://en.wikipedia.org/wiki/Singular_value_decomposition
     */
    @Test
    public void testWikipedia() {

        final PhysicalStore<Double> tmpMtrx = RawStore
                .wrap(new double[][] { { 1.0, 0.0, 0.0, 0.0, 2.0 }, { 0.0, 0.0, 3.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 4.0, 0.0, 0.0, 0.0 } });

        final Array1D<Double> tmpSingularValues = Array1D.R064.copy(new double[] { 4.0, 3.0, PrimitiveMath.SQRT.invoke(5.0), 0.0 });

        this.doTest(tmpMtrx, tmpSingularValues);
    }

    private void doTest(final PhysicalStore<Double> matrixA, final Array1D<Double> singularValues) {

        final MatrixStore<Double> transpA = matrixA.transpose();
        final MatrixStore<Double> leftA = matrixA.multiply(transpA);
        final MatrixStore<Double> rightA = transpA.multiply(matrixA);

        final Eigenvalue<Double> decompEvD = Eigenvalue.R064.make(true);

        decompEvD.decompose(leftA);
        final MatrixStore<Double> leftD = decompEvD.getD();
        final MatrixStore<Double> leftV = decompEvD.getV();
        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debugMatrix("Left D", leftD, CONTEXT);
            BasicLogger.debugMatrix("Left V", leftV, CONTEXT);
        }
        // Check that the eigenvalue decomposition of the "left" matrix is correct
        TestUtils.assertEquals(leftA, decompEvD, CONTEXT);

        decompEvD.decompose(rightA);
        final MatrixStore<Double> rightD = decompEvD.getD();
        final MatrixStore<Double> rightV = decompEvD.getV();
        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debugMatrix("Right D", rightD, CONTEXT);
            BasicLogger.debugMatrix("Right V", rightV, CONTEXT);
        }
        // Check that the eigenvalue decomposition of the "right" matrix is correct
        TestUtils.assertEquals(rightA, decompEvD, CONTEXT);

        // Check that the, left and right, singular values are correct
        for (int ij = 0; ij < singularValues.length; ij++) {
            final double expected = singularValues.doubleValue(ij);
            final double leftActual = PrimitiveMath.SQRT.invoke(PrimitiveMath.ABS.invoke(leftD.doubleValue(ij, ij)));
            final double rightActual = PrimitiveMath.SQRT.invoke(PrimitiveMath.ABS.invoke(rightD.doubleValue(ij, ij)));
            TestUtils.assertEquals("Left " + ij, expected, leftActual, CONTEXT);
            TestUtils.assertEquals("Right " + ij, expected, rightActual, CONTEXT);
        }

        // So far...

        final SingularValue<Double> tmpExperimental = SingularValue.R064.make();
        tmpExperimental.decompose(matrixA);

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Experimental  S: {}.", tmpExperimental.getSingularValues());
            BasicLogger.debugMatrix("D", tmpExperimental.getD(), CONTEXT);
            BasicLogger.debugMatrix("Q1", tmpExperimental.getU(), CONTEXT);
            BasicLogger.debugMatrix("Q2", tmpExperimental.getV(), CONTEXT);
        }

        TestUtils.assertEquals(matrixA, tmpExperimental, CONTEXT);
    }

}
