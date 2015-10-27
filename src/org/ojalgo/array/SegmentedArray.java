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

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.array.DenseArray.DenseFactory;
import org.ojalgo.array.SparseArray.SparseFactory;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;

/**
 * Huge array - only deals with long indices. Delegates to its segments, localises indices for them.
 *
 * @author apete
 */
public final class SegmentedArray<N extends Number> extends BasicArray<N> {

    static abstract class SegmentedFactory<N extends Number> extends ArrayFactory<N> {

        abstract DenseArray.DenseFactory<N> getDenseFactory();

        @Override
        long getElementSize() {
            return this.getDenseFactory().getElementSize();
        }

        abstract SparseArray.SparseFactory<N> getSparseFactory();

        @Override
        final SegmentedArray<N> makeStructuredZero(final long... structure) {
            return SegmentedArray.make(this.getSparseFactory(), structure);
        }

        @Override
        final SegmentedArray<N> makeToBeFilled(final long... structure) {
            return SegmentedArray.make(this.getDenseFactory(), structure);
        }

    }

    static final SegmentedFactory<BigDecimal> BIG = new SegmentedFactory<BigDecimal>() {

        @Override
        DenseFactory<BigDecimal> getDenseFactory() {
            return BigArray.FACTORY;
        }

        @Override
        SparseFactory<BigDecimal> getSparseFactory() {
            return SparseArray.BIG;
        }

    };

    static final SegmentedFactory<ComplexNumber> COMPLEX = new SegmentedFactory<ComplexNumber>() {

        @Override
        DenseFactory<ComplexNumber> getDenseFactory() {
            return ComplexArray.FACTORY;
        }

        @Override
        SparseFactory<ComplexNumber> getSparseFactory() {
            return SparseArray.COMPLEX;
        }

    };

    static final SegmentedFactory<Double> PRIMITIVE = new SegmentedFactory<Double>() {

        @Override
        DenseFactory<Double> getDenseFactory() {
            return PrimitiveArray.FACTORY;
        }

        @Override
        SparseFactory<Double> getSparseFactory() {
            return SparseArray.PRIMITIVE;
        }

    };

    static final SegmentedFactory<Quaternion> QUATERNION = new SegmentedFactory<Quaternion>() {

        @Override
        DenseFactory<Quaternion> getDenseFactory() {
            return QuaternionArray.FACTORY;
        }

        @Override
        SparseFactory<Quaternion> getSparseFactory() {
            return SparseArray.QUATERNION;
        }

    };

    static final SegmentedFactory<RationalNumber> RATIONAL = new SegmentedFactory<RationalNumber>() {

        @Override
        DenseFactory<RationalNumber> getDenseFactory() {
            return RationalArray.FACTORY;
        }

        @Override
        SparseFactory<RationalNumber> getSparseFactory() {
            return SparseArray.RATIONAL;
        }

    };

    public static SegmentedArray<BigDecimal> makeBigDense(final long count) {
        return SegmentedArray.make(BasicArray.BIG, count);
    }

    public static SegmentedArray<BigDecimal> makeBigSparse(final long count) {
        return SegmentedArray.make(SparseArray.BIG, count);
    }

    public static SegmentedArray<ComplexNumber> makeComplexDense(final long count) {
        return SegmentedArray.make(BasicArray.COMPLEX, count);
    }

    public static SegmentedArray<ComplexNumber> makeComplexSparse(final long count) {
        return SegmentedArray.make(SparseArray.COMPLEX, count);
    }

    public static SegmentedArray<Double> makePrimitiveDense(final long count) {
        return SegmentedArray.make(BasicArray.PRIMITIVE, count);
    }

    public static SegmentedArray<Double> makePrimitiveSparse(final long count) {
        return SegmentedArray.make(SparseArray.PRIMITIVE, count);
    }

    public static SegmentedArray<Quaternion> makeQuaternionDense(final long count) {
        return SegmentedArray.make(BasicArray.QUATERNION, count);
    }

    public static SegmentedArray<Quaternion> makeQuaternionSparse(final long count) {
        return SegmentedArray.make(SparseArray.QUATERNION, count);
    }

    public static SegmentedArray<RationalNumber> makeRationalDense(final long count) {
        return SegmentedArray.make(BasicArray.RATIONAL, count);
    }

    public static SegmentedArray<RationalNumber> makeRationalSparse(final long count) {
        return SegmentedArray.make(SparseArray.RATIONAL, count);
    }

    static <N extends Number> SegmentedArray<N> make(final ArrayFactory<N> segmentFactory, final long... structure) {

        final long tmpCount = AccessUtils.count(structure);

        int tmpNumberOfUniformSegments = 1; // NumberOfUniformSegments
        long tmpUniformSegmentSize = tmpCount;

        final long tmpMaxNumberOfSegments = (long) Math.min(Integer.MAX_VALUE - 1, Math.sqrt(tmpCount));

        for (int i = 0; i < structure.length; i++) {
            final long tmpNoS = (tmpNumberOfUniformSegments * structure[i]);
            final long tmpSS = tmpUniformSegmentSize / structure[i];
            if (tmpNoS <= tmpMaxNumberOfSegments) {
                tmpNumberOfUniformSegments = (int) tmpNoS;
                tmpUniformSegmentSize = tmpSS;
            }
        }

        final long tmpCacheDim = OjAlgoUtils.ENVIRONMENT.getCacheDim1D(segmentFactory.getElementSize());
        final long tmpUnits = OjAlgoUtils.ENVIRONMENT.units;
        while ((tmpUnits != 1L) && (tmpUniformSegmentSize >= tmpCacheDim) && ((tmpNumberOfUniformSegments * tmpUnits) <= tmpMaxNumberOfSegments)) {
            tmpNumberOfUniformSegments = (int) (tmpNumberOfUniformSegments * tmpUnits);
            tmpUniformSegmentSize = tmpUniformSegmentSize / tmpUnits;
        }

        final int tmpShift = (int) (Math.log(tmpUniformSegmentSize) / Math.log(2));

        return new SegmentedArray<N>(tmpCount, tmpShift, segmentFactory);
    }

    private final int myIndexBits;
    private final long myIndexMask;
    private final BasicArray<N>[] mySegments;

    /**
     * All segments except the last one are assumed to (must) be of equal length. The last segment cannot be
     * longer than the others.
     */
    private final long mySegmentSize;

    @SuppressWarnings("unchecked")
    SegmentedArray(final long count, final int indexBits, final ArrayFactory<N> segmentFactory) {

        super();

        final long tmpSegmentSize = 1L << indexBits; // 2^bits

        final int tmpNumberOfUniformSegments = (int) (count / tmpSegmentSize);
        final long tmpRemainder = count % tmpSegmentSize;

        final int tmpTotalNumberOfSegments = tmpRemainder == 0L ? (int) tmpNumberOfUniformSegments : tmpNumberOfUniformSegments + 1;

        mySegments = (BasicArray<N>[]) new BasicArray<?>[tmpTotalNumberOfSegments];
        for (int s = 0; s < tmpNumberOfUniformSegments; s++) {
            mySegments[s] = segmentFactory.makeZero(tmpSegmentSize);
        }
        if (tmpRemainder != 0L) {
            mySegments[tmpNumberOfUniformSegments] = segmentFactory.makeZero(tmpRemainder);
        }

        mySegmentSize = tmpSegmentSize;

        myIndexBits = indexBits;
        myIndexMask = tmpSegmentSize - 1L;
    }

    public void add(final long index, final double addend) {
        mySegments[(int) (index >> myIndexBits)].add(index & myIndexMask, addend);
    }

    public void add(final long index, final Number addend) {
        mySegments[(int) (index >> myIndexBits)].add(index & myIndexMask, addend);
    }

    @Override
    public long count() {
        final int tmpVal = mySegments.length - 1;
        return (mySegments[0].count() * tmpVal) + mySegments[tmpVal].count();
    }

    public double doubleValue(final long index) {
        return mySegments[(int) (index >> myIndexBits)].doubleValue(index & myIndexMask);
    }

    public void fillAll(final N value) {
        for (final BasicArray<N> tmpSegment : mySegments) {
            tmpSegment.fillAll(value);
        }
    }

    public void fillAll(final NullaryFunction<N> supplier) {
        for (final BasicArray<N> tmpSegment : mySegments) {
            tmpSegment.fillAll(supplier);
        }
    }

    public void fillOne(final long index, final N value) {
        mySegments[(int) (index >> myIndexBits)].fillOne(index & myIndexMask, value);
    }

    public void fillOneMatching(final long index, final Access1D<?> values, final long valueIndex) {
        mySegments[(int) (index >> myIndexBits)].fillOneMatching(index & myIndexMask, values, valueIndex);
    }

    public void fillOne(final long index, final NullaryFunction<N> supplier) {
        mySegments[(int) (index >> myIndexBits)].fillOne(index & myIndexMask, supplier);
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

    public void fillRange(final long first, final long limit, final NullaryFunction<N> supplier) {

        final int tmpFirstSegment = (int) (first / mySegmentSize);
        final int tmpLastSegemnt = (int) ((limit - 1) / mySegmentSize);

        long tmpFirstInSegment = (first % mySegmentSize);

        for (int s = tmpFirstSegment; s < tmpLastSegemnt; s++) {
            mySegments[s].fillRange(tmpFirstInSegment, mySegmentSize, supplier);
            tmpFirstInSegment = 0L;
        }
        mySegments[tmpLastSegemnt].fillRange(tmpFirstInSegment, limit - (tmpLastSegemnt * mySegmentSize), supplier);

    }

    public N get(final long index) {
        return mySegments[(int) (index >> myIndexBits)].get(index & myIndexMask);
    }

    public boolean isAbsolute(final long index) {
        return mySegments[(int) (index >> myIndexBits)].isAbsolute(index & myIndexMask);
    }

    public boolean isSmall(final long index, final double comparedTo) {
        return mySegments[(int) (index >> myIndexBits)].isSmall(index & myIndexMask, comparedTo);
    }

    public void modifyOne(final long index, final UnaryFunction<N> function) {
        final BasicArray<N> tmpSegment = mySegments[(int) (index >> myIndexBits)];
        final long tmpIndex = index & myIndexMask;
        tmpSegment.set(tmpIndex, function.invoke(tmpSegment.get(tmpIndex)));
    }

    public void set(final long index, final double value) {
        mySegments[(int) (index >> myIndexBits)].set(index & myIndexMask, value);
    }

    public void set(final long index, final Number value) {
        mySegments[(int) (index >> myIndexBits)].set(index & myIndexMask, value);
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

        if (step <= mySegmentSize) {
            // Will use a continuous range of segements

            final int tmpFirstSegment = (int) (first / mySegmentSize);
            final int tmpLastSegemnt = (int) ((limit - 1L) / mySegmentSize);

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
    protected void fill(final long first, final long limit, final long step, final NullaryFunction<N> supplier) {

        if (step <= mySegmentSize) {
            // Will use a continuous range of segements

            final int tmpFirstSegment = (int) (first / mySegmentSize);
            final int tmpLastSegemnt = (int) ((limit - 1L) / mySegmentSize);

            long tmpFirstInSegment = (first % mySegmentSize);

            for (int s = tmpFirstSegment; s < tmpLastSegemnt; s++) {
                mySegments[s].fill(tmpFirstInSegment, mySegmentSize, step, supplier);
                final long tmpRemainder = (mySegmentSize - tmpFirstInSegment) % step;
                tmpFirstInSegment = tmpRemainder == 0L ? 0L : step - tmpRemainder;
            }
            mySegments[tmpLastSegemnt].fill(tmpFirstInSegment, limit - (tmpLastSegemnt * mySegmentSize), step, supplier);

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
    protected long indexOfLargest(final long first, final long limit, final long step) {

        double tmpVal = PrimitiveMath.ZERO;
        long retVal = Long.MIN_VALUE;

        for (long tmpIndex = first; tmpIndex < limit; tmpIndex += step) {
            if (this.doubleValue(tmpIndex) > tmpVal) {
                tmpVal = Math.abs(this.doubleValue(tmpIndex));
                retVal = tmpIndex;
            }
        }

        return retVal;
    }

    @Override
    protected boolean isSmall(final long first, final long limit, final long step, final double comparedTo) {
        boolean retVal = true;
        for (long i = first; retVal && (i < limit); i += step) {
            retVal &= this.isSmall(i, comparedTo);
        }
        return retVal;
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
