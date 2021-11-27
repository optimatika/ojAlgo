/*
 * Copyright 1997-2021 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.io.IOException;
import java.math.MathContext;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.P20050125Case;
import org.ojalgo.matrix.P20061119Case;
import org.ojalgo.matrix.decomposition.Eigenvalue.Generalisation;
import org.ojalgo.matrix.store.DiagonalStore;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class CaseEigenvalue extends MatrixDecompositionTests {

    static class EvD {

        DiagonalStore<?, ?> D;

        PhysicalStore<Double> V;

    }

    private static void doVerifyGeneral(final Primitive64Store matrix) {

        for (Eigenvalue<Double> tmpEigenvalue : MatrixDecompositionTests.getPrimitiveEigenvalueGeneral()) {

            tmpEigenvalue.decompose(matrix);

            TestUtils.assertEquals(matrix, tmpEigenvalue, NumberContext.getGeneral(MathContext.DECIMAL32));

            Array1D<ComplexNumber> tmpValues = tmpEigenvalue.getEigenvalues();

            tmpEigenvalue.computeValuesOnly(matrix);
            Array1D<ComplexNumber> tmpEigenvaluesOnly = tmpEigenvalue.getEigenvalues();
            TestUtils.assertEquals(tmpValues, tmpEigenvaluesOnly);
        }
    }

    @Override
    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    /**
     * org.ojalgo.matrix.decomposition.EvD2D.hqr2 not converging #366. Following the debugger, it is the
     * method hqr2 that get stuck in a loop
     */
    @Test
    public void testGitHUbIssue366() throws IOException {

        String path = "src/test/resources/org/ojalgo/matrix/decomposition/GitHubIssue366_Mat.txt";
        List<String> lines = Files.lines(Paths.get(path)).collect(Collectors.toList());

        Primitive64Store problematic = Primitive64Store.FACTORY.make(lines.size(), lines.size());

        for (int r = 0; r < lines.size(); r++) {
            String[] line_vec = lines.get(r).split(" ");
            for (int c = 0; c < line_vec.length; c++) {
                double v = Double.parseDouble(line_vec[c]);
                problematic.set(r, c, v);
            }
        }

        CaseEigenvalue.doVerifyGeneral(problematic);
    }

    /**
     * A matrix that was once reported problematic with JAMA (JAMA v1.0.3 contained a fix)
     */
    @Test
    public void testJamaProblem() throws IOException {

        Primitive64Store problematic = Primitive64Store.FACTORY
                .rows(new double[][] { { 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 1 }, { 0, 0, 0, 1, 0 }, { 1, 1, 0, 0, 1 }, { 1, 0, 1, 0, 1 } });

        CaseEigenvalue.doVerifyGeneral(problematic);
    }

    @Test
    public void testP20050125Case() {

        PhysicalStore<Double> tmpOriginalMatrix = Primitive64Store.FACTORY.copy(P20050125Case.getProblematic());

        TestUtils.assertTrue(tmpOriginalMatrix.isHermitian());

        Eigenvalue<Double>[] tmpDecomps = MatrixDecompositionTests.getPrimitiveEigenvalueSymmetric();
        for (Eigenvalue<Double> tmpDecomp : tmpDecomps) {
            tmpDecomp.decompose(tmpOriginalMatrix);
        }

        if (DEBUG) {

            BasicLogger.debug("Eigenvalues");
            for (Eigenvalue<Double> tmpDecomp : tmpDecomps) {
                BasicLogger.debug(tmpDecomp.getClass().getName() + ": " + tmpDecomp.getEigenvalues().toString());
            }

            BasicLogger.debug("D");
            for (Eigenvalue<Double> tmpDecomp : tmpDecomps) {
                BasicLogger.debug(tmpDecomp.getClass().getName() + ": " + Primitive64Store.FACTORY.copy(tmpDecomp.getD()));
            }

            BasicLogger.debug("V");
            for (Eigenvalue<Double> tmpDecomp : tmpDecomps) {
                BasicLogger.debug(tmpDecomp.getClass().getName() + ": " + Primitive64Store.FACTORY.copy(tmpDecomp.getV()));
            }
        }

        for (Eigenvalue<Double> tmpDecomp : tmpDecomps) {
            TestUtils.assertEquals(tmpOriginalMatrix, tmpDecomp, new NumberContext(7, 6));
        }
    }

    @Test
    public void testP20061119Case() {

        PhysicalStore<Double> tmpOriginalMatrix = Primitive64Store.FACTORY.copy(P20061119Case.getProblematic());

        ComplexNumber tmp00 = ComplexNumber.valueOf(26.14421883828456);
        ComplexNumber tmp11 = ComplexNumber.of(2.727890580857718, 3.6223578444417908);
        ComplexNumber tmp22 = tmp11.conjugate();
        ComplexNumber tmp33 = ComplexNumber.ZERO;
        ComplexNumber tmp44 = tmp33;

        Array1D<ComplexNumber> tmpExpectedDiagonal = Array1D.COMPLEX.copy(new ComplexNumber[] { tmp00, tmp11, tmp22, tmp33, tmp44 });
        NumberContext accuracyContext = new NumberContext(7, 6);

        MatrixStore<Double> tmpRecreatedMatrix;

        Eigenvalue<Double> tmpDecomposition = Eigenvalue.PRIMITIVE.make(tmpOriginalMatrix);
        tmpDecomposition.decompose(tmpOriginalMatrix);

        Array1D<ComplexNumber> tmpEigenvalues = tmpDecomposition.getEigenvalues();
        MatrixStore<Double> tmpD = tmpDecomposition.getD();
        MatrixStore<Double> tmpV = tmpDecomposition.getV();

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
        Array1D<ComplexNumber> tmpEigenvaluesOnly = tmpDecomposition.getEigenvalues();
        TestUtils.assertEquals(tmpExpectedDiagonal, tmpEigenvaluesOnly, accuracyContext);
    }

    @Test
    public void testPaulsMathNote() {

        double[][] tmpData = new double[][] { { 3, -9 }, { 4, -3 } };
        Primitive64Store tmpA = Primitive64Store.FACTORY.rows(tmpData);
        int tmpLength = tmpData.length;

        Array1D<ComplexNumber> tmpExpVals = Array1D.COMPLEX.make(2);
        tmpExpVals.set(0, ComplexNumber.of(0.0, THREE * PrimitiveMath.SQRT.invoke(THREE)));
        tmpExpVals.set(1, tmpExpVals.get(0).conjugate());

        Array2D<ComplexNumber> tmpExpVecs = Array2D.COMPLEX.make(2, 2);
        tmpExpVecs.set(0, 0, ComplexNumber.of(THREE, ZERO));
        tmpExpVecs.set(1, 0, ComplexNumber.of(ONE, -PrimitiveMath.SQRT.invoke(THREE)));
        tmpExpVecs.set(0, 1, ComplexNumber.of(THREE, ZERO));
        tmpExpVecs.set(1, 1, ComplexNumber.of(ONE, PrimitiveMath.SQRT.invoke(THREE)));

        Eigenvalue<Double> tmpEvD = Eigenvalue.PRIMITIVE.make(tmpA, false);
        tmpEvD.decompose(tmpA);

        MatrixStore<Double> tmpD = tmpEvD.getD();
        MatrixStore<Double> tmpV = tmpEvD.getV();

        Array1D<ComplexNumber> tmpValues = tmpEvD.getEigenvalues();
        MatrixStore<ComplexNumber> tmpVectors = tmpEvD.getEigenvectors();

        TestUtils.assertEquals(tmpExpVals, tmpValues);
        for (int j = 0; j < tmpLength; j++) {
            Array1D<ComplexNumber> tmpSliceColumn = tmpExpVecs.sliceColumn(0, j);
            Access1D<ComplexNumber> tmpActual = tmpVectors.sliceColumn(0, j);

            ComplexNumber tmpFactor = tmpActual.get(0).divide(tmpSliceColumn.get(0));

            TestUtils.assertEquals(tmpSliceColumn.get(1).multiply(tmpFactor), tmpActual.get(1));
        }

        GenericStore<ComplexNumber> tmpCmplA = GenericStore.COMPLEX.copy(tmpA);
        GenericStore<ComplexNumber> tmpCmplD = GenericStore.COMPLEX.copy(tmpD);
        GenericStore<ComplexNumber> tmpCmplV = GenericStore.COMPLEX.copy(tmpV);

        MatrixStore<ComplexNumber> tmpExp1 = tmpCmplA.multiply(tmpCmplV);
        MatrixStore<ComplexNumber> tmpAct1 = tmpCmplV.multiply(tmpCmplD);
        TestUtils.assertEquals(tmpExp1, tmpAct1);

        GenericStore<ComplexNumber> tmpComplexD = GenericStore.COMPLEX.make(tmpLength, tmpLength);
        for (int j = 0; j < tmpLength; j++) {
            tmpComplexD.set(j, j, tmpValues.get(j));
        }

        MatrixStore<ComplexNumber> tmpExp2 = tmpCmplA.multiply(tmpVectors);
        MatrixStore<ComplexNumber> tmpAct2 = tmpVectors.multiply(tmpComplexD);
        TestUtils.assertEquals(tmpExp2, tmpAct2);

        tmpEvD.computeValuesOnly(tmpA);
        Array1D<ComplexNumber> tmpEigenvaluesOnly = tmpEvD.getEigenvalues();
        TestUtils.assertEquals(tmpValues, tmpEigenvaluesOnly);

    }

    @Test
    public void testPrimitiveAsComplex() {

        double[][] tmpData = new double[][] { { 1, 0, 3 }, { 0, 4, 1 }, { -5, 1, 0 } };
        Primitive64Store tmpA = Primitive64Store.FACTORY.rows(tmpData);

        int tmpLength = tmpData.length;

        Eigenvalue<Double> tmpEvD = Eigenvalue.PRIMITIVE.make(tmpA, false);

        tmpEvD.decompose(tmpA);

        MatrixStore<Double> tmpD = tmpEvD.getD();
        MatrixStore<Double> tmpV = tmpEvD.getV();

        Array1D<ComplexNumber> tmpValues = tmpEvD.getEigenvalues();
        MatrixStore<ComplexNumber> tmpVectors = tmpEvD.getEigenvectors();

        GenericStore<ComplexNumber> tmpCmplA = GenericStore.COMPLEX.copy(tmpA);
        GenericStore<ComplexNumber> tmpCmplD = GenericStore.COMPLEX.copy(tmpD);
        GenericStore<ComplexNumber> tmpCmplV = GenericStore.COMPLEX.copy(tmpV);

        MatrixStore<ComplexNumber> tmpExp1 = tmpCmplA.multiply(tmpCmplV);
        MatrixStore<ComplexNumber> tmpAct1 = tmpCmplV.multiply(tmpCmplD);
        TestUtils.assertEquals(tmpExp1, tmpAct1);

        GenericStore<ComplexNumber> tmpAltD = GenericStore.COMPLEX.make(tmpLength, tmpLength);
        MatrixStore<ComplexNumber> tmpAltV = tmpVectors;

        for (int j = 0; j < tmpLength; j++) {
            tmpAltD.set(j, j, tmpValues.get(j));
        }

        MatrixStore<ComplexNumber> tmpExp2 = tmpCmplA.multiply(tmpAltV);
        MatrixStore<ComplexNumber> tmpAct2 = tmpAltV.multiply(tmpAltD);
        TestUtils.assertEquals(tmpExp2, tmpAct2);

        tmpEvD.computeValuesOnly(tmpA);
        Array1D<ComplexNumber> tmpEigenvaluesOnly = tmpEvD.getEigenvalues();
        TestUtils.assertEquals(tmpValues, tmpEigenvaluesOnly);
    }

    /**
     * A matrix that has been problematic for another library...
     */
    @Test
    public void testProblemFoundInTheWild() {

        Primitive64Store matrix = Primitive64Store.FACTORY.rows(new double[][] { { 1, 0, 0 }, { 0.01, 0, -1 }, { 0.01, 1, 0 } });

        CaseEigenvalue.doVerifyGeneral(matrix);
    }

    @Test
    public void testRandomGeneralisedA_B() throws RecoverableCondition {

        NumberContext accuracy = NumberContext.getGeneral(MathContext.DECIMAL32);

        for (int dim = 2; dim < 10; dim++) {

            Primitive64Store mtrxA = Primitive64Store.FACTORY.makeSPD(dim);
            Primitive64Store mtrxB = Primitive64Store.FACTORY.makeSPD(dim);

            Cholesky<Double> cholesky = Cholesky.PRIMITIVE.make(mtrxB);
            cholesky.decompose(mtrxB);

            MatrixStore<Double> compL = cholesky.getL();
            MatrixStore<Double> compU = cholesky.getR();

            MatrixStore<Double> mtrxC = SolverTask.PRIMITIVE.solve(compL, SolverTask.PRIMITIVE.solve(compL, mtrxA).transpose());

            Eigenvalue<Double> eigenvalue = Eigenvalue.PRIMITIVE.make(mtrxC, true);
            eigenvalue.decompose(mtrxC);
            TestUtils.assertEquals(mtrxC, eigenvalue, accuracy);

            double[] values = new double[dim];
            eigenvalue.getEigenvalues(values, Optional.empty());

            MatrixStore<Double> vectorsY = eigenvalue.getV();

            MatrixStore<Double> leftY = mtrxC.multiply(vectorsY);
            MatrixStore<Double> scales = leftY.onMatching(DIVIDE, vectorsY).collect(Primitive64Store.FACTORY);
            MatrixStore<Double> averages = scales.reduceColumns(Aggregator.AVERAGE).collect(Primitive64Store.FACTORY);
            TestUtils.assertEquals(values, averages, accuracy);

            MatrixStore<Double> vectorsZ = SolverTask.PRIMITIVE.solve(compU, vectorsY);

            MatrixStore<Double> leftZ = mtrxA.multiply(vectorsZ);
            MatrixStore<Double> rightZ = mtrxB.multiply(vectorsZ);
            TestUtils.assertEquals(scales, leftZ.onMatching(DIVIDE, rightZ).collect(Primitive64Store.FACTORY), accuracy);

            Eigenvalue.Generalised<Double> generalised = Eigenvalue.PRIMITIVE.makeGeneralised(mtrxA);
            generalised.decompose(mtrxA, mtrxB);

            TestUtils.assertEquals(mtrxC, generalised.reconstruct(), accuracy);
            TestUtils.assertEquals(vectorsZ, generalised.getV(), accuracy);
            TestUtils.assertEquals(eigenvalue.getD(), generalised.getD(), accuracy);
        }
    }

    @Test
    public void testRandomGeneralisedAB() throws RecoverableCondition {

        NumberContext accuracy = NumberContext.getGeneral(MathContext.DECIMAL32);

        for (int dim = 2; dim < 10; dim++) {

            Primitive64Store mtrxA = Primitive64Store.FACTORY.makeSPD(dim);
            Primitive64Store mtrxB = Primitive64Store.FACTORY.makeSPD(dim);

            Cholesky<Double> cholesky = Cholesky.PRIMITIVE.make(mtrxB);
            cholesky.decompose(mtrxB);

            MatrixStore<Double> compL = cholesky.getL();
            MatrixStore<Double> compU = cholesky.getR();

            MatrixStore<Double> mtrxC = compL.transpose().multiply(mtrxA).multiply(compL);

            Eigenvalue<Double> eigenvalue = Eigenvalue.PRIMITIVE.make(mtrxC, true);
            eigenvalue.decompose(mtrxC);
            TestUtils.assertEquals(mtrxC, eigenvalue, accuracy);

            double[] values = new double[dim];
            eigenvalue.getEigenvalues(values, Optional.empty());

            MatrixStore<Double> vectorsY = eigenvalue.getV();

            MatrixStore<Double> leftY = mtrxC.multiply(vectorsY);
            MatrixStore<Double> scales = leftY.onMatching(DIVIDE, vectorsY).collect(Primitive64Store.FACTORY);
            MatrixStore<Double> averages = scales.reduceColumns(Aggregator.AVERAGE).collect(Primitive64Store.FACTORY);
            TestUtils.assertEquals(values, averages, accuracy);

            MatrixStore<Double> vectorsZ = SolverTask.PRIMITIVE.solve(compU, vectorsY);

            MatrixStore<Double> leftZ = mtrxA.multiply(mtrxB).multiply(vectorsZ);
            MatrixStore<Double> rightZ = vectorsZ;
            TestUtils.assertEquals(scales, leftZ.onMatching(DIVIDE, rightZ).collect(Primitive64Store.FACTORY), accuracy);

            Eigenvalue.Generalised<Double> generalised = Eigenvalue.PRIMITIVE.makeGeneralised(mtrxA, Generalisation.AB);
            generalised.decompose(mtrxA, mtrxB);

            if (DEBUG) {

            }

            TestUtils.assertEquals(mtrxC, generalised.reconstruct(), accuracy);
            TestUtils.assertEquals(vectorsZ, generalised.getV(), accuracy);
            TestUtils.assertEquals(eigenvalue.getD(), generalised.getD(), accuracy);
        }
    }

    @Test
    public void testRandomGeneralisedBA() throws RecoverableCondition {

        NumberContext accuracy = NumberContext.getGeneral(MathContext.DECIMAL32);

        for (int dim = 2; dim < 10; dim++) {

            Primitive64Store mtrxA = Primitive64Store.FACTORY.makeSPD(dim);
            Primitive64Store mtrxB = Primitive64Store.FACTORY.makeSPD(dim);

            Cholesky<Double> cholesky = Cholesky.PRIMITIVE.make(mtrxB);
            cholesky.decompose(mtrxB);

            MatrixStore<Double> compL = cholesky.getL();
            cholesky.getR();

            MatrixStore<Double> mtrxC = compL.transpose().multiply(mtrxA).multiply(compL);

            Eigenvalue<Double> eigenvalue = Eigenvalue.PRIMITIVE.make(mtrxC, true);
            eigenvalue.decompose(mtrxC);
            TestUtils.assertEquals(mtrxC, eigenvalue, accuracy);

            double[] values = new double[dim];
            eigenvalue.getEigenvalues(values, Optional.empty());

            MatrixStore<Double> vectorsY = eigenvalue.getV();

            MatrixStore<Double> leftY = mtrxC.multiply(vectorsY);
            MatrixStore<Double> scales = leftY.onMatching(DIVIDE, vectorsY).collect(Primitive64Store.FACTORY);
            MatrixStore<Double> averages = scales.reduceColumns(Aggregator.AVERAGE).collect(Primitive64Store.FACTORY);
            TestUtils.assertEquals(values, averages, accuracy);

            MatrixStore<Double> vectorsZ = compL.multiply(vectorsY);

            MatrixStore<Double> leftZ = mtrxB.multiply(mtrxA).multiply(vectorsZ);
            MatrixStore<Double> rightZ = vectorsZ;
            TestUtils.assertEquals(scales, leftZ.onMatching(DIVIDE, rightZ).collect(Primitive64Store.FACTORY), accuracy);

            Eigenvalue.Generalised<Double> generalised = Eigenvalue.PRIMITIVE.makeGeneralised(mtrxA, Generalisation.BA);
            generalised.decompose(mtrxA, mtrxB);

            TestUtils.assertEquals(mtrxC, generalised.reconstruct(), accuracy);
            TestUtils.assertEquals(vectorsZ, generalised.getV(), accuracy);
            TestUtils.assertEquals(eigenvalue.getD(), generalised.getD(), accuracy);
        }
    }

    @Test
    public void testRandomGeneralisedUnprepared() throws RecoverableCondition {

        NumberContext accuracy = NumberContext.getGeneral(MathContext.DECIMAL32);

        for (int dim = 2; dim < 10; dim++) {

            Primitive64Store mtrxA = Primitive64Store.FACTORY.makeSPD(dim);

            Eigenvalue<Double> eigenvalue = Eigenvalue.PRIMITIVE.make(mtrxA, true);
            eigenvalue.decompose(mtrxA);
            TestUtils.assertEquals(mtrxA, eigenvalue, accuracy);

            Eigenvalue.Generalised<Double> generalised = Eigenvalue.PRIMITIVE.makeGeneralised(mtrxA);
            generalised.decompose(mtrxA);
            TestUtils.assertEquals(mtrxA, generalised, accuracy);

            TestUtils.assertEquals(eigenvalue.reconstruct(), generalised.reconstruct(), accuracy);
            TestUtils.assertEquals(eigenvalue.getV(), generalised.getV(), accuracy);
            TestUtils.assertEquals(eigenvalue.getD(), generalised.getD(), accuracy);
        }
    }

    @Test
    public void testRandomSymmetricValuesOnly() {

        NumberContext evaluationContext = NumberContext.getGeneral(MathContext.DECIMAL32);

        for (int dim = 1; dim < 10; dim++) {

            final int dim1 = dim;
            Primitive64Store matrix = Primitive64Store.FACTORY.makeSPD(dim1);

            for (Eigenvalue<Double> decomp : MatrixDecompositionTests.getPrimitiveEigenvalueSymmetric()) {

                decomp.decompose(matrix);
                TestUtils.assertEquals(matrix, decomp, evaluationContext);

                Array1D<ComplexNumber> expected = decomp.getEigenvalues();
                decomp.computeValuesOnly(matrix);
                Array1D<ComplexNumber> actual = decomp.getEigenvalues();
                TestUtils.assertEquals(expected, actual, evaluationContext);
            }
        }
    }

}
