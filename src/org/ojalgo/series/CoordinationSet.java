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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.ojalgo.ProgrammingError;
import org.ojalgo.netio.ASCII;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateUnit;

/**
 * A {@link CoordinationSet} is used to coordinate a set of {@link CalendarDateSeries} instances.
 *
 * @author apete
 */
public class CoordinationSet<N extends Number> extends HashMap<String, CalendarDateSeries<N>> {

    private CalendarDateUnit myResolution = null;

    public CoordinationSet() {
        super();
    }

    public CoordinationSet(final CalendarDateUnit resolution) {

        super();

        myResolution = resolution;
    }

    public CoordinationSet(final Collection<CalendarDateSeries<N>> seriesCollection) {

        super(seriesCollection.size());

        for (final CalendarDateSeries<N> tmpTimeSeries : seriesCollection) {
            this.put(tmpTimeSeries);
        }
    }

    public CoordinationSet(final Collection<CalendarDateSeries<N>> seriesCollection, final CalendarDateUnit resolution) {

        super(seriesCollection.size());

        myResolution = resolution;

        for (final CalendarDateSeries<N> tmpTimeSeries : seriesCollection) {
            this.put(tmpTimeSeries);
        }
    }

    public CoordinationSet(final int initialCapacity) {
        super(initialCapacity);
    }

    public CoordinationSet(final int initialCapacity, final float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CoordinationSet(final Map<? extends String, ? extends CalendarDateSeries<N>> members) {
        super(members);
    }

    /**
     * Will call {@link CalendarDateSeries#complete()} on each of the instances in this set.
     */
    public void complete() {
        for (final CalendarDateSeries<N> tmpSeries : this.values()) {
            tmpSeries.complete();
        }
    }

    public CoordinationSet<N> copy() {
        return this.resample(this.getResolution());
    }

    @Override
    public CalendarDateSeries<N> get(final Object key) {
        if (key instanceof CalendarDateSeries<?>) {
            return super.get(((CalendarDateSeries<?>) key).getName());
        } else {
            return super.get(key.toString());
        }
    }

    public CalendarDateSeries<N> get(final String seriesName) {
        return super.get(seriesName);
    }

    public List<CalendarDate> getAllCalendarDates() {

        final TreeSet<CalendarDate> retVal = new TreeSet<>();

        for (final CalendarDateSeries<N> tmpSeries : this.values()) {
            retVal.addAll(tmpSeries.keySet());
        }

        return new ArrayList<>(retVal);
    }

    public List<String> getAllSeriesNames() {
        return new ArrayList<>(this.keySet());
    }

    public CalendarDate getEarliestFirstKey() {
        return BasicSeries.findEarliestFirstKey(this.values());
    }

    public CalendarDate getEarliestLastKey() {
        return BasicSeries.findEarliestLastKey(this.values());
    }

    public CalendarDate getLatestFirstKey() {
        return BasicSeries.findLatestFirstKey(this.values());
    }

    public CalendarDate getLatestLastKey() {
        return BasicSeries.findLatestLastKey(this.values());
    };

    public CalendarDateUnit getResolution() {

        if (myResolution != null) {

            return myResolution;

        } else {

            CalendarDateUnit retVal = null, tmpVal = null;

            for (final CalendarDateSeries<N> tmpSeries : this.values()) {

                tmpVal = tmpSeries.getResolution();

                if ((retVal == null) || (tmpVal.compareTo(retVal) > 0)) {
                    retVal = tmpVal;
                }
            }

            return retVal;
        }
    }

    public N getValue(final String series, final CalendarDate date) {
        return this.get(series).get(date);
    };

    /**
     * @return A new CoordinationSet where all series have the same first and last keys.
     */
    public CoordinationSet<N> prune() {

        final CoordinationSet<N> retVal = new CoordinationSet<>(this.getResolution());

        final CalendarDate tmpFirstKey = this.getLatestFirstKey();
        final CalendarDate tmpLastKey = this.getEarliestLastKey();

        if (tmpLastKey.compareTo(tmpFirstKey) != -1) {
            for (final CalendarDateSeries<N> tmpSeries : this.values()) {
                final CalendarDateSeries<N> tmpSubMap = tmpSeries.subMap(tmpFirstKey, true, tmpLastKey, true);
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
    public CoordinationSet<N> prune(final CalendarDateUnit resolution) {

        final CoordinationSet<N> retVal = new CoordinationSet<>(resolution);

        final CalendarDate tmpLatestFirstKey = this.getLatestFirstKey();
        final CalendarDate tmpEarliestLastKey = this.getEarliestLastKey();

        for (final Map.Entry<String, CalendarDateSeries<N>> tmpEntry : this.entrySet()) {
            retVal.put(tmpEntry.getKey(), tmpEntry.getValue().resample(tmpLatestFirstKey, tmpEarliestLastKey, resolution));
        }

        return retVal;
    }

    /**
     * Vill use the series' name as the key. Make sure you have set the name to something that uniquely
     * identifies the series.
     */
    public CalendarDateSeries<N> put(final CalendarDateSeries<N> series) {
        return this.put(series.getName(), series);
    }

    /**
     * @param resolution The new resolution
     * @return A new set of series each resampled to the supplied resolution
     */
    public CoordinationSet<N> resample(final CalendarDateUnit resolution) {

        final CoordinationSet<N> retVal = new CoordinationSet<>(resolution);

        for (final java.util.Map.Entry<String, CalendarDateSeries<N>> tmpEntry : this.entrySet()) {
            retVal.put(tmpEntry.getKey(), tmpEntry.getValue().resample(resolution));
        }

        return retVal;
    }

    @Override
    public String toString() {

        final StringBuilder retVal = new StringBuilder(this.getClass().getSimpleName() + '@' + Integer.toHexString(this.hashCode()));

        for (final CalendarDateSeries<N> tmpSeries : this.values()) {
            retVal.append(ASCII.LF);
            retVal.append(tmpSeries.toString());
        }

        return retVal.toString();
    }

}
