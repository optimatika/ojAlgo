package org.ojalgo.structure;

public interface Stream1D<N extends Comparable<N>, A extends Access1D<N>, R extends Mutate1D.Receiver<N>, P extends Stream1D<N, A, R, P>>
        extends Operate1D<N, A, P>, Access1D.Collectable<N, R> {

}
