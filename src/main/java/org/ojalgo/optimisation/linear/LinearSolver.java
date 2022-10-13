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
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.optimisation.linear.SimplexTableauSolver.Primitive1D;
import org.ojalgo.optimisation.linear.SimplexTableauSolver.Primitive2D;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.IndexSelector;

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

            SimplexTableau tableau = SimplexTableau.make(this, options);

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
            int nbProblemVariables = nbVariables;
            int nbConstraints = nbEqualites + nbInequalites;
            boolean needDual = true;

            SimplexTableau tableau = SimplexTableau.make(nbConstraints, nbProblemVariables, 0, nbSlackVariables, nbIdentitySlackVariables, needDual, options);
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
                    int col = positive ? nbProblemVariables + nbSlackVariables + insIdSlack++ : nbProblemVariables + insGnSlack++;

                    for (NonzeroView<Double> nz : body.nonzeros()) {
                        constraintsBody.set(row, nz.index(), positive ? nz.doubleValue() : -nz.doubleValue());
                    }

                    constraintsBody.set(row, col, positive ? ONE : NEG);

                    constraintsRHS.set(row, positive ? valRHS : -valRHS);
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
                }
            }

            MatrixStore<Double> mtrxC = this.getC();

            for (int i = 0; i < nbVariables; i++) {
                objective.set(i, mtrxC.doubleValue(i));
            }

            return new PrimalSimplex(tableau, options);
        }

    }

    public static final class ModelIntegration extends ExpressionsBasedModel.Integration<LinearSolver> {

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

        public LinearSolver build(final ConvexSolver.Builder convexBuilder, final Optimisation.Options options) {

            SimplexTableau tableau = PrimalSimplex.build(convexBuilder, options, false);

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

            ArrayR064 modelSolution = ArrayR064.make(model.countVariables());

            for (IntIndex fixed : model.getFixedVariables()) {
                modelSolution.set(fixed.index, model.getVariable(fixed.index).getValue().doubleValue());
            }

            ModelIntegration.toModelVariableValues(solverState, model, modelSolution);

            return new Result(solverState.getState(), solverState.getValue(), modelSolution);
        }

        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {

            List<Variable> tmpPositives = model.getPositiveVariables();
            List<Variable> tmpNegatives = model.getNegativeVariables();

            int tmpCountPositives = tmpPositives.size();
            int tmpCountNegatives = tmpNegatives.size();

            ArrayR064 tmpSolverSolution = ArrayR064.make(tmpCountPositives + tmpCountNegatives);

            for (int p = 0; p < tmpCountPositives; p++) {
                Variable tmpVariable = tmpPositives.get(p);
                int tmpIndex = model.indexOf(tmpVariable);
                tmpSolverSolution.set(p, MAX.invoke(modelState.doubleValue(tmpIndex), ZERO));
            }

            for (int n = 0; n < tmpCountNegatives; n++) {
                Variable tmpVariable = tmpNegatives.get(n);
                int tmpIndex = model.indexOf(tmpVariable);
                tmpSolverSolution.set(tmpCountPositives + n, MAX.invoke(-modelState.doubleValue(tmpIndex), ZERO));
            }

            return new Result(modelState.getState(), modelState.getValue(), tmpSolverSolution);
        }

        @Override
        protected int getIndexInSolver(final ExpressionsBasedModel model, final Variable variable) {

            int retVal = -1;

            BigDecimal value = variable.getValue();

            if ((value != null && value.signum() >= 0 || variable.isPositive()) && (retVal = model.indexOfPositiveVariable(variable)) >= 0) {
                return retVal;
            }

            if (((value != null && value.signum() <= 0) || variable.isNegative()) && (retVal = model.indexOfNegativeVariable(variable)) >= 0) {
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
     * Further it is required that the constraint right hand sides are nonnegative (nonnegative elements in
     * [BE]). Don't think that's an actual LP standard form requirement, but it is commonly required, and also
     * here.
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

        return dual ? DualSimplex.doSolve(convex, options, zeroC) : PrimalSimplex.doSolve(convex, options, zeroC);
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
