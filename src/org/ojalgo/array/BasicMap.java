package org.ojalgo.array;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.ojalgo.access.Access1D;
import org.ojalgo.array.DenseArray.DenseFactory;

public final class BasicMap<N extends Number> implements SortedMap<Long, N>, Access1D<N> {

    private static int INITIAL_CAPACITY = 16;

    private final SparseArray<N> myStorage;

    public BasicMap(DenseFactory<N> arrayFactory) {
        super();
        myStorage = new SparseArray<N>(Long.MAX_VALUE, arrayFactory, INITIAL_CAPACITY);
    }

    public void clear() {
        myStorage.empty();
    }

    public Comparator<? super Long> comparator() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean containsKey(long key) {
        return myStorage.index(key) >= 0;
    }

    public boolean containsKey(Object key) {
        if (key instanceof Number) {
            return this.containsKey(((Number) key).longValue());
        } else {
            return false;
        }
    }

    public boolean containsValue(double value) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean containsValue(Object value) {
        if (value instanceof Number) {
            return this.containsValue(((Number) value).doubleValue());
        } else {
            return false;
        }
    }

    public long count() {
        return myStorage.getActualLength();
    }

    public double doubleValue(long key) {
        // TODO Auto-generated method stub
        return Double.NaN;
    }

    public Set<java.util.Map.Entry<Long, N>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }

    public Long firstKey() {
        // TODO Auto-generated method stub
        return null;
    }

    public N get(long key) {
        // TODO Auto-generated method stub
        return null;
    }

    public N get(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    public BasicMap<N> headMap(long toKey) {
        // TODO Auto-generated method stub
        return null;
    }

    public BasicMap<N> headMap(Long toKey) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    public Set<Long> keySet() {
        // TODO Auto-generated method stub
        return null;
    }

    public Long lastKey() {
        // TODO Auto-generated method stub
        return null;
    }

    public double put(long key, double value) {
        // TODO Auto-generated method stub
        return Double.NaN;
    }

    public N put(long key, N value) {
        // TODO Auto-generated method stub
        return null;
    }

    public N put(Long key, N value) {
        // TODO Auto-generated method stub
        return null;
    }

    public void putAll(BasicMap<N> m) {
        // TODO Auto-generated method stub

    }

    public void putAll(Map<? extends Long, ? extends N> m) {
        // TODO Auto-generated method stub

    }

    public N remove(long key) {
        // TODO Auto-generated method stub
        return null;
    }

    public N remove(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    public int size() {
        return myStorage.getActualLength();
    }

    public BasicMap<N> subMap(long fromKey, long toKey) {
        // TODO Auto-generated method stub
        return null;
    }

    public BasicMap<N> subMap(Long fromKey, Long toKey) {
        // TODO Auto-generated method stub
        return null;
    }

    public BasicMap<N> tailMap(long fromKey) {
        // TODO Auto-generated method stub
        return null;
    }

    public BasicMap<N> tailMap(Long fromKey) {
        // TODO Auto-generated method stub
        return null;
    }

    public BasicList<N> values() {
        // TODO Auto-generated method stub
        return null;
    }

}
