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
 * You can query the size/count before accepting.
 *
 * @author apete
 */
public interface Consumer1D<I extends Structure1D> extends Structure1D, Consumer<I> {

    interface Elements<N extends Number, I extends Access1D<N>>
            extends Consumer1D<I>, Mutate1D, Mutate1D.Fillable<N>, Mutate1D.Modifiable<N>, Mutate1D.BiModifiable<N> {

        default void accept(final I supplied) {
            if (this.isAcceptable(supplied)) {
                final long tmpLimit = supplied.count();
                for (long i = 0L; i < tmpLimit; i++) {
                    this.set(i, supplied.get(i));
                }
            } else {
                throw new ProgrammingError("Not acceptable!");
            }
        }

    }

    default boolean isAcceptable(final Structure1D supplier) {
        return this.count() >= supplier.count();
    }

}
