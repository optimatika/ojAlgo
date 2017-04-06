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

    static <T extends Comparable<? super T>> void doTestPlain(TimeIndex<T> mapperFactory, T keyToTest) {

        final String implementation = keyToTest.getClass().getSimpleName();

        IndexMapper<T> mapper = mapperFactory.plain();

        T expected = keyToTest;
        final long index = mapper.toIndex(expected);
        T actual = mapper.toKey(index);

        TestUtils.assertEquals(implementation, expected, actual);
    }

    public TimeIndexTest() {
        super();
    }

    public TimeIndexTest(String name) {
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

}
