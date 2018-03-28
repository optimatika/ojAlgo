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

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import java.math.MathContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.P20050125Case;
import org.ojalgo.matrix.P20061119Case;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class EigenvalueTest {

    static final class EvD {

        DiagonalBasicArray<?> D;

        PhysicalStore<Double> V;

    }

    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testP20050125Case() {

        final PhysicalStore<Double> tmpOriginalMatrix = PrimitiveDenseStore.FACTORY.copy(P20050125Case.getProblematic());

        TestUtils.assertTrue(MatrixUtils.isHermitian(tmpOriginalMatrix));

        final Eigenvalue<Double>[] tmpDecomps = MatrixDecompositionTests.getEigenvaluePrimitiveSymmetric();
        for (Eigenvalue<Double> tmpDecomp : tmpDecomps) {
            tmpDecomp.decompose(tmpOriginalMatrix);
        }

        if (MatrixDecompositionTests.DEBUG) {

            BasicLogger.debug("Eigenvalues");
            for (Eigenvalue<Double> tmpDecomp : tmpDecomps) {
                BasicLogger.debug(tmpDecomp.getClass().getName() + ": " + tmpDecomp.getEigenvalues().toString());
            }

            BasicLogger.debug("D");
            for (Eigenvalue<Double> tmpDecomp : tmpDecomps) {
                BasicLogger.debug(tmpDecomp.getClass().getName() + ": " + PrimitiveDenseStore.FACTORY.copy(tmpDecomp.getD()));
            }

            BasicLogger.debug("V");
            for (Eigenvalue<Double> tmpDecomp : tmpDecomps) {
                BasicLogger.debug(tmpDecomp.getClass().getName() + ": " + PrimitiveDenseStore.FACTORY.copy(tmpDecomp.getV()));
            }
        }

        for (Eigenvalue<Double> tmpDecomp : tmpDecomps) {
            TestUtils.assertEquals(tmpOriginalMatrix, tmpDecomp, new NumberContext(7, 6));
        }
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
        final NumberContext accuracyContext = new NumberContext(7, 6);

        MatrixStore<Double> tmpRecreatedMatrix;

        final Eigenvalue<Double> tmpDecomposition = Eigenvalue.PRIMITIVE.make(tmpOriginalMatrix);
        tmpDecomposition.decompose(tmpOriginalMatrix);

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
            BasicLogger.debug("Original = {}", tmpOriginalMatrix);
            BasicLogger.debug("Recreated = {}", tmpRecreatedMatrix);
        }
        TestUtils.assertEquals(tmpOriginalMatrix.multiply(tmpV), tmpV.multiply(tmpDecomposition.getD()), accuracyContext);

        tmpExpectedDiagonal.sortDescending();
        tmpEigenvalues.sortDescending();
        TestUtils.assertEquals(tmpExpectedDiagonal, tmpEigenvalues, accuracyContext);

        tmpDecomposition.computeValuesOnly(tmpOriginalMatrix);
        final Array1D<ComplexNumber> tmpEigenvaluesOnly = tmpDecomposition.getEigenvalues();
        TestUtils.assertEquals(tmpExpectedDiagonal, tmpEigenvaluesOnly, accuracyContext);
    }

    @Test
    public void testPaulsMathNote() {

        final double[][] tmpData = new double[][] { { 3, -9 }, { 4, -3 } };
        final PrimitiveDenseStore tmpA = PrimitiveDenseStore.FACTORY.rows(tmpData);
        final int tmpLength = tmpData.length;

        final Array1D<ComplexNumber> tmpExpVals = Array1D.COMPLEX.makeZero(2);
        tmpExpVals.set(0, ComplexNumber.of(0.0, THREE * SQRT.invoke(THREE)));
        tmpExpVals.set(1, tmpExpVals.get(0).conjugate());

        final Array2D<ComplexNumber> tmpExpVecs = Array2D.COMPLEX.makeZero(2, 2);
        tmpExpVecs.set(0, 0, ComplexNumber.of(THREE, ZERO));
        tmpExpVecs.set(1, 0, ComplexNumber.of(ONE, -SQRT.invoke(THREE)));
        tmpExpVecs.set(0, 1, ComplexNumber.of(THREE, ZERO));
        tmpExpVecs.set(1, 1, ComplexNumber.of(ONE, SQRT.invoke(THREE)));

        final Eigenvalue<Double> tmpEvD = Eigenvalue.PRIMITIVE.make(tmpA, false);
        tmpEvD.decompose(tmpA);

        final MatrixStore<Double> tmpD = tmpEvD.getD();
        final MatrixStore<Double> tmpV = tmpEvD.getV();

        final Array1D<ComplexNumber> tmpValues = tmpEvD.getEigenvalues();
        final MatrixStore<ComplexNumber> tmpVectors = tmpEvD.getEigenvectors();

        TestUtils.assertEquals(tmpExpVals, tmpValues);
        for (int j = 0; j < tmpLength; j++) {
            final Array1D<ComplexNumber> tmpSliceColumn = tmpExpVecs.sliceColumn(0, j);
            final Access1D<ComplexNumber> tmpActual = tmpVectors.sliceColumn(0, j);

            final ComplexNumber tmpFactor = tmpActual.get(0).divide(tmpSliceColumn.get(0));

            TestUtils.assertEquals(tmpSliceColumn.get(1).multiply(tmpFactor), tmpActual.get(1));
        }

        final ComplexDenseStore tmpCmplA = ComplexDenseStore.FACTORY.copy(tmpA);
        final ComplexDenseStore tmpCmplD = ComplexDenseStore.FACTORY.copy(tmpD);
        final ComplexDenseStore tmpCmplV = ComplexDenseStore.FACTORY.copy(tmpV);

        final MatrixStore<ComplexNumber> tmpExp1 = tmpCmplA.multiply(tmpCmplV);
        final MatrixStore<ComplexNumber> tmpAct1 = tmpCmplV.multiply(tmpCmplD);
        TestUtils.assertEquals(tmpExp1, tmpAct1);

        final ComplexDenseStore tmpComplexD = ComplexDenseStore.FACTORY.makeZero(tmpLength, tmpLength);
        for (int j = 0; j < tmpLength; j++) {
            tmpComplexD.set(j, j, tmpValues.get(j));
        }

        final MatrixStore<ComplexNumber> tmpExp2 = tmpCmplA.multiply(tmpVectors);
        final MatrixStore<ComplexNumber> tmpAct2 = tmpVectors.multiply(tmpComplexD);
        TestUtils.assertEquals(tmpExp2, tmpAct2);

        tmpEvD.computeValuesOnly(tmpA);
        final Array1D<ComplexNumber> tmpEigenvaluesOnly = tmpEvD.getEigenvalues();
        TestUtils.assertEquals(tmpValues, tmpEigenvaluesOnly);

    }

    @Test
    public void testPrimitiveAsComplex() {

        final double[][] tmpData = new double[][] { { 1, 0, 3 }, { 0, 4, 1 }, { -5, 1, 0 } };
        final PrimitiveDenseStore tmpA = PrimitiveDenseStore.FACTORY.rows(tmpData);

        final int tmpLength = tmpData.length;

        final Eigenvalue<Double> tmpEvD = Eigenvalue.PRIMITIVE.make(tmpA, false);

        tmpEvD.decompose(tmpA);

        final MatrixStore<Double> tmpD = tmpEvD.getD();
        final MatrixStore<Double> tmpV = tmpEvD.getV();

        final Array1D<ComplexNumber> tmpValues = tmpEvD.getEigenvalues();
        final MatrixStore<ComplexNumber> tmpVectors = tmpEvD.getEigenvectors();

        final ComplexDenseStore tmpCmplA = ComplexDenseStore.FACTORY.copy(tmpA);
        final ComplexDenseStore tmpCmplD = ComplexDenseStore.FACTORY.copy(tmpD);
        final ComplexDenseStore tmpCmplV = ComplexDenseStore.FACTORY.copy(tmpV);

        final MatrixStore<ComplexNumber> tmpExp1 = tmpCmplA.multiply(tmpCmplV);
        final MatrixStore<ComplexNumber> tmpAct1 = tmpCmplV.multiply(tmpCmplD);
        TestUtils.assertEquals(tmpExp1, tmpAct1);

        final ComplexDenseStore tmpAltD = ComplexDenseStore.FACTORY.makeZero(tmpLength, tmpLength);
        final MatrixStore<ComplexNumber> tmpAltV = tmpVectors;

        for (int j = 0; j < tmpLength; j++) {
            tmpAltD.set(j, j, tmpValues.get(j));
        }

        final MatrixStore<ComplexNumber> tmpExp2 = tmpCmplA.multiply(tmpAltV);
        final MatrixStore<ComplexNumber> tmpAct2 = tmpAltV.multiply(tmpAltD);
        TestUtils.assertEquals(tmpExp2, tmpAct2);

        tmpEvD.computeValuesOnly(tmpA);
        final Array1D<ComplexNumber> tmpEigenvaluesOnly = tmpEvD.getEigenvalues();
        TestUtils.assertEquals(tmpValues, tmpEigenvaluesOnly);
    }

    /**
     * A matrix that has been problematic for another library...
     */
    @Test
    public void testProblemFoundInTheWild() {

        final PrimitiveDenseStore matrix = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1, 0, 0 }, { 0.01, 0, -1 }, { 0.01, 1, 0 } });

        for (final Eigenvalue<Double> tmpEigenvalue : MatrixDecompositionTests.getEigenvaluePrimitiveGeneral()) {

            tmpEigenvalue.decompose(matrix);

            TestUtils.assertEquals(matrix, tmpEigenvalue, NumberContext.getGeneral(MathContext.DECIMAL64));

            final Array1D<ComplexNumber> tmpValues = tmpEigenvalue.getEigenvalues();

            tmpEigenvalue.computeValuesOnly(matrix);
            final Array1D<ComplexNumber> tmpEigenvaluesOnly = tmpEigenvalue.getEigenvalues();
            TestUtils.assertEquals(tmpValues, tmpEigenvaluesOnly);

        }
    }

    @Test
    public void testRandomSymmetricValuesOnly() {

        final NumberContext evaluationContext = NumberContext.getGeneral(MathContext.DECIMAL32);

        for (int dim = 1; dim < 10; dim++) {

            final PrimitiveDenseStore matrix = MatrixUtils.makeSPD(dim);

            for (final Eigenvalue<Double> decomp : MatrixDecompositionTests.getEigenvaluePrimitiveSymmetric()) {

                decomp.decompose(matrix);
                TestUtils.assertEquals(matrix, decomp, evaluationContext);

                final Array1D<ComplexNumber> expected = decomp.getEigenvalues();
                decomp.computeValuesOnly(matrix);
                final Array1D<ComplexNumber> actual = decomp.getEigenvalues();
                TestUtils.assertEquals(expected, actual, evaluationContext);
            }
        }
    }

}
