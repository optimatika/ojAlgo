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
import java.net.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ojalgo.ProgrammingError;

/**
 * ResourceLocator - it's a URI/URL builder.
 *
 * @author apete
 */
public final class ResourceLocator {

    public static enum Method {
        DELETE, GET, HEAD, OPTIONS, POST, PUT, TRACE;
    }

    public static final class Request {

        private String myFragment = null;
        private String myHost = null;
        private ResourceLocator.Method myMethod = ResourceLocator.Method.GET;
        private String myPath = "";
        private int myPort = -1; // -1 ==> undefined
        private final Map<String, String> myQueryParameters = new LinkedHashMap<>();
        private String myScheme = "https";
        private final ResourceLocator.Session mySession;

        Request(ResourceLocator.Session session) {
            super();
            mySession = session;
        }

        public ResourceLocator.Request fragment(final String fragment) {
            myFragment = fragment;
            return this;
        }

        public ResourceLocator.Request host(final String host) {
            myHost = host;
            return this;
        }

        public ResourceLocator.Request method(final Method method) {
            myMethod = method;
            return this;
        }

        public ResourceLocator.Request parameter(final String key, final String value) {
            ProgrammingError.throwIfNull(key, value);
            myQueryParameters.put(key, value);
            return this;
        }

        public Map<String, String> parameters() {
            return myQueryParameters;
        }

        public ResourceLocator.Request path(final String path) {
            ProgrammingError.throwIfNull(path);
            myPath = path;
            return this;
        }

        /**
         * The default (null) value is -1.
         */
        public ResourceLocator.Request port(final int port) {
            myPort = port;
            return this;
        }

        public Response response() {
            return new Response(this);
        }

        /**
         * Protocol The default value is "https"
         */
        public ResourceLocator.Request scheme(final String scheme) {
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

        private URL toURL() {
            try {
                final URI uri = new URI(myScheme, null, myHost, myPort, myPath, this.query(), myFragment);
                return uri.toURL();
            } catch (final URISyntaxException | MalformedURLException xcptn) {
                throw new ProgrammingError(xcptn);
            }
        }

        void configure(HttpURLConnection connection) {
            try {
                connection.setRequestMethod(myMethod.name());
            } catch (ProtocolException exception) {
                throw new ProgrammingError(exception);
            }
        }

        ResourceLocator.Session getSession() {
            return mySession;
        }

        URLConnection newConnection() {

            URLConnection retVal = null;
            try {
                retVal = this.toURL().openConnection();
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
            return retVal;
        }

    }

    public static final class Response {

        private final URLConnection myConnection;

        Response(ResourceLocator.Request request) {

            super();

            myConnection = request.newConnection();

            if (myConnection instanceof HttpURLConnection) {
                request.configure((HttpURLConnection) myConnection);
            }

            try {
                Object obj = myConnection.getContent();
                obj.toString();
            } catch (IOException exception) {
                // TODO Auto-generated catch block
                exception.printStackTrace();
            }
        }

        /**
         * Open a connection and get the input stream.
         */
        public InputStream getInputStream() {
            InputStream retVal = null;
            try {
                retVal = myConnection.getInputStream();
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
            return retVal;
        }

        public Map<String, List<String>> getResponseHeaders() {
            return myConnection.getHeaderFields();
        }

        /**
         * Open connection and return an input stream reader.
         */
        public Reader getStreamReader() {
            return new InputStreamReader(this.getInputStream());
        }

    }

    public static final class Session {

        Session() {

            super();

        }

        public Request request() {
            return new Request(this);
        }

    }

    static {

        final CookieStore delegateCS = new CookieManager().getCookieStore();
        CookieStore store = new CookieStore() {

            public void add(final URI uri, final HttpCookie cookie) {
                if (cookie.getMaxAge() == 0L) {
                    cookie.setMaxAge(-1L);
                }
                delegateCS.add(uri, cookie);
            }

            public List<HttpCookie> get(final URI uri) {
                return delegateCS.get(uri);
            }

            public List<HttpCookie> getCookies() {
                return delegateCS.getCookies();
            }

            public List<URI> getURIs() {
                return delegateCS.getURIs();
            }

            public boolean remove(final URI uri, final HttpCookie cookie) {
                return delegateCS.remove(uri, cookie);
            }

            public boolean removeAll() {
                return delegateCS.removeAll();
            }

        };

        CookiePolicy policy = CookiePolicy.ACCEPT_ALL;

        CookieManager manager = new CookieManager(store, policy);

        CookieHandler.setDefault(manager);

    }

    public static Session session() {
        return new Session();
    }

    private transient ResourceLocator.Request myRequest = null;
    private transient ResourceLocator.Response myResponse = null;
    private final ResourceLocator.Session mySession = new Session();

    public ResourceLocator(final String host) {
        super();
        this.request().host(host);
    }

    public ResourceLocator cookies(final CookieHandler cookieHandler) {
        CookieHandler.setDefault(cookieHandler);
        return this;
    }

    public ResourceLocator fragment(final String fragment) {
        this.request().fragment(fragment);
        return this;
    }

    /**
     * Open a connection and get a stream reader.
     */
    public InputStream getInputStream() {
        return this.response().getInputStream();
    }

    /**
     * Open connection and return an input stream reader.
     */
    public Reader getStreamReader() {
        return this.response().getStreamReader();
    }

    /**
     * @deprecated v47
     */
    @Deprecated
    public URLConnection openConnection() {
        return this.request().newConnection();
    }

    public ResourceLocator parameter(final String key, final String value) {
        this.request().parameter(key, value);
        return this;
    }

    public Map<String, String> parameters() {
        return this.request().parameters();
    }

    public ResourceLocator path(final String path) {
        this.request().path(path);
        return this;
    }

    /**
     * The default (null) value is -1.
     */
    public ResourceLocator port(final int port) {
        this.request().port(port);
        return this;
    }

    /**
     * Protocol The default value is "https"
     */
    public ResourceLocator scheme(final String scheme) {
        this.request().scheme(scheme);
        return this;
    }

    @Override
    public String toString() {
        return this.request().toString();
    }

    private ResourceLocator.Request request() {
        if (myRequest == null) {
            myRequest = new Request(mySession);
        }
        return myRequest;
    }

    private ResourceLocator.Response response() {
        return new Response(this.request());
    }
}
