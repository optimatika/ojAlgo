/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

abstract class DynamicEvD<N extends Number> extends EigenvalueDecomposition<N> {

    static final class Primitive extends DynamicEvD<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY, new HermitianEvD.Primitive(), new GeneralEvD.Primitive());
        }

    }

    private boolean myHermitian = false;
    private final EigenvalueDecomposition<N> myNonsymmetricDelegate;

    private final EigenvalueDecomposition<N> mySymmetricDelegate;

    @SuppressWarnings("unused")
    private DynamicEvD(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {

        this(aFactory, null, null);

        ProgrammingError.throwForIllegalInvocation();
    }

    protected DynamicEvD(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory, final EigenvalueDecomposition<N> aSymmetric,
            final EigenvalueDecomposition<N> aNonsymmetric) {

        super(aFactory);

        mySymmetricDelegate = aSymmetric;
        myNonsymmetricDelegate = aNonsymmetric;
    }

    public boolean equals(final MatrixStore<N> other, final NumberContext context) {
        if (myHermitian) {
            return mySymmetricDelegate.equals(other, context);
        } else {
            return myNonsymmetricDelegate.equals(other, context);
        }
    }

    @Override
    public N getDeterminant() {
        if (myHermitian) {
            return mySymmetricDelegate.getDeterminant();
        } else {
            return myNonsymmetricDelegate.getDeterminant();
        }
    }

    public ComplexNumber getTrace() {
        if (myHermitian) {
            return mySymmetricDelegate.getTrace();
        } else {
            return myNonsymmetricDelegate.getTrace();
        }
    }

    public boolean isHermitian() {
        return myHermitian;
    }

    public boolean isOrdered() {
        if (myHermitian) {
            return mySymmetricDelegate.isOrdered();
        } else {
            return myNonsymmetricDelegate.isOrdered();
        }
    }

    @Override
    public void reset() {

        super.reset();

        myNonsymmetricDelegate.reset();
        mySymmetricDelegate.reset();

        myHermitian = false;
    }

    @Override
    protected boolean doNonsymmetric(final ElementsSupplier<N> matrix, final boolean eigenvaluesOnly) {

        myHermitian = false;

        return myNonsymmetricDelegate.compute(matrix, false, eigenvaluesOnly);
    }

    @Override
    protected boolean doSymmetric(final ElementsSupplier<N> matrix, final boolean eigenvaluesOnly) {

        myHermitian = true;

        return mySymmetricDelegate.compute(matrix, true, eigenvaluesOnly);
    }

    @Override
    protected MatrixStore<N> makeD() {
        if (myHermitian) {
            return mySymmetricDelegate.getD();
        } else {
            return myNonsymmetricDelegate.getD();
        }
    }

    @Override
    protected Array1D<ComplexNumber> makeEigenvalues() {
        if (myHermitian) {
            return mySymmetricDelegate.getEigenvalues();
        } else {
            return myNonsymmetricDelegate.getEigenvalues();
        }
    }

    @Override
    protected MatrixStore<N> makeV() {
        if (myHermitian) {
            return mySymmetricDelegate.getV();
        } else {
            return myNonsymmetricDelegate.getV();
        }
    }

}
