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
package org.ojalgo.access;

import java.util.function.Consumer;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;

/**
 * 2-dimensional mutator methods
 *
 * @author apete
 */
public interface Mutate2D extends Structure2D, Mutate1D {

    interface BiModifiable<N extends Number> extends Structure2D, Mutate1D.BiModifiable<N> {

    }

    /**
     * A few operations with no 1D or AnyD counterpart.
     *
     * @author apete
     */
    interface Exchangeable extends Structure2D {

        void exchangeColumns(final long colA, final long colB);

        void exchangeRows(final long rowA, final long rowB);

    }

    interface Fillable<N extends Number> extends Structure2D, Mutate1D.Fillable<N> {

        default void fillColumn(final long col, final Access1D<N> values) {
            this.fillColumn(0L, col, values);
        }

        default void fillColumn(final long row, final long col, final Access1D<N> values) {
            this.loopColumn(row, col, (r, c) -> this.fillOne(r, c, values.get(r - row)));
        }

        default void fillColumn(final long row, final long col, final N value) {
            this.loopColumn(row, col, (r, c) -> this.fillOne(r, c, value));
        }

        default void fillColumn(final long row, final long col, final NullaryFunction<N> supplier) {
            this.loopColumn(row, col, (r, c) -> this.fillOne(r, c, supplier));
        }

        default void fillColumn(final long col, final N value) {
            this.fillColumn(0L, col, value);
        }

        default void fillColumn(final long col, final NullaryFunction<N> supplier) {
            this.fillColumn(0L, col, supplier);
        }

        default void fillDiagonal(final Access1D<N> values) {
            this.fillDiagonal(0L, 0L, values);
        }

        default void fillDiagonal(final long row, final long col, final Access1D<N> values) {
            this.loopDiagonal(row, col, (r, c) -> this.fillOne(r, c, values.get(r - row)));
        }

        default void fillDiagonal(final long row, final long col, final N value) {
            this.loopDiagonal(row, col, (r, c) -> this.fillOne(r, c, value));
        }

        default void fillDiagonal(final long row, final long col, final NullaryFunction<N> supplier) {
            this.loopDiagonal(row, col, (r, c) -> this.fillOne(r, c, supplier));
        }

        default void fillDiagonal(final N value) {
            this.fillDiagonal(0L, 0L, value);
        }

        default void fillDiagonal(final NullaryFunction<N> supplier) {
            this.fillDiagonal(0L, 0L, supplier);
        }

        default void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
            final long tmpStructure = this.countRows();
            this.fillOne(Structure2D.row(index, tmpStructure), Structure2D.column(index, tmpStructure), values, valueIndex);
        }

        void fillOne(long row, long col, final Access1D<?> values, final long valueIndex);

        void fillOne(long row, long col, N value);

        void fillOne(long row, long col, NullaryFunction<N> supplier);

        default void fillOne(final long index, final N value) {
            final long tmpStructure = this.countRows();
            this.fillOne(Structure2D.row(index, tmpStructure), Structure2D.column(index, tmpStructure), value);
        }

        default void fillOne(final long index, final NullaryFunction<N> supplier) {
            final long tmpStructure = this.countRows();
            this.fillOne(Structure2D.row(index, tmpStructure), Structure2D.column(index, tmpStructure), supplier);
        }

        default void fillRow(final long row, final Access1D<N> values) {
            this.fillRow(row, 0L, values);
        }

        default void fillRow(final long row, final long col, final Access1D<N> values) {
            this.loopRow(row, col, (r, c) -> this.fillOne(r, c, values.get(c - col)));
        }

        default void fillRow(final long row, final long col, final N value) {
            this.loopRow(row, col, (r, c) -> this.fillOne(r, c, value));
        }

        default void fillRow(final long row, final long col, final NullaryFunction<N> supplier) {
            this.loopRow(row, col, (r, c) -> this.fillOne(r, c, supplier));
        }

        default void fillRow(final long row, final N value) {
            this.fillRow(row, 0L, value);
        }

        default void fillRow(final long row, final NullaryFunction<N> supplier) {
            this.fillRow(row, 0L, supplier);
        }

    }

    interface Mixable<N extends Number> extends Structure2D, Mutate1D.Mixable<N> {

        default double mix(final long index, final BinaryFunction<N> mixer, final double addend) {
            final long structure = this.countRows();
            return this.mix(Structure2D.row(index, structure), Structure2D.column(index, structure), mixer, addend);
        }

        default N mix(final long index, final BinaryFunction<N> mixer, final N addend) {
            final long structure = this.countRows();
            return this.mix(Structure2D.row(index, structure), Structure2D.column(index, structure), mixer, addend);
        }

        double mix(long row, long col, BinaryFunction<N> mixer, double addend);

        N mix(long row, long col, BinaryFunction<N> mixer, N addend);
    }

    interface Modifiable<N extends Number> extends Structure2D, Mutate1D.Modifiable<N> {

        default void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
            this.loopColumn(row, col, (r, c) -> this.modifyOne(r, c, modifier));
        }

        default void modifyColumn(final long col, final UnaryFunction<N> modifier) {
            this.modifyColumn(0L, col, modifier);
        }

        default void modifyDiagonal(final long row, final long col, final UnaryFunction<N> modifier) {
            this.loopDiagonal(row, col, (r, c) -> this.modifyOne(r, c, modifier));
        }

        default void modifyDiagonal(final UnaryFunction<N> modifier) {
            this.modifyDiagonal(0L, 0L, modifier);
        }

        void modifyOne(long row, long col, UnaryFunction<N> modifier);

        default void modifyOne(final long index, final UnaryFunction<N> modifier) {
            final long tmpStructure = this.countRows();
            this.modifyOne(Structure2D.row(index, tmpStructure), Structure2D.column(index, tmpStructure), modifier);
        }

        default void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
            this.loopRow(row, col, (r, c) -> this.modifyOne(r, c, modifier));
        }

        default void modifyRow(final long row, final UnaryFunction<N> modifier) {
            this.modifyRow(row, 0L, modifier);
        }

    }

    interface Receiver<N extends Number> extends Mutate2D, Fillable<N>, Consumer<Access2D<?>> {

        default void accept(final Access2D<?> supplied) {
            if (this.isAcceptable(supplied)) {
                supplied.loopAll((r, c) -> this.set(r, c, supplied.get(r, c)));
            } else {
                throw new ProgrammingError("Not acceptable!");
            }
        }

        default boolean isAcceptable(final Structure2D supplier) {
            return (this.countRows() >= supplier.countRows()) && (this.countColumns() >= supplier.countColumns());
        }

    }

    default void add(final long index, final double addend) {
        final long tmpStructure = this.countRows();
        this.add(Structure2D.row(index, tmpStructure), Structure2D.column(index, tmpStructure), addend);
    }

    void add(long row, long col, double addend);

    void add(long row, long col, Number addend);

    default void add(final long index, final Number addend) {
        final long tmpStructure = this.countRows();
        this.add(Structure2D.row(index, tmpStructure), Structure2D.column(index, tmpStructure), addend);
    }

    default void set(final long index, final double addend) {
        final long tmpStructure = this.countRows();
        this.set(Structure2D.row(index, tmpStructure), Structure2D.column(index, tmpStructure), addend);
    }

    void set(long row, long col, double value);

    void set(long row, long col, Number value);

    default void set(final long index, final Number addend) {
        final long tmpStructure = this.countRows();
        this.set(Structure2D.row(index, tmpStructure), Structure2D.column(index, tmpStructure), addend);
    }

}
