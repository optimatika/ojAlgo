package org.ojalgo.type;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.ojalgo.access.IndexMapper;

public abstract class TimeIndex<T extends Comparable<T>> {

    public static final TimeIndex<Instant> INSTANT = new TimeIndex<Instant>() {

        @Override
        public IndexMapper<Instant> from(final Instant reference, final CalendarDateDuration resolution) {
            return new IndexMapper<Instant>() {

                public long toIndex(final Instant key) {
                    return reference.until(key, ChronoUnit.MILLIS) / resolution.toDurationInMillis();
                }

                public Instant toKey(final long index) {
                    return Instant.ofEpochMilli((index * resolution.toDurationInMillis()) + reference.toEpochMilli());
                }

            };
        }

        @Override
        public IndexMapper<Instant> plain() {
            return new IndexMapper<Instant>() {

                public long toIndex(final Instant key) {
                    return key.toEpochMilli();
                }

                public Instant toKey(final long index) {
                    return Instant.ofEpochMilli(index);
                }

            };
        }

    };

    public static final TimeIndex<CalendarDate> CALENDAR_DATE = new TimeIndex<CalendarDate>() {

        @Override
        public IndexMapper<CalendarDate> from(final CalendarDate reference, final CalendarDateDuration resolution) {
            return new IndexMapper<CalendarDate>() {

                public long toIndex(final CalendarDate key) {
                    return (key.millis - reference.millis) / resolution.toDurationInMillis();
                }

                public CalendarDate toKey(final long index) {
                    return new CalendarDate((index * resolution.toDurationInMillis()) + reference.millis);
                }

            };
        }

        @Override
        public IndexMapper<CalendarDate> plain() {
            return new IndexMapper<CalendarDate>() {

                public long toIndex(final CalendarDate key) {
                    return key.millis;
                }

                public CalendarDate toKey(final long index) {
                    return new CalendarDate(index);
                }

            };
        }

    };

    public static final TimeIndex<Long> LONG = new TimeIndex<Long>() {

        @Override
        public IndexMapper<Long> from(final Long reference, final CalendarDateDuration resolution) {
            return new IndexMapper<Long>() {

                public long toIndex(final Long key) {
                    return (key - reference) / resolution.toDurationInMillis();
                }

                public Long toKey(final long index) {
                    return (index * resolution.toDurationInMillis()) + reference;
                }

            };
        }

        @Override
        public IndexMapper<Long> plain() {
            return new IndexMapper<Long>() {

                public long toIndex(final Long key) {
                    return key;
                }

                public Long toKey(final long index) {
                    return index;
                }

            };
        }

    };

    public abstract IndexMapper<T> from(final T reference, final CalendarDateDuration resolution);

    public abstract IndexMapper<T> plain();

}
