/*
 * Copyright 1997-2019 Optimatika
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

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Factory1D;
import org.ojalgo.structure.StructureAnyD;

abstract class ArrayFactory<N extends Comparable<N>, I extends BasicArray<N>> extends Object implements Factory1D<BasicArray<N>> {

    public abstract AggregatorSet<N> aggregator();

    public final I copy(final Access1D<?> source) {
        final long count = source.count();
        final I retVal = this.makeToBeFilled(count);
        retVal.fillMatching(source);
        return retVal;
    }

    public final I copy(final double... source) {
        final int length = source.length;
        final I retVal = this.makeToBeFilled(length);
        for (int i = 0; i < length; i++) {
            retVal.set(i, source[i]);
        }
        return retVal;
    }

    public final I copy(final List<? extends Comparable<?>> source) {
        final int size = source.size();
        final I retVal = this.makeToBeFilled(size);
        for (int i = 0; i < size; i++) {
            retVal.set(i, source.get(i));
        }
        return retVal;
    }

    public final I copy(final Comparable<?>... source) {
        final int length = source.length;
        final I retVal = this.makeToBeFilled(length);
        for (int i = 0; i < length; i++) {
            retVal.set(i, source[i]);
        }
        return retVal;
    }

    public abstract FunctionSet<N> function();

    public final I makeFilled(final long count, final NullaryFunction<?> supplier) {
        final I retVal = this.makeToBeFilled(count);
        if (retVal.isPrimitive()) {
            for (long i = 0L; i < count; i++) {
                retVal.set(i, supplier.doubleValue());
            }
        } else {
            for (long i = 0L; i < count; i++) {
                retVal.set(i, supplier.get());
            }
        }
        return retVal;
    }

    public final I make(final long count) {
        return this.makeStructuredZero(count);
    }

    public abstract Scalar.Factory<N> scalar();

    abstract long getCapacityLimit();

    final SegmentedArray<N> makeSegmented(final long... structure) {

        final long totalCount = StructureAnyD.count(structure);

        final int max = PrimitiveMath.powerOf2Smaller(Math.min(totalCount, this.getCapacityLimit()));
        final int min = PrimitiveMath.powerOf2Larger(totalCount / DenseArray.MAX_ARRAY_SIZE);

        if (min > max) {
            throw new IllegalArgumentException();
        }

        final int indexBits = Math.max(min, max - OjAlgoUtils.ENVIRONMENT.cores);

        return new SegmentedArray<>(totalCount, indexBits, this);
    }

    /**
     * Typically sparse, but if very small then dense If very large then also segmented
     */
    abstract I makeStructuredZero(final long... structure);

    /**
     * Always dense, but maybe segmented
     */
    abstract I makeToBeFilled(final long... structure);

    /**
     * There are several requirements on the segments:
     * <ol>
     * <li>All segements, except possibly the last, must have the same length/size/count.</li>
     * <li>That size must be a power of 2.</li>
     * <li>The size of the last segment must be <= "the segment size".</li>
     * </ol>
     *
     * @throws IllegalArgumentException if either of the 3 requirements are broken.
     */
    @SafeVarargs
    final SegmentedArray<N> wrapAsSegments(final BasicArray<N>... segments) {
        return new SegmentedArray<>(segments, this);
    }

}
