/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.type.IndexSelector;

abstract class ActiveSetSolver extends ConstrainedSolver {

    final IndexSelector myActivator;
    int myConstraintToInclude = -1;
    MatrixStore<Double> myInvQC;
    final PrimitiveDenseStore myIterationL;
    final PrimitiveDenseStore myIterationX;

    ActiveSetSolver(final Builder matrices, final Options solverOptions) {

        super(matrices, solverOptions);

        final int tmpCountVariables = this.countVariables();
        final int tmpCountInequalityConstraints = this.countInequalityConstraints();
        final int tmpCountEqualityConstraints = this.countEqualityConstraints();

        myActivator = new IndexSelector(tmpCountInequalityConstraints);

        myIterationL = PrimitiveDenseStore.FACTORY.makeZero(tmpCountEqualityConstraints + tmpCountInequalityConstraints, 1L);
        myIterationX = PrimitiveDenseStore.FACTORY.makeZero(tmpCountVariables, 1L);
    }

    public int countExcluded() {
        return myActivator.countExcluded();
    }

    public int countIncluded() {
        return myActivator.countIncluded();
    }

    public int[] getExcluded() {
        return myActivator.getExcluded();
    }

    public int[] getIncluded() {
        return myActivator.getIncluded();
    }

    public int getLastExcluded() {
        return myActivator.getLastExcluded();
    }

    public int getLastIncluded() {
        return myActivator.getLastIncluded();
    }

    protected boolean checkFeasibility(final boolean onlyExcluded) {

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
        final MatrixStore<Double> tmpLI = myIterationL.builder().offsets(this.countEqualityConstraints(), 0).row(tmpIncluded).get();

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
    protected int suggestConstraintToInclude() {
        return myConstraintToInclude;
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

}
