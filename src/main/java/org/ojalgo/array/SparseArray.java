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

import java.math.MathContext;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.LongStream;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Factory1D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * Only stores nonzero elements and/or elements specifically set by the user. The nonzero elements are stored
 * internally in a {@link DenseArray}.
 * </p>
 *
 * @author apete
 */
public final class SparseArray<N extends Comparable<N>> extends BasicArray<N> {

    @FunctionalInterface
    public interface NonzeroPrimitiveCallback {

        /**
         * @param index Index
         * @param value Value (nonzero) at that index
         */
        void call(long index, double value);

    }

    @FunctionalInterface
    public interface NonzeroReferenceTypeCallback<N extends Comparable<N>> {

        /**
         * @param index Index
         * @param number Number (nonzero) at that index
         */
        void call(long index, N number);

    }

    public static final class NonzeroView<N extends Comparable<N>> implements ElementView1D<N, NonzeroView<N>> {

        private int myCursor = -1;
        private final long[] myIndices;
        private final int myLastCursor;
        private final DenseArray<N> myValues;

        private NonzeroView(final long[] indices, final DenseArray<N> values, final int initial, final int last) {

            super();

            myIndices = indices;
            myValues = values;

            myCursor = initial;
            myLastCursor = last;
        }

        NonzeroView(final long[] indices, final DenseArray<N> values, final int actualLength) {
            this(indices, values, -1, actualLength - 1);
        }

        @Override
        public double doubleValue() {
            return myValues.doubleValue(myCursor);
        }

        @Override
        public long estimateSize() {
            return myLastCursor - myCursor;
        }

        @Override
        public void forEachRemaining(final Consumer<? super NonzeroView<N>> action) {

            // BasicLogger.debug("forEachRemaining [{}, {})", myCursor, myLastCursor);

            ElementView1D.super.forEachRemaining(action);
        }

        @Override
        public N get() {
            return myValues.get(myCursor);
        }

        @Override
        public boolean hasNext() {
            return myCursor < myLastCursor;
        }

        @Override
        public boolean hasPrevious() {
            return myCursor > 0;
        }

        @Override
        public long index() {
            return myIndices[myCursor];
        }

        @Override
        public NonzeroView<N> iterator() {
            return new NonzeroView<>(myIndices, myValues, -1, myLastCursor);
        }

        public void modify(final BinaryFunction<N> function, final double right) {
            myValues.set(myCursor, function.invoke(myValues.doubleValue(myCursor), right));
        }

        public void modify(final BinaryFunction<N> function, final N right) {
            myValues.set(myCursor, function.invoke(myValues.get(myCursor), right));
        }

        public void modify(final double left, final BinaryFunction<N> function) {
            myValues.set(myCursor, function.invoke(left, myValues.doubleValue(myCursor)));
        }

        public void modify(final N left, final BinaryFunction<N> function) {
            myValues.set(myCursor, function.invoke(left, myValues.get(myCursor)));
        }

        @Override
        public NonzeroView<N> next() {
            myCursor++;
            return this;
        }

        @Override
        public long nextIndex() {
            return myIndices[myCursor + 1];
        }

        @Override
        public NonzeroView<N> previous() {
            myCursor--;
            return this;
        }

        @Override
        public long previousIndex() {
            return myIndices[myCursor - 1];
        }

        @Override
        public boolean tryAdvance(final Consumer<? super NonzeroView<N>> action) {
            return ElementView1D.super.tryAdvance(action);
        }

        @Override
        public NonzeroView<N> trySplit() {

            final int remaining = myLastCursor - myCursor;

            if (remaining > 1) {

                final int split = myCursor + remaining / 2;

                // BasicLogger.debug("Splitting [{}, {}) into [{}, {}) and [{}, {})", myCursor, myLastCursor, myCursor, split, split, myLastCursor);

                final NonzeroView<N> retVal = new NonzeroView<>(myIndices, myValues, myCursor, split);

                myCursor = split;

                return retVal;

            }
            return null;
        }

    }

    public static final class SparseFactory<N extends Comparable<N>> extends StrategyBuildingFactory<N, SparseArray<N>, SparseFactory<N>>
            implements Factory1D<SparseArray<N>> {

        SparseFactory(final DenseArray.Factory<N> denseFactory) {
            super(denseFactory);
        }

        /**
         * @deprecated v53 This will create an array of maximum/infinite length. That's probably not what you
         *             want. Use {@link #make(long)} instead.
         */
        @Deprecated
        public SparseArray<N> make() {
            return this.make(Long.MAX_VALUE);
        }

        @Override
        public SparseArray<N> make(final long count) {
            return new SparseArray<>(this.getDenseFactory(), this.getGrowthStrategy(), count);
        }

    }

    private static final NumberContext MATH_CONTEXT = NumberContext.ofMath(MathContext.DECIMAL64);

    public static <N extends Comparable<N>> SparseFactory<N> factory(final DenseArray.Factory<N> denseFactory) {
        return new SparseFactory<>(denseFactory);
    }

    /**
     * The actual number of nonzwero elements
     */
    private int myActualLength = 0;
    private final long myCount;
    private long[] myIndices;
    private final DenseArray.Factory<N> myDenseFactory;
    private final GrowthStrategy myGrowthStrategy;
    private DenseArray<N> myValues;
    private final N myZeroNumber;
    private final Scalar<N> myZeroScalar;
    private final double myZeroValue;

    SparseArray(final DenseArray.Factory<N> denseFactory, final GrowthStrategy growthStrategy, final long count) {

        super(denseFactory);

        myCount = count;

        myDenseFactory = denseFactory;
        myGrowthStrategy = growthStrategy;

        myIndices = new long[growthStrategy.initial()];
        myValues = growthStrategy.makeInitial(denseFactory);

        myZeroScalar = denseFactory.scalar().zero();
        myZeroNumber = myZeroScalar.get();
        myZeroValue = myZeroScalar.doubleValue();
    }

    @Override
    public void add(final long index, final Comparable<?> addend) {
        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            myValues.add(tmpIndex, addend);
        } else {
            this.set(index, addend);
        }
    }

    @Override
    public void add(final long index, final double addend) {
        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            myValues.add(tmpIndex, addend);
        } else {
            this.set(index, addend);
        }
    }

    @Override
    public void add(final long index, final float addend) {
        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            myValues.add(tmpIndex, addend);
        } else {
            this.set(index, addend);
        }
    }

    @Override
    public void axpy(final double a, final Mutate1D.Modifiable<?> y) {
        for (int n = 0; n < myActualLength; n++) {
            y.add(myIndices[n], a * myValues.doubleValue(n));
        }
    }

    @Override
    public long count() {
        return myCount;
    }

    public long countNonzeros() {
        return myActualLength;
    }

    public long countZeros() {
        return myCount - myActualLength;
    }

    @Override
    public double dot(final Access1D<?> vector) {

        double retVal = PrimitiveMath.ZERO;

        for (int n = 0; n < myActualLength; n++) {
            retVal += myValues.doubleValue(n) * vector.doubleValue(myIndices[n]);
        }

        return retVal;
    }

    @Override
    public double doubleValue(final long index) {

        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            return this.doubleValueInternally(tmpIndex);
        }
        return myZeroValue;
    }

    @Override
    public double doubleValue(final int index) {

        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            return this.doubleValueInternally(tmpIndex);
        }
        return myZeroValue;
    }

    @Override
    public void fillAll(final N value) {

        if (PrimitiveScalar.isSmall(PrimitiveMath.ONE, NumberDefinition.doubleValue(value))) {

            myValues.fillAll(myZeroNumber);

        } else {

            // Bad idea...

            final int tmpSize = (int) this.count();

            if (tmpSize != myIndices.length) {
                myIndices = Structure1D.newIncreasingRange(0L, tmpSize);
                myValues = myDenseFactory.make(tmpSize);
                myActualLength = tmpSize;
            }

            myValues.fillAll(value);
        }
    }

    @Override
    public void fillAll(final NullaryFunction<?> supplier) {

        // Bad idea...

        final int tmpSize = (int) this.count();

        if (tmpSize != myIndices.length) {
            myIndices = Structure1D.newIncreasingRange(0L, tmpSize);
            myValues = myDenseFactory.make(tmpSize);
            myActualLength = tmpSize;
        }

        myValues.fillAll(supplier);
    }

    @Override
    public void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
        if (this.isPrimitive()) {
            this.set(index, values.doubleValue(valueIndex));
        } else {
            this.set(index, values.get(valueIndex));
        }
    }

    @Override
    public void fillOne(final long index, final N value) {
        this.set(index, value);
    }

    @Override
    public void fillOne(final long index, final NullaryFunction<?> supplier) {
        this.set(index, supplier.get());
    }

    @Override
    public void fillRange(final long first, final long limit, final N value) {
        this.fill(first, limit, 1L, value);
    }

    @Override
    public void fillRange(final long first, final long limit, final NullaryFunction<?> supplier) {
        this.fill(first, limit, 1L, supplier);
    }

    public long firstInRange(final long rangeFirst, final long rangeLimit) {
        int tmpFoundAt = this.index(rangeFirst);
        if (tmpFoundAt < 0) {
            tmpFoundAt = -(tmpFoundAt + 1);
        }
        if (tmpFoundAt >= myActualLength) {
            return rangeLimit;
        }
        return Math.min(myIndices[tmpFoundAt], rangeLimit);
    }

    @Override
    public N get(final long index) {

        final int tmpIndex = this.index(index);
        if (tmpIndex >= 0) {
            return this.getInternally(tmpIndex);
        }
        return myZeroNumber;
    }

    @Override
    public long indexOfLargest() {
        return myIndices[Math.toIntExact(myValues.indexOfLargest(0L, myActualLength, 1L))];
    }

    public long limitOfRange(final long rangeFirst, final long rangeLimit) {
        int tmpFoundAt = this.index(rangeLimit - 1L);
        if (tmpFoundAt < 0) {
            tmpFoundAt = -(tmpFoundAt + 2);
        }
        if (tmpFoundAt < 0) {
            return rangeFirst;
        }
        return Math.min(myIndices[tmpFoundAt] + 1L, rangeLimit);
    }

    @Override
    public void modifyAll(final UnaryFunction<N> modifier) {

        double zeroValue = modifier.invoke(myZeroValue);

        if (MATH_CONTEXT.isDifferent(myZeroValue, zeroValue)) {
            throw new IllegalArgumentException("SparseArray zero-value modification!");
        }

        myValues.modifyAll(modifier);
    }

    @Override
    public void modifyOne(final long index, final UnaryFunction<N> modifier) {
        this.set(index, modifier.invoke(this.get(index)));
    }

    @Override
    public NonzeroView<N> nonzeros() {
        return new NonzeroView<>(myIndices, myValues, myActualLength);
    }

    @Override
    public void reset() {
        myActualLength = 0;
        myValues.reset();
    }

    @Override
    public void set(final long index, final Comparable<?> value) {

        final int internalIndex = this.index(index);

        this.update(index, internalIndex, value, false);
    }

    @Override
    public void set(final long index, final double value) {

        final int internalIndex = this.index(index);

        this.update(index, internalIndex, value, false);
    }

    @Override
    public void set(final long index, final float value) {

        final int internalIndex = this.index(index);

        this.update(index, internalIndex, value, false);
    }

    public void supplyNonZerosTo(final Mutate1D consumer) {
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

    @Override
    public void visitOne(final long index, final VoidFunction<N> visitor) {
        if (this.isPrimitive()) {
            visitor.invoke(this.doubleValue(index));
        } else {
            visitor.invoke(this.get(index));
        }
    }

    public void visitPrimitiveNonzerosInRange(final long first, final long limit, final NonzeroPrimitiveCallback visitor) {

        int localFirst = this.index(first);
        if (localFirst < 0) {
            localFirst = -(localFirst + 1);
        }
        int localLimit = this.index(limit);
        if (localLimit < 0) {
            localLimit = -(localLimit + 1);
        }

        for (int i = localFirst; i < localLimit; i++) {
            visitor.call(myIndices[i], myValues.doubleValue(i));
        }
    }

    @Override
    public void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {

        int localFirst = this.index(first);
        if (localFirst < 0) {
            localFirst = -(localFirst + 1);
        }
        int localLimit = this.index(limit);
        if (localLimit < 0) {
            localLimit = -(localLimit + 1);
        }

        if (limit - first > localLimit - localFirst) {
            visitor.invoke(myZeroValue);
        }

        for (int i = localFirst; i < localLimit; i++) {
            myValues.visitOne(i, visitor);
        }
    }

    public void visitReferenceTypeNonzerosInRange(final long first, final long limit, final NonzeroReferenceTypeCallback<N> visitor) {

        int localFirst = this.index(first);
        if (localFirst < 0) {
            localFirst = -(localFirst + 1);
        }
        int localLimit = this.index(limit);
        if (localLimit < 0) {
            localLimit = -(localLimit + 1);
        }

        for (int i = localFirst; i < localLimit; i++) {
            visitor.call(myIndices[i], myValues.get(i));
        }
    }

    /**
     * Will never remove anything - just insert or update
     */
    private void update(final long externalIndex, final int internalIndex, final Comparable<?> value, final boolean shouldStoreZero) {

        if (internalIndex >= 0) {
            // Existing value, just update

            myValues.set(internalIndex, value);

        } else if (shouldStoreZero || !value.equals(myZeroNumber)) {
            // Not existing value, insert new
            final int tmpInsInd = -(internalIndex + 1);

            if (myActualLength + 1 <= myIndices.length) {
                // No need to grow the backing arrays

                for (int i = myActualLength; i > tmpInsInd; i--) {
                    myIndices[i] = myIndices[i - 1];
                    myValues.set(i, myValues.get(i - 1));
                }
                myIndices[tmpInsInd] = externalIndex;
                myValues.set(tmpInsInd, value);

            } else {
                // Needs to grow the backing arrays

                final int tmpCapacity = myGrowthStrategy.grow(myIndices.length);
                final long[] tmpIndices = new long[tmpCapacity];
                final DenseArray<N> tmpValues = myDenseFactory.make(tmpCapacity);

                for (int i = 0; i < tmpInsInd; i++) {
                    tmpIndices[i] = myIndices[i];
                    tmpValues.set(i, myValues.get(i));
                }
                tmpIndices[tmpInsInd] = externalIndex;
                tmpValues.set(tmpInsInd, value);
                for (int i = tmpInsInd; i < myIndices.length; i++) {
                    tmpIndices[i + 1] = myIndices[i];
                    tmpValues.set(i + 1, myValues.get(i));
                }
                for (int i = myIndices.length + 1; i < tmpIndices.length; i++) {
                    tmpIndices[i] = Long.MAX_VALUE;
                }

                myIndices = tmpIndices;
                myValues = tmpValues;
            }
            myActualLength++;
        }
    }

    /**
     * Will never remove anything - just insert or update
     */
    private void update(final long externalIndex, final int internalIndex, final double value, final boolean shouldStoreZero) {

        if (internalIndex >= 0) {
            // Existing value, just update

            myValues.set(internalIndex, value);

        } else if (shouldStoreZero || NumberContext.compare(value, PrimitiveMath.ZERO) != 0) {
            // Not existing value, insert new
            final int tmpInsInd = -(internalIndex + 1);

            if (myActualLength + 1 <= myIndices.length) {
                // No need to grow the backing arrays

                for (int i = myActualLength; i > tmpInsInd; i--) {
                    myIndices[i] = myIndices[i - 1];
                    myValues.set(i, myValues.doubleValue(i - 1));
                }
                myIndices[tmpInsInd] = externalIndex;
                myValues.set(tmpInsInd, value);

            } else {
                // Needs to grow the backing arrays

                final int tmpCapacity = myGrowthStrategy.grow(myIndices.length);
                final long[] tmpIndices = new long[tmpCapacity];
                final DenseArray<N> tmpValues = myDenseFactory.make(tmpCapacity);

                for (int i = 0; i < tmpInsInd; i++) {
                    tmpIndices[i] = myIndices[i];
                    tmpValues.set(i, myValues.doubleValue(i));
                }
                tmpIndices[tmpInsInd] = externalIndex;
                tmpValues.set(tmpInsInd, value);
                for (int i = tmpInsInd; i < myIndices.length; i++) {
                    tmpIndices[i + 1] = myIndices[i];
                    tmpValues.set(i + 1, myValues.doubleValue(i));
                }
                for (int i = myIndices.length + 1; i < tmpIndices.length; i++) {
                    tmpIndices[i] = Long.MAX_VALUE;
                }

                myIndices = tmpIndices;
                myValues = tmpValues;
            }
            myActualLength++;
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
        for (long i = first; i < limit; i += step) {
            this.fillOne(i, value);
        }
    }

    @Override
    protected void fill(final long first, final long limit, final long step, final NullaryFunction<?> supplier) {
        for (long i = first; i < limit; i += step) {
            this.fillOne(i, supplier);
        }
    }

    @Override
    protected long indexOfLargest(final long first, final long limit, final long step) {

        long retVal = first;
        double tmpLargest = PrimitiveMath.ZERO;
        double tmpValue;

        for (int i = 0; i < myIndices.length; i++) {
            final long tmpIndex = myIndices[i];
            if (tmpIndex >= first && tmpIndex < limit && (tmpIndex - first) % step == 0L) {
                tmpValue = PrimitiveMath.ABS.invoke(myValues.doubleValue(i));
                if (tmpValue > tmpLargest) {
                    tmpLargest = tmpValue;
                    retVal = i;
                }
            }
        }

        return retVal;
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final Access1D<N> left, final BinaryFunction<N> function) {

        final double tmpZeroValue = function.invoke(PrimitiveMath.ZERO, PrimitiveMath.ZERO);

        if (!PrimitiveScalar.isSmall(PrimitiveMath.ONE, tmpZeroValue)) {

            throw new IllegalArgumentException("SparseArray zero modification!");
        }
        for (int i = 0; i < myIndices.length; i++) {
            final long tmpIndex = myIndices[i];
            if (tmpIndex >= first && tmpIndex < limit && (tmpIndex - first) % step == 0L) {
                myValues.modify(tmpIndex, i, left, function);
            }
        }
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final BinaryFunction<N> function, final Access1D<N> right) {

        final double tmpZeroValue = function.invoke(PrimitiveMath.ZERO, PrimitiveMath.ZERO);

        if (!PrimitiveScalar.isSmall(PrimitiveMath.ONE, tmpZeroValue)) {

            throw new IllegalArgumentException("SparseArray zero modification!");
        }
        for (int i = 0; i < myIndices.length; i++) {
            final long tmpIndex = myIndices[i];
            if (tmpIndex >= first && tmpIndex < limit && (tmpIndex - first) % step == 0L) {
                myValues.modify(tmpIndex, i, function, right);
            }
        }
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final UnaryFunction<N> function) {

        final double tmpZeroValue = function.invoke(PrimitiveMath.ZERO);

        if (!PrimitiveScalar.isSmall(PrimitiveMath.ONE, tmpZeroValue)) {

            throw new IllegalArgumentException("SparseArray zero modification!");
        }
        for (int i = 0; i < myIndices.length; i++) {
            final long tmpIndex = myIndices[i];
            if (tmpIndex >= first && tmpIndex < limit && (tmpIndex - first) % step == 0L) {
                myValues.modify(tmpIndex, i, function);
            }
        }
    }

    @Override
    protected void visit(final long first, final long limit, final long step, final VoidFunction<N> visitor) {
        boolean tmpOnlyOnce = true;
        for (int i = 0; i < myIndices.length; i++) {
            final long tmpIndex = myIndices[i];
            if (tmpIndex >= first && tmpIndex < limit && (tmpIndex - first) % step == 0L) {
                myValues.visitOne(i, visitor);
            } else if (tmpOnlyOnce) {
                visitor.invoke(myZeroValue);
                tmpOnlyOnce = false;
            }
        }
    }

    long capacity() {
        return myValues.count();
    }

    DenseArray<N> densify() {

        final DenseArray<N> retVal = myDenseFactory.make((int) this.count());

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

    double doubleValueInternally(final int internalIndex) {
        return myValues.doubleValue(internalIndex);
    }

    long firstIndex() {
        return myIndices[0];
    }

    int getActualLength() {
        return myActualLength;
    }

    N getInternally(final int internalIndex) {
        return myValues.get(internalIndex);
    }

    DenseArray<N> getValues() {
        return myValues;
    }

    Access1D<N> getValues(final long fromIncl, final long toExcl) {

        int intFrom = this.index(fromIncl);
        if (intFrom < 0) {
            intFrom = -(intFrom + 1);
        }
        final int first = intFrom;

        int intTo = this.index(toExcl);
        if (intTo < 0) {
            intTo = -(intTo + 1);
        }
        final int limit = intTo;

        return new Access1D<>() {

            @Override
            public long count() {
                return limit - first;
            }

            @Override
            public double doubleValue(final int index) {
                return myValues.doubleValue(first + index);
            }

            @Override
            public double doubleValue(final long index) {
                return myValues.doubleValue(first + index);
            }

            @Override
            public N get(final long index) {
                return myValues.get(first + index);
            }

        };
    }

    int index(final long index) {
        return Arrays.binarySearch(myIndices, 0, myActualLength, index);
    }

    LongStream indices() {
        return Arrays.stream(myIndices, 0, myActualLength);
    }

    long lastIndex() {
        return myIndices[myActualLength - 1];
    }

    void put(final long key, final int index, final double value) {
        this.update(key, index, value, true);
    }

    void put(final long key, final int index, final N value) {
        this.update(key, index, value, true);
    }

    void remove(final long externalIndex, final int internalIndex) {

        if (internalIndex >= 0) {
            // Existing value, remove

            myActualLength--;

            if (myValues.isPrimitive()) {
                for (int i = internalIndex; i < myActualLength; i++) {
                    myIndices[i] = myIndices[i + 1];
                    myValues.set(i, myValues.doubleValue(i + 1));
                }
            } else {
                for (int i = internalIndex; i < myActualLength; i++) {
                    myIndices[i] = myIndices[i + 1];
                    myValues.set(i, myValues.get(i + 1));
                }
            }

        }
    }

    @Override
    public void set(final int index, final double value) {

        int internalIndex = this.index(index);

        this.update(index, internalIndex, value, false);
    }

}
