/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

public class CalendarDateSeries<V extends Number> extends AbstractSeries<CalendarDate, V, CalendarDateSeries<V>> {

    private final CalendarDateUnit myResolution;

    public CalendarDateSeries() {

        super();

        myResolution = CalendarDateUnit.MILLIS;
    }

    public CalendarDateSeries(final CalendarDateUnit aResolution) {

        super();

        myResolution = aResolution;
    }

    @SuppressWarnings("unused")
    private CalendarDateSeries(final Comparator<? super CalendarDate> someC) {
        super(someC);
        myResolution = null;
    }

    @SuppressWarnings("unused")
    private CalendarDateSeries(final Map<? extends CalendarDate, ? extends V> someM) {
        super(someM);
        myResolution = null;
    }

    @SuppressWarnings("unused")
    private CalendarDateSeries(final SortedMap<CalendarDate, ? extends V> someM) {
        super(someM);
        myResolution = null;
    }

    CalendarDateSeries(final SortedMap<CalendarDate, ? extends V> someM, final CalendarDateUnit aResolution) {

        super(someM);

        myResolution = aResolution;
    }

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

        final CalendarDateSeries<V> retVal = new CalendarDateSeries<V>(tmpMap, this.getResolution());
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    @Override
    public CalendarDateSeries<V> headMap(final CalendarDate newToKey, final boolean newInclusive) {

        final NavigableMap<CalendarDate, V> tmpMap = super.headMap(newToKey, newInclusive);

        final CalendarDateSeries<V> retVal = new CalendarDateSeries<V>(tmpMap, this.getResolution());
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    public V put(final Calendar aKey, final V aValue) {
        return super.put(CalendarDate.make(aKey, myResolution), aValue);
    }

    @Override
    public V put(final CalendarDate aKey, final V aValue) {
        return super.put(aKey.filter(myResolution), aValue);
    }

    public V put(final Date aKey, final V aValue) {
        return super.put(CalendarDate.make(aKey, myResolution), aValue);
    }

    public V put(final long aKey, final V aValue) {
        return super.put(CalendarDate.make(aKey, myResolution), aValue);
    }

    @Override
    public void putAll(final Map<? extends CalendarDate, ? extends V> aMap) {
        for (final java.util.Map.Entry<? extends CalendarDate, ? extends V> tmpEntry : aMap.entrySet()) {
            this.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
    }

    public CalendarDateSeries<V> resample(final CalendarDate aFirstKey, final CalendarDate aLastKey, final CalendarDateUnit aResolution) {

        final CalendarDateSeries<V> retVal = new CalendarDateSeries<V>(aResolution);
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        final SortedMap<CalendarDate, V> tmpSubMap = this.subMap(aFirstKey, true, aLastKey, true);

        retVal.putAll(tmpSubMap);

        return retVal;

    }

    public CalendarDateSeries<V> resample(final CalendarDateUnit aResolution) {

        final CalendarDateSeries<V> retVal = new CalendarDateSeries<V>(aResolution);
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        retVal.putAll(this);

        return retVal;
    }

    public CalendarDate step(final CalendarDate aKey) {
        return aKey.step(1, myResolution);
    }

    @Override
    public CalendarDateSeries<V> subMap(final CalendarDate aFromKey, final boolean inclusiveFromKey, final CalendarDate aToKey, final boolean inclusiveToKey) {

        final NavigableMap<CalendarDate, V> tmpMap = super.subMap(aFromKey, inclusiveFromKey, aToKey, inclusiveToKey);

        final CalendarDateSeries<V> retVal = new CalendarDateSeries<V>(tmpMap, this.getResolution());
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    @Override
    public CalendarDateSeries<V> subMap(final CalendarDate aFromKey, final CalendarDate aKeyLimit) {

        final SortedMap<CalendarDate, V> tmpMap = super.subMap(aFromKey, aKeyLimit);

        final CalendarDateSeries<V> retVal = new CalendarDateSeries<V>(tmpMap, this.getResolution());
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    @Override
    public CalendarDateSeries<V> tailMap(final CalendarDate aFromKey) {

        final SortedMap<CalendarDate, V> tmpMap = super.tailMap(aFromKey);

        final CalendarDateSeries<V> retVal = new CalendarDateSeries<V>(tmpMap, this.getResolution());
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    @Override
    public CalendarDateSeries<V> tailMap(final CalendarDate aFromKey, final boolean inclusive) {

        final NavigableMap<CalendarDate, V> tmpMap = super.tailMap(aFromKey, inclusive);

        final CalendarDateSeries<V> retVal = new CalendarDateSeries<V>(tmpMap, this.getResolution());
        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        return retVal;
    }

    @Override
    public String toString() {

        final StringBuilder retVal = this.toStringFirstPart();

        retVal.append(this.getResolution());
        retVal.append(ASCII.NBSP);

        this.appendLastPartToString(retVal);

        return retVal.toString();
    }

}
