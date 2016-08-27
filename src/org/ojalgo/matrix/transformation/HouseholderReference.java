package org.ojalgo.matrix.transformation;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;

public interface HouseholderReference<N extends Number> extends Householder<N> {

    public static <N extends Number> HouseholderReference<N> make(final MatrixStore<N> matrix, final boolean column) {
        return column ? new HouseholderColumn<>(matrix) : new HouseholderRow<>(matrix);
    }

    public static <N extends Number> HouseholderReference<N> makeColumn(final MatrixStore<N> matrix) {
        return new HouseholderColumn<>(matrix);
    }

    public static <N extends Number> HouseholderReference<N> makeRow(final MatrixStore<N> matrix) {
        return new HouseholderRow<>(matrix);
    }

    public <P extends Householder<N>> P getWorker(PhysicalStore.Factory<N, ?> factory);

    public boolean isZero();

    public void point(long row, long col);

}
