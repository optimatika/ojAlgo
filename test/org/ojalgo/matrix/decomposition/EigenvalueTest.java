/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import java.math.MathContext;

import org.ojalgo.TestUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.P20050125Case;
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
public class EigenvalueTest extends MatrixDecompositionTests {

    static final class EvD {

        DiagonalAccess<?> D;

        PhysicalStore<Double> V;

    }

    private static void doTest(final PhysicalStore<Double> originalMatrix, final Array1D<ComplexNumber> expectedEigenvalues,
            final NumberContext accuracyContext) {

        MatrixStore<Double> tmpRecreatedMatrix;

        final Eigenvalue<Double> tmpDecomposition = Eigenvalue.PRIMITIVE.make();
        tmpDecomposition.decompose(originalMatrix);

        final Array1D<ComplexNumber> tmpEigenvalues = tmpDecomposition.getEigenvalues();
        final MatrixStore<Double> tmpD = tmpDecomposition.getD();
        final MatrixStore<Double> tmpV = tmpDecomposition.getV();

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Eigenvalues = {}", tmpEigenvalues);
            BasicLogger.debug("D = {}", tmpD);
            BasicLogger.debug("V = {}", tmpV);
        }

        tmpRecreatedMatrix = tmpV.multiply(tmpDecomposition.getD()).multiply(tmpV.transpose());
        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Original = {}", originalMatrix);
            BasicLogger.debug("Recreated = {}", tmpRecreatedMatrix);
        }
        TestUtils.assertEquals(originalMatrix.multiply(tmpV), tmpV.multiply(tmpDecomposition.getD()), accuracyContext);

        expectedEigenvalues.sortDescending();
        tmpEigenvalues.sortDescending();
        TestUtils.assertEquals(expectedEigenvalues, tmpEigenvalues, accuracyContext);
    }

    public EigenvalueTest() {
        super();
    }

    public EigenvalueTest(final String arg0) {
        super(arg0);
    }

    public void testP20050125Case() {

        final PhysicalStore<Double> tmpOriginalMatrix = P20050125Case.getProblematic().toPrimitiveStore();

        TestUtils.assertTrue(MatrixUtils.isHermitian(tmpOriginalMatrix));

        final Eigenvalue<Double>[] tmpDecomps = MatrixDecompositionTests.getEigenvaluePrimitiveSymmetric();
        for (int d = 0; d < tmpDecomps.length; d++) {
            tmpDecomps[d].decompose(tmpOriginalMatrix);
        }

        if (MatrixDecompositionTests.DEBUG) {

            BasicLogger.debug("Eigenvalues");
            for (int d = 0; d < tmpDecomps.length; d++) {
                BasicLogger.debug(tmpDecomps[d].getClass().getName() + ": " + tmpDecomps[d].getEigenvalues().toString());
            }

            BasicLogger.debug("D");
            for (int d = 0; d < tmpDecomps.length; d++) {
                BasicLogger.debug(tmpDecomps[d].getClass().getName() + ": " + PrimitiveDenseStore.FACTORY.copy(tmpDecomps[d].getD()));
            }

            BasicLogger.debug("V");
            for (int d = 0; d < tmpDecomps.length; d++) {
                BasicLogger.debug(tmpDecomps[d].getClass().getName() + ": " + PrimitiveDenseStore.FACTORY.copy(tmpDecomps[d].getV()));
            }
        }

        for (int d = 0; d < tmpDecomps.length; d++) {
            TestUtils.assertEquals(tmpOriginalMatrix, tmpDecomps[d], new NumberContext(7, 6));
        }
    }

    public void testP20061119Case() {

        final PhysicalStore<Double> tmpOriginalMatrix = P20061119Case.getProblematic().toPrimitiveStore();

        final ComplexNumber tmp00 = ComplexNumber.valueOf(26.14421883828456);
        final ComplexNumber tmp11 = ComplexNumber.of(2.727890580857718, 3.6223578444417908);
        final ComplexNumber tmp22 = tmp11.conjugate();
        final ComplexNumber tmp33 = ComplexNumber.ZERO;
        final ComplexNumber tmp44 = tmp33;

        final Array1D<ComplexNumber> tmpExpectedDiagonal = Array1D.COMPLEX.copy(new ComplexNumber[] { tmp00, tmp11, tmp22, tmp33, tmp44 });

        EigenvalueTest.doTest(tmpOriginalMatrix, tmpExpectedDiagonal, new NumberContext(7, 6));
    }

    /**
     * A matrix that has been problematic for another library...
     */
    public void testProblemFoundInTheWild() {

        final PrimitiveDenseStore matrix = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1, 0, 0 }, { 0.01, 0, -1 }, { 0.01, 1, 0 } });

        for (final Eigenvalue<Double> tmpEigenvalue : MatrixDecompositionTests.getEigenvaluePrimitiveNonsymmetric()) {

            tmpEigenvalue.decompose(matrix);

            TestUtils.assertEquals(matrix, tmpEigenvalue, NumberContext.getGeneral(MathContext.DECIMAL64));
        }
    }

}
