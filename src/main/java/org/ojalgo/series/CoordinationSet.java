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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.ojalgo.netio.ASCII;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateUnit;

/**
 * A {@link CoordinationSet} is used to coordinate a set of {@link CalendarDateSeries} instances.
 *
 * @author apete
 */
public class CoordinationSet<N extends Comparable<N>> extends HashMap<String, CalendarDateSeries<N>> {

    private static final long serialVersionUID = 1L;

    private transient CalendarDateUnit myResolution = null;

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
    }

    public CalendarDateUnit getResolution() {

        if (myResolution == null) {

            CalendarDateUnit tmpRes = null;

            for (CalendarDateSeries<N> series : this.values()) {

                tmpRes = series.getResolution();

                if ((myResolution == null) || (tmpRes.compareTo(myResolution) > 0)) {
                    myResolution = tmpRes;
                }
            }
        }

        return myResolution;
    }

    public N getValue(final String series, final CalendarDate date) {
        return this.get(series).get(date);
    }

    /**
     * Returns a new CoordinationSet where all series have the same first and last keys, as well as a common
     * (the highest common) resolution.
     */
    public CoordinationSet<N> prune() {
        return this.doPruneAndResample(this.getLatestFirstKey(), this.getEarliestLastKey(), this.getResolution());
    }

    /**
     * Returns a new CoordinationSet where all series have the same first and last keys, as well as the
     * specified resolution.
     */
    public CoordinationSet<N> prune(final CalendarDateUnit resolution) {
        return this.doPruneAndResample(this.getLatestFirstKey(), this.getEarliestLastKey(), resolution);
    }

    /**
     * Vill use the series' name as the key. Make sure you have set the name to something that uniquely
     * identifies the series.
     */
    public CalendarDateSeries<N> put(final CalendarDateSeries<N> series) {
        return this.put(series.getName(), series);
    }

    /**
     * Returns a new set of series each resampled to the supplied resolution. No pruning!
     */
    public CoordinationSet<N> resample(final CalendarDateUnit resolution) {
        return this.doPruneAndResample(this.getEarliestFirstKey(), this.getLatestLastKey(), resolution);
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

    private CoordinationSet<N> doPruneAndResample(final CalendarDate firstKey, final CalendarDate lastKey, final CalendarDateUnit resolution) {

        CoordinationSet<N> retVal = new CoordinationSet<>();

        for (Map.Entry<String, CalendarDateSeries<N>> entry : this.entrySet()) {

            String key = entry.getKey();

            CalendarDateSeries<N> value = entry.getValue();
            CalendarDateSeries<N> pruned = value.subMap(firstKey, true, lastKey, true);
            CalendarDateSeries<N> resampled = (CalendarDateSeries<N>) pruned.resample(resolution::adjustInto);

            retVal.put(key, resampled);
        }

        return retVal;
    }

}
