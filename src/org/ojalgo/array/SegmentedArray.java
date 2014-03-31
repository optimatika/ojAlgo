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

import java.math.BigDecimal;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.array.DenseArray.DenseFactory;
import org.ojalgo.array.SparseArray.SparseFactory;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;

/**
 * Huge array - only deals with long indices. Delegates to its segments, localises indices for them.
 *
 * @author apete
 */
public final class SegmentedArray<N extends Number> extends BasicArray<N> {

    static abstract class SegmentedFactory<N extends Number> extends BasicFactory<N> {

        @Override
        final SegmentedFactory<N> getSegmentedFactory() {
            return this;
        }

        @Override
        final SegmentedArray<N> makeZero(final long... structure) {

            final SparseFactory<N> segementFactory = this.getSparseFactory();

            final long totalCount = AccessUtils.count(structure);

            final long tmpElementSize = this.getElementSize();
            final long tmpTotalCount = AccessUtils.count(structure);
            
            long retVal = 1; // NumberOfUniformSegments
            long tmpSegmentSize = tmpTotalCount;
            
            final long tmpMaxNumberOfSegments = (long) Math.min(Integer.MAX_VALUE - 1, Math.sqrt(tmpTotalCount));
            
            for (int i = 0; i < structure.length; i++) {
                final long tmpNoS = retVal * structure[i];
                final long tmpSS = tmpSegmentSize / structure[i];
                if (tmpNoS <= tmpMaxNumberOfSegments) {
                    retVal = tmpNoS;
                    tmpSegmentSize = tmpSS;
                }
            }
            
            final long tmpCacheDim = OjAlgoUtils.ENVIRONMENT.getCacheDim1D(tmpElementSize);
            final long tmpUnits = OjAlgoUtils.ENVIRONMENT.units;
            while ((tmpSegmentSize >= tmpCacheDim) && ((retVal * tmpUnits) <= tmpMaxNumberOfSegments)) {
                retVal = retVal * tmpUnits;
                tmpSegmentSize = tmpSegmentSize / tmpUnits;
            }
            final long tmpCalculateNumberOfUniformSegments = retVal;
            final int numberOfUniformSegments = (int) tmpCalculateNumberOfUniformSegments;

            final long tmpSize = totalCount / numberOfUniformSegments;
            final int tmpRemainder = (int) (totalCount % numberOfUniformSegments);

            final int tmpTotalNumberOfSegments = tmpRemainder == 0 ? numberOfUniformSegments : numberOfUniformSegments + 1;

            @SuppressWarnings("unchecked")
            final SparseArray<N>[] tmpSegments = new SparseArray[tmpTotalNumberOfSegments];
            for (int s = 0; s < numberOfUniformSegments; s++) {
                tmpSegments[s] = segementFactory.make(tmpSize);
            }
            if (tmpRemainder != 0) {
                tmpSegments[numberOfUniformSegments] = segementFactory.make(tmpRemainder);
            }

            return new SegmentedArray<N>(tmpSegments);
        }

        @Override
        final SegmentedArray<N> makeToBeFilled(final long... structure) {

            final DenseFactory<N> segementFactory = this.getDenseFactory();

            final long totalCount = AccessUtils.count(structure);

            final long tmpElementSize = this.getElementSize();
            final long tmpTotalCount = AccessUtils.count(structure);
            
            long retVal = 1; // NumberOfUniformSegments
            long tmpSegmentSize = tmpTotalCount;
            
            final long tmpMaxNumberOfSegments = (long) Math.min(Integer.MAX_VALUE - 1, Math.sqrt(tmpTotalCount));
            
            for (int i = 0; i < structure.length; i++) {
                final long tmpNoS = retVal * structure[i];
                final long tmpSS = tmpSegmentSize / structure[i];
                if (tmpNoS <= tmpMaxNumberOfSegments) {
                    retVal = tmpNoS;
                    tmpSegmentSize = tmpSS;
                }
            }
            
            final long tmpCacheDim = OjAlgoUtils.ENVIRONMENT.getCacheDim1D(tmpElementSize);
            final long tmpUnits = OjAlgoUtils.ENVIRONMENT.units;
            while ((tmpSegmentSize >= tmpCacheDim) && ((retVal * tmpUnits) <= tmpMaxNumberOfSegments)) {
                retVal = retVal * tmpUnits;
                tmpSegmentSize = tmpSegmentSize / tmpUnits;
            }
            final long tmpCalculateNumberOfUniformSegments = retVal;
            final int numberOfUniformSegments = (int) tmpCalculateNumberOfUniformSegments;

            final int tmpSize = (int) (totalCount / numberOfUniformSegments);
            final int tmpRemainder = (int) (totalCount % numberOfUniformSegments);

            final int tmpTotalNumberOfSegments = tmpRemainder == 0 ? numberOfUniformSegments : numberOfUniformSegments + 1;

            @SuppressWarnings("unchecked")
            final DenseArray<N>[] tmpSegments = new DenseArray[tmpTotalNumberOfSegments];
            for (int s = 0; s < numberOfUniformSegments; s++) {
                tmpSegments[s] = segementFactory.make(tmpSize);
            }
            if (tmpRemainder != 0) {
                tmpSegments[numberOfUniformSegments] = segementFactory.make(tmpRemainder);
            }

            return new SegmentedArray<N>(tmpSegments);
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

    public static SegmentedArray<BigDecimal> makeBig(final long count) {
        return BIG.makeZero(count);
    }

    public static SegmentedArray<ComplexNumber> makeComplex(final long count) {
        return COMPLEX.makeZero(count);
    }

    public static SegmentedArray<Double> makePrimitive(final long count) {
        return PRIMITIVE.makeZero(count);
    }

    public static SegmentedArray<RationalNumber> makeRational(final long count) {
        return RATIONAL.makeZero(count);
    }

    private final BasicArray<N>[] mySegments;

    /**
     * All segments except the last one are assumed to (must) be of equal length. The last segment cannot be longer than
     * the others.
     */
    private final long mySegmentSize;

    SegmentedArray(final BasicArray<N>[] segments) {

        super();

        mySegments = segments;
        mySegmentSize = segments[0].count();

    }

    @Override
    public long count() {
        final int tmpVal = mySegments.length - 1;
        return (mySegments[0].count() * tmpVal) + mySegments[tmpVal].count();
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
    protected boolean isZeros(final long first, final long limit, final long step) {
        boolean retVal = true;
        for (long i = first; retVal && (i < limit); i += step) {
            retVal &= this.isZero(i);
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
