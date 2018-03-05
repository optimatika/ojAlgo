/*
 * Copyright 1997-2018 Optimatika
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
import java.util.SortedMap;

import org.ojalgo.ProgrammingError;
import org.ojalgo.netio.ASCII;
import org.ojalgo.series.primitive.ExplicitTimeSeries;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDate.Resolution;
import org.ojalgo.type.CalendarDateUnit;

public final class CalendarDateSeries<N extends Number> extends TreeSeries<CalendarDate, N, CalendarDateSeries<N>>
        implements BasicSeries.NaturallySequenced<CalendarDate, N> {

    private final IndexMapper<CalendarDate> myMapper;
    private final CalendarDateUnit myResolution;

    public CalendarDateSeries() {
        this(CalendarDateUnit.MILLIS);
    }

    public CalendarDateSeries(final CalendarDateUnit resolution) {

        super();

        myResolution = resolution;
        myMapper = new IndexMapper<CalendarDate>() {

            public long toIndex(final CalendarDate key) {
                return key.toTimeInMillis(myResolution);
            }

            public CalendarDate toKey(final long index) {
                return new CalendarDate(index);
            }

        };
    }

    @SuppressWarnings("unused")
    private CalendarDateSeries(final Map<? extends CalendarDate, ? extends N> map) {
        super(map);
        myResolution = null;
        myMapper = null;
    }

    @SuppressWarnings("unused")
    private CalendarDateSeries(final SortedMap<CalendarDate, ? extends N> sortedMap) {
        super(sortedMap);
        myResolution = null;
        myMapper = null;
    }

    CalendarDateSeries(final SortedMap<CalendarDate, ? extends N> sortedMap, final CalendarDateUnit resolution, final IndexMapper<CalendarDate> mapper) {

        super(sortedMap);

        myResolution = resolution;
        myMapper = mapper;
    }

    public double doubleValue(final long index) {
        return this.doubleValue(myMapper.toKey(index));
    }

    public N get(final CalendarDate key) {
        return this.get((Object) key.filter(myResolution));
    }

    public N get(final long index) {
        return this.get(myMapper.toKey(index));
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
    public CalendarDateSeries<N> headMap(final CalendarDate newToKey) {

        final SortedMap<CalendarDate, N> tmpMap = super.headMap(newToKey);

        final CalendarDateSeries<N> retVal = new CalendarDateSeries<>(tmpMap, this.getResolution(), myMapper);
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    @Override
    public CalendarDateSeries<N> headMap(final CalendarDate newToKey, final boolean newInclusive) {

        final NavigableMap<CalendarDate, N> tmpMap = super.headMap(newToKey, newInclusive);

        final CalendarDateSeries<N> retVal = new CalendarDateSeries<>(tmpMap, this.getResolution(), myMapper);
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    public IndexMapper<CalendarDate> mapper() {
        return myMapper;
    }

    public CalendarDate nextKey() {
        return this.lastKey().step(1, myResolution);
    }

    public N put(final Calendar key, final N value) {
        return super.put(CalendarDate.make(key, myResolution), value);
    }

    /**
     * Will only work if values are types as Double.
     *
     * @see org.ojalgo.series.BasicSeries#put(java.lang.Comparable, double)
     */
    @SuppressWarnings("unchecked")
    public double put(final CalendarDate key, final double value) {
        final Double tmpValue = value;
        final N tmpOldValue = super.put(key.filter(myResolution), (N) tmpValue);
        if (tmpOldValue != null) {
            return tmpOldValue.doubleValue();
        } else {
            return Double.NaN;
        }
    }

    @Override
    public N put(final CalendarDate key, final N value) {
        return super.put(key.filter(myResolution), value);
    }

    public N put(final Date key, final N value) {
        return super.put(CalendarDate.make(key, myResolution), value);
    }

    public double put(final long index, final double value) {
        return this.put(myMapper.toKey(index), value);
    }

    public N put(final long index, final N value) {
        return super.put(myMapper.toKey(index), value);
    }

    @Override
    public void putAll(final Map<? extends CalendarDate, ? extends N> data) {
        for (final Map.Entry<? extends CalendarDate, ? extends N> tmpEntry : data.entrySet()) {
            this.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
    }

    public CalendarDateSeries<N> resample(final CalendarDate firstKey, final CalendarDate lastKey, final Resolution resolution) {

        if (resolution instanceof CalendarDateUnit) {

            final CalendarDateSeries<N> retVal = new CalendarDateSeries<>((CalendarDateUnit) resolution);
            retVal.setColour(this.getColour());
            retVal.setName(this.getName());

            final SortedMap<CalendarDate, N> tmpSubMap = this.subMap(firstKey, true, lastKey, true);

            retVal.putAll(tmpSubMap);

            return retVal;

        } else {

            ProgrammingError.throwWithMessage("Only {} supported!", CalendarDateUnit.class.getSimpleName());
            return null;
        }
    }

    public CalendarDateSeries<N> resample(final CalendarDate.Resolution resolution) {

        if (resolution instanceof CalendarDateUnit) {

            final CalendarDateSeries<N> retVal = new CalendarDateSeries<>((CalendarDateUnit) resolution);
            retVal.setColour(this.getColour());
            retVal.setName(this.getName());

            retVal.putAll(this);

            return retVal;

        } else {

            ProgrammingError.throwWithMessage("Only {} supported!", CalendarDateUnit.class.getSimpleName());
            return null;
        }
    }

    public CalendarDate step(final CalendarDate key) {
        return key.step(1, myResolution);
    }

    @Override
    public CalendarDateSeries<N> subMap(final CalendarDate fromKey, final boolean inclusiveFromKey, final CalendarDate toKey, final boolean inclusiveToKey) {

        final NavigableMap<CalendarDate, N> tmpMap = super.subMap(fromKey, inclusiveFromKey, toKey, inclusiveToKey);

        final CalendarDateSeries<N> retVal = new CalendarDateSeries<>(tmpMap, this.getResolution(), myMapper);
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    @Override
    public CalendarDateSeries<N> subMap(final CalendarDate fromKey, final CalendarDate keyLimit) {

        final SortedMap<CalendarDate, N> tmpMap = super.subMap(fromKey, keyLimit);

        final CalendarDateSeries<N> retVal = new CalendarDateSeries<>(tmpMap, this.getResolution(), myMapper);
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    @Override
    public CalendarDateSeries<N> tailMap(final CalendarDate fromKey) {

        final SortedMap<CalendarDate, N> tmpMap = super.tailMap(fromKey);

        final CalendarDateSeries<N> retVal = new CalendarDateSeries<>(tmpMap, this.getResolution(), myMapper);
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    @Override
    public CalendarDateSeries<N> tailMap(final CalendarDate fromKey, final boolean inclusive) {

        final NavigableMap<CalendarDate, N> tmpMap = super.tailMap(fromKey, inclusive);

        final CalendarDateSeries<N> retVal = new CalendarDateSeries<>(tmpMap, this.getResolution(), myMapper);
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    @Override
    public String toString() {

        final StringBuilder retVal = this.toStringFirstPart();

        retVal.append(myResolution);
        retVal.append(ASCII.NBSP);

        this.appendLastPartToString(retVal);

        return retVal.toString();
    }

}
