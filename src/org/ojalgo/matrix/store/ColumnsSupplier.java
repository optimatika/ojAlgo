package org.ojalgo.matrix.store;

import java.util.ArrayList;
import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Mutate1D;
import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.SparseFactory;
import org.ojalgo.matrix.store.PhysicalStore.Factory;

public final class ColumnsSupplier<N extends Number> implements Access2D<N>, ElementsSupplier<N> {

    private final List<SparseArray<N>> myColumns = new ArrayList<>();
    private final PhysicalStore.Factory<N, ?> myFactory;
    private final int myRowsCount;

    ColumnsSupplier(final Factory<N, ?> factory, final int numberOfRows) {
        super();
        myRowsCount = numberOfRows;
        myFactory = factory;
    }

    public SparseArray<N> addColumn() {
        return this.addColumn(SparseArray.factory(myFactory.array(), myRowsCount).make());
    }

    public void addColumns(final int numberToAdd) {
        final SparseFactory<N> factory = SparseArray.factory(myFactory.array(), myRowsCount);
        for (int j = 0; j < numberToAdd; j++) {
            myColumns.add(factory.make());
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

    public SparseArray<N> getColumn(final int index) {
        return myColumns.get(index);
    }

    public Factory<N, ?> physical() {
        return myFactory;
    }

    public Access1D<N> removeColumn(final int index) {
        return myColumns.remove(index);
    }

    public ColumnsSupplier<N> selectColumns(final int[] indices) {
        final ColumnsSupplier<N> retVal = new ColumnsSupplier<>(myFactory, myRowsCount);
        for (int i = 0; i < indices.length; i++) {
            retVal.addColumn(this.getColumn(indices[i]));
        }
        return retVal;
    }

    public void supplyTo(final ElementsConsumer<N> receiver) {

        receiver.reset();

        for (int j = 0, limit = myColumns.size(); j < limit; j++) {
            final long col = j;

            myColumns.get(j).supplyNonZerosTo(new Mutate1D() {

                public void add(final long index, final double addend) {
                    receiver.add(index, col, addend);
                }

                public void add(final long index, final Number addend) {
                    receiver.add(index, col, addend);
                }

                public long count() {
                    return receiver.countRows();
                }

                public void set(final long index, final double value) {
                    receiver.set(index, col, value);
                }

                public void set(final long index, final Number value) {
                    receiver.set(index, col, value);
                }

            });
        }
    }

    SparseArray<N> addColumn(final SparseArray<N> columnToAdd) {
        if (myColumns.add(columnToAdd)) {
            return columnToAdd;
        } else {
            return null;
        }
    }

}
