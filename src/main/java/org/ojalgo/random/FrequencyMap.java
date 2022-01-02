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
 *
 * @author apete
 */
public final class FrequencyMap<T> {

    @FunctionalInterface
    public interface FrequencyPredicate<T> {

        boolean test(T element, SampleSet properties, long frequency);

    }

    private final ConcurrentHashMap<T, LongAdder> myFrequencyMap = new ConcurrentHashMap<>();
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

    /**
     * @return The population size
     */
    public long count() {
        return myPopulation.longValue();
    }

    /**
     * @return All known elements
     */
    public Set<T> elements() {
        return myFrequencyMap.keySet();
    }

    /**
     * @param predicate An element filter
     * @return A set of elements that pass the filter
     */
    public Set<T> elements(final FrequencyPredicate<T> predicate) {

        Set<T> retVal = new HashSet<>();

        SampleSet sample = this.sample();

        for (Entry<T, LongAdder> entry : myFrequencyMap.entrySet()) {
            if (predicate.test(entry.getKey(), sample, entry.getValue().longValue())) {
                retVal.add(entry.getKey());
            }
        }

        return retVal;
    }

    public long getFrequency(final T element) {
        return myFrequencyMap.getOrDefault(element, new LongAdder()).longValue();
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
        if (myPopulation.sum() == 0L) {
            return PrimitiveMath.ZERO;
        }
        return this.getFrequency(element) / myPopulation.doubleValue();
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

    public void reset() {
        myFrequencyMap.clear();
        myPopulation.reset();
    }

    public SampleSet sample() {

        Set<Entry<T, LongAdder>> entries = myFrequencyMap.entrySet();

        double[] frequencies = new double[entries.size()];

        int index = 0;
        for (Entry<T, LongAdder> entry : entries) {
            frequencies[index++] = entry.getValue().doubleValue();
        }

        return SampleSet.wrap(frequencies);
    }

    private LongAdder get(final T element) {
        return myFrequencyMap.computeIfAbsent(element, k -> new LongAdder());
    }

    private Entry<T, LongAdder> getEntryWithHighestFrequenecy() {

        Entry<T, LongAdder> retVal = null;
        long maximum = 0L;

        for (Entry<T, LongAdder> entry : myFrequencyMap.entrySet()) {
            if (entry.getValue().longValue() > maximum) {
                maximum = entry.getValue().longValue();
                retVal = entry;
            }
        }

        return retVal;
    }

}
