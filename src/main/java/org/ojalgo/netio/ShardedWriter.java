/*
 * Copyright 1997-2025 Optimatika
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
import java.util.function.ToIntFunction;

import org.ojalgo.function.special.PowerOf2;

abstract class ShardedWriter<T> implements ToFileWriter<T> {

    static final class GeneralShardedConsumer<T> extends ShardedWriter<T> {

        private final ToFileWriter<T>[] myWriters;
        private final ToIntFunction<T> myDistributor;
        private final int myNumberOfShards;

        GeneralShardedConsumer(final ToIntFunction<T> distributor, final ToFileWriter<T>[] consumers) {

            super(consumers);

            myWriters = consumers;
            myDistributor = distributor;
            myNumberOfShards = consumers.length;
        }

        @Override
        public void write(final T item) {
            myWriters[Math.abs(myDistributor.applyAsInt(item) % myNumberOfShards)].write(item);
        }

    }

    static final class PowerOf2ShardedConsumer<T> extends ShardedWriter<T> {

        private final ToFileWriter<T>[] myWriters;
        private final ToIntFunction<T> myDistributor;
        private final int myIndexMask;

        PowerOf2ShardedConsumer(final ToIntFunction<T> distributor, final ToFileWriter<T>[] consumers) {

            super(consumers);

            if (!PowerOf2.isPowerOf2(consumers.length)) {
                throw new IllegalArgumentException("The number of consumers must be a power of 2!");
            }

            myWriters = consumers;
            myDistributor = distributor;
            myIndexMask = consumers.length - 1;
        }

        @Override
        public void write(final T item) {
            myWriters[myDistributor.applyAsInt(item) & myIndexMask].write(item);
        }

    }

    static <T> ShardedWriter<T> of(final ToIntFunction<T> distributor, final ToFileWriter<T>[] consumers) {
        if (PowerOf2.isPowerOf2(consumers.length)) {
            return new PowerOf2ShardedConsumer<>(distributor, consumers);
        } else {
            return new GeneralShardedConsumer<>(distributor, consumers);
        }
    }

    private final ToFileWriter<T>[] myWriters;

    ShardedWriter(final ToFileWriter<T>[] consumers) {

        super();

        myWriters = consumers;
    }

    @Override
    public void close() throws IOException {
        for (ToFileWriter<T> consumer : myWriters) {
            consumer.close();
        }
    }

}
