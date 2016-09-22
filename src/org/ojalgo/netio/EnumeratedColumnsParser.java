/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
import java.math.BigDecimal;
import java.util.function.Supplier;

import org.ojalgo.netio.EnumeratedColumnsParser.LineView;
import org.ojalgo.type.context.TypeContext;

public class EnumeratedColumnsParser extends AbstractParser<LineView> {

    public static class Builder implements Supplier<EnumeratedColumnsParser> {

        private char myDelimiter = ',';
        private final int myNumberOfColumns;
        private ParseStrategy myStrategy = ParseStrategy.RFC4180;

        Builder(final int numberOfColumns) {

            super();

            myNumberOfColumns = numberOfColumns;
        }

        public Builder delimiter(final char delimiter) {
            myDelimiter = delimiter;
            return this;
        }

        public EnumeratedColumnsParser get() {
            return new EnumeratedColumnsParser(myNumberOfColumns, myDelimiter, myStrategy);
        }

        public Builder strategy(final ParseStrategy quoted) {
            myStrategy = quoted;
            return this;
        }

    }

    public static abstract class LineView {

        final char delimiter;
        transient String line = null;

        @SuppressWarnings("unused")
        private LineView() {
            this(0, ',');
        }

        LineView(final int numberOfColumns, final char delimiter) {

            super();

            this.delimiter = delimiter;
        }

        public final double doubleValue(final Enum<?> column) {
            final String tmpStringValue = this.get(column.ordinal());
            if ((tmpStringValue != null) && (tmpStringValue.length() > 0)) {
                return Double.parseDouble(tmpStringValue);
            } else {
                return Double.NaN;
            }
        }

        public final double floatValue(final Enum<?> column) {
            final String tmpStringValue = this.get(column.ordinal());
            if ((tmpStringValue != null) && (tmpStringValue.length() > 0)) {
                return Float.parseFloat(tmpStringValue);
            } else {
                return Float.NaN;
            }
        }

        public final String get(final Enum<?> column) {
            return this.get(column.ordinal());
        }

        public final <P> P get(final Enum<?> column, final TypeContext<P> typeContext) {
            final String tmpStringValue = this.get(column.ordinal());
            if ((tmpStringValue != null) && (tmpStringValue.length() > 0)) {
                return typeContext.parse(tmpStringValue);
            } else {
                return null;
            }
        }

        public abstract String get(final int column);

        public final long intValue(final Enum<?> column) {
            final String tmpStringValue = this.get(column.ordinal());
            if ((tmpStringValue != null) && (tmpStringValue.length() > 0)) {
                return Integer.parseInt(tmpStringValue);
            } else {
                return 0;
            }
        }

        public boolean isLineOK() {
            return (line != null) && (line.length() > 0);
        }

        public final long longValue(final Enum<?> column) {
            final String tmpStringValue = this.get(column.ordinal());
            if ((tmpStringValue != null) && (tmpStringValue.length() > 0)) {
                return Long.parseLong(tmpStringValue);
            } else {
                return 0L;
            }
        }

        public final BigDecimal toBigDecimal(final Enum<?> column) {
            final String tmpStringValue = this.get(column.ordinal());
            if ((tmpStringValue != null) && (tmpStringValue.length() > 0)) {
                return new BigDecimal(tmpStringValue);
            } else {
                return null;
            }
        }

        abstract void index(final String line, BufferedReader bufferedReader);

    }

    public static enum ParseStrategy {

        /**
         * Simplest possible. Just delimited data. No quoting or any special characters...
         */
        FAST {

            @Override
            public LineView make(final int numberOfColumns, final char delimiter) {
                return new FastViewStrategy(numberOfColumns, delimiter);
            }
        },

        /**
         * Same as {@link #FAST} but assumes values are quoted (they have to be, ALL of them).
         */
        QUOTED() {

            @Override
            public LineView make(final int numberOfColumns, final char delimiter) {
                return new QuotedViewStrategy(numberOfColumns, delimiter);
            }
        },

        /**
         * As specified by the RFC4180 standard. (Not yet implemented!)
         */
        RFC4180 {

            @Override
            public LineView make(final int numberOfColumns, final char delimiter) {
                return new ViewStrategyRFC4180(numberOfColumns, delimiter);
            }
        };

        public abstract LineView make(int numberOfColumns, char delimiter);

    }

    static class FastViewStrategy extends LineView {

        private final int[] myIndices;

        FastViewStrategy(final int numberOfColumns, final char delimiter) {

            super(numberOfColumns, delimiter);

            myIndices = new int[numberOfColumns + 1];
        }

        @Override
        public String get(final int column) {
            return line.substring(myIndices[column], myIndices[column + 1] - 1);
        }

        @Override
        void index(final String line, final BufferedReader bufferedReader) {

            int tmpIndex = 0;
            int tmpPosition = 0;
            myIndices[tmpIndex] = tmpPosition;

            while ((tmpPosition = line.indexOf(delimiter, tmpPosition)) >= 0) {
                myIndices[++tmpIndex] = ++tmpPosition;
            }

            tmpPosition = line.length();
            myIndices[++tmpIndex] = tmpPosition;

            this.line = line;
        }
    }

    static class QuotedViewStrategy extends LineView {

        private final int[] myIndices;
        private final String mySplitter;

        QuotedViewStrategy(final int numberOfColumns, final char delimiter) {

            super(numberOfColumns, delimiter);

            myIndices = new int[numberOfColumns + 1];
            mySplitter = String.valueOf(new char[] { '"', delimiter, '"' });
        }

        @Override
        public String get(final int column) {
            return line.substring(myIndices[column], myIndices[column + 1] - 3);
        }

        @Override
        void index(final String line, final BufferedReader bufferedReader) {

            int tmpIndex = 0;
            int tmpPosition = 1;
            myIndices[tmpIndex] = tmpPosition;

            while ((tmpPosition = line.indexOf(mySplitter, tmpPosition)) >= 0) {
                tmpPosition += 3;
                myIndices[++tmpIndex] = tmpPosition;
            }

            tmpPosition = line.length() + 2;
            myIndices[++tmpIndex] = tmpPosition;

            this.line = line;
        }

    }

    static class ViewStrategyRFC4180 extends LineView {

        ViewStrategyRFC4180(final int numberOfColumns, final char delimiter) {

            super(numberOfColumns, delimiter);

        }

        @Override
        public String get(final int column) {
            return null;
        }

        @Override
        void index(final String line, final BufferedReader bufferedReader) {

            this.line = line;

            throw new RuntimeException("Not yet implemented!");
        }

    }

    public static EnumeratedColumnsParser.Builder make(final Class<? extends Enum<?>> columns) {
        return new EnumeratedColumnsParser.Builder(columns.getFields().length);
    }

    public static EnumeratedColumnsParser.Builder make(final int numberOfColumns) {
        return new EnumeratedColumnsParser.Builder(numberOfColumns);
    }

    private final LineView myLineView;

    @SuppressWarnings("unused")
    private EnumeratedColumnsParser() {
        this(0, ',', null);
    }

    EnumeratedColumnsParser(final int columns, final char delimiter, final ParseStrategy strategy) {

        super();

        myLineView = strategy.make(columns, delimiter);
    }

    @Override
    LineView parse(final String line, final BufferedReader bufferedReader) {

        myLineView.index(line, bufferedReader);

        return myLineView;
    }

}
