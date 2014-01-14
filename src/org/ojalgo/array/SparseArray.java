/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

import java.util.Arrays;
import java.util.Iterator;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Iterator1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

/**
 * Sparse array - maps long to int.
 * 
 * @author apete
 */
final class SparseArray<N extends Number> extends BasicArray<N> {

    static final int INITIAL_CAPACITY = 11;

    public static SparseArray<Double> make(final long count) {
        return new SparseArray<>(0L, count, new PrimitiveArray(INITIAL_CAPACITY), PrimitiveScalar.ZERO);
    }

    /**
     * The actual number of nonzwero elements
     */
    private int myActualLength = 0;
    private final long myCount;
    private final long myFirst;
    private long[] myIndices;
    private final long myLimit;
    private DenseArray<N> myValues;

    private final N myZeroNumber;
    private final Scalar<N> myZeroScalar;
    private final double myZeroValue;

    SparseArray(final long first, final long limit, final DenseArray<N> values, final Scalar<N> zero) {

        super();

        myFirst = first;
        myCount = limit - first;
        myLimit = limit;

        myIndices = new long[values.size()];
        myValues = values;

        myZeroScalar = zero;
        myZeroNumber = zero.getNumber();
        myZeroValue = zero.doubleValue();

        Arrays.fill(myIndices, Long.MAX_VALUE);
    }

    public final long count() {
        return myCount;
    }

    @Override
    public double doubleValue(final long index) {
        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            return myValues.doubleValue(tmpIndex);
        } else {
            return myZeroValue;
        }
    }

    @Override
    public void fillAll(final N value) {

        if (TypeUtils.isZero(value.doubleValue())) {

        } else {

            // Bad idea...

            final int tmpCount = (int) this.count();

            if (tmpCount != myIndices.length) {
                myIndices = new long[tmpCount];
                myValues = myValues.newInstance(tmpCount);
                myActualLength = tmpCount;
            }

            for (int i = 0; i < myActualLength; i++) {
                myIndices[i] = this.first() + i;
                myValues.set(i, value);
            }
        }
    }

    @Override
    public void fillRange(final long first, final long limit, final N value) {

        if (TypeUtils.isZero(value.doubleValue())) {

        } else {

        }

        //        final long tmpFirst = Math.max(this.first(), first);
        //        final long tmpLimit = Math.max(myLimit, limit);
        //
        //        for (int i = tmpFirst; i < tmpLimit; i++) {
        //            this.set(i, value);
        //        }
    }

    @Override
    public N get(final long index) {
        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            return myValues.get(tmpIndex);
        } else {
            return myZeroNumber;
        }
    }

    public boolean isAbsolute(final long index) {
        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            return myValues.isAbsolute(tmpIndex);
        } else {
            return true;
        }
    }

    public boolean isInfinite(final long index) {
        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            return myValues.isInfinite(tmpIndex);
        } else {
            return false;
        }
    }

    public boolean isNaN(final long index) {
        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            return myValues.isNaN(tmpIndex);
        } else {
            return false;
        }
    }

    public boolean isPositive(final long index) {
        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            return myValues.isPositive(tmpIndex);
        } else {
            return false;
        }
    }

    public boolean isReal(final long index) {
        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            return myValues.isReal(tmpIndex);
        } else {
            return true;
        }
    }

    public boolean isZero(final long index) {
        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            return myValues.isZero(tmpIndex);
        } else {
            return true;
        }
    }

    @Override
    public Iterator<N> iterator() {
        return new Iterator1D<>(this);
    }

    @Override
    public void set(final long index, final double value) {

        final int tmpIndex = this.index(index);

        if (tmpIndex >= 0) {
            // Existing value, just update

            //values[tmpIndex] = value;
            myValues.set(tmpIndex, value);

        } else {
            // Not existing value, insert new

            final long[] tmpOldIndeces = myIndices;

            final int tmpInsInd = -(tmpIndex + 1);

            if ((myActualLength + 1) <= tmpOldIndeces.length) {
                // No need to grow the backing arrays

                for (int i = myActualLength; i > tmpInsInd; i--) {
                    tmpOldIndeces[i] = tmpOldIndeces[i - 1];
                    //      values[i] = values[i - 1];
                    myValues.set(i, myValues.doubleValue(i - 1));
                }
                tmpOldIndeces[tmpInsInd] = index;
                // values[tmpInsInd] = value;
                myValues.set(tmpInsInd, value);

                myActualLength++;

            } else {
                // Needs to grow the backing arrays

                final int tmpCapacity = tmpOldIndeces.length * 2;
                final long[] tmpIndices = new long[tmpCapacity];
                final DenseArray<N> tmpValues = myValues.newInstance(tmpCapacity);

                for (int i = 0; i < tmpInsInd; i++) {
                    tmpIndices[i] = tmpOldIndeces[i];
                    tmpValues.set(i, myValues.doubleValue(i));
                }
                tmpIndices[tmpInsInd] = index;
                tmpValues.set(tmpInsInd, value);
                for (int i = tmpInsInd; i < tmpOldIndeces.length; i++) {
                    tmpIndices[i + 1] = tmpOldIndeces[i];
                    tmpValues.set(i + 1, myValues.doubleValue(i));
                }
                for (int i = tmpOldIndeces.length + 1; i < tmpIndices.length; i++) {
                    tmpIndices[i] = Long.MAX_VALUE;
                }

                myIndices = tmpIndices;
                myValues = tmpValues;
                myActualLength++;
            }
        }
    }

    @Override
    public void set(final long index, final Number value) {

        final int tmpIndex = this.index(index);

        if (tmpIndex >= 0) {
            // Existing value, just update

            //values[tmpIndex] = value;
            myValues.set(tmpIndex, value);

        } else {
            // Not existing value, insert new

            final long[] tmpOldIndeces = this.myIndices;

            final int tmpInsInd = -(tmpIndex + 1);

            if ((myActualLength + 1) <= tmpOldIndeces.length) {
                // No need to grow the backing arrays

                for (int i = myActualLength; i > tmpInsInd; i--) {
                    tmpOldIndeces[i] = tmpOldIndeces[i - 1];
                    //      values[i] = values[i - 1];
                    myValues.set(i, myValues.get(i - 1));
                }
                tmpOldIndeces[tmpInsInd] = index;
                // values[tmpInsInd] = value;
                myValues.set(tmpInsInd, value);

                myActualLength++;

            } else {
                // Needs to grow the backing arrays

                final int tmpCapacity = tmpOldIndeces.length * 2;
                final long[] tmpIndices = new long[tmpCapacity];
                final DenseArray<N> tmpValues = myValues.newInstance(tmpCapacity);

                for (int i = 0; i < tmpInsInd; i++) {
                    tmpIndices[i] = tmpOldIndeces[i];
                    //   tmpValues[i] = values[i];
                    tmpValues.set(i, myValues.get(i));
                }
                tmpIndices[tmpInsInd] = index;
                //  tmpValues[tmpInsInd] = value;
                tmpValues.set(tmpInsInd, value);
                for (int i = tmpInsInd; i < tmpOldIndeces.length; i++) {
                    tmpIndices[i + 1] = tmpOldIndeces[i];
                    // tmpValues[i + 1] = values[i];
                    tmpValues.set(i + 1, myValues.get(i));

                }
                for (int i = tmpOldIndeces.length + 1; i < tmpIndices.length; i++) {
                    tmpIndices[i] = Long.MAX_VALUE;
                }

                myIndices = tmpIndices;
                myValues = tmpValues;
                myActualLength++;
            }
        }
    }

    @Override
    protected void exchange(final long firstA, final long firstB, final long step, final long count) {
        // TODO Auto-generated method stub

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
        final N tmpValue = value;
        int tmpFirst = this.index(first);
        if (tmpFirst < 0) {
            tmpFirst = -tmpFirst + 1;
        }
        int tmpLimit = this.index(limit);
        if (tmpLimit < 0) {
            tmpLimit = -tmpLimit + 1;
        }
        for (int i = tmpFirst; i < tmpLimit; i++) {
            myValues.set(i, tmpValue);
        }
    }

    @Override
    protected long getIndexOfLargest(final long first, final long limit, final long step) {

        double tmpVal = PrimitiveMath.ZERO;
        long retVal = Long.MIN_VALUE;

        for (int i = 0; i < myIndices.length; i++) {
            final long tmpIndex = myIndices[i];
            if ((tmpIndex >= first) && (tmpIndex < limit)) {
                if (((tmpIndex - first) % step) == 0L) {
                    if (myValues.doubleValue(i) > tmpVal) {
                        tmpVal = Math.abs(myValues.doubleValue(i));
                        retVal = tmpIndex;
                    }
                }
            }
        }

        return retVal;
    }

    @Override
    protected boolean isZeros(final long first, final long limit, final long step) {

        boolean retVal = true;

        for (int i = 0; retVal && (i < myIndices.length); i++) {
            final long tmpIndex = myIndices[i];
            if ((tmpIndex >= first) && (tmpIndex < limit)) {
                if (((tmpIndex - first) % step) == 0L) {
                    retVal &= myValues.isZero(i);
                }
            }
        }

        return retVal;
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final Access1D<N> left, final BinaryFunction<N> function) {
        for (int i = 0; i < myIndices.length; i++) {
            final long tmpIndex = myIndices[i];
            if ((tmpIndex >= first) && (tmpIndex < limit)) {
                if (((tmpIndex - first) % step) == 0L) {
                    myValues.modify(i, left, function);
                }
            }
        }
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final BinaryFunction<N> function, final Access1D<N> right) {
        for (int i = 0; i < myIndices.length; i++) {
            final long tmpIndex = myIndices[i];
            if ((tmpIndex >= first) && (tmpIndex < limit) && (((tmpIndex - first) % step) == 0L)) {
                myValues.modify(i, function, right);
            }
        }
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final UnaryFunction<N> function) {
        for (int i = 0; i < myIndices.length; i++) {
            final long tmpIndex = myIndices[i];
            if ((tmpIndex >= first) && (tmpIndex < limit) && (((tmpIndex - first) % step) == 0L)) {
                myValues.modify(i, function);
            }
        }
    }

    @Override
    protected Scalar<N> toScalar(final long index) {
        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            return myValues.toScalar(tmpIndex);
        } else {
            return myZeroScalar;
        }
    }

    @Override
    protected void visit(final long first, final long limit, final long step, final VoidFunction<N> visitor) {
        for (int i = 0; i < myIndices.length; i++) {
            final long tmpIndex = myIndices[i];
            if ((tmpIndex >= first) && (tmpIndex < limit) && (((tmpIndex - first) % step) == 0L)) {
                myValues.visit(i, visitor);
            }
        }
    }

    final DenseArray<N> densify() {

        final DenseArray<N> retVal = myValues.newInstance((int) this.count());

        if (this.isPrimitive()) {
            for (int i = 0; i < myActualLength; i++) {
                retVal.set(myIndices[i], myValues.doubleValue(i));
            }
        } else {
            for (int i = 0; i < myActualLength; i++) {
                retVal.set(myIndices[i], myValues.get(i));
            }
        }

        return retVal;
    }

    final long first() {
        return myFirst;
    }

    final int first(final long first) {
        return this.index(Math.max(myFirst, first));
    }

    final int index(final long index) {
        return Arrays.binarySearch(myIndices, index);
    }

    @Override
    boolean isPrimitive() {
        return myValues.isPrimitive();
    }

    final long limit() {
        return myLimit;
    }

    final int limit(final long limit) {
        return this.index(Math.min(myLimit, limit));
    }

    final int step(final long step) {
        return (int) step;
    }

}
