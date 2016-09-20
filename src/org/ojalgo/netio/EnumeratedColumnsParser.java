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

    public static class Configuration {

        private final char delimiter = ',';
        private final char quote = '"';
        private final boolean heading = false;

    }

    public class LineView {

        private final int[] begin;
        private final int[] end;

        LineView(final int numberOfColumns) {

            super();

            begin = new int[numberOfColumns + 1];
            end = new int[numberOfColumns + 1];
        }

        public String get(final E column) {
            final int tmpOrdinal = column.ordinal();
            return line.substring(begin[tmpOrdinal], end[tmpOrdinal]);
        }

        public <P> P get(final E column, final TypeContext<P> context) {
            return context.parse(this.get(column));
        }

        public boolean isLineOK() {
            return (line != null) && (line.length() > 0);
        }

        void index(final String line) {

            int tmpIndex = 0;
            int tmpBreakpoint = 0;

            begin[tmpIndex] = tmpBreakpoint;

            while ((tmpBreakpoint = line.indexOf(',', tmpBreakpoint)) != -1) {
                end[++tmpIndex] = begin[tmpIndex];
                begin[tmpIndex] = tmpBreakpoint;
            }

            end[tmpIndex] = line.length();
        }

    }

    private final LineView myLineView;
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
