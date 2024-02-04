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
package org.ojalgo.series;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.ojalgo.netio.ASCII;
import org.ojalgo.type.ColourData;
import org.ojalgo.type.TypeUtils;

abstract class TreeSeries<K extends Comparable<? super K>, V extends Comparable<V>, I extends TreeSeries<K, V, I>>
        implements NavigableMap<K, V>, BasicSeries<K, V> {

    private ColourData myColour = null;
    private final NavigableMap<K, V> myDelegate;
    private String myName = null;

    protected TreeSeries(final NavigableMap<K, V> delegate) {
        super();
        myDelegate = delegate;
    }

    public Entry<K, V> ceilingEntry(final K key) {
        return myDelegate.ceilingEntry(key);
    }

    public K ceilingKey(final K key) {
        return myDelegate.ceilingKey(key);
    }

    public void clear() {
        myDelegate.clear();
    }

    public final I colour(final ColourData colour) {
        return (I) BasicSeries.super.colour(colour);
    }

    public Comparator<? super K> comparator() {
        return myDelegate.comparator();
    }

    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return myDelegate.compute(key, remappingFunction);
    }

    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        return myDelegate.computeIfAbsent(key, mappingFunction);
    }

    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return myDelegate.computeIfPresent(key, remappingFunction);
    }

    public boolean containsKey(final Object key) {
        return myDelegate.containsKey(key);
    }

    public boolean containsValue(final Object value) {
        return myDelegate.containsValue(value);
    }

    public NavigableSet<K> descendingKeySet() {
        return myDelegate.descendingKeySet();
    }

    public NavigableMap<K, V> descendingMap() {
        return myDelegate.descendingMap();
    }

    public Set<Entry<K, V>> entrySet() {
        return myDelegate.entrySet();
    }

    @Override
    public boolean equals(final Object o) {
        return myDelegate.equals(o);
    }

    public Entry<K, V> firstEntry() {
        return myDelegate.firstEntry();
    }

    public K firstKey() {
        return myDelegate.firstKey();
    }

    public Entry<K, V> floorEntry(final K key) {
        return myDelegate.floorEntry(key);
    }

    public K floorKey(final K key) {
        return myDelegate.floorKey(key);
    }

    public void forEach(final BiConsumer<? super K, ? super V> action) {
        myDelegate.forEach(action);
    }

    public V get(final K key) {
        return myDelegate.get(key);
    }

    public V get(final Object key) {
        return myDelegate.get(key);
    }

    public ColourData getColour() {
        return myColour;
    }

    public String getName() {
        return myName;
    }

    public V getOrDefault(final Object key, final V defaultValue) {
        return myDelegate.getOrDefault(key, defaultValue);
    }

    @Override
    public int hashCode() {
        return myDelegate.hashCode();
    }

    public SortedMap<K, V> headMap(final K toKey) {
        return myDelegate.headMap(toKey);
    }

    public NavigableMap<K, V> headMap(final K toKey, final boolean inclusive) {
        return myDelegate.headMap(toKey, inclusive);
    }

    public Entry<K, V> higherEntry(final K key) {
        return myDelegate.higherEntry(key);
    }

    public K higherKey(final K key) {
        return myDelegate.higherKey(key);
    }

    public boolean isEmpty() {
        return myDelegate.isEmpty();
    }

    public Set<K> keySet() {
        return myDelegate.keySet();
    }

    public Entry<K, V> lastEntry() {
        return myDelegate.lastEntry();
    }

    public K lastKey() {
        return myDelegate.lastKey();
    }

    public Entry<K, V> lowerEntry(final K key) {
        return myDelegate.lowerEntry(key);
    }

    public K lowerKey(final K key) {
        return myDelegate.lowerKey(key);
    }

    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return myDelegate.merge(key, value, remappingFunction);
    }

    public final I name(final String name) {
        return (I) BasicSeries.super.name(name);
    }

    public NavigableSet<K> navigableKeySet() {
        return myDelegate.navigableKeySet();
    }

    public Entry<K, V> pollFirstEntry() {
        return myDelegate.pollFirstEntry();
    }

    public Entry<K, V> pollLastEntry() {
        return myDelegate.pollLastEntry();
    }

    public V put(final K key, final V value) {
        return myDelegate.put(key, value);
    }

    public void putAll(final Map<? extends K, ? extends V> m) {
        myDelegate.putAll(m);
    }

    public V putIfAbsent(final K key, final V value) {
        return myDelegate.putIfAbsent(key, value);
    }

    public V remove(final Object key) {
        return myDelegate.remove(key);
    }

    public boolean remove(final Object key, final Object value) {
        return myDelegate.remove(key, value);
    }

    public V replace(final K key, final V value) {
        return myDelegate.replace(key, value);
    }

    public boolean replace(final K key, final V oldValue, final V newValue) {
        return myDelegate.replace(key, oldValue, newValue);
    }

    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        myDelegate.replaceAll(function);
    }

    public void setColour(final ColourData colour) {
        myColour = colour;
    }

    public void setName(final String name) {
        myName = name;
    }

    public int size() {
        return myDelegate.size();
    }

    public NavigableMap<K, V> subMap(final K fromKey, final boolean fromInclusive, final K toKey, final boolean toInclusive) {
        return myDelegate.subMap(fromKey, fromInclusive, toKey, toInclusive);
    }

    public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
        return myDelegate.subMap(fromKey, toKey);
    }

    public SortedMap<K, V> tailMap(final K fromKey) {
        return myDelegate.tailMap(fromKey);
    }

    public NavigableMap<K, V> tailMap(final K fromKey, final boolean inclusive) {
        return myDelegate.tailMap(fromKey, inclusive);
    }

    @Override
    public String toString() {

        StringBuilder retVal = this.toStringFirstPart();

        this.appendLastPartToString(retVal);

        return retVal.toString();
    }

    public Collection<V> values() {
        return myDelegate.values();
    }

    final void appendLastPartToString(final StringBuilder builder) {

        if (myColour != null) {
            builder.append(TypeUtils.toHexString(myColour.getRGB()));
            builder.append(ASCII.NBSP);
        }

        if (this.size() <= 30) {
            builder.append(myDelegate.toString());
        } else {
            builder.append("First:");
            builder.append(this.firstKey());
            builder.append("=");
            builder.append(this.firstValue());
            builder.append(ASCII.NBSP);
            builder.append("Last:");
            builder.append(this.lastKey());
            builder.append("=");
            builder.append(this.lastValue());
            builder.append(ASCII.NBSP);
            builder.append("Size:");
            builder.append(this.size());
        }
    }

    final StringBuilder toStringFirstPart() {

        final StringBuilder retVal = new StringBuilder();

        if (myName != null) {
            retVal.append(myName);
            retVal.append(ASCII.NBSP);
        }

        return retVal;
    }

}
