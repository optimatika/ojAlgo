package org.ojalgo.data.domain.finance.series;

import java.io.Reader;

import org.ojalgo.netio.ResourceLocator;
import org.ojalgo.type.CalendarDateUnit;

/**
 * This service has been moved/renamed/repackaged – this {@link DataFetcher} no longer works. Looks to me as
 * if they still offer a historical data download service – possibly even a free alternative – but this
 * fetcher needs to be re-implemented.
 * <p>
 * https://iexcloud.io
 *
 * @author stefanvanegmond
 * @deprecated It needs to be updated to function...
 */
@Deprecated
public class IEXTradingFetcher implements DataFetcher {

    private final ResourceLocator myResourceLocator;
    private final String mySymbol;

    /**
     * Maximum of 5 years data
     *
     * @param symbol Symbol of stock
     */
    public IEXTradingFetcher(final String symbol) {

        super();

        mySymbol = symbol;

        myResourceLocator = new ResourceLocator().host("cloud.iexapis.com").path("/1.0/stock/" + symbol + "/chart/5y").query("format", "csv");
    }

    /**
     * This will always be by day.
     *
     * @see org.ojalgo.data.domain.finance.series.DataFetcher#getResolution()
     */
    public CalendarDateUnit getResolution() {
        return CalendarDateUnit.DAY;
    }

    public Reader getStreamOfCSV() {
        return myResourceLocator.getStreamReader();
    }

    public String getSymbol() {
        return mySymbol;
    }
}
