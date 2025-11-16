/*
 * Copyright 1997-2025 Optimatika
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

/**
 * Proof-of-concept solver that eliminates equality constraints via a null-space projection and delegates the
 * reduced inequality-only QP to an existing active-set solver.
 * <p>
 * It constructs x = x0 + Z y, where x0 is a particular solution of AE x = BE and Z spans the null space of
 * AE. Then it solves the reduced problem in y with only inequalities and reconstructs x and multipliers
 * (recovering equality multipliers from the first-order optimality conditions).
 */
public class EqualityEliminatingASS extends ConvexSolver {

    private final ConvexData<Double> myOriginal;

    public EqualityEliminatingASS(final Optimisation.Options options, final ConvexData<Double> data) {
        super(options);
        myOriginal = data;

    }

    @Override
    public Optimisation.Result solve(final Optimisation.Result kickStarter) {

        NullSpaceProjection projection = NullSpaceProjection.reduce(myOriginal);

        ConvexData<Double> reduced = projection.getReduced();

        ConvexSolver solver = BasePrimitiveSolver.newSolver(reduced, options);

        Result start = projection.toReducedState(kickStarter);

        Optimisation.Result result = solver.solve(start);

        Result retVal = projection.toFullModelState(result);

        return retVal;
    }

}