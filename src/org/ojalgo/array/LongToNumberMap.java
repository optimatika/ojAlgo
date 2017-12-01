package org.ojalgo.array;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Mutate1D;
import org.ojalgo.array.DenseArray.Factory;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.type.context.NumberContext;

/**
 * A {@link SortedMap} with primitive valued long keys and {@link Number} values (incl. possibly primitive
 * double values). The main benefits of using this class is its use of primitive keys and values, and how it
 * integrates with other parts of ojAlgo. As a general purpose {@link Map} implementation (usage with high
 * frequency of randomly ordered put and remove operations) it is not very efficient.
 *
 * @author apete
 */
public final class LongToNumberMap<N extends Number> implements SortedMap<Long, N>, Access1D<N>, Mutate1D.Mixable<N> {

    public static final class MapFactory<N extends Number> extends StrategyBuilder<N, LongToNumberMap<N>, MapFactory<N>> {

        MapFactory(final Factory<N> denseFactory) {
            super(denseFactory);
        }

        @Override
        public LongToNumberMap<N> make() {
            return new LongToNumberMap<>(this.getStrategy());
        }

    }

    public static <N extends Number> MapFactory<N> factory(final DenseArray.Factory<N> denseFactory) {
        return new MapFactory<>(denseFactory);
    }

    private final SparseArray<N> myStorage;
    private final DenseCapacityStrategy<N> myStrategy;

    LongToNumberMap(final DenseCapacityStrategy<N> strategy) {

        super();

        myStrategy = strategy;

        myStorage = new SparseArray<>(Long.MAX_VALUE, strategy);
    }

    /**
     * The current capacity of the underlying data structure. The capacity is always greater than or equal to
     * the current number of entries in the map. When you add entries to the map the capacity may have to
     * grow.
     */
    public long capacity() {
        return myStorage.capacity();
    }

    public void clear() {
        myStorage.reset();
    }

    public Comparator<? super Long> comparator() {
        return null;
    }

    public boolean containsKey(final long key) {
        return myStorage.index(key) >= 0;
    }

    public boolean containsKey(final Object key) {
        if (key instanceof Number) {
            return this.containsKey(((Number) key).longValue());
        } else {
            return false;
        }
    }

    public boolean containsValue(final double value) {
        for (final NonzeroView<N> tmpView : myStorage.nonzeros()) {
            // if (tmpView.doubleValue() == value) {
            if (NumberContext.compare(tmpView.doubleValue(), value) == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean containsValue(final Object value) {
        for (final NonzeroView<N> tmpView : myStorage.nonzeros()) {
            if (value.equals(tmpView.get())) {
                return true;
            }
        }
        return false;
    }

    public long count() {
        return myStorage.getActualLength();
    }

    public double doubleValue(final long key) {
        final int tmpIndex = myStorage.index(key);
        if (tmpIndex >= 0) {
            return myStorage.doubleValueInternally(tmpIndex);
        } else {
            return PrimitiveMath.NaN;
        }
    }

    public Set<Map.Entry<Long, N>> entrySet() {
        return new AbstractSet<Map.Entry<Long, N>>() {

            @Override
            public Iterator<Map.Entry<Long, N>> iterator() {
                return new Iterator<Map.Entry<Long, N>>() {

                    NonzeroView<N> tmpNonzeros = myStorage.nonzeros();

                    public boolean hasNext() {
                        return tmpNonzeros.hasNext();
                    }

                    public Map.Entry<Long, N> next() {

                        tmpNonzeros.next();

                        return new Map.Entry<Long, N>() {

                            public Long getKey() {
                                return tmpNonzeros.index();
                            }

                            public N getValue() {
                                return tmpNonzeros.get();
                            }

                            public N setValue(final N value) {
                                ProgrammingError.throwForUnsupportedOptionalOperation();
                                return null;
                            }

                        };
                    }

                };
            }

            @Override
            public int size() {
                return myStorage.getActualLength();
            }
        };
    }

    public Long firstKey() {
        return myStorage.firstIndex();
    }

    public N get(final long key) {
        final int tmpIndex = myStorage.index(key);
        if (tmpIndex >= 0) {
            return myStorage.getInternally(tmpIndex);
        } else {
            return null;
        }
    }

    public N get(final Object key) {
        return key instanceof Number ? this.get(((Number) key).longValue()) : null;
    }

    public LongToNumberMap<N> headMap(final long toKey) {
        return this.subMap(myStorage.firstIndex(), toKey);
    }

    public LongToNumberMap<N> headMap(final Long toKey) {
        return this.headMap(toKey.longValue());
    }

    public boolean isEmpty() {
        return myStorage.getActualLength() == 0;
    }

    public Set<Long> keySet() {
        return new AbstractSet<Long>() {

            @Override
            public Iterator<Long> iterator() {
                return myStorage.indices().iterator();
            }

            @Override
            public int size() {
                return myStorage.getActualLength();
            }

        };
    }

    public Long lastKey() {
        return myStorage.lastIndex();
    }

    public double mix(final long key, final BinaryFunction<N> mixer, final double addend) {
        ProgrammingError.throwIfNull(mixer);
        synchronized (myStorage) {
            final int tmpIndex = myStorage.index(key);
            final double oldValue = tmpIndex >= 0 ? myStorage.doubleValueInternally(tmpIndex) : PrimitiveMath.NaN;
            final double newValue = tmpIndex >= 0 ? mixer.invoke(oldValue, addend) : addend;
            myStorage.put(key, tmpIndex, newValue);
            return newValue;
        }
    }

    public N mix(final long key, final BinaryFunction<N> mixer, final N addend) {
        ProgrammingError.throwIfNull(mixer);
        synchronized (myStorage) {
            final int tmpIndex = myStorage.index(key);
            final N oldValue = tmpIndex >= 0 ? myStorage.getInternally(tmpIndex) : null;
            final N newValue = tmpIndex >= 0 ? mixer.invoke(oldValue, addend) : addend;
            myStorage.put(key, tmpIndex, newValue);
            return newValue;
        }
    }

    public double put(final long key, final double value) {
        final int index = myStorage.index(key);
        final double oldValue = index >= 0 ? myStorage.doubleValueInternally(index) : PrimitiveMath.NaN;
        myStorage.put(key, index, value);
        return oldValue;
    }

    public N put(final long key, final N value) {
        final int index = myStorage.index(key);
        final N oldValue = index >= 0 ? myStorage.getInternally(index) : null;
        myStorage.put(key, index, value);
        return oldValue;
    }

    public N put(final Long key, final N value) {
        return this.put(key.longValue(), value);
    }

    public void putAll(final LongToNumberMap<N> m) {
        if (myStorage.isPrimitive()) {
            for (final NonzeroView<N> tmpView : m.getStorage().nonzeros()) {
                myStorage.set(tmpView.index(), tmpView.doubleValue());
            }
        } else {
            for (final NonzeroView<N> tmpView : m.getStorage().nonzeros()) {
                myStorage.set(tmpView.index(), tmpView.get());
            }
        }
    }

    public void putAll(final Map<? extends Long, ? extends N> m) {
        for (final java.util.Map.Entry<? extends Long, ? extends N> tmpEntry : m.entrySet()) {
            myStorage.set(tmpEntry.getKey(), tmpEntry.getValue());
        }
    }

    public N remove(final long key) {
        final int index = myStorage.index(key);
        final N oldValue = index >= 0 ? myStorage.getInternally(index) : null;
        myStorage.remove(key, index);
        return oldValue;
    }

    public N remove(final Object key) {
        if (key instanceof Number) {
            return this.remove(((Number) key).longValue());
        } else {
            return null;
        }
    }

    public int size() {
        return myStorage.getActualLength();
    }

    public LongToNumberMap<N> subMap(final long fromKey, final long toKey) {

        final LongToNumberMap<N> retVal = new LongToNumberMap<>(myStrategy);

        long tmpKey;
        for (final NonzeroView<N> tmpView : myStorage.nonzeros()) {
            tmpKey = tmpView.index();
            if ((fromKey <= tmpKey) && (tmpKey < toKey)) {
                final N tmpValue = tmpView.get();
                retVal.put(tmpKey, tmpValue);
            }
        }

        return retVal;
    }

    public LongToNumberMap<N> subMap(final Long fromKey, final Long toKey) {
        return this.subMap(fromKey.longValue(), toKey.longValue());
    }

    public LongToNumberMap<N> tailMap(final long fromKey) {
        return this.subMap(fromKey, myStorage.lastIndex() + 1L);
    }

    public LongToNumberMap<N> tailMap(final Long fromKey) {
        return this.tailMap(fromKey.longValue());
    }

    @Override
    public String toString() {

        final NonzeroView<N> nz = myStorage.nonzeros();

        if (!nz.hasNext()) {
            return "{}";
        }

        final StringBuilder builder = new StringBuilder();
        builder.append('{');
        for (;;) {
            final NonzeroView<N> entry = nz.next();
            final long key = entry.index();
            final N value = entry.get();
            builder.append(key);
            builder.append('=');
            builder.append(value);
            if (!nz.hasNext()) {
                return builder.append('}').toString();
            }
            builder.append(',').append(' ');
        }
    }

    public NumberList<N> values() {
        return new NumberList<>(myStorage.getValues(), myStrategy, myStorage.getActualLength());
    }

    /**
     * Should return the same elements/values as first calling {@link #subMap(Long, Long)} and then
     * {@link #values()} but this method does not create any copies. Any change in the underlying data
     * structure (this map) will corrupt this method's output.
     */
    public Access1D<N> values(final long fromKey, final long toKey) {
        return myStorage.getValues(fromKey, toKey);
    }

    SparseArray<N> getStorage() {
        return myStorage;
    }

}
