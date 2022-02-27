package org.ojalgo.matrix.transformation;

import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.structure.ColumnView;
import org.ojalgo.type.NumberDefinition;

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
        }
        if (index == myFirst) {
            return PrimitiveMath.ONE;
        }
        return PrimitiveMath.ZERO;
    }

    public int first() {
        return myFirst;
    }

    @Override
    public N get(final long index) {
        if (index > myFirst) {
            return myStore.get(index, this.column());
        }
        if (index == myFirst) {
            return myStore.physical().scalar().one().get();
        }
        return myStore.physical().scalar().zero().get();
    }

    @SuppressWarnings("unchecked")
    public <P extends Householder<N>> P getWorker(final PhysicalStore.Factory<N, ?> factory) {
        if (myWorker == null) {
            myWorker = factory.makeHouseholder((int) this.count());
        }
        return (P) myWorker;
    }

    public boolean isZero() {
        double largest = NumberDefinition.doubleValue(myStore.aggregateColumn(myFirst + 1L, this.column(), Aggregator.LARGEST));
        return PrimitiveScalar.isSmall(PrimitiveMath.ONE, largest);
    }

    public void point(final long row, final long col) {
        this.setColumn(col);
        myFirst = (int) row;
    }

}
