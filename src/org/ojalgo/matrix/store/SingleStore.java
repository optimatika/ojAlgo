/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
        return new SingleStore<N>(this.factory(), this.factory().scalar().convert(myNumber).conjugate().getNumber());
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

    @Override
    public MatrixStore<N> multiply(final Access1D<N> right) {

        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = (int) (right.count() / this.getColDim());

        final PhysicalStore.Factory<N, ?> tmpFactory = this.factory();

        final PhysicalStore<N> retVal = tmpFactory.makeZero(tmpRowDim, tmpColDim);

        retVal.fillMatching(tmpFactory.function().multiply().first(myNumber), right);

        return retVal;
    }

    @Override
    public void supplyTo(final ElementsConsumer<N> consumer) {
        this.supplyNonZerosTo(consumer);
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return this.factory().scalar().convert(myNumber);
    }

    @Override
    public MatrixStore<N> transpose() {
        return this;
    }

    @Override
    protected void supplyNonZerosTo(final ElementsConsumer<N> consumer) {
        consumer.fillOne(0L, 0L, myNumber);
    }

}
