package org.ojalgo.array;

import org.ojalgo.access.Access1D;

abstract class BuilderFactory<N extends Number, I extends Access1D<N>, BF extends BuilderFactory<N, I, BF>> {

    private final DenseStrategy<N> myStrategy;

    public BuilderFactory(final DenseArray.Factory<N> denseFactory) {

        super();

        myStrategy = new DenseStrategy<N>(denseFactory);
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
     * @param initial Sets the initial capacity.
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
