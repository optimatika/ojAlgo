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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access2D.Collectable;

abstract class DynamicEvD<N extends Comparable<N>> extends EigenvalueDecomposition<N> {

    static final class R064 extends DynamicEvD<Double> {

        R064() {
            super(Primitive64Store.FACTORY, new HermitianEvD.R064(), new GeneralEvD.R064());
        }

    }

    private final EigenvalueDecomposition<N> myGeneralDelegate;
    private boolean myHermitian = false;
    private final EigenvalueDecomposition<N> myHermitianDelegate;

    @SuppressWarnings("unused")
    private DynamicEvD(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> factory) {

        this(factory, null, null);

        ProgrammingError.throwForIllegalInvocation();
    }

    protected DynamicEvD(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> factory, final EigenvalueDecomposition<N> hermitianDelegate,
            final EigenvalueDecomposition<N> generalDelegate) {

        super(factory);

        myHermitianDelegate = hermitianDelegate;
        myGeneralDelegate = generalDelegate;
    }

    public boolean checkAndDecompose(final MatrixStore<N> matrix) {
        return this.decompose(matrix);
    }

    @Override
    public N getDeterminant() {
        if (myHermitian) {
            return myHermitianDelegate.getDeterminant();
        } else {
            return myGeneralDelegate.getDeterminant();
        }
    }

    public ComplexNumber getTrace() {
        if (myHermitian) {
            return myHermitianDelegate.getTrace();
        } else {
            return myGeneralDelegate.getTrace();
        }
    }

    public boolean isHermitian() {
        return myHermitian;
    }

    public boolean isOrdered() {
        return myHermitian ? myHermitianDelegate.isOrdered() : myGeneralDelegate.isOrdered();
    }

    @Override
    public void reset() {

        super.reset();

        myGeneralDelegate.reset();
        myHermitianDelegate.reset();
    }

    @Override
    protected boolean doDecompose(final Collectable<N, ? super PhysicalStore<N>> matrix, final boolean valuesOnly) {

        if (matrix instanceof MatrixStore) {
            myHermitian = ((MatrixStore<?>) matrix).isHermitian();
        } else {
            myHermitian = false;
        }

        if (myHermitian) {
            return myHermitianDelegate.doDecompose(matrix, valuesOnly);
        } else {
            return myGeneralDelegate.doDecompose(matrix, valuesOnly);
        }
    }

    @Override
    protected MatrixStore<N> makeD() {
        if (myHermitian) {
            return myHermitianDelegate.getD();
        } else {
            return myGeneralDelegate.getD();
        }
    }

    @Override
    protected Array1D<ComplexNumber> makeEigenvalues() {
        if (myHermitian) {
            return myHermitianDelegate.getEigenvalues();
        } else {
            return myGeneralDelegate.getEigenvalues();
        }
    }

    @Override
    protected MatrixStore<N> makeV() {
        if (myHermitian) {
            return myHermitianDelegate.getV();
        } else {
            return myGeneralDelegate.getV();
        }
    }

}
