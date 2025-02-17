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

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.ojalgo.array.operation.IndexOf;
import org.ojalgo.equation.Equation;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.optimisation.ConstraintsMetaData;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.convex.ConvexData;
import org.ojalgo.optimisation.linear.SimplexStore.ColumnState;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.RowView;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Primitive1D;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.context.NumberContext;

/**
 * Meant to replace {@link SimplexTableauSolver}. It is already better in many aspects, but still can't do
 * everything required to fully replace the old solver.
 *
 * @author apete
 */
abstract class SimplexSolver extends LinearSolver {

    enum Direction {
        DECREASE, INCREASE, STAY;
    }

    /**
     * Enter to {@link ColumnState#BASIS} from either {@link ColumnState#LOWER}, {@link ColumnState#UPPER}, or
     * {@link ColumnState#UNBOUNDED}.
     * <p>
     * In case of a bound-switch nothing enters the basis. In such case this type declares "the switch" rather
     * than the enter-part of a normal basis-update.
     *
     * @author apete
     */
    static final class EnterInfo {

        private final int[] myExcluded;

        /**
         * The variable (indicated by {@link #index}) that will enter the basis, will either increase or
         * decrease when entering.
         */
        Direction direction = Direction.STAY;
        ColumnState from = ColumnState.BASIS;
        int index = -1;

        EnterInfo(final int[] excluded) {
            super();
            myExcluded = excluded;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof EnterInfo)) {
                return false;
            }
            EnterInfo other = (EnterInfo) obj;
            if (direction != other.direction || from != other.from || index != other.index) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (direction == null ? 0 : direction.hashCode());
            result = prime * result + (from == null ? 0 : from.hashCode());
            return prime * result + index;
        }

        @Override
        public String toString() {
            return this.column() + "(" + index + ") " + from + " " + direction;
        }

        int column() {
            return index >= 0 ? myExcluded[index] : index;
        }

        void column(final int column) {
            index = this.indexOf(column);
        }

        int indexOf(final int column) {
            return IndexOf.indexOf(myExcluded, column);
        }

        void reset() {
            index = -1;
            from = ColumnState.BASIS;
            direction = Direction.STAY;
        }

    }

    /**
     * Exit from {@link ColumnState#BASIS} to either {@link ColumnState#LOWER} or {@link ColumnState#UPPER}.
     * <p>
     * In case of a bound-switch nothing leaves the basis, and this type bears no meaning.
     *
     * @author apete
     */
    static final class ExitInfo {

        private final int[] myIncluded;

        /**
         * The variable (indicated by {@link #index}) that will exit the basis, will either increase or
         * decrease when exiting.
         */
        Direction direction = Direction.STAY;
        int index = -1;
        ColumnState to = ColumnState.BASIS;

        ExitInfo(final int[] included) {
            super();
            myIncluded = included;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ExitInfo)) {
                return false;
            }
            ExitInfo other = (ExitInfo) obj;
            if (direction != other.direction || index != other.index || to != other.to) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (direction == null ? 0 : direction.hashCode());
            result = prime * result + index;
            return prime * result + (to == null ? 0 : to.hashCode());
        }

        @Override
        public String toString() {
            return this.column() + "(" + index + ") " + direction + " " + to;
        }

        int column() {
            return index >= 0 ? myIncluded[index] : index;
        }

        int indexOf(final int column) {
            return IndexOf.indexOf(myIncluded, column);
        }

        void reset() {
            index = -1;
            to = ColumnState.BASIS;
            direction = Direction.STAY;
        }

        int row() {
            return index;
        }

    }

    static final class IterDescr {

        final EnterInfo enter;
        final ExitInfo exit;

        IterDescr(final SimplexStore simplex) {
            super();
            enter = new EnterInfo(simplex.excluded);
            exit = new ExitInfo(simplex.included);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof IterDescr)) {
                return false;
            }
            IterDescr other = (IterDescr) obj;
            if (enter == null) {
                if (other.enter != null) {
                    return false;
                }
            } else if (!enter.equals(other.enter)) {
                return false;
            }
            if (exit == null) {
                if (other.exit != null) {
                    return false;
                }
            } else if (!exit.equals(other.exit)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (enter == null ? 0 : enter.hashCode());
            return prime * result + (exit == null ? 0 : exit.hashCode());
        }

        @Override
        public String toString() {
            if (this.isBasisUpdate()) {
                return exit.toString() + " -> " + enter.toString();
            } else if (this.isBoundFlip()) {
                return enter.toString() + " -> " + exit.to;
            } else {
                return "-";
            }
        }

        int column() {
            return enter.column();
        }

        /**
         * Normal basis update – one variable leaves the basis and another enters.
         */
        boolean isBasisUpdate() {
            return enter.index >= 0 && exit.index >= 0;
        }

        /**
         * Change from {@link ColumnState#LOWER} to {@link ColumnState#UPPER} or vice versa. (No change in
         * basis.)
         */
        boolean isBoundFlip() {
            return enter.index >= 0 && enter.direction != Direction.STAY && enter.direction == exit.direction;
        }

        boolean isNoOperation() {
            return !this.isBasisUpdate() && !this.isBoundFlip();
        }

        void markAsBoundFlip() {
            exit.reset();
            if (enter.direction == Direction.INCREASE) {
                exit.direction = Direction.INCREASE;
                exit.to = ColumnState.UPPER;
            } else {
                exit.direction = Direction.DECREASE;
                exit.to = ColumnState.LOWER;
            }
        }

        void reset() {
            exit.reset();
            enter.reset();
        }

        int row() {
            return exit.row();
        }

    }

    private static final NumberContext PIVOT = NumberContext.of(6).withMode(RoundingMode.HALF_DOWN);
    private static final NumberContext RATIO = NumberContext.of(8).withMode(RoundingMode.HALF_DOWN);

    static <S extends SimplexStore> S build(final ExpressionsBasedModel model, final Function<LinearStructure, S> factory) {

        Set<IntIndex> fixedVariables = model.getFixedVariables();
        List<Variable> freeVariables = model.getFreeVariables();

        List<Expression> equalConstraints = new ArrayList<>();
        List<Expression> lowerConstraints = new ArrayList<>();
        List<Expression> upperConstraints = new ArrayList<>();

        model.constraints().map(constraint -> constraint.compensate(fixedVariables)).forEach(constraint -> {
            if (constraint.isEqualityConstraint()) {
                equalConstraints.add(constraint);
            } else {
                if (constraint.isLowerConstraint()) {
                    lowerConstraints.add(constraint);
                }
                if (constraint.isUpperConstraint()) {
                    upperConstraints.add(constraint);
                }
            }
        });

        Expression objective = model.objective().compensate(fixedVariables);

        int nbUpConstr = upperConstraints.size();
        int nbLoConstr = lowerConstraints.size();
        int nbEqConstr = equalConstraints.size();

        int nbProbVars = freeVariables.size();
        int nbSlckVars = nbUpConstr + nbLoConstr;

        LinearStructure structure = new LinearStructure(true, nbUpConstr + nbLoConstr, nbEqConstr, nbProbVars, 0, 0, nbSlckVars);

        S simplex = factory.apply(structure);
        double[] lowerBounds = simplex.getLowerBounds();
        double[] upperBounds = simplex.getUpperBounds();

        Mutate2D mtrxA = simplex.constraintsBody();
        Mutate1D mtrxB = simplex.constraintsRHS();
        Mutate1D mtrxC = simplex.objective();

        for (int i = 0; i < nbUpConstr; i++) {
            Expression expression = upperConstraints.get(i);
            for (IntIndex key : expression.getLinearKeySet()) {
                int column = model.indexOfFreeVariable(key);
                double factor = expression.doubleValue(key, true);
                mtrxA.set(i, column, factor);
            }
            mtrxA.set(i, nbProbVars + i, ONE);
            mtrxB.set(i, expression.getUpperLimit(true, POSITIVE_INFINITY));
            lowerBounds[nbProbVars + i] = ZERO;
            upperBounds[nbProbVars + i] = POSITIVE_INFINITY;
            structure.setConstraintMap(i, expression, ConstraintType.UPPER, false);
        }

        for (int i = 0; i < nbLoConstr; i++) {
            Expression expression = lowerConstraints.get(i);
            for (IntIndex key : expression.getLinearKeySet()) {
                int column = model.indexOfFreeVariable(key);
                double factor = expression.doubleValue(key, true);
                mtrxA.set(nbUpConstr + i, column, factor);
            }
            mtrxA.set(nbUpConstr + i, nbProbVars + nbUpConstr + i, ONE);
            mtrxB.set(nbUpConstr + i, expression.getLowerLimit(true, NEGATIVE_INFINITY));
            lowerBounds[nbProbVars + nbUpConstr + i] = NEGATIVE_INFINITY;
            upperBounds[nbProbVars + nbUpConstr + i] = ZERO;
            structure.setConstraintMap(nbUpConstr + i, expression, ConstraintType.LOWER, true);
        }

        for (int i = 0; i < nbEqConstr; i++) {
            Expression expression = equalConstraints.get(i);
            for (IntIndex key : expression.getLinearKeySet()) {
                int column = model.indexOfFreeVariable(key);
                double factor = expression.doubleValue(key, true);
                mtrxA.set(nbUpConstr + nbLoConstr + i, column, factor);
            }
            mtrxA.set(nbUpConstr + nbLoConstr + i, nbProbVars + nbSlckVars + i, ONE);
            mtrxB.set(nbUpConstr + nbLoConstr + i, expression.getUpperLimit(true, ZERO));
            lowerBounds[nbProbVars + nbSlckVars + i] = ZERO;
            upperBounds[nbProbVars + nbSlckVars + i] = ZERO;
            structure.setConstraintMap(nbUpConstr + nbLoConstr + i, expression, ConstraintType.EQUALITY, false);
        }

        for (int i = 0; i < nbProbVars; i++) {
            Variable variable = freeVariables.get(i);
            double lb = lowerBounds[i] = variable.getLowerLimit(false, NEGATIVE_INFINITY);
            double ub = upperBounds[i] = variable.getUpperLimit(false, POSITIVE_INFINITY);
            structure.positivePartVariables[i] = model.indexOf(variable);

            if (lb > ub) {
                throw new IllegalStateException();
            }

            if (Double.isInfinite(lb) && Double.isInfinite(ub)) {
                simplex.unbounded(i);
            } else if (Math.abs(ub) < Math.abs(lb)) {
                simplex.upper(i);
            } else {
                simplex.lower(i);
            }
        }

        structure.setObjectiveAdjustmentFactor(objective.getAdjustmentFactor());
        boolean negate = model.getOptimisationSense() == Optimisation.Sense.MAX;
        for (IntIndex key : objective.getLinearKeySet()) {
            double weight = objective.doubleValue(key, true);
            mtrxC.set(model.indexOfFreeVariable(key), negate ? -weight : weight);
        }

        return simplex;
    }

    static Optimisation.Result doSolveConvexAsDual(final ConvexData<Double> convex, final Optimisation.Options options, final boolean zeroC) {

        int nbCvxVars = convex.countVariables();
        int nbCvxEqus = convex.countEqualityConstraints();
        int nbCvxInes = convex.countInequalityConstraints();

        MatrixStore<Double> cvxC = convex.getObjective().getLinearFactors(true);
        MatrixStore<Double> cvxBE = convex.getBE();
        MatrixStore<Double> cvxBI = convex.getBI();

        LinearStructure structure = new LinearStructure(false, 0, nbCvxVars, nbCvxEqus + nbCvxInes, 0, 0, 0);

        SimplexStore store = SimplexStore.newStoreFactory(options).apply(structure);

        double[] lb = store.getLowerBounds();
        double[] ub = store.getUpperBounds();
        for (int j = 0; j < nbCvxEqus; j++) {
            lb[j] = NEGATIVE_INFINITY;
            ub[j] = POSITIVE_INFINITY;
        }
        for (int j = 0; j < nbCvxInes; j++) {
            lb[nbCvxEqus + j] = ZERO;
            ub[nbCvxEqus + j] = POSITIVE_INFINITY;
        }

        Mutate2D constrBody = store.constraintsBody();
        Mutate1D constrRHS = store.constraintsRHS();
        Mutate1D objective = store.objective();

        for (int i = 0; i < nbCvxVars; i++) {
            constrRHS.set(i, zeroC ? ZERO : cvxC.doubleValue(i));
        }

        for (RowView<Double> rowAE : convex.getRowsAE()) {
            objective.set(rowAE.row(), cvxBE.doubleValue(rowAE.row()));
            rowAE.nonzeros().forEach(nz -> constrBody.set(nz.index(), rowAE.row(), nz.doubleValue()));
        }

        for (RowView<Double> rowAI : convex.getRowsAI()) {
            objective.set(nbCvxEqus + rowAI.row(), cvxBI.doubleValue(rowAI.row()));
            rowAI.nonzeros().forEach(nz -> constrBody.set(nz.index(), nbCvxEqus + rowAI.row(), nz.doubleValue()));
        }

        LinearSolver solver = store.newPhasedSimplexSolver(options);

        Result result = solver.solve();
        Access1D<?> multiplierNumbers = result.getMultipliers().get();

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
                return -multiplierNumbers.doubleValue(index);
            }

            @Override
            public void set(final int index, final double value) {
                throw new IllegalArgumentException();
            }

            @Override
            public int size() {
                return multiplierNumbers.size();
            }

        };

        Primitive1D retMultipliers = new Primitive1D() {

            @Override
            public double doubleValue(final int index) {

                return result.doubleValue(index);

            }

            @Override
            public void set(final int index, final double value) {
                throw new IllegalArgumentException();
            }

            @Override
            public int size() {
                return nbCvxEqus + nbCvxInes;
            }

        };

        Optimisation.Result retVal = new Optimisation.Result(retState, retValue, retSolution);
        retVal.multipliers(retMultipliers);
        return retVal;
    }

    static Optimisation.Result doSolveConvexAsPrimal(final ConvexData<Double> convex, final Optimisation.Options options, final boolean zeroC) {

        int nbCvxVars = convex.countVariables();
        int nbCvxEqus = convex.countEqualityConstraints();
        int nbCvxInes = convex.countInequalityConstraints();

        MatrixStore<Double> cvxC = convex.getObjective().getLinearFactors(true);
        MatrixStore<Double> cvxBE = convex.getBE();
        MatrixStore<Double> cvxBI = convex.getBI();

        LinearStructure structure = new LinearStructure(false, nbCvxInes, nbCvxEqus, nbCvxVars, 0, 0, nbCvxInes);

        SimplexStore store = SimplexStore.newStoreFactory(options).apply(structure);

        double[] lb = store.getLowerBounds();
        double[] ub = store.getUpperBounds();
        for (int j = 0; j < nbCvxVars; j++) {
            lb[j] = NEGATIVE_INFINITY;
            ub[j] = POSITIVE_INFINITY;
        }
        for (int j = 0; j < nbCvxInes; j++) {
            lb[nbCvxVars + j] = ZERO;
            ub[nbCvxVars + j] = POSITIVE_INFINITY;
        }
        for (int j = 0; j < structure.nbArti; j++) {
            lb[nbCvxVars + nbCvxInes + j] = ZERO;
            ub[nbCvxVars + nbCvxInes + j] = ZERO;
        }

        Mutate2D constrBody = store.constraintsBody();
        Mutate1D constrRHS = store.constraintsRHS();
        Mutate1D objective = store.objective();

        if (!zeroC) {
            for (int j = 0; j < nbCvxVars; j++) {
                objective.set(j, -cvxC.doubleValue(j));
            }
        }

        for (RowView<Double> rowAI : convex.getRowsAI()) {
            rowAI.nonzeros().forEach(nz -> constrBody.set(rowAI.row(), nz.index(), nz.doubleValue()));
            constrBody.set(rowAI.row(), nbCvxVars + rowAI.row(), ONE);
            constrRHS.set(rowAI.row(), cvxBI.doubleValue(rowAI.row()));
        }

        for (RowView<Double> rowAE : convex.getRowsAE()) {
            rowAE.nonzeros().forEach(nz -> constrBody.set(nbCvxInes + rowAE.row(), nz.index(), nz.doubleValue()));
            constrRHS.set(nbCvxInes + rowAE.row(), cvxBE.doubleValue(rowAE.row()));
        }

        LinearSolver solver = store.newPhasedSimplexSolver(options);

        Result result = solver.solve();
        Access1D<?> multiplierNumbers = result.getMultipliers().get();

        State retState = result.getState();

        double retValue = result.getValue();

        Primitive1D retSolution = new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return result.doubleValue(index);
            }

            @Override
            public void set(final int index, final double value) {
                throw new IllegalArgumentException();
            }

            @Override
            public int size() {
                return nbCvxVars;
            }

        };

        Primitive1D retMultipliers = new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                if (index < nbCvxEqus) {
                    return multiplierNumbers.doubleValue(nbCvxInes + index);
                } else {
                    return multiplierNumbers.doubleValue(index - nbCvxEqus);
                }
            }

            @Override
            public void set(final int index, final double value) {
                throw new IllegalArgumentException();
            }

            @Override
            public int size() {
                return nbCvxEqus + nbCvxInes;
            }

        };

        Optimisation.Result retVal = new Optimisation.Result(retState, retValue, retSolution);
        retVal.multipliers(retMultipliers);
        return retVal;
    }

    static int sizeOfDual(final ConvexData<?> convex) {

        int nbCvxVars = convex.countVariables();
        int nbCvxEqus = convex.countEqualityConstraints();
        int nbCvxInes = convex.countInequalityConstraints();

        int nbLinEqus = nbCvxVars;
        int nbLinVars = nbCvxEqus + nbCvxInes + nbLinEqus;

        return (nbLinEqus + 2) * (nbLinVars + 1);
    }

    static int sizeOfPrimal(final ConvexData<?> convex) {

        int nbCvxVars = convex.countVariables();
        int nbCvxEqus = convex.countEqualityConstraints();
        int nbCvxInes = convex.countInequalityConstraints();

        int nbLinEqus = nbCvxEqus + nbCvxInes;
        int nbLinVars = nbCvxVars + nbLinEqus;

        return (nbLinEqus + 2) * (nbLinVars + 1);
    }

    private final SimplexStore mySimplex;
    private final double[] mySolutionShift;
    private double myValueShift = ZERO;

    SimplexSolver(final Optimisation.Options solverOptions, final SimplexStore simplexStore) {
        super(solverOptions);
        mySimplex = simplexStore;
        mySolutionShift = new double[simplexStore.n];
    }

    @Override
    public boolean fixVariable(final int index, final double value) {
        return this.updateRange(index, value, value);
    }

    @Override
    public final Collection<Equation> generateCutCandidates(final double fractionality, final boolean... integer) {

        NumberContext integralityTolerance = options.integer().getIntegralityTolerance();

        return mySimplex.generateCutCandidates(integer, integralityTolerance, fractionality, mySolutionShift);
    }

    @Override
    public LinearStructure getEntityMap() {
        return mySimplex.structure;
    }

    @Override
    public boolean updateRange(final int index, final double lower, final double upper) {
        double shift = mySolutionShift[index];
        this.setState(State.UNEXPLORED);
        return mySimplex.updateRange(index, lower - shift, upper - shift);
    }

    private Access1D<?> extractMultipliers() {

        Access1D<Double> duals = mySimplex.sliceDualVariables();

        LinearStructure structure = mySimplex.structure;

        return new Access1D<Double>() {

            @Override
            public double doubleValue(final int index) {
                int i = Math.toIntExact(index);
                return structure.isConstraintNegated(i) ? -duals.doubleValue(index) : duals.doubleValue(index);
            }

            @Override
            public Double get(final long index) {
                return Double.valueOf(this.doubleValue(index));
            }

            @Override
            public int size() {
                return structure.countConstraints();
            }

            @Override
            public String toString() {
                return Access1D.toString(this);
            }
        };
    }

    private double[] extractSolution() {

        double[] retVal = mySimplex.extractSolution();

        for (int i = 0; i < mySolutionShift.length; i++) {
            retVal[i] += mySolutionShift[i];
        }

        return retVal;
    }

    private double extractValue() {
        return mySimplex.extractValue() + myValueShift;
    }

    private boolean getDualExitCandidate(final IterDescr iteration) {

        if (iteration != null && this.isLogDebug()) {
            this.log();
            this.log("getDualExitCandidate");
        }

        boolean retVal = false;

        boolean printable = mySimplex.isPrintable();

        ExitInfo exit = null;
        if (iteration != null) {
            exit = iteration.exit;
        }

        double largest = MACHINE_SMALLEST;
        int[] included = mySimplex.included;
        for (int ji = included.length - 1; ji >= 0; ji--) {
            int j = included[ji];

            double candidate = mySimplex.getInfeasibility(ji);
            double magnitude = Math.abs(candidate);
            double weight = mySimplex.edgeWeights[ji];
            double score = (magnitude * magnitude) / weight;

            if (printable && this.isLogDebug()) {
                this.log(1, "{}({}) {}", j, ji, candidate);
            }

            if (magnitude > 1e-10 && score > largest) {

                if (exit != null) {

                    exit.index = ji;

                    if (candidate > ZERO) {
                        exit.direction = Direction.DECREASE;
                        exit.to = ColumnState.UPPER;
                    } else {
                        exit.direction = Direction.INCREASE;
                        exit.to = ColumnState.LOWER;
                    }

                    if (this.isLogDebug()) {
                        this.log(2, "{}/{}={} => {}", candidate, weight, score, exit);
                    }
                }

                largest = score;
                retVal = true;
            }
        }

        if (iteration != null && this.isLogDebug()) {
            this.log("==>> {}", exit);
        }

        return retVal;
    }

    private double getLowerBound(final int index) {
        return mySimplex.getLowerBound(index) + mySolutionShift[index];
    }

    private double[] getLowerBounds() {

        double[] retVal = new double[mySolutionShift.length];

        for (int j = 0; j < retVal.length; j++) {
            retVal[j] = mySimplex.getLowerBound(j) + mySolutionShift[j];
        }

        return retVal;
    }

    private boolean getPrimalEnterCandidate(final IterDescr iteration) {

        if (iteration != null && this.isLogDebug()) {
            this.log();
            this.log("getPrimalEnterCandidate");
        }

        boolean retVal = false;

        boolean printable = mySimplex.isPrintable();

        EnterInfo enter = null;
        if (iteration != null) {
            enter = iteration.enter;
        }

        int n = mySimplex.structure.countVariables();
        double largest = MACHINE_SMALLEST;
        int[] excluded = mySimplex.excluded;
        for (int je = 0; je < excluded.length; je++) {
            int j = excluded[je];
            if (j < n) {

                ColumnState columnState = mySimplex.getColumnState(j);
                double candidate = mySimplex.getReducedCost(je);
                double magnitude = Math.abs(candidate);
                double weight = mySimplex.edgeWeights[je];
                double score = (magnitude * magnitude) / weight;

                if (printable && this.isLogDebug()) {
                    this.log(1, "{}({}) {} @ {}", j, je, candidate, columnState);
                }

                if (magnitude > 1e-10 && score > largest) {

                    if (candidate <= ZERO && columnState != ColumnState.UPPER) {

                        if (enter != null) {

                            enter.index = je;
                            enter.from = columnState;
                            enter.direction = Direction.INCREASE;

                            if (this.isLogDebug()) {
                                this.log(2, "{}/{}={} => {}", candidate, weight, score, enter);
                            }
                        }

                        largest = score;
                        retVal = true;

                    } else if (candidate >= ZERO && columnState != ColumnState.LOWER) {

                        if (enter != null) {

                            enter.index = je;
                            enter.from = columnState;
                            enter.direction = Direction.DECREASE;

                            if (this.isLogDebug()) {
                                this.log(2, "{}/{}={} => {}", candidate, weight, score, enter);
                            }
                        }

                        largest = score;
                        retVal = true;
                    }
                }
            }
        }

        if (iteration != null && this.isLogDebug()) {
            this.log("==>> {}", enter);
        }

        return retVal;
    }

    private double getUpperBound(final int index) {
        return mySimplex.getUpperBound(index) + mySolutionShift[index];
    }

    private double[] getUpperBounds() {

        double[] retVal = new double[mySolutionShift.length];

        for (int j = 0; j < retVal.length; j++) {
            retVal[j] = mySimplex.getUpperBound(j) + mySolutionShift[j];
        }

        return retVal;
    }

    private void logCurrentState() {

        this.log();
        this.log("{} iteration {}, partition {}", this.getState(), this.countIterations(), mySimplex.toString());

        double[] lb = this.getLowerBounds();
        this.log("LB: {}", lb);
        double[] x = this.extractSolution();
        this.log(" X: {}", x);
        double[] ub = this.getUpperBounds();
        this.log("UB: {}", ub);

        for (int j = 0; j < x.length; j++) {
            if (lb[j] > x[j] || ub[j] < x[j]) {
                this.log("!!! {} < {} < {}", lb[j], x[j], ub[j]);
            }
        }

        if (mySimplex.isPrintable() && mySimplex instanceof Access2D<?>) {
            this.log(Arrays.toString(mySimplex.included), (Access2D<?>) mySimplex);
        } else {
            this.log(Arrays.toString(mySimplex.included));
        }
    }

    private double shift(final int column, final ColumnState state) {

        double shift = ZERO;
        if (state == ColumnState.LOWER) {
            shift = mySimplex.getLowerBound(column);
        } else if (state == ColumnState.UPPER) {
            shift = mySimplex.getUpperBound(column);
        }

        if (shift != ZERO) {
            mySimplex.shiftColumn(column, shift);
            mySolutionShift[column] += shift;
            myValueShift += mySimplex.getCost(column) * shift;
        }

        return shift;
    }

    private Optimisation.Result solveUnconstrained() {

        int nbVars = mySimplex.n;

        double retValue = ZERO;
        double[] retSolution = new double[nbVars];
        State retState = State.OPTIMAL;

        for (int j = 0; j < nbVars; j++) {

            double cost = mySimplex.getCost(j);
            double value;

            if (cost == ZERO) {
                value = mySolutionShift[j];
                retSolution[j] = value;
            } else if (cost > ZERO) {
                value = this.getLowerBound(j);
                if (Double.isFinite(value)) {
                    retSolution[j] = value;
                    retValue += value * cost;
                } else {
                    retSolution[j] = ZERO;
                    retState = State.UNBOUNDED;
                }
            } else if (cost < ZERO) {
                value = this.getUpperBound(j);
                if (Double.isFinite(value)) {
                    retSolution[j] = value;
                    retValue += value * cost;
                } else {
                    retSolution[j] = ZERO;
                    retState = State.UNBOUNDED;
                }
            }
        }

        return Result.of(retValue, retState, retSolution);
    }

    private boolean testDualEnterRatio(final IterDescr iteration) {

        if (this.isLogDebug()) {
            this.log();
            this.log("testDualEnterRatio");
        }

        boolean printable = mySimplex.isPrintable();

        ExitInfo exit = iteration.exit;
        EnterInfo enter = iteration.enter;
        Direction exitDirection = exit.direction;

        double numer = ZERO;
        double denom = ONE;
        double ratio = ZERO;
        double scale = ONE;

        double iterationRatio = Double.MAX_VALUE;
        double iterationScale = MACHINE_LARGEST;

        int n = mySimplex.structure.countVariables();
        int[] excluded = mySimplex.excluded;
        for (int je = 0; je < excluded.length; je++) {
            int j = excluded[je];
            if (j < n) {

                denom = mySimplex.getCurrentElement(exit, je);
                scale = Math.abs(denom);

                if (!PIVOT.isZero(denom)) {

                    ColumnState columnState = mySimplex.getColumnState(j);

                    ratio = Double.MAX_VALUE;

                    if (columnState == ColumnState.UNBOUNDED) {

                        ratio = ZERO;

                    } else {

                        numer = mySimplex.getReducedCost(je);

                        if (exitDirection == Direction.INCREASE) {
                            if (columnState == ColumnState.LOWER && denom < ZERO) {
                                ratio = Math.max(ZERO, numer) / -denom;
                            } else if (columnState == ColumnState.UPPER && denom > ZERO) {
                                ratio = Math.max(ZERO, -numer) / denom;
                            }
                        } else if (exitDirection == Direction.DECREASE) {
                            if (columnState == ColumnState.LOWER && denom > ZERO) {
                                ratio = Math.max(ZERO, numer) / denom;
                            } else if (columnState == ColumnState.UPPER && denom < ZERO) {
                                ratio = Math.max(ZERO, -numer) / -denom;
                            }
                        }
                    }

                    if (ratio < ZERO || printable && this.isLogDebug()) {
                        this.log(1, "{}({}) {} / {} = {}", j, je, numer, denom, ratio);
                    }

                    if (ratio < iterationRatio
                            || scale > iterationScale && PIVOT.isDifferent(iterationScale, scale) && !RATIO.isDifferent(iterationRatio, ratio)) {

                        enter.index = je;
                        enter.from = columnState;
                        enter.direction = columnState == ColumnState.UPPER ? Direction.DECREASE : Direction.INCREASE;

                        if (this.isLogDebug()) {
                            this.log(2, "{} => {}", ratio, enter);
                        }

                        iterationRatio = ratio;
                        iterationScale = scale;
                    }
                }
            }
        }

        if (this.isLogDebug()) {
            this.log("==>> {}", enter);
        }

        return enter.index >= 0;
    }

    private boolean testPrimalExitRatio(final IterDescr iteration) {

        if (this.isLogDebug()) {
            this.log();
            this.log("testPrimalExitRatio");
        }

        boolean printable = mySimplex.isPrintable();

        EnterInfo enter = iteration.enter;
        ExitInfo exit = iteration.exit;
        Direction enterDirection = enter.direction;

        double range = mySimplex.getRange(enter.column());

        double numer = ZERO;
        double denom = ONE;
        double ratio = ZERO;
        double scale = ONE;

        double iterationRatio = range;
        double iterationScale = MACHINE_LARGEST;

        int[] included = mySimplex.included;
        for (int ji = included.length - 1; ji >= 0; ji--) {
            int j = included[ji];

            denom = mySimplex.getCurrentElement(ji, enter);
            scale = Math.abs(denom);

            if (!PIVOT.isZero(denom)) {

                if (enterDirection == Direction.INCREASE) {
                    // Entering variable will increase

                    if (denom < ZERO) {
                        // Basic (exiting) variable will increase
                        numer = mySimplex.getUpperGap(ji); // How much can it increase?
                        ratio = numer / -denom;
                    } else if (denom > ZERO) {
                        // Basic (exiting) variable will decrease
                        numer = mySimplex.getLowerGap(ji); // How much can it decrease?
                        ratio = numer / denom;
                    }

                    if (ratio < ZERO || printable && this.isLogDebug()) {
                        this.log(1, "{}({}) {} / {} = {}", j, ji, numer, denom, ratio);
                    }

                    if (ratio < iterationRatio
                            || scale > iterationScale && PIVOT.isDifferent(iterationScale, scale) && !RATIO.isDifferent(iterationRatio, ratio)) {

                        exit.index = ji;
                        if (denom < ZERO) {
                            exit.direction = Direction.INCREASE;
                            exit.to = ColumnState.UPPER;
                        } else {
                            exit.direction = Direction.DECREASE;
                            exit.to = ColumnState.LOWER;
                        }

                        if (this.isLogDebug()) {
                            this.log(2, "{} => {}", ratio, exit);
                        }

                        iterationRatio = ratio;
                        iterationScale = scale;
                    }

                } else if (enterDirection == Direction.DECREASE) {
                    // Entering variable will decrease

                    if (denom < ZERO) {
                        // Basic (exiting) variable will decrease
                        numer = mySimplex.getLowerGap(ji); // How much can it decrease?
                        ratio = numer / -denom;
                    } else if (denom > ZERO) {
                        // Basic (exiting) variable will increase
                        numer = mySimplex.getUpperGap(ji); // How much can it increase?
                        ratio = numer / denom;
                    }

                    if (ratio < ZERO || printable && this.isLogDebug()) {
                        this.log(1, "{}({}) {} / {} = {}", j, ji, numer, denom, ratio);
                    }

                    if (ratio < iterationRatio
                            || scale > iterationScale && PIVOT.isDifferent(iterationScale, scale) && !RATIO.isDifferent(iterationRatio, ratio)) {

                        exit.index = ji;
                        if (denom > ZERO) {
                            exit.direction = Direction.INCREASE;
                            exit.to = ColumnState.UPPER;
                        } else {
                            exit.direction = Direction.DECREASE;
                            exit.to = ColumnState.LOWER;
                        }

                        if (this.isLogDebug()) {
                            this.log(2, "{} => {}", ratio, exit);
                        }

                        iterationRatio = ratio;
                        iterationScale = scale;
                    }

                } else {

                    throw new IllegalStateException();
                }
            }
        }

        if (iterationRatio >= range && Double.isFinite(iterationRatio)) {

            if (this.isLogDebug()) {
                this.log("Bound switch!");
            }

            iteration.markAsBoundFlip();
        }

        if (this.isLogDebug()) {
            this.log("==>> {}", exit);
        }

        return exit.index >= 0 || iteration.isBoundFlip();
    }

    private void update(final IterDescr iteration) {

        if (iteration.isBasisUpdate()) {

            if (this.isLogDebug()) {
                this.log();
                this.log("Pivoting: {}", iteration);
                this.log("==>> Row: {}, Exit: {}, Column/Enter: {}.", iteration.exit.index, iteration.exit.column(), iteration.enter.column());
                this.log("Cost={}, Pivot1={}, Pivot2={}, RHS={}, Inf={}", mySimplex.getReducedCost(iteration.enter.index),
                        mySimplex.getCurrentElement(iteration.exit, iteration.enter.index), mySimplex.getCurrentElement(iteration.exit.index, iteration.enter),
                        mySimplex.getCurrentRHS(iteration.exit.row()), mySimplex.getInfeasibility(iteration.exit.index));
                this.log("Shift Exit: {}, Enter: {}", mySolutionShift[iteration.exit.column()], mySolutionShift[iteration.enter.column()]);
            }

            int exitCol = iteration.exit.column();
            int enterCol = iteration.enter.column();

            mySimplex.pivot(iteration);

            double shift = this.shift(iteration.enter.column(), iteration.exit.to);

            // mySimplex.calculateIteration();

            // mySimplex.calculateIterationOld(iteration, exitCol, enterCol, shift);
            mySimplex.calculateIteration(iteration, shift);

        } else if (iteration.isBoundFlip()) {

            if (this.isLogDebug()) {
                this.log();
                this.log("Switching bounds: {}", iteration.enter);
                this.log("==>> Column: {}.", iteration.enter.column());
            }

            int j = iteration.enter.column();

            ColumnState from = iteration.enter.from;
            if (from == ColumnState.LOWER) {
                mySimplex.upper(j);
                this.shift(j, ColumnState.UPPER);
            } else if (from == ColumnState.UPPER) {
                mySimplex.lower(j);
                this.shift(j, ColumnState.LOWER);
            } else if (from == ColumnState.UNBOUNDED) {
                this.setState(State.UNBOUNDED);
            } else {
                throw new IllegalStateException();
            }

        } else if (this.isLogDebug()) {
            this.log();
            this.log("No update operation!");
        }

    }

    private boolean verifyDualFeasibility() {

        boolean retVal = true;

        double epsilon = options.feasibility.epsilon();

        for (int je = 0, limit = mySimplex.excluded.length; je < limit; je++) {
            int j = mySimplex.excluded[je];

            if (mySimplex.isArtificial(j)) {
                continue;
            }

            ColumnState state = mySimplex.getColumnState(j);

            double rc = mySimplex.getReducedCost(je);
            double lb = mySimplex.getLowerBound(j);
            double ub = mySimplex.getUpperBound(j);

            switch (state) {
            case LOWER:
                if (rc < -epsilon) {
                    this.log("!DF {}({}) {}, but {} and [{},{}]", j, je, state, rc, lb, ub);
                    retVal = false;
                }
                break;

            case UPPER:
                if (rc > epsilon) {
                    this.log("!DF {}({}) {}, but {} and [{},{}]", j, je, state, rc, lb, ub);
                    retVal = false;
                }
                break;

            case UNBOUNDED:
                // No reduced cost constraints for unbounded variables
                break;

            default:
                throw new IllegalStateException("Unexpected ColumnState for variable " + j + ": " + state);
            }
        }

        return retVal;
    }

    private boolean verifyPrimalFeasibility() {

        boolean retVal = true;

        double epsilon = options.feasibility.epsilon();

        for (int i = 0, limit = mySimplex.included.length; i < limit; i++) {
            int j = mySimplex.included[i];

            double value = mySimplex.getCurrentRHS(i);

            double lb = mySimplex.getLowerBound(j);
            double ub = mySimplex.getUpperBound(j);

            // Check if the value lies within the bounds
            if (value < lb - epsilon || value > ub + epsilon) {
                this.log("!PF {}({}) {}, but [{},{}]", j, i, value, lb, ub);
                retVal = false;
            }
        }

        return retVal;
    }

    final SimplexSolver basis(final int[] basis) {
        mySimplex.resetBasis(basis);
        return this;
    }

    final void doDualIterations(final IterDescr iteration) {

        if (options.validate) {
            this.verifyDualFeasibility();
        }

        mySimplex.resetEdgeWeights();

        boolean done = false;
        while (this.isIterationAllowed() && !done) {

            iteration.reset();

            if (this.getDualExitCandidate(iteration)) {

                mySimplex.calculateDualDirection(iteration.exit);

                if (this.testDualEnterRatio(iteration)) {

                    if (iteration.isBasisUpdate()) {
                        mySimplex.calculatePrimalDirection(iteration.enter);
                        mySimplex.updateDualEdgeWeights(iteration);
                    }

                    this.update(iteration);

                    this.incrementIterationsCount();

                } else {

                    this.setState(State.INFEASIBLE);
                }

            } else {

                this.setState(State.FEASIBLE);
                done = true;
            }

            if (this.isLogDebug() && mySimplex.isPrintable()) {
                this.logCurrentState();
            }

            if (options.validate) {
                this.verifyDualFeasibility();
            }
        }
    }

    final void doPrimalIterations(final IterDescr iteration) {

        if (options.validate) {
            this.verifyPrimalFeasibility();
        }

        mySimplex.resetEdgeWeights();

        boolean done = false;
        while (this.isIterationAllowed() && !done) {

            iteration.reset();

            if (this.getPrimalEnterCandidate(iteration)) {

                mySimplex.calculatePrimalDirection(iteration.enter);

                if (this.testPrimalExitRatio(iteration)) {

                    if (iteration.isBasisUpdate()) {
                        mySimplex.calculateDualDirection(iteration.exit);
                        mySimplex.updatePrimalEdgeWeights(iteration);
                    }

                    this.update(iteration);

                    this.incrementIterationsCount();

                } else {

                    this.setState(State.UNBOUNDED);
                }

            } else {

                this.setState(State.OPTIMAL);
                done = true;
            }

            if (this.isLogDebug() && mySimplex.isPrintable()) {
                this.logCurrentState();
            }

            if (options.validate) {
                this.verifyPrimalFeasibility();
            }
        }
    }

    final Result extractResult() {

        double retValue = this.extractValue();

        State retState = this.getState();

        double[] retSolution = this.extractSolution();

        Result result = Optimisation.Result.of(retValue, retState, retSolution);

        if (this.isLogDebug()) {
            this.log();
            this.log("{} {} {}", retValue, retState, mySimplex.toString());
            this.log("LB: {}", this.getLowerBounds());
            this.log(" X: {}", retSolution);
            this.log("UB: {}", this.getUpperBounds());
        }

        ConstraintsMetaData constraints = this.getEntityMap().constraints;
        if (constraints.isEntityMap()) {
            return result.multipliers(constraints, this.extractMultipliers());
        } else {
            return result.multipliers(this.extractMultipliers());
        }
    }

    final boolean isDualFeasible() {
        return !this.getPrimalEnterCandidate(null);
    }

    final boolean isPrimalFeasible() {
        return !this.getDualExitCandidate(null);
    }

    final IterDescr prepareToIterate() {

        this.setup(mySimplex);

        if (mySimplex.m == 0) {
            this.solveUnconstrained(); // TODO return?
        }

        mySimplex.prepareToIterate();

        this.resetIterationsCount();

        if (this.isLogDebug()) {
            this.logCurrentState();
        }

        return new IterDescr(mySimplex);
    }

    /**
     * <ul>
     * <li>Determine if non-basic variables are at their lower or upper bound (or if they are unbounded)
     * <li>Shift ranges/bounds so that (one of) the bound(s) is at zero
     * <li>Assumes that the ranges/bounds of basic variables are already defined this way.
     * </ul>
     */
    abstract void setup(SimplexStore simplex);

    final void shift(final int index, final double shift, final double weight) {
        if (shift != ZERO) {
            mySimplex.shiftColumn(index, shift);
            mySolutionShift[index] += shift;
            myValueShift += weight * shift;
        }
    }

    void switchToPhase2() {
        mySimplex.removePhase1();
    }

}
