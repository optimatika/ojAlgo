package org.ojalgo.optimisation.linear;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.decomposition.SparseLU;
import org.ojalgo.matrix.store.ColumnsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;

/**
 * Maintains a {@link SparseLU} decomposition of the basis matrix for efficient solving of linear systems in
 * the revised simplex method. Supports incremental updates using the Forrest-Tomlin algorithm when columns
 * change, with periodic refactorization to maintain numerical stability.
 */
final class SparseDecomposition implements BasisRepresentation {

    /**
     * Maximum number of updates before forcing a complete refactorization to prevent numerical instability
     * from accumulated roundoff errors.
     */
    private static final int UPDATES_LIMIT = 100;

    private final SparseLU mySparse = new SparseLU();
    private int myUpdateCounter = 0;

    SparseDecomposition() {
        super();
    }

    @Override
    public void btran(final double[] arg) {
        if (mySparse.isComputed()) {
            mySparse.btran(arg);
        }
    }

    @Override
    public void btran(final PhysicalStore<Double> arg) {
        if (mySparse.isComputed()) {
            if (arg instanceof ArrayR064) {
                mySparse.btran(((ArrayR064) arg).data);
            } else {
                mySparse.btran(arg);
            }
        }
    }

    @Override
    public void ftran(final double[] arg) {
        if (mySparse.isComputed()) {
            mySparse.ftran(arg);
        }
    }

    @Override
    public void ftran(final PhysicalStore<Double> arg) {
        if (mySparse.isComputed()) {
            if (arg instanceof ArrayR064) {
                mySparse.ftran(((ArrayR064) arg).data);
            } else {
                mySparse.ftran(arg);
            }
        }
    }

    @Override
    public int getColDim() {
        return mySparse.getColDim();
    }

    @Override
    public int getRowDim() {
        return mySparse.getRowDim();
    }

    @Override
    public void reset(final MatrixStore<Double> basis) {
        if (basis instanceof ColumnsSupplier.Selection<?>) {
            mySparse.factor((ColumnsSupplier.Selection<Double>) basis);
        } else {
            mySparse.decompose(basis);
        }
        myUpdateCounter = 0;
    }

    /**
     * Updates the decomposition to reflect a change in the basis matrix. Uses the Forrest-Tomlin update
     * algorithm to efficiently modify the LU factors. Falls back to a complete refactorization if the update
     * counter exceeds the limit, the decomposition is not computed, or the update fails.
     */
    @Override
    public void update(final MatrixStore<Double> basis, final int col, final SparseArray<Double> values) {
        if (myUpdateCounter++ >= UPDATES_LIMIT || !mySparse.isComputed() || !mySparse.updateColumn(col, values)) {
            this.reset(basis);
        }
    }

}
