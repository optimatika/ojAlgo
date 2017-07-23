package org.ojalgo.array;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.machine.Hardware;
import org.ojalgo.random.Distribution;
import org.ojalgo.scalar.Scalar.Factory;

/**
 * To be used by implementations that delegate to a DenseArray
 *
 * @author apete
 */
final class DenseCapacityStrategy<N extends Number> {

    static long CHUNK = 512L;
    static long INITIAL = 8L;
    static long LIMIT = Long.MAX_VALUE;
    static long SEGMENT = 32_768L;

    /**
     * Will suggest an initial capacity (for a SparseArray) given the total count.
     */
    static int capacity(final long count) {

        double tmpInitialCapacity = count;

        while (tmpInitialCapacity > DenseArray.MAX_ARRAY_SIZE) {
            tmpInitialCapacity = PrimitiveFunction.SQRT.invoke(tmpInitialCapacity);
        }

        tmpInitialCapacity = PrimitiveFunction.SQRT.invoke(tmpInitialCapacity);
        return 2 * (int) tmpInitialCapacity;
    }

    private long myChunk = CHUNK;
    private final DenseArray.Factory<N> myDenseFactory;
    private long myInitial = INITIAL;
    private long myLimit = LIMIT;
    private long mySegment = SEGMENT;

    DenseCapacityStrategy(final DenseArray.Factory<N> denseFactory) {

        super();

        myDenseFactory = denseFactory;

        final long tmpHalfTopLevelCacheElements = (OjAlgoUtils.ENVIRONMENT.cache / 2L) / denseFactory.getElementSize();
        this.segment(tmpHalfTopLevelCacheElements);

        final long tmpMemoryPageElements = Hardware.OS_MEMORY_PAGE_SIZE / denseFactory.getElementSize();
        this.chunk(tmpMemoryPageElements);
    }

    private long expandToLarge(final long required) {

        long retVal = myChunk;

        if (required >= myChunk) {
            while (retVal < required) {
                retVal += myChunk;
            }
        } else {
            long maybe = retVal;
            while ((maybe /= 2L) >= required) {
                retVal = maybe;
            }
        }

        return retVal;
    }

    private long expandToSmall(final long required) {

        long maybe, retVal = myLimit;

        while ((maybe = Math.round(retVal / PrimitiveMath.GOLDEN_RATIO)) >= required) {
            retVal = maybe;
        }

        return retVal;

    }

    protected AggregatorSet<N> aggregator() {
        return myDenseFactory.aggregator();
    }

    protected FunctionSet<N> function() {
        return myDenseFactory.function();
    }

    protected Factory<N> scalar() {
        return myDenseFactory.scalar();
    }

    DenseCapacityStrategy<N> capacity(final Distribution countDistribution) {

        final double expected = countDistribution.getExpected();
        final double stdDev = countDistribution.getStandardDeviation();

        this.chunk((long) stdDev);

        this.initial((long) (expected - (stdDev + stdDev)));

        return this;
    }

    long chunk() {
        return myChunk;
    }

    DenseCapacityStrategy<N> chunk(final long chunk) {
        if (myLimit != LIMIT) {
            throw new IllegalStateException();
        }
        final int power = PrimitiveMath.powerOf2Smaller(Math.min(chunk, mySegment));
        myChunk = 1L << power;
        return this;
    }

    int grow(final int current) {
        return (int) this.grow((long) current);
    }

    long grow(final long current) {

        final long required = current + 1L;

        if (required > myLimit) {
            throw new IllegalStateException();
        }

        if (myLimit <= CHUNK) {

            return this.expandToSmall(required);

        } else {

            return this.expandToLarge(required);
        }
    }

    int initial() {
        return (int) myInitial;
    }

    /**
     * Enforced to be &gt;= 1
     */
    DenseCapacityStrategy<N> initial(final long initial) {
        if (myLimit != LIMIT) {
            throw new IllegalStateException();
        }
        myInitial = Math.max(1, initial);
        return this;
    }

    boolean isChunked(final long count) {
        return count > myChunk;
    }

    boolean isSegmented(final long count) {
        return count > mySegment;
    }

    long limit() {
        return myLimit;
    }

    DenseCapacityStrategy<N> limit(final long limit) {
        if (limit < myInitial) {
            throw new IllegalArgumentException();
        } else if (limit > myChunk) {
            throw new IllegalArgumentException();
        } else {
            myLimit = limit;
        }
        return this;
    }

    DenseArray<N> make(final long size) {
        return myDenseFactory.make(size);
    }

    DenseArray<N> makeChunk() {
        return this.make(myChunk);
    }

    DenseArray<N> makeInitial() {
        return this.make(myInitial);
    }

    DenseArray<N> makeSegment() {
        return this.make(mySegment);
    }

    SegmentedArray<N> makeSegmented(final BasicArray<N> segment) {
        if (segment.count() == mySegment) {
            return myDenseFactory.wrapAsSegments(segment, this.makeChunk());
        } else {
            throw new IllegalStateException();
        }
    }

    SegmentedArray<N> makeSegmented(final long count) {
        return myDenseFactory.makeSegmented(count);
    }

    long segment() {
        return mySegment;
    }

    DenseCapacityStrategy<N> segment(final long segment) {
        if (myLimit != LIMIT) {
            throw new IllegalStateException();
        }
        final int power = PrimitiveMath.powerOf2Smaller(Math.max(myChunk, segment));
        mySegment = 1L << power;
        return this;
    }

}
