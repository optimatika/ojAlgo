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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.operation.IndexOf;
import org.ojalgo.equation.Equation;
import org.ojalgo.matrix.store.MatrixStore;
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
 * Revised simplex solver family, intended as the successor to {@link SimplexTableauSolver}.
 * <p>
 * Unlike the tableau solver, this family natively supports explicit finite lower and upper bounds on
 * variables. Non-basic variables are tracked at their lower bound, upper bound, or as unbounded via
 * {@link SimplexStore.ColumnState}. The basis inverse is maintained in factored form through a
 * {@link BasisRepresentation} rather than stored in an explicit tableau.
 * <p>
 * Concrete subclasses implement different simplex strategies:
 * <ul>
 * <li>{@link PhasedSimplexSolver} — dual phase-1 then primal phase-2 (the default and most general).
 * <li>{@link DualSimplexSolver} — pure dual simplex; requires dual feasibility (all variables bounded).
 * <li>{@link PrimalSimplexSolver} — pure primal simplex; requires an already feasible starting basis.
 * </ul>
 * This solver is selected by default when the {@link LinearSolver.Builder} has modified variable bounds, or
 * when {@link LinearSolver.Configuration#dual()} is explicitly requested.
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

    private static final NumberContext COST = NumberContext.of(7);
    private static final NumberContext INFEASIBILITY = NumberContext.of(9);
    private static final NumberContext PIVOT = NumberContext.of(6);

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
            lowerBounds[i] = variable.getLowerLimit(false, NEGATIVE_INFINITY);
            upperBounds[i] = variable.getUpperLimit(false, POSITIVE_INFINITY);
            structure.positivePartVariables[i] = model.indexOf(variable);
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
        retVal.withDualSolution(() -> retMultipliers);
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
        retVal.withDualSolution(() -> retMultipliers);
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

    /**
     * Reused across solves. {@link IterDescr} only captures the (stable, lifetime-fixed) {@code excluded}
     * /{@code included} arrays of the store, so a single instance can be {@code reset()} and reused every
     * {@link #prepareToIterate(boolean)} instead of allocating one per solve — cuts per-B&amp;B-node GC
     * pressure. Lazily created on first use.
     */
    private IterDescr myIteration = null;
    /**
     * Scratch storage shared between the two passes of the Harris ratio tests. Pass 1 fills these arrays for
     * every valid candidate; pass 2 reads them sequentially. Sized to fit either {@code excluded.length}
     * (dual enter test) or {@code included.length} (primal exit test). Allocated lazily on first use.
     * <p>
     * {@code myRatioCacheExitDirection[k]} encodes the (direction, destination) pair for the cached row in
     * {@link #testPrimalExitRatio}: {@code true} = exit variable INCREASEs to its UPPER bound, {@code false}
     * = exit variable DECREASEs to its LOWER bound.
     */
    private boolean[] myRatioCacheExitDirection;
    private int[] myRatioCacheIdx;
    private double[] myRatioCacheScale;
    private ColumnState[] myRatioCacheState;
    private double[] myRatioCacheTrueRatio;

    /**
     * The simplex data structure (tableau) the algorithm operates on.
     */
    private final SimplexStore mySimplex;

    /**
     * The single flag deciding which path {@link #solve(Result)} takes next: {@code true} ⇒ attempt the
     * dual-simplex warm restart (retained optimal basis, no setup/phase-1); {@code false} ⇒ the cold
     * two-phase solve. Ownership of the decision:
     * <ul>
     * <li>set to {@code state.isOptimal()} at the end of every solve — a warm restart is only possible from a
     * retained optimal, dual-feasible basis;
     * <li>cleared by {@link #updateRange} when a <em>non-basic</em> variable's bound moves (its at-bound
     * value feeds the basic solution and the warm path doesn't re-place it / re-shift the tableau, so the
     * retained state would be stale) — a change on a <em>basic</em> variable only moves a feasibility limit
     * and is left warm;
     * <li>cleared on any structural change (basis reset);
     * <li>cleared by {@link #solve(Result)} itself before it re-enters when a warm attempt fails to certify
     * (the fall-back to cold).
     * </ul>
     */
    boolean warm = false;

    SimplexSolver(final Optimisation.Options solverOptions, final SimplexStore simplexStore) {
        super(solverOptions);
        mySimplex = simplexStore;
    }

    @Override
    public boolean fixVariable(final int index, final double value) {
        return this.updateRange(index, value, value);
    }

    @Override
    public final Collection<Equation> generateCutCandidates(final double fractionality, final boolean[] integer) {

        NumberContext integralityTolerance = options.integer().getIntegralityTolerance();

        return mySimplex.generateCutCandidates(integer, integralityTolerance, fractionality);
    }

    @Override
    public Optional<ExpressionsBasedModel.EntityMap> getEntityMap() {
        return mySimplex.structure.isEntityMap() ? Optional.of(mySimplex.structure) : Optional.empty();
    }

    @Override
    public boolean updateRange(final int index, final double lower, final double upper) {

        // The store's return tells us whether bounds actually moved; use it only to decide whether to
        // invalidate cached state and force a re-solve. Always report success: the UpdatableSolver
        // contract is "true if successfully applied", and a no-op IS successfully applied. Returning the
        // changed-flag made IntermediateSolver.update mistake a no-op (e.g. enforceBounds re-asserting the
        // same bound on the post-cut compute() recursion) for an unsupported update, tripping its one-way
        // myInPlaceUpdatesOK latch and permanently forcing every later B&B node to rebuild.
        boolean changed = mySimplex.updateRange(index, lower, upper);

        if (changed) {
            state = State.UNEXPLORED;
            this.invalidateCache();
            if (mySimplex.isExcluded(index)) {
                // A non-basic variable's bound moved: its at-bound value feeds the basic solution and the
                // warm path does not re-place it / re-shift the tableau, so the retained state would be
                // stale. Force the next solve cold (it re-establishes consistency via setup()). A change
                // on a basic variable only moves a feasibility limit, not x_B, so it stays warm.
                warm = false;
            }
        }

        return true;
    }

    private void ensureRatioCacheCapacity(final int needed) {
        if (myRatioCacheIdx == null || myRatioCacheIdx.length < needed) {
            myRatioCacheIdx = new int[needed];
            myRatioCacheScale = new double[needed];
            myRatioCacheTrueRatio = new double[needed];
            myRatioCacheState = new ColumnState[needed];
            myRatioCacheExitDirection = new boolean[needed];
        }
    }

    private Access1D<?> extractMultipliers() {

        int nbConstraints = mySimplex.structure.countConstraints();

        return new Access1D<Double>() {

            @Override
            public double doubleValue(final int index) {
                return SimplexSolver.this.getDualMultiplier(index);
            }

            @Override
            public Double get(final long index) {
                return Double.valueOf(this.doubleValue(index));
            }

            @Override
            public int size() {
                return nbConstraints;
            }

            @Override
            public String toString() {
                return Access1D.toString(this);
            }
        };
    }

    private double[] extractSolution() {
        return mySimplex.extractSolution();
    }

    private double extractValue() {
        return mySimplex.extractValue();
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
            double weight = mySimplex.edgeWeights[ji];
            double score = (candidate * candidate) / weight;

            if (printable && this.isLogDebug()) {
                this.log(1, "{}({}) {}", j, ji, candidate);
            }

            if (score > largest && !INFEASIBILITY.isZero(candidate)) {

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
        return mySimplex.getOriginalLowerBound(index);
    }

    private double[] getLowerBounds() {

        double[] retVal = new double[mySimplex.n];

        for (int j = 0; j < retVal.length; j++) {
            retVal[j] = mySimplex.getOriginalLowerBound(j);
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
                double weight = mySimplex.edgeWeights[je];
                double score = (candidate * candidate) / weight;

                if (printable && this.isLogDebug()) {
                    this.log(1, "{}({}) {} @ {}", j, je, candidate, columnState);
                }

                if (score > largest && !COST.isZero(candidate)) {

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
        return mySimplex.getOriginalUpperBound(index);
    }

    private double[] getUpperBounds() {

        double[] retVal = new double[mySimplex.n];

        for (int j = 0; j < retVal.length; j++) {
            retVal[j] = mySimplex.getOriginalUpperBound(j);
        }

        return retVal;
    }

    private void logCurrentState() {

        this.log();
        this.log("{} iteration {}, partition {}", state, this.countIterations(), mySimplex.toString());

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

    private Optimisation.Result solveUnconstrained() {

        int nbVars = mySimplex.n;

        double retValue = ZERO;
        double[] retSolution = new double[nbVars];
        State retState = State.OPTIMAL;

        for (int j = 0; j < nbVars; j++) {

            double cost = mySimplex.getCost(j);
            double value;

            if (cost == ZERO) {
                value = mySimplex.getOriginalLowerBound(j);
                retSolution[j] = Double.isFinite(value) ? value : ZERO;
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

        double tolerance = COST.getAbsoluteError();

        int n = mySimplex.structure.countVariables();
        int[] excluded = mySimplex.excluded;

        this.ensureRatioCacheCapacity(excluded.length);

        // Pass 1: compute the relaxed minimum theta_max = min over valid candidates of
        // (gap + tolerance) / |denom|, while caching per-candidate (idx, scale, trueRatio, state)
        // for sequential reuse in pass 2.
        double thetaMax = MACHINE_LARGEST;
        int cachedCount = 0;

        for (int je = 0; je < excluded.length; je++) {
            int j = excluded[je];
            if (j >= n) {
                continue;
            }

            double denom = mySimplex.getCurrentElement(exit, je);
            if (PIVOT.isZero(denom)) {
                continue;
            }

            double scale = Math.abs(denom);
            ColumnState columnState = mySimplex.getColumnState(j);
            double relaxed;
            double trueRatio;

            if (columnState == ColumnState.UNBOUNDED) {
                relaxed = ZERO;
                trueRatio = ZERO;
            } else {
                double numer = mySimplex.getReducedCost(je);
                double gap = ZERO;
                boolean valid = false;

                if (exitDirection == Direction.INCREASE) {
                    if (columnState == ColumnState.LOWER && denom < ZERO) {
                        gap = numer;
                        valid = true;
                    } else if (columnState == ColumnState.UPPER && denom > ZERO) {
                        gap = -numer;
                        valid = true;
                    }
                } else if (exitDirection == Direction.DECREASE) {
                    if (columnState == ColumnState.LOWER && denom > ZERO) {
                        gap = numer;
                        valid = true;
                    } else if (columnState == ColumnState.UPPER && denom < ZERO) {
                        gap = -numer;
                        valid = true;
                    }
                }

                if (!valid) {
                    continue;
                }

                relaxed = Math.max(ZERO, gap + tolerance) / scale;
                trueRatio = Math.max(ZERO, gap) / scale;
            }

            if (printable && this.isLogDebug()) {
                this.log(1, "{}({}) relaxed = {}", j, je, relaxed);
            }

            if (relaxed < thetaMax) {
                thetaMax = relaxed;
            }

            myRatioCacheIdx[cachedCount] = je;
            myRatioCacheScale[cachedCount] = scale;
            myRatioCacheTrueRatio[cachedCount] = trueRatio;
            myRatioCacheState[cachedCount] = columnState;
            cachedCount++;
        }

        if (cachedCount == 0) {
            // No valid candidate found
            if (this.isLogDebug()) {
                this.log("==>> {}", enter);
            }
            return false;
        }

        // Pass 2: among cached candidates with true ratio <= thetaMax, pick the largest |denom|.
        double bestScale = -ONE;

        for (int k = 0; k < cachedCount; k++) {
            double scale = myRatioCacheScale[k];
            if (myRatioCacheTrueRatio[k] <= thetaMax && scale > bestScale) {
                bestScale = scale;
                ColumnState columnState = myRatioCacheState[k];
                enter.index = myRatioCacheIdx[k];
                enter.from = columnState;
                enter.direction = columnState == ColumnState.UPPER ? Direction.DECREASE : Direction.INCREASE;

                if (this.isLogDebug()) {
                    this.log(2, "{} (scale {}) => {}", myRatioCacheTrueRatio[k], scale, enter);
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
        double tolerance = INFEASIBILITY.getAbsoluteError();

        int[] included = mySimplex.included;

        this.ensureRatioCacheCapacity(included.length);

        // Pass 1: relaxed minimum, capped at the entering variable's range. While iterating, cache per-row
        // (idx, scale, trueRatio, exit-to-upper) so pass 2 can iterate the cache sequentially.
        double thetaMax = range;
        int cachedCount = 0;

        for (int ji = included.length - 1; ji >= 0; ji--) {
            double denom = mySimplex.getCurrentElement(ji, enter);
            if (PIVOT.isZero(denom)) {
                continue;
            }

            double scale = Math.abs(denom);
            double numer;
            boolean exitDirection;

            if (enterDirection == Direction.INCREASE) {
                if (denom < ZERO) {
                    numer = mySimplex.getUpperGap(ji);
                    exitDirection = true;
                } else {
                    numer = mySimplex.getLowerGap(ji);
                    exitDirection = false;
                }
            } else if (enterDirection == Direction.DECREASE) {
                if (denom < ZERO) {
                    numer = mySimplex.getLowerGap(ji);
                    exitDirection = false;
                } else {
                    numer = mySimplex.getUpperGap(ji);
                    exitDirection = true;
                }
            } else {
                throw new IllegalStateException();
            }

            double relaxed = (numer + tolerance) / scale;
            double trueRatio = numer / scale;

            if (printable && this.isLogDebug()) {
                this.log(1, "{}({}) relaxed = {}", included[ji], ji, relaxed);
            }

            if (relaxed < thetaMax) {
                thetaMax = relaxed;
            }

            myRatioCacheIdx[cachedCount] = ji;
            myRatioCacheScale[cachedCount] = scale;
            myRatioCacheTrueRatio[cachedCount] = trueRatio;
            myRatioCacheExitDirection[cachedCount] = exitDirection;
            cachedCount++;
        }

        if (thetaMax >= range) {

            if (Double.isFinite(thetaMax)) {

                if (this.isLogDebug()) {
                    this.log("Bound switch!");
                }

                iteration.markAsBoundFlip();
            }
            // If thetaMax is non-finite, no candidate provides a bounded step:
            // the entering direction is unbounded. Caller will set State.UNBOUNDED.

            if (this.isLogDebug()) {
                this.log("==>> {}", exit);
            }

            return iteration.isBoundFlip();
        }

        // Pass 2: among cached rows with true ratio <= thetaMax, pick the largest |denom|.
        double bestScale = -ONE;

        for (int k = 0; k < cachedCount; k++) {
            double scale = myRatioCacheScale[k];
            if (myRatioCacheTrueRatio[k] <= thetaMax && scale > bestScale) {
                bestScale = scale;
                exit.index = myRatioCacheIdx[k];
                if (myRatioCacheExitDirection[k]) {
                    exit.direction = Direction.INCREASE;
                    exit.to = ColumnState.UPPER;
                } else {
                    exit.direction = Direction.DECREASE;
                    exit.to = ColumnState.LOWER;
                }

                if (this.isLogDebug()) {
                    this.log(2, "{} (scale {}) => {}", myRatioCacheTrueRatio[k], scale, exit);
                }
            }
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
            }

            mySimplex.pivot(iteration);

            mySimplex.calculateIteration(iteration);

        } else if (iteration.isBoundFlip()) {

            if (this.isLogDebug()) {
                this.log();
                this.log("Switching bounds: {}", iteration.enter);
                this.log("==>> Column: {}.", iteration.enter.column());
            }

            int j = iteration.enter.column();

            ColumnState from = iteration.enter.from;
            if (from == ColumnState.LOWER) {
                mySimplex.setToUpper(j);
            } else if (from == ColumnState.UPPER) {
                mySimplex.setToLower(j);
            } else if (from == ColumnState.UNBOUNDED) {
                state = State.UNBOUNDED;
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
        warm = false; // structural change — the prior basis no longer applies
        return this;
    }

    final void doDualIterations(final IterDescr iteration, final boolean resetEdgeWeights) {

        if (options.validate) {
            this.verifyDualFeasibility();
        }

        if (resetEdgeWeights) {
            mySimplex.resetEdgeWeights();
        }

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

                    double infeasibility = Math.abs(mySimplex.getInfeasibility(iteration.exit.index));
                    if (infeasibility < 1E-9) {
                        state = State.FEASIBLE;
                        done = true;
                    } else {
                        state = State.INFEASIBLE;
                    }
                }

            } else {

                state = State.FEASIBLE;
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

    final void doPrimalIterations(final IterDescr iteration, final boolean resetEdgeWeights) {

        if (options.validate) {
            this.verifyPrimalFeasibility();
        }

        if (resetEdgeWeights) {
            mySimplex.resetEdgeWeights();
        }

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

                    state = State.UNBOUNDED;
                }

            } else {

                state = State.OPTIMAL;
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

    @Override
    double[] extractDualMultipliers() {
        double[] duals = new double[mySimplex.m];
        mySimplex.extractDualValues(duals);
        for (int i = 0; i < duals.length; i++) {
            if (mySimplex.structure.isConstraintNegated(i)) {
                duals[i] = -duals[i];
            }
        }
        mySimplex.unscaleDuals(duals);
        return duals;
    }

    @Override
    double[] extractReducedGradients() {
        double[] gradients = new double[mySimplex.n];
        mySimplex.extractReducedCosts(gradients);
        mySimplex.unscaleReducedCosts(gradients);
        return gradients;
    }

    final Result extractResult() {

        double value = this.extractValue();

        warm = state.isOptimal();

        double[] solution = this.extractSolution();
        mySimplex.unscaleSolution(solution);

        Supplier<Access1D<?>> reducedGradient = () -> ArrayR064.wrap(this.extractReducedGradients());

        Result result = Optimisation.Result.of(value, state, solution).withReducedGradient(reducedGradient);

        if (this.isLogDebug()) {
            this.log();
            this.log("{} {} {}", value, state, mySimplex.toString());
            this.log("LB: {}", this.getLowerBounds());
            this.log(" X: {}", solution);
            this.log("UB: {}", this.getUpperBounds());
        }

        if (mySimplex.structure.isEntityMap()) {
            return result.withDualValues(mySimplex.structure.constraints, this::extractMultipliers);
        } else {
            return result.withDualSolution(this::extractMultipliers);
        }
    }

    final boolean isDualFeasible() {
        return !this.getPrimalEnterCandidate(null);
    }

    final boolean isPrimalFeasible() {
        return !this.getDualExitCandidate(null);
    }

    final IterDescr prepareToIterate(final boolean cold) {

        this.invalidateCache();

        if (cold) {
            this.setup(mySimplex);
        }

        if (mySimplex.m == 0) {
            this.solveUnconstrained(); // TODO return?
        }

        mySimplex.prepareToIterate(cold);

        this.resetIterationsCount();

        if (this.isLogDebug()) {
            this.logCurrentState();
        }

        if (myIteration == null) {
            myIteration = new IterDescr(mySimplex);
        } else {
            myIteration.reset();
        }
        return myIteration;
    }

    /**
     * <ul>
     * <li>Determine if non-basic variables are at their lower or upper bound (or if they are unbounded)
     * <li>Shift ranges/bounds so that (one of) the bound(s) is at zero
     * <li>Assumes that the ranges/bounds of basic variables are already defined this way.
     * </ul>
     */
    abstract void setup(SimplexStore simplex);

    void switchToPhase2() {
        mySimplex.removePhase1();
    }

}
