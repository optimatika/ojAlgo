/*
 * Copyright 1997-2026 Optimatika
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
import org.ojalgo.structure.Factory2D;

public class MultiplicationTest extends MatrixStoreTests {

    /**
     * A product with an empty inner dimension, m-by-0 times 0-by-n, is an m-by-n matrix of zeros. It used to
     * throw ArithmeticException (division by zero) for almost every combination of store type and multiply
     * path. The sizes cover the special-size kernels (up to 10 rows) and the general case.
     */
    private static <N extends Comparable<N>, I extends PhysicalStore<N> & Factory2D.Builder<I>> void doTestEmptyInnerDimension(final Factory<N, I> factory) {

        int[] dims = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 40 };

        for (int m : dims) {
            for (int n : dims) {

                I expected = factory.make(m, n);

                I left = factory.make(m, 0);
                I right = factory.make(0, n);
                MatrixStore<N> leftView = factory.make(0, m).transpose();
                MatrixStore<N> rightView = factory.make(n, 0).transpose();

                TestUtils.assertEquals(expected, left.multiply(right));

                I product = factory.make(m, n);
                N one = factory.scalar().one().get();

                product.fillAll(one);
                product.fillByMultiplying(left, right);
                TestUtils.assertEquals(expected, product);

                product.fillAll(one);
                product.fillByMultiplying(left, rightView);
                TestUtils.assertEquals(expected, product);

                product.fillAll(one);
                product.fillByMultiplying(leftView, right);
                TestUtils.assertEquals(expected, product);

                product.fillAll(one);
                product.fillByMultiplying(leftView, rightView);
                TestUtils.assertEquals(expected, product);
            }
        }
    }

    public MultiplicationTest() {
        super();
    }

    @Test
    public void testEmptyInnerDimensionGeneric() {
        MultiplicationTest.doTestEmptyInnerDimension(GenericStore.R128);
    }

    @Test
    public void testEmptyInnerDimensionR032() {
        MultiplicationTest.doTestEmptyInnerDimension(R032Store.FACTORY);
    }

    @Test
    public void testEmptyInnerDimensionR064() {
        MultiplicationTest.doTestEmptyInnerDimension(R064Store.FACTORY);
    }

    @Test
    public void testEmptyInnerDimensionRaw() {
        MultiplicationTest.doTestEmptyInnerDimension(RawStore.FACTORY);
    }

    @Test
    public void testPower() {

        Factory<Double, R064Store> factory = R064Store.FACTORY;

        int dim = 9;
        R064Store random = factory.makeSPD(dim);
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

            R064Store result = R064Store.FACTORY.make(dim, dim);
            MatrixStore<Double> matA = R064Store.FACTORY.makeFilled(dim, dim, new Normal());
            MatrixStore<Double> matB = R064Store.FACTORY.makeFilled(dim, dim, new Uniform());

            result.fillByMultiplying(matA, matB);
            R064Store pp = result.copy();

            result.fillByMultiplying(matA, matB.transpose());
            R064Store pt = result.copy();

            result.fillByMultiplying(matA.transpose(), matB);
            R064Store tp = result.copy();

            result.fillByMultiplying(matA.transpose(), matB.transpose());
            R064Store tt = result.copy();

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
