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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A (CSV) parser interface. Could theoretically parse anything, but is primarily aimed towards parsing
 * delimited text lines.
 * <p>
 * The default implementations are based on {@link TextLineReader}.
 *
 * @author apete
 */
@FunctionalInterface
public interface BasicParser<T> extends TextLineReader.Parser<T> {

    /**
     * Will parse this file, line by line, passing the reulting objects (1 per line) to the supplied consumer.
     *
     * @param file The CSV file to parse
     * @param skipHeader Should skip (1) header row/line
     * @param consumer The results consumer
     */
    default void parse(final File file, final boolean skipHeader, final Consumer<T> consumer) {

        try (TextLineReader supplier = TextLineReader.of(file)) {
            this.parse(supplier, skipHeader, consumer);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    /**
     * Will parse this file, line by line, passing the resulting objects (1 per line) to the supplied
     * consumer.
     *
     * @param file The CSV file to parse
     * @param consumer The results consumer
     */
    default void parse(final File file, final Consumer<T> consumer) {
        this.parse(file, false, consumer);
    }

    /**
     * @param reader The CSV data reader
     * @param skipHeader Should skip (1) header row/line
     * @param consumer The results consumer
     */
    default void parse(final Reader reader, final boolean skipHeader, final Consumer<T> consumer) {

        try (TextLineReader supplier = new TextLineReader(reader)) {
            this.parse(supplier, skipHeader, consumer);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    /**
     * @param reader The CSV data reader
     * @param consumer The results consumer
     */
    default void parse(final Reader reader, final Consumer<T> consumer) {
        this.parse(reader, false, consumer);
    }

    default void parse(final String filePath, final boolean skipHeader, final Consumer<T> consumer) {
        this.parse(new File(filePath), skipHeader, consumer);
    }

    default void parse(final String filePath, final Consumer<T> consumer) {
        this.parse(filePath, false, consumer);
    }

    default void parse(final Supplier<String> lineSupplier, final boolean skipHeader, final Consumer<T> consumer) {

        String line = null;
        T item = null;

        if (skipHeader) {
            line = lineSupplier.get();
            line = null;
        }

        while ((line = lineSupplier.get()) != null) {
            if (TextLineReader.isLineOK(line) && (item = this.parse(line)) != null) {
                consumer.accept(item);
            }
        }
    }

    default void parse(final Supplier<String> lineSupplier, final Consumer<T> consumer) {
        this.parse(lineSupplier, false, consumer);
    }

}
