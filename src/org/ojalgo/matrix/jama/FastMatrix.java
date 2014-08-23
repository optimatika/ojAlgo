/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.jama;

import java.util.Iterator;

import org.ojalgo.access.Access2D;

public abstract class FastMatrix implements Access2D<Double> {

    abstract Columns copyToColumns();

    abstract Rows copyToRows();

    static final class Columns extends FastMatrix {

        private final int myColumnLength = 0;

        @Override
        Columns copyToColumns() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        Rows copyToRows() {
            // TODO Auto-generated method stub
            return null;
        }

        public double doubleValue(final long row, final long column) {
            return data[(int) column][(int) row];
        }

        public Double get(final long row, final long column) {
            // TODO Auto-generated method stub
            return null;
        }

        public long countColumns() {
            return data.length;
        }

        public long countRows() {
            return myColumnLength;
        }

        public long count() {
            return data.length * myColumnLength;
        }

        public double doubleValue(final long index) {
            // TODO Auto-generated method stub
            return 0;
        }

        public Double get(final long index) {
            // TODO Auto-generated method stub
            return null;
        }

        public Iterator<Double> iterator() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    static final class Rows extends FastMatrix {

        private final long myRowLength = 0L;

        @Override
        Columns copyToColumns() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        Rows copyToRows() {
            // TODO Auto-generated method stub
            return null;
        }

        public double doubleValue(final long row, final long column) {
            return data[(int) row][(int) column];
        }

        public Double get(final long row, final long column) {
            return data[(int) row][(int) column];
        }

        public long countColumns() {
            // TODO Auto-generated method stub
            return 0;
        }

        public long countRows() {
            // TODO Auto-generated method stub
            return 0;
        }

        public long count() {
            // TODO Auto-generated method stub
            return 0;
        }

        public double doubleValue(final long index) {
            // TODO Auto-generated method stub
            return 0;
        }

        public Double get(final long index) {
            // TODO Auto-generated method stub
            return null;
        }

        public Iterator<Double> iterator() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    final double[][] data;

    FastMatrix() {

        super();

        data = null;
    }

}
