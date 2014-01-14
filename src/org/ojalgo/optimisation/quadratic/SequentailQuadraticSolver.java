/*
 * Copyright 1997-2013 Optimatika (www.optimatika.se)
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
package org.ojalgo.optimisation.quadratic;

import java.util.List;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;

public class SequentailQuadraticSolver extends GenericSolver {

    List<Expression> myEqualityConstraints;
    List<Expression> myLowerConstraints;
    List<Expression> myUpperConstraints;

    public SequentailQuadraticSolver(final ExpressionsBasedModel aModel, final Options solverOptions) {

        super(aModel, solverOptions);

        myEqualityConstraints = aModel.selectExpressionsQuadraticEquality();
        myLowerConstraints = aModel.selectExpressionsQuadraticLower();
        myUpperConstraints = aModel.selectExpressionsQuadraticUpper();
    }

    public Result solve(final Result kickStart) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected MatrixStore<Double> extractSolution() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean initialise(final Result kickStart) {
        return false;
        // TODO Auto-generated method stub

    }

    @Override
    protected boolean needsAnotherIteration() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected boolean validate() {

        final boolean retVal = true;
        this.setState(State.VALID);

        return retVal;
    }

}
