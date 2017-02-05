package org.ojalgo.access;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;

public interface Stream1D<N extends Number, A extends Access1D<N>, R extends Mutate1D.Receiver<N>, P extends Stream1D<N, A, R, P>>
        extends Access1D.Collectable<N, R> {

    P operateOnAll(final UnaryFunction<N> operator);

    P operateOnMatching(final A left, final BinaryFunction<N> operator);

    P operateOnMatching(final BinaryFunction<N> operator, final A right);

}
