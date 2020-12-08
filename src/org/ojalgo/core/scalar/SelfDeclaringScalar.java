package org.ojalgo.core.scalar;

import org.ojalgo.core.type.context.NumberContext.Enforceable;

interface SelfDeclaringScalar<S extends SelfDeclaringScalar<S>> extends Scalar<S>, Enforceable<S> {

    @Override
    S add(double scalarAddend);

    @Override
    S add(float scalarAddend);

    @Override
    S add(S scalarAddend);

    @Override
    S conjugate();

    @Override
    S divide(double scalarDivisor);

    @Override
    S divide(float scalarDivisor);

    @Override
    S divide(S scalarDivisor);

    @Override
    S invert();

    @Override
    S multiply(double scalarMultiplicand);

    @Override
    S multiply(float scalarMultiplicand);

    @Override
    S multiply(S scalarMultiplicand);

    @Override
    S negate();

    @Override
    S power(int power);

    @Override
    S signum();

    @Override
    S subtract(double scalarSubtrahend);

    @Override
    S subtract(float scalarSubtrahend);

    @Override
    S subtract(S scalarSubtrahend);

}
