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
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.ojalgo.type.management.MBeanUtils;
import org.ojalgo.type.management.Throughput;

/**
 * Utilities for {@link AutoCloseable} {@link Consumer}:s
 *
 * @author apete
 */
@FunctionalInterface
public interface AutoConsumer<T> extends AutoCloseable, Consumer<T>, AutoFunctional {

    AutoConsumer<?> NULL = item -> {
        throw new IllegalStateException("NULL!");
    };

    /**
     * Will create a JMX bean, with the given name, that keeps track of the consumer's throughput.
     */
    static <T> AutoConsumer<T> managed(final String name, final Consumer<T> consumer) {

        Throughput manager = new Throughput();

        MBeanUtils.register(manager, name);

        return new ManagedConsumer<>(manager, consumer);
    }

    /**
     * If you want that throughput manager to be registered as a JMX bean, that's up to you.
     */
    static <T> AutoConsumer<T> managed(final Throughput manager, final Consumer<T> consumer) {
        return new ManagedConsumer<>(manager, consumer);
    }

    /**
     * Map/transform and then consume
     */
    static <S, T> AutoConsumer<S> mapped(final Function<S, T> mapper, final Consumer<T> consumer) {
        return new MappedConsumer<>(mapper, consumer);
    }

    /**
     * Put on the queue, and then the consumers work off that queue. There will be 1 thread (executor task)
     * per consumer.
     */
    static <T> AutoConsumer<T> queued(final ExecutorService executor, final BlockingQueue<T> queue, final Consumer<T>... consumers) {
        return new QueuedConsumer<>(executor, queue, consumers);
    }

    /**
     * Distribute to 1 of the consumers
     */
    static <T> AutoConsumer<T> sharded(final ToIntFunction<T> distributor, final Consumer<T>... consumers) {
        return ShardedConsumer.of(distributor, consumers);
    }

    /**
     * @see #write(Object)
     */
    @Override
    default void accept(final T item) {
        this.write(item);
    }

    @Override
    default void close() throws Exception {
        // Default implementation does nothing
    }

    /**
     * Write the item to the consumer.
     * 
     * @param item The item to be written
     */
    void write(T item);

    /**
     * Write the batch (collection of items) to the consumer.
     * 
     * @param batch The batch to be written
     */
    default void writeBatch(final Iterable<? extends T> batch) {
        for (T item : batch) {
            this.write(item);
        }
    }
}
