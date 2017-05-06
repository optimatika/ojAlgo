package org.ojalgo.netio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.function.Consumer;

import org.ojalgo.RecoverableCondition;

abstract class AbstractParser<T> implements BasicParser<T> {

    private transient BufferedReader myBufferedReader = null;

    AbstractParser() {
        super();
    }

    public final void parse(final Reader reader, final Consumer<T> consumer) {
        // A reimplementation of the default method from the BasicParser interface.
        // The only difference is that it keeps a reference to the BufferedReader to enable passing it to other methods.
        String tmpLine = null;
        T tmpItem = null;
        try (final BufferedReader tmpBufferedReader = new BufferedReader(reader)) {
            myBufferedReader = tmpBufferedReader;
            while ((tmpLine = tmpBufferedReader.readLine()) != null) {
                if ((tmpLine.length() > 0) && !tmpLine.startsWith("#") && ((tmpItem = this.parse(tmpLine, tmpBufferedReader)) != null)) {
                    consumer.accept(tmpItem);
                }
            }
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    public final T parse(final String line) throws RecoverableCondition {
        return this.parse(line, myBufferedReader);
    }

    abstract T parse(String line, BufferedReader reader);

}
