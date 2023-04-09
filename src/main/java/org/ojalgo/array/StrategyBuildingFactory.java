package org.ojalgo.array;

import org.ojalgo.function.FunctionSet;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.math.MathType;

public abstract class StrategyBuildingFactory<N extends Comparable<N>, I extends Access1D<N>, SB extends StrategyBuildingFactory<N, I, SB>> {

    private final DenseArray.Factory<N> myDenseFactory;
    private final GrowthStrategy.Builder myStrategyBuilder;

    public StrategyBuildingFactory(final DenseArray.Factory<N> denseFactory) {

        super();

        myDenseFactory = denseFactory;
        myStrategyBuilder = GrowthStrategy.newBuilder(denseFactory);
    }

    /**
     * @param chunk Defines a capacity break point. Below this point the capacity is doubled when needed.
     *        Above it, it is grown by adding one "chunk" at the time. Must be a power of 2. (The builder will
     *        enforce that for you.)
     * @return this
     */
    public SB chunk(final long chunk) {
        myStrategyBuilder.chunk(chunk);
        return (SB) this;
    }

    /**
     * @deprecated v53 Irrelevant. Maybe call {@link #initial(long)}, otherwise just don't call anything.
     */
    @Deprecated
    public SB fixed(final long fixed) {
        return this.initial(fixed).limit(fixed);
    }

    public FunctionSet<N> function() {
        return myDenseFactory.function();
    }

    public MathType getMathType() {
        return myDenseFactory.getMathType();
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
     * @deprecated v53 Doesn't do anything. No need to call. Will be removed.
     */
    @Deprecated
    public SB limit(final long limit) {
        return (SB) this;
    }

    public Scalar.Factory<N> scalar() {
        return myDenseFactory.scalar();
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

    DenseArray.Factory<N> getDenseFactory() {
        return myDenseFactory;
    }

    GrowthStrategy getGrowthStrategy() {
        return myStrategyBuilder.build();
    }
}
