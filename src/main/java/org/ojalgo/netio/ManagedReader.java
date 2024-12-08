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

import java.io.IOException;
import java.util.Collection;

import org.ojalgo.type.management.Throughput;

final class ManagedReader<T> implements FromFileReader<T> {

    private final Throughput myManager;
    private final FromFileReader<T> myReader;

    ManagedReader(final Throughput manager, final FromFileReader<T> reader) {
        super();
        myManager = manager;
        myReader = reader;
    }

    @Override
    public void close() throws IOException {
        myReader.close();
    }

    @Override
    public int drainTo(final Collection<? super T> container, final int maxElements) {

        int retVal = myReader.drainTo(container, maxElements);

        if (retVal != 0) {
            myManager.add(retVal);
        }

        return retVal;
    }

    @Override
    public T read() {
        T retVal = myReader.read();
        if (retVal != null) {
            myManager.increment();
        }
        return retVal;
    }

}
