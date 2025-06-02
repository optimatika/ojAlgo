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
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Transformation2D;

/**
 * Sparse rows – rows can be added and removed.
 *
 * @author apete
 */
public final class RowsSupplier<N extends Comparable<N>> implements MatrixStore<N>, SparseStructure2D, Mutate2D.ModifiableReceiver<N> {

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
        public int getColDim() {
            return myBase.getColDim();
        }

        @Override
        public int getRowDim() {
            return 1;
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

    @Override
    public void add(final long row, final long col, final Comparable<?> addend) {
        myRows.get((int) row).add(col, addend);
    }

    @Override
    public void add(final long row, final long col, final double addend) {
        myRows.get((int) row).add(col, addend);
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
    public int countNonzeros() {

        int retVal = 0;
        for (SparseArray<N> array : myRows) {
            retVal += array.countNonzeros();
        }

        return retVal;
    }

    @Override
    public long countRows() {
        return myRows.size();
    }

    /**
     * Performs the row/column cyclic shifts required by the Forrest-Tomlin update algorithm as implemented in
     * ojAlgo's own sparse LU decomposition. This method is not intended for any other use case.
     *
     * @param from   the row index to start the cyclic shift
     * @param row    a Mutate1D to receive the removed row's values (shifted left by one column)
     * @param to     the row index to end the cyclic shift
     * @param column an Access1D providing the new column values
     */
    public void doCyclicFT(final int from, final Mutate1D row, final int to, final Access1D<?> column) {

        for (int i = 0; i < from; i++) {
            myRows.get(i).removeShiftAndInsert(from, to, column.doubleValue(i));
        }

        SparseArray<N> moved = myRows.get(from);
        moved.removeShiftAndInsert(from, to, column.doubleValue(from));
        moved.supplyTo(row);
        moved.reset();

        for (int i = from; i < to; i++) {
            SparseArray<N> next = myRows.get(i + 1);
            next.removeShiftAndInsert(from, to, column.doubleValue(i + 1));
            myRows.set(i, next);
        }

        myRows.set(to, moved);

        for (int i = to + 1; i < myRows.size(); i++) {
            myRows.get(i).removeShiftAndInsert(from, to, column.doubleValue(i));
        }
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return myRows.get(row).doubleValue(col);
    }

    @Override
    public void exchangeColumns(final long colA, final long colB) {
        int a = Math.toIntExact(colA);
        int b = Math.toIntExact(colB);
        for (SparseArray<N> row : myRows) {
            double temp = row.doubleValue(a);
            row.set(a, row.doubleValue(b));
            row.set(b, temp);
        }
    }

    @Override
    public void exchangeRows(final long rowA, final long rowB) {
        int a = Math.toIntExact(rowA);
        int b = Math.toIntExact(rowB);
        SparseArray<N> temp = myRows.get(a);
        myRows.set(a, myRows.get(b));
        myRows.set(b, temp);
    }

    @Override
    public PhysicalStore<N> get() {
        return this.collect(myPhysicalStoreFactory);
    }

    @Override
    public N get(final int row, final int col) {
        return myRows.get(row).get(col);
    }

    @Override
    public int getColDim() {
        return myColumnsCount;
    }

    public SparseArray<N> getRow(final int index) {
        return myRows.get(index);
    }

    @Override
    public int getRowDim() {
        return myRows.size();
    }

    @Override
    public void modifyAny(final Transformation2D<N> modifier) {
        modifier.transform(this);
    }

    @Override
    public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
        myRows.get((int) row).modifyOne(col, modifier);
    }

    @Override
    public Factory<N, ?> physical() {
        return myPhysicalStoreFactory;
    }

    /**
     * Efficiently appends a new nonzero element to the end of the specified row.
     * <p>
     * This method assumes that the supplied {@code col} is strictly greater than all existing column indices
     * in the specified row. No search is performed; the value is simply appended. If the ascending order of
     * column indices is broken, future behavior is unspecified. If the value is zero, nothing is stored.
     *
     * @param row   the row to which the value should be appended
     * @param col   the column index (must be after all existing column indices in the row)
     * @param value the value to insert (only nonzero values are actually stored)
     */
    public void putLast(final int row, final int col, final double value) {
        myRows.get(row).putLast(col, value);
    }

    public SparseArray<N> removeRow(final int index) {
        return myRows.remove(index);
    }

    @Override
    public void reset() {
        for (SparseArray<N> sparseArray : myRows) {
            sparseArray.reset();
        }
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

            public int getColDim() {
                return RowsSupplier.this.getColDim();
            }

            public int getRowDim() {
                return rows.length;
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
    public SparseArray<N> sliceRow(final long row) {
        return this.getRow(Math.toIntExact(row));
    }

    @Override
    public void supplyTo(final TransformableRegion<N> receiver) {

        receiver.reset();

        for (int i = 0, limit = myRows.size(); i < limit; i++) {
            myRows.get(i).supplyNonZerosTo(receiver.regionByRows(i));
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

        // Counting pass
        for (int i = 0; i < nbRows; i++) {
            SparseArray<N> row = myRows.get(i);
            for (SparseArray.NonzeroView<N> nz : row.nonzeros()) {
                int col = (int) nz.index();
                colPointers[col + 1]++;
            }
        }

        // Prefix sum
        for (int j = 0; j < nbCols; j++) {
            colPointers[j + 1] += colPointers[j];
        }

        // Filling pass
        int[] next = Arrays.copyOf(colPointers, nbCols);
        for (int i = 0; i < nbRows; i++) {
            SparseArray<N> row = myRows.get(i);
            for (SparseArray.NonzeroView<N> nz : row.nonzeros()) {
                int col = (int) nz.index();
                int pos = next[col]++;
                values[pos] = nz.doubleValue();
                rowIndices[pos] = i;
            }
        }

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

        int pos = 0;
        for (int i = 0; i < nbRows; i++) {
            rowPointers[i] = pos;
            SparseArray<N> row = myRows.get(i);
            for (SparseArray.NonzeroView<N> nz : row.nonzeros()) {
                values[pos] = nz.doubleValue();
                colIndices[pos] = (int) nz.index();
                pos++;
            }
        }
        rowPointers[nbRows] = pos;

        return new R064CSR(nbRows, nbCols, values, colIndices, rowPointers);
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
