package org.ojalgo.type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CookieTest {

    private final static String[] COOKIE_DATE_FORMATS = { "EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'", "EEE',' dd MMM yyyy HH:mm:ss 'GMT'",
            "EEE MMM dd yyyy HH:mm:ss 'GMT'Z", "EEE',' dd-MMM-yy HH:mm:ss 'GMT'", "EEE',' dd MMM yy HH:mm:ss 'GMT'", "EEE MMM dd yy HH:mm:ss 'GMT'Z" };

    public CookieTest() {
        super();
    }

    public static void main(final String[] args) {

        try {

            final CookieManager handler = new CookieManager();
            CookieHandler.setDefault(handler);

            final URI uri = new URI("https", "finance.yahoo.com", "/quote/AAPL", null);
            // final URI uri = new URI("https", "www.google.com", "/", null);
            final URL url = uri.toURL();

            final URLConnection connection = url.openConnection();
            final InputStream stream = connection.getInputStream();
            final Reader reader = new InputStreamReader(stream);
            final BufferedReader buffered = new BufferedReader(reader);

            String tmpLine = null;
            while ((tmpLine = buffered.readLine()) != null) {
            }

            handler.getCookieStore().getCookies().forEach(c -> System.out.println(c.toString()));

            final String header = "B=5i0hk85ck33v2&b=3&s=4v; expires=Wed, 14-Jun-2018 19:34:58 GMT; path=/; domain=.yahoo.com";
            final List<HttpCookie> cookies = HttpCookie.parse(header);
            cookies.forEach(c -> {
                System.out.println("Cookie: " + c.toString());
                System.out.println("\twith max age = " + c.getMaxAge());
            });

            final String timestampString = "Wed, 14-Jun-2018 19:34:58 GMT";

            for (final String pattern : COOKIE_DATE_FORMATS) {
                final SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
                Date date;
                try {
                    date = format.parse(timestampString);
                    System.out.println(date.toString());
                } catch (final ParseException exception) {
                    exception.printStackTrace();
                }

            }

        } catch (URISyntaxException | IOException xcptn) {
            xcptn.printStackTrace();
        }
    }

}
