package org.ojalgo.type.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.ToLongFunction;

import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateDuration;
import org.ojalgo.type.CalendarDateUnit;

public class TimeIndex<T> implements ToLongFunction<T> {

    public static TimeIndex<CalendarDate> from(CalendarDate reference, CalendarDateDuration resolution) {
        return new TimeIndex<CalendarDate>(value -> (value.millis - reference.millis) / resolution.toDurationInMillis());
    }

    public static TimeIndex<Instant> from(Instant reference, CalendarDateDuration resolution) {
        return new TimeIndex<Instant>(value -> reference.until(value, ChronoUnit.MILLIS) / resolution.toDurationInMillis());
    }

    public static TimeIndex<LocalDate> from(LocalDate reference, CalendarDateDuration resolution) {
        return new TimeIndex<LocalDate>(value -> (reference.until(value, CalendarDateUnit.MILLIS)) / resolution.toDurationInMillis());
    }

    public static TimeIndex<LocalDateTime> from(LocalDateTime reference, CalendarDateDuration resolution) {
        return new TimeIndex<LocalDateTime>(value -> (reference.until(value, CalendarDateUnit.MILLIS)) / resolution.toDurationInMillis());
    }

    public static TimeIndex<Long> from(Long reference, CalendarDateDuration resolution) {
        return new TimeIndex<Long>(value -> (value - reference) / resolution.toDurationInMillis());
    }

    private final ToLongFunction<T> myConverter;

    TimeIndex(ToLongFunction<T> converter) {

        super();

        myConverter = converter;
    }

    public long applyAsLong(T value) {
        return myConverter.applyAsLong(value);
    }

}
