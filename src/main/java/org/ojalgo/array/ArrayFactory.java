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

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.special.PowerOf2;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Factory1D;
import org.ojalgo.structure.StructureAnyD;

abstract class ArrayFactory<N extends Comparable<N>, I extends BasicArray<N>> implements Factory1D<I> {

    @Override
    public abstract FunctionSet<N> function();

    @Override
    public I make(final int size) {
        return this.makeStructuredZero(size);
    }

    @Override
    public I make(final long count) {
        return this.makeStructuredZero(count);
    }

    @Override
    public abstract Scalar.Factory<N> scalar();

    abstract AggregatorSet<N> aggregator();

    abstract long getCapacityLimit();

    SegmentedArray<N> makeSegmented(final long... structure) {

        long totalCount = StructureAnyD.count(structure);

        int max = PowerOf2.powerOf2Smaller(Math.min(totalCount, this.getCapacityLimit()));
        int min = PowerOf2.powerOf2Larger(totalCount / PlainArray.MAX_SIZE);

        if (min > max) {
            throw new IllegalArgumentException();
        }

        int indexBits = Math.max(min, max - OjAlgoUtils.ENVIRONMENT.cores);

        return new SegmentedArray<>(totalCount, indexBits, this);
    }

    /**
     * Typically sparse, but if very small then dense If very large then also segmented
     */
    abstract I makeStructuredZero(long... structure);

    /**
     * Always dense, but maybe segmented
     */
    abstract I makeToBeFilled(long... structure);

    /**
     * There are several requirements on the segments:
     * <ol>
     * <li>All segments, except possibly the last, must have the same length/size/count.</li>
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
