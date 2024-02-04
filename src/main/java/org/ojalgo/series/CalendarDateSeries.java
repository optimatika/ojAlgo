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

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.ojalgo.netio.ASCII;
import org.ojalgo.series.primitive.ExplicitTimeSeries;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateUnit;

public final class CalendarDateSeries<N extends Comparable<N>> extends TreeSeries<CalendarDate, N, CalendarDateSeries<N>>
        implements BasicSeries.NaturallySequenced<CalendarDate, N> {

    private final NavigableMap<CalendarDate, N> myDelegate;
    private final CalendarDateUnit myResolution;

    public CalendarDateSeries() {
        this(CalendarDateUnit.MILLIS);
    }

    public CalendarDateSeries(final CalendarDateUnit resolution) {
        this(new TreeMap<>(), resolution);
    }

    CalendarDateSeries(final NavigableMap<CalendarDate, N> delegate, final CalendarDateUnit resolution) {

        super(delegate);

        myDelegate = delegate;
        myResolution = resolution;
    }

    public void complete() {
        this.complete(key -> key.step(1, myResolution));
    }

    @Override
    public N get(final CalendarDate key) {
        return myDelegate.get(key.filter(myResolution));
    }

    public long getAverageStepSize() {
        return ((this.lastKey().millis - this.firstKey().millis) / (this.size() - 1));
    }

    public long[] getPrimitiveKeys() {

        final long[] retVal = new long[this.size()];

        int i = 0;
        for (final CalendarDate tmpKey : this.keySet()) {
            retVal[i] = tmpKey.millis;
            i++;
        }

        return retVal;
    }

    public ExplicitTimeSeries getPrimitiveTimeSeries() {
        return new ExplicitTimeSeries(this.getPrimitiveKeys(), this.asPrimitive());
    }

    public CalendarDateUnit getResolution() {
        return myResolution;
    }

    @Override
    public CalendarDateSeries<N> headMap(final CalendarDate toKey) {
        return this.headMap(toKey, false);
    }

    @Override
    public CalendarDateSeries<N> headMap(final CalendarDate toKey, final boolean inclusive) {

        CalendarDateSeries<N> retVal = new CalendarDateSeries<>(myDelegate.headMap(toKey, inclusive), this.getResolution());

        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    public CalendarDate nextKey() {
        return this.lastKey().step(1, myResolution);
    }

    public N put(final Calendar key, final N value) {
        return super.put(CalendarDate.make(key, myResolution), value);
    }

    @Override
    public N put(final CalendarDate key, final N value) {
        return super.put(key.filter(myResolution), value);
    }

    public N put(final Date key, final N value) {
        return super.put(CalendarDate.make(key, myResolution), value);
    }

    @Override
    public void putAll(final Map<? extends CalendarDate, ? extends N> data) {
        for (final Map.Entry<? extends CalendarDate, ? extends N> tmpEntry : data.entrySet()) {
            this.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
    }

    public BasicSeries<CalendarDate, N> resample(final CalendarDateUnit resolution) {
        return this.resample(resolution, resolution::adjustInto);
    }

    public BasicSeries<CalendarDate, N> resample(final UnaryOperator<CalendarDate> keyTranslator) {
        return this.resample(myResolution, keyTranslator);
    }

    public CalendarDate step(final CalendarDate key) {
        return key.step(1, myResolution);
    }

    @Override
    public CalendarDateSeries<N> subMap(final CalendarDate fromKey, final boolean inclusiveFromKey, final CalendarDate toKey, final boolean inclusiveToKey) {

        CalendarDateSeries<N> retVal = new CalendarDateSeries<>(myDelegate.subMap(fromKey, inclusiveFromKey, toKey, inclusiveToKey), this.getResolution());

        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    @Override
    public CalendarDateSeries<N> subMap(final CalendarDate fromKey, final CalendarDate toKey) {
        return this.subMap(fromKey, true, toKey, false);
    }

    @Override
    public CalendarDateSeries<N> tailMap(final CalendarDate fromKey) {
        return this.tailMap(fromKey, true);
    }

    @Override
    public CalendarDateSeries<N> tailMap(final CalendarDate fromKey, final boolean inclusive) {

        CalendarDateSeries<N> retVal = new CalendarDateSeries<>(myDelegate.tailMap(fromKey, inclusive), this.getResolution());

        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    @Override
    public String toString() {

        StringBuilder retVal = this.toStringFirstPart();

        retVal.append(myResolution);
        retVal.append(ASCII.NBSP);

        this.appendLastPartToString(retVal);

        return retVal.toString();
    }

    private BasicSeries<CalendarDate, N> resample(final CalendarDateUnit resolution, final Function<CalendarDate, CalendarDate> keyMapper) {

        CalendarDateSeries<N> retVal = new CalendarDateSeries<>(resolution);

        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        for (Map.Entry<CalendarDate, N> entry : this.entrySet()) {
            CalendarDate key = keyMapper.apply(entry.getKey());
            N value = entry.getValue();
            retVal.put(key, value);
        }

        return retVal;
    }

}
