/*
 * Copyright 1997-2025 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.matrix.decomposition;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;
import java.util.List;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.operation.COPY;
import org.ojalgo.array.operation.NRMINF;
import org.ojalgo.matrix.store.DiagonalStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.matrix.store.R064CSC.Builder;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.matrix.transformation.InvertibleFactor;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Access2D.ColumnView;
import org.ojalgo.structure.ElementView2D;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.type.ReciprocalPair;

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

    private static final class Work {

        /**
         * Holds the set of column indices that currently have nonzero entries in the working vector y for the
         * active column k. The entries are stored compactly from 0 to {@code nnzY - 1} and iterated in
         * reverse when finalising column k of L and D.
         */
        int[] activePattern = null;
        /**
         * Temporary stack used during symbolic traversal of the elimination tree when expanding each nonzero
         * in column k into its ancestors. Holds column indices that still need to be processed.
         */
        int[] eliminationStack = null;
        /**
         * For each column c in L, tracks the next free position inside the compressed-column storage of
         * {@code myL}. During numeric factorisation this is advanced as new entries L(k, c) are written.
         */
        int[] nextFreeInColumn = null;
        /**
         * Marker flags indicating whether a given column index is already present in {@link #activePattern}.
         * This avoids inserting duplicates while walking the elimination tree and building the sparsity
         * pattern of the working vector y.
         */
        boolean[] patternMarked = null;
        /**
         * Working vector y used to accumulate the contribution of A(:, k) and previously computed columns of
         * L into the Schur complement that defines D(k) and the off-diagonal entries L(k, :). Entries are
         * reset to zero once their contribution has been propagated.
         */
        double[] workY = null;

        void reset(final int n) {

            if (activePattern == null || activePattern.length != n) {
                activePattern = new int[n];
                eliminationStack = new int[n];
                nextFreeInColumn = new int[n];
                workY = new double[n];
                patternMarked = new boolean[n];
            }
        }

    }

    /**
     * [A]=[L][D][L]<sup>T</sup> — diagonal factor.
     */
    static final class FactorD extends AbstractDecomposition.PrimitiveFactor {

        private final ReciprocalPair myDiagonal;

        FactorD(final ReciprocalPair diagonal) {
            super();
            myDiagonal = diagonal;
        }

        @Override
        public void btran(final double[] arg) {
            this.ftran(arg);
        }

        @Override
        public void ftran(final double[] arg) {
            SparseQDLDL.ftranD(myDiagonal.inverse, arg);
        }

        @Override
        public MatrixStore<Double> get() {
            return DiagonalStore.wrap(myDiagonal.values);
        }

        @Override
        public int getColDim() {
            return myDiagonal.size();
        }

        @Override
        public int getRowDim() {
            return myDiagonal.size();
        }

    }

    /**
     * [A]=[L][D][L]<sup>T</sup> — unit lower triangular factor (CSC).
     */
    static final class FactorL extends AbstractDecomposition.PrimitiveFactor {

        private final R064CSC myBody;

        FactorL(final R064CSC body) {
            super();
            myBody = body;
        }

        @Override
        public void btran(final double[] arg) {
            SparseQDLDL.ftranU(myBody.pointers, myBody.indices, myBody.values, arg);
        }

        @Override
        public void ftran(final double[] arg) {
            SparseQDLDL.ftranL(myBody.pointers, myBody.indices, myBody.values, arg);
        }

        @Override
        public MatrixStore<Double> get() {
            return myBody.triangular(false, true);
        }

        @Override
        public int getColDim() {
            return myBody.getColDim();
        }

        @Override
        public int getRowDim() {
            return myBody.getRowDim();
        }

    }

    /**
     * [A]=[L][D][L]<sup>T</sup> — transpose of L (unit upper triangular).
     */
    static final class FactorLT extends AbstractDecomposition.PrimitiveFactor {

        private final R064CSC myBody;

        FactorLT(final R064CSC body) {
            super();
            myBody = body;
        }

        @Override
        public void btran(final double[] arg) {
            SparseQDLDL.ftranL(myBody.pointers, myBody.indices, myBody.values, arg);
        }

        @Override
        public void ftran(final double[] arg) {
            SparseQDLDL.ftranU(myBody.pointers, myBody.indices, myBody.values, arg);
        }

        @Override
        public MatrixStore<Double> get() {
            return myBody.triangular(false, true).transpose();
        }

        @Override
        public int getColDim() {
            return myBody.getColDim();
        }

        @Override
        public int getRowDim() {
            return myBody.getRowDim();
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

    private ReciprocalPair myD;
    private R064CSC myL;
    private int myPositiveValuesInD;
    private final Work myWorkerCache = new Work();

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

        if (myD == null || myD.size() != n) {
            myD = new ReciprocalPair(n);
        }

        myWorkerCache.reset(n);

        return eTree;
    }

    @Override
    public int countSignificant(final double threshold) {
        int significant = 0;
        if (myD != null) {
            double[] values = myD.values;
            for (int i = 0, length = values.length; i < length; i++) {
                if (Math.abs(values[i]) > threshold) {
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
        return this.decompose(matrix, eTree, myL, myD, myWorkerCache);
    }

    /**
     * Convenience for callers that have already computed the symbolic structure for a given sparsity pattern.
     * The caller is responsible for ensuring that the supplied {@link EliminationTree} matches the pattern of
     * {@code matrix}; behaviour is undefined if they do not.
     */
    public boolean factor(final R064CSC matrix, final EliminationTree eTree) {
        this.reset();
        return this.decompose(matrix, eTree, myL, myD, myWorkerCache);
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

        SparseQDLDL.ftranD(myD.inverse, x);

        SparseQDLDL.ftranU(pointers, indices, values, x);
    }

    @Override
    public void ftran(final PhysicalStore<Double> arg) {
        InvertibleFactor.doPrimitive(this, arg);
    }

    @Override
    public int getColDim() {
        return myL != null ? myL.getColDim() : 0;
    }

    @Override
    public MatrixStore<Double> getD() {
        return myD != null ? DiagonalStore.wrap(myD.values) : null;
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
        double[] values = myD.values;
        for (int i = 0, length = values.length; i < length; i++) {
            det *= values[i];
        }

        return Double.valueOf(det);
    }

    @Override
    public List<InvertibleFactor<Double>> getFactors() {
        return List.of(new FactorL(myL), new FactorD(myD), new FactorLT(myL));
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
        return Structure1D.newIncreasingRange(0, myD != null ? myD.size() : 0);
    }

    @Override
    public double getRankThreshold() {

        if (myD == null || myD.size() == 0) {
            return ZERO;
        }

        return NRMINF.invoke(myD.values) * this.getDimensionalEpsilon();
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

    private boolean decompose(final R064CSC matrix, final EliminationTree eTree, final R064CSC decomp, final ReciprocalPair diag, final Work work) {

        int n = matrix.getColDim();

        int[] mtrxAp = matrix.pointers;
        int[] mtrxAi = matrix.indices;
        double[] mtrxAx = matrix.values;

        int[] mtrxLp = decomp.pointers;
        int[] mtrxLi = decomp.indices;
        double[] mtrxLx = decomp.values;

        int[] treeColumnNonZeros = eTree.colNz;
        int[] eliminationTree = eTree.tree;

        double[] diagVal = diag.values;
        double[] diagInv = diag.inverse;

        int[] activePattern = work.activePattern;
        int[] eliminationStack = work.eliminationStack;
        int[] nextFreeInColumn = work.nextFreeInColumn;
        double[] workY = work.workY;
        boolean[] patternMarked = work.patternMarked;

        int yNonZeros, baseIndex, patternIndex, nextColumn, eliminationCount, insertionIndex;
        double yAtPatternIndex;

        int positiveValuesInD = 0;

        mtrxLp[0] = 0;
        for (int i = 0; i < n; i++) {
            mtrxLp[i + 1] = mtrxLp[i] + treeColumnNonZeros[i];
            patternMarked[i] = false;
            workY[i] = ZERO;
            diagVal[i] = ZERO;
            nextFreeInColumn[i] = mtrxLp[i];
        }

        diagVal[0] = mtrxAx[0];
        double maxAbsD = Math.abs(diagVal[0]);
        if (diagVal[0] == ZERO) {
            return false;
        }
        if (diagVal[0] > ZERO) {
            positiveValuesInD++;
        }
        diagInv[0] = ONE / diagVal[0];

        for (int k = 1; k < n; k++) {

            yNonZeros = 0;
            insertionIndex = mtrxAp[k + 1];

            for (int i = mtrxAp[k]; i < insertionIndex; i++) {

                baseIndex = mtrxAi[i];

                if (baseIndex == k) {
                    diagVal[k] = mtrxAx[i];
                    continue;
                }

                workY[baseIndex] = mtrxAx[i];

                nextColumn = baseIndex;

                if (!patternMarked[nextColumn]) {

                    patternMarked[nextColumn] = true;
                    eliminationStack[0] = nextColumn;
                    eliminationCount = 1;

                    nextColumn = eliminationTree[baseIndex];

                    while (nextColumn >= 0 && nextColumn < k) {
                        if (patternMarked[nextColumn]) {
                            break;
                        }

                        patternMarked[nextColumn] = true;
                        eliminationStack[eliminationCount] = nextColumn;
                        eliminationCount++;
                        nextColumn = eliminationTree[nextColumn];

                    }

                    while (eliminationCount > 0) {
                        activePattern[yNonZeros++] = eliminationStack[--eliminationCount];
                    }
                }
            }

            for (int i = yNonZeros - 1; i >= 0; i--) {

                patternIndex = activePattern[i];

                insertionIndex = nextFreeInColumn[patternIndex];
                yAtPatternIndex = workY[patternIndex];
                for (int j = mtrxLp[patternIndex]; j < insertionIndex; j++) {
                    workY[mtrxLi[j]] -= mtrxLx[j] * yAtPatternIndex;
                }

                mtrxLi[insertionIndex] = k;
                mtrxLx[insertionIndex] = yAtPatternIndex * diagInv[patternIndex];

                diagVal[k] -= yAtPatternIndex * mtrxLx[insertionIndex];
                nextFreeInColumn[patternIndex]++;

                workY[patternIndex] = ZERO;
                patternMarked[patternIndex] = false;
            }

            maxAbsD = Math.max(maxAbsD, Math.abs(diagVal[k]));

            double tol = maxAbsD * this.getDimensionalEpsilon();

            if (Math.abs(diagVal[k]) <= tol) {
                return false;
            }
            if (diagVal[k] > tol) {
                positiveValuesInD++;
            }

            diagInv[k] = ONE / diagVal[k];
        }

        myPositiveValuesInD = positiveValuesInD;

        return this.computed(true);
    }

    @Override
    protected boolean checkSolvability() {
        return myD != null && myPositiveValuesInD == myD.size();
    }

}
