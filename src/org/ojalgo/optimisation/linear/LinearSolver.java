/*
 * Copyright 1997-2018 Optimatika
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

import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Structure1D.IntIndex;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.optimisation.linear.SimplexTableau.DenseTableau;

public abstract class LinearSolver extends GenericSolver {

    public static final class Builder extends GenericSolver.Builder<LinearSolver.Builder, LinearSolver> {

        private final ConvexSolver.Builder myDelegate;

        public Builder() {

            super();

            myDelegate = new ConvexSolver.Builder();
            ;
        }

        public Builder(final MatrixStore<Double> C) {

            super();

            myDelegate = new ConvexSolver.Builder(C);
        }

        @Override
        public int countConstraints() {
            return myDelegate.countConstraints();
        }

        @Override
        public int countVariables() {
            return myDelegate.countVariables();
        }

        public LinearSolver.Builder equalities(final MatrixStore<Double> mtrxAE, final MatrixStore<Double> mtrxBE) {
            myDelegate.equalities(mtrxAE, mtrxBE);
            return this;
        }

        public MatrixStore<Double> getAE() {
            return myDelegate.getAE();
        }

        public MatrixStore<Double> getBE() {
            return myDelegate.getBE();
        }

        public MatrixStore<Double> getC() {
            return myDelegate.getC();
        }

        public LinearSolver.Builder objective(final MatrixStore<Double> mtrxC) {
            myDelegate.objective(mtrxC);
            return this;
        }

        @Override
        protected LinearSolver doBuild(final Optimisation.Options options) {

            final SimplexTableau tableau = new DenseTableau(this);

            return new SimplexSolver(tableau, options);
        }

    }

    public static final class ModelIntegration extends ExpressionsBasedModel.Integration<LinearSolver> {

        public LinearSolver build(final ConvexSolver.Builder convexBuilder, final Optimisation.Options options) {

            final SimplexTableau tableau = SimplexSolver.build(convexBuilder);

            return new SimplexSolver(tableau, options);
        }

        public LinearSolver build(final ExpressionsBasedModel model) {

            final SimplexTableau tableau = SimplexSolver.build(model);

            return new SimplexSolver(tableau, model.options);
        }

        public boolean isCapable(final ExpressionsBasedModel model) {
            return !(model.isAnyVariableInteger() || model.isAnyExpressionQuadratic());
        }

        @Override
        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {

            final Primitive64Array tmpModelSolution = Primitive64Array.make(model.countVariables());

            for (final IntIndex tmpFixed : model.getFixedVariables()) {
                tmpModelSolution.set(tmpFixed.index, model.getVariable(tmpFixed.index).getValue().doubleValue());
            }

            final List<Variable> tmpPositives = model.getPositiveVariables();
            for (int p = 0; p < tmpPositives.size(); p++) {
                final Variable tmpVariable = tmpPositives.get(p);
                final int tmpIndex = model.indexOf(tmpVariable);
                tmpModelSolution.set(tmpIndex, solverState.doubleValue(p));
            }

            final List<Variable> tmpNegatives = model.getNegativeVariables();
            for (int n = 0; n < tmpNegatives.size(); n++) {
                final Variable tmpVariable = tmpNegatives.get(n);
                final int tmpIndex = model.indexOf(tmpVariable);
                tmpModelSolution.set(tmpIndex, tmpModelSolution.doubleValue(tmpIndex) - solverState.doubleValue(tmpPositives.size() + n));
            }

            return new Result(solverState.getState(), solverState.getValue(), tmpModelSolution);
        }

        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {

            final List<Variable> tmpPositives = model.getPositiveVariables();
            final List<Variable> tmpNegatives = model.getNegativeVariables();

            final int tmpCountPositives = tmpPositives.size();
            final int tmpCountNegatives = tmpNegatives.size();

            final Primitive64Array tmpSolverSolution = Primitive64Array.make(tmpCountPositives + tmpCountNegatives);

            for (int p = 0; p < tmpCountPositives; p++) {
                final Variable tmpVariable = tmpPositives.get(p);
                final int tmpIndex = model.indexOf(tmpVariable);
                tmpSolverSolution.set(p, PrimitiveFunction.MAX.invoke(modelState.doubleValue(tmpIndex), 0.0));
            }

            for (int n = 0; n < tmpCountNegatives; n++) {
                final Variable tmpVariable = tmpNegatives.get(n);
                final int tmpIndex = model.indexOf(tmpVariable);
                tmpSolverSolution.set(tmpCountPositives + n, PrimitiveFunction.MAX.invoke(-modelState.doubleValue(tmpIndex), 0.0));
            }

            return new Result(modelState.getState(), modelState.getValue(), tmpSolverSolution);
        }

        @Override
        protected boolean isSolutionMapped() {
            return true;
        }

    }

    public static LinearSolver.Builder getBuilder() {
        return new LinearSolver.Builder();
    }

    public static LinearSolver.Builder getBuilder(final MatrixStore<Double> C) {
        return LinearSolver.getBuilder().objective(C);
    }

    public static Optimisation.Result solve(final ConvexSolver.Builder convex, final Optimisation.Options options) {

        final int numbVars = convex.countVariables();

        final SimplexTableau tableau = SimplexSolver.build(convex);

        final LinearSolver solver = new SimplexSolver(tableau, options);

        final Result result = solver.solve();

        final Optimisation.Result retVal = new Optimisation.Result(result.getState(), result.getValue(), new Access1D<Double>() {

            public long count() {
                return numbVars;
            }

            public double doubleValue(final long index) {
                return result.doubleValue(index) - result.doubleValue(numbVars + index);
            }

            public Double get(final long index) {
                return this.doubleValue(index);
            }

        });

        retVal.multipliers(result.getMultipliers().get());

        return retVal;
    }

    protected LinearSolver(final Options solverOptions) {
        super(solverOptions);
    }

    protected abstract boolean initialise(Result kickStarter);

    protected abstract boolean needsAnotherIteration();

}
