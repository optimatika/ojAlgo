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
package org.ojalgo.type;

import static java.time.temporal.ChronoField.*;

import java.time.*;
import java.time.temporal.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.ojalgo.ProgrammingError;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.structure.Structure1D.IndexMapper;

/**
 * <p>
 * Originally, long before Java 8 and its new Date and Time API, this class was designed to provide an
 * immutable complement to the existing {@linkplain Date} and {@linkplain Calendar} classes and to have
 * easy/direct access to the underlying epoch millisecond value.
 * </p>
 * <p>
 * In terms of the newer API it most closely corresponds to an {@linkplain Instant}, but does not have its
 * nanosecond granularity. At one point the plan was to remove and replace this class with
 * {@linkplain Instant}, but working with a single long as an "instant" representation is very practical and
 * efficient.
 * </p>
 * <p>
 * It has been retrofitted to implement the {@linkplain Temporal} interface.
 * </p>
 *
 * @see CalendarDateDuration
 * @see CalendarDateUnit
 * @author apete
 */
public final class CalendarDate implements Temporal, Comparable<CalendarDate> {

    /**
     * Extends {@link TemporalAdjuster} but also loosely corresponds to a {@link TemporalUnit} and/or
     * {@link TemporalAmount}.
     *
     * @author apete
     */
    public interface Resolution extends TemporalAdjuster, IndexMapper<CalendarDate> {

        /**
         * Will increment the input epochMilli by the size/duration of this timeline resolution.
         */
        default long addTo(final long epochMilli) {
            return epochMilli + this.toDurationInMillis();
        }

        /**
         * Maps a range of instances in time to a single instance.
         */
        default long adjustInto(final long epochMilli) {
            long duration = this.toDurationInMillis();
            long half = duration / 2L;
            return ((epochMilli / duration) * duration) + half;
        }

        /**
         * The size/duration of a timeline resolution "unit".
         */
        long toDurationInMillis();

        default long toDurationInNanos() {
            return this.toDurationInMillis() * NANOS_PER_MILLIS;
        }

        default long toIndex(final CalendarDate key) {
            return this.adjustInto(key.millis);
        }

        default CalendarDate toKey(final long index) {
            return new CalendarDate(index);
        }

        default CalendarDate adjustInto(final CalendarDate temporal) {
            return new CalendarDate(this.adjustInto(temporal.millis));
        }

    }

    static final long MILLIS_PER_SECOND = 1_000L;
    static final int NANOS_PER_MILLIS = 1_000_000;
    static final int NANOS_PER_SECOND = 1_000_000_000;
    static final long SECONDS_PER_DAY = 24L * 60L * 60L;

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
                return new CalendarDate((tmpSeconds * MILLIS_PER_SECOND) + tmpMillisOfSecond);
            } catch (DateTimeException ex) {
                throw new DateTimeException("Unable to obtain CalendarDate from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName(),
                        ex);
            }
        }
    }

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
        return new CalendarDate(offsetDateTime.toEpochSecond() * MILLIS_PER_SECOND);
    }

    public static CalendarDate valueOf(final ZonedDateTime zonedDateTime) {
        return new CalendarDate(zonedDateTime.toEpochSecond() * MILLIS_PER_SECOND);
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
                return (tmpSeconds * MILLIS_PER_SECOND) + tmpMillisOfSecond;
            } catch (DateTimeException ex) {
                throw new DateTimeException("No millis!");
            }
        }
    }

    /**
     * An "instant" with ms precision. Equivalent to what is returned by {@link Instant#toEpochMilli()} and/or
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
     * @param sqlString The String to parse
     * @throws RecoverableCondition When(if parsing the String fails
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

    public Calendar adjustInto(final Calendar temporal) {
        GregorianCalendar retVal = new GregorianCalendar();
        retVal.setTimeInMillis(millis);
        return retVal;
    }

    public Date adjustInto(final Date temporal) {
        return new Date(millis);
    }

    @SuppressWarnings("unchecked")
    public <T extends Temporal> T adjustInto(final T temporal) {
        if (temporal instanceof CalendarDate) {
            return (T) this;
        } else {
            long seconds = millis / MILLIS_PER_SECOND;
            long nanos = (millis % MILLIS_PER_SECOND) * (NANOS_PER_SECOND / MILLIS_PER_SECOND);
            return (T) temporal.with(INSTANT_SECONDS, seconds).with(NANO_OF_SECOND, nanos);
        }
    }

    public int compareTo(final CalendarDate ref) {
        return Long.signum(millis - ref.millis);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof CalendarDate)) {
            return false;
        }
        CalendarDate other = (CalendarDate) obj;
        if (millis != other.millis) {
            return false;
        }
        return true;
    }

    public CalendarDate filter(final CalendarDateUnit resolution) {
        if (resolution.isCalendarUnit()) {
            return resolution.adjustInto(this);
        } else {
            return new CalendarDate(resolution.adjustInto(millis));
        }
    }

    public long getLong(final TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == ChronoField.INSTANT_SECONDS) {
                return millis / MILLIS_PER_SECOND;
            } else {
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            }
        } else {
            return field.getFrom(this);
        }
    }

    @Override
    public int hashCode() {
        return (int) (millis ^ (millis >>> 32));
    }

    public boolean isSupported(final TemporalField field) {
        if (field instanceof ChronoField) {
            return (field == ChronoField.INSTANT_SECONDS) || (field == ChronoField.MILLI_OF_SECOND);
        } else {
            return field.isSupportedBy(this);
        }
    }

    public boolean isSupported(final TemporalUnit unit) {
        if (unit instanceof CalendarDateUnit) {
            return true;
        } else if (unit instanceof ChronoUnit) {
            return unit.isTimeBased() || (unit == ChronoUnit.DAYS);
        } else if (unit != null) {
            return unit.isSupportedBy(this);
        } else {
            return false;
        }
    }

    public Temporal plus(final long amountToAdd, final TemporalUnit unit) {
        if (unit instanceof CalendarDateUnit) {
            return this.step((int) amountToAdd, (CalendarDateUnit) unit);
        } else if (unit instanceof ChronoUnit) {
            return this.toInstant().plus(amountToAdd, unit);
        } else {
            return unit.addTo(this, amountToAdd);
        }
    }

    /**
     * Only steps with the int part of {@linkplain CalendarDateDuration#measure} .
     */
    public CalendarDate step(final CalendarDateDuration aStepDuration) {
        return this.step((int) aStepDuration.measure, aStepDuration.unit);
    }

    public CalendarDate step(final CalendarDateUnit aStepUnit) {
        return this.step(1, aStepUnit);
    }

    public CalendarDate step(final int aStepCount, final CalendarDateUnit aStepUnit) {
        return new CalendarDate(millis + (aStepCount * aStepUnit.toDurationInMillis()));
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
        int tmpNanos = (int) Math.floorMod(millis, MILLIS_PER_SECOND);
        return LocalDateTime.ofEpochSecond(tmpSeconds, tmpNanos, offset);
    }

    public LocalTime toLocalTime(final ZoneOffset offset) {
        long tmpSeconds = Math.floorDiv(millis, MILLIS_PER_SECOND);
        int tmpNanos = (int) Math.floorMod(millis, MILLIS_PER_SECOND);
        long tmpLocalSeconds = tmpSeconds + offset.getTotalSeconds();
        int tmpSecondOfDay = (int) Math.floorMod(tmpLocalSeconds, CalendarDate.SECONDS_PER_DAY);
        int tmpNanoOfDay = (tmpSecondOfDay * CalendarDate.NANOS_PER_SECOND) + tmpNanos;
        return LocalTime.ofNanoOfDay(tmpNanoOfDay);
    }

    public OffsetDateTime toOffsetDateTime(final ZoneOffset offset) {
        return OffsetDateTime.of(this.toLocalDateTime(offset), offset);
    }

    @Override
    public String toString() {
        return StandardType.SQL_DATETIME.format(this.toDate());
    }

    public ZonedDateTime toZonedDateTime(final ZoneOffset offset) {
        return ZonedDateTime.of(this.toLocalDateTime(offset), offset);
    }

    public long until(final Temporal endExclusive, final TemporalUnit unit) {
        if (unit instanceof CalendarDateUnit) {
            return ((CalendarDateUnit) unit).count(millis, CalendarDate.millis(endExclusive));
        } else if (unit instanceof ChronoUnit) {
            return this.toInstant().until(endExclusive, unit);
        } else {
            return unit.between(this, endExclusive);
        }
    }

    public CalendarDate with(final TemporalAdjuster adjuster) {
        return (CalendarDate) Temporal.super.with(adjuster);
    }

    public CalendarDate with(final TemporalField field, final long newValue) {
        if (field instanceof ChronoField) {
            if (field == ChronoField.INSTANT_SECONDS) {
                long tmpMillisOfSecond = millis % MILLIS_PER_SECOND;
                return new CalendarDate((newValue * MILLIS_PER_SECOND) + tmpMillisOfSecond);
            } else if (field == ChronoField.MILLI_OF_SECOND) {
                long tmpSeconds = millis / MILLIS_PER_SECOND;
                return new CalendarDate((tmpSeconds * MILLIS_PER_SECOND) + newValue);
            } else {
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            }
        } else {
            return field.adjustInto(this, newValue);
        }
    }

}
