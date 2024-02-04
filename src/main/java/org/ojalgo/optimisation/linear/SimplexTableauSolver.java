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

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.LongToNumberMap;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.equation.Equation;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ConstraintsMap;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.convex.ConvexData;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D.RowView;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.context.NumberContext;

/**
 * Classic simplex solver:
 * <ul>
 * <li>Primal algorithm
 * <li>2-phase
 * <li>All variables assumed >=0, and RHS required to be >=0
 * <li>Variable bounds other than >=0 handled like constraints
 * </ul>
 * 
 * @author apete
 */
final class SimplexTableauSolver extends LinearSolver {

    static final class IterationPoint {

        private boolean myPhase1 = true;

        int col;
        int row;

        IterationPoint() {
            super();
            this.reset();
        }

        boolean isPhase1() {
            return myPhase1;
        }

        boolean isPhase2() {
            return !myPhase1;
        }

        void reset() {
            row = -1;
            col = -1;
        }

        void returnToPhase1() {
            myPhase1 = true;
        }

        void switchToPhase2() {
            myPhase1 = false;
        }

    }

    private static final NumberContext ACC = NumberContext.of(12, 14).withMode(RoundingMode.HALF_DOWN);
    private static final NumberContext DEGENERATE = NumberContext.of(12, 8).withMode(RoundingMode.HALF_DOWN);
    private static final NumberContext PHASE1 = NumberContext.of(12, 8).withMode(RoundingMode.HALF_DOWN);
    private static final NumberContext PIVOT = NumberContext.of(12, 8).withMode(RoundingMode.HALF_DOWN);
    private static final NumberContext RATIO = NumberContext.of(12, 8).withMode(RoundingMode.HALF_DOWN);
    private static final NumberContext WEIGHT = NumberContext.of(8, 10).withMode(RoundingMode.HALF_DOWN);

    private static void set(final ExpressionsBasedModel model, final Primitive2D constraintsBdy, final int indCnstr, final int basePosVars,
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

    private static void set(final ExpressionsBasedModel model, final Primitive2D constraintsBdy, final int indCnstr, final int basePosVars,
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

    private static Optimisation.Result toConvexStateFromDual(final Optimisation.Result result, final ConvexData<Double> convex) {

        int nbEqus = convex.countEqualityConstraints();
        int nbInes = convex.countInequalityConstraints();

        Access1D<?> multipliers = result.getMultipliers().get();

        State retState = result.getState();
        if (retState == State.UNBOUNDED) {
            retState = State.INFEASIBLE;
        } else if (!retState.isFeasible()) {
            retState = State.UNBOUNDED;
        }

        double retValue = -result.getValue();

        Primitive1D retSolution = new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return -multipliers.doubleValue(index);
            }

            @Override
            public void set(final int index, final double value) {
                throw new IllegalArgumentException();
            }

            @Override
            public int size() {
                return multipliers.size();
            }

        };

        Primitive1D retMultipliers = new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                if (index < nbEqus) {
                    return result.doubleValue(index) - result.doubleValue(nbEqus + index);
                } else {
                    return result.doubleValue(nbEqus + index);
                }
            }

            @Override
            public void set(final int index, final double value) {
                throw new IllegalArgumentException();
            }

            @Override
            public int size() {
                return nbEqus + nbInes;
            }

        };

        Optimisation.Result retVal = new Optimisation.Result(retState, retValue, retSolution);

        retVal.multipliers(retMultipliers);

        return retVal;
    }

    private static Optimisation.Result toConvexStateFromPrimal(final Optimisation.Result result, final ConvexData<Double> convex) {

        int nbVars = convex.countVariables();

        Primitive1D solution = new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return result.doubleValue(index) - result.doubleValue(nbVars + index);
            }

            @Override
            public void set(final int index, final double value) {
                throw new IllegalArgumentException();
            }

            @Override
            public int size() {
                return nbVars;
            }

        };

        return result.withSolution(solution);
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

        int nbPosProbVars = posVariables.size();
        int nbNegProbVars = negVariables.size();
        int nbIdentitySlackVars = exprUpPos.size() + exprLoNeg.size() + varsPosUp.size() + varsNegLo.size();
        int nbOtherSlackVars = exprUpNeg.size() + exprLoPos.size() + varsNegUp.size() + varsPosLo.size();
        int nbConstraints = nbIdentitySlackVars + nbOtherSlackVars + exprEqPos.size() + exprEqNeg.size();
        int constrIn = nbConstraints - (exprEqPos.size() + exprEqNeg.size());
        int constrEq = exprEqPos.size() + exprEqNeg.size();
        int nbArtificials = constrIn + constrEq - nbIdentitySlackVars;

        LinearStructure structure = new LinearStructure(true, constrIn, constrEq, nbPosProbVars, nbNegProbVars, nbOtherSlackVars, nbIdentitySlackVars);

        SimplexTableau retVal = SimplexTableau.make(structure, model.options);

        Primitive2D retConstraintsBdy = retVal.constraintsBody();
        Primitive1D retConstraintsRHS = retVal.constraintsRHS();
        Primitive1D retObjective = retVal.objective();

        int basePosVars = 0; // 0 + 0
        int baseNegVars = nbPosProbVars; // 0 + nbPosProbVars
        int baseSlackVars = baseNegVars + nbNegProbVars;
        int baseIdSlackVars = baseSlackVars + nbOtherSlackVars;
        int baseArtificialVars = baseIdSlackVars + nbIdentitySlackVars;

        for (int i = 0; i < posVariables.size(); i++) {
            structure.positivePartVariables[i] = model.indexOf(posVariables.get(i));
        }
        for (int i = 0; i < negVariables.size(); i++) {
            structure.negativePartVariables[i] = model.indexOf(negVariables.get(i));
        }

        structure.setObjectiveAdjustmentFactor(objective.getAdjustmentFactor());

        for (IntIndex tmpKey : objective.getLinearKeySet()) {

            double tmpFactor = model.getOptimisationSense() == Optimisation.Sense.MAX ? -objective.getAdjustedLinearFactor(tmpKey)
                    : objective.getAdjustedLinearFactor(tmpKey);

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
                SimplexTableauSolver.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, key, factor);
            }

            retConstraintsBdy.set(indCnstr, indSlack, ONE);

            double rhs = expression.getAdjustedUpperLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            structure.setConstraintMap(indCnstr, expression, ConstraintType.UPPER, false);
            indCnstr++;
            indSlack++;
        }

        //  BasicLogger.debug("exprUpPos", retVal);

        for (Expression expression : exprLoNeg) {

            for (IntIndex key : expression.getLinearKeySet()) {
                double factor = -expression.getAdjustedLinearFactor(key);
                SimplexTableauSolver.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, key, factor);
            }

            retConstraintsBdy.set(indCnstr, indSlack, ONE);

            double rhs = -expression.getAdjustedLowerLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            structure.setConstraintMap(indCnstr, expression, ConstraintType.LOWER, false);
            indCnstr++;
            indSlack++;
        }

        //  BasicLogger.debug("exprLoNeg", retVal);

        for (Variable variable : varsPosUp) {

            double factor = variable.getAdjustmentFactor();
            SimplexTableauSolver.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, variable, factor);

            retConstraintsBdy.set(indCnstr, indSlack, ONE);

            double rhs = variable.getAdjustedUpperLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            structure.setConstraintMap(indCnstr, variable, ConstraintType.UPPER, false);
            indCnstr++;
            indSlack++;
        }

        //   BasicLogger.debug("varsUpPos", retVal);

        for (Variable variable : varsNegLo) {

            double factor = -variable.getAdjustmentFactor();
            SimplexTableauSolver.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, variable, factor);

            retConstraintsBdy.set(indCnstr, indSlack, ONE);

            double rhs = -variable.getAdjustedLowerLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            structure.setConstraintMap(indCnstr, variable, ConstraintType.LOWER, false);
            indCnstr++;
            indSlack++;
        }

        //   BasicLogger.debug("varsLoNeg", retVal);

        indSlack = baseSlackVars;

        for (Expression expression : exprLoPos) {

            for (IntIndex key : expression.getLinearKeySet()) {
                double factor = expression.getAdjustedLinearFactor(key);
                SimplexTableauSolver.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, key, factor);
            }

            retConstraintsBdy.set(indCnstr, indSlack, NEG);

            double rhs = expression.getAdjustedLowerLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            structure.setConstraintMap(indCnstr, expression, ConstraintType.LOWER, true);
            indCnstr++;
            indSlack++;
        }

        //   BasicLogger.debug("exprLoPos", retVal);

        for (Expression expression : exprUpNeg) {

            for (IntIndex key : expression.getLinearKeySet()) {
                double factor = -expression.getAdjustedLinearFactor(key);
                SimplexTableauSolver.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, key, factor);
            }

            retConstraintsBdy.set(indCnstr, indSlack, NEG);

            double rhs = -expression.getAdjustedUpperLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            structure.setConstraintMap(indCnstr, expression, ConstraintType.UPPER, true);
            indCnstr++;
            indSlack++;
        }

        //   BasicLogger.debug("exprUpNeg", retVal);

        for (Variable variable : varsPosLo) {

            double factor = variable.getAdjustmentFactor();
            SimplexTableauSolver.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, variable, factor);

            retConstraintsBdy.set(indCnstr, indSlack, NEG);

            double rhs = variable.getAdjustedLowerLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            structure.setConstraintMap(indCnstr, variable, ConstraintType.LOWER, true);
            indCnstr++;
            indSlack++;
        }

        //  BasicLogger.debug("varsLoPos", retVal);

        for (Variable variable : varsNegUp) {

            double factor = -variable.getAdjustmentFactor();
            SimplexTableauSolver.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, variable, factor);

            retConstraintsBdy.set(indCnstr, indSlack, NEG);

            double rhs = -variable.getAdjustedUpperLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            structure.setConstraintMap(indCnstr, variable, ConstraintType.UPPER, true);
            indCnstr++;
            indSlack++;
        }

        //  BasicLogger.debug("varsUpNeg", retVal);

        indSlack = Integer.MAX_VALUE;

        for (Expression expression : exprEqPos) {

            for (IntIndex key : expression.getLinearKeySet()) {
                double factor = expression.getAdjustedLinearFactor(key);
                SimplexTableauSolver.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, key, factor);
            }

            double rhs = expression.getAdjustedUpperLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            structure.setConstraintMap(indCnstr, expression, ConstraintType.EQUALITY, false);

            indCnstr++;
        }

        //  BasicLogger.debug("exprEqPos", retVal);

        for (Expression expression : exprEqNeg) {

            for (IntIndex key : expression.getLinearKeySet()) {
                double factor = -expression.getAdjustedLinearFactor(key);
                SimplexTableauSolver.set(model, retConstraintsBdy, indCnstr, basePosVars, baseNegVars, key, factor);
            }

            double rhs = -expression.getAdjustedLowerLimit();
            retConstraintsRHS.set(indCnstr, rhs);

            structure.setConstraintMap(indCnstr, expression, ConstraintType.EQUALITY, true);

            indCnstr++;
        }

        //   BasicLogger.debug("exprEqNeg", retVal);

        return retVal;
    }

    static SimplexTableau buildDual(final ConvexData<Double> convex, final Optimisation.Options options, final boolean checkFeasibility) {

        int nbCvxVars = convex.countVariables();
        int nbCvxEqus = convex.countEqualityConstraints();
        int nbCvxInes = convex.countInequalityConstraints();

        LinearStructure structure = new LinearStructure(false, 0, nbCvxVars, nbCvxEqus + nbCvxEqus + nbCvxInes, 0, 0, 0);

        SimplexTableau retVal = SimplexTableau.make(structure, options);

        Primitive2D constraintsBody = retVal.constraintsBody();
        Primitive1D constraintsRHS = retVal.constraintsRHS();
        Primitive1D objective = retVal.objective();

        MatrixStore<Double> convexC = convex.getObjective().getLinearFactors(true);

        for (int i = 0; i < nbCvxVars; i++) {
            double rhs = checkFeasibility ? ZERO : convexC.doubleValue(i);
            boolean neg = structure.negated(i, NumberContext.compare(rhs, ZERO) < 0);
            constraintsRHS.set(i, neg ? -rhs : rhs);
        }

        if (nbCvxEqus > 0) {
            for (RowView<Double> rowAE : convex.getRowsAE()) {
                int j = Math.toIntExact(rowAE.row());

                for (ElementView1D<Double, ?> element : rowAE.nonzeros()) {
                    int i = Math.toIntExact(element.index());

                    boolean neg = structure.isConstraintNegated(i);

                    double valE = element.doubleValue();
                    constraintsBody.set(i, j, neg ? -valE : valE);
                    constraintsBody.set(i, nbCvxEqus + j, neg ? valE : -valE);

                }
            }
        }

        if (nbCvxInes > 0) {
            for (RowView<Double> rowAI : convex.getRowsAI()) {
                int j = Math.toIntExact(rowAI.row());

                for (ElementView1D<Double, ?> element : rowAI.nonzeros()) {
                    int i = Math.toIntExact(element.index());

                    double valI = element.doubleValue();
                    constraintsBody.set(i, nbCvxEqus + nbCvxEqus + j, structure.isConstraintNegated(i) ? -valI : valI);
                }
            }
        }

        for (int j = 0; j < nbCvxEqus; j++) {
            double valBE = convex.getBE(j);
            objective.set(j, valBE);
            objective.set(nbCvxEqus + j, -valBE);
        }
        for (int j = 0; j < nbCvxInes; j++) {
            double valBI = convex.getBI(j);
            objective.set(nbCvxEqus + nbCvxEqus + j, valBI);
        }

        return retVal;
    }

    static SimplexTableau buildPrimal(final ConvexData<Double> convex, final Optimisation.Options options, final boolean checkFeasibility) {

        int nbVars = convex.countVariables();
        int nbEqus = convex.countEqualityConstraints();
        int nbInes = convex.countInequalityConstraints();

        LinearStructure structure = new LinearStructure(false, nbInes, nbEqus, nbVars + nbVars, 0, nbInes, 0);

        SimplexTableau retVal = SimplexTableau.make(structure, options);

        Primitive2D constraintsBody = retVal.constraintsBody();
        Primitive1D constraintsRHS = retVal.constraintsRHS();
        Primitive1D objective = retVal.objective();

        MatrixStore<Double> convexC = checkFeasibility ? Primitive64Store.FACTORY.makeZero(convex.countVariables(), 1)
                : convex.getObjective().getLinearFactors(true);

        for (int v = 0; v < nbVars; v++) {
            double valC = convexC.doubleValue(v);
            objective.set(v, -valC);
            objective.set(nbVars + v, valC);
        }

        MatrixStore<Double> convexAE = convex.getAE();
        MatrixStore<Double> convexBE = convex.getBE();

        for (int i = 0; i < nbEqus; i++) {
            double rhs = convexBE.doubleValue(i);

            boolean neg = structure.negated(i, NumberContext.compare(rhs, ZERO) < 0);

            for (int j = 0; j < nbVars; j++) {
                double valA = convexAE.doubleValue(i, j);
                constraintsBody.set(i, j, neg ? -valA : valA);
                constraintsBody.set(i, nbVars + j, neg ? valA : -valA);
            }
            constraintsRHS.set(i, neg ? -rhs : rhs);
        }

        for (RowView<Double> rowAI : convex.getRowsAI()) {

            int r = Math.toIntExact(rowAI.row());

            double rhs = convex.getBI(r);

            boolean neg = structure.negated(nbEqus + r, NumberContext.compare(rhs, ZERO) < 0);

            rowAI.nonzeros().forEach(nz -> constraintsBody.set(nbEqus + r, nz.index(), neg ? -nz.doubleValue() : nz.doubleValue()));
            rowAI.nonzeros().forEach(nz -> constraintsBody.set(nbEqus + r, nbVars + nz.index(), neg ? nz.doubleValue() : -nz.doubleValue()));
            constraintsBody.set(nbEqus + r, nbVars + nbVars + r, neg ? NEG : ONE);
            constraintsRHS.set(nbEqus + r, neg ? -rhs : rhs);
        }

        return retVal;
    }

    static Optimisation.Result doSolveDual(final ConvexData<Double> convex, final Optimisation.Options options, final boolean zeroC) {

        SimplexTableau tableau = SimplexTableauSolver.buildDual(convex, options, zeroC);

        SimplexTableauSolver solver = new SimplexTableauSolver(tableau, options);

        Result result = solver.solve();

        return SimplexTableauSolver.toConvexStateFromDual(result, convex);
    }

    static Optimisation.Result doSolvePrimal(final ConvexData<Double> convex, final Optimisation.Options options, final boolean zeroC) {

        SimplexTableau tableau = SimplexTableauSolver.buildPrimal(convex, options, zeroC);

        LinearSolver solver = new SimplexTableauSolver(tableau, options);

        Result result = solver.solve();

        return SimplexTableauSolver.toConvexStateFromPrimal(result, convex);
    }

    static int sizeOfDual(final ConvexData<?> convex) {

        int nbCvxVars = convex.countVariables();
        int nbCvxEqus = convex.countEqualityConstraints();
        int nbCvxInes = convex.countInequalityConstraints();

        int nbLinEqus = nbCvxVars;
        int nbLinVars = nbCvxEqus + nbCvxEqus + nbCvxInes + nbLinEqus;

        return (nbLinEqus + 2) * (nbLinVars + 1);
    }

    static int sizeOfPrimal(final ConvexData<?> convex) {

        int nbCvxVars = convex.countVariables();
        int nbCvxEqus = convex.countEqualityConstraints();
        int nbCvxInes = convex.countInequalityConstraints();

        int nbLinEqus = nbCvxEqus + nbCvxInes;
        int nbLinVars = nbCvxVars + nbCvxVars + nbCvxInes + nbLinEqus;

        return (nbLinEqus + 2) * (nbLinVars + 1);
    }

    private LongToNumberMap<Double> myFixedVariables = null;
    private final SimplexTableauSolver.IterationPoint myPoint;
    private final SimplexTableau myTableau;

    SimplexTableauSolver(final SimplexTableau tableau, final Optimisation.Options solverOptions) {

        super(solverOptions);

        myTableau = tableau;

        myPoint = new SimplexTableauSolver.IterationPoint();

        if (this.isLogProgress()) {
            this.log("");
            this.log("Created SimplexSolver");
            this.log("countVariables: {}", tableau.structure.countVariables());
            this.log("countProblemVariables: {}", tableau.structure.countModelVariables());
            this.log("countSlackVariables: {}", tableau.structure.nbSlck + tableau.structure.nbIdty);
            this.log("countArtificialVariables: {}", tableau.structure.nbArti);
            this.log("countVariablesTotally: {}",
                    tableau.structure.countModelVariables() + tableau.structure.nbSlck + tableau.structure.nbIdty + tableau.structure.nbArti);
            this.log("countConstraints: {}", tableau.m);
            this.log("countBasisDeficit: {}", tableau.countRemainingArtificials());
        }

        if (this.isLogDebug() && this.isTableauPrintable()) {
            this.logDebugTableau("Tableau Created");
        }
    }

    @Override
    public boolean fixVariable(final int index, final double value) {

        if (value < ZERO) {
            return false;
        }

        boolean retVal = myTableau.fixVariable(index, value);

        if (retVal) {
            if (myFixedVariables == null) {
                myFixedVariables = LongToNumberMap.factory(ArrayR064.FACTORY).make();
            }
            myFixedVariables.put(index, value);
            myPoint.returnToPhase1();
        }

        return retVal;
    }

    @Override
    public final Collection<Equation> generateCutCandidates(final double fractionality, final boolean... integer) {

        if (this.isLogDebug()) {
            BasicLogger.debug("Sol: {}", Arrays.toString(this.extractSolution().toRawCopy1D()));
            BasicLogger.debug("+++: {}", Arrays.toString(new double[integer.length]));
        }

        return myTableau.generateCutCandidates(integer, options.integer().getIntegralityTolerance(), fractionality);
    }

    @Override
    public LinearStructure getEntityMap() {
        return myTableau.structure;
    }

    @Override
    public Result solve(final Result kickStarter) {

        if (this.isLogDebug() && this.isTableauPrintable()) {
            this.logDebugTableau("Initial Tableau");
        }

        this.resetIterationsCount();

        while (this.isIterationAllowed() && this.needsAnotherIteration()) {

            this.performIteration(myPoint);

            this.incrementIterationsCount();

            if (this.isLogDebug() && this.isTableauPrintable()) {
                this.logDebugTableau("Tableau Iteration");
            }
        }

        if (this.isLogDebug() && this.isTableauPrintable()) {
            this.logDebugTableau("Final Tableau");
        }

        // BasicLogger.debug("Total iters: {}", this.countIterations());

        return this.buildResult();
    }

    /**
     * https://math.stackexchange.com/questions/3254444/artificial-variables-in-two-phase-simplex-method
     */
    private void cleanUpPhase1Artificials() {

        int[] basis = myTableau.included;
        int[] excluded = myTableau.excluded();

        int colRHS = myTableau.n;

        for (int i = 0; i < basis.length; i++) {

            if (myTableau.isArtificial(basis[i])) {

                double rhs = myTableau.doubleValue(i, colRHS);

                if (options.validate && !PHASE1.isZero(rhs)) {
                    this.log("Non-zero RHS artificial variable: {} = {}", i, rhs);
                }

                int enter = -1;
                double maxPivot = ZERO;
                for (int j = 0; j < excluded.length; j++) {
                    int posEnt = excluded[j];
                    double pivot = myTableau.doubleValue(i, posEnt);
                    if (pivot > maxPivot && !PIVOT.isZero(pivot)) {
                        maxPivot = pivot;
                        enter = posEnt;
                    }
                }

                if (enter >= 0) {
                    myPoint.row = i;
                    myPoint.col = enter;
                    this.performIteration(myPoint);
                }
            }
        }
    }

    private int getRowObjective() {
        return myPoint.isPhase1() ? myTableau.m + 1 : myTableau.m;
    }

    private double infeasibility() {
        return -myTableau.value(true);
    }

    private boolean isTableauPrintable() {
        return myTableau.count() <= 512L;
    }

    private void logDebugTableau(final String message) {
        this.log(message + "; Basics: " + Arrays.toString(myTableau.included), myTableau);
        // this.debug("New/alt " + message + "; Basics: " + Arrays.toString(myBasis), myTableau);
    }

    private int phase() {
        return myPoint.isPhase2() ? 2 : 1;
    }

    private double value() {
        return -myTableau.value(false);
    }

    protected Result buildResult() {

        Access1D<?> solution = this.extractSolution();
        double value = this.evaluateFunction(solution);
        Optimisation.State state = this.getState();

        Result result = new Optimisation.Result(state, value, solution);

        if (myTableau.isAbleToExtractDual()) {

            ConstraintsMap constraints = this.getEntityMap().constraints;

            if (constraints.isEntityMap()) {
                return result.multipliers(constraints, this.extractMultipliers());
            } else {
                return result.multipliers(this.extractMultipliers());
            }
        }

        return result;
    }

    protected double evaluateFunction(final Access1D<?> solution) {
        return -myTableau.value(false);
    }

    protected Access1D<?> extractMultipliers() {

        Access1D<Double> duals = myTableau.sliceDualVariables();

        LinearStructure structure = myTableau.structure;

        return new Access1D<Double>() {

            @Override
            public long count() {
                return structure.countConstraints();
            }

            @Override
            public double doubleValue(final int index) {
                int i = Math.toIntExact(index);
                double dualValue = duals.doubleValue(index);
                return structure.isConstraintNegated(i) ? -dualValue : dualValue;
            }

            @Override
            public Double get(final long index) {
                return Double.valueOf(this.doubleValue(index));
            }

            @Override
            public String toString() {
                return Access1D.toString(this);
            }
        };
    }

    /**
     * Extract solution MatrixStore from the tableau. Should be able to feed this to
     * {@link #evaluateFunction(Access1D)}.
     */
    protected Access1D<?> extractSolution() {

        int colRHS = myTableau.n;

        Primitive64Store solution = Primitive64Store.FACTORY.make(myTableau.structure.countVariables(), 1);

        int numberOfConstraints = myTableau.m;
        for (int row = 0; row < numberOfConstraints; row++) {
            int variableIndex = myTableau.included[row];
            if (!myTableau.isArtificial(variableIndex)) {
                solution.set(variableIndex, myTableau.doubleValue(row, colRHS));
            }
        }

        if (myFixedVariables != null) {
            for (NonzeroView<Double> entry : myFixedVariables.nonzeros()) {
                solution.set(entry.index(), entry.doubleValue());
            }
        }

        return solution;
    }

    protected boolean initialise(final Result kickStarter) {
        return false;
    }

    protected boolean needsAnotherIteration() {

        if (this.isLogDebug()) {
            this.log();
            this.log("Needs Another Iteration? Phase={} Artificials={} Infeasibility={} Objective={}", this.phase(), myTableau.countRemainingArtificials(),
                    this.infeasibility(), this.value());
        }

        boolean retVal = false;
        myPoint.reset();

        if (myPoint.isPhase1() && (PHASE1.isZero(this.infeasibility()) || !myTableau.isRemainingArtificials())) {

            this.cleanUpPhase1Artificials();

            if (this.isLogDebug()) {
                this.log();
                this.log("Switching to Phase2 with {} artificial variable(s) still in the basis and infeasibility {}.", myTableau.countRemainingArtificials(),
                        this.infeasibility());
                this.log();
            }

            // BasicLogger.debug("Phase1 iters: {}", this.countIterations());

            myPoint.switchToPhase2();
            this.setState(Optimisation.State.FEASIBLE);
        }

        myPoint.col = this.findNextPivotCol();

        if (myPoint.col >= 0) {

            myPoint.row = this.findNextPivotRow();

            if (myPoint.row >= 0) {

                retVal = true;

            } else {

                if (myPoint.isPhase2()) {
                    this.setState(State.UNBOUNDED);
                } else {
                    this.setState(State.INFEASIBLE);
                }

                retVal = false;
            }

        } else {

            if (myPoint.isPhase1()) {
                this.setState(State.INFEASIBLE);
            } else {
                this.setState(State.OPTIMAL);
            }

            retVal = false;
        }

        if (this.isLogDebug()) {
            if (retVal) {
                this.log("\n==>>\tRow: {},\tExit: {},\tColumn/Enter: {}.\n", myPoint.row, myTableau.included[myPoint.row], myPoint.col);
            } else {
                this.log("\n==>>\tNo more iterations needed/possible.\n");
            }
        }

        return retVal;
    }

    protected boolean validate() {

        boolean retVal = true;
        this.setState(State.VALID);

        return retVal;
    }

    int findNextPivotCol() {

        int row = this.getRowObjective();

        boolean phase2 = myPoint.isPhase2();

        int nbVariables = myTableau.structure.countVariables();

        if (this.isLogDebug()) {
            if (options.validate) {
                int[] excluded = myTableau.excluded();
                Access1D<Double> sliceTableauRow = myTableau.sliceTableauRow(row);
                double[] exclVals = new double[excluded.length];
                for (int i = 0; i < exclVals.length; i++) {
                    exclVals[i] = sliceTableauRow.doubleValue(excluded[i]);
                }
                this.log("\nfindNextPivotCol (index of most negative value) among these:\n{}", Arrays.toString(exclVals));
            } else {
                this.log("\nfindNextPivotCol");
            }
        }

        int retVal = -1;

        double tmpVal;
        double minVal = phase2 ? -ACC.epsilon() : ZERO;

        // for (int e = 0; e < excluded.length; e++) {
        for (int j = 0; j < nbVariables; j++) {
            if (myTableau.isExcluded(j)) {
                tmpVal = myTableau.doubleValue(row, j);
                if (tmpVal < minVal && (retVal < 0 || WEIGHT.isDifferent(minVal, tmpVal))) {
                    retVal = j;
                    minVal = tmpVal;
                    if (this.isLogDebug()) {
                        this.log("Col: {}\t=>\tReduced Contribution Weight: {}.", j, tmpVal);
                    }
                }
            }
        }

        return retVal;
    }

    int findNextPivotRow() {

        int numerCol = myTableau.n;
        int denomCol = myPoint.col;

        boolean phase1 = myPoint.isPhase1();
        boolean phase2 = myPoint.isPhase2();

        if (this.isLogDebug()) {
            if (options.validate) {
                Access1D<Double> numerators = myTableau.sliceBodyColumn(numerCol);
                Access1D<Double> denominators = myTableau.sliceBodyColumn(denomCol);
                Array1D<Double> ratios = Array1D.R064.copy(numerators);
                ratios.modifyMatching(DIVIDE, denominators);
                this.log("\nfindNextPivotRow (smallest positive ratio) among these:\nNumerators={}\nDenominators={}\nRatios={}", numerators, denominators,
                        ratios);
            } else {
                this.log("\nfindNextPivotRow");
            }
        }

        int retVal = -1;
        double numer = NaN, denom = NaN, ratio = NaN, minRatio = MACHINE_LARGEST, curDenom = MACHINE_SMALLEST;

        int constraintsCount = myTableau.m;
        for (int i = 0; i < constraintsCount; i++) {

            // Numerator/RHS: Should always be >=0.0, but very small numbers may "accidentally" get a negative sign.
            numer = Math.abs(myTableau.doubleValue(i, numerCol));

            // Denominator/Pivot
            denom = myTableau.doubleValue(i, denomCol);

            // Phase 2, artificial variable still in basis & RHS ≈ 0.0
            int basisColumnIndex = myTableau.included[i];
            boolean artificial = myTableau.isArtificial(basisColumnIndex);
            boolean degenerate = artificial && DEGENERATE.isZero(numer);
            boolean specialCase = phase2 && degenerate;

            if (specialCase) {
                ratio = ZERO;
            } else {
                ratio = numer / denom;
            }

            if ((denom > ZERO || specialCase) && !PIVOT.isZero(denom)) {

                if (ratio >= ZERO && (ratio < minRatio || !RATIO.isDifferent(minRatio, ratio) && denom > curDenom)) {

                    retVal = i;
                    minRatio = ratio;
                    // curDenom = denom;
                    curDenom = degenerate ? Math.max(denom, ONE) : denom;

                    if (this.isLogDebug()) {
                        this.log("Row: {}\t=>\tRatio: {},\tNumerator/RHS: {}, \tDenominator/Pivot: {},\tArtificial: {}.", i, ratio, numer, denom, artificial);
                    }
                }
            }
        }

        return retVal;
    }

    void performIteration(final SimplexTableauSolver.IterationPoint pivot) {

        double tmpPivotElement = myTableau.doubleValue(pivot.row, pivot.col);
        int tmpColRHS = myTableau.n;
        double tmpPivotRHS = myTableau.doubleValue(pivot.row, tmpColRHS);

        myTableau.pivot(pivot);

        if (this.isLogDebug()) {
            this.log("Iteration Point <{},{}>\tPivot: {} => {}\tRHS: {} => {}.", pivot.row, pivot.col, tmpPivotElement,
                    myTableau.doubleValue(pivot.row, pivot.col), tmpPivotRHS, myTableau.doubleValue(pivot.row, tmpColRHS));
        }

        if (this.isLogDebug() && options.validate) {

            // Right-most column of the tableau
            Access1D<Double> colRHS = myTableau.sliceConstraintsRHS();

            double tmpRHS;
            double minRHS = Double.MAX_VALUE;
            for (int i = 0; i < colRHS.size(); i++) {
                tmpRHS = colRHS.doubleValue(i);
                if (tmpRHS < minRHS) {
                    minRHS = tmpRHS;
                    if (minRHS < ZERO) {
                        this.log("Negative RHS {} @ Row: {}", minRHS, i);
                        this.log();
                    }
                }
            }

            if (minRHS < ZERO && !ACC.isZero(minRHS) && this.isLogDebug()) {
                this.log("Entire RHS columns: {}", colRHS);
                this.log();
            }

        }
    }

}
