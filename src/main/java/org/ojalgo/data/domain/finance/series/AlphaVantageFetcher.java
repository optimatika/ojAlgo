package org.ojalgo.data.domain.finance.series;

import java.io.Reader;

import org.ojalgo.netio.ResourceLocator;
import org.ojalgo.type.CalendarDateUnit;

/**
 * @author stefanvanegmond
 */
public class AlphaVantageFetcher implements DataFetcher {

    private final CalendarDateUnit myResolution;
    private final ResourceLocator myResourceLocator;
    private final String mySymbol;

    public AlphaVantageFetcher(final String symbol, final CalendarDateUnit resolution, final String apiKey, boolean fullOutputSize) {

        super();

        mySymbol = symbol;
        myResolution = resolution;

        myResourceLocator = new ResourceLocator().host("www.alphavantage.co").path("/query");

        switch (resolution) {
        case MONTH:
            myResourceLocator.query("function", "TIME_SERIES_MONTHLY_ADJUSTED");
            break;
        case WEEK:
            myResourceLocator.query("function", "TIME_SERIES_WEEKLY_ADJUSTED");
            break;
        default:
            myResourceLocator.query("function", "TIME_SERIES_DAILY_ADJUSTED");
            break;
        }
        myResourceLocator.query("symbol", symbol);
        myResourceLocator.query("apikey", apiKey);
        myResourceLocator.query("datatype", "csv");
        if (fullOutputSize && (resolution == CalendarDateUnit.DAY) && !"demo".equals(apiKey)) {
            myResourceLocator.query("outputsize", "full");
        }
    }

    public CalendarDateUnit getResolution() {
        return myResolution;
    }

    public Reader getStreamOfCSV() {
        return myResourceLocator.getStreamReader();
    }

    public String getSymbol() {
        return mySymbol;
    }

}
