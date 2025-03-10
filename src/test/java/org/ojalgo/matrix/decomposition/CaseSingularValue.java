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
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.constant.ComplexMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.MatrixQ128;
import org.ojalgo.matrix.P20030422Case;
import org.ojalgo.matrix.P20030512Case;
import org.ojalgo.matrix.P20030528Case;
import org.ojalgo.matrix.P20050125Case;
import org.ojalgo.matrix.P20050827Case;
import org.ojalgo.matrix.P20061119Case;
import org.ojalgo.matrix.P20071019Case;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Normal;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.context.NumberContext;

public class CaseSingularValue extends MatrixDecompositionTests {

    private static final SingularValue<RationalNumber> IMPL_BIG = SingularValue.Q128.make();
    private static final SingularValue<ComplexNumber> IMPL_COMPLEX = SingularValue.C128.make();
    private static final SingularValue<Double> IMPL_PRIMITIVE = new DenseSingularValue.R064();
    private static final SingularValue<Double> IMPL_RAW = new RawSingularValue();

    private static final MatrixQ128 MTRX_FAT = MatrixQ128.FACTORY.copy(TestUtils.makeRandomComplexStore(7, 9));
    private static final MatrixQ128 MTRX_SQUARE = MatrixQ128.FACTORY.copy(TestUtils.makeRandomComplexStore(8, 8));
    private static final MatrixQ128 MTRX_TALL = MatrixQ128.FACTORY.copy(TestUtils.makeRandomComplexStore(9, 7));

    static final NumberContext CNTXT_CPLX_DECOMP = NumberContext.of(3, 2);
    static final NumberContext CNTXT_CPLX_VALUES = NumberContext.of(7, 7);
    static final NumberContext CNTXT_REAL_DECOMP = NumberContext.of(3, 2);
    static final NumberContext CNTXT_REAL_VALUES = NumberContext.of(7, 10);

    private static void doTestTypes(final Access2D<?> original) {

        PhysicalStore<RationalNumber> tmpBigStore = GenericStore.Q128.copy(original);
        PhysicalStore<ComplexNumber> tmpComplexStore = GenericStore.C128.copy(original);
        PhysicalStore<Double> tmpPrimitiveStore = R064Store.FACTORY.copy(original);

        IMPL_BIG.decompose(GenericStore.Q128.copy(original));
        IMPL_COMPLEX.decompose(GenericStore.C128.copy(original));
        IMPL_RAW.decompose(R064Store.FACTORY.copy(original));
        IMPL_PRIMITIVE.decompose(R064Store.FACTORY.copy(original));

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
            BasicLogger.debugMatrix("Big   D", IMPL_BIG.getD());
            BasicLogger.debugMatrix("Cmplx D", IMPL_COMPLEX.getD());
            BasicLogger.debugMatrix("Jama  D", IMPL_RAW.getD());
            BasicLogger.debugMatrix("Direc D", IMPL_PRIMITIVE.getD());
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debugMatrix("Big   Q1", IMPL_BIG.getU());
            BasicLogger.debugMatrix("Cmplx Q1", IMPL_COMPLEX.getU());
            BasicLogger.debugMatrix("Jama  Q1", IMPL_RAW.getU());
            BasicLogger.debugMatrix("Direc Q1", IMPL_PRIMITIVE.getU());
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debugMatrix("Big   Q1 unitary", IMPL_BIG.getU().conjugate().multiply(IMPL_BIG.getU()));
            BasicLogger.debugMatrix("Cmplx Q1 unitary", IMPL_COMPLEX.getU().conjugate().multiply(IMPL_COMPLEX.getU()));
            BasicLogger.debugMatrix("Jama  Q1 unitary", IMPL_RAW.getU().conjugate().multiply(IMPL_RAW.getU()));
            BasicLogger.debugMatrix("Direc Q1 unitary", IMPL_PRIMITIVE.getU().conjugate().multiply(IMPL_PRIMITIVE.getU()));
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debugMatrix("Big   Q2", IMPL_BIG.getV());
            BasicLogger.debugMatrix("Cmplx Q2", IMPL_COMPLEX.getV());
            BasicLogger.debugMatrix("Jama  Q2", IMPL_RAW.getV());
            BasicLogger.debugMatrix("Direc Q2", IMPL_PRIMITIVE.getV());
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debugMatrix("Big   Q2 unitary", IMPL_BIG.getV().multiply(IMPL_BIG.getV().conjugate()));
            BasicLogger.debugMatrix("Cmplx Q2 unitary", IMPL_COMPLEX.getV().multiply(IMPL_COMPLEX.getV().conjugate()));
            BasicLogger.debugMatrix("Jama  Q2 unitary", IMPL_RAW.getV().multiply(IMPL_RAW.getV().conjugate()));
            BasicLogger.debugMatrix("Direc Q2 unitary", IMPL_PRIMITIVE.getV().multiply(IMPL_PRIMITIVE.getV().conjugate()));
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
            BasicLogger.debugMatrix("Big   Recreated", IMPL_BIG.reconstruct());
            BasicLogger.debugMatrix("Cmplx Recreated", IMPL_COMPLEX.reconstruct());
            BasicLogger.debugMatrix("Jama  Recreated", IMPL_RAW.reconstruct());
            BasicLogger.debugMatrix("Direc Recreated", IMPL_PRIMITIVE.reconstruct());
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debugMatrix("Big   inverse", IMPL_BIG.getInverse());
            BasicLogger.debugMatrix("Cmplx inverse", IMPL_COMPLEX.getInverse());
            BasicLogger.debugMatrix("Jama  inverse", IMPL_RAW.getInverse());
            BasicLogger.debugMatrix("Direc inverse", IMPL_PRIMITIVE.getInverse());
        }

        TestUtils.assertEquals(tmpBigStore, IMPL_BIG, CNTXT_REAL_DECOMP);
        TestUtils.assertEquals(tmpComplexStore, IMPL_COMPLEX, CNTXT_CPLX_DECOMP); // Fails too often...
        TestUtils.assertEquals(tmpPrimitiveStore, IMPL_RAW, CNTXT_REAL_DECOMP);
        TestUtils.assertEquals(tmpPrimitiveStore, IMPL_PRIMITIVE, CNTXT_REAL_DECOMP);
    }

    @Override
    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testBasicMatrixP20030422Case() {
        CaseSingularValue.doTestTypes(P20030422Case.getProblematic());
    }

    @Test
    public void testBasicMatrixP20030512Case() {
        CaseSingularValue.doTestTypes(P20030512Case.getProblematic());
    }

    @Test
    public void testBasicMatrixP20030528Case() {
        CaseSingularValue.doTestTypes(P20030528Case.getProblematic());
    }

    @Test
    public void testBasicMatrixP20050125Case() {
        CaseSingularValue.doTestTypes(P20050125Case.getProblematic());
    }

    @Test
    public void testBasicMatrixP20050827Case() {
        CaseSingularValue.doTestTypes(MatrixQ128.FACTORY.copy(P20050827Case.getProblematic()));
    }

    @Test
    public void testBasicMatrixP20061119Case() {
        CaseSingularValue.doTestTypes(P20061119Case.getProblematic());
    }

    @Test
    public void testBasicMatrixP20071019FatCase() {
        CaseSingularValue.doTestTypes(P20071019Case.getFatProblematic());
    }

    @Test
    public void testBasicMatrixP20071019TallCase() {
        CaseSingularValue.doTestTypes(P20071019Case.getTallProblematic());
    }

    /**
     * http://en.wikipedia.org/wiki/Singular_value_decomposition
     */
    @Test
    public void testComplexNumberVersionOfWikipediaCase() {

        PhysicalStore<Double> tmpBaseMtrx = RawStore
                .wrap(new double[][] { { 1.0, 0.0, 0.0, 0.0, 2.0 }, { 0.0, 0.0, 3.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 4.0, 0.0, 0.0, 0.0 } });

        double[] data = { 4.0, 3.0, PrimitiveMath.SQRT.invoke(5.0), 0.0 };
        Array1D<Double> tmpExpectedSingularValues = Array1D.R064.copy(data);

        ComplexNumber[] tmpScales = { ComplexNumber.makePolar(1.0, 0.0), ComplexNumber.makePolar(1.0, Math.PI / 2.0),
                ComplexNumber.makePolar(1.0, -Math.PI / 2.0), ComplexNumber.makePolar(1.0, Math.PI / 4.0), ComplexNumber.makePolar(1.0, 4.0 * Math.PI / 3.0) };

        Bidiagonal<ComplexNumber> tmpBidiagonal = Bidiagonal.C128.make();
        SingularValue<ComplexNumber> tmpSVD = SingularValue.C128.make();

        for (ComplexNumber tmpScale : tmpScales) {

            PhysicalStore<ComplexNumber> tmpOriginalMtrx = GenericStore.C128.transpose(tmpBaseMtrx);
            tmpOriginalMtrx.modifyAll(ComplexMath.MULTIPLY.first(tmpScale));

            tmpBidiagonal.decompose(tmpOriginalMtrx);
            MatrixStore<ComplexNumber> tmpReconstructed = tmpBidiagonal.reconstruct();
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug();
                BasicLogger.debugMatrix("Scale = {}", tmpScale);
                BasicLogger.debugMatrix("A", tmpOriginalMtrx);
                BasicLogger.debugMatrix("Q1", tmpBidiagonal.getLQ());
                BasicLogger.debugMatrix("D", tmpBidiagonal.getD());
                BasicLogger.debugMatrix("Q2", tmpBidiagonal.getRQ());
                BasicLogger.debugMatrix("Reconstructed", tmpReconstructed);
            }
            TestUtils.assertEquals(tmpOriginalMtrx, tmpReconstructed, NumberContext.of(7, 6));
        }

        for (ComplexNumber tmpScale : tmpScales) {

            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug();
                BasicLogger.debugMatrix("Scale = {}", tmpScale);
            }

            PhysicalStore<ComplexNumber> tmpOriginalMtrx = GenericStore.C128.copy(tmpBaseMtrx);
            tmpOriginalMtrx.modifyAll(ComplexMath.MULTIPLY.first(tmpScale));

            tmpBidiagonal.decompose(tmpOriginalMtrx.conjugate());
            tmpSVD.decompose(tmpOriginalMtrx);

            Array1D<Double> tmpActualSingularValues = tmpSVD.getSingularValues();
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug("Expected = {}", tmpExpectedSingularValues);
                BasicLogger.debug("Actual = {}", tmpActualSingularValues);
            }
            TestUtils.assertEquals(tmpExpectedSingularValues, tmpActualSingularValues, NumberContext.of(7, 6));

            MatrixStore<ComplexNumber> tmpReconstructed = tmpSVD.reconstruct();
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debugMatrix("Original", tmpOriginalMtrx);
                BasicLogger.debugMatrix("Reconstructed", tmpReconstructed);
            }
            TestUtils.assertEquals(tmpOriginalMtrx, tmpReconstructed, NumberContext.of(7, 6));
        }

    }

    @Test
    public void testGetCovariance() {

        R064Store original = R064Store.FACTORY.makeFilled(9, 3, new Normal());

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
        RawStore body = RawStore.wrap(olsColumns);

        double[] observationVector = { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
        R064Store rhs = R064Store.FACTORY.column(observationVector);

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

        SingularValue<ComplexNumber> tmpDecomposition = SingularValue.C128.make();

        tmpDecomposition.decompose(tmpOriginal);

        MatrixStore<ComplexNumber> tmpReconstructed = tmpDecomposition.reconstruct();

        if (!Access2D.equals(tmpOriginal, tmpReconstructed, NumberContext.of(7, 6))) {
            BasicLogger.error("Recreation failed for: {}", tmpDecomposition.getClass().getName());
        }
        if (!SingularValue.equals(tmpOriginal, tmpDecomposition, NumberContext.of(7, 6))) {
            BasicLogger.error("Decomposition not correct for: {}", tmpDecomposition.getClass().getName());
        }
        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug(tmpDecomposition.toString());
            BasicLogger.debugMatrix("Original", tmpOriginal);
            BasicLogger.debugMatrix("Q1", tmpDecomposition.getU());
            BasicLogger.debugMatrix("D", tmpDecomposition.getD());
            BasicLogger.debugMatrix("Q2", tmpDecomposition.getV());
            BasicLogger.debugMatrix("Reconstructed", tmpReconstructed);
            PhysicalStore<ComplexNumber> tmpCopy = tmpOriginal.copy();
            // tmpCopy.maxpy(ComplexNumber.NEG, tmpReconstructed);
            tmpReconstructed.axpy(-1, tmpCopy);
            BasicLogger.debugMatrix("Diff", tmpCopy);
        }

        TestUtils.assertEquals(tmpOriginal, tmpReconstructed, NumberContext.of(7, 6));
        TestUtils.assertEquals(tmpOriginal, tmpDecomposition, NumberContext.of(7, 6));

    }

    @Test
    public void testRandomFatCase() {
        CaseSingularValue.doTestTypes(MTRX_FAT);
    }

    @Test
    public void testRandomSquareCase() {
        CaseSingularValue.doTestTypes(MTRX_SQUARE);
    }

    @Test
    public void testRandomTallCase() {
        CaseSingularValue.doTestTypes(MTRX_TALL);
    }

    @Test
    public void testRecreationFat() {

        PhysicalStore<Double> tmpOriginal = R064Store.FACTORY.copy(MTRX_FAT);

        this.testRecreation(tmpOriginal);
    }

    @Test
    public void testRecreationSquare() {

        PhysicalStore<Double> tmpOriginal = R064Store.FACTORY.copy(MTRX_SQUARE);

        this.testRecreation(tmpOriginal);
    }

    @Test
    public void testRecreationTall() {

        PhysicalStore<Double> tmpOriginal = R064Store.FACTORY.copy(MTRX_TALL);

        this.testRecreation(tmpOriginal);
    }

    private void testRecreation(final PhysicalStore<Double> aMtrx) {

        SingularValue<Double>[] tmpImpls = MatrixDecompositionTests.getPrimitiveSingularValue();

        for (SingularValue<Double> tmpImpl : tmpImpls) {

            tmpImpl.decompose(aMtrx);
            final SingularValue<Double> decomposition = tmpImpl;
            MatrixStore<Double> tmpReconstructed = decomposition.reconstruct();
            if (!Access2D.equals(aMtrx, tmpReconstructed, NumberContext.of(7, 6))) {
                BasicLogger.error("Recreation failed for: {}", tmpImpl.getClass().getName());
            }
            if (!SingularValue.equals(aMtrx, tmpImpl, NumberContext.of(7, 6))) {
                BasicLogger.error("Decomposition not correct for: {}", tmpImpl.getClass().getName());
            }
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug(tmpImpl.toString());
                BasicLogger.debugMatrix("Original", aMtrx);
                BasicLogger.debugMatrix("Q1", tmpImpl.getU());
                BasicLogger.debugMatrix("D", tmpImpl.getD());
                BasicLogger.debugMatrix("Q2", tmpImpl.getV());
                BasicLogger.debugMatrix("Reconstructed", tmpReconstructed);
                PhysicalStore<Double> tmpCopy = aMtrx.copy();
                // tmpCopy.maxpy(-1.0, tmpReconstructed);
                tmpReconstructed.axpy(-1, tmpCopy);
                BasicLogger.debugMatrix("Diff", tmpCopy);
            }

        }
    }

}
