/*
 * Copyright 1997-2021 Optimatika
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

import java.util.function.Supplier;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Operate2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.structure.Transformation2D;

abstract class Pipeline2D<N extends Comparable<N>, M extends BasicMatrix<N, M>, P extends Pipeline2D<N, M, P>>
        implements Structure2D.Logical<Access2D<N>, P>, Operate2D<N, P>, Supplier<M>, Access2D.Collectable<N, TransformableRegion<N>> {

    private final MatrixFactory<N, M, ?, ?> myFactory;
    private final MatrixStore<N> myStore;
    private final ElementsSupplier<N> mySupplier;

    Pipeline2D(final MatrixFactory<N, M, ?, ?> factory, final ElementsSupplier<N> supplier) {

        super();

        myFactory = factory;

        mySupplier = supplier;

        if (supplier instanceof MatrixStore<?>) {
            myStore = (MatrixStore<N>) supplier;
        } else {
            myStore = null;
        }
    }

    public P above(final Access2D<N>... above) {
        return this.wrap(this.store().above(above));
    }

    public P above(final Access2D<N> above) {
        return this.wrap(this.store().above(above));
    }

    public P above(final long numberOfRows) {
        return this.wrap(this.store().above(numberOfRows));
    }

    public P below(final Access2D<N>... below) {
        return this.wrap(this.store().below(below));
    }

    public P below(final Access2D<N> below) {
        return this.wrap(this.store().below(below));
    }

    public P below(final long numberOfRows) {
        return this.wrap(this.store().below(numberOfRows));
    }

    public P bidiagonal(final boolean upper) {
        return this.wrap(this.store().bidiagonal(upper));
    }

    public P columns(final int[] columns) {
        return this.wrap(this.store().columns(columns));
    }

    public P conjugate() {
        return this.wrap(this.store().conjugate());
    }

    public long countColumns() {
        return mySupplier.countColumns();
    }

    public long countRows() {
        return mySupplier.countRows();
    }

    public P diagonal() {
        return this.wrap(this.store().diagonal());
    }

    public P diagonally(final Access2D<N>... diagonally) {
        return this.wrap(this.store().diagonally(diagonally));
    }

    public M get() {
        return myFactory.instantiate(this.store());
    }

    public P hermitian(final boolean upper) {
        return this.wrap(this.store().hermitian(upper));
    }

    public P hessenberg(final boolean upper) {
        return this.wrap(this.store().hessenberg(upper));
    }

    public P left(final Access2D<N>... left) {
        return this.wrap(this.store().left(left));
    }

    public P left(final Access2D<N> left) {
        return this.wrap(this.store().left(left));
    }

    public P left(final long numberOfColumns) {
        return this.wrap(this.store().left(numberOfColumns));
    }

    public P limits(final long rowLimit, final long columnLimit) {
        return this.wrap(this.store().limits(rowLimit, columnLimit));
    }

    public P offsets(final long rowOffset, final long columnOffset) {
        return this.wrap(this.store().offsets(rowOffset, columnOffset));
    }

    public P onAll(final UnaryFunction<N> operator) {
        return this.wrap(mySupplier.onAll(operator));
    }

    public P onAny(final Transformation2D<N> operator) {
        return this.wrap(mySupplier.onAny(operator));
    }

    public P onColumns(final BinaryFunction<N> operator, final Access1D<N> right) {
        return this.wrap(mySupplier.onColumns(operator, right));
    }

    public P onMatching(final Access2D<N> left, final BinaryFunction<N> operator) {
        return this.wrap(mySupplier.onMatching(left, operator));
    }

    public P onMatching(final BinaryFunction<N> operator, final Access2D<N> right) {
        return this.wrap(mySupplier.onMatching(operator, right));
    }

    public P onRows(final BinaryFunction<N> operator, final Access1D<N> right) {
        return this.wrap(mySupplier.onRows(operator, right));
    }

    public P repeat(final int rowsRepetitions, final int columnsRepetitions) {
        return this.wrap(this.store().repeat(rowsRepetitions, columnsRepetitions));
    }

    public P right(final Access2D<N>... right) {
        return this.wrap(this.store().right(right));
    }

    public P right(final Access2D<N> right) {
        return this.wrap(this.store().right(right));
    }

    public P right(final long numberOfColumns) {
        return this.wrap(this.store().right(numberOfColumns));
    }

    public P rows(final int[] rows) {
        return this.wrap(this.store().rows(rows));
    }

    public P superimpose(final long row, final long col, final Access2D<N> matrix) {
        return this.wrap(this.store().superimpose(row, col, matrix));
    }

    public void supplyTo(final TransformableRegion<N> receiver) {
        mySupplier.supplyTo(receiver);
    }

    public P symmetric(final boolean upper) {
        return this.wrap(this.store().symmetric(upper));
    }

    public P transpose() {
        return this.wrap(this.store().transpose());
    }

    public P triangular(final boolean upper, final boolean assumeOne) {
        return this.wrap(this.store().triangular(upper, assumeOne));
    }

    public P tridiagonal() {
        return this.wrap(this.store().tridiagonal());
    }

    private MatrixStore<N> store() {

        if (myStore != null) {
            return myStore;
        }

        return mySupplier.collect(myFactory.getPhysicalFactory());
    }

    abstract P wrap(ElementsSupplier<N> supplier);

}
