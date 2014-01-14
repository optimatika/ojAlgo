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
package org.ojalgo.optimisation.quadratic;

import static org.ojalgo.constant.PrimitiveMath.*;

import java.util.HashSet;

import org.ojalgo.matrix.store.AboveBelowStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.RowsStore;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelEntity;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.type.IndexSelector;

/**
 * @author apete
 */
final class ActiveSetSolver extends QuadraticSolver {

    private final IndexSelector myActivator;

    ActiveSetSolver(final ExpressionsBasedModel aModel, final Optimisation.Options solverOptions, final QuadraticSolver.Builder aBuilder) {

        super(aModel, solverOptions, aBuilder);

        if (this.hasInequalityConstraints()) {
            myActivator = new IndexSelector(this.countInequalityConstraints());
        } else {
            myActivator = new IndexSelector(0);
        }

        int tmpIterationsLimit = (int) Math.max(this.getAI().countRows(), this.getAI().countColumns());
        tmpIterationsLimit = (int) (9.0 + Math.sqrt(tmpIterationsLimit));
        tmpIterationsLimit = tmpIterationsLimit * tmpIterationsLimit;

        options.iterations_abort = tmpIterationsLimit;

        //BasicLogger.logDebug("AS solver innequalities: " + this.countInequalityConstraints());
    }

    private QuadraticSolver buildIterationSolver() {

        MatrixStore<Double> tmpSubAE = null;
        MatrixStore<Double> tmpSubBE = null;
        final MatrixStore<Double> tmpSubQ = this.getQ();
        final MatrixStore<Double> tmpSubC = this.getC();

        final int[] tmpActivator = myActivator.getIncluded();

        if (tmpActivator.length == 0) {
            if (this.hasEqualityConstraints()) {
                tmpSubAE = this.getAE();
                tmpSubBE = this.getBE();
            } else {
                tmpSubAE = null;
                tmpSubBE = null;
            }
        } else {
            if (this.hasEqualityConstraints()) {
                tmpSubAE = new AboveBelowStore<Double>(this.getAE(), new RowsStore<Double>(this.getAI(), tmpActivator));
                tmpSubBE = new AboveBelowStore<Double>(this.getBE(), new RowsStore<Double>(this.getBI(), tmpActivator));
            } else {
                tmpSubAE = new RowsStore<Double>(this.getAI(), tmpActivator);
                tmpSubBE = new RowsStore<Double>(this.getBI(), tmpActivator);
            }
        }

        final Builder retVal = new Builder(tmpSubQ, tmpSubC);
        if ((tmpSubAE != null) && (tmpSubBE != null)) {
            retVal.equalities(tmpSubAE, tmpSubBE);
        }
        return retVal.build(options);
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
                        this.debug("Best so far: {} @ {}.", tmpMin, retVal);
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
                    this.debug("Only the last included needs to be excluded: {} @ {}.", tmpMin, retVal);
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

        int retVal = -1;

        final int[] tmpExcluded = myActivator.getExcluded();
        final int tmpLastExcluded = myActivator.getLastExcluded();
        int tmpIndexOfLast = -1;

        double tmpMin = POSITIVE_INFINITY;
        double tmpVal;

        final MatrixStore<Double> tmpSI = this.getSI(tmpExcluded);

        if (this.isDebug() && (tmpSI.count() > 0L)) {
            this.debug("Looking for the largest negative slack among these: {}.", tmpSI.copy().toString());
        }

        for (int i = 0; i < tmpSI.countRows(); i++) {
            if (tmpExcluded[i] != tmpLastExcluded) {

                tmpVal = tmpSI.doubleValue(i, 0);

                if ((tmpVal < ZERO) && (tmpVal < tmpMin) && !options.slack.isZero(tmpVal)) {
                    tmpMin = tmpVal;
                    retVal = i;
                    if (this.isDebug()) {
                        this.debug("Best so far: {} @ {}.", tmpMin, retVal);
                    }
                }

            } else {

                tmpIndexOfLast = i;
            }
        }

        if ((retVal < 0) && (tmpIndexOfLast >= 0)) {

            tmpVal = tmpSI.doubleValue(tmpIndexOfLast, 0);

            if ((tmpVal < ZERO) && (tmpVal < tmpMin) && !options.slack.isZero(tmpVal)) {
                tmpMin = tmpVal;
                retVal = tmpIndexOfLast;
                if (this.isDebug()) {
                    this.debug("Only the last excluded needs to be included: {} @ {}.", tmpMin, retVal);
                }
            }
        }

        return retVal >= 0 ? tmpExcluded[retVal] : retVal;
    }

    @Override
    protected Result buildResult() {

        final Result retVal = super.buildResult();

        final ExpressionsBasedModel tmpModel = this.getModel();

        if (tmpModel != null) {

            //BasicLogger.logDebug("A S Iterations: " + this.countIterations());

            final HashSet<ModelEntity<?>> tmpActiveInequalityEntities = new HashSet<>();

            final ModelEntity<?>[] tmpInequalityEntities = this.getInequalityEnities();
            final int[] tmpActiveIndeces = myActivator.getIncluded();

            for (final int tmpIndexOfActive : tmpActiveIndeces) {
                tmpActiveInequalityEntities.add(tmpInequalityEntities[tmpIndexOfActive]);
            }

            tmpModel.markActiveInequalityConstraints(tmpActiveInequalityEntities);
        }

        return retVal;
    }

    @Override
    protected MatrixStore<Double> extractSolution() {
        return super.extractSolution();
    }

    @Override
    protected boolean initialise(final Result kickStart) {

        if (kickStart != null) {

            this.fillX(kickStart);

            myActivator.excludeAll();
            if (kickStart.isActiveSetDefined()) {
                final int[] tmpActiveSet = kickStart.getActiveSet();
                myActivator.include(tmpActiveSet);
            }

            final int[] tmpIncluded = myActivator.getIncluded();

            final MatrixStore<Double> tmpSlack = this.getSI(tmpIncluded);

            for (int i = 0; i < tmpSlack.countRows(); i++) {
                final double tmpVal = tmpSlack.doubleValue(i);
                if (!options.slack.isZero(tmpVal)) {
                    myActivator.exclude(tmpIncluded[i]);
                }
            }
        }

        return true;
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
            tmpToExclude = this.suggestConstraintToExclude();
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

    @Override
    protected void performIteration() {

        final QuadraticSolver tmpSolver = this.buildIterationSolver();
        final Optimisation.Result tmpResult = tmpSolver.solve();

        final int[] tmpIncluded = myActivator.getIncluded();

        final int tmpCountVariables = this.countVariables();
        final int tmpCountEqualityConstraints = this.countEqualityConstraints();
        final int tmpCountActiveInequalityConstraints = tmpIncluded.length;

        if (tmpResult.getState().isFeasible()) {

            final MatrixStore<Double> tmpSolutionX = tmpSolver.getSolutionX();
            final MatrixStore<Double> tmpSolutionLE = tmpSolver.getSolutionLE();

            for (int i = 0; i < tmpCountVariables; i++) {
                this.setX(i, tmpSolutionX.doubleValue(i));
            }

            for (int i = 0; i < tmpCountEqualityConstraints; i++) {
                this.setLE(i, tmpSolutionLE.doubleValue(i));
            }

            for (int i = 0; i < tmpCountActiveInequalityConstraints; i++) {
                this.setLI(tmpIncluded[i], tmpSolutionLE.doubleValue(tmpCountEqualityConstraints + i));
            }

            this.setState(State.APPROXIMATE);

        } else if (tmpCountActiveInequalityConstraints >= 1) {

            if ((myActivator.countIncluded() > 2) && myActivator.isLastIncluded()) {
                myActivator.revertLastInclusion();
            }

            myActivator.shrink();

            if (this.isDebug()) {
                this.debug("Did shrink!");
                this.debug(myActivator.toString());
            }

            this.performIteration();

        } else {

            this.resetX();
            this.setState(State.INFEASIBLE);

            throw new IllegalArgumentException("Not able to solve this problem!");
        }

    }

    @Override
    protected boolean validate() {

        final boolean retVal = true;
        this.setState(State.VALID);

        return retVal;
    }

}
