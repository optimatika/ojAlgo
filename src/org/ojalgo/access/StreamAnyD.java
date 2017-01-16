package org.ojalgo.access;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;

public interface StreamAnyD<N extends Number, A extends AccessAnyD<N>, C extends MutateAnyD.Receiver<N>, P extends StreamAnyD<N, A, C, P>>
        extends StructureAnyD {

    default <I extends C> I collect(final FactoryAnyD<I> factory) {

        final I retVal = factory.makeZero(this.shape());

        this.supplyTo(retVal);

        return retVal;
    }

    P operateOnAll(final UnaryFunction<N> operator);

    P operateOnMatching(final A left, final BinaryFunction<N> operator);

    P operateOnMatching(final BinaryFunction<N> operator, final A right);

    void supplyTo(C consumer);

}
