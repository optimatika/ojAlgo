package org.ojalgo.array;

import org.ojalgo.access.Access1D;

abstract class StrategyBuilder<N extends Number, I extends Access1D<N>, SB extends StrategyBuilder<N, I, SB>> {

    private final DenseCapacityStrategy<N> myStrategy;

    public StrategyBuilder(final DenseArray.Factory<N> denseFactory) {

        super();

        myStrategy = new DenseCapacityStrategy<>(denseFactory);
    }

    /**
     * @param chunk Defines the capacity break point. Below this point the capacity is doubled when needed.
     *        Above it, it is grown by adding one "chunk" at the time.
     * @return this
     */
    @SuppressWarnings("unchecked")
    public SB chunk(final long chunk) {
        myStrategy.chunk(chunk);
        return (SB) this;
    }

    public SB fixed(final long fixed) {
        return this.initial(fixed).limit(fixed);
    }

    /**
     * @param initial Sets the initial capacity of the "arrays" to be created using this factory.
     * @return this
     */
    @SuppressWarnings("unchecked")
    public SB initial(final long initial) {
        myStrategy.initial(initial);
        return (SB) this;
    }

    /**
     * @param limit Defines a maximum size. Only set this if you know the precise max size, and it should be
     *        something relatively small. Setting the max size is meant as an alternative to setting any/all
     *        of the other paramaters, and will switch to a tighter capacity strategy. The only other
     *        configuration you may want to set in combination with this one is the initial capacity (set that
     *        first in that case).
     * @return this
     */
    @SuppressWarnings("unchecked")
    public SB limit(final long limit) {
        myStrategy.limit(limit);
        return (SB) this;
    }

    public abstract I make();

    DenseCapacityStrategy<N> getStrategy() {
        return myStrategy;
    }

}
