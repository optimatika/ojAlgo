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
import org.ojalgo.function.BinaryFunction;
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

    public class TimeSeriesBuilder<K extends Comparable<? super K>> {

        private K myReference = null;
        private CalendarDateDuration myResolution = null;
        private final TimeIndex<K> myTimeIndex;

        TimeSeriesBuilder(final TimeIndex<K> timeIndex) {
            super();
            myTimeIndex = timeIndex;
        }

        public <N extends Number> BasicSeries<K, N> build(final DenseArray.Factory<N> arrayFactory) {
            return build(arrayFactory, null);
        }

        public <N extends Number> BasicSeries<K, N> build(final DenseArray.Factory<N> arrayFactory, BinaryFunction<N> accumularor) {
            if (myReference != null) {
                if (myResolution != null) {
                    return new MappedIndexSeries<>(arrayFactory, myTimeIndex.from(myReference, myResolution), accumularor);
                } else {
                    return new MappedIndexSeries<>(arrayFactory, myTimeIndex.from(myReference), accumularor);
                }
            } else {
                return new MappedIndexSeries<>(arrayFactory, myTimeIndex.plain(), accumularor);
            }
        }

        public TimeSeriesBuilder<K> reference(final K reference) {
            myReference = reference;
            return this;
        }

        public TimeSeriesBuilder<K> resolution(final CalendarDateDuration resolution) {
            myResolution = resolution;
            return this;
        }

    }

    public static final BasicSeries.TimeSeriesBuilder<Calendar> CALENDAR = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.CALENDAR);
    public static final BasicSeries.TimeSeriesBuilder<CalendarDate> CALENDAR_DATE = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.CALENDAR_DATE);
    public static final BasicSeries.TimeSeriesBuilder<Date> DATE = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.DATE);
    public static final BasicSeries.TimeSeriesBuilder<Instant> INSTANT = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.INSTANT);
    public static final BasicSeries.TimeSeriesBuilder<LocalDate> LOCAL_DATE = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.LOCAL_DATE);
    public static final BasicSeries.TimeSeriesBuilder<LocalDateTime> LOCAL_DATE_TIME = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.LOCAL_DATE_TIME);
    public static final BasicSeries.TimeSeriesBuilder<LocalTime> LOCAL_TIME = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.LOCAL_TIME);
    public static final BasicSeries.TimeSeriesBuilder<Long> LONG = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.LONG);
    public static final BasicSeries.TimeSeriesBuilder<OffsetDateTime> OFFSET_DATE_TIME = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.OFFSET_DATE_TIME);
    public static final BasicSeries.TimeSeriesBuilder<ZonedDateTime> ZONED_DATE_TIME = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.ZONED_DATE_TIME);

    public static BasicSeries<Double, Double> make(DenseArray.Factory<Double> arrayFactory) {
        return new MappedIndexSeries<>(arrayFactory, MappedIndexSeries.MAPPER, null);
    }

    public static BasicSeries<Double, Double> make(DenseArray.Factory<Double> arrayFactory, final BinaryFunction<Double> accumulator) {
        return new MappedIndexSeries<>(arrayFactory, MappedIndexSeries.MAPPER, accumulator);
    }

    public static <N extends Number & Comparable<? super N>> BasicSeries<N, N> make(final DenseArray.Factory<N> arrayFactory,
            final IndexMapper<N> indexMapper) {
        return new MappedIndexSeries<>(arrayFactory, indexMapper, null);
    }

    public static <N extends Number & Comparable<? super N>> BasicSeries<N, N> make(final DenseArray.Factory<N> arrayFactory, final IndexMapper<N> indexMapper,
            final BinaryFunction<N> accumulator) {
        return new MappedIndexSeries<>(arrayFactory, indexMapper, accumulator);
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

    public V get(double key);

    public V get(long key);

    BasicSeries<K, V> colour(ColourData colour);

    double doubleValue(final double key);

    double doubleValue(final K key);

    double doubleValue(final long key);

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

    double put(final double key, final double value);

    V put(final double key, final V value);

    double put(final K key, final double value);

    V put(final K key, final V value);

    double put(final long key, final double value);

    V put(final long key, final V value);

    default void putAll(final Collection<? extends KeyValue<? extends K, ? extends V>> data) {
        for (final KeyValue<? extends K, ? extends V> tmpKeyValue : data) {
            this.put(tmpKeyValue.getKey(), tmpKeyValue.getValue());
        }
    }

}
