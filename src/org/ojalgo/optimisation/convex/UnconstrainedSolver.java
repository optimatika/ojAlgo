/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.optimisation.Optimisation;

/**
 * Solves optimisation problems of the form:
 * <p>
 * min 1/2 [X]<sup>T</sup>[Q][X] - [C]<sup>T</sup>[X]
 * </p>
 *
 * @author apete
 */
final class UnconstrainedSolver extends ConvexSolver {

    UnconstrainedSolver(final ConvexSolver.Builder matrices, final Optimisation.Options solverOptions) {
        super(matrices, solverOptions);
    }

    @Override
    protected MatrixStore<Double> getIterationKKT() {
        return this.getIterationQ();
    }

    @Override
    protected MatrixStore<Double> getIterationRHS() {
        return this.getIterationC();
    }

    @Override
    protected boolean initialise(final Result kickStarter) {
        super.initialise(kickStarter);
        this.resetX();
        return true;
    }

    @Override
    protected boolean needsAnotherIteration() {
        return this.countIterations() < 1;
    }

    @Override
    protected void performIteration() {

        final MatrixStore<Double> tmpQ = this.getIterationQ();
        final MatrixStore<Double> tmpC = this.getIterationC();
        final DecompositionStore<Double> tmpX = this.getX();

        boolean tmpSolvable = true;

        if (tmpSolvable = myCholesky.isSolvable()) {
            // Q is SPD

            myCholesky.solve(tmpC, tmpX);

        } else if (tmpSolvable = myLU.compute(tmpQ)) {
            // The above failed, but the KKT system is solvable
            // Try solving the full KKT system instaed

            myLU.solve(tmpC, tmpX);
        }

        if (!tmpSolvable && this.isDebug()) {
            options.debug_appender.println("KKT system unsolvable!");
            options.debug_appender.printmtrx("KKT", this.getIterationKKT());
            options.debug_appender.printmtrx("RHS", this.getIterationRHS());
        }

        if (tmpSolvable) {
            this.setState(State.DISTINCT);
        } else {
            this.setState(State.UNBOUNDED);
            this.resetX();
        }
    }

    @Override
    final MatrixStore<Double> getIterationC() {
        return this.getC();
    }

}
