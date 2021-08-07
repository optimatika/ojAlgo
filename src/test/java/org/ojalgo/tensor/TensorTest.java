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

    private static final TensorFactory1D<Double, VectorTensor<Double>> FACTORY_1 = VectorTensor.factory(Primitive64Array.FACTORY);
    private static final TensorFactory2D<Double, MatrixTensor<Double>> FACTORY_2 = MatrixTensor.factory(Primitive64Array.FACTORY);
    private static final TensorFactoryAnyD<Double, AnyTensor<Double>> FACTORY_N = AnyTensor.factory(Primitive64Array.FACTORY);

    private static final Primitive64Matrix.Factory FACTORY_M = Primitive64Matrix.FACTORY;

    static final boolean DEBUG = false;

    @Test
    public void testCompareConjugateProducts() {

        VectorTensor<Double> vectorA = FACTORY_1.copy(ELEMENTS_A);
        VectorTensor<Double> vectorB = FACTORY_1.copy(ELEMENTS_B);
        VectorTensor<Double> vectorC = FACTORY_1.copy(ELEMENTS_C);

        VectorTensor<Double> vecProd21 = FACTORY_1.product(vectorA, vectorB);
        MatrixTensor<Double> vecProd22 = FACTORY_2.product(vectorA, vectorB).conjugate();
        AnyTensor<Double> vecProd2N = FACTORY_N.product(vectorA, vectorB).conjugate();

        TestUtils.assertEquals(vecProd21, vecProd22);
        TestUtils.assertEquals(vecProd21, vecProd2N);

        VectorTensor<Double> vecProd31 = FACTORY_1.product(vectorA, vectorB, vectorC);
        AnyTensor<Double> vecProd3N = FACTORY_N.product(vectorA, vectorB, vectorC).conjugate();

        TestUtils.assertEquals(vecProd31, vecProd3N);
    }

    @Test
    public void testCompareTensorProductOf2Vectors() {

        VectorTensor<Double> vectorA = FACTORY_1.copy(ELEMENTS_A);
        VectorTensor<Double> vectorB = FACTORY_1.copy(ELEMENTS_B);

        MatrixTensor<Double> matrix = FACTORY_2.product(vectorA, vectorB);
        AnyTensor<Double> any = FACTORY_N.product(vectorA, vectorB);

        if (DEBUG) {
            BasicLogger.debug("matrix", matrix);
            BasicLogger.debug("any: {}", any);
        }

        TestUtils.assertEquals(matrix.rank(), any.rank());
        TestUtils.assertEquals(matrix.dimensions(), any.dimensions());

        TestUtils.assertTensorEquals(matrix, any);
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

        MatrixTensor<Double> sum1 = FACTORY_2.sum(tensorA, tensorB);
        MatrixTensor<Double> sum2 = FACTORY_2.sum(tensorC, tensorD);

        MatrixTensor<Double> product1 = FACTORY_2.product(tensorA, tensorB);
        MatrixTensor<Double> product2 = FACTORY_2.product(tensorC, tensorD);

        Primitive64Matrix matrixS1 = FACTORY_M.copy(sum1);
        Primitive64Matrix matrixS2 = FACTORY_M.copy(sum2);
        Primitive64Matrix matrixP1 = FACTORY_M.copy(product1);
        Primitive64Matrix matrixP2 = FACTORY_M.copy(product2);

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
    public void testMatrixProduct() {

        MatrixTensor<Double> matrixA = FACTORY_2.copy(ELEMENTS_A);

        MatrixTensor<Double> identity = FACTORY_2.identity(3);

        MatrixTensor<Double> right = FACTORY_2.product(identity, matrixA);
        MatrixTensor<Double> left = FACTORY_2.product(matrixA, identity);
        MatrixTensor<Double> both = FACTORY_2.product(matrixA, matrixA);

        MatrixTensor<Double> sum = FACTORY_2.sum(matrixA, matrixA, matrixA);

        if (DEBUG) {
            BasicLogger.debug("right", right);
            BasicLogger.debug("left", left);
            BasicLogger.debug("both", both);
            BasicLogger.debug("sum", sum);
        }

        TestUtils.assertEquals(3 * DIM, right.dimensions());
        TestUtils.assertEquals(DIM * 3, left.dimensions());
        TestUtils.assertEquals(DIM * DIM, both.dimensions());

        TestUtils.assertEquals(sum, right);
    }

    @Test
    public void testProductOfVectors() {

        VectorTensor<Double> vectorA = FACTORY_1.copy(ELEMENTS_A);
        VectorTensor<Double> vectorB = FACTORY_1.copy(ELEMENTS_B);
        VectorTensor<Double> vectorC = FACTORY_1.copy(ELEMENTS_C);

        int length = DIM * DIM;

        VectorTensor<Double> vectorProduct = FACTORY_1.product(vectorA, vectorB, vectorC);

        if (DEBUG) {
            BasicLogger.debug("vectorA: {}", vectorA);
            BasicLogger.debug("vectorB: {}", vectorB);
            BasicLogger.debug("vectorC: {}", vectorC);
            BasicLogger.debug("product: {}", vectorProduct);
        }

        TestUtils.assertEquals(1, vectorProduct.rank());
        TestUtils.assertEquals(length * length * length, vectorProduct.dimensions());

        // Check first element
        TestUtils.assertEquals(vectorA.doubleValue(0) * vectorB.doubleValue(0) * vectorC.doubleValue(0), vectorProduct.doubleValue(0));

        int lastABC = length - 1;
        int lastProduct = vectorProduct.dimensions() - 1;

        // Check last element
        TestUtils.assertEquals(vectorA.doubleValue(lastABC) * vectorB.doubleValue(lastABC) * vectorC.doubleValue(lastABC),
                vectorProduct.doubleValue(lastProduct));
    }

    @Test
    public void testSumOfVectors() {

        VectorTensor<Double> vectorA = FACTORY_1.copy(ELEMENTS_A);
        VectorTensor<Double> vectorB = FACTORY_1.copy(ELEMENTS_B);
        VectorTensor<Double> vectorC = FACTORY_1.copy(ELEMENTS_C);

        int length = DIM * DIM;

        VectorTensor<Double> vectorSum = FACTORY_1.sum(vectorA, vectorB, vectorC);

        if (DEBUG) {
            BasicLogger.debug("vectorA: {}", vectorA);
            BasicLogger.debug("vectorB: {}", vectorB);
            BasicLogger.debug("vectorC: {}", vectorC);
            BasicLogger.debug("sum: {}", vectorSum);
        }

        TestUtils.assertEquals(1, vectorSum.rank());
        TestUtils.assertEquals(3 * length, vectorSum.dimensions());

        for (int i = 0; i < vectorA.dimensions(); i++) {
            TestUtils.assertEquals(vectorA.doubleValue(i), vectorSum.doubleValue(i));
        }
        for (int i = 0; i < vectorB.dimensions(); i++) {
            TestUtils.assertEquals(vectorB.doubleValue(i), vectorSum.doubleValue(length + i));
        }
        for (int i = 0; i < vectorC.dimensions(); i++) {
            TestUtils.assertEquals(vectorC.doubleValue(i), vectorSum.doubleValue(length + length + i));
        }
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
