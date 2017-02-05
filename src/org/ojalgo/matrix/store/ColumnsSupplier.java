package org.ojalgo.matrix.store;

import java.util.ArrayList;
import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.store.PhysicalStore.Factory;

public final class ColumnsSupplier<N extends Number> implements Access2D<N>, MatrixSupplier<N> {

    private final List<Access1D<N>> myColumns = new ArrayList<>();
    private final PhysicalStore.Factory<N, ?> myFactory;
    private final int myRowsCount;

    ColumnsSupplier(final Factory<N, ?> factory, final int numberOfRows) {
        super();
        myRowsCount = numberOfRows;
        myFactory = factory;
    }

    public PhysicalStore<N> addColumn() {
        final PhysicalStore<N> retVal = myFactory.makeZero(myRowsCount, 1L);
        if (myColumns.add(retVal)) {
            return retVal;
        } else {
            return null;
        }
    }

    public Access1D<N> addColumn(final Access1D<N> column) {
        if (column.count() != myRowsCount) {
            throw new IllegalArgumentException("All columns must have the same legth!");
        }
        if (myColumns.add(column)) {
            return column;
        } else {
            return null;
        }
    }

    public long countColumns() {
        return myColumns.size();
    }

    public long countRows() {
        return myRowsCount;
    }

    public double doubleValue(final long row, final long col) {
        return myColumns.get((int) col).doubleValue(row);
    }

    public N get(final long row, final long col) {
        return myColumns.get((int) col).get(row);
    }

    public Access1D<N> getColumn(final int index) {
        return myColumns.get(index);
    }

    public Factory<N, ?> physical() {
        return myFactory;
    }

    public Access1D<N> removeColumn(final int index) {
        return myColumns.remove(index);
    }

    public void supplyTo(final ElementsConsumer<N> receiver) {
        final int tmpLimit = myColumns.size();
        for (int j = 0; j < tmpLimit; j++) {
            receiver.fillColumn(j, myColumns.get(j));
        }
    }

}
