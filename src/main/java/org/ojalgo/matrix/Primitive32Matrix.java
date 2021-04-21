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
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive32Store;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;

/**
 * A matrix (linear algebra) with 32-bit primitive float elements.
 *
 * @see BasicMatrix
 * @author apete
 */
public final class Primitive32Matrix extends BasicMatrix<Double, Primitive32Matrix> {

    public static final class DenseReceiver extends
            MatrixFactory<Double, Primitive32Matrix, Primitive32Matrix.LogicalBuilder, Primitive32Matrix.DenseReceiver, Primitive32Matrix.SparseReceiver>.Mutator<PhysicalStore<Double>> {

        DenseReceiver(final Factory enclosing, final PhysicalStore<Double> delegate) {
            enclosing.super(delegate);
        }

    }

    public static final class Factory extends
            MatrixFactory<Double, Primitive32Matrix, Primitive32Matrix.LogicalBuilder, Primitive32Matrix.DenseReceiver, Primitive32Matrix.SparseReceiver> {

        Factory() {
            super(Primitive32Matrix.class, Primitive32Store.FACTORY);
        }

        @Override
        Primitive32Matrix.LogicalBuilder logical(final MatrixStore<Double> delegate) {
            return new Primitive32Matrix.LogicalBuilder(this, delegate);
        }

        @Override
        Primitive32Matrix.DenseReceiver physical(final PhysicalStore<Double> delegate) {
            return new Primitive32Matrix.DenseReceiver(this, delegate);
        }

        @Override
        Primitive32Matrix.SparseReceiver physical(final SparseStore<Double> delegate) {
            return new Primitive32Matrix.SparseReceiver(this, delegate);
        }

    }

    public static final class LogicalBuilder extends
            MatrixFactory<Double, Primitive32Matrix, Primitive32Matrix.LogicalBuilder, Primitive32Matrix.DenseReceiver, Primitive32Matrix.SparseReceiver>.Logical {

        LogicalBuilder(final Factory enclosing, final MatrixStore.LogicalBuilder<Double> delegate) {
            enclosing.super(delegate);
        }

        LogicalBuilder(final Factory enclosing, final MatrixStore<Double> store) {
            enclosing.super(store);
        }

        @Override
        LogicalBuilder self() {
            return this;
        }

    }

    public static final class SparseReceiver extends
            MatrixFactory<Double, Primitive32Matrix, Primitive32Matrix.LogicalBuilder, Primitive32Matrix.DenseReceiver, Primitive32Matrix.SparseReceiver>.Mutator<SparseStore<Double>> {

        SparseReceiver(final Factory enclosing, final SparseStore<Double> delegate) {
            enclosing.super(delegate);
        }

    }

    public static final Factory FACTORY = new Factory();

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    Primitive32Matrix(final MatrixStore<Double> aStore) {
        super(aStore);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Primitive32Matrix.DenseReceiver copy() {
        return new Primitive32Matrix.DenseReceiver(FACTORY, this.getStore().copy());
    }

    @Override
    public Primitive32Matrix.LogicalBuilder logical() {
        return new Primitive32Matrix.LogicalBuilder(FACTORY, this.getStore());
    }

    @SuppressWarnings("unchecked")
    @Override
    ElementsSupplier<Double> cast(final Access1D<?> matrix) {

        if (matrix instanceof Primitive32Matrix) {

            return ((Primitive32Matrix) matrix).getStore();

        } else if (matrix instanceof Primitive32Store) {

            return (Primitive32Store) matrix;

        } else if ((matrix instanceof ElementsSupplier) && (matrix.count() > 0L) && (matrix.get(0) instanceof Double)) {

            return (ElementsSupplier<Double>) matrix;

        } else if (matrix instanceof Access2D) {

            final Access2D<?> tmpAccess2D = (Access2D<?>) matrix;
            return this.getStore().physical().builder().makeWrapper(tmpAccess2D);

        } else {

            return this.getStore().physical().columns(matrix);
        }
    }

    @Override
    Cholesky<Double> getDecompositionCholesky(final Structure2D typical) {
        return Cholesky.PRIMITIVE.make(typical);
    }

    @Override
    Eigenvalue<Double> getDecompositionEigenvalue(final Structure2D typical) {
        return Eigenvalue.PRIMITIVE.make(typical, this.isHermitian());
    }

    @Override
    LDL<Double> getDecompositionLDL(final Structure2D typical) {
        return LDL.PRIMITIVE.make(typical);
    }

    @Override
    LU<Double> getDecompositionLU(final Structure2D typical) {
        return LU.PRIMITIVE.make(typical);
    }

    @Override
    QR<Double> getDecompositionQR(final Structure2D typical) {
        return QR.PRIMITIVE.make(typical);
    }

    @Override
    SingularValue<Double> getDecompositionSingularValue(final Structure2D typical) {
        return SingularValue.PRIMITIVE.make(typical);
    }

    @Override
    Factory getFactory() {
        return FACTORY;
    }

    @Override
    DeterminantTask<Double> getTaskDeterminant(final MatrixStore<Double> template) {
        return DeterminantTask.PRIMITIVE.make(template, this.isHermitian(), false);
    }

    @Override
    InverterTask<Double> getTaskInverter(final MatrixStore<Double> base) {
        return InverterTask.PRIMITIVE.make(base, this.isHermitian(), false);
    }

    @Override
    SolverTask<Double> getTaskSolver(final MatrixStore<Double> templateBody, final Access2D<?> templateRHS) {
        return SolverTask.PRIMITIVE.make(templateBody, templateRHS, this.isHermitian(), false);
    }

}
