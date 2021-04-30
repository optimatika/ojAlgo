package org.ojalgo.matrix.store;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.SparseFactory;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ColumnView;
import org.ojalgo.structure.ElementView1D;

public final class ColumnsSupplier<N extends Comparable<N>> implements Access2D<N>, ElementsSupplier<N>, Supplier<PhysicalStore<N>> {

    static final class ItemView<N extends Comparable<N>> extends ColumnView<N> {

        private final ColumnsSupplier<N> mySupplier;

        ItemView(final ColumnsSupplier<N> access) {
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
            return mySupplier.getColumn(Math.toIntExact(this.column()));
        }

    }

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
        SparseFactory<N> factory = SparseArray.factory(myFactory.array(), myRowsCount);
        for (int j = 0; j < numberToAdd; j++) {
            myColumns.add(factory.make());
        }
    }

    public ColumnView<N> columns() {
        return new ItemView<>(this);
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

    public PhysicalStore<N> get() {
        return this.collect(myFactory);
    }

    public N get(final long row, final long col) {
        return myColumns.get((int) col).get(row);
    }

    public SparseArray<N> getColumn(final int index) {
        return myColumns.get(index);
    }

    public Access1D<N> removeColumn(final int index) {
        return myColumns.remove(index);
    }

    public ColumnsSupplier<N> selectColumns(final int[] indices) {
        ColumnsSupplier<N> retVal = new ColumnsSupplier<>(myFactory, myRowsCount);
        for (int i = 0; i < indices.length; i++) {
            retVal.addColumn(this.getColumn(indices[i]));
        }
        return retVal;
    }

    public void supplyTo(final TransformableRegion<N> receiver) {

        receiver.reset();

        for (int j = 0, limit = myColumns.size(); j < limit; j++) {
            myColumns.get(j).supplyNonZerosTo(receiver.regionByColumns(j));
        }
    }

    @Override
    public String toString() {
        return Access2D.toString(this);
    }

    SparseArray<N> addColumn(final SparseArray<N> columnToAdd) {
        if (myColumns.add(columnToAdd)) {
            return columnToAdd;
        } else {
            return null;
        }
    }

}
