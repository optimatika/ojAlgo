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

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.scalar.RationalNumber;

/**
 * RationalMatrix
 *
 * @author apete
 */
public final class RationalMatrix extends AbstractMatrix<RationalNumber, RationalMatrix> {

    public static final BasicMatrix.Factory<RationalMatrix> FACTORY = new MatrixFactory<>(RationalMatrix.class, GenericDenseStore.RATIONAL);

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    RationalMatrix(final MatrixStore<RationalNumber> aStore) {
        super(aStore);
    }

    @SuppressWarnings("unchecked")
    @Override
    ElementsSupplier<RationalNumber> cast(final Access1D<?> matrix) {

        if (matrix instanceof RationalMatrix) {

            return ((RationalMatrix) matrix).getStore();

        } else if (matrix instanceof GenericDenseStore) {

            return (GenericDenseStore<RationalNumber>) matrix;

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
    DeterminantTask<RationalNumber> getDeterminantTask(final MatrixStore<RationalNumber> template) {
        return DeterminantTask.RATIONAL.make(template, this.isHermitian(), false);
    }

    @SuppressWarnings("unchecked")
    @Override
    MatrixFactory<RationalNumber, RationalMatrix> getFactory() {
        return (MatrixFactory<RationalNumber, RationalMatrix>) FACTORY;
    }

    @Override
    InverterTask<RationalNumber> getInverterTask(final MatrixStore<RationalNumber> template) {
        return InverterTask.RATIONAL.make(template, this.isHermitian(), false);
    }

    @Override
    SolverTask<RationalNumber> getSolverTask(final MatrixStore<RationalNumber> templateBody, final Access2D<?> templateRHS) {
        return SolverTask.RATIONAL.make(templateBody, templateRHS, this.isHermitian(), false);
    }

}
