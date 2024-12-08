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
package org.ojalgo.netio;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;

final class SequencedReader<S, T> implements FromFileReader<T> {

    private FromFileReader<T> myCurrent;
    private final Function<S, ? extends FromFileReader<T>> myFactory;
    private final BlockingQueue<S> mySources;

    /**
     * Create an {@link AutoSupplier} that will supply items from the containers, one after the other, until
     * all containers are empty. You can create multiple such suppliers sharing the same queue of containers.
     *
     * @param <S> The type of some sort of item container (maybe a {@link File})
     * @param <T> The supplier item type (what do the files contain?)
     * @param sources A set of item containers (could be a set of {@link File}:s)
     * @param factory A factory method that can take one of the "containers" and return an item supplier.
     * @return A sequenced supplier.
     */
    SequencedReader(final BlockingQueue<S> sources, final Function<S, ? extends FromFileReader<T>> factory) {

        super();

        mySources = sources;
        myFactory = factory;
        this.nextSupplier();
    }

    @Override
    public void close() throws IOException {
        if (myCurrent != null) {
            myCurrent.close();
        }
    }

    @Override
    public T read() {

        if (myCurrent == null) {
            return null;
        }

        T retVal = myCurrent.read();

        if (retVal == null) {

            try {
                myCurrent.close();
            } catch (Exception cause) {
                throw new RuntimeException(cause);
            }

            this.nextSupplier();

            if (myCurrent != null) {
                retVal = myCurrent.read();
            }
        }

        return retVal;
    }

    private void nextSupplier() {
        S next = mySources.poll();
        myCurrent = next != null ? myFactory.apply(next) : null;
    }

}
