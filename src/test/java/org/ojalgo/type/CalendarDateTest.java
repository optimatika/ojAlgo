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

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

/**
 * {@link CalendarDate} should agree with {@link Instant} (truncated to milliseconds), including before the
 * epoch.
 */
class CalendarDateTest {

    private static final long[] MILLIS = { 1234567890123L, 999L, 0L, -1L, -1234567890123L };
    private static final ZoneOffset OFFSET = ZoneOffset.ofHoursMinutes(-3, -30);

    @Test
    public void testAdjustInto() {
        OffsetDateTime reference = OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, OFFSET);
        for (long millis : MILLIS) {
            TestUtils.assertEquals(Instant.ofEpochMilli(millis).adjustInto(reference), new CalendarDate(millis).adjustInto(reference));
        }
    }

    @Test
    public void testCompareTo() {
        TestUtils.assertTrue(new CalendarDate(Long.MAX_VALUE).compareTo(new CalendarDate(-1L)) > 0);
        TestUtils.assertTrue(new CalendarDate(Long.MIN_VALUE).compareTo(new CalendarDate(1L)) < 0);
    }

    /**
     * As an adjuster a {@link CalendarDateDuration} maps {@link CalendarDate} and {@link Instant} to the
     * middle of a fixed-length interval (a zero length changes nothing), and rejects other temporals.
     */
    @Test
    public void testDurationAdjustInto() {

        CalendarDateDuration quarterOfAnHour = CalendarDateUnit.MINUTE.newDuration(15.0);

        for (long millis : MILLIS) {
            long expected = Math.floorDiv(millis, 900_000L) * 900_000L + 450_000L;
            TestUtils.assertEquals(new CalendarDate(expected), quarterOfAnHour.adjustInto(new CalendarDate(millis)));
            TestUtils.assertEquals(Instant.ofEpochMilli(expected), quarterOfAnHour.adjustInto(Instant.ofEpochMilli(millis).plusNanos(123_456L)));
        }

        CalendarDate date = new CalendarDate(MILLIS[0]);
        TestUtils.assertEquals(date, CalendarDateUnit.DAY.newDuration(0.0).adjustInto(date));

        TestUtils.assertThrows(UnsupportedTemporalTypeException.class, () -> quarterOfAnHour.adjustInto(LocalDateTime.of(2026, 10, 30, 13, 47)));
    }

    /**
     * {@link CalendarDateDuration#valueOf(Duration)} picks the largest whole unit, and converting back with
     * {@link CalendarDateDuration#toDuration()} gives the same length. Fractional measures and average unit
     * lengths convert too.
     */
    @Test
    public void testDurationConversion() {

        TestUtils.assertEquals(new CalendarDateDuration(15.0, CalendarDateUnit.MINUTE), CalendarDateDuration.valueOf(Duration.ofMinutes(15L)));
        TestUtils.assertEquals(new CalendarDateDuration(90.0, CalendarDateUnit.SECOND), CalendarDateDuration.valueOf(Duration.ofSeconds(90L)));
        TestUtils.assertEquals(new CalendarDateDuration(2.0, CalendarDateUnit.DAY), CalendarDateDuration.valueOf(Duration.ofDays(2L)));
        TestUtils.assertEquals(new CalendarDateDuration(-1_500.0, CalendarDateUnit.MILLIS), CalendarDateDuration.valueOf(Duration.ofMillis(-1_500L)));
        TestUtils.assertEquals(new CalendarDateDuration(123.0, CalendarDateUnit.NANOS), CalendarDateDuration.valueOf(Duration.ofNanos(123L)));

        for (Duration duration : new Duration[] { Duration.ZERO, Duration.ofNanos(1_234_567_891L), Duration.ofSeconds(-7L, 1L),
                Duration.ofDays(36_524_250L) }) {
            TestUtils.assertEquals(duration, CalendarDateDuration.valueOf(duration).toDuration());
        }

        TestUtils.assertEquals(Duration.ofMinutes(90L), CalendarDateUnit.HOUR.newDuration(1.5).toDuration());
        TestUtils.assertEquals(ChronoUnit.MONTHS.getDuration(), CalendarDateUnit.MONTH.newDuration(1.0).toDuration());
        TestUtils.assertEquals(ChronoUnit.MILLENNIA.getDuration(), CalendarDateUnit.MILLENIUM.newDuration(1.0).toDuration());
    }

    /**
     * As a {@link java.time.temporal.TemporalAmount} a {@link CalendarDateDuration} is a whole number of its
     * unit: {@code get} returns what {@code addTo} adds to other temporals, and other units are unsupported.
     */
    @Test
    public void testDurationGet() {

        CalendarDateDuration duration = CalendarDateUnit.HOUR.newDuration(1.6);
        LocalDateTime dateTime = LocalDateTime.of(2026, 3, 31, 8, 15);

        TestUtils.assertEquals(2L, duration.get(CalendarDateUnit.HOUR));
        TestUtils.assertEquals(dateTime.plusHours(2L), dateTime.plus(duration));
        TestUtils.assertEquals(Duration.ofHours(2L), Duration.from(duration));
        TestUtils.assertThrows(UnsupportedTemporalTypeException.class, () -> duration.get(ChronoUnit.HOURS));
    }

    /**
     * Subtracting a {@link CalendarDateDuration} from other temporals should match subtracting the
     * corresponding {@link ChronoUnit} amount (calendar months, not average month lengths). From a
     * {@link CalendarDate} it should exactly undo adding it.
     */
    @Test
    public void testDurationSubtractFrom() {

        LocalDateTime dateTime = LocalDateTime.of(2026, 3, 31, 8, 15);
        LocalDate date = dateTime.toLocalDate();

        for (CalendarDateUnit unit : CalendarDateUnit.values()) {
            CalendarDateDuration duration = unit.newDuration(2.0);
            unit.getChronoUnit().ifPresent(chrono -> {
                TestUtils.assertEquals(dateTime.minus(2L, chrono), dateTime.minus(duration));
                if (date.isSupported(chrono)) {
                    TestUtils.assertEquals(date.minus(2L, chrono), date.minus(duration));
                }
            });
        }
        TestUtils.assertEquals(dateTime.minus(6L, ChronoUnit.MONTHS), dateTime.minus(CalendarDateUnit.QUARTER.newDuration(2.0)));

        CalendarDate calendarDate = new CalendarDate(MILLIS[0]);
        for (CalendarDateDuration duration : new CalendarDateDuration[] { CalendarDateUnit.MONTH.newDuration(1.0), CalendarDateUnit.DAY.newDuration(1.6) }) {
            TestUtils.assertEquals(calendarDate, calendarDate.plus(duration).minus(duration));
        }
    }

    @Test
    public void testFields() {
        for (long millis : MILLIS) {
            Instant instant = Instant.ofEpochMilli(millis);
            CalendarDate date = new CalendarDate(millis);
            for (ChronoField field : new ChronoField[] { ChronoField.INSTANT_SECONDS, ChronoField.MILLI_OF_SECOND, ChronoField.MICRO_OF_SECOND,
                    ChronoField.NANO_OF_SECOND }) {
                TestUtils.assertTrue(date.isSupported(field));
                TestUtils.assertEquals(instant.getLong(field), date.getLong(field));
            }
            TestUtils.assertEquals(instant, Instant.from(date));
            TestUtils.assertEquals(ChronoUnit.MILLIS, date.query(TemporalQueries.precision()));
        }
    }

    /**
     * A millennium in nanoseconds does not fit in a long. {@link CalendarDateUnit#toDurationInNanos()}
     * saturates, but conversions and comparisons must still be correct.
     */
    @Test
    public void testMillenniumInNanos() {

        TestUtils.assertEquals(Long.MAX_VALUE, CalendarDateUnit.MILLENIUM.toDurationInNanos());

        TestUtils.assertEquals(1000.0, CalendarDateUnit.YEAR.convert(1.0, CalendarDateUnit.MILLENIUM));
        TestUtils.assertEquals(1.0, CalendarDateUnit.MILLENIUM.convert(1000.0, CalendarDateUnit.YEAR));

        TestUtils.assertTrue(CalendarDateUnit.MILLENIUM.newDuration(1.0).compareTo(CalendarDateUnit.YEAR.newDuration(1.0)) > 0);
        TestUtils.assertTrue(CalendarDateUnit.MILLENIUM.newDuration(2.0).compareTo(CalendarDateUnit.MILLENIUM.newDuration(3.0)) < 0);
        TestUtils.assertEquals(0, CalendarDateUnit.MILLENIUM.newDuration(0.5).compareTo(CalendarDateUnit.CENTURY.newDuration(5.0)));

        TestUtils.assertEquals(7_889_238_000_000_000_000L, CalendarDateUnit.MILLENIUM.newDuration(0.25).toDurationInNanos());
    }

    @Test
    public void testOffsetDateTimePreservesMilliseconds() {
        for (long millis : MILLIS) {
            Instant instant = Instant.ofEpochMilli(millis);
            TestUtils.assertEquals(millis, CalendarDate.valueOf(instant.atOffset(ZoneOffset.ofHours(9))).millis);
            TestUtils.assertEquals(millis, CalendarDate.valueOf(instant.plusNanos(999_999L).atOffset(OFFSET)).millis);
        }
    }

    @Test
    public void testPlus() {

        long millis = MILLIS[0];
        CalendarDate date = new CalendarDate(millis);

        TestUtils.assertEquals(new CalendarDate(millis + 3_000_000_000L), date.plus(3_000_000_000L, CalendarDateUnit.MILLIS));
        TestUtils.assertEquals(new CalendarDate(millis + 5L * 3_600_000L), CalendarDateUnit.HOUR.addTo(date, 5L));
        TestUtils.assertEquals(new CalendarDate(millis + 7L), date.plus(7L, ChronoUnit.MILLIS));
        TestUtils.assertEquals(new CalendarDate(millis + 30L * 86_400_000L), date.plus(Duration.ofDays(30L)));
        TestUtils.assertEquals(new CalendarDate(millis - 30L * 86_400_000L), date.minus(Duration.ofDays(30L)));
    }

    /**
     * Like MONTH and YEAR, QUARTER adjusts other temporals to noon on the last day of the period.
     */
    @Test
    public void testQuarterAdjustInto() {

        LocalDate[] quarterEnds = { LocalDate.of(2026, 3, 31), LocalDate.of(2026, 6, 30), LocalDate.of(2026, 9, 30), LocalDate.of(2026, 12, 31) };

        for (int month = 1; month <= 12; month++) {
            LocalDateTime expected = quarterEnds[(month - 1) / 3].atTime(12, 0);
            LocalDate first = LocalDate.of(2026, month, 1);
            TestUtils.assertEquals(expected, CalendarDateUnit.QUARTER.adjustInto(first.atTime(8, 15)));
            TestUtils.assertEquals(expected, CalendarDateUnit.QUARTER.adjustInto(first.withDayOfMonth(first.lengthOfMonth()).atTime(23, 59)));
        }
    }

    @Test
    public void testResolutionBeforeEpoch() {

        long sixInTheMorning = -18L * 3_600_000L; // 1969-12-31T06:00Z
        long noon = -12L * 3_600_000L; // 1969-12-31T12:00Z

        TestUtils.assertEquals(noon, CalendarDateUnit.DAY.adjustInto(sixInTheMorning));
        TestUtils.assertEquals(noon, CalendarDateUnit.DAY.newDuration(1.0).adjustInto(sixInTheMorning));
        TestUtils.assertEquals(new CalendarDate(noon), new CalendarDate(sixInTheMorning).filter(CalendarDateUnit.DAY));

        TestUtils.assertEquals(-1L, CalendarDateUnit.DAY.count(0L, -1L));
    }

    /**
     * {@link CalendarDate.Resolution#of(java.time.temporal.TemporalUnit)} gives the matching
     * {@link CalendarDateUnit} when there is one, and {@link CalendarDate.Resolution#of(Duration)} gives
     * intervals of any length.
     */
    @Test
    public void testResolutionOf() {

        TestUtils.assertEquals(CalendarDateUnit.HOUR, CalendarDate.Resolution.of(ChronoUnit.HOURS));
        TestUtils.assertEquals(CalendarDateUnit.WEEK, CalendarDate.Resolution.of(CalendarDateUnit.WEEK));
        TestUtils.assertEquals(CalendarDateUnit.QUARTER, CalendarDate.Resolution.of(IsoFields.QUARTER_YEARS));
        TestUtils.assertEquals(new CalendarDateDuration(12.0, CalendarDateUnit.HOUR), CalendarDate.Resolution.of(ChronoUnit.HALF_DAYS));

        CalendarDate.Resolution quarterOfAnHour = CalendarDate.Resolution.of(Duration.ofMinutes(15L));
        for (long millis : MILLIS) {
            TestUtils.assertEquals(Math.floorDiv(millis, 900_000L) * 900_000L + 450_000L, quarterOfAnHour.adjustInto(millis));
        }
    }

    /**
     * {@link CalendarDate.Resolution} works on plain epoch-millisecond longs, agrees with the
     * {@link CalendarDate} methods, and truncates like
     * {@link Instant#truncatedTo(java.time.temporal.TemporalUnit)}. From 23:59Z to 00:01Z crosses one day
     * boundary, but is not a complete day.
     */
    @Test
    public void testResolutionOnLongs() {

        CalendarDate.Resolution quarterOfAnHour = CalendarDate.Resolution.of(Duration.ofMinutes(15L));

        for (long millis : MILLIS) {

            long start = Math.floorDiv(millis, 900_000L) * 900_000L;
            TestUtils.assertEquals(start, quarterOfAnHour.truncate(millis));
            TestUtils.assertEquals(start + 450_000L, quarterOfAnHour.adjustInto(millis));
            TestUtils.assertEquals(millis + 3L * 900_000L, quarterOfAnHour.addTo(millis, 3L));
            TestUtils.assertEquals(-3L, quarterOfAnHour.between(millis, millis - 3L * 900_000L - 1L));

            TestUtils.assertEquals(Instant.ofEpochMilli(millis).truncatedTo(ChronoUnit.HOURS).toEpochMilli(), CalendarDateUnit.HOUR.truncate(millis));

            CalendarDate date = new CalendarDate(millis);
            CalendarDate other = new CalendarDate(millis - 123_456_789L);
            for (CalendarDateUnit unit : CalendarDateUnit.values()) {
                TestUtils.assertEquals(date.plus(5L, unit).millis, unit.addTo(millis, 5L));
                TestUtils.assertEquals(date.until(other, unit), unit.between(millis, other.millis));
            }
        }

        TestUtils.assertEquals(1L, CalendarDateUnit.DAY.count(-60_000L, 60_000L));
        TestUtils.assertEquals(0L, CalendarDateUnit.DAY.between(-60_000L, 60_000L));
        TestUtils.assertEquals(-1L, CalendarDateUnit.DAY.count(60_000L, -60_000L));
    }

    /**
     * NANOS and MICROS are shorter than the millisecond resolution of {@link CalendarDate}. Adding them
     * should match the corresponding {@link ChronoUnit} (that is {@link Instant} truncated to milliseconds).
     */
    @Test
    public void testSubMillisecondUnits() {

        for (long millis : MILLIS) {
            CalendarDate date = new CalendarDate(millis);
            for (long amount : new long[] { 1L, 999L, 1_000L, 1_500_000L, -1L, -1_500_000L }) {
                TestUtils.assertEquals(date.plus(amount, ChronoUnit.NANOS), date.plus(amount, CalendarDateUnit.NANOS));
                TestUtils.assertEquals(date.plus(amount, ChronoUnit.MICROS), date.plus(amount, CalendarDateUnit.MICROS));
            }
        }

        CalendarDate date = new CalendarDate(MILLIS[0]);
        CalendarDate later = new CalendarDate(MILLIS[0] + 7L);

        TestUtils.assertEquals(date.until(later, ChronoUnit.NANOS), date.until(later, CalendarDateUnit.NANOS));
        TestUtils.assertEquals(date.until(later, ChronoUnit.MICROS), date.until(later, CalendarDateUnit.MICROS));

        TestUtils.assertEquals(date, CalendarDateUnit.NANOS.adjustInto(date));
        TestUtils.assertEquals(date, CalendarDateUnit.MICROS.newDuration(500.0).adjustInto(date));

        TestUtils.assertEquals(2L, new CalendarDateDuration(2_000_000.0, CalendarDateUnit.NANOS).toDurationInMillis());
        TestUtils.assertEquals(2L, new CalendarDateDuration(2_000.0, CalendarDateUnit.MICROS).toDurationInMillis());
    }

    @Test
    public void testToJavaTime() {
        for (long millis : MILLIS) {
            Instant instant = Instant.ofEpochMilli(millis);
            CalendarDate date = new CalendarDate(millis);
            TestUtils.assertEquals(instant.atOffset(OFFSET), date.toOffsetDateTime(OFFSET));
            TestUtils.assertEquals(instant.atZone(OFFSET), date.toZonedDateTime(OFFSET));
            TestUtils.assertEquals(instant.atOffset(OFFSET).toLocalDateTime(), date.toLocalDateTime(OFFSET));
            TestUtils.assertEquals(instant.atOffset(OFFSET).toLocalTime(), date.toLocalTime(OFFSET));
            TestUtils.assertEquals(instant.atOffset(OFFSET).toLocalDate(), date.toLocalDate(OFFSET));
            TestUtils.assertEquals(millis, CalendarDate.valueOf(date.toOffsetDateTime(OFFSET)).millis);
        }
    }

    /**
     * {@link CalendarDateUnit#adjustInto(java.time.temporal.Temporal)}: other temporals go to the start of
     * units shorter than a day, and to noon on the last day of longer periods (also before year 0).
     * {@link LocalDate} only has its date adjusted, and {@link Instant} is handled as {@link CalendarDate}.
     * The reference date, 2026-10-30, is a Friday.
     */
    @Test
    public void testUnitAdjustInto() {

        LocalDateTime dateTime = LocalDateTime.of(2026, 10, 30, 13, 47, 12, 345_678_901);
        LocalDate date = dateTime.toLocalDate();

        TestUtils.assertEquals(dateTime, CalendarDateUnit.MILLIS.adjustInto(dateTime));
        TestUtils.assertEquals(LocalDateTime.of(2026, 10, 30, 13, 47, 12), CalendarDateUnit.SECOND.adjustInto(dateTime));
        TestUtils.assertEquals(LocalDateTime.of(2026, 10, 30, 13, 47), CalendarDateUnit.MINUTE.adjustInto(dateTime));
        TestUtils.assertEquals(LocalDateTime.of(2026, 10, 30, 13, 0), CalendarDateUnit.HOUR.adjustInto(dateTime));
        TestUtils.assertEquals(LocalDateTime.of(2026, 10, 30, 12, 0), CalendarDateUnit.DAY.adjustInto(dateTime));
        TestUtils.assertEquals(LocalDateTime.of(2026, 10, 30, 12, 0), CalendarDateUnit.WEEK.adjustInto(dateTime));
        TestUtils.assertEquals(LocalDateTime.of(2026, 10, 31, 12, 0), CalendarDateUnit.MONTH.adjustInto(dateTime));
        TestUtils.assertEquals(LocalDateTime.of(2026, 12, 31, 12, 0), CalendarDateUnit.YEAR.adjustInto(dateTime));
        TestUtils.assertEquals(LocalDateTime.of(2029, 12, 31, 12, 0), CalendarDateUnit.DECADE.adjustInto(dateTime));
        TestUtils.assertEquals(LocalDateTime.of(2099, 12, 31, 12, 0), CalendarDateUnit.CENTURY.adjustInto(dateTime));
        TestUtils.assertEquals(LocalDateTime.of(2999, 12, 31, 12, 0), CalendarDateUnit.MILLENIUM.adjustInto(dateTime));

        TestUtils.assertEquals(date, CalendarDateUnit.DAY.adjustInto(date));
        TestUtils.assertEquals(LocalDate.of(2026, 10, 31), CalendarDateUnit.MONTH.adjustInto(date));
        TestUtils.assertEquals(LocalDate.of(-1, 12, 31), CalendarDateUnit.DECADE.adjustInto(LocalDate.of(-5, 6, 15)));
        TestUtils.assertEquals(LocalDate.of(-11, 12, 31), CalendarDateUnit.DECADE.adjustInto(LocalDate.of(-15, 6, 15)));

        for (long millis : MILLIS) {
            Instant instant = Instant.ofEpochMilli(millis).plusNanos(123_456L);
            for (CalendarDateUnit unit : CalendarDateUnit.values()) {
                TestUtils.assertEquals(unit.adjustInto(new CalendarDate(millis)).toInstant(), unit.adjustInto(instant));
            }
        }
    }

    /**
     * {@link CalendarDate#until(java.time.temporal.Temporal, java.time.temporal.TemporalUnit)} counts
     * complete units, and agrees with
     * {@link CalendarDateUnit#between(java.time.temporal.Temporal, java.time.temporal.Temporal)} as well as
     * with the corresponding {@link ChronoUnit} (when supported). Unlike
     * {@link CalendarDateUnit#count(long, long)} it does not count resolution boundaries crossed.
     */
    @Test
    public void testUntil() {

        long millis = MILLIS[0];
        CalendarDate date = new CalendarDate(millis);
        TestUtils.assertEquals(-2L, date.until(new CalendarDate(millis - 2L * 3_600_000L), ChronoUnit.HOURS));

        long hour = 3_600_000L;
        CalendarDate halfPastEleven = new CalendarDate(11L * hour + hour / 2L);
        CalendarDate oneMinuteShortOfTwoHoursLater = new CalendarDate(13L * hour + hour / 2L - 60_000L);
        TestUtils.assertEquals(1L, halfPastEleven.until(oneMinuteShortOfTwoHoursLater, CalendarDateUnit.HOUR));
        TestUtils.assertEquals(-1L, oneMinuteShortOfTwoHoursLater.until(halfPastEleven, CalendarDateUnit.HOUR));
        TestUtils.assertEquals(0L, new CalendarDate(0L).until(new CalendarDate(-1L), CalendarDateUnit.DAY));

        CalendarDate other = new CalendarDate(millis - 123_456_789_012L);
        for (CalendarDateUnit unit : CalendarDateUnit.values()) {
            TestUtils.assertEquals(unit.between(date, other), date.until(other, unit));
            unit.getChronoUnit().filter(date::isSupported).ifPresent(chrono -> TestUtils.assertEquals(date.until(other, chrono), date.until(other, unit)));
        }

        for (CalendarDateUnit unit : new CalendarDateUnit[] { CalendarDateUnit.MILLIS, CalendarDateUnit.HOUR, CalendarDateUnit.WEEK, CalendarDateUnit.MONTH,
                CalendarDateUnit.QUARTER, CalendarDateUnit.MILLENIUM }) {
            TestUtils.assertEquals(3L, date.until(date.plus(3L, unit), unit));
            TestUtils.assertEquals(-3L, date.until(date.minus(3L, unit), unit));
        }
    }

    @Test
    public void testWith() {

        for (long millis : MILLIS) {
            Instant instant = Instant.ofEpochMilli(millis);
            CalendarDate date = new CalendarDate(millis);
            TestUtils.assertEquals(instant.with(ChronoField.INSTANT_SECONDS, 5L).toEpochMilli(), date.with(ChronoField.INSTANT_SECONDS, 5L).millis);
            TestUtils.assertEquals(instant.with(ChronoField.MILLI_OF_SECOND, 7L).toEpochMilli(), date.with(ChronoField.MILLI_OF_SECOND, 7L).millis);
            TestUtils.assertEquals(instant.with(ChronoField.MICRO_OF_SECOND, 7_654L).toEpochMilli(), date.with(ChronoField.MICRO_OF_SECOND, 7_654L).millis);
            TestUtils.assertEquals(instant.with(ChronoField.NANO_OF_SECOND, 7_654_321L).toEpochMilli(),
                    date.with(ChronoField.NANO_OF_SECOND, 7_654_321L).millis);
        }

        TestUtils.assertThrows(DateTimeException.class, () -> new CalendarDate(0L).with(ChronoField.MILLI_OF_SECOND, 1_000L));
    }

    @Test
    public void testZonedDateTimePreservesMilliseconds() {
        for (long millis : MILLIS) {
            Instant instant = Instant.ofEpochMilli(millis);
            TestUtils.assertEquals(millis, CalendarDate.valueOf(instant.atZone(ZoneId.of("Europe/Paris"))).millis);
            TestUtils.assertEquals(millis, CalendarDate.valueOf(instant.plusNanos(999_999L).atZone(OFFSET)).millis);
        }
    }

}
