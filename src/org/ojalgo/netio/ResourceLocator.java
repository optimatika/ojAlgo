/*
 * Copyright 1997-2018 Optimatika
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
package org.ojalgo.netio;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.ojalgo.ProgrammingError;

/**
 * ResourceLocator - it's a URI/URL builder.
 *
 * @author apete
 */
public final class ResourceLocator {

    public static final CookieManager DEFAULT_COOKIE_MANAGER = new CookieManager();

    private CookieHandler myCookieHandler = DEFAULT_COOKIE_MANAGER;
    private String myFragment = null;
    private final String myHost;
    private String myPath = "";
    private int myPort = -1; // -1 ==> undefined
    private final Map<String, String> myQueryParameters = new TreeMap<>();
    private String myScheme = "https";

    public ResourceLocator(final String host) {
        super();
        myHost = host;
    }

    public ResourceLocator cookies(final CookieHandler cookieHandler) {
        myCookieHandler = cookieHandler;
        return this;
    }

    public ResourceLocator fragment(final String fragment) {
        myFragment = fragment;
        return this;
    }

    /**
     * Open a connection and get a stream reader.
     */
    public InputStream getInputStream() {

        final URLConnection connection = this.openConnection();

        InputStream stream = null;
        try {
            stream = connection.getInputStream();
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
        return stream;
    }

    /**
     * Open connection and return an input stream reader.
     */
    public Reader getStreamReader() {
        return new InputStreamReader(this.getInputStream());
    }

    public URLConnection openConnection() {

        CookieHandler.setDefault(myCookieHandler);

        final URL url = this.toURL();

        URLConnection connection = null;
        try {
            connection = url.openConnection();
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
        return connection;
    }

    public ResourceLocator parameter(final String key, final String value) {
        ProgrammingError.throwIfNull(key, value);
        myQueryParameters.put(key, value);
        return this;
    }

    public Map<String, String> parameters() {
        return myQueryParameters;
    }

    public ResourceLocator path(final String path) {
        ProgrammingError.throwIfNull(path);
        myPath = path;
        return this;
    }

    /**
     * The default (null) value is -1.
     *
     * @return
     */
    public ResourceLocator port(final int port) {
        myPort = port;
        return this;
    }

    /**
     * Protocol The default value is "https"
     *
     * @return
     */
    public ResourceLocator scheme(final String scheme) {
        myScheme = scheme;
        return this;
    }

    @Override
    public String toString() {
        return this.toURL().toString();
    }

    private String query() {

        if (myQueryParameters.size() >= 1) {

            final StringBuilder retVal = new StringBuilder();

            Entry<String, String> tmpEntry;
            for (final Iterator<Entry<String, String>> tmpIter = myQueryParameters.entrySet().iterator(); tmpIter.hasNext();) {
                tmpEntry = tmpIter.next();
                retVal.append(tmpEntry.getKey());
                retVal.append('=');
                retVal.append(tmpEntry.getValue());
                retVal.append('&');
            }

            // Remove that last '&'
            retVal.setLength(retVal.length() - 1);

            return retVal.toString();

        } else {

            return null;
        }
    }

    URL toURL() {
        try {
            final URI uri = new URI(myScheme, null, myHost, myPort, myPath, this.query(), myFragment);
            return uri.toURL();
        } catch (final URISyntaxException | MalformedURLException xcptn) {
            throw new ProgrammingError(xcptn);
        }
    }

}
