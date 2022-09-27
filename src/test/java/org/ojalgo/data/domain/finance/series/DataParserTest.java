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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.netio.TextLineReader;
import org.ojalgo.type.function.AutoSupplier;

public class DataParserTest extends FinanceSeriesTests {

    static class ResultsConsumer<DP extends DatePrice> implements Consumer<DP> {

        List<DP> data = new ArrayList<>();

        public void accept(final DP parsed) {
            data.add(parsed);
        }

        LocalDate firstDate() {
            return data.get(0).date;
        }

        double firstPrice() {
            return data.get(0).getPrice();
        }

        LocalDate lastDate() {
            return data.get(data.size() - 1).date;
        }

        double lastPrice() {
            return data.get(data.size() - 1).getPrice();
        }

        int size() {
            return data.size();
        }

    }

    private static final String PATH = "./src/test/resources/org/ojalgo/data/domain/finance/series/";

    static void compareWithDetectingParser(final File file, final ResultsConsumer<?> results) {

        DatePriceParser parser = new DatePriceParser();

        // try (TextLineReader reader = TextLineReader.of(file); AutoSupplier<DatePrice> supplier = reader.withFilteredParser(BasicParser::isLineOK, parser)) {
        try (TextLineReader reader = TextLineReader.of(file); AutoSupplier<DatePrice> supplier = reader.withParser(parser)) {

            ResultsConsumer<DatePrice> collector = new ResultsConsumer<>();

            for (DatePrice datePrice : supplier) {
                collector.accept(datePrice);
            }

            TestUtils.assertEquals(results.size(), collector.size());
            TestUtils.assertEquals(results.firstDate(), collector.firstDate());
            TestUtils.assertEquals(results.lastDate(), collector.lastDate());
            TestUtils.assertEquals(results.firstPrice(), collector.firstPrice());
            TestUtils.assertEquals(results.lastPrice(), collector.lastPrice());

        } catch (Exception cause) {
            TestUtils.fail(cause);
        }

    }

    @Test
    public void testAlphaVantageDailyAAPL() {

        File file = new File(PATH + "AlphaVantage-AAPL-daily.csv");

        AlphaVantageParser parser = new AlphaVantageParser();

        ResultsConsumer<AlphaVantageParser.Data> collector = new ResultsConsumer<>();

        parser.parse(file, true, collector);

        TestUtils.assertEquals(5251, collector.size());

        TestUtils.assertEquals(LocalDate.of(2018, 11, 12), collector.firstDate());
        TestUtils.assertEquals(194.1700, collector.firstPrice(), PrimitiveMath.MACHINE_EPSILON);

        TestUtils.assertEquals(LocalDate.of(1998, 1, 2), collector.lastDate());
        TestUtils.assertEquals(0.5125, collector.lastPrice(), PrimitiveMath.MACHINE_EPSILON);

        DataParserTest.compareWithDetectingParser(file, collector);
    }

    @Test
    public void testAlphaVantageDailyMSFT() {

        File file = new File(PATH + "AlphaVantage-MSFT-daily.csv");

        AlphaVantageParser parser = new AlphaVantageParser();

        ResultsConsumer<AlphaVantageParser.Data> collector = new ResultsConsumer<>();

        parser.parse(file, true, collector);

        TestUtils.assertEquals(100, collector.size());

        TestUtils.assertEquals(LocalDate.of(2018, 11, 12), collector.firstDate());
        TestUtils.assertEquals(106.7600, collector.firstPrice(), PrimitiveMath.MACHINE_EPSILON);

        TestUtils.assertEquals(LocalDate.of(2018, 6, 22), collector.lastDate());
        TestUtils.assertEquals(100.0198, collector.lastPrice(), PrimitiveMath.MACHINE_EPSILON);

        DataParserTest.compareWithDetectingParser(file, collector);
    }

    @Test
    public void testAlphaVantageMonthlyAAPL() {

        File file = new File(PATH + "AlphaVantage-AAPL-monthly.csv");

        AlphaVantageParser parser = new AlphaVantageParser();

        ResultsConsumer<AlphaVantageParser.Data> collector = new ResultsConsumer<>();

        parser.parse(file, true, collector);

        TestUtils.assertEquals(250, collector.size());

        TestUtils.assertEquals(LocalDate.of(2018, 11, 12), collector.firstDate());
        TestUtils.assertEquals(194.1700, collector.firstPrice(), PrimitiveMath.MACHINE_EPSILON);

        TestUtils.assertEquals(LocalDate.of(1998, 2, 27), collector.lastDate());
        TestUtils.assertEquals(0.7450, collector.lastPrice(), PrimitiveMath.MACHINE_EPSILON);

        DataParserTest.compareWithDetectingParser(file, collector);
    }

    @Test
    public void testAlphaVantageMonthlyMSFT() {

        File file = new File(PATH + "AlphaVantage-MSFT-monthly.csv");

        AlphaVantageParser parser = new AlphaVantageParser();

        ResultsConsumer<AlphaVantageParser.Data> collector = new ResultsConsumer<>();

        parser.parse(file, true, collector);

        TestUtils.assertEquals(250, collector.size());

        TestUtils.assertEquals(LocalDate.of(2018, 11, 12), collector.firstDate());
        TestUtils.assertEquals(106.8100, collector.firstPrice(), PrimitiveMath.MACHINE_EPSILON);

        TestUtils.assertEquals(LocalDate.of(1998, 2, 27), collector.lastDate());
        TestUtils.assertEquals(13.9246, collector.lastPrice(), PrimitiveMath.MACHINE_EPSILON);

        DataParserTest.compareWithDetectingParser(file, collector);
    }

    @Test
    public void testAlphaVantageWeeklyAAPL() {

        File file = new File(PATH + "AlphaVantage-AAPL-weekly.csv");

        AlphaVantageParser parser = new AlphaVantageParser();

        ResultsConsumer<AlphaVantageParser.Data> collector = new ResultsConsumer<>();

        parser.parse(file, true, collector);

        TestUtils.assertEquals(1089, collector.size());

        TestUtils.assertEquals(LocalDate.of(2018, 11, 12), collector.firstDate());
        TestUtils.assertEquals(194.1700, collector.firstPrice(), PrimitiveMath.MACHINE_EPSILON);

        TestUtils.assertEquals(LocalDate.of(1998, 1, 9), collector.lastDate());
        TestUtils.assertEquals(0.5737, collector.lastPrice(), PrimitiveMath.MACHINE_EPSILON);

        DataParserTest.compareWithDetectingParser(file, collector);
    }

    @Test
    public void testAlphaVantageWeeklyMSFT() {

        File file = new File(PATH + "AlphaVantage-MSFT-weekly.csv");

        AlphaVantageParser parser = new AlphaVantageParser();

        ResultsConsumer<AlphaVantageParser.Data> collector = new ResultsConsumer<>();

        parser.parse(file, true, collector);

        TestUtils.assertEquals(1089, collector.size());

        TestUtils.assertEquals(LocalDate.of(2018, 11, 12), collector.firstDate());
        TestUtils.assertEquals(106.9363, collector.firstPrice(), PrimitiveMath.MACHINE_EPSILON);

        TestUtils.assertEquals(LocalDate.of(1998, 1, 9), collector.lastDate());
        TestUtils.assertEquals(10.4332, collector.lastPrice(), PrimitiveMath.MACHINE_EPSILON);

        DataParserTest.compareWithDetectingParser(file, collector);
    }

    @Test
    public void testIEXTradingDailyAAPL() {

        File file = new File(PATH + "IEXTrading-AAPL-daily.csv");

        IEXTradingParser parser = new IEXTradingParser();

        ResultsConsumer<IEXTradingParser.Data> collector = new ResultsConsumer<>();

        parser.parse(file, true, collector);

        TestUtils.assertEquals(1259, collector.size());

        TestUtils.assertEquals(LocalDate.of(2013, 11, 13), collector.firstDate());
        TestUtils.assertEquals(68.2606, collector.firstPrice(), PrimitiveMath.MACHINE_EPSILON);

        TestUtils.assertEquals(LocalDate.of(2018, 11, 12), collector.lastDate());
        TestUtils.assertEquals(194.17, collector.lastPrice(), PrimitiveMath.MACHINE_EPSILON);

        DataParserTest.compareWithDetectingParser(file, collector);
    }

    @Test
    public void testYahooDailyAAPL() {

        File file = new File(PATH + "Yahoo-AAPL-daily.csv");

        YahooParser parser = new YahooParser();

        ResultsConsumer<YahooParser.Data> collector = new ResultsConsumer<>();

        parser.parse(file, true, collector);

        TestUtils.assertEquals(9561, collector.size());

        TestUtils.assertEquals(LocalDate.of(1980, 12, 12), collector.firstDate());
        TestUtils.assertEquals(0.023106, collector.firstPrice(), PrimitiveMath.MACHINE_EPSILON);

        TestUtils.assertEquals(LocalDate.of(2018, 11, 9), collector.lastDate());
        TestUtils.assertEquals(204.470001, collector.lastPrice(), PrimitiveMath.MACHINE_EPSILON);

        DataParserTest.compareWithDetectingParser(file, collector);
    }

    @Test
    public void testYahooMonthlyAAPL() {

        File file = new File(PATH + "Yahoo-AAPL-monthly.csv");

        YahooParser parser = new YahooParser();

        ResultsConsumer<YahooParser.Data> collector = new ResultsConsumer<>();

        parser.parse(file, true, collector);

        TestUtils.assertEquals(457, collector.size());

        TestUtils.assertEquals(LocalDate.of(1980, 12, 1), collector.firstDate());
        TestUtils.assertEquals(0.027425, collector.firstPrice(), PrimitiveMath.MACHINE_EPSILON);

        TestUtils.assertEquals(LocalDate.of(2018, 11, 9), collector.lastDate());
        TestUtils.assertEquals(204.470001, collector.lastPrice(), PrimitiveMath.MACHINE_EPSILON);

        DataParserTest.compareWithDetectingParser(file, collector);
    }

    @Test
    public void testYahooWeeklyAAPL() {

        File file = new File(PATH + "Yahoo-AAPL-weekly.csv");

        YahooParser parser = new YahooParser();

        ResultsConsumer<YahooParser.Data> collector = new ResultsConsumer<>();

        parser.parse(file, true, collector);

        TestUtils.assertEquals(1980, collector.size());

        TestUtils.assertEquals(LocalDate.of(1980, 12, 8), collector.firstDate());
        TestUtils.assertEquals(0.023106, collector.firstPrice(), PrimitiveMath.MACHINE_EPSILON);

        TestUtils.assertEquals(LocalDate.of(2018, 11, 9), collector.lastDate());
        TestUtils.assertEquals(204.470001, collector.lastPrice(), PrimitiveMath.MACHINE_EPSILON);

        DataParserTest.compareWithDetectingParser(file, collector);
    }

}
