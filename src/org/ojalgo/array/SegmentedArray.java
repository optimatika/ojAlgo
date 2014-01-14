/*
 * Copyright 1997-2013 Optimatika (www.optimatika.se)
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

import org.ojalgo.access.Access1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

/**
 * Huge array - only deals with long indices. Delegates to its segments, localises indices for them.
 * 
 * @author apete
 */
final class SegmentedArray<N extends Number> extends BasicArray<N> {

    public static SegmentedArray<Double> makePrimitive(final long count) {

        final long mySegmentSize = (32L * 1024L) / 16L;

        final int tmpNumberOfSegments = (int) (count / mySegmentSize);

        @SuppressWarnings("unchecked")
        final SparseArray<Double>[] mySegments = new SparseArray[tmpNumberOfSegments + 1];
        for (int s = 0; s < tmpNumberOfSegments; s++) {
            mySegments[s] = SparseArray.make(mySegmentSize);
        }
        mySegments[tmpNumberOfSegments] = SparseArray.make(count - (mySegmentSize * tmpNumberOfSegments));

        return new SegmentedArray<Double>(count, mySegments);
    }

    private final long myCount;

    private final BasicArray<N>[] mySegments;

    private final long mySegmentSize;

    SegmentedArray(final long count, final SparseArray<N>[] segments) {

        super();

        myCount = count;

        mySegments = segments;
        mySegmentSize = segments[0].count();

    }

    @Override
    public long count() {
        return myCount;
    }

    public double doubleValue(final long index) {
        return mySegments[(int) (index / mySegmentSize)].doubleValue(index % mySegmentSize);
    }

    public void fillAll(final N value) {
        for (final BasicArray<N> tmpSegment : mySegments) {
            tmpSegment.fillAll(value);
        }
    }

    public void fillRange(final long first, final long limit, final N value) {

        final int tmpFirstSegment = (int) (first / mySegmentSize);
        final int tmpLastSegemnt = (int) ((limit - 1) / mySegmentSize);

        long tmpFirstInSegment = (first % mySegmentSize);

        for (int s = tmpFirstSegment; s < tmpLastSegemnt; s++) {
            mySegments[s].fillRange(tmpFirstInSegment, mySegmentSize, value);
            tmpFirstInSegment = 0L;
        }
        mySegments[tmpLastSegemnt].fillRange(tmpFirstInSegment, limit - (tmpLastSegemnt * mySegmentSize), value);

    }

    public N get(final long index) {
        return mySegments[(int) (index / mySegmentSize)].get(index % mySegmentSize);
    }

    public boolean isAbsolute(final long index) {
        return mySegments[(int) (index / mySegmentSize)].isAbsolute(index % mySegmentSize);
    }

    public boolean isInfinite(final long index) {
        return mySegments[(int) (index / mySegmentSize)].isInfinite(index % mySegmentSize);
    }

    public boolean isNaN(final long index) {
        return mySegments[(int) (index / mySegmentSize)].isNaN(index % mySegmentSize);
    }

    public boolean isPositive(final long index) {
        return mySegments[(int) (index / mySegmentSize)].isPositive(index % mySegmentSize);
    }

    public boolean isReal(final long index) {
        return mySegments[(int) (index / mySegmentSize)].isReal(index % mySegmentSize);
    }

    public boolean isZero(final long index) {
        return mySegments[(int) (index / mySegmentSize)].isZero(index % mySegmentSize);
    }

    public void set(final long index, final double value) {
        mySegments[(int) (index / mySegmentSize)].set(index % mySegmentSize, value);
    }

    public void set(final long index, final Number value) {
        mySegments[(int) (index / mySegmentSize)].set(index % mySegmentSize, value);
    }

    private boolean isZeroModified(final UnaryFunction<N> function) {
        return !TypeUtils.isZero(function.invoke(PrimitiveMath.ZERO));
    }

    @Override
    protected void exchange(final long firstA, final long firstB, final long step, final long count) {

        if (this.isPrimitive()) {

            long tmpIndexA = firstA;
            long tmpIndexB = firstB;

            double tmpVal;

            for (long i = 0; i < count; i++) {

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

            for (long i = 0; i < count; i++) {

                tmpVal = this.get(tmpIndexA);
                this.set(tmpIndexA, this.get(tmpIndexB));
                this.set(tmpIndexB, tmpVal);

                tmpIndexA += step;
                tmpIndexB += step;
            }
        }
    }

    @Override
    protected void fill(final Access1D<?> values) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void fill(final long first, final long limit, final Access1D<N> left, final BinaryFunction<N> function, final Access1D<N> right) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void fill(final long first, final long limit, final long step, final N value) {

        if (step <= mySegmentSize) {
            // Will use a continuous range of segements

            final int tmpFirstSegment = (int) (first / mySegmentSize);
            final int tmpLastSegemnt = (int) ((limit - 1) / mySegmentSize);

            long tmpFirstInSegment = (first % mySegmentSize);

            for (int s = tmpFirstSegment; s < tmpLastSegemnt; s++) {
                mySegments[s].fill(tmpFirstInSegment, mySegmentSize, step, value);
                final long tmpRemainder = (mySegmentSize - tmpFirstInSegment) % step;
                tmpFirstInSegment = tmpRemainder == 0L ? 0L : step - tmpRemainder;
            }
            mySegments[tmpLastSegemnt].fill(tmpFirstInSegment, limit - (tmpLastSegemnt * mySegmentSize), step, value);

        } else if (this.isPrimitive()) {

            final double tmpValue = value.doubleValue();
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
    protected long getIndexOfLargest(final long first, final long limit, final long step) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected boolean isZeros(final long first, final long limit, final long step) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final Access1D<N> left, final BinaryFunction<N> function) {
        final int tmpFirst = (int) (first / mySegmentSize);
        final int tmpLast = (int) (limit / mySegmentSize);
        for (int s = tmpFirst; s <= tmpLast; s++) {
            mySegments[s].modify(first, limit, step, left, function);
        }
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final BinaryFunction<N> function, final Access1D<N> right) {
        final int tmpFirst = (int) (first / mySegmentSize);
        final int tmpLast = (int) (limit / mySegmentSize);
        for (int s = tmpFirst; s <= tmpLast; s++) {
            mySegments[s].modify(first, limit, step, function, right);
        }
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final UnaryFunction<N> function) {

        if (step <= mySegmentSize) {
            // Will use a continuous range of segements

            final int tmpFirstSegment = (int) (first / mySegmentSize);
            final int tmpLastSegemnt = (int) ((limit - 1) / mySegmentSize);

            long tmpFirstInSegment = (first % mySegmentSize);

            for (int s = tmpFirstSegment; s < tmpLastSegemnt; s++) {
                mySegments[s].modify(tmpFirstInSegment, mySegmentSize, step, function);
                final long tmpRemainder = (mySegmentSize - tmpFirstInSegment) % step;
                tmpFirstInSegment = tmpRemainder == 0L ? 0L : step - tmpRemainder;
            }
            mySegments[tmpLastSegemnt].modify(tmpFirstInSegment, limit - (tmpLastSegemnt * mySegmentSize), step, function);

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
    protected Scalar<N> toScalar(final long index) {
        return mySegments[(int) (index / mySegmentSize)].toScalar(index % mySegmentSize);
    }

    @Override
    protected void visit(final long first, final long limit, final long step, final VoidFunction<N> visitor) {

        if (step <= mySegmentSize) {
            // Will use a continuous range of segements

            final int tmpFirstSegment = (int) (first / mySegmentSize);
            final int tmpLastSegemnt = (int) ((limit - 1) / mySegmentSize);

            long tmpFirstInSegment = (first % mySegmentSize);

            for (int s = tmpFirstSegment; s < tmpLastSegemnt; s++) {
                mySegments[s].visit(tmpFirstInSegment, mySegmentSize, step, visitor);
                final long tmpRemainder = (mySegmentSize - tmpFirstInSegment) % step;
                tmpFirstInSegment = tmpRemainder == 0L ? 0L : step - tmpRemainder;
            }
            mySegments[tmpLastSegemnt].visit(tmpFirstInSegment, limit - (tmpLastSegemnt * mySegmentSize), step, visitor);

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

    @Override
    boolean isPrimitive() {
        return mySegments[0].isPrimitive();
    }

}
