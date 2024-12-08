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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

/**
 * Essentially just a {@link Consumer}, but assumed to be writing to a file or similar, and therefore extends
 * {@link Closeable}.
 */
@FunctionalInterface
public interface ToFileWriter<T> extends Closeable {

    public static final class Builder<F> extends ReaderWriterBuilder<F, ToFileWriter.Builder<F>> {

        Builder(final F[] files) {
            super(files);

        }

        public <T> ToFileWriter<T> build(final Function<F, ToFileWriter<T>> factory) {
            return this.build(Object::hashCode, factory);
        }

        @SuppressWarnings("resource")
        public <T> ToFileWriter<T> build(final ToIntFunction<T> distributor, final Function<F, ? extends ToFileWriter<T>> factory) {

            F[] files = this.getFiles();

            ToFileWriter<T>[] shards = (ToFileWriter<T>[]) new ToFileWriter<?>[files.length];
            for (int i = 0; i < shards.length; i++) {
                shards[i] = factory.apply(files[i]);
            }

            int queueCapacity = this.getQueueCapacity();
            int parallelism = this.getParallelism();
            ExecutorService executor = this.getExecutor();

            int nbShards = shards.length;
            int nbQueues = Math.max(1, Math.min(parallelism, nbShards));
            int capacityPerQueue = Math.max(0, queueCapacity / nbQueues);

            ToFileWriter<T> single;

            if (nbShards == 1) {

                BlockingQueue<T> queue = this.newQueue(capacityPerQueue);

                single = new QueuedWriter<>(executor, queue, shards[0]);

            } else if (nbQueues == 1) {

                BlockingQueue<T> queue = this.newQueue(capacityPerQueue);
                ToFileWriter<T> consumer = ShardedWriter.of(distributor, shards);

                single = new QueuedWriter<>(executor, queue, consumer);

            } else if (nbQueues == nbShards) {

                ToFileWriter<T>[] queuedWriters = (ToFileWriter<T>[]) new ToFileWriter<?>[nbQueues];

                for (int q = 0; q < nbQueues; q++) {
                    BlockingQueue<T> queue = this.newQueue(capacityPerQueue);
                    queuedWriters[q] = new QueuedWriter<>(executor, queue, shards[q]);
                }

                single = ShardedWriter.of(distributor, queuedWriters);

            } else {

                int candidateShardsPerQueue = nbShards / nbQueues;
                while (candidateShardsPerQueue * nbQueues < nbShards) {
                    candidateShardsPerQueue++;
                }
                int shardsPerQueue = candidateShardsPerQueue;

                ToFileWriter<T>[] queuedWriters = (ToFileWriter<T>[]) new ToFileWriter<?>[nbQueues];

                ToIntFunction<T> toQueueDistributor = item -> Math.abs(distributor.applyAsInt(item) % nbShards) / shardsPerQueue;
                ToIntFunction<T> toShardDistributor = item -> Math.abs(distributor.applyAsInt(item) % nbShards) % shardsPerQueue;

                for (int q = 0; q < nbQueues; q++) {
                    int offset = q * shardsPerQueue;

                    ToFileWriter<T>[] shardWriters = (ToFileWriter<T>[]) new ToFileWriter<?>[shardsPerQueue];
                    Arrays.fill(shardWriters, ToFileWriter.NULL);
                    for (int b = 0; b < shardsPerQueue && offset + b < nbShards; b++) {
                        shardWriters[b] = shards[offset + b];
                    }

                    BlockingQueue<T> queue = this.newQueue(capacityPerQueue);
                    ToFileWriter<T> writer = ShardedWriter.of(toShardDistributor, shardWriters);

                    queuedWriters[q] = new QueuedWriter<>(executor, queue, writer);
                }

                single = ShardedWriter.of(toQueueDistributor, queuedWriters);
            }

            if (this.isStatisticsCollector()) {
                return new ManagedWriter<>(this.getStatisticsCollector(), single);
            } else {
                return single;
            }
        }

        public <T> ToFileWriter<KeyedPrimitive<T>> buildMapped(final Function<F, ToFileWriter<T>> factory) {

            Function<KeyedPrimitive<T>, T> mapper = KeyedPrimitive::getKey;
            ToIntFunction<KeyedPrimitive<T>> distributor = KeyedPrimitive::intValue;

            Function<F, ToFileWriter<KeyedPrimitive<T>>> mappedFactory = file -> new MappedWriter<>(mapper, factory.apply(file));

            return this.build(distributor, mappedFactory);
        }

    }

    ToFileWriter<?> NULL = item -> {
        throw new IllegalStateException("NULL!");
    };

    /**
     * Make sure this directory exists, create if necessary
     */
    static void mkdirs(final File dir) {
        if (!dir.exists() && !dir.mkdirs() && !dir.exists()) {
            throw new RuntimeException("Failed to create " + dir.getAbsolutePath());
        }
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

    static Builder<File> newBuilder(final ShardedFile sharded) {
        return new Builder<>(sharded.shards());
    }

    static OutputStream output(final File file) {

        try {

            ToFileWriter.mkdirs(file.getParentFile());
            String name = file.getName();
            OutputStream retVal = new FileOutputStream(file);

            if (name.endsWith(".gz")) {
                retVal = new GZIPOutputStream(retVal);
            } else if (name.endsWith(".zip")) {
                retVal = new ZipOutputStream(retVal);
            }

            return retVal;

        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    static <T extends Serializable> void serializeObjectToFile(final T object, final File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(ToFileWriter.output(file))) {
            oos.writeObject(object);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    @Override
    default void close() throws IOException {
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
