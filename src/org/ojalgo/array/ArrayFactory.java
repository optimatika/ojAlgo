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
package org.ojalgo.array;

import java.util.List;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.access.Factory1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.NullaryFunction;

abstract class ArrayFactory<N extends Number, I extends BasicArray<N>> extends Object implements Factory1D<BasicArray<N>> {

    private static final long DENSE_SEGMENTATION_LIMIT = DenseArray.MAX_ARRAY_SIZE;
    private static final long SPARSE_SEGMENTATION_LIMIT = DenseStrategy.capacity(DenseArray.MAX_ARRAY_SIZE);

    public final I copy(final Access1D<?> source) {
        final long tmpCount = source.count();
        final I retVal = this.makeToBeFilled(DENSE_SEGMENTATION_LIMIT, tmpCount);
        retVal.fillMatching(source);
        return retVal;
    }

    public final I copy(final double... source) {
        final int tmpLength = source.length;
        final I retVal = this.makeToBeFilled(DENSE_SEGMENTATION_LIMIT, tmpLength);
        for (int i = 0; i < tmpLength; i++) {
            retVal.set(i, source[i]);
        }
        return retVal;
    }

    public final I copy(final List<? extends Number> source) {
        final int tmpSize = source.size();
        final I retVal = this.makeToBeFilled(DENSE_SEGMENTATION_LIMIT, tmpSize);
        for (int i = 0; i < tmpSize; i++) {
            retVal.set(i, source.get(i));
        }
        return retVal;
    }

    public final I copy(final Number... source) {
        final int tmpLength = source.length;
        final I retVal = this.makeToBeFilled(DENSE_SEGMENTATION_LIMIT, tmpLength);
        for (int i = 0; i < tmpLength; i++) {
            retVal.set(i, source[i]);
        }
        return retVal;
    }

    public final I makeFilled(final long count, final NullaryFunction<?> supplier) {
        final I retVal = this.makeToBeFilled(DENSE_SEGMENTATION_LIMIT, count);
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

    public final I makeZero(final long count) {
        return this.makeStructuredZero(SPARSE_SEGMENTATION_LIMIT, count);
    }

    abstract long getElementSize();

    abstract long getMaxCount();

    final SegmentedArray<N> makeSegmented(final long... structure) {

        final long tmpTotalCount = AccessUtils.count(structure);

        final int tmpMax = PrimitiveMath.powerOf2Smaller(Math.min(tmpTotalCount, this.getMaxCount()));
        final int tmpMin = PrimitiveMath.powerOf2Larger(tmpTotalCount / DenseArray.MAX_ARRAY_SIZE);

        if (tmpMin > tmpMax) {
            throw new IllegalArgumentException();
        }

        final int tmpUse = Math.max(tmpMin, tmpMax - Math.min(OjAlgoUtils.ENVIRONMENT.cores, 10));

        return new SegmentedArray<>(tmpTotalCount, tmpUse, this);
    }

    abstract I makeStructuredZero(long segmentationLimit, final long... structure);

    abstract I makeToBeFilled(long segmentationLimit, final long... structure);

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
