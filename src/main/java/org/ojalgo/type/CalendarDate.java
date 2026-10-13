/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.type;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;

import java.time.*;
import java.time.temporal.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.ojalgo.ProgrammingError;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.structure.Structure1D.IndexMapper;

/**
 * An immutable instant on the time-line, stored as a single long: {@link #millis}, the number of milliseconds
 * since 1970-01-01T00:00Z. It corresponds to an {@link Instant} truncated to milliseconds, and converts to
 * and from {@link Date}, {@link Calendar} and the java.time types.
 * <p>
 * As a {@link Temporal} it supports:
 * <ul>
 * <li>the fields {@link ChronoField#INSTANT_SECONDS}, {@link ChronoField#MILLI_OF_SECOND},
 * {@link ChronoField#MICRO_OF_SECOND} and {@link ChronoField#NANO_OF_SECOND}
 * <li>the time-based {@link ChronoUnit}s and {@link ChronoUnit#DAYS}
 * <li>all {@link CalendarDateUnit}s, each with a fixed length
 * </ul>
 * Anything finer than a millisecond, such as an added number of nanoseconds, is rounded down to whole
 * milliseconds.
 * <p>
 * A {@link Resolution}, such as a {@link CalendarDateUnit} or a {@link CalendarDateDuration}, divides the
 * time-line into fixed-length intervals, see {@link #filter(CalendarDateUnit)} and
 * {@link #make(long, CalendarDate.Resolution)}. Its operations also work directly on epoch-millisecond longs,
 * without creating any objects.
 *
 * @see CalendarDateDuration
 * @see CalendarDateUnit
 * @author apete
 */
public final class CalendarDate implements Temporal, Comparable<CalendarDate> {

    /**
     * A time-line resolution: a fixed length that divides the time-line into intervals aligned with
     * 1970-01-01T00:00Z, each represented by its middle instant.
     * <p>
     * All operations are available on plain epoch-millisecond longs, without creating any objects:
     * {@link #truncate(long)}, {@link #adjustInto(long)}, {@link #addTo(long, long)},
     * {@link #between(long, long)} and {@link #count(long, long)}.
     * <p>
     * As an {@link IndexMapper} the index of a {@link CalendarDate} is the {@link #millis} of that middle
     * instant. Implemented by {@link CalendarDateUnit} and {@link CalendarDateDuration}, and obtained from a
     * {@link Duration} or a {@link TemporalUnit} with {@link #of(Duration)} and {@link #of(TemporalUnit)}.
     *
     * @author apete
     */
    public interface Resolution extends TemporalAdjuster, IndexMapper<CalendarDate> {

        /**
         * Intervals of the duration's length, see {@link CalendarDateDuration#valueOf(Duration)}.
         */
        static Resolution of(final Duration duration) {
            return CalendarDateDuration.valueOf(duration);
        }

        /**
         * The {@link CalendarDateUnit} corresponding to the unit, a {@link ChronoUnit} or
         * {@link IsoFields#QUARTER_YEARS}. Any other unit gives intervals of its
         * {@link TemporalUnit#getDuration()}.
         */
        static Resolution of(final TemporalUnit unit) {
            if (unit instanceof CalendarDateUnit) {
                return (CalendarDateUnit) unit;
            } else if (unit == IsoFields.QUARTER_YEARS) {
                return CalendarDateUnit.QUARTER;
            } else {
                for (CalendarDateUnit candidate : CalendarDateUnit.values()) {
                    if (candidate.getChronoUnit().orElse(null) == unit) {
                        return candidate;
                    }
                }
                return CalendarDateDuration.valueOf(unit.getDuration());
            }
        }

        /**
         * Same as {@link #addTo(long, long)} with an amount of 1.
         */
        default long addTo(final long epochMilli) {
            return this.addTo(epochMilli, 1L);
        }

        /**
         * Returns epochMilli plus amount lengths. A length shorter than a millisecond is added in
         * nanoseconds, and the result rounded down to whole milliseconds.
         */
        default long addTo(final long epochMilli, final long amount) {
            long duration = this.toDurationInMillis();
            if (duration == 0L) {
                return epochMilli + Math.floorDiv(amount * this.toDurationInNanos(), NANOS_PER_MILLIS);
            } else {
                return epochMilli + amount * duration;
            }
        }

        /**
         * The middle instant of the interval that contains the temporal, see {@link #adjustInto(long)}.
         */
        default CalendarDate adjustInto(final CalendarDate temporal) {
            return new CalendarDate(this.adjustInto(temporal.millis));
        }

        /**
         * Maps epochMilli to the middle of the interval, of length d = {@link #toDurationInMillis()} and
         * aligned with the epoch, that contains it: {@code truncate(epochMilli) + d / 2}. If d is 0,
         * epochMilli is returned unchanged.
         */
        default long adjustInto(final long epochMilli) {
            return this.truncate(epochMilli) + this.toDurationInMillis() / 2L;
        }

        /**
         * The number of complete lengths from one epoch millisecond value to the other, negative if
         * toEpochMilli is before fromEpochMilli.
         */
        default long between(final long fromEpochMilli, final long toEpochMilli) {
            long duration = this.toDurationInMillis();
            if (duration == 0L) {
                return (toEpochMilli - fromEpochMilli) * NANOS_PER_MILLIS / this.toDurationInNanos();
            } else {
                return (toEpochMilli - fromEpochMilli) / duration;
            }
        }

        /**
         * The number of interval boundaries, see {@link #truncate(long)}, crossed going from one epoch
         * millisecond value to the other. Negative if toEpochMilli is before fromEpochMilli. Unlike
         * {@link #between(long, long)} this counts boundaries, not complete lengths: from 23:59Z to 00:01Z is
         * one {@link CalendarDateUnit#DAY}. For lengths shorter than a millisecond it is the same as
         * {@link #between(long, long)}.
         */
        default long count(final long fromEpochMilli, final long toEpochMilli) {
            long duration = this.toDurationInMillis();
            if (duration == 0L) {
                return this.between(fromEpochMilli, toEpochMilli);
            } else {
                return Math.floorDiv(toEpochMilli, duration) - Math.floorDiv(fromEpochMilli, duration);
            }
        }

        /**
         * The length in whole milliseconds. It is 0 for {@link CalendarDateUnit#NANOS} and
         * {@link CalendarDateUnit#MICROS}.
         */
        long toDurationInMillis();

        /**
         * The length in nanoseconds, saturated at {@link Long#MAX_VALUE} (about 292 years).
         */
        default long toDurationInNanos() {
            return TimeUnit.MILLISECONDS.toNanos(this.toDurationInMillis());
        }

        @Override
        default long toIndex(final CalendarDate key) {
            return this.adjustInto(key.millis);
        }

        @Override
        default CalendarDate toKey(final long index) {
            return new CalendarDate(index);
        }

        /**
         * The start of the interval, of length d = {@link #toDurationInMillis()} and aligned with the epoch,
         * that contains epochMilli: {@code floorDiv(epochMilli, d) * d}. If d is 0, epochMilli is returned
         * unchanged.
         */
        default long truncate(final long epochMilli) {
            long duration = this.toDurationInMillis();
            if (duration == 0L) {
                return epochMilli;
            } else {
                return Math.floorDiv(epochMilli, duration) * duration;
            }
        }

    }

    static final int MICROS_PER_MILLIS = 1_000;
    static final long MILLIS_PER_SECOND = 1_000L;
    static final int NANOS_PER_MILLIS = 1_000_000;
    static final int NANOS_PER_SECOND = 1_000_000_000;
    static final long SECONDS_PER_DAY = 24L * 60L * 60L;

    /**
     * Obtains an instance from a temporal that supports {@link ChronoField#INSTANT_SECONDS} and
     * {@link ChronoField#MILLI_OF_SECOND}. Anything finer than a millisecond is dropped.
     *
     * @throws DateTimeException If the temporal does not support those fields
     */
    public static CalendarDate from(final TemporalAccessor temporal) {
        ProgrammingError.throwIfNull(temporal, "temporal");
        if (temporal instanceof CalendarDate) {
            return (CalendarDate) temporal;
        } else if (temporal instanceof Instant) {
            return new CalendarDate(((Instant) temporal).toEpochMilli());
        } else {
            try {
                long tmpSeconds = temporal.getLong(ChronoField.INSTANT_SECONDS);
                int tmpMillisOfSecond = temporal.get(ChronoField.MILLI_OF_SECOND);
                return new CalendarDate(tmpSeconds * MILLIS_PER_SECOND + tmpMillisOfSecond);
            } catch (DateTimeException ex) {
                throw new DateTimeException("Unable to obtain CalendarDate from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName(),
                        ex);
            }
        }
    }

    /**
     * The middle instant of the resolution interval that contains the given instant, or now if none is given.
     * See {@link CalendarDate.Resolution#adjustInto(long)}.
     */
    public static CalendarDate make(final Calendar calendar, final CalendarDate.Resolution resolution) {
        return new CalendarDate(resolution.adjustInto(calendar.getTimeInMillis()));
    }

    public static CalendarDate make(final CalendarDate.Resolution resolution) {
        return new CalendarDate(resolution.adjustInto(System.currentTimeMillis()));
    }

    public static CalendarDate make(final Date date, final CalendarDate.Resolution resolution) {
        return new CalendarDate(resolution.adjustInto(date.getTime()));
    }

    public static CalendarDate make(final long timeInMIllis, final CalendarDate.Resolution resolution) {
        return new CalendarDate(resolution.adjustInto(timeInMIllis));
    }

    public static CalendarDate now() {
        return new CalendarDate();
    }

    public static Calendar toCalendar(final Instant instant) {
        GregorianCalendar retVal = new GregorianCalendar();
        retVal.setTimeInMillis(instant.toEpochMilli());
        return retVal;
    }

    public static Calendar toCalendar(final Instant instant, final Locale locale) {
        GregorianCalendar retVal = new GregorianCalendar(locale);
        retVal.setTimeInMillis(instant.toEpochMilli());
        return retVal;
    }

    public static Calendar toCalendar(final Instant instant, final TimeZone zone) {
        GregorianCalendar retVal = new GregorianCalendar(zone);
        retVal.setTimeInMillis(instant.toEpochMilli());
        return retVal;
    }

    public static Calendar toCalendar(final Instant instant, final TimeZone zone, final Locale locale) {
        GregorianCalendar retVal = new GregorianCalendar(zone, locale);
        retVal.setTimeInMillis(instant.toEpochMilli());
        return retVal;
    }

    public static Date toDate(final Instant instant) {
        return new Date(instant.toEpochMilli());
    }

    public static LocalDate toLocalDate(final Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = 1 + calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return LocalDate.of(year, month, day);
    }

    public static LocalDate toLocalDate(final Instant instant, final ZoneId zone) {
        return CalendarDate.toLocalDateTime(instant, zone).toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(final Instant instant, final ZoneId zone) {
        return LocalDateTime.ofInstant(instant, zone);
    }

    public static LocalTime toLocalTime(final Instant instant, final ZoneId zone) {
        return CalendarDate.toLocalDateTime(instant, zone).toLocalTime();
    }

    public static OffsetDateTime toOffsetDateTime(final Instant instant, final ZoneId zone) {
        return OffsetDateTime.ofInstant(instant, zone);
    }

    /**
     * The instant at the offset the zone has at zoneToOffsetConversionInstant, rather than at the instant
     * itself.
     */
    public static OffsetDateTime toOffsetDateTime(final Instant instant, final ZoneId zone, final Instant zoneToOffsetConversionInstant) {
        return OffsetDateTime.ofInstant(instant, zone).withOffsetSameInstant(OffsetDateTime.ofInstant(zoneToOffsetConversionInstant, zone).getOffset());
    }

    public static OffsetDateTime toOffsetDateTime(final Instant instant, final ZoneOffset offset) {
        return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()).withOffsetSameInstant(offset);
    }

    public static ZonedDateTime toZonedDateTime(final Instant instant, final ZoneId zone) {
        return ZonedDateTime.ofInstant(instant, zone);
    }

    public static CalendarDate valueOf(final Instant instant) {
        return new CalendarDate(instant.toEpochMilli());
    }

    public static CalendarDate valueOf(final OffsetDateTime offsetDateTime) {
        return new CalendarDate(offsetDateTime.toEpochSecond() * MILLIS_PER_SECOND + offsetDateTime.getNano() / NANOS_PER_MILLIS);
    }

    public static CalendarDate valueOf(final ZonedDateTime zonedDateTime) {
        return new CalendarDate(zonedDateTime.toEpochSecond() * MILLIS_PER_SECOND + zonedDateTime.getNano() / NANOS_PER_MILLIS);
    }

    static long millis(final TemporalAccessor temporal) {
        if (temporal instanceof CalendarDate) {
            return ((CalendarDate) temporal).millis;
        } else if (temporal instanceof Instant) {
            return ((Instant) temporal).toEpochMilli();
        } else {
            try {
                long tmpSeconds = temporal.getLong(ChronoField.INSTANT_SECONDS);
                int tmpMillisOfSecond = temporal.get(ChronoField.MILLI_OF_SECOND);
                return tmpSeconds * MILLIS_PER_SECOND + tmpMillisOfSecond;
            } catch (DateTimeException ex) {
                throw new DateTimeException("No millis!");
            }
        }
    }

    /**
     * The number of milliseconds since 1970-01-01T00:00Z, as returned by {@link Instant#toEpochMilli()} and
     * {@link System#currentTimeMillis()}.
     */
    public final long millis;

    public CalendarDate() {

        super();

        millis = System.currentTimeMillis();
    }

    public CalendarDate(final Calendar calendar) {

        super();

        millis = calendar.getTimeInMillis();
    }

    public CalendarDate(final Date date) {

        super();

        millis = date.getTime();
    }

    public CalendarDate(final long timeInMillis) {

        super();

        millis = timeInMillis;
    }

    /**
     * Parses an SQL date "yyyy-MM-dd", time "HH:mm:ss" or date-time "yyyy-MM-dd HH:mm:ss", in the default
     * time zone. A time without a date is on 1970-01-01.
     *
     * @param sqlString The String to parse
     * @throws RecoverableCondition If parsing the String fails
     */
    public CalendarDate(final String sqlString) throws RecoverableCondition {

        super();

        boolean tmpDatePart = sqlString.indexOf('-') >= 0;
        boolean tmpTimePart = sqlString.indexOf(':') >= 0;

        if (tmpDatePart && tmpTimePart) {
            millis = StandardType.SQL_DATETIME.parse(sqlString).getTime();
        } else if (tmpDatePart && !tmpTimePart) {
            millis = StandardType.SQL_DATE.parse(sqlString).getTime();
        } else if (!tmpDatePart && tmpTimePart) {
            millis = StandardType.SQL_TIME.parse(sqlString).getTime();
        } else {
            millis = Long.MIN_VALUE;
            throw RecoverableCondition.newFailedToParseString(sqlString, CalendarDate.class);
        }
    }

    /**
     * A new {@link GregorianCalendar}, in the default time zone and locale, set to this instant. The argument
     * is not used.
     */
    public Calendar adjustInto(final Calendar temporal) {
        GregorianCalendar retVal = new GregorianCalendar();
        retVal.setTimeInMillis(millis);
        return retVal;
    }

    /**
     * A new {@link Date} of this instant. The argument is not used.
     */
    public Date adjustInto(final Date temporal) {
        return new Date(millis);
    }

    /**
     * Sets the temporal's {@link ChronoField#INSTANT_SECONDS} and {@link ChronoField#NANO_OF_SECOND} to this
     * instant, as {@link Instant#adjustInto(Temporal)} does.
     */
    @SuppressWarnings("unchecked")
    public <T extends Temporal> T adjustInto(final T temporal) {
        if (temporal instanceof CalendarDate) {
            return (T) this;
        } else {
            long seconds = Math.floorDiv(millis, MILLIS_PER_SECOND);
            long nanos = Math.floorMod(millis, MILLIS_PER_SECOND) * NANOS_PER_MILLIS;
            return (T) temporal.with(INSTANT_SECONDS, seconds).with(NANO_OF_SECOND, nanos);
        }
    }

    @Override
    public int compareTo(final CalendarDate ref) {
        return Long.compare(millis, ref.millis);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof CalendarDate other) || (millis != other.millis)) {
            return false;
        }
        return true;
    }

    /**
     * The middle instant of the resolution interval that contains this instant, see
     * {@link CalendarDate.Resolution#adjustInto(long)}.
     */
    public CalendarDate filter(final CalendarDateUnit resolution) {
        return resolution.adjustInto(this);
    }

    @Override
    public long getLong(final TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == ChronoField.INSTANT_SECONDS) {
                return Math.floorDiv(millis, MILLIS_PER_SECOND);
            } else if (field == ChronoField.MILLI_OF_SECOND) {
                return Math.floorMod(millis, MILLIS_PER_SECOND);
            } else if (field == ChronoField.MICRO_OF_SECOND) {
                return Math.floorMod(millis, MILLIS_PER_SECOND) * MICROS_PER_MILLIS;
            } else if (field == ChronoField.NANO_OF_SECOND) {
                return Math.floorMod(millis, MILLIS_PER_SECOND) * NANOS_PER_MILLIS;
            } else {
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            }
        } else {
            return field.getFrom(this);
        }
    }

    @Override
    public int hashCode() {
        return (int) (millis ^ millis >>> 32);
    }

    @Override
    public boolean isSupported(final TemporalField field) {
        if (field instanceof ChronoField) {
            return field == ChronoField.INSTANT_SECONDS || field == ChronoField.MILLI_OF_SECOND || field == ChronoField.MICRO_OF_SECOND
                    || field == ChronoField.NANO_OF_SECOND;
        } else {
            return field.isSupportedBy(this);
        }
    }

    @Override
    public boolean isSupported(final TemporalUnit unit) {
        if (unit instanceof CalendarDateUnit) {
            return true;
        } else if (unit instanceof ChronoUnit) {
            return unit.isTimeBased() || unit == ChronoUnit.DAYS;
        } else if (unit != null) {
            return unit.isSupportedBy(this);
        } else {
            return false;
        }
    }

    @Override
    public CalendarDate plus(final long amountToAdd, final TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return CalendarDate.valueOf(this.toInstant().plus(amountToAdd, unit));
        } else {
            return unit.addTo(this, amountToAdd);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R query(final TemporalQuery<R> query) {
        if (query == TemporalQueries.precision()) {
            return (R) ChronoUnit.MILLIS;
        } else {
            return Temporal.super.query(query);
        }
    }

    /**
     * Same as {@link #step(int, CalendarDateUnit)} with the duration's measure truncated to an int. Use
     * {@link #plus(TemporalAmount)} to add a fractional duration.
     */
    public CalendarDate step(final CalendarDateDuration aStepDuration) {
        return this.step((int) aStepDuration.measure, aStepDuration.unit);
    }

    /**
     * Same as {@link #step(int, CalendarDateUnit)} with a step count of 1.
     */
    public CalendarDate step(final CalendarDateUnit aStepUnit) {
        return this.step(1, aStepUnit);
    }

    /**
     * Same as {@link #plus(long, TemporalUnit)}.
     */
    public CalendarDate step(final int aStepCount, final CalendarDateUnit aStepUnit) {
        return aStepUnit.addTo(this, aStepCount);
    }

    public Calendar toCalendar() {
        GregorianCalendar retVal = new GregorianCalendar();
        retVal.setTimeInMillis(millis);
        return retVal;
    }

    public Calendar toCalendar(final Locale locale) {
        GregorianCalendar retVal = new GregorianCalendar(locale);
        retVal.setTimeInMillis(millis);
        return retVal;
    }

    public Calendar toCalendar(final TimeZone zone) {
        GregorianCalendar retVal = new GregorianCalendar(zone);
        retVal.setTimeInMillis(millis);
        return retVal;
    }

    public Calendar toCalendar(final TimeZone zone, final Locale locale) {
        GregorianCalendar retVal = new GregorianCalendar(zone, locale);
        retVal.setTimeInMillis(millis);
        return retVal;
    }

    public Date toDate() {
        return new Date(millis);
    }

    public Instant toInstant() {
        return Instant.ofEpochMilli(millis);
    }

    public LocalDate toLocalDate(final ZoneOffset offset) {
        long tmpSeconds = Math.floorDiv(millis, MILLIS_PER_SECOND);
        long tmpLocalSeconds = tmpSeconds + offset.getTotalSeconds();
        long tmpLocalDay = Math.floorDiv(tmpLocalSeconds, CalendarDate.SECONDS_PER_DAY);
        return LocalDate.ofEpochDay(tmpLocalDay);
    }

    public LocalDateTime toLocalDateTime(final ZoneOffset offset) {
        long tmpSeconds = Math.floorDiv(millis, MILLIS_PER_SECOND);
        int tmpNanos = (int) Math.floorMod(millis, MILLIS_PER_SECOND) * NANOS_PER_MILLIS;
        return LocalDateTime.ofEpochSecond(tmpSeconds, tmpNanos, offset);
    }

    public LocalTime toLocalTime(final ZoneOffset offset) {
        long tmpSeconds = Math.floorDiv(millis, MILLIS_PER_SECOND);
        int tmpNanos = (int) Math.floorMod(millis, MILLIS_PER_SECOND) * NANOS_PER_MILLIS;
        long tmpLocalSeconds = tmpSeconds + offset.getTotalSeconds();
        long tmpSecondOfDay = Math.floorMod(tmpLocalSeconds, CalendarDate.SECONDS_PER_DAY);
        long tmpNanoOfDay = tmpSecondOfDay * CalendarDate.NANOS_PER_SECOND + tmpNanos;
        return LocalTime.ofNanoOfDay(tmpNanoOfDay);
    }

    public OffsetDateTime toOffsetDateTime(final ZoneOffset offset) {
        return OffsetDateTime.of(this.toLocalDateTime(offset), offset);
    }

    /**
     * Formatted as "yyyy-MM-dd HH:mm:ss" in the default time zone.
     */
    @Override
    public String toString() {
        return StandardType.SQL_DATETIME.format(this.toDate());
    }

    public ZonedDateTime toZonedDateTime(final ZoneOffset offset) {
        return ZonedDateTime.of(this.toLocalDateTime(offset), offset);
    }

    @Override
    public long until(final Temporal endExclusive, final TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return this.toInstant().until(endExclusive, unit);
        } else {
            return unit.between(this, CalendarDate.from(endExclusive));
        }
    }

    @Override
    public CalendarDate with(final TemporalAdjuster adjuster) {
        return (CalendarDate) Temporal.super.with(adjuster);
    }

    @Override
    public CalendarDate with(final TemporalField field, final long newValue) {
        if (field instanceof ChronoField) {
            if (field == ChronoField.INSTANT_SECONDS) {
                long tmpMillisOfSecond = Math.floorMod(millis, MILLIS_PER_SECOND);
                return new CalendarDate(newValue * MILLIS_PER_SECOND + tmpMillisOfSecond);
            } else if (field == ChronoField.MILLI_OF_SECOND) {
                ChronoField.MILLI_OF_SECOND.checkValidValue(newValue);
                long tmpSeconds = Math.floorDiv(millis, MILLIS_PER_SECOND);
                return new CalendarDate(tmpSeconds * MILLIS_PER_SECOND + newValue);
            } else if (field == ChronoField.MICRO_OF_SECOND) {
                ChronoField.MICRO_OF_SECOND.checkValidValue(newValue);
                long tmpSeconds = Math.floorDiv(millis, MILLIS_PER_SECOND);
                return new CalendarDate(tmpSeconds * MILLIS_PER_SECOND + newValue / MICROS_PER_MILLIS);
            } else if (field == ChronoField.NANO_OF_SECOND) {
                ChronoField.NANO_OF_SECOND.checkValidValue(newValue);
                long tmpSeconds = Math.floorDiv(millis, MILLIS_PER_SECOND);
                return new CalendarDate(tmpSeconds * MILLIS_PER_SECOND + newValue / NANOS_PER_MILLIS);
            } else {
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            }
        } else {
            return field.adjustInto(this, newValue);
        }
    }

}
