package org.ojalgo.data.domain.finance.series;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.ojalgo.netio.ASCII;
import org.ojalgo.netio.BasicParser;

/**
 * https://www.alphavantage.co/documentation/
 *
 * @author stefanvanegmond
 */
public class AlphaVantageParser implements BasicParser<AlphaVantageParser.Data> {

    public static final class Data extends DatePrice {

        public double adjustedClose;
        public double close;
        public double dividendAmount;
        public double high;
        public double low;
        public double open;
        public double volume;

        Data(final CharSequence text) {
            super(text);
        }

        Data(final CharSequence text, final DateTimeFormatter formatter) {
            super(text, formatter);
        }

        Data(final LocalDate date) {
            super(date);
        }

        @Override
        public double getPrice() {
            return adjustedClose;
        }

    }

    public AlphaVantageParser() {
        super();
    }

    @Override
    public AlphaVantageParser.Data parse(final String line) {

        // timestamp,open,high,low,close,adjusted_close,volume,dividend_amount,split_coefficient
        // timestamp,open,high,low,close,adjusted close,volume,dividend amount
        // timestamp,open,high,low,close,adjusted close,volume,dividend amount

        Data retVal;

        try {

            int inclBegin = 0;
            int exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            String part = line.substring(inclBegin, exclEnd);
            retVal = new AlphaVantageParser.Data(part);

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
            exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            part = line.substring(inclBegin, exclEnd);
            try {
                retVal.volume = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                retVal.volume = Double.NaN;
            }

            inclBegin = exclEnd + 1;
            exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            if (exclEnd == -1) {
                part = line.substring(inclBegin);
            } else {
                part = line.substring(inclBegin, exclEnd);
            }
            try {
                retVal.dividendAmount = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                retVal.dividendAmount = Double.NaN;
            }

        } catch (final Exception exception) {

            retVal = null;
        }

        return retVal;
    }

}
