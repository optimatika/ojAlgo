/*
 * Copyright 1997-2019 Optimatika
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

import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;

final class GeneralisedEvD<N extends Number> extends EigenvalueDecomposition<N> implements Eigenvalue.Generalised<N> {

    private transient MatrixStore<N> myC = null;
    private final Cholesky<N> myCholesky;
    private final Eigenvalue<N> myEigenvalue;
    private final PhysicalStore.Factory<N, ? extends DecompositionStore<N>> myFactory;

    GeneralisedEvD(final PhysicalStore.Factory<N, ? extends DecompositionStore<N>> factory, final Cholesky<N> cholesky, final Eigenvalue<N> eigenvalue) {

        super(factory);

        myFactory = factory;
        myCholesky = cholesky;
        myEigenvalue = eigenvalue;
    }

    public boolean computeValuesOnly(final Collectable<N, ? super PhysicalStore<N>> matrixA, final Collectable<N, ? super PhysicalStore<N>> matrixB) {
        return myCholesky.decompose(matrixB) && this.computeValuesOnly(matrixA);
    }

    public boolean decompose(final Collectable<N, ? super PhysicalStore<N>> matrixA, final Collectable<N, ? super PhysicalStore<N>> matrixB) {
        return myCholesky.decompose(matrixB) && this.decompose(matrixA);
    }

    public N getDeterminant() {
        return myEigenvalue.getDeterminant();
    }

    public ComplexNumber getTrace() {
        return myEigenvalue.getTrace();
    }

    public boolean isHermitian() {
        return myEigenvalue.isHermitian();
    }

    public boolean isOrdered() {
        return myEigenvalue.isOrdered();
    }

    public boolean prepare(final Collectable<N, ? super PhysicalStore<N>> matrixB) {
        return myCholesky.decompose(matrixB);
    }

    public MatrixStore<N> reconstruct() {
        if (myC == null) {
            myC = myEigenvalue.reconstruct();
        }
        return myC;
    }

    @Override
    protected boolean doDecompose(final Collectable<N, ? super PhysicalStore<N>> matrix, final boolean valuesOnly) {
        myC = this.reduce(matrix);
        if (valuesOnly) {
            return myEigenvalue.computeValuesOnly(myC);
        } else {
            return myEigenvalue.decompose(myC);
        }
    }

    @Override
    protected MatrixStore<N> makeD() {
        return myEigenvalue.getD();
    }

    @Override
    protected Array1D<ComplexNumber> makeEigenvalues() {
        return myEigenvalue.getEigenvalues();
    }

    @Override
    protected MatrixStore<N> makeV() {
        return this.recover(myEigenvalue.getV());
    }

    MatrixStore<N> recover(final MatrixStore<N> reduced) {
        try {
            MatrixStore<N> mtrxR = myCholesky.getR();
            MatrixStore<Double> retVal = SolverTask.PRIMITIVE.solve(mtrxR, reduced);
            return (MatrixStore<N>) retVal;
        } catch (RecoverableCondition exception) {
            return null;
        }
    }

    MatrixStore<N> reduce(final Access2D.Collectable<N, ? super PhysicalStore<N>> original) {
        try {
            DecompositionStore<N> collected = original.collect(myFactory);
            MatrixStore<N> mtrxL = myCholesky.getL();
            MatrixStore<Double> step1 = SolverTask.PRIMITIVE.solve(mtrxL, collected);
            MatrixStore<Double> step2 = step1.transpose();
            MatrixStore<Double> step3 = SolverTask.PRIMITIVE.solve(mtrxL, step2);
            return (MatrixStore<N>) step3;
        } catch (RecoverableCondition exception) {
            return null;
        }
    }

}
