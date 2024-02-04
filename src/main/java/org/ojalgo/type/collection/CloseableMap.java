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
package org.ojalgo.type.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CloseableMap<K, V extends AutoCloseable> implements Map<K, V>, AutoCloseable {

    public static <K, V extends AutoCloseable> CloseableMap<K, V> newInstance() {
        return new CloseableMap<>(new HashMap<>());
    }

    public static <K, V extends AutoCloseable> CloseableMap<K, V> newInstance(final int capacity) {
        return new CloseableMap<>(new HashMap<>(capacity));
    }

    public static <K, V extends AutoCloseable> CloseableMap<K, V> wrap(final Map<K, V> delegate) {
        return new CloseableMap<>(delegate);
    }

    private final Map<K, V> myDelegate;

    CloseableMap(final Map<K, V> delegate) {
        super();
        myDelegate = delegate;
    }

    @Override
    public void clear() {
        myDelegate.clear();
    }

    public void close() throws Exception {
        myDelegate.values().forEach(e -> {
            try {
                if (e != null) {
                    e.close();
                }
            } catch (Exception cause) {
                throw new RuntimeException(cause);
            }
        });
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

    @Override
    public boolean containsKey(final Object key) {
        return myDelegate.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return myDelegate.containsValue(value);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return myDelegate.entrySet();
    }

    @Override
    public boolean equals(final Object o) {
        return myDelegate.equals(o);
    }

    public void forEach(final BiConsumer<? super K, ? super V> action) {
        myDelegate.forEach(action);
    }

    @Override
    public V get(final Object key) {
        return myDelegate.get(key);
    }

    public V getOrDefault(final Object key, final V defaultValue) {
        return myDelegate.getOrDefault(key, defaultValue);
    }

    @Override
    public int hashCode() {
        return myDelegate.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return myDelegate.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return myDelegate.keySet();
    }

    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return myDelegate.merge(key, value, remappingFunction);
    }

    @Override
    public V put(final K key, final V value) {
        return myDelegate.put(key, value);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        myDelegate.putAll(m);
    }

    public V putIfAbsent(final K key, final V value) {
        return myDelegate.putIfAbsent(key, value);
    }

    @Override
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

    @Override
    public int size() {
        return myDelegate.size();
    }

    @Override
    public Collection<V> values() {
        return myDelegate.values();
    }

}
