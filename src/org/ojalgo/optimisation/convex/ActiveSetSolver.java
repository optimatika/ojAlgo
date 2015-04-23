/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.store.AboveBelowStore;
import org.ojalgo.matrix.store.IdentityStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.RowsStore;
import org.ojalgo.matrix.store.ZeroStore;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.optimisation.system.KKTSolver;
import org.ojalgo.optimisation.system.KKTSolver.Input;
import org.ojalgo.optimisation.system.KKTSolver.Output;
import org.ojalgo.type.IndexSelector;

/**
 * Solves optimisation problems of the form:
 * <p>
 * min 1/2 [X]<sup>T</sup>[Q][X] - [C]<sup>T</sup>[X]<br>
 * when [AE][X] == [BE]<br>
 * and [AI][X] <= [BI]
 * </p>
 *
 * @author apete
 */
final class ActiveSetSolver extends ConvexSolver {

    private final IndexSelector myActivator;

    private int myConstraintToInclude = -1;

    ActiveSetSolver(final ConvexSolver.Builder matrices, final Optimisation.Options solverOptions) {

        super(matrices, solverOptions);

        if (this.hasInequalityConstraints()) {
            myActivator = new IndexSelector(this.countInequalityConstraints());
        } else {
            myActivator = new IndexSelector(0);
        }

        int tmpIterationsLimit = (int) Math.max(this.getAI().countRows(), this.getAI().countColumns());
        tmpIterationsLimit = (int) (9.0 + Math.sqrt(tmpIterationsLimit));
        tmpIterationsLimit = tmpIterationsLimit * tmpIterationsLimit;

        options.iterations_abort = tmpIterationsLimit;

        // BasicLogger.logDebug("AS solver innequalities: " + this.countInequalityConstraints());
    }

    private boolean checkFeasibility(final boolean onlyExcluded) {

        boolean retVal = true;

        if (!onlyExcluded) {

            if (this.hasEqualityConstraints()) {
                final MatrixStore<Double> tmpAEX = this.getAEX();
                final MatrixStore<Double> tmpBE = this.getBE();
                for (int i = 0; retVal && (i < tmpBE.countRows()); i++) {
                    if (options.slack.isDifferent(tmpBE.doubleValue(i), tmpAEX.doubleValue(i))) {
                        retVal = false;
                    }
                }
            }

            if (this.hasInequalityConstraints() && (myActivator.countIncluded() > 0)) {
                final int[] tmpIncluded = myActivator.getIncluded();
                final MatrixStore<Double> tmpAIX = this.getAIX(tmpIncluded);
                final MatrixStore<Double> tmpBI = this.getBI(tmpIncluded);
                for (int i = 0; retVal && (i < tmpIncluded.length); i++) {
                    final double tmpBody = tmpAIX.doubleValue(i);
                    final double tmpRHS = tmpBI.doubleValue(i);
                    if ((tmpBody > tmpRHS) && options.slack.isDifferent(tmpRHS, tmpBody)) {
                        retVal = false;
                    }
                }
            }
        }

        if (this.hasInequalityConstraints() && (myActivator.countExcluded() > 0)) {
            final int[] tmpExcluded = myActivator.getExcluded();
            final MatrixStore<Double> tmpAIX = this.getAIX(tmpExcluded);
            final MatrixStore<Double> tmpBI = this.getBI(tmpExcluded);
            for (int i = 0; retVal && (i < tmpExcluded.length); i++) {
                final double tmpBody = tmpAIX.doubleValue(i);
                final double tmpRHS = tmpBI.doubleValue(i);
                if ((tmpBody > tmpRHS) && options.slack.isDifferent(tmpRHS, tmpBody)) {
                    retVal = false;
                }
            }
        }

        return retVal;
    }

    /**
     * Find the minimum (largest negative) lagrange multiplier - for the active inequalities - to potentially
     * deactivate.
     */
    private int suggestConstraintToExclude() {

        int retVal = -1;

        final int[] tmpIncluded = myActivator.getIncluded();
        final int tmpLastIncluded = myActivator.getLastIncluded();
        int tmpIndexOfLast = -1;

        double tmpMin = POSITIVE_INFINITY;
        double tmpVal;

        final MatrixStore<Double> tmpLI = this.getLI(tmpIncluded);

        if (this.isDebug() && (tmpLI.count() > 0L)) {
            this.debug("Looking for the largest negative lagrange multiplier among these: {}.", tmpLI.copy().asList());
        }

        for (int i = 0; i < tmpLI.countRows(); i++) {

            if (tmpIncluded[i] != tmpLastIncluded) {

                tmpVal = tmpLI.doubleValue(i, 0);

                if ((tmpVal < ZERO) && (tmpVal < tmpMin) && !options.solution.isZero(tmpVal)) {
                    tmpMin = tmpVal;
                    retVal = i;
                    if (this.isDebug()) {
                        this.debug("Best so far: {} @ {} ({}).", tmpMin, retVal, tmpIncluded[i]);
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
                if (this.isDebug()) {
                    this.debug("Only the last included needs to be excluded: {} @ {} ({}).", tmpMin, retVal, tmpIncluded[retVal]);
                }
            }
        }

        return retVal >= 0 ? tmpIncluded[retVal] : retVal;
    }

    /**
     * Find minimum (largest negative) slack - for the inactive inequalities - to potentially activate.
     * Negative slack means the constraint is violated. Need to make sure it is enforced by activating it.
     */
    private int suggestConstraintToInclude() {
        return myConstraintToInclude;
    }

    @Override
    protected MatrixStore<Double> extractSolution() {
        return super.extractSolution();
    }

    @Override
    protected boolean initialise(final Result kickStarter) {

        final MatrixStore<Double> tmpQ = this.getQ();
        final MatrixStore<Double> tmpC = this.getC();
        final MatrixStore<Double> tmpAE = this.getAE();
        final MatrixStore<Double> tmpBE = this.getBE();
        final MatrixStore<Double> tmpAI = this.getAI();
        final MatrixStore<Double> tmpBI = this.getBI();

        final int tmpNumVars = (int) tmpC.countRows();
        final int tmpNumEqus = tmpAE != null ? (int) tmpAE.countRows() : 0;
        final int tmpNumInes = tmpAI != null ? (int) tmpAI.countRows() : 0;

        final DecompositionStore<Double> tmpX = this.getX();

        myActivator.excludeAll();

        boolean tmpFeasible = false;

        if ((kickStarter != null) && kickStarter.getState().isApproximate()) {

            this.fillX(kickStarter);

        } else {

            final KKTSolver.Input tmpInput = new KKTSolver.Input(tmpQ, tmpC, tmpAE, tmpBE);
            final KKTSolver tmpSolver = this.getDelegateSolver(tmpInput);
            final Output tmpOutput = tmpSolver.solve(tmpInput);

            if (tmpOutput.isSolvable()) {

                this.fillX(tmpOutput.getX());

            } else {

                this.fillX(ONE / tmpNumVars);
            }
        }

        if (!(tmpFeasible = this.checkFeasibility(false))) {
            // Form LP to check feasibility

            final MatrixStore<Double> tmpGradient = tmpQ.multiply(tmpX).subtract(tmpC);

            final MatrixStore<Double> tmpLinearC = tmpGradient.builder().below(tmpGradient.negate()).below(tmpNumInes).build();

            final LinearSolver.Builder tmpLinearBuilder = LinearSolver.getBuilder(tmpLinearC);

            MatrixStore<Double> tmpAEpart = null;
            MatrixStore<Double> tmpBEpart = null;

            if (tmpNumEqus > 0) {
                tmpAEpart = tmpAE.builder().right(tmpAE.negate()).right(tmpNumInes).build();
                tmpBEpart = tmpBE;
            }

            if (tmpNumInes > 0) {
                final MatrixStore<Double> tmpAIpart = tmpAI.builder().right(tmpAI.negate()).right(IdentityStore.makePrimitive(tmpNumInes)).build();
                final MatrixStore<Double> tmpBIpart = tmpBI;
                if (tmpAEpart != null) {
                    tmpAEpart = tmpAEpart.builder().below(tmpAIpart).build();
                    tmpBEpart = tmpBEpart.builder().below(tmpBIpart).build();
                } else {
                    tmpAEpart = tmpAIpart;
                    tmpBEpart = tmpBIpart;
                }
            }

            if (tmpAEpart != null) {

                final PhysicalStore<Double> tmpLinearAE = tmpAEpart.copy();
                final PhysicalStore<Double> tmpLinearBE = tmpBEpart.copy();

                for (int i = 0; i < tmpLinearBE.countRows(); i++) {
                    if (tmpLinearBE.doubleValue(i) < 0.0) {
                        tmpLinearAE.modifyRow(i, 0, PrimitiveFunction.NEGATE);
                        tmpLinearBE.modifyRow(i, 0, PrimitiveFunction.NEGATE);
                    }
                }

                tmpLinearBuilder.equalities(tmpLinearAE, tmpLinearBE);
            }

            final LinearSolver tmpLinearSolver = tmpLinearBuilder.build();

            final Result tmpLinearResult = tmpLinearSolver.solve();

            if (tmpFeasible = tmpLinearResult.getState().isFeasible()) {
                for (int i = 0; i < tmpNumVars; i++) {
                    this.setX(i, tmpLinearResult.doubleValue(i) - tmpLinearResult.doubleValue(tmpNumVars + i));
                }
            }
        }

        if (tmpFeasible) {

            this.setState(State.FEASIBLE);

            if (this.hasInequalityConstraints()) {

                final int[] tmpExcluded = myActivator.getExcluded();

                final MatrixStore<Double> tmpAIX = this.getAIX(tmpExcluded);
                for (int i = 0; i < tmpExcluded.length; i++) {
                    final double tmpBody = tmpAIX.doubleValue(i);
                    final double tmpRHS = tmpBI.doubleValue(tmpExcluded[i]);
                    if (!options.slack.isDifferent(tmpRHS, tmpBody)) {
                        myActivator.include(tmpExcluded[i]);
                    }
                }
            }

            while (((tmpNumEqus + myActivator.countIncluded()) >= tmpNumVars) && (myActivator.countIncluded() > 0)) {
                myActivator.shrink();
            }

        } else {

            this.setState(State.INFEASIBLE);

            this.resetX();
        }

        if (this.isDebug()) {

            this.debug("Initial solution: {}", tmpX.copy().asList());
            if (tmpAE != null) {
                this.debug("Initial E-slack: {}", this.getSE().copy().asList());
            }
            if (tmpAI != null) {
                this.debug("Initial I-included-slack: {}", this.getSI(myActivator.getIncluded()).copy().asList());
                this.debug("Initial I-excluded-slack: {}", this.getSI(myActivator.getExcluded()).copy().asList());
            }
        }

        return this.getState().isFeasible();
    }

    @Override
    protected boolean needsAnotherIteration() {

        if (this.isDebug()) {
            this.debug("\nNeedsAnotherIteration?");
            this.debug(myActivator.toString());
        }

        int tmpToInclude = -1;
        int tmpToExclude = -1;

        if (this.hasInequalityConstraints()) {
            tmpToInclude = this.suggestConstraintToInclude();
            if (tmpToInclude == -1) {
                tmpToExclude = this.suggestConstraintToExclude();
            }
        }

        if (this.isDebug()) {
            this.debug("Suggested to include: {}", tmpToInclude);
            this.debug("Suggested to exclude: {}", tmpToExclude);
        }

        if (tmpToExclude == -1) {
            if (tmpToInclude == -1) {
                // Suggested to do nothing
                this.setState(State.OPTIMAL);
                return false;
            } else {
                // Only suggested to include
                myActivator.include(tmpToInclude);
                this.setState(State.APPROXIMATE);
                return true;
            }
        } else {
            if (tmpToInclude == -1) {
                // Only suggested to exclude
                myActivator.exclude(tmpToExclude);
                this.setState(State.APPROXIMATE);
                return true;
            } else {
                // Suggested both to exclude and include
                myActivator.exclude(tmpToExclude);
                myActivator.include(tmpToInclude);
                this.setState(State.APPROXIMATE);
                return true;
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void performIteration() {

        if (this.isDebug()) {
            this.debug("\nPerformIteration");
            this.debug(myActivator.toString());
        }

        myConstraintToInclude = -1;

        final Input tmpInput = this.buildDelegateSolverInput();
        final KKTSolver tmpSolver = this.getDelegateSolver(tmpInput);
        final Output tmpOutput = tmpSolver.solve(tmpInput, options);

        if (this.isDebug()) {
            this.debug("X/L: {}", tmpOutput);
        }

        final int[] tmpIncluded = myActivator.getIncluded();

        final int tmpCountVariables = this.countVariables();
        final int tmpCountEqualityConstraints = this.countEqualityConstraints();
        final int tmpCountActiveInequalityConstraints = tmpIncluded.length;

        if (tmpOutput.isSolvable()) {
            // Subproblem solved successfully

            final MatrixStore<Double> tmpSubX = tmpOutput.getX();
            final MatrixStore<Double> tmpSubL = tmpOutput.getL();

            if (this.isDebug()) {
                this.debug("Current: {}", this.getX().copy().asList());
                this.debug("Step: {}", tmpSubX.copy().asList());
                this.debug("Step Nullspace: {}", this.getAE().multiply(tmpSubX).copy().asList());
            }

            final double tmpFrobNormX = tmpSubX.aggregateAll(Aggregator.NORM2);
            if (!options.solution.isZero(tmpFrobNormX)) {
                // Non-zero solution

                final int[] tmpExcluded = myActivator.getExcluded();

                final MatrixStore<Double> tmpNumer = this.getSI(tmpExcluded);
                final MatrixStore<Double> tmpDenom = this.getAI().builder().row(tmpExcluded).build().multiply(tmpSubX);
                final PhysicalStore<Double> tmpStepLengths = tmpNumer.copy();
                tmpStepLengths.fillMatching(tmpStepLengths, PrimitiveFunction.DIVIDE, tmpDenom);

                if (this.isDebug()) {
                    this.debug("Slack (numerator): {}", tmpNumer.copy().asList());
                    this.debug("Scaler (denominator): {}", tmpDenom.copy().asList());
                    this.debug("Looking for the largest possible step length (smallest positive scalar) among these: {}).", tmpStepLengths.asList());
                }

                double tmpStepLength = ONE;
                for (int i = 0; i < tmpExcluded.length; i++) {

                    final double tmpN = tmpNumer.doubleValue(i);
                    final double tmpD = tmpDenom.doubleValue(i);
                    final double tmpVal = options.slack.isSmall(tmpD, tmpN) ? ZERO : tmpN / tmpD;

                    if ((tmpD > ZERO) && (tmpVal >= ZERO) && (tmpVal < tmpStepLength) && !options.solution.isSmall(tmpFrobNormX, tmpD)) {
                        tmpStepLength = tmpVal;
                        myConstraintToInclude = tmpExcluded[i];
                        if (this.isDebug()) {
                            this.debug("Best so far: {} @ {} ({}).", tmpStepLength, i, myConstraintToInclude);
                        }
                    }
                }

                if (tmpStepLength > ZERO) {
                    this.getX().maxpy(tmpStepLength, tmpSubX);
                }

                this.setState(State.APPROXIMATE);

            } else if (this.isDebug()) {
                // Zero solution

                this.debug("Step too small!");

                this.setState(State.FEASIBLE);
            }

            for (int i = 0; i < tmpCountEqualityConstraints; i++) {
                this.setLE(i, tmpSubL.doubleValue(i));
            }

            for (int i = 0; i < tmpCountActiveInequalityConstraints; i++) {
                this.setLI(tmpIncluded[i], tmpSubL.doubleValue(tmpCountEqualityConstraints + i));
            }

        } else if (tmpCountActiveInequalityConstraints >= 1) {
            // Subproblem NOT solved successfully
            // At least 1 active inequality

            myActivator.shrink();

            //            final PhysicalStore<Double> tmpQ = (PhysicalStore<Double>) this.getQ();
            //            final AggregatorFunction<Double> tmpAggregator = PrimitiveAggregator.getSet().largest();
            //            tmpQ.visitAll(tmpAggregator);
            //            tmpQ.modifyDiagonal(0, 0, PrimitiveFunction.ADD.second(1000000 * tmpAggregator.doubleValue() * PrimitiveMath.MACHINE_EPSILON));

            if (this.isDebug()) {
                this.debug("Did shrink!");
            }

            this.performIteration();

        } else if (this.checkFeasibility(false)) {
            // Subproblem NOT solved successfully
            // No active inequality
            // Feasible current solution

            this.setState(State.FEASIBLE);

        } else {
            // Subproblem NOT solved successfully
            // No active inequality
            // Not feasible current solution

            this.setState(State.INFEASIBLE);
        }

        if (this.isDebug()) {
            this.debug("Post iteration");
            this.debug("\tSolution: {}", this.getX().copy().asList());
            if (this.getAE() != null) {
                this.debug("\tE-slack: {}", this.getSE().copy().asList());
            }
            if (this.getAI() != null) {
                this.debug("\tI-included-slack: {}", this.getSI(myActivator.getIncluded()).copy().asList());
                this.debug("\tI-excluded-slack: {}", this.getSI(myActivator.getExcluded()).copy().asList());
            }
        }
    }

    @Override
    KKTSolver.Input buildDelegateSolverInput() {

        MatrixStore<Double> tmpSubAE = null;
        final MatrixStore<Double> tmpSubQ = this.getQ();
        final MatrixStore<Double> tmpSubC = this.getC();

        final int[] tmpActivator = myActivator.getIncluded();

        if (tmpActivator.length == 0) {
            if (this.hasEqualityConstraints()) {
                tmpSubAE = this.getAE();
            } else {
                tmpSubAE = ZeroStore.makePrimitive(0, (int) tmpSubC.countRows());
            }
        } else {
            if (this.hasEqualityConstraints()) {
                tmpSubAE = new AboveBelowStore<Double>(this.getAE(), new RowsStore<Double>(this.getAI(), tmpActivator));
            } else {
                tmpSubAE = new RowsStore<Double>(this.getAI(), tmpActivator);
            }
        }

        final PhysicalStore<Double> tmpX = this.getX();

        return new KKTSolver.Input(tmpSubQ, tmpSubC.subtract(tmpSubQ.multiply(tmpX)), tmpSubAE, ZeroStore.makePrimitive((int) tmpSubAE.countRows(), 1));
    }

}
