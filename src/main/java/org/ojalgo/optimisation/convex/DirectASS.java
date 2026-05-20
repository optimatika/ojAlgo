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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;

import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.scalar.PrimitiveScalar;

final class DirectASS extends ActiveSetSolver {

    DirectASS(final ConvexData<Double> convexData, final Optimisation.Options optimisationOptions) {
        super(convexData, optimisationOptions);
        disableScaling = optimisationOptions.convex().iterative().getPrecision() >= 16;
    }

    private final boolean disableScaling;

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

            } else {
                // Actual/normal optimisation problem

                MatrixStore<Double> iterA = this.getIterationA();
                MatrixStore<Double> iterB = this.getIterationB();
                MatrixStore<Double> iterC = this.getIterationC();

                MatrixStore<Double> invQAt = this.getSolutionQ(iterA.transpose());
                if (this.isLogDebug()) {
                    this.log("invQAt", invQAt);
                }

                // Negated Schur complement base (unscaled) S = invQAt.premultiply(iterA)
                ElementsSupplier<Double> tmpS = invQAt.premultiply(iterA);
                R064Store baseS = tmpS.collect(R064Store.FACTORY); // Materialise full Schur complement

                // Compute & apply scaling
                double[] diag = SchurScaling.computeDiagonal(iterA, invQAt); // S positive diag
                double max = 0.0, min = Double.POSITIVE_INFINITY;
                for (double v : diag) { if (v > max) max = v; if (v > 0 && v < min) min = v; }
                boolean wellConditioned = min < Double.POSITIVE_INFINITY && max / min < 1.0e3;
                boolean extremeSpread = min < Double.POSITIVE_INFINITY && max / min > 1.0e6;
                boolean applyScaling = !disableScaling && !wellConditioned && !extremeSpread && (max / min >= 1.0e4);
                double[] scale;
                if (applyScaling) {
                    SchurScaling.regulariseDiagonal(diag);
                    scale = SchurScaling.buildScaling(diag);
                    for (int i = 0; i < numbConstr; i++) {
                        double di = scale[i];
                        for (int j = 0; j < numbConstr; j++) {
                            double dj = scale[j];
                            baseS.set(i, j, baseS.doubleValue(i, j) * di * dj);
                        }
                    }
                } else {
                    scale = new double[diag.length];
                    Arrays.fill(scale, ONE);
                }

                if (this.isLogDebug()) {
                    this.log("Scaled Schur complement", baseS);
                }

                if (solved = this.computeGeneral(baseS)) {

                    ElementsSupplier<Double> rhsL = invQC.premultiply(iterA).onMatching(SUBTRACT, iterB);
                    double[] rhsArray = rhsL.collect(R064Store.FACTORY).toRawCopy1D();
                    SchurScaling.scaleRHS(scale, rhsArray);
                    R064Store rhsStore = R064Store.FACTORY.make(numbConstr, 1);
                    for (int i = 0; i < numbConstr; i++) rhsStore.set(i, rhsArray[i]);

                    this.getSolutionGeneral(rhsStore, iterL);

                    if (this.isLogDebug()) {
                        this.log("RHS (scaled)={}", rhsArray);
                        this.log("Relative error {} in solution for L (scaled)={}", NaN, Arrays.toString(iterL.toRawCopy1D()));
                    }

                    // Unscale multipliers
                    for (int i = 0; i < numbConstr; i++) {
                        iterL.set(i, iterL.doubleValue(i) * scale[i]);
                    }

                    // Recover primal using same formulation as IterativeASS: x solves Qx = C - A^T L
                    ElementsSupplier<Double> rhsX = iterL.premultiply(iterA.transpose()).onMatching(iterC, SUBTRACT);
                    this.getSolutionQ(rhsX, iterX);
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