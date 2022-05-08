package org.ojalgo.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.ojalgo.type.function.TwoStepMapper;

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

    static final class CallableMapper<W, R> implements Callable<TwoStepMapper<W, R>> {

        private final TwoStepMapper<W, R> myMapper;
        private final Queue<W> myWork;

        CallableMapper(final Queue<W> work, final TwoStepMapper<W, R> mapper) {
            super();
            myWork = work;
            myMapper = mapper;
        }

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
     * @param <W> The work item type
     * @param <R> The function return type
     * @param work The collection of work items
     * @param parallelism The maximum number of concurrent workers that will process the work items
     * @param computer The processing code
     * @return A map of function input to output
     */
    public <W, R> Map<W, R> compute(final Collection<W> work, final int parallelism, final Function<W, R> computer) {
        return this.reduce(work, parallelism, () -> new TwoStepMapper.SimpleCache<>(computer));
    }

    /**
     * @see ProcessingService#compute(Collection, int, Function)
     */
    public <W, R> Map<W, R> compute(final Collection<W> work, final IntSupplier parallelism, final Function<W, R> computer) {
        return this.compute(work, parallelism.getAsInt(), computer);
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
     * @param <W> The input item type
     * @param <R> The output item type
     * @param work The collection of work items
     * @param parallelism The maximum number of concurrent workers that will process the work items
     * @param mapper The mapper functiom
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
     * @param <W> The work item type
     * @param work The collection of work items
     * @param parallelism The maximum number of concurrent workers that will process the work items
     * @param processor The processing code
     */
    public <W> void process(final Collection<? extends W> work, final int parallelism, final Consumer<W> processor) {

        int concurrency = Math.min(work.size(), parallelism);

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
     * Will (map and) reduce the collection of input work items to 1 single, but arbitrarily large/complex,
     * output instance.
     *
     * @param <W> The work item type
     * @param <R> The output type
     * @param work The collection of work items
     * @param parallelism The maximum number of concurrent workers that will process the work items
     * @param reducer Providing a {@link TwoStepMapper} implementation that does what you want is the key.
     * @return The results...
     */
    public <W, R> R reduce(final Collection<W> work, final int parallelism, final Supplier<TwoStepMapper<W, R>> reducer) {

        int concurrency = Math.min(work.size(), parallelism);

        Queue<W> queue = new LinkedBlockingDeque<>(work);

        List<CallableMapper<W, R>> tasks = new ArrayList<>(concurrency);
        for (int i = 0; i < concurrency; i++) {
            tasks.add(new CallableMapper<>(queue, reducer.get()));
        }

        TwoStepMapper<W, R> totalResults = reducer.get();

        try {
            for (Future<TwoStepMapper<W, R>> future : myExecutor.invokeAll(tasks)) {
                TwoStepMapper<W, R> mapper = future.get();
                totalResults.merge(mapper.getResults());
                mapper.reset();
            }
        } catch (InterruptedException | ExecutionException cause) {
            throw new RuntimeException(cause);
        }

        return totalResults.getResults();
    }

    /**
     * @see ProcessingService#reduce(Collection, int, Supplier)
     */
    public <W, R> R reduce(final Collection<W> work, final IntSupplier parallelism, final Supplier<TwoStepMapper<W, R>> reducer) {
        return this.reduce(work, parallelism.getAsInt(), reducer);
    }

    /**
     * @see ProcessingService#reduce(Collection, int, Supplier)
     */
    public <W, R> R reduce(final Collection<W> work, final Supplier<TwoStepMapper<W, R>> reducer) {
        return this.reduce(work, Parallelism.CORES, reducer);
    }

    /**
     * Will create precisely {@code parallelism} tasks that each execute the {@code processor}.
     *
     * @param parallelism The number of concurrent workers/threads that will run
     * @param processor The processing code
     */
    public void run(final int parallelism, final Runnable processor) {

        List<Callable<Object>> tasks = new ArrayList<>(parallelism);
        for (int i = 0; i < parallelism; i++) {
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

    /**
     * @see ProcessingService#run(int, Runnable)
     */
    public void run(final IntSupplier parallelism, final Runnable processor) {
        this.run(parallelism.getAsInt(), processor);
    }

}
