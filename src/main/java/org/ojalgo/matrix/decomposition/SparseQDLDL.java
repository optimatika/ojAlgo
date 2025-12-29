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
     * <p>
     * It's public so that you can cache it, but no need to access the internals.
     */
    public static final class EliminationTree {

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

    private static final class WorkerCache {

        int[] elimBuffer = null;
        int[] LNextSpaceInCol = null;
        int[] yIdx = null;
        boolean[] yMarkers = null;
        double[] yVals = null;

        void reset(final int n) {

            if (yIdx == null || yIdx.length != n) {
                yIdx = new int[n];
                elimBuffer = new int[n];
                LNextSpaceInCol = new int[n];
                yVals = new double[n];
                yMarkers = new boolean[n];
            }
        }

    }

    private static EliminationTree computeEliminationTree(final int n, final int[] pointers, final int[] indices) {

        int[] work = new int[n];
        int[] tree = new int[n];
        int[] colNz = new int[n];
        int totNz = 0;

        Arrays.fill(tree, -1);

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
                    if (tree[i] < 0) {
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

    private static void ftranD(final double[] inv, final double[] x) {
        for (int i = 0, n = x.length; i < n; i++) {
            x[i] *= inv[i]; // inv(D)
        }
    }

    private static void ftranL(final int[] pointers, final int[] indices, final double[] values, final double[] x) {
        for (int j = 0, n = x.length; j < n; j++) {
            double xj = x[j];
            for (int ji = pointers[j], jm = pointers[j + 1]; ji < jm; ji++) {
                x[indices[ji]] -= values[ji] * xj;
            }
        }
    }

    private static void ftranU(final int[] pointers, final int[] indices, final double[] values, final double[] x) {
        for (int j = x.length - 1; j >= 0; j--) {
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
    private final WorkerCache myWorkerCache = new WorkerCache();

    public SparseQDLDL() {
        super(R064Store.FACTORY);
    }

    @Override
    public void btran(final double[] arg) {
        this.ftran(arg);
    }

    @Override
    public void btran(final PhysicalStore<Double> arg) {
        this.ftran(arg);
    }

    @Override
    public Double calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    public EliminationTree computeEliminationTree(final R064CSC matrix) {

        int n = matrix.getColDim();

        EliminationTree eTree = SparseQDLDL.computeEliminationTree(n, matrix.pointers, matrix.indices);

        if (myL == null || myL.getColDim() != n || myL.countNonzeros() != eTree.totNz) {
            myL = new R064CSC(n, n, eTree.totNz);
        }

        if (myD == null || myD.length != n) {
            myD = new double[n];
            myDinv = new double[n];
        }

        myWorkerCache.reset(n);

        return eTree;
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
     * <p>
     * This method performs both the symbolic analysis (elimination tree) and numeric factorisation. For
     * repeated factorisations with identical sparsity patterns and updated values, callers may instead use
     * {@link #factor(R064CSC, EliminationTree)} together with a cached symbolic tree obtained from
     * {@link #getSymbolic()}.
     */
    public boolean factor(final R064CSC matrix) {
        this.reset();
        EliminationTree eTree = this.computeEliminationTree(matrix);
        return this.decompose(matrix, eTree, myL, myD, myDinv, myWorkerCache);
    }

    /**
     * Convenience for callers that have already computed the symbolic structure for a given sparsity pattern.
     * The caller is responsible for ensuring that the supplied {@link EliminationTree} matches the pattern of
     * {@code matrix}; behaviour is undefined if they do not.
     */
    public boolean factor(final R064CSC matrix, final EliminationTree eTree) {
        this.reset();
        return this.decompose(matrix, eTree, myL, myD, myDinv, myWorkerCache);
    }

    /**
     * Solve A x = b in-place for one column/vector x. Initially x holds b, on exit x holds the solution.
     */
    @Override
    public void ftran(final double[] x) {

        int[] pointers = myL.pointers;
        int[] indices = myL.indices;
        double[] values = myL.values;

        SparseQDLDL.ftranL(pointers, indices, values, x);

        SparseQDLDL.ftranD(myDinv, x);

        SparseQDLDL.ftranU(pointers, indices, values, x);
    }

    @Override
    public void ftran(final PhysicalStore<Double> arg) {
        if (arg instanceof R064Store) {
            this.ftran(((R064Store) arg).data);
        } else {
            double[] x = arg.toRawCopy1D();
            this.ftran(x);
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
        return myL.triangular(false, true);
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
            this.ftran(x);
            COPY.invoke(x, preallocated);

        } else {

            for (ColumnView<Double> column : preallocated.columns()) {
                COPY.invoke(column, x);
                this.ftran(x);
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

        this.ftran(x);

        return x;
    }

    /**
     * In this method, variable names are deliberately kept close to what they are in the original c-code.
     */
    private boolean decompose(final R064CSC matrix, final EliminationTree eTree, final R064CSC decomp, final double[] D, final double[] Dinv,
            final WorkerCache workers) {

        int n = matrix.getColDim();

        int[] Lnz = eTree.colNz;
        int[] etree = eTree.tree;

        int[] Ap = matrix.pointers;
        int[] Ai = matrix.indices;
        double[] Ax = matrix.values;

        int[] Lp = decomp.pointers;
        int[] Li = decomp.indices;
        double[] Lx = decomp.values;

        int i, j, k, nnzY, bidx, cidx, nextIdx, nnzE, tmpIdx;

        int[] yIdx = workers.yIdx;
        int[] elimBuffer = workers.elimBuffer;
        int[] LNextSpaceInCol = workers.LNextSpaceInCol;
        double[] yVals = workers.yVals;
        boolean[] yMarkers = workers.yMarkers;

        double yVals_cidx;

        int positiveValuesInD = 0;

        Lp[0] = 0;
        for (i = 0; i < n; i++) {
            Lp[i + 1] = Lp[i] + Lnz[i];
            yMarkers[i] = false;
            yVals[i] = ZERO;
            D[i] = ZERO;
            LNextSpaceInCol[i] = Lp[i];
        }

        D[0] = Ax[0];
        double maxAbsD = Math.abs(D[0]);
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

                if (!yMarkers[nextIdx]) {

                    yMarkers[nextIdx] = true;
                    elimBuffer[0] = nextIdx;
                    nnzE = 1;

                    nextIdx = etree[bidx];

                    while (nextIdx >= 0 && nextIdx < k) {
                        if (yMarkers[nextIdx]) {
                            break;
                        }

                        yMarkers[nextIdx] = true;
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
                yMarkers[cidx] = false;
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

        myPositiveValuesInD = positiveValuesInD;

        return this.computed(true);
    }

    @Override
    protected boolean checkSolvability() {
        return myD != null && myPositiveValuesInD == myD.length;
    }

}
