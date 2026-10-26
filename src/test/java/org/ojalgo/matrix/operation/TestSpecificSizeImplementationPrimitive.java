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
package org.ojalgo.matrix.operation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R032Store;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.random.Normal;
import org.ojalgo.type.context.NumberContext;

/**
 * Verify that the various specific-size implementations produce the same results as the general
 * implementation (for various shapes).
 */
public class TestSpecificSizeImplementationPrimitive {

    private static final NumberContext ACCURACY_P32 = NumberContext.of(6, 7);
    private static final NumberContext ACCURACY_P64 = NumberContext.of(12, 14);

    static final int[] DIMS = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 50, 100, 200 };

    @BeforeEach
    public void configure() {
        MatrixOperation.setAllOperationThresholds(150);
    }

    @Test
    public void testMultiplyBothP32() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    R032Store left = R032Store.FACTORY.makeFilled(m, c, Normal.standard());
                    R032Store right = R032Store.FACTORY.makeFilled(c, n, Normal.standard());
                    R032Store expected = R032Store.FACTORY.make(m, n);
                    R032Store actual = R032Store.FACTORY.make(m, n);

                    MultiplyBoth.fillMxN_P64(expected, left, c, right);
                    MultiplyBoth.newPrimitive32(m, n).invoke(actual, left, c, right);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P32);
                }
            }
        }
    }

    @Test
    public void testMultiplyBothP64() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    R064Store left = R064Store.FACTORY.makeFilled(m, c, Normal.standard());
                    R064Store right = R064Store.FACTORY.makeFilled(c, n, Normal.standard());
                    R064Store expected = R064Store.FACTORY.make(m, n);
                    R064Store actual = R064Store.FACTORY.make(m, n);

                    MultiplyBoth.fillMxN_P64(expected, left, c, right);
                    MultiplyBoth.newPrimitive64(m, n).invoke(actual, left, c, right);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P64);
                }
            }
        }
    }

    @Test
    public void testMultiplyBothShadedP32() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    R032Store left = R032Store.FACTORY.makeFilled(m, c, Normal.standard());
                    R032Store right = R032Store.FACTORY.makeFilled(c, n, Normal.standard());
                    R032Store expected = R032Store.FACTORY.make(m, n);
                    R032Store actual = R032Store.FACTORY.make(m, n);

                    MatrixStore<Double> l = left.tridiagonal();
                    MatrixStore<Double> r = right.tridiagonal();

                    MultiplyBoth.fillMxN_P64(expected, l, c, r);
                    MultiplyBoth.newPrimitive32(m, n).invoke(actual, l, c, r);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P32);
                }
            }
        }
    }

    @Test
    public void testMultiplyBothShadedP64() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    R064Store left = R064Store.FACTORY.makeFilled(m, c, Normal.standard());
                    R064Store right = R064Store.FACTORY.makeFilled(c, n, Normal.standard());
                    R064Store expected = R064Store.FACTORY.make(m, n);
                    R064Store actual = R064Store.FACTORY.make(m, n);

                    MatrixStore<Double> l = left.tridiagonal();
                    MatrixStore<Double> r = right.tridiagonal();

                    MultiplyBoth.fillMxN_P64(expected, l, c, r);
                    MultiplyBoth.newPrimitive64(m, n).invoke(actual, l, c, r);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P64);
                }
            }
        }
    }

    @Test
    public void testMultiplyNeitherP32() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    R032Store left = R032Store.FACTORY.makeFilled(m, c, Normal.standard());
                    R032Store leftT = R032Store.FACTORY.makeFilled(c, m, Normal.standard());
                    R032Store right = R032Store.FACTORY.makeFilled(c, n, Normal.standard());
                    R032Store rightT = R032Store.FACTORY.makeFilled(n, c, Normal.standard());
                    R032Store expected = R032Store.FACTORY.make(m, n);
                    R032Store actual = R032Store.FACTORY.make(m, n);

                    for (boolean transpL : new boolean[] { false, true }) {
                        for (boolean transpR : new boolean[] { false, true }) {

                            float[] dataL = transpL ? leftT.data : left.data;
                            float[] dataR = transpR ? rightT.data : right.data;

                            MultiplyNeither.fillMxN(expected.data, dataL, transpL, c, dataR, transpR);
                            MultiplyNeither.newPrimitive32(m, n).invoke(actual.data, dataL, transpL, c, dataR, transpR);

                            TestUtils.assertEquals(m + "-" + c + "-" + n + " " + transpL + "/" + transpR, expected, actual, ACCURACY_P32);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testMultiplyNeitherP64() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    R064Store left = R064Store.FACTORY.makeFilled(m, c, Normal.standard());
                    R064Store leftT = R064Store.FACTORY.makeFilled(c, m, Normal.standard());
                    R064Store right = R064Store.FACTORY.makeFilled(c, n, Normal.standard());
                    R064Store rightT = R064Store.FACTORY.makeFilled(n, c, Normal.standard());
                    R064Store expected = R064Store.FACTORY.make(m, n);
                    R064Store actual = R064Store.FACTORY.make(m, n);

                    for (boolean transpL : new boolean[] { false, true }) {
                        for (boolean transpR : new boolean[] { false, true }) {

                            double[] dataL = transpL ? leftT.data : left.data;
                            double[] dataR = transpR ? rightT.data : right.data;

                            MultiplyNeither.fillMxN(expected.data, dataL, transpL, c, dataR, transpR);
                            MultiplyNeither.newPrimitive64(m, n).invoke(actual.data, dataL, transpL, c, dataR, transpR);

                            TestUtils.assertEquals(m + "-" + c + "-" + n + " " + transpL + "/" + transpR, expected, actual, ACCURACY_P64);
                        }
                    }
                }
            }
        }
    }

}
