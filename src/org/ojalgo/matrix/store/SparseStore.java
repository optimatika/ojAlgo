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
package org.ojalgo.matrix.store;

import java.math.BigDecimal;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.array.SparseArray;
import org.ojalgo.scalar.ComplexNumber;

public final class SparseStore<N extends Number> extends FactoryStore<N>implements Access2D.Settable<N> {

    public static interface Factory<N extends Number> {

        SparseStore<N> make(int rows, int columns);

    }

    public static final SparseStore.Factory<BigDecimal> BIG = new SparseStore.Factory<BigDecimal>() {

        public SparseStore<BigDecimal> make(final int rows, final int columns) {
            return SparseStore.makeBig(rows, columns);
        }

    };

    public static final SparseStore.Factory<ComplexNumber> COMPLEX = new SparseStore.Factory<ComplexNumber>() {

        public SparseStore<ComplexNumber> make(final int rows, final int columns) {
            return SparseStore.makeComplex(rows, columns);
        }

    };

    public static final SparseStore.Factory<Double> PRIMITIVE = new SparseStore.Factory<Double>() {

        public SparseStore<Double> make(final int rows, final int columns) {
            return SparseStore.makePrimitive(rows, columns);
        }

    };

    private static final int[] EMPTY_ROW = new int[0];

    public static SparseStore<BigDecimal> makeBig(final int rows, final int columns) {
        return new SparseStore<>(rows, columns, BigDenseStore.FACTORY, SparseArray.makeBig(rows * columns));
    }

    public static SparseStore<ComplexNumber> makeComplex(final int rows, final int columns) {
        return new SparseStore<>(rows, columns, ComplexDenseStore.FACTORY, SparseArray.makeComplex(rows * columns));
    }

    public static SparseStore<Double> makePrimitive(final int rows, final int columns) {
        return new SparseStore<>(rows, columns, PrimitiveDenseStore.FACTORY, SparseArray.makePrimitive(rows * columns));
    }

    private final SparseArray<N> myElements;
    private final int[] myFirsts;
    private final int[] myLimits;

    @SuppressWarnings("unused")
    private SparseStore(final int rowsCount, final int columnsCount, final org.ojalgo.matrix.store.PhysicalStore.Factory<N, ?> factory) {
        this(rowsCount, columnsCount, factory, null);
        ProgrammingError.throwForIllegalInvocation();
    }

    SparseStore(final int rowsCount, final int columnsCount, final PhysicalStore.Factory<N, ?> factory, final SparseArray<N> elements) {

        super(rowsCount, columnsCount, factory);

        myElements = elements;
        myFirsts = new int[rowsCount];
        myLimits = new int[rowsCount];
        for (int i = 0; i < rowsCount; i++) {
            myFirsts[i] = columnsCount;
            myLimits[i] = 0;
        }
    }

    public void add(final long row, final long col, final double addend) {
        myElements.add(AccessUtils.index(myFirsts.length, row, col), addend);
        this.updateNonZeros(row, col);
    }

    public void add(final long row, final long col, final Number addend) {
        myElements.add(AccessUtils.index(myFirsts.length, row, col), addend);
        this.updateNonZeros(row, col);
    }

    public double doubleValue(final long row, final long col) {
        return myElements.doubleValue(AccessUtils.index(myFirsts.length, row, col));
    }

    public N get(final long row, final long col) {
        return myElements.get(AccessUtils.index(myFirsts.length, row, col));
    }

    public void set(final long row, final long col, final double value) {
        myElements.set(AccessUtils.index(myFirsts.length, row, col), value);
        this.updateNonZeros(row, col);
    }

    public void set(final long row, final long col, final Number value) {
        myElements.set(AccessUtils.index(myFirsts.length, row, col), value);
        this.updateNonZeros(row, col);
    }

    private void updateNonZeros(final long row, final long col) {
        this.updateNonZeros((int) row, (int) col);
    }

    void updateNonZeros(final int row, final int col) {
        myFirsts[row] = Math.min(col, myFirsts[row]);
        myLimits[row] = Math.max(col, myLimits[row]);
    }

    public int firstInColumn(final int col) {

        final int tmpRowDim = myFirsts.length;

        final int tmpRangeFirst = tmpRowDim * col;
        final int tmpRangeLimit = tmpRowDim * (col + 1);

        final long tmpFirstInRange = myElements.firstInRange(tmpRangeFirst, tmpRangeLimit);

        if (tmpRangeFirst == tmpFirstInRange) {
            return 0;
        } else {
            return (int) (tmpFirstInRange % tmpRowDim);
        }
    }

    public int firstInRow(final int row) {
        return myFirsts[row];
    }

    @Override
    public int limitOfColumn(final int col) {

        final int tmpRowDim = myFirsts.length;

        final int tmpRangeFirst = tmpRowDim * col;
        final int tmpRangeLimit = tmpRowDim * (col + 1);

        final long tmpLimitOfRange = myElements.limitOfRange(tmpRangeFirst, tmpRangeLimit);

        if (tmpRangeLimit == tmpLimitOfRange) {
            return tmpRowDim;
        } else {
            return (int) tmpLimitOfRange % tmpRowDim;
        }
    }

    @Override
    public int limitOfRow(final int row) {
        return myLimits[row];
    }

}
