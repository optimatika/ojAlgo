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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.netio.BasicLogger;
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

    private static SimplexTableau buildOldVersion(final ExpressionsBasedModel model) {

        List<Variable> posVariables = model.getPositiveVariables();
        List<Variable> negVariables = model.getNegativeVariables();
        Set<IntIndex> fixedVariables = model.getFixedVariables();

        Expression objFunc = model.objective().compensate(fixedVariables);

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

        //        BasicLogger.debug("tmpVarsPosLo: {}", tmpVarsPosLo);
        //        BasicLogger.debug("tmpVarsPosUp: {}", tmpVarsPosUp);
        //        BasicLogger.debug("tmpVarsNegLo: {}", tmpVarsNegLo);
        //        BasicLogger.debug("tmpVarsNegUp: {}", tmpVarsNegUp);

        int nbConstraints = tmpExprsEq.size() + tmpExprsLo.size() + tmpExprsUp.size() + tmpVarsPosLo.size() + tmpVarsPosUp.size() + tmpVarsNegLo.size()
                + tmpVarsNegUp.size();
        int nbProbVars = posVariables.size() + negVariables.size();
        int nbSlackVars = tmpExprsLo.size() + tmpExprsUp.size() + tmpVarsPosLo.size() + tmpVarsPosUp.size() + tmpVarsNegLo.size() + tmpVarsNegUp.size();
        int nbIdentitySlackVars = 0;
        boolean needDuals = false;

        SimplexTableau retVal = SimplexTableau.make(nbConstraints, nbProbVars, nbSlackVars, 0, false, model.options);

        int tmpPosVarsBaseIndex = 0;
        int tmpNegVarsBaseIndex = tmpPosVarsBaseIndex + posVariables.size();
        int tmpSlaVarsBaseIndex = tmpNegVarsBaseIndex + negVariables.size();

        for (IntIndex tmpKey : objFunc.getLinearKeySet()) {

            double tmpFactor = model.isMaximisation() ? -objFunc.getAdjustedLinearFactor(tmpKey) : objFunc.getAdjustedLinearFactor(tmpKey);

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

            Expression tmpExpr = tmpExprsEq.get(c).compensate(fixedVariables);
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

            Expression tmpExpr = tmpExprsLo.get(c).compensate(fixedVariables);
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

            Expression tmpExpr = tmpExprsUp.get(c).compensate(fixedVariables);
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

    private static void set(final ExpressionsBasedModel model, final SimplexSolver.Primitive2D constraintsBdy, final int indCnstr, final int basePosVars,
            final int baseNegVars, final IntIndex key, final double factor) {

        int tmpPosInd = model.indexOfPositiveVariable(key);
        if (tmpPosInd >= 0) {
            constraintsBdy.set(indCnstr, basePosVars + tmpPosInd, factor);
        }

        int tmpNegInd = model.indexOfNegativeVariable(key);
        if (tmpNegInd >= 0) {
            constraintsBdy.set(indCnstr, baseNegVars + tmpNegInd, -factor);
        }
    }

    private static void set(final ExpressionsBasedModel model, final SimplexSolver.Primitive2D constraintsBdy, final int indCnstr, final int basePosVars,
            final int baseNegVars, final Variable variable, final double factor) {

        int tmpPosInd = model.indexOfPositiveVariable(variable);
        if (tmpPosInd >= 0) {
            constraintsBdy.set(indCnstr, basePosVars + tmpPosInd, factor);
        }

        int tmpNegInd = model.indexOfNegativeVariable(variable);
        if (tmpNegInd >= 0) {
            constraintsBdy.set(indCnstr, baseNegVars + tmpNegInd, -factor);
        }
    }

    static SimplexTableau build(final ConvexSolver.Builder convex, final Optimisation.Options options, final boolean zeroC) {

        int numbVars = convex.countVariables();
        int numbEqus = convex.countEqualityConstraints();
        int numbInes = convex.countInequalityConstraints();

        SimplexTableau retVal = SimplexTableau.make(numbEqus + numbInes, numbVars + numbVars, numbInes, 0, true, options);

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

        List<Variable> posVariables = model.getPositiveVariables();
        List<Variable> negVariables = model.getNegativeVariables();
        Set<IntIndex> fixedVariables = model.getFixedVariables();

        for (Variable pos : posVariables) {
            if (pos.isEqualityConstraint()) {
                BasicLogger.debug(pos);
            }
        }
        for (Variable neg : negVariables) {
            if (neg.isEqualityConstraint()) {
                BasicLogger.debug(neg);
            }
        }

        List<Variable> bounds = model.bounds().collect(Collectors.toList());

        List<Variable> varsPosUp = new ArrayList<>();
        List<Variable> varsPosLo = new ArrayList<>();
        List<Variable> varsNegUp = new ArrayList<>();
        List<Variable> varsNegLo = new ArrayList<>();

        for (Variable variable : bounds) {
            if (variable.isPositive()) {
                if (variable.isUpperConstraint() && variable.getUpperLimit().signum() > 0) {
                    varsPosUp.add(variable);
                }
                if (variable.isLowerConstraint() && variable.getLowerLimit().signum() > 0) {
                    varsPosLo.add(variable);
                }
            }
            if (variable.isNegative()) {
                if (variable.isUpperConstraint() && variable.getUpperLimit().signum() < 0) {
                    varsNegUp.add(variable);
                }
                if (variable.isLowerConstraint() && variable.getLowerLimit().signum() < 0) {
                    varsNegLo.add(variable);
                }
            }
        }

        //        BasicLogger.debug("varsPosLo: {}", varsPosLo);
        //        BasicLogger.debug("varsPosUp: {}", varsPosUp);
        //        BasicLogger.debug("varsNegLo: {}", varsNegLo);
        //        BasicLogger.debug("varsNegUp: {}", varsNegUp);

        List<Expression> constraints = model.constraints().map(c -> c.compensate(fixedVariables)).collect(Collectors.toList());

        List<Expression> exprUpPos = new ArrayList<>();
        List<Expression> exprUpNeg = new ArrayList<>();
        List<Expression> exprLoPos = new ArrayList<>();
        List<Expression> exprLoNeg = new ArrayList<>();
        List<Expression> exprEqPos = new ArrayList<>();
        List<Expression> exprEqNeg = new ArrayList<>();

        for (Expression expression : constraints) {
            if (expression.isEqualityConstraint()) {
                if (expression.getUpperLimit().signum() < 0) {
                    exprEqNeg.add(expression);
                } else {
                    exprEqPos.add(expression);
                }
            } else {
                if (expression.isLowerConstraint()) {
                    if (expression.getLowerLimit().signum() < 0) {
                        exprLoNeg.add(expression);
                    } else {
                        exprLoPos.add(expression);
                    }
                }
                if (expression.isUpperConstraint()) {
                    if (expression.getUpperLimit().signum() < 0) {
                        exprUpNeg.add(expression);
                    } else {
                        exprUpPos.add(expression);
                    }
                }
            }
        }

        Expression objective = model.objective().compensate(fixedVariables);

        int nbProbVars = posVariables.size() + negVariables.size();
        int nbIdentitySlackVars = exprUpPos.size() + exprLoNeg.size() + varsPosUp.size() + varsNegLo.size();
        int nbOtherSlackVars = exprUpNeg.size() + exprLoPos.size() + varsNegUp.size() + varsPosLo.size();
        int nbConstraints = nbIdentitySlackVars + nbOtherSlackVars + exprEqPos.size() + exprEqNeg.size();
        boolean needDuals = false;

        SimplexTableau retVal = SimplexTableau.make(nbConstraints, nbProbVars, nbOtherSlackVars, nbIdentitySlackVars, needDuals, model.options);
        SimplexSolver.Primitive2D retConstraintsBdy = retVal.constraintsBody();
        SimplexSolver.Primitive1D retConstraintsRHS = retVal.constraintsRHS();
        SimplexSolver.Primitive1D retObjective = retVal.objective();

        int basePosVars = 0;
        int baseNegVars = basePosVars + posVariables.size();
        int baseSlackVars = baseNegVars + negVariables.size();
        int baseIdSlackVars = baseSlackVars + nbOtherSlackVars;
        int baseArtificialVars = baseIdSlackVars + nbIdentitySlackVars;

        for (IntIndex tmpKey : objective.getLinearKeySet()) {

            double tmpFactor = model.isMaximisation() ? -objective.getAdjustedLinearFactor(tmpKey) : objective.getAdjustedLinearFactor(tmpKey);

            int tmpPosInd = model.indexOfPositiveVariable(tmpKey);
            if (tmpPosInd >= 0) {
                retObjective.set(basePosVars + tmpPosInd, tmpFactor);
            }

            int tmpNegInd = model.indexOfNegativeVariable(tmpKey);
            if (tmpNegInd >= 0) {
                retObjective.set(baseNegVars + tmpNegInd, -tmpFactor);
            }
        }

        //  BasicLogger.debug("objective", retVal);

        int indCnstr = 0;
        int indSlack = baseIdSlackVars;

        for (Expression expression : exprUpPos) {

            for (IntIndex key : expression.getLinearKeySet()) {
                double factor = expression.getAdjustedLinearFactor(key);
                PrimalSimplex.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, key, factor);
            }

            retConstraintsBdy.set(indCnstr, indSlack, ONE);

            double rhs = expression.getAdjustedUpperLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            indCnstr++;
            indSlack++;
        }

        //  BasicLogger.debug("exprUpPos", retVal);

        for (Expression expression : exprLoNeg) {

            for (IntIndex key : expression.getLinearKeySet()) {
                double factor = -expression.getAdjustedLinearFactor(key);
                PrimalSimplex.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, key, factor);
            }

            retConstraintsBdy.set(indCnstr, indSlack, ONE);

            double rhs = -expression.getAdjustedLowerLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            indCnstr++;
            indSlack++;
        }

        //  BasicLogger.debug("exprLoNeg", retVal);

        for (Variable variable : varsPosUp) {

            double factor = variable.getAdjustmentFactor();
            PrimalSimplex.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, variable, factor);

            retConstraintsBdy.set(indCnstr, indSlack, ONE);

            double rhs = variable.getAdjustedUpperLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            indCnstr++;
            indSlack++;
        }

        //   BasicLogger.debug("varsUpPos", retVal);

        for (Variable variable : varsNegLo) {

            double factor = -variable.getAdjustmentFactor();
            PrimalSimplex.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, variable, factor);

            retConstraintsBdy.set(indCnstr, indSlack, ONE);

            double rhs = -variable.getAdjustedLowerLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            indCnstr++;
            indSlack++;
        }

        //   BasicLogger.debug("varsLoNeg", retVal);

        indSlack = baseSlackVars;

        for (Expression expression : exprLoPos) {

            for (IntIndex key : expression.getLinearKeySet()) {
                double factor = expression.getAdjustedLinearFactor(key);
                PrimalSimplex.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, key, factor);
            }

            retConstraintsBdy.set(indCnstr, indSlack, NEG);

            double rhs = expression.getAdjustedLowerLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            indCnstr++;
            indSlack++;
        }

        //   BasicLogger.debug("exprLoPos", retVal);

        for (Expression expression : exprUpNeg) {

            for (IntIndex key : expression.getLinearKeySet()) {
                double factor = -expression.getAdjustedLinearFactor(key);
                PrimalSimplex.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, key, factor);
            }

            retConstraintsBdy.set(indCnstr, indSlack, NEG);

            double rhs = -expression.getAdjustedUpperLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            indCnstr++;
            indSlack++;
        }

        //   BasicLogger.debug("exprUpNeg", retVal);

        for (Variable variable : varsPosLo) {

            double factor = variable.getAdjustmentFactor();
            PrimalSimplex.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, variable, factor);

            retConstraintsBdy.set(indCnstr, indSlack, NEG);

            double rhs = variable.getAdjustedLowerLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            indCnstr++;
            indSlack++;
        }

        //  BasicLogger.debug("varsLoPos", retVal);

        for (Variable variable : varsNegUp) {

            double factor = -variable.getAdjustmentFactor();
            PrimalSimplex.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, variable, factor);

            retConstraintsBdy.set(indCnstr, indSlack, NEG);

            double rhs = -variable.getAdjustedUpperLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            indCnstr++;
            indSlack++;
        }

        //  BasicLogger.debug("varsUpNeg", retVal);

        indSlack = Integer.MAX_VALUE;

        for (Expression expression : exprEqPos) {

            for (IntIndex key : expression.getLinearKeySet()) {
                double factor = expression.getAdjustedLinearFactor(key);
                PrimalSimplex.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, key, factor);
            }

            double rhs = expression.getAdjustedUpperLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            indCnstr++;
        }

        //  BasicLogger.debug("exprEqPos", retVal);

        for (Expression expression : exprEqNeg) {

            for (IntIndex key : expression.getLinearKeySet()) {
                double factor = -expression.getAdjustedLinearFactor(key);
                PrimalSimplex.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, key, factor);
            }

            double rhs = -expression.getAdjustedLowerLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            indCnstr++;
        }

        //   BasicLogger.debug("exprEqNeg", retVal);

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

        return SimplexTableau.size(numbEqus + numbInes, numbVars + numbVars, numbInes, 0, true);
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
