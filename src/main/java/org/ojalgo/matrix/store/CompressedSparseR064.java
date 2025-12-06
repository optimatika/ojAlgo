package org.ojalgo.matrix.store;

import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Factory2D;

abstract class CompressedSparseR064 extends FactoryStore<Double> implements SparseStructure2D {

    static abstract class Builder<I extends CompressedSparseR064> implements Factory2D.Builder<I> {

        private int myColDim = 0;
        private int myRowDim = 0;

        @Override
        public final int getColDim() {
            return myColDim;
        }

        @Override
        public final int getRowDim() {
            return myRowDim;
        }

        @Override
        public void reset() {
            myRowDim = 0;
            myColDim = 0;
        }

        @Override
        public final void set(final long row, final long col, final Comparable<?> value) {
            this.set(Math.toIntExact(row), Math.toIntExact(col), Scalar.doubleValue(value));

        }

        final void update(final int row, final int col) {
            myRowDim = Math.max(myRowDim, row + 1);
            myColDim = Math.max(myColDim, col + 1);
        }

    }

    public final int[] indices;
    public final int[] pointers;
    public final double[] values;

    CompressedSparseR064(final int nbRows, final int nbCols, final double[] elementValues, final int[] minorIndices, final int[] majorPointers) {

        super(R064Store.FACTORY, nbRows, nbCols);

        values = elementValues;
        indices = minorIndices;
        pointers = majorPointers;
    }

    @Override
    public final int countNonzeros() {
        return values.length;
    }

    @Override
    public final Double get(final int row, final int col) {
        return Double.valueOf(this.doubleValue(row, col));
    }

}
