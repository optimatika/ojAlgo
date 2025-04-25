package org.ojalgo.matrix.store;

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.ArrayList;
import java.util.List;

import org.ojalgo.structure.Factory2D;
import org.ojalgo.type.NumberDefinition;

abstract class CompressedR064 extends FactoryStore<Double> implements SparseStructure2D {

    static final class Builder<T extends CompressedR064> implements Factory2D.Builder<T>, SparseStructure2D {

        private final int myColDim;
        private final List<Triplet> myElements = new ArrayList<>();
        private final CompressedR064.Factory<T> myFactory;
        private final int myRowDim;

        Builder(final int nbRows, final int nbCols, final CompressedR064.Factory<T> factory) {
            super();
            myRowDim = nbRows;
            myColDim = nbCols;
            myFactory = factory;
        }

        @Override
        public T build() {
            return myFactory.make(myRowDim, myColDim, myElements);
        }

        @Override
        public double density() {
            double nz = myElements.size();
            return nz / this.count();
        }

        @Override
        public int getColDim() {
            return myColDim;
        }

        @Override
        public int getRowDim() {
            return myRowDim;
        }

        @Override
        public void set(final int row, final int col, final double value) {
            if (value != ZERO) {
                myElements.add(new Triplet(row, col, value));
            }
        }

        @Override
        public void set(final long row, final long col, final Comparable<?> value) {
            this.set(row, col, NumberDefinition.doubleValue(value));
        }

        @Override
        public List<Triplet> toTriplets() {
            return myElements;
        }

    }

    interface Factory<T extends CompressedR064> {

        T make(int nbRows, int nbCols, List<Triplet> elements);

    }

    final int[] indices;
    final int[] pointers;
    final double[] values;

    CompressedR064(final int nbRows, final int nbCols, final double[] elementValues, final int[] minorIndices, final int[] majorPointers) {
        super(R064Store.FACTORY, nbRows, nbCols);
        values = elementValues;
        indices = minorIndices;
        pointers = majorPointers;
    }

    @Override
    public final Double get(final int row, final int col) {
        return Double.valueOf(this.doubleValue(row, col));
    }

}
