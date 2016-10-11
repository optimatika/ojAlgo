package org.ojalgo.array;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.ojalgo.access.Access1D;
import org.ojalgo.array.DenseArray.DenseFactory;
import org.ojalgo.constant.PrimitiveMath;

public final class BasicMap<N extends Number> implements SortedMap<Long, N>, Access1D<N> {

    public static BasicMap<Double> makePrimitive() {
        return new BasicMap<Double>(PrimitiveArray.FACTORY);
    }

    private static int INITIAL_CAPACITY = 16;

    private final DenseFactory<N> myArrayFactory;

    private final SparseArray<N> myStorage;

    public BasicMap(final DenseFactory<N> arrayFactory) {

        super();

        myArrayFactory = arrayFactory;

        myStorage = new SparseArray<N>(Long.MAX_VALUE, arrayFactory, INITIAL_CAPACITY);
    }

    public void clear() {
        myStorage.empty();
    }

    public Comparator<? super Long> comparator() {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        return false;
    }

    public boolean containsValue(final Object value) {
        if (value instanceof Number) {
            if (myStorage.isPrimitive()) {
                return this.containsValue(((Number) value).doubleValue());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public long count() {
        return myStorage.getActualLength();
    }

    public double doubleValue(final long key) {
        int tmpIndex = myStorage.index(key);
        if (tmpIndex >= 0) {
            return myStorage.doDoubleValue(tmpIndex);
        } else {
            return PrimitiveMath.NaN;
        }
    }

    public Set<java.util.Map.Entry<Long, N>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }

    public Long firstKey() {
        return myStorage.firstInRange(0L, Long.MAX_VALUE);
    }

    public N get(final long key) {
        int tmpIndex = myStorage.index(key);
        if (tmpIndex >= 0) {
            return myStorage.doGet(tmpIndex);
        } else {
            return null;
        }
    }

    public N get(final Object key) {
        return key instanceof Number ? get(((Number) key).longValue()) : null;
    }

    public BasicMap<N> headMap(final long toKey) {
        // TODO Auto-generated method stub
        return null;
    }

    public BasicMap<N> headMap(final Long toKey) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isEmpty() {
        return myStorage.getActualLength() == 0;
    }

    public Set<Long> keySet() {
        // TODO Auto-generated method stub
        return null;
    }

    public Long lastKey() {
        // TODO Auto-generated method stub
        return null;
    }

    public double put(final long key, final double value) {
        int tmpIndex = myStorage.index(key);
        final double tmpOldValue = myStorage.doDoubleValue(tmpIndex);
        myStorage.doSet(key, tmpIndex, value);
        return tmpOldValue;
    }

    public N put(final long key, final N value) {
        int tmpIndex = myStorage.index(key);
        final N tmpOldValue = myStorage.doGet(tmpIndex);
        myStorage.doSet(key, tmpIndex, value);
        return tmpOldValue;
    }

    public N put(final Long key, final N value) {
        return this.put(key.longValue(), value);
    }

    public void putAll(final BasicMap<N> m) {
        // TODO Auto-generated method stub

    }

    public void putAll(final Map<? extends Long, ? extends N> m) {
        // TODO Auto-generated method stub

    }

    public N remove(final long key) {
        // TODO Auto-generated method stub
        return null;
    }

    public N remove(final Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    public int size() {
        return myStorage.getActualLength();
    }

    public BasicMap<N> subMap(final long fromKey, final long toKey) {
        // TODO Auto-generated method stub
        return null;
    }

    public BasicMap<N> subMap(final Long fromKey, final Long toKey) {
        // TODO Auto-generated method stub
        return null;
    }

    public BasicMap<N> tailMap(final long fromKey) {
        // TODO Auto-generated method stub
        return null;
    }

    public BasicMap<N> tailMap(final Long fromKey) {
        // TODO Auto-generated method stub
        return null;
    }

    public BasicList<N> values() {
        return new BasicList<>(myStorage, myArrayFactory, myStorage.getActualLength());
    }

}
