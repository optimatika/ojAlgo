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
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Structure2D;

/**
 * A matrix (linear algebra) with Complex {@link NumberSet#C} elements, implemented using dual 64-bit double
 * values. (2 x 64 = 128)
 *
 * @see BasicMatrix
 * @see ComplexNumber
 * @author apete
 */
public final class MatrixC128 extends BasicMatrix<ComplexNumber, MatrixC128> {

    public static final class DenseReceiver extends Mutator2D<ComplexNumber, MatrixC128, PhysicalStore<ComplexNumber>> {

        DenseReceiver(final PhysicalStore<ComplexNumber> delegate) {
            super(delegate);
        }

        @Override
        MatrixC128 instantiate(final MatrixStore<ComplexNumber> store) {
            return FACTORY.instantiate(store);
        }

    }

    public static final class Factory extends MatrixFactory<ComplexNumber, MatrixC128, MatrixC128.DenseReceiver, MatrixC128.SparseReceiver> {

        Factory() {
            super(MatrixC128.class, GenericStore.C128);
        }

        @Override
        MatrixC128.DenseReceiver dense(final PhysicalStore<ComplexNumber> store) {
            return new MatrixC128.DenseReceiver(store);
        }

        @Override
        MatrixC128.SparseReceiver sparse(final SparseStore<ComplexNumber> store) {
            return new MatrixC128.SparseReceiver(store);
        }

    }

    public static final class SparseReceiver extends Mutator2D<ComplexNumber, MatrixC128, SparseStore<ComplexNumber>> {

        SparseReceiver(final SparseStore<ComplexNumber> delegate) {
            super(delegate);
        }

        @Override
        MatrixC128 instantiate(final MatrixStore<ComplexNumber> store) {
            return FACTORY.instantiate(store);
        }

    }

    public static final Factory FACTORY = new Factory();

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    MatrixC128(final ElementsSupplier<ComplexNumber> supplier) {
        super(FACTORY.getPhysicalFactory(), supplier);
    }

    @Override
    public MatrixC128.DenseReceiver copy() {
        return new MatrixC128.DenseReceiver(this.store().copy());
    }

    /**
     * @return A primitive double valued matrix containg this matrix' element arguments
     */
    public MatrixR064 getArgument() {
        return MatrixR064.FACTORY.instantiate(Primitive64Store.getComplexArgument(this.store()));
    }

    /**
     * @return A primitive double valued matrix containg this matrix' element imaginary parts
     */
    public MatrixR064 getImaginary() {
        return MatrixR064.FACTORY.instantiate(Primitive64Store.getComplexImaginary(this.store()));
    }

    /**
     * @return A primitive double valued matrix containg this matrix' element modulus
     */
    public MatrixR064 getModulus() {
        return MatrixR064.FACTORY.instantiate(Primitive64Store.getComplexModulus(this.store()));
    }

    /**
     * @return A primitive double valued matrix containg this matrix' element real parts
     */
    public MatrixR064 getReal() {
        return MatrixR064.FACTORY.instantiate(Primitive64Store.getComplexReal(this.store()));
    }

    @Override
    Cholesky<ComplexNumber> newCholesky(final Structure2D typical) {
        return Cholesky.C128.make(typical);
    }

    @Override
    DeterminantTask<ComplexNumber> newDeterminantTask(final Structure2D template) {
        return DeterminantTask.C128.make(template, this.isHermitian(), false);
    }

    @Override
    Eigenvalue<ComplexNumber> newEigenvalue(final Structure2D typical) {
        return Eigenvalue.C128.make(typical, this.isHermitian());
    }

    @Override
    MatrixC128 newInstance(final ElementsSupplier<ComplexNumber> store) {
        return new MatrixC128(store);
    }

    @Override
    InverterTask<ComplexNumber> newInverterTask(final Structure2D base) {
        return InverterTask.C128.make(base, this.isHermitian(), false);
    }

    @Override
    LDL<ComplexNumber> newLDL(final Structure2D typical) {
        return LDL.C128.make(typical);
    }

    @Override
    LU<ComplexNumber> newLU(final Structure2D typical) {
        return LU.C128.make(typical);
    }

    @Override
    QR<ComplexNumber> newQR(final Structure2D typical) {
        return QR.C128.make(typical);
    }

    @Override
    SingularValue<ComplexNumber> newSingularValue(final Structure2D typical) {
        return SingularValue.C128.make(typical);
    }

    @Override
    SolverTask<ComplexNumber> newSolverTask(final Structure2D templateBody, final Structure2D templateRHS) {
        return SolverTask.C128.make(templateBody, templateRHS, this.isHermitian(), false);
    }

}
