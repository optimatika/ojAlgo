/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.type.function;

import java.util.concurrent.BlockingQueue;
import java.util.function.Function;
import java.util.function.Supplier;

final class SequencedSupplier<S, T> implements AutoSupplier<T> {

    private Supplier<T> myCurrent;
    private final Function<S, ? extends Supplier<T>> myFactory;
    private final BlockingQueue<S> mySources;

    SequencedSupplier(final BlockingQueue<S> sources, final Function<S, ? extends Supplier<T>> factory) {

        super();

        mySources = sources;
        myFactory = factory;
        this.nextSupplier();
    }

    @Override
    public void close() throws Exception {
        if ((myCurrent != null) && (myCurrent instanceof AutoCloseable)) {
            ((AutoCloseable) myCurrent).close();
        }
    }

    public T read() {

        if (myCurrent == null) {
            return null;
        }

        T retVal = myCurrent.get();

        if (retVal == null) {

            if (myCurrent instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) myCurrent).close();
                } catch (Exception cause) {
                    throw new RuntimeException(cause);
                }
            }

            this.nextSupplier();

            if (myCurrent != null) {
                retVal = myCurrent.get();
            }
        }

        return retVal;
    }

    private void nextSupplier() {
        S next = mySources.poll();
        myCurrent = next != null ? myFactory.apply(next) : null;
    }

}
