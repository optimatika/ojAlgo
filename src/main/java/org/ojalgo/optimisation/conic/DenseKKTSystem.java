package org.ojalgo.optimisation.conic;

import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.structure.Access1D;

/**
 * Dense KKT system implementation used for small to medium problems. Assembles the full saddle-point
 * matrix when equalities are present; otherwise uses a Cholesky solve on the SPD Hessian.
 *
 * Preallocates and reuses assembly buffers; grows them if needed.
 */
final class DenseKKTSystem implements KKTSystem {

    private R064Store myK;
    private R064Store myRhs;

    DenseKKTSystem() {
        // Lazy sizing on first use
    }

    DenseKKTSystem(final int n, final int meq) {
        this.ensureCapacity(n, meq);
    }

    private void ensureCapacity(final int n, final int meq) {
        int dim = n + meq;
        if (myK == null || myK.countRows() != dim || myK.countColumns() != dim) {
            myK = R064Store.FACTORY.make(dim, dim);
        }
        if (myRhs == null || myRhs.countRows() != dim) {
            myRhs = R064Store.FACTORY.make(dim, 1);
        }
    }

    @Override
    public boolean solve(final MatrixStore<Double> H, final MatrixStore<Double> Aeq, final Access1D<?> rx, final Access1D<?> ry,
            final PhysicalStore<Double> outDx, final PhysicalStore<Double> outY) {

        final int n = (int) H.countRows();
        final int meq = Aeq != null ? (int) Aeq.countRows() : 0;

        if (meq == 0) {
            // Try Cholesky first on H x = rx
            Cholesky<Double> chol = Cholesky.R064.make(H);
            R064Store rhsCol = R064Store.FACTORY.make(n, 1);
            for (int i = 0; i < n; i++) rhsCol.set(i, rx.doubleValue(i));
            if (!chol.compute(H)) {
                R064Store Hreg = R064Store.FACTORY.copy(H);
                double diag = 1e-9;
                for (int i = 0; i < n; i++) Hreg.add(i, i, diag);
                if (!chol.compute(Hreg)) {
                    return false;
                }
            }
            MatrixStore<Double> dx = chol.getSolution(rhsCol);
            for (int i = 0; i < n; i++) outDx.set(i, dx.doubleValue(i));
            if (outY != null) outY.fillAll(0.0);
            return true;
        }

        // Assemble full KKT
        this.ensureCapacity(n, meq);
        myK.fillAll(0.0);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double hij = H.doubleValue(i, j);
                if (hij != 0.0) myK.set(i, j, hij);
            }
        }
        for (int r = 0; r < meq; r++) {
            for (int c = 0; c < n; c++) {
                double a = Aeq.doubleValue(r, c);
                if (a != 0.0) {
                    myK.set(n + r, c, a);
                    myK.set(c, n + r, a);
                }
            }
        }

        // RHS
        myRhs.fillAll(0.0);
        for (int i = 0; i < n; i++) myRhs.set(i, rx.doubleValue(i));
        for (int r = 0; r < meq; r++) myRhs.set(n + r, ry.doubleValue(r));

        // Factor & solve
        LU<Double> lu = LU.R064.make(myK);
        if (!lu.compute(myK)) {
            for (int i = 0; i < n; i++) myK.add(i, i, 1e-9);
            if (!lu.compute(myK)) {
                return false;
            }
        }
        MatrixStore<Double> sol = lu.getSolution(myRhs);
        for (int i = 0; i < n; i++) outDx.set(i, sol.doubleValue(i));
        if (outY != null) {
            for (int r = 0; r < meq; r++) outY.set(r, sol.doubleValue(n + r));
        }
        return true;
    }
}
