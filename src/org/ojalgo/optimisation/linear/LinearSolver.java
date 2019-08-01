/*
 * Copyright 1997-2019 Optimatika
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

import java.math.BigDecimal;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.function.multiary.LinearFunction;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.optimisation.linear.SimplexTableau.DenseTableau;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure1D.IntIndex;

public abstract class LinearSolver extends GenericSolver implements UpdatableSolver {

    public static final class Builder extends GenericSolver.Builder<LinearSolver.Builder, LinearSolver> {

        private LinearFunction<Double> myObjective = null;

        public Builder() {
            super();
        }

        public Builder(final MatrixStore<Double> C) {

            super();

            this.objective(C);
        }

        /**
         * Currently/still the RHS elements need to non-negative. (You have to convert the LP to standard
         * form.)
         *
         * @see org.ojalgo.optimisation.GenericSolver.Builder#equalities(org.ojalgo.matrix.store.MatrixStore,
         *      org.ojalgo.matrix.store.MatrixStore)
         */
        @Override
        public Builder equalities(final MatrixStore<Double> mtrxAE, final MatrixStore<Double> mtrxBE) {
            return super.equalities(mtrxAE, mtrxBE);
        }

        public MatrixStore<Double> getC() {
            return myObjective.linear();
        }

        /**
         * Setting inequalities here is not yet supported. (You have to convert the LP to standard form.)
         *
         * @see org.ojalgo.optimisation.GenericSolver.Builder#inequalities(org.ojalgo.structure.Access2D,
         *      org.ojalgo.matrix.store.MatrixStore)
         */
        @Override
        public Builder inequalities(final Access2D<Double> mtrxAI, final MatrixStore<Double> mtrxBI) {
            ProgrammingError.throwForIllegalInvocation();
            return super.inequalities(mtrxAI, mtrxBI);
        }

        public LinearSolver.Builder objective(final MatrixStore<Double> mtrxC) {
            this.setObjective(mtrxC);
            return this;
        }

        @Override
        public void reset() {
            super.reset();
            myObjective = null;
        }

        @Override
        public String toString() {

            final String simpleName = this.getClass().getSimpleName();

            final StringBuilder retVal = new StringBuilder("<" + simpleName + ">");

            retVal.append("\n[AE] = " + (this.getAE() != null ? PrimitiveMatrix.FACTORY.copy(this.getAE()) : "?"));

            retVal.append("\n[BE] = " + (this.getBE() != null ? PrimitiveMatrix.FACTORY.copy(this.getBE()) : "?"));

            retVal.append("\n[C] = " + (myObjective != null ? PrimitiveMatrix.FACTORY.copy(this.getC()) : "?"));

            retVal.append("\n[AI] = " + (this.getAI() != null ? PrimitiveMatrix.FACTORY.copy(this.getAI()) : "?"));

            retVal.append("\n[BI] = " + (this.getBI() != null ? PrimitiveMatrix.FACTORY.copy(this.getBI()) : "?"));

            retVal.append("\n</" + simpleName + ">");

            return retVal.toString();
        }

        private Builder setObjective(final MatrixStore<Double> mtrxC) {

            PhysicalStore<Double> tmpC = null;

            if (mtrxC == null) {
                tmpC = PrimitiveDenseStore.FACTORY.make(this.countVariables(), 1);
            } else if (mtrxC instanceof PhysicalStore) {
                tmpC = (PhysicalStore<Double>) mtrxC;
            } else {
                tmpC = mtrxC.copy();
            }

            myObjective = LinearFunction.wrap(tmpC);
            super.setObjective(myObjective);

            return this;
        }

        @Override
        protected LinearSolver doBuild(final Optimisation.Options options) {

            final SimplexTableau tableau = new DenseTableau(this);

            return new PrimalSimplex(tableau, options);
        }

    }

    public static final class ModelIntegration extends ExpressionsBasedModel.Integration<LinearSolver> {

        public LinearSolver build(final ConvexSolver.Builder convexBuilder, final Optimisation.Options options) {

            final SimplexTableau tableau = PrimalSimplex.build(convexBuilder, options);

            return new PrimalSimplex(tableau, options);
        }

        public LinearSolver build(final ExpressionsBasedModel model) {

            final SimplexTableau tableau = PrimalSimplex.build(model);

            // BasicLogger.debug("EBM tabeau", tableau);

            return new PrimalSimplex(tableau, model.options);
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
                tmpSolverSolution.set(p, MAX.invoke(modelState.doubleValue(tmpIndex), ZERO));
            }

            for (int n = 0; n < tmpCountNegatives; n++) {
                final Variable tmpVariable = tmpNegatives.get(n);
                final int tmpIndex = model.indexOf(tmpVariable);
                tmpSolverSolution.set(tmpCountPositives + n, MAX.invoke(-modelState.doubleValue(tmpIndex), ZERO));
            }

            return new Result(modelState.getState(), modelState.getValue(), tmpSolverSolution);
        }

        @Override
        protected int getIndexInSolver(final ExpressionsBasedModel model, final Variable variable) {

            int retVal = -1;

            BigDecimal value = variable.getValue();

            if ((value.signum() >= 0) && ((retVal = model.indexOfPositiveVariable(variable)) >= 0)) {
                return retVal;
            } else if ((value.signum() <= 0) && ((retVal = model.indexOfNegativeVariable(variable)) >= 0)) {
                retVal += model.getPositiveVariables().size();
                return retVal;
            }

            return -1;
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

        Optimisation.Result primRes = PrimalSimplex.solve(convex, options);

        //        Optimisation.Result dualRes = DualSimplex.solve(convex, options);
        //
        //        if (primRes.getMultipliers().isPresent()
        //                && !Access1D.equals(primRes.getMultipliers().get(), dualRes.getMultipliers().get(), ACCURACY.withPrecision(8).withScale(6))) {
        //
        //            BasicLogger.debug();
        //            BasicLogger.debug("Prim sol: {}", primRes);
        //            BasicLogger.debug("Dual sol: {}", dualRes);
        //
        //            BasicLogger.debug("Prim mul: {}", primRes.getMultipliers().get());
        //            BasicLogger.debug("Dual mul: {}", dualRes.getMultipliers().get());
        //            BasicLogger.debug();
        //        }

        return primRes;
    }

    protected LinearSolver(final Options solverOptions) {
        super(solverOptions);
    }

    protected abstract boolean initialise(Result kickStarter);

    protected abstract boolean needsAnotherIteration();

}
