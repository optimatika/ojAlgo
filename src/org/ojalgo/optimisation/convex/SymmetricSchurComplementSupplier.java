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
package org.ojalgo.optimisation.convex;

import java.util.Arrays;
import java.util.function.LongUnaryOperator;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.array.SparseArray;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.ElementsConsumer;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.PrimitiveScalar;

final class SymmetricSchurComplementSupplier implements ElementsSupplier<Double> {

    private final MatrixStore<Double> myAE;
    private final MatrixStore<Double> myFullAI;
    private int[] myIncluded;

    private final SparseArray<Double> mySparse;

    private final int myCountE;
    private final int myCountI;
    private final int myFullDim;

    SymmetricSchurComplementSupplier(final MatrixStore<Double> equalities, final MatrixStore<Double> inequalities) {

        super();

        myAE = equalities;
        myFullAI = inequalities;

        myCountE = (int) myAE.countRows();
        myCountI = (int) myFullAI.countRows();

        myFullDim = myCountE + myCountI;
        mySparse = SparseArray.makePrimitive(myFullDim * myFullDim);

    }

    void add(final int j, final Access1D<Double> column) {

        final PrimitiveDenseStore tmpColE = this.factory().makeZero(myCountE, 1L);
        final MatrixStore<Double> tmpProdE = myAE.multiply(column, tmpColE);

        for (int i = 0; i < myCountE; i++) {
            final double tmpVal = tmpProdE.doubleValue(i);
            // if (!PrimitiveScalar.isSmall(tmpVal, 1.0)) {
            mySparse.set(AccessUtils.index(myFullDim, i, j), tmpVal);
            // mySparse.set(AccessUtils.index(myFullDim, j, i), tmpVal);
            // }
        }

        final PrimitiveDenseStore tmpColI = this.factory().makeZero(myIncluded.length, 1L);
        final MatrixStore<Double> tmpProdI = myFullAI.builder().row(myIncluded).get().multiply(column, tmpColI);

        for (int _i = 0; _i < myIncluded.length; _i++) {
            final double tmpVal = tmpProdI.doubleValue(_i);
            if (!PrimitiveScalar.isSmall(tmpVal, 1.0)) {
                final int i = myCountE + myIncluded[_i];
                mySparse.set(AccessUtils.index(myFullDim, i, j), tmpVal);
                // mySparse.set(AccessUtils.index(myFullDim, j, i), tmpVal);
            }
        }

    }

    public long countColumns() {
        return myCountE + myCountI;
    }

    public long countRows() {
        return myCountE + myCountI;
    }

    public Factory<Double, PrimitiveDenseStore> factory() {
        return PrimitiveDenseStore.FACTORY;
    }

    public void supplyTo(final ElementsConsumer<Double> consumer) {

        consumer.fillAll(0.0);

        mySparse.supplyNonZerosTo(new ElementsConsumer<Double>() {

            public long countColumns() {
                // TODO Auto-generated method stub
                return 0;
            }

            public long countRows() {
                // TODO Auto-generated method stub
                return 0;
            }

            public long count() {
                // TODO Auto-generated method stub
                return 0;
            }

            public void accept(final Access2D<Double> t) {
                // TODO Auto-generated method stub

            }

            public void fillColumn(final long row, final long column, final Double value) {
                // TODO Auto-generated method stub

            }

            public void fillColumn(final long row, final long column, final NullaryFunction<Double> supplier) {
                // TODO Auto-generated method stub

            }

            public void fillDiagonal(final long row, final long column, final Double value) {
                // TODO Auto-generated method stub

            }

            public void fillDiagonal(final long row, final long column, final NullaryFunction<Double> supplier) {
                // TODO Auto-generated method stub

            }

            public void fillOne(final long row, final long column, final Double value) {
                // TODO Auto-generated method stub

            }

            public void fillOne(final long row, final long column, final NullaryFunction<Double> supplier) {

            }

            public void fillOneMatching(final long row, final long column, final Access1D<?> values, final long valueIndex) {
                // TODO Auto-generated method stub

            }

            public void fillRow(final long row, final long column, final Double value) {
                // TODO Auto-generated method stub

            }

            public void fillRow(final long row, final long column, final NullaryFunction<Double> supplier) {
                // TODO Auto-generated method stub

            }

            public void add(final long row, final long column, final double addend) {
                // TODO Auto-generated method stub

            }

            public void add(final long row, final long column, final Number addend) {
                // TODO Auto-generated method stub

            }

            public void set(final long row, final long column, final double value) {
                // TODO Auto-generated method stub
                consumer.set(row, column, value);
                consumer.set(column, row, value);
            }

            public void set(final long row, final long column, final Number value) {
                // TODO Auto-generated method stub

            }

            public void add(final long index, final double addend) {
                // TODO Auto-generated method stub

            }

            public void add(final long index, final Number addend) {
                // TODO Auto-generated method stub

            }

            public void set(final long index, final double value) {
                // TODO Auto-generated method stub

            }

            public void set(final long index, final Number value) {
                // TODO Auto-generated method stub

            }

            public void fillAll(final Double value) {
                // TODO Auto-generated method stub

            }

            public void fillAll(final NullaryFunction<Double> supplier) {
                // TODO Auto-generated method stub

            }

            public void fillOne(final long index, final Double value) {
                // TODO Auto-generated method stub

            }

            public void fillOne(final long index, final NullaryFunction<Double> supplier) {
                // TODO Auto-generated method stub

            }

            public void fillOneMatching(final long index, final Access1D<?> values, final long valueIndex) {
                // TODO Auto-generated method stub

            }

            public void fillRange(final long first, final long limit, final Double value) {
                // TODO Auto-generated method stub

            }

            public void fillRange(final long first, final long limit, final NullaryFunction<Double> supplier) {
                // TODO Auto-generated method stub

            }

            public void modifyColumn(final long row, final long column, final UnaryFunction<Double> function) {
                // TODO Auto-generated method stub

            }

            public void modifyDiagonal(final long row, final long column, final UnaryFunction<Double> function) {
                // TODO Auto-generated method stub

            }

            public void modifyOne(final long row, final long column, final UnaryFunction<Double> function) {
                // TODO Auto-generated method stub

            }

            public void modifyRow(final long row, final long column, final UnaryFunction<Double> function) {
                // TODO Auto-generated method stub

            }

            public void modifyAll(final UnaryFunction<Double> function) {
                // TODO Auto-generated method stub

            }

            public void modifyMatching(final Access1D<Double> left, final BinaryFunction<Double> function) {
                // TODO Auto-generated method stub

            }

            public void modifyMatching(final BinaryFunction<Double> function, final Access1D<Double> right) {
                // TODO Auto-generated method stub

            }

            public void modifyOne(final long index, final UnaryFunction<Double> function) {
                // TODO Auto-generated method stub

            }

            public void modifyRange(final long first, final long limit, final UnaryFunction<Double> function) {
                // TODO Auto-generated method stub

            }

            public void fillByMultiplying(final Access1D<Double> left, final Access1D<Double> right) {
                // TODO Auto-generated method stub

            }

            public ElementsConsumer<Double> regionByColumns(final int... columns) {
                // TODO Auto-generated method stub
                return null;
            }

            public ElementsConsumer<Double> regionByLimits(final int rowLimit, final int columnLimit) {
                // TODO Auto-generated method stub
                return null;
            }

            public ElementsConsumer<Double> regionByOffsets(final int rowOffset, final int columnOffset) {
                // TODO Auto-generated method stub
                return null;
            }

            public ElementsConsumer<Double> regionByRows(final int... rows) {
                // TODO Auto-generated method stub
                return null;
            }

            public ElementsConsumer<Double> regionByTransposing() {
                // TODO Auto-generated method stub
                return null;
            }

        }, new LongUnaryOperator() {

            public long applyAsLong(final long operand) {

                final int tmpSparseRow = AccessUtils.row(operand, myFullDim);
                final int tmpSparseCol = AccessUtils.column(operand, myFullDim);

                int tmpFound;

                int tmpDenseRow = -1;
                if (tmpSparseRow < myCountE) {
                    tmpDenseRow = tmpSparseRow;
                } else if ((tmpFound = Arrays.binarySearch(myIncluded, tmpSparseRow)) >= 0) {
                    tmpDenseRow = myCountE + tmpFound;
                }

                int tmpDenseCol = -1;
                if (tmpSparseCol < myCountE) {
                    tmpDenseCol = tmpSparseCol;
                } else if ((tmpFound = Arrays.binarySearch(myIncluded, tmpSparseCol)) >= 0) {
                    tmpDenseCol = myCountE + tmpFound;
                }

                return (tmpDenseRow < 0) || (tmpDenseCol < 0) ? -1 : AccessUtils.index(myCountE + myIncluded.length, tmpDenseRow, tmpDenseCol);
            }
        });
    }

    void update(final int[] included) {
        myIncluded = included;
    }

}
