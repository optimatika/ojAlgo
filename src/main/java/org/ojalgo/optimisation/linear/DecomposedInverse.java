package org.ojalgo.optimisation.linear;

import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;

final class DecomposedInverse implements BasisRepresentation {

    private final LU<Double> myDecomposition = LU.newSparseR064();
    private final R064Store myWork;

    DecomposedInverse(final int dim) {
        super();
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
    public void update(final MatrixStore<Double> basis, final int col, final SparseArray<Double> values) {

        if (!myDecomposition.updateColumn(col, values, myWork)) {
            myDecomposition.decompose(basis);
        }
    }

}
