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

import java.math.BigDecimal;

import org.ojalgo.TestUtils;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.function.ComplexFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.*;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

public class SingularValueTest extends MatrixDecompositionTests {

    private static final SingularValue<BigDecimal> IMPL_BIG = SingularValue.BIG.make();
    private static final SingularValue<ComplexNumber> IMPL_COMPLEX = SingularValue.COMPLEX.make();
    private static final SingularValue<Double> IMPL_PRIMITIVE = new SVDnew32.Primitive();
    private static final SingularValue<Double> IMPL_RAW = new RawSingularValue();

    private static final BasicMatrix MTRX_FAT = BigMatrix.FACTORY.copy(MatrixUtils.makeRandomComplexStore(7, 9));
    private static final BasicMatrix MTRX_SQUARE = BigMatrix.FACTORY.copy(MatrixUtils.makeRandomComplexStore(8, 8));
    private static final BasicMatrix MTRX_TALL = BigMatrix.FACTORY.copy(MatrixUtils.makeRandomComplexStore(9, 7));

    static final NumberContext CNTXT_CPLX_DECOMP = new NumberContext(7, 5);
    static final NumberContext CNTXT_CPLX_VALUES = new NumberContext(7, 7);
    static final NumberContext CNTXT_REAL_DECOMP = new NumberContext(4, 6);
    static final NumberContext CNTXT_REAL_VALUES = new NumberContext(7, 10);

    public SingularValueTest() {
        super();
    }

    public SingularValueTest(final String arg0) {
        super(arg0);
    }

    public void testBasicMatrixP20030422Case() {
        this.doTestTypes(P20030422Case.getProblematic());
    }

    public void testBasicMatrixP20030512Case() {
        this.doTestTypes(P20030512Case.getProblematic());
    }

    public void testBasicMatrixP20030528Case() {
        this.doTestTypes(P20030528Case.getProblematic());
    }

    public void testBasicMatrixP20050125Case() {
        this.doTestTypes(P20050125Case.getProblematic());
    }

    public void testBasicMatrixP20050827Case() {
        this.doTestTypes(PrimitiveMatrix.FACTORY.copy(P20050827Case.getProblematic().toPrimitiveStore()));
    }

    public void testBasicMatrixP20061119Case() {
        this.doTestTypes(P20061119Case.getProblematic());
    }

    public void testBasicMatrixP20071019FatCase() {
        this.doTestTypes(P20071019Case.getFatProblematic());
    }

    public void testBasicMatrixP20071019TallCase() {
        this.doTestTypes(P20071019Case.getTallProblematic());
    }

    /**
     * http://en.wikipedia.org/wiki/Singular_value_decomposition
     */
    public void testComplexNumberVersionOfWikipediaCase() {

        final PhysicalStore<Double> tmpBaseMtrx = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 1.0, 0.0, 0.0, 0.0, 2.0 }, { 0.0, 0.0, 3.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 4.0, 0.0, 0.0, 0.0 } });

        final Array1D<Double> tmpExpectedSingularValues = Array1D.PRIMITIVE.copy(new double[] { 4.0, 3.0, PrimitiveFunction.SQRT.invoke(5.0), 0.0 });

        final ComplexNumber[] tmpScales = new ComplexNumber[] { ComplexNumber.makePolar(1.0, 0.0), ComplexNumber.makePolar(1.0, Math.PI / 2.0),
                ComplexNumber.makePolar(1.0, -Math.PI / 2.0), ComplexNumber.makePolar(1.0, Math.PI / 4.0),
                ComplexNumber.makePolar(1.0, (4.0 * Math.PI) / 3.0) };

        final Bidiagonal<ComplexNumber> tmpBidiagonal = Bidiagonal.COMPLEX.make();
        final SingularValue<ComplexNumber> tmpSVD = SingularValue.COMPLEX.make();

        for (int s = 0; s < tmpScales.length; s++) {

            final PhysicalStore<ComplexNumber> tmpOriginalMtrx = ComplexDenseStore.FACTORY.transpose(tmpBaseMtrx);
            tmpOriginalMtrx.modifyAll(ComplexFunction.MULTIPLY.first(tmpScales[s]));

            tmpBidiagonal.decompose(tmpOriginalMtrx);
            final MatrixStore<ComplexNumber> tmpReconstructed = tmpBidiagonal.reconstruct();
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug("Scale = {}", tmpScales[s]);
                BasicLogger.debug("A", tmpOriginalMtrx);
                BasicLogger.debug("Q1", tmpBidiagonal.getQ1());
                BasicLogger.debug("D", tmpBidiagonal.getD());
                BasicLogger.debug("Q2", tmpBidiagonal.getQ2());
                BasicLogger.debug("Reconstructed", tmpReconstructed);
            }
            TestUtils.assertEquals(tmpOriginalMtrx, tmpReconstructed, new NumberContext(7, 6));
        }

        for (int s = 0; s < tmpScales.length; s++) {

            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug("Scale = {}", tmpScales[s]);
            }

            final PhysicalStore<ComplexNumber> tmpOriginalMtrx = ComplexDenseStore.FACTORY.copy(tmpBaseMtrx);
            tmpOriginalMtrx.modifyAll(ComplexFunction.MULTIPLY.first(tmpScales[s]));

            tmpBidiagonal.decompose(tmpOriginalMtrx.conjugate());
            tmpSVD.setFullSize(false);
            tmpSVD.decompose(tmpOriginalMtrx);

            final Array1D<Double> tmpActualSingularValues = tmpSVD.getSingularValues();
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug("Expected = {}", tmpExpectedSingularValues);
                BasicLogger.debug("Actual = {}", tmpActualSingularValues);
            }
            TestUtils.assertEquals(tmpExpectedSingularValues, tmpActualSingularValues, new NumberContext(7, 6));

            final MatrixStore<ComplexNumber> tmpReconstructed = tmpSVD.reconstruct();
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug("Original", tmpOriginalMtrx);
                BasicLogger.debug("Reconstructed", tmpReconstructed);
            }
            TestUtils.assertEquals(tmpOriginalMtrx, tmpReconstructed, new NumberContext(7, 6));
        }

    }

    public void testRandomActuallyComplexCase() {

        final PhysicalStore<ComplexNumber> tmpOriginal = MatrixUtils.makeRandomComplexStore(4, 4);

        final SingularValue<ComplexNumber> tmpDecomposition = SingularValue.COMPLEX.make();

        tmpDecomposition.decompose(tmpOriginal);

        final MatrixStore<ComplexNumber> tmpReconstructed = tmpDecomposition.reconstruct();

        if (!AccessUtils.equals(tmpOriginal, tmpReconstructed, new NumberContext(7, 6))) {
            BasicLogger.error("Recreation failed for: {}", tmpDecomposition.getClass().getName());
        }
        if (!MatrixUtils.equals(tmpOriginal, tmpDecomposition, new NumberContext(7, 6))) {
            BasicLogger.error("Decomposition not correct for: {}", tmpDecomposition.getClass().getName());
        }
        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug(tmpDecomposition.toString());
            BasicLogger.debug("Original", tmpOriginal);
            BasicLogger.debug("Q1", tmpDecomposition.getQ1());
            BasicLogger.debug("D", tmpDecomposition.getD());
            BasicLogger.debug("Q2", tmpDecomposition.getQ2());
            BasicLogger.debug("Reconstructed", tmpReconstructed);
            final PhysicalStore<ComplexNumber> tmpCopy = tmpOriginal.copy();
            tmpCopy.maxpy(ComplexNumber.NEG, tmpReconstructed);
            BasicLogger.debug("Diff", tmpCopy);
        }

        TestUtils.assertEquals(tmpOriginal, tmpReconstructed, new NumberContext(7, 6));
        TestUtils.assertEquals(tmpOriginal, tmpDecomposition, new NumberContext(7, 6));

    }

    public void testRandomFatCase() {
        this.doTestTypes(MTRX_FAT);
    }

    public void testRandomSquareCase() {
        this.doTestTypes(MTRX_SQUARE);
    }

    public void testRandomTallCase() {
        this.doTestTypes(MTRX_TALL);
    }

    public void testRecreationFat() {

        final PhysicalStore<Double> tmpOriginal = MTRX_FAT.toPrimitiveStore();

        this.testRecreation(tmpOriginal);
    }

    public void testRecreationSquare() {

        final PhysicalStore<Double> tmpOriginal = MTRX_SQUARE.toPrimitiveStore();

        this.testRecreation(tmpOriginal);
    }

    public void testRecreationTall() {

        final PhysicalStore<Double> tmpOriginal = MTRX_TALL.toPrimitiveStore();

        this.testRecreation(tmpOriginal);
    }

    private void doTestTypes(final BasicMatrix original) {

        final PhysicalStore<BigDecimal> tmpBigStore = original.toBigStore();
        final PhysicalStore<ComplexNumber> tmpComplexStore = original.toComplexStore();
        final PhysicalStore<Double> tmpPrimitiveStore = original.toPrimitiveStore();

        IMPL_BIG.decompose(original.toBigStore());
        IMPL_COMPLEX.decompose(original.toComplexStore());
        IMPL_RAW.decompose(original.toPrimitiveStore());
        IMPL_PRIMITIVE.decompose(original.toPrimitiveStore());

        final Array1D<Double> tmpBigSingularValues = IMPL_BIG.getSingularValues();
        final Array1D<Double> tmpComplexSingularValues = IMPL_COMPLEX.getSingularValues();
        final Array1D<Double> tmpJamaSingularValues = IMPL_RAW.getSingularValues();
        final Array1D<Double> tmpDirectSingularValues = IMPL_PRIMITIVE.getSingularValues();

        UnaryFunction<Double> tmpPrimitiveRoundFunction = CNTXT_REAL_VALUES.getPrimitiveFunction();
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
            BasicLogger.debug("Big   Q1", IMPL_BIG.getQ1());
            BasicLogger.debug("Cmplx Q1", IMPL_COMPLEX.getQ1());
            BasicLogger.debug("Jama  Q1", IMPL_RAW.getQ1());
            BasicLogger.debug("Direc Q1", IMPL_PRIMITIVE.getQ1());

        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   Q1 unitary", IMPL_BIG.getQ1().logical().conjugate().get().multiply(IMPL_BIG.getQ1()));
            BasicLogger.debug("Cmplx Q1 unitary", IMPL_COMPLEX.getQ1().logical().conjugate().get().multiply(IMPL_COMPLEX.getQ1()));
            BasicLogger.debug("Jama  Q1 unitary", IMPL_RAW.getQ1().logical().conjugate().get().multiply(IMPL_RAW.getQ1()));
            BasicLogger.debug("Direc Q1 unitary", IMPL_PRIMITIVE.getQ1().logical().conjugate().get().multiply(IMPL_PRIMITIVE.getQ1()));

        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   Q2", IMPL_BIG.getQ2());
            BasicLogger.debug("Cmplx Q2", IMPL_COMPLEX.getQ2());
            BasicLogger.debug("Jama  Q2", IMPL_RAW.getQ2());
            BasicLogger.debug("Direc Q2", IMPL_PRIMITIVE.getQ2());

        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   Q2 unitary", IMPL_BIG.getQ2().multiply(IMPL_BIG.getQ2().logical().conjugate().get()));
            BasicLogger.debug("Cmplx Q2 unitary", IMPL_COMPLEX.getQ2().multiply(IMPL_COMPLEX.getQ2().logical().conjugate().get()));
            BasicLogger.debug("Jama  Q2 unitary", IMPL_RAW.getQ2().multiply(IMPL_RAW.getQ2().logical().conjugate().get()));
            BasicLogger.debug("Direc Q2 unitary", IMPL_PRIMITIVE.getQ2().multiply(IMPL_PRIMITIVE.getQ2().logical().conjugate().get()));

        }

        tmpPrimitiveRoundFunction = CNTXT_CPLX_VALUES.getPrimitiveFunction();
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

        TestUtils.assertEquals(tmpBigStore, IMPL_BIG, CNTXT_REAL_DECOMP);
        TestUtils.assertEquals(tmpComplexStore, IMPL_COMPLEX, CNTXT_CPLX_DECOMP); // Fails too often...
        TestUtils.assertEquals(tmpPrimitiveStore, IMPL_RAW, CNTXT_REAL_DECOMP);
        TestUtils.assertEquals(tmpPrimitiveStore, IMPL_PRIMITIVE, CNTXT_REAL_DECOMP);

    }

    void testRecreation(final PhysicalStore<Double> aMtrx) {

        final SingularValue<Double>[] tmpImpls = MatrixDecompositionTests.getSingularValuePrimitive();

        for (int i = 0; i < tmpImpls.length; i++) {

            tmpImpls[i].decompose(aMtrx);
            final MatrixStore<Double> tmpReconstructed = MatrixUtils.reconstruct(tmpImpls[i]);
            if (!AccessUtils.equals(aMtrx, tmpReconstructed, new NumberContext(7, 6))) {
                BasicLogger.error("Recreation failed for: {}", tmpImpls[i].getClass().getName());
            }
            if (!MatrixUtils.equals(aMtrx, tmpImpls[i], new NumberContext(7, 6))) {
                BasicLogger.error("Decomposition not correct for: {}", tmpImpls[i].getClass().getName());
            }
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug(tmpImpls[i].toString());
                BasicLogger.debug("Original", aMtrx);
                BasicLogger.debug("Q1", tmpImpls[i].getQ1());
                BasicLogger.debug("D", tmpImpls[i].getD());
                BasicLogger.debug("Q2", tmpImpls[i].getQ2());
                BasicLogger.debug("Reconstructed", tmpReconstructed);
                final PhysicalStore<Double> tmpCopy = aMtrx.copy();
                tmpCopy.maxpy(-1.0, tmpReconstructed);
                BasicLogger.debug("Diff", tmpCopy);
            }

        }
    }

}
