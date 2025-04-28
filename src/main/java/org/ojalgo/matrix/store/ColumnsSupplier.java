package org.ojalgo.matrix.store;

import java.util.ArrayList;
import java.util.List;

import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.array.SparseArray.SparseFactory;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Transformation2D;

/**
 * Sparse columns – columns can be added and removed.
 *
 * @author apete
 */
public final class ColumnsSupplier<N extends Comparable<N>> implements MatrixStore<N>, SparseStructure2D, Mutate2D.ModifiableReceiver<N> {

    public static final class SingleView<N extends Comparable<N>> extends ColumnView<N> implements Access2D.Collectable<N, PhysicalStore<N>> {

        private final ColumnsSupplier<N> myBase;

        SingleView(final ColumnsSupplier<N> base) {
            super(base);
            myBase = base;
        }

        @Override
        public long countColumns() {
            return 1L;
        }

        @Override
        public long countRows() {
            return myBase.countRows();
        }

        @Override
        public ElementView1D<N, ?> elements() {
            return this.getCurrent().elements();
        }

        @Override
        public int getColDim() {
            return 1;
        }

        @Override
        public int getRowDim() {
            return myBase.getRowDim();
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
        myColumnFactory = SparseArray.factory(factory.array());
    }

    @Override
    public void add(final long row, final long col, final Comparable<?> addend) {
        myColumns.get((int) col).add(row, addend);
    }

    @Override
    public void add(final long row, final long col, final double addend) {
        myColumns.get((int) col).add(row, addend);
    }

    public SparseArray<N> addColumn() {
        return this.addColumn(myColumnFactory.make(myRowsCount));
    }

    public void addColumns(final int numberToAdd) {
        for (int j = 0; j < numberToAdd; j++) {
            myColumns.add(myColumnFactory.make(myRowsCount));
        }
    }

    @Override
    public SingleView<N> columns() {
        return new SingleView<>(this);
    }

    @Override
    public MatrixStore<N> columns(final int... columns) {
        return new MatrixStore<>() {

            public long countColumns() {
                return columns.length;
            }

            public long countRows() {
                return ColumnsSupplier.this.countRows();
            }

            public double doubleValue(final int row, final int col) {
                return ColumnsSupplier.this.doubleValue(row, columns[col]);
            }

            public N get(final int row, final int col) {
                return ColumnsSupplier.this.get(row, columns[col]);
            }

            public int getColDim() {
                return columns.length;
            }

            public int getRowDim() {
                return ColumnsSupplier.this.getRowDim();
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

    @Override
    public long countColumns() {
        return myColumns.size();
    }

    @Override
    public long countRows() {
        return myRowsCount;
    }

    @Override
    public double density() {

        double totalElements = this.count();

        if (totalElements == 0) {
            return PrimitiveMath.ZERO;
        }

        long nonZeros = 0L;
        for (SparseArray<N> column : myColumns) {
            nonZeros += column.count();
        }

        return nonZeros / totalElements;
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return myColumns.get(col).doubleValue(row);
    }

    @Override
    public void exchangeColumns(final long colA, final long colB) {
        int a = Math.toIntExact(colA);
        int b = Math.toIntExact(colB);
        SparseArray<N> temp = myColumns.get(a);
        myColumns.set(a, myColumns.get(b));
        myColumns.set(b, temp);
    }

    @Override
    public void exchangeRows(final long rowA, final long rowB) {
        int a = Math.toIntExact(rowA);
        int b = Math.toIntExact(rowB);
        for (SparseArray<N> column : myColumns) {
            double temp = column.doubleValue(a);
            column.set(a, column.doubleValue(b));
            column.set(b, temp);
        }
    }

    @Override
    public PhysicalStore<N> get() {
        return this.collect(myPhysicalStoreFactory);
    }

    @Override
    public N get(final int row, final int col) {
        return myColumns.get(col).get(row);
    }

    @Override
    public int getColDim() {
        return myColumns.size();
    }

    public SparseArray<N> getColumn(final int index) {
        return myColumns.get(index);
    }

    @Override
    public int getRowDim() {
        return myRowsCount;
    }

    @Override
    public void modifyAny(final Transformation2D<N> modifier) {
        modifier.transform(this);
    }

    @Override
    public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
        myColumns.get((int) col).modifyOne(row, modifier);
    }

    @Override
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

    @Override
    public void set(final int row, final int col, final double value) {
        myColumns.get(col).set(row, value);
    }

    @Override
    public void set(final long row, final long col, final Comparable<?> value) {
        myColumns.get(Math.toIntExact(col)).set(row, value);
    }

    @Override
    public SparseArray<N> sliceColumn(final long col) {
        return this.getColumn(Math.toIntExact(col));
    }

    @Override
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

    @Override
    public List<Triplet> toTriplets() {
        List<Triplet> triplets = new ArrayList<>();

        // Iterate through each column
        for (int col = 0, limit = myColumns.size(); col < limit; col++) {
            SparseArray<N> column = myColumns.get(col);
            // For each non-zero element in this column
            for (NonzeroView<N> nz : column.nonzeros()) {
                triplets.add(new Triplet(Math.toIntExact(nz.index()), col, nz.doubleValue()));
            }
        }

        return triplets;
    }

    SparseArray<N> addColumn(final SparseArray<N> columnToAdd) {
        if (myColumns.add(columnToAdd)) {
            return columnToAdd;
        } else {
            return null;
        }
    }

}
