package org.ojalgo.matrix.store;

import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.array.SparseArray.SparseFactory;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Mutate2D;

public final class SparseColumnsStore<N extends Comparable<N>> extends FactoryStore<N> implements Mutate2D {

    public static final class SingleView<N extends Comparable<N>> extends ColumnView<N> implements Access2D.Collectable<N, PhysicalStore<N>> {

        private final SparseColumnsStore<N> myBase;

        SingleView(final SparseColumnsStore<N> base) {
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

    private final SparseArray<N>[] myColumns;

    SparseColumnsStore(final Factory<N, ?> factory, final int rowsCount, final int columnsCount) {

        super(factory, rowsCount, columnsCount);

        myColumns = (SparseArray<N>[]) new SparseArray<?>[columnsCount];

        SparseFactory<N> columnFactory = SparseArray.factory(factory.array()).limit(rowsCount);
        for (int i = 0; i < columnsCount; i++) {
            myColumns[i] = columnFactory.make();
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
                return SparseColumnsStore.this.countRows();
            }

            public double doubleValue(final long row, final long col) {
                return SparseColumnsStore.this.doubleValue(row, columns[(int) col]);
            }

            public N get(final long row, final long col) {
                return SparseColumnsStore.this.get(row, columns[(int) col]);
            }

            public Factory<N, ?> physical() {
                return SparseColumnsStore.this.physical();
            }

            public void supplyTo(final TransformableRegion<N> receiver) {

                receiver.reset();

                for (int j = 0; j < columns.length; j++) {

                    SparseArray<N> column = SparseColumnsStore.this.getColumn(columns[j]);

                    for (NonzeroView<N> nz : column.nonzeros()) {
                        receiver.set(nz.index(), j, nz.get());
                    }
                }
            }
        };
    }

    public double doubleValue(final long row, final long col) {
        return myColumns[(int) col].doubleValue(row);
    }

    public N get(final long row, final long col) {
        return myColumns[(int) col].get(row);
    }

    public void set(final long row, final long col, final Comparable<?> value) {
        myColumns[(int) col].set(row, value);
    }

    public void set(final long row, final long col, final double value) {
        myColumns[(int) col].set(row, value);
    }

    public void supplyTo(final TransformableRegion<N> receiver) {

        receiver.reset();

        for (int j = 0, limit = myColumns.length; j < limit; j++) {
            myColumns[j].supplyNonZerosTo(receiver.regionByColumns(j));
        }
    }

    SparseArray<N> getColumn(final int index) {
        return myColumns[index];
    }

}
