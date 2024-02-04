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
package org.ojalgo.optimisation.linear;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.IndexSelector;

public abstract class LinearSolver extends GenericSolver implements UpdatableSolver {

    public static final class Configuration {

    }

    /**
     * <p>
     * Compared to {@link LinearSolver.StandardBuilder} this builder: <br>
     * 1) Accepts inequality constraints <br>
     * 2) Has relaxed the requiremnt on the RHS to be non-negative (both equalities and inequalities) <br>
     * <p>
     * Compared to {@link ConvexSolver.Builder} this builder: <br>
     * 1) Requires the objective function to be linear (or only the linear factors will be concidered) <br>
     * 2) Assumes (requires) variables to be non-negative <br>
     * <p>
     *
     * @author apete
     */
    public static final class GeneralBuilder extends LinearSolver.Builder<LinearSolver.GeneralBuilder> {

        GeneralBuilder() {
            super();
        }

        @Override
        public LinearSolver.GeneralBuilder inequalities(final Access2D<?> mtrxAI, final Access1D<?> mtrxBI) {
            return super.inequalities(mtrxAI, mtrxBI);
        }

        @Override
        public LinearSolver.GeneralBuilder inequality(final double rhs, final double... factors) {
            return super.inequality(rhs, factors);
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

    }

    public static final class ModelIntegration extends ExpressionsBasedModel.Integration<LinearSolver> {

        @Override
        public LinearSolver build(final ExpressionsBasedModel model) {

            boolean experimental = model.options.experimental;

            this.setSwitch(model, experimental);

            if (experimental) {
                return NEW_INTEGRATION.build(model);
            } else {
                return OLD_INTEGRATION.build(model);
            }
        }

        @Override
        public boolean isCapable(final ExpressionsBasedModel model) {
            return OLD_INTEGRATION.isCapable(model) || NEW_INTEGRATION.isCapable(model);
        }

        @Override
        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {
            if (this.isSwitch(model)) {
                return NEW_INTEGRATION.toModelState(solverState, model);
            } else {
                return OLD_INTEGRATION.toModelState(solverState, model);
            }
        }

        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {
            if (this.isSwitch(model)) {
                return NEW_INTEGRATION.toSolverState(modelState, model);
            } else {
                return OLD_INTEGRATION.toSolverState(modelState, model);
            }
        }

        @Override
        protected int getIndexInSolver(final ExpressionsBasedModel model, final Variable variable) {
            if (this.isSwitch(model)) {
                return NEW_INTEGRATION.getIndexInSolver(model, variable);
            } else {
                return OLD_INTEGRATION.getIndexInSolver(model, variable);
            }
        }

    }

    /**
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
     * Further it is required that the constraint right hand sides are nonnegative (nonnegative elements in
     * [BE]). Don't think that's an actual LP standard form requirement, but it is commonly required, and also
     * here.
     * <p>
     * The LP standard form does not dictate if expressed on minimisation or maximisation form. Here it should
     * be a minimisation.
     * <p>
     *
     * @author apete
     */
    public static final class StandardBuilder extends LinearSolver.Builder<StandardBuilder> {

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

    }

    static abstract class Builder<B extends LinearSolver.Builder<B>> extends GenericSolver.Builder<B, LinearSolver> {

        Builder() {
            super();
        }

        @Override
        public B equalities(final Access2D<?> mtrxAE, final Access1D<?> mtrxBE) {
            return super.equalities(mtrxAE, mtrxBE);
        }

        @Override
        public B equality(final double rhs, final double... factors) {
            return super.equality(rhs, factors);
        }

        @Override
        public final LinearFunction<Double> getObjective() {
            LinearFunction<Double> retVal = this.getObjective(LinearFunction.class);
            if (retVal == null) {
                retVal = LinearFunction.factory(this.getFactory()).make(this.countVariables());
                super.setObjective(retVal);
            }
            return retVal;
        }

        public final B lower(final double... bounds) {
            double[] lowerBounds = this.getLowerBounds(ZERO).data;
            for (int i = 0, limit = Math.min(lowerBounds.length, bounds.length); i < limit; i++) {
                lowerBounds[i] = bounds[i];
            }
            return (B) this;
        }

        public final B lower(final double bound) {
            double[] lowerBounds = this.getLowerBounds(ZERO).data;
            Arrays.fill(lowerBounds, bound);
            return (B) this;
        }

        public final B objective(final double... factors) {
            this.setNumberOfVariables(factors.length);
            this.getObjective().linear().fillMatching(this.getFactory().column(factors));
            return (B) this;
        }

        public final B objective(final int index, final double value) {
            this.getObjective().linear().set(index, value);
            return (B) this;
        }

        public final B objective(final MatrixStore<Double> mtrxC) {
            this.setObjective(LinearSolver.toObjectiveFunction(mtrxC));
            return (B) this;
        }

        public final B upper(final double... bounds) {
            double[] upperBounds = this.getUpperBounds(POSITIVE_INFINITY).data;
            for (int i = 0, limit = Math.min(upperBounds.length, bounds.length); i < limit; i++) {
                upperBounds[i] = bounds[i];
            }
            return (B) this;
        }

        public final B upper(final double bound) {
            double[] upperBounds = this.getUpperBounds(POSITIVE_INFINITY).data;
            Arrays.fill(upperBounds, bound);
            return (B) this;
        }

        @Override
        protected LinearSolver doBuild(final Options options) {

            int nbInequalites = this.countInequalityConstraints();
            int nbEqualites = this.countEqualityConstraints();
            int nbVariables = this.countVariables();

            IndexSelector ineqSign = new IndexSelector(nbInequalites);

            if (nbInequalites > 0) {

                MatrixStore<Double> mtrxBI = this.getBI();

                for (int i = 0; i < nbInequalites; i++) {
                    double valRHS = mtrxBI.doubleValue(i);
                    if (valRHS < ZERO) {
                        ineqSign.exclude(i);
                    } else {
                        ineqSign.include(i);
                    }
                }
            }

            int nbIdentitySlackVariables = ineqSign.countIncluded();
            int nbSlackVariables = ineqSign.countExcluded();

            LinearStructure structure = new LinearStructure(false, nbInequalites, nbEqualites, nbVariables, 0, nbSlackVariables, nbIdentitySlackVariables);

            SimplexTableau tableau = SimplexTableau.make(structure, options);
            Primitive2D constraintsBody = tableau.constraintsBody();
            Primitive1D constraintsRHS = tableau.constraintsRHS();
            Primitive1D objective = tableau.objective();

            if (nbInequalites > 0) {

                int insIdSlack = 0;
                int insGnSlack = 0;

                for (int i = 0; i < nbInequalites; i++) {

                    SparseArray<Double> body = this.getAI(i);
                    double valRHS = this.getBI(i);
                    boolean positive = ineqSign.isIncluded(i);

                    int row = positive ? insIdSlack : nbIdentitySlackVariables + insGnSlack;
                    int col = positive ? nbVariables + nbSlackVariables + insIdSlack++ : nbVariables + insGnSlack++;

                    for (NonzeroView<Double> nz : body.nonzeros()) {
                        constraintsBody.set(row, nz.index(), positive ? nz.doubleValue() : -nz.doubleValue());
                    }

                    constraintsBody.set(row, col, positive ? ONE : NEG);

                    constraintsRHS.set(row, positive ? valRHS : -valRHS);

                    structure.negated(row, !positive);
                }
            }

            if (nbEqualites > 0) {

                MatrixStore<Double> mtrxAE = this.getAE();
                MatrixStore<Double> mtrxBE = this.getBE();

                for (int i = 0; i < nbEqualites; i++) {

                    double valRHS = mtrxBE.doubleValue(i);
                    boolean positive = valRHS >= ZERO;

                    int row = nbInequalites + i;

                    for (int j = 0; j < nbVariables; j++) {
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

            for (int i = 0; i < nbVariables; i++) {
                objective.set(i, mtrxC.doubleValue(i));
            }

            return new SimplexTableauSolver(tableau, options);
        }

        protected final double[] getLowerBounds() {
            return super.getLowerBounds(ZERO).data;
        }

        protected final double[] getUpperBounds() {
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

        <T extends SimplexTableau> T newSimplexTableau(final Function<LinearStructure, T> tableauFactory) {

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

            T tableau = tableauFactory.apply(structure);
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
                }
            }

            MatrixStore<Double> mtrxC = this.getC();

            for (int i = 0; i < nbVars; i++) {
                objective.set(i, mtrxC.doubleValue(i));
            }

            return tableau;
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

            PhasedSimplexSolver solver = SimplexStore.build(model, structure -> {
                if (Boolean.TRUE.equals(model.options.sparse)) {
                    return new RevisedStore(structure);
                } else if (Boolean.FALSE.equals(model.options.sparse)) {
                    return new TableauStore(structure);
                } else {
                    return SimplexStore.newInstance(structure);
                }
            }).newPhasedSimplexSolver(model.options);

            if (model.options.validate) {
                solver.setValidator(this.newValidator(model));
            }

            return solver;
        }

        @Override
        public boolean isCapable(final ExpressionsBasedModel model) {
            return !model.isAnyVariableInteger() && !model.isAnyExpressionQuadratic();
        }

        @Override
        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {

            List<Variable> freeVariables = model.getFreeVariables();
            Set<IntIndex> fixedVariables = model.getFixedVariables();
            int nbFreeVars = freeVariables.size();
            int nbModelVars = model.countVariables();

            ArrayR064 modelSolution = ArrayR064.make(nbModelVars);

            for (int i = 0; i < nbFreeVars; i++) {
                modelSolution.set(model.indexOf(freeVariables.get(i)), solverState.doubleValue(i));
            }

            for (IntIndex fixed : fixedVariables) {
                modelSolution.set(fixed.index, model.getVariable(fixed.index).getValue());
            }

            return solverState.withSolution(modelSolution);
        }

        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {

            List<Variable> freeVariables = model.getFreeVariables();
            int nbFreeVars = freeVariables.size();

            ArrayR064 solverSolution = ArrayR064.make(nbFreeVars);

            for (int i = 0; i < nbFreeVars; i++) {
                Variable variable = freeVariables.get(i);
                int modelIndex = model.indexOf(variable);
                solverSolution.set(i, modelState.doubleValue(modelIndex));
            }

            return modelState.withSolution(solverSolution);
        }

        @Override
        protected int getIndexInSolver(final ExpressionsBasedModel model, final Variable variable) {
            return super.getIndexInSolver(model, variable);
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

        public SimplexTableauSolver build(final ConvexData convexBuilder, final Optimisation.Options options) {

            SimplexTableau tableau = SimplexTableauSolver.buildPrimal(convexBuilder, options, false);

            return new SimplexTableauSolver(tableau, options);
        }

        @Override
        public SimplexTableauSolver build(final ExpressionsBasedModel model) {

            SimplexTableau tableau = SimplexTableauSolver.build(model);

            SimplexTableauSolver solver = new SimplexTableauSolver(tableau, model.options);

            if (model.options.validate) {
                solver.setValidator(this.newValidator(model));
            }

            return solver;
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

    public static final ExpressionsBasedModel.Integration<LinearSolver> INTEGRATION = new ModelIntegration();

    /**
     * An integration to a new/alternative/experimental LP-solver. This solver is intended to replace the
     * current solver, but is not yet ready to do that. You're welcome to try it - just add this integration
     * by calling {@link ExpressionsBasedModel#addIntegration(ExpressionsBasedModel.Integration)}.
     * <p>
     * With the next major release this solver/integration is expected to be the default, and there will no
     * longer be an integration constant named "NEW_INTEGRATION". Possibly there will instead be one named
     * "OLD_INTEGRATION".
     */
    static final NewIntegration NEW_INTEGRATION = new NewIntegration();

    static final OldIntegration OLD_INTEGRATION = new OldIntegration();

    public static LinearSolver.GeneralBuilder newGeneralBuilder() {
        return new LinearSolver.GeneralBuilder();
    }

    public static LinearSolver.GeneralBuilder newGeneralBuilder(final double... objective) {
        return LinearSolver.newGeneralBuilder().objective(objective);
    }

    public static LinearSolver newSolver(final ExpressionsBasedModel model) {

        SimplexTableau tableau = SimplexTableauSolver.build(model);

        return new SimplexTableauSolver(tableau, model.options);
    }

    public static LinearSolver.StandardBuilder newStandardBuilder() {
        return new LinearSolver.StandardBuilder();
    }

    public static LinearSolver.StandardBuilder newStandardBuilder(final double... objective) {
        return LinearSolver.newStandardBuilder().objective(objective);
    }

    public static Optimisation.Result solve(final ConvexData convex, final Optimisation.Options options, final boolean zeroC) {

        int dualSize = SimplexTableauSolver.sizeOfDual(convex);
        int primSize = SimplexTableauSolver.sizeOfPrimal(convex);
        boolean dual = dualSize <= primSize;

        return dual ? SimplexTableauSolver.doSolveDual(convex, options, zeroC) : SimplexTableauSolver.doSolvePrimal(convex, options, zeroC);
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

}
