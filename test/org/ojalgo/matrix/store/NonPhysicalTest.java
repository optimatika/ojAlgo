/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

    protected static NumberContext CNTXT = StandardType.DECIMAL_032;

    private static void testAggregation(final MatrixStore<?> aStore) {

        final PhysicalStore<?> tmpPhysical = aStore.copy();

        Number tmpExpected;
        Number tmpActual;

        for (final Aggregator tmpAggr : Aggregator.values()) {

            tmpExpected = tmpPhysical.aggregateAll(tmpAggr);
            tmpActual = aStore.aggregateAll(tmpAggr);

            if (MatrixStoreTests.DEBUG) {
                BasicLogger.debug("Aggregator={}, Expected/Physical={}, Actual/Logical={}", tmpAggr, tmpExpected, tmpActual);
            }

            TestUtils.assertEquals(tmpExpected, tmpActual, CNTXT);
        }
    }

    private static <N extends Number> void testElements(final MatrixStore<N> aStore) {
        TestUtils.assertEquals(aStore, aStore.copy(), CNTXT);
    }

    private static <N extends Number> void testMultiplication(final MatrixStore<N> aStore) {

        final PhysicalStore<N> tmpPhysical = aStore.copy();

        final int tmpRowDim = (int) aStore.countRows();
        final int tmpColDim = (int) aStore.countColumns();
        final int tmpNewDim = Uniform.randomInteger(1, tmpRowDim + tmpColDim);

        final BasicMatrix tmpLeftMtrx = NonPhysicalTest.makeRandomMatrix(tmpNewDim, tmpRowDim);
        final PhysicalStore<N> tmpLeft = aStore.factory().copy(tmpLeftMtrx);
        // multiplyLeft
        MatrixStore<N> tmpExpected = tmpPhysical.multiplyLeft(tmpLeft);
        MatrixStore<N> tmpActual = aStore.multiplyLeft(tmpLeft);
        TestUtils.assertEquals(tmpExpected, tmpActual, CNTXT);

        tmpExpected = tmpPhysical.multiplyLeft(tmpLeft);
        tmpActual = tmpLeft.multiply(tmpPhysical);
        TestUtils.assertEquals(tmpExpected, tmpActual, CNTXT);

        tmpExpected = aStore.multiplyLeft(tmpLeft);
        tmpActual = tmpLeft.multiply(aStore);
        TestUtils.assertEquals(tmpExpected, tmpActual, CNTXT);

        final BasicMatrix tmpRightMtrx = NonPhysicalTest.makeRandomMatrix(tmpColDim, tmpNewDim);
        final PhysicalStore<N> tmpRight = aStore.factory().copy(tmpRightMtrx);
        // multiplyRight
        tmpExpected = tmpPhysical.multiply(tmpRight);
        tmpActual = aStore.multiply(tmpRight);
        TestUtils.assertEquals(tmpExpected, tmpActual, CNTXT);

        tmpExpected = tmpPhysical.multiply(tmpRight);
        tmpActual = tmpRight.multiplyLeft(tmpPhysical);
        TestUtils.assertEquals(tmpExpected, tmpActual, CNTXT);

        tmpExpected = aStore.multiply(tmpRight);
        tmpActual = tmpRight.multiplyLeft(aStore);
        TestUtils.assertEquals(tmpExpected, tmpActual, CNTXT);
    }

    protected static BasicMatrix makeRandomMatrix(final int aRowDim, final int aColDim) {
        return ComplexMatrix.FACTORY.copy(MatrixUtils.makeRandomComplexStore(aRowDim, aColDim));
    }

    MatrixStore<BigDecimal> myBigStore;
    MatrixStore<ComplexNumber> myComplexStore;
    MatrixStore<Double> myPrimitiveStore;

    public NonPhysicalTest() {
        super();
    }

    public NonPhysicalTest(final String arg0) {
        super(arg0);
    }

    public void testBigAggregator() {
        NonPhysicalTest.testAggregation(myBigStore);
    }

    public void testBigElements() {
        NonPhysicalTest.testElements(myBigStore);
    }

    public void testBigMultiplication() {
        NonPhysicalTest.testMultiplication(myBigStore);
    }

    public void testComplexAggregator() {
        NonPhysicalTest.testAggregation(myComplexStore);
    }

    public void testComplexElements() {
        NonPhysicalTest.testElements(myComplexStore);
    }

    public void testComplexMultiplication() {
        NonPhysicalTest.testMultiplication(myComplexStore);
    }

    public void testPrimitiveAggregator() {
        NonPhysicalTest.testAggregation(myPrimitiveStore);
    }

    public void testPrimitiveElements() {
        NonPhysicalTest.testElements(myPrimitiveStore);
    }

    public void testPrimitiveMultiplication() {
        NonPhysicalTest.testMultiplication(myPrimitiveStore);
    }

    @Override
    protected void tearDown() throws Exception {

        super.tearDown();

        myBigStore = null;
        myComplexStore = null;
        myPrimitiveStore = null;
    }

}
