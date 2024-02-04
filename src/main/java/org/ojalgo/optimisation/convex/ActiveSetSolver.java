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
package org.ojalgo.optimisation.convex;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.math.RoundingMode;

import org.ojalgo.array.SparseArray;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.type.IndexSelector;
import org.ojalgo.type.context.NumberContext;

abstract class ActiveSetSolver extends ConstrainedSolver {

    private static final NumberContext ACC = NumberContext.of(12, 14).withMode(RoundingMode.HALF_DOWN);
    private static final NumberContext LAGRANGE = NumberContext.of(12, 6).withMode(RoundingMode.HALF_DOWN);
    private static final NumberContext SLACK = NumberContext.of(6, 10).withMode(RoundingMode.HALF_DOWN);
    private static final NumberContext SOLUTION = NumberContext.of(6).withMode(RoundingMode.HALF_DOWN);
    private static final NumberContext FEASIBILITY = NumberContext.of(12, 8);

    private final IndexSelector myActivator;
    private int myConstraintToInclude = -1;
    private transient int[] myExcluded = null;
    private transient int[] myIncluded = null;
    private MatrixStore<Double> myInvQC;
    private final Primitive64Store myIterationX;
    private boolean myShrinkSwitch = true;
    private final Primitive64Store mySlackI;

    ActiveSetSolver(final ConvexData<Double> convexSolverBuilder, final Optimisation.Options optimisationOptions) {

        super(convexSolverBuilder, optimisationOptions);

        int nbVars = this.countVariables();
        int nbEqus = this.countEqualityConstraints();
        int nbInes = this.countInequalityConstraints();

        myActivator = new IndexSelector(nbInes);

        myIterationX = MATRIX_FACTORY.make(nbVars, 1L);

        mySlackI = MATRIX_FACTORY.make(nbInes, 1L);
    }

    private void handleIterationSolution(final Primitive64Store iterX, final int[] excluded) {
        // Subproblem solved successfully

        PhysicalStore<Double> soluX = this.getSolutionX();

        iterX.modifyMatching(SUBTRACT, soluX);

        double normCurrX = soluX.aggregateAll(Aggregator.LARGEST).doubleValue();
        double normStepX = iterX.aggregateAll(Aggregator.LARGEST).doubleValue();

        if (this.isLogDebug()) {
            this.log("Current: {} - {}", normCurrX, soluX.asList());
            this.log("Step: {} - {}", normStepX, iterX.asList());
        }

        if (this.isLogDebug() && options.validate) {

            PhysicalStore<Double> includedChange = this.getMatrixAI(this.getIncluded()).get().multiply(iterX).copy();

            if (includedChange.count() > 0) {
                this.log("Included-change: {}", includedChange.asList());
                double introducedError = includedChange.aggregateAll(Aggregator.LARGEST);
                if (!FEASIBILITY.isZero(introducedError)) {
                    this.log("Nonzero Included-change! {}", introducedError);
                }
            }

        }

        if (!SOLUTION.isSmall(normCurrX, normStepX)) {
            // Non-zero solution

            double stepLength = ONE;

            if (excluded.length > 0) {

                MatrixStore<Double> slack = this.getSlackI(excluded);

                if (this.isLogDebug()) {

                    MatrixStore<Double> change = this.getMatrixAI(excluded).get().multiply(iterX);

                    if (slack.count() != change.count()) {
                        throw new IllegalStateException();
                    }

                    PhysicalStore<Double> steps = slack.copy();
                    steps.modifyMatching(DIVIDE, change);

                    this.log("Numer/slack: {}", slack.toRawCopy1D());
                    this.log("Denom/chang: {}", change.toRawCopy1D());
                    this.log("Looking for the largest possible step length (smallest positive scalar) among these: {}).", steps.toRawCopy1D());
                }

                for (int i = 0; i < excluded.length; i++) {

                    SparseArray<Double> excludedInequalityRow = this.getMatrixAI(excluded[i]);

                    double currentSlack = slack.doubleValue(i);
                    double slackChange = excludedInequalityRow.dot(iterX);
                    double fraction = Math.abs(currentSlack) / slackChange;
                    // If the current slack is negative something has already gone wrong.
                    // Taking the abs value is to handle small negative values due to rounding errors
                    if (slackChange > ZERO && !SLACK.isZero(slackChange) && SLACK.isSmall(slackChange, currentSlack)) {
                        fraction = ZERO;
                    } else if (slackChange <= ZERO || SLACK.isZero(slackChange)) {
                        fraction = ONE;
                    }

                    if (ZERO <= fraction && fraction < stepLength) {
                        stepLength = fraction;
                        this.setConstraintToInclude(excluded[i]);
                        if (this.isLogDebug()) {
                            this.log(1, "Best so far: {} @ {} ({}) ––– {} / {}.", stepLength, i, excluded[i], currentSlack, slackChange);
                        }
                    }
                }
            }

            if (ACC.isZero(stepLength) && this.getConstraintToInclude() == this.getLastExcluded()) {
                if (this.isLogProgress()) {
                    this.log("Break cycle on redundant constraints because step length {} on constraint {}", stepLength, this.getConstraintToInclude());
                }
                this.setConstraintToInclude(-1);
            } else if (stepLength > ZERO) {
                if (this.isLogProgress()) {
                    this.log("Performing update with step length {} adding constraint {}", stepLength, this.getConstraintToInclude());
                }
                iterX.axpy(stepLength, soluX);
            } else if (this.isLogProgress()) {
                this.log("Do nothing because step length {} and size {} but add constraint {}", stepLength, normStepX, this.getConstraintToInclude());
            }
            //  this.setConstraintToInclude(-1);

        } else {
            // Zero solution

            if (this.isLogDebug()) {
                this.log("Step too small!");
            }

            this.setState(State.FEASIBLE);
        }

        if (this.isLogDebug()) {
            this.log("Post iteration");
            this.log(1, "Solution: {}", soluX.asList());
            this.log(1, "L: {}", this.getSolutionL().asList());
        }

        if (this.isLogDebug() || options.validate) {
            this.checkFeasibility();
        }
    }

    private void shrink() {

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

    private int suggestUsingLagrangeMagnitude() {

        int[] incl = this.getIncluded();

        Primitive64Store soluL = this.getSolutionL();
        int numbEqus = this.countEqualityConstraints();

        int toExclude = incl[0];
        double maxWeight = ZERO;

        for (int i = 0; i < incl.length; i++) {
            double value = soluL.doubleValue(numbEqus + incl[i]);
            double weight = ABS.invoke(value) * MAX.invoke(-value, ONE);
            if (weight > maxWeight) {
                maxWeight = weight;
                toExclude = incl[i];
            }
        }

        return toExclude;
    }

    private int suggestUsingVectorProjection() {

        int[] incl = this.getIncluded();
        int lastIncluded = this.getLastIncluded();

        AggregatorFunction<Double> aggregator = PrimitiveAggregator.getSet().norm2();
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
            double weight = Math.abs(lastRow.dot(inclRow)) / lastNorm / inclNorm;
            if (weight > maxWeight) {
                maxWeight = weight;
                toExclude = incl[i];
            }
        }

        return toExclude;
    }

    protected final int countExcluded() {
        return myActivator.countExcluded();
    }

    protected final int countIncluded() {
        return myActivator.countIncluded();
    }

    protected void exclude(final int indexToExclude) {
        myActivator.exclude(indexToExclude);
        myExcluded = null;
        myIncluded = null;
    }

    @Override
    protected MatrixStore<Double> extractSolution() {
        return super.extractSolution();
    }

    protected final int[] getExcluded() {
        if (myExcluded == null) {
            myExcluded = myActivator.getExcluded();
        }
        return myExcluded;
    }

    protected int getExcluded(final int indexAmongExcluded) {
        return this.getExcluded()[indexAmongExcluded];
    }

    protected final int[] getIncluded() {
        if (myIncluded == null) {
            myIncluded = myActivator.getIncluded();
        }
        return myIncluded;
    }

    protected final int getIncluded(final int indexAmongIncluded) {
        return this.getIncluded()[indexAmongIncluded];
    }

    protected final int getLastExcluded() {
        return myActivator.getLastExcluded();
    }

    protected final int getLastIncluded() {
        return myActivator.getLastIncluded();
    }

    protected void include(final int indexToInclude) {
        myActivator.include(indexToInclude);
        myExcluded = null;
        myIncluded = null;
    }

    @Override
    protected boolean initialise(final Result kickStarter) {

        boolean ok = super.initialise(kickStarter);

        myInvQC = this.getSolutionQ(this.getIterationC());

        Optimisation.State state = this.getState();

        boolean usableKickStarter = kickStarter != null && kickStarter.getState().isApproximate();

        if (usableKickStarter) {
            this.getSolutionX().fillMatching(kickStarter);
            if (kickStarter.getState().isFeasible()) {
                state = kickStarter.getState();
            } else if (this.checkFeasibility()) {
                state = Optimisation.State.FEASIBLE;
            }
        }

        if (!state.isFeasible()) {

            Result resultLP = this.solveLP();

            this.getSolutionX().fillMatching(resultLP);
            this.getSolutionL().fillAll(ZERO);

            if (resultLP.getState().isFeasible()) {
                state = resultLP.getState();
            } else if (this.checkFeasibility()) {
                state = Optimisation.State.FEASIBLE;
            } else {
                state = Optimisation.State.INFEASIBLE;
            }
        }

        if (state.isFeasible()) {
            this.resetActivator();
        } else {
            this.getSolutionX().fillAll(ZERO);
        }

        if (this.isLogDebug()) {

            this.checkFeasibility();

            this.log("Initial solution: {}", this.getSolutionX().copy().asList());
        }

        this.setState(state);
        return ok && state.isFeasible();
    }

    @Override
    protected boolean isIteratingPossible() {
        return !this.isZeroQ();// Can't iterate, return what we have, maybe it's the LP solution
    }

    @Override
    protected boolean needsAnotherIteration() {

        if (this.isLogDebug()) {
            this.log("\nNeedsAnotherIteration?");
        }

        int toInclude = -1;
        int toExclude = -1;

        if ((toInclude = this.suggestConstraintToInclude()) >= 0) {
            if (this.isLogDebug()) {
                this.log("Suggested to include: {}", toInclude);
            }
            this.include(toInclude);
            return true;
        }

        if ((toExclude = this.suggestConstraintToExclude()) >= 0) {
            if (this.isLogDebug()) {
                this.log("Suggested to exclude: {}", toExclude);
            }
            this.exclude(toExclude);
            return true;
        }

        if (this.isLogDebug()) {
            this.log("Stop!");
        }
        this.setState(State.OPTIMAL);
        return false;
    }

    /**
     * Find the minimum (largest negative) lagrange multiplier - for the active inequalities - to potentially
     * deactivate.
     */
    protected int suggestConstraintToExclude() {

        int retVal = -1;

        int[] included = this.getIncluded();
        int lastIncluded = this.getLastIncluded();
        int indexOfLastIncluded = -1;

        double tmpMin = ZERO;
        double tmpVal;

        int nbEqus = this.countEqualityConstraints();
        Primitive64Store soluL = this.getSolutionL();

        if (this.isLogDebug() && included.length > 0) {
            double[] multipliers = soluL.offsets(nbEqus, 0).rows(included).toRawCopy1D();
            this.log("Looking for the largest negative lagrange multiplier among these: {}.", multipliers);
        }

        for (int i = 0, limit = included.length; i < limit; i++) {

            if (included[i] != lastIncluded) {

                tmpVal = soluL.doubleValue(nbEqus + included[i], 0);

                if (tmpVal < tmpMin && !LAGRANGE.isZero(tmpVal)) {
                    tmpMin = tmpVal;
                    retVal = i;
                    if (this.isLogDebug()) {
                        this.log(1, "Best so far: {} @ {} ({}).", tmpMin, retVal, included[retVal]);
                    }
                }

            } else {

                indexOfLastIncluded = i;
            }
        }

        if (retVal < 0 && indexOfLastIncluded >= 0) {

            tmpVal = soluL.doubleValue(nbEqus + included[indexOfLastIncluded], 0);

            if (tmpVal < tmpMin && !LAGRANGE.isZero(tmpVal)) {
                tmpMin = tmpVal;
                retVal = indexOfLastIncluded;
                if (this.isLogProgress()) {
                    this.log("Only the last included needs to be excluded: {} @ {} ({}).", tmpMin, retVal, included[retVal]);
                }
            }
        }

        if (this.isLogProgress()) {
            if (retVal < 0) {
                this.log("Nothing to exclude");
            } else {
                this.log("Suggest to exclude: {} @ {} ({}).", tmpMin, retVal, included[retVal]);
            }
        }

        return retVal >= 0 ? included[retVal] : retVal;
    }

    /**
     * Find minimum (largest negative) slack - for the inactive inequalities - to potentially activate.
     * Negative slack means the constraint is violated. Need to make sure it is enforced by activating it.
     */
    protected int suggestConstraintToInclude() {
        return this.getConstraintToInclude();
    }

    protected final String toActivatorString() {
        return myActivator.toString();
    }

    boolean checkFeasibility() {

        boolean retVal = true;

        PhysicalStore<Double> slackE = this.getSlackE();
        PhysicalStore<Double> slackI = this.getSlackI();

        if (retVal && slackE.count() > 0) {
            if (this.isLogDebug()) {
                this.log("E-slack: {}", slackE.asList());
            }
            double largestE = slackE.aggregateAll(Aggregator.LARGEST);
            if (!FEASIBILITY.isZero(largestE)) {
                retVal = false;
                if (this.isLogDebug()) {
                    this.log("Nonzero E-slack! {}", largestE);
                }
            }
        }

        if (retVal && slackI.count() > 0) {
            if (this.isLogDebug()) {
                this.log("I-slack: {}", slackI.asList());
            }
            double minimumI = slackI.aggregateAll(Aggregator.MINIMUM);
            if (minimumI < ZERO && !FEASIBILITY.isZero(minimumI)) {
                retVal = false;
                if (this.isLogDebug()) {
                    this.log("Negative I-slack! {}", minimumI);
                }
            }
        }

        return retVal;
    }

    @Override
    int countIterationConstraints() {
        return this.countEqualityConstraints() + this.countIncluded();
    }

    int getConstraintToInclude() {
        return myConstraintToInclude;
    }

    MatrixStore<Double> getInvQC() {
        return myInvQC;
    }

    @Override
    MatrixStore<Double> getIterationA() {

        int nbEqus = this.countEqualityConstraints();
        int nbVars = this.countVariables();
        int[] incl = this.getIncluded();

        PhysicalStore<Double> retVal = MATRIX_FACTORY.make(nbEqus + incl.length, nbVars);

        for (int i = 0; i < nbEqus; i++) {
            this.getMatrixAE(i).supplyNonZerosTo(retVal.regionByRows(i));
        }

        for (int i = 0; i < incl.length; i++) {
            this.getMatrixAI(incl[i]).supplyNonZerosTo(retVal.regionByRows(nbEqus + i));
        }

        return retVal;
    }

    @Override
    MatrixStore<Double> getIterationB() {

        int numbEqus = this.countEqualityConstraints();
        int[] incl = this.getIncluded();

        PhysicalStore<Double> retVal = MATRIX_FACTORY.make(numbEqus + incl.length, 1);

        for (int i = 0; i < numbEqus; i++) {
            retVal.set(i, this.getMatrixBE().doubleValue(i));
        }
        for (int i = 0; i < incl.length; i++) {
            retVal.set(numbEqus + i, this.getMatrixBI().doubleValue(incl[i]));
        }

        return retVal;
    }

    @Override
    MatrixStore<Double> getIterationC() {

        //          MatrixStore<Double> tmpQ = this.getQ();
        //          MatrixStore<Double> tmpC = this.getC();
        //
        //          PhysicalStore<Double> tmpX = this.getX();
        //
        //        return tmpC.subtract(tmpQ.multiply(tmpX));

        return this.getMatrixC();
    }

    Primitive64Store getIterationX() {
        return myIterationX;
    }

    PhysicalStore<Double> getSlackI() {

        MatrixStore<Double> mtrxBI = this.getMatrixBI();
        PhysicalStore<Double> mtrxX = this.getSolutionX();

        mySlackI.fillMatching(mtrxBI);

        for (int i = 0, limit = mtrxBI.getRowDim(); i < limit; i++) {
            mySlackI.add(i, -this.getMatrixAI(i).dot(mtrxX));
        }

        return mySlackI;
    }

    MatrixStore<Double> getSlackI(final int[] rows) {
        return this.getSlackI().rows(rows);
    }

    void handleIterationResults(final boolean solved, final Primitive64Store iterX, final int[] included, final int[] excluded) {

        this.incrementIterationsCount();

        if (solved) {

            this.handleIterationSolution(iterX, excluded);

        } else if (this.isIterationAllowed()) {
            // Assume Q solvable
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

        } else if (this.checkFeasibility()) {
            // Feasible current solution

            this.setState(State.FEASIBLE);

        } else {
            // Current solution somehow NOT feasible

            this.setState(State.FAILED);
        }

    }

    void resetActivator() {

        myActivator.excludeAll();
        myExcluded = null;
        myIncluded = null;

        int nbInes = this.countInequalityConstraints();
        int nbEqus = this.countEqualityConstraints();
        int nbVars = this.countVariables();

        int maxToInclude = nbVars - nbEqus;

        if (this.isLogDebug() && maxToInclude < 0) {
            this.log("Redundant contraints!");
        }

        if (nbInes > 0 && maxToInclude > 0) {

            MatrixStore<Double> ineqSlack = this.getSlackI();

            for (int i = 0; i < nbInes; i++) {

                double slack = ineqSlack.doubleValue(i);

                if (slack >= ZERO && ACC.isZero(slack) && this.countIncluded() < maxToInclude) {
                    if (this.isLogDebug()) {
                        this.log("Will inlcude ineq {} with slack={}", i, slack);
                    }
                    this.include(i);
                }
            }
        }
    }

    void setConstraintToInclude(final int constraintToInclude) {
        myConstraintToInclude = constraintToInclude;
    }

}
