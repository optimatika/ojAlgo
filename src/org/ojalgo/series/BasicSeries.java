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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;

import org.ojalgo.access.IndexMapper;
import org.ojalgo.array.DenseArray;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.series.primitive.CoordinatedSet;
import org.ojalgo.series.primitive.DataSeries;
import org.ojalgo.series.primitive.PrimitiveSeries;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateDuration;
import org.ojalgo.type.ColourData;
import org.ojalgo.type.TimeIndex;
import org.ojalgo.type.keyvalue.KeyValue;

/**
 * A BasicSeries is a {@linkplain SortedMap} with:
 * <ul>
 * <li>Keys restricted to Comparable</li>
 * <li>Values restricted to Number</li>
 * <li>The option to associate a name and colour with the data</li>
 * <li>A few additional methods to help access and modify series entries</li>
 * </ul>
 *
 * @author apete
 */
public interface BasicSeries<K extends Comparable<? super K>, V extends Number> extends SortedMap<K, V> {

    public class Builder<K extends Comparable<? super K>> {

        private transient K myReference = null;
        private transient CalendarDateDuration myResolution = null;
        private final TimeIndex<K> myTimeIndex;

        private Builder(final TimeIndex<K> timeIndex) {
            super();
            myTimeIndex = timeIndex;
        }

        public <N extends Number> BasicSeries<K, N> build(final DenseArray.Factory<N> arrayFactory) {
            if (myReference != null) {
                if (myResolution != null) {
                    return new DenseSeries<>(arrayFactory, myTimeIndex.from(myReference, myResolution));
                } else {
                    return new DenseSeries<>(arrayFactory, myTimeIndex.from(myReference));
                }
            } else {
                return new SparseSeries<>(arrayFactory, myTimeIndex.plain());
            }
        }

        public Builder<K> reference(final K reference) {
            myReference = reference;
            return this;
        }

        public Builder<K> resolution(final CalendarDateDuration resolution) {
            myResolution = resolution;
            return this;
        }

    }

    public static final BasicSeries.Builder<Calendar> CALENDAR = new BasicSeries.Builder<>(TimeIndex.CALENDAR);
    public static final BasicSeries.Builder<CalendarDate> CALENDAR_DATE = new BasicSeries.Builder<>(TimeIndex.CALENDAR_DATE);
    public static final BasicSeries.Builder<Date> DATE = new BasicSeries.Builder<>(TimeIndex.DATE);
    public static final BasicSeries.Builder<Instant> INSTANT = new BasicSeries.Builder<>(TimeIndex.INSTANT);
    public static final BasicSeries.Builder<LocalDate> LOCAL_DATE = new BasicSeries.Builder<>(TimeIndex.LOCAL_DATE);
    public static final BasicSeries.Builder<LocalDateTime> LOCAL_DATE_TIME = new BasicSeries.Builder<>(TimeIndex.LOCAL_DATE_TIME);
    public static final BasicSeries.Builder<LocalTime> LOCAL_TIME = new BasicSeries.Builder<>(TimeIndex.LOCAL_TIME);
    public static final BasicSeries.Builder<Long> LONG = new BasicSeries.Builder<>(TimeIndex.LONG);
    public static final BasicSeries.Builder<OffsetDateTime> OFFSET_DATE_TIME = new BasicSeries.Builder<>(TimeIndex.OFFSET_DATE_TIME);
    public static final BasicSeries.Builder<ZonedDateTime> ZONED_DATE_TIME = new BasicSeries.Builder<>(TimeIndex.ZONED_DATE_TIME);

    public static BasicSeries<Double, Double> make(DenseArray.Factory<Double> arrayFactory) {
        return new SparseSeries<>(arrayFactory, new IndexMapper<Double>() {

            public long toIndex(Double key) {
                return Double.doubleToLongBits(key);
            }

            public Double toKey(long index) {
                return Double.longBitsToDouble(index);
            }
        });
    }

    public static <N extends Number & Comparable<N>> BasicSeries<N, N> make(final DenseArray.Factory<N> arrayFactory, final IndexMapper<N> indexMapper) {
        return new SparseSeries<>(arrayFactory, indexMapper);
    }

    static <K extends Comparable<? super K>> CoordinatedSet<K> coordinate(final List<? extends BasicSeries<K, ?>> uncoordinated) {
        return CoordinatedSet.from(uncoordinated);
    }

    static <K extends Comparable<? super K>> K findEarliestFirstKey(final Collection<? extends BasicSeries<K, ?>> collection) {

        K retVal = null, tmpVal = null;

        for (final BasicSeries<K, ?> individual : collection) {

            tmpVal = individual.firstKey();

            if ((retVal == null) || (tmpVal.compareTo(retVal) < 0)) {
                retVal = tmpVal;
            }
        }

        return retVal;
    }

    static <K extends Comparable<? super K>> K findEarliestLastKey(final Collection<? extends BasicSeries<K, ?>> collection) {

        K retVal = null, tmpVal = null;

        for (final BasicSeries<K, ?> individual : collection) {

            tmpVal = individual.lastKey();

            if ((retVal == null) || (tmpVal.compareTo(retVal) < 0)) {
                retVal = tmpVal;
            }
        }

        return retVal;
    }

    static <K extends Comparable<? super K>> K findLatestFirstKey(final Collection<? extends BasicSeries<K, ?>> collection) {

        K retVal = null, tmpVal = null;

        for (final BasicSeries<K, ?> individual : collection) {

            tmpVal = individual.firstKey();

            if ((retVal == null) || (tmpVal.compareTo(retVal) > 0)) {
                retVal = tmpVal;
            }
        }

        return retVal;
    }

    static <K extends Comparable<? super K>> K findLatestLastKey(final Collection<? extends BasicSeries<K, ?>> collection) {

        K retVal = null, tmpVal = null;

        for (final BasicSeries<K, ?> individual : collection) {

            tmpVal = individual.lastKey();

            if ((retVal == null) || (tmpVal.compareTo(retVal) > 0)) {
                retVal = tmpVal;
            }
        }

        return retVal;
    }

    BasicSeries<K, V> colour(ColourData colour);

    double doubleValue(final K key);

    V firstValue();

    ColourData getColour();

    /**
     * @deprecated v41 Use {@link #getPrimitiveSeries()} instead
     */
    @Deprecated
    DataSeries getDataSeries();

    String getName();

    PrimitiveSeries getPrimitiveSeries();

    /**
     * @deprecated v41 Use {@link #getPrimitiveSeries()} instead
     */
    @Deprecated
    double[] getPrimitiveValues();

    V lastValue();

    /**
     * @deprecated v41 Use {@link #getPrimitiveSeries()} instead
     */
    @Deprecated
    void modifyAll(UnaryFunction<V> function);

    BasicSeries<K, V> name(String name);

    /**
     * @return The next, after the {@link #lastKey()}, key.
     */
    K nextKey();

    double put(final K key, final double value);

    V put(final K key, final V value);

    default void putAll(final Collection<? extends KeyValue<? extends K, ? extends V>> data) {
        for (final KeyValue<? extends K, ? extends V> tmpKeyValue : data) {
            this.put(tmpKeyValue.getKey(), tmpKeyValue.getValue());
        }
    }

}
