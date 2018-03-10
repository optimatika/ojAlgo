/*
 * Copyright 1997-2018 Optimatika
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.ojalgo.RecoverableCondition;

public interface BasicParser<T> {

    /**
     * Parse one line into some custom object.
     *
     * @param line The text line to parse
     * @return An object containing (referencing) the parsed data
     */
    public abstract T parse(String line) throws RecoverableCondition;

    /**
     * Will parse this file, line by line, passing the reulting objects (1 per line) to the supplied consumer.
     *
     * @param file The CSV file to parse
     * @param consumer The results consumer
     */
    default void parse(final File file, final Consumer<T> consumer) {

        if (file.exists() && file.isFile() && file.canRead()) {

            final String tmpPath = file.getPath();

            try {

                if (tmpPath.endsWith(".gz")) {
                    this.parse(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))), consumer);
                } else if (tmpPath.endsWith(".zip")) {
                    this.parse(new InputStreamReader(new ZipInputStream(new FileInputStream(file))), consumer);
                } else {
                    this.parse(new InputStreamReader(new FileInputStream(file)), consumer);
                }

            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * @param reader The CSV data reader
     * @param consumer The results consumer
     */
    default void parse(final Reader reader, final Consumer<T> consumer) {

        String tmpLine = null;
        T tmpItem = null;
        try (final BufferedReader tmpBufferedReader = new BufferedReader(reader)) {
            while ((tmpLine = tmpBufferedReader.readLine()) != null) {
                try {
                    if ((tmpLine.length() > 0) && !tmpLine.startsWith("#") && ((tmpItem = this.parse(tmpLine)) != null)) {
                        consumer.accept(tmpItem);
                    }
                } catch (final RecoverableCondition xcptn) {
                    // Skip this line and try the next
                }
            }
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

}
