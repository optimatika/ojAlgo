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
package org.ojalgo.type;

import java.time.Duration;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.ojalgo.concurrent.DaemonPoolExecutor;
import org.ojalgo.function.special.PowerOf2;

/**
 * A {@link Map} that can forget entries after a specified period of time – a cache in other words. The
 * entries can either expire after a certain amount of time has passed since they were created/written or
 * since they were last accessed.
 * <P>
 * In addition to implementing the {@link Map} interface, also looked at the Cache interface of the Caffeine
 * library, and mimicked some of its methods.
 * <p>
 * Furthermore, this class provides a re-implementation of {@link TypeCache}.
 */
public final class ForgetfulMap<K, V> extends AbstractMap<K, V> {

    public static final class Builder {

        private long myAccessLimit = Long.MAX_VALUE;
        private int myInitialCapacity = 16;
        private long myWriteLimit = Long.MAX_VALUE;

        Builder() {
            super();
        }

        public <K, V> ForgetfulMap<K, V> build() {
            return new ForgetfulMap<>(this);
        }

        public <K, V> ForgetfulMap<K, V> build(final Consumer<V> disposer) {
            return new ForgetfulMap<>(this, disposer);
        }

        public <V> ForgetfulMap.ValueCache<V> build(final Supplier<V> instantiator, final Consumer<V> disposer) {
            return this.initialCapacity(1).build(disposer).newValueCache("INSTANCE", k -> instantiator.get());
        }

        public Builder expireAfterAccess(final CalendarDateDuration duration) {
            return this.expireAfterAccess(duration.toDurationInMillis());
        }

        public Builder expireAfterAccess(final Duration duration) {
            return this.expireAfterAccess(duration.toMillis());
        }

        public Builder expireAfterAccess(final long durationInMillis) {
            myAccessLimit = durationInMillis;
            return this;
        }

        public Builder expireAfterAccess(final long duration, final TimeUnit unit) {
            return this.expireAfterAccess(unit.toMillis(duration));
        }

        public Builder expireAfterWrite(final CalendarDateDuration duration) {
            return this.expireAfterWrite(duration.toDurationInMillis());
        }

        public Builder expireAfterWrite(final Duration duration) {
            return this.expireAfterWrite(duration.toMillis());
        }

        public Builder expireAfterWrite(final long durationInMillis) {
            myWriteLimit = durationInMillis;
            return this;
        }

        public Builder expireAfterWrite(final long duration, final TimeUnit unit) {
            return this.expireAfterWrite(unit.toMillis(duration));
        }

        public Builder initialCapacity(final int initialCapacity) {
            myInitialCapacity = PowerOf2.smallestNotLessThan(initialCapacity);
            return this;
        }

        long getAccessLimit() {
            return myAccessLimit;
        }

        int getInitialCapacity() {
            return myInitialCapacity;
        }

        long getWriteLimit() {
            return myWriteLimit;
        }

    }

    /**
     * The specifications of {@link TypeCache} to allow for another implementation.
     */
    public static interface ValueCache<V> {

        void flushCache();

        V getCachedObject();

        /**
         * Is there currently a value cached for this key?
         */
        boolean isCacheSet();

        /**
         * @return true if {@link #makeDirty()} has been called since the last time {@link #getCachedObject()}
         *         was called.
         */
        boolean isDirty();

        /**
         * Will force re-creation of the value the next time {@link #getCachedObject()} is called. This method
         * does NOT immediately remove or invalidate the value from the underlying cache.
         */
        void makeDirty();

    }

    /**
     * Helper class to store value and timestamps
     */
    private static final class CachedValue<V> {

        long accessed;
        final V value;
        final long written;

        @SuppressWarnings("hiding")
        CachedValue(final V value) {

            super();

            written = System.currentTimeMillis();
            accessed = written;

            this.value = value;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CachedValue)) {
                return false;
            }
            CachedValue<?> other = (CachedValue<?>) obj;
            if (written != other.written || accessed != other.accessed) {
                return false;
            }
            if (value == null) {
                if (other.value != null) {
                    return false;
                }
            } else if (!value.equals(other.value)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (written ^ written >>> 32);
            result = prime * result + (int) (accessed ^ accessed >>> 32);
            result = prime * result + (value == null ? 0 : value.hashCode());
            return result;
        }

    }

    /**
     * A re-implementation of {@link TypeCache} backed by a {@link ForgetfulMap}. Essentially it's a supplier
     * that most of the time returns a cached value, and only recomputes it periodically.
     */
    private static final class ValueCacheImpl<K, V> implements ValueCache<V> {

        private final ForgetfulMap<K, V> myCache;
        private volatile boolean myDirty;
        private final K myKey;
        private final Function<? super K, ? extends V> myValueSupplier;

        ValueCacheImpl(final K key, final ForgetfulMap<K, V> cache, final Function<? super K, ? extends V> valueSupplier) {
            super();
            myKey = key;
            myCache = cache;
            myValueSupplier = valueSupplier;
        }

        @Override
        public final void flushCache() {
            myCache.remove(myKey);
        }

        @Override
        public final V getCachedObject() {

            if (myDirty) {
                myDirty = false;
                V newValue = myValueSupplier.apply(myKey);
                myCache.put(myKey, newValue);
                return newValue;
            } else {
                myDirty = false;
                return myCache.computeIfAbsent(myKey, myValueSupplier);
            }
        }

        /**
         * Is there currently a value cached for this key?
         */
        @Override
        public final boolean isCacheSet() {
            return myCache.containsKey(myKey);
        }

        /**
         * @return true if {@link #makeDirty()} has been called since the last time {@link #getCachedObject()}
         *         was called.
         */
        @Override
        public final boolean isDirty() {
            return myDirty;
        }

        /**
         * Will force re-creation of the value the next time {@link #getCachedObject()} is called. This method
         * does NOT immediately remove or invalidate the value from the underlying cache.
         */
        @Override
        public final void makeDirty() {
            myDirty = true;
        }

    }

    private static final ScheduledExecutorService CLEANER = DaemonPoolExecutor.newSingleThreadScheduledExecutor("ForgetfulMap-Cleaner");

    public static ForgetfulMap.Builder newBuilder() {
        return new ForgetfulMap.Builder();
    }

    private final long myAccessLimit;
    private final Consumer<V> myDisposer;
    private final Map<K, CachedValue<V>> myStorage;
    private final long myWriteLimit;

    ForgetfulMap(final Builder builder) {
        this(builder, null);
    }

    ForgetfulMap(final Builder builder, final Consumer<V> disposer) {

        super();

        myStorage = new ConcurrentHashMap<>(builder.getInitialCapacity());

        myDisposer = disposer;

        myWriteLimit = builder.getWriteLimit();
        myAccessLimit = builder.getAccessLimit();

        long interval = Math.max(1000L, Math.min(myWriteLimit, myAccessLimit) / 2L);

        // Periodically clean up expired entries
        CLEANER.scheduleAtFixedRate(this::cleanUp, interval, interval, TimeUnit.MILLISECONDS);

    }

    /**
     * Removes all entries that have expired. This method is automatically called at regular intervals, but
     * can also be called manually to remove entries that have expired since the last cleanup.
     */
    public void cleanUp() {
        long now = System.currentTimeMillis();
        myStorage.forEach((key, value) -> {
            if (this.isExpired(now, value)) {
                CachedValue<V> removed = myStorage.remove(key);
                if (myDisposer != null) {
                    myDisposer.accept(removed.value);
                }
            }
        });
    }

    @Override
    public void clear() {
        if (myDisposer == null) {
            myStorage.clear();
        } else {
            myStorage.forEach((key, value) -> myDisposer.accept(myStorage.remove(key).value));
        }
    }

    @Override
    public boolean containsKey(final Object key) {
        return myStorage.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return myStorage.values().stream().map(cached -> cached.value).anyMatch(v -> Objects.equals(v, value));
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return myStorage.entrySet().stream().map(this::unwrap).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof ForgetfulMap)) {
            return false;
        }
        ForgetfulMap<?, ?> other = (ForgetfulMap<?, ?>) obj;
        return Objects.equals(myStorage, other.myStorage) && myAccessLimit == other.myAccessLimit;
    }

    /**
     * Returns the approximate number of entries in this cache. The value returned is an estimate; the actual
     * count may differ if there are concurrent insertions or removals, or if some entries are pending removal
     * due to expiration or weak/soft reference collection. In the case of stale entries this inaccuracy can
     * be mitigated by performing a {@link #cleanUp()} first.
     */
    public long estimatedSize() {
        return myStorage.size();
    }

    /**
     * Returns the value associated with the {@code key} in this cache, obtaining that value from the
     * {@code mappingFunction} if necessary. This method provides a simple substitute for the conventional "if
     * cached, return; otherwise create, cache and return" pattern.
     * <p>
     * If the specified key is not already associated with a value, attempts to compute its value using the
     * given mapping function and enters it into this cache unless {@code null}. The entire method invocation
     * is performed atomically, so the function is applied at most once per key. Some attempted update
     * operations on this cache by other threads may be blocked while the computation is in progress, so the
     * computation should be short and simple, and must not attempt to update any other mappings of this
     * cache.
     */
    public V get(final K key, final Function<? super K, ? extends V> mappingFunction) {
        return this.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V get(final Object key) {

        CachedValue<V> cached = myStorage.get(key);

        if (cached == null) {

            return null;

        } else {

            long now = System.currentTimeMillis();

            if (this.isExpired(now, cached)) {
                V removed = myStorage.remove(key).value;
                if (myDisposer != null) {
                    myDisposer.accept(removed);
                }
                return null;
            } else {
                cached.accessed = now;
                return cached.value;
            }
        }
    }

    /**
     * Returns a map of the values associated with the {@code keys} in this cache. The returned map will only
     * contain entries which are already present in the cache.
     * <p>
     * Note that duplicate elements in {@code keys}, as determined by {@link Object#equals}, will be ignored.
     */
    public Map<K, V> getAllPresent(final Iterable<? extends K> keys) {
        return StreamSupport.stream(keys.spliterator(), false).filter(myStorage::containsKey).collect(Collectors.toMap(k -> k, this::get));
    }

    /**
     * Returns the value associated with the {@code key} in this cache, or {@code null} if there is no cached
     * value for the {@code key}.
     */
    public V getIfPresent(final K key) {
        return this.get(key);
    }

    @Override
    public int hashCode() {
        return myStorage.hashCode();
    }

    /**
     * Discards any cached value for the {@code key}. The behavior of this operation is undefined for an entry
     * that is being loaded (or reloaded) and is otherwise not present.
     */
    public void invalidate(final K key) {
        this.remove(key);
    }

    /**
     * Discards all entries in the cache. The behavior of this operation is undefined for an entry that is
     * being loaded (or reloaded) and is otherwise not present.
     */
    public void invalidateAll() {
        this.clear();
    }

    /**
     * Discards any cached values for the {@code keys}. The behavior of this operation is undefined for an
     * entry that is being loaded (or reloaded) and is otherwise not present.
     */
    public void invalidateAll(final Iterable<? extends K> keys) {
        keys.forEach(this::invalidate);
    }

    @Override
    public boolean isEmpty() {
        return myStorage.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return myStorage.keySet();
    }

    public ValueCache<V> newValueCache(final K key, final Function<? super K, ? extends V> valueSupplier) {
        return new ValueCacheImpl<>(key, this, valueSupplier);
    }

    @Override
    public V put(final K key, final V value) {
        CachedValue<V> newValue = new CachedValue<>(value);
        CachedValue<V> oldValue = myStorage.put(key, newValue);
        if (oldValue == null) {
            return null;
        } else {
            return oldValue.value;
        }
    }

    /**
     * Copies all of the mappings from the specified map to the cache. The effect of this call is equivalent
     * to that of calling {@code put(k, v)} on this map once for each mapping from key {@code k} to value
     * {@code v} in the specified map. The behavior of this operation is undefined if the specified map is
     * modified while the operation is in progress.
     */
    @Override
    public void putAll(final Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
    }

    @Override
    public V remove(final Object key) {
        CachedValue<V> removed = myStorage.remove(key);
        if (removed == null) {
            return null;
        } else {
            V retVal = removed.value;
            if (myDisposer != null) {
                myDisposer.accept(retVal);
            }
            return retVal;
        }
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        V cached = this.get(key);
        if (Objects.equals(cached, value)) {
            return this.remove(key) != null;
        } else {
            return false;
        }
    }

    @Override
    public int size() {
        return myStorage.size();
    }

    private boolean isExpired(final long now, final CachedValue<V> cached) {
        return cached.value == null || now - cached.accessed > myAccessLimit || now - cached.written > myWriteLimit;
    }

    private Entry<K, V> unwrap(final Entry<K, CachedValue<V>> entry) {
        return new Entry<>() {

            @Override
            public K getKey() {
                return entry.getKey();
            }

            @Override
            public V getValue() {
                CachedValue<V> cached = entry.getValue();
                if (cached == null) {
                    return null;
                } else {
                    return cached.value;
                }
            }

            @Override
            public V setValue(final V value) {
                CachedValue<V> newValue = new CachedValue<>(value);
                CachedValue<V> oldValue = entry.setValue(newValue);
                if (oldValue == null) {
                    return null;
                } else {
                    V retVal = oldValue.value;
                    if (myDisposer != null) {
                        myDisposer.accept(retVal);
                    }
                    return retVal;
                }
            }

        };
    }

}
