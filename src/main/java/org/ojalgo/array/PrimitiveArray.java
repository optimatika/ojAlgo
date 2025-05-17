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
package org.ojalgo.array;

import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.type.math.MathType;

public abstract class PrimitiveArray extends PlainArray<Double> implements Mutate1D.Sortable {

    public static abstract class Factory extends PlainArray.Factory<Double, PrimitiveArray> {

        protected Factory(final MathType mathType) {
            super(mathType);
        }

        @Override
        public PrimitiveArray copy(final Access1D<?> values) {
            PrimitiveArray retVal = this.make(values);
            for (long i = 0L, limit = values.count(); i < limit; i++) {
                retVal.set(i, values.doubleValue(i));
            }
            return retVal;
        }

    }

    public static PrimitiveArray make(final int size) {
        return ArrayR064.make(size);
    }

    public static PrimitiveArray wrap(final double... data) {
        return ArrayR064.wrap(data);
    }

    public static PrimitiveArray wrap(final float... data) {
        return ArrayR032.wrap(data);
    }

    PrimitiveArray(final PrimitiveArray.Factory factory, final int size) {
        super(factory, size);
    }

}
