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
import java.io.InputStream;
import java.io.OutputStream;

import org.ojalgo.type.function.AutoConsumer;
import org.ojalgo.type.function.AutoSupplier;
import org.ojalgo.type.function.OperatorWithException;

public interface TextLineInterpreter<T> extends TextLineReader.Parser<T>, TextLineWriter.Formatter<T> {

    default AutoSupplier<T> newReader(final File file) {
        return TextLineReader.of(file).withParser(this);
    }

    default AutoSupplier<T> newReader(final File file, final OperatorWithException<InputStream> filter) {
        return TextLineReader.of(file, filter).withParser(this);
    }

    default AutoConsumer<T> newWriter(final File file) {
        return TextLineWriter.of(file).withFormatter(this);
    }

    default AutoConsumer<T> newWriter(final File file, final OperatorWithException<OutputStream> filter) {
        return TextLineWriter.of(file, filter).withFormatter(this);
    }

}
