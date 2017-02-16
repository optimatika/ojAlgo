package org.ojalgo.array;

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.random.Distribution;
import org.ojalgo.scalar.Scalar;

/**
 * To be used by implementations that delegate to a DenseArray
 *
 * @author apete
 */
final class DenseStrategy<N extends Number> {

    static long CHUNK = 512L;
    static long INITIAL = 16L;
    static long SEGMENT = 16_384L;

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
    }

    DenseStrategy<N> capacity(final Distribution expected) {
        final long stdDev = (long) expected.getStandardDeviation();
        final long exp = (long) expected.getExpected();
        return this.chunk(stdDev).initial(exp + stdDev);
    }

    DenseStrategy<N> chunk(final long chunk) {
        final int power = PrimitiveMath.powerOf2Smaller(Math.min(chunk, mySegment));
        myChunk = Math.max(INITIAL, 1L << power);
        return this;
    }

    int grow(final int current) {
        return (int) grow((long) current);
    }

    long grow(final long current) {

        long required = current + 1L;

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
     * Enforced to be 1 &lt;= initial
     */
    DenseStrategy<N> initial(final long initial) {
        myInitial = Math.max(1, initial);
        return this;
    }

    boolean isChunked(final long count) {
        return count >= myChunk;
    }

    boolean isSegmented(final long count) {
        return count >= mySegment;
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
        if (segment.count() == myChunk) {
            return myDenseFactory.wrapAsSegments(segment, this.makeChunk());
        } else {
            throw new IllegalStateException();
        }
    }

    DenseStrategy<N> segment(final long segment) {
        final int power = PrimitiveMath.powerOf2Smaller(Math.max(myChunk, segment));
        mySegment = Math.max(INITIAL, 1L << power);
        return this;
    }

    Scalar<N> zero() {
        return myDenseFactory.zero();
    }

}
