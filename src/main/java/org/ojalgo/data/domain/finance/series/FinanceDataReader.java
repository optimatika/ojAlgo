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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.ojalgo.netio.InMemoryFile;
import org.ojalgo.netio.TextLineReader;
import org.ojalgo.series.BasicSeries;
import org.ojalgo.series.SimpleSeries;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.PrimitiveNumber;
import org.ojalgo.type.function.AutoSupplier;
import org.ojalgo.type.keyvalue.KeyValue;

public final class FinanceDataReader<DP extends DatePrice> implements FinanceData<DP>, DataFetcher {

    public static <T extends DatePrice> FinanceDataReader<T> of(final File file, final TextLineReader.Parser<T> parser) {
        return new FinanceDataReader<>(file, parser, CalendarDateUnit.DAY);
    }

    public static <T extends DatePrice> FinanceDataReader<T> of(final File file, final TextLineReader.Parser<T> parser, final CalendarDateUnit resolution) {
        return new FinanceDataReader<>(file, parser, resolution);
    }

    public static <T extends DatePrice> FinanceDataReader<T> of(final InMemoryFile file, final TextLineReader.Parser<T> parser) {
        return new FinanceDataReader<>(file, parser, CalendarDateUnit.DAY);
    }

    public static <T extends DatePrice> FinanceDataReader<T> of(final InMemoryFile file, final TextLineReader.Parser<T> parser,
            final CalendarDateUnit resolution) {
        return new FinanceDataReader<>(file, parser, resolution);
    }

    public static String toSymbol(final String fileName) {

        if (fileName == null || fileName.length() <= 0) {
            return "UNKNOWN";
        }

        String retVal = fileName;
        int lastDotIndex = retVal.lastIndexOf('.');
        if (lastDotIndex > 0) {
            retVal = retVal.substring(0, lastDotIndex);
        }
        return retVal.toUpperCase();
    }

    private final File myFile;
    private final InMemoryFile myInMemoryFile;
    private final TextLineReader.Parser<DP> myParser;
    private final CalendarDateUnit myResolution;

    FinanceDataReader(final File file, final TextLineReader.Parser<DP> parser, final CalendarDateUnit resolution) {
        super();
        myFile = file;
        myInMemoryFile = null;
        myParser = parser;
        myResolution = resolution;
    }

    FinanceDataReader(final InMemoryFile file, final TextLineReader.Parser<DP> parser, final CalendarDateUnit resolution) {
        super();
        myFile = null;
        myInMemoryFile = file;
        myParser = parser;
        myResolution = resolution;
    }

    public KeyValue<String, List<DP>> getHistoricalData() {
        return KeyValue.of(this.getSymbol(), this.getHistoricalPrices());
    }

    public List<DP> getHistoricalPrices() {

        List<DP> retVal = new ArrayList<>();

        if (myFile != null) {
            try (TextLineReader reader = TextLineReader.of(myFile); AutoSupplier<DP> supplier = reader.withFilteredParser(myParser)) {
                supplier.forEach(retVal::add);
            } catch (Exception cause) {
                throw new RuntimeException(cause);
            }
        } else if (myInMemoryFile != null) {
            try (TextLineReader reader = TextLineReader.of(myInMemoryFile); AutoSupplier<DP> supplier = reader.withFilteredParser(myParser)) {
                supplier.forEach(retVal::add);
            } catch (Exception cause) {
                throw new RuntimeException(cause);
            }
        } else {
            throw new IllegalStateException();
        }

        return retVal;
    }

    public InputStream getInputStream() {
        if (myFile != null) {
            try {
                return new FileInputStream(myFile);
            } catch (FileNotFoundException cause) {
                throw new RuntimeException(cause);
            }
        } else if (myInMemoryFile != null) {
            return myInMemoryFile.newInputStream();
        } else {
            throw new IllegalStateException();
        }
    }

    public BasicSeries<LocalDate, PrimitiveNumber> getPriceSeries() {

        BasicSeries<LocalDate, PrimitiveNumber> retVal = new SimpleSeries<>();

        if (myFile != null) {
            try (TextLineReader reader = TextLineReader.of(myFile); AutoSupplier<DP> supplier = reader.withFilteredParser(myParser)) {
                supplier.forEach(dp -> retVal.put(dp.date, dp));
            } catch (Exception cause) {
                throw new RuntimeException(cause);
            }
        } else if (myInMemoryFile != null) {
            try (TextLineReader reader = TextLineReader.of(myInMemoryFile); AutoSupplier<DP> supplier = reader.withFilteredParser(myParser)) {
                supplier.forEach(dp -> retVal.put(dp.date, dp));
            } catch (Exception cause) {
                throw new RuntimeException(cause);
            }
        } else {
            throw new IllegalStateException();
        }

        return retVal;
    }

    public CalendarDateUnit getResolution() {
        return myResolution;
    }

    public String getSymbol() {
        if (myFile != null) {
            return FinanceDataReader.toSymbol(myFile.getName());
        } else if (myInMemoryFile != null) {
            return FinanceDataReader.toSymbol(myInMemoryFile.getName().orElse(""));
        } else {
            throw new IllegalStateException();
        }
    }

}
