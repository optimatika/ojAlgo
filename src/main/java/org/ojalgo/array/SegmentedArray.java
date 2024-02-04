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
package org.ojalgo.array;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.special.PowerOf2;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.NumberDefinition;

/**
 * <p>
 * An array implemented as a sequence of segments that together make up the data structure. Any
 * {@link BasicArray} subclass can be used for segements. A {@link BasicArray.Factory} is used to create
 * sparse segments (they're not guaranteed to actually be sparse) and a {@link DenseArray.Factory} is used to
 * create dense segments (guaranteed to be dense).
 * </p>
 *
 * @author apete
 */
final class SegmentedArray<N extends Comparable<N>> extends BasicArray<N> {

    private final int myIndexBits;
    private final long myIndexMask;
    private final ArrayFactory<N, ?> mySegmentFactory;
    private final BasicArray<N>[] mySegments;

    /**
     * All segments except the last one are assumed to (must) be of equal length. The last segment cannot be
     * longer than the others.
     */
    private final long mySegmentSize;

    SegmentedArray(final BasicArray<N>[] segments, final ArrayFactory<N, ?> segmentFactory) {

        super(segmentFactory);

        mySegmentSize = segments[0].count();
        int tmpIndexOfLastSegment = segments.length - 1;
        for (int s = 1; s < tmpIndexOfLastSegment; s++) {
            if (segments[s].count() != mySegmentSize) {
                throw new IllegalArgumentException("All segments (except possibly the last) must have the same size!");
            }
        }
        if (segments[tmpIndexOfLastSegment].count() > mySegmentSize) {
            throw new IllegalArgumentException("The last segment cannot be larger than the others!");
        }

        myIndexBits = PowerOf2.find(mySegmentSize);
        if (myIndexBits < 0 || mySegmentSize != 1L << myIndexBits) {
            throw new IllegalArgumentException("The segment size must be a power of 2!");
        }

        myIndexMask = mySegmentSize - 1L;

        mySegments = segments;
        mySegmentFactory = segmentFactory;
    }

    @SuppressWarnings("unchecked")
    SegmentedArray(final long count, final int indexBits, final ArrayFactory<N, ?> segmentFactory) {

        super(segmentFactory);

        long tmpSegmentSize = 1L << indexBits; // 2^bits

        int tmpNumberOfUniformSegments = (int) (count / tmpSegmentSize);
        long tmpRemainder = count % tmpSegmentSize;

        int tmpTotalNumberOfSegments = tmpRemainder == 0L ? (int) tmpNumberOfUniformSegments : tmpNumberOfUniformSegments + 1;

        mySegments = (BasicArray<N>[]) new BasicArray<?>[tmpTotalNumberOfSegments];
        for (int s = 0; s < tmpNumberOfUniformSegments; s++) {
            mySegments[s] = segmentFactory.makeStructuredZero(tmpSegmentSize);
        }
        if (tmpRemainder != 0L) {
            mySegments[tmpNumberOfUniformSegments] = segmentFactory.makeStructuredZero(tmpRemainder);
        }

        mySegmentSize = tmpSegmentSize;

        myIndexBits = indexBits;
        myIndexMask = tmpSegmentSize - 1L;

        mySegmentFactory = segmentFactory;
    }

    @Override
    public void add(final long index, final Comparable<?> addend) {
        mySegments[(int) (index >> myIndexBits)].add(index & myIndexMask, addend);
    }

    @Override
    public void add(final long index, final double addend) {
        mySegments[(int) (index >> myIndexBits)].add(index & myIndexMask, addend);
    }

    @Override
    public void add(final long index, final float addend) {
        mySegments[(int) (index >> myIndexBits)].add(index & myIndexMask, addend);
    }

    @Override
    public long count() {
        return mySegments[0].count() * (mySegments.length - 1) + mySegments[mySegments.length - 1].count();
    }

    @Override
    public double doubleValue(final long index) {
        return mySegments[(int) (index >> myIndexBits)].doubleValue(index & myIndexMask);
    }

    @Override
    public double doubleValue(final int index) {
        return mySegments[index >> myIndexBits].doubleValue(index & myIndexMask);
    }

    @Override
    public void fillAll(final N value) {
        for (BasicArray<N> tmpSegment : mySegments) {
            tmpSegment.fillAll(value);
        }
    }

    @Override
    public void fillAll(final NullaryFunction<?> supplier) {
        for (BasicArray<N> tmpSegment : mySegments) {
            tmpSegment.fillAll(supplier);
        }
    }

    @Override
    public void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
        mySegments[(int) (index >> myIndexBits)].fillOne(index & myIndexMask, values, valueIndex);
    }

    @Override
    public void fillOne(final long index, final N value) {
        mySegments[(int) (index >> myIndexBits)].fillOne(index & myIndexMask, value);
    }

    @Override
    public void fillOne(final long index, final NullaryFunction<?> supplier) {
        mySegments[(int) (index >> myIndexBits)].fillOne(index & myIndexMask, supplier);
    }

    @Override
    public void fillRange(final long first, final long limit, final N value) {

        int tmpFirstSegment = (int) (first / mySegmentSize);
        int tmpLastSegemnt = (int) ((limit - 1) / mySegmentSize);

        long tmpFirstInSegment = first % mySegmentSize;

        for (int s = tmpFirstSegment; s < tmpLastSegemnt; s++) {
            mySegments[s].fillRange(tmpFirstInSegment, mySegmentSize, value);
            tmpFirstInSegment = 0L;
        }
        mySegments[tmpLastSegemnt].fillRange(tmpFirstInSegment, limit - tmpLastSegemnt * mySegmentSize, value);

    }

    @Override
    public void fillRange(final long first, final long limit, final NullaryFunction<?> supplier) {

        int tmpFirstSegment = (int) (first / mySegmentSize);
        int tmpLastSegemnt = (int) ((limit - 1) / mySegmentSize);

        long tmpFirstInSegment = first % mySegmentSize;

        for (int s = tmpFirstSegment; s < tmpLastSegemnt; s++) {
            mySegments[s].fillRange(tmpFirstInSegment, mySegmentSize, supplier);
            tmpFirstInSegment = 0L;
        }
        mySegments[tmpLastSegemnt].fillRange(tmpFirstInSegment, limit - tmpLastSegemnt * mySegmentSize, supplier);

    }

    @Override
    public N get(final long index) {
        return mySegments[(int) (index >> myIndexBits)].get(index & myIndexMask);
    }

    @Override
    public void modifyOne(final long index, final UnaryFunction<N> modifier) {
        BasicArray<N> tmpSegment = mySegments[(int) (index >> myIndexBits)];
        long tmpIndex = index & myIndexMask;
        tmpSegment.set(tmpIndex, modifier.invoke(tmpSegment.get(tmpIndex)));
    }

    @Override
    public void reset() {
        for (BasicArray<N> tmpSegment : mySegments) {
            tmpSegment.reset();
        }
    }

    @Override
    public void set(final long index, final Comparable<?> value) {
        mySegments[(int) (index >> myIndexBits)].set(index & myIndexMask, value);
    }

    @Override
    public void set(final long index, final double value) {
        mySegments[(int) (index >> myIndexBits)].set(index & myIndexMask, value);
    }

    @Override
    public void set(final long index, final float value) {
        mySegments[(int) (index >> myIndexBits)].set(index & myIndexMask, value);
    }

    @Override
    public void visitOne(final long index, final VoidFunction<N> visitor) {
        if (this.isPrimitive()) {
            visitor.invoke(this.doubleValue(index));
        } else {
            visitor.invoke(this.get(index));
        }
    }

    @Override
    protected void exchange(final long firstA, final long firstB, final long step, final long count) {

        if (this.isPrimitive()) {

            long tmpIndexA = firstA;
            long tmpIndexB = firstB;

            double tmpVal;

            for (long i = 0L; i < count; i++) {

                tmpVal = this.doubleValue(tmpIndexA);
                this.set(tmpIndexA, this.doubleValue(tmpIndexB));
                this.set(tmpIndexB, tmpVal);

                tmpIndexA += step;
                tmpIndexB += step;
            }

        } else {

            long tmpIndexA = firstA;
            long tmpIndexB = firstB;

            N tmpVal;

            for (long i = 0L; i < count; i++) {

                tmpVal = this.get(tmpIndexA);
                this.set(tmpIndexA, this.get(tmpIndexB));
                this.set(tmpIndexB, tmpVal);

                tmpIndexA += step;
                tmpIndexB += step;
            }
        }
    }

    @Override
    protected void fill(final long first, final long limit, final long step, final N value) {

        if (step <= mySegmentSize) {
            // Will use a continuous range of segements

            int tmpFirstSegment = (int) (first / mySegmentSize);
            int tmpLastSegemnt = (int) ((limit - 1L) / mySegmentSize);

            long tmpFirstInSegment = first % mySegmentSize;

            for (int s = tmpFirstSegment; s < tmpLastSegemnt; s++) {
                mySegments[s].fill(tmpFirstInSegment, mySegmentSize, step, value);
                long tmpRemainder = (mySegmentSize - tmpFirstInSegment) % step;
                tmpFirstInSegment = tmpRemainder == 0L ? 0L : step - tmpRemainder;
            }
            mySegments[tmpLastSegemnt].fill(tmpFirstInSegment, limit - tmpLastSegemnt * mySegmentSize, step, value);

        } else if (this.isPrimitive()) {

            double tmpValue = NumberDefinition.doubleValue(value);
            for (long i = first; i < limit; i += step) {
                this.set(i, tmpValue);
            }

        } else {

            for (long i = first; i < limit; i += step) {
                this.set(i, value);
            }
        }
    }

    @Override
    protected void fill(final long first, final long limit, final long step, final NullaryFunction<?> supplier) {

        if (step <= mySegmentSize) {
            // Will use a continuous range of segements

            int tmpFirstSegment = (int) (first / mySegmentSize);
            int tmpLastSegemnt = (int) ((limit - 1L) / mySegmentSize);

            long tmpFirstInSegment = first % mySegmentSize;

            for (int s = tmpFirstSegment; s < tmpLastSegemnt; s++) {
                mySegments[s].fill(tmpFirstInSegment, mySegmentSize, step, supplier);
                long tmpRemainder = (mySegmentSize - tmpFirstInSegment) % step;
                tmpFirstInSegment = tmpRemainder == 0L ? 0L : step - tmpRemainder;
            }
            mySegments[tmpLastSegemnt].fill(tmpFirstInSegment, limit - tmpLastSegemnt * mySegmentSize, step, supplier);

        } else if (this.isPrimitive()) {

            for (long i = first; i < limit; i += step) {
                this.set(i, supplier.doubleValue());
            }

        } else {

            for (long i = first; i < limit; i += step) {
                this.set(i, supplier.invoke());
            }
        }
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final Access1D<N> left, final BinaryFunction<N> function) {
        if (this.isPrimitive()) {
            for (long l = first; l < limit; l += step) {
                this.set(l, function.invoke(left.doubleValue(l), this.doubleValue(l)));
            }
        } else {
            for (long l = first; l < limit; l += step) {
                this.set(l, function.invoke(left.get(l), this.get(l)));
            }
        }
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final BinaryFunction<N> function, final Access1D<N> right) {
        if (this.isPrimitive()) {
            for (long l = first; l < limit; l += step) {
                this.set(l, function.invoke(this.doubleValue(l), right.doubleValue(l)));
            }
        } else {
            for (long l = first; l < limit; l += step) {
                this.set(l, function.invoke(this.get(l), right.get(l)));
            }
        }
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final UnaryFunction<N> function) {

        if (step <= mySegmentSize) {
            // Will use a continuous range of segements

            int tmpFirstSegment = (int) (first / mySegmentSize);
            int tmpLastSegemnt = (int) ((limit - 1) / mySegmentSize);

            long tmpFirstInSegment = first % mySegmentSize;

            for (int s = tmpFirstSegment; s < tmpLastSegemnt; s++) {
                mySegments[s].modify(tmpFirstInSegment, mySegmentSize, step, function);
                long tmpRemainder = (mySegmentSize - tmpFirstInSegment) % step;
                tmpFirstInSegment = tmpRemainder == 0L ? 0L : step - tmpRemainder;
            }
            mySegments[tmpLastSegemnt].modify(tmpFirstInSegment, limit - tmpLastSegemnt * mySegmentSize, step, function);

        } else if (this.isPrimitive()) {

            for (long i = first; i < limit; i += step) {
                this.set(i, function.invoke(this.doubleValue(i)));
            }

        } else {

            for (long i = first; i < limit; i += step) {
                this.set(i, function.invoke(this.get(i)));
            }
        }
    }

    @Override
    protected void visit(final long first, final long limit, final long step, final VoidFunction<N> visitor) {

        if (step <= mySegmentSize) {
            // Will use a continuous range of segements

            int tmpFirstSegment = (int) (first / mySegmentSize);
            int tmpLastSegemnt = (int) ((limit - 1) / mySegmentSize);

            long tmpFirstInSegment = first % mySegmentSize;

            for (int s = tmpFirstSegment; s < tmpLastSegemnt; s++) {
                mySegments[s].visit(tmpFirstInSegment, mySegmentSize, step, visitor);
                long tmpRemainder = (mySegmentSize - tmpFirstInSegment) % step;
                tmpFirstInSegment = tmpRemainder == 0L ? 0L : step - tmpRemainder;
            }
            mySegments[tmpLastSegemnt].visit(tmpFirstInSegment, limit - tmpLastSegemnt * mySegmentSize, step, visitor);

        } else if (this.isPrimitive()) {

            for (long i = first; i < limit; i += step) {
                visitor.invoke(this.doubleValue(i));
            }

        } else {

            for (long i = first; i < limit; i += step) {
                visitor.invoke(this.get(i));
            }
        }
    }

    /**
     * Will either grow the last segment to be the same size as all the others, or add another segment (with
     * the same size). The returned (could be the same) instance is guaranteed to have a last segement of the
     * same size as the others and at least one more "space" in that segment.
     */
    SegmentedArray<N> grow() {

        BasicArray<N> tmpLastSegment = mySegments[mySegments.length - 1];
        BasicArray<N> tmpNewSegment = mySegmentFactory.make(mySegmentSize);

        long tmpLastSegmentSize = tmpLastSegment.count();

        if (tmpLastSegmentSize < mySegmentSize) {

            mySegments[mySegments.length - 1] = tmpNewSegment;

            tmpNewSegment.fillMatching(tmpLastSegment);

            return this;

        }
        if (tmpLastSegmentSize != mySegmentSize) {

            throw new IllegalStateException();
        }
        @SuppressWarnings("unchecked")
        BasicArray<N>[] tmpSegments = (BasicArray<N>[]) new BasicArray<?>[mySegments.length + 1];

        for (int i = 0; i < mySegments.length; i++) {
            tmpSegments[i] = mySegments[i];
        }
        tmpSegments[mySegments.length] = tmpNewSegment;

        return new SegmentedArray<>(tmpSegments, mySegmentFactory);
    }

    @Override
    public void set(final int index, final double value) {
        mySegments[index >> myIndexBits].set(index & myIndexMask, value);
    }

}
