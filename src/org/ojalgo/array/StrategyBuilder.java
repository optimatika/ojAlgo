package org.ojalgo.array;

import org.ojalgo.access.Access1D;
import org.ojalgo.random.Distribution;

abstract class StrategyBuilder<N extends Number, I extends Access1D<N>, SB extends StrategyBuilder<N, I, SB>> {

    private final DenseStrategy<N> myStrategy;

    public StrategyBuilder(final DenseArray.Factory<N> denseFactory) {

        super();

        myStrategy = new DenseStrategy<N>(denseFactory);
    }

    /**
     * Updates both the initial capacity and the capacity chunk.
     *
     * @param countDistribution A probability distribution that estimates the count/size of the "arrays" to be
     *        created using this factory.
     * @return this
     */
    @SuppressWarnings("unchecked")
    public SB capacity(final Distribution countDistribution) {
        myStrategy.capacity(countDistribution);
        return (SB) this;
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

    /**
     * @param initial Sets the initial capacity of the "arrays" to be created using this factory.
     * @return this
     */
    @SuppressWarnings("unchecked")
    public SB initial(final long initial) {
        myStrategy.initial(initial);
        return (SB) this;
    }

    DenseStrategy<N> getStrategy() {
        return myStrategy;
    }

    public abstract I make();

}
