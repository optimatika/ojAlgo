package org.ojalgo.matrix.store;

import java.util.ArrayList;
import java.util.List;

import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.array.SparseArray.SparseFactory;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Mutate2D;

/**
 * Sparse rows – rows can be added and removed.
 *
 * @author apete
 */
public final class RowsSupplier<N extends Comparable<N>> implements MatrixStore<N>, Mutate2D {

    public static final class SingleView<N extends Comparable<N>> extends RowView<N> implements Access2D.Collectable<N, PhysicalStore<N>> {

        private final RowsSupplier<N> myBase;

        SingleView(final RowsSupplier<N> base) {
            super(base);
            myBase = base;
        }

        @Override
        public long countColumns() {
            return myBase.countColumns();
        }

        @Override
        public long countRows() {
            return 1L;
        }

        @Override
        public ElementView1D<N, ?> elements() {
            return this.getCurrent().elements();
        }

        @Override
        public ElementView1D<N, ?> nonzeros() {
            return this.getCurrent().nonzeros();
        }

        @Override
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
        myRowFactory = SparseArray.factory(factory.array());
    }

    public SparseArray<N> addRow() {
        return this.addRow(myRowFactory.make(myColumnsCount));
    }

    public void addRows(final int numberToAdd) {
        for (int i = 0; i < numberToAdd; i++) {
            myRows.add(myRowFactory.make(myColumnsCount));
        }
    }

    @Override
    public long countColumns() {
        return myColumnsCount;
    }

    @Override
    public long countRows() {
        return myRows.size();
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return myRows.get(row).doubleValue(col);
    }

    @Override
    public PhysicalStore<N> get() {
        return this.collect(myPhysicalStoreFactory);
    }

    @Override
    public N get(final int row, final int col) {
        return myRows.get(row).get(col);
    }

    public SparseArray<N> getRow(final int index) {
        return myRows.get(index);
    }

    @Override
    public Factory<N, ?> physical() {
        return myPhysicalStoreFactory;
    }

    public SparseArray<N> removeRow(final int index) {
        return myRows.remove(index);
    }

    @Override
    public RowView<N> rows() {
        return new SingleView<>(this);
    }

    @Override
    public MatrixStore<N> rows(final int... rows) {
        return new MatrixStore<>() {

            public long countColumns() {
                return RowsSupplier.this.countColumns();
            }

            public long countRows() {
                return rows.length;
            }

            public double doubleValue(final int row, final int col) {
                return RowsSupplier.this.doubleValue(rows[row], col);
            }

            public N get(final int row, final int col) {
                return RowsSupplier.this.get(rows[row], col);
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

            @Override
            public String toString() {
                return Access2D.toString(this);
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

    @Override
    public void set(final int row, final int col, final double value) {
        myRows.get(row).set(col, value);
    }

    @Override
    public void set(final long row, final long col, final Comparable<?> value) {
        myRows.get(Math.toIntExact(row)).set(col, value);
    }

    @Override
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
