package org.ojalgo.access;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;

public interface Stream2D<N extends Number, A extends Access2D<N>, C extends Mutate2D.Receiver<N>, P extends Stream2D<N, A, C, P>> extends Structure2D {

    default <I extends C> I collect(final Factory2D<I> factory) {

        final I retVal = factory.makeZero(this.countRows(), this.countColumns());

        this.supplyTo(retVal);

        return retVal;
    }

    P operateOnAll(final UnaryFunction<N> operator);

    P operateOnMatching(final A left, final BinaryFunction<N> operator);

    P operateOnMatching(final BinaryFunction<N> operator, final A right);

    void supplyTo(C consumer);

}
