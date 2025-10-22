/*
 * Copyright 1997-2025 Optimatika
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
package org.ojalgo.concurrent;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Options to control the child JVM process used by ProcessExecutorService. Immutable; use the builder to
 * construct instances.
 * <p>
 * Notes on inheritance:
 * <ul>
 * <li>Environment variables: a child {@link java.lang.ProcessBuilder} inherits the parent environment by
 * default. Any entries added via {@link Builder#env(String, String)} are applied on top of that.
 * <li>System properties: a child JVM does not automatically inherit arbitrary {@code -D} properties; only
 * those provided here are passed as {@code -Dkey=value}. Use the builder helpers to inherit selected
 * properties if needed.
 * <li>Classpath: by default the executor derives an effective classpath from test/main classpaths and build
 * output directories; specifying {@link Builder#classpath(String)} overrides that.
 * </ul>
 */
public final class ProcessOptions implements Serializable {

    public static final class Builder {

        private String myClasspath = System.getProperty("java.class.path");
        private boolean myEnableNativeAccessAllUnnamed = false;
        private final Map<String, String> myEnv = new HashMap<>();
        private final List<String> myJvmArgs = new ArrayList<>();
        private final Map<String, String> mySystemProperties = new HashMap<>();
        private Duration myTimeout = Duration.ZERO; // zero means no timeout
        private String myXmx = null; // e.g. "1G"; null means inherit

        public Builder addJvmArg(final String arg) {
            if (arg != null && !arg.isEmpty()) {
                myJvmArgs.add(arg);
            }
            return this;
        }

        public Builder addJvmArgs(final List<String> args) {
            if (args != null) {
                myJvmArgs.addAll(args);
            }
            return this;
        }

        public ProcessOptions build() {
            return new ProcessOptions(myJvmArgs, myEnv, myTimeout, myClasspath, myXmx, myEnableNativeAccessAllUnnamed, mySystemProperties);
        }

        public Builder classpath(final String cp) {
            if (cp != null) {
                myClasspath = cp;
            }
            return this;
        }

        public Builder enableNativeAccessAllUnnamed(final boolean enable) {
            myEnableNativeAccessAllUnnamed = enable;
            return this;
        }

        public Builder env(final String key, final String value) {
            if (key != null && value != null) {
                myEnv.put(key, value);
            }
            return this;
        }

        /**
         * Copy all current JVM system properties (as Strings) into these options so that the child JVM is
         * launched with the same {@code -D} set. Only String-typed keys/values are copied. Be cautious as
         * this may produce a long command line; prefer {@link #inheritSystemProperties(String...)} to
         * whitelist.
         */
        public Builder inheritSystemProperties() {
            Properties props = System.getProperties();
            for (Map.Entry<Object, Object> e : props.entrySet()) {
                Object k = e.getKey();
                Object v = e.getValue();
                if (k instanceof String && v instanceof String) {
                    mySystemProperties.put((String) k, (String) v);
                }
            }
            return this;
        }

        /**
         * Copy only the specified system properties from the current JVM into these options.
         */
        public Builder inheritSystemProperties(final String... keys) {
            if (keys != null) {
                for (String k : keys) {
                    if (k != null) {
                        String v = System.getProperty(k);
                        if (v != null) {
                            mySystemProperties.put(k, v);
                        }
                    }
                }
            }
            return this;
        }

        public Builder systemProperty(final String key, final String value) {
            if (key != null && value != null) {
                mySystemProperties.put(key, value);
            }
            return this;
        }

        public Builder timeout(final Duration t) {
            if (t != null) {
                myTimeout = t;
            }
            return this;
        }

        public Builder xmx(final String heap) {
            myXmx = heap;
            return this;
        }
    }

    public static final ProcessOptions DEFAULT = new Builder().build();

    private static final long serialVersionUID = 1L;

    public final String classpath;
    public final boolean enableNativeAccessAllUnnamed;
    public final Map<String, String> env; // unmodifiable
    public final List<String> jvmArgs; // unmodifiable
    public final Map<String, String> systemProperties; // unmodifiable
    public final Duration timeout; // zero => no timeout
    public final String xmx; // nullable

    private ProcessOptions(final List<String> jvmArgs, final Map<String, String> env, final Duration timeout, final String classpath, final String xmx,
            final boolean enableNativeAccessAllUnnamed, final Map<String, String> systemProperties) {

        super();

        this.jvmArgs = Collections.unmodifiableList(new ArrayList<>(jvmArgs));
        this.env = Collections.unmodifiableMap(new HashMap<>(env));
        this.timeout = timeout == null ? Duration.ZERO : timeout;
        this.classpath = classpath;
        this.xmx = xmx;
        this.enableNativeAccessAllUnnamed = enableNativeAccessAllUnnamed;
        this.systemProperties = Collections.unmodifiableMap(new HashMap<>(systemProperties));
    }
}