/*
 * Copyright 1997-2013 Optimatika (www.optimatika.se)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.matrix.store;

import java.math.BigDecimal;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;

/**
 * IdentityStore
 * 
 * @author apete
 */
public final class IdentityStore<N extends Number> extends FactoryStore<N> {

    public static interface Factory<N extends Number> {

        IdentityStore<N> make(int dimension);

    }

    public static final Factory<BigDecimal> BIG = new Factory<BigDecimal>() {

        public IdentityStore<BigDecimal> make(final int dimension) {
            return IdentityStore.makeBig(dimension);
        }

    };

    public static final Factory<ComplexNumber> COMPLEX = new Factory<ComplexNumber>() {

        public IdentityStore<ComplexNumber> make(final int dimension) {
            return IdentityStore.makeComplex(dimension);
        }

    };

    public static final Factory<Double> PRIMITIVE = new Factory<Double>() {

        public IdentityStore<Double> make(final int dimension) {
            return IdentityStore.makePrimitive(dimension);
        }

    };

    public static IdentityStore<BigDecimal> makeBig(final int dimension) {
        return new IdentityStore<BigDecimal>(BigDenseStore.FACTORY, dimension);
    }

    public static IdentityStore<ComplexNumber> makeComplex(final int dimension) {
        return new IdentityStore<ComplexNumber>(ComplexDenseStore.FACTORY, dimension);
    }

    public static IdentityStore<Double> makePrimitive(final int dimension) {
        return new IdentityStore<Double>(PrimitiveDenseStore.FACTORY, dimension);
    }

    private final int myDim;

    public IdentityStore(final PhysicalStore.Factory<N, ?> aFactory, final int dimension) {

        super(dimension, dimension, aFactory);

        myDim = dimension;
    }

    @SuppressWarnings("unused")
    private IdentityStore(final PhysicalStore.Factory<N, ?> aFactory) {

        this(aFactory, 0);

        ProgrammingError.throwForIllegalInvocation();
    }

    @Override
    public PhysicalStore<N> conjugate() {
        return this.factory().makeEye(myDim, myDim);
    }

    @Override
    public PhysicalStore<N> copy() {
        return this.factory().makeEye(myDim, myDim);
    }

    public double doubleValue(final long aRow, final long aCol) {
        if (aRow == aCol) {
            return PrimitiveMath.ONE;
        } else {
            return PrimitiveMath.ZERO;
        }
    }

    public N get(final long aRow, final long aCol) {
        if (aRow == aCol) {
            return this.factory().scalar().one().getNumber();
        } else {
            return this.factory().scalar().zero().getNumber();
        }
    }

    public boolean isLowerLeftShaded() {
        return true;
    }

    public boolean isUpperRightShaded() {
        return true;
    }

    @Override
    public MatrixStore<N> multiplyLeft(final Access1D<N> leftMtrx) {
        if (leftMtrx.count() == this.getRowDim()) {
            return this.factory().rows(leftMtrx);
        } else if (leftMtrx instanceof MatrixStore<?>) {
            return ((MatrixStore<N>) leftMtrx).copy();
        } else {
            return super.multiplyLeft(leftMtrx);
        }
    }

    @Override
    public MatrixStore<N> multiplyRight(final Access1D<N> rightMtrx) {
        if (this.getColDim() == rightMtrx.count()) {
            return this.factory().columns(rightMtrx);
        } else if (rightMtrx instanceof MatrixStore<?>) {
            return ((MatrixStore<N>) rightMtrx).copy();
        } else {
            return super.multiplyRight(rightMtrx);
        }
    }

    public Scalar<N> toScalar(final long row, final long column) {
        if (row == column) {
            return this.factory().scalar().one();
        } else {
            return this.factory().scalar().zero();
        }
    }

    @Override
    public PhysicalStore<N> transpose() {
        return this.factory().makeEye(myDim, myDim);
    }

}
