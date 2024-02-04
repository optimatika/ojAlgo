/*
 * Copyright 1997-2024 Optimatika
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
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.optimisation.Optimisation;

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

    DirectASS(final ConvexData<Double> convexSolverBuilder, final Optimisation.Options optimisationOptions) {
        super(convexSolverBuilder, optimisationOptions);
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

        Primitive64Store iterX = this.getIterationX();
        Primitive64Store iterL = MATRIX_FACTORY.make(numbConstr, 1L);
        Primitive64Store soluL = this.getSolutionL();

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
                // TODO Only 1 column change inbetween active set iterations (add or remove 1 column)
                if (this.isLogDebug()) {
                    this.log("invQAt", invQAt);
                }

                // Negated Schur complement
                ElementsSupplier<Double> tmpS = invQAt.premultiply(iterA);
                // TODO Symmetric, only need to calculate half the Schur complement, and only 1 row/column changes per iteration

                if (this.isLogDebug()) {
                    this.log("Negated Schur complement: " + Arrays.toString(incl), tmpS.collect(MATRIX_FACTORY));
                }

                if (solved = this.computeGeneral(tmpS)) {

                    ElementsSupplier<Double> rhsL = invQC.premultiply(iterA).onMatching(SUBTRACT, iterB);
                    this.getSolutionGeneral(rhsL, iterL);

                    if (this.isLogDebug()) {
                        this.log("RHS={}", rhsL.collect(MATRIX_FACTORY).toRawCopy1D());
                        this.log("Relative error {} in solution for L={}", NaN, Arrays.toString(iterL.toRawCopy1D()));
                    }

                    // ElementsSupplier<Double> rhsX = iterL.premultiply(iterA.transpose()).onMatching(iterC, SUBTRACT);
                    // this.getSolutionQ(rhsX, iterX);

                    iterL.premultiply(invQAt).onMatching(invQC, SUBTRACT).supplyTo(iterX);
                }
            }
        }

        if (!solved) {
            // The above failed, try solving the full KKT system instaed

            Primitive64Store tmpXL = MATRIX_FACTORY.make(numbVars + numbConstr, 1L);

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
