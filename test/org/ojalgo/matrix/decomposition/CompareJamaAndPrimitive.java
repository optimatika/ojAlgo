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

import org.ojalgo.TestUtils;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.matrix.SimpleCholeskyCase;
import org.ojalgo.matrix.SimpleEigenvalueCase;
import org.ojalgo.matrix.SimpleLUCase;
import org.ojalgo.matrix.SimpleQRCase;
import org.ojalgo.matrix.SimpleSingularValueCase;
import org.ojalgo.matrix.store.ColumnsStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.RowsStore;
import org.ojalgo.type.context.NumberContext;

public class CompareJamaAndPrimitive extends MatrixDecompositionTests {

    private static NumberContext COMPARE_CONTEXT = NumberContext.getGeneral(8);
    private static Cholesky<Double> JAMA_CHOLESKY = Cholesky.makeJama();
    private static Eigenvalue<Double> JAMA_EvD = Eigenvalue.makeJama();
    private static LU<Double> JAMA_LU = LU.makeJama();
    private static QR<Double> JAMA_QR = QR.makeJama();
    private static SingularValue<Double> JAMA_SVD = SingularValue.makeJama();
    private static Cholesky<Double> PRIMITIVE_CHOLESKY = Cholesky.makePrimitive();
    private static LU<Double> PRIMITIVE_DENSE_LU = LU.makePrimitive();
    private static Eigenvalue<Double> PRIMITIVE_EvD = Eigenvalue.makePrimitive();
    private static QR<Double> PRIMITIVE_QR = QR.makePrimitive();
    private static LU<Double> PRIMITIVE_RAW_LU = LU.makePrimitive();
    private static SingularValue<Double> PRIMITIVE_SVD = SingularValue.makePrimitive();

    public CompareJamaAndPrimitive() {
        super();
    }

    public CompareJamaAndPrimitive(final String arg0) {
        super(arg0);
    }

    public void testSimpleCholeskyCase() {

        final MatrixStore<Double> tmpMtrxA = SimpleCholeskyCase.getOriginal().toPrimitiveStore();

        TestUtils.assertEquals(JAMA_CHOLESKY.compute(tmpMtrxA), PRIMITIVE_CHOLESKY.compute(tmpMtrxA));

        TestUtils.assertEquals(JAMA_CHOLESKY.getL(), PRIMITIVE_CHOLESKY.getL(), COMPARE_CONTEXT);

        TestUtils.assertEquals(JAMA_CHOLESKY.getDeterminant(), PRIMITIVE_CHOLESKY.getDeterminant(), COMPARE_CONTEXT);

        TestUtils.assertEquals(JAMA_CHOLESKY.isComputed(), PRIMITIVE_CHOLESKY.isComputed());
        TestUtils.assertEquals(JAMA_CHOLESKY.isSPD(), PRIMITIVE_CHOLESKY.isSPD());
        TestUtils.assertEquals(JAMA_CHOLESKY.isSolvable(), PRIMITIVE_CHOLESKY.isSolvable());

        if (JAMA_CHOLESKY.isSolvable()) {
            TestUtils.assertEquals(JAMA_CHOLESKY.solve(tmpMtrxA), PRIMITIVE_CHOLESKY.solve(tmpMtrxA), COMPARE_CONTEXT);
        }
    }

    public void testSimpleEigenvalueCase() {

        final MatrixStore<Double> tmpMtrxA = SimpleEigenvalueCase.getOriginal().toPrimitiveStore();

        TestUtils.assertEquals(JAMA_EvD.compute(tmpMtrxA), PRIMITIVE_EvD.compute(tmpMtrxA));

        TestUtils.assertEquals(JAMA_EvD.isComputed(), PRIMITIVE_EvD.isComputed());
        TestUtils.assertEquals(JAMA_EvD.isFullSize(), PRIMITIVE_EvD.isFullSize());
        TestUtils.assertEquals(JAMA_EvD.isSolvable(), PRIMITIVE_EvD.isSolvable());

        TestUtils.assertEquals(JAMA_EvD.getTrace().doubleValue(), PRIMITIVE_EvD.getTrace().doubleValue(), COMPARE_CONTEXT);
        TestUtils.assertEquals(JAMA_EvD.getDeterminant().doubleValue(), PRIMITIVE_EvD.getDeterminant().doubleValue(), COMPARE_CONTEXT);

        TestUtils.assertEquals(tmpMtrxA, JAMA_EvD, COMPARE_CONTEXT);
        TestUtils.assertEquals(tmpMtrxA, PRIMITIVE_EvD, COMPARE_CONTEXT);

        TestUtils.assertEquals(JAMA_EvD.getEigenvalues(), PRIMITIVE_EvD.getEigenvalues());
    }

    public void testSimpleLUCase() {

        // Dense

        final MatrixStore<Double> tmpMtrxA = SimpleLUCase.getOrginal().toPrimitiveStore();

        TestUtils.assertEquals(JAMA_LU.compute(tmpMtrxA), PRIMITIVE_DENSE_LU.compute(tmpMtrxA));

        TestUtils.assertEquals(JAMA_LU.getL(), PRIMITIVE_DENSE_LU.getL(), COMPARE_CONTEXT);
        TestUtils.assertEquals(JAMA_LU.getU(), PRIMITIVE_DENSE_LU.getU(), COMPARE_CONTEXT);

        TestUtils.assertEquals(JAMA_LU.getDeterminant(), PRIMITIVE_DENSE_LU.getDeterminant(), COMPARE_CONTEXT);

        TestUtils.assertEquals(JAMA_LU.isComputed(), PRIMITIVE_DENSE_LU.isComputed());
        TestUtils.assertEquals(JAMA_LU.isSquareAndNotSingular(), PRIMITIVE_DENSE_LU.isSquareAndNotSingular());
        TestUtils.assertEquals(JAMA_LU.isSolvable(), PRIMITIVE_DENSE_LU.isSolvable());

        if (JAMA_LU.isSolvable()) {
            TestUtils.assertEquals(JAMA_LU.solve(tmpMtrxA), PRIMITIVE_DENSE_LU.solve(tmpMtrxA), COMPARE_CONTEXT);
        }

        // Raw

        TestUtils.assertEquals(JAMA_LU.compute(tmpMtrxA), PRIMITIVE_RAW_LU.compute(tmpMtrxA));

        TestUtils.assertEquals(JAMA_LU.getL(), PRIMITIVE_RAW_LU.getL(), COMPARE_CONTEXT);
        TestUtils.assertEquals(JAMA_LU.getU(), PRIMITIVE_RAW_LU.getU(), COMPARE_CONTEXT);

        TestUtils.assertEquals(JAMA_LU.getDeterminant(), PRIMITIVE_RAW_LU.getDeterminant(), COMPARE_CONTEXT);

        TestUtils.assertEquals(JAMA_LU.isComputed(), PRIMITIVE_RAW_LU.isComputed());
        TestUtils.assertEquals(JAMA_LU.isSquareAndNotSingular(), PRIMITIVE_RAW_LU.isSquareAndNotSingular());
        TestUtils.assertEquals(JAMA_LU.isSolvable(), PRIMITIVE_RAW_LU.isSolvable());

        if (JAMA_LU.isSolvable()) {
            TestUtils.assertEquals(JAMA_LU.solve(tmpMtrxA), PRIMITIVE_RAW_LU.solve(tmpMtrxA), COMPARE_CONTEXT);
        }
    }

    public void testSimpleQRCase() {

        final MatrixStore<Double> tmpMtrxA = SimpleQRCase.getOriginal().toPrimitiveStore();
        final int tmpMinDim = (int) Math.min(tmpMtrxA.countRows(), tmpMtrxA.countColumns());

        TestUtils.assertEquals(JAMA_QR.compute(tmpMtrxA), PRIMITIVE_QR.compute(tmpMtrxA));

        final int[] tmpEconSet = AccessUtils.makeIncreasingRange(0, tmpMinDim);

        TestUtils.assertEquals(JAMA_QR.getQ(), new ColumnsStore<Double>(PRIMITIVE_QR.getQ(), tmpEconSet), COMPARE_CONTEXT);
        TestUtils.assertEquals(JAMA_QR.getR(), new RowsStore<Double>(PRIMITIVE_QR.getR(), tmpEconSet), COMPARE_CONTEXT);
        TestUtils.assertEquals(JAMA_QR.getRank(), PRIMITIVE_QR.getRank());

        TestUtils.assertEquals(JAMA_QR.isComputed(), PRIMITIVE_QR.isComputed());
        TestUtils.assertEquals(JAMA_QR.isSolvable(), PRIMITIVE_QR.isSolvable());

        if (JAMA_QR.isSolvable()) {
            TestUtils.assertEquals(JAMA_QR.solve(tmpMtrxA), PRIMITIVE_QR.solve(tmpMtrxA), COMPARE_CONTEXT);
        }
    }

    public void testSimpleSingularValueCase() {

        final MatrixStore<Double> tmpMtrxA = SimpleSingularValueCase.getOriginal().toPrimitiveStore();

        TestUtils.assertEquals(JAMA_SVD.compute(tmpMtrxA), PRIMITIVE_SVD.compute(tmpMtrxA));

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
