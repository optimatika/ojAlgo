package org.ojalgo.matrix.store;

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.Arrays;

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

    /**
     * Performs target[j] += scalar * A[row, j] or target[i] += scalar * A[i, col] for all nonzero entries in
     * the specified row/col.
     *
     * @param index  The row/col index
     * @param scalar The scalar multiplier
     * @param target The target array
     */
    public final void axpy(final int index, final double scalar, final double[] target) {

        for (int k = pointers[index], limit = pointers[index + 1]; k < limit; k++) {
            target[indices[k]] += scalar * values[k];
        }
    }

    public final int capacity() {
        return values.length;
    }

    /**
     * @param index The row/col index
     * @return The number of non-zero elements in the specified row/column
     */
    public final int capacity(final int index) {
        return pointers[index + 1] - pointers[index];
    }

    @Override
    public final int countNonzeros() {
        return values.length;
    }

    /**
     * Computes the dot product of the specified row/column with a dense vector.
     *
     * @param index  The row/col index
     * @param vector The dense vector
     * @return The dot product sum(A[row, j] * vector[j]) or sum(A[i, col] * vector[i])
     */
    public final double dot(final int index, final double[] vector) {

        double sum = ZERO;
        for (int k = pointers[index], limit = pointers[index + 1]; k < limit; k++) {
            sum += values[k] * vector[indices[k]];
        }
        return sum;
    }

    @Override
    public final Double get(final int row, final int col) {
        return Double.valueOf(this.doubleValue(row, col));
    }

    /**
     * Copies the specified row/col into a dense target array, resetting it first.
     *
     * @param index  The row/col index
     * @param target The target array will be reset with zeros then populated
     */
    public final void supplyTo(final int index, final double[] target) {

        Arrays.fill(target, ZERO);

        for (int k = pointers[index], limit = pointers[index + 1]; k < limit; k++) {
            target[indices[k]] = values[k];
        }
    }

}
