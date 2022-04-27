/*
 * Copyright 1997-2022 Optimatika
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.ojalgo.ProgrammingError;
import org.ojalgo.random.FrequencyMap;
import org.ojalgo.random.FrequencyMap.FrequencyPredicate;

/**
 * Map/convert from one type to another in 2 steps – {@link #consume(Object)} and {@link #getResults()}.
 * <p>
 * Also resettable (to enable reuse) and possibly mergeable (reduce).
 * <p>
 *
 * @author apete
 */
public interface TwoStepMapper<T, R> {

    /**
     * A frequency counter that optionally filters some elements when merging subresults.
     */
    public static final class FrequencyCounter<T, G> implements TwoStepMapper<T, FrequencyMap<G>> {

        private final FrequencyMap<G> myCounter = new FrequencyMap<>();
        private final Function<T, G> myExtractor;
        private final FrequencyMap.FrequencyPredicate<G> myPredicate;

        public FrequencyCounter(final Function<T, G> extractor) {
            this(extractor, (element, statistics, frequency) -> true);
        }

        public FrequencyCounter(final Function<T, G> extractor, final FrequencyPredicate<G> predicate) {
            super();
            myExtractor = extractor;
            myPredicate = predicate;
        }

        public void consume(final T item) {
            myCounter.increment(myExtractor.apply(item));
        }

        public FrequencyMap<G> getResults() {
            return myCounter;
        }

        public void merge(final FrequencyMap<G> aggregate) {
            myCounter.merge(aggregate, myPredicate);
        }

        public void reset() {
            myCounter.reset();
        }

    }

    /**
     * A very simple implementation...
     */
    public static final class SimpleCache<K, V> implements TwoStepMapper<K, Map<K, V>> {

        private final Function<K, V> myFunction;
        private final Map<K, V> myCache = new ConcurrentHashMap<>();

        public SimpleCache(final Function<K, V> function) {
            super();
            myFunction = function;
        }

        public void consume(final K item) {
            myCache.computeIfAbsent(item, k -> myFunction.apply(k));
        }

        public Map<K, V> getResults() {
            return myCache;
        }

        public void merge(final Map<K, V> aggregate) {
            myCache.putAll(aggregate);
        }

        public void reset() {
            myCache.clear();
        }

    }

    /**
     * Input/consume the items that should be mapped
     */
    void consume(T item);

    /**
     * Output the mapped results
     */
    R getResults();

    /**
     * Merge partial result (from another {@link TwoStepMapper}). Only some use cases of this interface makes
     * use of this merge functionality (not always possible to do simple merge).
     */
    default void merge(final R aggregate) {
        throw new ProgrammingError("Override to implement! " + aggregate);
    }

    /**
     * Reset, so it can be re-used
     */
    void reset();

}
