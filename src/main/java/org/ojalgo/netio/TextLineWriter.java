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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.ojalgo.type.function.AutoConsumer;
import org.ojalgo.type.function.OperatorWithException;

public final class TextLineWriter implements ToFileWriter<CharSequence> {

    /**
     * A reusable delimited "text line" builder. When writing CSV data this can help create the lines/rows.
     * It's backed by the {@link TextLineWriter} used to instantiate it. Just specify the delimiter, once, and
     * start creating lines/rows.
     */
    public static final class CSVLineBuilder {

        private final String myDelimiter;
        private final StringBuilder myTextLine = new StringBuilder();
        private final TextLineWriter myWriter;

        CSVLineBuilder(final TextLineWriter writer, final char delimiter) {
            this(writer, Character.toString(delimiter));
        }

        CSVLineBuilder(final TextLineWriter writer, final String delimiter) {
            super();
            myWriter = writer;
            myDelimiter = delimiter;
        }

        public CSVLineBuilder append(final boolean colVal) {
            this.delimit();
            myTextLine.append(colVal);
            return this;
        }

        public CSVLineBuilder append(final byte colVal) {
            this.delimit();
            myTextLine.append(Byte.toString(colVal));
            return this;
        }

        public CSVLineBuilder append(final char colVal) {
            this.delimit();
            myTextLine.append(colVal);
            return this;
        }

        public CSVLineBuilder append(final double colVal) {
            this.delimit();
            myTextLine.append(colVal);
            return this;
        }

        public CSVLineBuilder append(final float colVal) {
            this.delimit();
            myTextLine.append(colVal);
            return this;
        }

        public CSVLineBuilder append(final int colVal) {
            this.delimit();
            myTextLine.append(colVal);
            return this;
        }

        public CSVLineBuilder append(final long colVal) {
            this.delimit();
            myTextLine.append(colVal);
            return this;
        }

        public CSVLineBuilder append(final Object colVal) {
            this.delimit();
            myTextLine.append(colVal);
            return this;
        }

        public CSVLineBuilder append(final short colVal) {
            this.delimit();
            myTextLine.append(Short.toString(colVal));
            return this;
        }

        private void delimit() {
            if (myTextLine.length() > 0) {
                myTextLine.append(myDelimiter);
            }
        }

        /**
         * Write the line/row and reset the builder – ready to build the next line.
         */
        public void write() {
            myWriter.write(myTextLine);
            myTextLine.setLength(0);
        }

    }

    @FunctionalInterface
    public interface Formatter<T> {

        /**
         * Returning null indicates that formatting failed!
         */
        String format(T data);

    }

    public static TextLineWriter of(final File file) {
        return new TextLineWriter(ToFileWriter.output(file));
    }

    public static TextLineWriter of(final File file, final OperatorWithException<OutputStream> filter) {
        return new TextLineWriter(filter.apply(ToFileWriter.output(file)));
    }

    public static TextLineWriter of(final InMemoryFile file) {
        return new TextLineWriter(file.newOutputStream());
    }

    public static TextLineWriter of(final InMemoryFile file, final OperatorWithException<OutputStream> filter) {
        return new TextLineWriter(filter.apply(file.newOutputStream()));
    }

    private final BufferedWriter myWriter;

    public TextLineWriter(final OutputStream outputStream) {
        super();
        try {
            myWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        } catch (UnsupportedEncodingException cause) {
            throw new RuntimeException(cause);
        }
    }

    @Override
    public void close() throws IOException {
        myWriter.close();
    }

    /**
     * @see CSVLineBuilder
     */
    public CSVLineBuilder newCSVLineBuilder(final char delimiter) {
        return new CSVLineBuilder(this, delimiter);
    }

    /**
     * @see CSVLineBuilder
     */
    public CSVLineBuilder newCSVLineBuilder(final String delimiter) {
        return new CSVLineBuilder(this, delimiter);
    }

    public <T> AutoConsumer<T> withFormatter(final Formatter<T> formatter) {
        return AutoConsumer.mapped(formatter::format, this);
    }

    @Override
    public void write(final CharSequence itemToWrite) {
        try {
            myWriter.write(itemToWrite.toString());
            myWriter.newLine();
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

}
