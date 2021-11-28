/*
 * Copyright 1997-2021 Optimatika
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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive32Store;
import org.ojalgo.matrix.store.Primitive64Store;
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

    @Test
    public void testMultiplyBothP32() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    Primitive32Store left = Primitive32Store.FACTORY.makeFilled(m, c, Normal.standard());
                    Primitive32Store right = Primitive32Store.FACTORY.makeFilled(c, n, Normal.standard());
                    Primitive32Store expected = Primitive32Store.FACTORY.make(m, n);
                    Primitive32Store actual = Primitive32Store.FACTORY.make(m, n);

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

                    Primitive64Store left = Primitive64Store.FACTORY.makeFilled(m, c, Normal.standard());
                    Primitive64Store right = Primitive64Store.FACTORY.makeFilled(c, n, Normal.standard());
                    Primitive64Store expected = Primitive64Store.FACTORY.make(m, n);
                    Primitive64Store actual = Primitive64Store.FACTORY.make(m, n);

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

                    Primitive32Store left = Primitive32Store.FACTORY.makeFilled(m, c, Normal.standard());
                    Primitive32Store right = Primitive32Store.FACTORY.makeFilled(c, n, Normal.standard());
                    Primitive32Store expected = Primitive32Store.FACTORY.make(m, n);
                    Primitive32Store actual = Primitive32Store.FACTORY.make(m, n);

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

                    Primitive64Store left = Primitive64Store.FACTORY.makeFilled(m, c, Normal.standard());
                    Primitive64Store right = Primitive64Store.FACTORY.makeFilled(c, n, Normal.standard());
                    Primitive64Store expected = Primitive64Store.FACTORY.make(m, n);
                    Primitive64Store actual = Primitive64Store.FACTORY.make(m, n);

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
    public void testMultiplyLeftP32() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    Primitive32Store left = Primitive32Store.FACTORY.makeFilled(m, c, Normal.standard());
                    Primitive32Store right = Primitive32Store.FACTORY.makeFilled(c, n, Normal.standard());
                    Primitive32Store expected = Primitive32Store.FACTORY.make(m, n);
                    Primitive32Store actual = Primitive32Store.FACTORY.make(m, n);

                    MultiplyLeft.fillMxN(expected.data, left, c, right.data);
                    MultiplyLeft.newPrimitive32(m, n).invoke(actual.data, left, c, right.data);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P32);
                }
            }
        }
    }

    @Test
    public void testMultiplyLeftP64() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    Primitive64Store left = Primitive64Store.FACTORY.makeFilled(m, c, Normal.standard());
                    Primitive64Store right = Primitive64Store.FACTORY.makeFilled(c, n, Normal.standard());
                    Primitive64Store expected = Primitive64Store.FACTORY.make(m, n);
                    Primitive64Store actual = Primitive64Store.FACTORY.make(m, n);

                    MultiplyLeft.fillMxN(expected.data, left, c, right.data);
                    MultiplyLeft.newPrimitive64(m, n).invoke(actual.data, left, c, right.data);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P64);
                }
            }
        }
    }

    @Test
    public void testMultiplyLeftShadedP32() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    Primitive32Store left = Primitive32Store.FACTORY.makeFilled(m, c, Normal.standard());
                    Primitive32Store right = Primitive32Store.FACTORY.makeFilled(c, n, Normal.standard());
                    Primitive32Store expected = Primitive32Store.FACTORY.make(m, n);
                    Primitive32Store actual = Primitive32Store.FACTORY.make(m, n);

                    MatrixStore<Double> l = left.tridiagonal();

                    MultiplyLeft.fillMxN(expected.data, l, c, right.data);
                    MultiplyLeft.newPrimitive32(m, n).invoke(actual.data, l, c, right.data);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P32);
                }
            }
        }
    }

    @Test
    public void testMultiplyLeftShadedP64() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    Primitive64Store left = Primitive64Store.FACTORY.makeFilled(m, c, Normal.standard());
                    Primitive64Store right = Primitive64Store.FACTORY.makeFilled(c, n, Normal.standard());
                    Primitive64Store expected = Primitive64Store.FACTORY.make(m, n);
                    Primitive64Store actual = Primitive64Store.FACTORY.make(m, n);

                    MatrixStore<Double> l = left.tridiagonal();

                    MultiplyLeft.fillMxN(expected.data, l, c, right.data);
                    MultiplyLeft.newPrimitive64(m, n).invoke(actual.data, l, c, right.data);

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

                    Primitive32Store left = Primitive32Store.FACTORY.makeFilled(m, c, Normal.standard());
                    Primitive32Store right = Primitive32Store.FACTORY.makeFilled(c, n, Normal.standard());
                    Primitive32Store expected = Primitive32Store.FACTORY.make(m, n);
                    Primitive32Store actual = Primitive32Store.FACTORY.make(m, n);

                    MultiplyNeither.fillMxN(expected.data, left.data, c, right.data);
                    MultiplyNeither.newPrimitive32(m, n).invoke(actual.data, left.data, c, right.data);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P32);
                }
            }
        }
    }

    @Test
    public void testMultiplyNeitherP64() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    Primitive64Store left = Primitive64Store.FACTORY.makeFilled(m, c, Normal.standard());
                    Primitive64Store right = Primitive64Store.FACTORY.makeFilled(c, n, Normal.standard());
                    Primitive64Store expected = Primitive64Store.FACTORY.make(m, n);
                    Primitive64Store actual = Primitive64Store.FACTORY.make(m, n);

                    MultiplyNeither.fillMxN(expected.data, left.data, c, right.data);
                    MultiplyNeither.newPrimitive64(m, n).invoke(actual.data, left.data, c, right.data);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P64);
                }
            }
        }
    }

    @Test
    public void testMultiplyRightP32() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    Primitive32Store left = Primitive32Store.FACTORY.makeFilled(m, c, Normal.standard());
                    Primitive32Store right = Primitive32Store.FACTORY.makeFilled(c, n, Normal.standard());
                    Primitive32Store expected = Primitive32Store.FACTORY.make(m, n);
                    Primitive32Store actual = Primitive32Store.FACTORY.make(m, n);

                    MultiplyRight.fillMxN(expected.data, left.data, c, right);
                    MultiplyRight.newPrimitive32(m, n).invoke(actual.data, left.data, c, right);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P32);
                }
            }
        }
    }

    @Test
    public void testMultiplyRightP64() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    Primitive64Store left = Primitive64Store.FACTORY.makeFilled(m, c, Normal.standard());
                    Primitive64Store right = Primitive64Store.FACTORY.makeFilled(c, n, Normal.standard());
                    Primitive64Store expected = Primitive64Store.FACTORY.make(m, n);
                    Primitive64Store actual = Primitive64Store.FACTORY.make(m, n);

                    MultiplyRight.fillMxN(expected.data, left.data, c, right);
                    MultiplyRight.newPrimitive64(m, n).invoke(actual.data, left.data, c, right);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P64);
                }
            }
        }
    }

    @Test
    public void testMultiplyRightShadedP32() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    Primitive32Store left = Primitive32Store.FACTORY.makeFilled(m, c, Normal.standard());
                    Primitive32Store right = Primitive32Store.FACTORY.makeFilled(c, n, Normal.standard());
                    Primitive32Store expected = Primitive32Store.FACTORY.make(m, n);
                    Primitive32Store actual = Primitive32Store.FACTORY.make(m, n);

                    MatrixStore<Double> r = right.tridiagonal();

                    MultiplyRight.fillMxN(expected.data, left.data, c, r);
                    MultiplyRight.newPrimitive32(m, n).invoke(actual.data, left.data, c, r);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P32);
                }
            }
        }
    }

    @Test
    public void testMultiplyRightShadedP64() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    Primitive64Store left = Primitive64Store.FACTORY.makeFilled(m, c, Normal.standard());
                    Primitive64Store right = Primitive64Store.FACTORY.makeFilled(c, n, Normal.standard());
                    Primitive64Store expected = Primitive64Store.FACTORY.make(m, n);
                    Primitive64Store actual = Primitive64Store.FACTORY.make(m, n);

                    MatrixStore<Double> r = right.tridiagonal();

                    MultiplyRight.fillMxN(expected.data, left.data, c, r);
                    MultiplyRight.newPrimitive64(m, n).invoke(actual.data, left.data, c, r);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P64);
                }
            }
        }
    }

}
