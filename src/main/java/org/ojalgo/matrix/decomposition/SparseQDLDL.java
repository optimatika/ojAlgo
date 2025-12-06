package org.ojalgo.matrix.decomposition;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.operation.COPY;
import org.ojalgo.matrix.store.DiagonalStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.matrix.store.R064CSC.Builder;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Access2D.ColumnView;
import org.ojalgo.structure.ElementView2D;

/**
 * Quasi-Definite LDL (QDLDL) sparse decomposition.
 * <p>
 * Reference: https://github.com/osqp/qdldl
 */
public final class SparseQDLDL extends AbstractDecomposition<Double, R064Store> implements LDL<Double> {

    /**
     * Symbolic elimination tree
     */
    static final class EliminationTree {

        /**
         * The number of non-zeros in each column of L
         */
        final int[] colNz;
        /**
         * The total number of non-zeros in L
         */
        final int totNz;
        /**
         * The elimination tree
         */
        final int[] tree;

        EliminationTree(final int[] colNz, final int[] tree, final int totNz) {
            this.colNz = colNz;
            this.tree = tree;
            this.totNz = totNz;
        }
    }

    private static final int UNKNOWN = -1;
    private static final boolean UNUSED = false;
    private static final boolean USED = true;

    static EliminationTree computeEliminationTree(final int n, final int[] pointers, final int[] indices) {

        int[] work = new int[n];
        int[] tree = new int[n];
        int[] colNz = new int[n];
        int totNz = 0;

        Arrays.fill(tree, UNKNOWN);

        int pj, pj1, i;
        for (int j = 0; j < n; j++) {
            pj = pointers[j];
            pj1 = pointers[j + 1];
            if (pj >= pj1) {
                // Empty column, or integer overflow
                throw new IllegalStateException();
            }
            work[j] = j;
            for (int p = pj; p < pj1; p++) {
                i = indices[p];
                if (i > j) {
                    // Lower triangle entry
                    throw new IllegalStateException();
                }
                while (work[i] != j) {
                    if (tree[i] == UNKNOWN) {
                        tree[i] = j;
                    }
                    colNz[i]++;
                    totNz++;
                    work[i] = j;
                    i = tree[i];
                }
            }
        }

        return new EliminationTree(colNz, tree, totNz);
    }

    static void solveD(final int n, final double[] inv, final double[] x) {
        for (int i = 0; i < n; i++) {
            x[i] *= inv[i]; // inv(D)
        }
    }

    static void solveL(final int n, final int[] pointers, final int[] indices, final double[] values, final double[] x) {
        for (int j = 0; j < n; j++) {
            double xj = x[j];
            for (int ji = pointers[j], jm = pointers[j + 1]; ji < jm; ji++) {
                x[indices[ji]] -= values[ji] * xj;
            }
        }
    }

    static void solveU(final int n, final int[] pointers, final int[] indices, final double[] values, final double[] x) {
        for (int j = n - 1; j >= 0; j--) {
            double dxj = ZERO;
            for (int ji = pointers[j], jm = pointers[j + 1]; ji < jm; ji++) {
                dxj -= values[ji] * x[indices[ji]];
            }
            x[j] += dxj;
        }
    }

    private double[] myD;
    private double[] myDinv;
    private R064CSC myL;
    private int myPositiveValuesInD;

    public SparseQDLDL() {
        super(R064Store.FACTORY);
    }

    @Override
    public void btran(final PhysicalStore<Double> arg) {
        if (arg instanceof R064Store) {
            this.solveOneColumn(((R064Store) arg).data);
        } else {
            double[] x = arg.toRawCopy1D();
            this.solveOneColumn(x);
            COPY.invoke(x, arg);
        }
    }

    @Override
    public Double calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    @Override
    public int countSignificant(final double threshold) {
        int significant = 0;
        if (myD != null) {
            for (int i = 0; i < myD.length; i++) {
                if (Math.abs(myD[i]) > threshold) {
                    significant++;
                }
            }
        }
        return significant;
    }

    @Override
    public boolean decompose(final Collectable<Double, ? super TransformableRegion<Double>> matrix) {

        Access2D<Double> access;
        if (matrix instanceof Access2D) {
            access = (Access2D<Double>) matrix;
        } else {
            access = matrix.collect(SparseStore.R064);
        }
        Builder builder = R064CSC.newBuilder();
        for (ElementView2D<Double, ?> nz : access.nonzeros()) {
            if (nz.row() <= nz.column()) {
                builder.set(nz.row(), nz.column(), nz.doubleValue());
            }
        }
        return this.factor(builder.build());
    }

    /**
     * Requirements on the input matrix and summary of the factorisation semantics:
     * <ul>
     * <li>Square.</li>
     * <li>Symmetric, with only the upper/right triangle stored. The lower/left triangle is not ignored; any
     * explicitly stored non-zero entries there will cause the symbolic phase to fail.</li>
     * <li>Sparse (not strictly required, but this implementation is optimised for sparse structure).</li>
     * <li>Quasi-definite for a fully solvable system: during factorisation the diagonal entries D[i] are
     * classified relative to a small, scale-dependent tolerance derived from the largest |D[i]| seen so far
     * and the dimensional epsilon. All D[i] must be classified as positive for {@link #isSolvable()} to
     * return {@code true}.</li>
     * </ul>
     * The underlying QDLDL-style algorithm will return false if any diagonal entry in D is classified as
     * (numerically) zero. Indefinite or near-singular matrices may still factorise, but will typically yield
     * {@code isSolvable() == false} and are not suitable for use as quasi-definite systems when solving or
     * inverting.
     */
    public boolean factor(final R064CSC matrix) {
        this.reset();
        EliminationTree eTree = SparseQDLDL.computeEliminationTree(matrix.getColDim(), matrix.pointers, matrix.indices);
        boolean ok = this.decompose(matrix, eTree);
        return this.computed(ok);
    }

    @Override
    public void ftran(final PhysicalStore<Double> arg) {
        if (arg instanceof R064Store) {
            this.solveOneColumn(((R064Store) arg).data);
        } else {
            double[] x = arg.toRawCopy1D();
            this.solveOneColumn(x);
            COPY.invoke(x, arg);
        }
    }

    @Override
    public int getColDim() {
        return myL != null ? myL.getColDim() : 0;
    }

    @Override
    public MatrixStore<Double> getD() {
        return myD != null ? DiagonalStore.wrap(myD) : null;
    }

    /**
     * Determinant of the factorised matrix.
     * <p>
     * With no pivoting the determinant is computed as the product of the diagonal entries of D returned by
     * the LDL factorisation. For a quasi-definite input (all diagonal entries classified as positive
     * according to the internal tolerance) {@link #isSolvable()} is {@code true} and the determinant is
     * strictly positive. For general symmetric or indefinite inputs the determinant may be negative or close
     * to zero and should be interpreted with care. If the factorisation has not been computed this method
     * returns NaN.
     */
    @Override
    public Double getDeterminant() {
        if (myD == null) {
            return NaN;
        }
        double det = ONE;
        for (int i = 0; i < myD.length; i++) {
            det *= myD[i];
        }
        return det;
    }

    @Override
    public MatrixStore<Double> getInverse(final PhysicalStore<Double> preallocated) {
        int dim = this.getColDim();
        preallocated.fillAll(ZERO);
        for (int i = 0; i < dim; i++) {
            preallocated.set(i, i, ONE);
        }
        return this.getSolution(preallocated, preallocated);
    }

    @Override
    public MatrixStore<Double> getL() {
        return myL;
    }

    @Override
    public int[] getPivotOrder() {
        int dim = this.getColDim();
        int[] order = new int[dim];
        for (int i = 0; i < dim; i++) {
            order[i] = i;
        }
        return order;
    }

    @Override
    public double getRankThreshold() {
        if (myD == null || myD.length == 0) {
            return ZERO;
        }
        double largest = ZERO;
        for (int i = 0; i < myD.length; i++) {
            largest = Math.max(largest, Math.abs(myD[i]));
        }
        return largest * this.getDimensionalEpsilon();
    }

    @Override
    public int[] getReversePivotOrder() {
        return this.getPivotOrder();
    }

    @Override
    public int getRowDim() {
        return myL != null ? myL.getRowDim() : 0;
    }

    @Override
    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {

        rhs.supplyTo(preallocated);

        int n = this.getColDim();
        if (n <= 0) {
            return preallocated;
        }

        double[] x = new double[n];

        if (n == 1) {

            COPY.invoke(preallocated, x);
            this.solveOneColumn(x);
            COPY.invoke(x, preallocated);

        } else {

            for (ColumnView<Double> column : preallocated.columns()) {
                COPY.invoke(column, x);
                this.solveOneColumn(x);
                COPY.invoke(x, column);
            }
        }

        return preallocated;
    }

    @Override
    public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
        this.decompose(this.wrap(original));
        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        } else {
            throw RecoverableCondition.newMatrixNotInvertible();
        }
    }

    @Override
    public boolean isPivoted() {
        return false;
    }

    @Override
    public boolean isSolvable() {
        return super.isSolvable();
    }

    @Override
    public PhysicalStore<Double> preallocate(final int nbEquations, final int nbVariables, final int nbSolutions) {
        return this.makeZero(nbEquations, nbSolutions);
    }

    @Override
    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
        this.decompose(this.wrap(body));
        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs), preallocated);
        } else {
            throw RecoverableCondition.newEquationSystemNotSolvable();
        }
    }

    /**
     * Solve A x = b
     */
    public double[] solve(final double[] b) {

        double[] x = b.clone();

        this.solveOneColumn(x);

        return x;
    }

    private boolean decompose(final R064CSC matrix, final EliminationTree eTree) {

        int[] Lnz = eTree.colNz;
        int[] etree = eTree.tree;
        int n = matrix.getColDim();
        int m = matrix.getRowDim();
        int[] Ap = matrix.pointers;
        int[] Ai = matrix.indices;
        double[] Ax = matrix.values;
        int[] Lp = new int[n + 1];
        int[] Li = new int[eTree.totNz];
        double[] Lx = new double[eTree.totNz];
        double[] D = new double[n];
        double[] Dinv = new double[n];

        int i, j, k, nnzY, bidx, cidx, nextIdx, nnzE, tmpIdx;
        int[] yIdx = new int[n];
        int[] elimBuffer = new int[n];
        int[] LNextSpaceInCol = new int[n];
        double[] yVals = new double[n];
        double yVals_cidx;
        boolean[] yMarkers = new boolean[n];
        int positiveValuesInD = 0;
        double maxAbsD = ZERO;

        Lp[0] = 0;
        for (i = 0; i < n; i++) {
            Lp[i + 1] = Lp[i] + Lnz[i];
            yMarkers[i] = UNUSED;
            yVals[i] = ZERO;
            D[i] = ZERO;
            LNextSpaceInCol[i] = Lp[i];
        }

        D[0] = Ax[0];
        maxAbsD = Math.abs(D[0]);
        if (D[0] == ZERO) {
            return false;
        }
        if (D[0] > ZERO) {
            positiveValuesInD++;
        }
        Dinv[0] = ONE / D[0];

        for (k = 1; k < n; k++) {

            nnzY = 0;
            tmpIdx = Ap[k + 1];

            for (i = Ap[k]; i < tmpIdx; i++) {

                bidx = Ai[i];

                if (bidx == k) {
                    D[k] = Ax[i];
                    continue;
                }

                yVals[bidx] = Ax[i];

                nextIdx = bidx;

                if (yMarkers[nextIdx] == UNUSED) {

                    yMarkers[nextIdx] = USED;
                    elimBuffer[0] = nextIdx;
                    nnzE = 1;

                    nextIdx = etree[bidx];

                    while (nextIdx != UNKNOWN && nextIdx < k) {
                        if (yMarkers[nextIdx] == USED) {
                            break;
                        }

                        yMarkers[nextIdx] = USED;
                        elimBuffer[nnzE] = nextIdx;
                        nnzE++;
                        nextIdx = etree[nextIdx];

                    }

                    while (nnzE > 0) {
                        yIdx[nnzY++] = elimBuffer[--nnzE];
                    }
                }

            }

            for (i = nnzY - 1; i >= 0; i--) {

                cidx = yIdx[i];

                tmpIdx = LNextSpaceInCol[cidx];
                yVals_cidx = yVals[cidx];
                for (j = Lp[cidx]; j < tmpIdx; j++) {
                    yVals[Li[j]] -= Lx[j] * yVals_cidx;
                }

                Li[tmpIdx] = k;
                Lx[tmpIdx] = yVals_cidx * Dinv[cidx];

                D[k] -= yVals_cidx * Lx[tmpIdx];
                LNextSpaceInCol[cidx]++;

                yVals[cidx] = ZERO;
                yMarkers[cidx] = UNUSED;

            }

            maxAbsD = Math.max(maxAbsD, Math.abs(D[k]));

            double tol = maxAbsD * this.getDimensionalEpsilon();

            if (Math.abs(D[k]) <= tol) {
                return false;
            }
            if (D[k] > tol) {
                positiveValuesInD++;
            }

            Dinv[k] = ONE / D[k];

        }

        myL = new R064CSC(n, m, Lx, Li, Lp);
        myD = D;
        myDinv = Dinv;
        myPositiveValuesInD = positiveValuesInD;

        return true;
    }

    /**
     * Solve A x = b in-place for one column/vector x. Initially x holds b, on exit x holds the solution.
     */
    private void solveOneColumn(final double[] x) {

        int n = myL.getColDim();
        int[] pointers = myL.pointers;
        int[] indices = myL.indices;
        double[] values = myL.values;

        SparseQDLDL.solveL(n, pointers, indices, values, x);

        SparseQDLDL.solveD(n, myDinv, x);

        SparseQDLDL.solveU(n, pointers, indices, values, x);
    }

    @Override
    protected boolean checkSolvability() {
        return myD != null && myPositiveValuesInD == myD.length;
    }

}
