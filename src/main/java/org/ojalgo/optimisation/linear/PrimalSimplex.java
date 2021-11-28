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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.RowView;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.context.NumberContext;

final class PrimalSimplex extends SimplexSolver {

    static SimplexTableau build(final ConvexSolver.Builder convex, final Optimisation.Options options, final boolean zeroC) {

        int numbVars = convex.countVariables();
        int numbEqus = convex.countEqualityConstraints();
        int numbInes = convex.countInequalityConstraints();

        SimplexTableau retVal = SimplexTableau.make(numbEqus + numbInes, numbVars + numbVars, numbInes, options);

        Mutate1D obj = retVal.objective();

        MatrixStore<Double> convexC = zeroC ? Primitive64Store.FACTORY.makeZero(convex.countVariables(), 1) : convex.getC();

        for (int v = 0; v < numbVars; v++) {
            double valC = convexC.doubleValue(v);
            obj.set(v, -valC);
            obj.set(numbVars + v, valC);
        }

        Mutate2D constrBody = retVal.constraintsBody();
        Mutate1D constrRHS = retVal.constraintsRHS();

        MatrixStore<Double> convexAE = convex.getAE();
        MatrixStore<Double> convexBE = convex.getBE();

        for (int i = 0; i < numbEqus; i++) {
            double rhs = convexBE.doubleValue(i);

            boolean neg = retVal.negative[i] = NumberContext.compare(rhs, ZERO) < 0;

            for (int j = 0; j < numbVars; j++) {
                double valA = convexAE.doubleValue(i, j);
                constrBody.set(i, j, neg ? -valA : valA);
                constrBody.set(i, numbVars + j, neg ? valA : -valA);
            }
            constrRHS.set(i, neg ? -rhs : rhs);
        }

        for (RowView<Double> rowAI : convex.getRowsAI()) {

            int r = Math.toIntExact(rowAI.row());

            double rhs = convex.getBI(r);

            boolean neg = retVal.negative[numbEqus + r] = NumberContext.compare(rhs, ZERO) < 0;

            rowAI.nonzeros().forEach(nz -> constrBody.set(numbEqus + r, nz.index(), neg ? -nz.doubleValue() : nz.doubleValue()));
            rowAI.nonzeros().forEach(nz -> constrBody.set(numbEqus + r, numbVars + nz.index(), neg ? nz.doubleValue() : -nz.doubleValue()));
            constrBody.set(numbEqus + r, numbVars + numbVars + r, neg ? NEG : ONE);
            constrRHS.set(numbEqus + r, neg ? -rhs : rhs);
        }

        return retVal;
    }

    static SimplexTableau build(final ExpressionsBasedModel model) {

        List<Variable> tmpPosVariables = model.getPositiveVariables();
        List<Variable> tmpNegVariables = model.getNegativeVariables();
        Set<IntIndex> tmpFixVariables = model.getFixedVariables();

        Expression tmpObjFunc = model.objective().compensate(tmpFixVariables);

        List<Expression> tmpExprsEq = model.constraints().filter(c -> c.isEqualityConstraint() && !c.isAnyQuadraticFactorNonZero())
                .collect(Collectors.toList());
        List<Expression> tmpExprsLo = model.constraints().filter(c -> c.isLowerConstraint() && !c.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
        List<Expression> tmpExprsUp = model.constraints().filter(c -> c.isUpperConstraint() && !c.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());

        List<Variable> tmpVarsPosLo = model.bounds().filter(v -> v.isPositive() && v.isLowerConstraint() && v.getLowerLimit().signum() > 0)
                .collect(Collectors.toList());
        List<Variable> tmpVarsPosUp = model.bounds().filter(v -> v.isPositive() && v.isUpperConstraint() && v.getUpperLimit().signum() > 0)
                .collect(Collectors.toList());

        List<Variable> tmpVarsNegLo = model.bounds().filter(v -> v.isNegative() && v.isLowerConstraint() && v.getLowerLimit().signum() < 0)
                .collect(Collectors.toList());
        List<Variable> tmpVarsNegUp = model.bounds().filter(v -> v.isNegative() && v.isUpperConstraint() && v.getUpperLimit().signum() < 0)
                .collect(Collectors.toList());

        int tmpConstraiCount = tmpExprsEq.size() + tmpExprsLo.size() + tmpExprsUp.size() + tmpVarsPosLo.size() + tmpVarsPosUp.size() + tmpVarsNegLo.size()
                + tmpVarsNegUp.size();
        int tmpProblVarCount = tmpPosVariables.size() + tmpNegVariables.size();
        int tmpSlackVarCount = tmpExprsLo.size() + tmpExprsUp.size() + tmpVarsPosLo.size() + tmpVarsPosUp.size() + tmpVarsNegLo.size() + tmpVarsNegUp.size();

        SimplexTableau retVal = SimplexTableau.make(tmpConstraiCount, tmpProblVarCount, tmpSlackVarCount, model.options);

        int tmpPosVarsBaseIndex = 0;
        int tmpNegVarsBaseIndex = tmpPosVarsBaseIndex + tmpPosVariables.size();
        int tmpSlaVarsBaseIndex = tmpNegVarsBaseIndex + tmpNegVariables.size();

        for (IntIndex tmpKey : tmpObjFunc.getLinearKeySet()) {

            double tmpFactor = model.isMaximisation() ? -tmpObjFunc.getAdjustedLinearFactor(tmpKey) : tmpObjFunc.getAdjustedLinearFactor(tmpKey);

            int tmpPosInd = model.indexOfPositiveVariable(tmpKey.index);
            if (tmpPosInd >= 0) {
                retVal.objective().set(tmpPosInd, tmpFactor);
            }

            int tmpNegInd = model.indexOfNegativeVariable(tmpKey.index);
            if (tmpNegInd >= 0) {
                retVal.objective().set(tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
            }
        }

        int tmpConstrBaseIndex = 0;
        int tmpCurrentSlackVarIndex = tmpSlaVarsBaseIndex;

        int tmpExprsEqLength = tmpExprsEq.size();
        for (int c = 0; c < tmpExprsEqLength; c++) {

            Expression tmpExpr = tmpExprsEq.get(c).compensate(tmpFixVariables);
            double tmpRHS = tmpExpr.getAdjustedLowerLimit();

            if (tmpRHS < ZERO) {

                retVal.constraintsRHS().set(tmpConstrBaseIndex + c, -tmpRHS);

                for (IntIndex tmpKey : tmpExpr.getLinearKeySet()) {

                    double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    int tmpPosInd = model.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, -tmpFactor);
                    }

                    int tmpNegInd = model.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, tmpFactor);
                    }
                }

            } else {

                retVal.constraintsRHS().set(tmpConstrBaseIndex + c, tmpRHS);

                for (IntIndex tmpKey : tmpExpr.getLinearKeySet()) {

                    double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    int tmpPosInd = model.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, tmpFactor);
                    }

                    int tmpNegInd = model.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
                    }
                }
            }
        }
        tmpConstrBaseIndex += tmpExprsEqLength;

        int tmpExprsLoLength = tmpExprsLo.size();
        for (int c = 0; c < tmpExprsLoLength; c++) {

            Expression tmpExpr = tmpExprsLo.get(c).compensate(tmpFixVariables);
            double tmpRHS = tmpExpr.getAdjustedLowerLimit();

            if (tmpRHS < ZERO) {

                retVal.constraintsRHS().set(tmpConstrBaseIndex + c, -tmpRHS);

                for (IntIndex tmpKey : tmpExpr.getLinearKeySet()) {

                    double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    int tmpPosInd = model.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, -tmpFactor);
                    }

                    int tmpNegInd = model.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, tmpFactor);
                    }
                }

                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, ONE);

            } else {

                retVal.constraintsRHS().set(tmpConstrBaseIndex + c, tmpRHS);

                for (IntIndex tmpKey : tmpExpr.getLinearKeySet()) {

                    double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    int tmpPosInd = model.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, tmpFactor);
                    }

                    int tmpNegInd = model.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
                    }
                }

                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, NEG);
            }
        }
        tmpConstrBaseIndex += tmpExprsLoLength;

        int tmpExprsUpLength = tmpExprsUp.size();
        for (int c = 0; c < tmpExprsUpLength; c++) {

            Expression tmpExpr = tmpExprsUp.get(c).compensate(tmpFixVariables);
            double tmpRHS = tmpExpr.getAdjustedUpperLimit();

            if (tmpRHS < ZERO) {

                retVal.constraintsRHS().set(tmpConstrBaseIndex + c, -tmpRHS);

                for (IntIndex tmpKey : tmpExpr.getLinearKeySet()) {

                    double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    int tmpPosInd = model.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, -tmpFactor);
                    }

                    int tmpNegInd = model.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, tmpFactor);
                    }
                }

                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, NEG);

            } else {

                retVal.constraintsRHS().set(tmpConstrBaseIndex + c, tmpRHS);

                for (IntIndex tmpKey : tmpExpr.getLinearKeySet()) {

                    double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    int tmpPosInd = model.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, tmpFactor);
                    }

                    int tmpNegInd = model.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
                    }
                }

                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, ONE);
            }
        }
        tmpConstrBaseIndex += tmpExprsUpLength;

        int tmpVarsPosLoLength = tmpVarsPosLo.size();
        for (int c = 0; c < tmpVarsPosLoLength; c++) {
            Variable tmpVar = tmpVarsPosLo.get(c);

            retVal.constraintsRHS().set(tmpConstrBaseIndex + c, tmpVar.getAdjustedLowerLimit());

            int tmpKey = model.indexOf(tmpVar);

            double tmpFactor = tmpVar.getAdjustmentFactor();

            int tmpPosInd = model.indexOfPositiveVariable(tmpKey);
            if (tmpPosInd >= 0) {
                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, tmpFactor);
            }

            int tmpNegInd = model.indexOfNegativeVariable(tmpKey);
            if (tmpNegInd >= 0) {
                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
            }

            retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, NEG);
        }
        tmpConstrBaseIndex += tmpVarsPosLoLength;

        int tmpVarsPosUpLength = tmpVarsPosUp.size();
        for (int c = 0; c < tmpVarsPosUpLength; c++) {
            Variable tmpVar = tmpVarsPosUp.get(c);

            retVal.constraintsRHS().set(tmpConstrBaseIndex + c, tmpVar.getAdjustedUpperLimit());

            int tmpKey = model.indexOf(tmpVar);

            double tmpFactor = tmpVar.getAdjustmentFactor();

            int tmpPosInd = model.indexOfPositiveVariable(tmpKey);
            if (tmpPosInd >= 0) {
                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, tmpFactor);
            }

            int tmpNegInd = model.indexOfNegativeVariable(tmpKey);
            if (tmpNegInd >= 0) {
                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
            }

            retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, ONE);
        }
        tmpConstrBaseIndex += tmpVarsPosUpLength;

        int tmpVarsNegLoLength = tmpVarsNegLo.size();
        for (int c = 0; c < tmpVarsNegLoLength; c++) {
            Variable tmpVar = tmpVarsNegLo.get(c);

            retVal.constraintsRHS().set(tmpConstrBaseIndex + c, -tmpVar.getAdjustedLowerLimit());

            int tmpKey = model.indexOf(tmpVar);

            double tmpFactor = tmpVar.getAdjustmentFactor();

            int tmpPosInd = model.indexOfPositiveVariable(tmpKey);
            if (tmpPosInd >= 0) {
                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, -tmpFactor);
            }

            int tmpNegInd = model.indexOfNegativeVariable(tmpKey);
            if (tmpNegInd >= 0) {
                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, tmpFactor);
            }

            retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, ONE);
        }
        tmpConstrBaseIndex += tmpVarsNegLoLength;

        int tmpVarsNegUpLength = tmpVarsNegUp.size();
        for (int c = 0; c < tmpVarsNegUpLength; c++) {
            Variable tmpVar = tmpVarsNegUp.get(c);

            retVal.constraintsRHS().set(tmpConstrBaseIndex + c, -tmpVar.getAdjustedUpperLimit());

            int tmpKey = model.indexOf(tmpVar);

            double tmpFactor = tmpVar.getAdjustmentFactor();

            int tmpPosInd = model.indexOfPositiveVariable(tmpKey);
            if (tmpPosInd >= 0) {
                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, -tmpFactor);
            }

            int tmpNegInd = model.indexOfNegativeVariable(tmpKey);
            if (tmpNegInd >= 0) {
                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, tmpFactor);
            }

            retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, NEG);
        }
        tmpConstrBaseIndex += tmpVarsNegUpLength;

        //        BasicLogger.DEBUG.printmtrx("Sparse", retVal);
        //        BasicLogger.DEBUG.printmtrx("Dense", retVal.toDense());

        return retVal;
    }

    static Optimisation.Result doSolve(final ConvexSolver.Builder convex, final Optimisation.Options options, final boolean zeroC) {

        SimplexTableau tableau = PrimalSimplex.build(convex, options, zeroC);

        LinearSolver solver = new PrimalSimplex(tableau, options);

        Result result = solver.solve();

        Optimisation.Result retVal = PrimalSimplex.toConvexState(result, convex);

        return retVal;
    }

    static int size(final ConvexSolver.Builder convex) {

        int numbVars = convex.countVariables();
        int numbEqus = convex.countEqualityConstraints();
        int numbInes = convex.countInequalityConstraints();

        return SimplexTableau.size(numbEqus + numbInes, numbVars + numbVars, numbInes);
    }

    static Optimisation.Result toConvexState(final Result result, final ConvexSolver.Builder convex) {

        int numbVars = convex.countVariables();

        Optimisation.Result retVal = new Optimisation.Result(result.getState(), result.getValue(), new Access1D<Double>() {

            public long count() {
                return numbVars;
            }

            public double doubleValue(final long index) {
                return result.doubleValue(index) - result.doubleValue(numbVars + index);
            }

            public Double get(final long index) {
                return this.doubleValue(index);
            }

            @Override
            public String toString() {
                return Access1D.toString(this);
            }

        });

        retVal.multipliers(result.getMultipliers().get());

        return retVal;
    }

    PrimalSimplex(final SimplexTableau tableau, final Options solverOptions) {
        super(tableau, solverOptions);
    }

}
