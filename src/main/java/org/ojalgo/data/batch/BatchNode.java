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
package org.ojalgo.data.batch;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.concurrent.ProcessingService;
import org.ojalgo.function.special.PowerOf2;
import org.ojalgo.netio.DataInterpreter;
import org.ojalgo.netio.DataReader;
import org.ojalgo.netio.DataWriter;
import org.ojalgo.netio.FromFileReader;
import org.ojalgo.netio.ShardedFile;
import org.ojalgo.netio.ToFileWriter;
import org.ojalgo.type.function.AutoConsumer;
import org.ojalgo.type.function.AutoSupplier;
import org.ojalgo.type.function.TwoStepMapper;
import org.ojalgo.type.management.MBeanUtils;
import org.ojalgo.type.management.Throughput;

/**
 * A batch processing data node for when there's no way to fit the data in memory.
 * <p>
 * Data is stored in sharded files, and data is written/consumed and processed concurrently.
 * <p>
 * The data is processed in batches. Each batch is processed in a single thread. The number of threads is
 * controlled by {@link #parallelism(IntSupplier)}.
 */
public final class BatchNode<T> {

    public static final class Builder<T> {

        private final File myDirectory;
        private ToIntFunction<T> myDistributor = obj -> ThreadLocalRandom.current().nextInt();
        private ExecutorService myExecutor = null;
        private int myFragmentation = 64;
        private final DataInterpreter<T> myInterpreter;
        private int myParallelism = Parallelism.CORES.getAsInt();

        private int myQueueCapacity = 1024;

        Builder(final File directory, final DataInterpreter<T> interpreter) {
            super();
            myDirectory = directory;
            myInterpreter = interpreter;
        }

        public BatchNode<T> build() {
            return new BatchNode<>(this);
        }

        /**
         * The default is to distribute randomly. Most likely you want to distribute based on some property of
         * the item/type – extract that property and get its hash code. That causes all items with same value
         * on that property to end up in the same shard, and that you can exploit when processing the data.
         */
        public BatchNode.Builder<T> distributor(final ToIntFunction<T> distributor) {
            myDistributor = distributor;
            return this;
        }

        public BatchNode.Builder<T> executor(final ExecutorService executor) {
            myExecutor = executor;
            return this;
        }

        /**
         * The number of underlying files/shards. Increasing the fragmentation (the number of shards)
         * typically reduces memory requirements when processong. The value set here is only an indication of
         * the desired order of magnitude. The exact number of shards actually used is a derived property.
         */
        public BatchNode.Builder<T> fragmentation(final int fragmentation) {
            myFragmentation = fragmentation;
            return this;
        }

        /**
         * @see #parallelism(IntSupplier)
         */
        public BatchNode.Builder<T> parallelism(final int parallelism) {
            myParallelism = parallelism;
            return this;
        }

        /**
         * How many worker threads should process data in parallel?
         */
        public BatchNode.Builder<T> parallelism(final IntSupplier parallelism) {
            return this.parallelism(parallelism.getAsInt());
        }

        /**
         * When reading and/or writing data from/to disk data is temporarily queued. This specifies the total
         * maximum number of items kept in the queues.
         */
        public Builder<T> queue(final int capacity) {
            myQueueCapacity = capacity;
            return this;
        }

        ToIntFunction<T> getDistributor() {
            return myDistributor;
        }

        /**
         * The total number of files/shards. Will always be power of 2 as well as a multiple of
         * {@link #getParallelism()}.
         */
        int getFragmentation() {

            int parallelism = this.getParallelism().getAsInt(); // Is power of 2

            int factor = PowerOf2.adjustUp(Math.max(parallelism, myFragmentation) / parallelism);

            return factor * parallelism; // Is power of 2, and multiple of parallelism
        }

        DataInterpreter<T> getInterpreter() {
            return myInterpreter;
        }

        String getName() {
            return "BatchNode-" + myDirectory.getName();
        }

        /**
         * Will always be power of 2
         */
        IntSupplier getParallelism() {
            return () -> PowerOf2.adjustDown(Math.min(myParallelism, myFragmentation));
        }

        ProcessingService getProcessor() {
            if (myExecutor != null) {
                return new ProcessingService(myExecutor);
            } else {
                return ProcessingService.newInstance(this.getName());
            }
        }

        int getQueueCapacity() {
            return myQueueCapacity;
        }

        ShardedFile getShardedFile() {
            return ShardedFile.of(myDirectory, "Shard.data", this.getFragmentation());
        }

    }

    private static final class TwoStepWrapper<T> implements TwoStepMapper<T, Boolean> {

        private final Consumer<T> myActualConsumer;

        TwoStepWrapper(final Supplier<Consumer<T>> consumerFactory) {
            myActualConsumer = consumerFactory.get();
        }

        @Override
        public void consume(final T item) {
            myActualConsumer.accept(item);
        }

        @Override
        public Boolean getResults() {
            return Boolean.TRUE;
        }

        @Override
        public void reset() {
            // No need to (not possible to) reset, just continue
        }
    }

    private static final Consumer<Boolean> DUMMY = b -> {
        // Dummy no-op consumer
    };

    public static <T> BatchNode.Builder<T> newBuilder(final File directory, final DataInterpreter<T> interpreter) {
        return new BatchNode.Builder<>(directory, interpreter);
    }

    public static <T> BatchNode<T> newInstance(final File directory, final DataInterpreter<T> interpreter) {
        return BatchNode.newBuilder(directory, interpreter).build();
    }

    private final ToIntFunction<T> myDistributor;
    private final DataInterpreter<T> myInterpreter;
    private final IntSupplier myParallelism;
    private final ProcessingService myProcessor;
    private final int myQueueCapacity;
    private transient Function<File, AutoSupplier<T>> myReaderFactory = null;
    private final Throughput myReaderManager;
    private final ShardedFile myShards;
    private final Throughput myWriterManger;

    BatchNode(final BatchNode.Builder<T> builder) {

        super();

        myShards = builder.getShardedFile();
        myParallelism = builder.getParallelism();

        myInterpreter = builder.getInterpreter();
        myDistributor = builder.getDistributor();
        myProcessor = builder.getProcessor();
        myQueueCapacity = builder.getQueueCapacity();

        myWriterManger = new Throughput();
        myReaderManager = new Throughput();

        String name = builder.getName();
        MBeanUtils.register(myWriterManger, name + "-Writer");
        MBeanUtils.register(myReaderManager, name + "-Reader");
    }

    /**
     * Dispose of this node and explicitly delete all files.
     */
    public void dispose() {
        myShards.delete();
    }

    public AutoConsumer<T> newWriter() {
        return ToFileWriter.newBuilder(myShards).queue(myQueueCapacity).parallelism(myParallelism).statistics(myWriterManger).build(myDistributor,
                shard -> DataWriter.of(shard, myInterpreter));
    }

    /**
     * Process each and every item individually
     *
     * @param processor Must be able to consume concurrently
     */
    public void processAll(final Consumer<T> processor) {
        myProcessor.process(myShards.files(), myParallelism, shard -> this.process(shard, processor));
    }

    /**
     * Similar to {@link #processAll(Consumer)} but you provide a consumer constructor/factory rather than a
     * specific consumer. Internally there will be 1 consumer per worker thread instantiated. This variant is
     * for when the consumer(s) are stateful.
     */
    public void processAll(final Supplier<Consumer<T>> processorFactory) {
        this.processMergeable(() -> new TwoStepWrapper<>(processorFactory), DUMMY);
    }

    /**
     * Similar to {@link #processMergeable(Supplier, Consumer)} but the {@code processor} is called with the
     * aggregator instance itself rather than its extracted results. This corresponds to
     * {@link TwoStepMapper#Combineable} rather than {@link TwoStepMapper#Mergeable}.
     * 
     * @see #processMergeable(Supplier, Consumer)
     */
    public <R, A extends TwoStepMapper<T, R>> void processCombineable(final Supplier<A> aggregatorFactory, final Consumer<A> processor) {
        ThreadLocal<A> threadLocal = ThreadLocal.withInitial(aggregatorFactory);
        myProcessor.process(myShards.files(), myParallelism, shard -> this.processAggregators(shard, threadLocal::get, processor));
    }

    /**
     * Process mapped/derived data in batches.
     * <P>
     * There will be one {@link TwoStepMapper} instance per underlying file/shard – that's a batch. Those
     * instances are likely to contain some sort of {@link Collection} or {@link Map} that hold mapped/derived
     * data.
     * <p>
     * You must make sure that all data items that need to be in the same {@link TwoStepMapper} instance (in
     * the same batch) are in the same file/shard. You control the number of shards via
     * {@link Builder#fragmentation(int)} and which item goes in which shard via
     * {@link Builder#distributor(ToIntFunction)}.
     *
     * @param <R> The mapped/derived data holding type
     * @param aggregatorFactory Produces the {@link TwoStepMapper} mapping instances
     * @param processor Consumes the mapped/derived data - the results of one whole {@link TwoStepMapper}
     *        instance at the time
     * @deprecated v54 Use {@link #processMergeable(Supplier<? extends TwoStepMapper<T, H>>,Consumer<H>)}
     *             instead
     */
    @Deprecated
    public <R> void processMapped(final Supplier<? extends TwoStepMapper<T, R>> aggregatorFactory, final Consumer<R> processor) {
        this.processMergeable(aggregatorFactory, processor);
    }

    /**
     * Each shard is processed/aggregated separately by a {@link TwoStepMapper} instance. The results are then
     * processed/merged by the provided {@code processor}.
     * <p>
     * There is one {@link TwoStepMapper} instance per underlying worker thread. Those instances are reset and
     * reused for each shard.
     * <p>
     * The {@code processor} is called concurrently from multiple threads.
     * <P>
     * You must make sure that all data items that need to be in the same aggregator instance are in the same
     * file/shard. You control the number of shards via {@link Builder#fragmentation(int)} and which item goes
     * in which shard via {@link Builder#distributor(ToIntFunction)}.
     *
     * @param aggregatorFactory Produces the {@link TwoStepMapper} aggregator instances
     * @param processor Consumes the aggregated/derived data - the results of one whole {@link TwoStepMapper}
     *        instance at the time
     */
    public <R, A extends TwoStepMapper<T, R>> void processMergeable(final Supplier<A> aggregatorFactory, final Consumer<R> processor) {
        ThreadLocal<TwoStepMapper<T, R>> threadLocal = ThreadLocal.withInitial(aggregatorFactory);
        myProcessor.process(myShards.files(), myParallelism, shard -> this.processResults(shard, threadLocal::get, processor));
    }

    /**
     * Calls {@link #processCombineable(Supplier, Consumer)} with the
     * {@link TwoStepMapper.Combineable#merge(Object)} method of a global {@link TwoStepMapper.Combineable}
     * instance as the {@code consumer}.
     */
    public <R, A extends TwoStepMapper.Combineable<T, R, A>> R reduceByCombining(final Supplier<A> aggregatorFactory) {

        A globalAggregator = aggregatorFactory.get();

        this.processCombineable(aggregatorFactory, globalAggregator::combine);

        return globalAggregator.getResults();
    }

    /**
     * Calls {@link #processMergeable(Supplier, Consumer)} with the
     * {@link TwoStepMapper.Mergeable#merge(Object)} method of a global {@link TwoStepMapper.Mergeable}
     * instance as the {@code consumer}.
     */
    public <R, A extends TwoStepMapper.Mergeable<T, R>> R reduceByMerging(final Supplier<A> aggregatorFactory) {

        A globalAggregator = aggregatorFactory.get();

        this.processMergeable(aggregatorFactory, globalAggregator::merge);

        return globalAggregator.getResults();
    }

    /**
     * Same as {@link #processMergeable(Supplier, Consumer)}, but then also reduce/merge the total results
     * using {@link TwoStepMapper#merge(Object)}.
     * <P>
     * Create a class that implements {@link TwoStepMapper} and make sure to also implement
     * {@link TwoStepMapper#merge(Object)} - you can only use this if merging partial (sub)results is
     * possible. Use a constructor or factory method that produce instances of that type as the argument to
     * this method.
     * 
     * @deprecated v54 Use {@link #reduceByMerging(Supplier<A>)} instead
     */
    @Deprecated
    public <R, A extends TwoStepMapper.Mergeable<T, R>> R reduceMapped(final Supplier<A> aggregatorFactory) {
        return this.reduceByMerging(aggregatorFactory);
    }

    private Function<File, AutoSupplier<T>> getReaderFactory() {
        if (myReaderFactory == null) {
            Function<File, DataReader<T>> baseReader = file -> DataReader.of(file, myInterpreter);
            myReaderFactory = file -> FromFileReader.newBuilder(file).parallelism(1).queue(myQueueCapacity / myParallelism.getAsInt())
                    .statistics(myReaderManager).build(baseReader);
        }
        return myReaderFactory;
    }

    private void process(final File shard, final Consumer<T> consumer) {

        try (AutoSupplier<T> reader = this.newReader(shard)) {

            T item = null;
            while ((item = reader.read()) != null) {
                consumer.accept(item);
            }

        } catch (Exception cause) {
            throw new RuntimeException(cause);
        }

    }

    private <R, A extends TwoStepMapper<T, R>> void processAggregators(final File shard, final Supplier<A> aggregatorFactory, final Consumer<A> processor) {

        A aggregator = aggregatorFactory.get(); // It's a ThreadLocal...

        try (AutoSupplier<T> reader = this.newReader(shard)) {

            T item = null;
            while ((item = reader.read()) != null) {
                aggregator.consume(item);
            }

            processor.accept(aggregator);

            aggregator.reset(); // ...and needs to be reset.

        } catch (Exception cause) {
            throw new RuntimeException(cause);
        }
    }

    private <R, A extends TwoStepMapper<T, R>> void processResults(final File shard, final Supplier<A> aggregatorFactory, final Consumer<R> processor) {

        A aggregator = aggregatorFactory.get(); // It's a ThreadLocal...

        try (AutoSupplier<T> reader = this.newReader(shard)) {

            T item = null;
            while ((item = reader.read()) != null) {
                aggregator.consume(item);
            }

            processor.accept(aggregator.getResults());

            aggregator.reset(); // ...and needs to be reset.

        } catch (Exception cause) {
            throw new RuntimeException(cause);
        }
    }

    AutoSupplier<T> newReader(final File file) {
        return this.getReaderFactory().apply(file);
    }

}
