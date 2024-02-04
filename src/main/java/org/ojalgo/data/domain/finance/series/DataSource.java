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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.ojalgo.array.DenseArray;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.BasicParser;
import org.ojalgo.series.BasicSeries;
import org.ojalgo.series.CalendarDateSeries;
import org.ojalgo.series.SimpleSeries;
import org.ojalgo.series.primitive.CoordinatedSet;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.PrimitiveNumber;
import org.ojalgo.type.function.AutoSupplier;
import org.ojalgo.type.keyvalue.KeyValue;

public final class DataSource implements FinanceData<DatePrice> {

    public static final class Coordinated implements Supplier<CoordinatedSet<LocalDate>> {

        private final CoordinatedSet.Builder<LocalDate> myBuilder = CoordinatedSet.builder();
        private final SourceCache myCache = new SourceCache(CalendarDateUnit.DAY);
        private CalendarDateUnit myResolution = CalendarDateUnit.MONTH;
        private final YahooSession myYahooSession = new YahooSession();

        public <T extends DatePrice> DataSource.Coordinated add(final FinanceData<T> data) {
            myBuilder.add(() -> myCache.get(data));
            return this;
        }

        public <T1 extends DatePrice, T2 extends DatePrice> DataSource.Coordinated add(final FinanceData<T1> primary, final FinanceData<T2> secondary) {
            myCache.register(primary, secondary);
            myBuilder.add(() -> myCache.get(primary));
            return this;
        }

        public DataSource.Coordinated addAlphaVantage(final String symbol, final String apiKey) {
            myBuilder.add(() -> myCache.get(DataSource.newAlphaVantage(symbol, myResolution, apiKey, true)));
            return this;
        }

        public DataSource.Coordinated addIEXTrading(final String symbol) {
            myBuilder.add(() -> myCache.get(DataSource.newIEXTrading(symbol)));
            return this;
        }

        public <T extends DatePrice> DataSource.Coordinated addReader(final File file, final BasicParser<T> parser) {
            return this.add(FinanceDataReader.of(file, parser));
        }

        public DataSource.Coordinated addYahoo(final String symbol) {
            myBuilder.add(() -> myCache.get(DataSource.newYahoo(myYahooSession, symbol, myResolution)));
            return this;
        }

        public CoordinatedSet<LocalDate> get() {
            switch (myResolution) {
            case YEAR:
                return myBuilder.build(LAST_DAY_OF_YEAR);
            case MONTH:
                return myBuilder.build(LAST_DAY_OF_MONTH);
            case WEEK:
                return myBuilder.build(FRIDAY_OF_WEEK);
            default:
                return myBuilder.build();
            }
        }

        public DataSource.Coordinated resolution(final CalendarDateUnit resolution) {
            myResolution = resolution;
            return this;
        }

    }

    /**
     * Move the date forward, if necessary, to a Friday.
     */
    public static final UnaryOperator<LocalDate> FRIDAY_OF_WEEK = d -> (LocalDate) TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY).adjustInto(d);
    /**
     * Move the date forward, if necessary, to the last day of the month.
     */
    public static final UnaryOperator<LocalDate> LAST_DAY_OF_MONTH = d -> (LocalDate) TemporalAdjusters.lastDayOfMonth().adjustInto(d);
    /**
     * Move the date forward, if necessary, to the last day of the year.
     */
    public static final UnaryOperator<LocalDate> LAST_DAY_OF_YEAR = d -> LocalDate.of(d.getYear(), 12, 31);

    public static Coordinated coordinated() {
        return new DataSource.Coordinated();
    }

    public static Coordinated coordinated(final CalendarDateUnit resolution) {
        return new DataSource.Coordinated().resolution(resolution);
    }

    public static DataSource newAlphaVantage(final String symbol, final CalendarDateUnit resolution, final String apiKey) {
        return DataSource.newAlphaVantage(symbol, resolution, apiKey, false);
    }

    public static DataSource newAlphaVantage(final String symbol, final CalendarDateUnit resolution, final String apiKey, final boolean fullOutputSize) {
        AlphaVantageFetcher fetcher = new AlphaVantageFetcher(symbol, resolution, apiKey, fullOutputSize);
        AlphaVantageParser parser = new AlphaVantageParser();
        return new DataSource(fetcher, parser);
    }

    public static <T extends DatePrice> DataSource newFileReader(final File file, final BasicParser<T> parser) {
        return new DataSource(FinanceDataReader.of(file, parser), parser);
    }

    public static <T extends DatePrice> DataSource newFileReader(final File file, final BasicParser<T> parser, final CalendarDateUnit resolution) {
        return new DataSource(FinanceDataReader.of(file, parser, resolution), parser);
    }

    public static DataSource newIEXTrading(final String symbol) {
        IEXTradingFetcher fetcher = new IEXTradingFetcher(symbol);
        IEXTradingParser parser = new IEXTradingParser();
        return new DataSource(fetcher, parser);
    }

    public static DataSource newYahoo(final YahooSession session, final String symbol, final CalendarDateUnit resolution) {
        YahooSession.Fetcher fetcher = session.newFetcher(symbol, resolution);
        YahooParser parser = new YahooParser();
        return new DataSource(fetcher, parser);
    }

    private final DataFetcher myFetcher;
    private final BasicParser<? extends DatePrice> myParser;

    DataSource(final DataFetcher fetcher, final BasicParser<? extends DatePrice> parser) {
        super();
        myFetcher = fetcher;
        myParser = parser;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof DataSource)) {
            return false;
        }
        DataSource other = (DataSource) obj;
        if (myFetcher == null) {
            if (other.myFetcher != null) {
                return false;
            }
        } else if (!myFetcher.equals(other.myFetcher)) {
            return false;
        }
        if (myParser == null) {
            if (other.myParser != null) {
                return false;
            }
        } else if (!myParser.equals(other.myParser)) {
            return false;
        }
        return true;
    }

    public CalendarDateSeries<Double> getCalendarDateSeries() {
        return this.getCalendarDateSeries(myFetcher.getResolution(), LocalTime.NOON, ZoneOffset.UTC);
    }

    public CalendarDateSeries<Double> getCalendarDateSeries(final CalendarDateUnit resolution) {
        return this.getCalendarDateSeries(resolution, LocalTime.NOON, ZoneOffset.UTC);
    }

    public CalendarDateSeries<Double> getCalendarDateSeries(final CalendarDateUnit resolution, final LocalTime time, final ZoneId zoneId) {

        CalendarDateSeries<Double> retVal = new CalendarDateSeries<>(resolution);

        for (DatePrice datePrice : this.getHistoricalPrices()) {
            retVal.put(CalendarDate.valueOf(datePrice.getKey().atTime(time).atZone(zoneId)), datePrice.getPrice());
        }

        return retVal;
    }

    public CalendarDateSeries<Double> getCalendarDateSeries(final LocalTime time, final ZoneId zoneId) {
        return this.getCalendarDateSeries(myFetcher.getResolution(), time, zoneId);
    }

    public KeyValue<String, List<DatePrice>> getHistoricalData() {

        String key = myFetcher.getSymbol();

        List<DatePrice> value = new ArrayList<>();

        try (AutoSupplier<? extends DatePrice> reader = myFetcher.getReader(myParser)) {
            reader.forEach(value::add);
        } catch (final Exception cause) {
            BasicLogger.error(cause, "Fetch problem for {}!", myFetcher.getClass().getSimpleName());
            BasicLogger.error("Symbol & Resolution: {} & {}", myFetcher.getSymbol(), myFetcher.getResolution());
        }

        Collections.sort(value);

        return KeyValue.of(key, value);
    }

    public List<DatePrice> getHistoricalPrices() {
        return this.getHistoricalData().getValue();
    }

    public BasicSeries<LocalDate, PrimitiveNumber> getLocalDateSeries() {
        return this.getLocalDateSeries(this.getHistoricalPrices(), myFetcher.getResolution());
    }

    public BasicSeries<LocalDate, PrimitiveNumber> getLocalDateSeries(final CalendarDateUnit resolution) {
        return this.getLocalDateSeries(this.getHistoricalPrices(), resolution);
    }

    public BasicSeries<LocalDate, PrimitiveNumber> getLocalDateSeries(final CalendarDateUnit resolution, final DenseArray.Factory<Double> denseArrayFactory) {
        return this.getLocalDateSeries(this.getHistoricalPrices(), resolution);
    }

    public BasicSeries<LocalDate, PrimitiveNumber> getLocalDateSeries(final DenseArray.Factory<Double> denseArrayFactory) {
        return this.getLocalDateSeries(this.getHistoricalPrices(), myFetcher.getResolution());
    }

    public BasicSeries<LocalDate, PrimitiveNumber> getPriceSeries() {
        return this.getLocalDateSeries(this.getHistoricalPrices(), myFetcher.getResolution());
    }

    public String getSymbol() {
        return myFetcher.getSymbol();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myFetcher == null ? 0 : myFetcher.hashCode());
        return prime * result + (myParser == null ? 0 : myParser.hashCode());
    }

    private BasicSeries<LocalDate, PrimitiveNumber> getLocalDateSeries(final List<DatePrice> historicalPrices, final CalendarDateUnit resolution) {

        BasicSeries<LocalDate, PrimitiveNumber> retVal = new SimpleSeries<>();
        retVal.name(this.getSymbol());

        LocalDate adjusted;
        for (DatePrice datePrice : historicalPrices) {
            switch (resolution) {
            case YEAR:
                adjusted = LAST_DAY_OF_YEAR.apply(datePrice.date);
                break;
            case MONTH:
                adjusted = LAST_DAY_OF_MONTH.apply(datePrice.date);
                break;
            case WEEK:
                adjusted = FRIDAY_OF_WEEK.apply(datePrice.date);
                break;
            default:
                adjusted = datePrice.date;
                break;
            }
            retVal.put(adjusted, datePrice);
        }

        return retVal;
    }

}
