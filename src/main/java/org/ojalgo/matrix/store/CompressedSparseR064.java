package org.ojalgo.matrix.store;

import org.ojalgo.function.UnaryFunction;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Mutate2D;

abstract class CompressedSparseR064 extends FactoryStore<Double> implements SparseStructure2D {

    static abstract class Builder<I extends CompressedSparseR064> implements Factory2D.Builder<I>, Mutate2D.Modifiable<Double> {

        private int myColDim = 0;
        private int myRowDim = 0;

        @Override
        public final void add(final long row, final long col, final Comparable<?> addend) {
            this.add(Math.toIntExact(row), Math.toIntExact(col), Scalar.doubleValue(addend));

        }

        @Override
        public final void add(final long row, final long col, final double addend) {
            this.add(Math.toIntExact(row), Math.toIntExact(col), addend);
        }

        @Override
        public final int getColDim() {
            return myColDim;
        }

        @Override
        public final int getRowDim() {
            return myRowDim;
        }

        /**
         * Will throw UnsupportedOperationException!
         */
        @Override
        public final void modifyOne(final long row, final long col, final UnaryFunction<Double> modifier) {
            throw new UnsupportedOperationException();
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

    public final int capacity() {
        return this.countNonzeros();
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
