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

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.ojalgo.type.management.MBeanUtils;
import org.ojalgo.type.management.Throughput;

/**
 * Utilities for {@link AutoCloseable} {@link Supplier}:s
 *
 * @author apete
 */
@FunctionalInterface
public interface AutoSupplier<T> extends AutoCloseable, Supplier<T>, AutoFunctional, Iterable<T> {

    static <T> AutoSupplier<T> empty() {
        return () -> null;
    }

    /**
     * Will create a JMX bean, with the given name, that keeps track of the supplier's throughput.
     */
    static <T> AutoSupplier<T> managed(final String name, final Supplier<T> supplier) {

        Throughput manager = new Throughput();

        MBeanUtils.register(manager, name);

        return new ManagedSupplier<>(manager, supplier);
    }

    /**
     * If you want that throughput manager to be registered as a JMX bean, that's up to you.
     */
    static <T> AutoSupplier<T> managed(final Throughput manager, final Supplier<T> supplier) {
        return new ManagedSupplier<>(manager, supplier);
    }

    /**
     * Get something and map/transform before returning it
     */
    static <T, U> AutoSupplier<U> mapped(final Supplier<T> supplier, final Function<T, U> mapper) {
        return new MappedSupplier<>(supplier, mapper);
    }

    /**
     * Get something, that passes the test, and map/transform before returning it
     */
    static <T, U> AutoSupplier<U> mapped(final Supplier<T> supplier, final Predicate<T> filter, final Function<T, U> mapper) {
        return new MappedSupplier<>(supplier, filter, mapper);
    }

    /**
     * Multiple suppliers supply to a queue, then you get from that queue. There will be 1 thread (executor
     * task) per supplier.
     */
    static <T> AutoSupplier<T> queued(final ExecutorService executor, final BlockingQueue<T> queue, final Supplier<T>... suppliers) {
        return new QueuedSupplier<>(executor, queue, suppliers);
    }

    static <T> AutoSupplier<T> sequenced(final BlockingQueue<? extends Supplier<T>> sources) {
        return new SequencedSupplier<>(sources, s -> s);
    }

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
    static <S, T> AutoSupplier<T> sequenced(final BlockingQueue<S> sources, final Function<S, ? extends Supplier<T>> factory) {
        return new SequencedSupplier<>(sources, factory);
    }

    @Override
    default void close() throws Exception {
        // Default implementation does nothing
    }

    default int drainTo(final Collection<? super T> container, final int maxElements) {

        int retVal = 0;

        T item = null;
        while (retVal < maxElements && (item = this.get()) != null) {
            container.add(item);
            retVal++;
        }

        return retVal;
    }

    @Override
    default T get() {
        return this.read();
    }

    @Override
    default Iterator<T> iterator() {
        return new SupplierIterator<>(this);
    }

    default void processAll(final Consumer<T> processor) {
        for (T item : this) {
            processor.accept(item);
        }
    }

    T read();
}
