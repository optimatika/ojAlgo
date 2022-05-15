/*
 * Copyright 1997-2022 Optimatika
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

    private final BufferedWriter myWriter;

    TextLineWriter(final OutputStream outputStream) {
        super();
        try {
            myWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        } catch (UnsupportedEncodingException cause) {
            throw new RuntimeException(cause);
        }
    }

    public void close() throws IOException {
        myWriter.close();
    }

    public <T> AutoConsumer<T> withFormatter(final Formatter<T> formatter) {
        return AutoConsumer.mapped(formatter::format, this);
    }

    public void write(final CharSequence itemToWrite) {
        try {
            myWriter.write(itemToWrite.toString());
            myWriter.newLine();
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

}
