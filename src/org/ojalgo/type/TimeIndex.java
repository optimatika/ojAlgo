package org.ojalgo.type;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.ToLongFunction;

public abstract class TimeIndex<T> implements ToLongFunction<T> {

    public static ToLongFunction<CalendarDate> from(final CalendarDate reference, final CalendarDateDuration resolution) {
        return value -> (value.millis - reference.millis) / resolution.toDurationInMillis();
    }

    public static ToLongFunction<Instant> from(final Instant reference, final CalendarDateDuration resolution) {
        return value -> reference.until(value, ChronoUnit.MILLIS) / resolution.toDurationInMillis();
    }

    public static ToLongFunction<LocalDate> from(final LocalDate reference, final CalendarDateDuration resolution) {
        return value -> (reference.until(value, CalendarDateUnit.MILLIS)) / resolution.toDurationInMillis();
    }

    public static ToLongFunction<LocalDateTime> from(final LocalDateTime reference, final CalendarDateDuration resolution) {
        return value -> (reference.until(value, CalendarDateUnit.MILLIS)) / resolution.toDurationInMillis();
    }

    public static ToLongFunction<Long> from(final long reference, final CalendarDateDuration resolution) {
        return value -> (value - reference) / resolution.toDurationInMillis();
    }

}
