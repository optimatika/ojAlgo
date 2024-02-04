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
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.structure.Structure2D;

/**
 * @deprecated v53 Use {@link MatrixH256} instead.
 */
@Deprecated
public final class QuaternionMatrix extends BasicMatrix<Quaternion, QuaternionMatrix> {

    public static final class DenseReceiver extends Mutator2D<Quaternion, QuaternionMatrix, PhysicalStore<Quaternion>> {

        DenseReceiver(final PhysicalStore<Quaternion> delegate) {
            super(delegate);
        }

        @Override
        QuaternionMatrix instantiate(final MatrixStore<Quaternion> store) {
            return FACTORY.instantiate(store);
        }

    }

    public static final class Factory extends MatrixFactory<Quaternion, QuaternionMatrix, QuaternionMatrix.DenseReceiver, QuaternionMatrix.SparseReceiver> {

        Factory() {
            super(QuaternionMatrix.class, GenericStore.H256);
        }

        @Override
        QuaternionMatrix.DenseReceiver dense(final PhysicalStore<Quaternion> store) {
            return new QuaternionMatrix.DenseReceiver(store);
        }

        @Override
        QuaternionMatrix.SparseReceiver sparse(final SparseStore<Quaternion> store) {
            return new QuaternionMatrix.SparseReceiver(store);
        }

    }

    public static final class SparseReceiver extends Mutator2D<Quaternion, QuaternionMatrix, SparseStore<Quaternion>> {

        SparseReceiver(final SparseStore<Quaternion> delegate) {
            super(delegate);
        }

        @Override
        QuaternionMatrix instantiate(final MatrixStore<Quaternion> store) {
            return FACTORY.instantiate(store);
        }

    }

    public static final Factory FACTORY = new Factory();

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    QuaternionMatrix(final ElementsSupplier<Quaternion> supplier) {
        super(FACTORY.getPhysicalFactory(), supplier);
    }

    @Override
    public QuaternionMatrix.DenseReceiver copy() {
        return new QuaternionMatrix.DenseReceiver(this.store().copy());
    }

    @Override
    Cholesky<Quaternion> newCholesky(final Structure2D typical) {
        return Cholesky.QUATERNION.make(typical);
    }

    @Override
    DeterminantTask<Quaternion> newDeterminantTask(final Structure2D template) {
        return DeterminantTask.QUATERNION.make(template, this.isHermitian(), false);
    }

    @Override
    Eigenvalue<Quaternion> newEigenvalue(final Structure2D typical) {
        return Eigenvalue.QUATERNION.make(typical, this.isHermitian());
    }

    @Override
    QuaternionMatrix newInstance(final ElementsSupplier<Quaternion> store) {
        return new QuaternionMatrix(store);
    }

    @Override
    InverterTask<Quaternion> newInverterTask(final Structure2D template) {
        return InverterTask.QUATERNION.make(template, this.isHermitian(), false);
    }

    @Override
    LDL<Quaternion> newLDL(final Structure2D typical) {
        return LDL.QUATERNION.make(typical);
    }

    @Override
    LU<Quaternion> newLU(final Structure2D typical) {
        return LU.QUATERNION.make(typical);
    }

    @Override
    QR<Quaternion> newQR(final Structure2D typical) {
        return QR.QUATERNION.make(typical);
    }

    @Override
    SingularValue<Quaternion> newSingularValue(final Structure2D typical) {
        return SingularValue.QUATERNION.make(typical);
    }

    @Override
    SolverTask<Quaternion> newSolverTask(final Structure2D templateBody, final Structure2D templateRHS) {
        return SolverTask.QUATERNION.make(templateBody, templateRHS, this.isHermitian(), false);
    }

}
