/*
 * Copyright 1997-2025 Optimatika
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

public abstract class Primitive1D implements Access1D<Double>, Mutate1D {

    static final class Simple extends Primitive1D {

        private final double[] myValues;

        Simple(final double[] values) {
            myValues = values;
        }

        @Override
        public double doubleValue(final int index) {
            return myValues[index];
        }

        @Override
        public void set(final int index, final double value) {
            myValues[index] = value;
        }

        @Override
        public int size() {
            return myValues.length;
        }
    }

    static final class Wrapper extends Primitive1D {

        private final Structure1D myDelegate;

        Wrapper(final Structure1D delegate) {
            myDelegate = delegate;
        }

        @Override
        public double doubleValue(final int index) {
            if (myDelegate instanceof Access1D<?>) {
                return ((Access1D<?>) myDelegate).doubleValue(index);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public void set(final int index, final double value) {
            if (myDelegate instanceof Mutate1D) {
                ((Mutate1D) myDelegate).set(index, value);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public int size() {
            return myDelegate.size();
        }
    }

    public static final Primitive1D EMPTY = new Simple(new double[0]);

    public static Primitive1D newInstance(final int size) {
        return new Simple(new double[size]);
    }

    public static Primitive1D of(final double... values) {
        return new Simple(values);
    }

    public static Primitive1D wrap(final Structure1D delegate) {
        return new Wrapper(delegate);
    }

    @Override
    public final Double get(final long index) {
        return Double.valueOf(this.doubleValue(index));
    }

    @Override
    public final void set(final long index, final Comparable<?> value) {
        this.set(index, NumberDefinition.doubleValue(value));
    }

    @Override
    public final String toString() {
        return Access1D.toString(this);
    }

}
