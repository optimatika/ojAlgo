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
package org.ojalgo.tensor;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayAnyD;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.matrix.Primitive64Matrix;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;

public class TensorTest {

    private static final int DIM = 2;

    private static final Primitive64Matrix ELEMENTS_A = Primitive64Matrix.FACTORY.makeFilled(DIM, DIM, Uniform.standard());
    private static final Primitive64Matrix ELEMENTS_B = Primitive64Matrix.FACTORY.makeFilled(DIM, DIM, Uniform.standard());
    private static final Primitive64Matrix ELEMENTS_C = Primitive64Matrix.FACTORY.makeFilled(DIM, DIM, Uniform.standard());
    private static final Primitive64Matrix ELEMENTS_D = Primitive64Matrix.FACTORY.makeFilled(DIM, DIM, Uniform.standard());

    private static final Primitive64Matrix.Factory FACTORY__M = Primitive64Matrix.FACTORY;
    private static final TensorFactory1D<Double, VectorTensor<Double>> FACTORY_1 = VectorTensor.factory(Primitive64Array.FACTORY);
    private static final TensorFactory2D<Double, MatrixTensor<Double>> FACTORY_2 = MatrixTensor.factory(Primitive64Array.FACTORY);
    private static final TensorFactoryAnyD<Double, AnyTensor<Double>> FACTORY_N = AnyTensor.factory(Primitive64Array.FACTORY);

    static final boolean DEBUG = false;

    @Test
    public void testCompareProductBetweenFactories() {

        VectorTensor<Double> vectorA = FACTORY_1.copy(ELEMENTS_A);
        VectorTensor<Double> vectorB = FACTORY_1.copy(ELEMENTS_B);

        MatrixTensor<Double> prod2 = FACTORY_2.product(vectorA, vectorB);
        AnyTensor<Double> prodN = FACTORY_N.product(vectorA, vectorB);

        if (DEBUG) {
            BasicLogger.debug("prod2: {}", prod2.toRawCopy1D());
            BasicLogger.debug("prodN: {}", prodN.toRawCopy1D());
        }

        TestUtils.assertEquals(prod2, prodN);
    }

    @Test
    public void testCompareSumBetweenFactories() {

        MatrixTensor<Double> matrixA = FACTORY_2.copy(ELEMENTS_A);
        MatrixTensor<Double> matrixB = FACTORY_2.copy(ELEMENTS_B);

        AnyTensor<Double> anyA = FACTORY_N.copy(ELEMENTS_A);
        AnyTensor<Double> anyB = FACTORY_N.copy(ELEMENTS_B);

        MatrixTensor<Double> sum2 = FACTORY_2.blocks(matrixA, matrixB);
        AnyTensor<Double> sumN = FACTORY_N.blocks(anyA, anyB);

        if (DEBUG) {
            BasicLogger.debug("vecProd22: {}", sum2.toRawCopy1D());
            BasicLogger.debug("vecProd2N: {}", sumN.toRawCopy1D());
        }

        TestUtils.assertEquals(sum2, sumN);
    }

    @Test
    public void testConjugate2D() {

        VectorTensor<Double> vectorA = FACTORY_1.copy(ELEMENTS_A);
        VectorTensor<Double> vectorB = FACTORY_1.copy(ELEMENTS_B);

        MatrixTensor<Double> original = FACTORY_2.product(vectorA, vectorB);
        MatrixTensor<Double> reversed = FACTORY_2.product(vectorB, vectorA);
        MatrixTensor<Double> conjugated = original.conjugate();

        if (DEBUG) {
            BasicLogger.debug("original", original);
            BasicLogger.debug("reversed", reversed);
            BasicLogger.debug("conjugated", conjugated);
        }

        TestUtils.assertEquals(reversed, conjugated);
    }

    @Test
    public void testConjugate3D() {

        VectorTensor<Double> vectorA = FACTORY_1.copy(ELEMENTS_A);
        VectorTensor<Double> vectorB = FACTORY_1.copy(ELEMENTS_B);
        VectorTensor<Double> vectorC = FACTORY_1.copy(ELEMENTS_C);

        AnyTensor<Double> original = FACTORY_N.product(vectorA, vectorB, vectorC);
        AnyTensor<Double> reversed = FACTORY_N.product(vectorC, vectorB, vectorA);
        AnyTensor<Double> conjugated = original.conjugate();

        if (DEBUG) {
            BasicLogger.debug("original: {}", original);
            BasicLogger.debug("reversed: {}", reversed);
            BasicLogger.debug("conjugated: {}", conjugated);
        }

        TestUtils.assertEquals(reversed, conjugated);
    }

    @Test
    public void testConjugate4D() {

        VectorTensor<Double> vectorA = FACTORY_1.copy(ELEMENTS_A);
        VectorTensor<Double> vectorB = FACTORY_1.copy(ELEMENTS_B);
        VectorTensor<Double> vectorC = FACTORY_1.copy(ELEMENTS_C);
        VectorTensor<Double> vectorD = FACTORY_1.copy(ELEMENTS_D);

        AnyTensor<Double> original = FACTORY_N.product(vectorA, vectorB, vectorC, vectorD);
        AnyTensor<Double> reversed = FACTORY_N.product(vectorD, vectorC, vectorB, vectorA);
        AnyTensor<Double> conjugated = original.conjugate();

        if (DEBUG) {
            BasicLogger.debug("original: {}", original);
            BasicLogger.debug("reversed: {}", reversed);
            BasicLogger.debug("conjugated: {}", conjugated);
        }

        TestUtils.assertEquals(reversed, conjugated);
    }

    @Test
    public void testDeterminantAndTrace() {

        MatrixTensor<Double> tensorA = FACTORY_2.copy(ELEMENTS_A);
        MatrixTensor<Double> tensorB = FACTORY_2.copy(ELEMENTS_B);
        MatrixTensor<Double> tensorC = FACTORY_2.copy(ELEMENTS_C);
        MatrixTensor<Double> tensorD = FACTORY_2.copy(ELEMENTS_D);

        MatrixTensor<Double> sum1 = FACTORY_2.blocks(tensorA, tensorB);
        MatrixTensor<Double> sum2 = FACTORY_2.blocks(tensorC, tensorD);

        MatrixTensor<Double> product1 = FACTORY_2.kronecker(tensorA, tensorB);
        MatrixTensor<Double> product2 = FACTORY_2.kronecker(tensorC, tensorD);

        Primitive64Matrix matrixS1 = FACTORY__M.copy(sum1);
        Primitive64Matrix matrixS2 = FACTORY__M.copy(sum2);
        Primitive64Matrix matrixP1 = FACTORY__M.copy(product1);
        Primitive64Matrix matrixP2 = FACTORY__M.copy(product2);

        TestUtils.assertEquals(ELEMENTS_A.getDeterminant().multiply(ELEMENTS_B.getDeterminant()), matrixS1.getDeterminant());
        TestUtils.assertEquals(ELEMENTS_C.getDeterminant().multiply(ELEMENTS_D.getDeterminant()), matrixS2.getDeterminant());

        TestUtils.assertEquals(ELEMENTS_A.getTrace().add(ELEMENTS_B.getTrace()), matrixS1.getTrace());
        TestUtils.assertEquals(ELEMENTS_C.getTrace().add(ELEMENTS_D.getTrace()), matrixS2.getTrace());

        TestUtils.assertEquals(ELEMENTS_A.getDeterminant().power(DIM).multiply(ELEMENTS_B.getDeterminant().power(DIM)), matrixP1.getDeterminant());
        TestUtils.assertEquals(ELEMENTS_C.getDeterminant().power(DIM).multiply(ELEMENTS_D.getDeterminant().power(DIM)), matrixP2.getDeterminant());

        TestUtils.assertEquals(ELEMENTS_A.getTrace().multiply(ELEMENTS_B.getTrace()), matrixP1.getTrace());
        TestUtils.assertEquals(ELEMENTS_C.getTrace().multiply(ELEMENTS_D.getTrace()), matrixP2.getTrace());
    }

    @Test
    public void testKroneckerProduct() {

        MatrixTensor<Double> matrixA = FACTORY_2.copy(ELEMENTS_A);
        MatrixTensor<Double> matrixB = FACTORY_2.copy(ELEMENTS_B);

        MatrixTensor<Double> identity = FACTORY_2.identity(3);

        MatrixTensor<Double> left = FACTORY_2.kronecker(matrixA, identity);
        MatrixTensor<Double> right = FACTORY_2.kronecker(identity, matrixB);
        MatrixTensor<Double> both = FACTORY_2.kronecker(matrixA, matrixB);

        MatrixTensor<Double> blocks = FACTORY_2.blocks(matrixB, matrixB, matrixB);

        if (DEBUG) {
            BasicLogger.debug("left", left);
            BasicLogger.debug("right", right);
            BasicLogger.debug("both", both);
            BasicLogger.debug("blocks", blocks);
        }

        TestUtils.assertEquals(3 * DIM, right.dimensions());
        TestUtils.assertEquals(DIM * 3, left.dimensions());
        TestUtils.assertEquals(DIM * DIM, both.dimensions());

        TestUtils.assertEquals(blocks, right);

        MatrixTensor<Double> matrixC = FACTORY_2.copy(ELEMENTS_C);
        MatrixTensor<Double> matrixD = FACTORY_2.copy(ELEMENTS_D);

        Primitive64Matrix matrixAkB = FACTORY__M.copy(FACTORY_2.kronecker(matrixA, matrixB));
        Primitive64Matrix matrixCkD = FACTORY__M.copy(FACTORY_2.kronecker(matrixC, matrixD));
        Primitive64Matrix matrixAkBmCkD = matrixAkB.multiply(matrixCkD);

        MatrixTensor<Double> matrixAmC = FACTORY_2.copy(ELEMENTS_A.multiply(ELEMENTS_C));
        MatrixTensor<Double> matrixBmD = FACTORY_2.copy(ELEMENTS_B.multiply(ELEMENTS_D));

        MatrixTensor<Double> matrixACkBD = FACTORY_2.kronecker(matrixAmC, matrixBmD);

        TestUtils.assertEquals(matrixAkBmCkD, matrixACkBD);
    }

    @Test
    public void testProductOfVectors() {

        VectorTensor<Double> vectorA = FACTORY_1.copy(ELEMENTS_A);
        VectorTensor<Double> vectorB = FACTORY_1.copy(ELEMENTS_B);
        VectorTensor<Double> vectorC = FACTORY_1.copy(ELEMENTS_C);

        int length = DIM * DIM;
        int count = length * length * length;

        AnyTensor<Double> vectorProduct = FACTORY_N.product(vectorA, vectorB, vectorC);

        if (DEBUG) {
            BasicLogger.debug("vectorA: {}", vectorA);
            BasicLogger.debug("vectorB: {}", vectorB);
            BasicLogger.debug("vectorC: {}", vectorC);
            BasicLogger.debug("product: {}", vectorProduct);
        }

        TestUtils.assertEquals(3, vectorProduct.rank());
        TestUtils.assertEquals(length, vectorProduct.dimensions());
        TestUtils.assertEquals(count, vectorProduct.count());

        // Check first element
        TestUtils.assertEquals(vectorA.doubleValue(0) * vectorB.doubleValue(0) * vectorC.doubleValue(0), vectorProduct.doubleValue(0));

        int lastIndex = length - 1;

        // Check last element
        double expectedLast = vectorA.doubleValue(lastIndex) * vectorB.doubleValue(lastIndex) * vectorC.doubleValue(lastIndex);
        TestUtils.assertEquals(expectedLast, vectorProduct.doubleValue(new long[] { lastIndex, lastIndex, lastIndex }));
    }

    @Test
    public void testSumOfVectors() {

        VectorTensor<Double> vectorA = FACTORY_1.copy(ELEMENTS_A);
        VectorTensor<Double> vectorB = FACTORY_1.copy(ELEMENTS_B);
        VectorTensor<Double> vectorC = FACTORY_1.copy(ELEMENTS_C);

        int length = DIM * DIM;

        VectorTensor<Double> sum1 = FACTORY_1.sum(vectorA, vectorB, vectorC);

        if (DEBUG) {
            BasicLogger.debug("vectorA: {}", vectorA);
            BasicLogger.debug("vectorB: {}", vectorB);
            BasicLogger.debug("vectorC: {}", vectorC);
            BasicLogger.debug("sum: {}", sum1);
        }

        TestUtils.assertEquals(1, sum1.rank());
        TestUtils.assertEquals(3 * length, sum1.dimensions());

        for (int i = 0; i < vectorA.dimensions(); i++) {
            TestUtils.assertEquals(vectorA.doubleValue(i), sum1.doubleValue(i));
        }
        for (int i = 0; i < vectorB.dimensions(); i++) {
            TestUtils.assertEquals(vectorB.doubleValue(i), sum1.doubleValue(length + i));
        }
        for (int i = 0; i < vectorC.dimensions(); i++) {
            TestUtils.assertEquals(vectorC.doubleValue(i), sum1.doubleValue(length + length + i));
        }

        AnyTensor<Double> sumN = FACTORY_N.sum(vectorA, vectorB, vectorC);

        TestUtils.assertEquals(sum1, sumN);
    }

    /**
     * https://math.stackexchange.com/questions/2763055/tensor-product-of-3-vectors
     */
    @Test
    public void testTensorProductOf3Vectors() {

        VectorTensor<Double> u = FACTORY_1.values(1, 1);
        VectorTensor<Double> v = FACTORY_1.values(1, -2);
        VectorTensor<Double> w = FACTORY_1.values(-1, 3);

        ArrayAnyD<Double> expected = ArrayAnyD.PRIMITIVE64.make(2, 2, 2);
        expected.set(new long[] { 0, 0, 0 }, -1);
        expected.set(new long[] { 0, 0, 1 }, 3);
        expected.set(new long[] { 0, 1, 0 }, 2);
        expected.set(new long[] { 0, 1, 1 }, -6);
        expected.set(new long[] { 1, 0, 0 }, -1);
        expected.set(new long[] { 1, 0, 1 }, 3);
        expected.set(new long[] { 1, 1, 0 }, 2);
        expected.set(new long[] { 1, 1, 1 }, -6);

        AnyTensor<Double> actual = FACTORY_N.product(u, v, w);

        if (DEBUG) {
            BasicLogger.debug("expected: {}", expected);
            BasicLogger.debug("actual: {}", actual);
        }

        TestUtils.assertEquals(3, actual.rank());
        TestUtils.assertEquals(2, actual.dimensions());
        TestUtils.assertEquals(expected, actual);
    }

}
