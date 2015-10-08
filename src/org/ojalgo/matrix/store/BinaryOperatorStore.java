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

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.matrix.store.MatrixStore.ElementsConsumer;

final class BinaryOperatorStore<N extends Number> extends AbstractSupplier<N> {

    private final BinaryFunction<N> myFunction;
    private final MatrixStore<N> myRight;

    private BinaryOperatorStore(final MatrixStore<N> base) {

        super(base);

        myFunction = null;
        myRight = null;
    }

    BinaryOperatorStore(final MatrixStore<N> base, final BinaryFunction<N> function, final MatrixStore<N> right) {

        super(base);

        myFunction = function;
        myRight = right;
    }

    public long countColumns() {
        return myRight.countColumns();
    }

    public long countRows() {
        return myRight.countRows();
    }

    public long count() {
        return myRight.count();
    }

    @Override
    public void supplyTo(final ElementsConsumer<N> consumer) {
        consumer.fillMatching(this.getBase(), myFunction, myRight);
    }

}
