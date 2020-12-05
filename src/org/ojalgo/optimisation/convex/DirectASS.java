/*
 * Copyright 1997-2020 Optimatika
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
 * and [AI][X] &lt;= [BI]
 * </p>
 * Where [AE] and [BE] are optional.
 *
 * @author apete
 */
final class DirectASS extends ActiveSetSolver {

    DirectASS(final ConvexSolver.Builder matrices, final Optimisation.Options solverOptions) {
        super(matrices, solverOptions);
    }

    @Override
    protected void performIteration() {

        if (this.isLogDebug()) {
            this.log("\nPerformIteration {}", 1 + this.countIterations());
            this.log(this.toActivatorString());
        }

        this.getConstraintToInclude();
        this.setConstraintToInclude(-1);
        final int[] incl = this.getIncluded();
        final int[] excl = this.getExcluded();

        boolean solved = false;

        final int numbConstr = this.countIterationConstraints();
        final int numbVars = this.countVariables();

        final Primitive64Store iterX = this.getIterationX();
        final Primitive64Store iterL = Primitive64Store.FACTORY.makeZero(numbConstr, 1L);
        final Primitive64Store soluL = this.getSolutionL();

        if ((numbConstr <= numbVars) && (solved = this.isSolvableQ())) {
            // Q is SPD

            if (numbConstr == 0L) {
                // Unconstrained - can happen when there are no equality constraints and all inequalities are inactive

                iterX.fillMatching(this.getInvQC());

            } else {
                // Actual/normal optimisation problem

                final MatrixStore<Double> iterA = this.getIterationA();
                final MatrixStore<Double> iterB = this.getIterationB();
                final MatrixStore<Double> iterC = this.getIterationC();

                final MatrixStore<Double> tmpInvQAT = this.getSolutionQ(iterA.transpose());
                // TODO Only 1 column change inbetween active set iterations (add or remove 1 column)
                // BasicLogger.debug("tmpInvQAT", tmpInvQAT);

                // Negated Schur complement
                final ElementsSupplier<Double> tmpS = tmpInvQAT.premultiply(iterA);
                // TODO Symmetric, only need to calculate half the Schur complement, and only 1 row/column changes per iteration

                if (this.isLogDebug()) {
                    // BasicLogger.debug("Negated Schur complement: " + Arrays.toString(incl), tmpS.get());
                }

                if (solved = this.computeGeneral(tmpS)) {

                    ElementsSupplier<Double> rhs = this.getInvQC().premultiply(iterA).operateOnMatching(SUBTRACT, iterB);
                    this.getSolutionGeneral(rhs, iterL);

                    if (this.isLogDebug()) {
                        this.log("RHS={}", rhs.get().toRawCopy1D());
                        this.log("Relative error {} in solution for L={}", NaN, Arrays.toString(iterL.toRawCopy1D()));
                    }

                    final ElementsSupplier<Double> tmpRHS = iterL.premultiply(iterA.transpose()).operateOnMatching(iterC, SUBTRACT);
                    this.getSolutionQ(tmpRHS, iterX);
                }
            }
        }

        if (!solved) {
            // The above failed, try solving the full KKT system instaed

            final Primitive64Store tmpXL = Primitive64Store.FACTORY.makeZero(numbVars + numbConstr, 1L);

            if (solved = this.solveFullKKT(tmpXL)) {

                iterX.fillMatching(tmpXL.logical().limits(numbVars, 1).get());
                iterL.fillMatching(tmpXL.logical().offsets(numbVars, 0).get());
            }
        }

        soluL.fillAll(0.0);
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
