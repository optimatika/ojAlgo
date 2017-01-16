package org.ojalgo.access;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;

public interface Stream1D<N extends Number, A extends Access1D<N>, C extends Mutate1D.Receiver<N>, P extends Stream1D<N, A, C, P>> extends Structure1D {

    default <I extends C> I collect(final Factory1D<I> factory) {

        final I retVal = factory.makeZero(this.count());

        this.supplyTo(retVal);

        return retVal;
    }

    P operateOnAll(final UnaryFunction<N> operator);

    P operateOnMatching(final A left, final BinaryFunction<N> operator);

    P operateOnMatching(final BinaryFunction<N> operator, final A right);

    void supplyTo(C consumer);

}
