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
package org.ojalgo.data.domain.finance.series;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.ojalgo.netio.TextLineReader;
import org.ojalgo.netio.TextLineReader.Parser;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.function.AutoSupplier;

public interface DataFetcher {

    InputStream getInputStream();

    default <T> AutoSupplier<T> getReader(final Parser<T> parser) {
        TextLineReader reader = new TextLineReader(this.getInputStream());
        return reader.withFilteredParser(parser);
    }

    /**
     * @return Typically DAY(ly), WEEK(ly) or MONTH(ly)
     */
    CalendarDateUnit getResolution();

    /**
     * @return A stream reader that can be sent to a CSV parser
     * @deprecated v52 Use {@link #getInputStream()} or {@link #getReader(Parser)} instead.
     */
    @Deprecated
    default Reader getStreamOfCSV() {
        return new InputStreamReader(this.getInputStream());
    }

    /**
     * @return Data identifier
     */
    String getSymbol();

}
