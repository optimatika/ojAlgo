/*
 * Copyright 1997-2025 Optimatika
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

import org.ojalgo.concurrent.DaemonPoolExecutor;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;

/**
 * Logical stores are (intended to be) immutable.
 *
 * @author apete
 */
abstract class LogicalStore<N extends Comparable<N>> extends AbstractStore<N> {

    private final Scalar<N> myOne;
    private final Scalar<N> myZero;
    final MatrixStore<N> base;

    protected LogicalStore(final MatrixStore<N> target, final int rowsCount, final int columnsCount) {

        super(rowsCount, columnsCount);

        base = target;

        myZero = target.physical().scalar().zero();
        myOne = target.physical().scalar().one();
    }

    protected LogicalStore(final MatrixStore<N> target, final long rowsCount, final long columnsCount) {
        this(target, Math.toIntExact(rowsCount), Math.toIntExact(columnsCount));
    }

    @Override
    public final PhysicalStore.Factory<N, ?> physical() {
        return base.physical();
    }

    protected final Future<?> executeMultiply(final Access1D<N> right, final TransformableRegion<N> target) {
        return DaemonPoolExecutor.invoke(() -> base.multiply(right, target));
    }

    protected final Future<MatrixStore<N>> executeMultiply(final double scalar) {
        return DaemonPoolExecutor.invoke(() -> base.multiply(scalar));
    }

    protected final Future<MatrixStore<N>> executeMultiply(final MatrixStore<N> right) {
        return DaemonPoolExecutor.invoke(() -> base.multiply(right));
    }

    protected final Future<MatrixStore<N>> executeMultiply(final N scalar) {
        return DaemonPoolExecutor.invoke(() -> base.multiply(scalar));
    }

    protected final Future<N> executeMultiplyBoth(final Access1D<N> leftAndRight) {
        return DaemonPoolExecutor.invoke(() -> base.multiplyBoth(leftAndRight));
    }

    protected final Future<ElementsSupplier<N>> executePremultiply(final Access1D<N> left) {
        return DaemonPoolExecutor.invoke(() -> base.premultiply(left));
    }

    final Scalar<N> one() {
        return myOne;
    }

    final Scalar<N> zero() {
        return myZero;
    }

}
