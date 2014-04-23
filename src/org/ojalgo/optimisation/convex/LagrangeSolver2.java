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
import static org.ojalgo.function.PrimitiveFunction.*;

import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.decomposition.*;
import org.ojalgo.matrix.store.AboveBelowStore;
import org.ojalgo.matrix.store.LeftRightStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.ZeroStore;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;

/**
 * @author apete
 */
public final class LagrangeSolver2 extends ConvexSolver {

    private final LU<Double> myLU = LUDecomposition.makePrimitive();
    private final SingularValue<Double> mySingularValue = SingularValueDecomposition.makePrimitive();

    LagrangeSolver2(final ExpressionsBasedModel aModel, final Optimisation.Options solverOptions, final ConvexSolver.Builder matrices) {
        super(aModel, solverOptions, matrices);
    }

    private void extractSolution(final Builder aSolver) {

        final MatrixStore<Double> tmpSolutionX = aSolver.getX();

        final int tmpCountVariables = this.countVariables();
        final int tmpCountEqualityConstraints = this.countEqualityConstraints();

        for (int i = 0; i < tmpCountVariables; i++) {
            this.setX(i, tmpSolutionX.doubleValue(i));
        }

        for (int i = 0; i < tmpCountEqualityConstraints; i++) {
            this.setLE(i, tmpSolutionX.doubleValue(tmpCountVariables + i));
        }
    }

    private Builder makeBuilder(final boolean addSmallDiagonal) {
        Builder tmpBuilder = null;

        MatrixStore<Double> tmpQ = this.getQ();
        final MatrixStore<Double> tmpC = this.getC();

        if (addSmallDiagonal) {

            final PhysicalStore<Double> tmpCopyQ = tmpQ.copy();

            final double tmpLargest = tmpCopyQ.aggregateAll(Aggregator.LARGEST);
            final double tmpRelativelySmall = MACHINE_DOUBLE_ERROR * tmpLargest;
            final double tmpPracticalLimit = MACHINE_DOUBLE_ERROR + IS_ZERO;
            final double tmpSmallToAdd = Math.max(tmpRelativelySmall, tmpPracticalLimit);

            final UnaryFunction<Double> tmpFunc = ADD.second(tmpSmallToAdd);

            tmpCopyQ.modifyDiagonal(0, 0, tmpFunc);
            tmpQ = tmpCopyQ;
        }

        if (this.hasEqualityConstraints()) {

            final MatrixStore<Double> tmpAE = this.getAE();
            final MatrixStore<Double> tmpBE = this.getBE();

            final int tmpZeroSize = (int) tmpAE.countRows();

            final MatrixStore<Double> tmpUpperLeftAE = tmpQ;
            final MatrixStore<Double> tmpUpperRightAE = tmpAE.builder().transpose().build();
            final MatrixStore<Double> tmpLowerLefAE = tmpAE;
            final MatrixStore<Double> tmpLowerRightAE = ZeroStore.makePrimitive(tmpZeroSize, tmpZeroSize);

            // tmpUpperLeftAE = tmpUpperLeftAE.builder().superimpose(0, 0, tmpUpperRightAE.multiplyRight(tmpLowerLefAE)).build();

            final MatrixStore<Double> tmpSubAE = new AboveBelowStore<Double>(new LeftRightStore<Double>(tmpUpperLeftAE, tmpUpperRightAE),
                    new LeftRightStore<Double>(tmpLowerLefAE, tmpLowerRightAE));

            final MatrixStore<Double> tmpUpperBE = tmpC;
            final MatrixStore<Double> tmpLowerBE = tmpBE;

            // tmpUpperBE = tmpUpperBE.builder().superimpose(0, 0, tmpUpperRightAE.multiplyRight(tmpLowerBE)).build();

            final MatrixStore<Double> tmpSubBE = new AboveBelowStore<Double>(tmpUpperBE, tmpLowerBE);

            tmpBuilder = new Builder().equalities(tmpSubAE, tmpSubBE);

        } else {

            tmpBuilder = new Builder().equalities(tmpQ, tmpC);
        }
        return tmpBuilder;
    }

    private void performIteration(final Builder builder) {

        final MatrixStore<Double> tmpAE = builder.getAE();
        final MatrixStore<Double> tmpBE = builder.getBE();

        final DecompositionStore<Double> tmpX = builder.getX();

        myLU.compute(tmpAE);

        if (myLU.isSolvable()) {

            if (this.isDebug()) {
                this.debug("LU solvable");
            }

            final MatrixStore<Double> tmpSolution = myLU.solve(tmpBE, tmpX);
            this.setState(State.DISTINCT);
            if (tmpSolution != tmpX) {
                tmpX.fillMatching(tmpSolution);
            }

        } else {

            if (this.isDebug()) {
                this.debug("LU not solvable, trying SVD");
            }

            mySingularValue.compute(tmpAE);

            if (mySingularValue.isSolvable()) {

                if (this.isDebug()) {
                    this.debug("SVD solvable");
                }

                final MatrixStore<Double> tmpSolution = mySingularValue.solve(tmpBE, tmpX);
                this.setState(State.OPTIMAL);
                if (tmpSolution != tmpX) {
                    tmpX.fillMatching(tmpSolution);
                }

                final AggregatorFunction<Double> tmpFrobNormCalc = PrimitiveAggregator.getCollection().norm2();
                final MatrixStore<Double> tmpSlack = builder.getSE();
                tmpSlack.visitAll(tmpFrobNormCalc);

                if (!options.slack.isZero(tmpFrobNormCalc.doubleValue())) {

                    if (this.isDebug()) {
                        this.debug("Solution not accurate enough!");
                    }

                    this.resetX();
                    this.setState(State.INFEASIBLE);
                }

            } else {

                if (this.isDebug()) {
                    this.debug("SVD not solvable");
                }

                this.resetX();
                this.setState(State.INFEASIBLE);

                // throw new IllegalArgumentException("Couldn't solve this problem!");
            }
        }
    }

    @Override
    protected boolean initialise(final Result kickStart) {
        myLU.reset();
        mySingularValue.reset();
        return true;
    }

    @Override
    protected boolean needsAnotherIteration() {
        return this.countIterations() < 1;
    }

    @Override
    protected void performIteration() {

        Builder tmpBuilder = this.makeBuilder(false);

        this.performIteration(tmpBuilder);

        if (this.getState().isFeasible()) {

            this.extractSolution(tmpBuilder);

            this.setState(State.OPTIMAL);

        } else {

            tmpBuilder = this.makeBuilder(true);

            this.performIteration(tmpBuilder);

            if (this.getState().isFeasible()) {

                this.extractSolution(tmpBuilder);

                this.setState(State.OPTIMAL);

            } else {

                this.resetX();
                this.setState(State.INFEASIBLE);
            }
        }
    }

    @Override
    protected boolean validate() {

        boolean retVal = true;
        this.setState(State.VALID);

        try {

            final MatrixStore<Double> tmpQ = this.getQ();

            final Cholesky<Double> tmpCholesky = CholeskyDecomposition.makePrimitive();
            tmpCholesky.compute(tmpQ, true);

            if (!tmpCholesky.isSPD()) {
                // Not positive definite. Check if at least positive semidefinite.

                final Eigenvalue<Double> tmpEigenvalue = EigenvalueDecomposition.makePrimitive(true);
                tmpEigenvalue.compute(tmpQ, true);

                final MatrixStore<Double> tmpD = tmpEigenvalue.getD();

                final int tmpLength = (int) tmpD.countRows();
                for (int ij = 0; retVal && (ij < tmpLength); ij++) {
                    if (tmpD.doubleValue(ij, ij) < ZERO) {
                        retVal = false;
                        this.setState(State.INVALID);
                    }
                }

            }

            if (retVal) {
                // Q ok, check AE

                //                final MatrixStore<Double> tmpAE = this.getAE();
                //
                //                final LU<Double> tmpLU = LUDecomposition.makePrimitive();
                //                tmpLU.compute(tmpAE);
                //
                //                if (tmpLU.getRank() != tmpAE.getRowDim()) {
                //                    retVal = false;
                //                    this.setState(State.INVALID);
                //                }
            }

        } catch (final Exception ex) {

            retVal = false;
            this.setState(State.FAILED);
        }

        return retVal;
    }

}
