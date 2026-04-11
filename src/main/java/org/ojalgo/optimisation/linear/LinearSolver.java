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
package org.ojalgo.optimisation.linear;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.function.multiary.LinearFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.convex.ConvexData;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Primitive1D;
import org.ojalgo.structure.Primitive2D;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.IndexSelector;

/**
 * Base class for ojAlgo's linear programming (LP) solvers. Several simplex-based subclasses exist, and the
 * exact problem form accepted may vary between them.
 * <p>
 * The recommended way to model and solve any LP (or optimisation problem in general) is to use
 * {@link ExpressionsBasedModel}. The model automatically selects a suitable solver and translates between the
 * user-friendly model representation and the solver's internal form. The {@link #INTEGRATION} field is the
 * bridge that makes this possible — it is an {@link ExpressionsBasedModel.Integration
 * ExpressionsBasedModel.Integration} registered by default so that {@link ExpressionsBasedModel} can delegate
 * to {@link LinearSolver} without any additional setup.
 * <p>
 * For more direct control, use {@link #newBuilder()} to create a {@link Builder}. The Builder accepts an LP
 * in standard form and produces a solver via {@link Builder#build()}.
 * <p>
 * Implements {@link UpdatableSolver} — after solving the solver can be updated in-place and re-solved without
 * rebuilding the entire problem.
 */
public abstract class LinearSolver extends GenericSolver implements UpdatableSolver {

    /**
     * Assembles a linear program and builds a {@link LinearSolver}. The problem is expressed as:
     *
     * <pre>
     * min [c]'[x]
     * s.t. [AE][x] = [bE]
     *      [AI][x] <= [bI]
     *      [lb] <= [x] <= [ub]
     * </pre>
     *
     * Objective: set via {@link #objective(double...)} or the convenience factory
     * {@link LinearSolver#newBuilder(double...)}.
     * <p>
     * Constraints:
     * <ul>
     * <li>{@link #inequality(double, double...)} / {@link #inequalities(Access2D, Access1D)} for [AI][x] <=
     * [bI]
     * <li>{@link #equality(double, double...)} / {@link #equalities(Access2D, Access1D)} for [AE][x] = [bE]
     * </ul>
     * The RHS may be negative; the implementation handles sign normalisation internally.
     * <p>
     * Variable bounds: every variable defaults to lower=0, upper=+Infinity (standard LP convention). Use
     * {@link #lower(double...)} and {@link #upper(double...)} to override per-variable or uniformly.
     * <p>
     * Building a solver: calling {@link #build(Optimisation.Options)} delegates to one of two simplex
     * implementations. When no explicit {@link Configuration} is set, the choice is automatic: if any
     * variable bounds have been modified (via {@link #lower(double...)} or {@link #upper(double...)}), the
     * revised simplex solver is used; otherwise the tableau solver is used. An explicit
     * {@link Configuration#primal()} or {@link Configuration#dual()} always overrides this default.
     * <ul>
     * <li>Tableau solver (default when bounds are unchanged / {@link Configuration#primal()}) — classic
     * 2-phase primal simplex operating on a full or sparse tableau. Requires all variables >= 0 (the
     * default).
     * <li>Revised simplex solver (default when bounds are modified / {@link Configuration#dual()}) — phased
     * dual/primal simplex using a factored basis inverse. Supports explicit finite lower and upper bounds on
     * variables.
     * </ul>
     */
    public static final class Builder extends GenericSolver.Builder<LinearSolver.Builder, LinearSolver> {

        private boolean myBoundsModified = false;

        Builder() {
            super();
        }

        @Override
        public LinearSolver.Builder equalities(final Access2D<?> mtrxAE, final Access1D<?> mtrxBE) {
            return super.equalities(mtrxAE, mtrxBE);
        }

        @Override
        public LinearSolver.Builder equality(final double rhs, final double... factors) {
            return super.equality(rhs, factors);
        }

        @Override
        public LinearFunction<Double> getObjective() {
            LinearFunction<Double> retVal = this.getObjective(LinearFunction.class);
            if (retVal == null) {
                retVal = LinearFunction.factory(this.getFactory()).make(this.countVariables());
                super.setObjective(retVal);
            }
            return retVal;
        }

        @Override
        public LinearSolver.Builder inequalities(final Access2D<?> mtrxAI, final Access1D<?> mtrxBI) {
            return super.inequalities(mtrxAI, mtrxBI);
        }

        @Override
        public LinearSolver.Builder inequality(final double rhs, final double... factors) {
            return super.inequality(rhs, factors);
        }

        public LinearSolver.Builder lower(final double... bounds) {
            myBoundsModified = true;
            double[] lowerBounds = this.getLowerBounds();
            for (int i = 0, limit = Math.min(lowerBounds.length, bounds.length); i < limit; i++) {
                lowerBounds[i] = bounds[i];
            }
            return this;
        }

        public LinearSolver.Builder lower(final double bound) {
            myBoundsModified = true;
            double[] lowerBounds = this.getLowerBounds();
            Arrays.fill(lowerBounds, bound);
            return this;
        }

        public LinearSolver.Builder objective(final double... factors) {
            this.setNumberOfVariables(factors.length);
            this.getObjective().linear().fillMatching(this.getFactory().column(factors));
            return this;
        }

        public LinearSolver.Builder objective(final int index, final double value) {
            this.getObjective().linear().set(index, value);
            return this;
        }

        public LinearSolver.Builder objective(final MatrixStore<Double> mtrxC) {
            this.setObjective(LinearSolver.toObjectiveFunction(mtrxC));
            return this;
        }

        /**
         * Convert inequalities to equalities (adding slack variables) and make sure all RHS are non-negative.
         * <p>
         * Defines optimisation problems on the LP standard form:
         * <p>
         * min [C]<sup>T</sup>[X] <br>
         * when [AE][X] == [BE] <br>
         * and 0 <= [X] <br>
         * and 0 <= [BE] <br>
         * <p>
         * A Linear Program is in Standard Form if:
         * <ul>
         * <li>All constraints are equality constraints.
         * <li>All variables have a nonnegativity sign restriction.
         * </ul>
         * <p>
         * Further it is required that the constraint right hand sides are nonnegative (nonnegative elements
         * in [BE]). Don't think that's an actual LP standard form requirement, but it is commonly required,
         * and also here.
         * <p>
         * The LP standard form does not dictate if expressed on minimisation or maximisation form. Here it
         * should be a minimisation.
         * <p>
         *
         * @author apete
         */
        public LinearSolver.Builder toStandardForm() {

            int nbInequalites = this.countInequalityConstraints();
            int nbEqualites = this.countEqualityConstraints();
            int nbVariables = this.countVariables();

            LinearSolver.Builder retVal = LinearSolver.newBuilder();

            PhysicalStore<Double> mtrxC = null;

            PhysicalStore<Double> mtrxAE = null;
            PhysicalStore<Double> mtrxBE = null;

            if (nbEqualites > 0) {

                if (nbInequalites > 0) {

                    mtrxC = this.getC().below(nbInequalites).collect(this.getFactory());

                    mtrxAE = this.getAE().below(this.getAI()).right(nbInequalites).collect(this.getFactory());
                    mtrxAE.fillDiagonal(nbEqualites, nbVariables, ONE);

                    mtrxBE = this.getBE().below(this.getBI()).collect(this.getFactory());

                } else {

                    mtrxC = this.getC().collect(this.getFactory());

                    mtrxAE = this.getAE().collect(this.getFactory());

                    mtrxBE = this.getBE().collect(this.getFactory());

                }

            } else if (nbInequalites > 0) {

                mtrxC = this.getC().below(nbInequalites).collect(this.getFactory());

                mtrxAE = this.getAI().right(nbInequalites).collect(this.getFactory());
                mtrxAE.fillDiagonal(nbEqualites, nbVariables, ONE);

                mtrxBE = this.getBI().collect(this.getFactory());

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

        public LinearSolver.Builder upper(final double... bounds) {
            myBoundsModified = true;
            double[] upperBounds = this.getUpperBounds();
            for (int i = 0, limit = Math.min(upperBounds.length, bounds.length); i < limit; i++) {
                upperBounds[i] = bounds[i];
            }
            return this;
        }

        public LinearSolver.Builder upper(final double bound) {
            myBoundsModified = true;
            double[] upperBounds = this.getUpperBounds();
            Arrays.fill(upperBounds, bound);
            return this;
        }

        @Override
        protected LinearSolver doBuild(final Options options) {

            Boolean dualOrPrimal = options.linear().getDualOrPrimal();

            boolean dual = dualOrPrimal != null ? dualOrPrimal.booleanValue() : myBoundsModified;

            if (dual) {

                Function<LinearStructure, SimplexStore> storeFactory = SimplexStore.newStoreFactory(options);
                SimplexStore store = this.newSimplexStore(storeFactory);
                return store.newPhasedSimplexSolver(options);

            } else {

                Function<LinearStructure, SimplexTableau> tableauFactory = SimplexTableau.newTableauFactory(options);
                SimplexTableau tableau = this.newSimplexTableau(tableauFactory);
                return new SimplexTableauSolver(tableau, options);
            }
        }

        protected double[] getLowerBounds() {
            return super.getLowerBounds(ZERO).data;
        }

        protected double[] getUpperBounds() {
            return super.getUpperBounds(POSITIVE_INFINITY).data;
        }

        <S extends SimplexStore> S newSimplexStore(final Function<LinearStructure, S> storeFactory, final int... basis) {

            MatrixStore<Double> builderC = this.getObjective().getLinearFactors(false);
            MatrixStore<Double> builderAE = this.getAE();
            MatrixStore<Double> builderBE = this.getBE();
            MatrixStore<Double> builderAI = this.getAI();
            MatrixStore<Double> builderBI = this.getBI();

            double[] builderLB = this.getLowerBounds();
            double[] builderUB = this.getUpperBounds();

            int nbUpConstr = builderAI.getRowDim();
            int nbLoConstr = 0;
            int nbEqConstr = builderAE.getRowDim();

            int nbProbVars = builderC.size();
            int nbSlckVars = nbUpConstr + nbLoConstr;
            int nbArtiVars = basis.length == nbUpConstr + nbLoConstr + nbEqConstr ? 0 : nbEqConstr;

            LinearStructure structure = new LinearStructure(false, nbUpConstr + nbLoConstr, nbEqConstr, nbProbVars, 0, 0, nbSlckVars);

            S simplex = storeFactory.apply(structure);

            double[] lowerBounds = simplex.getLowerBounds();
            double[] upperBounds = simplex.getUpperBounds();

            Mutate2D mtrxA = simplex.constraintsBody();
            Mutate1D mtrxB = simplex.constraintsRHS();
            Mutate1D mtrxC = simplex.objective();

            for (int i = 0; i < nbUpConstr; i++) {
                for (int j = 0; j < nbProbVars; j++) {
                    double factor = builderAI.doubleValue(i, j);
                    mtrxA.set(i, j, factor);
                }
                mtrxA.set(i, nbProbVars + i, ONE);
                mtrxB.set(i, builderBI.doubleValue(i));
                lowerBounds[nbProbVars + i] = ZERO;
                upperBounds[nbProbVars + i] = POSITIVE_INFINITY;
            }

            for (int i = 0; i < nbEqConstr; i++) {
                for (int j = 0; j < nbProbVars; j++) {
                    double factor = builderAE.doubleValue(i, j);
                    mtrxA.set(nbUpConstr + nbLoConstr + i, j, factor);
                }
                mtrxB.set(nbUpConstr + nbLoConstr + i, builderBE.doubleValue(i));
            }

            for (int j = 0; j < nbProbVars; j++) {
                lowerBounds[j] = builderLB[j];
                upperBounds[j] = builderUB[j];
                double weight = builderC.doubleValue(j);
                mtrxC.set(j, weight);
            }

            for (int j = 0; j < nbArtiVars; j++) {
                mtrxA.set(nbUpConstr + nbLoConstr + j, nbProbVars + nbSlckVars + j, ONE);
                lowerBounds[nbProbVars + nbSlckVars + j] = ZERO;
                upperBounds[nbProbVars + nbSlckVars + j] = ZERO;
            }

            return simplex;
        }

        SimplexTableau newSimplexTableau(final Function<LinearStructure, SimplexTableau> tableauFactory) {

            int nbVars = this.countVariables();
            int nbEqus = this.countEqualityConstraints();
            int nbInes = this.countInequalityConstraints();

            IndexSelector ineqSign = new IndexSelector(nbInes);

            if (nbInes > 0) {

                MatrixStore<Double> mtrxBI = this.getBI();

                for (int i = 0; i < nbInes; i++) {
                    double valRHS = mtrxBI.doubleValue(i);
                    if (valRHS < ZERO) {
                        ineqSign.exclude(i);
                    } else {
                        ineqSign.include(i);
                    }
                }
            }

            int nbIdentSlackVars = ineqSign.countIncluded();
            int nbOtherSlackVars = ineqSign.countExcluded();

            LinearStructure structure = new LinearStructure(false, nbInes, nbEqus, nbVars, 0, nbOtherSlackVars, nbIdentSlackVars);

            SimplexTableau tableau = tableauFactory.apply(structure);
            Primitive2D constraintsBody = tableau.constraintsBody();
            Primitive1D constraintsRHS = tableau.constraintsRHS();
            Primitive1D objective = tableau.objective();

            if (nbInes > 0) {

                int insIdSlack = 0;
                int insGnSlack = 0;

                for (int i = 0; i < nbInes; i++) {

                    SparseArray<Double> body = this.getAI(i);
                    double valRHS = this.getBI(i);
                    boolean positive = ineqSign.isIncluded(i);

                    int row = positive ? insIdSlack : nbIdentSlackVars + insGnSlack;
                    int col = positive ? nbVars + nbOtherSlackVars + insIdSlack++ : nbVars + insGnSlack++;

                    for (NonzeroView<Double> nz : body.nonzeros()) {
                        constraintsBody.set(row, nz.index(), positive ? nz.doubleValue() : -nz.doubleValue());
                    }

                    constraintsBody.set(row, col, positive ? ONE : NEG);

                    constraintsRHS.set(row, positive ? valRHS : -valRHS);

                    structure.negated(row, !positive);
                }
            }

            if (nbEqus > 0) {

                MatrixStore<Double> mtrxAE = this.getAE();
                MatrixStore<Double> mtrxBE = this.getBE();

                for (int i = 0; i < nbEqus; i++) {

                    double valRHS = mtrxBE.doubleValue(i);
                    boolean positive = valRHS >= ZERO;

                    int row = nbInes + i;

                    for (int j = 0; j < nbVars; j++) {
                        double value = mtrxAE.doubleValue(i, j);
                        if (Math.abs(value) > MACHINE_EPSILON) {
                            constraintsBody.set(row, j, positive ? value : -value);
                        }
                    }

                    constraintsRHS.set(row, positive ? valRHS : -valRHS);

                    structure.negated(row, !positive);
                }
            }

            MatrixStore<Double> mtrxC = this.getC();

            for (int i = 0; i < nbVars; i++) {
                objective.set(i, mtrxC.doubleValue(i));
            }

            return tableau;
        }

    }

    public static final class Configuration {

        private Boolean myDualOrPrimal = null;

        /**
         * Force use of the newer (mainly) dual simplex implementation. If you don't specify which to use,
         * there is internal logic that switches implementation based on problem size.
         */
        public Configuration dual() {
            myDualOrPrimal = Boolean.TRUE;
            return this;
        }

        /**
         * Force use of ojAlgo's original (classic 2-phase primal) simplex implementation.
         *
         * @see #dual()
         */
        public Configuration primal() {
            myDualOrPrimal = Boolean.FALSE;
            return this;
        }

        /**
         * TRUE for dual, FALSE for primal and null means "let the solver decide".
         */
        Boolean getDualOrPrimal() {
            return myDualOrPrimal;
        }

    }

    /**
     * Bridges {@link ExpressionsBasedModel} and the LP simplex solvers. This integration translates a
     * high-level model into the internal simplex representation and maps solver results back to model
     * variables. It is registered by default (via {@link #INTEGRATION}) so that {@link ExpressionsBasedModel}
     * can automatically delegate LP problems to {@link LinearSolver}.
     * <p>
     * Internally delegates to either the older tableau-based primal simplex or the newer revised dual
     * simplex, depending on the {@link Configuration} and problem characteristics.
     */
    public static final class ModelIntegration extends ExpressionsBasedModel.Integration<LinearSolver> {

        @Override
        public LinearSolver build(final ExpressionsBasedModel model) {

            boolean newerDualSolver = true;
            if (Boolean.FALSE.equals(model.options.linear().getDualOrPrimal())) {
                newerDualSolver = false;
            }

            ExpressionsBasedModel.Integration.setSwitch(model, ExpressionsBasedModel.IntegrationProperty.PRIMAL_OR_DUAL_LP, newerDualSolver);

            if (newerDualSolver) {
                return NEWER_DUAL_SOLVER.build(model);
            } else {
                return OLDER_PRIMAL_SOLVER.build(model);
            }
        }

        @Override
        public boolean isCapable(final ExpressionsBasedModel model) {
            return OLDER_PRIMAL_SOLVER.isCapable(model) || NEWER_DUAL_SOLVER.isCapable(model);
        }

        @Override
        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {
            if (ExpressionsBasedModel.Integration.isSwitch(model, ExpressionsBasedModel.IntegrationProperty.PRIMAL_OR_DUAL_LP)) {
                return NEWER_DUAL_SOLVER.toModelState(solverState, model);
            } else {
                return OLDER_PRIMAL_SOLVER.toModelState(solverState, model);
            }
        }

        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {
            if (ExpressionsBasedModel.Integration.isSwitch(model, ExpressionsBasedModel.IntegrationProperty.PRIMAL_OR_DUAL_LP)) {
                return NEWER_DUAL_SOLVER.toSolverState(modelState, model);
            } else {
                return OLDER_PRIMAL_SOLVER.toSolverState(modelState, model);
            }
        }

        @Override
        protected int getIndexInSolver(final ExpressionsBasedModel model, final Variable variable) {
            if (ExpressionsBasedModel.Integration.isSwitch(model, ExpressionsBasedModel.IntegrationProperty.PRIMAL_OR_DUAL_LP)) {
                return NEWER_DUAL_SOLVER.getIndexInSolver(model, variable);
            } else {
                return OLDER_PRIMAL_SOLVER.getIndexInSolver(model, variable);
            }
        }

    }

    /**
     * An integration to a new/alternative/experimental LP-solver. That solver is intended to replace the
     * current solver, but is not yet ready to do that. You're welcome to try it - just add this integration
     * by calling {@link ExpressionsBasedModel#addIntegration(ExpressionsBasedModel.Integration)}.
     */
    static final class NewIntegration extends ExpressionsBasedModel.Integration<SimplexSolver> {

        @Override
        public SimplexSolver build(final ExpressionsBasedModel model) {

            Options options = model.options;

            Function<LinearStructure, SimplexStore> storeFactory = SimplexStore.newStoreFactory(options);

            return SimplexSolver.build(model, storeFactory).newPhasedSimplexSolver(options);
        }

        @Override
        public boolean isCapable(final ExpressionsBasedModel model) {
            return !model.isAnyVariableInteger() && !model.isAnyExpressionQuadratic();
        }

        @Override
        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {
            return ExpressionsBasedModel.Integration.expandFreeToFull(solverState, model, ArrayR064.FACTORY, solverState.getReducedGradient());
        }

        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {
            return ExpressionsBasedModel.Integration.reduceFullToFree(modelState, model, ArrayR064.FACTORY);
        }

        @Override
        protected int getIndexInSolver(final ExpressionsBasedModel model, final Variable variable) {
            return ExpressionsBasedModel.Integration.getIndexOfFreeInSolver(model, variable);
        }

    }

    static final class OldIntegration extends ExpressionsBasedModel.Integration<SimplexTableauSolver> {

        private static ArrayR064 toModelVariableValues(final Access1D<?> solverVariableValues, final ExpressionsBasedModel model,
                final ArrayR064 modelVariableValues) {

            List<Variable> positiveVariables = model.getPositiveVariables();
            for (int p = 0; p < positiveVariables.size(); p++) {
                Variable variable = positiveVariables.get(p);
                int index = model.indexOf(variable);
                modelVariableValues.set(index, solverVariableValues.doubleValue(p));
            }

            List<Variable> negativeVariables = model.getNegativeVariables();
            for (int n = 0; n < negativeVariables.size(); n++) {
                Variable variable = negativeVariables.get(n);
                int index = model.indexOf(variable);
                modelVariableValues.add(index, -solverVariableValues.doubleValue(positiveVariables.size() + n));
            }

            return modelVariableValues;
        }

        @Override
        public SimplexTableauSolver build(final ExpressionsBasedModel model) {

            Options options = model.options;

            Function<LinearStructure, SimplexTableau> tableauFactory = SimplexTableau.newTableauFactory(options);

            SimplexTableau tableau = SimplexTableauSolver.build(model, tableauFactory);

            return new SimplexTableauSolver(tableau, options);
        }

        @Override
        public boolean isCapable(final ExpressionsBasedModel model) {
            return !model.isAnyVariableInteger() && !model.isAnyExpressionQuadratic();
        }

        @Override
        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {

            ArrayR064 modelSolution = ArrayR064.make(model.countVariables());

            for (IntIndex fixed : model.getFixedVariables()) {
                modelSolution.set(fixed.index, model.getVariable(fixed.index).getValue().doubleValue());
            }

            OldIntegration.toModelVariableValues(solverState, model, modelSolution);

            return solverState.withSolution(modelSolution);
        }

        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {

            List<Variable> tmpPositives = model.getPositiveVariables();
            List<Variable> tmpNegatives = model.getNegativeVariables();

            int tmpCountPositives = tmpPositives.size();
            int tmpCountNegatives = tmpNegatives.size();

            ArrayR064 solverSolution = ArrayR064.make(tmpCountPositives + tmpCountNegatives);

            for (int p = 0; p < tmpCountPositives; p++) {
                Variable tmpVariable = tmpPositives.get(p);
                int tmpIndex = model.indexOf(tmpVariable);
                solverSolution.set(p, MAX.invoke(modelState.doubleValue(tmpIndex), ZERO));
            }

            for (int n = 0; n < tmpCountNegatives; n++) {
                Variable tmpVariable = tmpNegatives.get(n);
                int tmpIndex = model.indexOf(tmpVariable);
                solverSolution.set(tmpCountPositives + n, MAX.invoke(-modelState.doubleValue(tmpIndex), ZERO));
            }

            return modelState.withSolution(solverSolution);
        }

        @Override
        protected int getIndexInSolver(final ExpressionsBasedModel model, final Variable variable) {

            int retVal = -1;

            BigDecimal value = variable.getValue();

            if ((value != null && value.signum() >= 0 || variable.isPositive()) && (retVal = model.indexOfPositiveVariable(variable)) >= 0) {
                return retVal;
            }

            if ((value != null && value.signum() <= 0 || variable.isNegative()) && (retVal = model.indexOfNegativeVariable(variable)) >= 0) {
                retVal += model.getPositiveVariables().size();
                return retVal;
            }

            return -1;
        }

    }

    /**
     * The default {@link ExpressionsBasedModel.Integration} that allows {@link ExpressionsBasedModel} to
     * solve LP problems using {@link LinearSolver}. Registered automatically — users do not normally need to
     * reference this directly.
     */
    public static final ExpressionsBasedModel.Integration<LinearSolver> INTEGRATION = new ModelIntegration();

    static final NewIntegration NEWER_DUAL_SOLVER = new NewIntegration();

    /**
     * An integration to a old/classic/primal LP-solver.
     */
    static final OldIntegration OLDER_PRIMAL_SOLVER = new OldIntegration();

    public static LinearSolver.Builder newBuilder() {
        return new LinearSolver.Builder();
    }

    public static LinearSolver.Builder newBuilder(final double... objective) {
        return LinearSolver.newBuilder().objective(objective);
    }

    /**
     * @deprecated v55 Use {@link #newBuilder()} instead
     */
    @Deprecated
    public static LinearSolver.Builder newGeneralBuilder() {
        return LinearSolver.newBuilder();
    }

    /**
     * @deprecated v55 Use {@link #newBuilder(double...)} instead
     */
    @Deprecated
    public static LinearSolver.Builder newGeneralBuilder(final double... objective) {
        return LinearSolver.newBuilder(objective);
    }

    /**
     * Use {@link ModelIntegration#build(ExpressionsBasedModel)} directly instead, and then remember to also
     * use {@link ModelIntegration#toModelState(Optimisation.Result, ExpressionsBasedModel)} and
     * {@link ModelIntegration#toSolverState(Optimisation.Result, ExpressionsBasedModel)}.
     *
     * @deprecated v57 Use {@link #INTEGRATION} instead.
     */
    @Deprecated
    public static LinearSolver newSolver(final ExpressionsBasedModel model) {
        return INTEGRATION.build(model);
    }

    /**
     * @deprecated v55 Use {@link #newBuilder()} instead
     */
    @Deprecated
    public static LinearSolver.Builder newStandardBuilder() {
        return LinearSolver.newBuilder();
    }

    /**
     * @deprecated v55 Use {@link #newBuilder(double...)} instead
     */
    @Deprecated
    public static LinearSolver.Builder newStandardBuilder(final double... objective) {
        return LinearSolver.newBuilder(objective);
    }

    public static Optimisation.Result solve(final ConvexData convex, final Optimisation.Options options, final boolean zeroC) {

        boolean store = false;

        if (store) {

            int dualSize = SimplexSolver.sizeOfDual(convex);
            int primSize = SimplexSolver.sizeOfPrimal(convex);
            boolean dual = dualSize <= primSize;

            return dual ? SimplexSolver.doSolveConvexAsDual(convex, options, zeroC) : SimplexSolver.doSolveConvexAsPrimal(convex, options, zeroC);

        } else {

            int dualSize = SimplexTableauSolver.sizeOfDual(convex);
            int primSize = SimplexTableauSolver.sizeOfPrimal(convex);
            boolean dual = dualSize <= primSize;

            return dual ? SimplexTableauSolver.doSolveConvexAsDual(convex, options, zeroC) : SimplexTableauSolver.doSolveConvexAsPrimal(convex, options, zeroC);
        }
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

    private transient double[] myCachedDualMultiplier = null;
    private transient double[] myCachedReducedGradient = null;

    protected LinearSolver(final Options solverOptions) {
        super(solverOptions);
    }

    @Override
    public final double getDualMultiplier(final int index) {
        if (myCachedDualMultiplier == null) {
            myCachedDualMultiplier = this.extractDualMultipliers();
        }
        return myCachedDualMultiplier[index];
    }

    @Override
    public final double getReducedGradient(final int index) {
        if (myCachedReducedGradient == null) {
            myCachedReducedGradient = this.extractReducedGradients();
        }
        return myCachedReducedGradient[index];
    }

    /**
     * Extract the full array of dual multipliers (Lagrange multipliers) for all constraints, in original
     * (un-negated) sign convention.
     */
    abstract double[] extractDualMultipliers();

    /**
     * Extract the full array of reduced gradients for all variables.
     */
    abstract double[] extractReducedGradients();

    void invalidateCache() {
        myCachedDualMultiplier = null;
        myCachedReducedGradient = null;
    }

}
