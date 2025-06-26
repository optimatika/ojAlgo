package org.ojalgo.array;

import java.util.function.LongFunction;

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

    /**
     * 512
     */
    private static final long CHUNK = Hardware.OS_MEMORY_PAGE_SIZE / MathType.R064.getTotalMemory();
    private static final long INITIAL = 4L;
    private static final long SEGMENT = 32_768L;

    static GrowthStrategy from(final MathType mathType) {

        long elementSize = mathType.getTotalMemory();

        long halfTopLevelCacheElements = (OjAlgoUtils.ENVIRONMENT.cache / 2L) / elementSize;

        long memoryPageElements = Hardware.OS_MEMORY_PAGE_SIZE / elementSize;

        return new GrowthStrategy(INITIAL, memoryPageElements, halfTopLevelCacheElements);
    }

    private final long myChunk;
    private final long myInitial;
    private final long mySegment;

    GrowthStrategy() {
        this(INITIAL, CHUNK, SEGMENT);
    }

    GrowthStrategy(final long initial, final long chunk, final long segment) {
        super();
        myInitial = initial;
        myChunk = chunk;
        mySegment = segment;
    }

    long chunk() {
        return myChunk;
    }

    GrowthStrategy chunk(final long chunk) {

        long notLargerThanCurrentSegment = Math.min(chunk, mySegment);
        int power = PowerOf2.powerOf2Smaller(notLargerThanCurrentSegment);

        return new GrowthStrategy(myInitial, 1L << power, mySegment);
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

    /**
     * Enforced to be <= 1
     */
    GrowthStrategy initial(final long initial) {
        return new GrowthStrategy(Math.max(1, initial), myChunk, mySegment);
    }

    boolean isChunked(final long count) {
        return count > myChunk;
    }

    boolean isSegmented(final long count) {
        return count > mySegment;
    }

    <T> T makeChunk(final LongFunction<T> factory) {
        return factory.apply(myChunk);
    }

    <T> T makeInitial(final LongFunction<T> factory) {
        return factory.apply(myInitial);
    }

    <T> T makeSegment(final LongFunction<T> factory) {
        return factory.apply(mySegment);
    }

    long segment() {
        return mySegment;
    }

    GrowthStrategy segment(final long segment) {

        long notSmallerThanCurrentChunk = Math.max(myChunk, segment);
        int power = PowerOf2.powerOf2Smaller(notSmallerThanCurrentChunk);

        return new GrowthStrategy(myInitial, myChunk, 1L << power);
    }

}
