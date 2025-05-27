package org.ojalgo.matrix.store;

abstract class CompressedR064 extends FactoryStore<Double> implements SparseStructure2D {

    public final int[] indices;
    public final int[] pointers;
    public final double[] values;

    CompressedR064(final int nbRows, final int nbCols, final double[] elementValues, final int[] minorIndices, final int[] majorPointers) {

        super(R064Store.FACTORY, nbRows, nbCols);

        values = elementValues;
        indices = minorIndices;
        pointers = majorPointers;
    }

    @Override
    public int countNonzeros() {
        return values.length;
    }

    @Override
    public final Double get(final int row, final int col) {
        return Double.valueOf(this.doubleValue(row, col));
    }

}
