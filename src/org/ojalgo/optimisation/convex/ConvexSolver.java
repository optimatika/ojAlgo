/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

import static org.ojalgo.function.PrimitiveFunction.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.ojalgo.array.Array1D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.optimisation.BaseSolver;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.Expression.Index;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelEntity;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.IndexSelector;

/**
 * QuadraticSolver solves optimisation problems of the form:
 * <p>
 * min 1/2 [X]<sup>T</sup>[Q][X] - [C]<sup>T</sup>[X]<br>
 * when [AE][X] == [BE]<br>
 * and [AI][X] <= [BI]
 * </p>
 * <p>
 * The matrix [Q] is assumed to be symmetric (it must be made that way) and:
 * <ul>
 * <li>If [Q] is positive semidefinite, then the objective function is convex: In this case the quadratic program has a
 * global minimizer if there exists some feasible vector [X] (satisfying the constraints) and if the objective function
 * is bounded below on the feasible region.</li>
 * <li>If [Q] is positive definite and the problem has a feasible solution, then the global minimizer is unique.</li>
 * </ul>
 * </p>
 * <p>
 * You construct instances by using the {@linkplain Builder} class. It will return an appropriate subclass for you. It's
 * recommended that you first create a {@linkplain ExpressionsBasedModel} and feed that to the {@linkplain Builder}.
 * Alternatively you can directly call {@linkplain ExpressionsBasedModel#getDefaultSolver()} or even
 * {@linkplain ExpressionsBasedModel#minimise()} or {@linkplain ExpressionsBasedModel#maximise()} on the model.
 * </p>
 *
 * @author apete
 */
public abstract class ConvexSolver extends BaseSolver {

    public static final class Builder extends AbstractBuilder<ConvexSolver.Builder, ConvexSolver> {

        public Builder(final MatrixStore<Double> Q, final MatrixStore<Double> C) {
            super(Q, C);
        }

        Builder() {
            super();
        }

        Builder(final ConvexSolver.Builder matrices) {
            super(matrices);
        }

        Builder(final ExpressionsBasedModel aModel) {

            super(aModel);

            ConvexSolver.copy(aModel, this);
        }

        Builder(final MatrixStore<Double> C) {
            super(C);
        }

        Builder(final MatrixStore<Double>[] aMtrxArr) {
            super(aMtrxArr);
        }

        @Override
        public ConvexSolver build(final Optimisation.Options options) {

            this.validate();

            final ExpressionsBasedModel tmpModel = this.getModel();

            if (this.hasInequalityConstraints()) {
                return new ActiveSetSolver(tmpModel, options, this);
            } else if (this.hasEqualityConstraints()) {
                //return new LagrangeSolver2(tmpModel, options, this);
                return new LagrangeSolver(tmpModel, options, this);
                //return new NullspaceSolver(tmpModel, options, this);
            } else {
                return new UnconstrainedSolver(tmpModel, options, this);
            }

        }

        @Override
        public ConvexSolver.Builder equalities(final MatrixStore<Double> AE, final MatrixStore<Double> BE) {
            return super.equalities(AE, BE);
        }

        @Override
        public ConvexSolver.Builder inequalities(final MatrixStore<Double> AI, final MatrixStore<Double> BI) {
            return super.inequalities(AI, BI);
        }

        @Override
        public ConvexSolver.Builder inequalities(final MatrixStore<Double> AI, final MatrixStore<Double> BI, final ModelEntity<?>[] originatingEntities) {
            return super.inequalities(AI, BI, originatingEntities);
        }

        @Override
        protected Builder objective(final MatrixStore<Double> Q, final MatrixStore<Double> C) {
            return super.objective(Q, C);
        }

    }

    static final PhysicalStore.Factory<Double, PrimitiveDenseStore> FACTORY = PrimitiveDenseStore.FACTORY;

    public static ConvexSolver make(final ExpressionsBasedModel aModel) {

        final ConvexSolver.Builder tmpBuilder = new ConvexSolver.Builder(aModel);

        return tmpBuilder.build();
    }

    @SuppressWarnings("unchecked")
    static void copy(final ExpressionsBasedModel sourceModel, final ConvexSolver.Builder destinationBuilder) {

        final List<Variable> tmpFreeVariables = sourceModel.getFreeVariables();
        final Set<Index> tmpFixedVariables = sourceModel.getFixedVariables();
        final int tmpFreeVarDim = tmpFreeVariables.size();

        final Array1D<Double> tmpCurrentSolution = Array1D.PRIMITIVE.makeZero(tmpFreeVarDim);
        for (int i = 0; i < tmpFreeVariables.size(); i++) {
            final BigDecimal tmpValue = tmpFreeVariables.get(i).getValue();
            if (tmpValue != null) {
                tmpCurrentSolution.set(i, tmpValue.doubleValue());
            }
        }
        final Optimisation.Result tmpKickStarter = new Optimisation.Result(Optimisation.State.UNEXPLORED, Double.NaN, tmpCurrentSolution);

        // AE & BE

        final List<Expression> tmpEqExpr = sourceModel.selectExpressionsLinearEquality();
        final int tmpEqExprDim = tmpEqExpr.size();

        if (tmpEqExprDim > 0) {

            final PhysicalStore<Double> tmpAE = FACTORY.makeZero(tmpEqExprDim, tmpFreeVarDim);
            final PhysicalStore<Double> tmpBE = FACTORY.makeZero(tmpEqExprDim, 1);

            for (int i = 0; i < tmpEqExprDim; i++) {

                final Expression tmpExpression = tmpEqExpr.get(i);

                for (final Expression.Index tmpKey : tmpExpression.getLinearFactorKeys()) {
                    final int tmpIndex = sourceModel.indexOfFreeVariable(tmpKey.index);
                    if (tmpIndex >= 0) {
                        tmpAE.set(i, tmpIndex, tmpExpression.getAdjustedLinearFactor(tmpKey));
                    }
                }
                tmpBE.set(i, 0, tmpExpression.getCompensatedUpperLimit(tmpFixedVariables));
            }

            destinationBuilder.equalities(tmpAE, tmpBE);
        }

        // Q & C

        final Expression tmpObjExpr = sourceModel.getObjectiveExpression();

        PhysicalStore<Double> tmpQ = null;
        if (tmpObjExpr.isAnyQuadraticFactorNonZero()) {
            tmpQ = FACTORY.makeZero(tmpFreeVarDim, tmpFreeVarDim);

            final BinaryFunction<Double> tmpBaseFunc = sourceModel.isMaximisation() ? SUBTRACT : ADD;
            UnaryFunction<Double> tmpModifier;
            for (final Expression.RowColumn tmpKey : tmpObjExpr.getQuadraticFactorKeys()) {
                final int tmpRow = sourceModel.indexOfFreeVariable(tmpKey.row);
                final int tmpColumn = sourceModel.indexOfFreeVariable(tmpKey.column);
                if ((tmpRow >= 0) && (tmpColumn >= 0)) {
                    tmpModifier = tmpBaseFunc.second(tmpObjExpr.getAdjustedQuadraticFactor(tmpKey));
                    tmpQ.modifyOne(tmpRow, tmpColumn, tmpModifier);
                    tmpQ.modifyOne(tmpColumn, tmpRow, tmpModifier);
                }
            }
        }

        PhysicalStore<Double> tmpC = null;
        if (tmpObjExpr.isAnyLinearFactorNonZero()) {
            tmpC = FACTORY.makeZero(tmpFreeVarDim, 1);
            if (sourceModel.isMinimisation()) {
                for (final Expression.Index tmpKey : tmpObjExpr.getLinearFactorKeys()) {
                    final int tmpIndex = sourceModel.indexOfFreeVariable(tmpKey.index);
                    if (tmpIndex >= 0) {
                        tmpC.set(tmpIndex, 0, -tmpObjExpr.getAdjustedLinearFactor(tmpKey));
                    }
                }
            } else {
                for (final Expression.Index tmpKey : tmpObjExpr.getLinearFactorKeys()) {
                    final int tmpIndex = sourceModel.indexOfFreeVariable(tmpKey.index);
                    if (tmpIndex >= 0) {
                        tmpC.set(tmpIndex, 0, tmpObjExpr.getAdjustedLinearFactor(tmpKey));
                    }
                }
            }
        }

        destinationBuilder.objective(tmpQ, tmpC);

        // AI & BI

        final List<Expression> tmpUpExpr = sourceModel.selectExpressionsLinearUpper();
        final int tmpUpExprDim = tmpUpExpr.size();
        final List<Variable> tmpUpVar = sourceModel.selectVariablesFreeUpper();
        final int tmpUpVarDim = tmpUpVar.size();

        final List<Expression> tmpLoExpr = sourceModel.selectExpressionsLinearLower();
        final int tmpLoExprDim = tmpLoExpr.size();
        final List<Variable> tmpLoVar = sourceModel.selectVariablesFreeLower();
        final int tmpLoVarDim = tmpLoVar.size();

        if ((tmpUpExprDim + tmpUpVarDim + tmpLoExprDim + tmpLoVarDim) > 0) {

            final ModelEntity<?>[] tmpEntities = new ModelEntity<?>[tmpUpExprDim + tmpUpVarDim + tmpLoExprDim + tmpLoVarDim];

            final PhysicalStore<Double> tmpUAI = FACTORY.makeZero(tmpUpExprDim + tmpUpVarDim, tmpFreeVarDim);
            final PhysicalStore<Double> tmpUBI = FACTORY.makeZero(tmpUpExprDim + tmpUpVarDim, 1);

            if (tmpUpExprDim > 0) {
                for (int i = 0; i < tmpUpExprDim; i++) {
                    final Expression tmpExpression = tmpUpExpr.get(i);
                    for (final Expression.Index tmpKey : tmpExpression.getLinearFactorKeys()) {
                        final int tmpIndex = sourceModel.indexOfFreeVariable(tmpKey.index);
                        if (tmpIndex >= 0) {
                            tmpUAI.set(i, tmpIndex, tmpExpression.getAdjustedLinearFactor(tmpKey));
                        }
                    }
                    tmpUBI.set(i, 0, tmpExpression.getCompensatedUpperLimit(tmpFixedVariables));
                    tmpEntities[i] = tmpExpression;
                }
            }

            if (tmpUpVarDim > 0) {
                for (int i = 0; i < tmpUpVarDim; i++) {
                    final Variable tmpVariable = tmpUpVar.get(i);
                    tmpUAI.set(tmpUpExprDim + i, sourceModel.indexOfFreeVariable(tmpVariable), tmpVariable.getAdjustmentFactor());
                    tmpUBI.set(tmpUpExprDim + i, 0, tmpVariable.getAdjustedUpperLimit());
                    tmpEntities[tmpUpExprDim + i] = tmpVariable;
                }
            }

            final PhysicalStore<Double> tmpLAI = FACTORY.makeZero(tmpLoExprDim + tmpLoVarDim, tmpFreeVarDim);
            final PhysicalStore<Double> tmpLBI = FACTORY.makeZero(tmpLoExprDim + tmpLoVarDim, 1);

            if (tmpLoExprDim > 0) {
                for (int i = 0; i < tmpLoExprDim; i++) {
                    final Expression tmpExpression = tmpLoExpr.get(i);
                    for (final Expression.Index tmpKey : tmpExpression.getLinearFactorKeys()) {
                        final int tmpIndex = sourceModel.indexOfFreeVariable(tmpKey.index);
                        if (tmpIndex >= 0) {
                            tmpLAI.set(i, tmpIndex, -tmpExpression.getAdjustedLinearFactor(tmpKey));
                        }
                    }
                    tmpLBI.set(i, 0, -tmpExpression.getCompensatedLowerLimit(tmpFixedVariables));
                    tmpEntities[tmpUpExprDim + tmpUpVarDim + i] = tmpExpression;
                }
            }

            if (tmpLoVarDim > 0) {
                for (int i = 0; i < tmpLoVarDim; i++) {
                    final Variable tmpVariable = tmpLoVar.get(i);
                    tmpLAI.set(tmpLoExprDim + i, sourceModel.indexOfFreeVariable(tmpVariable), -tmpVariable.getAdjustmentFactor());
                    tmpLBI.set(tmpLoExprDim + i, 0, -tmpVariable.getAdjustedLowerLimit());
                    tmpEntities[tmpUpExprDim + tmpUpVarDim + tmpLoExprDim + i] = tmpVariable;
                }
            }

            final MatrixStore<Double> tmpAI = tmpLAI.builder().above(tmpUAI).build();
            final MatrixStore<Double> tmpBI = tmpLBI.builder().above(tmpUBI).build();

            destinationBuilder.inequalities(tmpAI, tmpBI, tmpEntities);

            final IndexSelector tmpSelector = new IndexSelector(tmpEntities.length);
            for (int i = 0; i < tmpEntities.length; i++) {
                if (tmpEntities[i].isActiveInequalityConstraint()) {
                    tmpSelector.include(i);
                }
            }

            tmpKickStarter.activeSet(tmpSelector.getIncluded());
        }

        // destinationBuilder.setKickStarter(tmpKickStarter);
        destinationBuilder.setKickStarter(null);
    }

    protected ConvexSolver(final ExpressionsBasedModel aModel, final Optimisation.Options solverOptions, final ConvexSolver.Builder matrices) {
        super(aModel, solverOptions, matrices);
    }

    public final Optimisation.Result solve(final Optimisation.Result kickStarter) {

        try {

            boolean tmpContinue = true;

            if (options.validate) {
                tmpContinue = this.validate();
            }

            if (tmpContinue) {
                tmpContinue = this.initialise(kickStarter);
            }

            if (tmpContinue) {

                this.resetIterationsCount();

                do {

                    this.performIteration();

                    this.incrementIterationsCount();

                } while (!this.getState().isFailure() && this.needsAnotherIteration() && this.isIterationAllowed());
            }

        } catch (final Exception exception) {

            if (this.isDebug()) {
                this.debug(exception);
            }

            this.setState(State.FAILED);
            this.resetX();
            this.resetLI();
            this.resetLE();
        }

        return this.buildResult();
    }

    @Override
    protected MatrixStore<Double> extractSolution() {

        final ExpressionsBasedModel tmpModel = this.getModel();

        if (tmpModel != null) {

            final List<Variable> tmpFreeVariables = tmpModel.getFreeVariables();
            final Set<Index> tmpFixedVariables = tmpModel.getFixedVariables();

            final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(tmpFixedVariables.size() + tmpFreeVariables.size(), 1);

            for (final Index tmpVariable : tmpFixedVariables) {
                retVal.set(tmpVariable.index, 0, tmpModel.getVariable(tmpVariable.index).getValue().doubleValue());
            }

            final MatrixStore<Double> tmpSolutionX = this.getSolutionX();
            for (int i = 0; i < tmpFreeVariables.size(); i++) {
                final Variable tmpVariable = tmpFreeVariables.get(i);
                final int tmpIndexOf = tmpModel.indexOf(tmpVariable);
                retVal.set(tmpIndexOf, 0, tmpSolutionX.doubleValue(i));

            }

            return retVal;

        } else {

            return this.getSolutionX().copy();
        }
    }

    abstract protected void performIteration();

    final MatrixStore<Double> getSolutionLE() {
        return this.getLE();
    }

    final MatrixStore<Double> getSolutionLI(final int... active) {
        return this.getLI(active);
    }

    final MatrixStore<Double> getSolutionX() {
        return this.getX();
    }

}
