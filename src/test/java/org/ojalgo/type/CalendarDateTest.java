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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

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

    @Test
    public void testFields() {
        for (long millis : MILLIS) {
            Instant instant = Instant.ofEpochMilli(millis);
            CalendarDate date = new CalendarDate(millis);
            for (ChronoField field : new ChronoField[] { ChronoField.INSTANT_SECONDS, ChronoField.MILLI_OF_SECOND, ChronoField.NANO_OF_SECOND }) {
                TestUtils.assertTrue(date.isSupported(field));
                TestUtils.assertEquals(instant.getLong(field), date.getLong(field));
            }
            TestUtils.assertEquals(instant, Instant.from(date));
        }
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

    @Test
    public void testResolutionBeforeEpoch() {

        long sixInTheMorning = -18L * 3_600_000L; // 1969-12-31T06:00Z
        long noon = -12L * 3_600_000L; // 1969-12-31T12:00Z

        TestUtils.assertEquals(noon, CalendarDateUnit.DAY.adjustInto(sixInTheMorning));
        TestUtils.assertEquals(noon, CalendarDateUnit.DAY.newDuration(1.0).adjustInto(sixInTheMorning));
        TestUtils.assertEquals(new CalendarDate(noon), new CalendarDate(sixInTheMorning).filter(CalendarDateUnit.DAY));

        TestUtils.assertEquals(-1L, CalendarDateUnit.DAY.count(0L, -1L));
        TestUtils.assertEquals(-1L, new CalendarDate(0L).until(new CalendarDate(-1L), CalendarDateUnit.DAY));
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

    @Test
    public void testUntil() {
        long millis = MILLIS[0];
        CalendarDate date = new CalendarDate(millis);
        TestUtils.assertEquals(-2L, date.until(new CalendarDate(millis - 2L * 3_600_000L), ChronoUnit.HOURS));
    }

    @Test
    public void testWith() {

        for (long millis : MILLIS) {
            Instant instant = Instant.ofEpochMilli(millis);
            CalendarDate date = new CalendarDate(millis);
            TestUtils.assertEquals(instant.with(ChronoField.INSTANT_SECONDS, 5L).toEpochMilli(), date.with(ChronoField.INSTANT_SECONDS, 5L).millis);
            TestUtils.assertEquals(instant.with(ChronoField.MILLI_OF_SECOND, 7L).toEpochMilli(), date.with(ChronoField.MILLI_OF_SECOND, 7L).millis);
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
