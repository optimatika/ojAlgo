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
package org.ojalgo.optimisation.convex;

import java.util.BitSet;
import java.util.function.IntConsumer;

import org.ojalgo.array.operation.COPY;
import org.ojalgo.array.operation.IndexOf;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.decomposition.MinimumDegree;
import org.ojalgo.matrix.decomposition.SparseQDLDL;
import org.ojalgo.matrix.decomposition.SparseQDLDL.EliminationTree;
import org.ojalgo.matrix.store.ColumnsSupplier;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.transformation.InvertibleFactor;
import org.ojalgo.type.ReciprocalPair;

/**
 * Factorisation and solves for the KKT system of an equality-constrained quadratic programme.
 * <p>
 * The KKT matrix has block structure
 *
 * <pre>
 *     [ P + σ I      A']
 *     [   A     -diag(w⁻¹) ]
 * </pre>
 *
 * where {@code P} is the quadratic term, {@code A} the constraint matrix, {@code σ > 0} a primal
 * regularisation parameter and {@code w} the per-constraint dual weights. The matrix is stored as a sparse,
 * symmetric, quasi-definite system and factorised using a QDLDL decomposition after an approximate minimum
 * degree ordering.
 * <p>
 * A {@link FactorKKT} instance owns the assembled and permuted KKT body, the symbolic and numeric QDLDL
 * factors, and mapping structures that link entries of {@code P}, {@code A} and the dual diagonal to their
 * locations in the KKT values array. This allows efficient in-place updates of those blocks followed by
 * numerical refactorisation while keeping the sparsity pattern fixed.
 */
final class FactorKKT implements InvertibleFactor<Double> {

    static final class Map2KKT {

        /**
         * Create mapping structures for a specific pair of quadratic and constraint matrices.
         * <p>
         * The mappings are sized to match the current sparsity patterns of {@code mtrxP} and {@code mtrxA}
         * and are later populated when the KKT matrix is first assembled.
         */
        static Map2KKT from(final R064CSC mtrxP, final R064CSC mtrxA) {
            return new Map2KKT(new int[mtrxP.capacity()], new BitSet(mtrxP.capacity() + 1), new int[mtrxA.capacity()], new int[mtrxA.getRowDim()]);
        }

        private transient int[] myMapSigma = null;
        private final BitSet myOnDiagP;
        /** Map nonzeros of {@code A} to indices in the KKT {@code values} array. */
        final int[] mapA;
        /** Map entries on the dual diagonal to indices in the KKT {@code values} array. */
        final int[] mapDualDiag;
        /** Map nonzeros of {@code P} to indices in the KKT {@code values} array. */
        final int[] mapP;

        private Map2KKT(final int[] mapP, final BitSet mapSigma, final int[] mapA, final int[] mapRho) {
            super();
            this.mapP = mapP;
            this.mapA = mapA;
            myOnDiagP = mapSigma;
            mapDualDiag = mapRho;
        }

        /**
         * Mark that the {@code P}-entry at {@code index} lies on the diagonal of the KKT matrix and receives
         * the {@code σ} regularisation contribution.
         */
        void diagonalP(final int index) {
            myOnDiagP.set(index, true);
        }

        /**
         * Indices, into {@link #mapP}, for the diagonal {@code P}-entries that must receive a {@code σ}
         * contribution when updating the quadratic block.
         */
        int[] mapSigma() {
            if (myMapSigma == null) {
                myMapSigma = new int[myOnDiagP.cardinality()];
                int i = 0;
                for (int v = myOnDiagP.nextSetBit(0); v >= 0; v = myOnDiagP.nextSetBit(v + 1)) {
                    myMapSigma[i++] = v;
                }
            }
            return myMapSigma;
        }

    }

    /**
     * Assemble the KKT system as an {@link R064CSC} matrix and populate the supplied index mappings.
     * <p>
     * The KKT matrix uses the block structure described in the class-level Javadoc and is built via a
     * {@link ColumnsSupplier}-backed builder. Only the required upper-triangular entries of the off-diagonal
     * blocks are written; the QDLDL factorisation treats the matrix as symmetric.
     */
    private static R064CSC formKKT(final R064CSC mtrxP, final R064CSC mtrxA, final double sigma, final double[] invDiagW, final int[] mapP,
            final IntConsumer diagonalP, final int[] mapA, final int[] mapDualDiag) {

        int n = mtrxP.getColDim();
        int m = mtrxA.getRowDim();

        R064CSC.Builder builder = R064CSC.newBuilder(n + m);

        int nnzP = mtrxP.capacity();
        int nnzA = mtrxA.capacity();

        int[] posProw = new int[nnzP];
        int[] posPcol = new int[nnzP];

        int[] posArow = new int[nnzA];
        int[] posAcol = new int[nnzA];

        int[] posDrow = new int[m];
        int[] posDcol = new int[m];

        int row, col;

        // P + sigma * I
        for (int j = 0; j < n; j++) {

            boolean diagonal = false;

            for (int p = mtrxP.pointers[j], lim = mtrxP.pointers[j + 1]; p < lim; p++) {
                int i = mtrxP.indices[p];
                double val = mtrxP.values[p];

                if (val == PrimitiveMath.ZERO) {
                    throw new IllegalArgumentException();
                }

                row = i;
                col = j;

                builder.set(row, col, val);
                posProw[p] = row;
                posPcol[p] = col;

                if (row == col) {

                    diagonal = true;
                    builder.add(row, col, sigma);
                    diagonalP.accept(p);
                }
            }

            if (!diagonal) {
                builder.set(j, j, sigma);
            }
        }

        // A'
        for (int j = 0; j < n; j++) {
            for (int p = mtrxA.pointers[j], lim = mtrxA.pointers[j + 1]; p < lim; p++) {
                int i = mtrxA.indices[p];
                double val = mtrxA.values[p];

                if (val == PrimitiveMath.ZERO) {
                    throw new IllegalArgumentException();
                }

                row = j;
                col = n + i;

                builder.set(row, col, val);
                posArow[p] = row;
                posAcol[p] = col;
            }
        }

        // dual diagonal
        for (int ij = 0; ij < m; ij++) {
            double val = -invDiagW[ij];

            if (val == PrimitiveMath.ZERO) {
                throw new IllegalArgumentException();
            }

            row = n + ij;
            col = row;

            builder.set(row, col, val);
            posDrow[ij] = row;
            posDcol[ij] = col;
        }

        R064CSC retVal = builder.build();

        for (int p = 0; p < nnzP; p++) {
            row = posProw[p];
            col = posPcol[p];
            mapP[p] = IndexOf.indexOf(retVal.indices, retVal.pointers[col], retVal.pointers[col + 1], row);
        }

        for (int a = 0; a < nnzA; a++) {
            row = posArow[a];
            col = posAcol[a];
            mapA[a] = IndexOf.indexOf(retVal.indices, retVal.pointers[col], retVal.pointers[col + 1], row);
        }

        for (int r = 0; r < m; r++) {
            row = posDrow[r];
            col = posDcol[r];
            mapDualDiag[r] = IndexOf.indexOf(retVal.indices, retVal.pointers[col], retVal.pointers[col + 1], row);
        }

        return retVal;
    }

    /**
     * Permute the KKT matrix using an approximate minimum degree ordering and update the index mappings
     * accordingly so that subsequent in-place updates remain valid.
     */
    private static R064CSC permuteKKT(final R064CSC original, final MinimumDegree md, final int[] mapP, final int[] mapA, final int[] mapDualDiag) {

        md.approximate(original);

        int[] moves = new int[original.capacity()];
        R064CSC permuted = md.permute(original, moves);

        for (int i = 0, lim = mapP.length; i < lim; i++) {
            mapP[i] = moves[mapP[i]];
        }

        for (int i = 0, lim = mapA.length; i < lim; i++) {
            mapA[i] = moves[mapA[i]];
        }

        for (int i = 0, lim = mapDualDiag.length; i < lim; i++) {
            mapDualDiag[i] = moves[mapDualDiag[i]];
        }

        return permuted;
    }

    /**
     * Update the constraint block {@code A} of the KKT matrix in-place using the precomputed mapping.
     *
     * @param valKKT KKT {@code values} array to modify
     * @param mapA   mapping from {@code A}'s nonzeros to indices in {@code valKKT}
     * @param valA   new nonzero values for {@code A} in the same order as the original sparsity pattern
     */
    private static void updateA(final double[] valKKT, final int[] mapA, final double[] valA) {

        for (int i = 0, lim = valA.length; i < lim; i++) {
            valKKT[mapA[i]] = valA[i];
        }
    }

    /**
     * Update the dual-diagonal block of the KKT matrix in-place using the precomputed mapping.
     *
     * @param valKKT   KKT {@code values} array to modify
     * @param mapDual  mapping from dual-diagonal entries to indices in {@code valKKT}
     * @param invDiagW current dual weights stored as {@code 1/w}
     */
    private static void updateDualDiagonal(final double[] valKKT, final int[] mapDual, final double[] invDiagW) {

        for (int i = 0, lim = invDiagW.length; i < lim; i++) {
            valKKT[mapDual[i]] = -invDiagW[i];
        }
    }

    /**
     * Update the quadratic block {@code P} of the KKT matrix in-place and re-apply the {@code σ}
     * regularisation on the diagonal.
     *
     * @param valKKT   KKT {@code values} array to modify
     * @param mapP     mapping from {@code P}'s nonzeros to indices in {@code valKKT}
     * @param valP     new nonzero values for {@code P} in the same order as the original sparsity pattern
     * @param mapSigma indices, into {@code mapP}, for diagonal {@code P}-entries that must receive {@code σ}
     * @param sigma    primal regularisation parameter
     */
    private static void updateP(final double[] valKKT, final int[] mapP, final double[] valP, final int[] mapSigma, final double sigma) {

        for (int i = 0, lim = valP.length; i < lim; i++) {
            valKKT[mapP[i]] = valP[i];
        }

        for (int i = 0, lim = mapSigma.length; i < lim; i++) {
            valKKT[mapP[mapSigma[i]]] += sigma;
        }
    }

    /**
     * Construct a {@link FactorKKT} instance for a specific quadratic programme: assemble and permute the KKT
     * matrix, compute its QDLDL factorisation and prepare mappings for later updates.
     *
     * @param mtrxP quadratic objective matrix (upper-triangular part stored)
     * @param mtrxA constraint matrix
     * @param sigma primal regularisation parameter
     * @param diagW diagonal weights associated with the dual variables (one per constraint)
     */
    static FactorKKT of(final R064CSC mtrxP, final R064CSC mtrxA, final double sigma, final ReciprocalPair diagW) {

        Map2KKT mapper = Map2KKT.from(mtrxP, mtrxA);

        R064CSC tmpKKT = FactorKKT.formKKT(mtrxP, mtrxA, sigma, diagW.inverse, mapper.mapP, mapper::diagonalP, mapper.mapA, mapper.mapDualDiag);

        MinimumDegree md = new MinimumDegree();

        tmpKKT = FactorKKT.permuteKKT(tmpKKT, md, mapper.mapP, mapper.mapA, mapper.mapDualDiag);

        SparseQDLDL qdldl = new SparseQDLDL();
        EliminationTree eTree = qdldl.computeEliminationTree(tmpKKT);
        qdldl.factor(tmpKKT, eTree);

        return new FactorKKT(mapper, md, qdldl, eTree, tmpKKT, sigma, diagW.inverse);
    }

    private final EliminationTree myEliminationTree;
    private double[] myInvDiagW;
    private final R064CSC myKKT;
    private final Map2KKT myMapper;
    private final MinimumDegree myMD;
    private final SparseQDLDL myQDLDL;
    private final double mySigma;
    private final double[] myWork1;
    private final double[] myWork2;

    private FactorKKT(final Map2KKT mapper, final MinimumDegree md, final SparseQDLDL qdldl, final EliminationTree eTree, final R064CSC body,
            final double sigma, final double[] invDiagW) {

        super();

        int dim = body.getColDim();

        myMapper = mapper;
        myMD = md;
        myQDLDL = qdldl;
        myEliminationTree = eTree;
        myKKT = body;
        mySigma = sigma;
        myInvDiagW = invDiagW;
        myWork1 = new double[dim];
        myWork2 = new double[dim];
    }

    /**
     * For this symmetric KKT system, {@code btran} is equivalent to {@link #ftran(double[])}.
     */
    @Override
    public void btran(final double[] arg) {
        this.ftran(arg);
    }

    /**
     * Backward solve for a right-hand side stored in a {@link PhysicalStore}. Delegates to
     * {@link #btran(double[])} when possible.
     */
    @Override
    public void btran(final PhysicalStore<Double> arg) {
        this.ftran(arg);
    }

    /**
     * Solve KKT · x = b for a single right-hand side.
     * <p>
     * The input vector is interpreted as the concatenation of primal and dual components and is overwritten
     * with the solution. Internally the right-hand side is permuted to the AMD ordering, solved via QDLDL
     * forward/backward substitutions, and then mapped back. The dual components are finally shifted to undo
     * the effect of the diagonal scaling defined by the current dual weights.
     */
    @Override
    public void ftran(final double[] arg) {

        myMD.permute(myWork1, arg);
        myQDLDL.ftran(myWork1);
        myMD.reverse(myWork2, myWork1);

        int m = myInvDiagW.length;
        int n = myWork1.length - m;

        for (int j = 0; j < n; j++) {
            arg[j] = myWork2[j];
        }

        for (int i = 0; i < m; i++) {
            arg[n + i] += myInvDiagW[i] * myWork2[n + i];
        }
    }

    /**
     * Forward solve for a right-hand side stored in a {@link PhysicalStore}. Delegates to
     * {@link #ftran(double[])} when efficient.
     */
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
        return myKKT.getColDim();
    }

    @Override
    public int getRowDim() {
        return myKKT.getRowDim();
    }

    /**
     * Update the dual weights (and thus the dual diagonal block) and refactorise the KKT matrix numerically.
     *
     * @param diagW new diagonal dual weights (one per constraint)
     * @return {@code true} if the numerical factorisation succeeds
     */
    boolean updateDualWeights(final ReciprocalPair diagW) {

        myInvDiagW = diagW.inverse;

        FactorKKT.updateDualDiagonal(myKKT.values, myMapper.mapDualDiag, myInvDiagW);

        return myQDLDL.factor(myKKT, myEliminationTree);
    }

    /**
     * Update the quadratic and constraint blocks of the KKT matrix and refactorise numerically while keeping
     * the sparsity pattern and permutation fixed.
     *
     * @param mtrxP new quadratic objective matrix (upper-triangular part stored)
     * @param mtrxA new constraint matrix
     * @return {@code true} if the numerical factorisation succeeds
     */
    boolean updateMatrices(final R064CSC mtrxP, final R064CSC mtrxA) {

        FactorKKT.updateP(myKKT.values, myMapper.mapP, mtrxP.values, myMapper.mapSigma(), mySigma);

        FactorKKT.updateA(myKKT.values, myMapper.mapA, mtrxA.values);

        return myQDLDL.factor(myKKT, myEliminationTree);
    }

}
