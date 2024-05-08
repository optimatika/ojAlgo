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
package org.ojalgo.matrix;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.structure.Factory2D;

abstract class SparseMutator2D<N extends Comparable<N>, M extends BasicMatrix<N, M>> implements Factory2D.Builder<M> {

    private final SparseStore.Builder<N> myDelegate;
    private boolean mySafe = true;

    SparseMutator2D(final SparseStore.Builder<N> delegate) {

        super();

        myDelegate = delegate;
    }

    @Override
    public M build() {
        mySafe = false;
        return this.instantiate(myDelegate.build());
    }

    @Override
    public long count() {
        return myDelegate.count();
    }

    @Override
    public long countColumns() {
        return myDelegate.countColumns();
    }

    @Override
    public long countRows() {
        return myDelegate.countRows();
    }

    @Override
    public int getColDim() {
        return myDelegate.getColDim();
    }

    @Override
    public int getRowDim() {
        return myDelegate.getRowDim();
    }

    @Override
    public void reset() {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.reset();
    }

    @Override
    public void set(final int row, final int col, final double value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.set(row, col, value);
    }

    @Override
    public void set(final long index, final double value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long row, final long col, final Comparable<?> value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.set(row, col, value);
    }

    @Override
    public int size() {
        return myDelegate.size();
    }

    abstract M instantiate(MatrixStore<N> store);

}
