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
package org.ojalgo.optimisation.linear;

import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.type.NumberDefinition;

abstract class Primitive1D implements Access1D<Double>, Mutate1D {

    static Primitive1D of(final double... values) {
        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return values[index];
            }

            @Override
            public void set(final int index, final double value) {
                values[index] = value;
            }

            @Override
            public int size() {
                return values.length;
            }

        };
    }

    @Override
    public final long count() {
        return this.size();
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
    public abstract int size();

    @Override
    public final String toString() {
        return Access1D.toString(this);
    }

}
