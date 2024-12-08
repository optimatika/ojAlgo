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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.ojalgo.type.function.OperatorWithException;
import org.ojalgo.type.management.MBeanUtils;
import org.ojalgo.type.management.Throughput;

public interface FromFileReader<T> extends Iterable<T>, Closeable {

    public static final class Builder<F> extends ReaderWriterBuilder<F, FromFileReader.Builder<F>> {

        Builder(final F[] files) {
            super(files);
        }

        public <T> FromFileReader<T> build(final Function<F, ? extends FromFileReader<T>> factory) {

            F[] files = this.getFiles();

            BlockingQueue<T> queue = new LinkedBlockingQueue<>(this.getQueueCapacity());

            FromFileReader<T> single;

            if (files.length == 1) {

                single = FromFileReader.queued(this.getExecutor(), queue, factory.apply(files[0]));

            } else {

                BlockingQueue<F> containers = new ArrayBlockingQueue<>(files.length);
                Collections.addAll(containers, files);

                FromFileReader<T>[] readers = (FromFileReader<T>[]) new FromFileReader<?>[this.getParallelism()];
                for (int i = 0; i < readers.length; i++) {
                    readers[i] = FromFileReader.sequenced(containers, factory);
                }

                single = FromFileReader.queued(this.getExecutor(), queue, readers);
            }

            if (this.isStatisticsCollector()) {
                return FromFileReader.managed(this.getStatisticsCollector(), single);
            } else {
                return single;
            }
        }

    }

    /**
     * Read the properties file and copy the entries to the supplied destination {@link Properties} instance.
     *
     * @param sourceFile Source properties file
     * @param destinationMap Destination properties map
     */
    static void copy(final File sourceFile, final Properties destinationMap) {

        BasicLogger.debug("Path to properties file: {}", sourceFile);

        try (FileInputStream stream = new java.io.FileInputStream(sourceFile)) {
            destinationMap.load(stream);
        } catch (IOException cause) {
            BasicLogger.error(cause, "Failed to load properties file!");
        }
    }

    /**
     * Delete this file or directory (does not need to be empty).
     *
     * @param file Path to a file or directory to be deleted
     */
    static void delete(final File file) {

        if (file == null || !file.exists()) {
            return;
        }

        File[] nested = file.listFiles();

        if (nested != null && nested.length > 0) {
            for (File subfile : nested) {
                FromFileReader.delete(subfile);
            }
        }

        if (!file.delete()) {
            throw new RuntimeException("Failed to delete " + file.getAbsolutePath());
        }
    }

    static <T extends Serializable> T deserializeObjectFromFile(final File file) {
        try (ObjectInputStream ois = new ObjectInputStream(FromFileReader.input(file))) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException cause) {
            throw new RuntimeException(cause);
        }
    }

    static <T> FromFileReader<T> empty() {
        return () -> null;
    }

    static InputStream input(final File file) {

        try {

            ToFileWriter.mkdirs(file.getParentFile());
            String name = file.getName();
            InputStream retVal = new FileInputStream(file);

            if (name.endsWith(".gz")) {
                retVal = new GZIPInputStream(retVal);
            } else if (name.endsWith(".zip")) {
                retVal = new ZipInputStream(retVal);
            }

            return retVal;

        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    static InputStream input(final File file, final OperatorWithException<InputStream> filter) {
        return filter.apply(FromFileReader.input(file));
    }

    /**
     * Will create a JMX bean, with the given name, that keeps track of the supplier's throughput.
     */
    static <T> FromFileReader<T> managed(final String name, final FromFileReader<T> supplier) {

        Throughput manager = new Throughput();

        MBeanUtils.register(manager, name);

        return new ManagedSupplier<>(manager, supplier);
    }

    /**
     * If you want that throughput manager to be registered as a JMX bean, that's up to you.
     */
    static <T> FromFileReader<T> managed(final Throughput manager, final FromFileReader<T> supplier) {
        return new ManagedSupplier<>(manager, supplier);
    }

    /**
     * Get something and map/transform before returning it
     */
    static <T, U> FromFileReader<U> mapped(final FromFileReader<T> supplier, final Function<T, U> mapper) {
        return new MappedSupplier<>(supplier, mapper);
    }

    /**
     * Get something, that passes the test, and map/transform before returning it
     */
    static <T, U> FromFileReader<U> mapped(final FromFileReader<T> supplier, final Predicate<T> filter, final Function<T, U> mapper) {
        return new MappedSupplier<>(supplier, filter, mapper);
    }

    static Builder<File> newBuilder(final File... file) {
        return new Builder<>(file);
    }

    static Builder<SegmentedFile.Segment> newBuilder(final SegmentedFile segmentedFile) {
        return new Builder<>(segmentedFile.getSegments());
    }

    static Builder<File> newBuilder(final ShardedFile shards) {
        return new Builder<>(shards.shards());
    }

    /**
     * A factory that produce readers that read items from the supplied sources. (You have a collection of
     * files and want to read through them all using 1 or more readers.)
     */
    static <S, T> Supplier<FromFileReader<T>> newFactory(final Function<S, FromFileReader<T>> factory, final Collection<? extends S> sources) {

        BlockingQueue<S> work = new ArrayBlockingQueue<>(sources.size(), false, sources);

        return () -> FromFileReader.sequenced(work, factory);
    }

    static <S, T> Supplier<FromFileReader<T>> newFactory(final Function<S, FromFileReader<T>> factory, final S... sources) {

        BlockingQueue<S> work = new ArrayBlockingQueue<>(sources.length, false);
        Collections.addAll(work, sources);

        return () -> FromFileReader.sequenced(work, factory);
    }

    /**
     * Multiple suppliers supply to a queue, then you get from that queue. There will be 1 thread (executor
     * task) per supplier.
     */
    static <T> FromFileReader<T> queued(final ExecutorService executor, final BlockingQueue<T> queue, final FromFileReader<T>... suppliers) {
        return new QueuedSupplier<>(executor, queue, suppliers);
    }

    static <T> FromFileReader<T> sequenced(final BlockingQueue<? extends FromFileReader<T>> sources) {
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
    static <S, T> FromFileReader<T> sequenced(final BlockingQueue<S> sources, final Function<S, ? extends FromFileReader<T>> factory) {
        return new SequencedSupplier<>(sources, factory);
    }

    @Override
    default void close() throws IOException {
        // Default implementation does nothing
    }

    default int drainTo(final Collection<? super T> container, final int maxElements) {

        int retVal = 0;

        T item = null;
        while (retVal < maxElements && (item = this.read()) != null) {
            container.add(item);
            retVal++;
        }

        return retVal;
    }

    @Override
    default Iterator<T> iterator() {
        return new SupplierIterator<>(this);
    }

    default <U> FromFileReader<U> map(final Function<T, U> mapper) {
        return new MappedSupplier<>(this, mapper);
    }

    T read();

}
