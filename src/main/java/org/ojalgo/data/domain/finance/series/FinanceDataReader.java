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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.ojalgo.netio.BasicParser;
import org.ojalgo.series.BasicSeries;
import org.ojalgo.series.SimpleSeries;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.PrimitiveNumber;
import org.ojalgo.type.keyvalue.KeyValue;

public final class FinanceDataReader<T extends DatePrice> implements FinanceData<T>, DataFetcher {

    public static <T extends DatePrice> FinanceDataReader<T> of(final File file, final BasicParser<T> parser) {
        return new FinanceDataReader<>(file, parser, CalendarDateUnit.DAY);
    }

    public static <T extends DatePrice> FinanceDataReader<T> of(final File file, final BasicParser<T> parser, final CalendarDateUnit resolution) {
        return new FinanceDataReader<>(file, parser, resolution);
    }

    private final File myFile;
    private final BasicParser<T> myParser;
    private final CalendarDateUnit myResolution;

    FinanceDataReader(final File file, final BasicParser<T> parser, final CalendarDateUnit resolution) {
        super();
        myFile = file;
        myParser = parser;
        myResolution = resolution;
    }

    public KeyValue<String, List<T>> getHistoricalData() {
        return KeyValue.of(this.getSymbol(), this.getHistoricalPrices());
    }

    public List<T> getHistoricalPrices() {

        List<T> retVal = new ArrayList<>();

        myParser.parse(myFile, retVal::add);

        return retVal;
    }

    public BasicSeries<LocalDate, PrimitiveNumber> getPriceSeries() {

        BasicSeries<LocalDate, PrimitiveNumber> series = new SimpleSeries<>();

        myParser.parse(myFile, dp -> series.put(dp.date, dp));

        return series;
    }

    public CalendarDateUnit getResolution() {
        return myResolution;
    }

    public Reader getStreamOfCSV() {
        try {
            return new FileReader(myFile);
        } catch (FileNotFoundException cause) {
            throw new RuntimeException(cause);
        }
    }

    public String getSymbol() {
        String name = myFile.getName();
        int limit = name.indexOf('.');
        return name.substring(0, limit);
    }

}
