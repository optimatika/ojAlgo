package org.ojalgo.matrix.store;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.SparseFactory;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ElementView1D;

public final class RowsSupplier<N extends Comparable<N>> implements Access2D<N>, ElementsSupplier<N>, Supplier<PhysicalStore<N>> {

    static final class ItemView<N extends Comparable<N>> extends RowView<N> {

        private final RowsSupplier<N> mySupplier;

        ItemView(final RowsSupplier<N> access) {
            super(access);
            mySupplier = access;
        }

        public ElementView1D<N, ?> elements() {
            return this.getCurrent().elements();
        }

        public ElementView1D<N, ?> nonzeros() {
            return this.getCurrent().nonzeros();
        }

        private SparseArray<N> getCurrent() {
            return mySupplier.getRow(Math.toIntExact(this.row()));
        }

    }

    private final int myColumnsCount;
    private final PhysicalStore.Factory<N, ?> myFactory;
    private final List<SparseArray<N>> myRows = new ArrayList<>();

    RowsSupplier(final Factory<N, ?> factory, final int numberOfColumns) {
        super();
        myColumnsCount = numberOfColumns;
        myFactory = factory;
    }

    public SparseArray<N> addRow() {
        return this.addRow(SparseArray.factory(myFactory.array()).limit(myColumnsCount).make());
    }

    public void addRows(final int numberToAdd) {
        SparseFactory<N> factory = SparseArray.factory(myFactory.array()).limit(myColumnsCount);
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

    public PhysicalStore<N> get() {
        return this.collect(myFactory);
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

    public RowView<N> rows() {
        return new ItemView<>(this);
    }

    public RowsSupplier<N> selectRows(final int[] indices) {
        RowsSupplier<N> retVal = new RowsSupplier<>(myFactory, myColumnsCount);
        for (int i = 0; i < indices.length; i++) {
            retVal.addRow(this.getRow(indices[i]));
        }
        return retVal;
    }

    public void supplyTo(final TransformableRegion<N> receiver) {

        receiver.reset();

        for (int i = 0, limit = myRows.size(); i < limit; i++) {
            myRows.get(i).supplyNonZerosTo(receiver.regionByRows(i));
        }
    }

    @Override
    public String toString() {
        return Access2D.toString(this);
    }

    SparseArray<N> addRow(final SparseArray<N> rowToAdd) {
        if (myRows.add(rowToAdd)) {
            return rowToAdd;
        } else {
            return null;
        }
    }

}
