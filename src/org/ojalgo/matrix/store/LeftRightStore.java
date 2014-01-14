/*
 * Copyright 1997-2013 Optimatika (www.optimatika.se)
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.scalar.Scalar;

/**
 * A merger of two {@linkplain MatrixStore} instances by placing one store to the right of the other. The two matrices
 * must have the same number of rows. The rows of the two matrices are logically merged to form new longer rows.
 * 
 * @author apete
 */
public final class LeftRightStore<N extends Number> extends DelegatingStore<N> {

    private final int myColSplit;
    private final MatrixStore<N> myRightStore;

    public LeftRightStore(final MatrixStore<N> base, final MatrixStore<N> rightStore) {

        super((int) base.countRows(), (int) (base.countColumns() + rightStore.countColumns()), base);

        myRightStore = rightStore;
        myColSplit = (int) base.countColumns();
    }

    @SuppressWarnings("unused")
    private LeftRightStore(final MatrixStore<N> base) {

        this(base, null);

        ProgrammingError.throwForIllegalInvocation();
    }

    /**
     * @see org.ojalgo.matrix.store.MatrixStore#doubleValue(long, long)
     */
    public double doubleValue(final long row, final long column) {
        return (column >= myColSplit) ? myRightStore.doubleValue(row, column - myColSplit) : this.getBase().doubleValue(row, column);
    }

    public N get(final long row, final long column) {
        return (column >= myColSplit) ? myRightStore.get(row, column - myColSplit) : this.getBase().get(row, column);
    }

    public boolean isLowerLeftShaded() {
        return this.getBase().isLowerLeftShaded();
    }

    public boolean isUpperRightShaded() {
        return false;
    }

    @Override
    public MatrixStore<N> multiplyLeft(final Access1D<N> leftMtrx) {

        final Future<MatrixStore<N>> tmpBaseFuture = this.executeMultiplyLeftOnBase(leftMtrx);

        final MatrixStore<N> tmpRight = myRightStore.multiplyLeft(leftMtrx);

        try {
            return new LeftRightStore<N>(tmpBaseFuture.get(), tmpRight);
        } catch (final InterruptedException | ExecutionException ex) {
            return null;
        }
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return (column >= myColSplit) ? myRightStore.toScalar(row, column - myColSplit) : this.getBase().toScalar(row, column);
    }

}
