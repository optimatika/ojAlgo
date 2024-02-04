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
package org.ojalgo.matrix;

import org.ojalgo.algebra.NumberSet;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.LDL;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive32Store;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.structure.Structure2D;

/**
 * A matrix (linear algebra) with Real {@link NumberSet#R} elements, approximated by 32-bit float.
 *
 * @see BasicMatrix
 * @author apete
 */
public final class MatrixR032 extends BasicMatrix<Double, MatrixR032> {

    public static final class DenseReceiver extends Mutator2D<Double, MatrixR032, PhysicalStore<Double>> {

        DenseReceiver(final PhysicalStore<Double> delegate) {
            super(delegate);
        }

        @Override
        MatrixR032 instantiate(final MatrixStore<Double> store) {
            return FACTORY.instantiate(store);
        }

    }

    public static final class Factory extends MatrixFactory<Double, MatrixR032, MatrixR032.DenseReceiver, MatrixR032.SparseReceiver> {

        Factory() {
            super(MatrixR032.class, Primitive32Store.FACTORY);
        }

        @Override
        MatrixR032.DenseReceiver dense(final PhysicalStore<Double> store) {
            return new MatrixR032.DenseReceiver(store);
        }

        @Override
        MatrixR032.SparseReceiver sparse(final SparseStore<Double> store) {
            return new MatrixR032.SparseReceiver(store);
        }

    }

    public static final class SparseReceiver extends Mutator2D<Double, MatrixR032, SparseStore<Double>> {

        SparseReceiver(final SparseStore<Double> delegate) {
            super(delegate);
        }

        @Override
        MatrixR032 instantiate(final MatrixStore<Double> store) {
            return FACTORY.instantiate(store);
        }

    }

    public static final Factory FACTORY = new Factory();

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    MatrixR032(final ElementsSupplier<Double> supplier) {
        super(FACTORY.getPhysicalFactory(), supplier);
    }

    @Override
    public MatrixR032.DenseReceiver copy() {
        return new MatrixR032.DenseReceiver(this.store().copy());
    }

    @Override
    Cholesky<Double> newCholesky(final Structure2D typical) {
        return Cholesky.R064.make(typical);
    }

    @Override
    DeterminantTask<Double> newDeterminantTask(final Structure2D template) {
        return DeterminantTask.R064.make(template, this.isHermitian(), false);
    }

    @Override
    Eigenvalue<Double> newEigenvalue(final Structure2D typical) {
        return Eigenvalue.R064.make(typical, this.isHermitian());
    }

    @Override
    MatrixR032 newInstance(final ElementsSupplier<Double> store) {
        return new MatrixR032(store);
    }

    @Override
    InverterTask<Double> newInverterTask(final Structure2D base) {
        return InverterTask.R064.make(base, this.isHermitian(), false);
    }

    @Override
    LDL<Double> newLDL(final Structure2D typical) {
        return LDL.R064.make(typical);
    }

    @Override
    LU<Double> newLU(final Structure2D typical) {
        return LU.R064.make(typical);
    }

    @Override
    QR<Double> newQR(final Structure2D typical) {
        return QR.R064.make(typical);
    }

    @Override
    SingularValue<Double> newSingularValue(final Structure2D typical) {
        return SingularValue.R064.make(typical);
    }

    @Override
    SolverTask<Double> newSolverTask(final Structure2D templateBody, final Structure2D templateRHS) {
        return SolverTask.R064.make(templateBody, templateRHS, this.isHermitian(), false);
    }

}
