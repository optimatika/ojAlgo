/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.NullaryFunction;

abstract class ArrayFactory<N extends Number> extends Object implements Access1D.Factory<BasicArray<N>> {

    public final BasicArray<N> copy(final Access1D<?> source) {
        final long tmpCount = source.count();
        final BasicArray<N> retVal = this.makeToBeFilled(tmpCount);
        for (long i = 0L; i < tmpCount; i++) {
            retVal.set(i, source.doubleValue(i));
        }
        return retVal;
    }

    public final BasicArray<N> copy(final double... source) {
        final int tmpLength = source.length;
        final BasicArray<N> retVal = this.makeToBeFilled(tmpLength);
        for (int i = 0; i < tmpLength; i++) {
            retVal.set(i, source[i]);
        }
        return retVal;
    }

    public final BasicArray<N> copy(final List<? extends Number> source) {
        final int tmpSize = source.size();
        final BasicArray<N> retVal = this.makeToBeFilled(tmpSize);
        for (int i = 0; i < tmpSize; i++) {
            retVal.set(i, source.get(i));
        }
        return retVal;
    }

    public final BasicArray<N> copy(final Number... source) {
        final int tmpLength = source.length;
        final BasicArray<N> retVal = this.makeToBeFilled(tmpLength);
        for (int i = 0; i < tmpLength; i++) {
            retVal.set(i, source[i]);
        }
        return retVal;
    }

    public BasicArray<N> makeFilled(final long count, final NullaryFunction<?> supplier) {
        final BasicArray<N> retVal = this.makeToBeFilled(count);
        for (long i = 0L; i < count; i++) {
            retVal.set(i, supplier.get());
        }
        return retVal;
    }

    public final BasicArray<N> makeZero(final long count) {
        return this.makeStructuredZero(count);
    }

    abstract long getElementSize();

    abstract BasicArray<N> makeStructuredZero(final long... structure);

    abstract BasicArray<N> makeToBeFilled(final long... structure);

}
