import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.data.domain.finance.series.DataSource;
import org.ojalgo.data.domain.finance.series.FinanceDataReader;
import org.ojalgo.data.domain.finance.series.YahooParser;
import org.ojalgo.data.domain.finance.series.YahooParser.Data;
import org.ojalgo.matrix.store.Primitive32Store;
import org.ojalgo.netio.ASCII;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.TextLineWriter;
import org.ojalgo.netio.TextLineWriter.CSVLineBuilder;
import org.ojalgo.random.SampleSet;
import org.ojalgo.random.process.RandomProcess.SimulationResults;
import org.ojalgo.random.process.StationaryNormalProcess;
import org.ojalgo.random.scedasticity.GARCH;
import org.ojalgo.series.BasicSeries;
import org.ojalgo.series.primitive.CoordinatedSet;
import org.ojalgo.series.primitive.PrimitiveSeries;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.PrimitiveNumber;
import org.ojalgo.type.StandardType;

/**
 * Example use of GARCH models, and in addition some general time series handling.
 */
public abstract class ExampleGARCH {

    /**
     * A file containing historical data for the Nikkei 225 index, downloaded from Yahoo Finance.
     *
     * @see https://finance.yahoo.com/quote/^N225/history
     */
    static final File N225 = new File("/Users/apete/Developer/data/finance/^N225.csv");
    /**
     * Where to write output data for the chart.
     */
    static final File OUTPUT = new File("/Users/apete/Developer/data/finance/output.csv");
    /**
     * A file containing historical data for the Russell 2000 index, downloaded from Yahoo Finance.
     *
     * @see https://finance.yahoo.com/quote/^RUT/history
     */
    static final File RUT = new File("/Users/apete/Developer/data/finance/^RUT.csv");

    public static void main(final String... args) {

        BasicLogger.debug();
        BasicLogger.debug(ExampleGARCH.class);
        BasicLogger.debug(OjAlgoUtils.getTitle());
        BasicLogger.debug(OjAlgoUtils.getDate());
        BasicLogger.debug();

        FinanceDataReader<Data> readerN225 = FinanceDataReader.of(N225, YahooParser.INSTANCE);
        BasicSeries<LocalDate, PrimitiveNumber> seriesN225 = readerN225.getPriceSeries();
        BasicLogger.debug("N225: {}", seriesN225.toString());

        FinanceDataReader<Data> readerRUT = FinanceDataReader.of(RUT, YahooParser.INSTANCE);
        BasicSeries<LocalDate, PrimitiveNumber> seriesRUT = readerRUT.getPriceSeries();
        BasicLogger.debug("RUT: {}", seriesRUT.toString());

        BasicLogger.debug();

        /*
         * If you want to calculate something like correlations between a set of series they need to be
         * coordinated – the same start, finish and sampling frequency, as well as no missing values.
         */

        CoordinatedSet<LocalDate> coordinatedDaily = DataSource.coordinated(CalendarDateUnit.DAY).add(readerN225).add(readerRUT).get();
        BasicLogger.debug("Coordinated daily   : {}", coordinatedDaily);

        /*
         * We have 1 value per day, but also know that we're missing data for weekends, holidays and such
         * (which need to be filled-in). Could be a good idea to instead coordinate weekly data.
         */

        CoordinatedSet<LocalDate> coordinatedWeekly = DataSource.coordinated(CalendarDateUnit.WEEK).add(readerN225).add(readerRUT).get();
        BasicLogger.debug("Coordinated weekly  : {}", coordinatedWeekly);

        /*
         * Note that we don't feed the coordinator series, but something that can provide series. In this case
         * it's file readers/parsers, but it could just as well be something that calls web services, queries
         * a database or whatever. A coordinated data source will populate itself (lazily) on request, and
         * then clean up / reset itself using a timer.
         */

        CoordinatedSet<LocalDate> coordinatedMonthly = DataSource.coordinated(CalendarDateUnit.MONTH).add(readerN225).add(readerRUT).get();
        BasicLogger.debug("Coordinated monthly : {}", coordinatedMonthly);

        CoordinatedSet<LocalDate> coordinatedAnnually = DataSource.coordinated(CalendarDateUnit.YEAR).add(readerN225).add(readerRUT).get();
        BasicLogger.debug("Coordinated annually: {}", coordinatedAnnually);

        BasicLogger.debug();

        /*
         * Once we have a CoordinatedSet we can easily calculate the correlation coefficients.
         */

        Primitive32Store correlations = coordinatedWeekly.log().differences().getCorrelations(Primitive32Store.FACTORY);
        BasicLogger.debug("Correlations", correlations);

        /*
         * That was a bit of a detour. Now let's focus on just one series, and the volatility of that index.
         */

        PrimitiveSeries logDifferences = seriesN225.resample(DataSource.FRIDAY_OF_WEEK).asPrimitive().log().differences();
        SampleSet logDifferenceStatistics = SampleSet.wrap(logDifferences);
        BasicLogger.debug("Log difference statistics: {}", logDifferenceStatistics.toString());

        PrimitiveSeries errorTerms = logDifferences.subtract(logDifferenceStatistics.getMean());
        SampleSet errorTermStatistics = SampleSet.wrap(errorTerms);
        BasicLogger.debug("Error term statistics: {}", errorTermStatistics.toString());

        /*
         * If we assume homoscedasticity then the (weekly) variance of the N225 time series is simply the
         * variance of the error term sample set. Note also that the variance of the log differences series
         * and the error term differences are the same.
         */

        BasicLogger.debug();
        BasicLogger.debug("Variance");
        BasicLogger.debug(1, "of log differences: {}", logDifferenceStatistics.getVariance());
        BasicLogger.debug(1, "of error terms    : {}", errorTermStatistics.getVariance());

        /*
         * Any/all financial markets experience periods of turbulence with higher volatility than otherwise.
         * To model that we cannot simply have 1 fixed number to describe the volatility of a time series
         * spanning almost 60 years. We need something else.
         */

        GARCH model = GARCH.newInstance(1, 1);

        try (TextLineWriter writer = TextLineWriter.of(OUTPUT)) {

            // CSV line builder - to help write CSV files
            CSVLineBuilder lineBuilder = writer.newCSVLineBuilder(ASCII.HT);

            lineBuilder.append("Day").append("Error Term").append("Standard Deviation").write();

            /*
             * Looping through the error term series. For each item update the GARCH model and write the error
             * term and the estimated standard deviation to file. The contents of that file is then used to
             * generate the chart in the blog post.
             */

            for (int i = 0; i < errorTerms.size(); i++) {

                double standardDeviation = model.getStandardDeviation(); // estimated std dev
                double value = errorTerms.value(i); // actual value/error (the mean is 0.0)

                lineBuilder.append(i).append(value).append(standardDeviation).write();

                model.update(value); // update model with actual value
            }

        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }

        /*
         * We used weekly values. To annualize volatility it needs to be scaled.
         */
        double annualizer = Math.sqrt(52);

        BasicLogger.debug();
        BasicLogger.debug("Volatility (annualized)");
        BasicLogger.debug(1, "constant (homoscedasticity): {}", StandardType.PERCENT.format(annualizer * errorTermStatistics.getStandardDeviation()));
        BasicLogger.debug(1, "of GARCH model (current/latest value): {}", StandardType.PERCENT.format(annualizer * model.getStandardDeviation()));

        /*
         * We can also use the GARCH model to simulate future scenarios.
         */

        int numberOfScenarios = 100;
        int numberOfProcessSteps = 52;
        double processStepSize = 1.0;

        /*
         * This will simulate 100 scenarios, stepping/incrementing the process 52 times, 1 week at the time.
         */

        SimulationResults simulationResults = StationaryNormalProcess.of(model).simulate(numberOfScenarios, numberOfProcessSteps, processStepSize);

    }

}
