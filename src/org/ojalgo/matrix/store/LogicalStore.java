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

import java.util.concurrent.Future;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.concurrent.DaemonPoolExecutor;

/**
 * Logical stores are (intended to be) immutable.
 *
 * @author apete
 */
abstract class LogicalStore<N extends Number> extends AbstractStore<N> {

    private MatrixStore<N> myBase;

    @SuppressWarnings("unused")
    private LogicalStore(final int rowsCount, final int columnsCount) {

        this(null, rowsCount, columnsCount);

        ProgrammingError.throwForIllegalInvocation();
    }

    protected LogicalStore(final MatrixStore<N> base, final int rowsCount, final int columnsCount) {

        super(rowsCount, columnsCount);

        myBase = base;

        if (myBase == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " cannot have a null 'base'!");
        }
    }

    public final PhysicalStore.Factory<N, ?> physical() {
        return myBase.physical();
    }

    protected final Future<?> executeMultiply(final Access1D<N> right, final ElementsConsumer<N> target) {
        return DaemonPoolExecutor.invoke(() -> myBase.multiply(right, target));
    }

    protected final Future<MatrixStore<N>> executeMultiply(final double scalar) {
        return DaemonPoolExecutor.invoke(() -> myBase.multiply(scalar));
    }

    protected final Future<MatrixStore<N>> executeMultiply(final MatrixStore<N> right) {
        return DaemonPoolExecutor.invoke(() -> myBase.multiply(right));
    }

    protected final Future<MatrixStore<N>> executeMultiply(final N scalar) {
        return DaemonPoolExecutor.invoke(() -> myBase.multiply(scalar));
    }

    protected final Future<N> executeMultiplyBoth(final Access1D<N> leftAndRight) {
        return DaemonPoolExecutor.invoke(() -> myBase.multiplyBoth(leftAndRight));
    }

    protected final Future<ElementsSupplier<N>> executePremultiply(final Access1D<N> left) {
        return DaemonPoolExecutor.invoke(() -> myBase.premultiply(left));
    }

    protected final MatrixStore<N> getBase() {
        return myBase;
    }

}
