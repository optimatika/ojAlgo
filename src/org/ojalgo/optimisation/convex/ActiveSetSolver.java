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
import static org.ojalgo.function.PrimitiveFunction.*;

import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.type.IndexSelector;

/**
 * Solves optimisation problems of the form:
 * <p>
 * min 1/2 [X]<sup>T</sup>[Q][X] - [C]<sup>T</sup>[X]<br>
 * when [AE][X] == [BE]<br>
 * and [AI][X] &lt;= [BI]
 * </p>
 * Where [AE] and [BE] are optinal.
 *
 * @author apete
 */
abstract class ActiveSetSolver extends ConstrainedSolver {

    private final IndexSelector myActivator;
    private int myConstraintToInclude = -1;
    private final PrimitiveDenseStore myIterationX;

    MatrixStore<Double> myInvQC;

    ActiveSetSolver(final ConvexSolver.Builder matrices, final Optimisation.Options solverOptions) {

        super(matrices, solverOptions);

        myActivator = new IndexSelector(this.countInequalityConstraints());

        myIterationX = PrimitiveDenseStore.FACTORY.makeZero(this.countVariables(), 1L);

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
    protected final MatrixStore<Double> getIterationKKT() {
        return this.getIterationKKT(myActivator.getIncluded());
    }

    protected final MatrixStore<Double> getIterationKKT(final int[] included) {
        final MatrixStore<Double> tmpIterationQ = this.getIterationQ();
        final MatrixStore<Double> tmpIterationA = this.getIterationA(included);
        return tmpIterationQ.builder().right(tmpIterationA.transpose()).below(tmpIterationA).build();
    }

    @Override
    protected final MatrixStore<Double> getIterationRHS() {
        return this.getIterationRHS(myActivator.getIncluded());
    }

    protected final MatrixStore<Double> getIterationRHS(final int[] included) {
        final MatrixStore<Double> tmpIterationC = this.getIterationC();
        final MatrixStore<Double> tmpIterationB = this.getIterationB(included);
        return tmpIterationC.builder().below(tmpIterationB).build();
    }

    @Override
    protected boolean initialise(final Result kickStarter) {

        super.initialise(kickStarter);

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
        final boolean tmpUsableKickStarter = (kickStarter != null) && kickStarter.getState().isApproximate();

        if (tmpUsableKickStarter) {
            this.fillX(kickStarter);
            tmpFeasible = this.checkFeasibility(false);
        }

        if (!tmpFeasible) {
            // Form LP to check feasibility

            final MatrixStore<Double> tmpGradient = tmpUsableKickStarter ? tmpQ.multiply(tmpX).subtract(tmpC) : tmpC.negate();

            final MatrixStore<Double> tmpLinearC = tmpGradient.builder().below(tmpGradient.negate()).below(tmpNumInes).build();

            final LinearSolver.Builder tmpLinearBuilder = LinearSolver.getBuilder(tmpLinearC);

            MatrixStore<Double> tmpAEpart = null;
            MatrixStore<Double> tmpBEpart = null;

            if (tmpNumEqus > 0) {
                tmpAEpart = tmpAE.builder().right(tmpAE.negate()).right(tmpNumInes).build();
                tmpBEpart = tmpBE;
            }

            if (tmpNumInes > 0) {
                final MatrixStore<Double> tmpAIpart = tmpAI.builder().right(tmpAI.negate()).right(MatrixStore.PRIMITIVE.makeIdentity(tmpNumInes).get()).build();
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
                        tmpLinearAE.modifyRow(i, 0, NEGATE);
                        tmpLinearBE.modifyRow(i, 0, NEGATE);
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
                @SuppressWarnings("deprecation")
                final double[] tmpResidual = tmpLinearSolver.getResidualCosts();
                for (int i = tmpNumVars * 2; i < tmpResidual.length; i++) {
                    final int tmpIndexToInclude = i - (2 * tmpNumVars);
                    this.setLI(tmpIndexToInclude, tmpResidual[i]);
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

            while ((tmpNumEqus + myActivator.countIncluded()) > tmpNumVars) {
                this.shrink();
            }

            if (this.isDebug() && ((tmpNumEqus + myActivator.countIncluded()) > tmpNumVars)) {
                this.debug("Redundant contraints!");
            }

            myInvQC = myCholesky.solve(this.getIterationC());

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
            this.debug("\nPerformIteration {}", 1 + this.countIterations());
            this.debug(myActivator.toString());
        }

        myConstraintToInclude = -1;
        final int[] tmpIncluded = myActivator.getIncluded();

        final MatrixStore<Double> tmpIterQ = this.getIterationQ();
        final MatrixStore<Double> tmpIterC = this.getIterationC();
        final MatrixStore<Double> tmpIterA = this.getIterationA(tmpIncluded);
        final MatrixStore<Double> tmpIterB = this.getIterationB(tmpIncluded);

        boolean tmpSolvable = false;

        final PrimitiveDenseStore tmpIterX = myIterationX;
        final PrimitiveDenseStore tmpIterL = PrimitiveDenseStore.FACTORY.makeZero(tmpIterA.countRows(), 1L);

        if ((tmpIterA.countRows() < tmpIterA.countColumns()) && (tmpSolvable = myCholesky.isSolvable())) {
            // Q is SPD

            if (tmpIterA.countRows() == 0L) {
                // Unconstrained - can happen when PureASS and all inequalities are inactive

                myCholesky.solve(tmpIterC, tmpIterX);

            } else {
                // Actual/normal optimisation problem

                final MatrixStore<Double> tmpInvQAT = myCholesky.solve(tmpIterA.transpose());
                // TODO Only 1 column change inbetween active set iterations (add or remove 1 column)

                // Negated Schur complement
                // final MatrixStore<Double> tmpS = tmpIterA.multiply(tmpInvQAT);
                final ElementsSupplier<Double> tmpS = tmpInvQAT.multiplyLeft(tmpIterA);
                // TODO Symmetric, only need to calculate halv the Schur complement

                if (tmpSolvable = myLU.compute(tmpS)) {

                    // tmpIterX temporarely used to store tmpInvQC
                    // final MatrixStore<Double> tmpInvQC = myCholesky.solve(tmpIterC, tmpIterX);
                    //TODO Constant if C doesn't change

                    //tmpIterL = myLU.solve(tmpInvQC.multiplyLeft(tmpIterA));
                    //myLU.solve(tmpIterA.multiply(myInvQC).subtract(tmpIterB), tmpIterL);
                    myLU.solve(myInvQC.multiplyLeft(tmpIterA).operateOnMatching(SUBTRACT, tmpIterB), tmpIterL);

                    //myCholesky.solve(tmpIterC.subtract(tmpIterA.transpose().multiply(tmpIterL)), tmpIterX);
                    myCholesky.solve(tmpIterL.multiplyLeft(tmpIterA.transpose()).operateOnMatching(tmpIterC, SUBTRACT), tmpIterX);
                }
            }
        }

        if (!tmpSolvable && (tmpSolvable = myLU.compute(this.getIterationKKT(tmpIncluded)))) {
            // The above failed, but the KKT system is solvable
            // Try solving the full KKT system instaed

            final MatrixStore<Double> tmpXL = myLU.solve(this.getIterationRHS(tmpIncluded));
            tmpIterX.fillMatching(tmpXL.builder().rows(0, this.countVariables()).build());
            tmpIterL.fillMatching(tmpXL.builder().rows(this.countVariables(), (int) tmpXL.count()).build());
        }

        if (!tmpSolvable && this.isDebug()) {
            options.debug_appender.println("KKT system unsolvable!");
            options.debug_appender.printmtrx("KKT", this.getIterationKKT());
            options.debug_appender.printmtrx("RHS", this.getIterationRHS());
        }

        if (tmpSolvable) {
            // Subproblem solved successfully

            tmpIterX.fillMatching(tmpIterX, SUBTRACT, this.getX());

            if (this.isDebug()) {
                this.debug("Current: {}", this.getX().asList());
                this.debug("Step: {}", tmpIterX.copy().asList());
            }

            final double tmpNormCurrentX = this.getX().aggregateAll(Aggregator.NORM2);
            final double tmpNormStepX = tmpIterX.aggregateAll(Aggregator.NORM2);
            if (!options.solution.isSmall(tmpNormCurrentX, tmpNormStepX)) {
                // Non-zero solution

                double tmpStepLength = ONE;

                final int[] tmpExcluded = myActivator.getExcluded();
                if (tmpExcluded.length > 0) {

                    final MatrixStore<Double> tmpNumer = this.getSI(tmpExcluded);
                    final MatrixStore<Double> tmpDenom = this.getAI().builder().row(tmpExcluded).build().multiply(tmpIterX);
                    final PhysicalStore<Double> tmpStepLengths = tmpNumer.copy();
                    tmpStepLengths.fillMatching(tmpStepLengths, DIVIDE, tmpDenom);

                    if (this.isDebug()) {
                        this.debug("Looking for the largest possible step length (smallest positive scalar) among these: {}).", tmpStepLengths.asList());
                    }

                    for (int i = 0; i < tmpExcluded.length; i++) {

                        final double tmpN = tmpNumer.doubleValue(i); // Current slack
                        final double tmpD = tmpDenom.doubleValue(i); // Proposed slack change
                        final double tmpVal = options.slack.isSmall(tmpD, tmpN) ? ZERO : tmpN / tmpD;

                        if ((tmpD > ZERO) && (tmpVal >= ZERO) && (tmpVal < tmpStepLength) && !options.solution.isSmall(tmpNormStepX, tmpD)) {
                            tmpStepLength = tmpVal;
                            myConstraintToInclude = tmpExcluded[i];
                            if (this.isDebug()) {
                                this.debug("Best so far: {} @ {} ({}).", tmpStepLength, i, myConstraintToInclude);
                            }
                        }
                    }

                }

                if (tmpStepLength > ZERO) { // It is possible that it becomes == 0.0
                    this.getX().maxpy(tmpStepLength, tmpIterX);
                }

                this.setState(State.APPROXIMATE);

            } else if (this.isDebug()) {
                // Zero solution

                if (this.isDebug()) {
                    this.debug("Step too small!");
                }

                this.setState(State.FEASIBLE);
            }

            for (int i = 0; i < this.countEqualityConstraints(); i++) {
                this.setLE(i, tmpIterL.doubleValue(i));
            }

            for (int i = 0; i < tmpIncluded.length; i++) {
                this.setLI(tmpIncluded[i], tmpIterL.doubleValue(this.countEqualityConstraints() + i));
            }

        } else if (tmpIncluded.length >= 1) {
            // Subproblem NOT solved successfully
            // At least 1 active inequality

            this.shrink();

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
            if ((this.getAE() != null) && (this.getAE().count() > 0)) {
                this.debug("\tE-slack: {}", this.getSE().copy().asList());
            }
            if ((this.getAI() != null) && (this.getAI().count() > 0)) {
                if (tmpIncluded.length != 0) {
                    this.debug("\tI-included-slack: {}", this.getSI(tmpIncluded).copy().asList());
                }
                if (myActivator.getExcluded().length != 0) {
                    this.debug("\tI-excluded-slack: {}", this.getSI(myActivator.getExcluded()).copy().asList());
                }
            }
        }
    }

    @Override
    final MatrixStore<Double> getIterationA() {
        return this.getIterationA(myActivator.getIncluded());
    }

    abstract MatrixStore<Double> getIterationA(int[] included);

    @Override
    final MatrixStore<Double> getIterationB() {
        return this.getIterationB(myActivator.getIncluded());
    }

    abstract MatrixStore<Double> getIterationB(int[] included);

    @Override
    final MatrixStore<Double> getIterationC() {

        //        final MatrixStore<Double> tmpQ = this.getQ();
        //        final MatrixStore<Double> tmpC = this.getC();
        //
        //        final PhysicalStore<Double> tmpX = this.getX();
        //
        //        return tmpC.subtract(tmpQ.multiply(tmpX));

        return this.getC();
    }

    void shrink() {

        final int[] tmpIncluded = myActivator.getIncluded();

        int tmpToExclude = -1;
        double tmpMaxLagrange = ZERO;

        final MatrixStore<Double> tmpLI = this.getLI(tmpIncluded);
        for (int i = 0; i < tmpIncluded.length; i++) {
            final double tmpVal = Math.abs(tmpLI.doubleValue(i));
            if (tmpVal >= tmpMaxLagrange) {
                tmpMaxLagrange = tmpVal;
                tmpToExclude = tmpIncluded[i];
            }
        }
        myActivator.exclude(tmpToExclude);
    }

}
