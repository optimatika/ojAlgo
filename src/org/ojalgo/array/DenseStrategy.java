package org.ojalgo.array;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.machine.Hardware;
import org.ojalgo.random.Distribution;
import org.ojalgo.scalar.Scalar;

/**
 * To be used by implementations that delegate to a DenseArray
 *
 * @author apete
 */
final class DenseStrategy<N extends Number> {

    static long CHUNK = 512L;
    static long INITIAL = 8L;
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
    private long mySegment = SEGMENT;

    DenseStrategy(final DenseArray.Factory<N> denseFactory) {

        super();

        myDenseFactory = denseFactory;

        final long tmpHalfTopLevelCacheElements = (OjAlgoUtils.ENVIRONMENT.cache / 2L) / denseFactory.getElementSize();
        this.segment(tmpHalfTopLevelCacheElements);

        final long tmpMemoryPageElements = Hardware.OS_MEMORY_PAGE_SIZE / denseFactory.getElementSize();
        this.chunk(tmpMemoryPageElements);
    }

    DenseStrategy<N> capacity(final Distribution countDistribution) {

        final double expected = countDistribution.getExpected();
        final double stdDev = countDistribution.getStandardDeviation();

        this.chunk((long) stdDev);

        this.initial((long) (expected - (stdDev + stdDev)));

        return this;
    }

    long chunk() {
        return myChunk;
    }

    DenseStrategy<N> chunk(final long chunk) {
        final int power = PrimitiveMath.powerOf2Smaller(Math.min(chunk, mySegment));
        myChunk = Math.max(INITIAL, 1L << power);
        return this;
    }

    int grow(final int current) {
        return (int) this.grow((long) current);
    }

    long grow(final long current) {

        final long required = current + 1L;

        long retVal = myChunk;

        if (required >= myChunk) {
            while (retVal < required) {
                retVal += myChunk;
            }
        } else {
            long maybe = retVal / 2L;
            while (maybe >= required) {
                retVal = maybe;
                maybe /= 2L;
            }
        }

        return retVal;
    }

    int initial() {
        return (int) myInitial;
    }

    /**
     * Enforced to be &gt;= 1
     */
    DenseStrategy<N> initial(final long initial) {
        myInitial = Math.max(1, initial);
        return this;
    }

    boolean isChunked(final long count) {
        return count > myChunk;
    }

    boolean isSegmented(final long count) {
        return count > mySegment;
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

    /**
     * Will be set to a multiple of {@link Hardware#OS_MEMORY_PAGE_SIZE} amd not {@code 0L}.
     */
    DenseStrategy<N> segment(final long segment) {
        final long tmpElementsPerPage = Hardware.OS_MEMORY_PAGE_SIZE / myDenseFactory.getElementSize();
        final long tmpNumberOfPages = Math.max(1L, Math.max(myChunk, segment) / tmpElementsPerPage);
        mySegment = tmpElementsPerPage * tmpNumberOfPages;
        return this;
    }

    Scalar<N> zero() {
        return myDenseFactory.zero();
    }

}
