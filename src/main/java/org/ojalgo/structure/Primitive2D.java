/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.structure;

import org.ojalgo.type.NumberDefinition;

public abstract class Primitive2D implements Access2D<Double>, Mutate2D {

    static final class Simple extends Primitive2D {

        private final int myColDim;
        private final int myRowDim;
        private final double[] myValues;

        Simple(final int nbRows, final int nbCols) {
            myValues = new double[nbRows * nbCols];
            myRowDim = nbRows;
            myColDim = nbCols;
        }

        @Override
        public double doubleValue(final int row, final int col) {
            return myValues[row + col * myRowDim];
        }

        @Override
        public int getColDim() {
            return myColDim;
        }

        @Override
        public int getRowDim() {
            return myRowDim;
        }

        @Override
        public void set(final int row, final int col, final double value) {
            myValues[row + col * myRowDim] = value;
        }

        @Override
        public int size() {
            return myValues.length;
        }
    }

    static final class Wrapper extends Primitive2D {

        private final Structure2D myDelegate;

        Wrapper(final Structure2D delegate) {
            myDelegate = delegate;
        }

        @Override
        public long countColumns() {
            return myDelegate.countColumns();
        }

        @Override
        public long countRows() {
            return myDelegate.countRows();
        }

        @Override
        public double doubleValue(final int row, final int col) {
            if (myDelegate instanceof Access2D<?>) {
                return ((Access2D<?>) myDelegate).doubleValue(row, col);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public double doubleValue(final long row, final long col) {
            if (myDelegate instanceof Access2D<?>) {
                return ((Access2D<?>) myDelegate).doubleValue(row, col);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public int getColDim() {
            return myDelegate.getColDim();
        }

        @Override
        public int getRowDim() {
            return myDelegate.getRowDim();
        }

        @Override
        public void set(final int row, final int col, final double value) {
            if (myDelegate instanceof Mutate2D) {
                ((Mutate2D) myDelegate).set(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public void set(final long row, final long col, final double value) {
            if (myDelegate instanceof Mutate2D) {
                ((Mutate2D) myDelegate).set(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

    }

    public static final Primitive2D EMPTY = new Simple(0, 0);

    public static Primitive2D newInstance(final int nbRows, final int nbCols) {
        return new Simple(nbRows, nbCols);
    }

    public static Primitive2D wrap(final Structure2D delegate) {
        return new Wrapper(delegate);
    }

    @Override
    public final Double get(final long row, final long col) {
        return Double.valueOf(this.doubleValue(row, col));
    }

    @Override
    public final void set(final long row, final long col, final Comparable<?> value) {
        this.set(row, col, NumberDefinition.doubleValue(value));
    }

    @Override
    public final String toString() {
        return Access2D.toString(this);
    }

}
