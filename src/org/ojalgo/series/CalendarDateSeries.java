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

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;

import org.ojalgo.netio.ASCII;
import org.ojalgo.series.primitive.ExplicitTimeSeries;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateUnit;

public class CalendarDateSeries<V extends Number> extends OldAbstractSeries<CalendarDate, V, CalendarDateSeries<V>> {

    private final CalendarDateUnit myResolution;

    public CalendarDateSeries() {

        super();

        myResolution = CalendarDateUnit.MILLIS;
    }

    public CalendarDateSeries(final CalendarDateUnit resolution) {

        super();

        myResolution = resolution;
    }

    @SuppressWarnings("unused")
    private CalendarDateSeries(final Comparator<? super CalendarDate> comparator) {
        super(comparator);
        myResolution = null;
    }

    @SuppressWarnings("unused")
    private CalendarDateSeries(final Map<? extends CalendarDate, ? extends V> map) {
        super(map);
        myResolution = null;
    }

    @SuppressWarnings("unused")
    private CalendarDateSeries(final SortedMap<CalendarDate, ? extends V> sortedMap) {
        super(sortedMap);
        myResolution = null;
    }

    CalendarDateSeries(final SortedMap<CalendarDate, ? extends V> sortedMap, final CalendarDateUnit resolution) {

        super(sortedMap);

        myResolution = resolution;
    }

    /**
     * Will fill in missing values, inbetween the first and last keys.
     */
    public void complete() {

        CalendarDate tmpKey = this.firstKey();
        V tmpVal = null;

        V tmpPatch = this.firstValue();

        //BasicLogger.logDebug("First key={}, value={}", ((Calendar) tmpKey).getTime(), tmpPatch);

        final CalendarDate tmpLastKey = this.lastKey();
        while (tmpKey.compareTo(tmpLastKey) <= 0) {

            tmpVal = this.get(tmpKey);

            if (tmpVal != null) {
                tmpPatch = tmpVal;
                //BasicLogger.logDebug("Existing key={}, value={}", ((Calendar) tmpKey).getTime(), tmpVal);
            } else {
                this.put(tmpKey, tmpPatch);
                //BasicLogger.logDebug("Patching key={}, value={}", ((Calendar) tmpKey).getTime(), tmpPatch);
            }

            tmpKey = this.step(tmpKey);
        }
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
        return new ExplicitTimeSeries(this.getPrimitiveKeys(), this.getDataSeries());
    }

    public CalendarDateUnit getResolution() {
        return myResolution;
    }

    @Override
    public CalendarDateSeries<V> headMap(final CalendarDate newToKey) {

        final SortedMap<CalendarDate, V> tmpMap = super.headMap(newToKey);

        final CalendarDateSeries<V> retVal = new CalendarDateSeries<>(tmpMap, this.getResolution());
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    @Override
    public CalendarDateSeries<V> headMap(final CalendarDate newToKey, final boolean newInclusive) {

        final NavigableMap<CalendarDate, V> tmpMap = super.headMap(newToKey, newInclusive);

        final CalendarDateSeries<V> retVal = new CalendarDateSeries<>(tmpMap, this.getResolution());
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    public CalendarDate nextKey() {
        return this.lastKey().step(1, myResolution);
    }

    public V put(final Calendar key, final V value) {
        return super.put(CalendarDate.make(key, myResolution), value);
    }

    public double put(final CalendarDate key, final double value) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public V put(final CalendarDate key, final V value) {
        return super.put(key.filter(myResolution), value);
    }

    public V put(final Date key, final V value) {
        return super.put(CalendarDate.make(key, myResolution), value);
    }

    public V put(final long key, final V value) {
        return super.put(CalendarDate.make(key, myResolution), value);
    }

    @Override
    public void putAll(final Map<? extends CalendarDate, ? extends V> data) {
        for (final Map.Entry<? extends CalendarDate, ? extends V> tmpEntry : data.entrySet()) {
            this.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
    }

    public CalendarDateSeries<V> resample(final CalendarDate firstKey, final CalendarDate lastKey, final CalendarDateUnit resolution) {

        final CalendarDateSeries<V> retVal = new CalendarDateSeries<>(resolution);
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        final SortedMap<CalendarDate, V> tmpSubMap = this.subMap(firstKey, true, lastKey, true);

        retVal.putAll(tmpSubMap);

        return retVal;

    }

    public CalendarDateSeries<V> resample(final CalendarDateUnit resolution) {

        final CalendarDateSeries<V> retVal = new CalendarDateSeries<>(resolution);
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        retVal.putAll(this);

        return retVal;
    }

    public CalendarDate step(final CalendarDate key) {
        return key.step(1, myResolution);
    }

    @Override
    public CalendarDateSeries<V> subMap(final CalendarDate fromKey, final boolean inclusiveFromKey, final CalendarDate toKey, final boolean inclusiveToKey) {

        final NavigableMap<CalendarDate, V> tmpMap = super.subMap(fromKey, inclusiveFromKey, toKey, inclusiveToKey);

        final CalendarDateSeries<V> retVal = new CalendarDateSeries<>(tmpMap, this.getResolution());
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    @Override
    public CalendarDateSeries<V> subMap(final CalendarDate fromKey, final CalendarDate keyLimit) {

        final SortedMap<CalendarDate, V> tmpMap = super.subMap(fromKey, keyLimit);

        final CalendarDateSeries<V> retVal = new CalendarDateSeries<>(tmpMap, this.getResolution());
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    @Override
    public CalendarDateSeries<V> tailMap(final CalendarDate fromKey) {

        final SortedMap<CalendarDate, V> tmpMap = super.tailMap(fromKey);

        final CalendarDateSeries<V> retVal = new CalendarDateSeries<>(tmpMap, this.getResolution());
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    @Override
    public CalendarDateSeries<V> tailMap(final CalendarDate fromKey, final boolean inclusive) {

        final NavigableMap<CalendarDate, V> tmpMap = super.tailMap(fromKey, inclusive);

        final CalendarDateSeries<V> retVal = new CalendarDateSeries<>(tmpMap, this.getResolution());
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
