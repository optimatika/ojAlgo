package org.ojalgo.matrix.decomposition;

import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.store.ColumnsSupplier;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.matrix.store.R064CSR;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.matrix.store.SparseStructure2D;
import org.ojalgo.structure.Primitive2D;

final class DualSparse extends Primitive2D implements SparseStructure2D {

    private final ColumnsSupplier<Double> myColumns;
    private final RowsSupplier<Double> myRows;
    private final double[] myDiagonal;

    DualSparse(final int nbRows, final int nbCols) {
        super();
        myColumns = R064Store.FACTORY.makeColumnsSupplier(nbRows);
        myColumns.addColumns(nbCols);
        myRows = R064Store.FACTORY.makeRowsSupplier(nbCols);
        myRows.addRows(nbRows);
        myDiagonal = new double[Math.min(nbRows, nbCols)];
    }

    @Override
    public int countNonzeros() {
        return myRows.countNonzeros();
    }

    @Override
    public double doubleValue(final int row, final int col) {
        if (row == col) {
            return myDiagonal[row];
        } else {
            return myColumns.doubleValue(row, col);
        }
    }

    @Override
    public int getColDim() {
        return myRows.getColDim();
    }

    @Override
    public int getRowDim() {
        return myColumns.getRowDim();
    }

    @Override
    public void set(final int row, final int col, final double value) {
        if (row == col) {
            myDiagonal[row] = value;
        } else {
            myColumns.set(row, col, value);
            myRows.set(row, col, value);
        }
    }

    @Override
    public R064CSC toCSC() {
        return myColumns.toCSC();
    }

    @Override
    public R064CSR toCSR() {
        return myRows.toCSR();
    }

    SparseArray<Double> getColumn(final int index) {
        return myColumns.getColumn(index);
    }

    double[] getDiagonal() {
        return myDiagonal;
    }

    SparseArray<Double> getRow(final int index) {
        return myRows.getRow(index);
    }

    /**
     * The input element needs to be the last element on the existing row AND column – all zeros below and to
     * the right of this point. When/if that requirement is met, then this alternative is safe and faster than
     * {@link #set(int, int, double)}.
     * <p>
     * Cannot be used for updates, only for new non-zeros in a strictly increasing index order.
     *
     * @see SparseArray#putLast(int, double)
     */
    void putLast(final int row, final int col, final double value) {
        if (row == col) {
            myDiagonal[row] = value;
        } else {
            myColumns.putLast(row, col, value);
            myRows.putLast(row, col, value);
        }
    }

}
