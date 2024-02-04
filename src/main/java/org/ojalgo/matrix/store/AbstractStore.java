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
package org.ojalgo.matrix.store;

import org.ojalgo.ProgrammingError;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;

abstract class AbstractStore<N extends Comparable<N>> implements MatrixStore<N> {

    @SafeVarargs
    static <N extends Comparable<N>> MatrixStore<N> buildColumn(final PhysicalStore.Factory<N, ?> factory, final long rowsCount,
            final Access2D<N>... columnStores) {
        MatrixStore<N> retVal = AbstractStore.cast(factory, columnStores[0]);
        for (int i = 1; i < columnStores.length; i++) {
            retVal = new AboveBelowStore<>(retVal, AbstractStore.cast(factory, columnStores[i]));
        }
        long rowsSoFar = retVal.countRows();
        if (rowsSoFar < rowsCount) {
            retVal = new AboveBelowStore<>(retVal, new ZeroStore<>(retVal.physical(), rowsCount - rowsSoFar, retVal.countColumns()));
        }
        return retVal;
    }

    static <N extends Comparable<N>> MatrixStore<N> buildColumn(final PhysicalStore.Factory<N, ?> factory, final long rowsCount,
            final Access2D<N> columnStore) {
        MatrixStore<N> retVal = AbstractStore.cast(factory, columnStore);
        long rowsSoFar = retVal.countRows();
        if (rowsSoFar < rowsCount) {
            retVal = new AboveBelowStore<>(retVal, new ZeroStore<>(retVal.physical(), rowsCount - rowsSoFar, retVal.countColumns()));
        }
        return retVal;
    }

    @SafeVarargs
    static <N extends Comparable<N>> MatrixStore<N> buildColumn(final PhysicalStore.Factory<N, ?> factory, final long rowsCount, final N... columnElements) {
        MatrixStore<N> retVal = factory.columns(columnElements);
        long rowsSoFar = retVal.countRows();
        if (rowsSoFar < rowsCount) {
            retVal = new AboveBelowStore<>(retVal, new ZeroStore<>(factory, rowsCount - rowsSoFar, retVal.countColumns()));
        }
        return retVal;
    }

    @SafeVarargs
    static <N extends Comparable<N>> MatrixStore<N> buildRow(final PhysicalStore.Factory<N, ?> factory, final long colsCount, final Access2D<N>... rowStores) {
        MatrixStore<N> retVal = AbstractStore.cast(factory, rowStores[0]);
        for (int j = 1; j < rowStores.length; j++) {
            retVal = new LeftRightStore<>(retVal, AbstractStore.cast(factory, rowStores[j]));
        }
        long colsSoFar = retVal.countColumns();
        if (colsSoFar < colsCount) {
            retVal = new LeftRightStore<>(retVal, new ZeroStore<>(retVal.physical(), retVal.countRows(), colsCount - colsSoFar));
        }
        return retVal;
    }

    static <N extends Comparable<N>> MatrixStore<N> buildRow(final PhysicalStore.Factory<N, ?> factory, final long colsCount, final Access2D<N> rowStore) {
        MatrixStore<N> retVal = AbstractStore.cast(factory, rowStore);
        long colsSoFar = retVal.countColumns();
        if (colsSoFar < colsCount) {
            retVal = new LeftRightStore<>(retVal, new ZeroStore<>(retVal.physical(), retVal.countRows(), colsCount - colsSoFar));
        }
        return retVal;
    }

    @SafeVarargs
    static <N extends Comparable<N>> MatrixStore<N> buildRow(final PhysicalStore.Factory<N, ?> factory, final long colsCount, final N... rowElements) {
        MatrixStore<N> retVal = new TransposedStore<>(factory.columns(rowElements));
        long colsSoFar = retVal.countColumns();
        if (colsSoFar < colsCount) {
            retVal = new LeftRightStore<>(retVal, new ZeroStore<>(factory, retVal.countRows(), colsCount - colsSoFar));
        }
        return retVal;
    }

    static <N extends Comparable<N>> MatrixStore<N> cast(final PhysicalStore.Factory<N, ?> factory, final Access2D<?> access) {
        if (access instanceof MatrixStore<?>) {
            return (MatrixStore<N>) access;
        }
        return new WrapperStore<>(factory, access);
    }

    private final int myColDim;
    private transient Class<?> myComponentType = null;
    private final int myRowDim;

    @SuppressWarnings("unused")
    private AbstractStore() {

        this(0, 0);

        ProgrammingError.throwForIllegalInvocation();
    }

    protected AbstractStore(final int numberOfRows, final int numberOfColumns) {

        super();

        myRowDim = numberOfRows;
        myColDim = numberOfColumns;
    }

    protected AbstractStore(final long numberOfRows, final long numberOfColumns) {
        this(Math.toIntExact(numberOfRows), Math.toIntExact(numberOfColumns));
    }

    public long countColumns() {
        return myColDim;
    }

    public long countRows() {
        return myRowDim;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AbstractStore)) {
            return false;
        }
        AbstractStore other = (AbstractStore) obj;
        if (myColDim != other.myColDim) {
            return false;
        }
        if (myComponentType == null) {
            if (other.myComponentType != null) {
                return false;
            }
        } else if (!myComponentType.equals(other.myComponentType)) {
            return false;
        }
        if (myRowDim != other.myRowDim) {
            return false;
        }
        return true;
    }

    public final int getColDim() {
        return myColDim;
    }

    public final int getMaxDim() {
        return Math.max(myRowDim, myColDim);
    }

    public final int getMinDim() {
        return Math.min(myRowDim, myColDim);
    }

    public final int getRowDim() {
        return myRowDim;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + myColDim;
        result = prime * result + (myComponentType == null ? 0 : myComponentType.hashCode());
        result = prime * result + myRowDim;
        return result;
    }

    public int limitOfColumn(final int col) {
        return myRowDim;
    }

    public int limitOfRow(final int row) {
        return myColDim;
    }

    public N multiplyBoth(final Access1D<N> leftAndRight) {

        if (this.isPrimitive()) {

            final PhysicalStore<N> tmpStep1 = this.physical().make(1L, leftAndRight.count());
            tmpStep1.fillByMultiplying(leftAndRight, this);

            final PhysicalStore<N> tmpStep2 = this.physical().make(1L, 1L);
            tmpStep2.fillByMultiplying(tmpStep1, leftAndRight);

            return tmpStep2.get(0L);

        }
        return MatrixStore.super.multiplyBoth(leftAndRight);
    }

    @Override
    public final String toString() {
        return Access2D.toString(this);
    }

    protected final boolean isPrimitive() {
        return this.getComponentType().equals(Double.class);
    }

    final Class<?> getComponentType() {
        if (myComponentType == null) {
            myComponentType = this.get(0, 0).getClass();
        }
        return myComponentType;
    }

}
