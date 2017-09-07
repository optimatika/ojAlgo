/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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

import static org.ojalgo.function.PrimitiveFunction.SUBTRACT;

import java.util.Arrays;

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Optimisation;

/**
 * Solves optimisation problems of the form:
 * <p>
 * min 1/2 [X]<sup>T</sup>[Q][X] - [C]<sup>T</sup>[X]<br>
 * when [AE][X] == [BE]<br>
 * and [AI][X] &lt;= [BI]
 * </p>
 * Where [AE] and [BE] are optinal.
 *
 * @author apete
 */
final class DirectASS extends ActiveSetSolver {

    DirectASS(final ConvexSolver.Builder matrices, final Optimisation.Options solverOptions) {
        super(matrices, solverOptions);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void performIteration() {

        if (this.isDebug()) {
            this.debug("\nPerformIteration {}", 1 + this.countIterations());
            this.debug(this.toActivatorString());
        }

        final int toInclude = this.getConstraintToInclude();
        this.setConstraintToInclude(-1);
        final int[] incl = this.getIncluded();

        boolean tmpSolvable = false;

        final PrimitiveDenseStore tmpIterL = PrimitiveDenseStore.FACTORY.makeZero(this.countIterationConstraints(), 1L);

        if ((this.countIterationConstraints() < this.countVariables()) && (tmpSolvable = this.isSolvableQ())) {
            // Q is SPD

            if (this.countIterationConstraints() == 0L) {
                // Unconstrained - can happen when PureASS and all inequalities are inactive

                this.getSolutionQ(this.getIterationC(), this.getIterationX());

            } else {
                // Actual/normal optimisation problem

                final MatrixStore<Double> iterA = this.getIterationA();
                final MatrixStore<Double> tmpInvQAT = this.getSolutionQ(iterA.transpose());
                // TODO Only 1 column change inbetween active set iterations (add or remove 1 column)
                // BasicLogger.debug("tmpInvQAT", tmpInvQAT);

                // Negated Schur complement
                // final MatrixStore<Double> tmpS = tmpIterA.multiply(tmpInvQAT);
                final ElementsSupplier<Double> tmpS = tmpInvQAT.premultiply(iterA);
                // TODO Symmetric, only need to calculate halv the Schur complement, and only 1 row/column changes per iteration
                //BasicLogger.debug("Negated Schur complement", tmpS.get());

                if (this.isDebug()) {
                    BasicLogger.debug(Arrays.toString(incl), tmpS.get());
                }

                if (tmpSolvable = this.computeGeneral(tmpS)) {

                    // tmpIterX temporarely used to store tmpInvQC
                    // final MatrixStore<Double> tmpInvQC = myCholesky.solve(tmpIterC, tmpIterX);
                    //TODO Constant if C doesn't change

                    //tmpIterL = myLU.solve(tmpInvQC.multiplyLeft(tmpIterA));
                    //myLU.solve(tmpIterA.multiply(myInvQC).subtract(tmpIterB), tmpIterL);
                    this.getSolutionGeneral(this.getInvQC().premultiply(iterA).operateOnMatching(SUBTRACT, this.getIterationB()), tmpIterL);

                    //BasicLogger.debug("L", tmpIterL);

                    if (this.isDebug()) {
                        this.debug("Relative error {} in solution for L={}", PrimitiveMath.NaN, tmpIterL);
                    }

                    final ElementsSupplier<Double> tmpRHS = tmpIterL.premultiply(iterA.transpose()).operateOnMatching(this.getIterationC(), SUBTRACT);
                    this.getSolutionQ(tmpRHS, this.getIterationX());
                }
            }
        }

        if (!tmpSolvable && (tmpSolvable = this.computeGeneral(this.getIterationKKT()))) {
            // The above failed, but the KKT system is solvable
            // Try solving the full KKT system instaed

            final MatrixStore<Double> tmpXL = this.getSolutionGeneral(this.getIterationRHS());
            this.getIterationX().fillMatching(tmpXL.logical().limits(this.countVariables(), (int) tmpXL.countColumns()).get());
            tmpIterL.fillMatching(tmpXL.logical().offsets(this.countVariables(), 0).get());
        }

        if (!tmpSolvable && this.isDebug()) {
            options.debug_appender.println("KKT system unsolvable!");
            options.debug_appender.printmtrx("KKT", this.getIterationKKT().collect(PrimitiveDenseStore.FACTORY));
            options.debug_appender.printmtrx("RHS", this.getIterationRHS().collect(PrimitiveDenseStore.FACTORY));
        }

        this.getSolutionL().fillAll(0.0);
        final int tmpCountE = this.countEqualityConstraints();
        for (int i = 0; i < tmpCountE; i++) {
            this.getSolutionL().set(i, tmpIterL.doubleValue(i));
        }
        for (int i = 0; i < incl.length; i++) {
            this.getSolutionL().set(tmpCountE + incl[i], tmpIterL.doubleValue(tmpCountE + i));
        }

        this.handleSubsolution(tmpSolvable, this.getIterationX(), incl);
    }

}
