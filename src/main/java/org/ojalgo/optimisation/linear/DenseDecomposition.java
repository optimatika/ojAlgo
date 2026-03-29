package org.ojalgo.optimisation.linear;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;

/**
 * Dense LU based {@link BasisRepresentation}. Uses {@link LU#R064} for the factorisation. Useful as a
 * benchmark baseline; the production simplex path uses the sparse variant {@link SparseDecomposition}.
 */
final class DenseDecomposition implements BasisRepresentation {

    private static final int UPDATES_LIMIT = 100;

    private final LU<Double> myDense;
    private int myUpdateCounter = 0;

    DenseDecomposition(final int dim) {
        super();
        myDense = LU.R064.make(dim, dim);
    }

    @Override
    public void btran(final double[] arg) {
        if (myDense.isComputed()) {
            myDense.btran(arg);
        }
    }

    @Override
    public void btran(final PhysicalStore<Double> arg) {
        if (myDense.isComputed()) {
            if (arg instanceof ArrayR064) {
                myDense.btran(((ArrayR064) arg).data);
            } else {
                myDense.btran(arg);
            }
        }
    }

    @Override
    public void ftran(final double[] arg) {
        if (myDense.isComputed()) {
            myDense.ftran(arg);
        }
    }

    @Override
    public void ftran(final PhysicalStore<Double> arg) {
        if (myDense.isComputed()) {
            if (arg instanceof ArrayR064) {
                myDense.ftran(((ArrayR064) arg).data);
            } else {
                myDense.ftran(arg);
            }
        }
    }

    @Override
    public int getColDim() {
        return myDense.getColDim();
    }

    @Override
    public int getRowDim() {
        return myDense.getRowDim();
    }

    @Override
    public void reset(final MatrixStore<Double> basis) {
        myDense.decompose(basis);
        myUpdateCounter = 0;
    }

    @Override
    public void update(final MatrixStore<Double> basis, final int col, final SparseArray<Double> values) {
        if (myUpdateCounter++ >= UPDATES_LIMIT || !myDense.isComputed() || !myDense.updateColumn(col, values)) {
            this.reset(basis);
        }
    }

}
