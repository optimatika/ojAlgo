/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * BigMatrix
 *
 * @author apete
 */
public final class BigMatrix extends AbstractMatrix<BigDecimal, BigMatrix> {

    public static final BasicMatrix.Factory<BigMatrix> FACTORY = new MatrixFactory<>(BigMatrix.class, BigDenseStore.FACTORY);

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    BigMatrix(final MatrixStore<BigDecimal> aStore) {
        super(aStore);
    }

    public BigMatrix enforce(final NumberContext context) {
        return this.modify(context.getBigFunction());
    }

    public BigDecimal toBigDecimal(final int row, final int column) {
        return this.getStore().get(row, column);
    }

    @Override
    public PhysicalStore<BigDecimal> toBigStore() {
        return this.getStore().copy();
    }

    public ComplexNumber toComplexNumber(final int row, final int column) {
        return ComplexNumber.valueOf(this.getStore().doubleValue(row, column));
    }

    public String toString(final int row, final int col) {
        return this.toBigDecimal(row, col).toPlainString();
    }

    @SuppressWarnings("unchecked")
    @Override
    MatrixStore<BigDecimal> cast(final Access1D<?> matrix) {
        if (matrix instanceof BigMatrix) {
            return ((BigMatrix) matrix).getStore();
        } else if (matrix instanceof BigDenseStore) {
            return (BigDenseStore) matrix;
        } else if ((matrix instanceof MatrixStore) && !this.isEmpty() && (matrix.get(0) instanceof BigDecimal)) {
            return (MatrixStore<BigDecimal>) matrix;
        } else if (matrix instanceof Access2D<?>) {
            return this.getStore().physical().copy((Access2D<?>) matrix);
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
    SolverTask<BigDecimal> getSolverTask(final MatrixStore<BigDecimal> templateBody, final MatrixStore<BigDecimal> templateRHS) {
        return SolverTask.BIG.make(templateBody, templateRHS, this.isHermitian(), false);
    }

}
