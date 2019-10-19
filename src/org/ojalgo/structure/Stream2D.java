package org.ojalgo.structure;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.UnaryFunction;

public interface Stream2D<N extends Comparable<N>, A extends Access2D<N>, R extends Mutate2D.Receiver<N>, P extends Stream2D<N, A, R, P>>
        extends Access2D.Collectable<N, R> {

    default P operateOnAll(final BinaryFunction<N> operator, final double right) {
        return this.operateOnAll(operator.second(right));
    }

    default P operateOnAll(final BinaryFunction<N> operator, final N right) {
        return this.operateOnAll(operator.second(right));
    }

    default P operateOnAll(final double left, final BinaryFunction<N> operator) {
        return this.operateOnAll(operator.first(left));
    }

    default P operateOnAll(final N left, final BinaryFunction<N> operator) {
        return this.operateOnAll(operator.first(left));
    }

    default P operateOnAll(final ParameterFunction<N> operator, final int parameter) {
        return this.operateOnAll(operator.parameter(parameter));
    }

    P operateOnAll(UnaryFunction<N> operator);

    P operateOnAny(Transformation2D<N> operator);

    P operateOnColumns(BinaryFunction<N> operator, Access1D<N> right);

    P operateOnMatching(A left, BinaryFunction<N> operator);

    P operateOnMatching(BinaryFunction<N> operator, A right);

    P operateOnRows(BinaryFunction<N> operator, Access1D<N> right);

    P transpose();

}
