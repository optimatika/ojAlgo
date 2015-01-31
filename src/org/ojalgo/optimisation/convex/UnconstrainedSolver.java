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

import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.convex.KKTSolver.Input;

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
    protected boolean initialise(final Result kickStart) {
        this.resetX();
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
            this.fillX(tmpOutput.getX());

        } else {

            this.setState(State.INVALID);
            this.resetX();
        }
    }

    @Override
    Input buildDelegateSolverInput() {
        return new KKTSolver.Input(this.getQ(), this.getC());
    }

}
