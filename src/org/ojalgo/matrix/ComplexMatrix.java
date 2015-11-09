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
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * ComplexMatrix
 *
 * @author apete
 */
public final class ComplexMatrix extends AbstractMatrix<ComplexNumber, ComplexMatrix> {

    public static final BasicMatrix.Factory<ComplexMatrix> FACTORY = new MatrixFactory<ComplexNumber, ComplexMatrix>(ComplexMatrix.class,
            ComplexDenseStore.FACTORY);

    public static Builder<ComplexMatrix> getBuilder(final int aLength) {
        return FACTORY.getBuilder(aLength);
    }

    public static Builder<ComplexMatrix> getBuilder(final int aRowDim, final int aColDim) {
        return FACTORY.getBuilder(aRowDim, aColDim);
    }

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    ComplexMatrix(final MatrixStore<ComplexNumber> aStore) {
        super(aStore);
    }

    public ComplexMatrix enforce(final NumberContext context) {
        return this.modify(context.getComplexFunction());
    }

    /**
     * @return A primitive double valued matrix containg this matrix' element arguments
     */
    @SuppressWarnings("unchecked")
    public PrimitiveMatrix getArgument() {
        return ((MatrixFactory<Double, PrimitiveMatrix>) PrimitiveMatrix.FACTORY).instantiate(MatrixUtils.getComplexArgument(this.getStore()));
    }

    /**
     * @return A primitive double valued matrix containg this matrix' element imaginary parts
     */
    @SuppressWarnings("unchecked")
    public PrimitiveMatrix getImaginary() {
        return ((MatrixFactory<Double, PrimitiveMatrix>) PrimitiveMatrix.FACTORY).instantiate(MatrixUtils.getComplexImaginary(this.getStore()));
    }

    /**
     * @return A primitive double valued matrix containg this matrix' element modulus
     */
    @SuppressWarnings("unchecked")
    public PrimitiveMatrix getModulus() {
        return ((MatrixFactory<Double, PrimitiveMatrix>) PrimitiveMatrix.FACTORY).instantiate(MatrixUtils.getComplexModulus(this.getStore()));
    }

    /**
     * @return A primitive double valued matrix containg this matrix' element real parts
     */
    @SuppressWarnings("unchecked")
    public PrimitiveMatrix getReal() {
        return ((MatrixFactory<Double, PrimitiveMatrix>) PrimitiveMatrix.FACTORY).instantiate(MatrixUtils.getComplexReal(this.getStore()));
    }

    public BigDecimal toBigDecimal(final int row, final int column) {
        return new BigDecimal(this.getStore().doubleValue(row, column));
    }

    public ComplexNumber toComplexNumber(final int row, final int column) {
        return this.getStore().get(row, column);
    }

    @Override
    public PhysicalStore<ComplexNumber> toComplexStore() {
        return this.getStore().copy();
    }

    public String toString(final int row, final int col) {
        return this.toComplexNumber(row, col).toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    MatrixFactory<ComplexNumber, ComplexMatrix> getFactory() {
        return (MatrixFactory<ComplexNumber, ComplexMatrix>) FACTORY;
    }

    @SuppressWarnings("unchecked")
    @Override
    MatrixStore<ComplexNumber> getStoreFrom(final Access1D<?> aMtrx) {
        if (aMtrx instanceof ComplexMatrix) {
            return ((ComplexMatrix) aMtrx).getStore();
        } else if (aMtrx instanceof ComplexDenseStore) {
            return (ComplexDenseStore) aMtrx;
        } else if ((aMtrx instanceof MatrixStore) && !this.isEmpty() && (aMtrx.get(0) instanceof ComplexNumber)) {
            return (MatrixStore<ComplexNumber>) aMtrx;
        } else if (aMtrx instanceof Access2D<?>) {
            return this.getPhysicalFactory().copy((Access2D<?>) aMtrx);
        } else {
            return this.getPhysicalFactory().columns(aMtrx);
        }
    }

}
