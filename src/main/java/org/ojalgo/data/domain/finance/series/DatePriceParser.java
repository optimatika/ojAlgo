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

import org.ojalgo.netio.DetectingParser;
import org.ojalgo.netio.LineSplittingParser;
import org.ojalgo.netio.TextLineReader;

/**
 * Will switch between any/all known parsers producing {@link DatePrice} subclasses. There is also a default
 * parser that assumes just 2 columns, no header, separated by whitespace.
 *
 * @author apete
 */
public final class DatePriceParser extends DetectingParser<DatePrice> {

    static final class DefaultParser implements TextLineReader.Parser<DatePrice> {

        private final LineSplittingParser myDelegate = new LineSplittingParser();

        public DatePrice parse(final String line) {
            String[] parsed = myDelegate.parse(line);
            return DatePrice.of(parsed[0], parsed[1]);
        }

    }

    public DatePriceParser() {

        super(new DefaultParser());

        this.addPotentialParser(YahooParser::testHeader, new YahooParser());

        this.addPotentialParser(AlphaVantageParser::testHeader, new AlphaVantageParser());

        this.addPotentialParser(IEXTradingParser::testHeader, new IEXTradingParser());
    }

}
