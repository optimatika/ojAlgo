/*
 * Copyright 1997-2024 Optimatika
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

import org.junit.jupiter.api.Test;
import org.ojalgo.OjAlgoUtils;
import org.ojalgo.TestUtils;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.context.NumberContext;

public class StoreProblems extends MatrixStoreTests {

    static class EyeStore implements MatrixStore<Double> {

        private final int myCountRows, myCountColumns;

        EyeStore(final int countRows, final int countColumns) {
            super();
            myCountRows = countRows;
            myCountColumns = countColumns;
        }

        @Override
        public long countColumns() {
            return myCountColumns;
        }

        @Override
        public long countRows() {
            return myCountRows;
        }

        @Override
        public double doubleValue(final int row, final int col) {
            return row == col ? 1D : 0D;
        }

        @Override
        public int firstInColumn(final int col) {
            return col;
        }

        @Override
        public int firstInRow(final int row) {
            return row;
        }

        @Override
        public Double get(final int row, final int col) {
            return this.doubleValue(row, col);
        }

        @Override
        public int getColDim() {
            return myCountColumns;
        }

        @Override
        public int getRowDim() {
            return myCountRows;
        }

        @Override
        public int limitOfColumn(final int col) {
            return col + 1;
        }

        @Override
        public int limitOfRow(final int row) {
            return row + 1;
        }

        @Override
        public org.ojalgo.matrix.store.PhysicalStore.Factory<Double, ?> physical() {
            return Primitive64Store.FACTORY;
        }

        @Override
        public void supplyTo(final TransformableRegion<Double> receiver) {
            receiver.reset();
            receiver.fillDiagonal(1D);
        }

    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/252 <br>
     * <br>
     * Test that the multiplication logic works when there are more elements that an int can count.
     */
    @Test
    public void testGitHubIssue252() {

        //int c = Integer.MAX_VALUE - 2;
        int c = 50_000;

        for (int m = 1; m < 10; m++) {
            for (int n = 1; n < 10; n++) {
                int min = MissingMath.min(m, c, n);

                MatrixStore<Double> left = new EyeStore(m, c);
                MatrixStore<Double> right = new EyeStore(c, n);

                MatrixStore<Double> denseProduct = left.multiply(right);

                TestUtils.assertEquals(m, denseProduct.countRows());
                TestUtils.assertEquals(n, denseProduct.countColumns());

                TestUtils.assertEquals(min, denseProduct.aggregateAll(Aggregator.SUM).intValue());

                SparseStore<Double> sparseProduct = SparseStore.R064.make(m, n);
                sparseProduct.fillByMultiplying(left, right);

                TestUtils.assertEquals(m, sparseProduct.countRows());
                TestUtils.assertEquals(n, sparseProduct.countColumns());

                TestUtils.assertEquals(min, sparseProduct.aggregateAll(Aggregator.SUM).intValue());
            }
        }
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/330
     */
    @Test
    public void testGitHubIssue330() {

        double[][] data = { { 1.0, 2.0, 3.0, 10.0 }, { 4.0, 5.0, 6.0, 11.0 }, { 7.0, 8.0, 9.0, 11.0 } };

        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        Primitive64Store m = storeFactory.rows(data);
        Primitive64Store r = storeFactory.make(m.countRows(), m.countColumns());

        QR<Double> qr = QR.PRIMITIVE.make(true);
        qr.decompose(m);

        if (DEBUG) {
            BasicLogger.debugMatrix("Original", m);
            BasicLogger.debugMatrix("Q", qr.getQ());
            BasicLogger.debugMatrix("R", qr.getR());
            BasicLogger.debugMatrix("Receiver (before)", r);
        }

        // java.lang.ArrayIndexOutOfBoundsException was thrown here
        qr.getR().supplyTo(r);

        if (DEBUG) {
            BasicLogger.debugMatrix("Receiver (after)", r);
        }

        TestUtils.assertEquals(qr.getR(), r);
    }

    /**
     * Problem with LogicalStore and multi-threading. The MinBatchSize#EXECUTOR was designed to have a fixed
     * number of threads which doesn't work if nested LogicalStores require more. The program would hang. This
     * test makes sure ojAlgo no longer hangs in such a case.
     */
    @Test
    public void testP20071210() {

        MatrixR064 A, Bu, K, sx, currentState;

        final double[][] a = { { 1, 2 }, { 3, 4 } };
        A = MatrixR064.FACTORY.rows(a);
        final double[][] bu = { { 1, 0 }, { 0, 1 } };
        Bu = MatrixR064.FACTORY.rows(bu);
        MatrixR064.FACTORY.makeEye(2, 2);
        K = MatrixR064.FACTORY.makeEye(2, 2);
        final int hp = 2 * OjAlgoUtils.ENVIRONMENT.threads;

        final MatrixR064 eye = MatrixR064.FACTORY.makeEye(A);
        final MatrixR064 Aprime = A.subtract(Bu.multiply(K));
        MatrixR064 Apow = MatrixR064.FACTORY.copy(Aprime);
        final MatrixR064 tmp = Aprime.subtract(eye);
        sx = MatrixR064.FACTORY.copy(eye);
        sx = sx.below(tmp);

        //loop runs hp-2 times, which means the first elements of the matrices must be "hardcoded"
        for (int i = 0; i < hp - 2; i++) {
            sx = sx.below(tmp.multiply(Apow));
            Apow = Apow.multiply(Apow);
        }
        currentState = MatrixR064.FACTORY.make(A.countRows(), 1);
        currentState = currentState.add(1.0);
        sx.multiply(currentState);
    }

    /**
     * Peter Abeles reported a problem with ojAlgo his benchmark's C=A*BT test. The problem turned out be that
     * fillByMultiplying did not reset the destination matrix elements when doung "multiply right".
     */
    @Test
    public void testP20110223() {

        final int tmpDim = 9;

        final PhysicalStore<Double> tmpMtrxA = Primitive64Store.FACTORY.copy(TestUtils.makeRandomComplexStore(tmpDim, tmpDim));
        final PhysicalStore<Double> tmpMtrxB = Primitive64Store.FACTORY.copy(TestUtils.makeRandomComplexStore(tmpDim, tmpDim));
        final PhysicalStore<Double> tmpMtrxC = Primitive64Store.FACTORY.make(tmpDim, tmpDim);

        PhysicalStore<Double> tmpExpected;
        PhysicalStore<Double> tmpActual;

        tmpMtrxC.fillByMultiplying(tmpMtrxA, tmpMtrxB);
        tmpExpected = tmpMtrxC.copy();
        tmpMtrxC.fillByMultiplying(tmpMtrxA, tmpMtrxB);
        tmpActual = tmpMtrxC.copy();
        TestUtils.assertEquals(tmpExpected, tmpActual, NumberContext.of(7, 6));

        tmpMtrxC.fillByMultiplying(tmpMtrxA, tmpMtrxB.transpose());
        tmpExpected = tmpMtrxC.copy();
        tmpMtrxC.fillByMultiplying(tmpMtrxA, tmpMtrxB.transpose());
        tmpActual = tmpMtrxC.copy();
        TestUtils.assertEquals(tmpExpected, tmpActual, NumberContext.of(7, 6));

        tmpMtrxC.fillByMultiplying(tmpMtrxA.transpose(), tmpMtrxB);
        tmpExpected = tmpMtrxC.copy();
        tmpMtrxC.fillByMultiplying(tmpMtrxA.transpose(), tmpMtrxB);
        tmpActual = tmpMtrxC.copy();
        TestUtils.assertEquals(tmpExpected, tmpActual, NumberContext.of(7, 6));
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/55
     */
    @Test
    public void testP20180121() {

        final SparseStore<Double> m = SparseStore.R064.make(3, 2);
        final Primitive64Store mAdd = Primitive64Store.FACTORY.rows(new double[][] { { 1.0, 0.0 }, { 0.0, 0.0 }, { 0.0, 0.0 } });
        final MatrixStore<Double> n = m.add(mAdd);

        final SparseStore<Double> eye = SparseStore.R064.make(2, 2);
        eye.set(0, 0, 1.0);
        eye.set(1, 1, 1.0);

        final MatrixStore<Double> prod = n.multiply(eye);

        TestUtils.assertEquals(mAdd, prod);

        final SparseStore<Double> m2 = SparseStore.R064.make(3, 2);
        m2.set(0, 0, 1.0);

        TestUtils.assertEquals(mAdd, m2.multiply(eye));
        TestUtils.assertEquals(mAdd, eye.premultiply(m2).collect(Primitive64Store.FACTORY));

        final SparseStore<Double> a = SparseStore.R064.make(3, 3);
        a.set(1, 1, 1.0);

        final SparseStore<Double> b = SparseStore.R064.make(3, 5);
        b.set(1, 1, 1.0);
        b.set(0, 3, 1.0);

        final SparseStore<Double> c = SparseStore.R064.make(3, 5);
        c.set(1, 1, 1.0);

        if (DEBUG) {
            BasicLogger.debugMatrix("A", a);
            BasicLogger.debugMatrix("B", b);
            BasicLogger.debugMatrix("C", c);
        }

        TestUtils.assertEquals(c, a.multiply(b));
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/133
     */
    @Test
    public void testTransposeElementsSupplier() {

        double[][] _x = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
        double[][] _y = { { 0, 0, 0 }, { 1, 1, 1 }, { 2, 2, 2 } };
        double[][] exp = { { 1.0, 2.0, 3.0 }, { 3.0, 4.0, 5.0 }, { 5.0, 6.0, 7.0 } };

        Factory<Double, Primitive64Store> factory = Primitive64Store.FACTORY;

        Primitive64Store x = factory.rows(_x);
        Primitive64Store y = factory.rows(_y);

        ElementsSupplier<Double> diff = y.onMatching(x, PrimitiveMath.SUBTRACT);
        ElementsSupplier<Double> transp = diff.transpose();

        TestUtils.assertEquals(factory.rows(exp), diff.collect(factory));
        TestUtils.assertEquals(factory.columns(exp), transp.collect(factory));
    }

}
