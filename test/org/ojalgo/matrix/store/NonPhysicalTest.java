/*
 * Copyright 1997-2018 Optimatika
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

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.ComplexMatrix;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.StandardType;
import org.ojalgo.type.context.NumberContext;

public abstract class NonPhysicalTest extends AbstractMatrixStoreTest {

    private static NumberContext CNTXT = StandardType.DECIMAL_032;

    private static void testAggregation(final MatrixStore<?> anyStore) {

        final PhysicalStore<?> copied = anyStore.copy();

        Number tmpExpected;
        Number tmpActual;

        for (final Aggregator tmpAggr : Aggregator.values()) {

            tmpExpected = copied.aggregateAll(tmpAggr);
            tmpActual = anyStore.aggregateAll(tmpAggr);

            if (MatrixStoreTests.DEBUG) {
                BasicLogger.debug("Aggregator={}, Expected/Physical={}, Actual/Logical={}", tmpAggr, tmpExpected, tmpActual);
            }

            TestUtils.assertEquals(tmpAggr.name(), tmpExpected, tmpActual, CNTXT);
        }
    }

    private static <N extends Number> void testElements(final MatrixStore<N> aStore) {
        TestUtils.assertEquals(aStore, aStore.copy(), CNTXT);
    }

    private static <N extends Number> void testMultiplication(final MatrixStore<N> matrixStore) {

        final PhysicalStore<N> tmpCopy = matrixStore.copy();

        final int tmpRowDim = (int) matrixStore.countRows();
        final int tmpColDim = (int) matrixStore.countColumns();
        final int tmpNewDim = Uniform.randomInteger(1, tmpRowDim + tmpColDim);

        // multiplyLeft
        final BasicMatrix tmpLeftMtrx = NonPhysicalTest.makeRandomMatrix(tmpNewDim, tmpRowDim);
        final PhysicalStore<N> tmpLeft = matrixStore.physical().copy(tmpLeftMtrx);

        MatrixStore<N> tmpExpected = tmpLeft.multiply(tmpCopy);
        MatrixStore<N> tmpActual = tmpLeft.multiply(matrixStore);
        TestUtils.assertEquals(tmpExpected, tmpActual, CNTXT);

        tmpExpected = tmpCopy.premultiply(tmpLeft).get();
        tmpActual = matrixStore.premultiply(tmpLeft).get();
        TestUtils.assertEquals(tmpExpected, tmpActual, CNTXT);

        // multiplyRight
        final BasicMatrix tmpRightMtrx = NonPhysicalTest.makeRandomMatrix(tmpColDim, tmpNewDim);
        final PhysicalStore<N> tmpRight = matrixStore.physical().copy(tmpRightMtrx);

        tmpExpected = tmpCopy.multiply(tmpRight);
        tmpActual = matrixStore.multiply(tmpRight);
        TestUtils.assertEquals(tmpExpected, tmpActual, CNTXT);

        tmpExpected = tmpRight.premultiply(tmpCopy).get();
        tmpActual = tmpRight.premultiply(matrixStore).get();
        TestUtils.assertEquals(tmpExpected, tmpActual, CNTXT);
    }

    protected static BasicMatrix makeRandomMatrix(final int aRowDim, final int aColDim) {
        return ComplexMatrix.FACTORY.copy(MatrixUtils.makeRandomComplexStore(aRowDim, aColDim));
    }

    MatrixStore<BigDecimal> myBigStore;
    MatrixStore<ComplexNumber> myComplexStore;
    MatrixStore<Double> myPrimitiveStore;

    @AfterEach
    public void tearDown() {
        myBigStore = null;
        myComplexStore = null;
        myPrimitiveStore = null;
    }

    @Test
    public void testBigAggregator() {
        NonPhysicalTest.testAggregation(myBigStore);
    }

    @Test
    public void testBigElements() {
        NonPhysicalTest.testElements(myBigStore);
    }

    @Test
    public void testBigMultiplication() {
        NonPhysicalTest.testMultiplication(myBigStore);
    }

    @Test
    public void testComplexAggregator() {
        NonPhysicalTest.testAggregation(myComplexStore);
    }

    @Test
    public void testComplexElements() {
        NonPhysicalTest.testElements(myComplexStore);
    }

    @Test
    public void testComplexMultiplication() {
        NonPhysicalTest.testMultiplication(myComplexStore);
    }

    @Test
    public void testPrimitiveAggregator() {
        NonPhysicalTest.testAggregation(myPrimitiveStore);
    }

    @Test
    public void testPrimitiveElements() {
        NonPhysicalTest.testElements(myPrimitiveStore);
    }

    @Test
    public void testPrimitiveMultiplication() {
        NonPhysicalTest.testMultiplication(myPrimitiveStore);
    }

}
