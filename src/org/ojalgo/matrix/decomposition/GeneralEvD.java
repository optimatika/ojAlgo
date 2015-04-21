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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

abstract class GeneralEvD<N extends Number> extends EigenvalueDecomposition<N> {

    static final class Primitive extends GeneralEvD<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY, new HermitianEvD32.Primitive(), new NonsymmetricEvD.Primitive());
        }

    }

    private final EigenvalueDecomposition<N> myNonsymmetricDelegate;
    private final EigenvalueDecomposition<N> mySymmetricDelegate;

    private boolean mySymmetricOrNot = false;

    @SuppressWarnings("unused")
    private GeneralEvD(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {

        this(aFactory, null, null);

        ProgrammingError.throwForIllegalInvocation();
    }

    protected GeneralEvD(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory, final EigenvalueDecomposition<N> aSymmetric,
            final EigenvalueDecomposition<N> aNonsymmetric) {

        super(aFactory);

        mySymmetricDelegate = aSymmetric;
        myNonsymmetricDelegate = aNonsymmetric;
    }

    public final boolean compute(final Access2D<?> matrix, final boolean eigenvaluesOnly) {

        final boolean tmpSymmetric = MatrixUtils.isHermitian(matrix);

        return this.compute(matrix, tmpSymmetric, eigenvaluesOnly);
    }

    public boolean equals(final MatrixStore<N> other, final NumberContext context) {
        if (mySymmetricOrNot) {
            return mySymmetricDelegate.equals(other, context);
        } else {
            return myNonsymmetricDelegate.equals(other, context);
        }
    }

    @Override
    public N getDeterminant() {
        if (mySymmetricOrNot) {
            return mySymmetricDelegate.getDeterminant();
        } else {
            return myNonsymmetricDelegate.getDeterminant();
        }
    }

    public MatrixStore<N> getInverse() {
        if (mySymmetricOrNot) {
            return mySymmetricDelegate.getInverse();
        } else {
            return myNonsymmetricDelegate.getInverse();
        }
    }

    public MatrixStore<N> getInverse(final DecompositionStore<N> preallocated) {
        if (mySymmetricOrNot) {
            return mySymmetricDelegate.getInverse(preallocated);
        } else {
            return myNonsymmetricDelegate.getInverse(preallocated);
        }
    }

    public ComplexNumber getTrace() {
        if (mySymmetricOrNot) {
            return mySymmetricDelegate.getTrace();
        } else {
            return myNonsymmetricDelegate.getTrace();
        }
    }

    public boolean isFullSize() {
        if (mySymmetricOrNot) {
            return mySymmetricDelegate.isFullSize();
        } else {
            return myNonsymmetricDelegate.isFullSize();
        }
    }

    public boolean isHermitian() {
        if (mySymmetricOrNot) {
            return mySymmetricDelegate.isHermitian();
        } else {
            return myNonsymmetricDelegate.isHermitian();
        }
    }

    public boolean isOrdered() {
        if (mySymmetricOrNot) {
            return mySymmetricDelegate.isOrdered();
        } else {
            return myNonsymmetricDelegate.isOrdered();
        }
    }

    public boolean isSolvable() {
        if (mySymmetricOrNot) {
            return mySymmetricDelegate.isSolvable();
        } else {
            return myNonsymmetricDelegate.isSolvable();
        }
    }

    @Override
    public void reset() {

        super.reset();

        myNonsymmetricDelegate.reset();
        mySymmetricDelegate.reset();

        mySymmetricOrNot = false;
    }

    @Override
    protected boolean doNonsymmetric(final Access2D<?> aMtrx, final boolean eigenvaluesOnly) {

        mySymmetricOrNot = false;

        return myNonsymmetricDelegate.compute(aMtrx, false, eigenvaluesOnly);
    }

    @Override
    protected boolean doSymmetric(final Access2D<?> aMtrx, final boolean eigenvaluesOnly) {

        mySymmetricOrNot = true;

        return mySymmetricDelegate.compute(aMtrx, true, eigenvaluesOnly);
    }

    @Override
    protected MatrixStore<N> makeD() {
        if (mySymmetricOrNot) {
            return mySymmetricDelegate.getD();
        } else {
            return myNonsymmetricDelegate.getD();
        }
    }

    @Override
    protected Array1D<ComplexNumber> makeEigenvalues() {
        if (mySymmetricOrNot) {
            return mySymmetricDelegate.getEigenvalues();
        } else {
            return myNonsymmetricDelegate.getEigenvalues();
        }
    }

    @Override
    protected MatrixStore<N> makeV() {
        if (mySymmetricOrNot) {
            return mySymmetricDelegate.getV();
        } else {
            return myNonsymmetricDelegate.getV();
        }
    }

}
