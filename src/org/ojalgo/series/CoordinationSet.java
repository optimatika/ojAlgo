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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.netio.ASCII;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateUnit;

public class CoordinationSet<V extends Number> extends HashMap<String, CalendarDateSeries<V>> {

    private CalendarDateUnit myResolution = null;

    public CoordinationSet() {
        super();
    }

    public CoordinationSet(final CalendarDateUnit aResolution) {

        super();

        myResolution = aResolution;
    }

    public CoordinationSet(final Collection<CalendarDateSeries<V>> aTimeSeriesCollection) {

        super(aTimeSeriesCollection.size());

        for (final CalendarDateSeries<V> tmpTimeSeries : aTimeSeriesCollection) {
            this.put(tmpTimeSeries);
        }
    }

    public CoordinationSet(final Collection<CalendarDateSeries<V>> aTimeSeriesCollection, final CalendarDateUnit aResolution) {

        super(aTimeSeriesCollection.size());

        myResolution = aResolution;

        for (final CalendarDateSeries<V> tmpTimeSeries : aTimeSeriesCollection) {
            this.put(tmpTimeSeries);
        }
    }

    public CoordinationSet(final int someInitialCapacity) {
        super(someInitialCapacity);
    }

    public CoordinationSet(final int someInitialCapacity, final float someLoadFactor) {
        super(someInitialCapacity, someLoadFactor);
    }

    public CoordinationSet(final Map<? extends String, ? extends CalendarDateSeries<V>> someM) {
        super(someM);
    }

    public void complete() {
        for (final CalendarDateSeries<V> tmpSeries : this.values()) {
            tmpSeries.complete();
        }
    }

    public CoordinationSet<V> copy() {
        return this.resample(this.getResolution());
    }

    @Override
    public CalendarDateSeries<V> get(final Object key) {
        if (key instanceof CalendarDateSeries<?>) {
            return super.get(((CalendarDateSeries<?>) key).getName());
        } else {
            return super.get(key.toString());
        }
    }

    public CalendarDateSeries<V> get(final String aSeriesName) {
        return super.get(aSeriesName);
    }

    public List<CalendarDate> getAllCalendarDates() {

        final TreeSet<CalendarDate> retVal = new TreeSet<CalendarDate>();

        for (final CalendarDateSeries<V> tmpSeries : this.values()) {
            retVal.addAll(tmpSeries.keySet());
        }

        return new ArrayList<CalendarDate>(retVal);
    }

    public List<String> getAllSeriesNames() {
        return new ArrayList<String>(this.keySet());
    }

    public CalendarDate getEarliestFirstKey() {

        CalendarDate retVal = null, tmpVal = null;

        for (final CalendarDateSeries<V> tmpSeries : this.values()) {

            tmpVal = tmpSeries.firstKey();

            if ((retVal == null) || (tmpVal.compareTo(retVal) < 0)) {
                retVal = tmpVal;
            }
        }

        return retVal;
    }

    public CalendarDate getEarliestLastKey() {

        CalendarDate retVal = null, tmpVal = null;

        for (final CalendarDateSeries<V> tmpSeries : this.values()) {

            tmpVal = tmpSeries.lastKey();

            if ((retVal == null) || (tmpVal.compareTo(retVal) < 0)) {
                retVal = tmpVal;
            }
        }

        return retVal;
    }

    public CalendarDate getLatestFirstKey() {

        CalendarDate retVal = null, tmpVal = null;

        for (final CalendarDateSeries<V> tmpSeries : this.values()) {

            tmpVal = tmpSeries.firstKey();

            if ((retVal == null) || (tmpVal.compareTo(retVal) > 0)) {
                retVal = tmpVal;
            }
        }

        return retVal;
    }

    public CalendarDate getLatestLastKey() {

        CalendarDate retVal = null, tmpVal = null;

        for (final CalendarDateSeries<V> tmpSeries : this.values()) {

            tmpVal = tmpSeries.lastKey();

            if ((retVal == null) || (tmpVal.compareTo(retVal) > 0)) {
                retVal = tmpVal;
            }
        }

        return retVal;
    };

    public CalendarDateUnit getResolution() {

        if (myResolution != null) {

            return myResolution;

        } else {

            CalendarDateUnit retVal = null, tmpVal = null;

            for (final CalendarDateSeries<V> tmpSeries : this.values()) {

                tmpVal = tmpSeries.getResolution();

                if ((retVal == null) || (tmpVal.compareTo(retVal) > 0)) {
                    retVal = tmpVal;
                }
            }

            return retVal;
        }
    }

    public V getValue(final String aSeriesName, final CalendarDate aCalendarDate) {
        return this.get(aSeriesName).get(aCalendarDate);
    };

    public void modify(final BinaryFunction<V> aFunc, final V anArg) {
        for (final CalendarDateSeries<V> tmpSeries : this.values()) {
            tmpSeries.modify(aFunc, anArg);
        }
    }

    public void modify(final ParameterFunction<V> aFunc, final int aParam) {
        for (final CalendarDateSeries<V> tmpSeries : this.values()) {
            tmpSeries.modify(aFunc, aParam);
        }
    }

    public void modify(final UnaryFunction<V> aFunc) {
        for (final CalendarDateSeries<V> tmpSeries : this.values()) {
            tmpSeries.modify(aFunc);
        }
    }

    public CoordinationSet<V> prune() {

        final CoordinationSet<V> retVal = new CoordinationSet<V>(this.getResolution());

        final CalendarDate tmpFirstKey = this.getLatestFirstKey();
        final CalendarDate tmpLastKey = this.getEarliestLastKey();

        if (tmpLastKey.compareTo(tmpFirstKey) != -1) {
            for (final CalendarDateSeries<V> tmpSeries : this.values()) {
                final CalendarDateSeries<V> tmpSubMap = tmpSeries.subMap(tmpFirstKey, true, tmpLastKey, true);
                retVal.put(tmpSubMap);
            }
        }

        final CalendarDate tmpEarliestFirstKey = retVal.getEarliestFirstKey();
        final CalendarDate tmpLatestFirstKey = retVal.getLatestFirstKey();
        final CalendarDate tmpEarliestLastKey = retVal.getEarliestLastKey();
        final CalendarDate tmpLatestLastKey = retVal.getLatestLastKey();
        if (!tmpEarliestFirstKey.equals(tmpFirstKey)) {
            throw new ProgrammingError("Something went wrong!");
        }
        if (!tmpLatestFirstKey.equals(tmpFirstKey)) {
            throw new ProgrammingError("Something went wrong!");
        }
        if (!tmpEarliestLastKey.equals(tmpLastKey)) {
            throw new ProgrammingError("Something went wrong!");
        }
        if (!tmpLatestLastKey.equals(tmpLastKey)) {
            throw new ProgrammingError("Something went wrong!");
        }

        return retVal;
    }

    public CoordinationSet<V> prune(final CalendarDateUnit aResolution) {

        final CoordinationSet<V> retVal = new CoordinationSet<V>(aResolution);

        final CalendarDate tmpLatestFirstKey = this.getLatestFirstKey();
        final CalendarDate tmpEarliestLastKey = this.getEarliestLastKey();

        for (final CalendarDateSeries<V> tmpSeries : this.values()) {
            retVal.put(tmpSeries.resample(tmpLatestFirstKey, tmpEarliestLastKey, aResolution));
        }

        return retVal;
    }

    public CalendarDateSeries<V> put(final CalendarDateSeries<V> aSeries) {
        return this.put(aSeries.getName(), aSeries);
    }

    public CoordinationSet<V> resample(final CalendarDateUnit aResolution) {

        final CoordinationSet<V> retVal = new CoordinationSet<V>(aResolution);

        for (final CalendarDateSeries<V> tmpSeries : this.values()) {
            retVal.put(tmpSeries.resample(aResolution));
        }

        return retVal;
    }

    @Override
    public String toString() {

        final StringBuilder retVal = new StringBuilder(this.getClass().getSimpleName() + '@' + Integer.toHexString(this.hashCode()));

        for (final CalendarDateSeries<V> tmpSeries : this.values()) {
            retVal.append(ASCII.LF);
            retVal.append(tmpSeries.toString());
        }

        return retVal.toString();
    }

}
