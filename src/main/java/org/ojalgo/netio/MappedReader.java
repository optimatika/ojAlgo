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
import java.util.function.Function;
import java.util.function.Predicate;

final class MappedReader<IN, OUT> implements FromFileReader<OUT> {

    private final Predicate<IN> myFilter;
    private final Function<IN, OUT> myMapper;
    private final FromFileReader<IN> myReader;

    MappedReader(final FromFileReader<IN> supplier, final Function<IN, OUT> mapper) {
        this(supplier, in -> true, mapper);
    }

    MappedReader(final FromFileReader<IN> reader, final Predicate<IN> filter, final Function<IN, OUT> mapper) {
        super();
        myReader = reader;
        myFilter = filter;
        myMapper = mapper;
    }

    @Override
    public void close() throws IOException {
        myReader.close();
    }

    @Override
    public OUT read() {

        IN unmapped = null;
        OUT retVal = null;

        while ((unmapped = myReader.read()) != null && (!myFilter.test(unmapped) || (retVal = myMapper.apply(unmapped)) == null)) {
            // Read until we get a non-null item that passes the test and mapping works (return not-null)
        }

        return retVal;
    }

}
