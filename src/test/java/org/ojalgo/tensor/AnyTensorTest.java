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
package org.ojalgo.tensor;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayC128;
import org.ojalgo.array.ArrayH256;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;

/**
 * An {@link AnyTensor} of rank 1 or 2 should behave exactly as a {@link VectorTensor} or {@link MatrixTensor}
 * with the same elements – for every operation and every factory method that exists in both forms.
 * <p>
 * The elements are complex (and in one test quaternions) so that conjugation and lost imaginary parts are
 * detected. Comparing the classes with each other is not enough on its own – they could be equally wrong –
 * so the factory results and the conjugates are also checked against their definitions.
 */
public class AnyTensorTest {

    private static final int DIM = 3;
    private static final TensorFactory1D<ComplexNumber, VectorTensor<ComplexNumber>> FACTORY_1 = VectorTensor.factory(ArrayC128.FACTORY);
    private static final TensorFactory2D<ComplexNumber, MatrixTensor<ComplexNumber>> FACTORY_2 = MatrixTensor.factory(ArrayC128.FACTORY);
    private static final TensorFactoryAnyD<ComplexNumber, AnyTensor<ComplexNumber>> FACTORY_N = AnyTensor.factory(ArrayC128.FACTORY);
    private static final ComplexNumber SCALAR = ComplexNumber.of(0.5, -2.0);

    private static <N extends Comparable<N>> void assertEquivalent(final MatrixTensor<N> expected, final AnyTensor<N> actual) {
        TestUtils.assertEquals(expected.rank(), actual.rank());
        TestUtils.assertEquals(expected.dimensions(), actual.dimensions());
        for (long j = 0L; j < expected.dimensions(); j++) {
            for (long i = 0L; i < expected.dimensions(); i++) {
                TestUtils.assertEquals(expected.get(i, j), actual.get(i, j));
            }
        }
    }

    private static <N extends Comparable<N>> void assertEquivalent(final VectorTensor<N> expected, final AnyTensor<N> actual) {
        TestUtils.assertEquals(expected.rank(), actual.rank());
        TestUtils.assertEquals(expected.dimensions(), actual.dimensions());
        for (long i = 0L; i < expected.dimensions(); i++) {
            TestUtils.assertEquals(expected.get(i), actual.get(i));
        }
    }

    private static MatrixTensor<ComplexNumber> newMatrix(final int offset) {
        MatrixTensor<ComplexNumber> retVal = FACTORY_2.make(DIM, DIM);
        for (int j = 0; j < DIM; j++) {
            for (int i = 0; i < DIM; i++) {
                retVal.set(i, j, AnyTensorTest.value(i + j * DIM, offset));
            }
        }
        return retVal;
    }

    private static VectorTensor<ComplexNumber> newVector(final int offset) {
        VectorTensor<ComplexNumber> retVal = FACTORY_1.make(DIM);
        for (int i = 0; i < DIM; i++) {
            retVal.set(i, AnyTensorTest.value(i, offset));
        }
        return retVal;
    }

    private static ComplexNumber value(final int index, final int offset) {
        return ComplexNumber.of(index + offset, offset - 2 * index);
    }

    /**
     * The conjugate of a tensor product of vectors is the tensor product of the conjugated vectors in reverse
     * order. With quaternions that only holds if the conjugate reverses the indices (and the factories multiply
     * in the order of the vectors).
     */
    @Test
    public void testConjugateOfProductH256() {

        TensorFactory1D<Quaternion, VectorTensor<Quaternion>> factory1 = VectorTensor.factory(ArrayH256.FACTORY);
        TensorFactory2D<Quaternion, MatrixTensor<Quaternion>> factory2 = MatrixTensor.factory(ArrayH256.FACTORY);
        TensorFactoryAnyD<Quaternion, AnyTensor<Quaternion>> factoryN = AnyTensor.factory(ArrayH256.FACTORY);

        VectorTensor<Quaternion> u = factory1.values(Quaternion.I, Quaternion.of(1, 2, 3, 4));
        VectorTensor<Quaternion> v = factory1.values(Quaternion.J, Quaternion.of(-1, 1, -2, 2));
        VectorTensor<Quaternion> w = factory1.values(Quaternion.K, Quaternion.of(3, 0, 1, -1));

        MatrixTensor<Quaternion> uv = factory2.product(u, v);

        TestUtils.assertEquals(Quaternion.K, uv.get(0, 0)); // i * j = k (while j * i = -k)
        TestUtils.assertEquals(factory2.product(v.conjugate(), u.conjugate()), uv.conjugate());

        AnyTensor<Quaternion> uvN = factoryN.product(u, v);

        assertEquivalent(uv, uvN);
        assertEquivalent(uv.conjugate(), uvN.conjugate());

        AnyTensor<Quaternion> uvw = factoryN.product(u, v, w);

        TestUtils.assertEquals(factoryN.product(w.conjugate(), v.conjugate(), u.conjugate()), uvw.conjugate());
    }

    @Test
    public void testKroneckerC128() {

        MatrixTensor<ComplexNumber> matrixA = FACTORY_2.make(2, 2);
        matrixA.set(0, 0, ComplexNumber.I);
        matrixA.set(0, 1, ComplexNumber.ONE);
        matrixA.set(1, 0, ComplexNumber.ZERO);
        matrixA.set(1, 1, ComplexNumber.of(2, 0));

        MatrixTensor<ComplexNumber> matrixB = FACTORY_2.make(2, 2);
        matrixB.set(0, 0, ComplexNumber.ONE);
        matrixB.set(0, 1, ComplexNumber.I);
        matrixB.set(1, 0, ComplexNumber.of(0, -1));
        matrixB.set(1, 1, ComplexNumber.ONE);

        MatrixTensor<ComplexNumber> kronecker = FACTORY_2.kronecker(matrixA, matrixB);

        TestUtils.assertEquals(4, kronecker.dimensions());

        for (int j1 = 0; j1 < 2; j1++) {
            for (int i1 = 0; i1 < 2; i1++) {
                for (int j2 = 0; j2 < 2; j2++) {
                    for (int i2 = 0; i2 < 2; i2++) {
                        ComplexNumber expected = matrixA.get(i1, j1).multiply(matrixB.get(i2, j2));
                        TestUtils.assertEquals(expected, kronecker.get(i1 * 2 + i2, j1 * 2 + j2));
                    }
                }
            }
        }

        TestUtils.assertEquals(ComplexNumber.NEG, kronecker.get(0, 1)); // i * i
        TestUtils.assertEquals(ComplexNumber.of(0, -2), kronecker.get(3, 2)); // 2 * -i
    }

    @Test
    public void testRank1Factories() {

        VectorTensor<ComplexNumber> vectorA = AnyTensorTest.newVector(1);
        VectorTensor<ComplexNumber> vectorB = AnyTensorTest.newVector(7);

        VectorTensor<ComplexNumber> sum1 = FACTORY_1.sum(vectorA, vectorB);

        TestUtils.assertEquals(2 * DIM, sum1.dimensions());
        for (int i = 0; i < DIM; i++) {
            TestUtils.assertEquals(vectorA.get(i), sum1.get(i));
            TestUtils.assertEquals(vectorB.get(i), sum1.get(DIM + i));
        }

        assertEquivalent(sum1, FACTORY_N.sum(vectorA, vectorB));

        assertEquivalent(vectorA, FACTORY_N.product(vectorA));
    }

    @Test
    public void testRank1Operations() {

        VectorTensor<ComplexNumber> vectorA = AnyTensorTest.newVector(1);
        VectorTensor<ComplexNumber> vectorB = AnyTensorTest.newVector(7);

        AnyTensor<ComplexNumber> anyA = FACTORY_N.copy(vectorA);
        AnyTensor<ComplexNumber> anyB = FACTORY_N.copy(vectorB);

        assertEquivalent(vectorA, anyA);

        assertEquivalent(vectorA.add(vectorB), anyA.add(anyB));
        assertEquivalent(vectorA.multiply(2.5), anyA.multiply(2.5));
        assertEquivalent(vectorA.multiply(SCALAR), anyA.multiply(SCALAR));
        assertEquivalent(vectorA.negate(), anyA.negate());
        assertEquivalent(vectorA.normalised(), anyA.normalised());
        TestUtils.assertEquals(vectorA.norm(), anyA.norm());

        VectorTensor<ComplexNumber> conjugated = vectorA.conjugate();

        for (int i = 0; i < DIM; i++) {
            TestUtils.assertEquals(vectorA.get(i).conjugate(), conjugated.get(i));
        }

        assertEquivalent(conjugated, anyA.conjugate());
    }

    @Test
    public void testRank2Factories() {

        VectorTensor<ComplexNumber> vectorA = AnyTensorTest.newVector(1);
        VectorTensor<ComplexNumber> vectorB = AnyTensorTest.newVector(7);

        MatrixTensor<ComplexNumber> product2 = FACTORY_2.product(vectorA, vectorB);

        for (int j = 0; j < DIM; j++) {
            for (int i = 0; i < DIM; i++) {
                TestUtils.assertEquals(vectorA.get(i).multiply(vectorB.get(j)), product2.get(i, j));
            }
        }

        assertEquivalent(product2, FACTORY_N.product(vectorA, vectorB));

        assertEquivalent(FACTORY_2.power2(vectorA), FACTORY_N.power(vectorA, 2));

        MatrixTensor<ComplexNumber> matrixA = AnyTensorTest.newMatrix(1);
        MatrixTensor<ComplexNumber> matrixB = AnyTensorTest.newMatrix(7);

        MatrixTensor<ComplexNumber> blocks2 = FACTORY_2.blocks(matrixA, matrixB);

        for (int j = 0; j < DIM; j++) {
            for (int i = 0; i < DIM; i++) {
                TestUtils.assertEquals(matrixA.get(i, j), blocks2.get(i, j));
                TestUtils.assertEquals(matrixB.get(i, j), blocks2.get(DIM + i, DIM + j));
                TestUtils.assertEquals(ComplexNumber.ZERO, blocks2.get(DIM + i, j));
                TestUtils.assertEquals(ComplexNumber.ZERO, blocks2.get(i, DIM + j));
            }
        }

        assertEquivalent(blocks2, FACTORY_N.blocks(FACTORY_N.copy(matrixA), FACTORY_N.copy(matrixB)));
    }

    @Test
    public void testRank2Operations() {

        MatrixTensor<ComplexNumber> matrixA = AnyTensorTest.newMatrix(1);
        MatrixTensor<ComplexNumber> matrixB = AnyTensorTest.newMatrix(7);

        AnyTensor<ComplexNumber> anyA = FACTORY_N.copy(matrixA);
        AnyTensor<ComplexNumber> anyB = FACTORY_N.copy(matrixB);

        assertEquivalent(matrixA, anyA);

        assertEquivalent(matrixA.add(matrixB), anyA.add(anyB));
        assertEquivalent(matrixA.multiply(2.5), anyA.multiply(2.5));
        assertEquivalent(matrixA.multiply(SCALAR), anyA.multiply(SCALAR));
        assertEquivalent(matrixA.negate(), anyA.negate());
        assertEquivalent(matrixA.normalised(), anyA.normalised());
        TestUtils.assertEquals(matrixA.norm(), anyA.norm());

        MatrixTensor<ComplexNumber> conjugated = matrixA.conjugate();

        for (int j = 0; j < DIM; j++) {
            for (int i = 0; i < DIM; i++) {
                TestUtils.assertEquals(matrixA.get(j, i).conjugate(), conjugated.get(i, j));
            }
        }

        assertEquivalent(conjugated, anyA.conjugate());
    }

}
