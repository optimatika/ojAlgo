/*
 * Copyright 1997-2022 Optimatika
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
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure1D.IntIndex;

public abstract class LinearSolver extends GenericSolver implements UpdatableSolver {

    /**
     * @deprecated v50 Use {@link StandardBuilder} instead
     */
    @Deprecated
    public static abstract class Builder extends GenericSolver.Builder<LinearSolver.StandardBuilder, LinearSolver> {

        Builder() {
            super();
        }

        public StandardBuilder objective(final MatrixStore<Double> mtrxC) {
            this.setObjective(LinearSolver.toObjectiveFunction(mtrxC));
            return (StandardBuilder) this;
        }

        @Override
        protected LinearSolver doBuild(final Optimisation.Options options) {

            final SimplexTableau tableau = SimplexTableau.make(this, options);

            return new PrimalSimplex(tableau, options);
        }
    }

    /**
     * Compared to {@link LinearSolver.StandardBuilder} this builder: <br>
     * 1) Accepts inequality constraints <br>
     * 2) Has relaxed the requiremnt on the RHS to be non-negative (both equalities and inequalities) <br>
     * <br>
     * Compared to {@link ConvexSolver.Builder} this builder: <br>
     * 1) Requires the objective function to be linear (or only the linear factors will be concidered) <br>
     * 2) Assumes (requires) variables to be non-negative <br>
     * <br>
     *
     * @author apete
     */
    public static final class GeneralBuilder extends GenericSolver.Builder<LinearSolver.GeneralBuilder, LinearSolver> {

        GeneralBuilder() {
            super();
        }

        @Override
        public LinearSolver.GeneralBuilder inequalities(final Access2D<Double> mtrxAI, final Access1D<Double> mtrxBI) {
            return super.inequalities(mtrxAI, mtrxBI);
        }

        public GeneralBuilder objective(final MatrixStore<Double> mtrxC) {
            this.setObjective(LinearSolver.toObjectiveFunction(mtrxC));
            return this;
        }

        /**
         * Convert inequalities to equalities (adding slack variables) and make sure all RHS are non-negative.
         */
        public StandardBuilder toStandardForm() {

            int nbInequalites = this.countInequalityConstraints();
            int nbEqualites = this.countEqualityConstraints();
            int nbVariables = this.countVariables();

            StandardBuilder retVal = LinearSolver.newStandardBuilder();

            PhysicalStore<Double> mtrxC = null;

            PhysicalStore<Double> mtrxAE = null;
            PhysicalStore<Double> mtrxBE = null;

            if (nbEqualites > 0) {

                if (nbInequalites > 0) {

                    mtrxC = this.getC().below(nbInequalites).collect(FACTORY);

                    mtrxAE = this.getAE().below(this.getAI()).right(nbInequalites).collect(FACTORY);
                    mtrxAE.fillDiagonal(nbEqualites, nbVariables, ONE);

                    mtrxBE = this.getBE().below(this.getBI()).collect(FACTORY);

                } else {

                    mtrxC = this.getC().collect(FACTORY);

                    mtrxAE = this.getAE().collect(FACTORY);

                    mtrxBE = this.getBE().collect(FACTORY);

                }

            } else if (nbInequalites > 0) {

                mtrxC = this.getC().below(nbInequalites).collect(FACTORY);

                mtrxAE = this.getAI().right(nbInequalites).collect(FACTORY);
                mtrxAE.fillDiagonal(nbEqualites, nbVariables, ONE);

                mtrxBE = this.getBI().collect(FACTORY);

            } else {

                throw new IllegalStateException("The problem is unconstrained!");
            }

            for (int i = 0; i < mtrxBE.getRowDim(); i++) {
                double rhs = mtrxBE.doubleValue(i, 0);
                if (rhs < ZERO) {
                    mtrxAE.modifyRow(i, NEGATE);
                    mtrxBE.set(i, 0, -rhs);
                }
            }

            retVal.objective(mtrxC);
            retVal.equalities(mtrxAE, mtrxBE);

            return retVal;
        }

        @Override
        protected LinearSolver doBuild(final Options options) {
            // TODO Do it better
            return this.toStandardForm().build(options);
        }

    }

    public static final class ModelIntegration extends ExpressionsBasedModel.Integration<LinearSolver> {

        public LinearSolver build(final ConvexSolver.Builder convexBuilder, final Optimisation.Options options) {

            final SimplexTableau tableau = PrimalSimplex.build(convexBuilder, options, false);

            return new PrimalSimplex(tableau, options);
        }

        public LinearSolver build(final ExpressionsBasedModel model) {

            SimplexTableau tableau = PrimalSimplex.build(model);

            return new PrimalSimplex(tableau, model.options);
        }

        public boolean isCapable(final ExpressionsBasedModel model) {
            return !model.isAnyVariableInteger() && !model.isAnyExpressionQuadratic();
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

            if (value.signum() >= 0 && (retVal = model.indexOfPositiveVariable(variable)) >= 0) {
                return retVal;
            }
            if (value.signum() <= 0 && (retVal = model.indexOfNegativeVariable(variable)) >= 0) {
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

    /**
     * Defines optimisation problems on the (LP standard) form:
     * <p>
     * min [C]<sup>T</sup>[X]<br>
     * when [AE][X] == [BE]<br>
     * and 0 &lt;= [X]<br>
     * and 0 &lt;= [BE]
     * </p>
     * A Linear Program is in Standard Form if:
     * <ul>
     * <li>All constraints are equality constraints.</li>
     * <li>All variables have a nonnegativity sign restriction.</li>
     * </ul>
     * <p>
     * Further it is required here that the constraint right hand sides are nonnegative (nonnegative elements
     * in [BE]). Don't think that's an LP standard form requirement, but it is required here.
     * </p>
     * <p>
     * The LP standard form does not dictate if expressed on minimisation or maximisation form. Here it should
     * be a minimisation.
     * </p>
     *
     * @author apete
     */
    public static final class StandardBuilder extends LinearSolver.Builder {

        /**
         * @deprecated v50 Use {@link LinearSolver#newBuilder()} instead.
         */
        @Deprecated
        public StandardBuilder() {
            super();
        }

        /**
         * @deprecated v50 Use {@link LinearSolver#newBuilder()} instead.
         */
        @Deprecated
        public StandardBuilder(final MatrixStore<Double> mtrxC) {

            super();

            this.objective(mtrxC);
        }

        @Override
        public StandardBuilder objective(final MatrixStore<Double> mtrxC) {
            return super.objective(mtrxC);
        }

    }

    public static final ModelIntegration INTEGRATION = new ModelIntegration();

    /**
     * @deprecated v50 Use {@link LinearSolver#newStandardBuilder()} or
     *             {@link LinearSolver#newGeneralBuilder()} instead.
     */
    @Deprecated
    public static LinearSolver.StandardBuilder getBuilder() {
        return LinearSolver.newStandardBuilder();
    }

    /**
     * @deprecated v50 Use {@link LinearSolver#newStandardBuilder()} or
     *             {@link LinearSolver#newGeneralBuilder()} instead.
     */
    @Deprecated
    public static LinearSolver.StandardBuilder getBuilder(final MatrixStore<Double> C) {
        return LinearSolver.newStandardBuilder().objective(C);
    }

    public static LinearSolver.GeneralBuilder newGeneralBuilder() {
        return new LinearSolver.GeneralBuilder();
    }

    public static LinearSolver newSolver(final ExpressionsBasedModel model) {

        SimplexTableau tableau = PrimalSimplex.build(model);

        return new PrimalSimplex(tableau, model.options);
    }

    public static LinearSolver.StandardBuilder newStandardBuilder() {
        return new LinearSolver.StandardBuilder();
    }

    public static Optimisation.Result solve(final ConvexSolver.Builder convex, final Optimisation.Options options, final boolean zeroC) {

        int dualSize = DualSimplex.size(convex);
        int primSize = PrimalSimplex.size(convex);
        boolean dual = dualSize <= primSize;

        Optimisation.Result result = dual ? DualSimplex.doSolve(convex, options, zeroC) : PrimalSimplex.doSolve(convex, options, zeroC);

        if (options.validate) {

            Optimisation.Result altResult = dual ? PrimalSimplex.doSolve(convex, options, zeroC) : DualSimplex.doSolve(convex, options, zeroC);

            if (result.getMultipliers().isPresent()
                    && !Access1D.equals(result.getMultipliers().get(), altResult.getMultipliers().get(), ACCURACY.withPrecision(8).withScale(6))) {

                Optimisation.Result primRes = dual ? altResult : result;
                Optimisation.Result dualRes = dual ? result : altResult;

                BasicLogger.error();
                BasicLogger.error("Prim sol: {}", primRes);
                BasicLogger.error("Dual sol: {}", dualRes);

                BasicLogger.error("Prim mul: {}", primRes.getMultipliers().get());
                BasicLogger.error("Dual mul: {}", dualRes.getMultipliers().get());
                BasicLogger.error();
            }
        }

        return result;
    }

    static LinearFunction<Double> toObjectiveFunction(final MatrixStore<Double> mtrxC) {

        ProgrammingError.throwIfNull(mtrxC);

        PhysicalStore<Double> tmpC = null;

        if (mtrxC instanceof PhysicalStore) {
            tmpC = (PhysicalStore<Double>) mtrxC;
        } else {
            tmpC = mtrxC.copy();
        }

        return LinearFunction.wrap(tmpC);
    }

    protected LinearSolver(final Options solverOptions) {
        super(solverOptions);
    }

    protected abstract boolean initialise(Result kickStarter);

    protected abstract boolean needsAnotherIteration();

}
