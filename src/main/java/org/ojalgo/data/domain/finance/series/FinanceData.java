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
import java.util.List;

import org.ojalgo.series.BasicSeries;
import org.ojalgo.series.SimpleSeries;
import org.ojalgo.type.PrimitiveNumber;
import org.ojalgo.type.keyvalue.KeyValue;

/**
 * A source of (historical) financial time series data.
 */
@FunctionalInterface
public interface FinanceData<DP extends DatePrice> {

    KeyValue<String, List<DP>> getHistoricalData();

    /**
     * @deprecated v52 Use {@link #getHistoricalData()} instead.
     */
    @Deprecated
    default List<DP> getHistoricalPrices() {
        return this.getHistoricalData().getValue();
    }

    /**
     * @deprecated v52 Use {@link #getHistoricalData()} instead.
     */
    @Deprecated
    default BasicSeries<LocalDate, PrimitiveNumber> getPriceSeries() {

        KeyValue<String, List<DP>> data = this.getHistoricalData();

        BasicSeries<LocalDate, PrimitiveNumber> series = new SimpleSeries<>();

        series.setName(data.getKey());

        for (DP item : data.getValue()) {
            series.put(item.getKey(), item.getValue());
        }

        return series;
    }

    /**
     * @deprecated v52 Use {@link #getHistoricalData()} instead.
     */
    @Deprecated
    default String getSymbol() {
        return this.getHistoricalData().getKey();

    }

}
