/*
 * Copyright 1997-2022 Optimatika
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

import java.util.function.Consumer;
import java.util.function.Function;

final class MappedConsumer<IN, OUT> implements AutoConsumer<IN> {

    private final Consumer<OUT> myConsumer;
    private final Function<IN, OUT> myMapper;

    MappedConsumer(final Function<IN, OUT> mapper, final Consumer<OUT> consumer) {
        super();
        myMapper = mapper;
        myConsumer = consumer;
    }

    public void close() throws Exception {
        if (myConsumer instanceof AutoCloseable) {
            ((AutoCloseable) myConsumer).close();
        }
    }

    public void write(final IN item) {
        myConsumer.accept(myMapper.apply(item));
    }

}
