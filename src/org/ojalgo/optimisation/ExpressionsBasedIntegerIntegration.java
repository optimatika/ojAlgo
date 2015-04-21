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
package org.ojalgo.optimisation;

import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.integer.IntegerSolver;

final class ExpressionsBasedIntegerIntegration extends Integration<IntegerSolver> {

    public IntegerSolver build(final ExpressionsBasedModel model) {
        return IntegerSolver.make(model);
    }

    public Capabilities getCapabilities() {
        return new Capabilities() {

            /**
             * @see org.ojalgo.optimisation.Optimisation.Capabilities#integerVariables()
             */
            public boolean integerVariables() {
                return true;
            }

            /**
             * @see org.ojalgo.optimisation.Optimisation.Capabilities#linearConstraints()
             */
            public boolean linearConstraints() {
                return true;
            }

            /**
             * @see org.ojalgo.optimisation.Optimisation.Capabilities#linearObjective()
             */
            public boolean linearObjective() {
                return true;
            }

            /**
             * @see org.ojalgo.optimisation.Optimisation.Capabilities#quadraticConstraints()
             */
            public boolean quadraticConstraints() {
                return true;
            }

            /**
             * @see org.ojalgo.optimisation.Optimisation.Capabilities#quadraticObjective()
             */
            public boolean quadraticObjective() {
                return true;
            }

        };
    }

    public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {
        return solverState;
    }

    public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {
        return modelState;
    }

}
