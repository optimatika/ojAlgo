package org.ojalgo.data.domain.finance.series;

import java.time.LocalDate;

import org.ojalgo.netio.ASCII;
import org.ojalgo.netio.BasicParser;

/**
 * https://iextrading.com/developer/docs/#chart
 *
 * @author stefanvanegmond
 */
public class IEXTradingParser implements BasicParser<IEXTradingParser.Data> {

    public static final class Data extends DatePrice {

        public final double close;
        public final double high;
        public final double low;
        public final double open;
        public final double unadjustedVolume;
        public final double volume;

        public Data(final LocalDate date, final double open, final double high, final double low, final double close, final double volume,
                final double unadjustedVolume) {
            super(date);
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.volume = volume;
            this.unadjustedVolume = unadjustedVolume;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj) || !(obj instanceof Data)) {
                return false;
            }
            Data other = (Data) obj;
            if ((Double.doubleToLongBits(close) != Double.doubleToLongBits(other.close))
                    || (Double.doubleToLongBits(high) != Double.doubleToLongBits(other.high))
                    || (Double.doubleToLongBits(low) != Double.doubleToLongBits(other.low))
                    || (Double.doubleToLongBits(open) != Double.doubleToLongBits(other.open))) {
                return false;
            }
            if ((Double.doubleToLongBits(unadjustedVolume) != Double.doubleToLongBits(other.unadjustedVolume))
                    || (Double.doubleToLongBits(volume) != Double.doubleToLongBits(other.volume))) {
                return false;
            }
            return true;
        }

        @Override
        public double getPrice() {
            return close;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            long temp;
            temp = Double.doubleToLongBits(close);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(high);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(low);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(open);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(unadjustedVolume);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(volume);
            return prime * result + (int) (temp ^ (temp >>> 32));
        }

    }

    public static final IEXTradingParser INSTANCE = new IEXTradingParser();

    /**
     * Checks if the header matches what this parser can handle.
     */
    public static boolean testHeader(final String header) {

        String[] columns = header.split("" + ASCII.COMMA);

        int length = columns.length;
        if (length != 12) {
            return false;
        }

        String date = columns[0].trim();
        if (!"date".equalsIgnoreCase(date)) {
            return false;
        }

        String price = columns[4].trim();
        if (!"close".equalsIgnoreCase(price)) {
            return false;
        }

        return true;
    }

    public IEXTradingParser() {
        super();
    }

    @Override
    public IEXTradingParser.Data parse(final String line) {

        // date,open,high,low,close,volume,unadjustedVolume,change,changePercent,vwap,label,changeOverTime

        LocalDate date = null;
        double open = Double.NaN;
        double high = Double.NaN;
        double low = Double.NaN;
        double close = Double.NaN;
        double volume = Double.NaN;
        double unadjustedVolume = Double.NaN;

        try {

            int inclBegin = 0;
            int exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            String part = line.substring(inclBegin, exclEnd);
            date = LocalDate.parse(part);

            inclBegin = exclEnd + 1;
            exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            part = line.substring(inclBegin, exclEnd);
            try {
                open = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                open = Double.NaN;
            }

            inclBegin = exclEnd + 1;
            exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            part = line.substring(inclBegin, exclEnd);
            try {
                high = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                high = Double.NaN;
            }

            inclBegin = exclEnd + 1;
            exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            part = line.substring(inclBegin, exclEnd);
            try {
                low = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                low = Double.NaN;
            }

            inclBegin = exclEnd + 1;
            exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            part = line.substring(inclBegin, exclEnd);
            try {
                close = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                close = Double.NaN;
            }

            inclBegin = exclEnd + 1;
            exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            part = line.substring(inclBegin, exclEnd);
            try {
                volume = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                volume = Double.NaN;
            }

            inclBegin = exclEnd + 1;
            exclEnd = line.indexOf(ASCII.COMMA, inclBegin);
            part = line.substring(inclBegin, exclEnd);
            try {
                unadjustedVolume = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                unadjustedVolume = Double.NaN;
            }

        } catch (Exception cause) {

            date = null;
            close = Double.NaN;
        }

        if (date != null && Double.isFinite(close)) {
            // date,open,high,low,close,volume,unadjustedVolume,change,changePercent,vwap,label,changeOverTime
            return new Data(date, open, high, low, close, volume, unadjustedVolume);
        } else {
            return null;
        }
    }

}
