/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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
package org.ojalgo.finance.data;

import java.util.Calendar;
import java.util.Date;

import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.keyvalue.KeyValue;

public abstract class DatePrice implements KeyValue<CalendarDate, Double> {

    public final CalendarDate key;

    protected DatePrice(final Calendar calendar) {

        super();

        key = new CalendarDate(calendar);
    }

    protected DatePrice(final Date date) {

        super();

        key = new CalendarDate(date);
    }

    protected DatePrice(final long millis) {

        super();

        key = new CalendarDate(millis);
    }

    protected DatePrice(final String sqlString) {

        super();

        key = new CalendarDate(sqlString);
    }

    public int compareTo(final KeyValue<CalendarDate, ?> ref) {
        return key.compareTo(ref.getKey());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DatePrice)) {
            return false;
        }
        final DatePrice other = (DatePrice) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }

    public final CalendarDate getKey() {
        return key;
    }

    public abstract double getPrice();

    public final Double getValue() {
        return this.getPrice();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public final String toString() {
        return key + ": " + this.getPrice();
    }

}
