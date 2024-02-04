/*
 * Copyright 1997-2024 Optimatika
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

import org.ojalgo.scalar.Scalar.Factory;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;

/**
 * @author apete
 */
final class WrapperStore<N extends Comparable<N>> extends FactoryStore<N> {

    private final Access1D<?> myAccess;
    private final Factory<N> myScalarFactory;
    private final int myStructure;

    WrapperStore(final Access1D<?> access1D, final PhysicalStore.Factory<N, ?> factory) {

        super(factory, access1D.size(), 1);

        myAccess = access1D;
        myScalarFactory = factory.scalar();
        myStructure = access1D.size();
    }

    WrapperStore(final PhysicalStore.Factory<N, ?> factory, final Access2D<?> access2D) {

        super(factory, access2D.getRowDim(), access2D.getColDim());

        myAccess = access2D;
        myScalarFactory = factory.scalar();
        myStructure = access2D.getRowDim();
    }

    @Override
    public double doubleValue(final int index) {
        return myAccess.doubleValue(index);
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return myAccess.doubleValue(Structure2D.index(myStructure, row, col));
    }

    @Override
    public N get(final int row, final int col) {
        return myScalarFactory.cast(myAccess.get(Structure2D.index(myStructure, row, col)));
    }

    @Override
    public N get(final long index) {
        return myScalarFactory.cast(myAccess.get(index));
    }

}
