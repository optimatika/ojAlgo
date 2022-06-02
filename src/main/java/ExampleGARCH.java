import java.io.File;
import java.time.LocalDate;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.data.domain.finance.series.DataSource;
import org.ojalgo.data.domain.finance.series.DataSource.Coordinated;
import org.ojalgo.data.domain.finance.series.FinanceDataReader;
import org.ojalgo.data.domain.finance.series.YahooParser;
import org.ojalgo.data.domain.finance.series.YahooParser.Data;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.SampleSet;
import org.ojalgo.series.BasicSeries;
import org.ojalgo.series.primitive.PrimitiveSeries;
import org.ojalgo.type.CalendarDateUnit;

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

public abstract class ExampleGARCH {

    static final File N225 = new File("/Users/apete/Developer/data/finance/^N225.csv");
    static final File RUT = new File("/Users/apete/Developer/data/finance/^RUT.csv");

    public static void main(final String... args) {

        BasicLogger.debug();
        BasicLogger.debug(ExampleGARCH.class);
        BasicLogger.debug(OjAlgoUtils.getTitle());
        BasicLogger.debug(OjAlgoUtils.getDate());
        BasicLogger.debug();

        FinanceDataReader<Data> readerN225 = FinanceDataReader.of(N225, YahooParser.INSTANCE);
        BasicSeries<LocalDate, Double> seriesN225 = readerN225.getPriceSeries();
        BasicLogger.debug("N225: {}", seriesN225.toString());

        FinanceDataReader<Data> readerRUT = FinanceDataReader.of(RUT, YahooParser.INSTANCE);
        BasicSeries<LocalDate, Double> seriesRUT = readerRUT.getPriceSeries();
        BasicLogger.debug("RUT: {}", seriesRUT.toString());
        BasicLogger.debug();

        Coordinated monthly = DataSource.coordinated(CalendarDateUnit.MONTH);

        monthly.add(readerN225);
        monthly.add(readerRUT);
        BasicLogger.debug("Coordinated monthly: {}", monthly.get());

        Coordinated weekly = DataSource.coordinated(CalendarDateUnit.WEEK);

        weekly.add(readerN225);
        weekly.add(readerRUT);

        BasicLogger.debug("Coordinated weekly: {}", weekly.get());

        PrimitiveSeries asPrimitive = seriesN225.asPrimitive();
        BasicLogger.debug("asPrimitive: {}", asPrimitive);
        PrimitiveSeries log = asPrimitive.log();
        BasicLogger.debug("log: {}", log);
        PrimitiveSeries logDifferences = log.differences();
        BasicLogger.debug("logDifferences: {}", logDifferences);

        SampleSet samples = SampleSet.wrap(logDifferences);
        double mean = samples.getMean();
        double stdDev = samples.getStandardDeviation();

        PrimitiveSeries error = logDifferences.subtract(mean);
        BasicLogger.debug("error: {}", error);
        SampleSet samples2 = SampleSet.wrap(error);
        double mean2 = samples2.getMean();
        double stdDev2 = samples2.getStandardDeviation();
        BasicLogger.debug("mean2: {}", mean2);
        BasicLogger.debug("stdDev2: {}", stdDev2);

    }
}
