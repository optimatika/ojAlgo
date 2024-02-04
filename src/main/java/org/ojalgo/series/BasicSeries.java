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
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.DenseArray;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.series.primitive.CoordinatedSet;
import org.ojalgo.series.primitive.DataSeries;
import org.ojalgo.series.primitive.PrimitiveSeries;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.ColourData;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.TimeIndex;
import org.ojalgo.type.keyvalue.EntryPair;

/**
 * A BasicSeries is a {@linkplain SortedMap} with:
 * <ul>
 * <li>Keys restricted to {@linkplain Comparable} (the keys have a natural order)
 * <li>Values restricted to {@linkplain Comparable} (the values are "numeric" as in extending {@link Number}
 * or implementing {@link NumberDefinition}.
 * <li>The option to associate a name and colour with the data.
 * <li>A few additional methods to help access and modify series entries.
 * </ul>
 *
 * @author apete
 */
public interface BasicSeries<K extends Comparable<? super K>, V extends Comparable<V>> extends SortedMap<K, V> {

    /**
     * A series with naturally sequenced keys - given any key there is a natural "next" key, e.g. with a
     * series of daily values the natural next key is the next day.
     *
     * @author apete
     */
    interface NaturallySequenced<K extends Comparable<? super K>, V extends Comparable<V>> extends BasicSeries<K, V> {

        /**
         * Using the natural sequencing as the key incrementor.
         *
         * @see BasicSeries#complete(UnaryOperator)
         */
        void complete();

        /**
         * @return The next, after the {@link #lastKey()}, key.
         */
        default K nextKey() {
            return this.step(this.lastKey());
        }

        /**
         * Will step (increment) the key given to the next in the natural sequence.
         */
        K step(final K key);

    }

    public static final class TimeSeriesBuilder<K extends Comparable<? super K>> {

        private K myReference = null;
        private CalendarDate.Resolution myResolution = null;
        private final TimeIndex<K> myTimeIndex;

        TimeSeriesBuilder(final TimeIndex<K> timeIndex) {
            super();
            myTimeIndex = timeIndex;
        }

        public <N extends Comparable<N>> BasicSeries<K, N> build(final DenseArray.Factory<N> denseArrayFactory) {
            ProgrammingError.throwIfNull(denseArrayFactory);
            return this.doBuild(denseArrayFactory, null);
        }

        public <N extends Comparable<N>> BasicSeries<K, N> build(final DenseArray.Factory<N> denseArrayFactory, final BinaryFunction<N> accumularor) {
            ProgrammingError.throwIfNull(denseArrayFactory, accumularor);
            return this.doBuild(denseArrayFactory, accumularor);
        }

        public TimeSeriesBuilder<K> reference(final K reference) {
            myReference = reference;
            return this;
        }

        public TimeSeriesBuilder<K> resolution(final CalendarDate.Resolution resolution) {
            myResolution = resolution;
            return this;
        }

        private <N extends Comparable<N>> BasicSeries<K, N> doBuild(final DenseArray.Factory<N> arrayFactory, final BinaryFunction<N> accumularor) {
            if (myReference != null) {
                if (myResolution != null) {
                    return new MappedIndexSeries<>(arrayFactory, myTimeIndex.from(myReference, myResolution), accumularor);
                } else {
                    return new MappedIndexSeries<>(arrayFactory, myTimeIndex.from(myReference), accumularor);
                }
            } else if (myResolution != null) {
                return new MappedIndexSeries<>(arrayFactory, myTimeIndex.plain(myResolution), accumularor);
            } else {
                return new MappedIndexSeries<>(arrayFactory, myTimeIndex.plain(), accumularor);
            }
        }

    }

    BasicSeries.TimeSeriesBuilder<Calendar> CALENDAR = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.CALENDAR);
    BasicSeries.TimeSeriesBuilder<CalendarDate> CALENDAR_DATE = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.CALENDAR_DATE);
    BasicSeries.TimeSeriesBuilder<Date> DATE = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.DATE);
    BasicSeries.TimeSeriesBuilder<Instant> INSTANT = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.INSTANT);
    BasicSeries.TimeSeriesBuilder<LocalDate> LOCAL_DATE = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.LOCAL_DATE);
    BasicSeries.TimeSeriesBuilder<LocalDateTime> LOCAL_DATE_TIME = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.LOCAL_DATE_TIME);
    BasicSeries.TimeSeriesBuilder<LocalTime> LOCAL_TIME = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.LOCAL_TIME);
    BasicSeries.TimeSeriesBuilder<OffsetDateTime> OFFSET_DATE_TIME = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.OFFSET_DATE_TIME);
    BasicSeries.TimeSeriesBuilder<ZonedDateTime> ZONED_DATE_TIME = new BasicSeries.TimeSeriesBuilder<>(TimeIndex.ZONED_DATE_TIME);

    static <K extends Comparable<? super K>> CoordinatedSet<K> coordinate(final List<? extends BasicSeries<K, ?>> uncoordinated) {
        return CoordinatedSet.from(uncoordinated);
    }

    static <K extends Comparable<? super K>> K findEarliestFirstKey(final Collection<? extends BasicSeries<K, ?>> collection) {

        K retVal = null, tmpVal = null;

        for (BasicSeries<K, ?> individual : collection) {

            tmpVal = individual.firstKey();

            if ((retVal == null) || (tmpVal.compareTo(retVal) < 0)) {
                retVal = tmpVal;
            }
        }

        return retVal;
    }

    static <K extends Comparable<? super K>> K findEarliestLastKey(final Collection<? extends BasicSeries<K, ?>> collection) {

        K retVal = null, tmpVal = null;

        for (BasicSeries<K, ?> individual : collection) {

            tmpVal = individual.lastKey();

            if ((retVal == null) || (tmpVal.compareTo(retVal) < 0)) {
                retVal = tmpVal;
            }
        }

        return retVal;
    }

    static <K extends Comparable<? super K>> K findLatestFirstKey(final Collection<? extends BasicSeries<K, ?>> collection) {

        K retVal = null, tmpVal = null;

        for (BasicSeries<K, ?> individual : collection) {

            tmpVal = individual.firstKey();

            if ((retVal == null) || (tmpVal.compareTo(retVal) > 0)) {
                retVal = tmpVal;
            }
        }

        return retVal;
    }

    static <K extends Comparable<? super K>> K findLatestLastKey(final Collection<? extends BasicSeries<K, ?>> collection) {

        K retVal = null, tmpVal = null;

        for (BasicSeries<K, ?> individual : collection) {

            tmpVal = individual.lastKey();

            if ((retVal == null) || (tmpVal.compareTo(retVal) > 0)) {
                retVal = tmpVal;
            }
        }

        return retVal;
    }

    static BasicSeries<Double, Double> make(final DenseArray.Factory<Double> arrayFactory) {
        return new MappedIndexSeries<>(arrayFactory, MappedIndexSeries.MAPPER, null);
    }

    static BasicSeries<Double, Double> make(final DenseArray.Factory<Double> arrayFactory, final BinaryFunction<Double> accumulator) {
        return new MappedIndexSeries<>(arrayFactory, MappedIndexSeries.MAPPER, accumulator);
    }

    static <N extends Comparable<N>> BasicSeries<N, N> make(final DenseArray.Factory<N> arrayFactory, final Structure1D.IndexMapper<N> indexMapper) {
        return new MappedIndexSeries<>(arrayFactory, indexMapper, null);
    }

    static <N extends Comparable<N>> BasicSeries<N, N> make(final DenseArray.Factory<N> arrayFactory, final Structure1D.IndexMapper<N> indexMapper,
            final BinaryFunction<N> accumulator) {
        return new MappedIndexSeries<>(arrayFactory, indexMapper, accumulator);
    }

    default PrimitiveSeries asPrimitive() {

        double[] retVal = new double[this.size()];

        int i = 0;
        for (V tmpValue : this.values()) {
            retVal[i] = NumberDefinition.doubleValue(tmpValue);
            i++;
        }

        return DataSeries.wrap(retVal);
    }

    default BasicSeries<K, V> colour(final ColourData colour) {
        this.setColour(colour);
        return this;
    }

    /**
     * Will fill in missing values, inbetween the first and last keys.
     */
    default void complete(final UnaryOperator<K> keyIncrementor) {

        K tmpKey = this.firstKey();
        V tmpVal = null;

        V patchVal = this.firstValue();

        K lastKey = this.lastKey();
        while (tmpKey.compareTo(lastKey) <= 0) {

            tmpVal = this.get(tmpKey);

            if (tmpVal != null) {
                patchVal = tmpVal;
            } else {
                this.put(tmpKey, patchVal);
            }

            tmpKey = keyIncrementor.apply(tmpKey);
        }
    }

    default double doubleValue(final K key) {

        if (key == null) {
            return Double.NaN;
        }

        V value = this.get(key);

        if (value == null) {
            return Double.NaN;
        }

        return NumberDefinition.doubleValue(value);
    }

    default V firstValue() {
        return this.get(this.firstKey());
    }

    V get(K key);

    default V get(final Object key) {
        return this.get((K) key);
    }

    ColourData getColour();

    String getName();

    default V lastValue() {
        return this.get(this.lastKey());
    }

    default BasicSeries<K, V> name(final String name) {
        this.setName(name);
        return this;
    }

    default double put(final EntryPair.KeyedPrimitive<K> entry) {
        return this.put(entry.getKey(), entry.doubleValue());
    }

    /**
     * Will only work if values are types as Double.
     *
     * @see #put(Comparable, Number)
     */
    default double put(final K key, final double value) {
        Double tmpValue = Double.valueOf(value);
        V newValue = (V) tmpValue;
        V oldValue = this.put(key, newValue);
        if (oldValue != null) {
            return NumberDefinition.doubleValue(oldValue);
        } else {
            return Double.NaN;
        }
    }

    default void putAll(final Collection<? extends EntryPair<? extends K, ? extends V>> data) {
        for (EntryPair<? extends K, ? extends V> entry : data) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    default <K2 extends Comparable<? super K2>> void resample(final Function<K, K2> keyTranslator, final BasicSeries<K2, V> destination) {

        destination.setColour(this.getColour());
        destination.setName(this.getName());

        for (Map.Entry<K, V> entry : this.entrySet()) {
            K2 key = keyTranslator.apply(entry.getKey());
            V value = entry.getValue();
            destination.put(key, value);
        }
    }

    BasicSeries<K, V> resample(UnaryOperator<K> keyTranslator);

    void setColour(ColourData colour);

    void setName(String name);

}
