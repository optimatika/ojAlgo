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

    /**
     * Ratio threshold: refactorise when eta-chain nonzeros exceed this multiple of the L+U factor nonzeros. A
     * lower value refactorises more frequently (better btran/ftran speed, more factorisation overhead); a
     * higher value delays refactorisation (faster pivots, slower solves as the chain grows).
     */
    private static final double ETA_FILL_RATIO = 1.5;

    /**
     * Hard ceiling on updates between refactorisations. The effective ceiling is
     * {@code min(UPDATES_LIMIT, UPDATES_MULTIPLIER * m)} where m is the basis dimension. This ensures small
     * bases (like FIT2D with m=26) refactorise frequently enough to maintain accuracy, while large bases get
     * the full ceiling.
     */
    private static final int UPDATES_LIMIT = 300;

    /**
     * Per-dimension multiplier for the update ceiling. The effective ceiling for a basis of dimension m is
     * {@code min(UPDATES_LIMIT, UPDATES_MULTIPLIER * m)}. A value of 3 means the ceiling scales as 3x the
     * basis dimension for small models.
     */
    private static final int UPDATES_MULTIPLIER = 3;

    private final int myEffectiveLimit;
    private final SparseLU mySparse = new SparseLU();
    private int myUpdateCounter = 0;

    SparseDecomposition(final int dim) {
        super();
        myEffectiveLimit = Math.min(UPDATES_LIMIT, UPDATES_MULTIPLIER * dim);
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
        if (!mySparse.isComputed() || !mySparse.updateColumn(exitIndex, matrix, enterColumn) || this.shouldRefactorise()) {
            this.reset(matrix, included);
            return true;
        }
        return false;
    }

    private boolean shouldRefactorise() {
        if (++myUpdateCounter >= myEffectiveLimit) {
            return true;
        }
        int factorNnz = mySparse.countFactorNonzeros();
        if (factorNnz > 0) {
            return mySparse.countEtaNonzeros() > ETA_FILL_RATIO * factorNnz;
        }
        return false;
    }

}