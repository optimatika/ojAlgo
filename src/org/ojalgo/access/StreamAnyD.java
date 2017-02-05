package org.ojalgo.access;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;

public interface StreamAnyD<N extends Number, A extends AccessAnyD<N>, R extends MutateAnyD.Receiver<N>, P extends StreamAnyD<N, A, R, P>>
        extends AccessAnyD.Collectable<N, R> {

    P operateOnAll(final UnaryFunction<N> operator);

    P operateOnMatching(final A left, final BinaryFunction<N> operator);

    P operateOnMatching(final BinaryFunction<N> operator, final A right);

}
