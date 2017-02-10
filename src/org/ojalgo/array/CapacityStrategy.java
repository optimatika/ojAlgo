package org.ojalgo.array;

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.random.Distribution;

abstract class CapacityStrategy<CS extends CapacityStrategy<CS>> {

    private static long CHUNK_CAPACITY = 16_384L;
    private static long INITIAL_CAPACITY = 16L;

    private long myChunk = CHUNK_CAPACITY;

    CapacityStrategy() {
        super();
    }

    @SuppressWarnings("unchecked")
    CS chunk(long chunk) {
        int power = PrimitiveMath.powerOf2Smaller(chunk);
        myChunk = Math.max(INITIAL_CAPACITY, 1L << power);
        return (CS) this;
    }

    CS capacity(Distribution expected) {
        return this.chunk((long) expected.getStandardDeviation());
    }

    long grow(long current) {

        long required = current + 1L;

        long retVal = myChunk;

        if (required >= myChunk) {
            while (retVal < required) {
                required += myChunk;
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

}
