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
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.structure.Structure2D;

/**
 * A matrix (linear algebra) with Real {@link NumberSet#R} elements, approximated by 128-bit floating-point
 * values (implemented using dual 64-bit double). (2 x 64 = 128)
 *
 * @see BasicMatrix
 * @see Quadruple
 * @author apete
 */
public final class MatrixR128 extends BasicMatrix<Quadruple, MatrixR128> {

    public static final class DenseReceiver extends Mutator2D<Quadruple, MatrixR128, PhysicalStore<Quadruple>> {

        DenseReceiver(final PhysicalStore<Quadruple> delegate) {
            super(delegate);
        }

        @Override
        MatrixR128 instantiate(final MatrixStore<Quadruple> store) {
            return FACTORY.instantiate(store);
        }

    }

    public static final class Factory extends MatrixFactory<Quadruple, MatrixR128, MatrixR128.DenseReceiver, MatrixR128.SparseReceiver> {

        Factory() {
            super(MatrixR128.class, GenericStore.R128);
        }

        @Override
        MatrixR128.DenseReceiver dense(final PhysicalStore<Quadruple> store) {
            return new MatrixR128.DenseReceiver(store);
        }

        @Override
        MatrixR128.SparseReceiver sparse(final SparseStore<Quadruple> store) {
            return new MatrixR128.SparseReceiver(store);
        }

    }

    public static final class SparseReceiver extends Mutator2D<Quadruple, MatrixR128, SparseStore<Quadruple>> {

        SparseReceiver(final SparseStore<Quadruple> delegate) {
            super(delegate);
        }

        @Override
        MatrixR128 instantiate(final MatrixStore<Quadruple> store) {
            return FACTORY.instantiate(store);
        }

    }

    public static final Factory FACTORY = new Factory();

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    MatrixR128(final ElementsSupplier<Quadruple> supplier) {
        super(FACTORY.getPhysicalFactory(), supplier);
    }

    @Override
    public MatrixR128.DenseReceiver copy() {
        return new MatrixR128.DenseReceiver(this.store().copy());
    }

    @Override
    Cholesky<Quadruple> newCholesky(final Structure2D typical) {
        return Cholesky.R128.make(typical);
    }

    @Override
    DeterminantTask<Quadruple> newDeterminantTask(final Structure2D template) {
        return DeterminantTask.R128.make(template, this.isHermitian(), false);
    }

    @Override
    Eigenvalue<Quadruple> newEigenvalue(final Structure2D typical) {
        return Eigenvalue.R128.make(typical, this.isHermitian());
    }

    @Override
    MatrixR128 newInstance(final ElementsSupplier<Quadruple> store) {
        return new MatrixR128(store);
    }

    @Override
    InverterTask<Quadruple> newInverterTask(final Structure2D template) {
        return InverterTask.R128.make(template, this.isHermitian(), false);
    }

    @Override
    LDL<Quadruple> newLDL(final Structure2D typical) {
        return LDL.R128.make(typical);
    }

    @Override
    LU<Quadruple> newLU(final Structure2D typical) {
        return LU.R128.make(typical);
    }

    @Override
    QR<Quadruple> newQR(final Structure2D typical) {
        return QR.R128.make(typical);
    }

    @Override
    SingularValue<Quadruple> newSingularValue(final Structure2D typical) {
        return SingularValue.R128.make(typical);
    }

    @Override
    SolverTask<Quadruple> newSolverTask(final Structure2D templateBody, final Structure2D templateRHS) {
        return SolverTask.R128.make(templateBody, templateRHS, this.isHermitian(), false);
    }

}
