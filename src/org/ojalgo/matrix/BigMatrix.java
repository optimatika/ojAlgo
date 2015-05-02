/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * BigMatrix
 *
 * @author apete
 */
public final class BigMatrix extends AbstractMatrix<BigDecimal, BigMatrix> {

    public static final BasicMatrix.Factory<BigMatrix> FACTORY = new MatrixFactory<BigDecimal, BigMatrix>(BigMatrix.class, BigDenseStore.FACTORY);

    public static Builder<BigMatrix> getBuilder(final int aLength) {
        return FACTORY.getBuilder(aLength);
    }

    public static Builder<BigMatrix> getBuilder(final int aRowDim, final int aColDim) {
        return FACTORY.getBuilder(aRowDim, aColDim);
    }

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
    MatrixFactory<BigDecimal, BigMatrix> getFactory() {
        return (MatrixFactory<BigDecimal, BigMatrix>) FACTORY;
    }

    @SuppressWarnings("unchecked")
    @Override
    MatrixStore<BigDecimal> getStoreFrom(final Access1D<?> aMtrx) {
        if (aMtrx instanceof BigMatrix) {
            return ((BigMatrix) aMtrx).getStore();
        } else if (aMtrx instanceof BigDenseStore) {
            return (BigDenseStore) aMtrx;
        } else if ((aMtrx instanceof MatrixStore) && !this.isEmpty() && (aMtrx.get(0) instanceof BigDecimal)) {
            return (MatrixStore<BigDecimal>) aMtrx;
        } else if (aMtrx instanceof Access2D<?>) {
            return this.getPhysicalFactory().copy((Access2D<?>) aMtrx);
        } else {
            return this.getPhysicalFactory().columns(aMtrx);
        }
    }

}
