package org.ojalgo.array;

import org.ojalgo.access.Access1D;
import org.ojalgo.random.Distribution;

abstract class BuilderFactory<N extends Number, I extends Access1D<N>, BF extends BuilderFactory<N, I, BF>> {

    private final DenseStrategy<N> myStrategy;

    public BuilderFactory(final DenseArray.Factory<N> denseFactory) {

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
    public BF capacity(final Distribution countDistribution) {
        myStrategy.capacity(countDistribution);
        return (BF) this;
    }

    /**
     * @param chunk Defines the capacity break point. Below this point the capacity is doubled when needed.
     *        Above it, it is grown by adding one "chunk" at the time.
     * @return this
     */
    @SuppressWarnings("unchecked")
    public BF chunk(final long chunk) {
        myStrategy.chunk(chunk);
        return (BF) this;
    }

    /**
     * @param initial Sets the initial capacity of the "arrays" to be created using this factory.
     * @return this
     */
    @SuppressWarnings("unchecked")
    public BF initial(final long initial) {
        myStrategy.initial(initial);
        return (BF) this;
    }

    public abstract I make();

    DenseStrategy<N> getStrategy() {
        return myStrategy;
    }

}
