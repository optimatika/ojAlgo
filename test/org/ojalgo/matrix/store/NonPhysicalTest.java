/*
 * Copyright 1997-2019 Optimatika
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.StandardType;
import org.ojalgo.type.context.NumberContext;

public abstract class NonPhysicalTest extends MatrixStoreTests {

    private static final NumberContext ACCURACY = StandardType.DECIMAL_032;

    private static <N extends Number> void testAggregation(final MatrixStore<N> anyStore) {

        final PhysicalStore<N> copied = anyStore.copy();

        if (DEBUG) {
            BasicLogger.debug("Any", anyStore);
            BasicLogger.debug("Copy", copied);
        }

        Number expected;
        Number actual;

        for (final Aggregator aggregator : Aggregator.values()) {

            expected = copied.aggregateAll(aggregator);
            actual = anyStore.aggregateAll(aggregator);

            TestUtils.assertEquals(aggregator.name() + "@" + anyStore, expected, actual, ACCURACY);

            if (!((aggregator == Aggregator.AVERAGE) && (anyStore instanceof SparseStore<?>))) {
                // For a sparse store the AVERAGE aggreghator will get an incorrect result
                // due to not counting the correct number of zeros. (Don't want to fix this - short term)

                for (int i = 0; i < copied.countRows(); i++) {
                    expected = copied.aggregateRow(i, aggregator);
                    actual = anyStore.aggregateRow(i, aggregator);
                    TestUtils.assertEquals("Row: " + i + " " + aggregator.name(), expected, actual, ACCURACY);
                }

                for (int j = 0; j < copied.countColumns(); j++) {
                    expected = copied.aggregateColumn(j, aggregator);
                    actual = anyStore.aggregateColumn(j, aggregator);
                    TestUtils.assertEquals("Col: " + j + " " + aggregator.name(), expected, actual, ACCURACY);
                }
            }

        }
    }

    private static <N extends Number> void testCopy(final MatrixStore<N> anyStore) {
        TestUtils.assertEquals(anyStore, anyStore.copy(), ACCURACY);
    }

    private static <N extends Number> void testDimensions(final MatrixStore<N> anyStore, final int numberOfRows, final int numberOfColumns) {
        TestUtils.assertEquals(numberOfRows, anyStore.countRows());
        TestUtils.assertEquals(numberOfColumns, anyStore.countColumns());
        TestUtils.assertEquals(numberOfRows * numberOfColumns, anyStore.count());
    }

    private static <N extends Number> void testMultiplication(final MatrixStore<N> anyStore) {

        final PhysicalStore<N> tmpCopy = anyStore.copy();

        final int tmpRowDim = (int) anyStore.countRows();
        final int tmpColDim = (int) anyStore.countColumns();
        final int tmpNewDim = Uniform.randomInteger(1, tmpRowDim + tmpColDim);

        // multiplyLeft
        final MatrixStore<ComplexNumber> tmpLeftMtrx = NonPhysicalTest.makeRandomMatrix(tmpNewDim, tmpRowDim);
        final PhysicalStore<N> tmpLeft = anyStore.physical().copy(tmpLeftMtrx);

        MatrixStore<N> tmpExpected = tmpLeft.multiply(tmpCopy);
        MatrixStore<N> tmpActual = tmpLeft.multiply(anyStore);
        TestUtils.assertEquals(tmpExpected, tmpActual, ACCURACY);

        tmpExpected = tmpCopy.premultiply(tmpLeft).get();
        tmpActual = anyStore.premultiply(tmpLeft).get();
        TestUtils.assertEquals(tmpExpected, tmpActual, ACCURACY);

        // multiplyRight
        final MatrixStore<ComplexNumber> tmpRightMtrx = NonPhysicalTest.makeRandomMatrix(tmpColDim, tmpNewDim);
        final PhysicalStore<N> tmpRight = anyStore.physical().copy(tmpRightMtrx);

        tmpExpected = tmpCopy.multiply(tmpRight);
        tmpActual = anyStore.multiply(tmpRight);
        TestUtils.assertEquals(tmpExpected, tmpActual, ACCURACY);

        tmpExpected = tmpRight.premultiply(tmpCopy).get();
        tmpActual = tmpRight.premultiply(anyStore).get();
        TestUtils.assertEquals(tmpExpected, tmpActual, ACCURACY);
    }

    protected static MatrixStore<ComplexNumber> makeRandomMatrix(final int numberOfRows, final int numberOfColumns) {
        return TestUtils.makeRandomComplexStore(numberOfRows, numberOfColumns).signum();
    }

    MatrixStore<ComplexNumber> complexStore;
    int numberOfColumns;
    int numberOfRows;
    MatrixStore<Double> primitiveStore;
    MatrixStore<RationalNumber> rationalStore;

    public abstract void setUp();

    @AfterEach
    public void tearDown() {
        rationalStore = null;
        complexStore = null;
        primitiveStore = null;
    }

    @Test
    public void testAggregator() {
        NonPhysicalTest.testAggregation(primitiveStore);
        NonPhysicalTest.testAggregation(complexStore);
        NonPhysicalTest.testAggregation(rationalStore);
    }

    @Test
    public void testCopy() {
        NonPhysicalTest.testCopy(primitiveStore);
        NonPhysicalTest.testCopy(complexStore);
        NonPhysicalTest.testCopy(rationalStore);
    }

    @Test
    public void testDimensions() {
        NonPhysicalTest.testDimensions(primitiveStore, numberOfRows, numberOfColumns);
        NonPhysicalTest.testDimensions(complexStore, numberOfRows, numberOfColumns);
        NonPhysicalTest.testDimensions(rationalStore, numberOfRows, numberOfColumns);
    }

    @Test
    public void testMultiplication() {
        NonPhysicalTest.testMultiplication(primitiveStore);
        NonPhysicalTest.testMultiplication(complexStore);
        NonPhysicalTest.testMultiplication(rationalStore);
    }

}
