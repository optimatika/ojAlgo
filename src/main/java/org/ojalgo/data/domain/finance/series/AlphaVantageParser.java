package org.ojalgo.data.domain.finance.series;

import java.time.LocalDate;

import org.ojalgo.netio.ASCII;
import org.ojalgo.netio.BasicParser;

/**
 * https://www.alphavantage.co/documentation/
 *
 * @author stefanvanegmond
 */
public class AlphaVantageParser implements BasicParser<AlphaVantageParser.Data> {

    public static final class Data extends DatePrice {

        public final double adjustedClose;
        public final double close;
        public final double dividendAmount;
        public final double high;
        public final double low;
        public final double open;
        public final double volume;

        public Data(final LocalDate date, final double open, final double high, final double low, final double close, final double adjustedClose,
                final double volume, final double dividendAmount) {
            super(date);
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.adjustedClose = adjustedClose;
            this.volume = volume;
            this.dividendAmount = dividendAmount;
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
            if ((Double.doubleToLongBits(adjustedClose) != Double.doubleToLongBits(other.adjustedClose))
                    || (Double.doubleToLongBits(close) != Double.doubleToLongBits(other.close))
                    || (Double.doubleToLongBits(dividendAmount) != Double.doubleToLongBits(other.dividendAmount))
                    || (Double.doubleToLongBits(high) != Double.doubleToLongBits(other.high))) {
                return false;
            }
            if ((Double.doubleToLongBits(low) != Double.doubleToLongBits(other.low)) || (Double.doubleToLongBits(open) != Double.doubleToLongBits(other.open))
                    || (Double.doubleToLongBits(volume) != Double.doubleToLongBits(other.volume))) {
                return false;
            }
            return true;
        }

        @Override
        public double getPrice() {
            return adjustedClose;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            long temp;
            temp = Double.doubleToLongBits(adjustedClose);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(close);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(dividendAmount);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(high);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(low);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(open);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(volume);
            return prime * result + (int) (temp ^ (temp >>> 32));
        }

    }

    public static final AlphaVantageParser INSTANCE = new AlphaVantageParser();

    /**
     * Checks if the header matches what this parser can handle.
     */
    public static boolean testHeader(final String header) {

        String[] columns = header.split("" + ASCII.COMMA);

        int length = columns.length;
        if (((length != 8) && (length != 9))) {
            return false;
        }

        String date = columns[0].trim();
        if (!"timestamp".equalsIgnoreCase(date)) {
            return false;
        }

        String price = columns[5].trim();
        if ((!"adjusted_close".equalsIgnoreCase(price) && !"adjusted close".equalsIgnoreCase(price))) {
            return false;
        }

        return true;
    }

    public AlphaVantageParser() {
        super();
    }

    @Override
    public AlphaVantageParser.Data parse(final String line) {

        // timestamp,open,high,low,close,adjusted_close,volume,dividend_amount,split_coefficient
        // timestamp,open,high,low,close,adjusted close,volume,dividend amount

        LocalDate date = null;
        double open = Double.NaN;
        double high = Double.NaN;
        double low = Double.NaN;
        double close = Double.NaN;
        double adjustedClose = Double.NaN;
        double volume = Double.NaN;
        double dividendAmount = Double.NaN;

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
                adjustedClose = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                adjustedClose = Double.NaN;
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
            if (exclEnd == -1) {
                part = line.substring(inclBegin);
            } else {
                part = line.substring(inclBegin, exclEnd);
            }
            try {
                dividendAmount = Double.parseDouble(part);
            } catch (final NumberFormatException ex) {
                dividendAmount = Double.NaN;
            }

        } catch (Exception cause) {

            date = null;
            adjustedClose = Double.NaN;
        }

        if (date != null && Double.isFinite(adjustedClose)) {
            // timestamp,open,high,low,close,adjusted_close,volume,dividend_amount,split_coefficient
            // timestamp,open,high,low,close,adjusted close,volume,dividend amount
            return new Data(date, open, high, low, close, adjustedClose, volume, dividendAmount);
        } else {
            return null;
        }
    }
}
