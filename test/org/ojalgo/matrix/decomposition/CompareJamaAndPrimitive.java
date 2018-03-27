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
import org.ojalgo.array.BasicArray;
import org.ojalgo.matrix.SimpleCholeskyCase;
import org.ojalgo.matrix.SimpleEigenvalueCase;
import org.ojalgo.matrix.SimpleLUCase;
import org.ojalgo.matrix.SimpleQRCase;
import org.ojalgo.matrix.SimpleSingularValueCase;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.type.context.NumberContext;

public class CompareJamaAndPrimitive {

    private static NumberContext COMPARE_CONTEXT = NumberContext.getGeneral(8);
    private static Cholesky<Double> JAMA_CHOLESKY = new RawCholesky();
    private static Eigenvalue<Double> JAMA_EvD = new RawEigenvalue.Dynamic();
    private static LU<Double> JAMA_LU = new RawLU();
    private static QR<Double> JAMA_QR = new RawQR();
    private static SingularValue<Double> JAMA_SVD = new RawSingularValue();
    private static Cholesky<Double> PRIMITIVE_CHOLESKY = Cholesky.PRIMITIVE.make();
    private static LU<Double> PRIMITIVE_DENSE_LU = LU.PRIMITIVE.make();
    private static Eigenvalue<Double> PRIMITIVE_EvD = Eigenvalue.PRIMITIVE.make();
    private static QR<Double> PRIMITIVE_QR = QR.PRIMITIVE.make();
    private static LU<Double> PRIMITIVE_RAW_LU = LU.PRIMITIVE.make();
    private static SingularValue<Double> PRIMITIVE_SVD = SingularValue.PRIMITIVE.make();

    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testSimpleCholeskyCase() {

        final MatrixStore<Double> tmpMtrxA = PrimitiveDenseStore.FACTORY.copy(SimpleCholeskyCase.getOriginal());

        TestUtils.assertEquals(JAMA_CHOLESKY.decompose(tmpMtrxA), PRIMITIVE_CHOLESKY.decompose(tmpMtrxA));

        TestUtils.assertEquals(JAMA_CHOLESKY.getL(), PRIMITIVE_CHOLESKY.getL(), COMPARE_CONTEXT);

        TestUtils.assertEquals(JAMA_CHOLESKY.getDeterminant(), PRIMITIVE_CHOLESKY.getDeterminant(), COMPARE_CONTEXT);

        TestUtils.assertEquals(JAMA_CHOLESKY.isComputed(), PRIMITIVE_CHOLESKY.isComputed());
        TestUtils.assertEquals(JAMA_CHOLESKY.isSPD(), PRIMITIVE_CHOLESKY.isSPD());
        TestUtils.assertEquals(JAMA_CHOLESKY.isSolvable(), PRIMITIVE_CHOLESKY.isSolvable());

        if (JAMA_CHOLESKY.isSolvable()) {
            TestUtils.assertEquals(JAMA_CHOLESKY.getSolution(tmpMtrxA), PRIMITIVE_CHOLESKY.getSolution(tmpMtrxA), COMPARE_CONTEXT);
        }
    }

    @Test
    public void testSimpleEigenvalueCase() {

        final MatrixStore<Double> tmpMtrxA = PrimitiveDenseStore.FACTORY.copy(SimpleEigenvalueCase.getOriginal());

        TestUtils.assertEquals(JAMA_EvD.decompose(tmpMtrxA), PRIMITIVE_EvD.decompose(tmpMtrxA));

        TestUtils.assertEquals(JAMA_EvD.isComputed(), PRIMITIVE_EvD.isComputed());
        // TestUtils.assertEquals(JAMA_EvD.isSolvable(), PRIMITIVE_EvD.isSolvable());

        TestUtils.assertEquals(JAMA_EvD.getTrace().doubleValue(), PRIMITIVE_EvD.getTrace().doubleValue(), COMPARE_CONTEXT);
        TestUtils.assertEquals(JAMA_EvD.getDeterminant().doubleValue(), PRIMITIVE_EvD.getDeterminant().doubleValue(), COMPARE_CONTEXT);

        TestUtils.assertEquals(tmpMtrxA, JAMA_EvD, COMPARE_CONTEXT);
        TestUtils.assertEquals(tmpMtrxA, PRIMITIVE_EvD, COMPARE_CONTEXT);

        TestUtils.assertEquals(JAMA_EvD.getEigenvalues(), PRIMITIVE_EvD.getEigenvalues());
    }

    @Test
    public void testSimpleLUCase() {

        // Dense

        final MatrixStore<Double> tmpMtrxA = PrimitiveDenseStore.FACTORY.copy(SimpleLUCase.getOrginal());

        TestUtils.assertEquals(JAMA_LU.decompose(tmpMtrxA), PRIMITIVE_DENSE_LU.decompose(tmpMtrxA));

        TestUtils.assertEquals(JAMA_LU.getL(), PRIMITIVE_DENSE_LU.getL(), COMPARE_CONTEXT);
        TestUtils.assertEquals(JAMA_LU.getU(), PRIMITIVE_DENSE_LU.getU(), COMPARE_CONTEXT);

        TestUtils.assertEquals(JAMA_LU.getDeterminant(), PRIMITIVE_DENSE_LU.getDeterminant(), COMPARE_CONTEXT);

        TestUtils.assertEquals(JAMA_LU.isComputed(), PRIMITIVE_DENSE_LU.isComputed());
        TestUtils.assertEquals(JAMA_LU.isSolvable(), PRIMITIVE_DENSE_LU.isSolvable());
        TestUtils.assertEquals(JAMA_LU.isSolvable(), PRIMITIVE_DENSE_LU.isSolvable());

        if (JAMA_LU.isSolvable()) {
            TestUtils.assertEquals(JAMA_LU.getSolution(tmpMtrxA), PRIMITIVE_DENSE_LU.getSolution(tmpMtrxA), COMPARE_CONTEXT);
        }

        // Raw

        TestUtils.assertEquals(JAMA_LU.decompose(tmpMtrxA), PRIMITIVE_RAW_LU.decompose(tmpMtrxA));

        TestUtils.assertEquals(JAMA_LU.getL(), PRIMITIVE_RAW_LU.getL(), COMPARE_CONTEXT);
        TestUtils.assertEquals(JAMA_LU.getU(), PRIMITIVE_RAW_LU.getU(), COMPARE_CONTEXT);

        TestUtils.assertEquals(JAMA_LU.getDeterminant(), PRIMITIVE_RAW_LU.getDeterminant(), COMPARE_CONTEXT);

        TestUtils.assertEquals(JAMA_LU.isComputed(), PRIMITIVE_RAW_LU.isComputed());
        TestUtils.assertEquals(JAMA_LU.isSolvable(), PRIMITIVE_RAW_LU.isSolvable());
        TestUtils.assertEquals(JAMA_LU.isSolvable(), PRIMITIVE_RAW_LU.isSolvable());

        if (JAMA_LU.isSolvable()) {
            TestUtils.assertEquals(JAMA_LU.getSolution(tmpMtrxA), PRIMITIVE_RAW_LU.getSolution(tmpMtrxA), COMPARE_CONTEXT);
        }
    }

    @Test
    public void testSimpleQRCase() {

        final MatrixStore<Double> tmpMtrxA = PrimitiveDenseStore.FACTORY.copy(SimpleQRCase.getOriginal());
        final int tmpMinDim = (int) Math.min(tmpMtrxA.countRows(), tmpMtrxA.countColumns());

        TestUtils.assertEquals(JAMA_QR.decompose(tmpMtrxA), PRIMITIVE_QR.decompose(tmpMtrxA));

        final int[] tmpEconSet = BasicArray.makeIncreasingRange(0, tmpMinDim);

        TestUtils.assertEquals(JAMA_QR.getQ(), PRIMITIVE_QR.getQ().logical().column(tmpEconSet).get(), COMPARE_CONTEXT);
        TestUtils.assertEquals(JAMA_QR.getR(), PRIMITIVE_QR.getR().logical().row(tmpEconSet).get(), COMPARE_CONTEXT);
        TestUtils.assertEquals(JAMA_QR.getRank(), PRIMITIVE_QR.getRank());

        TestUtils.assertEquals(JAMA_QR.isComputed(), PRIMITIVE_QR.isComputed());
        TestUtils.assertEquals(JAMA_QR.isSolvable(), PRIMITIVE_QR.isSolvable());

        if (JAMA_QR.isSolvable()) {
            TestUtils.assertEquals(JAMA_QR.getSolution(tmpMtrxA), PRIMITIVE_QR.getSolution(tmpMtrxA), COMPARE_CONTEXT);
        }
    }

    @Test
    public void testSimpleSingularValueCase() {

        final MatrixStore<Double> tmpMtrxA = PrimitiveDenseStore.FACTORY.copy(SimpleSingularValueCase.getOriginal());

        TestUtils.assertEquals(JAMA_SVD.decompose(tmpMtrxA), PRIMITIVE_SVD.decompose(tmpMtrxA));

        TestUtils.assertEquals(JAMA_SVD.isComputed(), PRIMITIVE_SVD.isComputed());
        TestUtils.assertEquals(JAMA_SVD.isFullSize(), PRIMITIVE_SVD.isFullSize());
        TestUtils.assertEquals(JAMA_SVD.isSolvable(), PRIMITIVE_SVD.isSolvable());

        TestUtils.assertEquals(JAMA_SVD.getRank(), PRIMITIVE_SVD.getRank());

        TestUtils.assertEquals(JAMA_SVD.getCondition(), PRIMITIVE_SVD.getCondition(), COMPARE_CONTEXT);
        TestUtils.assertEquals(JAMA_SVD.getFrobeniusNorm(), PRIMITIVE_SVD.getFrobeniusNorm(), COMPARE_CONTEXT);
        TestUtils.assertEquals(JAMA_SVD.getKyFanNorm(0), PRIMITIVE_SVD.getKyFanNorm(0), COMPARE_CONTEXT);
        TestUtils.assertEquals(JAMA_SVD.getInverse(), PRIMITIVE_SVD.getInverse(), COMPARE_CONTEXT);
        TestUtils.assertEquals(JAMA_SVD.getSingularValues(), PRIMITIVE_SVD.getSingularValues());
    }

}
