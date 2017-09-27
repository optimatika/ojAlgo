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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CookieTest {

    /**
     * Definitions copied from HttpCookie
     */
    private final static String[] COOKIE_DATE_FORMATS = { "EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'", "EEE',' dd MMM yyyy HH:mm:ss 'GMT'",
            "EEE MMM dd yyyy HH:mm:ss 'GMT'Z", "EEE',' dd-MMM-yy HH:mm:ss 'GMT'", "EEE',' dd MMM yy HH:mm:ss 'GMT'", "EEE MMM dd yy HH:mm:ss 'GMT'Z" };

    static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    public static void main(final String[] args) {

        try {

            final ConsoleHandler handler = new ConsoleHandler();
            handler.setLevel(Level.FINE);

            final Logger logger = Logger.getLogger("");
            logger.setLevel(Level.FINE);
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);

            final CookieHandler manager = new CookieManager();
            CookieHandler.setDefault(manager);

            final URI uri = new URI("https", "finance.yahoo.com", "/quote/AAPL", null);
            final URL url = uri.toURL();

            final URLConnection connection = url.openConnection();
            final InputStream stream = connection.getInputStream();
            final Reader reader = new InputStreamReader(stream);
            final BufferedReader buffered = new BufferedReader(reader);

            while (buffered.readLine() != null) {
            }

            // manager.getCookieStore().getCookies().forEach(cookie -> System.out.println("Cookie store entry: " + cookie.toString()));

            // Setting a breakpoint inside HttpCookie.parse(...) you can see that the input is a string like this
            final String header = "B=5i0hk85ck33v2&b=3&s=4v; expires=Wed, 14-Jun-2018 19:34:58 GMT; path=/; domain=.yahoo.com";
            final List<HttpCookie> cookies = HttpCookie.parse(header);
            cookies.forEach(cookie -> {
                System.out.println("Explicitly created cookie: " + cookie.toString());
                System.out.println("\twith max age = " + cookie.getMaxAge());
            });

            // 1 cookie was craeted, but with max age == 0

            final String timestamp = "Wed, 14-Jun-2018 19:34:58 GMT";

            final CookieTest test = new CookieTest();
            test.expiryDate2DeltaSeconds(timestamp);

            for (final String pattern : COOKIE_DATE_FORMATS) {
                final SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
                try {
                    System.out.println(format.parse(timestamp).toString());
                } catch (final ParseException exception) {
                    System.out.println("Failed!");
                }
            }

        } catch (URISyntaxException | IOException xcptn) {
            xcptn.printStackTrace();
        }
    }

    long whenCreated = System.currentTimeMillis();

    public CookieTest() {
        super();
    }

    long expiryDate2DeltaSeconds(final String dateString) {
        final Calendar cal = new GregorianCalendar(GMT);
        for (int i = 0; i < COOKIE_DATE_FORMATS.length; i++) {
            final SimpleDateFormat df = new SimpleDateFormat(COOKIE_DATE_FORMATS[i], Locale.US);
            cal.set(1970, 0, 1, 0, 0, 0);
            df.setTimeZone(GMT);
            df.setLenient(true);
            df.set2DigitYearStart(cal.getTime());
            try {
                final Date tmpParse = df.parse(dateString);
                cal.setTime(tmpParse);
                if (!COOKIE_DATE_FORMATS[i].contains("yyyy")) {
                    // 2-digit years following the standard set
                    // out it rfc 6265
                    int year = cal.get(Calendar.YEAR);
                    year %= 100;
                    if (year < 70) {
                        year += 2000;
                    } else {
                        year += 1900;
                    }
                    cal.set(Calendar.YEAR, year);
                }
                return (cal.getTimeInMillis() - whenCreated) / 1000;
            } catch (final Exception e) {
                // Ignore, try the next date format
            }
        }
        return 0;
    }

}
