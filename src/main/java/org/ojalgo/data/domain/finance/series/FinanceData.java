/*
 * Copyright 1997-2022 Optimatika
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.ojalgo.series.BasicSeries;

public interface FinanceData {

    /**
     * Assumes there will be no data for weekends. If there is the implementation nedds to be changed to
     * <code>(temporal) -> TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY).adjustInto(temporal.minus(2, ChronoUnit.DAYS))</code>.
     */
    TemporalAdjuster FRIDAY_OF_WEEK = TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY);
    TemporalAdjuster LAST_DAY_OF_MONTH = TemporalAdjusters.lastDayOfMonth();

    List<DatePrice> getHistoricalPrices();

    BasicSeries<LocalDate, Double> getPriceSeries();

    String getSymbol();

}
