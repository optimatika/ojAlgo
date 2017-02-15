package org.ojalgo.array;

import org.ojalgo.access.Access1D;

abstract class BuilderFactory<N extends Number, I extends Access1D<N>, BF extends BuilderFactory<N, I, BF>> {

    private final DenseStrategy<N> myStrategy;

    public BuilderFactory(final DenseArray.Factory<N> denseFactory) {

        super();

        myStrategy = new DenseStrategy<N>(denseFactory);
    }

    public abstract I make();

    DenseStrategy<N> getStrategy() {
        return myStrategy;
    }

    @SuppressWarnings("unchecked")
    public BF initial(long initial) {
        myStrategy.initial(initial);
        return (BF) this;
    }

    @SuppressWarnings("unchecked")
    public BF chunk(long chunk) {
        myStrategy.chunk(chunk);
        return (BF) this;
    }

}
