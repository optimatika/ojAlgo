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

import org.ojalgo.type.context.TypeContext;

public class EnumeratedColumnsParser<E extends Enum<E>> implements BasicParser<EnumeratedColumnsParser<E>.LineView> {

    public class Configuration {

        public Configuration delimiter(char d) {
            delimiter = d;
            return this;
        }

        public Configuration heading(boolean h) {
            heading = h;
            return this;
        }

        public boolean isQuoted() {
            return quote != _NULL;
        }

        public Configuration quote(char q) {
            quote = q;
            return this;
        }

    }

    public class LineView {

        private final int[] indexOfs;

        private char quoteChar = _NULL;

        private char splitChar = ',';

        private String splitter;

        LineView(final int numberOfColumns) {

            super();

            indexOfs = new int[numberOfColumns + 1];
        }

        public String get(final E column) {
            final int tmpOrdinal = column.ordinal();
            return line.substring(indexOfs[tmpOrdinal], indexOfs[tmpOrdinal + 1]);
        }

        public <P> P get(final E column, final TypeContext<P> context) {
            return context.parse(this.get(column));
        }

        public boolean isLineOK() {
            return (line != null) && (line.length() > 0);
        }

        void index(final String line) {

            int tmpSplitterLength = splitter.length();

            int tmpIndex = 0;
            int tmpPosition = quoteChar == _NULL ? 0 : 1;
            indexOfs[tmpIndex] = tmpPosition;

            while ((tmpPosition = line.indexOf(splitter, tmpPosition)) >= 0) {
                tmpPosition += tmpSplitterLength;
                indexOfs[++tmpIndex] = tmpPosition;
            }

            tmpPosition = line.length() + tmpSplitterLength;
            if (quoteChar != _NULL) {
                tmpPosition--;
            }
            indexOfs[++tmpIndex] = tmpPosition;

        }

    }

    private static final char _NULL = (char) 0;

    private char delimiter = ',';
    private boolean heading = false;
    private final LineView myLineView;
    private char quote = '"';
    transient String line = null;

    public EnumeratedColumnsParser(final Class<E> clazz) {

        super();

        myLineView = new LineView(clazz.getFields().length);
    }

    @SuppressWarnings("unused")
    private EnumeratedColumnsParser() {
        this(null);
    }

    @Override
    public LineView parse(final String line) {

        this.line = line;

        myLineView.index(line);

        return myLineView;
    }

}
