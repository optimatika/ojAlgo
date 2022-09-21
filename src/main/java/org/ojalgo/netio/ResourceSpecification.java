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
        if (myPort != other.myPort) {
            return false;
        }
        if (myQuery == null) {
            if (other.myQuery != null) {
                return false;
            }
        } else if (!myQuery.equals(other.myQuery)) {
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
        result = prime * result + ((myFragment == null) ? 0 : myFragment.hashCode());
        result = prime * result + ((myHost == null) ? 0 : myHost.hashCode());
        result = prime * result + ((myPath == null) ? 0 : myPath.hashCode());
        result = prime * result + myPort;
        result = prime * result + ((myQuery == null) ? 0 : myQuery.hashCode());
        return prime * result + ((myScheme == null) ? 0 : myScheme.hashCode());
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
