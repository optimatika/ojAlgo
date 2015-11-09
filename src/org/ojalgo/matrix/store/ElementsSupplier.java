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

import org.ojalgo.access.Supplier2D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;

/**
 * An elements supplier is not (yet) a matrix. There are 4 things you can do with them:
 * <ol>
 * <li>You can query the size/shape of the (future) matrix</li>
 * <li>You can get that matrix</li>
 * <li>You can supply the elements to an already existing matrix (or more precisely to an
 * {@linkplain ElementsConsumer})</li>
 * <li>You can also define additional, chained, operations to be executed when the elements are extracted.
 * </li>
 * </ol>
 *
 * @author apete
 */
public interface ElementsSupplier<N extends Number> extends Supplier2D<MatrixStore<N>> {

    PhysicalStore.Factory<N, ?> factory();

    default MatrixStore<N> get() {

        final PhysicalStore<N> retVal = this.factory().makeZero(this.countRows(), this.countColumns());

        this.supplyTo(retVal);

        return retVal;
    }

    default ElementsSupplier<N> operateOnAll(final UnaryFunction<N> operator) {

        return new ContextSupplier<N>(this) {

            public long count() {
                return ElementsSupplier.this.count();
            }

            public long countColumns() {
                return ElementsSupplier.this.countColumns();
            }

            public long countRows() {
                return ElementsSupplier.this.countRows();
            }

            @Override
            public void supplyTo(final ElementsConsumer<N> consumer) {
                ElementsSupplier.this.supplyTo(consumer);
                consumer.modifyAll(operator);
            }

        };

    }

    default ElementsSupplier<N> operateOnMatching(final BinaryFunction<N> operator, final MatrixStore<N> right) {

        return new ContextSupplier<N>(this) {

            public long count() {
                return ElementsSupplier.this.count();
            }

            public long countColumns() {
                return ElementsSupplier.this.countColumns();
            }

            public long countRows() {
                return ElementsSupplier.this.countRows();
            }

            @Override
            public void supplyTo(final ElementsConsumer<N> consumer) {
                ElementsSupplier.this.supplyTo(consumer);
                consumer.modifyMatching(operator, right);
            }

        };

    }

    default ElementsSupplier<N> operateOnMatching(final MatrixStore<N> left, final BinaryFunction<N> operator) {

        return new ContextSupplier<N>(this) {

            public long count() {
                return ElementsSupplier.this.count();
            }

            public long countColumns() {
                return ElementsSupplier.this.countColumns();
            }

            public long countRows() {
                return ElementsSupplier.this.countRows();
            }

            @Override
            public void supplyTo(final ElementsConsumer<N> consumer) {
                ElementsSupplier.this.supplyTo(consumer);
                consumer.modifyMatching(left, operator);
            }

        };

    }

    void supplyTo(final ElementsConsumer<N> consumer);

    default ElementsSupplier<N> transpose() {

        return new ContextSupplier<N>(this) {

            public long count() {
                return ElementsSupplier.this.count();
            }

            public long countColumns() {
                return ElementsSupplier.this.countRows();
            }

            public long countRows() {
                return ElementsSupplier.this.countColumns();
            }

            public MatrixStore<N> get() {

                final PhysicalStore<N> retVal = this.factory().makeZero(ElementsSupplier.this.countRows(), ElementsSupplier.this.countColumns());

                this.supplyTo(retVal);

                return retVal;
            }

            public ElementsSupplier<N> operateOnAll(final UnaryFunction<N> operator) {
                return ElementsSupplier.this.operateOnAll(operator);
            }

            public ElementsSupplier<N> operateOnMatching(final BinaryFunction<N> operator, final MatrixStore<N> right) {
                return ElementsSupplier.this.operateOnMatching(operator, right.transpose());
            }

            public ElementsSupplier<N> operateOnMatching(final MatrixStore<N> left, final BinaryFunction<N> operator) {
                return ElementsSupplier.this.operateOnMatching(left.transpose(), operator);
            }

            @Override
            public void supplyTo(final ElementsConsumer<N> consumer) {
                ElementsSupplier.this.supplyTo(consumer.regionByTransposing());
            }

            public ElementsSupplier<N> transpose() {
                return ElementsSupplier.this;
            }

        };

    }

}
