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

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Supplier2D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;

/**
 * An elements supplier is not (yet) a matrix. There are 3 things you can do with them:
 * <ol>
 * <li>You can query the size/shape of the (future) matrix</li>
 * <li>You can get that matrix</li>
 * <li>You can supply the elements to an already existing matrix (or more precisely to an
 * {@linkplain ElementsConsumer})</li>
 * </ol>
 *
 * @author apete
 */
public interface ElementsSupplier<N extends Number> extends Supplier2D<MatrixStore<N>> {

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

    PhysicalStore.Factory<N, ?> factory();

    default MatrixStore<N> get() {

        final PhysicalStore<N> retVal = this.factory().makeZero(this.countRows(), this.countColumns());

        this.supplyTo(retVal);

        return retVal;
    }

    void supplyTo(final ElementsConsumer<N> consumer);

    default ElementsSupplier<N> transpose() {

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
                ElementsSupplier.this.supplyTo(new ElementsConsumer<N>() {

                    public void add(final long row, final long column, final double addend) {
                        consumer.add(column, row, addend);
                    }

                    public void add(final long row, final long column, final Number addend) {
                        consumer.add(column, row, addend);
                    }

                    public long countColumns() {
                        return consumer.countRows();
                    }

                    public long countRows() {
                        return consumer.countColumns();
                    }

                    public void fillByMultiplying(final Access1D<N> left, final Access1D<N> right) {
                        consumer.fillByMultiplying(left, right);
                    }

                    public void fillColumn(final long row, final long column, final N value) {
                        consumer.fillRow(column, row, value);
                    }

                    public void fillColumn(final long row, final long column, final NullaryFunction<N> supplier) {
                        consumer.fillRow(column, row, supplier);
                    }

                    public void fillDiagonal(final long row, final long column, final N value) {
                        consumer.fillDiagonal(column, row, value);
                    }

                    public void fillDiagonal(final long row, final long column, final NullaryFunction<N> supplier) {
                        consumer.fillRow(column, row, supplier);
                    }

                    public void fillOne(final long row, final long column, final N value) {
                        consumer.fillOne(column, row, value);
                    }

                    public void fillOne(final long row, final long column, final NullaryFunction<N> supplier) {
                        consumer.fillOne(column, row, supplier);
                    }

                    public void fillOneMatching(final long row, final long column, final Access1D<?> values, final long valueIndex) {
                        consumer.fillOneMatching(column, row, values, valueIndex);
                    }

                    public void fillRow(final long row, final long column, final N value) {
                        consumer.fillDiagonal(column, row, value);
                    }

                    public void fillRow(final long row, final long column, final NullaryFunction<N> supplier) {
                        consumer.fillDiagonal(column, row, supplier);
                    }

                    public void modifyColumn(final long row, final long column, final UnaryFunction<N> function) {
                        consumer.modifyRow(column, row, function);
                    }

                    public void modifyDiagonal(final long row, final long column, final UnaryFunction<N> function) {
                        consumer.modifyDiagonal(column, row, function);
                    }

                    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
                        consumer.modifyMatching(left, function);
                    }

                    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
                        consumer.modifyMatching(function, right);
                    }

                    public void modifyOne(final long row, final long column, final UnaryFunction<N> function) {
                        consumer.modifyOne(column, row, function);
                    }

                    public void modifyRow(final long row, final long column, final UnaryFunction<N> function) {
                        consumer.modifyColumn(column, row, function);
                    }

                    public ElementsConsumer<N> regionByColumns(final int... columns) {
                        return consumer.regionByRows(columns);
                    }

                    public ElementsConsumer<N> regionByLimits(final int rowLimit, final int columnLimit) {
                        return consumer.regionByLimits(columnLimit, rowLimit);
                    }

                    public ElementsConsumer<N> regionByOffsets(final int rowOffset, final int columnOffset) {
                        return consumer.regionByOffsets(columnOffset, rowOffset);
                    }

                    public ElementsConsumer<N> regionByRows(final int... rows) {
                        return consumer.regionByColumns(rows);
                    }

                    public void set(final long row, final long column, final double value) {
                        consumer.set(column, row, value);
                    }

                    public void set(final long row, final long column, final Number value) {
                        consumer.set(column, row, value);
                    }

                });
            }

        };

    }

}
