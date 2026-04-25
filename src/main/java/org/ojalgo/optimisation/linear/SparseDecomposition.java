package org.ojalgo.optimisation.linear;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.matrix.decomposition.SparseLU;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064CSC;

/**
 * Maintains a {@link SparseLU} decomposition of the basis matrix for efficient solving of linear systems in
 * the revised simplex method. Supports incremental updates using the Forrest-Tomlin algorithm when columns
 * change, with periodic refactorization to maintain numerical stability.
 * <p>
 * Refactorisation is triggered dynamically when the accumulated eta-chain fill-in exceeds a configurable
 * ratio of the original L+U nonzero count, or when a dimension-scaled update ceiling is reached.
 */
final class SparseDecomposition implements BasisRepresentation {

    private static final int ETA_MULTIPLIER = 3;
    private static final int MAX_UPDATES = 250;
    private static final int MIN_UPDATES = 25;

    private final int myUpperLimit;
    private final int myLowerLimit;
    private final SparseLU mySparse = new SparseLU();
    private int myUpdateCounter = 0;

    SparseDecomposition(final int dim) {
        super();
        myUpperLimit = Math.min(dim, MAX_UPDATES);
        myLowerLimit = Math.min(MIN_UPDATES, myUpperLimit);
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
    public void reset(final R064CSC matrix, final int[] included) {
        mySparse.factor(matrix, included);
        myUpdateCounter = 0;
    }

    /**
     * Updates the decomposition to reflect a change in the basis matrix. Uses the Forrest-Tomlin update
     * algorithm to efficiently modify the LU factors. Falls back to a complete refactorization when:
     * <ul>
     * <li>The eta-chain fill-in exceeds {@link #ETA_FILL_RATIO} times the original factor nonzeros
     * <li>The dimension-scaled update ceiling is reached
     * <li>The decomposition is not computed
     * <li>The update itself fails (e.g. singular pivot)
     * </ul>
     */
    @Override
    public boolean update(final R064CSC matrix, final int[] included, final int exitIndex, final int enterColumn) {
        if (!mySparse.isComputed() || this.shouldRefactor() || !mySparse.updateColumn(exitIndex, matrix, enterColumn)) {
            this.reset(matrix, included);
            return true;
        }
        ++myUpdateCounter;
        return false;
    }

    private boolean shouldRefactor() {
        if (myUpdateCounter < myLowerLimit) {
            return false;
        } else if (myUpdateCounter >= myUpperLimit) {
            return true;
        } else {
            return ETA_MULTIPLIER * mySparse.countEtaNonzeros() > mySparse.countFactorNonzeros();
        }
    }

}