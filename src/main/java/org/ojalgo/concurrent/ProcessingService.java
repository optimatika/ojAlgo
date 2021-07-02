package org.ojalgo.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;

public final class ProcessingService {

    static final class CallableConsumer<W> implements Callable<Boolean> {

        private final Consumer<W> myConsumer;
        private final Queue<W> myWork;

        CallableConsumer(final Queue<W> work, final Consumer<W> consumer) {
            super();
            myWork = work;
            myConsumer = consumer;
        }

        public Boolean call() throws Exception {

            W item = null;
            while ((item = myWork.poll()) != null) {
                myConsumer.accept(item);
            }

            return Boolean.TRUE;
        }

    }

    static final class CallableFunction<W, R> implements Callable<Map<W, R>> {

        private final Function<W, R> myFunction;
        private final Map<W, R> myResults;
        private final Queue<W> myWork;

        CallableFunction(final Queue<W> work, final Function<W, R> function, final Map<W, R> results) {
            super();
            myWork = work;
            myFunction = function;
            myResults = results;
        }

        public Map<W, R> call() throws Exception {

            W item = null;
            while ((item = myWork.poll()) != null) {
                myResults.computeIfAbsent(item, k -> myFunction.apply(k));
            }

            return myResults;
        }

    }

    static final ProcessingService INSTANCE = new ProcessingService(DaemonPoolExecutor.INSTANCE);

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
     * @see #compute(Collection, IntSupplier, Function)
     */
    public <W, R> Map<W, R> compute(final Collection<W> work, final Function<W, R> processor) {
        return this.compute(work, Parallelism.CORES, processor);
    }

    /**
     * Will create at most {@code parallelism} tasks to work through the {@code work} items, processing them
     * with {@code processor} and collectiing the results in a {@link Map}.
     *
     * @param <W> The work item type
     * @param <R> The function return type
     * @param work The collection of work items
     * @param parallelism The maximum number of concurrent workers that will process the work items
     * @param processor The processing code
     * @return A map of function input to output
     */
    public <W, R> Map<W, R> compute(final Collection<W> work, final IntSupplier parallelism, final Function<W, R> processor) {

        int load = work.size();
        int concurrency = Math.min(load, parallelism.getAsInt());

        Queue<W> queue = new LinkedBlockingDeque<>(work);
        Map<W, R> results = new ConcurrentHashMap<>(load);

        List<CallableFunction<W, R>> tasks = new ArrayList<>(concurrency);
        for (int i = 0; i < concurrency; i++) {
            tasks.add(new CallableFunction<>(queue, processor, results));
        }

        try {
            for (Future<Map<W, R>> future : myExecutor.invokeAll(tasks)) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException cause) {
            throw new RuntimeException(cause);
        }

        return results;
    }

    /**
     * @see #compute(Collection, Function)
     */
    public <W, R> Map<W, R> computePair(final W work1, final W work2, final Function<W, R> processor) {
        return this.compute(Arrays.asList(work1, work2), processor);
    }

    /**
     * @see #compute(Collection, Function)
     */
    public <W, R> Map<W, R> computeTriplet(final W work1, final W work2, final W work3, final Function<W, R> processor) {
        return this.compute(Arrays.asList(work1, work2, work3), processor);
    }

    public DivideAndConquer.Divider divider() {
        return new DivideAndConquer.Divider(myExecutor);
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
     * @see #process(Collection, IntSupplier, Consumer)
     */
    public <W> void process(final Collection<? extends W> work, final Consumer<W> processor) {
        this.process(work, Parallelism.CORES, processor);
    }

    /**
     * Will create at most {@code parallelism} tasks to work through the {@code work} items, processing them
     * with {@code processor}.
     *
     * @param <W> The work item type
     * @param work The collection of work items
     * @param parallelism The maximum number of concurrent workers that will process the work items
     * @param processor The processing code
     */
    public <W> void process(final Collection<? extends W> work, final IntSupplier parallelism, final Consumer<W> processor) {

        int load = work.size();
        int concurrency = Math.min(load, parallelism.getAsInt());

        Queue<W> queue = new LinkedBlockingDeque<>(work);

        List<CallableConsumer<W>> tasks = new ArrayList<>(concurrency);
        for (int i = 0; i < concurrency; i++) {
            tasks.add(new CallableConsumer<>(queue, processor));
        }

        try {
            for (Future<Boolean> future : myExecutor.invokeAll(tasks)) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException cause) {
            throw new RuntimeException(cause);
        }
    }

    /**
     * @see #process(Collection, Consumer)
     */
    public <W> void processPair(final W work1, final W work2, final Consumer<W> processor) {
        this.process(Arrays.asList(work1, work2), processor);
    }

    /**
     * @see #process(Collection, Consumer)
     */
    public <W> void processTriplet(final W work1, final W work2, final W work3, final Consumer<W> processor) {
        this.process(Arrays.asList(work1, work2, work3), processor);
    }

    /**
     * Will create precisely {@code parallelism} tasks that each execute the {@code processor}.
     *
     * @param parallelism The number of concurrent workers/threads that will run
     * @param processor The processing code
     */
    public void run(final IntSupplier parallelism, final Runnable processor) {

        int concurrency = parallelism.getAsInt();

        List<Callable<Object>> tasks = new ArrayList<>(concurrency);
        for (int i = 0; i < concurrency; i++) {
            tasks.add(Executors.callable(processor));
        }

        try {
            for (Future<Object> future : myExecutor.invokeAll(tasks)) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException cause) {
            throw new RuntimeException(cause);
        }
    }

}
