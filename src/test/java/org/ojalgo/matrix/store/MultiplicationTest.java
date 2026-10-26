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

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.random.Normal;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.Quaternion;
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

    /**
     * Products with operands that are plain, transposed or conjugated (a transposed array, that GEMM takes
     * with a transpose flag), or a transposed triangular view (a structured operand), in all combinations,
     * compared to a naive implementation. The shapes cover the special-size kernels, the switch to GEMM and
     * the multi-threaded Java implementation.
     */
    @SuppressWarnings("unchecked")
    private static <I extends PhysicalStore<Double> & Factory2D.Builder<I>> void doTestTransposedProducts(final Factory<Double, I> factory,
            final double accuracy) {

        String[] names = { "plain", "transposed", "conjugated", "triangular" };

        int[][] dims = { { 1, 1, 1 }, { 2, 2, 2 }, { 2, 30, 2 }, { 3, 3, 3 }, { 4, 4, 4 }, { 4, 50, 4 }, { 5, 5, 5 }, { 5, 30, 5 }, { 1, 5, 7 }, { 7, 5, 1 },
                { 6, 6, 50 }, { 8, 200, 8 }, { 10, 10, 10 }, { 10, 10, 1000 }, { 12, 12, 12 }, { 17, 15, 16 }, { 100, 3, 3 }, { 40, 30, 35 },
                { 150, 140, 130 } };

        for (int[] mkn : dims) {

            int m = mkn[0];
            int k = mkn[1];
            int n = mkn[2];

            Random random = new Random(m + 31 * k + 961 * n);
            I left = MultiplicationTest.makeRandom(factory, random, m, k);
            I leftT = MultiplicationTest.makeRandom(factory, random, k, m);
            I right = MultiplicationTest.makeRandom(factory, random, k, n);
            I rightT = MultiplicationTest.makeRandom(factory, random, n, k);

            MatrixStore<Double>[] lefts = new MatrixStore[] { left, leftT.transpose(), leftT.conjugate(), leftT.triangular(true, false).transpose() };
            MatrixStore<Double>[] rights = new MatrixStore[] { right, rightT.transpose(), rightT.conjugate(), rightT.triangular(false, false).transpose() };

            I product = factory.make(m, n);

            for (int l = 0; l < lefts.length; l++) {
                for (int r = 0; r < rights.length; r++) {

                    String message = m + "x" + k + "x" + n + " " + names[l] + " * " + names[r];

                    R064Store expected = MultiplicationTest.multiplyNaivelyR064(lefts[l], rights[r]);
                    product.fillByMultiplying(lefts[l], rights[r]);

                    MatrixStore<Double>[] actuals = new MatrixStore[] { lefts[l].multiply(rights[r]), product };

                    for (MatrixStore<Double> actual : actuals) {
                        TestUtils.assertEquals(message + ", rows", m, actual.countRows());
                        TestUtils.assertEquals(message + ", columns", n, actual.countColumns());
                        for (int j = 0; j < n; j++) {
                            for (int i = 0; i < m; i++) {
                                double error = Math.abs(expected.doubleValue(i, j) - actual.doubleValue(i, j));
                                TestUtils.assertTrue(message + " at (" + i + "," + j + "): " + error, error <= accuracy * k);
                            }
                        }
                    }
                }
            }
        }
    }

    private static GenericStore<Quaternion> makeQuaternions(final Random random, final int nbRows, final int nbCols) {
        GenericStore<Quaternion> retVal = GenericStore.H256.make(nbRows, nbCols);
        for (int j = 0; j < nbCols; j++) {
            for (int i = 0; i < nbRows; i++) {
                retVal.set(i, j, Quaternion.of(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5));
            }
        }
        return retVal;
    }

    private static <I extends PhysicalStore<Double> & Factory2D.Builder<I>> I makeRandom(final Factory<Double, I> factory, final Random random,
            final int nbRows, final int nbCols) {
        I retVal = factory.make(nbRows, nbCols);
        for (int j = 0; j < nbCols; j++) {
            for (int i = 0; i < nbRows; i++) {
                retVal.set(i, j, random.nextDouble() - 0.5);
            }
        }
        return retVal;
    }

    /**
     * The product, element by element, with the left factor on the left in each product.
     */
    private static GenericStore<Quaternion> multiplyNaively(final MatrixStore<Quaternion> left, final MatrixStore<Quaternion> right) {
        GenericStore<Quaternion> retVal = GenericStore.H256.make(left.getRowDim(), right.getColDim());
        for (int j = 0; j < right.getColDim(); j++) {
            for (int i = 0; i < left.getRowDim(); i++) {
                Quaternion sum = Quaternion.ZERO;
                for (int p = 0; p < left.getColDim(); p++) {
                    sum = sum.add(left.get(i, p).multiply(right.get(p, j)));
                }
                retVal.set(i, j, sum);
            }
        }
        return retVal;
    }

    /**
     * The product, element by element.
     */
    private static R064Store multiplyNaivelyR064(final MatrixStore<Double> left, final MatrixStore<Double> right) {
        R064Store retVal = R064Store.FACTORY.make(left.getRowDim(), right.getColDim());
        for (int j = 0; j < right.getColDim(); j++) {
            for (int i = 0; i < left.getRowDim(); i++) {
                double sum = 0.0;
                for (int p = 0; p < left.getColDim(); p++) {
                    sum += left.doubleValue(i, p) * right.doubleValue(p, j);
                }
                retVal.set(i, j, sum);
            }
        }
        return retVal;
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

    /**
     * Quaternion multiplication does not commute: each element of a matrix product must be formed with the
     * left factor's elements on the left. Plain, transposed and conjugated operands, in all combinations, are
     * compared to a naive implementation by the quaternion norm of the difference (all four components).
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testQuaternionProducts() {

        String[] names = { "plain", "transposed", "conjugated" };

        int[][] dims = { { 1, 1, 1 }, { 1, 5, 4 }, { 5, 1, 4 }, { 5, 4, 1 }, { 3, 4, 2 }, { 7, 7, 7 }, { 11, 9, 13 }, { 40, 30, 35 } };

        for (int[] mkn : dims) {

            int m = mkn[0];
            int k = mkn[1];
            int n = mkn[2];

            Random random = new Random(m + 31 * k + 961 * n);
            GenericStore<Quaternion> left = MultiplicationTest.makeQuaternions(random, m, k);
            GenericStore<Quaternion> leftT = MultiplicationTest.makeQuaternions(random, k, m);
            GenericStore<Quaternion> right = MultiplicationTest.makeQuaternions(random, k, n);
            GenericStore<Quaternion> rightT = MultiplicationTest.makeQuaternions(random, n, k);

            MatrixStore<Quaternion>[] lefts = new MatrixStore[] { left, leftT.transpose(), leftT.conjugate() };
            MatrixStore<Quaternion>[] rights = new MatrixStore[] { right, rightT.transpose(), rightT.conjugate() };

            GenericStore<Quaternion> product = GenericStore.H256.make(m, n);

            for (int l = 0; l < lefts.length; l++) {
                for (int r = 0; r < rights.length; r++) {

                    String message = m + "x" + k + "x" + n + " " + names[l] + " * " + names[r];

                    GenericStore<Quaternion> expected = MultiplicationTest.multiplyNaively(lefts[l], rights[r]);
                    product.fillByMultiplying(lefts[l], rights[r]);

                    MatrixStore<Quaternion>[] actuals = new MatrixStore[] { lefts[l].multiply(rights[r]), product };

                    for (MatrixStore<Quaternion> actual : actuals) {
                        TestUtils.assertEquals(message + ", rows", m, actual.countRows());
                        TestUtils.assertEquals(message + ", columns", n, actual.countColumns());
                        for (int j = 0; j < n; j++) {
                            for (int i = 0; i < m; i++) {
                                double error = expected.get(i, j).subtract(actual.get(i, j)).norm();
                                TestUtils.assertTrue(message + " at (" + i + "," + j + "): " + error, error <= 1E-12 * k);
                            }
                        }
                    }
                }
            }
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

    @Test
    public void testTransposedR032Products() {
        MultiplicationTest.doTestTransposedProducts(R032Store.FACTORY, 1E-6);
    }

    @Test
    public void testTransposedR064Products() {
        MultiplicationTest.doTestTransposedProducts(R064Store.FACTORY, 1E-14);
    }

    /**
     * Operands are used with the shape their role in the product needs, whatever their own shape: a column
     * vector as the left operand of a 1-by-n product (as in x<sup>T</sup>*Q*x), a row vector as the right
     * operand of an m-by-1 product. Their own structure must then not be used.
     */
    @Test
    public void testVectorOperandsOfOtherShape() {

        for (int n : new int[] { 1, 2, 3, 5, 11, 40 }) {

            Random random = new Random(n);

            R064Store q = MultiplicationTest.makeRandom(R064Store.FACTORY, random, n, n);
            MatrixStore<Double> upper = q.triangular(true, false);

            RawStore column = MultiplicationTest.makeRandom(RawStore.FACTORY, random, n, 1);
            MatrixStore<Double> columnView = MultiplicationTest.makeRandom(R064Store.FACTORY, random, n + 2, 3).offsets(1, 1).limits(n, 1);
            MatrixStore<Double> rowView = MultiplicationTest.makeRandom(R064Store.FACTORY, random, 3, n + 2).offsets(1, 1).limits(1, n);

            for (MatrixStore<Double> x : List.of(column, columnView)) {

                double expected = 0.0;
                double expectedUpper = 0.0;
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        double term = x.doubleValue(i) * q.doubleValue(i, j) * x.doubleValue(j);
                        expected += term;
                        expectedUpper += i <= j ? term : 0.0;
                    }
                }

                TestUtils.assertTrue(n + " x^T*Q*x", Math.abs(expected - q.multiplyBoth(x)) <= 1E-12 * n * n);
                TestUtils.assertTrue(n + " x^T*U*x", Math.abs(expectedUpper - upper.multiplyBoth(x)) <= 1E-12 * n * n);
            }

            for (MatrixStore<Double> left : List.of(q, upper, columnView.transpose())) {

                int m = left.getRowDim();
                R064Store product = R064Store.FACTORY.make(m, 1);
                product.fillByMultiplying(left, rowView);

                for (int i = 0; i < m; i++) {
                    double expected = 0.0;
                    for (int c = 0; c < n; c++) {
                        expected += left.doubleValue(i, c) * rowView.doubleValue(c);
                    }
                    TestUtils.assertTrue(n + " A*x at " + i, Math.abs(expected - product.doubleValue(i)) <= 1E-12 * n);
                }
            }
        }
    }

}
