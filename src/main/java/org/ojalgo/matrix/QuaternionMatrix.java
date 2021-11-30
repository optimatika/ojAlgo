/*
 * Copyright 1997-2021 Optimatika
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
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;

/**
 * A matrix (linear algebra) with {@link Quaternion} elements.
 *
 * @see BasicMatrix
 * @author apete
 */
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
            super(QuaternionMatrix.class, GenericStore.QUATERNION);
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

    public static final class LogicalBuilder extends Pipeline2D<Quaternion, QuaternionMatrix, LogicalBuilder> {

        LogicalBuilder(final MatrixFactory<Quaternion, QuaternionMatrix, ?, ?> factory, final ElementsSupplier<Quaternion> supplier) {
            super(factory, supplier);
        }

        @Override
        LogicalBuilder wrap(final ElementsSupplier<Quaternion> supplier) {
            return new LogicalBuilder(FACTORY, supplier);
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
    QuaternionMatrix(final MatrixStore<Quaternion> store) {
        super(store);
    }

    @Override
    public QuaternionMatrix.DenseReceiver copy() {
        return new QuaternionMatrix.DenseReceiver(this.getStore().copy());
    }

    @Override
    public QuaternionMatrix.LogicalBuilder logical() {
        return new QuaternionMatrix.LogicalBuilder(FACTORY, this.getStore());
    }

    @Override
    ElementsSupplier<Quaternion> cast(final Access1D<?> matrix) {

        if (matrix instanceof QuaternionMatrix) {
            return ((QuaternionMatrix) matrix).getStore();
        }

        if (matrix instanceof ElementsSupplier && matrix.count() > 0L && matrix.get(0) instanceof Quaternion) {
            return (ElementsSupplier<Quaternion>) matrix;
        }

        if (matrix instanceof Access2D) {
            return this.getStore().physical().makeWrapper((Access2D<?>) matrix);
        }

        return this.getStore().physical().columns(matrix);
    }

    @Override
    QuaternionMatrix newInstance(final MatrixStore<Quaternion> store) {
        return new QuaternionMatrix(store);
    }

    @Override
    Cholesky<Quaternion> newCholesky(final Structure2D typical) {
        return Cholesky.QUATERNION.make(typical);
    }

    @Override
    DeterminantTask<Quaternion> newDeterminantTask(final MatrixStore<Quaternion> template) {
        return DeterminantTask.QUATERNION.make(template, this.isHermitian(), false);
    }

    @Override
    Eigenvalue<Quaternion> newEigenvalue(final Structure2D typical) {
        return Eigenvalue.QUATERNION.make(typical, this.isHermitian());
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
    SolverTask<Quaternion> newSolverTask(final MatrixStore<Quaternion> templateBody, final Access2D<?> templateRHS) {
        return SolverTask.QUATERNION.make(templateBody, templateRHS, this.isHermitian(), false);
    }

}
