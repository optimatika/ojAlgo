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
import org.ojalgo.access.Access2D;

/**
 * @author apete
 */
final class WrapperStore<N extends Number> extends FactoryStore<N> {

    private final Access2D<?> myAccess;

    private WrapperStore(final PhysicalStore.Factory<N, ?> factory, final int rowsCount, final int columnsCount) {
        super(factory, rowsCount, columnsCount);
        myAccess = null;
        ProgrammingError.throwForIllegalInvocation();
    }

    WrapperStore(final PhysicalStore.Factory<N, ?> factory, final Access2D<?> access) {

        super(factory, (int) access.countRows(), (int) access.countColumns());

        myAccess = access;
    }

    public double doubleValue(final long aRow, final long aCol) {
        return myAccess.doubleValue(aRow, aCol);
    }

    public N get(final long aRow, final long aCol) {
        return this.physical().scalar().cast(myAccess.get(aRow, aCol));
    }

    public void multiply(final Access1D<N> right, final ElementsConsumer<N> target) {
        // TODO Auto-generated method stub
        super.multiply(right, target);
    }

    public MatrixStore<N> multiply(final double scalar) {
        // TODO Auto-generated method stub
        return super.multiply(scalar);
    }

    public MatrixStore<N> multiply(final MatrixStore<N> right) {
        // TODO Auto-generated method stub
        return super.multiply(right);
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

}
