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

import java.util.function.Consumer;

import org.ojalgo.ProgrammingError;

/**
 * You can query the size/shape before accepting.
 *
 * @author apete
 */
public interface Consumer2D extends Structure2D, Consumer<Access2D<?>> {

    interface Elements<N extends Number> extends Consumer2D, Mutate2D, Mutate2D.Fillable<N>, Mutate2D.Modifiable<N>, Mutate2D.BiModifiable<N> {

        default void accept(final Access2D<?> supplied) {
            if (this.isAcceptable(supplied)) {
                final long tmpCountRows = supplied.countRows();
                final long tmpCountColumns = supplied.countColumns();
                for (long j = 0L; j < tmpCountColumns; j++) {
                    for (long i = 0L; i < tmpCountRows; i++) {
                        this.set(i, j, supplied.get(i, j));
                    }
                }
            } else {
                throw new ProgrammingError("Not acceptable!");
            }
        }

    }

    default boolean isAcceptable(final Structure2D supplier) {
        return (this.countRows() >= supplier.countRows()) && (this.countColumns() >= supplier.countColumns());
    }

}
