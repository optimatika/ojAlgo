/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.*;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

public class SingularValueTest extends AbstractMatrixDecompositionTest {

    private static final SingularValue<BigDecimal> BIG = SingularValueDecomposition.makeBig();
    private static final SingularValue<ComplexNumber> COMPLEX = SingularValueDecomposition.makeComplex();
    private static final SingularValue<Double> JAMA = SingularValueDecomposition.makeJama();
    private static final SingularValue<Double> DIRECT = SingularValueDecomposition.makePrimitive();

    private static final BasicMatrix FAT = BigMatrix.FACTORY.copy(MatrixUtils.makeRandomComplexStore(7, 9));
    private static final BasicMatrix SQUARE = BigMatrix.FACTORY.copy(MatrixUtils.makeRandomComplexStore(8, 8));
    private static final BasicMatrix TALL = BigMatrix.FACTORY.copy(MatrixUtils.makeRandomComplexStore(9, 7));

    static final NumberContext CNTXT_CPLX_DECOMP = TestUtils.EQUALS.newScale(5);
    static final NumberContext CNTXT_CPLX_VALUES = TestUtils.EQUALS.newScale(7);
    static final NumberContext CNTXT_REAL_DECOMP = TestUtils.EQUALS.newScale(6);
    static final NumberContext CNTXT_REAL_VALUES = TestUtils.EQUALS.newScale(10);

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

        final PhysicalStore<Double> tmpBaseMtrx = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0, 0.0, 0.0, 0.0, 2.0 }, { 0.0, 0.0, 3.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 4.0, 0.0, 0.0, 0.0 } });

        final Array1D<Double> tmpExpectedSingularValues = Array1D.PRIMITIVE.copy(new double[] { 4.0, 3.0, Math.sqrt(5.0), 0.0 });

        final ComplexNumber[] tmpScales = new ComplexNumber[] { ComplexNumber.makePolar(1.0, 0.0), ComplexNumber.makePolar(1.0, Math.PI / 2.0),
                ComplexNumber.makePolar(1.0, -Math.PI / 2.0), ComplexNumber.makePolar(1.0, Math.PI / 4.0), ComplexNumber.makePolar(1.0, (4.0 * Math.PI) / 3.0) };

        final Bidiagonal<ComplexNumber> tmpBidiagonal = BidiagonalDecomposition.makeComplex();
        final SingularValue<ComplexNumber> tmpSVD = SingularValueDecomposition.makeComplex();

        for (int s = 0; s < tmpScales.length; s++) {

            final PhysicalStore<ComplexNumber> tmpOriginalMtrx = ComplexDenseStore.FACTORY.transpose(tmpBaseMtrx);
            tmpOriginalMtrx.modifyAll(ComplexFunction.MULTIPLY.first(tmpScales[s]));

            tmpBidiagonal.compute(tmpOriginalMtrx);
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
            TestUtils.assertEquals(tmpOriginalMtrx, tmpReconstructed, TestUtils.EQUALS.newScale(6));
        }

        for (int s = 0; s < tmpScales.length; s++) {

            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug("Scale = {}", tmpScales[s]);
            }

            final PhysicalStore<ComplexNumber> tmpOriginalMtrx = ComplexDenseStore.FACTORY.copy(tmpBaseMtrx);
            tmpOriginalMtrx.modifyAll(ComplexFunction.MULTIPLY.first(tmpScales[s]));

            tmpBidiagonal.compute(tmpOriginalMtrx.conjugate());
            tmpSVD.compute(tmpOriginalMtrx, false, false);

            final Array1D<Double> tmpActualSingularValues = tmpSVD.getSingularValues();
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug("Expected = {}", tmpExpectedSingularValues);
                BasicLogger.debug("Actual = {}", tmpActualSingularValues);
            }
            TestUtils.assertEquals(tmpExpectedSingularValues, tmpActualSingularValues, TestUtils.EQUALS.newScale(6));

            final MatrixStore<ComplexNumber> tmpReconstructed = tmpSVD.reconstruct();
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug("Original", tmpOriginalMtrx);
                BasicLogger.debug("Reconstructed", tmpReconstructed);
            }
            TestUtils.assertEquals(tmpOriginalMtrx, tmpReconstructed, TestUtils.EQUALS.newScale(6));
        }

    }

    public void testRandomActuallyComplexCase() {

        final PhysicalStore<ComplexNumber> tmpOriginal = MatrixUtils.makeRandomComplexStore(4, 4);

        final SingularValue<ComplexNumber> tmpDecomposition = SingularValueDecomposition.makeComplex();

        tmpDecomposition.compute(tmpOriginal);

        final MatrixStore<ComplexNumber> tmpReconstructed = tmpDecomposition.reconstruct();

        if (!AccessUtils.equals(tmpOriginal, tmpReconstructed, TestUtils.EQUALS.newScale(6))) {
            BasicLogger.error("Recreation failed for: {}", tmpDecomposition.getClass().getName());
        }
        if (!MatrixUtils.equals(tmpOriginal, tmpDecomposition, TestUtils.EQUALS.newScale(6))) {
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

        TestUtils.assertEquals(tmpOriginal, tmpReconstructed, TestUtils.EQUALS.newScale(6));
        TestUtils.assertEquals(tmpOriginal, tmpDecomposition, TestUtils.EQUALS.newScale(6));

    }

    public void testRandomFatCase() {
        this.doTestTypes(FAT);
    }

    public void testRandomSquareCase() {
        this.doTestTypes(SQUARE);
    }

    public void testRandomTallCase() {
        this.doTestTypes(TALL);
    }

    public void testRecreationFat() {

        final PhysicalStore<Double> tmpOriginal = FAT.toPrimitiveStore();

        this.testRecreation(tmpOriginal);
    }

    public void testRecreationSquare() {

        final PhysicalStore<Double> tmpOriginal = SQUARE.toPrimitiveStore();

        this.testRecreation(tmpOriginal);
    }

    public void testRecreationTall() {

        final PhysicalStore<Double> tmpOriginal = TALL.toPrimitiveStore();

        this.testRecreation(tmpOriginal);
    }

    private void doTestTypes(final BasicMatrix aStore) {

        final PhysicalStore<BigDecimal> tmpBigStore = aStore.toBigStore();
        final PhysicalStore<ComplexNumber> tmpComplexStore = aStore.toComplexStore();
        final PhysicalStore<Double> tmpPrimitiveStore = aStore.toPrimitiveStore();

        BIG.compute(tmpBigStore);
        COMPLEX.compute(tmpComplexStore);
        JAMA.compute(tmpPrimitiveStore);
        DIRECT.compute(tmpPrimitiveStore);

        final Array1D<Double> tmpBigSingularValues = BIG.getSingularValues();
        final Array1D<Double> tmpComplexSingularValues = COMPLEX.getSingularValues();
        final Array1D<Double> tmpJamaSingularValues = JAMA.getSingularValues();
        final Array1D<Double> tmpDirectSingularValues = DIRECT.getSingularValues();

        UnaryFunction<Double> tmpPrimitiveRoundFunction = CNTXT_REAL_VALUES.getPrimitiveRoundFunction();
        tmpBigSingularValues.modifyAll(tmpPrimitiveRoundFunction);
        tmpComplexSingularValues.modifyAll(tmpPrimitiveRoundFunction);
        tmpJamaSingularValues.modifyAll(tmpPrimitiveRoundFunction);
        tmpDirectSingularValues.modifyAll(tmpPrimitiveRoundFunction);

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   S: {}.", tmpBigSingularValues);
            BasicLogger.debug("Cmplx S: {}.", tmpComplexSingularValues);
            BasicLogger.debug("Jama  S: {}.", tmpJamaSingularValues);
            BasicLogger.debug("Direc S: {}.", tmpDirectSingularValues);

        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   D", BIG.getD());
            BasicLogger.debug("Cmplx D", COMPLEX.getD());
            BasicLogger.debug("Jama  D", JAMA.getD());
            BasicLogger.debug("Direc D", DIRECT.getD());

        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   Q1", BIG.getQ1());
            BasicLogger.debug("Cmplx Q1", COMPLEX.getQ1());
            BasicLogger.debug("Jama  Q1", JAMA.getQ1());
            BasicLogger.debug("Direc Q1", DIRECT.getQ1());

        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   Q1 unitary", BIG.getQ1().builder().conjugate().build().multiplyRight(BIG.getQ1()));
            BasicLogger.debug("Cmplx Q1 unitary", COMPLEX.getQ1().builder().conjugate().build().multiplyRight(COMPLEX.getQ1()));
            BasicLogger.debug("Jama  Q1 unitary", JAMA.getQ1().builder().conjugate().build().multiplyRight(JAMA.getQ1()));
            BasicLogger.debug("Direc Q1 unitary", DIRECT.getQ1().builder().conjugate().build().multiplyRight(DIRECT.getQ1()));

        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   Q2", BIG.getQ2());
            BasicLogger.debug("Cmplx Q2", COMPLEX.getQ2());
            BasicLogger.debug("Jama  Q2", JAMA.getQ2());
            BasicLogger.debug("Direc Q2", DIRECT.getQ2());

        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Big   Q2 unitary", BIG.getQ2().builder().conjugate().build().multiplyLeft(BIG.getQ2()));
            BasicLogger.debug("Cmplx Q2 unitary", COMPLEX.getQ2().builder().conjugate().build().multiplyLeft(COMPLEX.getQ2()));
            BasicLogger.debug("Jama  Q2 unitary", JAMA.getQ2().builder().conjugate().build().multiplyLeft(JAMA.getQ2()));
            BasicLogger.debug("Direc Q2 unitary", DIRECT.getQ2().builder().conjugate().build().multiplyLeft(DIRECT.getQ2()));

        }

        tmpPrimitiveRoundFunction = CNTXT_CPLX_VALUES.getPrimitiveRoundFunction();
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
            BasicLogger.debug("Big   Recreated", BIG.reconstruct());
            BasicLogger.debug("Cmplx Recreated", COMPLEX.reconstruct());
            BasicLogger.debug("Jama  Recreated", JAMA.reconstruct());
            BasicLogger.debug("Direc Recreated", DIRECT.reconstruct());

        }

        TestUtils.assertEquals(tmpBigStore, BIG, CNTXT_REAL_DECOMP);
        // JUnitUtils.assertEquals(tmpComplexStore, COMPLEX, CNTXT_CPLX_DECOMP); // Fails too often...
        TestUtils.assertEquals(tmpPrimitiveStore, JAMA, CNTXT_REAL_DECOMP);
        TestUtils.assertEquals(tmpPrimitiveStore, DIRECT, CNTXT_REAL_DECOMP);

    }

    void testRecreation(final PhysicalStore<Double> aMtrx) {

        final SingularValue<Double>[] tmpImpls = MatrixDecompositionTests.getSingularValuePrimitive();

        for (int i = 0; i < tmpImpls.length; i++) {

            tmpImpls[i].compute(aMtrx);
            final MatrixStore<Double> tmpReconstructed = MatrixUtils.reconstruct(tmpImpls[i]);
            if (!AccessUtils.equals(aMtrx, tmpReconstructed, TestUtils.EQUALS.newScale(6))) {
                BasicLogger.error("Recreation failed for: {}", tmpImpls[i].getClass().getName());
            }
            if (!MatrixUtils.equals(aMtrx, tmpImpls[i], TestUtils.EQUALS.newScale(6))) {
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
