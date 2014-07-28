/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.ZeroStore;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.convex.KKTSolver.Input;

/**
 * Solves optimisation problems of the form:
 * <p>
 * min 1/2 [X]<sup>T</sup>[Q][X] - [C]<sup>T</sup>[X]<br>
 * when [AE][X] == [BE]
 * </p>
 *
 * @author apete
 */
final class QPESolver extends ConvexSolver {

    private boolean myFeasible = false;

    QPESolver(final ExpressionsBasedModel aModel, final Optimisation.Options solverOptions, final ConvexSolver.Builder matrices) {
        super(aModel, solverOptions, matrices);
    }

    private boolean isFeasible() {

        boolean retVal = true;

        final MatrixStore<Double> tmpSE = this.getSE();
        for (int i = 0; retVal && (i < tmpSE.countRows()); i++) {
            final double tmpVal = tmpSE.doubleValue(i);
            if (!options.slack.isZero(tmpVal)) {
                retVal = false;
            }
        }

        return retVal;
    }

    @Override
    protected boolean initialise(final Result kickStarter) {

        if (kickStarter != null) {
            this.fillX(kickStarter);
            myFeasible = this.isFeasible();
        } else {
            this.resetX();
            myFeasible = false; // Could still be feasible, but doesn't matter...
        }

        return true;
    }

    @Override
    protected boolean needsAnotherIteration() {
        return this.countIterations() < 1;
    }

    @Override
    protected void performIteration() {

        final KKTSolver.Input tmpInput = this.buildDelegateSolverInput();

        final KKTSolver tmpSolver = this.getDelegateSolver(tmpInput);

        final KKTSolver.Output tmpOutput = tmpSolver.solve(tmpInput, options.validate);

        if (tmpOutput.isSolvable()) {

            this.setState(State.OPTIMAL);
            this.getX().fillMatching(this.getX(), PrimitiveFunction.ADD, tmpOutput.getX());
            this.getLE().fillMatching(tmpOutput.getL());

        } else if (myFeasible) {

            this.setState(State.FEASIBLE);

        } else {

            this.setState(State.INFEASIBLE);
            this.resetX();
        }
    }

    @Override
    Input buildDelegateSolverInput() {

        final MatrixStore<Double> tmpQ = this.getQ();
        final MatrixStore<Double> tmpC = this.getC();
        final MatrixStore<Double> tmpA = this.getAE();

        if (myFeasible) {

            final PhysicalStore<Double> tmpX = this.getX();

            return new KKTSolver.Input(tmpQ, tmpC.subtract(tmpQ.multiplyRight(tmpX)), tmpA, ZeroStore.makePrimitive((int) tmpA.countRows(), 1));

        } else {

            final MatrixStore<Double> tmpB = this.getBE();

            return new KKTSolver.Input(tmpQ, tmpC, tmpA, tmpB);
        }
    }

}
