/*
 * Copyright 1997-2024 Optimatika
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.ojalgo.ProgrammingError;

/**
 * Locate/fetch resources such as csv, json or text/html.
 * <p>
 * Started out as something relatively simple built around {@link URL} (and related stuff in the java.net
 * package) but grew into a complete http client.
 * <p>
 * The newer {@link ServiceClient} is instead built around the {@link HttpClient} introduced with Java 11.
 * Think of {@link ServiceClient} as v2 of {@link ResourceLocator}, and what you should use for http/https
 * calls.
 * <p>
 * This class is not (yet) deprecated, but if it is further developed it will not be primarily as an
 * http/https client. The focus will then be on other {@link URL} based use cases.
 *
 * @author apete
 */
public final class ResourceLocator {

    public static class KeyedValues implements Map<String, String> {

        private final Set<String> myOrderedKeys = new LinkedHashSet<>();
        /**
         * Takes advantage of the fact that java.util.Properties support strings with unicode escape
         * sequences, and that it can delegate to default values.
         */
        private final Properties myValues;

        public KeyedValues() {
            super();
            myValues = new Properties();
        }

        KeyedValues(final Properties defaults) {
            super();
            myValues = new Properties(defaults);
        }

        public void clear() {
            myOrderedKeys.clear();
            myValues.clear();
        }

        public boolean containsKey(final Object key) {
            return myValues.containsKey(key);
        }

        public boolean containsValue(final Object value) {
            return myValues.containsValue(value);
        }

        public Set<Entry<String, String>> entrySet() {
            return myValues.entrySet().stream().map(e -> new Entry<String, String>() {

                public String getKey() {
                    return e.getKey().toString();
                }

                public String getValue() {
                    return e.getValue().toString();
                }

                public String setValue(final String value) {
                    String prev = e.getValue().toString();
                    e.setValue(value);
                    return prev;
                }
            }).collect(Collectors.toSet());
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || !(obj instanceof KeyedValues)) {
                return false;
            }
            KeyedValues other = (KeyedValues) obj;
            return Objects.equals(myOrderedKeys, other.myOrderedKeys) && Objects.equals(myValues, other.myValues);
        }

        public String get(final Object key) {
            return myValues.getProperty(key.toString());
        }

        @Override
        public int hashCode() {
            return Objects.hash(myOrderedKeys, myValues);
        }

        public boolean isEmpty() {
            return myValues.isEmpty();
        }

        public Set<String> keySet() {
            return myValues.stringPropertyNames();
        }

        /**
         * Will parse/split the query string into key-value pairs and store those.
         *
         * @param keysAndValues A query (or form) string
         */
        public void parse(final String keysAndValues) {
            if (keysAndValues != null) {
                String[] pairs = keysAndValues.split("&");
                for (int i = 0; i < pairs.length; i++) {
                    String pair = pairs[i];
                    int split = pair.indexOf("=");
                    String key = pair.substring(0, split);
                    String value = pair.substring(split + 1);
                    this.put(key, value);
                }
            }
        }

        public String put(final String key, final String value) {
            myOrderedKeys.add(key);
            return String.valueOf(myValues.setProperty(key, value));
        }

        public void putAll(final Map<? extends String, ? extends String> m) {
            myValues.putAll(m);
        }

        public String remove(final Object key) {
            myOrderedKeys.remove(key);
            Object removed = myValues.remove(key);
            return removed != null ? removed.toString() : null;
        }

        public int size() {
            return myValues.size();
        }

        /**
         * @return A query string like: key1=value1&key2=value2...
         */
        @Override
        public String toString() {
            return myOrderedKeys.stream().map(key -> key + "=" + myValues.getProperty(key)).collect(Collectors.joining("&"));
        }

        public Collection<String> values() {
            return myValues.values().stream().map(Object::toString).collect(Collectors.toList());
        }

        Set<String> getOrderedKeys() {
            return myOrderedKeys;
        }

        Properties getValues() {
            return myValues;
        }

    }

    public enum Method {
        DELETE, GET, HEAD, OPTIONS, POST, PUT, TRACE;
    }

    public static final class Request implements BasicLogger.Printable {

        private final KeyedValues myForm = new KeyedValues();
        private ResourceLocator.Method myMethod = ResourceLocator.Method.GET;
        private final ResourceSpecification myResourceSpecification;
        private final ResourceLocator.Session mySession;

        Request(final ResourceLocator.Session session) {
            super();
            mySession = session;
            myResourceSpecification = new ResourceSpecification();
        }

        Request(final ResourceLocator.Session session, final URL url) {
            super();
            mySession = session;
            myResourceSpecification = new ResourceSpecification(url);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Request)) {
                return false;
            }
            Request other = (Request) obj;
            if (!myForm.equals(other.myForm) || (myMethod != other.myMethod)) {
                return false;
            }
            if (myResourceSpecification == null) {
                if (other.myResourceSpecification != null) {
                    return false;
                }
            } else if (!myResourceSpecification.equals(other.myResourceSpecification)) {
                return false;
            }
            if (mySession == null) {
                if (other.mySession != null) {
                    return false;
                }
            } else if (!mySession.equals(other.mySession)) {
                return false;
            }
            return true;
        }

        public ResourceLocator.Request form(final String form) {
            myForm.parse(form);
            return this;
        }

        public ResourceLocator.Request form(final String key, final String value) {
            ProgrammingError.throwIfNull(key);
            if (value != null) {
                myForm.put(key, value);
            } else {
                myForm.remove(key);
            }
            return this;
        }

        public ResourceLocator.Request fragment(final String fragment) {
            myResourceSpecification.setFragment(fragment);
            return this;
        }

        public String getFormValue(final String key) {
            return myForm.get(key);
        }

        public String getQueryValue(final String key) {
            return myResourceSpecification.getQueryValue(key);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + myForm.hashCode();
            result = prime * result + ((myMethod == null) ? 0 : myMethod.hashCode());
            result = prime * result + ((myResourceSpecification == null) ? 0 : myResourceSpecification.hashCode());
            return prime * result + ((mySession == null) ? 0 : mySession.hashCode());
        }

        public ResourceLocator.Request host(final String host) {
            myResourceSpecification.setHost(host);
            return this;
        }

        public ResourceLocator.Request method(final Method method) {
            myMethod = method;
            return this;
        }

        public ResourceLocator.Request path(final String path) {
            myResourceSpecification.setPath(path);
            return this;
        }

        /**
         * The default (null) value is -1.
         */
        public ResourceLocator.Request port(final int port) {
            myResourceSpecification.setPort(port);
            return this;
        }

        public void print(final BasicLogger receiver) {
            mySession.print(receiver);
            receiver.println("Request URL: {}", this.toURL());
            if (myForm.size() > 0) {
                receiver.println("Request form: {}", myForm);
            }
        }

        public ResourceLocator.Request query(final String query) {
            myResourceSpecification.setQuery(query);
            return this;
        }

        public ResourceLocator.Request query(final String key, final String value) {
            myResourceSpecification.putQueryEntry(key, value);
            return this;
        }

        public Response response() {
            return new Response(this);
        }

        /**
         * Protocol The default value is "https"
         */
        public ResourceLocator.Request scheme(final String scheme) {
            myResourceSpecification.setScheme(scheme);
            return this;
        }

        @Override
        public String toString() {
            return this.toURL().toString();
        }

        private String query() {
            return myResourceSpecification.getQuery();
        }

        private URL toURL() {
            return myResourceSpecification.toURL();
        }

        void configure(final HttpURLConnection connection) {
            try {
                connection.setRequestMethod(myMethod.name());
            } catch (ProtocolException exception) {
                throw new ProgrammingError(exception);
            }
        }

        String form() {
            if (myForm.isEmpty()) {
                return null;
            } else {
                return myForm.toString();
            }
        }

        ResourceLocator.Session getSession() {
            return mySession;
        }

        URLConnection newConnection() {

            URLConnection retVal = null;
            try {
                CookieHandler.setDefault(mySession.getCookieManager());
                retVal = this.toURL().openConnection();
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
            return retVal;
        }

    }

    public static final class Response implements BasicLogger.Printable {

        private final URLConnection myConnection;
        private final ResourceLocator.Session mySession;

        private transient String myString;

        Response(final ResourceLocator.Request request) {

            super();

            mySession = request.getSession();

            myConnection = request.newConnection();

            if (myConnection instanceof HttpURLConnection) {
                request.configure((HttpURLConnection) myConnection);
            }

            String form = request.form();
            if ((form != null) && (form.length() > 0)) {
                myConnection.setDoOutput(true);
                try (DataOutputStream output = new DataOutputStream(myConnection.getOutputStream())) {
                    output.write(form.getBytes(UTF_8));
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }

        /**
         * Will recreate the request that resulted in the final response. If there has been one or more
         * redirects, then this is NOT the same as the original request.
         */
        public ResourceLocator.Request getRequest() {
            return new ResourceLocator.Request(mySession, myConnection.getURL());
        }

        /**
         * @return The http response status code, or -1 if this is not http/hhtps or if the code cannot be
         *         discerned from the response
         */
        public int getStatusCode() {
            if (myConnection instanceof HttpURLConnection) {
                try {
                    return ((HttpURLConnection) myConnection).getResponseCode();
                } catch (IOException exception) {
                    return -1;
                }
            } else {
                return -1;
            }
        }

        /**
         * Open connection and return an input stream reader. This method can only be called once on each
         * instance, and the {@link #toString()} and {@link #print(Printer)} methods delegate to this method.
         * (Those methods can be called multiple timea.)
         */
        public Reader getStreamReader() {
            return new InputStreamReader(this.getInputStream());
        }

        /**
         * @return true if the status (response) code is in [200,300)
         */
        public boolean isResponseOK() {
            int statusCode = this.getStatusCode();
            if ((200 <= statusCode) && (statusCode < 300)) {
                return true;
            } else {
                return false;
            }
        }

        public void print(final BasicLogger receiver) {
            receiver.println("Response body: {}", this.toString());
            receiver.println("Response headers: {}", myConnection.getHeaderFields());
            receiver.println("<Recreated>");
            this.getRequest().print(receiver);
            receiver.println("</Recreated>");
            mySession.print(receiver);
        }

        @Override
        public String toString() {

            if (myString == null) {

                StringBuilder builder = new StringBuilder();
                String line = null;
                try (BufferedReader reader = new BufferedReader(this.getStreamReader())) {
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                myString = builder.toString();
            }

            return myString;
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

    }

    public static final class Session implements BasicLogger.Printable {

        private final CookieManager myCookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        private final KeyedValues myParameters = new KeyedValues(DEFAULTS.getValues());

        Session() {
            super();
            CookieHandler.setDefault(myCookieManager);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Session)) {
                return false;
            }
            Session other = (Session) obj;
            if (!myCookieManager.equals(other.myCookieManager) || !myParameters.equals(other.myParameters)) {
                return false;
            }
            return true;
        }

        public String getParameterValue(final String key) {
            return myParameters.get(key);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + myCookieManager.hashCode();
            return prime * result + myParameters.hashCode();
        }

        public ResourceLocator.Session parameter(final String key, final String value) {
            if (value != null) {
                myParameters.put(key, value);
            } else {
                myParameters.remove(key);
            }
            return this;
        }

        public void print(final BasicLogger receiver) {
            receiver.println("Session parameters: {}", myParameters);
            receiver.println("Session cookies: {}", myCookieManager.getCookieStore().getCookies());
        }

        public Request request() {
            return new Request(this);
        }

        public Request request(final String url) {
            try {
                return new Request(this, new URL(url));
            } catch (MalformedURLException exception) {
                throw new ProgrammingError(exception);
            }
        }

        public void reset() {
            myParameters.clear();
            myCookieManager.getCookieStore().removeAll();
        }

        CookieManager getCookieManager() {
            return myCookieManager;
        }

    }

    /**
     * Default session parameters
     */
    public static ResourceLocator.KeyedValues DEFAULTS = new ResourceLocator.KeyedValues();

    static final String UTF_8 = StandardCharsets.UTF_8.name();

    public static ResourceLocator.Session session() {
        return new Session();
    }

    static String urldecode(final String encoded) {
        try {
            return URLDecoder.decode(encoded, UTF_8);
        } catch (UnsupportedEncodingException exception) {
            return null;
        }
    }

    static String urlencode(final String unencoded) {
        try {
            return URLEncoder.encode(unencoded, UTF_8);
        } catch (UnsupportedEncodingException exception) {
            return null;
        }
    }

    private transient ResourceLocator.Request myRequest = null;
    private final ResourceLocator.Session mySession = new Session();

    public ResourceLocator() {
        super();
    }

    public ResourceLocator(final String url) {
        super();
        myRequest = mySession.request(url);
    }

    public ResourceLocator form(final String key, final String value) {
        this.request().form(key, value);
        return this;
    }

    public ResourceLocator fragment(final String fragment) {
        this.request().fragment(fragment);
        return this;
    }

    /**
     * Open connection and return an input stream reader.
     */
    public Reader getStreamReader() {
        return this.response().getStreamReader();
    }

    public InputStream getInputStream() {
        return this.response().getInputStream();
    }

    public ResourceLocator host(final String host) {
        this.request().host(host);
        return this;
    }

    public ResourceLocator method(final Method method) {
        this.request().method(method);
        return this;
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

    public ResourceLocator query(final String key, final String value) {
        this.request().query(key, value);
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
            myRequest = mySession.request();
        }
        return myRequest;
    }

    private ResourceLocator.Response response() {
        return new Response(this.request());
    }

}
