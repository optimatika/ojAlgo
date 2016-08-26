package org.ojalgo.matrix.transformation;

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.matrix.decomposition.DecompositionStore;

public final class HouseholderReference<N extends Number> implements Householder<N> {

    public int col = 0;
    public int row = 0;
    private transient Householder.Big myBigWorker = null;
    private final boolean myColumn;
    private transient Householder.Complex myComplexWorker = null;
    private transient Householder.Primitive myPrimitiveWorker = null;
    private final DecompositionStore<N> myStore;

    @SuppressWarnings("unused")
    private HouseholderReference() {
        this(null, true);
    }

    public HouseholderReference(final DecompositionStore<N> store, final boolean column) {

        super();

        myStore = store;
        myColumn = column;
    }

    public long count() {
        if (myColumn) {
            return myStore.countRows();
        } else {
            return myStore.countColumns();
        }
    }

    public double doubleValue(final long index) {
        if (myColumn) {
            if (index > row) {
                return myStore.doubleValue((int) index, col);
            } else if (index == row) {
                return PrimitiveMath.ONE;
            } else {
                return PrimitiveMath.ZERO;
            }
        } else {
            if (index > col) {
                return myStore.doubleValue(row, (int) index);
            } else if (index == col) {
                return PrimitiveMath.ONE;
            } else {
                return PrimitiveMath.ZERO;
            }
        }
    }

    public int first() {
        return myColumn ? row : col;
    }

    public N get(final long index) {
        if (myColumn) {
            if (index > row) {
                return myStore.get((int) index, col);
            } else if (index == row) {
                return myStore.physical().scalar().one().getNumber();
            } else {
                return myStore.physical().scalar().zero().getNumber();
            }
        } else {
            if (index > col) {
                return myStore.get(row, (int) index);
            } else if (index == col) {
                return myStore.physical().scalar().one().getNumber();
            } else {
                return myStore.physical().scalar().zero().getNumber();
            }
        }
    }

    public final Householder.Big getBigWorker() {

        if (myBigWorker == null) {
            if (myColumn) {
                myBigWorker = new Householder.Big((int) myStore.countRows());
            } else {
                myBigWorker = new Householder.Big((int) myStore.countColumns());
            }
        }

        return myBigWorker;
    }

    public final Householder.Complex getComplexWorker() {

        if (myComplexWorker == null) {
            if (myColumn) {
                myComplexWorker = new Householder.Complex((int) myStore.countRows());
            } else {
                myComplexWorker = new Householder.Complex((int) myStore.countColumns());
            }
        }

        return myComplexWorker;
    }

    public final Householder.Primitive getPrimitiveWorker() {

        if (myPrimitiveWorker == null) {
            if (myColumn) {
                myPrimitiveWorker = new Householder.Primitive((int) myStore.countRows());
            } else {
                myPrimitiveWorker = new Householder.Primitive((int) myStore.countColumns());
            }
        }

        return myPrimitiveWorker;
    }

    public final boolean isZero() {
        if (myColumn) {
            return myStore.isColumnSmall(row + 1L, col, PrimitiveMath.ONE);
        } else {
            return myStore.isRowSmall(row, col + 1L, PrimitiveMath.ONE);
        }
    }

    @Override
    public String toString() {

        final StringBuilder retVal = new StringBuilder("{ ");

        final int tmpLastIndex = (int) this.count() - 1;
        for (int i = 0; i < tmpLastIndex; i++) {
            retVal.append(this.get(i));
            retVal.append(", ");
        }
        retVal.append(this.get(tmpLastIndex));

        retVal.append(" }");

        return retVal.toString();
    }

}