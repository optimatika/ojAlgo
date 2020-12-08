package org.ojalgo.matrix.transformation;

import org.ojalgo.core.function.constant.PrimitiveMath;
import org.ojalgo.core.structure.ColumnView;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;

final class HouseholderColumn<N extends Comparable<N>> extends ColumnView<N> implements HouseholderReference<N> {

    private int myFirst = 0;
    private final MatrixStore<N> myStore;
    private transient Householder<N> myWorker = null;

    public HouseholderColumn(final MatrixStore<N> store) {

        super(store);

        myStore = store;
    }

    @Override
    public long count() {
        return myStore.countRows();
    }

    @Override
    public double doubleValue(final long index) {
        if (index > myFirst) {
            return myStore.doubleValue(index, this.column());
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
            return myStore.get(index, this.column());
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
        return myStore.isColumnSmall(myFirst + 1L, this.column(), PrimitiveMath.ONE);
    }

    public void point(final long row, final long col) {
        this.setColumn(col);
        myFirst = (int) row;
    }

}
