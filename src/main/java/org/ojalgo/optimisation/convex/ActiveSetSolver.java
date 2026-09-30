/*
 * Copyright 1997-2026 Optimatika
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
import java.util.BitSet;

import org.ojalgo.array.SparseArray;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.IndexSelector;
import org.ojalgo.type.context.NumberContext;

abstract class ActiveSetSolver extends ConstrainedSolver {

    private static final NumberContext ACC = NumberContext.of(12, 14).withMode(RoundingMode.HALF_DOWN);
    /**
     * A constraint found to be linearly dependent on the active constraints is only skipped, in the ratio
     * test, if its slack change is not larger than this factor times the constraint row norm times the step
     * norm. Also, the numerical noise (in the step) is only used to filter the ratio test if it is not larger
     * than this factor times the step norm.
     */
    private static final double DEPENDENT = 1E-6;
    private static final NumberContext FEASIBILITY = NumberContext.of(12, 8);
    /**
     * The iterations limit is this factor times the total number of variables and constraints. Normally far
     * fewer iterations are needed - the limit is there to stop cycling.
     */
    private static final int ITERATIONS_FACTOR = 10;
    /**
     * How many times larger than the numerical noise a slack change has to be, to be considered moving towards
     * the constraint boundary.
     */
    private static final double NOISE_FACTOR = TEN;
    private static final NumberContext LAGRANGE = NumberContext.of(12, 6).withMode(RoundingMode.HALF_DOWN);
    private static final NumberContext SLACK = NumberContext.of(6, 10).withMode(RoundingMode.HALF_DOWN);
    private static final NumberContext SOLUTION = NumberContext.of(6).withMode(RoundingMode.HALF_DOWN);

    private final IndexSelector myActivator;
    private final int myIterationsLimit;
    /**
     * Inequality constraints found to be linearly dependent on the active constraints (the KKT system became
     * unsolvable when they were included). Valid until the active set shrinks or the solution moves.
     */
    private final BitSet myDependent;
    private int myConstraintToInclude = -1;
    private transient int[] myExcluded = null;
    private transient int[] myIncluded = null;
    private MatrixStore<Double> myInvQC;
    private final R064Store myIterationX;
    private transient double[] myRowNormsAE = null;
    private transient double[] myRowNormsAI = null;
    private boolean myShrinkSwitch = true;
    private final R064Store mySlackI;

    ActiveSetSolver(final ConvexData<Double> convexData, final Optimisation.Options optimisationOptions) {

        super(convexData, optimisationOptions);

        int nbVars = this.countVariables();
        int nbEqus = this.countEqualityConstraints();
        int nbInes = this.countInequalityConstraints();

        myActivator = new IndexSelector(nbInes);
        myIterationsLimit = ITERATIONS_FACTOR * (nbVars + nbEqus + nbInes);
        myDependent = new BitSet(nbInes);

        myIterationX = MATRIX_FACTORY.make(nbVars, 1L);

        mySlackI = MATRIX_FACTORY.make(nbInes, 1L);
    }

    private static double norm(final SparseArray<Double> row) {
        return Math.max(Math.sqrt(row.dot(row)), MACHINE_EPSILON);
    }

    private void handleIterationSolution(final R064Store iterX, final int[] excluded) {
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

                MatrixStore<Double> allIneqSlack = this.getSlackI();

                if (this.isLogDebug()) {

                    MatrixStore<Double> slack = allIneqSlack.rows(excluded);
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

                double noise = this.estimateNoise(iterX);
                if (noise > DEPENDENT * Math.sqrt(iterX.dot(iterX))) {
                    // Too noisy to tell noise from actual change
                    noise = ZERO;
                }

                stepLength = this.findStepLength(iterX, allIneqSlack, noise, -1);

                int blocking = this.getConstraintToInclude();
                if (ACC.isZero(stepLength) && blocking == this.getLastExcluded()) {
                    // Blocked, without moving, by the constraint just excluded. Including it again would only
                    // repeat what was just undone, so ignore it (for this step) and take the step anyway.
                    if (this.isLogProgress()) {
                        this.log("Break cycle on redundant constraints because step length {} on constraint {}", stepLength, blocking);
                    }
                    stepLength = this.findStepLength(iterX, allIneqSlack, noise, blocking);
                    if (ACC.isZero(stepLength) && this.getConstraintToInclude() == blocking) {
                        // Still blocked by it - the slack change is not negligible. Neither include it, nor step.
                        this.setConstraintToInclude(-1);
                        stepLength = ZERO;
                    }
                }
            }

            if (stepLength > ZERO) {
                if (this.isLogProgress()) {
                    this.log("Performing update with step length {} adding constraint {}", stepLength, this.getConstraintToInclude());
                }
                iterX.axpy(stepLength, soluX);
                myDependent.clear();
            } else if (this.isLogProgress()) {
                this.log("Do nothing because step length {} and size {} but add constraint {}", stepLength, normStepX, this.getConstraintToInclude());
            }
            // this.setConstraintToInclude(-1);

        } else {
            // Zero solution

            if (this.isLogDebug()) {
                this.log("Step too small!");
            }

            state = State.FEASIBLE;
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

    /**
     * The step should not change the slack of any of the active constraints (equalities and included
     * inequalities). How much it does anyway is a measure of the numerical noise in the step – the largest
     * change in the distance to an active constraint.
     */
    private double estimateNoise(final Access1D<Double> step) {

        double noise = ZERO;

        for (int i = 0, limit = this.countEqualityConstraints(); i < limit; i++) {
            noise = Math.max(noise, Math.abs(this.getMatrixAE(i).dot(step)) / this.getRowNormAE(i));
        }

        for (int i : this.getIncluded()) {
            noise = Math.max(noise, Math.abs(this.getMatrixAI(i).dot(step)) / this.getRowNormAI(i));
        }

        return noise;
    }

    /**
     * Ratio test: the largest step length, not more than 1, that keeps all currently excluded inequalities
     * satisfied. The constraint limiting the step length, if any, is set to be included next.
     * <p>
     * A slack change (distance to the constraint boundary) that is not larger than the numerical noise,
     * scaled with the size of the constraint row, is not considered to be moving towards the boundary. A
     * constraint that is linearly dependent on the active constraints does not change its slack, other than
     * by noise, and including it would make the KKT system unsolvable. Constraints found to be dependent (and
     * the one to ignore) are skipped, unless the slack change is significant compared to the step.
     *
     * @param ignore An inequality constraint to ignore, or -1
     */
    private double findStepLength(final Access1D<Double> step, final Access1D<Double> slack, final double noise, final int ignore) {

        double stepLength = ONE;
        this.setConstraintToInclude(-1);

        double stepNorm = Math.sqrt(step.dot(step));

        int nbIneqs = this.countInequalityConstraints();
        int testThisLast = Math.min(this.getLastIncluded(), this.getLastExcluded());
        int base = Math.max(0, testThisLast + 1);
        for (int ii = 0; ii < nbIneqs; ii++) {
            int i = (base + ii) % nbIneqs; // Wrap around to handle cyclically

            if (myActivator.isIncluded(i)) {
                continue; // Skip currently included rows
            }

            double slackChange = this.getMatrixAI(i).dot(step);

            if (slackChange <= ZERO || SLACK.isZero(slackChange) || slackChange <= NOISE_FACTOR * noise * this.getRowNormAI(i)) {
                continue; // Not moving towards the boundary
            }

            if ((i == ignore || myDependent.get(i)) && slackChange <= DEPENDENT * this.getRowNormAI(i) * stepNorm) {
                continue; // Linearly dependent on the active constraints, and the slack change is negligible
            }

            double currentSlack = slack.doubleValue(i);
            // If the current slack is negative something has already gone wrong.
            // Taking the max value is to handle small negative values due to rounding errors
            double fraction = SLACK.isSmall(slackChange, currentSlack) ? ZERO : Math.max(currentSlack, ZERO) / slackChange;

            if (fraction < stepLength) {
                stepLength = fraction;
                this.setConstraintToInclude(i);
                if (this.isLogDebug()) {
                    this.log(1, "Best so far: {} @ {} ––– {} / {}.", stepLength, i, currentSlack, slackChange);
                }
                if (stepLength == ZERO) {
                    break;
                }
            }
        }

        return stepLength;
    }

    private double getRowNormAE(final int row) {
        if (myRowNormsAE == null) {
            myRowNormsAE = new double[this.countEqualityConstraints()];
            for (int i = 0; i < myRowNormsAE.length; i++) {
                myRowNormsAE[i] = ActiveSetSolver.norm(this.getMatrixAE(i));
            }
        }
        return myRowNormsAE[row];
    }

    private double getRowNormAI(final int row) {
        if (myRowNormsAI == null) {
            myRowNormsAI = new double[this.countInequalityConstraints()];
            for (int i = 0; i < myRowNormsAI.length; i++) {
                myRowNormsAI[i] = ActiveSetSolver.norm(this.getMatrixAI(i));
            }
        }
        return myRowNormsAI[row];
    }

    private void shrink() {

        int toExclude = -1;

        int lastIncluded = this.getLastIncluded();
        if (lastIncluded >= 0 && myActivator.isIncluded(lastIncluded) && !myDependent.get(lastIncluded)) {
            // The KKT system was solvable before this constraint was included - it is linearly dependent on
            // the other active constraints. Exclude it again, and don't let it block the next step(s).
            toExclude = lastIncluded;
            myDependent.set(lastIncluded);
        }

        if (toExclude < 0) {
            toExclude = this.suggestConstraintToExclude();
        }

        if (toExclude < 0) {
            if (myShrinkSwitch && this.getLastIncluded() >= 0) {
                toExclude = this.suggestUsingVectorProjection();
            } else {
                toExclude = this.suggestUsingLagrangeMagnitude();
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

        R064Store soluL = this.getSolutionL();
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
        // The weight is the absolute value of the cosine of the angle between the vectors (the constraint
        // rows).
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

    /**
     * Checks that the solution satisfies the active constraints (the equality constraints and the included
     * inequality constraints), as equalities, to within the feasibility tolerance.
     */
    protected final boolean isActiveConstraintsSatisfied(final Access1D<Double> solution) {

        MatrixStore<Double> mtrxBE = this.getMatrixBE();
        for (int i = 0, limit = this.countEqualityConstraints(); i < limit; i++) {
            double rhs = mtrxBE.doubleValue(i);
            if (Math.abs(this.getMatrixAE(i).dot(solution) - rhs) > FEASIBILITY.error(rhs)) {
                return false;
            }
        }

        for (int i : this.getIncluded()) {
            double rhs = this.getMatrixBI(i);
            if (Math.abs(this.getMatrixAI(i).dot(solution) - rhs) > FEASIBILITY.error(rhs)) {
                return false;
            }
        }

        return true;
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

    protected final int[] getIncluded() {
        if (myIncluded == null) {
            myIncluded = myActivator.getIncluded();
        }
        return myIncluded;
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

        if (state.isOptimal() && this.isIteratingPossible()) {
            // Feasible, but the iterations determine if it is optimal
            state = Optimisation.State.FEASIBLE;
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

        if (this.countIterations() >= myIterationsLimit) {
            // Safety net, should not happen. The current solution is feasible, but most likely not optimal.
            if (this.isLogProgress()) {
                this.log("Iterations limit {} reached!", myIterationsLimit);
            }
            state = State.FEASIBLE;
            return false;
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
            myDependent.clear();
            return true;
        }

        if (this.isLogDebug()) {
            this.log("Stop!");
        }
        state = State.OPTIMAL;
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
        R064Store soluL = this.getSolutionL();

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

        MatrixStore<Double> mtrxBE = this.getMatrixBE();
        MatrixStore<Double> mtrxBI = this.getMatrixBI();

        PhysicalStore<Double> slackE = this.getSlackE();
        PhysicalStore<Double> slackI = this.getSlackI();

        int nbE = slackE.size();
        if (retVal && nbE > 0) {
            if (this.isLogDebug()) {
                this.log("E-slack: {}", slackE.asList());
            }
            for (int i = 0; i < nbE; i++) {
                double slack = slackE.doubleValue(i);
                if (!FEASIBILITY.isSmall(mtrxBE.doubleValue(i), slack)) {
                    retVal = false;
                    if (this.isLogDebug()) {
                        this.log("Nonzero E-slack! {}", slack);
                    }
                }
            }
        }

        int nbI = slackI.size();
        if (retVal && nbI > 0) {
            if (this.isLogDebug()) {
                this.log("I-slack: {}", slackI.asList());
            }
            for (int i = 0; i < nbI; i++) {
                double slack = slackI.doubleValue(i);
                if (slack < ZERO && !FEASIBILITY.isSmall(mtrxBI.doubleValue(i), slack)) {
                    retVal = false;
                    if (this.isLogDebug()) {
                        this.log("Negative I-slack! {}", slack);
                    }
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

        // MatrixStore<Double> tmpQ = this.getQ();
        // MatrixStore<Double> tmpC = this.getC();
        //
        // PhysicalStore<Double> tmpX = this.getX();
        //
        // return tmpC.subtract(tmpQ.multiply(tmpX));

        return this.getMatrixC();
    }

    R064Store getIterationX() {
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

    void handleIterationResults(final boolean solved, final R064Store iterX, final int[] included, final int[] excluded) {

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

                state = State.FAILED;
            }

        } else if (this.checkFeasibility()) {
            // Feasible current solution

            state = State.FEASIBLE;

        } else {
            // Current solution somehow NOT feasible

            state = State.FAILED;
        }

    }

    void resetActivator() {

        myActivator.excludeAll();
        myDependent.clear();
        myExcluded = null;
        myIncluded = null;

        int nbInes = this.countInequalityConstraints();
        int nbEqus = this.countEqualityConstraints();
        int nbVars = this.countVariables();

        int remaining = nbVars - nbEqus;

        if (this.isLogDebug() && remaining < 0) {
            this.log("Redundant contraints!");
        }

        int maxToInclude = Math.abs((nbVars / 2) - nbEqus);

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

        myActivator.resetHistory();
    }

    void setConstraintToInclude(final int constraintToInclude) {
        myConstraintToInclude = constraintToInclude;
    }

}
