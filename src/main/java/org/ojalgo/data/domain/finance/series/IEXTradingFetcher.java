package org.ojalgo.data.domain.finance.series;

import java.io.InputStream;
import java.net.http.HttpResponse.BodyHandlers;

import org.ojalgo.netio.ServiceClient;
import org.ojalgo.netio.ServiceClient.Response;
import org.ojalgo.type.CalendarDateUnit;

/**
 * Fetch historical financial time series data from IEX Trading: https://exchange.iex.io / https://iexcloud.io
 * <p>
 * This service has been moved/renamed/repackaged – this {@link DataFetcher} no longer works. Looks to me as
 * if they still offer a historical data download service – possibly even a free alternative – but this
 * fetcher needs to be re-implemented.
 *
 * @author stefanvanegmond
 * @see https://iexcloud.io
 * @deprecated It needs to be updated to function...
 */
@Deprecated
public class IEXTradingFetcher implements DataFetcher {

    private final ServiceClient.Request myRequest;
    private final String mySymbol;

    /**
     * Maximum of 5 years data
     *
     * @param symbol Symbol of stock
     */
    public IEXTradingFetcher(final String symbol) {

        super();

        mySymbol = symbol;

        myRequest = ServiceClient.newRequest().host("cloud.iexapis.com").path("/1.0/stock/" + symbol + "/chart/5y").query("format", "csv");
    }

    public InputStream getInputStream() {
        Response<InputStream> response = myRequest.send(BodyHandlers.ofInputStream());
        if (response.isResponseOK()) {
            return response.getBody();
        } else {
            return InputStream.nullInputStream();
        }
    }

    /**
     * This will always be by day.
     *
     * @see org.ojalgo.data.domain.finance.series.DataFetcher#getResolution()
     */
    public CalendarDateUnit getResolution() {
        return CalendarDateUnit.DAY;
    }

    public String getSymbol() {
        return mySymbol;
    }

}
