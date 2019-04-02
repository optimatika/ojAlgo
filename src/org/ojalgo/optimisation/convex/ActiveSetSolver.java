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
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.type.IndexSelector;

abstract class ActiveSetSolver extends ConstrainedSolver {

    private final IndexSelector myActivator;
    private int myConstraintToInclude = -1;
    private MatrixStore<Double> myInvQC;
    private final PrimitiveDenseStore myIterationX;
    private boolean myShrinkSwitch = true;
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

    private void handleIterationFailure(final int[] included) {

        if (this.isIterationAllowed()) {

            if (this.isSolvableQ()) {
                // There must be a problem with the constraints

                if (this.isLogProgress()) {
                    this.log("Constraints problem!");
                }

                if (included.length >= 1) {
                    // At least 1 active inequality

                    this.shrink();
                    this.performIteration();

                } else {
                    // Should not be possible to end up here, infeasibility among
                    // the equality constraints should have been detected earlier.

                    this.setState(State.FAILED);
                }

            } else {
                // Patch Q

                if (this.isLogProgress()) {
                    this.log("Q problem!");
                }

                final double largestInQ = this.getIterationQ().aggregateAll(Aggregator.LARGEST);
                final double largestInC = this.getMatrixC().aggregateAll(Aggregator.LARGEST);
                final double largest = PrimitiveFunction.MAX.invoke(largestInQ, largestInC);

                this.getIterationQ().modifyDiagonal(ADD.second(largest * ConvexSolver.RELATIVELY_SMALL));
                this.computeQ(this.getIterationQ());

                this.getSolutionL().modifyAll((Unary) arg -> {
                    if (Double.isFinite(arg)) {
                        return arg;
                    } else {
                        return ZERO;
                    }
                });

                this.resetActivator(true);
                this.performIteration();
            }

        } else if (this.checkFeasibility(false)) {
            // Feasible current solution

            this.setState(State.FEASIBLE);

        } else {
            // Current solution somehow NOT feasible

            this.setState(State.FAILED);
        }
    }

    private void handleIterationSolution(final PrimitiveDenseStore iterX, final int[] excluded) {
        // Subproblem solved successfully

        final PhysicalStore<Double> soluX = this.getSolutionX();

        iterX.modifyMatching(SUBTRACT, soluX);

        final double normCurrentX = soluX.aggregateAll(Aggregator.LARGEST);
        final double normStepX = iterX.aggregateAll(Aggregator.LARGEST);

        if (this.isLogDebug()) {
            this.log("Current: {} - {}", normCurrentX, soluX.asList());
            this.log("Step: {} - {}", normStepX, iterX.asList());
        }

        if (!options.solution.isSmall(normCurrentX, normStepX)
                && (ConvexSolver.ALGORITHM_ACCURACY.isSmall(ONE, normCurrentX) || !ConvexSolver.ALGORITHM_ACCURACY.isSmall(normStepX, normCurrentX))) {
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
                    final double fraction = (Math.signum(currentSlack) == Math.signum(Math.signum(currentSlack)))
                            && ConvexSolver.ITERATION_FEASIBILITY.isSmall(slackChange, currentSlack) ? ZERO : currentSlack / slackChange;

                    if ((slackChange <= ZERO) || ConvexSolver.ALGORITHM_ACCURACY.isSmall(normStepX, slackChange)) {
                        // This constraint not affected
                    } else if (fraction >= ZERO) {
                        // Must check the step length
                        if (fraction < stepLength) {
                            stepLength = fraction;
                            this.setConstraintToInclude(excluded[i]);
                            if (this.isLogDebug()) {
                                this.log("Best so far: {} @ {} ({}).", stepLength, i, excluded[i]);
                            }
                        }
                    }
                }
            }

            if (ConvexSolver.ALGORITHM_ACCURACY.isZero(stepLength) && (this.getConstraintToInclude() == this.getLastExcluded())) {
                if (this.isLogProgress()) {
                    this.log("Break cycle on redundant constraints because step length {} on constraint {}", stepLength, this.getConstraintToInclude());
                }
                this.setConstraintToInclude(-1);
            } else if (stepLength > ZERO) {
                if (this.isLogProgress()) {
                    this.log("Performing update with step length {} adding constraint {}", stepLength, this.getConstraintToInclude());
                }
                iterX.axpy(stepLength, soluX);
            } else {
                if (this.isLogProgress()) {
                    this.log("Do nothing because step length {} and size {} but add constraint {}", stepLength, normStepX, this.getConstraintToInclude());
                }
            }

        } else {
            // Zero solution

            if (this.isLogDebug()) {
                this.log("Step too small!");
            }

            this.setState(State.FEASIBLE);
        }

        if (this.isLogDebug()) {
            this.log("Post iteration");
            this.log("\tSolution: {}", soluX.asList());
            this.log("\tL: {}", this.getSolutionL().asList());
            if ((this.getMatrixAE() != null) && (this.getMatrixAE().count() > 0)) {
                this.log("\tE-slack: {}", this.getSE().copy().asList());
            }
            this.log("\tI-slack: {}", this.getSlackI().asList());
            if (this.getSlackI().aggregateAll(Aggregator.MINIMUM) < -0.000001) {
                this.log("Negative slack!");
            }
        }
    }

    private final void shrink() {

        int toExclude = this.suggestConstraintToExclude();

        if (toExclude < 0) {
            if (myShrinkSwitch) {
                toExclude = this.suggestUsingLagrangeMagnitude();
            } else {
                toExclude = this.suggestUsingVectorProjection();
            }
            myShrinkSwitch = !myShrinkSwitch;
        }

        if (this.isLogDebug()) {
            this.log("Will remove {}", toExclude);
        }
        this.exclude(toExclude);
    }

    private final int suggestUsingLagrangeMagnitude() {

        final int[] incl = myActivator.getIncluded();

        final PrimitiveDenseStore soluL = this.getSolutionL();
        final int numbEqus = this.countEqualityConstraints();

        int toExclude = incl[0];
        double maxWeight = ZERO;

        for (int i = 0; i < incl.length; i++) {
            final double value = soluL.doubleValue(numbEqus + incl[i]);
            final double weight = PrimitiveFunction.ABS.invoke(value) * PrimitiveFunction.MAX.invoke(-value, ONE);
            if (weight > maxWeight) {
                maxWeight = weight;
                toExclude = incl[i];
            }
        }

        return toExclude;
    }

    private final int suggestUsingVectorProjection() {

        final int[] incl = myActivator.getIncluded();
        int lastIncluded = myActivator.getLastIncluded();

        AggregatorFunction<Double> aggregator = PrimitiveAggregator.NORM2.get();
        SparseArray<Double> lastRow = this.getMatrixAI(lastIncluded);
        lastRow.visitAll(aggregator);
        double lastNorm = aggregator.doubleValue();

        int toExclude = lastIncluded;
        double maxWeight = ZERO;
        // The weight is the absolute value of the cosine of the angle between the vectors (the constraint rows).
        for (int i = 0; i < incl.length; i++) {
            aggregator.reset();
            SparseArray<Double> inclRow = this.getMatrixAI(incl[i]);
            inclRow.visitAll(aggregator);
            double inclNorm = aggregator.doubleValue();
            final double weight = Math.abs(lastRow.dot(inclRow)) / lastNorm / inclNorm;
            if (weight > maxWeight) {
                maxWeight = weight;
                toExclude = incl[i];
            }
        }

        return toExclude;
    }

    protected boolean checkFeasibility(final boolean onlyExcluded) {

        boolean retVal = true;

        if (!onlyExcluded) {

            if (this.hasEqualityConstraints()) {
                final MatrixStore<Double> tmpSE = this.getSE();
                for (int i = 0; retVal && (i < tmpSE.countRows()); i++) {
                    if (!ConvexSolver.CHECK_FEASIBILITY.isZero(tmpSE.doubleValue(i))) {
                        retVal = false;
                    }
                }
            }

            if (this.hasInequalityConstraints() && (myActivator.countIncluded() > 0)) {
                final int[] tmpIncluded = myActivator.getIncluded();
                final MatrixStore<Double> tmpSI = this.getSlackI();
                for (int i = 0; retVal && (i < tmpIncluded.length); i++) {
                    final double tmpSlack = tmpSI.doubleValue(tmpIncluded[i]);
                    if ((tmpSlack < ZERO) && !ConvexSolver.CHECK_FEASIBILITY.isZero(tmpSlack)) {
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
                if ((tmpSlack < ZERO) && !ConvexSolver.CHECK_FEASIBILITY.isZero(tmpSlack)) {
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

        boolean initWithLP = false;

        if (!feasible) {

            final Result resultLP = this.solveLP();

            if (feasible = resultLP.getState().isFeasible()) {

                this.getSolutionX().fillMatching(resultLP);

                final Optional<Access1D<?>> tmpMultipliers = resultLP.getMultipliers();
                if (tmpMultipliers.isPresent()) {
                    this.getSolutionL().fillMatching(tmpMultipliers.get());
                    initWithLP = true;
                } else {
                    this.getSolutionL().fillAll(ZERO);
                }
            }
        }

        if (feasible) {

            this.setState(State.FEASIBLE);

            this.resetActivator(initWithLP);

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
            if (this.getSlackI().aggregateAll(Aggregator.MINIMUM) < -0.000001) {
                this.log("Negative slack!");
            }

        }

        return this.getState().isFeasible();
    }

    @Override
    protected final boolean needsAnotherIteration() {

        if (this.isLogDebug()) {
            this.log("\nNeedsAnotherIteration?");
        }

        int toInclude = -1;
        int toExclude = -1;

        if ((toInclude = this.suggestConstraintToInclude()) >= 0) {
            if (this.isLogDebug()) {
                this.log("Suggested to include: {}", toInclude);
            }
            myActivator.include(toInclude);
            return true;
        } else {
            if ((toExclude = this.suggestConstraintToExclude()) >= 0) {
                if (this.isLogDebug()) {
                    this.log("Suggested to exclude: {}", toExclude);
                }
                this.exclude(toExclude);
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

                if ((tmpVal < ZERO) && (tmpVal < tmpMin) && !ConvexSolver.NEGATIVE_LAGRANGE.isZero(tmpVal)) {
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

            if ((tmpVal < ZERO) && (tmpVal < tmpMin) && !ConvexSolver.NEGATIVE_LAGRANGE.isZero(tmpVal)) {
                tmpMin = tmpVal;
                retVal = tmpIndexOfLast;
                if (this.isLogProgress()) {
                    this.log("Only the last included needs to be excluded: {} @ {} ({}).", tmpMin, retVal, tmpIncluded[retVal]);
                }
            }
        }

        if (this.isLogProgress()) {
            if (retVal < 0) {
                this.log("Nothing to exclude");
            } else {
                this.log("Suggest to exclude: {} @ {} ({}).", tmpMin, retVal, tmpIncluded[retVal]);
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

    void cleanActivations() {

        final int numbEqus = this.countEqualityConstraints();
        final int numbVars = this.countVariables();

        if (this.hasInequalityConstraints()) {
            final MatrixStore<Double> slack = this.getSlackI();
            final int[] incl = this.getIncluded();

            PrimitiveDenseStore lagrange = this.getSolutionL();
            for (int i = 0; i < incl.length; i++) {
                double slac = slack.doubleValue(incl[i]);
                double lagr = lagrange.doubleValue(numbEqus + incl[i]);
                if (!ConvexSolver.SLACK_ZERO.isZero(slac) || ((lagr < ZERO) && !ConvexSolver.INCLUDE_CONSTRAINT.isZero(lagr))) {
                    if (this.isLogDebug()) {
                        this.log("Will exclude ineq {} with slack={} L={}", i, slac, lagr);
                    }
                    this.exclude(incl[i]);
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

        if (solved) {
            this.handleIterationSolution(iterX, excluded);
        } else {
            this.handleIterationFailure(included);
        }

        if (options.validate && !this.checkFeasibility(false)) {
            this.log("Problem!");
        }
    }

    void resetActivator(boolean useLagrange) {

        myActivator.excludeAll();

        int numbEqus = this.countEqualityConstraints();
        int numbVars = this.countVariables();
        int maxToInclude = numbVars - numbEqus;

        if (this.isLogDebug() && (numbEqus > numbVars)) {
            this.log("Redundant contraints!");
        }

        if (this.hasInequalityConstraints()) {
            final MatrixStore<Double> inqSlack = this.getSlackI();
            final int[] excl = this.getExcluded();

            PrimitiveDenseStore lagrange = this.getSolutionL();
            for (int i = 0; i < excl.length; i++) {
                double slack = inqSlack.doubleValue(excl[i]);
                if (ConvexSolver.ALGORITHM_ACCURACY.isZero(slack) && (this.countIncluded() < maxToInclude)) {
                    if (!useLagrange || !ConvexSolver.ALGORITHM_ACCURACY.isZero(lagrange.doubleValue(numbEqus + excl[i]))) {
                        if (this.isLogDebug()) {
                            this.log("Will inlcude ineq {} with slack={} L={}", i, slack, lagrange.doubleValue(numbEqus + excl[i]));
                        }
                        this.include(excl[i]);
                    }
                }
            }
        }
    }

    void setConstraintToInclude(final int constraintToInclude) {
        myConstraintToInclude = constraintToInclude;
    }

}
