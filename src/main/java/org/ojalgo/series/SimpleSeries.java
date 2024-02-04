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

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

public final class SimpleSeries<K extends Comparable<? super K>, V extends Comparable<V>> extends TreeSeries<K, V, SimpleSeries<K, V>> {

    public static <K extends Comparable<? super K>, V extends Comparable<V>> SimpleSeries<K, V> copy(final Map<? extends K, ? extends V> entries) {
        SimpleSeries<K, V> retVal = new SimpleSeries<>();
        retVal.putAll(entries);
        return retVal;
    }

    public static <K extends Comparable<? super K>, V extends Comparable<V>> SimpleSeries<K, V> wrap(final NavigableMap<K, V> delegate) {
        return new SimpleSeries<>(delegate);
    }

    public SimpleSeries() {
        super(new TreeMap<>());
    }

    SimpleSeries(final NavigableMap<K, V> delegate) {
        super(delegate);
    }

    public BasicSeries<K, V> resample(final UnaryOperator<K> keyTranslator) {
        BasicSeries<K, V> retVal = new SimpleSeries<>();
        this.resample(keyTranslator, retVal);
        return retVal;
    }

}
