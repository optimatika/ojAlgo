package org.ojalgo.optimisation.linear;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.DensityTrackingArray;
import org.ojalgo.matrix.decomposition.ForestTomlinFactor;
import org.ojalgo.matrix.store.ColumnsSupplier;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.matrix.store.R064Store;

/**
 * {@link BasisRepresentation} backed by {@link ForestTomlinFactor} (Markowitz LU with Forest-Tomlin updates).
 * Adapts the hyper-sparse-vector-based API of ForestTomlinFactor to the dense-array-based API required by the
 * revised simplex solver.
 *
 * @see SparseDecomposition
 */
final class MarkowitzDecomposition implements BasisRepresentation {

    private static ColumnsSupplier<Double> extractBasis(final R064CSC matrix, final int[] included) {

        int m = matrix.getRowDim();

        ColumnsSupplier<Double> basis = R064Store.FACTORY.makeColumnsSupplier(m);
        basis.addColumns(included.length);

        for (int j = 0; j < included.length; j++) {
            int srcCol = included[j];
            for (int k = matrix.pointers[srcCol], limit = matrix.pointers[srcCol + 1]; k < limit; k++) {
                basis.set(matrix.indices[k], j, matrix.values[k]);
            }
        }

        return basis;
    }

    private final int myDim;

    private ForestTomlinFactor myFactor;

    MarkowitzDecomposition(final int dim) {
        super();
        myDim = dim;
        myFactor = new ForestTomlinFactor(dim);
    }

    @Override
    public void btran(final double[] arg) {
        DensityTrackingArray v = DensityTrackingArray.wrap(arg);
        myFactor.btran(v);
    }

    @Override
    public void btran(final PhysicalStore<Double> arg) {
        if (arg instanceof ArrayR064) {
            this.btran(((ArrayR064) arg).data);
        } else {
            double[] tmp = arg.toRawCopy1D();
            this.btran(tmp);
            for (int i = 0; i < tmp.length; i++) {
                arg.set(i, tmp[i]);
            }
        }
    }

    @Override
    public void ftran(final double[] arg) {
        DensityTrackingArray v = DensityTrackingArray.wrap(arg);
        myFactor.ftran(v);
    }

    @Override
    public void ftran(final PhysicalStore<Double> arg) {
        if (arg instanceof ArrayR064) {
            this.ftran(((ArrayR064) arg).data);
        } else {
            double[] tmp = arg.toRawCopy1D();
            this.ftran(tmp);
            for (int i = 0; i < tmp.length; i++) {
                arg.set(i, tmp[i]);
            }
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

    @Override
    public void reset(final R064CSC matrix, final int[] included) {
        ColumnsSupplier<Double> basis = MarkowitzDecomposition.extractBasis(matrix, included);
        myFactor = new ForestTomlinFactor(myDim);
        myFactor.build(basis);
    }

    @Override
    public boolean update(final R064CSC matrix, final int[] included, final int exitIndex, final int enterColumn) {

        // Compute aq = B^{-1} * a_q (entering column through FTRAN)
        double[] col = new double[myDim];
        matrix.supplyTo(enterColumn, col);
        DensityTrackingArray aq = DensityTrackingArray.wrap(col);
        myFactor.ftran(aq);

        DensityTrackingArray ep = DensityTrackingArray.unit(myDim, exitIndex);
        myFactor.btran(ep);

        try {
            myFactor.update(exitIndex, aq, ep);
        } catch (ArithmeticException ae) {
            this.reset(matrix, included);
            return true;
        }

        if (myFactor.shouldRefactor()) {
            this.reset(matrix, included);
            return true;
        }

        return false;
    }
}
