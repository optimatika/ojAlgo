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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

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
final class UnconstrainedSolver extends BasePrimitiveSolver {

    UnconstrainedSolver(final ConvexData<Double> convexSolverBuilder, final Optimisation.Options optimisationOptions) {
        super(convexSolverBuilder, optimisationOptions);
    }

    @Override
    protected MatrixStore<Double> getIterationKKT() {
        return this.getMatrixQ();
    }

    @Override
    protected MatrixStore<Double> getIterationRHS() {
        return this.getMatrixC();
    }

    @Override
    protected boolean needsAnotherIteration() {
        return this.countIterations() < 1;
    }

    @Override
    protected void performIteration() {

        boolean solved = false;

        if (solved = this.isSolvableQ()) {
            // Q is SPD

            this.getSolutionQ(this.getMatrixC(), this.getSolutionX());

        } else if (solved = this.solveFullKKT(this.getSolutionX())) {
            // Q not SPD, but the KKT system is solvable
        }

        if (solved) {
            this.setState(State.DISTINCT);
        } else {
            this.setState(State.UNBOUNDED);
            this.getSolutionX().fillAll(ZERO);
        }

        this.incrementIterationsCount();
    }

}
