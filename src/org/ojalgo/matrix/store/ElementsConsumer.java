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

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Consumer2D;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;

public interface ElementsConsumer<N extends Number> extends Consumer2D<Access2D<N>>, Access2D.Fillable<N>, Access2D.Modifiable<N> {

    default void accept(final Access2D<N> supplied) {
        final long tmpCountRows = supplied.countRows();
        final long tmpCountColumns = supplied.countColumns();
        for (long j = 0; j < tmpCountColumns; j++) {
            for (long i = 0; i < tmpCountRows; i++) {
                this.set(i, j, supplied.get(i, j));
            }
        }
    }

    default void acceptFrom(final ElementsSupplier<N> supplier) {
        if (this.isAcceptable(supplier)) {
            this.accept(supplier.get());
        } else {
            throw new ProgrammingError("Not acceptable!");
        }
    }

    default void fillAll(final N value) {
        final long tmpCountRows = this.countRows();
        final long tmpCountColumns = this.countColumns();
        for (long j = 0L; j < tmpCountColumns; j++) {
            for (long i = 0L; i < tmpCountRows; i++) {
                this.fillOne(i, j, value);
            }
        }
    }

    default void fillAll(final NullaryFunction<N> supplier) {
        final long tmpCountRows = this.countRows();
        final long tmpCountColumns = this.countColumns();
        for (long j = 0L; j < tmpCountColumns; j++) {
            for (long i = 0L; i < tmpCountRows; i++) {
                this.fillOne(i, j, supplier);
            }
        }
    }

    void fillByMultiplying(final Access1D<N> left, final Access1D<N> right);

    default void fillColumn(final long row, final long column, final N value) {
        final long tmpCountRows = this.countRows();
        for (long i = row; i < tmpCountRows; i++) {
            this.fillOne(i, column, value);
        }
    }

    default void fillColumn(final long row, final long column, final NullaryFunction<N> supplier) {
        final long tmpCountRows = this.countRows();
        for (long i = row; i < tmpCountRows; i++) {
            this.fillOne(i, column, supplier);
        }
    }

    default void fillDiagonal(final long row, final long column, final N value) {
        final long tmpCountRows = this.countRows();
        final long tmpCountColumns = this.countColumns();
        for (long i = row, j = column; (i < tmpCountRows) && (j < tmpCountColumns); i++, j++) {
            this.fillOne(i, j, value);
        }
    }

    default void fillDiagonal(final long row, final long column, final NullaryFunction<N> supplier) {
        final long tmpCountRows = this.countRows();
        final long tmpCountColumns = this.countColumns();
        for (long i = row, j = column; (i < tmpCountRows) && (j < tmpCountColumns); i++, j++) {
            this.fillOne(i, j, supplier);
        }
    }

    default void fillRow(final long row, final long column, final N value) {
        final long tmpCountColumns = this.countColumns();
        for (long j = column; j < tmpCountColumns; j++) {
            this.fillOne(row, j, value);
        }
    }

    default void fillRow(final long row, final long column, final NullaryFunction<N> supplier) {
        final long tmpCountColumns = this.countColumns();
        for (long j = column; j < tmpCountColumns; j++) {
            this.fillOne(row, j, supplier);
        }
    }

    default void modifyAll(final UnaryFunction<N> function) {
        final long tmpCountRows = this.countRows();
        final long tmpCountColumns = this.countColumns();
        for (long j = 0L; j < tmpCountColumns; j++) {
            for (long i = 0L; i < tmpCountRows; i++) {
                this.modifyOne(i, j, function);
            }
        }
    }

    default void modifyColumn(final long row, final long column, final UnaryFunction<N> function) {
        final long tmpCountRows = this.countRows();
        for (long i = row; i < tmpCountRows; i++) {
            this.modifyOne(i, column, function);
        }
    }

    default void modifyDiagonal(final long row, final long column, final UnaryFunction<N> function) {
        final long tmpCountRows = this.countRows();
        final long tmpCountColumns = this.countColumns();
        for (long i = row, j = column; (i < tmpCountRows) && (j < tmpCountColumns); i++, j++) {
            this.modifyOne(i, j, function);
        }
    }

    default void modifyRow(final long row, final long column, final UnaryFunction<N> function) {
        final long tmpCountColumns = this.countColumns();
        for (long j = column; j < tmpCountColumns; j++) {
            this.modifyOne(row, j, function);
        }
    }

    /**
     * @return A consumer (sub)region
     */
    ElementsConsumer<N> regionByColumns(int... columns);

    /**
     * @return A consumer (sub)region
     */
    ElementsConsumer<N> regionByLimits(int rowLimit, int columnLimit);

    /**
     * @return A consumer (sub)region
     */
    ElementsConsumer<N> regionByOffsets(int rowOffset, int columnOffset);

    /**
     * @return A consumer (sub)region
     */
    ElementsConsumer<N> regionByRows(int... rows);

    /**
     * @return A transposed consumer region
     */
    ElementsConsumer<N> regionByTransposing();

}
