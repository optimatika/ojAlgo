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

/**
 * A {@link CoordinationSet} is used to coordinate a set of {@link CalendarDateSeries} instances.
 *
 * @author apete
 */
public class CoordinationSet<V extends Number> extends HashMap<String, CalendarDateSeries<V>> {

    private CalendarDateUnit myResolution = null;

    public CoordinationSet() {
        super();
    }

    public CoordinationSet(final CalendarDateUnit resolution) {

        super();

        myResolution = resolution;
    }

    public CoordinationSet(final Collection<CalendarDateSeries<V>> seriesCollection) {

        super(seriesCollection.size());

        for (final CalendarDateSeries<V> tmpTimeSeries : seriesCollection) {
            this.put(tmpTimeSeries);
        }
    }

    public CoordinationSet(final Collection<CalendarDateSeries<V>> seriesCollection, final CalendarDateUnit resolution) {

        super(seriesCollection.size());

        myResolution = resolution;

        for (final CalendarDateSeries<V> tmpTimeSeries : seriesCollection) {
            this.put(tmpTimeSeries);
        }
    }

    public CoordinationSet(final int initialCapacity) {
        super(initialCapacity);
    }

    public CoordinationSet(final int initialCapacity, final float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CoordinationSet(final Map<? extends String, ? extends CalendarDateSeries<V>> members) {
        super(members);
    }

    /**
     * Will call {@link CalendarDateSeries#complete()} on each of the instances in this set.
     */
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

    public V getValue(final String series, final CalendarDate date) {
        return this.get(series).get(date);
    };

    public void modify(final BinaryFunction<V> function, final V argument) {
        for (final CalendarDateSeries<V> tmpSeries : this.values()) {
            tmpSeries.modify(function, argument);
        }
    }

    public void modify(final ParameterFunction<V> function, final int parameter) {
        for (final CalendarDateSeries<V> tmpSeries : this.values()) {
            tmpSeries.modify(function, parameter);
        }
    }

    public void modify(final UnaryFunction<V> function) {
        for (final CalendarDateSeries<V> tmpSeries : this.values()) {
            tmpSeries.modifyAll(function);
        }
    }

    /**
     * @return A new CoordinationSet<V> where all series have the same first and last keys.
     */
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
        if ((tmpEarliestFirstKey == null) || !tmpEarliestFirstKey.equals(tmpFirstKey)) {
            throw new ProgrammingError("Something went wrong!");
        }
        if ((tmpLatestFirstKey == null) || !tmpLatestFirstKey.equals(tmpFirstKey)) {
            throw new ProgrammingError("Something went wrong!");
        }
        if ((tmpEarliestLastKey == null) || !tmpEarliestLastKey.equals(tmpLastKey)) {
            throw new ProgrammingError("Something went wrong!");
        }
        if ((tmpLatestLastKey == null) || !tmpLatestLastKey.equals(tmpLastKey)) {
            throw new ProgrammingError("Something went wrong!");
        }

        return retVal;
    }

    /**
     * Will prune and resample the data
     */
    public CoordinationSet<V> prune(final CalendarDateUnit resolution) {

        final CoordinationSet<V> retVal = new CoordinationSet<V>(resolution);

        final CalendarDate tmpLatestFirstKey = this.getLatestFirstKey();
        final CalendarDate tmpEarliestLastKey = this.getEarliestLastKey();

        for (final java.util.Map.Entry<String, CalendarDateSeries<V>> tmpEntry : this.entrySet()) {
            retVal.put(tmpEntry.getKey(), tmpEntry.getValue().resample(tmpLatestFirstKey, tmpEarliestLastKey, resolution));
        }

        return retVal;
    }

    public CalendarDateSeries<V> put(final CalendarDateSeries<V> aSeries) {
        return this.put(aSeries.getName(), aSeries);
    }

    /**
     * @param resolution The new resolution
     * @return A new set of series each resampled to the supplied resolution
     */
    public CoordinationSet<V> resample(final CalendarDateUnit resolution) {

        final CoordinationSet<V> retVal = new CoordinationSet<V>(resolution);

        for (final java.util.Map.Entry<String, CalendarDateSeries<V>> tmpEntry : this.entrySet()) {
            retVal.put(tmpEntry.getKey(), tmpEntry.getValue().resample(resolution));
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
