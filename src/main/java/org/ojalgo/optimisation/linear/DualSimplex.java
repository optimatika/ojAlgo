/*
 * Copyright 1997-2021 Optimatika
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
package org.ojalgo.optimisation.linear;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.RowView;
import org.ojalgo.type.context.NumberContext;

final class DualSimplex extends SimplexSolver {

    static SimplexTableau build(final ConvexSolver.Builder convex, final Optimisation.Options options, final boolean zeroC) {

        int numbVars = convex.countVariables();
        int numbEqus = convex.countEqualityConstraints();
        int numbInes = convex.countInequalityConstraints();

        SimplexTableau retVal = SimplexTableau.make(numbVars, numbEqus + numbEqus + numbInes, 0, options);

        Mutate1D obj = retVal.objective();
        Mutate2D constrBody = retVal.constraintsBody();
        Mutate1D constrRHS = retVal.constraintsRHS();

        MatrixStore<Double> convexC = zeroC ? Primitive64Store.FACTORY.makeZero(convex.countVariables(), 1) : convex.getC();
        MatrixStore<Double> convexAE = convex.getAE();
        MatrixStore<Double> convexBE = convex.getBE();

        for (int i = 0; i < numbVars; i++) {
            double rhs = convexC.doubleValue(i);
            boolean neg = retVal.negative[i] = NumberContext.compare(rhs, ZERO) < 0;
            for (int j = 0; j < numbEqus; j++) {
                double valE = convexAE.doubleValue(j, i);
                constrBody.set(i, j, neg ? -valE : valE);
                constrBody.set(i, numbEqus + j, neg ? valE : -valE);
            }
            constrRHS.set(i, neg ? -rhs : rhs);
        }

        for (RowView<Double> rowAI : convex.getRowsAI()) {

            long tabJ = rowAI.row();

            for (ElementView1D<Double, ?> elemV : rowAI.nonzeros()) {
                int tabI = Math.toIntExact(elemV.index());

                double tabVal = elemV.doubleValue();
                constrBody.set(tabI, numbEqus + numbEqus + tabJ, retVal.negative[tabI] ? -tabVal : tabVal);
            }
        }

        for (int j = 0; j < numbEqus; j++) {
            obj.set(j, convexBE.doubleValue(j));
            obj.set(numbEqus + j, -convexBE.doubleValue(j));
        }
        for (int j = 0; j < numbInes; j++) {
            obj.set(numbEqus + numbEqus + j, convex.getBI(j));
        }

        return retVal;
    }

    static Optimisation.Result doSolve(final ConvexSolver.Builder convex, final Optimisation.Options options, final boolean zeroC) {

        SimplexTableau tableau = DualSimplex.build(convex, options, zeroC);

        DualSimplex solver = new DualSimplex(tableau, options);

        Result result = solver.solve();

        Optimisation.Result retVal = DualSimplex.toConvexState(result, convex);

        return retVal;
    }

    static int size(final ConvexSolver.Builder convex) {

        int numbVars = convex.countVariables();
        int numbEqus = convex.countEqualityConstraints();
        int numbInes = convex.countInequalityConstraints();

        return SimplexTableau.size(numbVars, numbEqus + numbEqus + numbInes, 0);
    }

    static Optimisation.Result toConvexState(final Result result, final ConvexSolver.Builder convex) {

        int numbEqus = convex.countEqualityConstraints();
        int numbInes = convex.countInequalityConstraints();

        Access1D<?> multipliers = result.getMultipliers().get();

        Optimisation.Result retVal = new Optimisation.Result(result.getState(), result.getValue(), result);

        retVal.multipliers(new Access1D<Double>() {

            public long count() {
                return numbEqus + numbInes;
            }

            public double doubleValue(final long index) {
                if (index < numbEqus) {
                    return -(multipliers.doubleValue(index) - multipliers.doubleValue(numbEqus + index));
                }
                return -multipliers.doubleValue(numbEqus + index);
            }

            public Double get(final long index) {
                return this.doubleValue(index);
            }

            @Override
            public String toString() {
                return Access1D.toString(this);
            }

        });

        return retVal;
    }

    DualSimplex(final SimplexTableau tableau, final Options solverOptions) {
        super(tableau, solverOptions);
    }

    @Override
    protected Access1D<?> extractMultipliers() {
        return super.extractSolution();
    }

    @Override
    protected Access1D<?> extractSolution() {
        return super.extractMultipliers();
    }

    @Override
    protected State getState() {

        State state = super.getState();

        if (state == State.UNBOUNDED) {
            return State.INFEASIBLE;
        }
        if (!state.isFeasible()) {
            return State.UNBOUNDED;
        }
        return state;
    }

}
