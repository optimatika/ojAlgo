/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;

/**
 * 2-dimensional mutator methods
 *
 * @author apete
 */
public interface Mutate2D extends Structure2D, Mutate1D {

    interface BiModifiable<N extends Number> extends Mutate2D, Mutate1D.BiModifiable<N> {

    }

    interface Fillable<N extends Number> extends Mutate2D, Mutate1D.Fillable<N> {

        default void fillColumn(final long row, final long column, final Access1D<N> values) {
            final long tmpCount = values.count();
            for (long i = 0L; i < tmpCount; i++) {
                this.set(row + i, column, values.get(i));
            }
        }

        void fillColumn(long row, long column, N value);

        void fillColumn(long row, long column, NullaryFunction<N> supplier);

        default void fillDiagonal(final long row, final long column, final Access1D<N> values) {
            for (long ij = 0L; ij < values.count(); ij++) {
                this.set(row + ij, column + ij, values.get(ij));
            }
        }

        void fillDiagonal(long row, long column, N value);

        void fillDiagonal(long row, long column, NullaryFunction<N> supplier);

        void fillOne(long row, long column, N value);

        void fillOne(long row, long column, NullaryFunction<N> supplier);

        default void fillOne(final long index, final N value) {
            final long tmpStructure = this.countRows();
            this.fillOne(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), value);
        }

        default void fillOne(final long index, final NullaryFunction<N> supplier) {
            final long tmpStructure = this.countRows();
            this.fillOne(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), supplier);
        }

        default void fillOneMatching(final long index, final Access1D<?> values, final long valueIndex) {
            final long tmpStructure = this.countRows();
            this.fillOneMatching(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), values, valueIndex);
        }

        void fillOneMatching(long row, long column, final Access1D<?> values, final long valueIndex);

        default void fillRange(final long first, final long limit, final N value) {
            for (long i = first; i < limit; i++) {
                this.fillOne(i, value);
            }
        }

        default void fillRange(final long first, final long limit, final NullaryFunction<N> supplier) {
            for (long i = first; i < limit; i++) {
                this.fillOne(i, supplier);
            }
        }

        default void fillRow(final long row, final long column, final Access1D<N> values) {
            for (long j = 0L; j < values.count(); j++) {
                this.set(row, column + j, values.get(j));
            }
        }

        void fillRow(long row, long column, N value);

        void fillRow(long row, long column, NullaryFunction<N> supplier);

    }

    interface Modifiable<N extends Number> extends Mutate2D, Mutate1D.Modifiable<N> {

        void modifyColumn(long row, long column, UnaryFunction<N> function);

        void modifyDiagonal(long row, long column, UnaryFunction<N> function);

        void modifyOne(long row, long column, UnaryFunction<N> function);

        default void modifyOne(final long index, final UnaryFunction<N> function) {
            final long tmpStructure = this.countRows();
            this.modifyOne(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), function);
        }

        default void modifyRange(final long first, final long limit, final UnaryFunction<N> function) {
            for (long i = first; i < limit; i++) {
                this.modifyOne(i, function);
            }
        }

        void modifyRow(long row, long column, UnaryFunction<N> function);

    }

    default void add(final long index, final double addend) {
        final long tmpStructure = this.countRows();
        this.add(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), addend);
    }

    void add(long row, long col, double addend);

    void add(long row, long col, Number addend);

    default void add(final long index, final Number addend) {
        final long tmpStructure = this.countRows();
        this.add(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), addend);
    }

    default void set(final long index, final double addend) {
        final long tmpStructure = this.countRows();
        this.set(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), addend);
    }

    void set(long row, long col, double value);

    void set(long row, long col, Number value);

    default void set(final long index, final Number addend) {
        final long tmpStructure = this.countRows();
        this.set(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), addend);
    }

}
