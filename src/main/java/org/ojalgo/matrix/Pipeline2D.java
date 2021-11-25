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
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Operate2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.structure.Transformation2D;

public class Pipeline2D<N extends Comparable<N>, M extends BasicMatrix<N, M>>
        implements Structure2D.Logical<Access2D<N>, Pipeline2D<N, M>>, Operate2D<N, Pipeline2D<N, M>>, Supplier<M> {

    private final MatrixFactory<N, M, ?, ?, ?> myFactory;
    private final MatrixStore<N> myStore;
    private final ElementsSupplier<N> mySupplier;

    Pipeline2D(final MatrixFactory<N, M, ?, ?, ?> factory, final ElementsSupplier<N> supplier) {

        super();

        myFactory = factory;

        mySupplier = supplier;

        if (supplier instanceof MatrixStore<?>) {
            myStore = (MatrixStore<N>) supplier;
        } else {
            myStore = null;
        }
    }

    public Pipeline2D<N, M> above(final Access2D<N>... above) {
        return new Pipeline2D<>(myFactory, this.store().above(above));
    }

    public Pipeline2D<N, M> above(final Access2D<N> above) {
        return new Pipeline2D<>(myFactory, this.store().above(above));
    }

    public Pipeline2D<N, M> above(final long numberOfRows) {
        return new Pipeline2D<>(myFactory, this.store().above(numberOfRows));
    }

    public Pipeline2D<N, M> below(final Access2D<N>... below) {
        return new Pipeline2D<>(myFactory, this.store().below(below));
    }

    public Pipeline2D<N, M> below(final Access2D<N> below) {
        return new Pipeline2D<>(myFactory, this.store().below(below));
    }

    public Pipeline2D<N, M> below(final long numberOfRows) {
        return new Pipeline2D<>(myFactory, this.store().below(numberOfRows));
    }

    public Pipeline2D<N, M> bidiagonal(final boolean upper) {
        return new Pipeline2D<>(myFactory, this.store().bidiagonal(upper));
    }

    public Pipeline2D<N, M> columns(final int[] columns) {
        return new Pipeline2D<>(myFactory, this.store().columns(columns));
    }

    public Pipeline2D<N, M> conjugate() {
        return new Pipeline2D<>(myFactory, this.store().conjugate());
    }

    public long countColumns() {
        return mySupplier.countColumns();
    }

    public long countRows() {
        return mySupplier.countRows();
    }

    public Pipeline2D<N, M> diagonal() {
        return new Pipeline2D<>(myFactory, this.store().diagonal());
    }

    public Pipeline2D<N, M> diagonally(final Access2D<N>... diagonally) {
        return new Pipeline2D<>(myFactory, this.store().diagonally(diagonally));
    }

    public M get() {
        return myFactory.instantiate(this.store());
    }

    public Pipeline2D<N, M> hermitian(final boolean upper) {
        return new Pipeline2D<>(myFactory, this.store().hermitian(upper));
    }

    public Pipeline2D<N, M> hessenberg(final boolean upper) {
        return new Pipeline2D<>(myFactory, this.store().hessenberg(upper));
    }

    public Pipeline2D<N, M> left(final Access2D<N>... left) {
        return new Pipeline2D<>(myFactory, this.store().left(left));
    }

    public Pipeline2D<N, M> left(final Access2D<N> left) {
        return new Pipeline2D<>(myFactory, this.store().left(left));
    }

    public Pipeline2D<N, M> left(final long numberOfColumns) {
        return new Pipeline2D<>(myFactory, this.store().left(numberOfColumns));
    }

    public Pipeline2D<N, M> limits(final long rowLimit, final long columnLimit) {
        return new Pipeline2D<>(myFactory, this.store().limits(rowLimit, columnLimit));
    }

    public Pipeline2D<N, M> offsets(final long rowOffset, final long columnOffset) {
        return new Pipeline2D<>(myFactory, this.store().offsets(rowOffset, columnOffset));
    }

    public Pipeline2D<N, M> onAll(final UnaryFunction<N> operator) {
        return new Pipeline2D<>(myFactory, mySupplier.onAll(operator));
    }

    public Pipeline2D<N, M> onAny(final Transformation2D<N> operator) {
        return new Pipeline2D<>(myFactory, mySupplier.onAny(operator));
    }

    public Pipeline2D<N, M> onColumns(final BinaryFunction<N> operator, final Access1D<N> right) {
        return new Pipeline2D<>(myFactory, mySupplier.onColumns(operator, right));
    }

    public Pipeline2D<N, M> onMatching(final Access2D<N> left, final BinaryFunction<N> operator) {
        return new Pipeline2D<>(myFactory, mySupplier.onMatching(left, operator));
    }

    public Pipeline2D<N, M> onMatching(final BinaryFunction<N> operator, final Access2D<N> right) {
        return new Pipeline2D<>(myFactory, mySupplier.onMatching(operator, right));
    }

    public Pipeline2D<N, M> onRows(final BinaryFunction<N> operator, final Access1D<N> right) {
        return new Pipeline2D<>(myFactory, mySupplier.onRows(operator, right));
    }

    public Pipeline2D<N, M> repeat(final int rowsRepetitions, final int columnsRepetitions) {
        return new Pipeline2D<>(myFactory, this.store().repeat(rowsRepetitions, columnsRepetitions));
    }

    public Pipeline2D<N, M> right(final Access2D<N>... right) {
        return new Pipeline2D<>(myFactory, this.store().right(right));
    }

    public Pipeline2D<N, M> right(final Access2D<N> right) {
        return new Pipeline2D<>(myFactory, this.store().right(right));
    }

    public Pipeline2D<N, M> right(final long numberOfColumns) {
        return new Pipeline2D<>(myFactory, this.store().right(numberOfColumns));
    }

    public Pipeline2D<N, M> rows(final int[] rows) {
        return new Pipeline2D<>(myFactory, this.store().rows(rows));
    }

    public Pipeline2D<N, M> superimpose(final long row, final long col, final Access2D<N> matrix) {
        return new Pipeline2D<>(myFactory, this.store().superimpose(row, col, matrix));
    }

    public Pipeline2D<N, M> symmetric(final boolean upper) {
        return new Pipeline2D<>(myFactory, this.store().symmetric(upper));
    }

    public Pipeline2D<N, M> transpose() {
        return new Pipeline2D<>(myFactory, this.store().transpose());
    }

    public Pipeline2D<N, M> triangular(final boolean upper, final boolean assumeOne) {
        return new Pipeline2D<>(myFactory, this.store().triangular(upper, assumeOne));
    }

    public Pipeline2D<N, M> tridiagonal() {
        return new Pipeline2D<>(myFactory, this.store().tridiagonal());
    }

    MatrixStore<N> store() {

        if (myStore != null) {
            return myStore;
        }

        return mySupplier.collect(myFactory.getPhysicalFactory());
    }

}
