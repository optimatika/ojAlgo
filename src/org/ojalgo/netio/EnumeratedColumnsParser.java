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

import java.math.BigDecimal;
import java.util.function.Supplier;

import org.ojalgo.netio.EnumeratedColumnsParser.LineView;
import org.ojalgo.type.context.TypeContext;

public class EnumeratedColumnsParser implements BasicParser<LineView> {

    public static class Builder implements Supplier<EnumeratedColumnsParser> {

        private int myColumns = 0;
        private char myDelimiter = ',';
        private Quoted myQuoted = Quoted.NEVER;

        public Builder() {
            super();
        }

        public Builder columns(final Class<? extends Enum<?>> columns) {
            myColumns = columns.getFields().length;
            return this;
        }

        public Builder columns(final int columns) {
            myColumns = columns;
            return this;
        }

        public Builder delimiter(final char delimiter) {
            myDelimiter = delimiter;
            return this;
        }

        public EnumeratedColumnsParser get() {
            return new EnumeratedColumnsParser(myColumns, myDelimiter, myQuoted);
        }

        public Builder quoted(final Quoted quoted) {
            myQuoted = quoted;
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

        public final String get(final Enum<?> column) {
            return this.get(column.ordinal());
        }

        public final <P> P get(final Enum<?> column, final TypeContext<P> context) {
            final String tmpStringValue = this.get(column.ordinal());
            if ((tmpStringValue != null) && (tmpStringValue.length() > 0)) {
                return context.parse(tmpStringValue);
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

        abstract void index(final String line);

    }

    public static enum Quoted {

        ALWAYS() {

            @Override
            public LineView make(final int numberOfColumns, final char delimiter) {
                return new AlwaysQuoted(numberOfColumns, delimiter);
            }
        },
        NEVER {

            @Override
            public LineView make(final int numberOfColumns, final char delimiter) {
                return new NeverQuoted(numberOfColumns, delimiter);
            }
        },
        SOMETIMES {

            @Override
            public LineView make(final int numberOfColumns, final char delimiter) {
                return new SometimesQuoted(numberOfColumns, delimiter);
            }
        };

        public abstract LineView make(int numberOfColumns, char delimiter);

    }

    static class AlwaysQuoted extends LineView {

        private final int[] indices;
        private final String splitter;

        AlwaysQuoted(final int numberOfColumns, final char delimiter) {

            super(numberOfColumns, delimiter);

            indices = new int[numberOfColumns + 1];
            splitter = String.valueOf(new char[] { '"', delimiter, '"' });
        }

        @Override
        public String get(final int column) {
            return line.substring(indices[column], indices[column + 1] - 3);
        }

        @Override
        void index(final String line) {

            int tmpIndex = 0;
            int tmpPosition = 1;
            indices[tmpIndex] = tmpPosition;

            while ((tmpPosition = line.indexOf(splitter, tmpPosition)) >= 0) {
                tmpPosition += 3;
                indices[++tmpIndex] = tmpPosition;
            }

            tmpPosition = line.length() + 2;
            indices[++tmpIndex] = tmpPosition;

            this.line = line;
        }

    }

    static class NeverQuoted extends LineView {

        private final int[] indices;

        NeverQuoted(final int numberOfColumns, final char delimiter) {

            super(numberOfColumns, delimiter);

            indices = new int[numberOfColumns + 1];
        }

        @Override
        public String get(final int column) {
            return line.substring(indices[column], indices[column + 1] - 1);
        }

        @Override
        void index(final String line) {

            int tmpIndex = 0;
            int tmpPosition = 0;
            indices[tmpIndex] = tmpPosition;

            while ((tmpPosition = line.indexOf(delimiter, tmpPosition)) >= 0) {
                indices[++tmpIndex] = ++tmpPosition;
            }

            tmpPosition = line.length();
            indices[++tmpIndex] = tmpPosition;

            this.line = line;
        }
    }

    static class SometimesQuoted extends LineView {

        SometimesQuoted(final int numberOfColumns, final char delimiter) {

            super(numberOfColumns, delimiter);

        }

        @Override
        public String get(final int column) {
            return null;
        }

        @Override
        void index(final String line) {

            this.line = line;

            throw new RuntimeException("Not yet implemented!");

        }
    }

    public static EnumeratedColumnsParser.Builder make() {
        return new EnumeratedColumnsParser.Builder();
    }

    private final LineView myLineView;

    @SuppressWarnings("unused")
    private EnumeratedColumnsParser() {
        this(0, ',', null);
    }

    EnumeratedColumnsParser(final int columns, final char delimiter, final Quoted quoted) {

        super();

        myLineView = quoted.make(columns, delimiter);
    }

    @Override
    public LineView parse(final String line) {

        myLineView.index(line);

        return myLineView;
    }

}
