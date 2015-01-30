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
package org.ojalgo.optimisation.linear;

import static org.ojalgo.constant.PrimitiveMath.*;

import java.util.List;
import java.util.Set;

import org.ojalgo.access.AccessUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.optimisation.BaseSolver;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.Expression.Index;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.IndexSelector;

/**
 * LinearSolver solves optimisation problems of the (LP standard) form:
 * <p>
 * min [C]<sup>T</sup>[X]<br>
 * when [AE][X] == [BE]<br>
 * and 0 <= [X]<br>
 * and 0 <= [BE]
 * </p>
 * A Linear Program is in Standard Form if:
 * <ul>
 * <li>All constraints are equality constraints.</li>
 * <li>All variables have a nonnegativity sign restriction.</li>
 * </ul>
 * <p>
 * Further it is required here that the constraint right hand sides are nonnegative (nonnegative elements in [BE]).
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
public abstract class LinearSolver extends BaseSolver {

    public static final class Builder extends AbstractBuilder<LinearSolver.Builder, LinearSolver> {

        public Builder(final MatrixStore<Double> C) {
            super(C);
        }

        Builder() {
            super();
        }

        Builder(final BaseSolver.AbstractBuilder<LinearSolver.Builder, LinearSolver> matrices) {
            super(matrices);
        }

        Builder(final MatrixStore<Double> Q, final MatrixStore<Double> C) {
            super(Q, C);
        }

        Builder(final MatrixStore<Double>[] aMtrxArr) {
            super(aMtrxArr);
        }

        @Override
        public LinearSolver build(final Optimisation.Options options) {

            this.validate();

            return new SimplexTableauSolver(this, options);
        }

        @Override
        public Builder equalities(final MatrixStore<Double> AE, final MatrixStore<Double> BE) {
            return super.equalities(AE, BE);
        }

        @Override
        public Builder objective(final MatrixStore<Double> C) {
            return super.objective(C);
        }
    }

    public static void copy(final ExpressionsBasedModel sourceModel, final LinearSolver.Builder destinationBuilder) {

        final boolean tmpMaximisation = sourceModel.isMaximisation();

        final List<Variable> tmpPosVariables = sourceModel.getPositiveVariables();
        final List<Variable> tmpNegVariables = sourceModel.getNegativeVariables();
        final Set<Index> tmpFixVariables = sourceModel.getFixedVariables();

        final Expression tmpObjFunc = sourceModel.getObjectiveExpression();

        final List<Expression> tmpExprsEq = sourceModel.selectExpressionsLinearEquality();
        final List<Expression> tmpExprsLo = sourceModel.selectExpressionsLinearLower();
        final List<Expression> tmpExprsUp = sourceModel.selectExpressionsLinearUpper();

        final List<Variable> tmpVarsPosLo = sourceModel.selectVariablesPositiveLower();
        final List<Variable> tmpVarsPosUp = sourceModel.selectVariablesPositiveUpper();

        final List<Variable> tmpVarsNegLo = sourceModel.selectVariablesNegativeLower();
        final List<Variable> tmpVarsNegUp = sourceModel.selectVariablesNegativeUpper();

        final int tmpConstraiCount = tmpExprsEq.size() + tmpExprsLo.size() + tmpExprsUp.size() + tmpVarsPosLo.size() + tmpVarsPosUp.size()
                + tmpVarsNegLo.size() + tmpVarsNegUp.size();
        final int tmpProblVarCount = tmpPosVariables.size() + tmpNegVariables.size();
        final int tmpSlackVarCount = tmpExprsLo.size() + tmpExprsUp.size() + tmpVarsPosLo.size() + tmpVarsPosUp.size() + tmpVarsNegLo.size()
                + tmpVarsNegUp.size();
        final int tmpTotalVarCount = tmpProblVarCount + tmpSlackVarCount;

        final int[] tmpBasis = AccessUtils.makeIncreasingRange(-tmpConstraiCount, tmpConstraiCount);

        final PhysicalStore<Double> tmpC = FACTORY.makeZero(tmpTotalVarCount, 1);
        final PhysicalStore<Double> tmpAE = FACTORY.makeZero(tmpConstraiCount, tmpTotalVarCount);
        final PhysicalStore<Double> tmpBE = FACTORY.makeZero(tmpConstraiCount, 1);

        destinationBuilder.objective(tmpC);
        destinationBuilder.equalities(tmpAE, tmpBE);

        final int tmpPosVarsBaseIndex = 0;
        final int tmpNegVarsBaseIndex = tmpPosVarsBaseIndex + tmpPosVariables.size();
        final int tmpSlaVarsBaseIndex = tmpNegVarsBaseIndex + tmpNegVariables.size();

        for (final Expression.Index tmpKey : tmpObjFunc.getLinearFactorKeys()) {

            final double tmpFactor = tmpMaximisation ? -tmpObjFunc.getAdjustedLinearFactor(tmpKey) : tmpObjFunc.getAdjustedLinearFactor(tmpKey);

            final int tmpPosInd = sourceModel.indexOfPositiveVariable(tmpKey.index);
            if (tmpPosInd >= 0) {
                tmpC.set(tmpPosInd, 0, tmpFactor);
            }

            final int tmpNegInd = sourceModel.indexOfNegativeVariable(tmpKey.index);
            if (tmpNegInd >= 0) {
                tmpC.set(tmpNegVarsBaseIndex + tmpNegInd, 0, -tmpFactor);
            }
        }

        int tmpConstrBaseIndex = 0;
        int tmpCurrentSlackVarIndex = tmpSlaVarsBaseIndex;

        final int tmpExprsEqLength = tmpExprsEq.size();
        for (int c = 0; c < tmpExprsEqLength; c++) {

            final Expression tmpExpr = tmpExprsEq.get(c);
            final double tmpRHS = tmpExpr.getCompensatedLowerLimit(tmpFixVariables);

            if (tmpRHS < ZERO) {

                tmpBE.set(tmpConstrBaseIndex + c, 0, -tmpRHS);

                for (final Expression.Index tmpKey : tmpExpr.getLinearFactorKeys()) {

                    final double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    final int tmpPosInd = sourceModel.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        tmpAE.set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, -tmpFactor);
                    }

                    final int tmpNegInd = sourceModel.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        tmpAE.set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, tmpFactor);
                    }
                }

            } else {

                tmpBE.set(tmpConstrBaseIndex + c, 0, tmpRHS);

                for (final Expression.Index tmpKey : tmpExpr.getLinearFactorKeys()) {

                    final double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    final int tmpPosInd = sourceModel.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        tmpAE.set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, tmpFactor);
                    }

                    final int tmpNegInd = sourceModel.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        tmpAE.set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
                    }
                }
            }
        }
        tmpConstrBaseIndex += tmpExprsEqLength;

        final int tmpExprsLoLength = tmpExprsLo.size();
        for (int c = 0; c < tmpExprsLoLength; c++) {

            final Expression tmpExpr = tmpExprsLo.get(c);
            final double tmpRHS = tmpExpr.getCompensatedLowerLimit(tmpFixVariables);

            if (tmpRHS < ZERO) {

                tmpBE.set(tmpConstrBaseIndex + c, 0, -tmpRHS);
                tmpBasis[tmpConstrBaseIndex + c] = tmpCurrentSlackVarIndex;
                tmpAE.set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, ONE);

                for (final Expression.Index tmpKey : tmpExpr.getLinearFactorKeys()) {

                    final double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    final int tmpPosInd = sourceModel.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        tmpAE.set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, -tmpFactor);
                    }

                    final int tmpNegInd = sourceModel.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        tmpAE.set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, tmpFactor);
                    }
                }

            } else {

                tmpBE.set(tmpConstrBaseIndex + c, 0, tmpRHS);
                tmpAE.set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, NEG);

                for (final Expression.Index tmpKey : tmpExpr.getLinearFactorKeys()) {

                    final double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    final int tmpPosInd = sourceModel.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        tmpAE.set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, tmpFactor);
                    }

                    final int tmpNegInd = sourceModel.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        tmpAE.set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
                    }
                }
            }
        }
        tmpConstrBaseIndex += tmpExprsLoLength;

        final int tmpExprsUpLength = tmpExprsUp.size();
        for (int c = 0; c < tmpExprsUpLength; c++) {

            final Expression tmpExpr = tmpExprsUp.get(c);
            final double tmpRHS = tmpExpr.getCompensatedUpperLimit(tmpFixVariables);

            if (tmpRHS < ZERO) {

                tmpBE.set(tmpConstrBaseIndex + c, 0, -tmpRHS);
                tmpAE.set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, NEG);

                for (final Expression.Index tmpKey : tmpExpr.getLinearFactorKeys()) {

                    final double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    final int tmpPosInd = sourceModel.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        tmpAE.set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, -tmpFactor);
                    }

                    final int tmpNegInd = sourceModel.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        tmpAE.set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, tmpFactor);
                    }
                }

            } else {

                tmpBE.set(tmpConstrBaseIndex + c, 0, tmpRHS);
                tmpBasis[tmpConstrBaseIndex + c] = tmpCurrentSlackVarIndex;
                tmpAE.set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, ONE);

                for (final Expression.Index tmpKey : tmpExpr.getLinearFactorKeys()) {

                    final double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    final int tmpPosInd = sourceModel.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        tmpAE.set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, tmpFactor);
                    }

                    final int tmpNegInd = sourceModel.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        tmpAE.set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
                    }
                }
            }
        }
        tmpConstrBaseIndex += tmpExprsUpLength;

        final int tmpVarsPosLoLength = tmpVarsPosLo.size();
        for (int c = 0; c < tmpVarsPosLoLength; c++) {

            final Variable tmpVar = tmpVarsPosLo.get(c);

            tmpBE.set(tmpConstrBaseIndex + c, 0, tmpVar.getAdjustedLowerLimit());
            tmpAE.set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, NEG);

            final int tmpKey = sourceModel.indexOf(tmpVar);

            final double tmpFactor = tmpVar.getAdjustmentFactor();

            final int tmpPosInd = sourceModel.indexOfPositiveVariable(tmpKey);
            if (tmpPosInd >= 0) {
                tmpAE.set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, tmpFactor);
            }

            final int tmpNegInd = sourceModel.indexOfNegativeVariable(tmpKey);
            if (tmpNegInd >= 0) {
                tmpAE.set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
            }

        }
        tmpConstrBaseIndex += tmpVarsPosLoLength;

        final int tmpVarsPosUpLength = tmpVarsPosUp.size();
        for (int c = 0; c < tmpVarsPosUpLength; c++) {

            final Variable tmpVar = tmpVarsPosUp.get(c);

            tmpBE.set(tmpConstrBaseIndex + c, 0, tmpVar.getAdjustedUpperLimit());
            tmpBasis[tmpConstrBaseIndex + c] = tmpCurrentSlackVarIndex;
            tmpAE.set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, ONE);

            final int tmpKey = sourceModel.indexOf(tmpVar);

            final double tmpFactor = tmpVar.getAdjustmentFactor();

            final int tmpPosInd = sourceModel.indexOfPositiveVariable(tmpKey);
            if (tmpPosInd >= 0) {
                tmpAE.set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, tmpFactor);
            }

            final int tmpNegInd = sourceModel.indexOfNegativeVariable(tmpKey);
            if (tmpNegInd >= 0) {
                tmpAE.set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
            }

        }
        tmpConstrBaseIndex += tmpVarsPosUpLength;

        final int tmpVarsNegLoLength = tmpVarsNegLo.size();
        for (int c = 0; c < tmpVarsNegLoLength; c++) {

            final Variable tmpVar = tmpVarsNegLo.get(c);

            tmpBE.set(tmpConstrBaseIndex + c, 0, -tmpVar.getAdjustedLowerLimit());
            tmpBasis[tmpConstrBaseIndex + c] = tmpCurrentSlackVarIndex;
            tmpAE.set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, ONE);

            final int tmpKey = sourceModel.indexOf(tmpVar);

            final double tmpFactor = tmpVar.getAdjustmentFactor();

            final int tmpPosInd = sourceModel.indexOfPositiveVariable(tmpKey);
            if (tmpPosInd >= 0) {
                tmpAE.set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, -tmpFactor);
            }

            final int tmpNegInd = sourceModel.indexOfNegativeVariable(tmpKey);
            if (tmpNegInd >= 0) {
                tmpAE.set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, tmpFactor);
            }

        }
        tmpConstrBaseIndex += tmpVarsNegLoLength;

        final int tmpVarsNegUpLength = tmpVarsNegUp.size();
        for (int c = 0; c < tmpVarsNegUpLength; c++) {

            final Variable tmpVar = tmpVarsNegUp.get(c);

            tmpBE.set(tmpConstrBaseIndex + c, 0, -tmpVar.getAdjustedUpperLimit());
            tmpAE.set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, NEG);

            final int tmpKey = sourceModel.indexOf(tmpVar);

            final double tmpFactor = tmpVar.getAdjustmentFactor();

            final int tmpPosInd = sourceModel.indexOfPositiveVariable(tmpKey);
            if (tmpPosInd >= 0) {
                tmpAE.set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, -tmpFactor);
            }

            final int tmpNegInd = sourceModel.indexOfNegativeVariable(tmpKey);
            if (tmpNegInd >= 0) {
                tmpAE.set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, tmpFactor);
            }

        }
        tmpConstrBaseIndex += tmpVarsNegUpLength;

    }

    public static LinearSolver.Builder getBuilder() {
        return new LinearSolver.Builder();
    }

    static final Factory<Double, PrimitiveDenseStore> FACTORY = PrimitiveDenseStore.FACTORY;

    private final IndexSelector mySelector;

    protected LinearSolver(final BaseSolver.AbstractBuilder<LinearSolver.Builder, LinearSolver> matrices, final Optimisation.Options solverOptions) {

        super(matrices, solverOptions);

        mySelector = new IndexSelector(matrices.countVariables());
    }

    protected final int countBasisDeficit() {
        return this.countEqualityConstraints() - mySelector.countIncluded();
    }

    protected final int countConstraints() {
        return this.countEqualityConstraints();
    }

    protected final void exclude(final int anIndexToExclude) {
        mySelector.exclude(anIndexToExclude);
    }

    protected final void excludeAll() {
        mySelector.excludeAll();
    }

    protected final int[] getExcluded() {
        return mySelector.getExcluded();
    }

    protected final int[] getIncluded() {
        return mySelector.getIncluded();
    }

    protected final boolean hasConstraints() {
        return this.hasEqualityConstraints();
    }

    protected final void include(final int anIndexToInclude) {
        mySelector.include(anIndexToInclude);
    }

    protected final void include(final int[] someIndecesToInclude) {
        mySelector.include(someIndecesToInclude);
    }

}
