package org.ojalgo.matrix.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.array.SparseArray.SparseFactory;
import org.ojalgo.function.UnaryFunction;
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
    public int countNonzeros() {

        int retVal = 0;
        for (SparseArray<N> array : myColumns) {
            retVal += array.countNonzeros();
        }

        return retVal;

    }

    @Override
    public long countRows() {
        return myRowsCount;
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

    /**
     * Efficiently appends a new nonzero element to the end of the specified column.
     * <p>
     * This method assumes that the supplied {@code row} is strictly greater than all existing row indices in
     * the specified column. No search is performed; the value is simply appended. If the ascending order of
     * row indices is broken, future behavior is unspecified. If the value is zero, nothing is stored.
     *
     * @param row   the row index (must be after all existing row indices in the column)
     * @param col   the column to which the value should be appended
     * @param value the value to insert (only nonzero values are actually stored)
     */
    public void putLast(final int row, final int col, final double value) {
        myColumns.get(col).putLast(row, value);
    }

    public Access1D<N> removeColumn(final int index) {
        return myColumns.remove(index);
    }

    @Override
    public void reset() {
        for (SparseArray<N> sparseArray : myColumns) {
            sparseArray.reset();
        }
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
    public R064CSC toCSC() {

        int nbRows = this.getRowDim();
        int nbCols = this.getColDim();
        int nbNz = this.countNonzeros();

        double[] values = new double[nbNz];
        int[] rowIndices = new int[nbNz];
        int[] colPointers = new int[nbCols + 1];

        int pos = 0;
        for (int j = 0; j < nbCols; j++) {
            colPointers[j] = pos;
            SparseArray<N> col = myColumns.get(j);
            for (SparseArray.NonzeroView<N> nz : col.nonzeros()) {
                values[pos] = nz.doubleValue();
                rowIndices[pos] = (int) nz.index();
                pos++;
            }
        }
        colPointers[nbCols] = pos;

        return new R064CSC(nbRows, nbCols, values, rowIndices, colPointers);
    }

    @Override
    public R064CSR toCSR() {

        int nbRows = this.getRowDim();
        int nbCols = this.getColDim();
        int nbNz = this.countNonzeros();

        double[] values = new double[nbNz];
        int[] colIndices = new int[nbNz];
        int[] rowPointers = new int[nbRows + 1];

        // Counting pass
        for (int j = 0; j < nbCols; j++) {
            SparseArray<N> col = myColumns.get(j);
            for (SparseArray.NonzeroView<N> nz : col.nonzeros()) {
                int row = (int) nz.index();
                rowPointers[row + 1]++;
            }
        }

        // Prefix sum
        for (int i = 0; i < nbRows; i++) {
            rowPointers[i + 1] += rowPointers[i];
        }

        // Filling pass
        int[] next = Arrays.copyOf(rowPointers, nbRows);
        for (int j = 0; j < nbCols; j++) {
            SparseArray<N> col = myColumns.get(j);
            for (SparseArray.NonzeroView<N> nz : col.nonzeros()) {
                int row = (int) nz.index();
                int pos = next[row]++;
                values[pos] = nz.doubleValue();
                colIndices[pos] = j;
            }
        }

        return new R064CSR(nbRows, nbCols, values, colIndices, rowPointers);
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
