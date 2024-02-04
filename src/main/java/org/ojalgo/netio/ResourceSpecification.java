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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.ojalgo.ProgrammingError;
import org.ojalgo.netio.ResourceLocator.KeyedValues;

/**
 * ResourceSpecification - it's a URI/URL builder.
 *
 * @author apete
 */
final class ResourceSpecification {

    private String myFragment = null;
    private String myHost = null;
    private String myPath = "";
    private int myPort = -1; // -1 ==> undefined
    private final KeyedValues myQuery = new KeyedValues();
    private String myScheme = "https";

    ResourceSpecification() {
        super();
    }

    ResourceSpecification(final URI uri) {

        super();

        myScheme = uri.getScheme();
        myHost = uri.getHost();
        myPort = uri.getPort();
        myPath = uri.getPath();
        myQuery.parse(uri.getQuery());
    }

    ResourceSpecification(final URL uri) {

        super();

        myScheme = uri.getProtocol();
        myHost = uri.getHost();
        myPort = uri.getPort();
        myPath = uri.getPath();
        myQuery.parse(uri.getQuery());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ResourceSpecification)) {
            return false;
        }
        ResourceSpecification other = (ResourceSpecification) obj;
        if (myFragment == null) {
            if (other.myFragment != null) {
                return false;
            }
        } else if (!myFragment.equals(other.myFragment)) {
            return false;
        }
        if (myHost == null) {
            if (other.myHost != null) {
                return false;
            }
        } else if (!myHost.equals(other.myHost)) {
            return false;
        }
        if (myPath == null) {
            if (other.myPath != null) {
                return false;
            }
        } else if (!myPath.equals(other.myPath)) {
            return false;
        }
        if (myPort != other.myPort || !myQuery.equals(other.myQuery)) {
            return false;
        }
        if (myScheme == null) {
            if (other.myScheme != null) {
                return false;
            }
        } else if (!myScheme.equals(other.myScheme)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myFragment == null ? 0 : myFragment.hashCode());
        result = prime * result + (myHost == null ? 0 : myHost.hashCode());
        result = prime * result + (myPath == null ? 0 : myPath.hashCode());
        result = prime * result + myPort;
        result = prime * result + myQuery.hashCode();
        return prime * result + (myScheme == null ? 0 : myScheme.hashCode());
    }

    @Override
    public String toString() {
        return this.toURI().toString();
    }

    String getQuery() {
        if (myQuery.isEmpty()) {
            return null;
        } else {
            return myQuery.toString();
        }
    }

    String getQueryValue(final String key) {
        return myQuery.get(key);
    }

    String putQueryEntry(final String key, final String value) {
        ProgrammingError.throwIfNull(key);
        if (value != null) {
            return myQuery.put(key, value);
        } else {
            return myQuery.remove(key);
        }
    }

    String removeQueryEntry(final String key) {
        return myQuery.remove(key);
    }

    ResourceSpecification setFragment(final String fragment) {
        myFragment = fragment;
        return this;
    }

    ResourceSpecification setHost(final String host) {
        myHost = host;
        return this;
    }

    ResourceSpecification setPath(final String path) {
        ProgrammingError.throwIfNull(path);
        myPath = path;
        return this;
    }

    /**
     * The default (null) value is -1.
     */
    ResourceSpecification setPort(final int port) {
        myPort = port;
        return this;
    }

    ResourceSpecification setQuery(final String query) {
        myQuery.parse(query);
        return this;
    }

    /**
     * Protocol, the default value is "https"
     */
    ResourceSpecification setScheme(final String scheme) {
        myScheme = scheme;
        return this;
    }

    URI toURI() {
        try {
            return new URI(myScheme, null, myHost, myPort, myPath, this.getQuery(), myFragment);
        } catch (URISyntaxException cause) {
            throw new RuntimeException(cause);
        }
    }

    URL toURL() {
        try {
            return this.toURI().toURL();
        } catch (MalformedURLException cause) {
            throw new RuntimeException(cause);
        }
    }

}
