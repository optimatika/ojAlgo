/*
 * Copyright 1997-2019 Optimatika
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
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.constant.ComplexMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.P20030422Case;
import org.ojalgo.matrix.P20030512Case;
import org.ojalgo.matrix.P20030528Case;
import org.ojalgo.matrix.P20050125Case;
import org.ojalgo.matrix.P20050827Case;
import org.ojalgo.matrix.P20061119Case;
import org.ojalgo.matrix.P20071019Case;
import org.ojalgo.matrix.RationalMatrix;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Normal;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.context.NumberContext;

public class CaseSingularValue extends MatrixDecompositionTests {

    private static final SingularValue<RationalNumber> IMPL_BIG = SingularValue.RATIONAL.make();
    private static final SingularValue<ComplexNumber> IMPL_COMPLEX = SingularValue.COMPLEX.make();
    private static final SingularValue<Double> IMPL_PRIMITIVE = new SingularValueDecomposition.Primitive();
    private static final SingularValue<Double> IMPL_RAW = new RawSingularValue();

    private static final RationalMatrix MTRX_FAT = RationalMatrix.FACTORY.copy(TestUtils.makeRandomComplexStore(7, 9));
    private static final RationalMatrix MTRX_SQUARE = RationalMatrix.FACTORY.copy(TestUtils.makeRandomComplexStore(8, 8));
    private static final RationalMatrix MTRX_TALL = RationalMatrix.FACTORY.copy(TestUtils.makeRandomComplexStore(9, 7));

    static final NumberContext CNTXT_CPLX_DECOMP = new NumberContext(3, 2);
    static final NumberContext CNTXT_CPLX_VALUES = new NumberContext(7, 7);
    static final NumberContext CNTXT_REAL_DECOMP = new NumberContext(3, 2);
    static final NumberContext CNTXT_REAL_VALUES = new NumberContext(7, 10);

    @Override
    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testBasicMatrixP20030422Case() {
        this.doTestTypes(P20030422Case.getProblematic());
    }

    @Test
    public void testBasicMatrixP20030512Case() {
        this.doTestTypes(P20030512Case.getProblematic());
    }

    @Test
    public void testBasicMatrixP20030528Case() {
        this.doTestTypes(P20030528Case.getProblematic());
    }

    @Test
    public void testBasicMatrixP20050125Case() {
        this.doTestTypes(P20050125Case.getProblematic());
    }

    @Test
    public void testBasicMatrixP20050827Case() {
        this.doTestTypes(RationalMatrix.FACTORY.copy(P20050827Case.getProblematic()));
    }

    @Test
    public void testBasicMatrixP20061119Case() {
        this.doTestTypes(P20061119Case.getProblematic());
    }

    @Test
    public void testBasicMatrixP20071019FatCase() {
        this.doTestTypes(P20071019Case.getFatProblematic());
    }

    @Test
    public void testBasicMatrixP20071019TallCase() {
        this.doTestTypes(P20071019Case.getTallProblematic());
    }

    /**
     * http://en.wikipedia.org/wiki/Singular_value_decomposition
     */
    @Test
    public void testComplexNumberVersionOfWikipediaCase() {

        PhysicalStore<Double> tmpBaseMtrx = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 1.0, 0.0, 0.0, 0.0, 2.0 }, { 0.0, 0.0, 3.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 4.0, 0.0, 0.0, 0.0 } });

        Array1D<Double> tmpExpectedSingularValues = Array1D.PRIMITIVE64.copy(new double[] { 4.0, 3.0, PrimitiveMath.SQRT.invoke(5.0), 0.0 });

        ComplexNumber[] tmpScales = new ComplexNumber[] { ComplexNumber.makePolar(1.0, 0.0), ComplexNumber.makePolar(1.0, Math.PI / 2.0),
                ComplexNumber.makePolar(1.0, -Math.PI / 2.0), ComplexNumber.makePolar(1.0, Math.PI / 4.0),
                ComplexNumber.makePolar(1.0, (4.0 * Math.PI) / 3.0) };

        Bidiagonal<ComplexNumber> tmpBidiagonal = Bidiagonal.COMPLEX.make();
        SingularValue<ComplexNumber> tmpSVD = SingularValue.COMPLEX.make();

        for (ComplexNumber tmpScale : tmpScales) {

            PhysicalStore<ComplexNumber> tmpOriginalMtrx = GenericDenseStore.COMPLEX.transpose(tmpBaseMtrx);
            tmpOriginalMtrx.modifyAll(ComplexMath.MULTIPLY.first(tmpScale));

            tmpBidiagonal.decompose(tmpOriginalMtrx);
            MatrixStore<ComplexNumber> tmpReconstructed = tmpBidiagonal.reconstruct();
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug("Scale = {}", tmpScale);
                BasicLogger.debug("A", tmpOriginalMtrx);
                BasicLogger.debug("Q1", tmpBidiagonal.getLQ());
                BasicLogger.debug("D", tmpBidiagonal.getD());
                BasicLogger.debug("Q2", tmpBidiagonal.getRQ());
                BasicLogger.debug("Reconstructed", tmpReconstructed);
            }
            TestUtils.assertEquals(tmpOriginalMtrx, tmpReconstructed, new NumberContext(7, 6));
        }

        for (ComplexNumber tmpScale : tmpScales) {

            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug("Scale = {}", tmpScale);
            }

            PhysicalStore<ComplexNumber> tmpOriginalMtrx = GenericDenseStore.COMPLEX.copy(tmpBaseMtrx);
            tmpOriginalMtrx.modifyAll(ComplexMath.MULTIPLY.first(tmpScale));

            tmpBidiagonal.decompose(tmpOriginalMtrx.conjugate());
            tmpSVD.decompose(tmpOriginalMtrx);

            Array1D<Double> tmpActualSingularValues = tmpSVD.getSingularValues();
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug("Expected = {}", tmpExpectedSingularValues);
                BasicLogger.debug("Actual = {}", tmpActualSingularValues);
            }
            TestUtils.assertEquals(tmpExpectedSingularValues, tmpActualSingularValues, new NumberContext(7, 6));

            MatrixStore<ComplexNumber> tmpReconstructed = tmpSVD.reconstruct();
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug("Original", tmpOriginalMtrx);
                BasicLogger.debug("Reconstructed", tmpReconstructed);
            }
            TestUtils.assertEquals(tmpOriginalMtrx, tmpReconstructed, new NumberContext(7, 6));
        }

    }

    @Test
    public void testGetCovariance() {

        PrimitiveDenseStore original = PrimitiveDenseStore.FACTORY.makeFilled(9, 3, new Normal());

        for (SingularValue<Double> decomp : MatrixDecompositionTests.getPrimitiveSingularValue()) {

            decomp.decompose(original);

            MatrixStore<Double> covariance = decomp.getCovariance();

            decomp.decompose(original.premultiply(original.transpose()));

            MatrixStore<Double> inverse = decomp.getInverse();

            TestUtils.assertEquals(inverse, covariance);
        }

    }

    /**
     * https://stackoverflow.com/questions/56752647/huge-difference-between-svd-solution-output-of-commons-math3-and-ojalgo-librarie
     */
    @Test
    public void testPseudoinverseSolve() {

        double[][] olsColumns = { { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 },
                { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 },
                { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 },
                { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 },
                { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 },
                { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 },
                { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 },
                { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 },
                { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 },
                { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0 } };
        PrimitiveDenseStore body = PrimitiveDenseStore.FACTORY.rows(olsColumns);

        double[] observationVector = { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
        PrimitiveDenseStore rhs = PrimitiveDenseStore.FACTORY.column(observationVector);

        double expected = 0.0161290322580645;
        MatrixStore<Double> actual;
        MatrixStore<Double> inverse;

        for (SingularValue<Double> decomp : MatrixDecompositionTests.getPrimitiveSingularValue()) {

            decomp.reset();
            decomp.decompose(body);

            actual = decomp.getSolution(rhs);
            TestUtils.assertEquals(2, actual.count());
            TestUtils.assertEquals(expected, actual.doubleValue(0));
            TestUtils.assertEquals(expected, actual.doubleValue(1));

            decomp.reset();
            decomp.decompose(body);

            actual = decomp.getSolution(rhs, decomp.preallocate(body, rhs));
            TestUtils.assertEquals(2, actual.count());
            TestUtils.assertEquals(expected, actual.doubleValue(0));
            TestUtils.assertEquals(expected, actual.doubleValue(1));

            decomp.reset();
            decomp.decompose(body);

            inverse = decomp.getInverse();
            actual = inverse.multiply(rhs);
            TestUtils.assertEquals(2, actual.count());
            TestUtils.assertEquals(expected, actual.doubleValue(0));
            TestUtils.assertEquals(expected, actual.doubleValue(1));

            decomp.reset();
            decomp.decompose(body);

            inverse = decomp.getInverse(decomp.preallocate(body));
            actual = inverse.multiply(rhs);
            TestUtils.assertEquals(2, actual.count());
            TestUtils.assertEquals(expected, actual.doubleValue(0));
            TestUtils.assertEquals(expected, actual.doubleValue(1));
        }
    }

    @Test
    public void testRandomActuallyComplexCase() {

        PhysicalStore<ComplexNumber> tmpOriginal = TestUtils.makeRandomComplexStore(4, 4);

        SingularValue<ComplexNumber> tmpDecomposition = SingularValue.COMPLEX.make();

        tmpDecomposition.decompose(tmpOriginal);

        MatrixStore<ComplexNumber> tmpReconstructed = tmpDecomposition.reconstruct();

        if (!Access2D.equals(tmpOriginal, tmpReconstructed, new NumberContext(7, 6))) {
            BasicLogger.error("Recreation failed for: {}", tmpDecomposition.getClass().getName());
        }
        if (!SingularValue.equals(tmpOriginal, tmpDecomposition, new NumberContext(7, 6))) {
            BasicLogger.error("Decomposition not correct for: {}", tmpDecomposition.getClass().getName());
        }
        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug(tmpDecomposition.toString());
            BasicLogger.debug("Original", tmpOriginal);
            BasicLogger.debug("Q1", tmpDecomposition.getU());
            BasicLogger.debug("D", tmpDecomposition.getD());
            BasicLogger.debug("Q2", tmpDecomposition.getV());
            BasicLogger.debug("Reconstructed", tmpReconstructed);
            PhysicalStore<ComplexNumber> tmpCopy = tmpOriginal.copy();
            // tmpCopy.maxpy(ComplexNumber.NEG, tmpReconstructed);
            tmpReconstructed.axpy(-1, tmpCopy);
            BasicLogger.debug("Diff", tmpCopy);
        }

        TestUtils.assertEquals(tmpOriginal, tmpReconstructed, new NumberContext(7, 6));
        TestUtils.assertEquals(tmpOriginal, tmpDecomposition, new NumberContext(7, 6));

    }

    @Test
    public void testRandomFatCase() {
        this.doTestTypes(MTRX_FAT);
    }

    @Test
    public void testRandomSquareCase() {
        this.doTestTypes(MTRX_SQUARE);
    }

    @Test
    public void testRandomTallCase() {
        this.doTestTypes(MTRX_TALL);
    }

    @Test
    public void testRecreationFat() {

        PhysicalStore<Double> tmpOriginal = PrimitiveDenseStore.FACTORY.copy(MTRX_FAT);

        this.testRecreation(tmpOriginal);
    }

    @Test
    public void testRecreationSquare() {

        PhysicalStore<Double> tmpOriginal = PrimitiveDenseStore.FACTORY.copy(MTRX_SQUARE);

        this.testRecreation(tmpOriginal);
    }

    @Test
    public void testRecreationTall() {

        PhysicalStore<Double> tmpOriginal = PrimitiveDenseStore.FACTORY.copy(MTRX_TALL);

        this.testRecreation(tmpOriginal);
    }

    private void doTestTypes(final RationalMatrix original) {

        PhysicalStore<RationalNumber> tmpBigStore = GenericDenseStore.RATIONAL.copy(original);
        PhysicalStore<ComplexNumber> tmpComplexStore = GenericDenseStore.COMPLEX.copy(original);
        PhysicalStore<Double> tmpPrimitiveStore = PrimitiveDenseStore.FACTORY.copy(original);

        IMPL_BIG.decompose(GenericDenseStore.RATIONAL.copy(original));
        IMPL_COMPLEX.decompose(GenericDenseStore.COMPLEX.copy(original));
        IMPL_RAW.decompose(PrimitiveDenseStore.FACTORY.copy(original));
        IMPL_PRIMITIVE.decompose(PrimitiveDenseStore.FACTORY.copy(original));

        Array1D<Double> tmpBigSingularValues = IMPL_BIG.getSingularValues();
        Array1D<Double> tmpComplexSingularValues = IMPL_COMPLEX.getSingularValues();
        Array1D<Double> tmpJamaSingularValues = IMPL_RAW.getSingularValues();
        Array1D<Double> tmpDirectSingularValues = IMPL_PRIMITIVE.getSingularValues();

        UnaryFunction<Double> tmpPrimitiveRoundFunction = CNTXT_REAL_VALUES.getFunction(PrimitiveFunction.getSet());
        //        tmpBigSingularValues.modifyAll(tmpPrimitiveRoundFunction);
        //        tmpComplexSingularValues.modifyAll(tmpPrimitiveRoundFunction);
        //        tmpJamaSingularValues.modifyAll(tmpPrimitiveRoundFunction);
        //        tmpDirectSingularValues.modifyAll(tmpPrimitiveRoundFunction);

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   S: {}.", tmpBigSingularValues);
            BasicLogger.debug("Cmplx S: {}.", tmpComplexSingularValues);
            BasicLogger.debug("Jama  S: {}.", tmpJamaSingularValues);
            BasicLogger.debug("Direc S: {}.", tmpDirectSingularValues);
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   rank: {}.", IMPL_BIG.getRank());
            BasicLogger.debug("Cmplx rank: {}.", IMPL_COMPLEX.getRank());
            BasicLogger.debug("Jama  rank: {}.", IMPL_RAW.getRank());
            BasicLogger.debug("Direc rank: {}.", IMPL_PRIMITIVE.getRank());
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   D", IMPL_BIG.getD());
            BasicLogger.debug("Cmplx D", IMPL_COMPLEX.getD());
            BasicLogger.debug("Jama  D", IMPL_RAW.getD());
            BasicLogger.debug("Direc D", IMPL_PRIMITIVE.getD());
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   Q1", IMPL_BIG.getU());
            BasicLogger.debug("Cmplx Q1", IMPL_COMPLEX.getU());
            BasicLogger.debug("Jama  Q1", IMPL_RAW.getU());
            BasicLogger.debug("Direc Q1", IMPL_PRIMITIVE.getU());
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   Q1 unitary", IMPL_BIG.getU().conjugate().multiply(IMPL_BIG.getU()));
            BasicLogger.debug("Cmplx Q1 unitary", IMPL_COMPLEX.getU().conjugate().multiply(IMPL_COMPLEX.getU()));
            BasicLogger.debug("Jama  Q1 unitary", IMPL_RAW.getU().conjugate().multiply(IMPL_RAW.getU()));
            BasicLogger.debug("Direc Q1 unitary", IMPL_PRIMITIVE.getU().conjugate().multiply(IMPL_PRIMITIVE.getU()));
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   Q2", IMPL_BIG.getV());
            BasicLogger.debug("Cmplx Q2", IMPL_COMPLEX.getV());
            BasicLogger.debug("Jama  Q2", IMPL_RAW.getV());
            BasicLogger.debug("Direc Q2", IMPL_PRIMITIVE.getV());
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   Q2 unitary", IMPL_BIG.getV().multiply(IMPL_BIG.getV().conjugate()));
            BasicLogger.debug("Cmplx Q2 unitary", IMPL_COMPLEX.getV().multiply(IMPL_COMPLEX.getV().conjugate()));
            BasicLogger.debug("Jama  Q2 unitary", IMPL_RAW.getV().multiply(IMPL_RAW.getV().conjugate()));
            BasicLogger.debug("Direc Q2 unitary", IMPL_PRIMITIVE.getV().multiply(IMPL_PRIMITIVE.getV().conjugate()));
        }

        tmpPrimitiveRoundFunction = CNTXT_CPLX_VALUES.getFunction(PrimitiveFunction.getSet());
        tmpBigSingularValues.modifyAll(tmpPrimitiveRoundFunction);
        tmpComplexSingularValues.modifyAll(tmpPrimitiveRoundFunction);
        tmpJamaSingularValues.modifyAll(tmpPrimitiveRoundFunction);
        tmpDirectSingularValues.modifyAll(tmpPrimitiveRoundFunction);

        TestUtils.assertEquals(tmpBigSingularValues, tmpComplexSingularValues);
        TestUtils.assertEquals(tmpComplexSingularValues, tmpJamaSingularValues);
        TestUtils.assertEquals(tmpJamaSingularValues, tmpDirectSingularValues);
        TestUtils.assertEquals(tmpDirectSingularValues, tmpBigSingularValues);

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   Recreated", IMPL_BIG.reconstruct());
            BasicLogger.debug("Cmplx Recreated", IMPL_COMPLEX.reconstruct());
            BasicLogger.debug("Jama  Recreated", IMPL_RAW.reconstruct());
            BasicLogger.debug("Direc Recreated", IMPL_PRIMITIVE.reconstruct());
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   inverse", IMPL_BIG.getInverse());
            BasicLogger.debug("Cmplx inverse", IMPL_COMPLEX.getInverse());
            BasicLogger.debug("Jama  inverse", IMPL_RAW.getInverse());
            BasicLogger.debug("Direc inverse", IMPL_PRIMITIVE.getInverse());
        }

        TestUtils.assertEquals(tmpBigStore, IMPL_BIG, CNTXT_REAL_DECOMP);
        TestUtils.assertEquals(tmpComplexStore, IMPL_COMPLEX, CNTXT_CPLX_DECOMP); // Fails too often...
        TestUtils.assertEquals(tmpPrimitiveStore, IMPL_RAW, CNTXT_REAL_DECOMP);
        TestUtils.assertEquals(tmpPrimitiveStore, IMPL_PRIMITIVE, CNTXT_REAL_DECOMP);
    }

    private void testRecreation(final PhysicalStore<Double> aMtrx) {

        SingularValue<Double>[] tmpImpls = MatrixDecompositionTests.getPrimitiveSingularValue();

        for (SingularValue<Double> tmpImpl : tmpImpls) {

            tmpImpl.decompose(aMtrx);
            MatrixStore<Double> tmpReconstructed = SingularValue.reconstruct(tmpImpl);
            if (!Access2D.equals(aMtrx, tmpReconstructed, new NumberContext(7, 6))) {
                BasicLogger.error("Recreation failed for: {}", tmpImpl.getClass().getName());
            }
            if (!SingularValue.equals(aMtrx, tmpImpl, new NumberContext(7, 6))) {
                BasicLogger.error("Decomposition not correct for: {}", tmpImpl.getClass().getName());
            }
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug(tmpImpl.toString());
                BasicLogger.debug("Original", aMtrx);
                BasicLogger.debug("Q1", tmpImpl.getU());
                BasicLogger.debug("D", tmpImpl.getD());
                BasicLogger.debug("Q2", tmpImpl.getV());
                BasicLogger.debug("Reconstructed", tmpReconstructed);
                PhysicalStore<Double> tmpCopy = aMtrx.copy();
                // tmpCopy.maxpy(-1.0, tmpReconstructed);
                tmpReconstructed.axpy(-1, tmpCopy);
                BasicLogger.debug("Diff", tmpCopy);
            }

        }
    }

}
