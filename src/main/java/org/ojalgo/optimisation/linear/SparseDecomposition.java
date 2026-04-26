package org.ojalgo.optimisation.linear;

import java.util.concurrent.atomic.AtomicLong;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.matrix.decomposition.SparseLU;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.context.NumberContext;

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
     * Refactor when {@code ETA_MULTIPLIER * etaNonzeros > factorNonzeros}. Lower values refactor more often
     * (shorter eta chains, fresher numerics, more refactor cost); higher values let the eta chain grow.
     * Package-private and non-final to permit benchmark-time tuning from tests.
     */
    static int ETA_MULTIPLIER = 3;
    /**
     * Hard ceiling on updates between refactorisations (a backstop in case the other gates stay silent).
     * Package-private and non-final to permit benchmark-time tuning from tests.
     */
    static int MAX_UPDATES = 250;
    /**
     * Gate on diagonal-spread degradation since the last factorisation. Refactorisation fires when the
     * current spread {@code max/min} has grown by more than the gate's tolerance relative to the spread
     * snapshotted at factorisation time. Measuring degradation (not absolute spread) avoids refactor-loops
     * on bases that are already ill-conditioned, where refactoring won't help — only updates that have
     * worsened things should trigger. Package-private and non-final to permit benchmark-time tuning.
     */
    static NumberContext PIVOT_DEGRADATION_GATE = NumberContext.of(5);

    /**
     * Diagnostic counters for which trigger caused refactorisation. Process-wide; call
     * {@link #resetCounters()} before a measured run and {@link #logCounters(String)} after.
     */
    static final AtomicLong COUNT_UPDATES = new AtomicLong();
    static final AtomicLong COUNT_TRIGGER_MAX = new AtomicLong();
    static final AtomicLong COUNT_TRIGGER_PIVOT = new AtomicLong();
    static final AtomicLong COUNT_TRIGGER_ETA = new AtomicLong();
    static final AtomicLong COUNT_TRIGGER_FAIL = new AtomicLong();

    static void resetCounters() {
        COUNT_UPDATES.set(0);
        COUNT_TRIGGER_MAX.set(0);
        COUNT_TRIGGER_PIVOT.set(0);
        COUNT_TRIGGER_ETA.set(0);
        COUNT_TRIGGER_FAIL.set(0);
    }

    static void logCounters(final String tag) {
        long upd = COUNT_UPDATES.get();
        long max = COUNT_TRIGGER_MAX.get();
        long piv = COUNT_TRIGGER_PIVOT.get();
        long eta = COUNT_TRIGGER_ETA.get();
        long fail = COUNT_TRIGGER_FAIL.get();
        long total = max + piv + eta + fail;
        BasicLogger.debug("{}: updates={} refactors={} (max={}, pivot={}, eta={}, failed={})", tag, upd, total, max, piv, eta, fail);
    }

    private final SparseLU mySparse = new SparseLU();
    private int myUpdateCounter = 0;
    private final int myUpperLimit;

    SparseDecomposition(final int dim) {
        super();
        myUpperLimit = Math.min(dim, MAX_UPDATES);
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
        COUNT_UPDATES.incrementAndGet();
        if (!mySparse.isComputed() || this.shouldRefactor()) {
            this.reset(matrix, included);
            return true;
        }
        if (!mySparse.updateColumn(exitIndex, matrix, enterColumn)) {
            COUNT_TRIGGER_FAIL.incrementAndGet();
            this.reset(matrix, included);
            return true;
        }
        ++myUpdateCounter;
        return false;
    }

    private boolean shouldRefactor() {
        if (myUpdateCounter >= myUpperLimit) {
            COUNT_TRIGGER_MAX.incrementAndGet();
            return true;
        }
        // Fire when current spread exceeds factor-time spread by more than the gate's tolerance.
        // Equivalently: (factorMax/factorMin) / (currentMax/currentMin) < epsilon.
        double currentMax = mySparse.getMaxPivotMagnitude();
        double currentMin = mySparse.getMinPivotMagnitude();
        double factorMax = mySparse.getFactorMaxPivotMagnitude();
        double factorMin = mySparse.getFactorMinPivotMagnitude();
        if (PIVOT_DEGRADATION_GATE.isSmall(currentMax * factorMin, currentMin * factorMax)) {
            COUNT_TRIGGER_PIVOT.incrementAndGet();
            return true;
        }
        if (ETA_MULTIPLIER * mySparse.countEtaNonzeros() > mySparse.countFactorNonzeros()) {
            COUNT_TRIGGER_ETA.incrementAndGet();
            return true;
        }
        return false;
    }

}