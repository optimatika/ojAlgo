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
package org.ojalgo.matrix.store;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class SuppliersAndConsumers extends AbstractMatrixStoreTest {

    @Test
    public void testMultiplyingAndTransposing() {

        final PrimitiveDenseStore tmpMtrxA = PrimitiveDenseStore.FACTORY.makeZero(10, 5);
        final MatrixStore<Double> tmpMtrxB = PrimitiveDenseStore.FACTORY.makeEye(5, 5).multiply(2.0);
        final PrimitiveDenseStore tmpMtrxC = PrimitiveDenseStore.FACTORY.makeZero(5, 10);
        final PrimitiveDenseStore tmpMtrxD = PrimitiveDenseStore.FACTORY.makeZero(5, 10);

        for (int j = 0; j < 5; j++) {
            tmpMtrxA.fillColumn(0L, j, j + 1.0);
            tmpMtrxD.fillRow(j, 0L, 2.0 * (j + 1.0));
        }

        tmpMtrxB.premultiply(tmpMtrxA).transpose().supplyTo(tmpMtrxC);

        TestUtils.assertEquals(tmpMtrxD, tmpMtrxC);
    }

}
