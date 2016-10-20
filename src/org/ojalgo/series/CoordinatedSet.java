/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ojalgo.series.primitive.PrimitiveSeries;
import org.ojalgo.type.CalendarDateDuration;

public class CoordinatedSet<K extends Comparable<? super K>> {

    public static <K extends Comparable<? super K>> CoordinatedSet<K> from(final List<? extends BasicSeries<K, ?>> series) {
        return new CoordinatedSet<K>(series, null);
    }

    public static <K extends Comparable<? super K>> CoordinatedSet<K> from(final List<? extends BasicSeries<K, ?>> series,
            final CalendarDateDuration resolution) {
        return new CoordinatedSet<K>(series, resolution);
    }

    private final K myFirst = null;
    private final K myLast = null;
    private final int myLength = 0;
    private final CalendarDateDuration myResolution = null;
    private final Map<String, PrimitiveSeries> mySet = new HashMap<>();

    private CoordinatedSet(final List<? extends BasicSeries<K, ?>> series, final CalendarDateDuration resolution) {

        super();
    }

    public K getFirst() {
        return myFirst;
    }

    public K getLast() {
        return myLast;
    }

    public CalendarDateDuration getResolution() {
        return myResolution;
    }

    public PrimitiveSeries getSeries(final String key) {
        return mySet.get(key);
    }

    public int size() {
        return myLength;
    }

}
