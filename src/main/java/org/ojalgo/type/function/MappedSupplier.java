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

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class MappedSupplier<IN, OUT> implements AutoSupplier<OUT> {

    private final Function<IN, OUT> myMapper;
    private final Supplier<IN> mySupplier;
    private final Predicate<IN> myFilter;

    MappedSupplier(final Supplier<IN> supplier, final Function<IN, OUT> mapper) {
        this(supplier, in -> true, mapper);
    }

    MappedSupplier(final Supplier<IN> supplier, final Predicate<IN> filter, final Function<IN, OUT> mapper) {
        super();
        mySupplier = supplier;
        myFilter = filter;
        myMapper = mapper;
    }

    public void close() throws Exception {
        if (mySupplier instanceof AutoCloseable) {
            ((AutoCloseable) mySupplier).close();
        }
    }

    public OUT read() {

        IN unmapped = null;
        OUT retVal = null;

        while ((unmapped = mySupplier.get()) != null && (!myFilter.test(unmapped) || (retVal = myMapper.apply(unmapped)) == null)) {
            // Read until we get a non-null item that passes the test and mapping works (return not-null)
        }

        return retVal;
    }

}
