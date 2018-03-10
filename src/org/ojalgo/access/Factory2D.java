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

import java.util.List;

import org.ojalgo.function.NullaryFunction;

public interface Factory2D<I extends Structure2D> extends FactorySupplement {

    I columns(Access1D<?>... source);

    I columns(double[]... source);

    @SuppressWarnings("unchecked")
    I columns(List<? extends Number>... source);

    I columns(Number[]... source);

    I copy(Access2D<?> source);

    I makeEye(long rows, long columns);

    default I makeEye(final Structure2D shape) {
        return this.makeEye(shape.countRows(), shape.countColumns());
    }

    I makeFilled(long rows, long columns, NullaryFunction<?> supplier);

    default I makeFilled(final Structure2D shape, final NullaryFunction<?> supplier) {
        return this.makeFilled(shape.countRows(), shape.countColumns(), supplier);
    }

    I makeZero(long rows, long columns);

    default I makeZero(final Structure2D shape) {
        return this.makeZero(shape.countRows(), shape.countColumns());
    }

    I rows(Access1D<?>... source);

    I rows(double[]... source);

    @SuppressWarnings("unchecked")
    I rows(List<? extends Number>... source);

    I rows(Number[]... source);

}
