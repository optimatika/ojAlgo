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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class CloseableList<T extends AutoCloseable> implements List<T>, AutoCloseable {

    public static <T extends AutoCloseable> CloseableList<T> newInstance() {
        return new CloseableList<>(new ArrayList<>());
    }

    public static <T extends AutoCloseable> CloseableList<T> newInstance(final int capacity) {
        return new CloseableList<>(new ArrayList<>(capacity));
    }

    public static <T extends AutoCloseable> CloseableList<T> wrap(final List<T> delegate) {
        return new CloseableList<>(delegate);
    }

    private final List<T> myDelegate;

    CloseableList(final List<T> delegate) {
        super();
        myDelegate = delegate;
    }

    public void add(final int index, final T element) {
        myDelegate.add(index, element);
    }

    @Override
    public boolean add(final T element) {
        return myDelegate.add(element);
    }

    public boolean addAll(final Collection<? extends T> c) {
        return myDelegate.addAll(c);
    }

    public boolean addAll(final int index, final Collection<? extends T> c) {
        return myDelegate.addAll(index, c);
    }

    public void clear() {
        myDelegate.clear();
    }

    public void close() {
        myDelegate.forEach(e -> {
            try {
                if (e != null) {
                    e.close();
                }
            } catch (Exception cause) {
                throw new RuntimeException(cause);
            }
        });
    }

    public boolean contains(final Object o) {
        return myDelegate.contains(o);
    }

    public boolean containsAll(final Collection<?> c) {
        return myDelegate.containsAll(c);
    }

    @Override
    public boolean equals(final Object o) {
        return myDelegate.equals(o);
    }

    public void forEach(final Consumer<? super T> action) {
        myDelegate.forEach(action);
    }

    @Override
    public T get(final int index) {
        return myDelegate.get(index);
    }

    @Override
    public int hashCode() {
        return myDelegate.hashCode();
    }

    public int indexOf(final Object o) {
        return myDelegate.indexOf(o);
    }

    public boolean isEmpty() {
        return myDelegate.isEmpty();
    }

    public Iterator<T> iterator() {
        return myDelegate.iterator();
    }

    public int lastIndexOf(final Object o) {
        return myDelegate.lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
        return myDelegate.listIterator();
    }

    public ListIterator<T> listIterator(final int index) {
        return myDelegate.listIterator(index);
    }

    public Stream<T> parallelStream() {
        return myDelegate.parallelStream();
    }

    public T remove(final int index) {
        return myDelegate.remove(index);
    }

    public boolean remove(final Object o) {
        return myDelegate.remove(o);
    }

    public boolean removeAll(final Collection<?> c) {
        return myDelegate.removeAll(c);
    }

    public boolean removeIf(final Predicate<? super T> filter) {
        return myDelegate.removeIf(filter);
    }

    public void replaceAll(final UnaryOperator<T> operator) {
        myDelegate.replaceAll(operator);
    }

    public boolean retainAll(final Collection<?> c) {
        return myDelegate.retainAll(c);
    }

    public T set(final int index, final T element) {
        return myDelegate.set(index, element);
    }

    @Override
    public int size() {
        return myDelegate.size();
    }

    public void sort(final Comparator<? super T> c) {
        myDelegate.sort(c);
    }

    public Spliterator<T> spliterator() {
        return myDelegate.spliterator();
    }

    public Stream<T> stream() {
        return myDelegate.stream();
    }

    public List<T> subList(final int fromIndex, final int toIndex) {
        return myDelegate.subList(fromIndex, toIndex);
    }

    public Object[] toArray() {
        return myDelegate.toArray();
    }

    public <E> E[] toArray(final E[] a) {
        return myDelegate.toArray(a);
    }

}
