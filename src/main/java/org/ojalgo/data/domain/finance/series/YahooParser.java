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
package org.ojalgo.data.domain.finance.series;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.ojalgo.netio.ASCII;
import org.ojalgo.netio.BasicParser;

/**
 * @author apete
 */
public class YahooParser implements BasicParser<YahooParser.Data> {

    public static final class Data extends DatePrice {

        public double adjustedClose;
        public double close;
        public double high;
        public double low;
        public double open;
        public double volume;

        public Data(final LocalDate date) {
            super(date);
        }

        Data(final CharSequence text) {
            super(text);
        }

        Data(final CharSequence text, final DateTimeFormatter formatter) {
            super(text, formatter);
        }

        @Override
        public double getPrice() {
            return adjustedClose;
        }

    }

    public YahooParser() {
        super();
    }

    @Override
    public YahooParser.Data parse(final String line) {

        // Date,Open,High,Low,Close,Adj Close,Volume

        Data retVal = null;

        try {

            int inclBegin = 0;
            int exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            String part = line.substring(inclBegin, exclEnd);
            retVal = new Data(part);

            inclBegin = exclEnd + 1;
            exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            part = line.substring(inclBegin, exclEnd);
            try {
                retVal.open = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                retVal.open = Double.NaN;
            }

            inclBegin = exclEnd + 1;
            exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            part = line.substring(inclBegin, exclEnd);
            try {
                retVal.high = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                retVal.high = Double.NaN;
            }

            inclBegin = exclEnd + 1;
            exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            part = line.substring(inclBegin, exclEnd);
            try {
                retVal.low = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                retVal.low = Double.NaN;
            }

            inclBegin = exclEnd + 1;
            exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            part = line.substring(inclBegin, exclEnd);
            try {
                retVal.close = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                retVal.close = Double.NaN;
            }

            inclBegin = exclEnd + 1;
            exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            part = line.substring(inclBegin, exclEnd);
            try {
                retVal.adjustedClose = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                retVal.adjustedClose = Double.NaN;
            }

            inclBegin = exclEnd + 1;
            part = line.substring(inclBegin);
            try {
                retVal.volume = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                retVal.volume = Double.NaN;
            }

        } catch (final Exception exception) {

            retVal = null;
        }

        return retVal;
    }

}
