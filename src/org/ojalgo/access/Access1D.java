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
package org.ojalgo.access;

import java.util.Iterator;
import java.util.stream.BaseStream;
import java.util.stream.StreamSupport;

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.scalar.Scalar;

/**
 * 1-dimensional accessor methods
 *
 * @author apete
 */
public interface Access1D<N extends Number> extends Structure1D, Iterable<N> {

    public interface Elements extends Structure1D {

        /**
         * @see Scalar#isAbsolute()
         * @param index
         * @return
         */
        boolean isAbsolute(long index);

        /**
         * @see Scalar#isSmall(double)
         */
        boolean isSmall(long index, double comparedTo);

        /**
         * @deprecated v37
         */
        @Deprecated
        default boolean isZero(final long index) {
            return this.isSmall(index, PrimitiveMath.ONE);
        }

    }

    public interface IndexOf extends Structure1D {

        default long indexOfLargest() {
            return this.indexOfLargestInRange(0L, this.count());
        }

        long indexOfLargestInRange(final long first, final long limit);

    }

    public interface Sliceable<N extends Number> extends Structure1D {

        Access1D<N> sliceRange(long first, long limit);

    }

    public interface Visitable<N extends Number> extends Structure1D {

        void visitAll(VoidFunction<N> visitor);

        void visitOne(long index, VoidFunction<N> visitor);

        void visitRange(long first, long limit, VoidFunction<N> visitor);

    }

    double doubleValue(long index);

    N get(long index);

    default Iterator<N> iterator() {
        return new Iterator1D<>(this);
    }

    default BaseStream<N, ? extends BaseStream<N, ?>> stream(final boolean parallel) {
        return StreamSupport.stream(this.spliterator(), parallel);
    }

    default double[] toRawCopy1D() {

        final int tmpLength = (int) this.count();

        final double[] retVal = new double[tmpLength];

        for (int i = 0; i < tmpLength; i++) {
            retVal[i] = this.doubleValue(i);
        }

        return retVal;
    }

}
