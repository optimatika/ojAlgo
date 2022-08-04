package org.ojalgo.type;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.ojalgo.structure.Structure1D.IndexMapper;
import org.ojalgo.type.CalendarDate.Resolution;

public abstract class TimeIndex<T extends Comparable<? super T>> {

    public static final TimeIndex<Calendar> CALENDAR = new TimeIndex<>() {

        @Override
        public IndexMapper<Calendar> from(final Calendar reference) {
            return new IndexMapper<>() {

                public long toIndex(final Calendar key) {
                    return key.getTimeInMillis() - reference.getTimeInMillis();
                }

                public Calendar toKey(final long index) {
                    final long tmpTimeInMillis = index + reference.getTimeInMillis();
                    final GregorianCalendar retVal = new GregorianCalendar();
                    retVal.setTimeInMillis(tmpTimeInMillis);
                    return retVal;
                }

            };
        }

        @Override
        public IndexMapper<Calendar> from(final Calendar reference, final Resolution resolution) {
            return new IndexMapper<>() {

                public long toIndex(final Calendar key) {
                    return (key.getTimeInMillis() - reference.getTimeInMillis()) / resolution.toDurationInMillis();
                }

                public Calendar toKey(final long index) {
                    final long tmpTimeInMillis = (index * resolution.toDurationInMillis()) + reference.getTimeInMillis();
                    final GregorianCalendar retVal = new GregorianCalendar();
                    retVal.setTimeInMillis(tmpTimeInMillis);
                    return retVal;
                }

            };
        }

        @Override
        public IndexMapper<Calendar> plain() {
            return new IndexMapper<>() {

                public long toIndex(final Calendar key) {
                    return key.getTimeInMillis();
                }

                public Calendar toKey(final long index) {
                    final GregorianCalendar retVal = new GregorianCalendar();
                    retVal.setTimeInMillis(index);
                    return retVal;
                }

            };
        }

        @Override
        public IndexMapper<Calendar> plain(final Resolution resolution) {
            return new IndexMapper<>() {

                public long toIndex(final Calendar key) {
                    return key.getTimeInMillis() / resolution.toDurationInMillis();
                }

                public Calendar toKey(final long index) {
                    final long tmpTimeInMillis = index * resolution.toDurationInMillis();
                    final GregorianCalendar retVal = new GregorianCalendar();
                    retVal.setTimeInMillis(tmpTimeInMillis);
                    return retVal;
                }

            };
        }

    };

    public static final TimeIndex<CalendarDate> CALENDAR_DATE = new TimeIndex<>() {

        @Override
        public IndexMapper<CalendarDate> from(final CalendarDate reference) {
            return new IndexMapper<>() {

                public long toIndex(final CalendarDate key) {
                    return key.millis - reference.millis;
                }

                public CalendarDate toKey(final long index) {
                    return new CalendarDate(index + reference.millis);
                }

            };
        }

        @Override
        public IndexMapper<CalendarDate> from(final CalendarDate reference, final Resolution resolution) {
            return new IndexMapper<>() {

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
            return new IndexMapper<>() {

                public long toIndex(final CalendarDate key) {
                    return key.millis;
                }

                public CalendarDate toKey(final long index) {
                    return new CalendarDate(index);
                }

            };
        }

        @Override
        public IndexMapper<CalendarDate> plain(final Resolution resolution) {
            return new IndexMapper<>() {

                public long toIndex(final CalendarDate key) {
                    return key.millis / resolution.toDurationInMillis();
                }

                public CalendarDate toKey(final long index) {
                    return new CalendarDate(index * resolution.toDurationInMillis());
                }

            };
        }

    };

    public static final TimeIndex<Date> DATE = new TimeIndex<>() {

        @Override
        public IndexMapper<Date> from(final Date reference) {
            return new IndexMapper<>() {

                public long toIndex(final Date key) {
                    return key.getTime() - reference.getTime();
                }

                public Date toKey(final long index) {
                    return new Date(index + reference.getTime());
                }

            };
        }

        @Override
        public IndexMapper<Date> from(final Date reference, final Resolution resolution) {
            return new IndexMapper<>() {

                public long toIndex(final Date key) {
                    return (key.getTime() - reference.getTime()) / resolution.toDurationInMillis();
                }

                public Date toKey(final long index) {
                    return new Date((index * resolution.toDurationInMillis()) + reference.getTime());
                }

            };
        }

        @Override
        public IndexMapper<Date> plain() {
            return new IndexMapper<>() {

                public long toIndex(final Date key) {
                    return key.getTime();
                }

                public Date toKey(final long index) {
                    return new Date(index);
                }

            };
        }

        @Override
        public IndexMapper<Date> plain(final Resolution resolution) {
            return new IndexMapper<>() {

                public long toIndex(final Date key) {
                    return key.getTime() / resolution.toDurationInMillis();
                }

                public Date toKey(final long index) {
                    return new Date(index * resolution.toDurationInMillis());
                }

            };
        }

    };

    public static final TimeIndex<Instant> INSTANT = new TimeIndex<>() {

        @Override
        public IndexMapper<Instant> from(final Instant reference) {
            return new IndexMapper<>() {

                public long toIndex(final Instant key) {
                    return reference.until(key, ChronoUnit.NANOS);
                }

                public Instant toKey(final long index) {
                    return reference.plus(index, ChronoUnit.NANOS);
                }

            };
        }

        @Override
        public IndexMapper<Instant> from(final Instant reference, final Resolution resolution) {
            return new IndexMapper<>() {

                private final long myResolution = resolution.toDurationInNanos();

                public long toIndex(final Instant key) {
                    return reference.until(key, ChronoUnit.NANOS) / myResolution;
                }

                public Instant toKey(final long index) {
                    return reference.plus(index * myResolution, ChronoUnit.NANOS);
                }

            };
        }

        @Override
        public IndexMapper<Instant> plain() {
            return new IndexMapper<>() {

                public long toIndex(final Instant key) {
                    return key.toEpochMilli();
                }

                public Instant toKey(final long index) {
                    return Instant.ofEpochMilli(index);
                }

            };
        }

        @Override
        public IndexMapper<Instant> plain(final Resolution resolution) {
            return new IndexMapper<>() {

                private final long myResolution = resolution.toDurationInMillis();

                public long toIndex(final Instant key) {
                    return key.toEpochMilli() / myResolution;
                }

                public Instant toKey(final long index) {
                    return Instant.ofEpochMilli(index * myResolution);
                }

            };
        }

    };

    public static final TimeIndex<LocalDate> LOCAL_DATE = new TimeIndex<>() {

        @Override
        public IndexMapper<LocalDate> from(final LocalDate reference) {
            return new IndexMapper<>() {

                private final long myReference = reference.toEpochDay();

                public long toIndex(final LocalDate key) {
                    return key.toEpochDay() - myReference;
                }

                public LocalDate toKey(final long index) {
                    return LocalDate.ofEpochDay(myReference + index);
                }

            };
        }

        @Override
        public IndexMapper<LocalDate> from(final LocalDate reference, final Resolution resolution) {
            return new IndexMapper<>() {

                private final long myReference = reference.toEpochDay() * DAY_SIZE;
                private final long myResolution = resolution.toDurationInMillis();

                public long toIndex(final LocalDate key) {
                    return ((DAY_SIZE * key.toEpochDay()) - myReference) / myResolution;
                }

                public LocalDate toKey(final long index) {
                    return LocalDate.ofEpochDay((myReference + (index * myResolution)) / DAY_SIZE);
                }

            };
        }

        @Override
        public IndexMapper<LocalDate> plain() {
            return new IndexMapper<>() {

                public long toIndex(final LocalDate key) {
                    return key.toEpochDay();
                }

                public LocalDate toKey(final long index) {
                    return LocalDate.ofEpochDay(index);
                }

            };
        }

        @Override
        public IndexMapper<LocalDate> plain(final Resolution resolution) {
            return new IndexMapper<>() {

                private final long myResolution = resolution.toDurationInMillis();

                public long toIndex(final LocalDate key) {
                    return (DAY_SIZE * key.toEpochDay()) / myResolution;
                }

                public LocalDate toKey(final long index) {
                    return LocalDate.ofEpochDay((index * myResolution) / DAY_SIZE);
                }

            };
        }

    };

    public static final TimeIndex<LocalDateTime> LOCAL_DATE_TIME = new TimeIndex<>() {

        @Override
        public IndexMapper<LocalDateTime> from(final LocalDateTime reference) {
            return new IndexMapper<>() {

                private final long myReference = reference.toInstant(ZoneOffset.UTC).toEpochMilli();

                public long toIndex(final LocalDateTime key) {
                    return key.toInstant(ZoneOffset.UTC).toEpochMilli() - myReference;
                }

                public LocalDateTime toKey(final long index) {
                    return LocalDateTime.ofInstant(Instant.ofEpochMilli(myReference + index), ZoneOffset.UTC);
                }

            };
        }

        @Override
        public IndexMapper<LocalDateTime> from(final LocalDateTime reference, final Resolution resolution) {
            return new IndexMapper<>() {

                private final long myReference = reference.toEpochSecond(ZoneOffset.UTC);
                private final long myResolution = resolution.toDurationInMillis();

                public long toIndex(final LocalDateTime key) {
                    return (key.toEpochSecond(ZoneOffset.UTC) - myReference) / myResolution;
                }

                public LocalDateTime toKey(final long index) {
                    return LocalDateTime.ofEpochSecond(myReference + (index * myResolution), 0, ZoneOffset.UTC);
                }

            };
        }

        @Override
        public IndexMapper<LocalDateTime> plain() {
            return new IndexMapper<>() {

                public long toIndex(final LocalDateTime key) {
                    return key.toInstant(ZoneOffset.UTC).toEpochMilli();
                }

                public LocalDateTime toKey(final long index) {
                    return LocalDateTime.ofInstant(Instant.ofEpochMilli(index), ZoneOffset.UTC);
                }

            };
        }

        @Override
        public IndexMapper<LocalDateTime> plain(final Resolution resolution) {
            return new IndexMapper<>() {

                private final long myResolution = resolution.toDurationInMillis();

                public long toIndex(final LocalDateTime key) {
                    return key.toEpochSecond(ZoneOffset.UTC) / myResolution;
                }

                public LocalDateTime toKey(final long index) {
                    return LocalDateTime.ofEpochSecond(index * myResolution, 0, ZoneOffset.UTC);
                }

            };
        }

    };

    public static final TimeIndex<LocalTime> LOCAL_TIME = new TimeIndex<>() {

        @Override
        public IndexMapper<LocalTime> from(final LocalTime reference) {
            return new IndexMapper<>() {

                final long myReference = reference.toNanoOfDay();

                public long toIndex(final LocalTime key) {
                    return key.toNanoOfDay() - myReference;
                }

                public LocalTime toKey(final long index) {
                    return LocalTime.ofNanoOfDay(myReference + index);
                }

            };
        }

        @Override
        public IndexMapper<LocalTime> from(final LocalTime reference, final Resolution resolution) {
            return new IndexMapper<>() {

                final long myReference = reference.toNanoOfDay();
                final long myResolution = resolution.toDurationInNanos();

                public long toIndex(final LocalTime key) {
                    return (key.toNanoOfDay() - myReference) / myResolution;
                }

                public LocalTime toKey(final long index) {
                    return LocalTime.ofNanoOfDay(myReference + (index * myResolution));
                }

            };
        }

        @Override
        public IndexMapper<LocalTime> plain() {
            return new IndexMapper<>() {

                public long toIndex(final LocalTime key) {
                    return key.toNanoOfDay();
                }

                public LocalTime toKey(final long index) {
                    return LocalTime.ofNanoOfDay(index);
                }

            };
        }

        @Override
        public IndexMapper<LocalTime> plain(final Resolution resolution) {
            return new IndexMapper<>() {

                final long myResolution = resolution.toDurationInNanos();

                public long toIndex(final LocalTime key) {
                    return key.toNanoOfDay() / myResolution;
                }

                public LocalTime toKey(final long index) {
                    return LocalTime.ofNanoOfDay(index * myResolution);
                }

            };
        }

    };

    public static final TimeIndex<OffsetDateTime> OFFSET_DATE_TIME = new TimeIndex<>() {

        @Override
        public IndexMapper<OffsetDateTime> from(final OffsetDateTime reference) {
            return new IndexMapper<>() {

                private final IndexMapper<Instant> myDelegate = INSTANT.from(reference.toInstant());
                private final ZoneOffset myOffset = reference.getOffset();

                public long toIndex(final OffsetDateTime key) {
                    return myDelegate.toIndex(key.toInstant());
                }

                public OffsetDateTime toKey(final long index) {
                    final Instant tmpInstant = myDelegate.toKey(index);
                    if (myOffset != null) {
                        return OffsetDateTime.ofInstant(tmpInstant, myOffset);
                    } else {
                        return OffsetDateTime.ofInstant(tmpInstant, ZoneOffset.UTC);
                    }
                }
            };
        }

        @Override
        public IndexMapper<OffsetDateTime> from(final OffsetDateTime reference, final Resolution resolution) {
            return new IndexMapper<>() {

                private final IndexMapper<Instant> myDelegate = INSTANT.from(reference.toInstant(), resolution);
                private final ZoneOffset myOffset = reference.getOffset();

                public long toIndex(final OffsetDateTime key) {
                    return myDelegate.toIndex(key.toInstant());
                }

                public OffsetDateTime toKey(final long index) {
                    final Instant tmpInstant = myDelegate.toKey(index);
                    if (myOffset != null) {
                        return OffsetDateTime.ofInstant(tmpInstant, myOffset);
                    } else {
                        return OffsetDateTime.ofInstant(tmpInstant, ZoneOffset.UTC);
                    }
                }
            };
        }

        @Override
        public IndexMapper<OffsetDateTime> plain() {
            return new IndexMapper<>() {

                private final IndexMapper<Instant> myDelegate = INSTANT.plain();
                private transient ZoneOffset myOffset = null;

                public long toIndex(final OffsetDateTime key) {
                    myOffset = key.getOffset();
                    return myDelegate.toIndex(key.toInstant());
                }

                public OffsetDateTime toKey(final long index) {
                    final Instant tmpInstant = myDelegate.toKey(index);
                    if (myOffset != null) {
                        return OffsetDateTime.ofInstant(tmpInstant, myOffset);
                    } else {
                        return OffsetDateTime.ofInstant(tmpInstant, ZoneOffset.UTC);
                    }
                }
            };
        }

        @Override
        public IndexMapper<OffsetDateTime> plain(final Resolution resolution) {
            return new IndexMapper<>() {

                private final IndexMapper<Instant> myDelegate = INSTANT.plain(resolution);

                public long toIndex(final OffsetDateTime key) {
                    return myDelegate.toIndex(key.toInstant());
                }

                public OffsetDateTime toKey(final long index) {
                    final Instant tmpInstant = myDelegate.toKey(index);

                    return OffsetDateTime.ofInstant(tmpInstant, ZoneOffset.UTC);

                }
            };
        }

    };

    public static final TimeIndex<ZonedDateTime> ZONED_DATE_TIME = new TimeIndex<>() {

        @Override
        public IndexMapper<ZonedDateTime> from(final ZonedDateTime reference) {
            return new IndexMapper<>() {

                private final IndexMapper<Instant> myDelegate = INSTANT.from(reference.toInstant());
                private final ZoneId myZone = reference.getZone();

                public long toIndex(final ZonedDateTime key) {
                    return myDelegate.toIndex(key.toInstant());
                }

                public ZonedDateTime toKey(final long index) {
                    final Instant tmpInstant = myDelegate.toKey(index);
                    if (myZone != null) {
                        return ZonedDateTime.ofInstant(tmpInstant, myZone);
                    } else {
                        return ZonedDateTime.ofInstant(tmpInstant, ZoneOffset.UTC);
                    }
                }
            };
        }

        @Override
        public IndexMapper<ZonedDateTime> from(final ZonedDateTime reference, final Resolution resolution) {
            return new IndexMapper<>() {

                private final IndexMapper<Instant> myDelegate = INSTANT.from(reference.toInstant(), resolution);
                private final ZoneId myZone = reference.getZone();

                public long toIndex(final ZonedDateTime key) {
                    return myDelegate.toIndex(key.toInstant());
                }

                public ZonedDateTime toKey(final long index) {
                    final Instant tmpInstant = myDelegate.toKey(index);
                    if (myZone != null) {
                        return ZonedDateTime.ofInstant(tmpInstant, myZone);
                    } else {
                        return ZonedDateTime.ofInstant(tmpInstant, ZoneOffset.UTC);
                    }
                }
            };
        }

        @Override
        public IndexMapper<ZonedDateTime> plain() {
            return new IndexMapper<>() {

                private final IndexMapper<Instant> myDelegate = INSTANT.plain();
                private transient ZoneId myZone = null;

                public long toIndex(final ZonedDateTime key) {
                    myZone = key.getZone();
                    return myDelegate.toIndex(key.toInstant());
                }

                public ZonedDateTime toKey(final long index) {
                    final Instant tmpInstant = myDelegate.toKey(index);
                    if (myZone != null) {
                        return ZonedDateTime.ofInstant(tmpInstant, myZone);
                    } else {
                        return ZonedDateTime.ofInstant(tmpInstant, ZoneOffset.UTC);
                    }
                }
            };
        }

        @Override
        public IndexMapper<ZonedDateTime> plain(final Resolution resolution) {
            return new IndexMapper<>() {

                private final IndexMapper<Instant> myDelegate = INSTANT.plain(resolution);

                public long toIndex(final ZonedDateTime key) {
                    return myDelegate.toIndex(key.toInstant());
                }

                public ZonedDateTime toKey(final long index) {
                    final Instant tmpInstant = myDelegate.toKey(index);
                    return ZonedDateTime.ofInstant(tmpInstant, ZoneOffset.UTC);
                }
            };
        }

    };

    static final long DAY_SIZE = CalendarDateUnit.DAY.toDurationInMillis();

    public abstract IndexMapper<T> from(T reference);

    public abstract IndexMapper<T> from(T reference, Resolution resolution);

    public abstract IndexMapper<T> plain();

    public abstract IndexMapper<T> plain(Resolution resolution);

}
