/*
 * Copyright 1997-2017 Optimatika
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
import org.ojalgo.access.Mutate2D;

public interface ElementsConsumer<N extends Number> extends Mutate2D.Receiver<N>, Mutate2D.BiModifiable<N>, Mutate2D.Modifiable<N> {

    void fillByMultiplying(final Access1D<N> left, final Access1D<N> right);

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
