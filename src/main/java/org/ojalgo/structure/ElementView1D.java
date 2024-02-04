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
package org.ojalgo.structure;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.ojalgo.ProgrammingError;

public interface ElementView1D<N extends Comparable<N>, V extends ElementView1D<N, V>>
        extends AccessScalar<N>, Iterable<V>, Iterator<V>, Spliterator<V>, Comparable<V> {

    int CHARACTERISTICS = Spliterator.CONCURRENT | Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SIZED
            | Spliterator.SORTED | Spliterator.SUBSIZED;

    default int characteristics() {
        return CHARACTERISTICS;
    }

    default int compareTo(final V other) {
        return Long.compare(this.index(), other.index());
    }

    default void forEachRemaining(final Consumer<? super V> action) {
        Spliterator.super.forEachRemaining(action);
    }

    default Comparator<? super V> getComparator() {
        return null;
    }

    boolean hasPrevious();

    long index();

    V iterator();

    default long nextIndex() {
        return this.index() + 1L;
    }

    V previous();

    default long previousIndex() {
        return this.index() - 1L;
    }

    default void remove() {
        ProgrammingError.throwForUnsupportedOptionalOperation();
    }

    default boolean step() {
        if (this.hasNext()) {
            this.next();
            return true;
        } else {
            return false;
        }
    }

    default Stream<V> stream() {
        return StreamSupport.stream(this, false);
    }

    default boolean tryAdvance(final Consumer<? super V> action) {
        if (this.hasNext()) {
            action.accept(this.next());
            return true;
        } else {
            return false;
        }
    }

    V trySplit();

}
