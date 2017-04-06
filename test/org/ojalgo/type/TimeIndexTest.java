package org.ojalgo.type;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;

import org.ojalgo.TestUtils;
import org.ojalgo.access.IndexMapper;

public class TimeIndexTest extends TypeTests {

    static <T extends Comparable<? super T>> void doTestPlain(final TimeIndex<T> mapperFactory, final T keyToTest) {

        final String implementation = keyToTest.getClass().getSimpleName();

        final IndexMapper<T> mapper = mapperFactory.plain();

        final T expected = keyToTest;
        final long index = mapper.toIndex(expected);
        final T actual = mapper.toKey(index);

        TestUtils.assertEquals(implementation, expected, actual);
    }

    static <T extends Comparable<? super T>> void doTestWithReference(final TimeIndex<T> mapperFactory, final T keyToTest, final T referenceKey) {

        final String implementation = keyToTest.getClass().getSimpleName();

        final IndexMapper<T> mapper = mapperFactory.from(referenceKey);

        final T expected = keyToTest;
        final long index = mapper.toIndex(expected);
        final T actual = mapper.toKey(index);

        TestUtils.assertEquals(implementation, expected, actual);
    }

    public TimeIndexTest() {
        super();
    }

    public TimeIndexTest(final String name) {
        super(name);
    }

    public void testPlain() {

        TimeIndexTest.doTestPlain(TimeIndex.CALENDAR, new GregorianCalendar());
        TimeIndexTest.doTestPlain(TimeIndex.CALENDAR_DATE, new CalendarDate());
        TimeIndexTest.doTestPlain(TimeIndex.DATE, new Date());

        // ms precision (strip micros and nanos)
        final Instant instant = Instant.ofEpochMilli(Instant.now().toEpochMilli());

        TimeIndexTest.doTestPlain(TimeIndex.INSTANT, instant);

        final LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        TimeIndexTest.doTestPlain(TimeIndex.LOCAL_DATE_TIME, localDateTime);
        TimeIndexTest.doTestPlain(TimeIndex.LOCAL_DATE, localDateTime.toLocalDate());
        TimeIndexTest.doTestPlain(TimeIndex.LOCAL_TIME, localDateTime.toLocalTime());

        final OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
        TimeIndexTest.doTestPlain(TimeIndex.OFFSET_DATE_TIME, offsetDateTime);

        final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        TimeIndexTest.doTestPlain(TimeIndex.ZONED_DATE_TIME, zonedDateTime);
    }

    public void testWithReference() {

        // ms precision (strip micros and nanos)
        final Instant instant = Instant.ofEpochMilli(Instant.now().toEpochMilli());
        final Instant reference = instant.minusMillis(TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR);

        TimeIndexTest.doTestWithReference(TimeIndex.CALENDAR, new GregorianCalendar(), null);
        TimeIndexTest.doTestWithReference(TimeIndex.CALENDAR_DATE, new CalendarDate(), null);
        TimeIndexTest.doTestWithReference(TimeIndex.DATE, new Date(), null);

        TimeIndexTest.doTestWithReference(TimeIndex.INSTANT, instant, reference);

        final LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        TimeIndexTest.doTestWithReference(TimeIndex.LOCAL_DATE_TIME, localDateTime, null);
        TimeIndexTest.doTestWithReference(TimeIndex.LOCAL_DATE, localDateTime.toLocalDate(), null);
        TimeIndexTest.doTestWithReference(TimeIndex.LOCAL_TIME, localDateTime.toLocalTime(), null);

        final OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
        TimeIndexTest.doTestWithReference(TimeIndex.OFFSET_DATE_TIME, offsetDateTime, null);

        final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        TimeIndexTest.doTestWithReference(TimeIndex.ZONED_DATE_TIME, zonedDateTime, null);
    }

}
