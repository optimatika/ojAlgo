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
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;

/**
 * A matrix (linear algebra) with {@link RationalNumber} elements.
 *
 * @see BasicMatrix
 * @author apete
 */
public final class RationalMatrix extends BasicMatrix<RationalNumber, RationalMatrix> {

    public static final class DenseReceiver extends Mutator2D<RationalNumber, RationalMatrix, PhysicalStore<RationalNumber>> {

        DenseReceiver(final PhysicalStore<RationalNumber> delegate) {
            super(delegate);
        }

        @Override
        RationalMatrix instantiate(final MatrixStore<RationalNumber> store) {
            return FACTORY.instantiate(store);
        }

    }

    public static final class Factory extends MatrixFactory<RationalNumber, RationalMatrix, RationalMatrix.DenseReceiver, RationalMatrix.SparseReceiver> {

        Factory() {
            super(RationalMatrix.class, GenericStore.RATIONAL);
        }

        @Override
        RationalMatrix.DenseReceiver dense(final PhysicalStore<RationalNumber> store) {
            return new RationalMatrix.DenseReceiver(store);
        }

        @Override
        RationalMatrix.SparseReceiver sparse(final SparseStore<RationalNumber> store) {
            return new RationalMatrix.SparseReceiver(store);
        }

    }

    public static final class LogicalBuilder extends Pipeline2D<RationalNumber, RationalMatrix, LogicalBuilder> {

        LogicalBuilder(final MatrixFactory<RationalNumber, RationalMatrix, ?, ?> factory, final ElementsSupplier<RationalNumber> supplier) {
            super(factory, supplier);
        }

        @Override
        LogicalBuilder wrap(final ElementsSupplier<RationalNumber> supplier) {
            return new LogicalBuilder(FACTORY, supplier);
        }

    }

    public static final class SparseReceiver extends Mutator2D<RationalNumber, RationalMatrix, SparseStore<RationalNumber>> {

        SparseReceiver(final SparseStore<RationalNumber> delegate) {
            super(delegate);
        }

        @Override
        RationalMatrix instantiate(final MatrixStore<RationalNumber> store) {
            return FACTORY.instantiate(store);
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
        return new RationalMatrix.DenseReceiver(this.getStore().copy());
    }

    @Override
    public RationalMatrix.LogicalBuilder logical() {
        return new RationalMatrix.LogicalBuilder(FACTORY, this.getStore());
    }

    @Override
    ElementsSupplier<RationalNumber> cast(final Access1D<?> matrix) {

        if (matrix instanceof RationalMatrix) {
            return ((RationalMatrix) matrix).getStore();
        }

        if (matrix instanceof ElementsSupplier && matrix.count() > 0L && matrix.get(0) instanceof RationalNumber) {
            return (ElementsSupplier<RationalNumber>) matrix;
        }

        if (matrix instanceof Access2D) {
            return this.getStore().physical().makeWrapper((Access2D<?>) matrix);
        }

        return this.getStore().physical().columns(matrix);
    }

    @Override
    Cholesky<RationalNumber> getDecompositionCholesky(final Structure2D typical) {
        return Cholesky.RATIONAL.make(typical);
    }

    @Override
    Eigenvalue<RationalNumber> getDecompositionEigenvalue(final Structure2D typical) {
        return Eigenvalue.RATIONAL.make(typical, this.isHermitian());
    }

    @Override
    LDL<RationalNumber> getDecompositionLDL(final Structure2D typical) {
        return LDL.RATIONAL.make(typical);
    }

    @Override
    LU<RationalNumber> getDecompositionLU(final Structure2D typical) {
        return LU.RATIONAL.make(typical);
    }

    @Override
    QR<RationalNumber> getDecompositionQR(final Structure2D typical) {
        return QR.RATIONAL.make(typical);
    }

    @Override
    SingularValue<RationalNumber> getDecompositionSingularValue(final Structure2D typical) {
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
