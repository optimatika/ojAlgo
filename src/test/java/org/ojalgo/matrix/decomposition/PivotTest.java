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
package org.ojalgo.matrix.decomposition;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;

public class PivotTest extends MatrixDecompositionTests {

    private static int DIM = 7;

    /**
     * Element by element, comparing both the real and the imaginary parts.
     */
    private static void assertSameElements(final MatrixStore<ComplexNumber> expected, final MatrixStore<ComplexNumber> actual) {
        TestUtils.assertEquals(expected.getRowDim(), actual.getRowDim());
        TestUtils.assertEquals(expected.getColDim(), actual.getColDim());
        for (int j = 0; j < expected.getColDim(); j++) {
            for (int i = 0; i < expected.getRowDim(); i++) {
                TestUtils.assertEquals(expected.get(i, j), actual.get(i, j));
            }
        }
    }

    /**
     * Distinct elements with non-zero imaginary parts.
     */
    private static GenericStore<ComplexNumber> makeComplex(final int nbRows, final int nbCols) {
        GenericStore<ComplexNumber> retVal = GenericStore.C128.make(nbRows, nbCols);
        for (int j = 0; j < nbCols; j++) {
            for (int i = 0; i < nbRows; i++) {
                retVal.set(i, j, ComplexNumber.of(i + 10 * j, nbRows - i + 100 * j));
            }
        }
        return retVal;
    }

    private static Pivot makePermutation() {
        Pivot retVal = new Pivot();
        retVal.reset(DIM);
        // Generate random permutation through series of swaps
        for (int i = 0; i < DIM; i++) {
            retVal.change(Uniform.randomInteger(DIM), Uniform.randomInteger(DIM));
        }
        return retVal;
    }

    @Test
    public void testColumnInPivotOrder() {

        Pivot pivot = new Pivot();
        pivot.reset(DIM);

        PhysicalStore<Double> original = R064Store.FACTORY.makeFilled(DIM, 1, Uniform.standard());
        PhysicalStore<Double> actual = original.copy();

        // Generate random permutation through series of swaps
        for (int i = 0; i < DIM; i++) {
            pivot.change(Uniform.randomInteger(DIM), Uniform.randomInteger(DIM));
        }

        pivot.applyPivotOrder(actual);

        // Should work for any valid permutation
        MatrixStore<Double> expected = original.rows(pivot.getOrder());

        TestUtils.assertEquals(expected, actual);
    }

    @Test
    public void testColumnInReverseOrder() {

        Pivot pivot = new Pivot();
        pivot.reset(DIM);

        PhysicalStore<Double> original = R064Store.FACTORY.makeFilled(DIM, 1, Uniform.standard());
        PhysicalStore<Double> actual = original.copy();

        // Generate random permutation through series of swaps
        for (int i = 0; i < DIM; i++) {
            pivot.change(Uniform.randomInteger(DIM), Uniform.randomInteger(DIM));
        }

        pivot.applyReverseOrder(actual);

        // Should work for any valid permutation
        MatrixStore<Double> expected = original.rows(pivot.reverseOrder());

        TestUtils.assertEquals(expected, actual);
    }

    @Test
    public void testComplexColumnInPivotOrder() {

        Pivot pivot = PivotTest.makePermutation();

        GenericStore<ComplexNumber> original = PivotTest.makeComplex(DIM, 1);
        GenericStore<ComplexNumber> actual = original.copy();

        pivot.applyPivotOrder(actual);

        PivotTest.assertSameElements(original.rows(pivot.getOrder()), actual);
    }

    @Test
    public void testComplexMatrixInPivotOrder() {

        Pivot pivot = PivotTest.makePermutation();

        GenericStore<ComplexNumber> original = PivotTest.makeComplex(DIM, 3);
        GenericStore<ComplexNumber> actual = original.copy();

        pivot.applyPivotOrder(actual);

        PivotTest.assertSameElements(original.rows(pivot.getOrder()), actual);
    }

    @Test
    public void testComplexMatrixInReverseOrder() {

        Pivot pivot = PivotTest.makePermutation();

        GenericStore<ComplexNumber> original = PivotTest.makeComplex(DIM, 3);
        GenericStore<ComplexNumber> actual = original.copy();

        pivot.applyReverseOrder(actual);

        PivotTest.assertSameElements(original.rows(pivot.reverseOrder()), actual);
    }

    @Test
    public void testComplexRowInPivotOrder() {

        Pivot pivot = PivotTest.makePermutation();

        GenericStore<ComplexNumber> original = PivotTest.makeComplex(1, DIM);
        GenericStore<ComplexNumber> actual = original.copy();

        pivot.applyPivotOrder(actual);

        PivotTest.assertSameElements(original.columns(pivot.getOrder()), actual);
    }

    @Test
    public void testMatrixInPivotOrder() {

        Pivot pivot = PivotTest.makePermutation();

        PhysicalStore<Double> original = R064Store.FACTORY.makeFilled(DIM, 3, Uniform.standard());
        PhysicalStore<Double> actual = original.copy();

        pivot.applyPivotOrder(actual);

        TestUtils.assertEquals(original.rows(pivot.getOrder()), actual);
    }

    @Test
    public void testMatrixInReverseOrder() {

        Pivot pivot = PivotTest.makePermutation();

        PhysicalStore<Double> original = R064Store.FACTORY.makeFilled(DIM, 3, Uniform.standard());
        PhysicalStore<Double> actual = original.copy();

        pivot.applyReverseOrder(actual);

        TestUtils.assertEquals(original.rows(pivot.reverseOrder()), actual);
    }

    @Test
    public void testNoModification() {

        Pivot pivot = new Pivot();
        pivot.reset(DIM);

        // Create test data
        PhysicalStore<Double> original = R064Store.FACTORY.makeFilled(DIM, 1, Uniform.standard());
        PhysicalStore<Double> actual = original.copy();

        // No pivots applied

        pivot.applyPivotOrder(actual);
        TestUtils.assertEquals(original, actual);

        pivot.applyReverseOrder(actual);
        TestUtils.assertEquals(original, actual);
    }

    @Test
    public void testPivotAndReverse() {

        Pivot pivot = new Pivot();
        pivot.reset(DIM);

        // Random test data
        PhysicalStore<Double> original = R064Store.FACTORY.makeFilled(DIM, 1, Uniform.standard());
        PhysicalStore<Double> transformed = original.copy();

        // Generate random permutation through series of swaps
        for (int i = 0; i < DIM; i++) {
            pivot.change(Uniform.randomInteger(DIM), Uniform.randomInteger(DIM));
        }

        pivot.applyPivotOrder(transformed);
        pivot.applyReverseOrder(transformed);

        // Should get back original values
        TestUtils.assertEquals(original, transformed);
    }

    @Test
    public void testRowInPivotOrder() {

        Pivot pivot = new Pivot();
        pivot.reset(DIM);

        PhysicalStore<Double> original = R064Store.FACTORY.makeFilled(1, DIM, Uniform.standard());
        PhysicalStore<Double> actual = original.copy();

        // Generate random permutation through series of swaps
        for (int i = 0; i < DIM; i++) {
            pivot.change(Uniform.randomInteger(DIM), Uniform.randomInteger(DIM));
        }

        pivot.applyPivotOrder(actual);

        // Should work for any valid permutation
        MatrixStore<Double> expected = original.columns(pivot.getOrder());

        TestUtils.assertEquals(expected, actual);
    }

    @Test
    public void testRowInReverseOrder() {

        Pivot pivot = new Pivot();
        pivot.reset(DIM);

        PhysicalStore<Double> original = R064Store.FACTORY.makeFilled(1, DIM, Uniform.standard());
        PhysicalStore<Double> actual = original.copy();

        // Generate random permutation through series of swaps
        for (int i = 0; i < DIM; i++) {
            pivot.change(Uniform.randomInteger(DIM), Uniform.randomInteger(DIM));
        }

        pivot.applyReverseOrder(actual);

        // Should work for any valid permutation
        MatrixStore<Double> expected = original.columns(pivot.reverseOrder());

        TestUtils.assertEquals(expected, actual);
    }

}
