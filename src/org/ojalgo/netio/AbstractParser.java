package org.ojalgo.netio;

public abstract class AbstractParser {

    public static class AlwaysQuoted<T> implements LineParserStrategy<T> {

    }

    public static interface LineParserStrategy<T> {

    }

    public static class NeverQuoted<T> implements LineParserStrategy<T> {

    }

    public static class SometimesQuoted<T> implements LineParserStrategy<T> {

    }

    AbstractParser() {
        super();
    }

}
