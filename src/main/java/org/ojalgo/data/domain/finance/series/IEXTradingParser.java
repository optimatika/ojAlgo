package org.ojalgo.data.domain.finance.series;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.ojalgo.netio.ASCII;
import org.ojalgo.netio.BasicParser;

/**
 * https://iextrading.com/developer/docs/#chart
 *
 * @author stefanvanegmond
 */
public class IEXTradingParser implements BasicParser<IEXTradingParser.Data> {

    public static final class Data extends DatePrice {

        public double close;
        public double high;
        public double low;
        public double open;
        public double unadjustedVolume;
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
            return close;
        }

    }

    public IEXTradingParser() {
        super();
    }

    @Override
    public IEXTradingParser.Data parse(final String line) {

        // date,open,high,low,close,volume,unadjustedVolume,change,changePercent,vwap,label,changeOverTime

        IEXTradingParser.Data retVal;

        try {

            int inclBegin = 0;
            int exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            String part = line.substring(inclBegin, exclEnd);
            retVal = new IEXTradingParser.Data(part);

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
                retVal.volume = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                retVal.volume = Double.NaN;
            }

            inclBegin = exclEnd + 1;
            exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            part = line.substring(inclBegin, exclEnd);
            try {
                retVal.unadjustedVolume = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                retVal.unadjustedVolume = Double.NaN;
            }

        } catch (final Exception exception) {

            retVal = null;
        }

        return retVal;
    }

}
