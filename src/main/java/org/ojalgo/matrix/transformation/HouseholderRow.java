package org.ojalgo.matrix.transformation;

import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.structure.Access2D.RowView;
import org.ojalgo.type.NumberDefinition;

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
    public double doubleValue(final int index) {
        if (index > myFirst) {
            return myStore.doubleValue(this.row(), index);
        } else if (index == myFirst) {
            return PrimitiveMath.ONE;
        } else {
            return PrimitiveMath.ZERO;
        }
    }

    @Override
    public int first() {
        return myFirst;
    }

    @Override
    public N get(final long index) {
        if (index > myFirst) {
            return myStore.get(this.row(), index);
        }
        if (index == myFirst) {
            return myStore.physical().scalar().one().get();
        } else {
            return myStore.physical().scalar().zero().get();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P extends Householder<N>> P getWorker(final PhysicalStore.Factory<N, ?> factory) {
        if (myWorker == null) {
            myWorker = factory.makeHouseholder((int) this.count());
        }
        return (P) myWorker;
    }

    @Override
    public boolean isZero() {
        double largest = NumberDefinition.doubleValue(myStore.aggregateRow(this.row(), myFirst + 1L, Aggregator.LARGEST));
        return PrimitiveScalar.isSmall(PrimitiveMath.ONE, largest);
    }

    @Override
    public void point(final long row, final long col) {
        this.goToRow(row);
        myFirst = (int) col;
    }

}
