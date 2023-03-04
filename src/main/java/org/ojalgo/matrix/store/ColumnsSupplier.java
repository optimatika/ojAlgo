package org.ojalgo.matrix.store;

import java.util.ArrayList;
import java.util.List;

import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.array.SparseArray.SparseFactory;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Mutate2D;

/**
 * Sparse columns – columns can be added and removed.
 *
 * @author apete
 */
public final class ColumnsSupplier<N extends Comparable<N>> implements MatrixStore<N>, Mutate2D {

    public static final class SingleView<N extends Comparable<N>> extends ColumnView<N> implements Access2D.Collectable<N, PhysicalStore<N>> {

        private final ColumnsSupplier<N> myBase;

        SingleView(final ColumnsSupplier<N> base) {
            super(base);
            myBase = base;
        }

        public long countColumns() {
            return 1L;
        }

        public long countRows() {
            return myBase.countRows();
        }

        public ElementView1D<N, ?> elements() {
            return this.getCurrent().elements();
        }

        public ElementView1D<N, ?> nonzeros() {
            return this.getCurrent().nonzeros();
        }

        public void supplyTo(final PhysicalStore<N> receiver) {

            receiver.reset();

            for (ElementView1D<N, ?> element : this.nonzeros()) {
                receiver.set(element.index(), element.doubleValue());
            }
        }

        private SparseArray<N> getCurrent() {
            return myBase.getColumn(Math.toIntExact(this.column()));
        }

    }

    private final SparseFactory<N> myColumnFactory;
    private final List<SparseArray<N>> myColumns = new ArrayList<>();
    private final PhysicalStore.Factory<N, ?> myPhysicalStoreFactory;
    private final int myRowsCount;

    ColumnsSupplier(final PhysicalStore.Factory<N, ?> factory, final int numberOfRows) {
        super();
        myRowsCount = numberOfRows;
        myPhysicalStoreFactory = factory;
        myColumnFactory = SparseArray.factory(factory.array()).limit(myRowsCount);
    }

    public SparseArray<N> addColumn() {
        return this.addColumn(myColumnFactory.make());
    }

    public void addColumns(final int numberToAdd) {
        for (int j = 0; j < numberToAdd; j++) {
            myColumns.add(myColumnFactory.make());
        }
    }

    public SingleView<N> columns() {
        return new SingleView<>(this);
    }

    public MatrixStore<N> columns(final int... columns) {
        return new MatrixStore<>() {

            public long countColumns() {
                return columns.length;
            }

            public long countRows() {
                return ColumnsSupplier.this.countRows();
            }

            public double doubleValue(final long row, final long col) {
                return ColumnsSupplier.this.doubleValue(row, columns[(int) col]);
            }

            public N get(final long row, final long col) {
                return ColumnsSupplier.this.get(row, columns[(int) col]);
            }

            public Factory<N, ?> physical() {
                return ColumnsSupplier.this.physical();
            }

            public void supplyTo(final TransformableRegion<N> receiver) {

                receiver.reset();

                for (int j = 0; j < columns.length; j++) {

                    SparseArray<N> column = ColumnsSupplier.this.getColumn(columns[j]);

                    for (NonzeroView<N> nz : column.nonzeros()) {
                        receiver.set(nz.index(), j, nz.get());
                    }
                }
            }

            @Override
            public String toString() {
                return Access2D.toString(this);
            }
        };
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
        return this.collect(myPhysicalStoreFactory);
    }

    public N get(final long row, final long col) {
        return myColumns.get((int) col).get(row);
    }

    public SparseArray<N> getColumn(final int index) {
        return myColumns.get(index);
    }

    public Factory<N, ?> physical() {
        return myPhysicalStoreFactory;
    }

    public Access1D<N> removeColumn(final int index) {
        return myColumns.remove(index);
    }

    public ColumnsSupplier<N> selectColumns(final int[] indices) {
        ColumnsSupplier<N> retVal = new ColumnsSupplier<>(myPhysicalStoreFactory, myRowsCount);
        for (int i = 0; i < indices.length; i++) {
            retVal.addColumn(this.getColumn(indices[i]));
        }
        return retVal;
    }

    public void set(final long row, final long col, final Comparable<?> value) {
        myColumns.get((int) col).set(row, value);
    }

    public void set(final long row, final long col, final double value) {
        myColumns.get((int) col).set(row, value);
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
