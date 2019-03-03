/*
 * Copyright 1997-2018 Optimatika
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

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import java.util.Optional;

import org.ojalgo.array.SparseArray;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.PrimitiveFunction.Unary;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.type.IndexSelector;

abstract class ActiveSetSolver extends ConstrainedSolver {

    private static final double RELATIVELY_SMALL = PrimitiveFunction.SQRT.invoke(MACHINE_EPSILON);

    private final IndexSelector myActivator;
    private int myConstraintToInclude = -1;
    private boolean myInitWithLP = false;
    private MatrixStore<Double> myInvQC;
    private final PrimitiveDenseStore myIterationX;
    private final PrimitiveDenseStore mySlackI;
    private final PrimitiveDenseStore mySolutionL;

    ActiveSetSolver(final ConvexSolver.Builder matrices, final Options solverOptions) {

        super(matrices, solverOptions);

        final int tmpCountVariables = this.countVariables();
        final int tmpCountEqualityConstraints = this.countEqualityConstraints();
        final int tmpCountInequalityConstraints = this.countInequalityConstraints();

        myActivator = new IndexSelector(tmpCountInequalityConstraints);

        mySolutionL = PrimitiveDenseStore.FACTORY.makeZero(tmpCountEqualityConstraints + tmpCountInequalityConstraints, 1L);
        myIterationX = PrimitiveDenseStore.FACTORY.makeZero(tmpCountVariables, 1L);

        mySlackI = PrimitiveDenseStore.FACTORY.makeZero(tmpCountInequalityConstraints, 1L);
    }

    private final void shrink() {

        final int[] incl = myActivator.getIncluded();

        final PrimitiveDenseStore soluL = this.getSolutionL();
        final int numbEqus = this.countEqualityConstraints();

        int toExclude = incl[0];
        double maxWeight = ZERO;

        for (int i = 0; i < incl.length; i++) {
            final double tmpValue = soluL.doubleValue(numbEqus + incl[i]);
            final double tmpWeight = PrimitiveFunction.ABS.invoke(tmpValue) * PrimitiveFunction.MAX.invoke(-tmpValue, ONE);
            if (tmpWeight > maxWeight) {
                maxWeight = tmpWeight;
                toExclude = incl[i];
            }
        }
        this.exclude(toExclude);
    }

    protected boolean checkFeasibility(final boolean onlyExcluded) {

        boolean retVal = true;

        if (!onlyExcluded) {

            if (this.hasEqualityConstraints()) {
                final MatrixStore<Double> tmpSE = this.getSE();
                for (int i = 0; retVal && (i < tmpSE.countRows()); i++) {
                    if (!options.feasibility.isZero(tmpSE.doubleValue(i))) {
                        retVal = false;
                    }
                }
            }

            if (this.hasInequalityConstraints() && (myActivator.countIncluded() > 0)) {
                final int[] tmpIncluded = myActivator.getIncluded();
                final MatrixStore<Double> tmpSI = this.getSlackI();
                for (int i = 0; retVal && (i < tmpIncluded.length); i++) {
                    final double tmpSlack = tmpSI.doubleValue(tmpIncluded[i]);
                    if ((tmpSlack < ZERO) && !options.feasibility.isZero(tmpSlack)) {
                        retVal = false;
                    }
                }
            }

        }

        if (this.hasInequalityConstraints() && (myActivator.countExcluded() > 0)) {
            final int[] tmpExcluded = myActivator.getExcluded();
            final MatrixStore<Double> tmpSI = this.getSlackI();
            for (int e = 0; retVal && (e < tmpExcluded.length); e++) {
                final double tmpSlack = tmpSI.doubleValue(tmpExcluded[e]);
                if ((tmpSlack < ZERO) && !options.feasibility.isZero(tmpSlack)) {
                    retVal = false;
                }
            }
        }

        return retVal;
    }

    @Override
    protected boolean computeQ(final Collectable<Double, ? super PhysicalStore<Double>> matrix) {
        final boolean retVal = super.computeQ(matrix);
        myInvQC = this.getSolutionQ(this.getIterationC());
        return retVal;
    }

    protected int countExcluded() {
        return myActivator.countExcluded();
    }

    protected int countIncluded() {
        return myActivator.countIncluded();
    }

    protected void exclude(final int anIndexToExclude) {
        myActivator.exclude(anIndexToExclude);
    }

    @Override
    protected MatrixStore<Double> extractSolution() {
        return super.extractSolution();
    }

    protected int[] getExcluded() {
        return myActivator.getExcluded();
    }

    protected int[] getIncluded() {
        return myActivator.getIncluded();
    }

    protected int getLastExcluded() {
        return myActivator.getLastExcluded();
    }

    protected int getLastIncluded() {
        return myActivator.getLastIncluded();
    }

    protected void include(final int anIndexToInclude) {
        myActivator.include(anIndexToInclude);
    }

    @Override
    protected final boolean initialise(final Result kickStarter) {

        super.initialise(kickStarter);

        boolean feasible = false;
        final boolean usableKickStarter = (kickStarter != null) && kickStarter.getState().isApproximate();

        if (usableKickStarter) {
            this.getSolutionX().fillMatching(kickStarter);
            if (kickStarter.getState().isFeasible()) {
                feasible = true;
            } else {
                feasible = this.checkFeasibility(false);
            }
        }

        if (!feasible) {

            final Result resultLP = this.solveLP();

            if (feasible = resultLP.getState().isFeasible()) {

                this.getSolutionX().fillMatching(resultLP);

                final Optional<Access1D<?>> tmpMultipliers = resultLP.getMultipliers();
                if (tmpMultipliers.isPresent()) {
                    this.getSolutionL().fillMatching(tmpMultipliers.get());
                    myInitWithLP = true;
                } else {
                    this.getSolutionL().fillAll(ZERO);
                }
            }
        }

        if (feasible) {

            this.setState(State.FEASIBLE);

            this.resetActivator();

        } else {

            this.setState(State.INFEASIBLE);

            this.getSolutionX().fillAll(ZERO);
        }

        if (this.isLogDebug()) {

            this.log("Initial solution: {}", this.getSolutionX().copy().asList());
            if (this.getMatrixAE() != null) {
                this.log("Initial E-slack: {}", this.getSE().copy().asList());
            }
            if (this.getMatrixAI() != null) {
                this.log("Initial I-slack: {}", this.getSlackI().copy().asList());
            }
        }

        return this.getState().isFeasible();
    }

    @Override
    protected final boolean needsAnotherIteration() {

        if (this.isLogDebug()) {
            this.log("\nNeedsAnotherIteration?");
            this.log(myActivator.toString());
        }

        int toInclude = -1;
        int toExclude = -1;

        if ((toInclude = this.suggestConstraintToInclude()) >= 0) {
            if (this.isLogDebug()) {
                this.log("Suggested to include: {}", toInclude);
            }
            myActivator.include(toInclude);
            this.setState(State.APPROXIMATE);
            return true;
        } else {
            if ((toExclude = this.suggestConstraintToExclude()) >= 0) {
                if (this.isLogDebug()) {
                    this.log("Suggested to exclude: {}", toExclude);
                }
                this.exclude(toExclude);
                this.setState(State.APPROXIMATE);
                return true;
            } else {
                if (this.isLogDebug()) {
                    this.log("Stop!");
                }
                this.setState(State.OPTIMAL);
                return false;
            }
        }
    }

    /**
     * Find the minimum (largest negative) lagrange multiplier - for the active inequalities - to potentially
     * deactivate.
     */
    protected int suggestConstraintToExclude() {

        int retVal = -1;

        final int[] tmpIncluded = myActivator.getIncluded();

        final int tmpLastIncluded = myActivator.getLastIncluded();
        int tmpIndexOfLast = -1;

        double tmpMin = POSITIVE_INFINITY;
        double tmpVal;

        // final MatrixStore<Double> tmpLI = this.getLI(tmpIncluded);
        final MatrixStore<Double> tmpLI = this.getSolutionL().logical().offsets(this.countEqualityConstraints(), 0).row(tmpIncluded).get();

        if (this.isLogDebug() && (tmpLI.count() > 0L)) {
            this.log("Looking for the largest negative lagrange multiplier among these: {}.", tmpLI.copy().asList());
        }

        for (int i = 0; i < tmpLI.countRows(); i++) {

            if (tmpIncluded[i] != tmpLastIncluded) {

                tmpVal = tmpLI.doubleValue(i, 0);

                if ((tmpVal < ZERO) && (tmpVal < tmpMin) && !options.solution.isZero(tmpVal)) {
                    tmpMin = tmpVal;
                    retVal = i;
                    if (this.isLogDebug()) {
                        this.log("Best so far: {} @ {} ({}).", tmpMin, retVal, tmpIncluded[i]);
                    }
                }

            } else {

                tmpIndexOfLast = i;
            }
        }

        if ((retVal < 0) && (tmpIndexOfLast >= 0)) {

            tmpVal = tmpLI.doubleValue(tmpIndexOfLast, 0);

            if ((tmpVal < ZERO) && (tmpVal < tmpMin) && !options.solution.isZero(tmpVal)) {
                tmpMin = tmpVal;
                retVal = tmpIndexOfLast;
                if (this.isLogDebug()) {
                    this.log("Only the last included needs to be excluded: {} @ {} ({}).", tmpMin, retVal, tmpIncluded[retVal]);
                }
            }
        }

        return retVal >= 0 ? tmpIncluded[retVal] : retVal;
    }

    /**
     * Find minimum (largest negative) slack - for the inactive inequalities - to potentially activate.
     * Negative slack means the constraint is violated. Need to make sure it is enforced by activating it.
     */
    protected int suggestConstraintToInclude() {
        return this.getConstraintToInclude();
    }

    protected String toActivatorString() {
        return myActivator.toString();
    }

    @Override
    final int countIterationConstraints() {
        return this.countEqualityConstraints() + this.countIncluded();
    }

    int getConstraintToInclude() {
        return myConstraintToInclude;
    }

    MatrixStore<Double> getInvQC() {
        return myInvQC;
    }

    @Override
    final MatrixStore<Double> getIterationA() {

        final int numbEqus = this.countEqualityConstraints();
        final int numbVars = this.countVariables();
        final int[] incl = myActivator.getIncluded();

        final PhysicalStore<Double> retVal = PrimitiveDenseStore.FACTORY.makeZero(numbEqus + incl.length, numbVars);

        if (numbEqus > 0) {
            this.getMatrixAE().supplyTo(retVal.regionByLimits(numbEqus, numbVars));
        }
        for (int i = 0; i < incl.length; i++) {
            this.getMatrixAI(incl[i]).supplyNonZerosTo(retVal.regionByRows(numbEqus + i));
        }

        return retVal;
    }

    @Override
    final MatrixStore<Double> getIterationB() {

        final int numbEqus = this.countEqualityConstraints();
        final int[] incl = myActivator.getIncluded();

        final PhysicalStore<Double> retVal = PrimitiveDenseStore.FACTORY.makeZero(numbEqus + incl.length, 1);

        for (int i = 0; i < numbEqus; i++) {
            retVal.set(i, this.getMatrixBE().doubleValue(i));
        }
        for (int i = 0; i < incl.length; i++) {
            retVal.set(numbEqus + i, this.getMatrixBI().doubleValue(incl[i]));
        }

        return retVal;
    }

    @Override
    final MatrixStore<Double> getIterationC() {

        //        final MatrixStore<Double> tmpQ = this.getQ();
        //        final MatrixStore<Double> tmpC = this.getC();
        //
        //        final PhysicalStore<Double> tmpX = this.getX();
        //
        //        return tmpC.subtract(tmpQ.multiply(tmpX));

        return this.getMatrixC();
    }

    MatrixStore<Double> getIterationL(final int[] included) {

        final int tmpCountE = this.countEqualityConstraints();

        final MatrixStore<Double> tmpLI = mySolutionL.logical().offsets(tmpCountE, 0).row(included).get();

        return mySolutionL.logical().limits(tmpCountE, 1).below(tmpLI).get();
    }

    PrimitiveDenseStore getIterationX() {
        return myIterationX;
    }

    PhysicalStore<Double> getSlackI() {
        this.supplySlackI(mySlackI);
        return mySlackI;
    }

    PrimitiveDenseStore getSolutionL() {
        return mySolutionL;
    }

    final void handleIterationResults(final boolean solved, final PrimitiveDenseStore iterX, final int[] included, final int[] excluded) {

        this.incrementIterationsCount();

        final PhysicalStore<Double> soluX = this.getSolutionX();

        if (solved) {
            // Subproblem solved successfully

            iterX.modifyMatching(SUBTRACT, soluX);

            if (this.isLogDebug()) {
                this.log("Current: {}", soluX.asList());
                this.log("Step: {}", iterX.asList());
            }

            final double normCurrentX = soluX.aggregateAll(Aggregator.NORM2);
            final double normStepX = iterX.aggregateAll(Aggregator.NORM2);
            if (!options.solution.isSmall(normCurrentX, normStepX)
                    && (options.solution.isSmall(ONE, normCurrentX) || !options.solution.isSmall(normStepX, normCurrentX))) {
                // Non-zero && non-freak solution

                double stepLength = ONE;

                if (excluded.length > 0) {

                    final PhysicalStore<Double> slack = this.getSlackI();

                    if (this.isLogDebug()) {

                        final MatrixStore<Double> change = this.getMatrixAI(excluded).get().multiply(iterX);

                        final PhysicalStore<Double> steps = slack.copy();
                        steps.modifyMatching(DIVIDE, change);

                        this.log("Numer/slack: {}", slack.asList());
                        this.log("Denom/chang: {}", change.copy().asList());
                        this.log("Looking for the largest possible step length (smallest positive scalar) among these: {}).", steps.asList());
                    }

                    for (int i = 0; i < excluded.length; i++) {

                        final SparseArray<Double> excludedInequalityRow = this.getMatrixAI(excluded[i]);

                        final double currentSlack = slack.doubleValue(excluded[i]);
                        final double slackChange = excludedInequalityRow.dot(iterX);
                        final double fraction = options.feasibility.isSmall(slackChange, currentSlack) ? ZERO : currentSlack / slackChange;

                        if ((slackChange <= ZERO) || options.solution.isSmall(normStepX, slackChange)) {
                            // This constraint not affected
                        } else if (fraction >= ZERO) {
                            // Must check the step length
                            if (fraction < stepLength) {
                                stepLength = fraction;
                                this.setConstraintToInclude(excluded[i]);
                                if (this.isLogDebug()) {
                                    this.log("Best so far: {} @ {} ({}).", stepLength, i, this.getConstraintToInclude());
                                }
                            }
                        }
                    }
                }

                if (stepLength > ZERO) { // It is possible that it becomes == 0.0
                    iterX.axpy(stepLength, soluX);
                } else if (((this.getConstraintToInclude() >= 0) && (myActivator.getLastExcluded() == this.getConstraintToInclude()))
                        && (myActivator.getLastIncluded() == this.getConstraintToInclude())) {
                    this.setConstraintToInclude(-1);
                }

                this.setState(State.APPROXIMATE);

            } else if (this.isLogDebug()) {
                // Zero solution

                if (this.isLogDebug()) {
                    this.log("Step too small!");
                }

                this.setState(State.FEASIBLE);
            }

        } else if (this.isIterationAllowed()) {
            // Subproblem NOT solved successfully, but further iterations allowed

            if (included.length >= 1) {
                // Subproblem NOT solved successfully
                // At least 1 active inequality

                this.shrink();

                this.performIteration();

            } else if (!this.isSolvableQ()) {
                // Subproblem NOT solved successfully
                // 0 active inequalities
                // Q not SPD

                final double largestInQ = this.getIterationQ().aggregateAll(Aggregator.LARGEST);
                final double largestInC = this.getMatrixC().aggregateAll(Aggregator.LARGEST);
                final double largest = PrimitiveFunction.MAX.invoke(largestInQ, largestInC);

                this.getIterationQ().modifyDiagonal(ADD.second(largest * RELATIVELY_SMALL));
                this.computeQ(this.getIterationQ());

                this.getSolutionL().modifyAll((Unary) arg -> {
                    if (Double.isFinite(arg)) {
                        return arg;
                    } else {
                        return ZERO;
                    }
                });

                this.resetActivator();
                this.performIteration();

            } else {
                // Subproblem NOT solved successfully
                // 0 active inequalities
                // Q SPD

                // Should not be possible to end up here, infeasibility among
                // the equality constraints should have been detected earlier.

                this.setState(State.FAILED);
            }

        } else if (this.checkFeasibility(false)) {
            // Subproblem NOT solved successfully
            // Further iterations NOT allowed
            // Feasible current solution

            this.setState(State.FEASIBLE);

        } else {
            // Subproblem NOT solved successfully
            // Further iterations NOT allowed
            // Current solution somehow NOT feasible

            this.setState(State.FAILED);
        }

        if (this.isLogDebug()) {
            this.log("Post iteration");
            this.log("\tSolution: {}", soluX.copy().asList());
            this.log("\tL: {}", this.getSolutionL().asList());
            if ((this.getMatrixAE() != null) && (this.getMatrixAE().count() > 0)) {
                this.log("\tE-slack: {}", this.getSE().copy().asList());
                if (!options.feasibility.isZero(this.getSE().aggregateAll(Aggregator.LARGEST).doubleValue())) {
                    // throw new IllegalStateException("E-slack!");
                }
            }

            this.log("\tI-slack: {}", mySlackI.copy().asList());
            if (!options.feasibility.isZero(mySlackI.aggregateAll(Aggregator.LARGEST).doubleValue())) {
                // throw new IllegalStateException("I-slack!");
            }

        }
    }

    void resetActivator() {

        myActivator.excludeAll();

        final int numbEqus = this.countEqualityConstraints();
        final int numbVars = this.countVariables();

        if (this.hasInequalityConstraints()) {
            final MatrixStore<Double> slack = this.getSlackI();
            final int[] excl = this.getExcluded();

            PrimitiveDenseStore lagrange = this.getSolutionL();
            for (int i = 0; i < excl.length; i++) {
                if (options.feasibility.isZero(slack.doubleValue(excl[i]))
                        && (!myInitWithLP || !options.solution.isZero(lagrange.doubleValue(numbEqus + excl[i])))) {
                    this.include(excl[i]);
                }
            }
        }

        while (((numbEqus + this.countIncluded()) > numbVars) && (this.countIncluded() > 0)) {
            this.shrink();
        }

        if (this.isLogDebug() && ((numbEqus + this.countIncluded()) > numbVars)) {
            this.log("Redundant contraints!");
        }
    }

    void setConstraintToInclude(final int constraintToInclude) {
        myConstraintToInclude = constraintToInclude;
    }

}
