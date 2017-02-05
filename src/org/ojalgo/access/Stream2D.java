package org.ojalgo.access;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;

public interface Stream2D<N extends Number, A extends Access2D<N>, R extends Mutate2D.Receiver<N>, P extends Stream2D<N, A, R, P>>
        extends Access2D.Collectable<N, R> {

    P operateOnAll(final UnaryFunction<N> operator);

    P operateOnMatching(final A left, final BinaryFunction<N> operator);

    P operateOnMatching(final BinaryFunction<N> operator, final A right);

}
