package org.ojalgo.matrix.store;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.array.SparseArray.SparseFactory;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Mutate2D;

public final class RowsSupplier<N extends Comparable<N>> implements MatrixStore<N>, Mutate2D, Supplier<PhysicalStore<N>> {

    public static final class SingleView<N extends Comparable<N>> extends RowView<N> implements Access2D.Collectable<N, PhysicalStore<N>> {

        private final RowsSupplier<N> myBase;

        SingleView(final RowsSupplier<N> base) {
            super(base);
            myBase = base;
        }

        public long countColumns() {
            return myBase.countColumns();
        }

        public long countRows() {
            return 1L;
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
            return myBase.getRow(Math.toIntExact(this.row()));
        }

    }

    private final int myColumnsCount;
    private final PhysicalStore.Factory<N, ?> myPhysicalStoreFactory;
    private final SparseFactory<N> myRowFactory;
    private final List<SparseArray<N>> myRows = new ArrayList<>();

    RowsSupplier(final Factory<N, ?> factory, final int numberOfColumns) {
        super();
        myColumnsCount = numberOfColumns;
        myPhysicalStoreFactory = factory;
        myRowFactory = SparseArray.factory(factory.array()).limit(myColumnsCount);
    }

    public SparseArray<N> addRow() {
        return this.addRow(myRowFactory.make());
    }

    public void addRows(final int numberToAdd) {
        for (int i = 0; i < numberToAdd; i++) {
            myRows.add(myRowFactory.make());
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
        return this.collect(myPhysicalStoreFactory);
    }

    public N get(final long row, final long col) {
        return myRows.get((int) row).get(col);
    }

    public SparseArray<N> getRow(final int index) {
        return myRows.get(index);
    }

    public Factory<N, ?> physical() {
        return myPhysicalStoreFactory;
    }

    public SparseArray<N> removeRow(final int index) {
        return myRows.remove(index);
    }

    public RowView<N> rows() {
        return new SingleView<>(this);
    }

    public MatrixStore<N> rows(final int... rows) {
        return new MatrixStore<>() {

            public long countColumns() {
                return RowsSupplier.this.countColumns();
            }

            public long countRows() {
                return rows.length;
            }

            public double doubleValue(final long row, final long col) {
                return RowsSupplier.this.doubleValue(rows[(int) row], col);
            }

            public N get(final long row, final long col) {
                return RowsSupplier.this.get(rows[(int) row], col);
            }

            public Factory<N, ?> physical() {
                return RowsSupplier.this.physical();
            }

            public void supplyTo(final TransformableRegion<N> receiver) {

                receiver.reset();

                for (int i = 0; i < rows.length; i++) {

                    SparseArray<N> row = RowsSupplier.this.getRow(rows[i]);

                    for (NonzeroView<N> nz : row.nonzeros()) {
                        receiver.set(nz.index(), i, nz.get());
                    }
                }
            }
        };
    }

    public RowsSupplier<N> selectRows(final int[] indices) {
        RowsSupplier<N> retVal = new RowsSupplier<>(myPhysicalStoreFactory, myColumnsCount);
        for (int i = 0; i < indices.length; i++) {
            retVal.addRow(this.getRow(indices[i]));
        }
        return retVal;
    }

    public void set(final long row, final long col, final Comparable<?> value) {
        myRows.get((int) row).set(col, value);
    }

    public void set(final long row, final long col, final double value) {
        myRows.get((int) row).set(col, value);
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
