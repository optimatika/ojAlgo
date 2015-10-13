/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.ojalgo.access.Access1D;
import org.ojalgo.concurrent.DaemonPoolExecutor;

abstract class DelegatingStore<N extends Number> extends LogicalStore<N> {

    private static final class MultiplyLeft<N extends Number> implements Callable<MatrixStore<N>> {

        private Access1D<N> myLeftStore;
        private MatrixStore<N> myThisStore;

        public MultiplyLeft(final MatrixStore<N> thisStore, final Access1D<N> leftStore) {

            super();

            myThisStore = thisStore;
            myLeftStore = leftStore;
        }

        @SuppressWarnings("unused")
        private MultiplyLeft() {
            this(null, null);
        }

        public MatrixStore<N> call() throws Exception {
            return ((MatrixStore<N>) myLeftStore).multiply(myThisStore);
        }

    }

    private static final class MultiplyRight<N extends Number> implements Callable<MatrixStore<N>> {

        private Access1D<N> myRightStore;
        private MatrixStore<N> myThisStore;

        public MultiplyRight(final MatrixStore<N> thisStore, final Access1D<N> rightStore) {

            super();

            myThisStore = thisStore;
            myRightStore = rightStore;
        }

        @SuppressWarnings("unused")
        private MultiplyRight() {
            this(null, null);
        }

        public MatrixStore<N> call() throws Exception {
            return myThisStore.multiply(myRightStore);
        }

    }

    protected DelegatingStore(final int rowsCount, final int columnsCount, final MatrixStore<N> base) {
        super(base, rowsCount, columnsCount);
    }

    @Override
    public void supplyTo(final ElementsConsumer<N> consumer) {
        this.supplyNonZerosTo(consumer);
    }

    protected final Future<MatrixStore<N>> executeMultiplyLeftOnBase(final Access1D<N> left) {
        return DaemonPoolExecutor.invoke(new MultiplyLeft<N>(this.getBase(), left));
    }

    protected final Future<MatrixStore<N>> executeMultiplyRightOnBase(final Access1D<N> right) {
        return DaemonPoolExecutor.invoke(new MultiplyRight<N>(this.getBase(), right));
    }

}
