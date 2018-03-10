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

import java.math.BigDecimal;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;

/**
 * BigMatrix
 *
 * @author apete
 * @deprecated v45 Use {@link RationalMatrix} instead.
 */
@Deprecated
public final class BigMatrix extends AbstractMatrix<BigDecimal, BigMatrix> {

    /**
     * @deprecated v45 Use {@link RationalMatrix#FACTORY} instead.
     */
    @Deprecated
    public static final BasicMatrix.Factory<BigMatrix> FACTORY = new MatrixFactory<>(BigMatrix.class, BigDenseStore.FACTORY);

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    BigMatrix(final MatrixStore<BigDecimal> aStore) {
        super(aStore);
    }

    @SuppressWarnings("unchecked")
    @Override
    ElementsSupplier<BigDecimal> cast(final Access1D<?> matrix) {

        if (matrix instanceof BigMatrix) {

            return ((BigMatrix) matrix).getStore();

        } else if (matrix instanceof BigDenseStore) {

            return (BigDenseStore) matrix;

        } else if ((matrix instanceof ElementsSupplier) && (matrix.count() > 0L) && (matrix.get(0) instanceof BigDecimal)) {

            return (ElementsSupplier<BigDecimal>) matrix;

        } else if (matrix instanceof Access2D) {

            final Access2D<?> tmpAccess2D = (Access2D<?>) matrix;
            return this.getStore().physical().builder().makeWrapper(tmpAccess2D);

        } else {

            return this.getStore().physical().columns(matrix);
        }
    }

    @Override
    DeterminantTask<BigDecimal> getDeterminantTask(final MatrixStore<BigDecimal> template) {
        return DeterminantTask.BIG.make(template, this.isHermitian(), false);
    }

    @SuppressWarnings("unchecked")
    @Override
    MatrixFactory<BigDecimal, BigMatrix> getFactory() {
        return (MatrixFactory<BigDecimal, BigMatrix>) FACTORY;
    }

    @Override
    InverterTask<BigDecimal> getInverterTask(final MatrixStore<BigDecimal> base) {
        return InverterTask.BIG.make(base, this.isHermitian(), false);
    }

    @Override
    SolverTask<BigDecimal> getSolverTask(final MatrixStore<BigDecimal> templateBody, final Access2D<?> templateRHS) {
        return SolverTask.BIG.make(templateBody, templateRHS, this.isHermitian(), false);
    }

}
