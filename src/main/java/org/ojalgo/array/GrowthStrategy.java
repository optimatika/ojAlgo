package org.ojalgo.array;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.PowerOf2;
import org.ojalgo.machine.Hardware;
import org.ojalgo.type.math.MathType;

/**
 * To be used by non fixed size data structures that delegate the actual storage to a DenseArray.
 *
 * @author apete
 */
final class GrowthStrategy {

    static final class Builder {

        /**
         * 512
         */
        private static final long CHUNK = Hardware.OS_MEMORY_PAGE_SIZE / MathType.R064.getTotalMemory();
        private static final long INITIAL = 4L;
        private static final long SEGMENT = 32_768L;

        private long myChunk = CHUNK;
        private long myInitial = INITIAL;
        private long mySegment = SEGMENT;

        Builder(final DenseArray.Factory<?> denseFactory) {
            this(denseFactory.getElementSize());

        }

        Builder(final long elementSize) {

            super();

            long halfTopLevelCacheElements = (OjAlgoUtils.ENVIRONMENT.cache / 2L) / elementSize;
            this.segment(halfTopLevelCacheElements);

            long memoryPageElements = Hardware.OS_MEMORY_PAGE_SIZE / elementSize;
            this.chunk(memoryPageElements);
        }

        GrowthStrategy build() {
            return new GrowthStrategy(this);
        }

        long chunk() {
            return myChunk;
        }

        Builder chunk(final long chunk) {
            long notLargerThanCurrentSegment = Math.min(chunk, mySegment);
            int power = PowerOf2.powerOf2Smaller(notLargerThanCurrentSegment);
            myChunk = 1L << power;
            return this;
        }

        int initial() {
            return (int) myInitial;
        }

        /**
         * Enforced to be &gt;= 1
         */
        Builder initial(final long initial) {
            myInitial = Math.max(1, initial);
            return this;
        }

        long segment() {
            return mySegment;
        }

        Builder segment(final long segment) {
            long notSmallerThanCurrentChunk = Math.max(myChunk, segment);
            int power = PowerOf2.powerOf2Smaller(notSmallerThanCurrentChunk);
            mySegment = 1L << power;
            return this;
        }

    }

    static Builder newBuilder(final DenseArray.Factory<?> denseFactory) {
        return new Builder(denseFactory);
    }

    static GrowthStrategy newInstance(final DenseArray.Factory<?> denseFactory) {
        return new Builder(denseFactory).build();
    }

    private final long myChunk;
    private final long myInitial;
    private final long mySegment;

    GrowthStrategy(final Builder builder) {

        super();

        myInitial = builder.initial();
        myChunk = builder.chunk();
        mySegment = builder.segment();
    }

    long chunk() {
        return myChunk;
    }

    int grow(final int current) {
        return Math.toIntExact(this.grow((long) current));
    }

    long grow(final long current) {

        long required = current + 1L;

        long retVal = myChunk;

        if (required >= myChunk) {
            while (retVal < required) {
                retVal += myChunk;
            }
        } else {
            long maybe = retVal;
            while ((maybe = Math.round(retVal / PrimitiveMath.GOLDEN_RATIO)) >= required) {
                retVal = maybe;
            }
        }

        return retVal;
    }

    int initial() {
        return (int) myInitial;
    }

    boolean isChunked(final long count) {
        return count > myChunk;
    }

    boolean isSegmented(final long count) {
        return count > mySegment;
    }

    <N extends Comparable<N>> DenseArray<N> makeChunk(final DenseArray.Factory<N> denseFactory) {
        return denseFactory.make(myChunk);
    }

    <N extends Comparable<N>> DenseArray<N> makeInitial(final DenseArray.Factory<N> denseFactory) {
        return denseFactory.make(myInitial);
    }

    <N extends Comparable<N>> DenseArray<N> makeSegment(final DenseArray.Factory<N> denseFactory) {
        return denseFactory.make(mySegment);
    }

    long segment() {
        return mySegment;
    }

}
