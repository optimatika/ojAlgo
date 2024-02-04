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
package org.ojalgo.random;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import org.ojalgo.function.constant.PrimitiveMath;

/**
 * Count the occurrence of different keys
 */
public final class FrequencyMap<T> {

    @FunctionalInterface
    public interface FrequencyPredicate<T> {

        boolean test(T element, SampleSet statistics, long frequency);

    }

    private final ConcurrentHashMap<T, LongAdder> myMap = new ConcurrentHashMap<>();
    private final LongAdder myPopulation = new LongAdder();

    public FrequencyMap() {
        super();
    }

    /**
     * @param initial An initial set of elements to add to the frequency map (with frequency zero)
     */
    public FrequencyMap(final Iterable<? extends T> initial) {

        super();

        for (T element : initial) {
            this.get(element);
        }
    }

    public void add(final T element, final long count) {
        if (count < 0L) {
            throw new IllegalArgumentException();
        }
        this.get(element).add(count);
        myPopulation.add(count);
    }

    public void addAll(final Map<? extends T, ? extends Number> frequencies) {
        for (Entry<? extends T, ? extends Number> entry : frequencies.entrySet()) {
            this.add(entry.getKey(), entry.getValue().longValue());
        }
    }

    public void addAll(final Set<? extends T> elements, final long count) {
        for (T element : elements) {
            this.add(element, count);
        }
    }

    /**
     * @return All known elements
     */
    public Set<T> elements() {
        return myMap.keySet();
    }

    /**
     * @param predicate An element filter
     * @return A set of elements that pass the filter
     */
    public Set<T> elements(final FrequencyPredicate<T> predicate) {

        Set<T> retVal = new HashSet<>();

        SampleSet statistics = this.sample();

        for (Entry<T, LongAdder> entry : myMap.entrySet()) {
            T element = entry.getKey();
            if (predicate.test(element, statistics, entry.getValue().longValue())) {
                retVal.add(element);
            }
        }

        return retVal;
    }

    public long getFrequency(final T element) {
        return myMap.getOrDefault(element, new LongAdder()).longValue();
    }

    public long getMaximumFrequenecy() {
        Entry<T, LongAdder> entryWithHighestFrequenecy = this.getEntryWithHighestFrequenecy();
        if (entryWithHighestFrequenecy != null) {
            return entryWithHighestFrequenecy.getValue().longValue();
        }
        return 0L;
    }

    public T getMode() {
        Entry<T, LongAdder> entryWithHighestFrequenecy = this.getEntryWithHighestFrequenecy();
        if (entryWithHighestFrequenecy != null) {
            return entryWithHighestFrequenecy.getKey();
        }
        return null;
    }

    public double getRelativeFrequency(final T element) {

        long populationSize = this.populationSize();

        if (populationSize == 0L) {
            return PrimitiveMath.ZERO;
        }

        return (double) this.getFrequency(element) / (double) populationSize;
    }

    public void increment(final T element) {
        this.get(element).increment();
        myPopulation.increment();
    }

    public void incrementAll(final Iterable<? extends T> elements) {
        for (T element : elements) {
            this.increment(element);
        }
    }

    public void merge(final FrequencyMap<T> other) {
        this.addAll(other.getMap());
    }

    public void merge(final FrequencyMap<T> other, final FrequencyPredicate<T> predicate) {

        SampleSet statistics = other.sample();

        for (Entry<T, LongAdder> entry : other.getMap().entrySet()) {
            T element = entry.getKey();
            long frequency = entry.getValue().longValue();
            if (predicate.test(element, statistics, frequency)) {
                this.add(element, frequency);
            }
        }
    }

    public int numberOfKnownKeys() {
        return myMap.size();
    }

    public long populationSize() {
        return myPopulation.longValue();
    }

    public void reset() {
        myMap.clear();
        myPopulation.reset();
    }

    /**
     * Remove entries that do not satisfy the predicate - remove elements that would not be returned by
     * {@link #elements(FrequencyPredicate)}.
     */
    public void retainIf(final FrequencyPredicate<T> predicate) {

        SampleSet statistics = this.sample();

        for (Entry<T, LongAdder> entry : myMap.entrySet()) {
            T element = entry.getKey();
            if (!predicate.test(element, statistics, entry.getValue().longValue())) {
                myMap.remove(element);
            }
        }
    }

    public SampleSet sample() {

        Set<Entry<T, LongAdder>> entries = myMap.entrySet();

        double[] frequencies = new double[entries.size()];

        int index = 0;
        for (Entry<T, LongAdder> entry : entries) {
            frequencies[index++] = entry.getValue().doubleValue();
        }

        return SampleSet.wrap(frequencies);
    }

    private LongAdder get(final T element) {
        return myMap.computeIfAbsent(element, k -> new LongAdder());
    }

    private Entry<T, LongAdder> getEntryWithHighestFrequenecy() {

        Entry<T, LongAdder> retVal = null;
        long maximum = 0L;

        for (Entry<T, LongAdder> entry : myMap.entrySet()) {
            if (entry.getValue().longValue() > maximum) {
                maximum = entry.getValue().longValue();
                retVal = entry;
            }
        }

        return retVal;
    }

    ConcurrentHashMap<T, LongAdder> getMap() {
        return myMap;
    }

}
