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
import org.ojalgo.array.Array1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.P20061119Case;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class SchurTest {

    private static void doTest(final PhysicalStore<Double> originalMatrix, final Array1D<ComplexNumber> expectedDiagonal, final NumberContext accuracyContext) {

        MatrixStore<Double> tmpRecreatedMatrix;

        final Schur<Double> tmpSchurDecomp = Schur.makePrimitive();
        tmpSchurDecomp.decompose(originalMatrix);

        final Array1D<ComplexNumber> tmpDiagonal = tmpSchurDecomp.getDiagonal();
        final MatrixStore<Double> tmpU = tmpSchurDecomp.getU();
        final MatrixStore<Double> tmpQ = tmpSchurDecomp.getQ();

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Diagonal = {}", tmpDiagonal);
            BasicLogger.debug("U = {}", tmpU);
            BasicLogger.debug("Q = {}", tmpQ);
        }

        tmpRecreatedMatrix = Schur.reconstruct(tmpSchurDecomp);
        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Original = {}", originalMatrix);
            BasicLogger.debug("Recreated = {}", tmpRecreatedMatrix);
        }
        TestUtils.assertEquals(originalMatrix, tmpRecreatedMatrix, accuracyContext);

        expectedDiagonal.sortDescending();
        tmpDiagonal.sortDescending();
        TestUtils.assertEquals(expectedDiagonal, tmpDiagonal, accuracyContext);
    }

    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testDiagonalCase() {

        final PhysicalStore<Double> tmpOriginalMatrix = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 4.0, 3.0, 2.0, 1.0 }, { 0.0, 3.0, 2.0, 1.0 }, { 0.0, 0.0, 2.0, 1.0 }, { 0.0, 0.0, 0.0, 1.0 } });

        final ComplexNumber tmp00 = ComplexNumber.valueOf(4.0);
        final ComplexNumber tmp11 = ComplexNumber.valueOf(3.0);
        final ComplexNumber tmp22 = ComplexNumber.valueOf(2.0);
        final ComplexNumber tmp33 = ComplexNumber.valueOf(1.0);

        final Array1D<ComplexNumber> tmpExpectedDiagonal = Array1D.COMPLEX.copy(new ComplexNumber[] { tmp00, tmp11, tmp22, tmp33 });

        SchurTest.doTest(tmpOriginalMatrix, tmpExpectedDiagonal, new NumberContext(7, 6));
    }

    /**
     * http://mathworld.wolfram.com/SchurDecomposition.html
     */
    @Test
    public void testMathWorldCase() {

        final PhysicalStore<Double> tmpOriginalMatrix = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 3, 2, 1 }, { 4, 2, 1 }, { 4, 4, 0 } });
        final double tmp00 = 3.0 + PrimitiveFunction.SQRT.invoke(13.0);
        final double tmp11 = 3.0 - PrimitiveFunction.SQRT.invoke(13.0);
        final double tmp22 = -1.0;
        final Array1D<ComplexNumber> tmpExpectedDiagonal = Array1D.COMPLEX
                .copy(new ComplexNumber[] { ComplexNumber.valueOf(tmp00), ComplexNumber.valueOf(tmp11), ComplexNumber.valueOf(tmp22) });

        SchurTest.doTest(tmpOriginalMatrix, tmpExpectedDiagonal, new NumberContext(7, 3));

        PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.49857, 0.76469, 0.40825 }, { 0.57405, 0.061628, -0.81650 }, { 0.64953, -0.64144, 0.40825 } });
        PrimitiveDenseStore.FACTORY.rows(new double[][] { { tmp00, 4.4907, -0.82632 }, { 0.0, tmp11, 1.0726 }, { 0.0, 0.0, tmp22 } });
    }

    @Test
    public void testP20061119Case() {

        final PhysicalStore<Double> tmpOriginalMatrix = PrimitiveDenseStore.FACTORY.copy(P20061119Case.getProblematic());

        final ComplexNumber tmp00 = ComplexNumber.valueOf(26.14421883828456);
        final ComplexNumber tmp11 = ComplexNumber.of(2.727890580857718, 3.6223578444417908);
        final ComplexNumber tmp22 = tmp11.conjugate();
        final ComplexNumber tmp33 = ComplexNumber.ZERO;
        final ComplexNumber tmp44 = tmp33;

        final Array1D<ComplexNumber> tmpExpectedDiagonal = Array1D.COMPLEX.copy(new ComplexNumber[] { tmp00, tmp11, tmp22, tmp33, tmp44 });

        SchurTest.doTest(tmpOriginalMatrix, tmpExpectedDiagonal, new NumberContext(7, 6));
    }

    /**
     * http://planetmath.org/encyclopedia/AnExampleForSchurDecomposition.html
     */
    @Test
    public void testPlanetMathCase() {

        final PhysicalStore<Double> tmpOriginalMatrix = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 5, 7 }, { -2, -4 } });
        PrimitiveDenseStore.FACTORY.rows(
                new double[][] { { 1 / PrimitiveMath.SQRT_TWO, 1 / PrimitiveMath.SQRT_TWO }, { -1 / PrimitiveMath.SQRT_TWO, 1 / PrimitiveMath.SQRT_TWO } });
        final double tmp00 = -2;
        final double tmp11 = 3.0;
        final Array1D<ComplexNumber> tmpExpectedDiagonal = Array1D.COMPLEX
                .copy(new ComplexNumber[] { ComplexNumber.valueOf(tmp00), ComplexNumber.valueOf(tmp11) });
        PrimitiveDenseStore.FACTORY.rows(new double[][] { { tmp00, 9 }, { 0.0, tmp11 } });

        SchurTest.doTest(tmpOriginalMatrix, tmpExpectedDiagonal, new NumberContext(7, 5));
    }
}
