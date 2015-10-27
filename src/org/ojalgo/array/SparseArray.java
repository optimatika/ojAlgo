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

import java.math.BigDecimal;
import java.util.Arrays;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.array.DenseArray.DenseFactory;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

/**
 * Sparse array - maps long indices to a localiced int.
 *
 * @author apete
 */
public final class SparseArray<N extends Number> extends BasicArray<N> {

    static abstract class SparseFactory<N extends Number> extends ArrayFactory<N> {

        abstract SparseArray<N> make(long count);

        @Override
        final SparseArray<N> makeStructuredZero(final long... structure) {
            return this.make(AccessUtils.count(structure));
        }

        @Override
        final SparseArray<N> makeToBeFilled(final long... structure) {
            return this.make(AccessUtils.count(structure));
        }

    }

    private static final int INITIAL_CAPACITY = 97;

    static final SparseFactory<BigDecimal> BIG = new SparseFactory<BigDecimal>() {

        @Override
        long getElementSize() {
            return BigArray.ELEMENT_SIZE;
        }

        @Override
        SparseArray<BigDecimal> make(final long count) {
            return SparseArray.makeBig(count);
        }

    };

    static final SparseFactory<ComplexNumber> COMPLEX = new SparseFactory<ComplexNumber>() {

        @Override
        long getElementSize() {
            return ComplexArray.ELEMENT_SIZE;
        }

        @Override
        SparseArray<ComplexNumber> make(final long count) {
            return SparseArray.makeComplex(count);
        }

    };

    static final SparseFactory<Double> PRIMITIVE = new SparseFactory<Double>() {

        @Override
        long getElementSize() {
            return PrimitiveArray.ELEMENT_SIZE;
        }

        @Override
        SparseArray<Double> make(final long count) {
            return SparseArray.makePrimitive(count);
        }

    };

    static final SparseFactory<Quaternion> QUATERNION = new SparseFactory<Quaternion>() {

        @Override
        long getElementSize() {
            return QuaternionArray.ELEMENT_SIZE;
        }

        @Override
        SparseArray<Quaternion> make(final long count) {
            return SparseArray.makeQuaternion(count);
        }

    };

    static final SparseFactory<RationalNumber> RATIONAL = new SparseFactory<RationalNumber>() {

        @Override
        long getElementSize() {
            return RationalArray.ELEMENT_SIZE;
        }

        @Override
        SparseArray<RationalNumber> make(final long count) {
            return SparseArray.makeRational(count);
        }

    };

    public static SparseArray<BigDecimal> makeBig(final long count) {
        return new SparseArray<>(count, BigArray.FACTORY);
    }

    public static final SegmentedArray<BigDecimal> makeBigSegmented(final long count) {
        return SegmentedArray.make(BIG, count);
    }

    public static SparseArray<ComplexNumber> makeComplex(final long count) {
        return new SparseArray<>(count, ComplexArray.FACTORY);
    }

    public static final SegmentedArray<ComplexNumber> makeComplexSegmented(final long count) {
        return SegmentedArray.make(COMPLEX, count);
    }

    public static SparseArray<Double> makePrimitive(final long count) {
        return new SparseArray<>(count, PrimitiveArray.FACTORY);
    }

    public static final SegmentedArray<Double> makePrimitiveSegmented(final long count) {
        return SegmentedArray.make(PRIMITIVE, count);
    }

    public static SparseArray<Quaternion> makeQuaternion(final long count) {
        return new SparseArray<>(count, QuaternionArray.FACTORY);
    }

    public static final SegmentedArray<Quaternion> makeQuaternionSegmented(final long count) {
        return SegmentedArray.make(QUATERNION, count);
    }

    public static SparseArray<RationalNumber> makeRational(final long count) {
        return new SparseArray<>(count, RationalArray.FACTORY);
    }

    public static final SegmentedArray<RationalNumber> makeRationalSegmented(final long count) {
        return SegmentedArray.make(RATIONAL, count);
    }

    /**
     * The actual number of nonzwero elements
     */
    private int myActualLength = 0;
    private final long myCount;
    private long[] myIndices;
    private DenseArray<N> myValues;

    private final N myZeroNumber;
    private final Scalar<N> myZeroScalar;
    private final double myZeroValue;

    SparseArray(final long count, final DenseFactory<N> factory) {

        super();

        myCount = count;

        myIndices = new long[INITIAL_CAPACITY];
        myValues = factory.make(INITIAL_CAPACITY);

        myZeroScalar = factory.zero();
        myZeroNumber = myZeroScalar.getNumber();
        myZeroValue = myZeroNumber.doubleValue();
    }

    public void add(final long index, final double addend) {
        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            myValues.add(tmpIndex, addend);
        } else {
            this.set(index, addend);
        }
    }

    public void add(final long index, final Number addend) {
        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            myValues.add(tmpIndex, addend);
        } else {
            this.set(index, addend);
        }
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

            myValues.fillAll(myZeroNumber);

        } else {

            // Bad idea...

            final int tmpSize = (int) this.count();

            if (tmpSize != myIndices.length) {
                myIndices = AccessUtils.makeIncreasingRange(0L, tmpSize);
                myValues = myValues.newInstance(tmpSize);
                myActualLength = tmpSize;
            }

            myValues.fillAll(value);
        }
    }

    @Override
    public void fillAll(final NullaryFunction<N> supplier) {

        // Bad idea...

        final int tmpSize = (int) this.count();

        if (tmpSize != myIndices.length) {
            myIndices = AccessUtils.makeIncreasingRange(0L, tmpSize);
            myValues = myValues.newInstance(tmpSize);
            myActualLength = tmpSize;
        }

        myValues.fillAll(supplier);
    }

    public void fillOne(final long index, final N value) {
        this.set(index, value);
    }

    public void fillOne(final long index, final NullaryFunction<N> supplier) {
        this.set(index, supplier.get());
    }

    public void fillOneMatching(final long index, final Access1D<?> values, final long valueIndex) {
        if (this.isPrimitive()) {
            this.set(index, values.doubleValue(valueIndex));
        } else {
            this.set(index, values.get(valueIndex));
        }
    }

    @Override
    public void fillRange(final long first, final long limit, final N value) {
        this.fill(first, limit, 1L, value);
    }

    @Override
    public void fillRange(final long first, final long limit, final NullaryFunction<N> supplier) {
        this.fill(first, limit, 1L, supplier);
    }

    public long firstInRange(final long rangeFirst, final long rangeLimit) {
        final int tmpFoundAt = this.index(rangeFirst);
        if (tmpFoundAt < 0) {
            return Math.min(myIndices[-tmpFoundAt + 1], rangeLimit);
        } else {
            return rangeFirst;
        }
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

    public boolean isSmall(final long index, final double comparedTo) {
        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            return myValues.isSmall(tmpIndex, comparedTo);
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

    public long limitOfRange(final long rangeFirst, final long rangeLimit) {
        final int tmpFoundAt = this.index(rangeLimit - 1L);
        if (tmpFoundAt < 0) {
            return Math.max(rangeFirst, myIndices[-tmpFoundAt] + 1L);
        } else {
            return rangeLimit;
        }
    }

    public void modifyOne(final long index, final UnaryFunction<N> function) {
        this.set(index, function.invoke(this.get(index)));
    }

    @Override
    public void set(final long index, final double value) {

        final int tmpIndex = this.index(index);

        if (tmpIndex >= 0) {
            // Existing value, just update

            // values[tmpIndex] = value;
            myValues.set(tmpIndex, value);

        } else {
            // Not existing value, insert new

            final long[] tmpOldIndeces = myIndices;

            final int tmpInsInd = -(tmpIndex + 1);

            if ((myActualLength + 1) <= tmpOldIndeces.length) {
                // No need to grow the backing arrays

                for (int i = myActualLength; i > tmpInsInd; i--) {
                    tmpOldIndeces[i] = tmpOldIndeces[i - 1];
                    // values[i] = values[i - 1];
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

            // values[tmpIndex] = value;
            myValues.set(tmpIndex, value);

        } else {
            // Not existing value, insert new

            final long[] tmpOldIndeces = this.myIndices;

            final int tmpInsInd = -(tmpIndex + 1);

            if ((myActualLength + 1) <= tmpOldIndeces.length) {
                // No need to grow the backing arrays

                for (int i = myActualLength; i > tmpInsInd; i--) {
                    tmpOldIndeces[i] = tmpOldIndeces[i - 1];
                    // values[i] = values[i - 1];
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
                    // tmpValues[i] = values[i];
                    tmpValues.set(i, myValues.get(i));
                }
                tmpIndices[tmpInsInd] = index;
                // tmpValues[tmpInsInd] = value;
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

    public void supplyNonZerosTo(final Access1D.Settable<N> consumer) {
        if (this.isPrimitive()) {
            for (int n = 0; n < myActualLength; n++) {
                consumer.set(myIndices[n], myValues.doubleValue(n));
            }
        } else {
            for (int n = 0; n < myActualLength; n++) {
                consumer.set(myIndices[n], myValues.get(n));
            }
        }
    }

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
    protected void fill(final long first, final long limit, final long step, final N value) {
        int tmpFirst = this.index(first);
        if (tmpFirst < 0) {
            tmpFirst = -tmpFirst + 1;
        }
        int tmpLimit = this.index(limit);
        if (tmpLimit < 0) {
            tmpLimit = -tmpLimit + 1;
        }
        if (this.isPrimitive()) {
            final double tmpValue = value.doubleValue();
            for (int i = tmpFirst; i < tmpLimit; i++) {
                myValues.set(i, tmpValue);
            }
        } else {
            for (int i = tmpFirst; i < tmpLimit; i++) {
                myValues.set(i, value);
            }
        }
    }

    @Override
    protected void fill(final long first, final long limit, final long step, final NullaryFunction<N> supplier) {
        int tmpFirst = this.index(first);
        if (tmpFirst < 0) {
            tmpFirst = -tmpFirst + 1;
        }
        int tmpLimit = this.index(limit);
        if (tmpLimit < 0) {
            tmpLimit = -tmpLimit + 1;
        }
        if (this.isPrimitive()) {
            final double tmpValue = supplier.doubleValue();
            for (int i = tmpFirst; i < tmpLimit; i++) {
                myValues.set(i, tmpValue);
            }
        } else {
            for (int i = tmpFirst; i < tmpLimit; i++) {
                myValues.set(i, supplier.invoke());
            }
        }
    }

    @Override
    protected long indexOfLargest(final long first, final long limit, final long step) {

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
    protected boolean isSmall(final long first, final long limit, final long step, final double comparedTo) {

        boolean retVal = true;

        for (int i = 0; retVal && (i < myIndices.length); i++) {
            final long tmpIndex = myIndices[i];
            if ((tmpIndex >= first) && (tmpIndex < limit)) {
                if (((tmpIndex - first) % step) == 0L) {
                    retVal &= myValues.isSmall(i, comparedTo);
                }
            }
        }

        return retVal;
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final Access1D<N> left, final BinaryFunction<N> function) {

        final double tmpZeroValue = function.invoke(PrimitiveMath.ZERO, PrimitiveMath.ZERO);

        if (TypeUtils.isZero(tmpZeroValue)) {

            for (int i = 0; i < myIndices.length; i++) {
                final long tmpIndex = myIndices[i];
                if ((tmpIndex >= first) && (tmpIndex < limit)) {
                    if (((tmpIndex - first) % step) == 0L) {
                        myValues.modify(i, left, function);
                    }
                }
            }

        } else {

            throw new IllegalArgumentException("SparseArray zero modification!");
        }
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final BinaryFunction<N> function, final Access1D<N> right) {

        final double tmpZeroValue = function.invoke(PrimitiveMath.ZERO, PrimitiveMath.ZERO);

        if (TypeUtils.isZero(tmpZeroValue)) {

            for (int i = 0; i < myIndices.length; i++) {
                final long tmpIndex = myIndices[i];
                if ((tmpIndex >= first) && (tmpIndex < limit) && (((tmpIndex - first) % step) == 0L)) {
                    myValues.modify(i, function, right);
                }
            }

        } else {

            throw new IllegalArgumentException("SparseArray zero modification!");
        }
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final UnaryFunction<N> function) {

        final double tmpZeroValue = function.invoke(PrimitiveMath.ZERO);

        if (TypeUtils.isZero(tmpZeroValue)) {

            for (int i = 0; i < myIndices.length; i++) {
                final long tmpIndex = myIndices[i];
                if ((tmpIndex >= first) && (tmpIndex < limit) && (((tmpIndex - first) % step) == 0L)) {
                    myValues.modify(i, function);
                }
            }

        } else {

            throw new IllegalArgumentException("SparseArray zero modification!");
        }
    }

    @Override
    protected void visit(final long first, final long limit, final long step, final VoidFunction<N> visitor) {
        boolean tmpOnlyOnce = true;
        for (int i = 0; i < myIndices.length; i++) {
            final long tmpIndex = myIndices[i];
            if ((tmpIndex >= first) && (tmpIndex < limit) && (((tmpIndex - first) % step) == 0L)) {
                myValues.visitOne(i, visitor);
            } else if (tmpOnlyOnce) {
                visitor.invoke(myZeroValue);
                tmpOnlyOnce = false;
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

    final int index(final long index) {
        return Arrays.binarySearch(myIndices, 0, myActualLength, index);
    }

    @Override
    boolean isPrimitive() {
        return myValues.isPrimitive();
    }

}
