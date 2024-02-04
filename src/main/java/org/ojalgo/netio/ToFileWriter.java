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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import org.ojalgo.type.function.AutoConsumer;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

public interface ToFileWriter<T> extends AutoConsumer<T>, Closeable {

    public static final class Builder extends ReaderWriterBuilder<ToFileWriter.Builder> {

        Builder(final File[] files) {
            super(files);

        }

        public <T> AutoConsumer<T> build(final Function<File, ToFileWriter<T>> factory) {
            return this.build(Object::hashCode, factory);
        }

        @SuppressWarnings("resource")
        public <T> AutoConsumer<T> build(final ToIntFunction<T> distributor, final Function<File, ? extends AutoConsumer<T>> factory) {

            File[] files = this.getFiles();

            AutoConsumer<T>[] shards = (AutoConsumer<T>[]) new AutoConsumer<?>[files.length];
            for (int i = 0; i < shards.length; i++) {
                shards[i] = factory.apply(files[i]);
            }

            int queueCapacity = this.getQueueCapacity();
            int parallelism = this.getParallelism();
            ExecutorService executor = this.getExecutor();

            int numberOfShards = shards.length;
            int numberOfQueues = Math.max(1, Math.min(parallelism, numberOfShards));
            int capacityPerQueue = Math.max(3, queueCapacity / numberOfQueues);

            AutoConsumer<T> single;

            if (numberOfShards == 1) {

                LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>(capacityPerQueue);
                single = AutoConsumer.queued(executor, queue, shards[0]);

            } else if (numberOfQueues == 1) {

                LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>(capacityPerQueue);
                AutoConsumer<T> consumer = AutoConsumer.sharded(distributor, shards);
                single = AutoConsumer.queued(executor, queue, consumer);

            } else if (numberOfQueues == numberOfShards) {

                AutoConsumer<T>[] queuedWriters = (AutoConsumer<T>[]) new AutoConsumer<?>[numberOfQueues];

                for (int q = 0; q < numberOfQueues; q++) {
                    LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>(capacityPerQueue);
                    queuedWriters[q] = AutoConsumer.queued(executor, queue, shards[q]);
                }

                single = AutoConsumer.sharded(distributor, queuedWriters);

            } else {

                int candidateShardsPerQueue = numberOfShards / numberOfQueues;
                while (candidateShardsPerQueue * numberOfQueues < numberOfShards) {
                    candidateShardsPerQueue++;
                }
                int shardsPerQueue = candidateShardsPerQueue;

                AutoConsumer<T>[] queuedWriters = (AutoConsumer<T>[]) new AutoConsumer<?>[numberOfQueues];

                ToIntFunction<T> toQueueDistributor = item -> Math.abs(distributor.applyAsInt(item) % numberOfShards) / shardsPerQueue;
                ToIntFunction<T> toShardDistributor = item -> Math.abs(distributor.applyAsInt(item) % numberOfShards) % shardsPerQueue;

                for (int q = 0; q < numberOfQueues; q++) {
                    int offset = q * shardsPerQueue;

                    AutoConsumer<T>[] shardWriters = (AutoConsumer<T>[]) new AutoConsumer<?>[shardsPerQueue];
                    Arrays.fill(shardWriters, AutoConsumer.NULL);
                    for (int b = 0; b < shardsPerQueue && offset + b < numberOfShards; b++) {
                        shardWriters[b] = shards[offset + b];
                    }

                    LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>(capacityPerQueue);
                    AutoConsumer<T> writer = AutoConsumer.sharded(toShardDistributor, shardWriters);
                    queuedWriters[q] = AutoConsumer.queued(executor, queue, writer);
                }

                single = AutoConsumer.sharded(toQueueDistributor, queuedWriters);
            }

            if (this.isStatisticsCollector()) {
                return AutoConsumer.managed(this.getStatisticsCollector(), single);
            } else {
                return single;
            }
        }

        public <T> AutoConsumer<KeyedPrimitive<T>> buildMapped(final Function<File, ToFileWriter<T>> factory) {

            Function<KeyedPrimitive<T>, T> mapper = KeyedPrimitive::getKey;
            ToIntFunction<KeyedPrimitive<T>> distributor = KeyedPrimitive::intValue;

            Function<File, AutoConsumer<KeyedPrimitive<T>>> mappedFactory = file -> AutoConsumer.mapped(mapper, factory.apply(file));

            return this.build(distributor, mappedFactory);
        }

    }

    /**
     * Make sure this directory exists, create if necessary
     */
    static void mkdirs(final File dir) {
        if (!dir.exists() && (!dir.mkdirs() && !dir.exists())) {
            throw new RuntimeException("Failed to create " + dir.getAbsolutePath());
        }
    }

    static Builder newBuilder(final File... file) {
        return new Builder(file);
    }

    static Builder newBuilder(final ShardedFile shards) {
        return new Builder(shards.shards());
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

    default void close() throws IOException {
        try {
            AutoConsumer.super.close();
        } catch (Exception cause) {
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }

}
