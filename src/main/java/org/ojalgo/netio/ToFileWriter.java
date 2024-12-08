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
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;
import org.ojalgo.type.management.MBeanUtils;
import org.ojalgo.type.management.Throughput;

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

            int numberOfShards = shards.length;
            int numberOfQueues = Math.max(1, Math.min(parallelism, numberOfShards));
            int capacityPerQueue = Math.max(3, queueCapacity / numberOfQueues);

            ToFileWriter<T> single;

            if (numberOfShards == 1) {

                BlockingQueue<T> queue = new LinkedBlockingQueue<>(capacityPerQueue);
                single = ToFileWriter.queued(executor, queue, shards[0]);

            } else if (numberOfQueues == 1) {

                BlockingQueue<T> queue = new LinkedBlockingQueue<>(capacityPerQueue);
                ToFileWriter<T> consumer = ToFileWriter.sharded(distributor, shards);
                single = ToFileWriter.queued(executor, queue, consumer);

            } else if (numberOfQueues == numberOfShards) {

                ToFileWriter<T>[] queuedWriters = (ToFileWriter<T>[]) new ToFileWriter<?>[numberOfQueues];

                for (int q = 0; q < numberOfQueues; q++) {
                    BlockingQueue<T> queue = new LinkedBlockingQueue<>(capacityPerQueue);
                    queuedWriters[q] = ToFileWriter.queued(executor, queue, shards[q]);
                }

                single = ToFileWriter.sharded(distributor, queuedWriters);

            } else {

                int candidateShardsPerQueue = numberOfShards / numberOfQueues;
                while (candidateShardsPerQueue * numberOfQueues < numberOfShards) {
                    candidateShardsPerQueue++;
                }
                int shardsPerQueue = candidateShardsPerQueue;

                ToFileWriter<T>[] queuedWriters = (ToFileWriter<T>[]) new ToFileWriter<?>[numberOfQueues];

                ToIntFunction<T> toQueueDistributor = item -> Math.abs(distributor.applyAsInt(item) % numberOfShards) / shardsPerQueue;
                ToIntFunction<T> toShardDistributor = item -> Math.abs(distributor.applyAsInt(item) % numberOfShards) % shardsPerQueue;

                for (int q = 0; q < numberOfQueues; q++) {
                    int offset = q * shardsPerQueue;

                    ToFileWriter<T>[] shardWriters = (ToFileWriter<T>[]) new ToFileWriter<?>[shardsPerQueue];
                    Arrays.fill(shardWriters, ToFileWriter.NULL);
                    for (int b = 0; b < shardsPerQueue && offset + b < numberOfShards; b++) {
                        shardWriters[b] = shards[offset + b];
                    }

                    BlockingQueue<T> queue = new LinkedBlockingQueue<>(capacityPerQueue);
                    ToFileWriter<T> writer = ToFileWriter.sharded(toShardDistributor, shardWriters);
                    queuedWriters[q] = ToFileWriter.queued(executor, queue, writer);
                }

                single = ToFileWriter.sharded(toQueueDistributor, queuedWriters);
            }

            if (this.isStatisticsCollector()) {
                return ToFileWriter.managed(this.getStatisticsCollector(), single);
            } else {
                return single;
            }
        }

        public <T> ToFileWriter<KeyedPrimitive<T>> buildMapped(final Function<F, ToFileWriter<T>> factory) {

            Function<KeyedPrimitive<T>, T> mapper = KeyedPrimitive::getKey;
            ToIntFunction<KeyedPrimitive<T>> distributor = KeyedPrimitive::intValue;

            Function<F, ToFileWriter<KeyedPrimitive<T>>> mappedFactory = file -> ToFileWriter.mapped(mapper, factory.apply(file));

            return this.build(distributor, mappedFactory);
        }

    }

    ToFileWriter<?> NULL = item -> {
        throw new IllegalStateException("NULL!");
    };

    /**
     * Will create a JMX bean, with the given name, that keeps track of the consumer's throughput.
     */
    static <T> ToFileWriter<T> managed(final String name, final ToFileWriter<T> consumer) {

        Throughput manager = new Throughput();

        MBeanUtils.register(manager, name);

        return new ManagedConsumer<>(manager, consumer);
    }

    /**
     * If you want that throughput manager to be registered as a JMX bean, that's up to you.
     */
    static <T> ToFileWriter<T> managed(final Throughput manager, final ToFileWriter<T> consumer) {
        return new ManagedConsumer<>(manager, consumer);
    }

    /**
     * Map/transform and then consume
     */
    static <S, T> ToFileWriter<S> mapped(final Function<S, T> mapper, final ToFileWriter<T> consumer) {
        return new MappedConsumer<>(mapper, consumer);
    }

    /**
     * Make sure this directory exists, create if necessary
     */
    static void mkdirs(final File dir) {
        if (!dir.exists() && !dir.mkdirs() && !dir.exists()) {
            throw new RuntimeException("Failed to create " + dir.getAbsolutePath());
        }
    }

    static Builder<File> newBuilder(final File... file) {
        return new Builder<>(file);
    }

    static Builder<File> newBuilder(final ShardedFile shards) {
        return new Builder<>(shards.shards());
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

    /**
     * Put on the queue, and then the consumers work off that queue. There will be 1 thread (executor task)
     * per consumer.
     */
    static <T> ToFileWriter<T> queued(final ExecutorService executor, final BlockingQueue<T> queue, final ToFileWriter<T>... consumers) {
        return new QueuedConsumer<>(executor, queue, consumers);
    }

    static <T extends Serializable> void serializeObjectToFile(final T object, final File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(ToFileWriter.output(file))) {
            oos.writeObject(object);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    /**
     * Distribute to 1 of the consumers
     */
    static <T> ToFileWriter<T> sharded(final ToIntFunction<T> distributor, final ToFileWriter<T>... consumers) {
        return ShardedConsumer.of(distributor, consumers);
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
