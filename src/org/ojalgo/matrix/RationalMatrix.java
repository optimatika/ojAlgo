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
package org.ojalgo.matrix;

import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;

/**
 * RationalMatrix
 *
 * @author apete
 */
public final class RationalMatrix extends BasicMatrix<RationalNumber, RationalMatrix> {

    public static final class DenseReceiver extends
            MatrixFactory<RationalNumber, RationalMatrix, RationalMatrix.LogicalBuilder, RationalMatrix.DenseReceiver, RationalMatrix.SparseReceiver>.DenseReceiver {

        DenseReceiver(Factory enclosing, PhysicalStore<RationalNumber> delegate) {
            enclosing.super(delegate);
        }

    }

    public static final class Factory
            extends MatrixFactory<RationalNumber, RationalMatrix, RationalMatrix.LogicalBuilder, RationalMatrix.DenseReceiver, RationalMatrix.SparseReceiver> {

        Factory() {
            super(RationalMatrix.class, GenericDenseStore.RATIONAL);
        }

        @Override
        RationalMatrix.LogicalBuilder logical(MatrixStore<RationalNumber> delegate) {
            return new RationalMatrix.LogicalBuilder(this, delegate);
        }

        @Override
        RationalMatrix.DenseReceiver physical(PhysicalStore<RationalNumber> delegate) {
            return new RationalMatrix.DenseReceiver(this, delegate);
        }

        @Override
        RationalMatrix.SparseReceiver physical(SparseStore<RationalNumber> delegate) {
            return new RationalMatrix.SparseReceiver(this, delegate);
        }

    }

    public static final class LogicalBuilder extends
            MatrixFactory<RationalNumber, RationalMatrix, RationalMatrix.LogicalBuilder, RationalMatrix.DenseReceiver, RationalMatrix.SparseReceiver>.Logical {

        LogicalBuilder(Factory enclosing, MatrixStore.LogicalBuilder<RationalNumber> delegate) {
            enclosing.super(delegate);
        }

        LogicalBuilder(Factory enclosing, MatrixStore<RationalNumber> store) {
            enclosing.super(store);
        }

        @Override
        LogicalBuilder self() {
            return this;
        }

    }

    public static final class SparseReceiver extends
            MatrixFactory<RationalNumber, RationalMatrix, RationalMatrix.LogicalBuilder, RationalMatrix.DenseReceiver, RationalMatrix.SparseReceiver>.SparseReceiver {

        SparseReceiver(Factory enclosing, SparseStore<RationalNumber> delegate) {
            enclosing.super(delegate);
        }

    }

    public static final Factory FACTORY = new Factory();

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    RationalMatrix(final MatrixStore<RationalNumber> aStore) {
        super(aStore);
    }

    @Override
    public RationalMatrix.DenseReceiver copy() {
        return new RationalMatrix.DenseReceiver(FACTORY, this.getStore().copy());
    }

    @Override
    public RationalMatrix.LogicalBuilder logical() {
        return new RationalMatrix.LogicalBuilder(FACTORY, this.getStore());
    }

    @SuppressWarnings("unchecked")
    @Override
    ElementsSupplier<RationalNumber> cast(final Access1D<?> matrix) {

        if (matrix instanceof RationalMatrix) {

            return ((RationalMatrix) matrix).getStore();

        } else if ((matrix instanceof ElementsSupplier) && (matrix.count() > 0L) && (matrix.get(0) instanceof RationalNumber)) {

            return (ElementsSupplier<RationalNumber>) matrix;

        } else if (matrix instanceof Access2D) {

            final Access2D<?> tmpAccess2D = (Access2D<?>) matrix;
            return this.getStore().physical().builder().makeWrapper(tmpAccess2D);

        } else {

            return this.getStore().physical().columns(matrix);
        }
    }

    @Override
    Eigenvalue<RationalNumber> getDecompositionEigenvalue(Structure2D typical) {
        return Eigenvalue.RATIONAL.make(typical, this.isHermitian());
    }

    @Override
    LU<RationalNumber> getDecompositionLU(Structure2D typical) {
        return LU.RATIONAL.make(typical);
    }

    @Override
    QR<RationalNumber> getDecompositionQR(Structure2D typical) {
        return QR.RATIONAL.make(typical);
    }

    @Override
    SingularValue<RationalNumber> getDecompositionSingularValue(Structure2D typical) {
        return SingularValue.RATIONAL.make(typical);
    }

    @Override
    Factory getFactory() {
        return FACTORY;
    }

    @Override
    DeterminantTask<RationalNumber> getTaskDeterminant(final MatrixStore<RationalNumber> template) {
        return DeterminantTask.RATIONAL.make(template, this.isHermitian(), false);
    }

    @Override
    InverterTask<RationalNumber> getTaskInverter(final MatrixStore<RationalNumber> template) {
        return InverterTask.RATIONAL.make(template, this.isHermitian(), false);
    }

    @Override
    SolverTask<RationalNumber> getTaskSolver(final MatrixStore<RationalNumber> templateBody, final Access2D<?> templateRHS) {
        return SolverTask.RATIONAL.make(templateBody, templateRHS, this.isHermitian(), false);
    }

}
