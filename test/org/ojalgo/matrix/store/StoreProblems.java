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
package org.ojalgo.matrix.store;

import org.junit.jupiter.api.Test;
import org.ojalgo.OjAlgoUtils;
import org.ojalgo.TestUtils;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.context.NumberContext;

public class StoreProblems extends MatrixStoreTests {

    /**
     * Problem with LogicalStore and multi-threading. The MinBatchSize#EXECUTOR was designed to have a fixed
     * number of threads which doesn't work if nested LogicalStores require more. The program would hang. This
     * test makes sure ojAlgo no longer hangs in such a case.
     */
    @Test
    public void testP20071210() {

        PrimitiveMatrix A, Bu, K, sx, currentState;

        final double[][] a = { { 1, 2 }, { 3, 4 } };
        A = PrimitiveMatrix.FACTORY.rows(a);
        final double[][] bu = { { 1, 0 }, { 0, 1 } };
        Bu = PrimitiveMatrix.FACTORY.rows(bu);
        PrimitiveMatrix.FACTORY.makeEye(2, 2);
        K = PrimitiveMatrix.FACTORY.makeEye(2, 2);
        final int hp = 2 * OjAlgoUtils.ENVIRONMENT.threads;

        final PrimitiveMatrix eye = PrimitiveMatrix.FACTORY.makeEye(A);
        final PrimitiveMatrix Aprime = A.subtract(Bu.multiply(K));
        PrimitiveMatrix Apow = PrimitiveMatrix.FACTORY.copy(Aprime);
        final PrimitiveMatrix tmp = Aprime.subtract(eye);
        sx = PrimitiveMatrix.FACTORY.copy(eye);
        sx = sx.logical().below(tmp).get();

        //loop runs hp-2 times, which means the first elements of the matrices must be "hardcoded"
        for (int i = 0; i < (hp - 2); i++) {
            sx = sx.logical().below(tmp.multiply(Apow)).get();
            Apow = Apow.multiply(Apow);
        }
        currentState = PrimitiveMatrix.FACTORY.makeZero(A.countRows(), 1);
        currentState = currentState.add(1.0);
        sx.multiply(currentState);
    }

    /**
     * Peter Abeles reported a problem with ojAlgo his benchmark's C=A*BT test. The problem turned out be that
     * fillByMultiplying did not reset the destination matrix elements when doung "multiply right".
     */
    @Test
    public void testP20110223() {

        final int tmpDim = 9;

        final PhysicalStore<Double> tmpMtrxA = PrimitiveDenseStore.FACTORY.copy(TestUtils.makeRandomComplexStore(tmpDim, tmpDim));
        final PhysicalStore<Double> tmpMtrxB = PrimitiveDenseStore.FACTORY.copy(TestUtils.makeRandomComplexStore(tmpDim, tmpDim));
        final PhysicalStore<Double> tmpMtrxC = PrimitiveDenseStore.FACTORY.makeZero(tmpDim, tmpDim);

        PhysicalStore<Double> tmpExpected;
        PhysicalStore<Double> tmpActual;

        tmpMtrxC.fillByMultiplying(tmpMtrxA, tmpMtrxB);
        tmpExpected = tmpMtrxC.copy();
        tmpMtrxC.fillByMultiplying(tmpMtrxA, tmpMtrxB);
        tmpActual = tmpMtrxC.copy();
        TestUtils.assertEquals(tmpExpected, tmpActual, new NumberContext(7, 6));

        tmpMtrxC.fillByMultiplying(tmpMtrxA, tmpMtrxB.logical().transpose().get());
        tmpExpected = tmpMtrxC.copy();
        tmpMtrxC.fillByMultiplying(tmpMtrxA, tmpMtrxB.logical().transpose().get());
        tmpActual = tmpMtrxC.copy();
        TestUtils.assertEquals(tmpExpected, tmpActual, new NumberContext(7, 6));

        tmpMtrxC.fillByMultiplying(tmpMtrxA.logical().transpose().get(), tmpMtrxB);
        tmpExpected = tmpMtrxC.copy();
        tmpMtrxC.fillByMultiplying(tmpMtrxA.logical().transpose().get(), tmpMtrxB);
        tmpActual = tmpMtrxC.copy();
        TestUtils.assertEquals(tmpExpected, tmpActual, new NumberContext(7, 6));
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/55
     */
    @Test
    public void testP20180121() {

        final SparseStore<Double> m = SparseStore.PRIMITIVE.make(3, 2);
        final PrimitiveDenseStore mAdd = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0, 0.0 }, { 0.0, 0.0 }, { 0.0, 0.0 } });
        final MatrixStore<Double> n = m.add(mAdd);

        final SparseStore<Double> eye = SparseStore.PRIMITIVE.make(2, 2);
        eye.set(0, 0, 1.0);
        eye.set(1, 1, 1.0);

        final MatrixStore<Double> prod = n.multiply(eye);

        TestUtils.assertEquals(mAdd, prod);

        final SparseStore<Double> m2 = SparseStore.PRIMITIVE.make(3, 2);
        m2.set(0, 0, 1.0);

        TestUtils.assertEquals(mAdd, m2.multiply(eye));
        TestUtils.assertEquals(mAdd, eye.premultiply(m2).get());

        final SparseStore<Double> a = SparseStore.PRIMITIVE.make(3, 3);
        a.set(1, 1, 1.0);

        final SparseStore<Double> b = SparseStore.PRIMITIVE.make(3, 5);
        b.set(1, 1, 1.0);
        b.set(0, 3, 1.0);

        final SparseStore<Double> c = SparseStore.PRIMITIVE.make(3, 5);
        c.set(1, 1, 1.0);

        if (DEBUG) {
            BasicLogger.debug("A", a);
            BasicLogger.debug("B", b);
            BasicLogger.debug("C", c);
        }

        TestUtils.assertEquals(c, a.multiply(b));
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/133
     */
    @Test
    public void testTransposeElementsSupplier() {

        double[][] _x = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
        double[][] _y = { { 0, 0, 0 }, { 1, 1, 1 }, { 2, 2, 2 } };
        double[][] exp = { { 1.0, 2.0, 3.0 }, { 3.0, 4.0, 5.0 }, { 5.0, 6.0, 7.0 } };

        PrimitiveDenseStore x = PrimitiveDenseStore.FACTORY.rows(_x);
        PrimitiveDenseStore y = PrimitiveDenseStore.FACTORY.rows(_y);

        ElementsSupplier<Double> diff = y.operateOnMatching(x, PrimitiveFunction.SUBTRACT);
        ElementsSupplier<Double> transp = diff.transpose();

        TestUtils.assertEquals(PrimitiveDenseStore.FACTORY.rows(exp), diff.get());
        TestUtils.assertEquals(PrimitiveDenseStore.FACTORY.columns(exp), transp.get());
    }

}
