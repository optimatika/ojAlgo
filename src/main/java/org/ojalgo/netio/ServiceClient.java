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

import java.io.InputStream;
import java.net.Authenticator;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import org.ojalgo.ProgrammingError;
import org.ojalgo.netio.ResourceLocator.KeyedValues;
import org.ojalgo.netio.ResourceLocator.Method;

/**
 * Make http/https calls.
 * <p>
 * Based on {@link HttpClient} and somewhat designed after how {@link ResourceLocator} works.
 * <p>
 *
 * @author apete
 */
public final class ServiceClient {

    public static final class Request implements BasicLogger.Printable {

        private Object myBody = null;
        private final HttpRequest.Builder myBuilder;
        private final KeyedValues myForm = new KeyedValues();
        private ResourceLocator.Method myMethod = ResourceLocator.Method.GET;
        private transient HttpRequest myRequest = null;
        private final ResourceSpecification myResourceSpecification;
        private final ServiceClient.Session mySession;

        Request(final ServiceClient.Session session) {

            super();

            mySession = session;
            myBuilder = HttpRequest.newBuilder();
            myResourceSpecification = new ResourceSpecification();

            this.copy(session.getParameters());
        }

        Request(final ServiceClient.Session session, final URI uri) {

            super();

            mySession = session;
            myBuilder = HttpRequest.newBuilder(uri);
            myResourceSpecification = new ResourceSpecification(uri);

            this.copy(session.getParameters());
        }

        public ServiceClient.Request body(final Object body) {
            myBody = body;
            myForm.clear();
            myRequest = null;
            return this;
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
            if (myBody == null) {
                if (other.myBody != null) {
                    return false;
                }
            } else if (!myBody.equals(other.myBody)) {
                return false;
            }
            if (myBuilder == null) {
                if (other.myBuilder != null) {
                    return false;
                }
            } else if (!myBuilder.equals(other.myBuilder)) {
                return false;
            }
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

        public ServiceClient.Request expectContinue(final boolean enable) {
            myBuilder.expectContinue(enable);
            return this;
        }

        public ServiceClient.Request form(final String form) {
            myForm.parse(form);
            myBody = myForm;
            myRequest = null;
            return this;
        }

        public ServiceClient.Request form(final String key, final String value) {
            ProgrammingError.throwIfNull(key);
            if (value != null) {
                myForm.put(key, value);
            } else {
                myForm.remove(key);
            }
            myBody = myForm;
            myRequest = null;
            return this;
        }

        public ServiceClient.Request fragment(final String fragment) {
            myResourceSpecification.setFragment(fragment);
            myRequest = null;
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
            result = prime * result + ((myBody == null) ? 0 : myBody.hashCode());
            result = prime * result + ((myBuilder == null) ? 0 : myBuilder.hashCode());
            result = prime * result + myForm.hashCode();
            result = prime * result + ((myMethod == null) ? 0 : myMethod.hashCode());
            result = prime * result + ((myResourceSpecification == null) ? 0 : myResourceSpecification.hashCode());
            return prime * result + ((mySession == null) ? 0 : mySession.hashCode());
        }

        public ServiceClient.Request header(final String name, final String value) {
            myBuilder.header(name, value);
            return this;
        }

        public ServiceClient.Request host(final String host) {
            myResourceSpecification.setHost(host);
            myRequest = null;
            return this;
        }

        public ServiceClient.Request method(final ResourceLocator.Method method) {
            myMethod = method;
            myRequest = null;
            return this;
        }

        public ServiceClient.Request method(final String method, final BodyPublisher bodyPublisher) {
            myBuilder.method(method, bodyPublisher);
            return this;
        }

        public ServiceClient.Request path(final String path) {
            myResourceSpecification.setPath(path);
            myRequest = null;
            return this;
        }

        /**
         * The default (null) value is -1.
         */
        public ServiceClient.Request port(final int port) {
            myResourceSpecification.setPort(port);
            myRequest = null;
            return this;
        }

        public void print(final BasicLogger receiver) {
            mySession.print(receiver);
            receiver.println("Request URI: {}", this.getURI());
            if (myForm.size() > 0) {
                receiver.println("Request form: {}", myForm);
            }
        }

        public ServiceClient.Request query(final String query) {
            myResourceSpecification.setQuery(query);
            myRequest = null;
            return this;
        }

        public ServiceClient.Request query(final String key, final String value) {
            myResourceSpecification.putQueryEntry(key, value);
            myRequest = null;
            return this;
        }

        /**
         * https or http ?
         */
        public ServiceClient.Request secure(final boolean secure) {
            if (secure) {
                myResourceSpecification.setScheme("https");
            } else {
                myResourceSpecification.setScheme("http");
            }
            myRequest = null;
            return this;
        }

        public <T> Response<T> send(final BodyHandler<T> responseBodyHandler) {
            return this.getSession().send(this, responseBodyHandler);
        }

        public ServiceClient.Request timeout(final Duration duration) {
            myBuilder.timeout(duration);
            return this;
        }

        @Override
        public String toString() {
            return this.getURI().toString();
        }

        public ServiceClient.Request version(final Version version) {
            myBuilder.version(version);
            return this;
        }

        private void copy(final KeyedValues sessionParameters) {
            for (Entry<String, String> entry : sessionParameters.entrySet()) {
                myBuilder.setHeader(entry.getKey(), entry.getValue());
            }
        }

        BodyPublisher body() {
            if (myBody != null) {
                Class<? extends Object> bodyType = myBody.getClass();
                if (byte[].class.isAssignableFrom(bodyType)) {
                    return BodyPublishers.ofByteArray((byte[]) myBody);
                } else if (InputStream.class.isAssignableFrom(bodyType)) {
                    return BodyPublishers.ofInputStream(() -> (InputStream) myBody);
                } else {
                    return BodyPublishers.ofString(myBody.toString());
                }
            } else {
                return BodyPublishers.noBody();
            }
        }

        HttpRequest getRequest() {

            if (myRequest == null) {

                myBuilder.uri(this.getURI());

                BodyPublisher body = this.body();

                switch (myMethod) {
                case DELETE:
                    myBuilder.DELETE();
                    break;
                case GET:
                    myBuilder.GET();
                    break;
                case POST:
                    myBuilder.POST(body);
                    break;
                case PUT:
                    myBuilder.PUT(body);
                    break;
                default:
                    myBuilder.method(myMethod.name(), body);
                    break;
                }

                myRequest = myBuilder.build();
            }

            return myRequest;
        }

        ServiceClient.Session getSession() {
            return mySession;
        }

        URI getURI() {
            return myResourceSpecification.toURI();
        }

    }

    /**
     * This is actually a wrapper of a future response. The only things you can do without waiting/blocking
     * for the actual response is {@link Response#isDone()} and {@link Response#cancel()}.
     *
     * @author apete
     */
    public static final class Response<T> implements BasicLogger.Printable {

        private final CompletableFuture<HttpResponse<T>> myFutureResponse;
        private final ServiceClient.Session mySession;

        Response(final ServiceClient.Request request, final CompletableFuture<HttpResponse<T>> response) {

            super();

            mySession = request.getSession();
            myFutureResponse = response;
        }

        public boolean cancel() {
            return myFutureResponse.cancel(true);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Response)) {
                return false;
            }
            Response other = (Response) obj;
            if (!myFutureResponse.equals(other.myFutureResponse)) {
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

        public T getBody() {
            return this.getResponse().body();
        }

        public HttpHeaders getHeaders() {
            return this.getResponse().headers();
        }

        public Optional<HttpResponse<T>> getPreviousResponse() {
            return this.getResponse().previousResponse();
        }

        /**
         * Will recreate the request that resulted in the final response. If there has been one or more
         * redirects, then this is NOT the same as the original request.
         */
        public HttpRequest getRequest() {
            return this.getResponse().request();
        }

        /**
         * @return The http response status code, or -1 if this is not http/hhtps or if the code cannot be
         *         discerned from the response
         */
        public int getStatusCode() {
            return this.getResponse().statusCode();
        }

        public URI getURI() {
            return this.getResponse().uri();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((myFutureResponse == null) ? 0 : myFutureResponse.hashCode());
            return prime * result + ((mySession == null) ? 0 : mySession.hashCode());
        }

        public boolean isDone() {
            return myFutureResponse.isDone();
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
            receiver.println("Response headers: {}", this.getResponse().headers().map());
            receiver.println("<Recreated>");
            // TODO  this.getRequest().print(receiver);
            receiver.println("</Recreated>");
            mySession.print(receiver);
        }

        @Override
        public String toString() {
            return myFutureResponse.toString();
        }

        HttpResponse<T> getResponse() {
            try {
                return myFutureResponse.get();
            } catch (InterruptedException | ExecutionException cause) {
                throw new RuntimeException(cause);
            }
        }

    }

    /**
     * When you need to make a sequence of calls maintaining some state inbetween calls.
     *
     * @author apete
     */
    public static final class Session implements BasicLogger.Printable {

        private final HttpClient myClient;
        private final CookieManager myCookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        private final KeyedValues myParameters = new KeyedValues(ResourceLocator.DEFAULTS.getValues());

        Session(final HttpClient.Builder builder) {

            super();

            builder.cookieHandler(myCookieManager);

            myClient = builder.build();
        }

        /**
         * @see #parameter(String, String)
         */
        public String getParameter(final String key) {
            return myParameters.get(key);
        }

        public Request newRequest() {
            return new Request(this);
        }

        public Request newRequest(final String url) {
            try {
                return new Request(this, new URI(url));
            } catch (URISyntaxException cause) {
                throw new ProgrammingError(cause);
            }
        }

        /**
         * Session parameters are transferred to requests as headers
         */
        public ServiceClient.Session parameter(final String key, final String value) {
            Objects.requireNonNull(key);
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

        HttpClient getClient() {
            return myClient;
        }

        KeyedValues getParameters() {
            return myParameters;
        }

        <T> Response<T> send(final Request request, final BodyHandler<T> responseBodyHandler) {
            HttpClient client = this.getClient();
            CompletableFuture<HttpResponse<T>> futureResponse = client.sendAsync(request.getRequest(), responseBodyHandler);
            return new Response<>(request, futureResponse);
        }

    }

    public static Response<String> get(final String url) {
        return ServiceClient.newRequest(url).method(Method.GET).send(BodyHandlers.ofString());
    }

    public static Request newRequest() {
        return ServiceClient.newSession().newRequest();
    }

    public static Request newRequest(final String url) {
        return ServiceClient.newSession().newRequest(url);
    }

    public static ServiceClient.Session newSession() {
        ServiceClient serviceClient = new ServiceClient();
        return serviceClient.getSession();
    }

    public static Response<String> post(final String url, final byte[] body) {
        return ServiceClient.newRequest(url).method(Method.POST).body(body).send(BodyHandlers.ofString());
    }

    public static Response<String> post(final String url, final KeyedValues body) {
        return ServiceClient.newRequest(url).method(Method.POST).body(body).send(BodyHandlers.ofString());
    }

    private final HttpClient.Builder myBuilder = HttpClient.newBuilder();

    public ServiceClient() {

        super();

        myBuilder.executor(ReaderWriterBuilder.executor());
    }

    public ServiceClient authenticator(final Authenticator authenticator) {
        myBuilder.authenticator(authenticator);
        return this;
    }

    public ServiceClient connectTimeout(final Duration duration) {
        myBuilder.connectTimeout(duration);
        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ServiceClient)) {
            return false;
        }
        ServiceClient other = (ServiceClient) obj;
        if (myBuilder == null) {
            if (other.myBuilder != null) {
                return false;
            }
        } else if (!myBuilder.equals(other.myBuilder)) {
            return false;
        }
        return true;
    }

    public ServiceClient executor(final Executor executor) {
        myBuilder.executor(executor);
        return this;
    }

    public ServiceClient followRedirects(final Redirect policy) {
        myBuilder.followRedirects(policy);
        return this;
    }

    public ServiceClient.Session getSession() {
        return new Session(myBuilder);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        return prime * result + ((myBuilder == null) ? 0 : myBuilder.hashCode());
    }

    public ServiceClient priority(final int priority) {
        myBuilder.priority(priority);
        return this;
    }

    public ServiceClient proxy(final ProxySelector proxySelector) {
        myBuilder.proxy(proxySelector);
        return this;
    }

    public ServiceClient sslContext(final SSLContext sslContext) {
        myBuilder.sslContext(sslContext);
        return this;
    }

    public ServiceClient sslParameters(final SSLParameters sslParameters) {
        myBuilder.sslParameters(sslParameters);
        return this;
    }

    public ServiceClient version(final Version version) {
        myBuilder.version(version);
        return this;
    }

}
