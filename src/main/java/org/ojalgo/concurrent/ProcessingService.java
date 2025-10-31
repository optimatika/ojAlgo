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
package org.ojalgo.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.ojalgo.type.function.TwoStepMapper;

/**
 * A simple wrapper around an {@link ExecutorService} that makes it easier to process collections of items in
 * parallel. The work items are processed by a {@link Consumer}, {@link Function} or {@link TwoStepMapper}. In
 * particular the {@link TwoStepMapper} can be used to aggregate/reduce data from the work items and then
 * combine the collected data into a final result.
 */
public final class ProcessingService {

    static final class CallableConsumer<W> implements Callable<Boolean> {

        private final Consumer<W> myConsumer;
        private final Queue<W> myWork;

        CallableConsumer(final Queue<W> work, final Consumer<W> consumer) {
            super();
            myWork = work;
            myConsumer = consumer;
        }

        @Override
        public Boolean call() throws Exception {

            W item = null;
            while ((item = myWork.poll()) != null) {
                myConsumer.accept(item);
            }

            return Boolean.TRUE;
        }

    }

    static final class CallableMapper<W, R> implements Callable<TwoStepMapper<W, R>> {

        private final TwoStepMapper<W, R> myMapper;
        private final Queue<W> myWork;

        CallableMapper(final Queue<W> work, final TwoStepMapper<W, R> mapper) {
            super();
            myWork = work;
            myMapper = mapper;
        }

        @Override
        public TwoStepMapper<W, R> call() throws Exception {

            W item = null;
            while ((item = myWork.poll()) != null) {
                myMapper.consume(item);
            }

            return myMapper;
        }

    }

    public static final ProcessingService INSTANCE = new ProcessingService(DaemonPoolExecutor.INSTANCE);

    public static ProcessingService newInstance(final String name) {
        return new ProcessingService(DaemonPoolExecutor.newCachedThreadPool(name));
    }

    private final ExecutorService myExecutor;

    public ProcessingService(final ExecutorService executor) {
        super();
        myExecutor = executor;
    }

    /**
     * Using parallelism {@link Parallelism#CORES}.
     *
     * @see ProcessingService#compute(Collection, IntSupplier, Function)
     */
    public <W, R> Map<W, R> compute(final Collection<W> work, final Function<W, R> computer) {
        return this.compute(work, Parallelism.CORES, computer);
    }

    /**
     * Compute an output item for each (unique) input item, and return the results as a {@link Map}. If the
     * input contains duplicates, the output will have fewer items. It is therefore vital that the input type
     * implements {@link Object#hashCode()} and {@link Object#equals(Object)} properly.
     * <p>
     * Will create at most {@code parallelism} tasks to work through the {@code work} items, processing them
     * with {@code computer} and collectiing the results in a {@link Map}.
     *
     * @param <W>         The work item type
     * @param <R>         The function return type
     * @param work        The collection of work items
     * @param parallelism The maximum number of concurrent workers that will process the work items
     * @param computer    The processing code
     * @return A map of function input to output
     */
    public <W, R> Map<W, R> compute(final Collection<W> work, final int parallelism, final Function<W, R> computer) {
        return this.reduceMergeable(work, parallelism, () -> new TwoStepMapper.SimpleCache<>(computer));
    }

    /**
     * @see ProcessingService#compute(Collection, int, Function)
     */
    public <W, R> Map<W, R> compute(final Collection<W> work, final IntSupplier parallelism, final Function<W, R> computer) {
        return this.compute(work, parallelism.getAsInt(), computer);
    }

    /**
     * @deprecated v56 Use {@link #newDivider()} or {@link Parallelism#newDivider(int)} instead
     */
    @Deprecated
    public DivideAndConquer.Divider divider() {
        return this.newDivider();
    }

    /**
     * @return The underlying {@link ExecutorService}
     */
    public ExecutorService getExecutor() {
        return myExecutor;
    }

    /**
     * Using parallelism {@link Parallelism#CORES}.
     *
     * @see ProcessingService#map(Collection, IntSupplier, Function)
     */
    public <W, R> Collection<R> map(final Collection<W> work, final Function<W, R> mapper) {
        return this.map(work, Parallelism.CORES, mapper);
    }

    /**
     * Simply map each (unique) input item to an output item - a {@link Collection} of input results in a
     * {@link Collection} of output. If the input contains duplicates, the output will have fewer items. It is
     * therefore vital that the input type implements {@link Object#hashCode()} and
     * {@link Object#equals(Object)} properly.
     *
     * @param <W>         The input item type
     * @param <R>         The output item type
     * @param work        The collection of work items
     * @param parallelism The maximum number of concurrent workers that will process the work items
     * @param mapper      The mapper functiom
     * @return The mapped results
     */
    public <W, R> Collection<R> map(final Collection<W> work, final int parallelism, final Function<W, R> mapper) {
        return this.compute(work, parallelism, mapper).values();
    }

    /**
     * @see ProcessingService#map(Collection, int, Function)
     */
    public <W, R> Collection<R> map(final Collection<W> work, final IntSupplier parallelism, final Function<W, R> mapper) {
        return this.map(work, parallelism.getAsInt(), mapper);
    }

    public DivideAndConquer.Divider newDivider() {
        return new DivideAndConquer.Divider(myExecutor);
    }

    public <T> AtomicBoolean poll(final BlockingQueue<T> queue, final int parallelism, final Consumer<T> processor) {

        AtomicBoolean active = new AtomicBoolean(true);

        for (int i = 0; i < parallelism; i++) {
            myExecutor.submit(() -> {
                while (active.get()) {
                    try {
                        T item = queue.poll(100L, TimeUnit.MILLISECONDS);
                        if (item != null) {
                            processor.accept(item);
                        }
                    } catch (InterruptedException cause) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        return active;
    }

    public <T> AtomicBoolean poll(final BlockingQueue<T> queue, final IntSupplier parallelism, final Consumer<T> processor) {
        return this.poll(queue, parallelism.getAsInt(), processor);
    }

    /**
     * Using parallelism {@link Parallelism#CORES}.
     *
     * @see ProcessingService#process(Collection, IntSupplier, Consumer)
     */
    public <W> void process(final Collection<? extends W> work, final Consumer<W> processor) {
        this.process(work, Parallelism.CORES, processor);
    }

    /**
     * Will create at most {@code parallelism} tasks to work through the {@code work} items, processing them
     * with {@code processor}.
     *
     * @param <W>         The work item type
     * @param work        The collection of work items
     * @param parallelism The maximum number of concurrent workers that will process the work items
     * @param processor   The processing code
     */
    public <W> void process(final Collection<? extends W> work, final int parallelism, final Consumer<W> processor) {

        int concurrency = Math.min(work.size(), parallelism);

        Queue<W> queue = new LinkedTransferQueue<>(work);

        List<CallableConsumer<W>> tasks = new ArrayList<>(concurrency);
        for (int i = 0; i < concurrency; i++) {
            tasks.add(new CallableConsumer<>(queue, processor));
        }

        try {
            List<Future<Boolean>> futures = myExecutor.invokeAll(tasks);
            for (int i = 0; i < concurrency; i++) {
                try {
                    futures.get(i).get();
                } catch (InterruptedException | ExecutionException cause) {
                    for (int j = 0; j < concurrency; j++) {
                        if (j != i) {
                            futures.get(j).cancel(true);
                        }
                    }
                    if (cause instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    throw new RuntimeException(cause);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * @see ProcessingService#process(Collection, int, Consumer)
     */
    public <W> void process(final Collection<? extends W> work, final IntSupplier parallelism, final Consumer<W> processor) {
        this.process(work, parallelism.getAsInt(), processor);
    }

    /**
     * Just 2 work items.
     *
     * @see #process(Collection, Consumer)
     */
    public <W> void processPair(final W work1, final W work2, final Consumer<W> processor) {
        this.process(Arrays.asList(work1, work2), processor);
    }

    /**
     * Just 3 work items.
     *
     * @see #process(Collection, Consumer)
     */
    public <W> void processTriplet(final W work1, final W work2, final W work3, final Consumer<W> processor) {
        this.process(Arrays.asList(work1, work2, work3), processor);
    }

    /**
     * @deprecated v54 Use {@link #reduceMergeable(Collection<W>,int,Supplier<? extends
     *             TwoStepMapper.Mergeable<W, R>>)} instead
     */
    @Deprecated
    public <W, R> R reduce(final Collection<W> work, final int parallelism, final Supplier<? extends TwoStepMapper.Mergeable<W, R>> reducer) {
        return this.reduceMergeable(work, parallelism, reducer);
    }

    /**
     * @deprecated v54 Use {@link #reduceMergeable(Collection<W>,IntSupplier,Supplier<? extends
     *             TwoStepMapper.Mergeable<W, R>>)} instead
     */
    @Deprecated
    public <W, R> R reduce(final Collection<W> work, final IntSupplier parallelism, final Supplier<? extends TwoStepMapper.Mergeable<W, R>> reducer) {
        return this.reduceMergeable(work, parallelism, reducer);
    }

    /**
     * @deprecated v54 Use {@link #reduceMergeable(Collection<W>,Supplier<? extends TwoStepMapper.Mergeable<W,
     *             R>>)} instead
     */
    @Deprecated
    public <W, R> R reduce(final Collection<W> work, final Supplier<? extends TwoStepMapper.Mergeable<W, R>> reducer) {
        return this.reduceMergeable(work, reducer);
    }

    /**
     * Will create at most {@code parallelism} tasks to work through the {@code work} items, processing them
     * with {@code reducer}. The state of each task's {@code reducer} will be combined into a single instance,
     * and the results of that instance will be returned.
     * <p>
     * Each {@link TwoStepMapper.Combineable} is only worked on by a single thread, and the results are
     * combined into a single instance. The instances are not reused.
     *
     * @param work        The collection of work items
     * @param parallelism The maximum number of concurrent workers that will process the work items
     * @param reducer     A {@link TwoStepMapper.Combineable} implementation that does what you want.
     * @return The results...
     */
    public <W, R, A extends TwoStepMapper.Combineable<W, R, A>> R reduceCombineable(final Collection<W> work, final int parallelism,
            final Supplier<A> reducer) {

        int concurrency = Math.min(work.size(), parallelism);

        Queue<W> queue = new LinkedTransferQueue<>(work);

        List<CallableMapper<W, R>> tasks = new ArrayList<>(concurrency);
        for (int i = 0; i < concurrency; i++) {
            tasks.add(new CallableMapper<>(queue, reducer.get()));
        }

        A totalResults = reducer.get();

        try {
            List<Future<TwoStepMapper<W, R>>> futures = myExecutor.invokeAll(tasks);
            for (int i = 0; i < concurrency; i++) {
                try {
                    A partialResults = (A) futures.get(i).get();
                    totalResults.combine(partialResults);
                    partialResults.reset();
                } catch (InterruptedException | ExecutionException cause) {
                    for (int j = 0; j < concurrency; j++) {
                        if (j != i) {
                            futures.get(j).cancel(true);
                        }
                    }
                    if (cause instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    throw new RuntimeException(cause);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        return totalResults.getResults();
    }

    /**
     * @see ProcessingService#reduceCombineable(Collection, int, Supplier)
     */
    public <W, R, A extends TwoStepMapper.Combineable<W, R, A>> R reduceCombineable(final Collection<W> work, final IntSupplier parallelism,
            final Supplier<A> reducer) {
        return this.reduceCombineable(work, parallelism.getAsInt(), reducer);
    }

    /**
     * Using parallelism {@link Parallelism#CORES}.
     *
     * @see ProcessingService#reduceCombineable(Collection, int, Supplier)
     */
    public <W, R, A extends TwoStepMapper.Combineable<W, R, A>> R reduceCombineable(final Collection<W> work, final Supplier<A> reducer) {
        return this.reduceCombineable(work, Parallelism.CORES, reducer);
    }

    /**
     * Will create at most {@code parallelism} tasks to work through the {@code work} items, processing them
     * with {@code reducer}. The results of each task's {@code reducer} will be merged into a single instance,
     * and the results of that instance will be returned.
     * <p>
     * Each {@link TwoStepMapper.Mergeable} is only worked on by a single thread, and the results are combined
     * into a single instance. The instances are not reused.
     *
     * @param work        The collection of work items
     * @param parallelism The maximum number of concurrent workers that will process the work items
     * @param reducer     A {@link TwoStepMapper.Mergeable} implementation that does what you want.
     * @return The results...
     */
    public <W, R, A extends TwoStepMapper.Mergeable<W, R>> R reduceMergeable(final Collection<W> work, final int parallelism, final Supplier<A> reducer) {

        int concurrency = Math.min(work.size(), parallelism);

        Queue<W> queue = new LinkedTransferQueue<>(work);

        List<CallableMapper<W, R>> tasks = new ArrayList<>(concurrency);
        for (int i = 0; i < concurrency; i++) {
            tasks.add(new CallableMapper<>(queue, reducer.get()));
        }

        A totalResults = reducer.get();

        try {
            List<Future<TwoStepMapper<W, R>>> futures = myExecutor.invokeAll(tasks);
            for (int i = 0; i < concurrency; i++) {
                try {
                    TwoStepMapper<W, R> partialResults = futures.get(i).get();
                    totalResults.merge(partialResults.getResults());
                    partialResults.reset();
                } catch (InterruptedException | ExecutionException cause) {
                    for (int j = 0; j < concurrency; j++) {
                        if (j != i) {
                            futures.get(j).cancel(true);
                        }
                    }
                    if (cause instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    throw new RuntimeException(cause);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        return totalResults.getResults();
    }

    /**
     * @see ProcessingService#reduceMergeable(Collection, int, Supplier)
     */
    public <W, R, A extends TwoStepMapper.Mergeable<W, R>> R reduceMergeable(final Collection<W> work, final IntSupplier parallelism,
            final Supplier<A> reducer) {
        return this.reduceMergeable(work, parallelism.getAsInt(), reducer);
    }

    /**
     * Using parallelism {@link Parallelism#CORES}.
     *
     * @see ProcessingService#reduceMergeable(Collection, int, Supplier)
     */
    public <W, R, A extends TwoStepMapper.Mergeable<W, R>> R reduceMergeable(final Collection<W> work, final Supplier<A> reducer) {
        return this.reduceMergeable(work, Parallelism.CORES, reducer);
    }

    /**
     * Will create precisely {@code parallelism} tasks that each execute the {@code processor}.
     *
     * @param parallelism The number of concurrent workers/threads that will run
     * @param processor   The processing code
     */
    public void run(final int parallelism, final Runnable processor) {

        List<Callable<Object>> tasks = new ArrayList<>(parallelism);
        for (int i = 0; i < parallelism; i++) {
            tasks.add(Executors.callable(processor));
        }

        try {
            List<Future<Object>> futures = myExecutor.invokeAll(tasks);
            for (int i = 0; i < parallelism; i++) {
                try {
                    futures.get(i).get();
                } catch (InterruptedException | ExecutionException cause) {
                    for (int j = 0; j < parallelism; j++) {
                        if (j != i) {
                            futures.get(j).cancel(true);
                        }
                    }
                    if (cause instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    throw new RuntimeException(cause);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * @see ProcessingService#run(int, Runnable)
     */
    public void run(final IntSupplier parallelism, final Runnable processor) {
        this.run(parallelism.getAsInt(), processor);
    }

    public void run(final Runnable task1, final Runnable task2) {

        Future<?> future1 = myExecutor.submit(task1);
        Future<?> future2 = myExecutor.submit(task2);

        try {
            future1.get();
            future2.get();
        } catch (InterruptedException | ExecutionException cause) {
            future1.cancel(true);
            future2.cancel(true);
            if (cause instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException(cause);
        }
    }

    /**
     * Will submit precisely {@code parallelism} tasks that each take from the {@code queue} feeding the items
     * to the {@code processor}. The tasks will continue to run until the returned {@link AtomicBoolean} is
     * set to {@code false} (or the thread is interrupted).
     * <p>
     * If the threads of the underlying {@link ExecutorService} are daemon threads, the JVM will not wait for
     * them to finish before it exits. The default behaviour, using {@link #INSTANCE} or
     * {@link #newInstance(String)}, is to make use of ojAlgo's {@link DaemonPoolExecutor}.
     * <p>
     * Although the method name is {@code take}, it is using {@link BlockingQueue#poll(long, TimeUnit)}
     * internally. This is to ensure that the tasks periodically check the {@link AtomicBoolean} flag, and can
     * be terminated.
     *
     * @param <T>         The work item type
     * @param queue       The queue to take from
     * @param parallelism How many parallel workers to create
     * @param processor   What to do with each of the work items
     * @return A flag that can be used to signal the tasks to stop
     */
    public <T> void take(final BlockingQueue<T> queue, final int parallelism, final Consumer<T> processor) {

        for (int i = 0; i < parallelism; i++) {
            myExecutor.submit(() -> {
                while (true) {
                    try {
                        processor.accept(queue.take());
                    } catch (InterruptedException cause) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
    }

    /**
     * @see ProcessingService#take(BlockingQueue, int, Consumer)
     */
    public <T> void take(final BlockingQueue<T> queue, final IntSupplier parallelism, final Consumer<T> processor) {
        this.take(queue, parallelism.getAsInt(), processor);
    }

}