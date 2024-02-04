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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.ojalgo.series.BasicSeries;
import org.ojalgo.series.SimpleSeries;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.PrimitiveNumber;

public final class SourceCache {

    private static final class Value {

        final BasicSeries<LocalDate, PrimitiveNumber> series;
        CalendarDate updated = new CalendarDate();
        CalendarDate used = null;

        Value(final String name) {

            super();

            series = new SimpleSeries<>();
            series.name(name);
        }

    }

    private static final Timer TIMER = new Timer("SourceCache-Daemon", true);

    private final Map<FinanceData, SourceCache.Value> myCache = new ConcurrentHashMap<>();
    private final Map<FinanceData, FinanceData> myFallback = new ConcurrentHashMap<>();

    private final CalendarDate.Resolution myRefreshInterval;

    public SourceCache(final CalendarDateUnit refreshInterval) {

        super();

        myRefreshInterval = refreshInterval;

        TIMER.schedule(new TimerTask() {

            @Override
            public void run() {
                SourceCache.this.cleanUp();
            }

        }, 0L, refreshInterval.toDurationInMillis());

    }

    public synchronized BasicSeries<LocalDate, PrimitiveNumber> get(final FinanceData key) {

        final CalendarDate now = new CalendarDate();

        Value value = myCache.computeIfAbsent(key, k -> new SourceCache.Value(k.getSymbol()));

        if ((value.used == null) || ((now.millis - value.updated.millis) > myRefreshInterval.toDurationInMillis())) {
            this.update(value, key, now);
        }

        if ((value.series.size() <= 1) && myFallback.containsKey(key)) {
            return this.get(myFallback.get(key));
        } else {
            value.used = now;
            return value.series;
        }
    }

    public synchronized void register(final FinanceData primary, final FinanceData secondary) {

        myCache.computeIfAbsent(primary, k -> new SourceCache.Value(k.getSymbol()));

        myCache.computeIfAbsent(secondary, k -> new SourceCache.Value(k.getSymbol()));

        myFallback.put(primary, secondary);
    }

    private void cleanUp() {

        final CalendarDate now = new CalendarDate();

        for (final Entry<FinanceData, SourceCache.Value> entry : myCache.entrySet()) {
            FinanceData key = entry.getKey();
            Value value = entry.getValue();
            if ((value.used == null) || ((now.millis - value.used.millis) > myRefreshInterval.toDurationInMillis())) {
                value.series.clear();
                myCache.remove(key);
            }
        }
    }

    private void update(final Value cacheValue, final FinanceData cacheKey, final CalendarDate now) {
        BasicSeries<LocalDate, PrimitiveNumber> priceSeries = cacheKey.getPriceSeries();
        for (Entry<LocalDate, PrimitiveNumber> entry : priceSeries.entrySet()) {
            LocalDate entryKey = entry.getKey();
            PrimitiveNumber entryValue = entry.getValue();
            BasicSeries<LocalDate, PrimitiveNumber> cacheValueSeries = cacheValue.series;
            cacheValueSeries.put(entryKey, entryValue);
        }
        cacheValue.updated = now;
    }
}
