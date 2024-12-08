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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.ojalgo.type.function.OperatorWithException;

/**
 * Essentially just a {@link Supplier}, but assumed to be reading from a file or some other source of data,
 * and therefore extends {@link Closeable}.
 */
@FunctionalInterface
public interface FromFileReader<T> extends Iterable<T>, Closeable {

    public static final class Builder<F> extends ReaderWriterBuilder<F, FromFileReader.Builder<F>> {

        Builder(final F[] files) {
            super(files);
        }

        @SuppressWarnings("resource")
        public <T> FromFileReader<T> build(final Function<F, ? extends FromFileReader<T>> factory) {

            F[] files = this.getFiles();

            BlockingQueue<T> queue = this.newQueue(this.getQueueCapacity());

            FromFileReader<T> single = FromFileReader.empty();

            if (files.length == 1) {

                single = new QueuedReader<>(this.getExecutor(), queue, factory.apply(files[0]));

            } else {

                BlockingQueue<F> containers = new LinkedTransferQueue<>();
                Collections.addAll(containers, files);

                FromFileReader<T>[] readers = (FromFileReader<T>[]) new FromFileReader<?>[this.getParallelism()];
                for (int i = 0; i < readers.length; i++) {
                    readers[i] = new SequencedReader<>(containers, factory);
                }

                single = new QueuedReader<>(this.getExecutor(), queue, readers);
            }

            if (this.isStatisticsCollector()) {
                return new ManagedReader<>(this.getStatisticsCollector(), single);
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

    static <F> Builder<F> newBuilder(final F... file) {
        return new Builder<>(file);
    }

    static Builder<File> newBuilder(final File file) {
        return new Builder<>(new File[] { file });
    }

    static Builder<Path> newBuilder(final Path file) {
        return new Builder<>(new Path[] { file });
    }

    static Builder<SegmentedFile.Segment> newBuilder(final SegmentedFile segmented) {
        return new Builder<>(segmented.getSegments());
    }

    static Builder<File> newBuilder(final ShardedFile sharded) {
        return new Builder<>(sharded.shards());
    }

    @Override
    default void close() throws IOException {
        // Default implementation does nothing
    }

    /**
     * Behaves similar to {@link BlockingQueue#drainTo(Collection, int)} except that returning 0 means there
     * are no more items to read.
     */
    default int drainTo(final Collection<? super T> container, final int maxElements) {

        int retVal = 0;

        T item = null;
        while (retVal < maxElements && (item = this.read()) != null) {
            container.add(item);
            retVal++;
        }

        return retVal;
    }

    /**
     * Similar to {@link #forEach(Consumer)} but processes items in batches. Will extract up to batchSize
     * items before calling the action, and then repeat until no more items are available.
     */
    default void forEachInBacthes(final int batchSize, final Consumer<? super T> action) {

        List<T> batch = new ArrayList<>(batchSize);

        while (this.drainTo(batch, batchSize) > 0) {
            batch.forEach(action);
            batch.clear();
        }
    }

    @Override
    default Iterator<T> iterator() {
        return new SupplierIterator<>(this);
    }

    default <U> FromFileReader<U> map(final Function<T, U> mapper) {
        return new MappedReader<>(this, mapper);
    }

    /**
     * Returning null indicates that there are no more items to read. That's the same behaviour as
     * {@link BufferedReader#readLine()}. All implementations must return null precisely once.
     */
    T read();

    default Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this.iterator(), Spliterator.ORDERED | Spliterator.NONNULL), false);
    }

}
