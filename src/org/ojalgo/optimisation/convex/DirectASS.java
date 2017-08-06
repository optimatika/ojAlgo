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

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import java.util.Arrays;

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
abstract class DirectASS extends ActiveSetSolver {

    DirectASS(final ConvexSolver.Builder matrices, final Optimisation.Options solverOptions) {

        super(matrices, solverOptions);

    }

    @SuppressWarnings("deprecation")
    @Override
    protected void performIteration() {

        if (this.isDebug()) {
            this.debug("\nPerformIteration {}", 1 + this.countIterations());
            this.debug(myActivator.toString());
        }

        myConstraintToInclude = -1;
        final int[] tmpIncluded = myActivator.getIncluded();

        this.getIterationQ();
        final MatrixStore<Double> tmpIterC = this.getIterationC();
        final MatrixStore<Double> tmpIterA = this.getIterationA(tmpIncluded);
        final MatrixStore<Double> tmpIterB = this.getIterationB(tmpIncluded);

        boolean tmpSolvable = false;

        final PrimitiveDenseStore tmpIterX = myIterationX;
        final PrimitiveDenseStore tmpIterL = PrimitiveDenseStore.FACTORY.makeZero(tmpIterA.countRows(), 1L);

        if ((tmpIterA.countRows() < tmpIterA.countColumns()) && (tmpSolvable = myCholesky.isSolvable())) {
            // Q is SPD

            if (tmpIterA.countRows() == 0L) {
                // Unconstrained - can happen when PureASS and all inequalities are inactive

                myCholesky.getSolution(tmpIterC, tmpIterX);

            } else {
                // Actual/normal optimisation problem

                final MatrixStore<Double> tmpInvQAT = myCholesky.getSolution(tmpIterA.transpose());
                // TODO Only 1 column change inbetween active set iterations (add or remove 1 column)
                // BasicLogger.debug("tmpInvQAT", tmpInvQAT);

                // Negated Schur complement
                // final MatrixStore<Double> tmpS = tmpIterA.multiply(tmpInvQAT);
                final ElementsSupplier<Double> tmpS = tmpInvQAT.premultiply(tmpIterA);
                // TODO Symmetric, only need to calculate halv the Schur complement, and only 1 row/column changes per iteration
                //BasicLogger.debug("Negated Schur complement", tmpS.get());

                if (this.isDebug()) {
                    BasicLogger.debug(Arrays.toString(tmpIncluded), tmpS.get());
                }

                if (tmpSolvable = myLU.compute(tmpS)) {

                    // tmpIterX temporarely used to store tmpInvQC
                    // final MatrixStore<Double> tmpInvQC = myCholesky.solve(tmpIterC, tmpIterX);
                    //TODO Constant if C doesn't change

                    //tmpIterL = myLU.solve(tmpInvQC.multiplyLeft(tmpIterA));
                    //myLU.solve(tmpIterA.multiply(myInvQC).subtract(tmpIterB), tmpIterL);
                    myLU.getSolution(myInvQC.premultiply(tmpIterA).operateOnMatching(SUBTRACT, tmpIterB), tmpIterL);

                    //BasicLogger.debug("L", tmpIterL);

                    if (this.isDebug()) {
                        this.debug("Iteration L", tmpIterL);
                    }

                    //myCholesky.solve(tmpIterC.subtract(tmpIterA.transpose().multiply(tmpIterL)), tmpIterX);
                    myCholesky.getSolution(tmpIterL.premultiply(tmpIterA.transpose()).operateOnMatching(tmpIterC, SUBTRACT), tmpIterX);
                }
            }
        }

        if (!tmpSolvable && (tmpSolvable = myLU.compute(this.getIterationKKT(tmpIncluded)))) {
            // The above failed, but the KKT system is solvable
            // Try solving the full KKT system instaed

            final MatrixStore<Double> tmpXL = myLU.getSolution(this.getIterationRHS(tmpIncluded));
            tmpIterX.fillMatching(tmpXL.logical().limits(this.countVariables(), (int) tmpXL.countColumns()).get());
            tmpIterL.fillMatching(tmpXL.logical().offsets(this.countVariables(), 0).get());
        }

        if (!tmpSolvable && this.isDebug()) {
            options.debug_appender.println("KKT system unsolvable!");
            options.debug_appender.printmtrx("KKT", this.getIterationKKT());
            options.debug_appender.printmtrx("RHS", this.getIterationRHS());
        }

        myIterationL.fillAll(0.0);
        final int tmpCountE = this.countEqualityConstraints();
        for (int i = 0; i < tmpCountE; i++) {
            myIterationL.set(i, tmpIterL.doubleValue(i));
        }
        for (int i = 0; i < tmpIncluded.length; i++) {
            myIterationL.set(tmpCountE + tmpIncluded[i], tmpIterL.doubleValue(tmpCountE + i));
        }

        this.handleSubsolution(tmpSolvable, tmpIterX, tmpIncluded);
    }

    @Override
    void excludeAndRemove(final int toExclude) {
        myActivator.exclude(toExclude);
    }

    @Override
    void initSolution(final MatrixStore<Double> tmpBI, final int tmpNumVars, final int tmpNumEqus) {

        if (this.hasInequalityConstraints()) {

            final int[] tmpExcluded = myActivator.getExcluded();

            final MatrixStore<Double> tmpAIX = this.getAIX(tmpExcluded);
            for (int i = 0; i < tmpExcluded.length; i++) {
                final double tmpBody = tmpAIX.doubleValue(i);
                final double tmpRHS = tmpBI.doubleValue(tmpExcluded[i]);
                if (!options.slack.isDifferent(tmpRHS, tmpBody) && (myIterationL.doubleValue(tmpNumEqus + tmpExcluded[i]) != ZERO)) {
                    myActivator.include(tmpExcluded[i]);
                }
            }
        }

        while (((tmpNumEqus + myActivator.countIncluded()) >= tmpNumVars) && (myActivator.countIncluded() > 0)) {
            this.shrink();
        }

        if (this.isDebug() && ((tmpNumEqus + myActivator.countIncluded()) > tmpNumVars)) {
            this.debug("Redundant contraints!");
        }

        myInvQC = myCholesky.getSolution(this.getIterationC());
    }

}
