package org.ojalgo.matrix.store;

import java.util.ArrayList;
import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.store.PhysicalStore.Factory;

public final class RowsSupplier<N extends Number> implements Access2D<N>, MatrixSupplier<N> {

    private final int myColumnsCount;
    private final PhysicalStore.Factory<N, ?> myFactory;
    private final List<Access1D<N>> myRows = new ArrayList<>();

    RowsSupplier(final Factory<N, ?> factory, final int numberOfColumns) {
        super();
        myColumnsCount = numberOfColumns;
        myFactory = factory;
    }

    public PhysicalStore<N> addRow() {
        final PhysicalStore<N> retVal = myFactory.makeZero(1L, myColumnsCount);
        if (myRows.add(retVal)) {
            return retVal;
        } else {
            return null;
        }
    }

    public Access1D<N> addRow(final Access1D<N> row) {
        if (row.count() != myColumnsCount) {
            throw new IllegalArgumentException("All rows must have the same legth!");
        }
        if (myRows.add(row)) {
            return row;
        } else {
            return null;
        }
    }

    public long countColumns() {
        return myColumnsCount;
    }

    public long countRows() {
        return myRows.size();
    }

    public double doubleValue(final long row, final long col) {
        return myRows.get((int) row).doubleValue(col);
    }

    public N get(final long row, final long col) {
        return myRows.get((int) row).get(col);
    }

    public Access1D<N> getRow(final int index) {
        return myRows.get(index);
    }

    public Factory<N, ?> physical() {
        return myFactory;
    }

    public Access1D<N> removeRow(final int index) {
        return myRows.remove(index);
    }

    public void supplyTo(final ElementsConsumer<N> receiver) {
        final int tmpLimit = myRows.size();
        for (int i = 0; i < tmpLimit; i++) {
            receiver.fillRow(i, myRows.get(i));
        }
    }

}
