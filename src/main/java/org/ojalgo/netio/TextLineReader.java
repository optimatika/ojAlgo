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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.ojalgo.type.function.AutoSupplier;
import org.ojalgo.type.function.OperatorWithException;

public final class TextLineReader implements FromFileReader<String> {

    @FunctionalInterface
    public interface Parser<T> {

        /**
         * Parse one line into some custom object. Returning null indicates that parsing failed!
         *
         * @param line The text line to parse
         * @return An object containing (referencing) the parsed data
         */
        T parse(String line);

    }

    public static TextLineReader of(final File file) {
        return new TextLineReader(FromFileReader.input(file));
    }

    public static TextLineReader of(final File file, final OperatorWithException<InputStream> filter) {
        return new TextLineReader(filter.apply(FromFileReader.input(file)));
    }

    private final BufferedReader myReader;

    TextLineReader(final InputStream inputStream) {
        super();
        try {
            myReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        } catch (UnsupportedEncodingException cause) {
            throw new RuntimeException(cause);
        }
    }

    TextLineReader(final Reader delegate) {
        super();
        myReader = new BufferedReader(delegate);
    }

    public void close() throws IOException {
        myReader.close();
    }

    public String read() {
        try {
            return myReader.readLine();
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    public <T> AutoSupplier<T> withParser(final Parser<T> parser) {
        return AutoSupplier.mapped(this, parser::parse);
    }

}
