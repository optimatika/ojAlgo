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
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Structure2D;

/**
 * @deprecated v53 Use {@link MatrixQ128} instead.
 */
@Deprecated
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
            super(RationalMatrix.class, GenericStore.Q128);
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
    RationalMatrix(final ElementsSupplier<RationalNumber> supplier) {
        super(FACTORY.getPhysicalFactory(), supplier);
    }

    @Override
    public RationalMatrix.DenseReceiver copy() {
        return new RationalMatrix.DenseReceiver(this.store().copy());
    }

    @Override
    Cholesky<RationalNumber> newCholesky(final Structure2D typical) {
        return Cholesky.RATIONAL.make(typical);
    }

    @Override
    DeterminantTask<RationalNumber> newDeterminantTask(final Structure2D template) {
        return DeterminantTask.RATIONAL.make(template, this.isHermitian(), false);
    }

    @Override
    Eigenvalue<RationalNumber> newEigenvalue(final Structure2D typical) {
        return Eigenvalue.RATIONAL.make(typical, this.isHermitian());
    }

    @Override
    RationalMatrix newInstance(final ElementsSupplier<RationalNumber> store) {
        return new RationalMatrix(store);
    }

    @Override
    InverterTask<RationalNumber> newInverterTask(final Structure2D template) {
        return InverterTask.RATIONAL.make(template, this.isHermitian(), false);
    }

    @Override
    LDL<RationalNumber> newLDL(final Structure2D typical) {
        return LDL.RATIONAL.make(typical);
    }

    @Override
    LU<RationalNumber> newLU(final Structure2D typical) {
        return LU.RATIONAL.make(typical);
    }

    @Override
    QR<RationalNumber> newQR(final Structure2D typical) {
        return QR.RATIONAL.make(typical);
    }

    @Override
    SingularValue<RationalNumber> newSingularValue(final Structure2D typical) {
        return SingularValue.RATIONAL.make(typical);
    }

    @Override
    SolverTask<RationalNumber> newSolverTask(final Structure2D templateBody, final Structure2D templateRHS) {
        return SolverTask.RATIONAL.make(templateBody, templateRHS, this.isHermitian(), false);
    }

}
