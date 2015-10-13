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

import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.optimisation.Optimisation;

/**
 * Solves optimisation problems of the form:
 * <p>
 * min 1/2 [X]<sup>T</sup>[Q][X] - [C]<sup>T</sup>[X]<br>
 * when [AE][X] == [BE]
 * </p>
 *
 * @author apete
 */
final class QPESolver extends ConstrainedSolver {

    private boolean myFeasible = false;

    private final PrimitiveDenseStore myIterationX;

    QPESolver(final ConvexSolver.Builder matrices, final Optimisation.Options solverOptions) {

        super(matrices, solverOptions);

        myIterationX = PrimitiveDenseStore.FACTORY.makeZero(this.countVariables(), 1L);
    }

    private boolean isFeasible() {

        boolean retVal = true;

        final MatrixStore<Double> tmpAEX = this.getAEX();
        final MatrixStore<Double> tmpBE = this.getBE();
        for (int i = 0; retVal && (i < tmpBE.countRows()); i++) {
            if (options.slack.isDifferent(tmpBE.doubleValue(i), tmpAEX.doubleValue(i))) {
                retVal = false;
            }
        }

        return retVal;
    }

    @Override
    protected final MatrixStore<Double> getIterationKKT() {
        final MatrixStore<Double> tmpIterationQ = this.getIterationQ();
        final MatrixStore<Double> tmpIterationA = this.getIterationA();
        return tmpIterationQ.builder().right(tmpIterationA.transpose()).below(tmpIterationA).build();
    }

    @Override
    protected final MatrixStore<Double> getIterationRHS() {
        final MatrixStore<Double> tmpIterationC = this.getIterationC();
        final MatrixStore<Double> tmpIterationB = this.getIterationB();
        return tmpIterationC.builder().below(tmpIterationB).build();
    }

    @Override
    protected boolean initialise(final Result kickStarter) {

        super.initialise(kickStarter);

        if (kickStarter != null) {
            this.fillX(kickStarter);
            if (!(myFeasible = this.isFeasible())) {
                this.resetX();
            }
        } else {
            this.resetX();
            myFeasible = false; // Could still be feasible, but doesn't matter...
        }

        return true;
    }

    @Override
    protected boolean needsAnotherIteration() {
        return this.countIterations() < 1;
    }

    @Override
    protected void performIteration() {

        final MatrixStore<Double> tmpIterQ = this.getIterationQ();
        final MatrixStore<Double> tmpIterC = this.getIterationC();
        final MatrixStore<Double> tmpIterA = this.getIterationA();
        final MatrixStore<Double> tmpIterB = this.getIterationB();

        boolean tmpSolvable = false;

        final PrimitiveDenseStore tmpIterX = myIterationX;
        final PrimitiveDenseStore tmpIterL = PrimitiveDenseStore.FACTORY.makeZero(tmpIterA.countRows(), 1L);

        if ((tmpIterA.countRows() < tmpIterA.countColumns()) && (tmpSolvable = myCholesky.isSolvable())) {
            // Q is SPD
            // Actual/normal optimisation problem

            final MatrixStore<Double> tmpInvQAT = myCholesky.solve(tmpIterA.transpose());
            // TODO Only 1 column change inbetween active set iterations (add or remove 1 column)

            // Negated Schur complement
            final MatrixStore<Double> tmpS = tmpIterA.multiply(tmpInvQAT);
            // TODO Symmetric, only need to calculate halv the Schur complement
            if (tmpSolvable = myLU.compute(tmpS)) {

                // tmpX temporarely used to store tmpInvQC
                final MatrixStore<Double> tmpInvQC = myCholesky.solve(tmpIterC, tmpIterX); //TODO Constant if C doesn't change

                myLU.solve(tmpIterA.multiply(tmpInvQC).subtract(tmpIterB), tmpIterL);
                myCholesky.solve(tmpIterC.subtract(tmpIterA.transpose().multiply(tmpIterL)), tmpIterX);
            }

        }

        if (!tmpSolvable && (tmpSolvable = myLU.compute(this.getIterationKKT()))) {
            // The above failed, but the KKT system is solvable
            // Try solving the full KKT system instaed

            final MatrixStore<Double> tmpXL = myLU.solve(this.getIterationRHS());
            tmpIterX.fillMatching(tmpXL.builder().rows(0, this.countVariables()).build());
            tmpIterL.fillMatching(tmpXL.builder().rows(this.countVariables(), (int) tmpXL.count()).build());
        }

        if (!tmpSolvable && this.isDebug()) {
            options.debug_appender.println("KKT system unsolvable!");
            options.debug_appender.printmtrx("KKT", this.getIterationKKT());
            options.debug_appender.printmtrx("RHS", this.getIterationRHS());
        }

        if (tmpSolvable) {

            this.setState(State.OPTIMAL);

            if (myFeasible) {

                this.getX().fillMatching(this.getX(), PrimitiveFunction.ADD, tmpIterX);
                this.getLE().fillMatching(tmpIterL);

            } else {

                this.getX().fillMatching(tmpIterX);
                this.getLE().fillMatching(tmpIterL);
            }

        } else {

            if (myFeasible) {

                this.setState(State.FEASIBLE);

            } else {

                this.setState(State.INFEASIBLE);
                this.resetX();
            }
        }
    }

    @Override
    final MatrixStore<Double> getIterationA() {
        return this.getAE();
    }

    @Override
    final MatrixStore<Double> getIterationB() {
        if (myFeasible) {
            return MatrixStore.PRIMITIVE.makeZero(this.countEqualityConstraints(), 1).get();
        } else {
            return this.getBE();
        }
    }

    @Override
    final MatrixStore<Double> getIterationC() {

        if (myFeasible) {

            final MatrixStore<Double> tmpQ = this.getQ();
            final MatrixStore<Double> tmpC = this.getC();

            final PhysicalStore<Double> tmpX = this.getX();

            return tmpC.subtract(tmpQ.multiply(tmpX));

        } else {

            return this.getC();
        }
    }

}
