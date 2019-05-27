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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.random.Normal;
import org.ojalgo.random.Uniform;

public class MultiplicationTest extends MatrixStoreTests {

    public MultiplicationTest() {
        super();
    }

    @Test
    public void testPower() {

        Factory<Double, PrimitiveDenseStore> factory = PrimitiveDenseStore.FACTORY;

        int dim = 9;
        PrimitiveDenseStore random = factory.makeSPD(dim);
        MatrixStore<Double> expected = factory.makeEye(dim, dim);

        for (int p = 0; p < 20; p++) {
            MatrixStore<Double> actual = random.power(p);
            TestUtils.assertEquals(expected, actual);
            expected = expected.multiply(random);
        }
    }

    @Test
    @Tag("slow")
    public void testRepeatedMultiplications() {

        int[] sizes = new int[] { 1, 2, 3, 4, 5, 10, 20, 50, 100 };

        for (int s = 0; s < sizes.length; s++) {
            int dim = sizes[s];

            PrimitiveDenseStore result = PrimitiveDenseStore.FACTORY.makeZero(dim, dim);
            MatrixStore<Double> matA = PrimitiveDenseStore.FACTORY.makeFilled(dim, dim, new Normal());
            MatrixStore<Double> matB = PrimitiveDenseStore.FACTORY.makeFilled(dim, dim, new Uniform());

            result.fillByMultiplying(matA, matB);
            PrimitiveDenseStore pp = result.copy();

            result.fillByMultiplying(matA, matB.transpose());
            PrimitiveDenseStore pt = result.copy();

            result.fillByMultiplying(matA.transpose(), matB);
            PrimitiveDenseStore tp = result.copy();

            result.fillByMultiplying(matA.transpose(), matB.transpose());
            PrimitiveDenseStore tt = result.copy();

            for (long i = 0; i < 3; i++) {

                result.fillByMultiplying(matA, matB);
                TestUtils.assertEquals(pp, result);

                result.fillByMultiplying(matA, matB.transpose());
                TestUtils.assertEquals(pt, result);

                result.fillByMultiplying(matA.transpose(), matB);
                TestUtils.assertEquals(tp, result);

                result.fillByMultiplying(matA.transpose(), matB.transpose());
                TestUtils.assertEquals(tt, result);
            }

            for (long i = 0; i < 3; i++) {
                result.fillByMultiplying(matA, matB);
                TestUtils.assertEquals(pp, result);
            }

            for (long i = 0; i < 3; i++) {
                result.fillByMultiplying(matA, matB.transpose());
                TestUtils.assertEquals(pt, result);
            }

            for (long i = 0; i < 3; i++) {
                result.fillByMultiplying(matA.transpose(), matB);
                TestUtils.assertEquals(tp, result);
            }

            for (long i = 0; i < 3; i++) {
                result.fillByMultiplying(matA.transpose(), matB.transpose());
                TestUtils.assertEquals(tt, result);
            }
        }
    }

}
