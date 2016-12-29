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

import static org.ojalgo.constant.PrimitiveMath.*;

import java.util.Arrays;
import java.util.List;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.access.Factory1D;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.PrimitiveFunction;

abstract class ArrayFactory<N extends Number, I extends BasicArray<N>> extends Object implements Factory1D<BasicArray<N>> {

    public I copy(final Access1D<?> source) {
        final long tmpCount = source.count();
        final I retVal = this.makeToBeFilled(tmpCount);
        retVal.fillMatching(source);
        return retVal;
    }

    public I copy(final double... source) {
        final int tmpLength = source.length;
        final I retVal = this.makeToBeFilled(tmpLength);
        for (int i = 0; i < tmpLength; i++) {
            retVal.set(i, source[i]);
        }
        return retVal;
    }

    public I copy(final List<? extends Number> source) {
        final int tmpSize = source.size();
        final I retVal = this.makeToBeFilled(tmpSize);
        for (int i = 0; i < tmpSize; i++) {
            retVal.set(i, source.get(i));
        }
        return retVal;
    }

    public I copy(final Number... source) {
        final int tmpLength = source.length;
        final I retVal = this.makeToBeFilled(tmpLength);
        for (int i = 0; i < tmpLength; i++) {
            retVal.set(i, source[i]);
        }
        return retVal;
    }

    public I makeFilled(final long count, final NullaryFunction<?> supplier) {
        final I retVal = this.makeToBeFilled(count);
        for (long i = 0L; i < count; i++) {
            retVal.set(i, supplier.get());
        }
        return retVal;
    }

    public I makeZero(final long count) {
        return this.makeStructuredZero(count);
    }

    abstract I makeStructuredZero(final long... structure);

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

        final BasicArray<N> tmpFirstSegment = segments[0];
        final long tmpSegmentSize = tmpFirstSegment.count();

        final int tmpIndexBits = Arrays.binarySearch(POWERS_OF_2, tmpSegmentSize);

        if ((tmpIndexBits < 0) || (tmpSegmentSize != (1L << tmpIndexBits))) {
            throw new IllegalArgumentException("The segment size must be a power of 2!");
        }

        final int tmpIndexOfLastSegment = segments.length - 1;
        for (int s = 1; s < tmpIndexOfLastSegment; s++) {
            if (segments[s].count() != tmpSegmentSize) {
                throw new IllegalArgumentException("All segments (except possibly the last) must have the same size!");
            }
        }
        if (segments[tmpIndexOfLastSegment].count() > tmpSegmentSize) {
            throw new IllegalArgumentException("The last segment cannot be larger than the others!");
        }

        final long tmpIndexMask = tmpSegmentSize - 1L;

        return new SegmentedArray<>(segments, tmpSegmentSize, tmpIndexBits, tmpIndexMask, this);
    }

    final SegmentedArray<N> makeSegmented(final long... structure) {

        final long tmpCount = AccessUtils.count(structure);

        int tmpNumberOfUniformSegments = 1; // NumberOfUniformSegments
        long tmpUniformSegmentSize = tmpCount;

        final long tmpMaxNumberOfSegments = (long) Math.min(Integer.MAX_VALUE - 1, PrimitiveFunction.SQRT.invoke(tmpCount));

        for (int i = 0; i < structure.length; i++) {
            final long tmpNoS = (tmpNumberOfUniformSegments * structure[i]);
            final long tmpSS = tmpUniformSegmentSize / structure[i];
            if (tmpNoS <= tmpMaxNumberOfSegments) {
                tmpNumberOfUniformSegments = (int) tmpNoS;
                tmpUniformSegmentSize = tmpSS;
            }
        }

        final long tmpCacheDim = OjAlgoUtils.ENVIRONMENT.getCacheDim1D(8L); // TODO Make dynamic
        final long tmpUnits = OjAlgoUtils.ENVIRONMENT.units;
        while ((tmpUnits != 1L) && (tmpUniformSegmentSize >= tmpCacheDim) && ((tmpNumberOfUniformSegments * tmpUnits) <= tmpMaxNumberOfSegments)) {
            tmpNumberOfUniformSegments = (int) (tmpNumberOfUniformSegments * tmpUnits);
            tmpUniformSegmentSize = tmpUniformSegmentSize / tmpUnits;
        }

        final int tmpShift = (int) (PrimitiveFunction.LOG.invoke(tmpUniformSegmentSize) / PrimitiveFunction.LOG.invoke(2));

        return new SegmentedArray<>(tmpCount, tmpShift, this);
    }

}
