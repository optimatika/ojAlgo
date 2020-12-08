package org.ojalgo.matrix.transformation;

import org.ojalgo.core.function.constant.PrimitiveMath;
import org.ojalgo.core.structure.RowView;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;

final class HouseholderRow<N extends Comparable<N>> extends RowView<N> implements HouseholderReference<N> {

    private int myFirst = 0;
    private final MatrixStore<N> myStore;
    private transient Householder<N> myWorker = null;

    public HouseholderRow(final MatrixStore<N> store) {

        super(store);

        myStore = store;
    }

    @Override
    public long count() {
        return myStore.countColumns();
    }

    @Override
    public double doubleValue(final long index) {
        if (index > myFirst) {
            return myStore.doubleValue(this.row(), index);
        } else if (index == myFirst) {
            return PrimitiveMath.ONE;
        } else {
            return PrimitiveMath.ZERO;
        }
    }

    public int first() {
        return myFirst;
    }

    @Override
    public N get(final long index) {
        if (index > myFirst) {
            return myStore.get(this.row(), index);
        } else if (index == myFirst) {
            return myStore.physical().scalar().one().get();
        } else {
            return myStore.physical().scalar().zero().get();
        }
    }

    @SuppressWarnings("unchecked")
    public <P extends Householder<N>> P getWorker(final PhysicalStore.Factory<N, ?> factory) {
        if (myWorker == null) {
            myWorker = factory.makeHouseholder((int) this.count());
        }
        return (P) myWorker;
    }

    public boolean isZero() {
        return myStore.isRowSmall(this.row(), myFirst + 1L, PrimitiveMath.ONE);
    }

    public void point(final long row, final long col) {
        this.setRow(row);
        myFirst = (int) col;
    }

}
