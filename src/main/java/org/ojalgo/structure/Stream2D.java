package org.ojalgo.structure;

public interface Stream2D<N extends Comparable<N>, A extends Access2D<N>, R extends Mutate2D.Receiver<N>, P extends Stream2D<N, A, R, P>>
        extends Operate2D<N, A, P>, Access2D.Collectable<N, R> {

    P transpose();

}
