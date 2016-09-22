package org.ojalgo.netio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.function.Consumer;

abstract class AbstractParser<T> implements BasicParser<T> {

    private transient BufferedReader myBufferedReader;

    AbstractParser() {
        super();
    }

    public final void parse(final Reader reader, final Consumer<T> consumer) {

        String tmpLine = null;
        T tmpItem = null;
        try (final BufferedReader tmpBufferedReader = new BufferedReader(reader)) {
            myBufferedReader = tmpBufferedReader;
            while ((tmpLine = tmpBufferedReader.readLine()) != null) {
                if ((tmpLine.length() > 0) && !tmpLine.startsWith("#") && ((tmpItem = this.parse(tmpLine)) != null)) {
                    consumer.accept(tmpItem);
                }
            }
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    public final T parse(String line) {
        return parse(line, myBufferedReader);
    }

    abstract T parse(String line, BufferedReader bufferedReader);

}
