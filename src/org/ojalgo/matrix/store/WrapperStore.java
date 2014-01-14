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
import org.ojalgo.access.Access2D;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;

/**
 * @author apete
 */
public final class WrapperStore<N extends Number> extends FactoryStore<N> {

    public static interface Factory<N extends Number> {

        WrapperStore<N> make(Access2D<?> access);

    }

    public static final Factory<BigDecimal> BIG = new Factory<BigDecimal>() {

        public WrapperStore<BigDecimal> make(final Access2D<?> access) {
            return WrapperStore.makeBig(access);
        }

    };

    public static final Factory<ComplexNumber> COMPLEX = new Factory<ComplexNumber>() {

        public WrapperStore<ComplexNumber> make(final Access2D<?> access) {
            return WrapperStore.makeComplex(access);
        }

    };

    public static final Factory<Double> PRIMITIVE = new Factory<Double>() {

        public WrapperStore<Double> make(final Access2D<?> access) {
            return WrapperStore.makePrimitive(access);
        }

    };

    public static WrapperStore<BigDecimal> makeBig(final Access2D<?> access) {
        return new WrapperStore<BigDecimal>(BigDenseStore.FACTORY, access);
    }

    public static WrapperStore<ComplexNumber> makeComplex(final Access2D<?> access) {
        return new WrapperStore<ComplexNumber>(ComplexDenseStore.FACTORY, access);
    }

    public static WrapperStore<Double> makePrimitive(final Access2D<?> access) {
        return new WrapperStore<Double>(PrimitiveDenseStore.FACTORY, access);
    }

    private final Access2D<?> myAccess;

    public WrapperStore(final PhysicalStore.Factory<N, ?> factory, final Access2D<?> access) {

        super((int) access.countRows(), (int) access.countColumns(), factory);

        myAccess = access;
    }

    @SuppressWarnings("unused")
    private WrapperStore(final PhysicalStore.Factory<N, ?> aFactory) {

        this(aFactory, null);

        ProgrammingError.throwForIllegalInvocation();
    }

    public double doubleValue(final long aRow, final long aCol) {
        return myAccess.doubleValue(aRow, aCol);
    }

    public N get(final long aRow, final long aCol) {
        return this.factory().scalar().cast(myAccess.get(aRow, aCol));
    }

    public boolean isLowerLeftShaded() {
        return false;
    }

    public boolean isUpperRightShaded() {
        return false;
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return this.factory().scalar().convert(myAccess.get(row, column));
    }

}
