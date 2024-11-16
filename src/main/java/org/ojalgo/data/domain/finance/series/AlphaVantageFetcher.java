package org.ojalgo.data.domain.finance.series;

import java.io.InputStream;
import java.net.http.HttpResponse.BodyHandlers;

import org.ojalgo.netio.ServiceClient;
import org.ojalgo.netio.ServiceClient.Response;
import org.ojalgo.type.CalendarDateUnit;

/**
 * All data downloaders/fetchers are deprecated. They will be removed in a future release, and most likely
 * they're already broken.
 * <P>
 * Fetch historical financial time series data from Alpha Vantage: https://www.alphavantage.co
 *
 * @see https://www.alphavantage.co
 * @author stefanvanegmond
 * @deprecated
 */
@Deprecated
public final class AlphaVantageFetcher implements DataFetcher {

    private final ServiceClient.Request myRequest;
    private final CalendarDateUnit myResolution;
    private final String mySymbol;

    public AlphaVantageFetcher(final String symbol, final CalendarDateUnit resolution, final String apiKey, final boolean fullOutputSize) {

        super();

        mySymbol = symbol;
        myResolution = resolution;

        myRequest = ServiceClient.newRequest().host("www.alphavantage.co").path("/query");

        switch (resolution) {
            case MONTH:
                myRequest.query("function", "TIME_SERIES_MONTHLY_ADJUSTED");
                break;
            case WEEK:
                myRequest.query("function", "TIME_SERIES_WEEKLY_ADJUSTED");
                break;
            default:
                myRequest.query("function", "TIME_SERIES_DAILY_ADJUSTED");
                break;
        }
        myRequest.query("symbol", symbol);
        myRequest.query("apikey", apiKey);
        myRequest.query("datatype", "csv");
        if (fullOutputSize && resolution == CalendarDateUnit.DAY && !"demo".equals(apiKey)) {
            myRequest.query("outputsize", "full");
        }
    }

    @Override
    public InputStream getInputStream() {
        Response<InputStream> response = myRequest.send(BodyHandlers.ofInputStream());
        if (response.isResponseOK()) {
            return response.getBody();
        } else {
            return InputStream.nullInputStream();
        }
    }

    @Override
    public CalendarDateUnit getResolution() {
        return myResolution;
    }

    @Override
    public String getSymbol() {
        return mySymbol;
    }

}
