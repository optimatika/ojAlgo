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
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;

/**
 * A matrix (linear algebra) with {@link ComplexNumber} elements.
 *
 * @see BasicMatrix
 * @author apete
 */
public final class ComplexMatrix extends BasicMatrix<ComplexNumber, ComplexMatrix> {

    public static final class DenseReceiver extends
            MatrixFactory<ComplexNumber, ComplexMatrix, ComplexMatrix.LogicalBuilder, ComplexMatrix.DenseReceiver, ComplexMatrix.SparseReceiver>.Mutator<PhysicalStore<ComplexNumber>> {

        DenseReceiver(final Factory enclosing, final PhysicalStore<ComplexNumber> delegate) {
            enclosing.super(delegate);
        }

    }

    public static final class Factory
            extends MatrixFactory<ComplexNumber, ComplexMatrix, ComplexMatrix.LogicalBuilder, ComplexMatrix.DenseReceiver, ComplexMatrix.SparseReceiver> {

        Factory() {
            super(ComplexMatrix.class, GenericStore.COMPLEX);
        }

        @Override
        ComplexMatrix.LogicalBuilder logical(final MatrixStore<ComplexNumber> delegate) {
            return new ComplexMatrix.LogicalBuilder(this, delegate);
        }

        @Override
        ComplexMatrix.DenseReceiver physical(final PhysicalStore<ComplexNumber> delegate) {
            return new ComplexMatrix.DenseReceiver(this, delegate);
        }

        @Override
        ComplexMatrix.SparseReceiver physical(final SparseStore<ComplexNumber> delegate) {
            return new ComplexMatrix.SparseReceiver(this, delegate);
        }

    }

    public static final class LogicalBuilder extends
            MatrixFactory<ComplexNumber, ComplexMatrix, ComplexMatrix.LogicalBuilder, ComplexMatrix.DenseReceiver, ComplexMatrix.SparseReceiver>.Logical {

        LogicalBuilder(final Factory enclosing, final MatrixStore.LogicalBuilder<ComplexNumber> delegate) {
            enclosing.super(delegate);
        }

        LogicalBuilder(final Factory enclosing, final MatrixStore<ComplexNumber> store) {
            enclosing.super(store);
        }

        @Override
        LogicalBuilder self() {
            return this;
        }

    }

    public static final class SparseReceiver extends
            MatrixFactory<ComplexNumber, ComplexMatrix, ComplexMatrix.LogicalBuilder, ComplexMatrix.DenseReceiver, ComplexMatrix.SparseReceiver>.Mutator<SparseStore<ComplexNumber>> {

        SparseReceiver(final Factory enclosing, final SparseStore<ComplexNumber> delegate) {
            enclosing.super(delegate);
        }

    }

    public static final Factory FACTORY = new Factory();

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    ComplexMatrix(final MatrixStore<ComplexNumber> aStore) {
        super(aStore);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ComplexMatrix.DenseReceiver copy() {
        return new ComplexMatrix.DenseReceiver(FACTORY, this.getStore().copy());
    }

    /**
     * @return A primitive double valued matrix containg this matrix' element arguments
     */
    public Primitive64Matrix getArgument() {
        return Primitive64Matrix.FACTORY.instantiate(Primitive64Store.getComplexArgument(this.getStore()));
    }

    /**
     * @return A primitive double valued matrix containg this matrix' element imaginary parts
     */
    public Primitive64Matrix getImaginary() {
        return Primitive64Matrix.FACTORY.instantiate(Primitive64Store.getComplexImaginary(this.getStore()));
    }

    /**
     * @return A primitive double valued matrix containg this matrix' element modulus
     */
    public Primitive64Matrix getModulus() {
        return Primitive64Matrix.FACTORY.instantiate(Primitive64Store.getComplexModulus(this.getStore()));
    }

    /**
     * @return A primitive double valued matrix containg this matrix' element real parts
     */
    public Primitive64Matrix getReal() {
        return Primitive64Matrix.FACTORY.instantiate(Primitive64Store.getComplexReal(this.getStore()));
    }

    @Override
    public ComplexMatrix.LogicalBuilder logical() {
        return new ComplexMatrix.LogicalBuilder(FACTORY, this.getStore());
    }

    @SuppressWarnings("unchecked")
    @Override
    ElementsSupplier<ComplexNumber> cast(final Access1D<?> matrix) {

        if (matrix instanceof ComplexMatrix) {

            return ((ComplexMatrix) matrix).getStore();

        } else if ((matrix instanceof ElementsSupplier) && (matrix.count() > 0L) && (matrix.get(0) instanceof ComplexNumber)) {

            return (ElementsSupplier<ComplexNumber>) matrix;

        } else if (matrix instanceof Access2D) {

            final Access2D<?> tmpAccess2D = (Access2D<?>) matrix;
            return this.getStore().physical().builder().makeWrapper(tmpAccess2D);

        } else {

            return this.getStore().physical().columns(matrix);
        }
    }

    @Override
    Cholesky<ComplexNumber> getDecompositionCholesky(final Structure2D typical) {
        return Cholesky.COMPLEX.make(typical);
    }

    @Override
    Eigenvalue<ComplexNumber> getDecompositionEigenvalue(final Structure2D typical) {
        return Eigenvalue.COMPLEX.make(typical, this.isHermitian());
    }

    @Override
    LDL<ComplexNumber> getDecompositionLDL(final Structure2D typical) {
        return LDL.COMPLEX.make(typical);
    }

    @Override
    LU<ComplexNumber> getDecompositionLU(final Structure2D typical) {
        return LU.COMPLEX.make(typical);
    }

    @Override
    QR<ComplexNumber> getDecompositionQR(final Structure2D typical) {
        return QR.COMPLEX.make(typical);
    }

    @Override
    SingularValue<ComplexNumber> getDecompositionSingularValue(final Structure2D typical) {
        return SingularValue.COMPLEX.make(typical);
    }

    @Override
    ComplexMatrix.Factory getFactory() {
        return FACTORY;
    }

    @Override
    DeterminantTask<ComplexNumber> getTaskDeterminant(final MatrixStore<ComplexNumber> template) {
        return DeterminantTask.COMPLEX.make(template, this.isHermitian(), false);
    }

    @Override
    InverterTask<ComplexNumber> getTaskInverter(final MatrixStore<ComplexNumber> base) {
        return InverterTask.COMPLEX.make(base, this.isHermitian(), false);
    }

    @Override
    SolverTask<ComplexNumber> getTaskSolver(final MatrixStore<ComplexNumber> templateBody, final Access2D<?> templateRHS) {
        return SolverTask.COMPLEX.make(templateBody, templateRHS, this.isHermitian(), false);
    }

}
