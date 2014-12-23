/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.store.AboveBelowStore;
import org.ojalgo.matrix.store.IdentityStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.RowsStore;
import org.ojalgo.matrix.store.ZeroStore;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.convex.KKTSolver.Input;
import org.ojalgo.optimisation.convex.KKTSolver.Output;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.random.Uniform;
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

    private boolean isFeasible(final boolean onlyExcluded) {

        boolean retVal = true;

        if (!onlyExcluded) {

            final MatrixStore<Double> tmpSE = this.getSE();
            for (int i = 0; retVal && (i < tmpSE.countRows()); i++) {
                final double tmpVal = tmpSE.doubleValue(i);
                if (!options.slack.isZero(tmpVal)) {
                    retVal = false;
                }
            }

            final MatrixStore<Double> tmpSIincl = this.getSI(myActivator.getIncluded());
            for (int i = 0; retVal && (i < tmpSIincl.countRows()); i++) {
                final double tmpVal = tmpSIincl.doubleValue(i);
                if (!options.slack.isZero(tmpVal)) {
                    retVal = false;
                }
            }
        }

        final MatrixStore<Double> tmpSIexcl = this.getSI(myActivator.getExcluded());
        for (int i = 0; retVal && (i < tmpSIexcl.countRows()); i++) {
            final double tmpVal = tmpSIexcl.doubleValue(i);
            if ((tmpVal < 0.0) && !options.slack.isZero(tmpVal)) {
                retVal = false;
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
            this.debug("Looking for the largest negative lagrange multiplier among these: {}.", tmpLI.copy().toString());
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
     * Find minimum (largest negative) slack - for the inactive inequalities - to potentially activate. Negative slack
     * means the constraint is violated. Need to make sure it is enforced by activating it.
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

        myActivator.excludeAll();

        boolean tmpFeasible = false;

        if (kickStarter != null) {

            this.fillX(kickStarter);

        } else {

            final KKTSolver.Input tmpUnconstrInput = new KKTSolver.Input(tmpQ, tmpC, tmpAE, tmpBE);
            final KKTSolver tmpUnconstrSolver = this.getDelegateSolver(tmpUnconstrInput);
            final Output tmpUnconstrOutput = tmpUnconstrSolver.solve(tmpUnconstrInput);

            if (tmpUnconstrOutput.isSolvable()) {
                this.fillX(tmpUnconstrOutput.getX());
                tmpFeasible = this.isFeasible(true);
            } else {
                //                for (int i = 0; i < tmpC.countRows(); i++) {
                //                    final double tmpNumer = tmpC.doubleValue(i);
                //                    final double tmpDenom = tmpQ.doubleValue(i, i);
                //                    if (options.problem.isZero(tmpDenom)) {
                //                        this.setX(i, 0.0);
                //                    } else {
                //                        this.setX(i, tmpNumer / tmpDenom);
                //                    }
                //                }
                this.resetX();
                tmpFeasible = this.isFeasible(false);
            }
        }

        if (!tmpFeasible) {

            //final MatrixStore<Double> tmpLinearC = tmpQ.multiplyRight(this.getX()).subtract(tmpC);
            //final MatrixStore<Double> tmpLinearC = tmpC;
            //final MatrixStore<Double> tmpLinearC = ZeroStore.makePrimitive((int) tmpC.countRows(), (int) tmpC.countColumns());
            final PrimitiveDenseStore tmpLinearC = PrimitiveDenseStore.FACTORY.makeRandom((int) tmpC.countRows(), (int) tmpC.countColumns(), new Uniform());
            for (int i = 0; i < tmpLinearC.count(); i++) {
                tmpLinearC.set(i, 1.0 + i);
            }

            final int tmpNumberOfVariables = (int) tmpC.countRows();
            final int tmpNumberOfEqualities = tmpAE != null ? (int) tmpAE.countRows() : 0;
            final int tmpNumberOfInequalities = tmpAI != null ? (int) tmpAI.countRows() : 0;

            final LinearSolver.Builder tmpLinBuilder = new LinearSolver.Builder(tmpLinearC.builder().below(tmpLinearC.negate()).below(tmpNumberOfInequalities)
                    .build());

            MatrixStore<Double> tmpLinearAE = null;
            MatrixStore<Double> tmpLinearBE = null;

            if (tmpNumberOfEqualities > 0) {
                tmpLinearAE = tmpAE.builder().right(tmpAE.negate()).right(tmpNumberOfInequalities).build();
                tmpLinearBE = tmpBE;
            }

            if (tmpNumberOfInequalities > 0) {
                final MatrixStore<Double> tmpLinAI = tmpAI.builder().right(tmpAI.negate()).right(IdentityStore.makePrimitive(tmpNumberOfInequalities)).build();
                if (tmpLinearAE != null) {
                    tmpLinearAE = tmpLinearAE.builder().below(tmpLinAI).build();
                    tmpLinearBE = tmpLinearBE.builder().below(tmpBI).build();
                } else {
                    tmpLinearAE = tmpLinAI;
                    tmpLinearBE = tmpBI;
                }
            }

            if (tmpLinearAE != null) {

                final PhysicalStore<Double> tmpCopyAE = tmpLinearAE.copy();
                final PhysicalStore<Double> tmpCopyBE = tmpLinearBE.copy();

                for (int i = 0; i < tmpCopyBE.countRows(); i++) {
                    if (tmpCopyBE.doubleValue(i) < 0.0) {
                        tmpCopyAE.modifyRow(i, 0, PrimitiveFunction.NEGATE);
                        tmpCopyBE.modifyRow(i, 0, PrimitiveFunction.NEGATE);
                    }
                }

                tmpLinBuilder.equalities(tmpCopyAE, tmpCopyBE);
            }

            final LinearSolver tmpLinearSolver = tmpLinBuilder.build();

            final Result tmpLinearResult = tmpLinearSolver.solve();

            tmpFeasible = tmpLinearResult.getState().isFeasible();

            for (int i = 0; tmpFeasible && (i < tmpNumberOfVariables); i++) {
                this.setX(i, tmpLinearResult.doubleValue(i) - tmpLinearResult.doubleValue(tmpNumberOfVariables + i));
            }
        }

        if (tmpFeasible) {

            this.setState(State.FEASIBLE);

            final int[] tmpIncluded = myActivator.getIncluded();
            final MatrixStore<Double> tmpSIincl = this.getSI(tmpIncluded);
            for (int i = 0; i < tmpIncluded.length; i++) {
                final double tmpVal = tmpSIincl.doubleValue(i);
                if (!options.slack.isZero(tmpVal)) {
                    myActivator.exclude(tmpIncluded[i]);
                }
            }

            final int[] tmpExcluded = myActivator.getExcluded();
            final MatrixStore<Double> tmpSIexcl = this.getSI(tmpExcluded);
            final int tmpMaxToAct = this.countVariables() - this.countEqualityConstraints();
            for (int i = 0; (i < tmpExcluded.length) && (myActivator.countIncluded() < tmpMaxToAct); i++) {
                final double tmpVal = tmpSIexcl.doubleValue(i);
                if (options.slack.isZero(tmpVal)) {
                    myActivator.include(tmpExcluded[i]);
                }
            }

        } else {

            this.setState(State.INFEASIBLE);

            this.resetX();
        }

        if (this.isDebug()) {
            this.debug("Initial solution: {}", this.getX().copy());
            if (tmpAE != null) {
                this.debug("Initial E-slack: {}", this.getSE().copy());
            }
            if (tmpAI != null) {
                this.debug("Initial I-included-slack: {}", this.getSI(myActivator.getIncluded()).copy());
                this.debug("Initial I-excluded-slack: {}", this.getSI(myActivator.getExcluded()).copy());
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
        final Output tmpOutput = tmpSolver.solve(tmpInput, options.validate);

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
                this.debug("Current: {}", this.getX());
                this.debug("Step: {}", tmpSubX);
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
                    this.debug("Slack (numerator): {}", tmpNumer);
                    this.debug("Scaler (denominator): {}", tmpDenom);
                    this.debug("Looking for the largest possible step length (smallest positive scalar) among these: {}).", tmpStepLengths.toString());
                }

                double tmpStepLength = PrimitiveMath.ONE;
                for (int i = 0; i < tmpExcluded.length; i++) {

                    final double tmpN = tmpNumer.doubleValue(i);
                    final double tmpD = tmpDenom.doubleValue(i);
                    final double tmpVal = tmpN / tmpD;

                    if ((tmpD > PrimitiveMath.ZERO) && (tmpVal >= PrimitiveMath.ZERO) && (tmpVal < tmpStepLength)
                            && !options.solution.isSmall(tmpFrobNormX, tmpD)) {
                        tmpStepLength = tmpVal;
                        myConstraintToInclude = tmpExcluded[i];
                        if (this.isDebug()) {
                            this.debug("Best so far: {} @ {} ({}).", tmpStepLength, i, myConstraintToInclude);
                        }
                    }
                }

                if (tmpStepLength > PrimitiveMath.ZERO) {
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

            if (this.isDebug()) {
                this.debug("Did shrink!");
            }

            this.performIteration();

        } else if (this.isFeasible(false)) {
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
            this.debug("\tSolution: {}", this.getX().copy());
            if (this.getAE() != null) {
                this.debug("\tE-slack: {}", this.getSE().copy());
            }
            if (this.getAI() != null) {
                this.debug("\tI-included-slack: {}", this.getSI(myActivator.getIncluded()).copy());
                this.debug("\tI-excluded-slack: {}", this.getSI(myActivator.getExcluded()).copy());
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
