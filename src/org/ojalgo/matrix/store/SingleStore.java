/*
 * Copyright 1997-2018 Optimatika
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

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.scalar.Scalar;

final class SingleStore<N extends Number> extends FactoryStore<N> {

    private final N myNumber;
    private final double myValue;

    private SingleStore(final PhysicalStore.Factory<N, ?> factory, final int rowsCount, final int columnsCount) {
        super(factory, rowsCount, columnsCount);
        myNumber = null;
        myValue = 0;
        ProgrammingError.throwForIllegalInvocation();
    }

    SingleStore(final PhysicalStore.Factory<N, ?> factory, final Number element) {

        super(factory, 1, 1);

        myNumber = factory.scalar().cast(element);
        myValue = myNumber.doubleValue();
    }

    @Override
    public MatrixStore<N> conjugate() {
        return new SingleStore<>(this.physical(), this.physical().scalar().convert(myNumber).conjugate().get());
    }

    @Override
    public double doubleValue(final long anInd) {
        return myValue;
    }

    public double doubleValue(final long aRow, final long aCol) {
        return myValue;
    }

    public N get(final long aRow, final long aCol) {
        return myNumber;
    }

    public void multiply(final Access1D<N> right, final ElementsConsumer<N> target) {
        // TODO Auto-generated method stub
        super.multiply(right, target);
    }

    public MatrixStore<N> multiply(final double scalar) {
        // TODO Auto-generated method stub
        return super.multiply(scalar);
    }

    @Override
    public MatrixStore<N> multiply(final MatrixStore<N> right) {

        final PhysicalStore.Factory<N, ?> tmpFactory = this.physical();

        final PhysicalStore<N> retVal = tmpFactory.copy(right);

        retVal.modifyAll(tmpFactory.function().multiply().first(myNumber));

        return retVal;
    }

    public MatrixStore<N> multiply(final N scalar) {
        // TODO Auto-generated method stub
        return super.multiply(scalar);
    }

    @Override
    public N multiplyBoth(final Access1D<N> leftAndRight) {
        // TODO Auto-generated method stub
        return super.multiplyBoth(leftAndRight);
    }

    public ElementsSupplier<N> premultiply(final Access1D<N> left) {
        // TODO Auto-generated method stub
        return super.premultiply(left);
    }

    public void supplyTo(final ElementsConsumer<N> receiver) {
        receiver.fillOne(0L, 0L, myNumber);
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return this.physical().scalar().convert(myNumber);
    }

    @Override
    public MatrixStore<N> transpose() {
        return this;
    }

}
