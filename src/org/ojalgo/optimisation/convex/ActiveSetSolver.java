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

import java.util.HashSet;

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.BigMatrix;
import org.ojalgo.matrix.store.AboveBelowStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.RowsStore;
import org.ojalgo.matrix.store.ZeroStore;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelEntity;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.convex.KKTSolver.Input;
import org.ojalgo.optimisation.convex.KKTSolver.Output;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.type.IndexSelector;

/**
 * @author apete
 */
public final class ActiveSetSolver extends ConvexSolver {

    private final IndexSelector myActivator;

    private transient KKTSolver myDelegateSolver = null;

    private int myConstraintToInclude = -1;
    private boolean myNeedsAnotherIteration = false;

    ActiveSetSolver(final ExpressionsBasedModel aModel, final Optimisation.Options solverOptions, final ConvexSolver.Builder aBuilder) {

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

        // BasicLogger.logDebug("AS solver innequalities: " + this.countInequalityConstraints());
    }

    private KKTSolver.Input buildDelegateSolverInput() {

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

        return new KKTSolver.Input(tmpSubQ, tmpSubC.add(tmpSubQ.multiplyRight(tmpX).negate()), tmpSubAE, ZeroStore.makePrimitive((int) tmpSubAE.countRows(), 1));
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
    protected Result buildResult() {

        final Result retVal = super.buildResult();

        final ExpressionsBasedModel tmpModel = this.getModel();

        if (tmpModel != null) {

            // BasicLogger.logDebug("A S Iterations: " + this.countIterations());

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

        myActivator.excludeAll();

        if (kickStart != null) {

            this.fillX(kickStart);

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

        } else if (this.getModel() != null) {

            final LinearSolver tmpLinearSolver = LinearSolver.make(this.getModel());
            final Result tmpResult = tmpLinearSolver.solve(kickStart);

            if (tmpResult.getState().isFeasible()) {

                this.fillX(tmpResult);

                if (this.isDebug()) {
                    this.debug("Initial E-slack: {}", this.getSE().copy());
                    this.debug("Initial I-included-slack: {}", this.getSI(myActivator.getIncluded()).copy());
                    this.debug("Initial I-excluded-slack: {}", this.getSI(myActivator.getExcluded()).copy());
                }

                final int[] tmpExcluded = myActivator.getExcluded();

                final MatrixStore<Double> tmpSlack = this.getSI(tmpExcluded);

                for (int i = 0; i < tmpSlack.countRows(); i++) {
                    final double tmpVal = tmpSlack.doubleValue(i);
                    if (options.slack.isZero(tmpVal)) {
                        myActivator.include(tmpExcluded[i]);
                    }
                }

                this.setState(State.FEASIBLE);

                final int[] tmpIncluded = myActivator.getIncluded();

                //                final int tmpIneg = this.countVariables() - this.countEqualityConstraints();
                //                while (myActivator.countIncluded() >= tmpIneg) {
                //                    myActivator.shrink();
                //                }

                //                final QR<Double> tmpQR = QRDecomposition.makePrimitive();
                //                tmpQR.compute(this.getAI().builder().row(tmpIncluded).build());
                //                final MatrixStore<Double> tmpLagr = tmpQR.solve(this.getC().builder().superimpose(this.getQ().multiplyRight(this.getX()).negate()).build());
                //
                //                for (int i = 0; i < tmpIncluded.length; i++) {
                //                    if (tmpLagr.doubleValue(i) < PrimitiveMath.ZERO) {
                //                        myActivator.exclude(tmpIncluded[i]);
                //                    }
                //                }

            } else {

                this.resetX();

                this.setState(State.INFEASIBLE);
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
                if (myNeedsAnotherIteration) {
                    this.setState(State.APPROXIMATE);
                    return true;
                } else {
                    this.setState(State.OPTIMAL);
                    return false;
                }
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

        myNeedsAnotherIteration = false;
        myConstraintToInclude = -1;

        final Input tmpInput = this.buildDelegateSolverInput();
        final KKTSolver tmpSolver = this.getDelegateSolver(tmpInput);
        final Output tmpOutput = tmpSolver.solve(tmpInput, options.validate);

        final int[] tmpIncluded = myActivator.getIncluded();

        final int tmpCountVariables = this.countVariables();
        final int tmpCountEqualityConstraints = this.countEqualityConstraints();
        final int tmpCountActiveInequalityConstraints = tmpIncluded.length;

        if (tmpOutput.isSolvable()) {

            final MatrixStore<Double> tmpSubX = tmpOutput.getX();
            final MatrixStore<Double> tmpSubL = tmpOutput.getL();

            if (options.validate) {
                if (this.isDebug()) {

                    final MatrixStore<Double> tmpSubXL = tmpSubX.builder().below(tmpSubL).build().copy(); // Copy avoid problem when/if "L" has 0 rows.
                    final PhysicalStore<Double> tmpSubSlack = tmpInput.getRHS().copy();
                    tmpSubSlack.fillMatching(tmpSubSlack, PrimitiveFunction.SUBTRACT, tmpInput.getKKT().multiplyRight(tmpSubXL));

                    final double tmpLargest = tmpSubSlack.aggregateAll(Aggregator.LARGEST);

                    if (tmpLargest > Math.sqrt(PrimitiveMath.IS_ZERO)) {
                        this.debug("KKT slack: {}", tmpSubSlack);
                        this.debug("KKT X: {}", tmpSubX);
                        if ((this.getAE() != null) && (this.getAE().count() != 0L)) {
                            this.debug("KKT AE*X: {}", this.getAE().multiplyRight(tmpSubX));
                        }
                        if ((this.getAI() != null) && (this.getAI().count() != 0L)) {
                            this.debug("KKT AI*X: {}", this.getAI().multiplyRight(tmpSubX));
                        }
                    }
                }
            }

            final double tmpFrobNormX = tmpSubX.aggregateAll(Aggregator.NORM2);
            if (tmpFrobNormX > 1.0E-10) {

                final int[] tmpExcluded = myActivator.getExcluded();

                final MatrixStore<Double> tmpNumer = this.getSI(tmpExcluded);
                final MatrixStore<Double> tmpDenom = this.getAI().builder().row(tmpExcluded).build().multiplyRight(tmpSubX);
                final PhysicalStore<Double> tmpStepLengths = tmpNumer.copy();
                tmpStepLengths.fillMatching(tmpStepLengths, PrimitiveFunction.DIVIDE, tmpDenom);

                if (this.isDebug()) {
                    this.debug("Current: {}", this.getX());
                    this.debug("Step: {}", tmpSubX);
                    this.debug("Slack: {}", tmpNumer);
                    this.debug("Scaler: {}", tmpDenom);
                    this.debug("Looking for the largest possible step length (smallest positive scalar among these: {}).", tmpStepLengths.toString());
                }

                double tmpStepLength = PrimitiveMath.POSITIVE_INFINITY;
                for (int i = 0; i < tmpExcluded.length; i++) {

                    final double tmpN = tmpNumer.doubleValue(i);
                    final double tmpD = tmpDenom.doubleValue(i);
                    final double tmpVal = tmpN / tmpD;

                    if ((tmpD > PrimitiveMath.ZERO) && !options.slack.isZero(tmpD) && (tmpVal >= PrimitiveMath.ZERO) && (tmpVal < tmpStepLength)) {
                        tmpStepLength = tmpVal;
                        myConstraintToInclude = tmpExcluded[i];
                        if (this.isDebug()) {
                            this.debug("Best so far: {} @ {} ({}).", tmpStepLength, i, myConstraintToInclude);
                        }
                    }
                }

                if (myConstraintToInclude >= 0) {
                    if (tmpStepLength >= PrimitiveMath.ONE) {
                        this.getX().maxpy(PrimitiveMath.ONE, tmpSubX);
                    } else if (tmpStepLength > PrimitiveMath.ZERO) {
                        this.getX().maxpy(tmpStepLength, tmpSubX);
                    }
                }
            }

            if (options.validate && (this.getModel() != null)) {
                if (!this.getModel().validate(BigMatrix.FACTORY.columns(this.getX()))) {
                    if (this.isDebug()) {
                        this.debug("Solution not feasible {}: ", this.getX());
                    }
                }
            }

            for (int i = 0; i < tmpCountEqualityConstraints; i++) {
                this.setLE(i, tmpSubL.doubleValue(i));
            }

            for (int i = 0; i < tmpCountActiveInequalityConstraints; i++) {
                this.setLI(tmpIncluded[i], tmpSubL.doubleValue(tmpCountEqualityConstraints + i));
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

    KKTSolver getDelegateSolver(final KKTSolver.Input templeate) {
        if (myDelegateSolver == null) {
            myDelegateSolver = new KKTSolver(templeate);
        }
        return myDelegateSolver;
    }

}
