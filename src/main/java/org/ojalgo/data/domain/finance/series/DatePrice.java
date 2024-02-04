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
package org.ojalgo.data.domain.finance.series;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;

import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.PrimitiveNumber;
import org.ojalgo.type.keyvalue.EntryPair;

public abstract class DatePrice implements EntryPair.KeyedPrimitive<LocalDate> {

    static final class DefaultDP extends DatePrice {

        private final double myPrice;

        DefaultDP(final CharSequence text, final DateTimeFormatter formatter, final double price) {
            super(text, formatter);
            myPrice = price;
        }

        DefaultDP(final CharSequence text, final double price) {
            super(text);
            myPrice = price;
        }

        DefaultDP(final LocalDate key, final double price) {
            super(key);
            myPrice = price;
        }

        @Override
        public double getPrice() {
            return myPrice;
        }

    }

    public static DatePrice of(final CharSequence date, final CharSequence price) {
        return new DefaultDP(date, Double.parseDouble(price.toString()));
    }

    public static DatePrice of(final CharSequence date, final DateTimeFormatter formatter, final CharSequence price) {
        return new DefaultDP(date, formatter, Double.parseDouble(price.toString()));
    }

    public static DatePrice of(final CharSequence date, final DateTimeFormatter formatter, final double price) {
        return new DefaultDP(date, formatter, price);
    }

    public static DatePrice of(final CharSequence date, final double price) {
        return new DefaultDP(date, price);
    }

    public static DatePrice of(final LocalDate date, final CharSequence price) {
        return new DefaultDP(date, Double.parseDouble(price.toString()));
    }

    public static DatePrice of(final LocalDate date, final double price) {
        return new DefaultDP(date, price);
    }

    public final LocalDate date;

    protected DatePrice(final CharSequence text) {

        super();

        date = LocalDate.parse(text);
    }

    protected DatePrice(final CharSequence text, final DateTimeFormatter formatter) {

        super();

        date = LocalDate.parse(text, formatter);
    }

    protected DatePrice(final LocalDate key) {

        super();

        date = key;
    }

    public int compareTo(final PrimitiveNumber reference) {

        int retVal = 0;

        if (reference instanceof DatePrice) {
            retVal = date.compareTo(((DatePrice) reference).date);
        }

        if (retVal == 0) {
            retVal = Double.compare(this.getPrice(), reference.doubleValue());
        }

        return retVal;
    }

    public boolean containsKey(final Object key) {
        return date.equals(key);
    }

    public boolean containsValue(final Object value) {
        if (value instanceof Comparable<?>) {
            return NumberDefinition.doubleValue((Comparable<?>) value) == this.getPrice();
        } else {
            return false;
        }
    }

    public final double doubleValue() {
        return this.getPrice();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DatePrice)) {
            return false;
        }
        DatePrice other = (DatePrice) obj;
        if (date == null) {
            if (other.date != null) {
                return false;
            }
        } else if (!date.equals(other.date)) {
            return false;
        }
        return true;
    }

    public PrimitiveNumber get(final Object key) {
        if (date.equals(key)) {
            return this;
        } else {
            return null;
        }
    }

    public final LocalDate getKey() {
        return date;
    }

    public abstract double getPrice();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        return prime * result + ((date == null) ? 0 : date.hashCode());
    }

    public Set<LocalDate> keySet() {
        return Collections.singleton(date);
    }

    @Override
    public final String toString() {
        return this.getKey() + ": " + this.getPrice();
    }
}
