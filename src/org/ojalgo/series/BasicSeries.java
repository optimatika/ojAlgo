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
import java.util.Map;
import java.util.SortedMap;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Structure1D;
import org.ojalgo.array.DenseArray;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.series.primitive.CoordinatedSet;
import org.ojalgo.series.primitive.PrimitiveSeries;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.ColourData;
import org.ojalgo.type.TimeIndex;
import org.ojalgo.type.keyvalue.KeyValue;

/**
 * A BasicSeries is a {@linkplain SortedMap} with:
 * <ul>
 * <li>Keys restricted to {@linkplain Comparable} (the keys have a natural order)</li>
 * <li>Values restricted to {@linkplain Number} (you can do maths on the values)</li>
 * <li>The option to associate a name and colour with the data</li>
 * <li>The option to define an accumlator function to be used with multilple/subsequent put operations on the
 * same key</li>
 * <li>Some additional methods to work with primitive keys and values more efficiently</li>
 * <li>A few additional methods to help access and modify series entries</li>
 * </ul>
 * The extension {@link NaturallySequenced} is typically used with time series data.
 *
 * @author apete
 */
public interface BasicSeries<K extends Comparable<? super K>, V extends Number> extends SortedMap<K, V> {

    /**
     * A series with naturally sequenced keys - given any key there is a natural "next" key, e.g. with a
     * series of daily values the natural next key is the next day. Further natural sequencing implies a
     * bidirectional mapping between the keys and long indices.
     *
     * @author apete
     */
    interface NaturallySequenced<K extends Comparable<? super K>, V extends Number> extends BasicSeries<K, V>, Access1D<V> {

        default PrimitiveSeries asPrimitive() {
            return PrimitiveSeries.wrap(this);
        }

        /**
         * Will fill in missing values, inbetween the first and last keys.
         */
        default void complete() {

            K tmpKey = this.firstKey();
            V tmpVal = null;

            V patchVal = this.firstValue();

            final K lastKey = this.lastKey();
            while (tmpKey.compareTo(lastKey) <= 0) {

                tmpVal = this.get(tmpKey);

                if (tmpVal != null) {
                    patchVal = tmpVal;
                } else {
                    this.put(tmpKey, patchVal);
                }

                tmpKey = this.step(tmpKey);
            }
        }

        default long count() {
            return this.size();
        }

        IndexMapper<K> mapper();

        /**
         * @return The next, after the {@link #lastKey()}, key.
         */
        default K nextKey() {
            return this.step(this.lastKey());
        }

        double put(long index, double value);

        V put(long index, V value);

        NaturallySequenced<K, V> resample(CalendarDate.Resolution resolution);

        NaturallySequenced<K, V> resample(K firstKey, K lastKey, CalendarDate.Resolution resolution);

        K step(K key);

    }

    public static final class TimeSeriesBuilder<K extends Comparable<? super K>> {

        private K myReference = null;
        private CalendarDate.Resolution myResolution = null;
        private final TimeIndex<K> myTimeIndex;

        TimeSeriesBuilder(final TimeIndex<K> timeIndex) {
            super();
            myTimeIndex = timeIndex;
        }

        public <N extends Number> BasicSeries.NaturallySequenced<K, N> build(final DenseArray.Factory<N> arrayFactory) {
            ProgrammingError.throwIfNull(arrayFactory);
            return this.doBuild(arrayFactory, null);
        }

        public <N extends Number> BasicSeries.NaturallySequenced<K, N> build(final DenseArray.Factory<N> arrayFactory, final BinaryFunction<N> accumularor) {
            ProgrammingError.throwIfNull(arrayFactory, accumularor);
            return this.doBuild(arrayFactory, accumularor);
        }

        public TimeSeriesBuilder<K> reference(final K reference) {
            myReference = reference;
            return this;
        }

        public TimeSeriesBuilder<K> resolution(final CalendarDate.Resolution resolution) {
            myResolution = resolution;
            return this;
        }

        private <N extends Number> BasicSeries.NaturallySequenced<K, N> doBuild(final DenseArray.Factory<N> arrayFactory, final BinaryFunction<N> accumularor) {
            if (myReference != null) {
                if (myResolution != null) {
                    return new MappedIndexSeries<>(arrayFactory, myTimeIndex.from(myReference, myResolution), accumularor);
                } else {
                    return new MappedIndexSeries<>(arrayFactory, myTimeIndex.from(myReference), accumularor);
                }
            } else {
                if (myResolution != null) {
                    return new MappedIndexSeries<>(arrayFactory, myTimeIndex.plain(myResolution), accumularor);
                } else {
                    return new MappedIndexSeries<>(arrayFactory, myTimeIndex.plain(), accumularor);
                }
            }
        }

    }

    public static final BasicSeries.TimeSeriesBuilder<Calendar> CALENDAR = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.CALENDAR);
    public static final BasicSeries.TimeSeriesBuilder<CalendarDate> CALENDAR_DATE = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.CALENDAR_DATE);
    public static final BasicSeries.TimeSeriesBuilder<Date> DATE = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.DATE);
    public static final BasicSeries.TimeSeriesBuilder<Instant> INSTANT = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.INSTANT);
    public static final BasicSeries.TimeSeriesBuilder<LocalDate> LOCAL_DATE = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.LOCAL_DATE);
    public static final BasicSeries.TimeSeriesBuilder<LocalDateTime> LOCAL_DATE_TIME = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.LOCAL_DATE_TIME);
    public static final BasicSeries.TimeSeriesBuilder<LocalTime> LOCAL_TIME = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.LOCAL_TIME);
    public static final BasicSeries.TimeSeriesBuilder<OffsetDateTime> OFFSET_DATE_TIME = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.OFFSET_DATE_TIME);
    public static final BasicSeries.TimeSeriesBuilder<ZonedDateTime> ZONED_DATE_TIME = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.ZONED_DATE_TIME);

    public static BasicSeries<Double, Double> make(final DenseArray.Factory<Double> arrayFactory) {
        return new MappedIndexSeries<>(arrayFactory, MappedIndexSeries.MAPPER, null);
    }

    public static BasicSeries<Double, Double> make(final DenseArray.Factory<Double> arrayFactory, final BinaryFunction<Double> accumulator) {
        return new MappedIndexSeries<>(arrayFactory, MappedIndexSeries.MAPPER, accumulator);
    }

    public static <N extends Number & Comparable<? super N>> BasicSeries<N, N> make(final DenseArray.Factory<N> arrayFactory,
            final Structure1D.IndexMapper<N> indexMapper) {
        return new MappedIndexSeries<>(arrayFactory, indexMapper, null);
    }

    public static <N extends Number & Comparable<? super N>> BasicSeries<N, N> make(final DenseArray.Factory<N> arrayFactory,
            final Structure1D.IndexMapper<N> indexMapper, final BinaryFunction<N> accumulator) {
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

    PrimitiveSeries asPrimitive();

    BasicSeries<K, V> colour(ColourData colour);

    double doubleValue(final K key);

    V firstValue();

    V get(final K key);

    ColourData getColour();

    String getName();

    V lastValue();

    BasicSeries<K, V> name(String name);

    /**
     * @see #put(Comparable, Number)
     */
    double put(final K key, final double value);

    /**
     * Some implementations may specify an accumulator function to be used with subsequent put operation with
     * the same key. If such an accumulator is prsent it should be used here, and in that case the method
     * returns the new/accumulated/mixed value. With out an accumulator this method should behave exactly as
     * with any other {@link Map}.
     *
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    V put(final K key, final V value);

    default void putAll(final Collection<? extends KeyValue<? extends K, ? extends V>> data) {
        for (final KeyValue<? extends K, ? extends V> tmpKeyValue : data) {
            this.put(tmpKeyValue.getKey(), tmpKeyValue.getValue());
        }
    }

}
