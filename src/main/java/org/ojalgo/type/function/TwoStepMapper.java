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
package org.ojalgo.type.function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.ojalgo.random.FrequencyMap;
import org.ojalgo.random.FrequencyMap.FrequencyPredicate;

/**
 * A {@link TwoStepMapper} is a mapper/reducer that is used in 2 steps – {@link #consume(Object)} and
 * {@link #getResults()}. First you consume some (many) elements, then you get the results. The results is one
 * instance of some aggregate type – most likely a collection or map containing derived information.
 * <p>
 * Resettable (to enable reuse).
 * <p>
 * There are 2 optional extensions to this interface: {@link Combineable} and {@link Mergeable}. The first
 * enables combining the state of multiple instances. The second enables merging the results from multiple
 * instances. The difference is that combining is done before the final results are calculated, while merging
 * is done after the final results are calculated. If {@link #getResults()} simply returns the internal state
 * of the instance, unaltered, then combining and merging are identical.
 *
 * @author apete
 */
public interface TwoStepMapper<T, R> {

    /**
     * Enables combining the state of multiple instances.
     */
    interface Combineable<T, R, A extends Combineable<T, R, A>> extends TwoStepMapper<T, R> {

        static <T, R, A extends Combineable<T, R, A>> A combine(final A target, final A other) {
            target.combine(other);
            return target;
        }

        /**
         * This method can be used to combine the state of multiple instances. The intermediate/internal state
         * from one instance is combined into another.
         */
        void combine(A other);

    }

    /**
     * A frequency counter that optionally filters some elements when merging subresults.
     */
    public static final class KeyCounter<T, G>
            implements TwoStepMapper.Combineable<T, FrequencyMap<G>, KeyCounter<T, G>>, TwoStepMapper.Mergeable<T, FrequencyMap<G>> {

        public static <T> KeyCounter<T, T> newInstance() {
            return new KeyCounter<>(Function.identity());
        }

        public static <T> KeyCounter<T, T> newInstance(final FrequencyPredicate<T> predicate) {
            return new KeyCounter<>(Function.identity(), predicate);
        }

        private final FrequencyMap<G> myCounter = new FrequencyMap<>();
        private final Function<T, G> myExtractor;
        private final FrequencyMap.FrequencyPredicate<G> myPredicate;

        /**
         * @param extractor The key extractor
         */
        public KeyCounter(final Function<T, G> extractor) {
            this(extractor, (element, statistics, frequency) -> true);
        }

        /**
         * @param extractor The key extractor
         * @param predicate Filter used when merging/combining results
         */
        public KeyCounter(final Function<T, G> extractor, final FrequencyPredicate<G> predicate) {
            super();
            myExtractor = extractor;
            myPredicate = predicate;
        }

        @Override
        public void combine(final KeyCounter<T, G> other) {
            myCounter.merge(other.getCounter(), myPredicate);
        }

        @Override
        public void consume(final T item) {
            myCounter.increment(myExtractor.apply(item));
        }

        @Override
        public FrequencyMap<G> getResults() {
            return myCounter;
        }

        @Override
        public void merge(final FrequencyMap<G> other) {
            myCounter.merge(other, myPredicate);
        }

        @Override
        public void reset() {
            myCounter.reset();
        }

        FrequencyMap<G> getCounter() {
            return myCounter;
        }

    }

    /**
     * Enables merging the results from multiple instances.
     */
    interface Mergeable<T, R> extends TwoStepMapper<T, R> {

        static <T, R, A extends Mergeable<T, R>> A merge(final A target, final A other) {
            target.merge(other.getResults());
            return target;
        }

        /**
         * This method can be used to merge the results from multiple instances. The final results from one
         * instance is merged into another.
         */
        void merge(R other);

    }

    /**
     * Will calculate the function value for each input item, and cache the result. The cache is used to avoid
     * re-calculating the same value multiple times. The final result is a complete map of all input output
     * pairs.
     */
    public static final class SimpleCache<K, V> implements TwoStepMapper.Combineable<K, Map<K, V>, SimpleCache<K, V>>, TwoStepMapper.Mergeable<K, Map<K, V>> {

        private final Map<K, V> myCache = new ConcurrentHashMap<>();
        private final Function<K, V> myFunction;

        public SimpleCache(final Function<K, V> function) {
            super();
            myFunction = function;
        }

        @Override
        public void combine(final SimpleCache<K, V> other) {
            myCache.putAll(other.getCache());
        }

        @Override
        public void consume(final K item) {
            myCache.computeIfAbsent(item, k -> myFunction.apply(k));
        }

        @Override
        public Map<K, V> getResults() {
            return myCache;
        }

        @Override
        public void merge(final Map<K, V> other) {
            myCache.putAll(other);
        }

        @Override
        public void reset() {
            myCache.clear();
        }

        Map<K, V> getCache() {
            return myCache;
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
     * Reset, so it can be re-used
     */
    void reset();

}
