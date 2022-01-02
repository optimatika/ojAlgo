/*
 * Copyright 1997-2022 Optimatika
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
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.random.Normal;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * Verify that the various specific-size implementations produce the same results as the general
 * implementation (for various shapes).
 */
public class TestSpecificSizeImplementationRational {

    private static final NumberContext ACCURACY_P64 = NumberContext.of(12, 14);

    static final int[] DIMS = new int[] { 1, 10, 100 };

    @Test
    public void testMultiplyBoth() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    GenericStore<RationalNumber> left = GenericStore.RATIONAL.makeFilled(m, c, Normal.standard());
                    GenericStore<RationalNumber> right = GenericStore.RATIONAL.makeFilled(c, n, Normal.standard());
                    GenericStore<RationalNumber> expected = GenericStore.RATIONAL.make(m, n);
                    GenericStore<RationalNumber> actual = GenericStore.RATIONAL.make(m, n);

                    MultiplyBoth.fillMxN_G(expected, left, c, right);
                    MultiplyBoth.Generic<RationalNumber> generic = MultiplyBoth.newGeneric(m, n);
                    generic.invoke(actual, left, c, right);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P64);
                }
            }
        }
    }

    @Test
    public void testMultiplyBothShaded() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    GenericStore<RationalNumber> left = GenericStore.RATIONAL.makeFilled(m, c, Normal.standard());
                    GenericStore<RationalNumber> right = GenericStore.RATIONAL.makeFilled(c, n, Normal.standard());
                    GenericStore<RationalNumber> expected = GenericStore.RATIONAL.make(m, n);
                    GenericStore<RationalNumber> actual = GenericStore.RATIONAL.make(m, n);

                    MatrixStore<RationalNumber> l = left.tridiagonal();
                    MatrixStore<RationalNumber> r = right.tridiagonal();

                    MultiplyBoth.fillMxN_G(expected, l, c, r);
                    MultiplyBoth.Generic<RationalNumber> generic = MultiplyBoth.newGeneric(m, n);
                    generic.invoke(actual, l, c, r);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P64);
                }
            }
        }
    }

    @Test
    public void testMultiplyLeft() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    GenericStore<RationalNumber> left = GenericStore.RATIONAL.makeFilled(m, c, Normal.standard());
                    GenericStore<RationalNumber> right = GenericStore.RATIONAL.makeFilled(c, n, Normal.standard());
                    GenericStore<RationalNumber> expected = GenericStore.RATIONAL.make(m, n);
                    GenericStore<RationalNumber> actual = GenericStore.RATIONAL.make(m, n);

                    MultiplyLeft.fillMxN(expected.data, left, c, right.data, RationalNumber.FACTORY);
                    MultiplyLeft.Generic<RationalNumber> generic = MultiplyLeft.newGeneric(m, n);
                    generic.invoke(actual.data, left, c, right.data, RationalNumber.FACTORY);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P64);
                }
            }
        }
    }

    @Test
    public void testMultiplyLeftShaded() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    GenericStore<RationalNumber> left = GenericStore.RATIONAL.makeFilled(m, c, Normal.standard());
                    GenericStore<RationalNumber> right = GenericStore.RATIONAL.makeFilled(c, n, Normal.standard());
                    GenericStore<RationalNumber> expected = GenericStore.RATIONAL.make(m, n);
                    GenericStore<RationalNumber> actual = GenericStore.RATIONAL.make(m, n);

                    MatrixStore<RationalNumber> l = left.tridiagonal();

                    MultiplyLeft.fillMxN(expected.data, l, c, right.data, RationalNumber.FACTORY);
                    MultiplyLeft.Generic<RationalNumber> generic = MultiplyLeft.newGeneric(m, n);
                    generic.invoke(actual.data, l, c, right.data, RationalNumber.FACTORY);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P64);
                }
            }
        }
    }

    @Test
    public void testMultiplyNeither() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    GenericStore<RationalNumber> left = GenericStore.RATIONAL.makeFilled(m, c, Normal.standard());
                    GenericStore<RationalNumber> right = GenericStore.RATIONAL.makeFilled(c, n, Normal.standard());
                    GenericStore<RationalNumber> expected = GenericStore.RATIONAL.make(m, n);
                    GenericStore<RationalNumber> actual = GenericStore.RATIONAL.make(m, n);

                    MultiplyNeither.fillMxN(expected.data, left.data, c, right.data, RationalNumber.FACTORY);
                    MultiplyNeither.Generic<RationalNumber> generic = MultiplyNeither.newGeneric(m, n);
                    generic.invoke(actual.data, left.data, c, right.data, RationalNumber.FACTORY);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P64);
                }
            }
        }
    }

    @Test
    public void testMultiplyRight() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    GenericStore<RationalNumber> left = GenericStore.RATIONAL.makeFilled(m, c, Normal.standard());
                    GenericStore<RationalNumber> right = GenericStore.RATIONAL.makeFilled(c, n, Normal.standard());
                    GenericStore<RationalNumber> expected = GenericStore.RATIONAL.make(m, n);
                    GenericStore<RationalNumber> actual = GenericStore.RATIONAL.make(m, n);

                    MultiplyRight.fillMxN(expected.data, left.data, c, right, RationalNumber.FACTORY);
                    MultiplyRight.Generic<RationalNumber> generic = MultiplyRight.newGeneric(m, n);
                    generic.invoke(actual.data, left.data, c, right, RationalNumber.FACTORY);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P64);
                }
            }
        }
    }

    @Test
    public void testMultiplyRightShaded() {

        for (int m : DIMS) {
            for (int c : DIMS) {
                for (int n : DIMS) {

                    GenericStore<RationalNumber> left = GenericStore.RATIONAL.makeFilled(m, c, Normal.standard());
                    GenericStore<RationalNumber> right = GenericStore.RATIONAL.makeFilled(c, n, Normal.standard());
                    GenericStore<RationalNumber> expected = GenericStore.RATIONAL.make(m, n);
                    GenericStore<RationalNumber> actual = GenericStore.RATIONAL.make(m, n);

                    MatrixStore<RationalNumber> r = right.tridiagonal();

                    MultiplyRight.fillMxN(expected.data, left.data, c, r, RationalNumber.FACTORY);
                    MultiplyRight.Generic<RationalNumber> generic = MultiplyRight.newGeneric(m, n);
                    generic.invoke(actual.data, left.data, c, r, RationalNumber.FACTORY);

                    TestUtils.assertEquals(m + "-" + c + "-" + n, expected, actual, ACCURACY_P64);
                }
            }
        }
    }

}
