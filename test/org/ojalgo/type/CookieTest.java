package org.ojalgo.type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.poi.ss.formula.functions.T;

public class CookieTest {

    public CookieTest() {
        super();
    }

    public static void main(String[] args) {

        try {

            CookieManager handler = new CookieManager();
            CookieHandler.setDefault(handler);

            URI uri = new URI("https", "finance.yahoo.com", "/quote/AAPL", null);
            URL url = uri.toURL();

            final URLConnection connection = url.openConnection();
            final InputStream stream = connection.getInputStream();
            Reader reader = new InputStreamReader(stream);

            String tmpLine = null;
            T tmpItem = null;
            final BufferedReader tmpBufferedReader = new BufferedReader(reader);
            while ((tmpLine = tmpBufferedReader.readLine()) != null) {
            }

            handler.getCookieStore().getCookies().forEach(c -> System.out.println(c.toString()));

        } catch (URISyntaxException | IOException xcptn) {
            xcptn.printStackTrace();
        }
    }

}
