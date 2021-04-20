package org.ojalgo.structure;

public interface StreamAnyD<N extends Comparable<N>, A extends AccessAnyD<N>, R extends MutateAnyD.Receiver<N>, P extends StreamAnyD<N, A, R, P>>
        extends OperateAnyD<N, A, P>, AccessAnyD.Collectable<N, R> {

}
