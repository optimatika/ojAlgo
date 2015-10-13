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

import org.ojalgo.function.UnaryFunction;

final class UnaryOperatorSupplier<N extends Number> extends ContextSupplier<N> {

    private final MatrixStore<N> myArguments;
    private final UnaryFunction<N> myFunction;

    private UnaryOperatorSupplier(final MatrixStore<N> context) {

        super(context);

        myFunction = null;
        myArguments = null;
    }

    UnaryOperatorSupplier(final UnaryFunction<N> function, final MatrixStore<N> arguments) {

        super(arguments);

        myFunction = function;
        myArguments = arguments;
    }

    public long count() {
        return myArguments.count();
    }

    public long countColumns() {
        return myArguments.countColumns();
    }

    public long countRows() {
        return myArguments.countRows();
    }

    @Override
    public void supplyTo(final ElementsConsumer<N> consumer) {
        consumer.fillMatching(myFunction, myArguments);
    }

}
