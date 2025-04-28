package org.ojalgo.array;

import org.ojalgo.structure.Access1D;
import org.ojalgo.type.math.MathType;

abstract class StrategyBuildingFactory<N extends Comparable<N>, I extends Access1D<N>, SB extends StrategyBuildingFactory<N, I, SB>> {

    private final GrowthStrategy.Builder myStrategyBuilder;

    StrategyBuildingFactory(final MathType mathType) {

        super();

        myStrategyBuilder = GrowthStrategy.newBuilder(mathType);
    }

    /**
     * @param chunk Defines a capacity break point. Below this point the capacity is doubled when needed.
     *              Above it, it is grown by adding one "chunk" at the time. Must be a power of 2. (The
     *              builder will enforce that for you.)
     * @return this
     */
    public SB chunk(final long chunk) {
        myStrategyBuilder.chunk(chunk);
        return (SB) this;
    }

    /**
     * @param initial Sets the initial capacity of the "arrays" to be created using this factory.
     * @return this
     */
    public SB initial(final long initial) {
        myStrategyBuilder.initial(initial);
        return (SB) this;
    }

    /**
     * With very large data structures, particularly sparse ones, the underlying (dense) storage is segmented.
     * (Very large arrays are implemented as an array of arrays.) This determines the size/length of one such
     * segment. Must be a multiple of the chunk size as well as a power of 2. (The builder will enforce this
     * for you.)
     */
    public SB segment(final long segment) {
        myStrategyBuilder.segment(segment);
        return (SB) this;
    }

    GrowthStrategy getGrowthStrategy() {
        return myStrategyBuilder.build();
    }
}
