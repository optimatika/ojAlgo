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
import java.util.Arrays;
import java.util.Collection;

import org.ojalgo.equation.Equation;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ConstraintsMap;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.linear.SimplexStore.ColumnState;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.PrimitiveNumber;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.EntryPair;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

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
        double ratioDual = MACHINE_LARGEST;
        double ratioPrimal = MACHINE_LARGEST;

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
            } else if (this.isBoundSwitch()) {
                return enter.toString() + " -> " + exit.to;
            } else {
                return "-";
            }
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
        boolean isBoundSwitch() {
            return enter.index >= 0 && enter.direction != Direction.STAY && enter.direction == exit.direction;
        }

        boolean isNoOperation() {
            return !this.isBasisUpdate() && !this.isBoundSwitch();
        }

        void markAsBoundSwitch() {
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
            ratioPrimal = MACHINE_LARGEST;
            ratioDual = MACHINE_LARGEST;
        }

    }

    private static final NumberContext ALGORITHM = NumberContext.of(8).withMode(RoundingMode.HALF_DOWN);

    private final SimplexStore mySimplex;
    private final double[] mySolutionShift;
    private double myValueShift = ZERO;

    SimplexSolver(final Optimisation.Options solverOptions, final SimplexStore simplexStore) {
        super(solverOptions);
        mySimplex = simplexStore;
        mySolutionShift = new double[simplexStore.n];
    }

    @Override
    public final Collection<Equation> generateCutCandidates(final double fractionality, final boolean... integer) {

        double[] solution = this.extractSolution();

        if (this.isLogDebug()) {
            BasicLogger.debug("Sol: {}", Arrays.toString(solution));
            BasicLogger.debug("+++: {}", Arrays.toString(mySolutionShift));
        }

        boolean[] negated = new boolean[integer.length];
        for (int j = 0; j < negated.length; j++) {
            //if (this.getEntityMap().isNegated(j)) {
            if (this.isNegated(j)) {
                negated[j] = true;
            }
        }

        return mySimplex.generateCutCandidates(solution, integer, negated, options.integer().getIntegralityTolerance(), fractionality);
    }

    @Override
    public LinearStructure getEntityMap() {
        return mySimplex.structure;
    }

    @Override
    public KeyedPrimitive<EntryPair<ConstraintType, PrimitiveNumber>> getImpliedBoundSlack(final int col) {

        double shift = mySolutionShift[col];

        if (shift != ZERO) {

            ColumnState columnState = mySimplex.getColumnState(col);
            ConstraintType constraintType = ConstraintType.EQUALITY;
            if (columnState == ColumnState.LOWER) {
                constraintType = ConstraintType.LOWER;
            } else if (columnState == ColumnState.UPPER) {
                constraintType = ConstraintType.UPPER;
            }

            return EntryPair.of(constraintType, col).asKeyTo(shift);

        } else {

            return null;
        }
    }

    private Access1D<?> extractMultipliers() {

        Access1D<Double> duals = mySimplex.sliceDualVariables();

        LinearStructure structure = mySimplex.structure;

        return new Access1D<Double>() {

            @Override
            public long count() {
                return structure.countConstraints();
            }

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

        double largest = 1E-10;
        int[] included = mySimplex.included;
        for (int ji = 0, limit = included.length; ji < limit; ji++) {
            int j = included[ji];

            double candidate = mySimplex.getInfeasibility(ji);
            double magnitude = Math.abs(candidate);

            if (printable && this.isLogDebug()) {
                this.log(1, "{}({}) {}", j, ji, candidate);
            }

            if (magnitude > largest) {

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
                        this.log(2, "{} => {}", magnitude, exit);
                    }
                }

                largest = magnitude;
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
        double largest = 1E-10;
        int[] excluded = mySimplex.excluded;
        for (int je = 0; je < excluded.length; je++) {
            int j = excluded[je];
            if (j < n) {

                ColumnState columnState = mySimplex.getColumnState(j);
                double candidate = mySimplex.getReducedCost(je);
                double magnitude = Math.abs(candidate);

                if (printable && this.isLogDebug()) {
                    this.log(1, "{}({}) {} @ {}", j, je, candidate, columnState);
                }

                if (magnitude > largest) {

                    if (candidate <= ZERO && columnState != ColumnState.UPPER) {

                        if (enter != null) {

                            enter.index = je;
                            enter.from = columnState;
                            enter.direction = Direction.INCREASE;

                            if (this.isLogDebug()) {
                                this.log(2, "{} => {}", magnitude, enter);
                            }
                        }

                        largest = magnitude;
                        retVal = true;

                    } else if (candidate >= ZERO && columnState != ColumnState.LOWER) {

                        if (enter != null) {

                            enter.index = je;
                            enter.from = columnState;
                            enter.direction = Direction.DECREASE;

                            if (this.isLogDebug()) {
                                this.log(2, "{} => {}", magnitude, enter);
                            }
                        }

                        largest = magnitude;
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

    private boolean isNegated(final int j) {
        if (this.getUpperBound(j) <= ZERO && this.getLowerBound(j) < ZERO) {
            return true;
        } else {
            return false;
        }
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

    private void shift(final int column, final ColumnState state) {

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
    }

    /**
     * <ul>
     * <li>Determine if non-basic variables are at their lower or upper bound (or if they are unbounded)
     * <li>Shift ranges/bounds so that (one of) the bound(s) is at zero
     * <li>Assumes that the ranges/bounds of basic variables are already defined this way.
     * </ul>
     *
     * @param prioritiseFeasibility TODO
     * @param modifyObjective TODO
     */
    private void shiftBounds(final boolean prioritiseFeasibility, final boolean modifyObjective) {

        int[] excluded = mySimplex.excluded;
        for (int je = 0, limit = excluded.length; je < limit; je++) {
            int j = excluded[je];

            double rc = prioritiseFeasibility ? ZERO : mySimplex.getCost(j);
            double lb = mySimplex.getLowerBound(j);
            double ub = mySimplex.getUpperBound(j);

            if (lb > ub) {
                throw new IllegalStateException();
            }

            if (rc > ZERO && Double.isFinite(lb)) {
                mySimplex.lower(j);
                mySimplex.shiftColumn(j, lb);
                mySolutionShift[j] = lb;
                myValueShift += rc * lb;
            } else if (rc < ZERO && Double.isFinite(ub)) {
                mySimplex.upper(j);
                mySimplex.shiftColumn(j, ub);
                mySolutionShift[j] = ub;
                myValueShift += rc * ub;
            } else if (!Double.isFinite(lb) && !Double.isFinite(ub)) {
                mySimplex.unbounded(j);
                if (modifyObjective) {
                    mySimplex.objective().set(j, ZERO);
                }
            } else if (Math.abs(lb) <= Math.abs(ub)) {
                mySimplex.lower(j);
                mySimplex.shiftColumn(j, lb);
                mySolutionShift[j] = lb;
                myValueShift += rc * lb;
                if (modifyObjective) {
                    mySimplex.objective().set(j, ONE);
                }
            } else if (Math.abs(lb) >= Math.abs(ub)) {
                mySimplex.upper(j);
                mySimplex.shiftColumn(j, ub);
                mySolutionShift[j] = ub;
                myValueShift += rc * ub;
                if (modifyObjective) {
                    mySimplex.objective().set(j, NEG);
                }
            } else {
                mySimplex.lower(j);
                if (modifyObjective) {
                    mySimplex.objective().set(j, ONE);
                }
            }
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

        iteration.ratioDual = Double.MAX_VALUE;

        int n = mySimplex.structure.countVariables();
        int[] excluded = mySimplex.excluded;
        for (int je = 0; je < excluded.length; je++) {
            int j = excluded[je];
            if (j < n) {

                denom = mySimplex.getTableauElement(exit, je);

                if (!ALGORITHM.isZero(denom)) {

                    ColumnState columnState = mySimplex.getColumnState(j);

                    ratio = Double.MAX_VALUE;

                    if (columnState == ColumnState.UNBOUNDED) {

                        ratio = ZERO;

                    } else {

                        numer = mySimplex.getReducedCost(je);

                        if (exitDirection == Direction.INCREASE) {
                            if (columnState == ColumnState.LOWER && denom < ZERO) {
                                ratio = numer / -denom;
                            } else if (columnState == ColumnState.UPPER && denom > ZERO) {
                                ratio = -numer / denom;
                            }
                        } else if (exitDirection == Direction.DECREASE) {
                            if (columnState == ColumnState.LOWER && denom > ZERO) {
                                ratio = numer / denom;
                            } else if (columnState == ColumnState.UPPER && denom < ZERO) {
                                ratio = -numer / -denom;
                            }
                        }
                    }

                    if (printable && this.isLogDebug()) {
                        this.log(1, "{}({}) {} / {} = {}", j, je, numer, denom, ratio);
                    }

                    if (ratio < iteration.ratioDual) {

                        enter.index = je;
                        enter.from = columnState;
                        enter.direction = columnState == ColumnState.UPPER ? Direction.DECREASE : Direction.INCREASE;

                        if (this.isLogDebug()) {
                            this.log(2, "{} => {}", ratio, enter);
                        }

                        iteration.ratioDual = ratio;
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

        iteration.ratioPrimal = range;

        int[] included = mySimplex.included;
        for (int ji = 0; ji < included.length; ji++) {
            int j = included[ji];

            denom = mySimplex.getTableauElement(ji, enter);

            if (!ALGORITHM.isZero(denom)) {

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

                    if (printable && this.isLogDebug()) {
                        this.log(1, "{}({}) {} / {} = {}", j, ji, numer, denom, ratio);
                    }

                    if (ratio < iteration.ratioPrimal) {

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

                        iteration.ratioPrimal = ratio;
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

                    if (printable && this.isLogDebug()) {
                        this.log(1, "{}({}) {} / {} = {}", j, ji, numer, denom, ratio);
                    }

                    if (ratio < iteration.ratioPrimal) {

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

                        iteration.ratioPrimal = ratio;
                    }

                } else {

                    throw new IllegalStateException();
                }
            }
        }

        if (iteration.ratioPrimal >= range) {

            if (this.isLogDebug()) {
                this.log("Bound switch!");
            }

            iteration.markAsBoundSwitch();
        }

        if (this.isLogDebug()) {
            this.log("==>> {}", exit);
        }

        return exit.index >= 0 || iteration.isBoundSwitch();
    }

    private void update(final IterDescr iteration) {

        if (iteration.isBasisUpdate()) {

            if (this.isLogDebug()) {
                this.log();
                this.log("Pivoting: {}", iteration);
                this.log("==>> Row: {}, Exit: {}, Column/Enter: {}.", iteration.exit.index, iteration.exit.column(), iteration.enter.column());
                this.log("Shift Exit: {}, Enter: {}", mySolutionShift[iteration.exit.column()], mySolutionShift[iteration.enter.column()]);
            }

            mySimplex.pivot(iteration);

            this.shift(iteration.enter.column(), iteration.exit.to);

            mySimplex.calculateIteration(iteration);

        } else if (iteration.isBoundSwitch()) {

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

        } else {

            if (this.isLogDebug()) {
                this.log();
                this.log("No update operation!");
            }
        }

    }

    final SimplexSolver basis(final int[] basis) {
        mySimplex.resetBasis(basis);
        return this;
    }

    final void doDualIterations(final IterDescr iteration) {

        boolean done = false;
        while (this.isIterationAllowed() && !done) {

            iteration.reset();

            if (this.getDualExitCandidate(iteration)) {

                mySimplex.calculateDualDirection(iteration.exit);

                if (this.testDualEnterRatio(iteration)) {

                    if (iteration.isBasisUpdate()) {
                        mySimplex.calculatePrimalDirection(iteration.enter);
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

            if (this.isLogDebug()) {
                this.logCurrentState();
            }
        }
    }

    final void doPrimalIterations(final IterDescr iteration) {

        if (options.validate) {
            this.validate(this.extractResult());
        }

        boolean done = false;
        while (this.isIterationAllowed() && !done) {

            iteration.reset();

            if (this.getPrimalEnterCandidate(iteration)) {

                mySimplex.calculatePrimalDirection(iteration.enter);

                if (this.testPrimalExitRatio(iteration)) {

                    if (iteration.isBasisUpdate()) {
                        mySimplex.calculateDualDirection(iteration.exit);
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

            if (this.isLogDebug()) {
                this.logCurrentState();
            }

            if (options.validate) {
                this.validate(this.extractResult());
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

        ConstraintsMap constraints = this.getEntityMap().constraints;
        if (constraints.isEntityMap()) {
            return result.multipliers(constraints, this.extractMultipliers());
        } else {
            return result.multipliers(this.extractMultipliers());
        }
    }

    void initiatePhase1() {
        mySimplex.copyObjective();
    }

    final boolean isDualFeasible() {
        return !this.getPrimalEnterCandidate(null);
    }

    final boolean isPrimalFeasible() {
        return !this.getDualExitCandidate(null);
    }

    final IterDescr prepareToIterate(final boolean prioritiseFeasibility, final boolean modifyObjective) {

        this.shiftBounds(prioritiseFeasibility, modifyObjective);

        if (mySimplex.m == 0) {
            this.solveUnconstrained(); // TODO return?
        }

        mySimplex.calculateIteration();

        this.resetIterationsCount();

        if (this.isLogDebug()) {
            this.logCurrentState();
        }

        return new IterDescr(mySimplex);
    }

    void switchToPhase2() {
        mySimplex.restoreObjective();
        mySimplex.calculateIteration();
    }

}
