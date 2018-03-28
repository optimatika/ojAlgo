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
import org.ojalgo.matrix.SimpleCholeskyCase;
import org.ojalgo.matrix.SimpleEigenvalueCase;
import org.ojalgo.matrix.SimpleLUCase;
import org.ojalgo.matrix.SimpleQRCase;
import org.ojalgo.matrix.SimpleSingularValueCase;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.type.context.NumberContext;

/**
 * SimpleDecompositionCases
 *
 * @author apete
 */
public class TestJama {

    private static Cholesky<Double> CHOLESKY = new RawCholesky();
    private static Eigenvalue<Double> EIGENVALUE = new RawEigenvalue.Dynamic();
    private static NumberContext EVAL_CNTXT = NumberContext.getGeneral(8).newPrecision(15);
    private static LU<Double> LU = new RawLU();
    private static QR<Double> QR = new RawQR();
    private static SingularValue<Double> SINGULAR_VALUE = new RawSingularValue();

    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testSimpleCholeskyCase() {

        final MatrixStore<Double> tmpMtrxA = PrimitiveDenseStore.FACTORY.copy(SimpleCholeskyCase.getOriginal());

        this.computeAndTest(tmpMtrxA);
    }

    @Test
    public void testSimpleEigenvalueCase() {

        final MatrixStore<Double> tmpMtrxA = PrimitiveDenseStore.FACTORY.copy(SimpleEigenvalueCase.getOriginal());

        this.computeAndTest(tmpMtrxA);
    }

    @Test
    public void testSimpleLUCase() {

        final MatrixStore<Double> tmpMtrxA = PrimitiveDenseStore.FACTORY.copy(SimpleLUCase.getOrginal());

        this.computeAndTest(tmpMtrxA);
    }

    @Test
    public void testSimpleQRCase() {

        final MatrixStore<Double> tmpMtrxA = PrimitiveDenseStore.FACTORY.copy(SimpleQRCase.getOriginal());

        this.computeAndTest(tmpMtrxA);
    }

    @Test
    public void testSimpleSingularValueCase() {

        final MatrixStore<Double> tmpMtrxA = PrimitiveDenseStore.FACTORY.copy(SimpleSingularValueCase.getOriginal());

        this.computeAndTest(tmpMtrxA);
    }

    private void computeAndTest(final MatrixStore<Double> aMtrx) {

        if (aMtrx.countRows() == aMtrx.countColumns()) {

            // Requires square matrices

            CHOLESKY.decompose(aMtrx);
            if (CHOLESKY.isSolvable()) {
                TestUtils.assertEquals(aMtrx, CHOLESKY, EVAL_CNTXT);
            }

            EIGENVALUE.decompose(aMtrx);
            if (EIGENVALUE.isComputed()) {
                TestUtils.assertEquals(aMtrx, EIGENVALUE, EVAL_CNTXT);
            }
        }

        if (aMtrx.countRows() >= aMtrx.countColumns()) {

            // The Jama QR decomposition can't handle matrices that are fat,
            // and it's not the most common use case for this decomposition.

            QR.decompose(aMtrx);
            if (QR.isSolvable()) {
                TestUtils.assertEquals(aMtrx, QR, EVAL_CNTXT);
            }
        }

        LU.decompose(aMtrx);
        if (LU.isSolvable()) {
            TestUtils.assertEquals(aMtrx, LU, EVAL_CNTXT);
        }

        SINGULAR_VALUE.decompose(aMtrx);
        if (SINGULAR_VALUE.isSolvable()) {
            TestUtils.assertEquals(aMtrx, SINGULAR_VALUE, EVAL_CNTXT);
        }
    }

}
