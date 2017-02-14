package org.ojalgo.array;

import org.ojalgo.access.Access1D;

abstract class BuilderFactory<N extends Number, I extends Access1D<N>> {

    private final DenseStrategy<N> myStrategy;

    public BuilderFactory(DenseArray.Factory<N> denseFactory) {

        super();

        myStrategy = new DenseStrategy<N>(denseFactory);
    }

    public abstract I make();

}
