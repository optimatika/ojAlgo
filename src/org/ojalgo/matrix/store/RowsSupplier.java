package org.ojalgo.matrix.store;

import java.util.ArrayList;
import java.util.List;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Mutate1D;
import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.SparseFactory;
import org.ojalgo.matrix.store.PhysicalStore.Factory;

public final class RowsSupplier<N extends Number> implements Access2D<N>, ElementsSupplier<N> {

    private final int myColumnsCount;
    private final PhysicalStore.Factory<N, ?> myFactory;
    private final List<SparseArray<N>> myRows = new ArrayList<>();

    RowsSupplier(final Factory<N, ?> factory, final int numberOfColumns) {
        super();
        myColumnsCount = numberOfColumns;
        myFactory = factory;
    }

    public SparseArray<N> addRow() {
        return this.addRow(SparseArray.factory(myFactory.array(), myColumnsCount).make());
    }

    public void addRows(final int numberToAdd) {
        final SparseFactory<N> factory = SparseArray.factory(myFactory.array(), myColumnsCount);
        for (int i = 0; i < numberToAdd; i++) {
            myRows.add(factory.make());
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

    public SparseArray<N> getRow(final int index) {
        return myRows.get(index);
    }

    public Factory<N, ?> physical() {
        return myFactory;
    }

    public SparseArray<N> removeRow(final int index) {
        return myRows.remove(index);
    }

    public RowsSupplier<N> selectRows(final int[] indices) {
        final RowsSupplier<N> retVal = new RowsSupplier<>(myFactory, myColumnsCount);
        for (int i = 0; i < indices.length; i++) {
            retVal.addRow(this.getRow(indices[i]));
        }
        return retVal;
    }

    public void supplyTo(final ElementsConsumer<N> receiver) {

        receiver.reset();

        for (int i = 0, limit = myRows.size(); i < limit; i++) {
            final long row = i;

            myRows.get(i).supplyNonZerosTo(new Mutate1D() {

                public void add(final long index, final double addend) {
                    receiver.add(row, index, addend);
                }

                public void add(final long index, final Number addend) {
                    receiver.add(row, index, addend);
                }

                public long count() {
                    return receiver.countColumns();
                }

                public void set(final long index, final double value) {
                    receiver.set(row, index, value);
                }

                public void set(final long index, final Number value) {
                    receiver.set(row, index, value);
                }

            });
        }
    }

    SparseArray<N> addRow(final SparseArray<N> rowToAdd) {
        if (myRows.add(rowToAdd)) {
            return rowToAdd;
        } else {
            return null;
        }
    }

}
