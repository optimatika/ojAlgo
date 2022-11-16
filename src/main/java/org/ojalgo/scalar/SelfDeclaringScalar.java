package org.ojalgo.scalar;

import org.ojalgo.type.context.NumberContext.Enforceable;

interface SelfDeclaringScalar<S extends SelfDeclaringScalar<S>> extends Scalar<S>, Enforceable<S> {

    @Override
    S add(double scalarAddend);

    @Override
    default S add(final float scalarAddend) {
        return this.add((double) scalarAddend);
    }

    @Override
    S add(S scalarAddend);

    @Override
    S conjugate();

    @Override
    S divide(double scalarDivisor);

    @Override
    default S divide(final float scalarDivisor) {
        return this.divide((double) scalarDivisor);
    }

    @Override
    S divide(S scalarDivisor);

    @Override
    S invert();

    @Override
    S multiply(double scalarMultiplicand);

    @Override
    default S multiply(final float scalarMultiplicand) {
        return this.multiply((double) scalarMultiplicand);
    }

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
    default S subtract(final float scalarSubtrahend) {
        return this.subtract((double) scalarSubtrahend);
    }

    @Override
    S subtract(S scalarSubtrahend);

}
