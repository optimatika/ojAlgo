/*
 * Copyright 1997-2026 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;
import java.util.BitSet;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.structure.Access2D;

/**
 * Solves optimisation problems of the form:
 * <p>
 * min 1/2 [X]<sup>T</sup>[Q][X] - [C]<sup>T</sup>[X]<br>
 * when [AE][X] == [BE]<br>
 * and [AI][X] <= [BI]
 * </p>
 * Where [AE] and [BE] are optional.
 *
 * @author apete
 */
final class DirectASS extends ActiveSetSolver {

    /**
     * Cholesky decomposition of the (negated) Schur complement, [A][Q]<sup>-1</sup>[A]<sup>T</sup>, for the
     * active constraints. Updated as constraints are included/excluded. Created for, and grown from, an empty
     * matrix – the factory then gives the implementation that is fastest to grow.
     */
    private final Cholesky<Double> myCholesky;
    /**
     * The active constraint of each row/column in {@link #myCholesky}. Equality constraint i has key i, and
     * inequality constraint j key (number of equality constraints + j).
     */
    private int[] myCholeskyKeys;
    private final BitSet myInCholesky;
    /**
     * [Q]<sup>-1</sup>[a]<sub>i</sub> for each of the equality constraints, calculated when first needed.
     */
    private final double[][] myInvQAE;
    /**
     * [Q]<sup>-1</sup>[a]<sub>i</sub> for each of the included inequality constraints, calculated when first
     * needed and forgotten when excluded.
     */
    private final double[][] myInvQAI;
    private final R064Store myWorkColumn;

    DirectASS(final ConvexData<Double> convexData, final Optimisation.Options optimisationOptions) {

        super(convexData, optimisationOptions);

        int nbEqus = this.countEqualityConstraints();
        int nbInes = this.countInequalityConstraints();

        myCholesky = Cholesky.R064.make(0, 0);
        myCholeskyKeys = new int[16];
        myInCholesky = new BitSet(nbEqus + nbInes);

        myInvQAE = new double[nbEqus][];
        myInvQAI = new double[nbInes][];
        myWorkColumn = MATRIX_FACTORY.make(this.countVariables(), 1L);
    }

    private void clearCholesky() {
        myCholesky.reset();
        myInCholesky.clear();
    }

    private int getCholeskySize() {
        return myCholesky.isComputed() ? myCholesky.getRowDim() : 0;
    }

    private double getB(final int key) {
        int nbEqus = this.countEqualityConstraints();
        return key < nbEqus ? this.getMatrixBE().doubleValue(key) : this.getMatrixBI(key - nbEqus);
    }

    private double[] getInvQA(final int key) {
        int nbEqus = this.countEqualityConstraints();
        return key < nbEqus ? this.getInvQAE(key) : this.getInvQAI(key - nbEqus);
    }

    private SparseArray<Double> getRow(final int key) {
        int nbEqus = this.countEqualityConstraints();
        return key < nbEqus ? this.getMatrixAE(key) : this.getMatrixAI(key - nbEqus);
    }

    /**
     * Adds factor * [Q]<sup>-1</sup>[A]<sup>T</sup>[L] to x, where [A] are the active constraints (in the
     * order of the Cholesky decomposition).
     */
    private void addMultiplierTerms(final R064Store x, final double[] multipliers, final double factor) {
        int nbVars = this.countVariables();
        for (int p = 0, limit = this.getCholeskySize(); p < limit; p++) {
            double scaled = factor * multipliers[p];
            if (scaled != ZERO) {
                double[] column = this.getInvQA(myCholeskyKeys[p]);
                for (int k = 0; k < nbVars; k++) {
                    x.add(k, scaled * column[k]);
                }
            }
        }
    }

    /**
     * Adds a row/column (active constraint) to the Cholesky decomposition of the Schur complement.
     *
     * @return false if not possible - the constraint is (numerically) linearly dependent on those already
     *         there.
     */
    private boolean addToCholesky(final int key) {

        int size = this.getCholeskySize();

        double[] invQA = this.getInvQA(key);

        // The new column of the Schur complement, with the diagonal element last
        double[] column = new double[size + 1];
        for (int p = 0; p < size; p++) {
            column[p] = this.getRow(myCholeskyKeys[p]).dot(invQA);
        }
        column[size] = this.getRow(key).dot(invQA);

        if (!myCholesky.appendColumn(ArrayR064.wrap(column))) {
            return false;
        }

        if (size == myCholeskyKeys.length) {
            myCholeskyKeys = Arrays.copyOf(myCholeskyKeys, 2 * size);
        }
        myCholeskyKeys[size] = key;
        myInCholesky.set(key);

        return true;
    }

    /**
     * Makes sure the Cholesky decomposition of the Schur complement matches the current set of active
     * constraints: removes those no longer active and adds new ones.
     *
     * @return false if it was not possible to add all active constraints
     */
    private boolean updateCholesky(final int[] included) {

        int nbEqus = this.countEqualityConstraints();

        for (int p = this.getCholeskySize() - 1; p >= 0; p--) {
            int key = myCholeskyKeys[p];
            if (key >= nbEqus && Arrays.binarySearch(included, key - nbEqus) < 0) {
                myCholesky.removeColumn(p);
                System.arraycopy(myCholeskyKeys, p + 1, myCholeskyKeys, p, this.getCholeskySize() - p);
                myInCholesky.clear(key);
            }
        }

        for (int i = 0; i < nbEqus; i++) {
            if (!myInCholesky.get(i) && !this.addToCholesky(i)) {
                return false;
            }
        }
        for (int j : included) {
            if (!myInCholesky.get(nbEqus + j) && !this.addToCholesky(nbEqus + j)) {
                return false;
            }
        }

        return true;
    }

    private double[] getInvQAE(final int row) {
        if (myInvQAE[row] == null) {
            myInvQAE[row] = this.solveQ(this.getMatrixAE(row));
        }
        return myInvQAE[row];
    }

    private double[] getInvQAI(final int row) {
        if (myInvQAI[row] == null) {
            myInvQAI[row] = this.solveQ(this.getMatrixAI(row));
        }
        return myInvQAI[row];
    }

    private double[] solveQ(final SparseArray<Double> row) {
        this.getSolutionQ(Access2D.newPrimitiveColumnCollectable(row), myWorkColumn);
        return myWorkColumn.toRawCopy1D();
    }

    @Override
    void resetActivator() {
        super.resetActivator();
        this.clearCholesky();
    }

    @Override
    protected void exclude(final int toExclude) {
        super.exclude(toExclude);
        myInvQAI[toExclude] = null;
    }

    @Override
    protected void performIteration() {

        if (this.isLogDebug()) {
            this.log();
            this.log("PerformIteration {}", 1 + this.countIterations());
            this.log(this.toActivatorString());
        }

        this.getConstraintToInclude();
        this.setConstraintToInclude(-1);
        int[] incl = this.getIncluded();
        int[] excl = this.getExcluded();

        boolean solved = false;

        int numbConstr = this.countIterationConstraints();
        int numbVars = this.countVariables();

        R064Store iterX = this.getIterationX();
        R064Store iterL = MATRIX_FACTORY.make(numbConstr, 1L);
        R064Store soluL = this.getSolutionL();

        if (numbConstr <= numbVars && (solved = this.isSolvableQ())) {
            // Q is SPD

            MatrixStore<Double> invQC = this.getInvQC();

            if (numbConstr == 0L) {
                // Unconstrained - can happen when there are no equality constraints and all inequalities are inactive

                iterX.fillMatching(invQC);

            } else if (solved = this.updateCholesky(incl)) {
                // Actual/normal optimisation problem

                int nbEqus = this.countEqualityConstraints();

                // [A][Q]^-1[A]^T [L] = [A][Q]^-1[C] - [B]
                double[] multipliers = new double[numbConstr];
                for (int p = 0; p < numbConstr; p++) {
                    int key = myCholeskyKeys[p];
                    multipliers[p] = this.getRow(key).dot(invQC) - this.getB(key);
                }
                myCholesky.ftran(multipliers);

                if (this.isLogDebug()) {
                    this.log("Solution for L={} with keys {}", Arrays.toString(multipliers), Arrays.toString(Arrays.copyOf(myCholeskyKeys, numbConstr)));
                }

                // [X] = [Q]^-1[C] - [Q]^-1[A]^T[L]
                iterX.fillAll(ZERO);
                this.addMultiplierTerms(iterX, multipliers, ONE);
                iterX.modifyMatching(invQC, SUBTRACT);

                // One step of iterative refinement. The residual of the equation system for the multipliers is
                // the error in the active constraints: [A][X] - [B]
                double[] correction = new double[numbConstr];
                for (int p = 0; p < numbConstr; p++) {
                    int key = myCholeskyKeys[p];
                    correction[p] = this.getRow(key).dot(iterX) - this.getB(key);
                }
                myCholesky.ftran(correction);
                this.addMultiplierTerms(iterX, correction, NEG);

                for (int p = 0; p < numbConstr; p++) {
                    int key = myCholeskyKeys[p];
                    iterL.set(key < nbEqus ? key : nbEqus + Arrays.binarySearch(incl, key - nbEqus), multipliers[p] + correction[p]);
                }

                // With an ill-conditioned Q the Schur complement based solution may not be accurate enough
                if (!(solved = this.isActiveConstraintsSatisfied(iterX))) {
                    // Maybe the updated decomposition has lost accuracy - recalculate it next time
                    this.clearCholesky();
                }
            }
        }

        if (!solved) {
            // The above failed, try solving the full KKT system instaed

            R064Store tmpXL = MATRIX_FACTORY.make(numbVars + numbConstr, 1L);

            if (solved = this.solveFullKKT(tmpXL)) {

                iterX.fillMatching(tmpXL.limits(numbVars, 1));
                iterL.fillMatching(tmpXL.offsets(numbVars, 0));
            }
        }

        soluL.fillAll(ZERO);
        if (solved) {
            for (int i = 0; i < this.countEqualityConstraints(); i++) {
                soluL.set(i, iterL.doubleValue(i));
            }
            for (int i = 0; i < incl.length; i++) {
                soluL.set(this.countEqualityConstraints() + incl[i], iterL.doubleValue(this.countEqualityConstraints() + i));
            }
        }

        this.handleIterationResults(solved, iterX, incl, excl);
    }

}
