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
package org.ojalgo.matrix.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Structure2D;

public class SparseCase extends NonPhysicalTest {

    static <N extends Comparable<N>> void doTestMultiplication(final SparseStore<N> sparseA, final SparseStore<N> sparseB, final SparseStore<N> sparseC,
            final PhysicalStore.Factory<N, ?> fctory) {

        SparsePerformance.fill(sparseA);
        SparsePerformance.fill(sparseB);
        SparsePerformance.fill(sparseC);

        PhysicalStore<N> denseA = sparseA.copy();
        PhysicalStore<N> denseB = sparseB.copy();
        PhysicalStore<N> denseC = sparseC.copy();

        TestUtils.assertEquals(denseA.multiply(denseB), sparseA.multiply(sparseB));
        TestUtils.assertEquals(denseA.multiply(sparseB), sparseA.multiply(denseB));

        TestUtils.assertEquals(denseB.premultiply(denseA).collect(fctory), sparseB.premultiply(sparseA).collect(fctory));
        TestUtils.assertEquals(denseB.premultiply(sparseA).collect(fctory), sparseB.premultiply(denseA).collect(fctory));

        denseA.multiply(denseB, denseC);
        sparseA.multiply(sparseB, sparseC);
        TestUtils.assertEquals(denseC, sparseC);

        denseA.multiply(sparseB, denseC);
        sparseA.multiply(denseB, sparseC);
        TestUtils.assertEquals(denseC, sparseC);
    }

    @Override
    @BeforeEach
    public void setUp() {

        int dim = Uniform.randomInteger(1, 9);

        rationalStore = SparseStore.RATIONAL.make(dim, dim);
        complexStore = SparseStore.COMPLEX.make(dim, dim);
        primitiveStore = SparseStore.PRIMITIVE64.make(dim, dim);

        for (int ij = 0; ij < dim; ij++) {
            ((SparseStore<?>) rationalStore).set(ij, ij, 1.0);
            ((SparseStore<?>) complexStore).set(ij, ij, 1.0);
            ((SparseStore<?>) primitiveStore).set(ij, ij, 1.0);
        }

        numberOfRows = dim;
        numberOfColumns = dim;
    }

    @Test
    public void testIndexOfLargest() {

        SparseStore<Double> sparseStore = SparseStore.PRIMITIVE64.make(1_000L, 1_000L);

        for (int i = 0; i < 100; i++) {
            long row = Uniform.randomInteger(1_000L);
            long col = Uniform.randomInteger(1_000L);
            sparseStore.set(row, col, Math.random());
        }

        long row = Uniform.randomInteger(1_000L);
        long col = Uniform.randomInteger(1_000L);
        sparseStore.set(row, col, -2.0);

        long expected = Structure2D.index(1_000L, row, col);
        long actual = sparseStore.indexOfLargest();
        TestUtils.assertEquals(expected, actual);
    }

    @Test
    public void testMultiplySparseDenseComplex() {

        SparseStore<ComplexNumber> sparseA = SparseStore.makeComplex(7, 8);
        SparseStore<ComplexNumber> sparseB = SparseStore.makeComplex(8, 9);
        SparseStore<ComplexNumber> sparseC = SparseStore.makeComplex(7, 9);

        SparseCase.doTestMultiplication(sparseA, sparseB, sparseC, GenericStore.COMPLEX);
    }

    @Test
    public void testMultiplySparseDensePrimitive() {

        SparseStore<Double> sparseA = SparseStore.makePrimitive(7, 8);
        SparseStore<Double> sparseB = SparseStore.makePrimitive(8, 9);
        SparseStore<Double> sparseC = SparseStore.makePrimitive(7, 9);

        SparseCase.doTestMultiplication(sparseA, sparseB, sparseC, Primitive64Store.FACTORY);
    }

    @Test
    public void testMultiplySparseDenseQuaternion() {

        SparseStore<Quaternion> sparseA = SparseStore.makeQuaternion(7, 8);
        SparseStore<Quaternion> sparseB = SparseStore.makeQuaternion(8, 9);
        SparseStore<Quaternion> sparseC = SparseStore.makeQuaternion(7, 9);

        SparseCase.doTestMultiplication(sparseA, sparseB, sparseC, GenericStore.QUATERNION);
    }

    @Test
    public void testMultiplySparseDenseRational() {

        SparseStore<RationalNumber> sparseA = SparseStore.makeRational(7, 8);
        SparseStore<RationalNumber> sparseB = SparseStore.makeRational(8, 9);
        SparseStore<RationalNumber> sparseC = SparseStore.makeRational(7, 9);

        SparseCase.doTestMultiplication(sparseA, sparseB, sparseC, GenericStore.RATIONAL);
    }

    @Test
    public void testOneFullColumn() {

        int ind = Uniform.randomInteger(0, 10);

        SparseStore<Double> store = SparseStore.PRIMITIVE64.make(10, 10);
        store.fillColumn(ind, 1.0);

        for (int i = 0; i < 10; i++) {
            double sum = store.aggregateRow(i, Aggregator.SUM);
            double prod = store.aggregateRow(i, Aggregator.PRODUCT);
            TestUtils.assertEquals(1.0, sum);
            TestUtils.assertEquals(0.0, prod);
        }

        for (int j = 0; j < 10; j++) {
            double sum = store.aggregateColumn(j, Aggregator.SUM);
            double prod = store.aggregateColumn(j, Aggregator.PRODUCT);
            if (j == ind) {
                TestUtils.assertEquals(10.0, sum);
                TestUtils.assertEquals(1.0, prod);
            } else {
                TestUtils.assertEquals(0.0, sum);
                TestUtils.assertEquals(0.0, prod);
            }
        }
    }

    @Test
    public void testOneFullRow() {

        int ind = Uniform.randomInteger(0, 10);

        SparseStore<Double> store = SparseStore.PRIMITIVE64.make(10, 10);
        store.fillRow(ind, 1.0);

        for (int i = 0; i < 10; i++) {
            double sum = store.aggregateRow(i, Aggregator.SUM);
            double prod = store.aggregateRow(i, Aggregator.PRODUCT);
            if (i == ind) {
                TestUtils.assertEquals(10.0, sum);
                TestUtils.assertEquals(1.0, prod);
            } else {
                TestUtils.assertEquals(0.0, sum);
                TestUtils.assertEquals(0.0, prod);
            }
        }

        for (int j = 0; j < 10; j++) {
            double sum = store.aggregateColumn(j, Aggregator.SUM);
            double prod = store.aggregateColumn(j, Aggregator.PRODUCT);
            TestUtils.assertEquals(1.0, sum);
            TestUtils.assertEquals(0.0, prod);
        }
    }

}
