/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.ojalgo.series.CalendarDateSeries;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateUnit;

public final class SourceCache {

    private static final class Value {

        CalendarDate used = new CalendarDate();
        final CalendarDateSeries<Double> series;
        CalendarDate updated = new CalendarDate();

        @SuppressWarnings("unused")
        private Value() {
            this(null, null);
        }

        Value(final String name, final CalendarDateUnit resolution) {

            super();

            series = new CalendarDateSeries<Double>(resolution);
            series.name(name);
        }

    }

    private static final Timer TIMER = new Timer("SourceCache-Daemon", true);

    private final Map<DataSource<?>, SourceCache.Value> myCache = Collections.synchronizedMap(new HashMap<DataSource<?>, SourceCache.Value>());
    private final CalendarDateUnit myResolution;

    public SourceCache(final CalendarDateUnit aResolution) {

        super();

        myResolution = aResolution;

        TIMER.schedule(new TimerTask() {

            @Override
            public void run() {
                SourceCache.this.cleanUp();
            }

        }, 0L, aResolution.size());

    }

    public synchronized CalendarDateSeries<Double> get(final DataSource<?> key) {

        final CalendarDate tmpNow = new CalendarDate();

        Value tmpValue = myCache.get(key);

        if (tmpValue != null) {

            if (myResolution.count(tmpValue.updated.millis, tmpNow.millis) > 0L) {
                this.update(tmpValue, key, tmpNow);
            }

        } else {

            tmpValue = new SourceCache.Value(key.getSymbol(), myResolution);

            myCache.put(key, tmpValue);

            this.update(tmpValue, key, tmpNow);
        }

        tmpValue.used = tmpNow;

        return tmpValue.series;
    }

    private void cleanUp() {

        final CalendarDate tmpNow = new CalendarDate();

        for (final Entry<DataSource<?>, SourceCache.Value> tmpEntry : myCache.entrySet()) {

            if (myResolution.count(tmpEntry.getValue().used.millis, tmpNow.millis) > 1L) {
                tmpEntry.getValue().series.clear();
                myCache.remove(tmpEntry.getKey());
            }
        }
    }

    private void update(final Value aCacheValue, final DataSource<?> key, final CalendarDate now) {
        aCacheValue.series.putAll(key.getPriceSeries());
        aCacheValue.updated = now;
    }
}
