package org.ojalgo.optimisation.linear;

import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;

final class DecomposedInverse implements BasisRepresentation {

    private final LU<Double> myDecomposition;
    private final R064Store myWork;

    DecomposedInverse(final boolean sparse, final int dim) {
        super();
        myDecomposition = sparse ? null : LU.R064.make(dim, 1);
        myWork = R064Store.FACTORY.make(dim, 1);
    }

    @Override
    public void btran(final PhysicalStore<Double> arg) {
        myDecomposition.btran(arg);
    }

    @Override
    public void ftran(final PhysicalStore<Double> arg) {
        myDecomposition.ftran(arg);
    }

    @Override
    public int getColDim() {
        return myDecomposition.getColDim();
    }

    @Override
    public int getRowDim() {
        return myDecomposition.getRowDim();
    }

    @Override
    public void reset(final MatrixStore<Double> basis) {
        myDecomposition.decompose(basis);
    }

    @Override
    public void update(final MatrixStore<Double> basis, final int col, final SparseArray<Double> values) {

        if (!myDecomposition.updateColumn(col, values, myWork)) {
            myDecomposition.decompose(basis);
        }
    }

}
