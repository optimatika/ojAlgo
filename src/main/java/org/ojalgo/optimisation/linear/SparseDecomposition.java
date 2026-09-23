package org.ojalgo.optimisation.linear;

import org.ojalgo.array.DensityTrackingArray;
import org.ojalgo.matrix.decomposition.SparseLU;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.matrix.transformation.InvertibleFactor;

/**
 * The basis factorisation of the revised simplex: a {@link SparseLU} (factorised with
 * {@link SparseLU#factor(R064CSC, int[])}) with Forrest-Tomlin updates. This class refactorises instead of
 * updating when the updates have added too many nonzeros, after too many updates, or when a pivot is too
 * small, and it decides when the basis is to be considered singular. The store may force further
 * refactorisations for reasons of its own (see {@link RevisedStore#pivot(SimplexSolver.IterDescr)}).
 * <p>
 * The sparse solves ({@link #ftranColumn(R064CSC, int, DensityTrackingArray)} and
 * {@link #btranUnit(int, DensityTrackingArray)}) retain partial results that the following
 * {@link #update(R064CSC, int[], int, int)} reuses, so an update does no solves of its own.
 */
final class SparseDecomposition implements BasisRepresentation {

    /**
     * Refactorise when the nonzeros added by updates (R-etas plus replacement columns of U) exceed this
     * multiple of the nonzeros in the factorisation.
     */
    private static final double GROWTH_LIMIT = 2.0;
    /**
     * Maximum number of updates between refactorisations.
     */
    private static final int MAX_UPDATES = 100;
    /**
     * A pivot (from the factorisation or an update) smaller than this means the basis is (numerically)
     * singular.
     */
    private static final double PIVOT_TOLERANCE = 1E-11;

    private final int myDim;
    private final SparseLU myLU = new SparseLU();
    private boolean mySingular = false;
    private int myUpdateCount = 0;

    SparseDecomposition(final int dim) {
        super();
        myDim = dim;
    }

    @Override
    public void btran(final double[] arg) {
        if (myLU.isComputed()) {
            myLU.btran(arg);
        }
    }

    @Override
    public void btran(final PhysicalStore<Double> arg) {
        InvertibleFactor.doPrimitive(arg, this);
    }

    @Override
    public void btranUnit(final int position, final DensityTrackingArray result) {
        if (myLU.isComputed()) {
            myLU.btranUnit(position, result);
        } else {
            BasisRepresentation.super.btranUnit(position, result);
        }
    }

    @Override
    public int countUpdates() {
        return myUpdateCount;
    }

    @Override
    public void ftran(final double[] arg) {
        if (myLU.isComputed()) {
            myLU.ftran(arg);
        }
    }

    @Override
    public void ftran(final PhysicalStore<Double> arg) {
        InvertibleFactor.doPrimitive(this, arg);
    }

    @Override
    public void ftranColumn(final R064CSC matrix, final int column, final DensityTrackingArray result) {
        if (myLU.isComputed()) {
            myLU.ftranColumn(matrix, column, result);
        } else {
            BasisRepresentation.super.ftranColumn(matrix, column, result);
        }
    }

    @Override
    public int getColDim() {
        return myDim;
    }

    @Override
    public int getRowDim() {
        return myDim;
    }

    /**
     * A factorisation with a pivot smaller than {@link #PIVOT_TOLERANCE} is singular.
     */
    @Override
    public boolean isSingular() {
        return mySingular;
    }

    @Override
    public void reset(final R064CSC matrix, final int[] included) {
        myLU.factor(matrix, included);
        mySingular = myLU.getMinPivotMagnitude() < PIVOT_TOLERANCE;
        myUpdateCount = 0;
    }

    /**
     * Forrest-Tomlin update, or a refactorisation when: the basis is singular, the update limits are reached,
     * or the new pivot is too small.
     */
    @Override
    public boolean update(final R064CSC matrix, final int[] included, final int exitIndex, final int enterColumn) {

        if (!myLU.isComputed() || mySingular || myUpdateCount >= MAX_UPDATES || myLU.countEtaNonzeros() > GROWTH_LIMIT * myLU.countFactorNonzeros()) {
            this.reset(matrix, included);
            return true;
        }

        if (!myLU.updateColumn(exitIndex, matrix, enterColumn) || myLU.getMinPivotMagnitude() < PIVOT_TOLERANCE) {
            this.reset(matrix, included);
            return true;
        }

        myUpdateCount++;
        return false;
    }

}
