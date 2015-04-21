/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.type.context.NumberContext;

public class StoreProblems extends AbstractMatrixStoreTest {

    public StoreProblems() {
        super();
    }

    public StoreProblems(final String arg0) {
        super(arg0);
    }

    /**
     * Problem with {@linkplain LogicalStore} and multi-threading. The {@linkplain MinBatchSize#EXECUTOR} was
     * designed to have a fixed number of threads which doesn't work if nested {@linkplain LogicalStore}s
     * require more. The program would hang. This test makes sure ojAlgo no longer hangs in such a case.
     */
    public void testP20071210() {

        BasicMatrix A, Bu, C, K, sx, r, currentState;

        final double[][] a = { { 1, 2 }, { 3, 4 } };
        A = PrimitiveMatrix.FACTORY.rows(a);
        final double[][] bu = { { 1, 0 }, { 0, 1 } };
        Bu = PrimitiveMatrix.FACTORY.rows(bu);
        C = PrimitiveMatrix.FACTORY.makeEye(2, 2);
        K = PrimitiveMatrix.FACTORY.makeEye(2, 2);
        final int hp = 2 * OjAlgoUtils.ENVIRONMENT.threads;

        final BasicMatrix eye = PrimitiveMatrix.FACTORY.makeEye(A.countRows(), A.countColumns());
        final BasicMatrix Aprime = A.subtract(Bu.multiply(K));
        BasicMatrix Apow = PrimitiveMatrix.FACTORY.copy(Aprime);
        final BasicMatrix tmp = Aprime.subtract(eye);
        sx = PrimitiveMatrix.FACTORY.copy(eye);
        sx = sx.mergeColumns(tmp);

        //loop runs hp-2 times, which means the first elements of the matrices must be "hardcoded"
        for (int i = 0; i < (hp - 2); i++) {
            sx = sx.mergeColumns(tmp.multiply(Apow));
            Apow = Apow.multiply(Apow);
        }
        currentState = PrimitiveMatrix.FACTORY.makeZero(A.countRows(), 1);
        currentState = currentState.add(1.0);
        r = sx.multiply(currentState);
    }

    /**
     * Peter Abeles reported a problem with ojAlgo his benchmark's C=A*BT test. The problem turned out be that
     * fillByMultiplying did not reset the destination matrix elements when doung "multiply right".
     */
    public void testP20110223() {

        final int tmpDim = 9;

        final PhysicalStore<Double> tmpMtrxA = PrimitiveDenseStore.FACTORY.copy(MatrixUtils.makeRandomComplexStore(tmpDim, tmpDim));
        final PhysicalStore<Double> tmpMtrxB = PrimitiveDenseStore.FACTORY.copy(MatrixUtils.makeRandomComplexStore(tmpDim, tmpDim));
        final PhysicalStore<Double> tmpMtrxC = PrimitiveDenseStore.FACTORY.makeZero(tmpDim, tmpDim);

        PhysicalStore<Double> tmpExpected;
        PhysicalStore<Double> tmpActual;

        tmpMtrxC.fillByMultiplying(tmpMtrxA, tmpMtrxB);
        tmpExpected = tmpMtrxC.copy();
        tmpMtrxC.fillByMultiplying(tmpMtrxA, tmpMtrxB);
        tmpActual = tmpMtrxC.copy();
        TestUtils.assertEquals(tmpExpected, tmpActual, new NumberContext(7, 6));

        tmpMtrxC.fillByMultiplying(tmpMtrxA, tmpMtrxB.builder().transpose().build());
        tmpExpected = tmpMtrxC.copy();
        tmpMtrxC.fillByMultiplying(tmpMtrxA, tmpMtrxB.builder().transpose().build());
        tmpActual = tmpMtrxC.copy();
        TestUtils.assertEquals(tmpExpected, tmpActual, new NumberContext(7, 6));

        tmpMtrxC.fillByMultiplying(tmpMtrxA.builder().transpose().build(), tmpMtrxB);
        tmpExpected = tmpMtrxC.copy();
        tmpMtrxC.fillByMultiplying(tmpMtrxA.builder().transpose().build(), tmpMtrxB);
        tmpActual = tmpMtrxC.copy();
        TestUtils.assertEquals(tmpExpected, tmpActual, new NumberContext(7, 6));
    }

}
